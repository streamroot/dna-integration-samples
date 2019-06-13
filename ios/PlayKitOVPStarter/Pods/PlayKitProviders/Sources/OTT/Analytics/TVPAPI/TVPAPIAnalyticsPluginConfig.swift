//
//  TVPAPIAnalyticsPluginConfig.swift
//  PlayKitProviders
//
//  Created by Nilit Danan on 11/16/18.
//

import Foundation

@objc public class TVPAPIAnalyticsPluginConfig: OTTAnalyticsPluginConfig {
    
    let initObject: [String: Any]
    
    @objc public init(baseUrl: String, timerInterval: TimeInterval, initObject: [String: Any]) {
        self.initObject = initObject
        super.init(baseUrl: baseUrl, timerInterval: timerInterval)
    }
}
