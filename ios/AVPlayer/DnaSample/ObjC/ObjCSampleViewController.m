    //
    //  ViewController.m
    //  ObjectiveCDemo
    //
    //  Created by Maxime Bokobza on 03/03/17.
    //  Copyright © 2017 Streamroot. All rights reserved.
    //

#import "ObjCSampleViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <StreamrootSDK/StreamrootSDK-Swift.h>

@interface ObjCSampleViewController() <DNAClientDelegate>

@property (nonatomic, strong) DNAClient *dnaClient;

@end


@implementation ObjCSampleViewController

- (void)viewDidLoad {
    [super viewDidLoad];
  
    NSURL *manifestUrl = [NSURL URLWithString: @"http://wowza-test.streamroot.io/liveOrigin/BBB-bl-1500/playlist.m3u8"];
    NSError *error;
    
    // the streamroot key will default to the one in the Info.plist if not overridden here
  self.dnaClient = [/*[*/[[DNAClient.builder dnaClientDelegate: self]
                      latency: 30]
                      //streamrootKey: @"demoswebsiteandpartners"]
                    start:manifestUrl error: &error];
  if (error || !self.dnaClient) {
    NSLog(@"error: %@", error);
  }
  
  NSURL *url = [[NSURL alloc] initWithString: self.dnaClient.manifestLocalURLPath];
    AVPlayerItem *playerItem =[[AVPlayerItem alloc] initWithURL: url];
    self.player = [[AVPlayer alloc] initWithPlayerItem: playerItem];
    [self.player play];
    [self.view setNeedsLayout];
    [self.view layoutIfNeeded];
    [self.dnaClient displayStatsOnView: self.contentOverlayView];
}

- (void)viewDidDisappear:(BOOL)animated {
    [self.dnaClient stop];
}


//MARK: - DNAClientDelegate

- (double)playbackTime {
    return CMTimeGetSeconds([self.player currentTime]);
}

- (NSArray<NSValue *> *)loadedTimeRanges {
    if (self.player == nil) {
        return [NSArray array];
    }
  
    NSMutableArray *timeRanges = [NSMutableArray array];
    for (NSValue *value in [[self.player currentItem] loadedTimeRanges]) {
        TimeRange *timeRange = [[TimeRange alloc] initWithRange:[value CMTimeRangeValue]];
        [timeRanges addObject:[[NSValue alloc] initWithTimeRange:timeRange]];
    }
  
    return timeRanges;
}

- (void)updatePeakBitRate:(double)bitRate {
    self.player.currentItem.preferredPeakBitRate = bitRate;
}

@end
