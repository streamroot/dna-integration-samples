//
//  ViewController.swift
//  SwiftDemo
//
//  Created by Maxime Bokobza on 02/03/17.
//  Copyright © 2017 Streamroot. All rights reserved.
//

import AVFoundation
import AVKit
import StreamrootSDK

class SwiftSampleViewController: AVPlayerViewController {
  var dnaClient: DNAClient?
  var assetUrlString: String?
  var drmToken: String?
  var assetLoader: AVAssetResourceLoader?
  let resourceLoadingRequestQueue = DispatchQueue(label: "ios.streamroot.sample.drm")
  
  override func viewDidDisappear(_ animated: Bool) {
    dnaClient?.stop()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    fetchAndStartStream()
  }
  
  func startDNA() {
    guard let assetUrlString = assetUrlString,
      let manifestUrl = URL(string: assetUrlString) else {
        print("Manifest not found")
      return
    }
    do {
      dnaClient = try DNAClient.builder()
        .dnaClientDelegate(self)
        // the streamroot key will default to the one in the Info.plist if not overridden here
        //.streamrootKey("demoswebsiteandpartners")
        .latency(20)
        .start(manifestUrl)
    } catch {
      print("\(error)")
    }
    
    guard let localPath = self.dnaClient?.manifestLocalURLPath,
      let localUrl = URL(string: localPath) else {
        print("Could not generate localPath, please check your network")
        return
    }
    let asset = AVURLAsset(url: localUrl)
    assetLoader = asset.resourceLoader
    assetLoader?.setDelegate(self, queue: DispatchQueue(label: "AVAssetResourceLoaderDelegate"))
    let playerItem = AVPlayerItem(asset: asset)
    if let bufferTarget = dnaClient?.bufferTarget {
      if #available(iOS 10.0, *) {
        playerItem.preferredForwardBufferDuration = bufferTarget
      }
    }
    
    player = AVPlayer(playerItem: playerItem)
    player?.play()
    view.setNeedsLayout()
    view.layoutIfNeeded()
    dnaClient?.displayStats(onView: contentOverlayView!)
  }
  
  func fetchAndStartStream() {
    let streamServer = URL(string: <#Stream provider url#>)
    let request = URLRequest(url: streamServer!)
    let task = URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
      guard let self = self else { return }
      if let data = data {
        print(data)
        let str = String(data: data, encoding: .utf8)
        guard let streamsInfo = str?.split(separator: "\n") else { return }
        
        self.drmToken = self.extractFrom(info: streamsInfo, at: 2, withKey: "DRM Token: ") ?? ""
        self.assetUrlString = self.extractFrom(info: streamsInfo, at: 3, withKey: "Main Asset URL: ") ?? ""
        
        print("TOKEN: \(String(describing: self.drmToken))")
        print("MANIFEST: \(String(describing: self.assetUrlString))")
        DispatchQueue.main.async {
          self.startDNA()
        }
        
      }
    }
    task.resume()
  }
  
  func extractFrom(info: [Substring], at index: Int, withKey key: String) -> String? {
    let snippet = info[index]
    if let range = snippet.range(of: key) {
      return  String(snippet[range.upperBound...])
    }
    return nil
  }
}

//MARK: - DNAClientDelegate

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
