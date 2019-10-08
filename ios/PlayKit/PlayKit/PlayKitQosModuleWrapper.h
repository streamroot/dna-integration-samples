//
//  PlayKitQosModuleWrapper.h
//  PlayKit
//
//  Created by Lamine Ndiaye on 21/06/2019.
//  Copyright © 2019 Kaltura. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <PlayKit/PlayKit-Swift.h>
#import <StreamrootSDK/StreamrootSDK-Swift.h>

NS_ASSUME_NONNULL_BEGIN

@interface PlayKitQosModuleWrapper : NSObject
+(instancetype)playKitQoSModule:(id<Player>) player;
@property (nonatomic, strong) id<QosModule> dnaQoSModule;
@end

NS_ASSUME_NONNULL_END
