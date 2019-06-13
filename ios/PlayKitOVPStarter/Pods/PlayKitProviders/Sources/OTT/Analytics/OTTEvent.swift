// ===================================================================================================
// Copyright (C) 2017 Kaltura Inc.
//
// Licensed under the AGPLv3 license, unless a different license for a 
// particular library is specified in the applicable library path.
//
// You may obtain a copy of the License at
// https://www.gnu.org/licenses/agpl-3.0.html
// ===================================================================================================

import UIKit
import PlayKit

/// OTT Event
public class OttEvent : PKEvent {
    
    static let codeKey = "code"
    static let messageKey = "message"
    
    class Concurrency : OttEvent {}
    /// represents the Concurrency event Type.
    /// Concurrency events fire when more then the allowed connections are exceeded.
    public static let concurrency: OttEvent.Type = OttEvent.Concurrency.self
    
    class Report: OttEvent {
        convenience init(message: String) {
            self.init([OttEvent.messageKey: message])
        }
    }
    
    @objc public static let report: OttEvent.Type = OttEvent.Report.self
    
    class BookmarkError : OttEvent {
        convenience init(code: String?, message: String?) {
            self.init([OttEvent.codeKey: code ?? "", OttEvent.messageKey: message ?? ""])
        }
    }
    
    @objc public static let bookmarkError: OttEvent.Type = OttEvent.BookmarkError.self
}

extension PKEvent {
    /// bufferTime Value, PKEvent Data Accessor
    @objc public var ottEventMessage: String? {
        return self.data?[OttEvent.messageKey] as? String
    }
}
