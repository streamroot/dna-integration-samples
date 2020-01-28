//# sourceURL=application.js

//
//  application.js
//  TVMLSample
//
//  Created by Lamine Ndiaye on 02/01/2020.
//  Copyright © 2020 Lamine Ndiaye. All rights reserved.
//

/*
 * This file provides an example skeletal stub for the server-side implementation 
 * of a TVML application.
 *
 * A javascript file such as this should be provided at the tvBootURL that is 
 * configured in the AppDelegate of the TVML application. Note that  the various 
 * javascript functions here are referenced by name in the AppDelegate. This skeletal 
 * implementation shows the basic entry points that you will want to handle 
 * application lifecycle events.
 */

/**
 * @description The onLaunch callback is invoked after the application JavaScript 
 * has been parsed into a JavaScript context. The handler is passed an object 
 * that contains options passed in for launch. These options are defined in the
 * swift or objective-c client code. Options can be used to communicate to
 * your JavaScript code that data and as well as state information, like if the 
 * the app is being launched in the background.
 *
 * The location attribute is automatically added to the object and represents 
 * the URL that was used to retrieve the application JavaScript.
 */

var mPlayer;

App.onLaunch = function(options) {
  var alert = createAlert("Hello World!");
    
  // When it's selected, doSomething
var globalOnSelect = function(ev) {
  App.start();
}

alert.addEventListener('select', globalOnSelect);
navigationDocument.pushDocument(alert);
}

App.start = function() {
  playerHandlerObj.play("http://wowza-test-cloudfront.streamroot.io/liveOrigin/BBB-bl-1500/playlist.m3u8");
}

App.onWillResignActive = function() {

}

App.onDidEnterBackground = function() {

}

App.onWillEnterForeground = function() {
    
}

App.onDidBecomeActive = function() {
    
} 

App.onWillTerminate = function() {
    
}

App.playStream = function(url) {
  if (url) {
    videoURL = url;
    mPlayer = new Player();
    var playlist = new Playlist();
    var mediaItem = new MediaItem("video", videoURL);
    mPlayer.playlist = playlist;
    mPlayer.playlist.push(mediaItem);
    mPlayer.present();

    // Optional for the QoS module
    mPlayer.addEventListener('playbackDidStall', App.playbackDidStall);
    mPlayer.addEventListener('playbackError', App.playbackError);

    // Stop the DNACLient whenever the video is finished
    // doc.addEventListener("unload", function(event) { playerHandlerObj.stop() });
  }
}

App.displayStats = function(statsString) {
var parser = new DOMParser();  
var parsedDoc = parser.parseFromString(`<document style="tv-align:left; tv-position: top-left;">  
                                        <descriptiveAlertTemplate>
                                          <description>${statsString}</description>
                                        </descriptiveAlertTemplate>
                                       </document>`, "application/xml");  
mPlayer.interactiveOverlayDocument = parsedDoc;  
mPlayer.interactiveOverlayDismissable = true;
}

App.playbackDidStall = function(event) {
  playerHandlerObj.playbackRebuffering();
}

App.playbackError = function(event) {
  playerHandlerObj.playbackError();
}

App.stateDidChange = function(event) {  
  switch(event.state) {
    case "playing":
      playerHandlerObj.playbackPlaying();
      break;
    case "paused":
      playerHandlerObj.playbackPaused();
      break;
    case "end":
      playerHandlerObj.playbackEnded();
      playerHandlerObj.stop();
      break;
    default: break;
  }
}

/**
 * This convenience funnction returns an alert template, which can be used to present errors to the user.
 */
var createAlert = function(title) {

  var alertString = `<?xml version="1.0" encoding="UTF-8" ?>
  <document>
    <alertTemplate>
      <title>${title}</title>
      <buttonLockup>
        <text>PLAY</text>
      </buttonLockup>
    </alertTemplate>
  </document>`

    var parser = new DOMParser();

    var alertDoc = parser.parseFromString(alertString, "application/xml");

    return alertDoc
}