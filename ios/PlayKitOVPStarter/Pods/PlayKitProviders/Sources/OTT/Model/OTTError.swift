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
import SwiftyJSON

class OTTError: OTTBaseObject {

    var message: String?
    var code: String?
    var objectType: String?

    let errorKey = "error"
    let messageKey = "message"
    let codeKey = "code"
    let objectTypeKey = "objectType"

    required init?(json: Any) {

        let jsonObj: JSON = JSON(json)
        let errorDict = jsonObj[errorKey]
        self.message = errorDict[messageKey].string
        self.code = errorDict[codeKey].string
        self.objectType = errorDict[objectTypeKey].string
    }
}
