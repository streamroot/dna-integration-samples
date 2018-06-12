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
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        do {
            self.dnaClient = try DNAClient.builder()
                .dnaClientDelegate(self)
                .latency(30)
                .start(manifestUrl)
        } catch let error {
            print("\(error)")
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        dnaClient?.stop()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        do {
            self.dnaClient = try DNAClient.builder()
                .dnaClientDelegate(self)
                .latency(30)
                .start(manifestUrl)
        } catch let error {
            print("\(error)")
        }
        
        if let localPath = self.dnaClient?.manifestLocalURLPath {
            if let url = URL(string: localPath) {
                self.player = AVPlayer(url: url)
                self.player!.play()
                self.view.setNeedsLayout()
                self.view.layoutIfNeeded()
                dnaClient?.displayStats(onView: contentOverlayView!)
            }
        } else {
            print("Could not generate localPath, please check your network")
        }
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
        self.player?.currentItem?.preferredPeakBitRate = bitRate
    }
}
