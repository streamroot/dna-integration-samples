//
//  ViewController.swift
//  AMPSample
//
//  Created by Boris BORGOBELLO on 10/12/2019.
//  Copyright © 2019 Streamroot. All rights reserved.
//

import UIKit
import StreamrootSDK
import AmpCore

class ViewController: UIViewController {
  
  var ampPlayer: AmpPlayer!
  var ampSRModule: AMPSRModule?

  override func viewDidLoad() {
    super.viewDidLoad()
    setup()
    play()
  }
}
