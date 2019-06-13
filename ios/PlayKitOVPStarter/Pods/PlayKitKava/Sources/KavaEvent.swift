// ===================================================================================================
// Copyright (C) 2017 Kaltura Inc.
//
// Licensed under the AGPLv3 license, unless a different license for a
// particular library is specified in the applicable library path.
//
// You may obtain a copy of the License at
// https://www.gnu.org/licenses/agpl-3.0.html
// ===================================================================================================

import PlayKit

/// `KavaEvent` represents an event reporting from kava plugin.
@objc public class KavaEvent: PKEvent {
    
    static let messageKey = "message"
    
    class Report: KavaEvent {
        convenience init(message: String) {
            self.init([KavaEvent.messageKey: message])
        }
    }
    
    @objc public static let report: KavaEvent.Type = Report.self
}
