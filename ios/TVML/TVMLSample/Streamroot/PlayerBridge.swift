//
//  PlayerBridge.swift
//  TVMLSample
//
//  Created by Lamine Ndiaye on 27/01/2020.
//  Copyright © 2020 Lamine Ndiaye. All rights reserved.
//

import Foundation

// MARK: Protocol to call JS function swift
protocol PlayerJSBridge: class {
  func playLocalStream(_: String)
  func playbackTime() -> TimeInterval
  func displayStats(_ stats: String)
}
