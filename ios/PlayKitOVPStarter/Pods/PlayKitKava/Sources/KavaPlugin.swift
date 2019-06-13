// ===================================================================================================
// Copyright (C) 2017 Kaltura Inc.
//
// Licensed under the AGPLv3 license, unless a different license for a
// particular library is specified in the applicable library path.
//
// You may obtain a copy of the License at
// https://www.gnu.org/licenses/agpl-3.0.html
// ===================================================================================================

import PlayKit
import KalturaNetKit
import SwiftyJSON

/************************************************************/
// MARK: - Playback Points Array
/************************************************************/

/// playbackPoints Array represents points in %, that show how much was reached from playback
let playbackPoints: [KavaPlugin.KavaEventType] = [KavaPlugin.KavaEventType.playReached25Percent, KavaPlugin.KavaEventType.playReached50Percent, KavaPlugin.KavaEventType.playReached75Percent, KavaPlugin.KavaEventType.playReached100Percent]

/************************************************************/
// MARK: - KavaPlugin
/************************************************************/

/// This class represents Kaltura real time analytics for live and on-demand video.
@objc public class KavaPlugin: BasePlugin {
    
    private static let userAgent = "\(Bundle.main.bundleIdentifier ?? "") \(PlayKitManager.clientTag) (\(UIDevice.current.model); CPU OS \(ProcessInfo().operatingSystemVersion.majorVersion)_\(ProcessInfo().operatingSystemVersion.minorVersion)_\(ProcessInfo().operatingSystemVersion.patchVersion) like Mac OS X; \(Locale.current.identifier.lowercased()))"
    
    let viewInterval: TimeInterval = 10
    let timerInterval: TimeInterval = 1
    let maxViewIdleInterval: TimeInterval = 30
    
    /// Kava event types
    enum KavaEventType : Int {
        /// Media was loaded
        case impression = 1
        /// Play event was triggred
        case playRequest = 2
        /// Playing event was triggred
        case play = 3
        /// Resume event was triggred
        case resume = 4
        /// player reached 25 percent
        case playReached25Percent = 11
        /// player reached 50 percent
        case playReached50Percent = 12
        /// player reached 75 percent
        case playReached75Percent = 13
        /// player reached 100 percent
        case playReached100Percent = 14
        /// Pause event was triggred
        case pause = 33
        /// Replay event was triggred
        case replay = 34
        /// Seeking event was triggred
        case seek = 35
        /// Captions event (text track was changed) was triggred
        case captions = 38
        /// Source Selected (media was changed) event was triggred
        case sourceSelected = 39
        /// Sent when audio track changed
        case audioSelected = 42
        /// The video track has changed to a different bitrate (indicated bitrate).
        case flavorSwitched = 43
        /// Error event was triggred
        case error = 98
        /// Sent every 10 seconds of active playback.
        case view = 99
    }
    
    var config: KavaPluginConfig
    var sentPlaybackPoints: [KavaEventType : Bool] = KavaPlugin.cleanPlaybackPoints()
    var boundaryObservationToken: UUID?
    var viewTimer: Timer?
    var bufferingStartTime: Date?
    var kavaData = KavaPluginData()
    var currentViewTime: TimeInterval = 0
    var lastEventSentTime: TimeInterval = 0
    var joinTimeStart: TimeInterval = 0
    var isViewEventsEnabled = true
    var declaredMediaType: MediaType = .unknown
    
    /// A sequence number which describe the order of events in a viewing session.
    private var eventIndex = 1
    
    /************************************************************/
    // MARK: PKPlugin
    /************************************************************/
    
    public override class var pluginName: String {
        return "KavaPlugin"
    }
    
    public required init(player: Player, pluginConfig: Any?, messageBus: MessageBus) throws {
        guard let config = pluginConfig as? KavaPluginConfig else {
            PKLog.error("missing plugin config or wrong plugin class type")
            throw PKPluginError.missingPluginConfig(pluginName: KavaPlugin.pluginName)
        }
        self.config = config
        try super.init(player: player, pluginConfig: self.config, messageBus: messageBus)
        self.registerEvents()
    }
    
    public override func onUpdateMedia(mediaConfig: MediaConfig) {
        PKLog.debug("onUpdateMedia: \(String(describing: mediaConfig))")
        super.onUpdateMedia(mediaConfig: mediaConfig)
        self.resetPlayerFlags()
        self.unregisterFromBoundaries()
        self.stopViewTimer()
        
        self.declaredMediaType = mediaConfig.mediaEntry.mediaType
    }
    
    public override func onUpdateConfig(pluginConfig: Any) {
        super.onUpdateConfig(pluginConfig: pluginConfig)
        
        guard let config = pluginConfig as? KavaPluginConfig else {
            PKLog.error("plugin config is wrong")
            return
        }
        
        PKLog.debug("new config::\(String(describing: config))")
        self.config = config
    }
    
    public override func destroy() {
        self.unregisterEvents()
        self.unregisterFromBoundaries()
        self.stopViewTimer()
        super.destroy()
    }
    
    static func cleanPlaybackPoints() -> [KavaEventType : Bool] {
        return playbackPoints.reduce([KavaEventType : Bool]()) { (dict, point) -> [KavaEventType : Bool] in
            var dict = dict
            dict[point] = false
            return dict
        }
    }
    
    func registerToBoundaries() {
        if let player = player, boundaryObservationToken == nil, !player.isLive() {
            let boundaryFactory = PKBoundaryFactory(duration: player.duration)
            let boundaries = playbackPoints.map({ boundaryFactory.percentageTimeBoundary(boundary: convertToPercentage(type: $0)) })
            boundaryObservationToken = player.addBoundaryObserver(boundaries: boundaries, observeOn: nil) { [weak self] (time, percentage) in
                guard let self = self else { return }
                self.sendPercentageReachedEvent(percentage: Int(percentage * 100))
            }
        }
    }
    
    func unregisterFromBoundaries() {
        if let _ = boundaryObservationToken {
            player?.removeBoundaryObserver(boundaryObservationToken!)
            boundaryObservationToken = nil
        }
    }
    
    func setupViewTimer() {
        if viewTimer == nil {
            viewTimer = Timer.scheduledTimer(timeInterval: self.timerInterval, target: self, selector: #selector(timerTick), userInfo: nil, repeats: true)
        }
    }
    
    func stopViewTimer() {
        self.config.sessionStartTime = nil
        viewTimer?.invalidate()
        viewTimer = nil
    }
    
    @objc func timerTick() {
        self.currentViewTime += self.timerInterval
        // handle bitrate
        self.kavaData.bitrateCount += 1
        self.kavaData.bitrateSum += kavaData.indicatedBitrate
        // report view when view interval is reached
        if self.currentViewTime >= self.viewInterval {
            self.currentViewTime -= self.viewInterval
            self.reportView()
        }
    }
    
    func reportView() {
        // If timer is nil, no reason to report.
        guard self.viewTimer != nil, isViewEventsEnabled else { return }
        
        if let _ = bufferingStartTime {
            self.kavaData.totalBufferingInCurrentInterval += -bufferingStartTime!.timeIntervalSinceNow
            bufferingStartTime = Date()
        }
        
        self.kavaData.totalBuffering += self.kavaData.totalBufferingInCurrentInterval
        self.kavaData.playTimeInCurrentInterval = self.viewInterval - self.kavaData.totalBufferingInCurrentInterval
        
        self.sendAnalyticsEvent(event: .view)
    }
    
    func resetPlayerFlags() {
        self.kavaData.isMediaLoaded = false
        self.sentPlaybackPoints = KavaPlugin.cleanPlaybackPoints()
        self.kavaData.isFirstPlay = true
        self.kavaData.errorCode = -1
        self.bufferingStartTime = nil
        self.kavaData.totalBuffering = 0
        self.kavaData.totalBufferingInCurrentInterval = 0
        self.eventIndex = 1
        self.kavaData.indicatedBitrate = 0
    }
    
    func sendPercentageReachedEvent(percentage: Int) {
        var eventsToSend: [KavaEventType] = []
        for item in sentPlaybackPoints {
            if item.value == false && convertToPercentage(type: item.key) <= percentage {
                eventsToSend.append(item.key)
                sentPlaybackPoints[item.key] = true
            }
        }
        
        for item in eventsToSend.sorted(by: { (item1, item2) -> Bool in return item1.rawValue < item2.rawValue }) {
            sendAnalyticsEvent(event: item)
        }
    }
    
    func sendAnalyticsEvent(event: KavaEventType) {
        guard let player = self.player else {
            PKLog.warning("Player/ MediaEntry is nil")
            return
        }
        
        PKLog.debug("Action: \(event)")
        
        // Send event to messageBus
        let eventType = KavaEvent.Report(message: "Kava event: \(event) (\(event.rawValue))")
        self.messageBus?.post(eventType)
        
        let currentTime = Date().timeIntervalSince1970
        
        if self.isViewEventsEnabled {
            if currentTime - self.lastEventSentTime > self.maxViewIdleInterval && self.lastEventSentTime != 0 {
                self.eventIndex = 1
                self.kavaData.totalPlayTime = 0
                self.kavaData.totalBuffering = 0
                self.kavaData.bitrateSum = 0
                self.kavaData.bitrateCount = 0
            }
        }
        
        self.lastEventSentTime = currentTime
        
        // Update total play time
        if player.currentTime > 0 && event == .view {
            self.kavaData.totalPlayTime += (self.kavaData.playTimeInCurrentInterval > 0 ? self.kavaData.playTimeInCurrentInterval : self.viewInterval)
            self.kavaData.playTimeInCurrentInterval = 0
        }
        
        self.kavaData.mediaDuration = player.duration
        
        // Handle media current time. For live, send position against real time in "-" (minus values)
        let mediaCurrentTime: TimeInterval
        if player.isLive() {
            let currentTime = player.currentTime - player.duration
            mediaCurrentTime = currentTime <= 0 ? currentTime : 0
        } else {
            mediaCurrentTime = player.currentTime
        }
        self.kavaData.mediaCurrentTime = mediaCurrentTime
        
        guard let builder: KalturaRequestBuilder = KavaHelper.builder(config: self.config, 
                                                                      mediaType: declaredMediaType,
                                                                      eventType: event.rawValue,
                                                                      eventIndex: self.eventIndex,
                                                                      kavaData: self.kavaData,
                                                                      player: player)
            else {
                PKLog.warning("KalturaRequestBuilder is nil")
                return
        }
        
        builder.add(headerKey: "User-Agent", headerValue: KavaPlugin.userAgent)
        builder.set { (response: Response) in
            PKLog.debug("Response: \(String(describing: response))")
            
            guard let data = response.data else { return }
            
            if JSONSerialization.isValidJSONObject(data) {
                // Response returned as JSON
                guard let responseJson = JSON(data).dictionary else { return }
                
                if (self.config.sessionStartTime == nil) {
                    if let sessionStartTime = responseJson["time"]?.number {
                        self.config.sessionStartTime = sessionStartTime.stringValue
                    }
                }
                
                if let viewEventsEnabled = responseJson["viewEventsEnabled"]?.bool {
                    self.isViewEventsEnabled = viewEventsEnabled
                }
            } else {
                // Response returned as String (Old format)
                if (self.config.sessionStartTime == nil) {
                    if let sessionStartTime = data as? String {
                        self.config.sessionStartTime = sessionStartTime
                    }
                }
            }
        }
        
        print("Send Kava Event: \(builder.urlParams!)")
        USRExecutor.shared.send(request: builder.build())
        
        self.eventIndex += 1
        self.kavaData.totalBufferingInCurrentInterval = TimeInterval()
    }
}

/* ***********************************************************/
// MARK: - Extensions
/* ***********************************************************/

extension PKEvent {
    /// Report Value, PKEvent Data Accessor
    @objc public var kavaMessage: String? {
        return self.data?[KavaEvent.messageKey] as? String
    }
}

extension KavaPlugin {
    func convertToPercentage(type: KavaEventType) -> Int {
        switch type {
        case .playReached25Percent:
            return 25
        case .playReached50Percent:
            return 50
        case .playReached75Percent:
            return 75
        case .playReached100Percent:
            return 100
        default:
            return 0
        }
    }
}
