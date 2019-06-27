//
//  ViewController.m
//  BasicSample
//
//  Created by Gal Orlanczyk on 15/03/2017.
//  Copyright © 2017 Kaltura. All rights reserved.
//

#import "ViewController.h"
#import <PlayKit/PlayKit-Swift.h>
#import <StreamrootSDK/StreamrootSDK-Swift.h>
#import "PlayKitInteractor.h"
#import "PlayKitQosModuleWrapper.h"

/*
 This sample will show you how to create a player with basic functionality.
 The steps required:
 1. Load player with plugin config (only if has plugins).
 2. Register player events.
 3. Prepare Player.
 */
@interface ViewController ()

/*********************************/
#pragma mark - Attributes
/*********************************/
@property (strong, nonatomic) id<Player> player;
@property (strong, nonatomic) NSTimer *playheadTimer;
@property (strong, nonatomic) id<DNAClientDelegate> playerInteractor;
@property (strong, nonatomic) PlayKitQosModuleWrapper *playKitQoSModule;
@property (nonatomic, strong) DNAClient *dnaClient;

/*********************************/
#pragma mark - Outlets
/*********************************/
@property (weak, nonatomic) IBOutlet PlayerView *playerContainer;
@property (weak, nonatomic) IBOutlet UISlider *playheadSlider;

@end

@implementation ViewController
   
/*********************************/
#pragma mark - LifeCycle
/*********************************/

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.playheadSlider.continuous = NO;
    
    // 1. Load the player
    self.player = [[PlayKitManager sharedInstance] loadPlayerWithPluginConfig:nil];
    
    // 2. Register events if have ones.
    // Event registeration must be after loading the player successfully to make sure events are added,
    // and before prepare to make sure no events are missed (when calling prepare player starts buffering and sending events)
    
    // 3. Prepare the player (can be called at a later stage, preparing starts buffering the video)
    [self preparePlayer];
}

- (void) viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    [self.dnaClient stop];
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}
    
/*********************************/
#pragma mark - Player Setup
/*********************************/

- (void)preparePlayer {
    self.player.view = self.playerContainer;
    NSURL *contentURL = [[NSURL alloc] initWithString:@"https://cdnapisec.kaltura.com/p/2215841/playManifest/entryId/1_w9zx2eti/format/applehttp/protocol/https/a.m3u8"];
    
    // create media source and initialize a media entry with that source
    NSString *entryId = @"sintel";
    self.playerInteractor = [PlayKitInteractor playKitInteractor: self.player];
    NSError *error;
    self.dnaClient = [[[[DNAClient.builder dnaClientDelegate: self.playerInteractor]
                        qosModule: self.playKitQoSModule.dnaQoSModule]
                       contentId:entryId error: &error]
                      start:contentURL error: &error];
    if (error || !self.dnaClient) {
        NSLog(@"error: %@", error);
    }
    
    NSURL *localContentUrl = [[NSURL alloc] initWithString: self.dnaClient.manifestLocalURLPath];
    
    PKMediaSource* source = [[PKMediaSource alloc] init:entryId contentUrl: localContentUrl mimeType: nil drmData: nil mediaFormat: MediaFormatHls];
    NSArray<PKMediaSource*>* sources = [[NSArray alloc] initWithObjects: source, nil];
    // setup media entry
    PKMediaEntry *mediaEntry = [[PKMediaEntry alloc] init:entryId sources: sources duration:-1];
    
    // create media config
    MediaConfig *mediaConfig = [[MediaConfig alloc] initWithMediaEntry: mediaEntry startTime:0.0];
    
    // prepare the player
    [self.player prepare: mediaConfig];
    
    [self.dnaClient displayStatsOnView: self.playerContainer];
}

/*********************************/
#pragma mark - Actions
/*********************************/
    
- (IBAction)playTouched:(UIButton *)sender {
    if(!self.player.isPlaying) {
        self.playheadTimer = [NSTimer scheduledTimerWithTimeInterval:0.5f target:self selector:@selector(playheadUpdate) userInfo:nil repeats:YES];
        [self.player play];
    }
}
    
- (IBAction)pauseTouched:(UIButton *)sender {
    if(self.player.isPlaying) {
        [self.playheadTimer invalidate];
        self.playheadTimer = nil;
        [self.player pause];
    }
}
    
- (IBAction)playheadValueChanged:(UISlider *)sender {
    NSLog(@"playhead value: %f", sender.value);
    self.player.currentTime = self.player.duration * sender.value;
}
   
- (void)playheadUpdate {
    self.playheadSlider.value = self.player.currentTime / self.player.duration;
}
    
@end
