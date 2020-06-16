//
//  GoogleDAISampleViewController.swift
//  DnaSample-iOS
//
//  Created by Lamine Ndiaye on 18/03/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//
import AVFoundation
import AVKit
import GoogleInteractiveMediaAds
import StreamrootSDK

class GoogleDAISampleViewController: AVPlayerViewController {
  var adsLoader: IMAAdsLoader?
  var streamManager: IMAStreamManager?
  var videoDisplay: DNAAVPlayerVideoDisplay?
  var contentPlayhead: IMAAVPlayerContentPlayhead?
  
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

extension GoogleDAISampleViewController {
  func setUpAdsLoader() {
    let settings = IMASettings()
    settings.enableDebugMode = true
    
    adsLoader = IMAAdsLoader(settings: settings)
    adsLoader!.delegate = self
  }
  
  func requestStream() {
//    let adDisplayContainer = IMAAdDisplayContainer(adContainer: contentOverlayView!, companionSlots: nil)
//    // the streamroot key will default to the one in the Info.plist if not overridden here
//    let config = DNAConfiguration(
//        //streamrootKey: "demoswebsiteandpartners",
//        contentId: "GoogleDAI_TEST",
//        latency: 30,
//        property:  "SSAI"
//    )
//    videoDisplay = DNAAVPlayerVideoDisplay(avPlayer: player, dnaConfig: config)
//    videoDisplay?.delegate = self
//    let request = IMALiveStreamRequest(assetKey: "sN_IYUG8STe1ZzhIIE_ksA",
//                                       adDisplayContainer: adDisplayContainer,
//                                       videoDisplay: videoDisplay)
//    adsLoader!.requestStream(with: request)
  }
}

extension GoogleDAISampleViewController: IMAAdsLoaderDelegate {
  func adsLoader(_ loader: IMAAdsLoader!, adsLoadedWith adsLoadedData: IMAAdsLoadedData!) {
    self.streamManager = adsLoadedData.streamManager
    self.streamManager?.delegate = self
    
    let adsRenderingSettings = IMAAdsRenderingSettings()
    adsRenderingSettings.webOpenerPresentingController = self
    
    streamManager!.initialize(with: adsRenderingSettings)
  }
  
  func adsLoader(_ loader: IMAAdsLoader!, failedWith adErrorData: IMAAdLoadingErrorData!) {
    NSLog("Error loading ads: \(String(describing: adErrorData.adError.message))")
    player?.play()
  }
}

extension GoogleDAISampleViewController: IMAStreamManagerDelegate {
  func streamManager(_ streamManager: IMAStreamManager!, didReceive event: IMAAdEvent!) {
    switch event.type {
    case .STARTED: print("ℹ️ DAI didStartSlot")
    case .COMPLETE: print("ℹ️ DAI didFinishAd")
    case .AD_BREAK_ENDED: print("ℹ️ DAI didFinishSlot")
      player?.play()
    case .STREAM_STARTED:
      print("ℹ️ DAI Stream Started !")
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
    print("adDidProgressToTime: \(time)")
  }
}

extension GoogleDAISampleViewController: IMAAVPlayerVideoDisplayDelegate {
  func avPlayerVideoDisplay(_ avPlayerVideoDisplay: IMAAVPlayerVideoDisplay!,
                            willLoadStreamAsset avUrlAsset: AVURLAsset!) {
    self.videoDisplay?.dnaClient?.displayStats(onView: contentOverlayView!)
  }
}
