//
//  AMPSRConfig.swift
//  AMPSample
//
//  Created by Boris BORGOBELLO on 10/12/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//

import Foundation
import StreamrootSDK

struct AMPSRConfig {
  static let ampLicense = {
    guard let path = Bundle.main.path(forResource: "amp", ofType: "lic") else { return nil }
    guard let licenseStr = try? String(contentsOfFile: path, encoding: .utf8), licenseStr.count > 0 else { return nil }
    return licenseStr.replacingOccurrences(of: "\n", with: "").replacingOccurrences(of: "\r", with: "")
  }() ?? "or put your AMP base64 licence here"
  
  static let url = "http://wowza-test.streamroot.io/liveOrigin/Sintel1/playlist.m3u8"
  static let dnaEnabled = true
  static let showStatsView = true
  
  static func setupDNA(_ builder: DNATrigger) -> DNATrigger {
    return builder
    .streamrootKey("demoswebsiteandpartners")
    .latency(30)
    .logLevel(.debug)
  }
}
