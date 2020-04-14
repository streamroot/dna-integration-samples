//
//  ViewController+Extensions.swift
//  AMPSample
//
//  Created by Boris BORGOBELLO on 10/12/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//

import Foundation
import StreamrootSDK
#if os(iOS)
import AmpCore
#elseif os(tvOS)
import AmpCoreTV
import AVKit
#endif

extension ViewController : PlayerEventObserver {
  func setup() {
    #if os(iOS)
    self.ampPlayer = AmpPlayer(parentView: self.view)
    #elseif os(tvOS)
    self.ampPlayer = AmpPlayer(parentViewController: self, playerController: AVPlayerViewController())
    #endif
    self.ampPlayer.setLicense(AMPSRConfig.ampLicense)
    _ = self.ampPlayer.registerObserver(self)
    self.ampPlayer.autoplay = true
  }
  
  func play() {
    let playerUrl: String = {
      if AMPSRConfig.dnaEnabled {
        ampSRModule = AMPSRModule(ampPlayer: ampPlayer, vc: self).start()
        return ampSRModule?.dnaClient?.manifestLocalURLPath ?? AMPSRConfig.url
      } else {
        return AMPSRConfig.url
      }
    }()
    self.ampPlayer.play(url: playerUrl)
  }
}
