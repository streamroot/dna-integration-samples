//
//  AMPInteractor.swift
//  AMPSample
//
//  Created by Boris BORGOBELLO on 10/12/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//

import AVFoundation
import StreamrootSDK
#if os(iOS)
import AmpCore
#elseif os(tvOS)
import AmpCoreTV
#endif

class AMPInteractor: DNAClientDelegate {
  private let player: AmpPlayer
  
  init(_ player: AmpPlayer) {
    self.player = player
  }
  
  func playbackTime() -> Double {
    return player.currentStreamTime
  }

  func loadedTimeRanges() -> [NSValue] {
    let buffer = self.player.currentBuffer
    return buffer > 0.0 ? [NSValue(timeRange: TimeRange(start: playbackTime(), duration: buffer))] : []
  }

  func bufferTarget() -> Double {
    return player.totalBuffer
  }

  func setBufferTarget(_ target: Double) {
    player.fowardBufferDuration = target
  }
    
  func updatePeakBitRate(_ bitRate: Double) {
    player.peakBitRate = bitRate
  }
}
