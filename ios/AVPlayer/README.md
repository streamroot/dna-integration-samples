# Streamroot iOS SDK

## Requirements

- iOS 9.0+
- Xcode 9+
- For swift projects: Swift 3.2+

## Embedded dependencies

- [swisspol/GCDWebServer](https://github.com/swisspol/GCDWebServer) v3.3.3  
- WebRTC
- MurmurHash3

## Dependencies

- [Alamofire/Alamofire](https://github.com/Alamofire/Alamofire) v4.5.1 
- [daltoniam/Starscream](https://github.com/daltoniam/Starscream) v3.0.2
- [getsentry/sentry-cocoa](https://github.com/getsentry/sentry-cocoa) v3.11.1

Install dependencies:

- [Carthage](#carthage)
- [Cocoapods](#cocoapods)
- [Manually](#manually)

## Known limitations

P2P is not yet supported with chunks referenced via an absolute URL. In this case, the playback will fallback to full CDN delivery.

## Project setup

## Installation

### Carthage

[Carthage](https://github.com/Carthage/Carthage) is a decentralized dependency manager that builds your dependencies and provides you with binary frameworks.

You can install Carthage with [Homebrew](http://brew.sh/) using the following command:

```bash
$ brew update
$ brew install carthage
```

To integrate StreamrootSDK into your Xcode project using Carthage, specify it in your `Cartfile`:

```ogdl
binary "https://sdk.streamroot.io/ios/StreamrootSDK.json" ~> 2.1.0
github "Alamofire/Alamofire" == 4.6.0
github "daltoniam/Starscream" == 3.0.2
github "getsentry/sentry-cocoa" == 3.11.1
```

Run `carthage update` to build the framework and drag the built `StreamrootSDK.framework`, `Alamofire.framework`, `Starscream.framework` and `Sentry.framework` into your Xcode project as linked libraries.

### CocoaPods

[CocoaPods](http://cocoapods.org) is a dependency manager for Cocoa projects. You can install it with the following command:

```bash
$ gem install cocoapods
```

> CocoaPods 1.3+ is recommended

To integrate StreamrootSDK into your Xcode project using CocoaPods, specify it in your `Podfile`:

```ruby
source 'https://github.com/CocoaPods/Specs.git'
platform :ios, '9..0'
use_frameworks!

target '<Your Target Name>' do
	pod 'StreamrootSDK', '~> 2.0.0'
end
```

Then, run the following command:

```bash
$ pod install
```

### Manually

#### Downloads

StreamrootSDK.framework:  
[http://sdk.streamroot.io/ios/stable/StreamrootSDK.framework.zip](http://sdk.streamroot.io/ios/latest/StreamrootSDK.framework.zip)

Copy script:  
[http://sdk.streamroot.io/ios/stable/copy\_streamroot\_sdk](http://sdk.streamroot.io/ios/latest/copy_streamroot_sdk)

#### Add StreamrootSDK.framework to your project

##### Link Binary With Libraries

Drag and drop `StreamrootSDK.framework` into your Xcode project.
Make sure `StreamrootSDK.framework` has been added to your target's build phases under `Link Binary With Libraries`.

##### Copy the framework

Create a new build phase (`New Run Script Phase`) to call the [`copy_streamroot_sdk`](http://sdk.streamroot.io/ios/stable/copy_streamroot_sdk) script.

Add the folder containing the `StreamrootSDK.framework` in the `Input Files` field. It defaults to `$(SRCROOT)/` if none input file is set.

#### Notes

Make sure to have all dependencies installed in your project.  

For Objective-C projects, set `Always Embed Swift Standard Libraries` to `yes` in your target's build settings.

### Build for devices

To build for devices, you need to disable [bitcode](https://developer.apple.com/library/ios/documentation/IDEs/Conceptual/AppDistributionGuide/AppThinning/AppThinning.html#//apple_ref/doc/uid/TP40012582-CH35-SW2) in your project.  

### Enable http requests

By default, the application will prevent http requests. If you need to enable them, add those lines to your `info.plist` file.

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```