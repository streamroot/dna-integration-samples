//
//  KavaResponseSerializer.swift
//  PlayKitKava
//
//  Created by Nilit Danan on 5/16/18.
//

import Foundation
import KalturaNetKit

/**
 ** Currently we need to support the response data to arrive as JSON or as String.
 ** Until all is changed to JSON.
 **/
public class KavaSerializer: ResponseSerializer {
    public init() {}
    public func serialize(data: Data) throws -> Any {
        do {
            let json = try JSONSerialization.jsonObject(with: data, options: JSONSerialization.ReadingOptions())
            return json
        } catch {
            let string = String(data: data, encoding: .utf8)
            return string ?? ""
        }
    }
}
