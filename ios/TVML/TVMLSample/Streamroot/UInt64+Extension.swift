//
//  UInt+Extension.swift
//  TVMLSample
//
//  Created by Lamine Ndiaye on 27/01/2020.
//  Copyright © 2020 Lamine Ndiaye. All rights reserved.
//

import Foundation

extension Int64 {
  func humanReadableBytes() -> String {
    let countBytes = ByteCountFormatter()
    countBytes.allowedUnits = [.useBytes, .useKB, .useMB, .useGB]
    countBytes.countStyle = .binary
    countBytes.isAdaptive = true
    countBytes.allowsNonnumericFormatting = false
    return countBytes.string(fromByteCount: self)
  }
}
