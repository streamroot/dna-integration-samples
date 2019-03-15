# Streamroot iOS DNA 

## Dependencies
- [daltoniam/Starscream](https://github.com/daltoniam/Starscream)
- [getsentry/sentry-cocoa](https://github.com/getsentry/sentry-cocoa)
 
## Getting started

### Installation
Get the SDK via [cocoapods](https://cocoapods.org/) -> `pod install` in the current directory

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
  dnaClient = try DNAClient.builder().dnaClientDelegate(self)
                                          .start(manifest)
                                          .latency(30)
                                          } catch let error {
                                            print("\(error)")
}
```

### Play the stream
```swift
guard let localPath = self.dnaClient?.manifestLocalURLPath,
  let url = URL(string: localPath) else  {
  print("Could not generate localPath, please check your network")
  return
  }
 
let playerItem = AVPlayerItem(asset: AVURLAsset(url: url))
if let bufferTarget = dnaClient?.bufferTarget {
  if #available(iOS 10.0, *) {
    playerItem.preferredForwardBufferDuration = bufferTarget
  }
}

player = AVPlayer(playerItem: playerItem)
player?.play()
```

### Implement DNAClientDelegate
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

  func bufferTarget() -> Double {
    if #available(iOS 10.0, tvOS 10.0, *) {
      return self.player?.currentItem?.preferredForwardBufferDuration ?? 0
    }
    return 0.0
  }
  
  func setBufferTarget(_ target: Double) {
    if #available(iOS 10.0, tvOS 10.0, *) {
      self.player?.currentItem?.preferredForwardBufferDuration = target
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
