//
//  PlayKitInteractor.swift
//  PlayKitOVPStarter
//
//  Created by Jean-Baptiste Louazel on 13/06/2019.
//  Copyright © 2019 Kaltura. All rights reserved.
//

import PlayKit
import StreamrootSDK

// Interactor between Streamroot and Kaltura PlayKit
// Binds Streamroot's events with player API
class PlayKitInteractor: DNAClientDelegate {
    private let player: Player
    
    public init(_ player: Player) {
        self.player = player
    }

    func updatePeakBitRate(_ bitRate: Double) {
        player.settings.network.preferredPeakBitRate = bitRate
    }
    
    func playbackTime() -> Double {
        return player.currentTime
    }
    
    func loadedTimeRanges() -> [NSValue] {
        return (player.loadedTimeRanges ?? [])
            .map { NSValue(timeRange: TimeRange(start: $0.start, duration: $0.duration)) }
    }
    
    func setBufferTarget(_ target: Double) {
        player.settings.preferredForwardBufferDuration = target
    }
    
    func bufferTarget() -> Double {
        return player.settings.preferredForwardBufferDuration
    }
}
