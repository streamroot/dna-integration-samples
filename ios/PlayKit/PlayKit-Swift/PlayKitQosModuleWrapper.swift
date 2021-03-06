//
//  PlayKitQoSModule.swift
//  PlayKitOVPStarter
//
//  Created by Jean-Baptiste Louazel on 13/06/2019.
//  Copyright © 2019 Kaltura. All rights reserved.
//

import PlayKit
import StreamrootSDK

// Streamroot QoS module
// Binds player' events to raise precise data to DNAClient
class PlayKitQosModuleWrapper {
  let dnaQoSModule: QosModule = DNAQos.module(type: .custom)
    let player: Player

    public init(_ player: Player) {
        self.player = player
        player.addObserver(self, event: PlayerEvent.stateChanged, block: { event in
            switch (event.newState) {
            case .idle:
                self.dnaQoSModule.playerStateDidChange(PlaybackState.idle)
            case .ready:
                player.isPlaying
                    ? self.dnaQoSModule.playerStateDidChange(PlaybackState.playing)
                    : self.dnaQoSModule.playerStateDidChange(PlaybackState.paused)
            case .buffering:
                self.dnaQoSModule.playerStateDidChange(PlaybackState.buffering)
            case .ended:
                self.dnaQoSModule.playerStateDidChange(PlaybackState.ended)
            case .error:
                self.dnaQoSModule.playbackErrorOccurred()
            default:
                break
            }
        })

        player.addObserver(self, event: PlayerEvent.videoTrackChanged, block: { _ in self.dnaQoSModule.trackSwitchOccurred() })

        player.addObserver(self, event: PlayerEvent.seeked, block: { _ in self.dnaQoSModule.playerStateDidChange(PlaybackState.seeking) })
        
        player.addObserver(self, event: PlayerEvent.playbackStalled, block: { _ in self.dnaQoSModule.playbackErrorOccurred() })
        
        player.addObserver(self, event: PlayerEvent.ended, block: { _ in self.dnaQoSModule.playerStateDidChange(PlaybackState.ended) })
        
        player.addObserver(self, event: PlayerEvent.error, block: { _ in self.dnaQoSModule.playbackErrorOccurred() })
    }
    
    deinit {
        player.removeObserver(self, events: [PlayerEvent.stateChanged,
                                             PlayerEvent.videoTrackChanged,
                                             PlayerEvent.seeked,
                                             PlayerEvent.playbackStalled,
                                             PlayerEvent.ended,
                                             PlayerEvent.error])
    }
    
}
