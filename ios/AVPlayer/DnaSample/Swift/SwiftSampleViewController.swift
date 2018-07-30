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

class SwiftSampleViewController: AVPlayerViewController {
  
  var dnaClient: DNAClient?
  private let manifestUrl = URL(string: "https://demo-live.streamroot.io/index.m3u8")!
  
  override func viewDidDisappear(_ animated: Bool) {
    dnaClient?.stop()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    do {
      dnaClient = try DNAClient.builder()
        .dnaClientDelegate(self)
        .latency(30)
        .start(manifestUrl)
    } catch let error {
      print("\(error)")
    }
    
    guard let localPath = self.dnaClient?.manifestLocalURLPath,
      let url = URL(string: localPath) else  {
      print("Could not generate localPath, please check your network")
      return
    }
 
    let playerItem = AVPlayerItem(asset: AVURLAsset(url: url))
    if let bufferTarget = dnaClient?.bufferTarget {
      if #available(iOS 10.0, *) {
        playerItem.preferredForwardBufferDuration = bufferTarget
      }
    }
    
    player = AVPlayer(playerItem: playerItem)
    player?.play()
    player?.play()
    view.setNeedsLayout()
    view.layoutIfNeeded()
    dnaClient?.displayStats(onView: contentOverlayView!)
  }
}

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
    
    let timeRanges = player.currentItem!.loadedTimeRanges
    return timeRanges.map { (value) -> NSValue in
      NSValue(timeRange: TimeRange(range: value.timeRangeValue))
    }
  }
  
  func updatePeakBitRate(_ bitRate: Double) {
    player?.currentItem?.preferredPeakBitRate = bitRate
  }
}
