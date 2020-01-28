//
//  PlayerHandler.swift
//  TVMLSample
//
//  Created by Lamine Ndiaye on 27/01/2020.
//  Copyright © 2020 Lamine Ndiaye. All rights reserved.
//

import Foundation
import StreamrootSDK
import JavaScriptCore

// MARK: JavascriptCore Interface
@objc protocol PlayerJSExport: JSExport {
  func play(_: String)
  func stop()
  func playbackDidStall()
  func playbackError()
  func requestSeekToTime()
  func playbackRebuffering()
  func playbackPlaying()
  func playbackPaused()
  func playbackEnded()
}

// MARK: Player handler - Handle playback requets from the TVML
@objc class PlayerHandler: NSObject, PlayerJSExport {
   var dnaClient: DNAClient?
  var qosModuleWrapper = DNAQos.module(type: .custom)
  weak var delegate: PlayerJSBridge?
  var timer: DispatchSourceTimer?

  
  func play(_ url: String) {
    do {
      dnaClient = try DNAClient.builder()
        .dnaClientDelegate(self)
        // the streamroot key will default to the one in the Info.plist if not overridden here
        .streamrootKey("demoswebsiteandpartners")
        .latency(30)
        .logLevel(.error)
        .start(URL(string: url)!)
    } catch let error {
      print("\(error)")
    }
    delegate?.playLocalStream(dnaClient!.manifestLocalURLPath!)
  }
  
  func stop() {
    dnaClient?.stop()
  }
  
  func key() -> (NSCopying & NSObjectProtocol) {
    return "playerHandlerObj" as (NSCopying & NSObjectProtocol)
  }
}

// MARK: StatView
extension PlayerHandler {
    func displayStaView() {
      timer = DispatchSource.makeTimerSource(queue: DispatchQueue.main)
      timer?.schedule(deadline: DispatchTime.now() + 1, repeating: 1)
      timer?.setEventHandler {
        self.dnaClient?.stats({ stats in
          self.delegate?.displayStats(self.formattedStats(stats))
        })
      }
      timer?.resume()
    }
  
    private func formattedStats(_ stats: [String: Any]?) -> String{
      let cdn = stats?["cdn"] as? Int64 ?? 0
      let p2p = stats?["p2p"] as? Int64 ?? 0
      let upload = stats?["upload"] as? Int64 ?? 0
      let peers = stats?["peers"] as? Int ?? 0
      let status = stats?["status"] as? String ?? ""
      let offload = cdn + p2p > 0 ? Int(floor(Float(p2p * 100) / Float(cdn + p2p))) : 0
      return """
      \(status)
      CDN: \(cdn.humanReadableBytes())
      Offload: \(offload) %
      DNA: \(p2p.humanReadableBytes())
      Upload: \(upload.humanReadableBytes())"
      Peers: \(peers)
      """
    }

}

// MARK: DNAClient delegate
extension PlayerHandler: DNAClientDelegate {
  func updatePeakBitRate(_ bitRate: Double) {}
  
  func playbackTime() -> Double {
    return 0
  }
  
  func loadedTimeRanges() -> [NSValue] {
    let range = TimeRange(start: Double(0),
                          duration: Double(0))
    return [NSValue(timeRange: range)]
  }
}

// MARK: QoSModule helper
extension PlayerHandler {
  func playbackDidStall() {
    print(#function)
    qosModuleWrapper.playerStateDidChange(.buffering)
  }
  
  func playbackError() {
    print(#function)
    qosModuleWrapper.playbackErrorOccurred()
  }
  
  func requestSeekToTime() {
    print(#function)
    qosModuleWrapper.playerStateDidChange(.seeking)
  }
  
  func playbackRebuffering() {
    print(#function)
    qosModuleWrapper.playerStateDidChange(.buffering)
  }
  
  func playbackPlaying() {
    print(#function)
    qosModuleWrapper.playerStateDidChange(.playing)
    
  }
  
  func playbackPaused() {
    print(#function)
    qosModuleWrapper.playerStateDidChange(.paused)
  }
  
  func playbackEnded() {
    print(#function)
    qosModuleWrapper.playerStateDidChange(.ended)
  }
}

