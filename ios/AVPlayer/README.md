# Streamroot iOS DNA 

## Dependencies
- [daltoniam/Starscream](https://github.com/daltoniam/Starscream)
- [getsentry/sentry-cocoa](https://github.com/getsentry/sentry-cocoa)
 
## Getting started
###
Get the SDK
Via Carthage -> https://github.com/Carthage/Carthage
Via Cocoapods-> https://cocoapods.org/

### Set the streamrootKey
In the Project Navigator, right click on "Info.plist", and "Open as" → "Source Code"
Add the following lines with the right parameters values

```xml
  <key>Streamroot</key>
  <dict>
    <key>Key</key>
    <string>yourStreamrootKey</string>
  </dict>
```

### Build and start the DNAClient
```swift
do {
  self.dnaClient = try DNAClient.builder().dnaClientDelegate(self)
                                          .start(manifest)
                                          } catch let error {
                                            print("\(error)")
}
```

### Play stream
```swift
if let localPath = self.dnaClient?.manifestLocalURLPath {
 self.play(url: localPath)
}
```

### implement DNAClientDelegate
```swift
extension PlayerViewController: DNAClientDelegate {
  func playbackTime() -> Double {
    if let player = self.player {
      return CMTimeGetSeconds(player.currentTime())
    }
    return 0
  }
  
  func loadedTimeRanges() -> [NSValue] {
    guard let player = self.player else {
      return []
    }
    
    let timeRanges = player.currentItem!.loadedTimeRanges
    return timeRanges.map { (value) -> NSValue in
      NSValue(timeRange: TimeRange(range: value.timeRangeValue))
    }
  }
  
  func updatePeakBitRate(_ bitRate: Double) {
    player?.currentItem?.preferredPeakBitRate = bitRate
  }
}
```

### Stop the SDK

```swift 
dnaClient?.stop()
```

### Display stats

```swift 
dnaClient?.displayStats(onView: contentOverlayView!)
```

### Disable App Transport security
In the Project Navigator, right click on "Info.plist", and "Open as" → "Source Code"
Add the following lines with the right parameters values

```xml
	<key>NSAppTransportSecurity</key>
	<dict>
		<key>NSAllowsArbitraryLoads</key>
		<true/>
	</dict>
```