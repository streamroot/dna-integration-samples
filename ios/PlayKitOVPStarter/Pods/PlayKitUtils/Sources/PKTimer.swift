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

#if swift(>=4.2)
fileprivate let defaultRunLoopMode: RunLoop.Mode = .default
#else
fileprivate let defaultRunLoopMode: RunLoopMode = .defaultRunLoopMode
#endif


public class PKTimer {
        
    /// Create a timer that will call `block` after interval only once.
    public static func after(_ interval: TimeInterval, _ block: @escaping (Timer) -> Void) -> Timer {
        if #available(iOS 10, tvOS 10.0, *) {
            let timer = Timer.scheduledTimer(withTimeInterval: interval, repeats: false, block: block)
            return timer
        } else {
            let fireDate: CFAbsoluteTime = CFAbsoluteTimeGetCurrent() + interval
            var timer: Timer!
            timer = CFRunLoopTimerCreateWithHandler(kCFAllocatorDefault, fireDate, 0, 0, 0) { _ in
                block(timer)
            }
            RunLoop.main.add(timer, forMode: defaultRunLoopMode)
            return timer
        }
    }
    
    /// Create a timer that will call `block` every interval.
    public static func every(_ interval: TimeInterval, _ block: @escaping (Timer) -> Void) -> Timer {
        if #available(iOS 10, tvOS 10.0, *) {
            let timer = Timer.scheduledTimer(withTimeInterval: interval, repeats: true, block: block)
            return timer
        } else {
            let fireDate: CFAbsoluteTime = CFAbsoluteTimeGetCurrent() + interval
            var timer: Timer!
            timer = CFRunLoopTimerCreateWithHandler(kCFAllocatorDefault, fireDate, interval, 0, 0) { _ in
                block(timer)
            }
            RunLoop.main.add(timer, forMode: defaultRunLoopMode)
            return timer
        }
    }
}
