//
//  RestMultiRequestBuilder.swift
//  Pods
//
//  Created by Admin on 13/11/2016.
//
//

import UIKit
import SwiftyJSON

public class KalturaMultiRequestBuilder: KalturaRequestBuilder {
    
    var requests: [KalturaRequestBuilder] = [KalturaRequestBuilder]()
    
    public init?(url: String) {
        super.init(url: url, service: "multirequest", action: nil)
    }
    
    @discardableResult
    public func add(request:KalturaRequestBuilder) -> Self {
        self.requests.append(request)
        return self
    }
    
    override public func build() -> Request {
        
        let data = self.kalturaMultiRequestData()
        let request = RequestElement(requestId: self.requestId,
                                     method: self.method,
                                     url: self.url,
                                     dataBody: data,
                                     headers: self.headers,
                                     timeout: self.timeout,
                                     configuration: self.configuration,
                                     responseSerializer: self.responseSerializer,
                                     completion: self.completion)
        
        return request
    }
    
    func kalturaMultiRequestData() -> Data {
        
        if self.jsonBody == nil {
            self.jsonBody = JSON([String: Any]())
        }
        
        for (index, request)  in self.requests.enumerated() {
            if let body = request.jsonBody {
                var singleRequestBody: JSON = body
                singleRequestBody["action"] = JSON(request.action ?? "")
                singleRequestBody["service"] =  JSON(request.service ?? "")
                self.jsonBody?[String(index+1)] = singleRequestBody
            }
        }
        
        let prefix = "{"
        let suffix = "}"
        
        var data = Data()
        
        if let prefixData = prefix.data(using: String.Encoding.utf8) {
            data.append(prefixData)
        }
        
        for  index in 1...self.requests.count {
            let requestBody = self.jsonBody?[String(index)].rawString(String.Encoding.utf8, options: JSONSerialization.WritingOptions())?.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
            
            if let indexData = "\"\(index)\":".data(using: String.Encoding.utf8) {
                data.append(indexData)
            }
            
            if let requestBodyData = requestBody?.data(using: String.Encoding.utf8) {
                data.append(requestBodyData)
            }
            
            if let separatorData = ",".data(using: String.Encoding.utf8) {
                data.append(separatorData)
            }
            
            _ = self.jsonBody?.dictionaryObject?.removeValue(forKey: String(index))
        }
        
        if let jsonBody = self.jsonBody{
            let remainingJsonAsString: String? = jsonBody.rawString(String.Encoding.utf8, options: JSONSerialization.WritingOptions())
            if let jsonString = remainingJsonAsString{
                var jsonWithoutFirstAndLastChar = String(jsonString.dropLast())
                jsonWithoutFirstAndLastChar = String(jsonWithoutFirstAndLastChar.dropFirst())
                if let remainingData = jsonWithoutFirstAndLastChar.data(using: String.Encoding.utf8) {
                    data.append(remainingData)
                }
            }
        }
        
        if let suffixData = suffix.data(using: String.Encoding.utf8) {
            data.append(suffixData)
        }
        
        return data
    }
}
