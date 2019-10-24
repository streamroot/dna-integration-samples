//
//  SwiftSampleVC+AVAssetResourceLoaderDelegate.swift
//  DnaSample-iOS
//
//  Created by Lamine Ndiaye on 24/10/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//

import Foundation
import AVKit
import StreamrootSDK

extension SwiftSampleViewController: AVAssetResourceLoaderDelegate {
  
  func resourceLoader(_ resourceLoader: AVAssetResourceLoader, shouldWaitForLoadingOfRequestedResource loadingRequest: AVAssetResourceLoadingRequest) -> Bool {
    
    print("\(#function) was called in AssetLoaderDelegate with loadingRequest: \(loadingRequest)")
    return shouldLoadOrRenewRequestedResource(resourceLoadingRequest: loadingRequest)
  }
  
  func shouldLoadOrRenewRequestedResource(resourceLoadingRequest: AVAssetResourceLoadingRequest) -> Bool {
    
    guard let url = resourceLoadingRequest.request.url else {
      return false
    }
    
    // AssetLoaderDelegate only should handle FPS Content Key requests.
    if url.scheme != "skd" {
      return false
    }
    
    resourceLoadingRequestQueue.async { [weak self] in
      self?.prepareAndSendContentKeyRequest(resourceLoadingRequest: resourceLoadingRequest)
    }
    
    return true
  }
  
  func prepareAndSendContentKeyRequest(resourceLoadingRequest: AVAssetResourceLoadingRequest) {
    
    guard let drmToken = drmToken else {
      print("DRM Token not found")
      return
    }
    
    // get Asset data
    guard let contentKeyIdentifierURL = resourceLoadingRequest.request.url,
      let assetIDString = contentKeyIdentifierURL.host,
      let assetIDData = assetIDString.data(using: .utf8) else {
        
        print(" --> Failed to get url or assetIDString for the request object of the resource.")
        let error = NSError(domain: "io.streamroot.fps", code: -2, userInfo: [kCFErrorLocalizedDescriptionKey as String: "Failed to retrieve certificate data from bundle"])
        resourceLoadingRequest.finishLoading(with: error)
        return
    }
    do {
      // Get certificate
      let applicationCertificate = try Data(contentsOf: Bundle.main.url(forResource: <#fairplay Certificat name#>, withExtension: "cer")!)
      
      // Get Spc
      let spcData = try resourceLoadingRequest.streamingContentKeyRequestData(forApp: applicationCertificate,
                                                                              contentIdentifier: assetIDData,
                                                                              options: nil)
      guard let dataRequest = resourceLoadingRequest.dataRequest else {
        let customError = NSError(domain: "io.streamroot.fps", code: -4, userInfo: [kCFErrorLocalizedDescriptionKey as String: "Failed loading data request"])
        resourceLoadingRequest.finishLoading(with: customError)
        return
      }
      
      // Send SPC to Key Server and obtain CKC.
      let ckcUrl = URL(string: <#DRM server url#>)
      var request = URLRequest(url: ckcUrl!)
      request.httpMethod = "POST"
      request.addValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
      request.addValue("drmToken=" + drmToken, forHTTPHeaderField: "Cookie");
      
      var requestBody = "spc=" + Encoder.urlencode(with: spcData.base64EncodedString())
      requestBody += "&assetID=" + assetIDString
      
      request.httpBody = requestBody.data(using: .utf8)
      
      let session = URLSession.shared
      let task = session.dataTask(with: request) { data, response, error in
        if let data = data {
          print("--> CKC Length=\(data.count) dataRequest=\(String(describing: resourceLoadingRequest.dataRequest))")
          dataRequest.respond(with: data.base64EncodedData())
          //          dataRequest.respond(with: NSData(base64Encoded: data, options: [])! as Data)
          resourceLoadingRequest.finishLoading()
          
        } else {
          let customError = NSError(domain: "io.streamroot.fps", code: -4, userInfo: [kCFErrorLocalizedDescriptionKey as String: "Failed fetching CKC"])
          
          print("--> \(customError.description)")
          resourceLoadingRequest.finishLoading(with: customError)
        }
      }
      task.resume()
    } catch {
      resourceLoadingRequest.finishLoading(with: error)
    }
  }
}
