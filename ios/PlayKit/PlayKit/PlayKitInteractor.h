//
//  PlayKitInteractor.h
//  PlayKit
//
//  Created by Lamine Ndiaye on 21/06/2019.
//  Copyright © 2019 Kaltura. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PlayKitInteractor.h"
#import <PlayKit/PlayKit-Swift.h>
#import <StreamrootSDK/StreamrootSDK-Swift.h>

NS_ASSUME_NONNULL_BEGIN

@interface PlayKitInteractor: NSObject <DNAClientDelegate>

+(instancetype)playKitInteractor:(id<Player>) player;

@end

NS_ASSUME_NONNULL_END
