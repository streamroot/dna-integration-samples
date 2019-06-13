//
//  ViewController+DNAClientDelegate.swift
//  PlayKit-Swift
//
//  Created by Jean-Baptiste Louazel on 12/06/2019.
//  Copyright © 2019 Kaltura. All rights reserved.
//

import PlayKit
import StreamrootSDK

extension ViewController: DNAClientDelegate {
    func playbackTime() -> Double {
        return player?.currentTime ?? 0.0
    }
    
    func loadedTimeRanges() -> [NSValue] {
        return (player?.loadedTimeRanges ?? [])
            .map { NSValue(timeRange: TimeRange(start: $0.start, duration: $0.duration)) }
    }
    
    func bufferTarget() -> Double {
        return 0.0
    }
    
    func setBufferTarget(_ target: Double) {
    }
    
    func updatePeakBitRate(_ bitRate: Double) {
        player?.settings.network.preferredPeakBitRate = bitRate
    }
}
