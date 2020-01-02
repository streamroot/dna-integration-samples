# iOS AMP sample

## Getting started

AMP is a player composed of modules. 
It is coded in Swift and compiled with a specific version of Xcode, thus Streamroot SDK, AMP, and this project need to be compiled with the same xcode.  
Current version uses Xcode 11.1.

* Install the correct version of Xcode

* Install [CocoaPods](https://cocoapods.org/)

* Install dependencies from this repo's root directory
```
pod install
```

* Build / run using Xcode

## Updating AMP

[AMP SDKs](https://mdtp-a.akamaihd.net/amp-ios-sdk/premier)

AMP is not implemented as pods. Just replace the iOS and tvOS directories with the ones of the new version. Cores are required : **AmpCore** and **AmpCoreTV**.

## Updating Streamroot

Simply change the StreamrootSDK pod version in the Podfile.  
Streamroot is linked as pod. If integrated as .framework, change the integration_type in the Podfile to :framework. Rerun pod install after each change.

## AMP license and configuration

You can jump straight away to AMPSRConfig. There you can setup the stream and the licences.

AMP license is provided to you by Akamai as a base64 string specific to a reverse DNS bundle id. 

In the current sample you can either :
- provide it as is to the ampLicence variable 
- create a file named amp.lic (git ignored by default) in folder AMPCommon and past the base64 licence there.