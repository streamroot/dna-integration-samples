//
//  SwiftPluginSampleViewController.swift
//  DnaSample
//
//  Created by Lamine Ndiaye on 05/11/2018.
//  Copyright © 2018 Streamroot. All rights reserved.
//

import AVFoundation
import AVPlayerDNAPlugin
import AVKit

class SwiftPluginSampleViewController: AVPlayerViewController {

  var strPlugin: StreamrootPlugin?
  private let manifestUrl = URL(string: "http://wowza-test.streamroot.io/liveOrigin/BBB-bl-1500/playlist.m3u8")!
  
  override func viewDidDisappear(_ animated: Bool) {
    strPlugin?.stop()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    let options = StreamrootOptions()
    options.latency = 30
    strPlugin = StreamrootAVPlayerPlugin(manifestUrl: manifestUrl, options: options)
    do {
      if let localPath = try strPlugin?.start() {
        player = AVPlayer(url: localPath)
        try strPlugin?.linkPlayer(player)
        player?.play()
        strPlugin?.dnaClient?.displayStats(onView: contentOverlayView!)
      }
    } catch let error {
      print(error.localizedDescription)
    }
  }
}
