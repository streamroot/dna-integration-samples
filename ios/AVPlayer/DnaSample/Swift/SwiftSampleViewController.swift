//
//  ViewController.swift
//  SwiftDemo
//
//  Created by Maxime Bokobza on 02/03/17.
//  Copyright © 2017 Streamroot. All rights reserved.
//

import AVFoundation
import AVKit
import StreamrootSDK

class SwiftSampleViewController: AVPlayerViewController {
  var dnaClient: DNAClient?
  private let manifestUrl = URL(string: "http://wowza-test.streamroot.io/liveOrigin/BBB-bl-1500/playlist.m3u8")!
  
  override func viewDidDisappear(_ animated: Bool) {
    dnaClient?.stop()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    do {
      dnaClient = try DNAClient.builder()
        .dnaClientDelegate(self)
        // the streamroot key will default to the one in the Info.plist if not overridden here
        //.streamrootKey("demoswebsiteandpartners")
        .latency(30)
        .useSentryHub(false)
        .start(manifestUrl)
    } catch let error {
      print("\(error)")
    }
    
    guard let localPath = self.dnaClient?.manifestLocalURLPath,
      let url = URL(string: localPath) else {
        print("Could not generate localPath, please check your network")
        return
    }
    
    let playerItem = AVPlayerItem(asset: AVURLAsset(url: url))
    player = AVPlayer(playerItem: playerItem)
    player?.play()
    view.setNeedsLayout()
    view.layoutIfNeeded()
    dnaClient?.displayStats(onView: contentOverlayView!)
  }
}

//MARK: - DNAClientDelegate

extension SwiftSampleViewController: DNAClientDelegate {
  func playbackTime() -> Double {
    if let player = self.player {
      return CMTimeGetSeconds(player.currentTime())
    }
    return 0
  }
  
  func loadedTimeRanges() -> [NSValue] {
    guard let player = self.player else {
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
    player?.currentItem?.preferredPeakBitRate = bitRate
  }
  
  public func bufferTarget() -> Double {
    if #available(iOS 10.0, tvOS 10.0, *) {
      return self.player?.currentItem?.preferredForwardBufferDuration ?? 0
    }
    return 0.0
  }
  
  public func setBufferTarget(_ target: Double) {
    if #available(iOS 10.0, tvOS 10.0, *) {
      self.player?.currentItem?.preferredForwardBufferDuration = target
    }
  }
}
