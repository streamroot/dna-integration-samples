//
//  PlayKitInteractor.m
//  PlayKit
//
//  Created by Lamine Ndiaye on 21/06/2019.
//  Copyright © 2019 Kaltura. All rights reserved.
//

#import "PlayKitInteractor.h"
#import <AVFoundation/AVFoundation.h>

@interface PlayKitInteractor()
@property (strong, nonatomic) id<Player> player;
@end

@implementation PlayKitInteractor

+(instancetype)playKitInteractor:(id<Player>) player {
    PlayKitInteractor *interactor = [[PlayKitInteractor alloc] init];
    interactor.player = player;
    return interactor;
}

- (NSArray<NSValue *> * _Nonnull)loadedTimeRanges {
    if (self.player == nil) {
        return [NSArray array];
    }
    
    NSMutableArray *timeRanges = [NSMutableArray array];
    for (PKTimeRange *range in [self.player loadedTimeRanges]) {
        TimeRange *timeRange = [[TimeRange alloc] initWithStart: range.start duration: range.duration];
        [timeRanges addObject:[[NSValue alloc] initWithTimeRange:timeRange]];
    }
    
    return timeRanges;
}

- (double)playbackTime {
    return self.player.currentTime;
}

- (void)updatePeakBitRate:(double)bitRate {
    self.player.settings.network.preferredPeakBitRate = bitRate;
}

- (double)bufferTarget {
    return self.player.settings.network.preferredForwardBufferDuration;
}

- (void)setBufferTarget:(double)target {
    self.player.settings.network.preferredForwardBufferDuration = target;
}

@end

