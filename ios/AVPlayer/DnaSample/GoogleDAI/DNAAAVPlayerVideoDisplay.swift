//
//  TF1IMAAVPlayerVideoDisplay.swift
//  DnaSample-iOS
//
//  Created by Lamine Ndiaye on 18/03/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//

import AVKit
import Foundation
import GoogleInteractiveMediaAds
import StreamrootSDK

// MARK: - Configuration

struct DNAConfiguration {
  let streamrootKey: String?
  let contentId: String?
  let latency: Int
  let property: String?
  let backendHost: URL?
  
    init(streamrootKey: String? = nil,
       contentId: String? = nil,
       latency: Int = 0,
       property: String? = nil,
       backendHost: URL? = nil) {
    self.streamrootKey = streamrootKey
    self.contentId = contentId
    self.latency = latency
    self.property = property
    self.backendHost = backendHost
  }
}

// MARK: - Main

class DNAAVPlayerVideoDisplay: IMAAVPlayerVideoDisplay {
  private(set) var dnaClient: DNAClient?
  private(set) var dnaConfig: DNAConfiguration
  
  private var strKey: String? {
    return self.dnaConfig.streamrootKey
  }
  
  private var latency: Int {
    return self.dnaConfig.latency
  }
  
  init!(avPlayer player: AVPlayer!, dnaConfig config: DNAConfiguration) {
    self.dnaConfig = config
    super.init(avPlayer: player)
  }
  
  override func loadStream(_ streamURL: URL!, withSubtitles subtitles: [Any]!) {
    print(" Intercepted url \(String(describing: streamURL))")
    print("🔥 Intercepted \(Date().timeIntervalSince1970)")

    var fetchedUrl: URL!
    do {
      fetchedUrl = try startStreamURL(streamURL)
    } catch let error {
      print("\(error)")
      fetchedUrl = streamURL
    }
    super.loadStream(fetchedUrl, withSubtitles: subtitles)
  }
  
  deinit {
    dnaClient?.stop()
  }
}

// MARK: - DNA Builder

extension DNAAVPlayerVideoDisplay {
  func startStreamURL(_ url: URL) throws -> URL? {
    print("🔥 starting DNA builder \(Date().timeIntervalSince1970)")
    var builder = DNAClient.builder().dnaClientDelegate(self)
    if let strKey = strKey, !strKey.isEmpty {
        builder = builder.streamrootKey(strKey)
    }
    
    if self.latency > 0 {
      builder = builder.latency(self.latency)
    }
    
    if let backendHost = dnaConfig.backendHost {
      builder = builder.backendHost(backendHost)
    }
    
    if let contentId = dnaConfig.contentId, !contentId.isEmpty {
      builder = try builder.contentId(contentId)
    }
    
    if let property = dnaConfig.property, !property.isEmpty {
      builder = builder.property(property)
    }
    
    self.dnaClient = try builder.start(url)
    
    guard let localUrlString = dnaClient?.manifestLocalURLPath else {
      return url
    }
    
    guard let localUrl = URL(string: localUrlString) else {
      return url
    }
    
    return localUrl
  }
}

// MARK: - DNAClientDelegate

extension DNAAVPlayerVideoDisplay: DNAClientDelegate {
  func playbackTime() -> Double {
    if let player = self.player {
      return CMTimeGetSeconds(player.currentTime())
    }
    return 0
  }
  
  public func loadedTimeRanges() -> [NSValue] {
    guard let player = self.player else {
      return []
    }
    
    guard let playerItem = player.currentItem else {
      return []
    }
    
    let timeRanges = playerItem.loadedTimeRanges
    return timeRanges.map { (value) -> NSValue in
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
