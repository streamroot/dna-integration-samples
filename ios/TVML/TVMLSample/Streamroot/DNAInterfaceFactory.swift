//
//  DNAInterfaceFactory.swift
//  TVMLSample
//
//  Created by Lamine Ndiaye on 28/01/2020.
//  Copyright © 2020 Lamine Ndiaye. All rights reserved.
//

import TVMLKit

class DNAInterfaceFactory: TVInterfaceFactory {
  
  static let templateName = "DNAPlayerTemplate"
  
  override func makeViewController(element: TVViewElement, existingViewController: UIViewController?) -> UIViewController? {
    
    guard element.name == DNAInterfaceFactory.templateName else { return nil }
    let dnaPlayerViewController = existingViewController as? DNAPlayerViewController ?? DNAPlayerViewController()
    
    if let attributes = element.attributes, let urlString = attributes["url"] {
      dnaPlayerViewController.manifestUrlString = urlString
    }
    
    if let attributes = element.attributes, let contentId = attributes["contentId"] {
      dnaPlayerViewController.contentId = contentId
    }
    
    if let attributes = element.attributes, let displayStats = attributes["displayStats"] {
      dnaPlayerViewController.displayStatView = displayStats == "showed"
    }
    
    if let attributes = element.attributes, let latencyAttribute = attributes["latency"], let latency = Int(latencyAttribute) {
      dnaPlayerViewController.latency = latency
    }
    
    return dnaPlayerViewController
  }
}
