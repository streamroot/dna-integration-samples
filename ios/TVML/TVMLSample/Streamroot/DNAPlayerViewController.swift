//
//  MeshPlayerViewController.swift
//  TVMLSample
//
//  Created by Lamine Ndiaye on 28/01/2020.
//  Copyright © 2020 Lamine Ndiaye. All rights reserved.
//

import Foundation

import AVFoundation
import AVKit
import AVPlayerDNAPlugin

class DNAPlayerViewController: AVPlayerViewController {
  var strPlugin: StreamrootPlugin?
  var displayStatView: Bool = false
  var contentId: String?
  var manifestUrlString: String?
  var latency = 30
  
  override func viewDidDisappear(_ animated: Bool) {
    strPlugin?.stop()
  }
  
  override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    
    guard let manifest = manifestUrlString,
      let manifestURL = URL(string: manifest) else {
        print("Invalid manifest URL \(String(describing: manifestUrlString))")
        return
    }
    
    let config = DNAConfig(/*streamrootKey: "demoswebsiteandpartners"*/)
    // the streamroot key will default to the one in the Info.plist if not overridden here
    
    // set the latency
    config.latency = latency
    strPlugin = AVPlayerDNAPlugin(manifestUrl: manifestURL, config: config)
    do {
      if let localPath = try strPlugin?.start() {
        player = AVPlayer(url: localPath)
        try strPlugin?.linkPlayer(player)
        player?.play()
        if displayStatView {
          strPlugin?.dnaClient?.displayStats(onView: contentOverlayView!)
        }
      }
    } catch let error {
      print(error.localizedDescription)
    }
  }
}
