// ===================================================================================================
// Copyright (C) 2017 Kaltura Inc.
//
// Licensed under the AGPLv3 license, unless a different license for a 
// particular library is specified in the applicable library path.
//
// You may obtain a copy of the License at
// https://www.gnu.org/licenses/agpl-3.0.html
// ===================================================================================================

import Foundation
import KalturaNetKit
import PlayKitUtils
import PlayKit

/// class `BaseOTTAnalyticsPlugin` is a base plugin object used for OTT analytics plugin subclasses
public class BaseOTTAnalyticsPlugin: BasePlugin, OTTAnalyticsPluginProtocol, AppStateObservable {
    
    /// indicates whether we played for the first time or not.
    public var isFirstPlay: Bool = true
    var intervalOn: Bool = false
    var timer: Timer?
    var interval: TimeInterval = 30
    var fileId: String?
    var lastPosition: Int32 = 0
    var periodicObserverUUID: UUID?
    var mediaId: String?
    var stopSentByDestroy: Bool = false

    /************************************************************/
    // MARK: - PKPlugin
    /************************************************************/
    
    public required init(player: Player, pluginConfig: Any?, messageBus: MessageBus) throws {
        try super.init(player: player, pluginConfig: pluginConfig, messageBus: messageBus)
        AppStateSubject.shared.add(observer: self)
        self.periodicObserverUUID = self.player?.addPeriodicObserver(interval: 1.0, observeOn: DispatchQueue.main, using: { (time) in
            self.lastPosition = time.toInt32()
        })
        self.registerEvents()
    }

    public override func onUpdateMedia(mediaConfig: MediaConfig) {
        super.onUpdateMedia(mediaConfig: mediaConfig)
        self.intervalOn = false
        self.isFirstPlay = true
        self.timer?.invalidate()
    }
    
    public override func destroy() {
        self.unregisterEvents()
        if let periodicObserverUUID = self.periodicObserverUUID {
            self.player?.removePeriodicObserver(periodicObserverUUID)
        }
        
        // only send stop event if content started playing already & content is not ended
        if !self.isFirstPlay && self.player?.currentState != PlayerState.ended {
            self.sendAnalyticsEvent(ofType: .stop)
            self.stopSentByDestroy = true
        }
        self.timer?.invalidate()
        AppStateSubject.shared.remove(observer: self)
        super.destroy()
    }
    
    /************************************************************/
    // MARK: - App State Handling
    /************************************************************/
    
    public var observations: Set<NotificationObservation> {
        return [
            NotificationObservation(name: UIApplication.willTerminateNotification) { [weak self] in
                guard let self = self else { return }
                
                PKLog.debug("plugin: \(self) will terminate event received, sending analytics stop event")
                self.destroy()
            }
        ]
    }
    
    /************************************************************/
    // MARK: - AnalyticsPluginProtocol
    /************************************************************/
    
    /// default events to register
    public var playerEventsToRegister: [PlayerEvent.Type] {
        return [
            PlayerEvent.ended,
            PlayerEvent.error,
            PlayerEvent.pause,
            PlayerEvent.stopped,
            PlayerEvent.loadedMetadata,
            PlayerEvent.playing,
            PlayerEvent.sourceSelected
        ]
    }
    
    public func registerEvents() {
        PKLog.debug("plugin \(type(of:self)) register to all player events")
        
        self.playerEventsToRegister.forEach { event in
            PKLog.debug("Register event: \(event.self)")
            
            switch event {
            case let e where e.self == PlayerEvent.ended:
                self.messageBus?.addObserver(self, events: [e.self]) { [weak self] event in
                    guard let self = self else { return }
                    self.timer?.invalidate()
                    self.lastPosition = self.player?.currentTime.toInt32() ?? self.lastPosition
                    self.sendAnalyticsEvent(ofType: .finish)
                }
            case let e where e.self == PlayerEvent.error:
                self.messageBus?.addObserver(self, events: [e.self]) { [weak self] event in
                    guard let self = self else { return }
                    self.sendAnalyticsEvent(ofType: .error)
                }
            case let e where e.self == PlayerEvent.pause:
                self.messageBus?.addObserver(self, events: [e.self]) { [weak self] event in
                    guard let self = self else { return }
                    // invalidate timer when receiving pause event only after first play
                    // and set intervalOn to false in order to start timer again on play event.
                    if !self.isFirstPlay {
                        self.timer?.invalidate()
                        self.intervalOn = false
                    }
                    self.sendAnalyticsEvent(ofType: .pause)
                }
            case let e where e.self == PlayerEvent.stopped:
                self.messageBus?.addObserver(self, events: [e.self]) { [weak self] event in
                    guard let self = self else { return }
                    self.cancelTimer()
                    // If destory was already called, it may have sent stop. Don't send it again.
                    if !self.stopSentByDestroy {
                        self.sendAnalyticsEvent(ofType: .stop)
                    }
                }
            case let e where e.self == PlayerEvent.loadedMetadata:
                self.messageBus?.addObserver(self, events: [e.self]) { [weak self] event in
                    guard let self = self else { return }
                    self.sendAnalyticsEvent(ofType: .load)
                }
            case let e where e.self == PlayerEvent.playing:
                self.messageBus?.addObserver(self, events: [e.self]) { [weak self] event in
                    guard let self = self else { return }
                    
                    if !self.intervalOn {
                        self.createTimer()
                        self.intervalOn = true
                    }
                    
                    if self.isFirstPlay {
                        self.isFirstPlay = false
                        self.sendAnalyticsEvent(ofType: .first_play);
                    } else {
                        self.sendAnalyticsEvent(ofType: .play);
                    }
                }
            case let e where e.self == PlayerEvent.sourceSelected:
                self.messageBus?.addObserver(self, events: [e.self]) { [weak self] event in
                    guard let self = self else { return }
                    guard let mediaSource = event.mediaSource else { return }
                    self.fileId = mediaSource.id
                    self.mediaId = self.player?.mediaEntry?.id
                    self.lastPosition = 0
                }
            default: assertionFailure("plugin \(type(of:self)) all events must be handled")
            }
        }
    }
    
    public func unregisterEvents() {
        self.messageBus?.removeObserver(self, events: playerEventsToRegister)
    }
    
    /************************************************************/
    // MARK: - OTTAnalyticsPluginProtocol
    /************************************************************/
    
    func sendAnalyticsEvent(ofType type: OTTAnalyticsEventType) {
        PKLog.debug("Send analytics event of type: \(type)")
        // post to messageBus
        let event = OttEvent.Report(message: "\(type) event")
        self.messageBus?.post(event)
        
        if let request = self.buildRequest(ofType: type) {
            self.send(request: request)
        }
    }
    
    func buildRequest(ofType type: OTTAnalyticsEventType) -> Request? {
        fatalError("abstract method should be implemented in subclasses only")
    }
    
    func send(request: Request) {
        USRExecutor.shared.send(request: request)
    }
    
    /************************************************************/
    // MARK: - Internal
    /************************************************************/
    
    func reportConcurrencyEvent() {
        self.messageBus?.post(OttEvent.Concurrency())
    }
    
    func reportBookmarkErrorEvent(code: String?, message: String?) {
        self.messageBus?.post(OttEvent.BookmarkError(code: code, message: message))
    }
}

/************************************************************/
// MARK: - Private
/************************************************************/

extension BaseOTTAnalyticsPlugin {
    
    fileprivate func createTimer() {
        if let t = self.timer {
            t.invalidate()
            self.timer = nil
        }
        
        self.timer = PKTimer.every(self.interval) { [weak self] _ in
            guard let self = self else { return }
            PKLog.debug("timerHit")
            self.sendProgressEvent()
        }
    }
    
    fileprivate func cancelTimer() {
        if let t = self.timer {
            t.invalidate()
            self.timer = nil
            self.intervalOn = false
        }
    }
    
    fileprivate func sendProgressEvent() {
        guard let player = self.player else { return }
        self.sendAnalyticsEvent(ofType: .hit);
        
        let progress = Float(player.currentTime) / Float(player.duration)
        PKLog.debug("Progress is \(progress)")
    }
}
