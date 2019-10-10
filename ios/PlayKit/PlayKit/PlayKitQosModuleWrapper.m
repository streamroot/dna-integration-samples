    //
    //  PlayKitQosModuleWrapper.m
    //  PlayKit
    //
    //  Created by Lamine Ndiaye on 21/06/2019.
    //  Copyright © 2019 Kaltura. All rights reserved.
    //

#import "PlayKitQosModuleWrapper.h"
#import <AVFoundation/AVFoundation.h>

@interface PlayKitQosModuleWrapper()
@property (strong, nonatomic) id<Player> player;
@end

@implementation PlayKitQosModuleWrapper
+(instancetype)playKitQoSModule:(id<Player>) player {
    PlayKitQosModuleWrapper *playkitQosModule = [[PlayKitQosModuleWrapper alloc] init];
    playkitQosModule.player = player;
    return playkitQosModule;
}

- (instancetype)init {
    if (self = [super init]) {
        self.dnaQoSModule = [DNAQos moduleWithType:QOSModuleTypeCustom];
        [self registerObservers];
    }
    return self;
}

-(void)registerObservers {
    __weak typeof(self) weakSelf = self;
    [self.player addObserver: self event: PlayerEvent.stateChanged block:^(PKEvent* event)  {
        typeof(self) strongSelf = weakSelf;
        switch (event.newState) {
            case PlayerStateIdle:
                [strongSelf.dnaQoSModule playerStateDidChange: PlaybackStateIdle];
            case PlayerStateReady:
                if (strongSelf.player.isPlaying) {
                    [strongSelf.dnaQoSModule playerStateDidChange: PlaybackStatePlaying];
                } else {
                    [strongSelf.dnaQoSModule playerStateDidChange: PlaybackStatePaused];
                }
            case PlayerStateBuffering:
                [strongSelf.dnaQoSModule playerStateDidChange: PlaybackStateBuffering];
            case PlayerStateError:
                [strongSelf.dnaQoSModule playbackErrorOccurred];
            default:
                break;
        }
    }];
    
    [self.player addObserver: self event: PlayerEvent.videoTrackChanged block:^(PKEvent* event) {
        typeof(self) strongSelf = weakSelf;
        [strongSelf.dnaQoSModule trackSwitchOccurred];
    }];
    
    [self.player addObserver: self event: PlayerEvent.seeked block:^(PKEvent* event) {
        typeof(self) strongSelf = weakSelf;
        [strongSelf.dnaQoSModule playerStateDidChange: PlaybackStateSeeking];
    }];
    
    [self.player addObserver: self event: PlayerEvent.playbackStalled block:^(PKEvent* event) {
        typeof(self) strongSelf = weakSelf;
        [strongSelf.dnaQoSModule playbackErrorOccurred];
    }];
    
    [self.player addObserver: self event: PlayerEvent.ended block:^(PKEvent* event) {
        typeof(self) strongSelf = weakSelf;
        [strongSelf.dnaQoSModule playerStateDidChange: PlaybackStateEnded];
    }];
    
    [self.player addObserver: self event: PlayerEvent.error block:^(PKEvent* event) {
        typeof(self) strongSelf = weakSelf;
        [strongSelf.dnaQoSModule playbackErrorOccurred];
    }];
}

- (void)dealloc
{
    [self.player removeObserver: self events: @[PlayerEvent.stateChanged,
                                                PlayerEvent.videoTrackChanged,
                                                PlayerEvent.seeked,
                                                PlayerEvent.playbackStalled,
                                                PlayerEvent.ended,
                                                PlayerEvent.error]];
}
@end
