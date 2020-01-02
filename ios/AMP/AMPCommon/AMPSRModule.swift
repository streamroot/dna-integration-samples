//
//  AMPSRModule.swift
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
#endif

class AMPSRModule {
  var dnaClient: DNAClient?
  var ampInteractor: AMPInteractor!
  
  private weak var vc: UIViewController?
  private let ampPlayer: AmpPlayer!
  private let url = URL(string: AMPSRConfig.url)!
  
  init(ampPlayer: AmpPlayer!, vc: UIViewController!) {
    self.ampPlayer = ampPlayer
    self.vc = vc
    NSLog("AMP version = \(ampPlayer.version)")
  }
  
  func start() -> AMPSRModule {
    do {
      ampInteractor = AMPInteractor(self.ampPlayer)
      
      dnaClient = try AMPSRConfig
        .setupDNA(DNAClient.builder().dnaClientDelegate(ampInteractor))
        .start(url)
      
      // Make stats view
      if AMPSRConfig.showStatsView, let rootV = vc?.view {
        let statsView = UIView(frame: CGRect(x: 0, y: 30, width: rootV.bounds.width, height: rootV.bounds.height - 30))
        statsView.isUserInteractionEnabled = false
        rootV.addSubview(statsView)
        dnaClient?.displayStats(onView: statsView)
      }
    } catch {
      print("\(error)")
    }
    return self
  }
}
