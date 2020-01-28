//
//  MeshPlayerViewController.swift
//  TVMLSample
//
//  Created by Lamine Ndiaye on 28/01/2020.
//  Copyright © 2020 Lamine Ndiaye. All rights reserved.
//

import Foundation

import AVFoundation
import AVKit
import StreamrootSDK

class DNAPlayerViewController: AVPlayerViewController {
  var dnaClient: DNAClient?
  var displayStatView: Bool = false
  var contentId: String?
  var manifestUrlString: String?
  
  override func viewDidDisappear(_ animated: Bool) {
    dnaClient?.stop()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    
    guard let manifest = manifestUrlString,
      let manifestURL = URL(string: manifest) else {
        print("Invalid manifest URL \(String(describing: manifestUrlString))")
        return
    }
    
    var builder = DNAClient.builder().dnaClientDelegate(self)
    
    do {
      if let contentId = self.contentId {
        builder = try builder.contentId(contentId)
      }
      dnaClient = try builder
        // the streamroot key will default to the one in the Info.plist if not overridden here
        //.streamrootKey("demoswebsiteandpartners")
        .latency(30)
        .start(manifestURL)
    } catch let error {
      print("\(error)")
    }
    
    guard let localPath = self.dnaClient?.manifestLocalURLPath,
      let localUrl = URL(string: localPath) else {
        print("Could not generate localPath, please check your network")
        return
    }
    
    let playerItem = AVPlayerItem(asset: AVURLAsset(url: localUrl))
    player = AVPlayer(playerItem: playerItem)
    player?.play()
    view.setNeedsLayout()
    view.layoutIfNeeded()
    if displayStatView {
      dnaClient?.displayStats(onView: contentOverlayView!)
    }
  }
}

//MARK: - DNAClientDelegate

extension DNAPlayerViewController: DNAClientDelegate {
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
