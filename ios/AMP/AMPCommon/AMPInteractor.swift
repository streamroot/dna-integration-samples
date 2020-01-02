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
  
  private let viaAV: DNAClientDelegate
  private let viaAMP: DNAClientDelegate
  
  public init(_ player: AmpPlayer) {
    self.player = player
    viaAV = AMPInteractorViaAV(player)
    viaAMP = AMPInteractorViaAMP(player)
  }
  
  func playbackTime() -> Double {
    NSLog("PlaybackTime = viaAV : \(viaAV.playbackTime()) / viaAMP : \(viaAMP.playbackTime())")
    return viaAV.playbackTime()
  }

  func loadedTimeRanges() -> [NSValue] {
    NSLog("TimeRanges = viaAV : \(viaAV.loadedTimeRanges()) / viaAMP : \(viaAMP.loadedTimeRanges())")
    return viaAV.loadedTimeRanges()
  }

  func bufferTarget() -> Double {
    NSLog("Get BufferTarget = viaAV : \(viaAV.bufferTarget!()) / viaAMP : \(viaAMP.bufferTarget!())")
    return viaAV.bufferTarget!()
  }

  func setBufferTarget(_ target: Double) {
    NSLog("Set BufferTarget = \(target)")
    viaAMP.setBufferTarget!(target)
    assert(viaAV.bufferTarget!() == target)
  }
    
  func updatePeakBitRate(_ bitRate: Double) {
    NSLog("Set PeakBitRate = \(bitRate)")
    viaAMP.updatePeakBitRate(bitRate)
    assert(player.player!.currentItem!.preferredPeakBitRate == bitRate)
  }
}

class AMPInteractorViaBase {
  let player: AmpPlayer
  
  public init(_ player: AmpPlayer) {
    self.player = player
  }
}

class AMPInteractorViaAV: AMPInteractorViaBase, DNAClientDelegate {
  func playbackTime() -> Double {
    if let player = self.player.player {
      return CMTimeGetSeconds(player.currentTime())
    }
    return 0
  }

  func loadedTimeRanges() -> [NSValue] {
    guard let player = self.player.player else {
      return []
    }

    let timeRanges = player.currentItem!.loadedTimeRanges
    return timeRanges.map { (value) -> NSValue in
      NSValue(timeRange: TimeRange(range: value.timeRangeValue))
    }
  }

  func bufferTarget() -> Double {
    if #available(iOS 10.0, tvOS 10.0, *) {
      return self.player.player?.currentItem?.preferredForwardBufferDuration ?? 0
    }
    return 0.0
  }

  func setBufferTarget(_ target: Double) {
    if #available(iOS 10.0, tvOS 10.0, *) {
      self.player.player?.currentItem?.preferredForwardBufferDuration = target
    }
  }
    
  func updatePeakBitRate(_ bitRate: Double) {
    player.player?.currentItem?.preferredPeakBitRate = bitRate
  }
}

class AMPInteractorViaAMP: AMPInteractorViaBase, DNAClientDelegate {
  func playbackTime() -> Double {
    return player.currentStreamTime
  }

  func loadedTimeRanges() -> [NSValue] {
    let buffer = self.player.currentBuffer
    if buffer > 0.0 {
      return [NSValue(timeRange: TimeRange(start: playbackTime(), duration: buffer))]
    } else {
      return []
    }
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
