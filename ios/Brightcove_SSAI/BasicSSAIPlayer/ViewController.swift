//
//  ViewController.swift
//  BasicSSAIPlayer
//
//  Created by Jeremy Blaker on 3/15/19.
//  Copyright © 2019 Brightcove, Inc. All rights reserved.
//

import UIKit
import BrightcovePlayerSDK
import BrightcoveSSAI
import StreamrootSDK

// ** Customize these values with your own account information **
struct Constants {
    static let AccountID = "5434391461001"
    static let PlaybackServicePolicyKey = "BCpkADawqM0T8lW3nMChuAbrcunBBHmh4YkNl5e6ZrKQwPiK_Y83RAOF4DP5tyBF_ONBVgrEjqW6fbV0nKRuHvjRU3E8jdT9WMTOXfJODoPML6NUDCYTwTHxtNlr5YdyGYaCPLhMUZ3Xu61L"
    static let VideoId = "5702141808001"
    static let AdConfigId = "0e0bbcd1-bba0-45bf-a986-1288e5f9fc85"
    static let SSAIDeliveryMethod = "application/x-mpegURL.boltSSAI"
}

class ViewController: UIViewController {
    @IBOutlet weak var videoContainerView: UIView!
    @IBOutlet weak var companionSlotContainerView: UIView!
    @IBOutlet weak var statViewContainer: UIView!
    
    fileprivate var dnaClient: DNAClient?
    fileprivate weak var currentPlayer: AVPlayer?
    
    private lazy var fairplayAuthProxy: BCOVFPSBrightcoveAuthProxy? = {
        guard let _authProxy = BCOVFPSBrightcoveAuthProxy(publisherId: nil, applicationId: nil) else {
            return nil
        }
        return _authProxy
    }()
    
    private lazy var playbackService: BCOVPlaybackService = {
        let factory = BCOVPlaybackServiceRequestFactory(accountId: Constants.AccountID, policyKey: Constants.PlaybackServicePolicyKey)
        return BCOVPlaybackService(requestFactory: factory)
    }()
    
    private lazy var playbackController: BCOVPlaybackController? = {
        guard let manager = BCOVPlayerSDKManager.shared(), let fairplayAuthProxy = fairplayAuthProxy else {
            return nil
        }
        
        // Create a companion slot.
        let companionSlot = BCOVSSAICompanionSlot(view: companionSlotContainerView, width: 300, height: 250)
        
        // In order to display an ad progress banner on the top of the view, we create this display container.  This object is also responsible for populating the companion slots.
        let adComponentDisplayContainer = BCOVSSAIAdComponentDisplayContainer(companionSlots: [companionSlot])
        
        let fairplaySessionProvider = manager.createFairPlaySessionProvider(with: fairplayAuthProxy, upstreamSessionProvider: nil)
        let ssaiSessionProvider = manager.createSSAISessionProvider(withUpstreamSessionProvider: fairplaySessionProvider)
        
        guard let _playbackController = manager.createPlaybackController(with: ssaiSessionProvider, viewStrategy: nil) else {
            return nil
        }
        
        // In order for the ad display container to receive ad information, we add it as a session consumer.
        _playbackController.add(adComponentDisplayContainer)
        
        _playbackController.delegate = self
        _playbackController.isAutoPlay = true
        
        self.playerView?.playbackController = _playbackController
        
        return _playbackController
    }()
    
    private lazy var playerView: BCOVPUIPlayerView? = {
        // Create PlayerUI views with normal VOD controls.
        let controlView = BCOVPUIBasicControlView.withVODLayout()
        guard let _playerView = BCOVPUIPlayerView(playbackController: nil, options: nil, controlsView: controlView) else {
            return nil
        }
        
        // Add to parent view
        self.videoContainerView.addSubview(_playerView)
        _playerView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            _playerView.topAnchor.constraint(equalTo: self.videoContainerView.topAnchor),
            _playerView.rightAnchor.constraint(equalTo: self.videoContainerView.rightAnchor),
            _playerView.leftAnchor.constraint(equalTo: self.videoContainerView.leftAnchor),
            _playerView.bottomAnchor.constraint(equalTo: self.videoContainerView.bottomAnchor)
            ])
        
        return _playerView
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        requestContentFromPlaybackService()
    }
    
    deinit {
        dnaClient?.stop()
    }
    
    private func requestContentFromPlaybackService() {
        let queryParameters = [kBCOVPlaybackServiceParamaterKeyAdConfigId: Constants.AdConfigId]
        
        playbackService.findVideo(withVideoID: Constants.VideoId, parameters: queryParameters) { [weak self] (video: BCOVVideo?, jsonResponse: [AnyHashable: Any]?, error: Error?) -> Void in
            
            guard let self = self else { return }
            
            if let error = error {
                print("ViewController Debug - Error retrieving video playlist: \(error.localizedDescription)")
                return
            }
            
            guard let video = video else {
                return
            }
            
            // Look for an HLS stream to setup streamroot DNA client
            let hlsSource = video.sources
                .compactMap { $0 as? BCOVSource }
                .filter { $0.deliveryMethod ==  "application/x-mpegURL.boltSSAI"}
                .first
            
            guard hlsSource != nil else {
                self.playbackController?.setVideos([video] as NSFastEnumeration)
                return
            }
            
            self.setupDnaWithSource(hlsSource!)
            
            guard let localManifest = self.dnaClient?.manifestLocalURLPath else {
                self.playbackController?.setVideos([video] as NSFastEnumeration)
                return
            }
            
            let dnaVideo = BCOVVideo(url: URL(string: localManifest)!,
                                     deliveryMethod: Constants.SSAIDeliveryMethod)
            self.playbackController?.setVideos([dnaVideo] as NSFastEnumeration)
            self.dnaClient?.displayStats(onView: self.videoContainerView)
        }
    }
}
    
// MARK: - BCOVPlaybackControllerDelegate
extension ViewController: BCOVPlaybackControllerDelegate {
    
    func playbackController(_ controller: BCOVPlaybackController!, didAdvanceTo session: BCOVPlaybackSession!) {
        print("ViewController Debug - Advanced to new session.")
        currentPlayer = session.player
        controller?.disableBufferOptimisation()
    }
}

// MARK: - BCOVPlaybackControllerAdsDelegate
extension ViewController: BCOVPlaybackControllerAdsDelegate {
    
    func playbackController(_ controller: BCOVPlaybackController!, playbackSession session: BCOVPlaybackSession!, didEnter adSequence: BCOVAdSequence!) {
        print("ViewController Debug - Entering ad sequence")
    }
    
    func playbackController(_ controller: BCOVPlaybackController!, playbackSession session: BCOVPlaybackSession!, didExitAdSequence adSequence: BCOVAdSequence!) {
        print("ViewController Debug - Exiting ad sequence")
    }
    
    func playbackController(_ controller: BCOVPlaybackController!, playbackSession session: BCOVPlaybackSession!, didEnter ad: BCOVAd!) {
        print("ViewController Debug - Entering ad")
    }
    
    func playbackController(_ controller: BCOVPlaybackController!, playbackSession session: BCOVPlaybackSession!, didExitAd ad: BCOVAd!) {
        print("ViewController Debug - Exiting ad")
    }
}

// MARK: - Streamroot
extension BCOVPlaybackController {
    
    /// Disable brightcove buffer optimisation to avoid
    /// collisition with streamroot Dynamic Buffer Level algorithm
    func disableBufferOptimisation() {
        var options = self.options
        options?[kBCOVBufferOptimizerMethodKey] = BCOVBufferOptimizerMethod.none
        self.options = options
    }
}

extension ViewController: DNAClientDelegate {
    /// Instanciate DNA Client from video source
    func setupDnaWithSource(_ source: BCOVSource) {
        do {
            dnaClient = try DNAClient.builder()
                .dnaClientDelegate(self)
                .latency(20)
                .start(source.url)
        } catch {
            print("\(error)")
        }
    }
    
    func playbackTime() -> Double {
        if let player = self.currentPlayer {
            return CMTimeGetSeconds(player.currentTime())
        }
        return 0
    }
    
    func loadedTimeRanges() -> [NSValue] {
        guard let player = self.currentPlayer else {
            return []
        }
        
        guard let playerItem = player.currentItem else {
            return []
        }
        
        return playerItem.loadedTimeRanges.map { (value) -> NSValue in
            NSValue(timeRange: TimeRange(range: value.timeRangeValue))
        }
    }
    
    func updatePeakBitRate(_ bitRate: Double) {
        currentPlayer?.currentItem?.preferredPeakBitRate = bitRate
    }
    
    func setBufferTarget(_ target: Double) {
        self.currentPlayer?.currentItem?.preferredForwardBufferDuration = target
    }
    
    func bufferTarget() -> Double {
        guard let item = self.currentPlayer?.currentItem else {
            return 0
        }
        return item.preferredForwardBufferDuration
    }
}


