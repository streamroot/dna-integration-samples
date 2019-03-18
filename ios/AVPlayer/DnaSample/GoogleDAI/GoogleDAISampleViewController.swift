//
//  ViewController.swift
//  SwiftDemo
//
//  Created by Maxime Bokobza on 02/03/17.
//  Copyright © 2017 Streamroot. All rights reserved.
//

import AVFoundation
import StreamrootSDK
import AVKit
import GoogleInteractiveMediaAds

class GoogleDAISampleViewController: AVPlayerViewController {
  
  // Google IMA component
  
  var adsLoader: IMAAdsLoader?
  var streamManager: IMAStreamManager?
  var videoDisplay: DNAAVPlayerVideoDisplay?
  var contentPlayhead: IMAAVPlayerContentPlayhead?
  
  var dnaUrlAsset: AVURLAsset?
  
  // View LifeCycle
  
  override func viewDidLoad() {
    super.viewDidLoad()
    player = AVPlayer()
    setUpAdsLoader()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    requestStream()
  }

}

// MARK: - Google DAI setup

extension GoogleDAISampleViewController {
  
  func setUpAdsLoader() {
    let settings = IMASettings()
    settings.enableDebugMode = true
    
    adsLoader = IMAAdsLoader(settings: settings)
    adsLoader!.delegate = self
  }
  
  func requestStream() {
    
    let adDisplayContainer = IMAAdDisplayContainer(adContainer: contentOverlayView!, companionSlots: nil)
    let config = DNAConfiguration(streamrootKey: "<#Streamroot key#>",
                                  contentId: "<#Content ID#>",
                                latency: 30,
                                property: "<#Property key#>",
                                displaysStats: true)
    videoDisplay = DNAAVPlayerVideoDisplay(avPlayer: player, dnaConfig: config)
    videoDisplay?.avPlayerVideoDisplayDelegate = self
    let request = IMALiveStreamRequest(assetKey: "<#Your google IMA Asset key#>", adDisplayContainer: adDisplayContainer, videoDisplay: videoDisplay)
    
    adsLoader!.requestStream(with: request)
  }
}

// MARK: - IMAAdsLoaderDelegate

extension GoogleDAISampleViewController: IMAAdsLoaderDelegate {
  
  func adsLoader(_ loader: IMAAdsLoader!, adsLoadedWith adsLoadedData: IMAAdsLoadedData!) {
    
    streamManager = adsLoadedData.streamManager
    streamManager?.delegate = self
    
    let adsRenderingSettings = IMAAdsRenderingSettings()
    adsRenderingSettings.webOpenerPresentingController = self
    
    streamManager!.initialize(with: adsRenderingSettings)
  }
  
  func adsLoader(_ loader: IMAAdsLoader!, failedWith adErrorData: IMAAdLoadingErrorData!) {
    NSLog("Error loading ads: \(String(describing: adErrorData.adError.message))")
    player?.play()
  }
}

// MARK: - IMAStreamManagerDelegate

extension GoogleDAISampleViewController: IMAStreamManagerDelegate {
  
  func streamManager(_ streamManager: IMAStreamManager!, didReceive event: IMAAdEvent!) {
    switch event.type {
    case .STARTED: print("Ad break started")
    case .AD_BREAK_ENDED: print("Ad break ended")
      player?.play()
    case .STREAM_STARTED:
      print("Stream Started !")
    default: break
    }
  }
  
  func streamManager(_ streamManager: IMAStreamManager!, didReceive error: IMAAdError!) {
    print("DAI Error :: \(String(describing: error.message))")
    player?.play()
  }
  
  func streamManager(_ streamManager: IMAStreamManager!,
                     adDidProgressToTime time: TimeInterval,
                     adDuration: TimeInterval,
                     adPosition: Int,
                     totalAds: Int,
                     adBreakDuration: TimeInterval) {
    print("Ad progresion: \(time)")
  }
}

// MARK: - IMAAVPlayerVideoDisplayDelegate

extension GoogleDAISampleViewController: IMAAVPlayerVideoDisplayDelegate {
  func avPlayerVideoDisplay(_ avPlayerVideoDisplay: IMAAVPlayerVideoDisplay!,
                            willLoadStreamAsset avUrlAsset: AVURLAsset!) {
    self.videoDisplay?.dnaClient?.displayStats(onView: contentOverlayView!)
  }
}


