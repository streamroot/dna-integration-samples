//
//  CompassSampleViewController.swift
//  DnaSample-iOS
//
//  Created by Lamine Ndiaye on 02/12/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//

import UIKit
import AVFoundation
import AVKit
import StreamrootSDK

class CompassSampleViewController: AVPlayerViewController {
  
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
          .product(.compass)
          .compassProperty("classic")
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

  extension CompassSampleViewController: DNAClientDelegate {
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
    
    func updatePeakBitRate(_ bitRate: Double) {}
  }
