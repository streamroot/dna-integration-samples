function Html5PlayerInterface (playerStateManager, videoElement) {

    // "this" binding for callbacks
    var _s = this;

    // We happen to reuse some of the system interfaces we defined to bootstrap the Conviva SDK here.
    // We could use system APIs directly as well.
    this._timerInterface = new Html5Timer();
    this._loggingInterface = new Html5Logging();
    
    this._addEventListener = function (eventName, eventListener, eventSource) {
        // Shorthand
        if (typeof eventSource === 'undefined') {
            eventSource = _s._videoElement;
        }

        // Remember what event we are listening to, on what source and through which listener.
        _s._eventListeners.push([eventName, eventListener, eventSource]);

        if (window.addEventListener) {
            eventSource.addEventListener(eventName, eventListener, false);
        } else {
            eventSource.attachEvent('on' + eventName, eventListener);
        }
    };

    this._removeEventListener = function (eventName, eventListener, eventSource) {
        if (typeof eventSource === 'undefined') {
            eventSource = _s._videoElement;
        }

        if (window.removeEventListener) {
           eventSource.removeEventListener(eventName, eventListener, false);
        } else {
           eventSource.detachEvent('on'+eventName, eventListener);
        }
    };

    this._registerVideoEventListeners = function () {
        _s._addEventListener("ended", function () {
            _s._receivedHtml5Event("ended");
        });
        _s._addEventListener("pause", function () {
            _s._receivedHtml5Event("pause");
        });
        _s._addEventListener("playing", function () {
            _s._receivedHtml5Event("playing");
        });
        _s._addEventListener("emptied", function () {
            _s._receivedHtml5Event("emptied");
        });
        _s._addEventListener("stalled", function () {
            if (_s._videoElement.paused) {
                // Viewer experience will not be affected by the lack of data
                // if the video is not actively playing.
                // So we do not consider this experience-impacting buffering.
                _s._log("Ignored stalled event (player was paused)");
                return; 
            }
            _s._receivedHtml5Event("stalled");
        });
        _s._addEventListener("waiting", function () {
            if (_s._videoElement.seeking) {
                // Some HTML5 video element implementations send out tons of 
                // "waiting" events while seeking.
                // So we do not consider this experience-impacting buffering.
                _s._log("Ignored waiting event (player was seeking)");
                return;
            }
            _s._receivedHtml5Event("waiting");
        });
        _s._addEventListener("error", function () {
            // Sometimes triggers without an error attached.
            if (_s._videoElement.error) {
                var html5ErrorCode = _s._videoElement.error.code;
                _s._reportHtml5Error(html5ErrorCode);
            }
        });
        _s._addEventListener("loadedmetadata", _s._loadedMetadata);
        _s._addEventListener("seeking", function () {
            if (!_s.isSeekStarted) {
                _s.isSeekStarted = true;
                //_s._playerStateManager.setPlayerSeekStart(-1);
            }
        });
        _s._addEventListener("seeked", function () {
            _s.isSeekStarted = false;
            //_s._playerStateManager.setPlayerSeekEnd();
        });

        // Monitor error events from nested <source> elements as well.
        _s._monitorErrorsFromSourceElements();
    };

    this._monitorErrorsFromSourceElements = function () {
        if (typeof _s._videoElement.children !== "undefined") {
            var sourceElementErrorHandler = function () {
                // <source> elements do not provide an actual error code
                _s._log("Caught non-specific error from <source> element, reporting as ERR_UNKNOWN");
                _s._reportHtml5Error(0);
            };
            _s._videoElement._sources = _s._videoElement.children;
            for (var i = 0; i < _s._videoElement._sources.length; i++) {
                var source = _s._videoElement._sources[i];
                if (source.tagName == "SOURCE") {
                    _s._addEventListener("error", sourceElementErrorHandler, source);
                }
            }
        }
    };

    this._removeVideoEventHandlers = function () {
        for(var i=0; i<_s._eventListeners.length; i++) {
            var evNameHandler = _s._eventListeners[i];
            _s._removeEventListener(evNameHandler[0], evNameHandler[1], evNameHandler[2]);
        }
        _s._eventListeners = [];
    };

    // Sometimes we may start monitoring the HTML5 video element late.
    // This method attempts to deduce the current Conviva player state without relying
    // on HTML5 video element events.
    this._findCurrentState = function () {
        _s._prevReadyState = _s._videoElement.readyState;
        
        if (_s._videoElement.readyState === 0) {
            // Nothing happened yet
            _s._updateConvivaPlayerState(Conviva.PlayerStateManager.PlayerState.STOPPED);
        } else if (_s._videoElement.ended) {
            // Video playback already ended
            _s._updateConvivaPlayerState(Conviva.PlayerStateManager.PlayerState.STOPPED);
        } else if (_s._videoElement.paused || _s._videoElement.seeking) {
            // Video playback is currently paused.
            _s._updateConvivaPlayerState(Conviva.PlayerStateManager.PlayerState.PAUSED);
        } else {
            // Otherwise we must be actively playing video
            _s._updateConvivaPlayerState(Conviva.PlayerStateManager.PlayerState.PLAYING);
        } 
        // Perhaps we have received some metadata already
        if (_s._videoElement.readyState >= _s._videoElement.HAVE_METADATA) {
            _s._loadedMetadata();
        }
    };

    // Extract Conviva video quality data from HTML5 video element events.
    this._receivedHtml5Event = function (html5Event) {
        var convivaPlayerState = _s._convertHtml5EventToConvivaPlayerState(html5Event);

        _s._log("Received HTML5 event: " + html5Event + ". Mapped to Conviva player state: " + convivaPlayerState);

        _s._updateConvivaPlayerState(convivaPlayerState);
    };

    this._updateConvivaPlayerState = function (newConvivaPlayerState) {
        if (_s._playerStateManager.getPlayerState() !== newConvivaPlayerState) {
            _s._log("Changing Conviva player state to: " + newConvivaPlayerState);

            // Report the new player state to Conviva.
            _s._playerStateManager.setPlayerState(newConvivaPlayerState);

            // Failsafe to avoid overcorrecting the player state when we just changed it.
            _s._playerStateRecentlyChanged = true;
        }
    };

    // Maps relevant HTML5 video element events to the corresponding Conviva player state.
    this._convertHtml5EventToConvivaPlayerState = function (html5Event) {
        switch (html5Event) {
            case "playing":
                return Conviva.PlayerStateManager.PlayerState.PLAYING;
            case "waiting":
            case "stalled":
            case "emptied":
                return Conviva.PlayerStateManager.PlayerState.BUFFERING;
            case "ended":
            case "stopped":
                return Conviva.PlayerStateManager.PlayerState.STOPPED;
            case "pause":
                return Conviva.PlayerStateManager.PlayerState.PAUSED;
            default:
                return Conviva.PlayerStateManager.PlayerState.UNKNOWN;
        }
    };

    // Converts HTML5 video element errors to human-readable Conviva errors.
    // See http://dev.w3.org/html5/spec-author-view/video.html#error-codes
    this._reportHtml5Error = function (html5ErrorCode) {
        var html5ErrorMessage;
        switch (html5ErrorCode) {
            case 1:
                html5ErrorMessage = "MEDIA_ERR_ABORTED";
                break;
            case 2: html5ErrorMessage = "MEDIA_ERR_NETWORK";
                break;
            case 3: html5ErrorMessage = "MEDIA_ERR_DECODE";
                break;
            case 4: html5ErrorMessage = "MEDIA_ERR_SRC_NOT_SUPPORTED";
                break;
            default: html5ErrorMessage = "MEDIA_ERR_UNKNOWN";
        }

        _s._log("Reporting error: code=" + html5ErrorCode + " message=" + html5ErrorMessage);

        // HTML5 players generally do not recover from these errors,
        // so we mark all errors as fatal.
        // If the player does recover and the video starts playing,
        // the Conviva Platform will ignore the error.
        var html5ErrorSeverity = Conviva.Client.ErrorSeverity.FATAL;

        // Report the error to Conviva.
        _s._playerStateManager.sendError(html5ErrorMessage, html5ErrorSeverity);
    };

    // When metadata is available, we extract the duration of the video and report it.
    this._loadedMetadata = function () {
        var dur = _s._videoElement.duration;
        if (!isNaN(dur) && dur != Infinity) {
            // Report the duration to Conviva.
            _s._playerStateManager.setDuration(dur);
        }
    };

    // Regularly poll for the current position in the video and current buffer length
    this._startPolling = function () {
        this._previousPosition = 0;
        this._currentPosition = 0;
        this._currentBufferLength = 0;
        this._pollingTimerCancel = this._timerInterface.createTimer(
            this._poll,
            500, // check updated position every 500 milliseconds
            "Html5PlayerInterface._poll()"
        );
    };

    this._poll = function () {
        _s._pollPosition();
        _s._pollBufferLength();
        _s._inferPlayerStateFromPosition();
    };

    this._pollPosition = function () {
        // Get the current position in the video.
        _s._previousPosition = _s._currentPosition;
        _s._currentPosition = _s._videoElement.currentTime;

        // Report to Conviva, in milliseconds
        _s._playerStateManager.setPlayheadTime(_s._currentPosition * 1000);
    };

    this._pollBufferLength = function () {
        var buffered = _s._videoElement.buffered;
        if (buffered === undefined) return;

        // Find all buffered segments ahead of the current position and add up the lengths
        // to figure out how long content can keep playing with the data already downloaded.
        var bufferLength = 0; 
        for (var i = 0; i < buffered.length; i++) {
            var start = buffered.start(i);
            var end = buffered.end(i);
            if (start <= _s._currentPosition && _s._currentPosition < end) {
                bufferLength += end - _s._currentPosition;
            }
        }

        _s._currentBufferLength = bufferLength;

        // Report to Conviva, in milliseconds
        _s._playerStateManager.setBufferLength(_s._currentBufferLength * 1000);
    };

    // Sometimes we can't rely on HTML5 video element waiting/emptied/stalled/playing events,
    // they simply won't trigger. In those scenarios, the current position in the video
    // can be a more accurate indicator of whether content is actively playing.

    // The general idea is then for us to regularly query the current position in the video,
    // compare it to previous values, and adjust the current Conviva player state based
    // on whether the video is progressing or not:
    // - If the current Conviva player state is not "playing", and the video is progressing,
    //   we correct the Conviva player state to "playing".
    // - If the current Conviva player state is "playing", and the video is not progressing,
    //   we check whether the video is paused. If it is, we correct the Conviva player state
    //   to "paused". Otherwise, we must be stalled, so we correct the Conviva player state to "buffering".

    // Note that for live streaming content, some video players do not maintain a reliable value
    // for the current position inside the video stream.
    this._inferPlayerStateFromPosition = function () {
        // Compare the current position with the last observation.
        // If different, video playback has progressed since we last checked.
        _s._positionDelta = Math.abs(_s._previousPosition - _s._currentPosition);

        // Ignore large position increments most likely due to seeking.
        // Can't rely on HTML5 video "seeking" events or "seeking" property for that.
        if (_s._positionDelta > 1) return;

        // To prevent cascading state changes, only adjust the player state
        // based on position observations within the same player state
        if (_s._playerStateRecentlyChanged) {
            _s._playerStateRecentlyChanged = false;
            return;
        }

        var videoProgressed = _s._positionDelta !== 0;

        // Get the current Conviva player state.
        var currentConvivaState = _s._playerStateManager.getPlayerState();

        // Verify that the current Conviva player state is consistent with video progress.
        if (videoProgressed && currentConvivaState !== Conviva.PlayerStateManager.PlayerState.PLAYING) {
            // Video is actually playing, we should be in playing state.
            _s._log("Adjusting Conviva player state to: PLAYING");
            _s._updateConvivaPlayerState(Conviva.PlayerStateManager.PlayerState.PLAYING);
        } else if (!videoProgressed && currentConvivaState === Conviva.PlayerStateManager.PlayerState.PLAYING) {
            // Video is not actually playing, we should not be in playing state.
            if (_s._videoElement.paused) {
                // If the video is currently paused, switch to paused state.
                _s._log("Adjusting Conviva player state to: PAUSED");
                _s._updateConvivaPlayerState(Conviva.PlayerStateManager.PlayerState.PAUSED);
            } else if (_s._videoElement.seeking) {
                // If we are seeking, stick with the current player state for now.
            } else {
                // Otherwise we must be waiting for more data, go to buffering state.
                _s._log("Adjusting Conviva player state to: BUFFERING");
                _s._updateConvivaPlayerState(Conviva.PlayerStateManager.PlayerState.BUFFERING);
            }
        }
    };

    this._stopPolling = function () {
        this._pollingTimerCancel();
    };

    // PlayerStateManager

    this.getPlayerStateManager = function () {
        return this._playerStateManager;
    };

    // Utilities

    this._log = function (message) {
        var formattedMessage = "[Html5PlayerInterface] " + message;
        this._loggingInterface.consoleLog(formattedMessage, Conviva.SystemSettings.LogLevel.DEBUG);
    };

    // Constructor

    function _constr (playerStateManager, videoElement) {
        this._log("Html5PlayerInterface._constr()");

        if (!playerStateManager) {
            throw new Error("Html5PlayerInterface: playerStateManager argument cannot be null.");
        }
        if (!videoElement) {
            throw new Error("Html5PlayerInterface: videoElement argument cannot be null.");
        }

        this._playerStateManager = playerStateManager;
        this._videoElement = videoElement;

        // Start listening for HTML5 video element events.
        this._eventListeners = [];
        this._registerVideoEventListeners();

        // Start polling for the current position in the video.
        this._startPolling();

        this._findCurrentState();
    }

    _constr.apply(this, arguments);

    // Destructor

    this.cleanup = function () {
        this._log("Html5PlayerInterface.cleanup()");

        this._stopPolling();
        this._removeVideoEventHandlers();

        this._videoElement = null;
        this._playerStateManager = null;
    };
}


/*! (C) 2016 Conviva, Inc. All rights reserved. Confidential and proprietary. */
/*! This is sample code meant to illustrate proper Conviva integration in video applications. */
/*! This file should not be included in video applications as part of integrating Conviva. */

// Implements Conviva.HttpInterface for Chrome.

/*! (C) 2016 Conviva, Inc. All rights reserved. Confidential and proprietary. */
/*! This is sample code meant to illustrate proper Conviva integration in video applications. */
/*! This file should not be included in video applications as part of integrating Conviva. */

// Implements Conviva.HttpInterface for Chrome.

function Html5Http () {

    function _constr() {
        // nothing to initialize
    }

    _constr.apply(this, arguments);

    this.makeRequest = function (httpMethod, url, data, contentType, timeoutMs, callback) {
    	// XDomainRequest only exists in IE, and is IE8-IE9's way of making CORS requests.
    	// It is present in IE10 but won't work right.
    	// if (typeof XDomainRequest !== "undefined" && navigator.userAgent.indexOf('MSIE 10') === -1) {
    	// 	return this.makeRequestIE89.apply(this, arguments);
    	// }
		return this.makeRequestStandard.apply(this, arguments);
    };

    this.makeRequestStandard = function (httpMethod, url, data, contentType, timeoutMs, callback) {
	    var xmlHttpReq = new XMLHttpRequest();

	    xmlHttpReq.open(httpMethod, url, true);
	 
        if (contentType && xmlHttpReq.overrideMimeType) {
            xmlHttpReq.overrideMimeType = contentType;
        }
	    if (contentType && xmlHttpReq.setRequestHeader) {
	        xmlHttpReq.setRequestHeader('Content-Type',  contentType);
	    }
	    if (timeoutMs > 0) {
	        xmlHttpReq.timeout = timeoutMs;
	        xmlHttpReq.ontimeout = function () {
	            // Often this callback will be called after onreadystatechange.
	            // The first callback called will cleanup the other to prevent duplicate responses.
	            xmlHttpReq.ontimeout = xmlHttpReq.onreadystatechange = null;
	            if (callback) callback(false, "timeout after " + timeoutMs + " ms");
	        };
	    }

	    xmlHttpReq.onreadystatechange = function () {
	        if (xmlHttpReq.readyState === 4) {
		        xmlHttpReq.ontimeout = xmlHttpReq.onreadystatechange = null;
		        if (xmlHttpReq.status == 200) {
	           		if (callback) callback(true, xmlHttpReq.responseText);
		        } else {
	            	if (callback) callback(false, "http status " + xmlHttpReq.status);
		        }
		    }
	    };

	    xmlHttpReq.send(data);

	    return null; // no way to cancel the request
    };

  //   this.makeRequestIE89 = function (httpMethod, url, data, contentType, timeoutMs, callback) {
	 //    // IE8-9 does not allow changing the contentType on CORS requests.
	 //    // IE8-9 does not like mixed intranet/extranet CORS requests.
	 //    // IE8-9 does not like mixed HTTPS-in-HTTP-page / HTTP-in-HTTPS-page CORS requests.

	 //    var xmlHttpReq = new XDomainRequest();

	 //    xmlHttpReq.open(httpMethod, url, true); // async=true

	 //    if (timeoutMs != null) {
	 //        xmlHttpReq.timeout = timeoutMs;
	 //        xmlHttpReq.ontimeout = function () {
	 //            xmlHttpReq.onload = xmlHttpReq.onerror = null;
	 //            if (callback) callback(false, "timeout after "+timeoutMs+" ms");
	 //        };
	 //    }

		// // onreadystatechange won't trigger for XDomainRequest.
	 //    xmlHttpReq.onload = function () {
	 //    	xmlHttpReq.ontimeout = null;
	 //    	if (callback) callback(true, xmlHttpReq.responseText);
	 //    };
	 //    xmlHttpReq.onerror = function () {
	 //    	xmlHttpReq.ontimeout = null;
	 //    	if (callback) callback(false, "http status " + xmlHttpReq.status);
	 //    };

	 //    xmlHttpReq.send(data);

	 //    return null; // no way to cancel the request
  //   };

    this.release = function() {
        // nothing to release
    };

}
/*! (C) 2016 Conviva, Inc. All rights reserved. Confidential and proprietary. */
/*! This is sample code meant to illustrate proper Conviva integration in video applications. */
/*! This file should not be included in video applications as part of integrating Conviva. */

// Implements Conviva.LoggingInterface for Chrome.

function Html5Logging () {

    function _constr () {
        // nothing to initialize
    }

    _constr.apply(this, arguments);

    this.consoleLog = function (message, logLevel) {
        if (typeof console === 'undefined') return;
        if (console.log && logLevel === Conviva.SystemSettings.LogLevel.DEBUG ||
            logLevel === Conviva.SystemSettings.LogLevel.INFO) {
            console.log(message);
        } else if (console.warn && logLevel === Conviva.SystemSettings.LogLevel.WARNING) {
            console.warn(message);
        } else if (console.error && logLevel === Conviva.SystemSettings.LogLevel.ERROR) {
            console.error(message);
        }
    };

    this.release = function () {
        // nothing to release
    };

}

/*! (C) 2016 Conviva, Inc. All rights reserved. Confidential and proprietary. */
/*! This is sample code meant to illustrate proper Conviva integration in video applications. */
/*! This file should not be included in video applications as part of integrating Conviva. */

// Implements Conviva.MetadataInterface for Chrome.

// The Conviva Platform will recognize HTTP user agent strings for major browsers,
// and use these to fill in some of the missing metadata.
// You can validate the resulting metadata through our validation tools.
// If you wish you can maintain your own user agent string parsing on the client side
// instead, and use it to supply the requested Conviva data.

function Html5Metadata () {

    function _constr() {
        // nothing to initialize
    }

    _constr.apply(this, arguments);

    // Relying on HTTP user agent string parsing on the Conviva Platform.
    this.getBrowserName = function () {
        return null;
    };

    // Relying on HTTP user agent string parsing on the Conviva Platform.
    this.getBrowserVersion = function () {
        return null;
    };

    // Relying on HTTP user agent string parsing on the Conviva Platform.
    this.getDeviceBrand = function () {
        return null;
    };

    // Relying on HTTP user agent string parsing on the Conviva Platform.
    this.getDeviceManufacturer = function () {
        return null;
    };
    
    // Relying on HTTP user agent string parsing on the Conviva Platform.
    this.getDeviceModel = function () {
        return null;
    };

    // Relying on HTTP user agent string parsing on the Conviva Platform.
    this.getDeviceType = function () {
        return null;
    };

    // There is no value we can access that qualifies as the device version.
    this.getDeviceVersion = function () {
        return null;
    };

    // HTML5 can qualify as an application framework of sorts.
    this.getFrameworkName = function () {
        return "HTML5";
    };

    // No convenient way to detect HTML5 version.
    this.getFrameworkVersion = function () {
        return null;
    };

    // Relying on HTTP user agent string parsing on the Conviva Platform.
    this.getOperatingSystemName = function () {
        return null;
    };

    // Relying on HTTP user agent string parsing on the Conviva Platform.
    this.getOperatingSystemVersion = function () {
        return null;
    };

    this.release = function() {
        // nothing to release
    };
}/*! (C) 2016 Conviva, Inc. All rights reserved. Confidential and proprietary. */
/*! This is sample code meant to illustrate proper Conviva integration in video applications. */
/*! This file should not be included in video applications as part of integrating Conviva. */

// Implements Conviva.StorageInterface for Chrome.

// HTML5 localStorage relies on a single key to index items,
// so we find a consistent way to combine storageSpace and storageKey.

function Html5Storage () {

    function _constr() {
        // nothing to initialize
    }

    _constr.apply(this, arguments);

    this.saveData = function (storageSpace, storageKey, data, callback) {
        var localStorageKey = storageSpace + "." + storageKey;
        try {
            localStorage.setItem(localStorageKey, data);
            callback(true, null);
        } catch (e) {
            callback(false, e.toString());
        }
    };

    this.loadData = function (storageSpace, storageKey, callback) {
        var localStorageKey = storageSpace + "." + storageKey;
        try {
            var data = localStorage.getItem(localStorageKey);
            callback(true, data);
        } catch (e) { 
            callback(false, e.toString());
        }
    };

    this.release = function() {
        // nothing to release
    };

}

// Implements SystemInterfaceFactory

// Convenience factory class to produce HTML5-specific SystemInterface objects
// based on the underlying system implementations.

var Html5SystemInterfaceFactory = function () {
	this.build = function () {
		return new Conviva.SystemInterface(
		  new Html5Time(),
		  new Html5Timer(),
		  new Html5Http(),
		  new Html5Storage(),
		  new Html5Metadata(),
		  new Html5Logging()
		);
	};
};
/*! (C) 2016 Conviva, Inc. All rights reserved. Confidential and proprietary. */
/*! This is sample code meant to illustrate proper Conviva integration in video applications. */
/*! This file should not be included in video applications as part of integrating Conviva. */

// Implements Conviva.TimeInterface for Chrome.

function Html5Time () {

    function _constr() {
        // nothing to initialize
    }

    _constr.apply(this, arguments);

    this.getEpochTimeMs = function () {
        var d = new Date();
        return d.getTime();
    };

    this.release = function() {
        // nothing to release
    };
}

/*! (C) 2016 Conviva, Inc. All rights reserved. Confidential and proprietary. */
/*! This is sample code meant to illustrate proper Conviva integration in video applications. */
/*! This file should not be included in video applications as part of integrating Conviva. */

// Implements Conviva.TimerInterface for Chrome.

// setInterval does exactly what we need. We just need to return a function 
// which cancels the timer when called.
// Some JavaScript implementations do not have setInterval, in which case
// you may have to write it yourself using setTimeout.

function Html5Timer () {

    function _constr() {
        // nothing to initialize
    }

    _constr.apply(this, arguments);

    this.createTimer = function (timerAction, intervalMs, actionName) {
        var timerId = setInterval(timerAction, intervalMs);
        var cancelTimerFunc = (function () {
            if (timerId !== -1) {
                clearInterval(timerId);
                timerId = -1;
            }
        });
        return cancelTimerFunc;
    };

    this.release = function() {
        // nothing to release
    };

}

