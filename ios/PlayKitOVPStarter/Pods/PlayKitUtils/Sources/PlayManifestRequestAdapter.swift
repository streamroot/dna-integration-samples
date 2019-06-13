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

/// `PlayManifestRequestAdapter` object is used to adapt a play manifest request with query items (playSessionId, clientTag etc...).
/// If the adapt fails it will return the received url as is instead of adding the new query items.
public class PlayManifestRequestAdapter {
    
    public var url: URL
    public var sessionId: String
    public var clientTag: String
    public var referrer: String
    public var playbackType: String?
    
    public init(url: URL, sessionId: String, clientTag: String, referrer: String, playbackType: String? = nil) {
        self.url = url
        self.sessionId = sessionId
        self.clientTag = clientTag
        self.referrer = referrer
        self.playbackType = playbackType
    }
    
    /// adapts the provided url with the data given.
    public func adapt() -> URL {
        guard self.url.path.contains("/playManifest/") else { return self.url }
        guard var urlComponents = URLComponents(url: self.url, resolvingAgainstBaseURL: false) else { return self.url }
        // add query items to the request
        var queryItems = [
            URLQueryItem(name: "playSessionId", value: self.sessionId),
            URLQueryItem(name: "clientTag", value: self.clientTag),
            URLQueryItem(name: "referrer", value: self.referrer)
        ]
        if let playbackType = self.playbackType {
            queryItems.append(URLQueryItem(name: "playbackType", value: playbackType))
        }
        if var urlQueryItems = urlComponents.queryItems { // if query items exists add more
            urlQueryItems += queryItems
            urlComponents.queryItems = urlQueryItems
        } else { // if no query items use ones we created
            urlComponents.queryItems = queryItems
        }
        // create the url
        guard let url = urlComponents.url else { return self.url }
        return url
    }
}
