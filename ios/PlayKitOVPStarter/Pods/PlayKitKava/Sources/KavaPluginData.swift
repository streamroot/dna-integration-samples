// ===================================================================================================
// Copyright (C) 2017 Kaltura Inc.
//
// Licensed under the AGPLv3 license, unless a different license for a
// particular library is specified in the applicable library path.
//
// You may obtain a copy of the License at
// https://www.gnu.org/licenses/agpl-3.0.html
// ===================================================================================================

import Foundation
import PlayKit

/************************************************************/
// MARK: - KavaPluginData
/************************************************************/

class KavaPluginData {
  
    /// Media Delivery Type
    enum DeliveryType : String {
        /// Media Format is HLS
        case hls
        /// Media Format is not HLS
        case url
    }
    
    /************************************************************/
    // MARK: - Properties
    /************************************************************/
    var isMediaLoaded = false
    
    /// The position of the movie in seconds that the user requested to change to.
    var targetSeekPosition: TimeInterval = 0
    /// The sum of all the buffer time during the playback.
    var totalBuffering: TimeInterval = 0
    /// The amount time spent on bufferring from the last viewing event.
    var totalBufferingInCurrentInterval: TimeInterval = 0
    /// The amount of time spent viewing the video in this interval.
    var playTimeInCurrentInterval: TimeInterval = 0
    /// The total amount of time spent viewing
    var totalPlayTime: TimeInterval = 0
    /// The player's streamerType (hls/ url)
    var deliveryType: String = DeliveryType.url.rawValue
    var selectedSource: PKMediaSource?
    /// The current video track bitrate
    var indicatedBitrate: Double = 0
    /// The selected track indicated bitrate sum.
    var bitrateSum: Double = 0
    /// The selected track bitrate counts. used to get the average later.
    var bitrateCount: Int = 0
    /// Error Code will be -1 unless it's player/ ad error.
    var errorCode: Int = -1
    /// Language of the selected caption.
    var currentCaptionLanguage: String?
    /// Language of the selected audio track.
    var currentAudioLanguage: String?
    /// indicates whether we played for the first time or not.
    public var isFirstPlay: Bool = true
    /// indicates whether playback is paused.
    public var isPaused: Bool = true
    /// indicates playback type (vod/ live)
    public var playbackType: String?
    // These params help identify DVR
    var mediaDuration: Double?
    var mediaCurrentTime: Double?
    var joinTime: TimeInterval?
}

extension KavaPluginData {
    static func hasDVR(duration: Double,
                       currentTime: Double,
                       dvrThreshold: Int) -> Bool {
        let distanceFromLive = duration - currentTime
        
        return distanceFromLive >= Double(dvrThreshold)
    }
}
