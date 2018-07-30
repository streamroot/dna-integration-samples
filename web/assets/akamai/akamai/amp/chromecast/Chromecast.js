(function (exports) {
'use strict';

var ChromecastConstants = function () {
  function ChromecastConstants() {
    babelHelpers.classCallCheck(this, ChromecastConstants);
  }

  babelHelpers.createClass(ChromecastConstants, null, [{
    key: "ID",
    get: function get() {
      return "chromecast";
    }
  }, {
    key: "CHANNEL_ID",
    get: function get() {
      return "urn:x-cast:com.akamai.amp.cast";
    }
  }, {
    key: "APPLICATION_ID",
    get: function get() {
      return "CC1AD845";
    }
  }, {
    key: "SUPPORTED_TYPES",
    get: function get() {
      return [akamai.amp.Utils.mimeTypes.m3u8, akamai.amp.Utils.mimeTypes.mp4, akamai.amp.Utils.mimeTypes.mpd, akamai.amp.Utils.mimeTypes.ism, akamai.amp.Utils.mimeTypes.webm, akamai.amp.Utils.mimeTypes.mp3];
    }
  }]);
  return ChromecastConstants;
}();

var AmpCafSenderCaptioning = function (_akamai$amp$EventDisp) {
  babelHelpers.inherits(AmpCafSenderCaptioning, _akamai$amp$EventDisp);

  function AmpCafSenderCaptioning(player) {
    babelHelpers.classCallCheck(this, AmpCafSenderCaptioning);

    var _this = babelHelpers.possibleConstructorReturn(this, (AmpCafSenderCaptioning.__proto__ || Object.getPrototypeOf(AmpCafSenderCaptioning)).call(this));

    _this.player = player.player;
    _this.plugin = player.plugin;
    _this._hidden = _this.player.captioning.hidden;
    _this._tracks = [];
    _this.onEditTracksInfoSuccess = _this.onEditTracksInfoSuccess.bind(_this);
    _this.onEditTracksInfoError = _this.onEditTracksInfoError.bind(_this);

    _this.autoSelectTrack();
    _this.hidden = _this.player.hidden;
    return _this;
  }

  babelHelpers.createClass(AmpCafSenderCaptioning, [{
    key: "onEditTracksInfoSuccess",
    value: function onEditTracksInfoSuccess(event) {}
  }, {
    key: "onEditTracksInfoError",
    value: function onEditTracksInfoError(event) {}
  }, {
    key: "selectTrackByLanguage",
    value: function selectTrackByLanguage(lang) {
      var track = this.tracks.filter(function (track) {
        return track.language === lang;
      })[0];
      if (track != null) {
        this.track = track;
      }
      return track;
    }
  }, {
    key: "changeSettings",
    value: function changeSettings(obj) {}
  }, {
    key: "autoSelectTrack",
    value: function autoSelectTrack() {
      var lang = this.player.language;
      var track = null;

      if (lang != null) {
        lang = lang.split("-").shift();
        track = this.selectTrackByLanguage(lang);
      }

      return track;
    }
  }, {
    key: "tracks",
    get: function get() {
      var mediaSession = this.plugin.mediaSession;
      if (mediaSession == null || mediaSession.media == null || mediaSession.media.tracks == null) {
        return [];
      }
      return mediaSession.media.tracks.filter(function (track) {
        return track.subtype == chrome.cast.media.TextTrackType.CAPTIONS ? track : null;
      });
    }
  }, {
    key: "track",
    get: function get() {
      return this._track || {};
    },
    set: function set(value) {
      this._track = value;
    }
  }, {
    key: "hidden",
    get: function get() {
      return this._hidden;
    },
    set: function set(value) {
      if (value == this._hidden) {
        return;
      }
      this._hidden = value;

      var activeTrackIds = value ? [] : [this.track.trackId];
      var tracksInfoRequest = new chrome.cast.media.EditTracksInfoRequest(activeTrackIds);
      this.plugin.mediaSession.editTracksInfo(tracksInfoRequest, this.onEditTracksInfoSuccess, this.onEditTracksInfoError);

      this.dispatch("visibilitychange", !this._hidden);
    }
  }]);
  return AmpCafSenderCaptioning;
}(akamai.amp.EventDispatcher);

var AmpCafSender = function (_akamai$amp$EventDisp) {
  babelHelpers.inherits(AmpCafSender, _akamai$amp$EventDisp);

  function AmpCafSender(plugin) {
    babelHelpers.classCallCheck(this, AmpCafSender);

    var _this = babelHelpers.possibleConstructorReturn(this, (AmpCafSender.__proto__ || Object.getPrototypeOf(AmpCafSender)).call(this));

    cast.framework.setLoggerLevel(plugin.debug ? cast.framework.LoggerLevel.DEBUG : cast.framework.LoggerLevel.NONE);

    _this.player = plugin.player;
    _this.config = akamai.amp.Utils.clone(_this.player.config);
    _this.plugin = plugin;
    _this.state = {
      playState: akamai.amp.PlayState.READY,
      waiting: false,
      seeking: false
    };

    _this.onRemotePlayerChange = _this.onRemotePlayerChange.bind(_this);
    _this.onMediaSession = _this.onMediaSession.bind(_this);
    _this.onMediaSession();

    _this.plugin.castSession.addEventListener(cast.framework.SessionEventType.MEDIA_SESSION, _this.onMediaSession);

    _this.createElements();
    _this.loadPlugins().then(function () {
      _this.dispatch(akamai.amp.Events.READY);
      _this.dispatch(akamai.amp.Events.MEDIA_CHANGE, _this.media);
      _this.dispatch(akamai.amp.Events.CAN_PLAY_THROUGH);
    });
    return _this;
  }

  babelHelpers.createClass(AmpCafSender, [{
    key: "createElements",
    value: function createElements() {
      this.container = document.createElement("div");
      this.container.className = "amp-chromecast-sender";
      this.player.container.appendChild(this.container);

      this.message = document.createElement("div");
      this.message.className = "amp-chromecast-sender-msg";
      var element = document.createElement("div");
      element.className = "amp-chromecast-sender-text";
      element.innerHTML = this.getLocalizedString("MSG_CHROMECAST_MESSAGE", { device: this.plugin.castSession.getCastDevice().friendlyName });
      this.message.appendChild(element);
      this.player.container.appendChild(this.message);
    }
  }, {
    key: "loadPlugins",
    value: function loadPlugins() {
      var _this2 = this;

      this.chromecast = {
        available: true,
        launch: function launch() {
          return _this2.plugin.launch();
        }
      };

      if (this.player.captioning != null) {
        this.captioning = new AmpCafSenderCaptioning(this);
      }

      this.localization = {
        getLanguageString: function getLanguageString(lang) {
          return _this2.player.localization.getLanguageString(lang);
        },
        getString: function getString(key) {
          return _this2.player.localization.getString(key);
        }
      };

      var type = this.player.mode;
      var key = "react";
      var def = akamai.amp.AMP.plugins[key][type];
      var config = this.config.plugins.react;
      config.mode = akamai.amp.react.Mode.PERSISTENT;

      if (def == null) {
        throw new Error("[AMP] Plugin could not be found: " + key);
      }

      return def(this, config, key).then(function (plugin) {
        _this2[key] = plugin;
        if (plugin.feature != null) {
          _this2[plugin.feature] = plugin;
        }
        plugin.container.classList.add("amp-remote-playback");
        _this2.logger.log("[AMP] Plugin registered: " + key);
      }).catch(function (error) {
        return _this2.logger.error(error);
      });
    }
  }, {
    key: "onRemotePlayerChange",
    value: function onRemotePlayerChange(event) {
      this.logger.log("[AMP CHROMECAST EVENT]", event.field, event.value);

      var type = cast.framework.RemotePlayerEventType;
      var amp = akamai.amp.Events;
      var value = event.value;

      switch (event.field) {
        case "mediaInfo":
          if (this.state.media == null && value != null) {
            this.state.media = value;
            this.playState = this.remotePlayer.playerState.toLowerCase();
            this.dispatch(akamai.amp.Events.DURATION_CHANGE, value.duration);
            if (value.customData && value.customData.media) {
              var media = value.customData.media;
              this.dispatch(akamai.amp.Events.TEMPORAL_TYPE_CHANGE, media.temporalType);
              this.dispatch(akamai.amp.Events.IS_LIVE, media.isLive);
            }
            this.dispatch(akamai.amp.Events.MEDIA_CHANGE, value);
          } else if (value == null) {
            this.state.media = null;
          }
          break;

        case "isMediaLoaded":
          if (value === false) {
            this.playState = akamai.amp.PlayState.ENDED;
          }
          break;

        case "currentTime":
          if (this.seeking === true) {
            this.dispatch(akamai.amp.Events.CAN_PLAY_THROUGH);
            this.dispatch(akamai.amp.Events.SEEKED);
            this.state.seeking = false;
          }

          if (this.waiting === true) {
            this.waiting = false;
          }

          this.dispatch(amp.TIME_UPDATE, this.currentTime);
          break;

        case "duration":
          this.dispatch(amp.DURATION_CHANGE, this.duration);
          break;

        case "volumeLevel":
          this.dispatch(amp.VOLUME_CHANGE, this.volume);
          break;

        case "isMuted":
          this.dispatch(amp.VOLUME_CHANGE, value ? 0 : this.volume);
          this.dispatch(amp.MUTE_CHANGE, this.muted);
          break;

        case "playerState":
          var state = null;

          switch (value) {
            case chrome.cast.media.PlayerState.PLAYING:
              state = akamai.amp.PlayState.PLAYING;
              break;

            case chrome.cast.media.PlayerState.PAUSED:
              state = akamai.amp.PlayState.PAUSED;
              break;

            case chrome.cast.media.PlayerState.BUFFERING:
              this.waiting = true;
              break;
          }

          if (state) {
            this.playState = state;
          }
          break;
      }
      this.dispatch(event);
    }
  }, {
    key: "onMediaSession",
    value: function onMediaSession(event) {
      this.logger.log("[AMP CHROMECAST EVENT]", event);

      if (this.remotePlayerController != null) {
        this.remotePlayerController.removeEventListener(cast.framework.RemotePlayerEventType.ANY_CHANGE, this.onRemotePlayerChange);
      }
      this.remotePlayer = new cast.framework.RemotePlayer();
      this.remotePlayerController = new cast.framework.RemotePlayerController(this.remotePlayer);
      this.remotePlayerController.addEventListener(cast.framework.RemotePlayerEventType.ANY_CHANGE, this.onRemotePlayerChange);
      this.dispatch(event);
    }

    /**
     *
     */

  }, {
    key: "evaluateBindings",
    value: function evaluateBindings(value, context) {
      var data = {
        media: this.media,
        player: {
          mode: "html5"
        },
        now: Date.now()
      };
      return akamai.amp.DataBinding.evaluateBindings(value, akamai.amp.Utils.override(data, context));
    }
  }, {
    key: "play",
    value: function play() {
      this.remotePlayerController.playOrPause();
    }
  }, {
    key: "pause",
    value: function pause() {
      this.remotePlayerController.playOrPause();
    }
  }, {
    key: "goLive",
    value: function goLive() {}
  }, {
    key: "getLocalizedString",
    value: function getLocalizedString(value, context) {
      return this.player.getLocalizedString(value, context);
    }
  }, {
    key: "destroy",
    value: function destroy() {
      if (this.remotePlayerController != null) {
        this.remotePlayerController.removeEventListener(cast.framework.RemotePlayerEventType.ANY_CHANGE, this.onRemotePlayerChange);
      }
      this.plugin.castSession.removeEventListener(cast.framework.SessionEventType.MEDIA_SESSION, this.onMediaSession);
      this.player.container.removeChild(this.container);
      this.player.container.removeChild(this.message);
    }
  }, {
    key: "mediaElement",
    get: function get() {
      return {};
    }
  }, {
    key: "logger",
    get: function get() {
      return this.plugin.logger;
    }
  }, {
    key: "currentTime",
    set: function set(value) {
      if (value == this.state.currentTime) {
        return;
      }

      this.state.seeking = true;
      this.state.currentTime = value;
      this.remotePlayer.currentTime = value;
      if (this.paused === false) this.dispatch(akamai.amp.Events.SEEKING);
      this.remotePlayerController.seek();
    },
    get: function get() {
      return this.remotePlayer.currentTime;
    }
  }, {
    key: "duration",
    get: function get() {
      return this.remotePlayer.duration;
    }
  }, {
    key: "autoplay",
    set: function set(value) {},
    get: function get() {
      return true;
    }
  }, {
    key: "media",
    set: function set(value) {
      this.plugin.load(this.player.defaultMediaTransform(value));
    },
    get: function get() {
      var session = this.plugin.mediaSession;
      return session != null && session.media.customData != null ? session.media.customData.media : this.player.media;
    }
  }, {
    key: "volume",
    set: function set(value) {
      this.remotePlayer.volumeLevel = value;
      this.remotePlayerController.setVolumeLevel();
    },
    get: function get() {
      return this.remotePlayer.volumeLevel;
    }
  }, {
    key: "muted",
    set: function set(value) {
      this.remotePlayerController.muteOrUnmute();
    },
    get: function get() {
      return this.remotePlayer.isMuted;
    }
  }, {
    key: "playbackRate",
    set: function set(value) {},
    get: function get() {
      return 1;
    }
  }, {
    key: "waiting",
    set: function set(value) {
      if (value == this.state.waiting) {
        return;
      }

      this.state.waiting = value;
      if (value) {
        this.dispatch(akamai.amp.Events.WAITING);
      }
    },
    get: function get() {
      return this.state.waiting;
    }
  }, {
    key: "seeking",
    get: function get() {
      return this.state.seeking;
    }
  }, {
    key: "paused",
    get: function get() {
      return this.remotePlayer.isPaused;
    }
  }, {
    key: "ended",
    get: function get() {
      return false;
    }
  }, {
    key: "quality",
    set: function set(value) {
      return;
    },
    get: function get() {
      return 0;
    }
  }, {
    key: "qualityMode",
    get: function get() {
      return "auto";
    }
  }, {
    key: "qualityLevels",
    get: function get() {
      return [];
    }
  }, {
    key: "audioTracks",
    get: function get() {
      return [];
    }
  }, {
    key: "playState",
    set: function set(value) {
      var previous = this.state.playState;
      if (value == previous) {
        return;
      }
      this.state.playState = value;
      this.dispatch(akamai.amp.Events.PLAY_STATE_CHANGE, { previous: previous, value: value });

      switch (value) {
        case akamai.amp.PlayState.PLAYING:
          this.dispatch(akamai.amp.Events.PLAYING);
          break;

        case akamai.amp.PlayState.PAUSED:
          this.dispatch(akamai.amp.Events.PAUSE);
          break;

        case akamai.amp.PlayState.ENDED:
          this.dispatch(akamai.amp.Events.ENDED);
          break;
      }
    },
    get: function get() {
      return this.state.playState;
    }
  }, {
    key: "viewComponent",
    get: function get() {
      return this.container;
    }
  }, {
    key: "settings",
    get: function get() {
      return this.player.settings;
    }
  }]);
  return AmpCafSender;
}(akamai.amp.EventDispatcher);

var Chromecast = function (_akamai$amp$Plugin) {
  babelHelpers.inherits(Chromecast, _akamai$amp$Plugin);

  function Chromecast(player, config) {
    babelHelpers.classCallCheck(this, Chromecast);

    var _this = babelHelpers.possibleConstructorReturn(this, (Chromecast.__proto__ || Object.getPrototypeOf(Chromecast)).call(this, player, config));

    _this._available = false;
    return _this;
  }

  babelHelpers.createClass(Chromecast, [{
    key: "onready",
    value: function onready() {
      this.bindHandlers(["onCastApiAvailable", "onCastStateChanged", "onSessionStateChanged", "onMediaSession"]);
      window['__onGCastApiAvailable'] = this.onCastApiAvailable;

      this.player.loadResources(this.config.sdk);
    }
  }, {
    key: "setAvailable",
    value: function setAvailable(value) {
      if (value === this._available) {
        return;
      }

      this._available = value;
      this.player.dispatch(akamai.amp.Events.PLAYBACK_TARGET_AVAILABILITY_CHANGE, { target: ChromecastConstants.ID, available: value });
    }
  }, {
    key: "onCastApiAvailable",
    value: function onCastApiAvailable(isAvailable) {
      if (!isAvailable) {
        return;
      }

      this.castContext = cast.framework.CastContext.getInstance();

      this.castContext.addEventListener(cast.framework.CastContextEventType.CAST_STATE_CHANGED, this.onCastStateChanged);
      this.castContext.addEventListener(cast.framework.CastContextEventType.SESSION_STATE_CHANGED, this.onSessionStateChanged);
      this.castContext.setOptions({
        receiverApplicationId: this.config.applicationId,
        autoJoinPolicy: chrome.cast.AutoJoinPolicy.ORIGIN_SCOPED
      });
    }
  }, {
    key: "onCastStateChanged",
    value: function onCastStateChanged(event) {
      this.setAvailable(event.castState !== cast.framework.CastState.NO_DEVICES_AVAILABLE);
      this.dispatch(event);
    }
  }, {
    key: "onSessionStateChanged",
    value: function onSessionStateChanged(event) {
      var mediaSession = null;

      switch (event.sessionState) {
        case cast.framework.SessionState.SESSION_STARTING:
          this.player.pause();
          this.player.busy = true;
          break;

        case cast.framework.SessionState.SESSION_RESUMED:
          this.player.busy = true;
          this.castSession = event.session;
          mediaSession = event.session.getMediaSession();
          if (mediaSession.media && this.player.media) {
            var sources = (this.player.media.sources || []).map(function (source) {
              return source.src;
            });
            sources.push(this.player.media.src);
            if (sources.includes(mediaSession.media.contentId)) {
              this.onMediaSession({ mediaSession: mediaSession });
            }
          }
          break;

        case cast.framework.SessionState.SESSION_STARTED:
          this.castSession = event.session;
          this.castSession.addEventListener(cast.framework.SessionEventType.MEDIA_SESSION, this.onMediaSession);
          if (this.castSession.getMediaSession() == null) {
            this.load(this.player.media, this.player.currentTime);
          }
          break;

        case cast.framework.SessionState.SESSION_ENDED:
          mediaSession = event.session.getMediaSession();
          if (mediaSession != null) {
            this.player.currentTime = event.session.getMediaSession().currentTime;
          }
          this.player.playbackTarget = "amp";
          break;
      }
      this.dispatch(event);
    }
  }, {
    key: "onMediaSession",
    value: function onMediaSession(event) {
      this.mediaSession = event.mediaSession;
      this.player.playbackTarget = ChromecastConstants.ID;
      this.dispatch(event);
    }
  }, {
    key: "onplaybacktargetchange",
    value: function onplaybacktargetchange(event) {
      if (event.detail.value === ChromecastConstants.ID) {
        this.sender = new AmpCafSender(this);
      } else {
        if (this.sender != null) {
          this.sender.destroy();
        }
        this.player.busy = false;
      }
    }
  }, {
    key: "launch",
    value: function launch() {
      var _this2 = this;

      this.castContext.requestSession().catch(function (error) {
        if (error != "cancel") _this2.logger.error(error);
      });
    }
  }, {
    key: "load",
    value: function load(media) {
      var _this3 = this;

      var startTime = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 0;


      if (media == null) {
        return;
      }

      var source = akamai.amp.Utils.selectSource(media.source, function (type) {
        return ChromecastConstants.SUPPORTED_TYPES.includes(type) ? "maybe" : "";
      });

      if (source == null) {
        return;
      }

      media.src = source.src;
      media.type = source.type;

      var metadata = new chrome.cast.media.GenericMediaMetadata();
      metadata.title = media.title;
      metadata.subtitle = media.description;
      metadata.image = [new chrome.cast.Image(media.poster)];
      metadata.releaseDate = media.pubDate && media.pubDate.toISOString ? media.pubDate.toISOString() : null;

      var mediaInfo = new chrome.cast.media.MediaInfo(source.src, source.type);
      mediaInfo.duration = media.duration;
      mediaInfo.customData = { media: media };
      mediaInfo.metadata = metadata;

      if (media.track && media.track.length > 0) {
        mediaInfo.tracks = [];
        media.track.forEach(function (value, index) {
          var track = new chrome.cast.media.Track(index, chrome.cast.media.TrackType.TEXT);
          track.subtype = chrome.cast.media.TextTrackType.CAPTIONS;
          track.name = amp.l10n.MSG_CLOSED_CAPTIONING;
          track.language = value.srclang;
          track.customData = value;
          track.trackContentId = value.src;
          track.trackContentType = value.type;
          mediaInfo.tracks.push(track);
        });
      }

      var request = new chrome.cast.media.LoadRequest(mediaInfo);
      request.currentTime = startTime;

      this.castSession.loadMedia(request).catch(function (error) {
        return _this3.logger.error(error);
      });
    }
  }, {
    key: "available",
    get: function get() {
      return this._available;
    }
  }]);
  return Chromecast;
}(akamai.amp.Plugin);

akamai.amp.AMP.registerPlugin("chromecast", akamai.amp.Plugin.createFactory(Chromecast));

exports.Chromecast = Chromecast;

}((this.akamai.amp.chromecast = this.akamai.amp.chromecast || {})));
//# sourceMappingURL=Chromecast.js.map
