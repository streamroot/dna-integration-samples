(function (exports) {
'use strict';

var MediaAnalytics = function (_akamai$amp$Plugin) {
  babelHelpers.inherits(MediaAnalytics, _akamai$amp$Plugin);

  function MediaAnalytics(player, config) {
    babelHelpers.classCallCheck(this, MediaAnalytics);

    var _this = babelHelpers.possibleConstructorReturn(this, (MediaAnalytics.__proto__ || Object.getPrototypeOf(MediaAnalytics)).call(this, player, config));

    _this.sdk = new JS_AkamaiMediaAnalytics(config.config);
    _this.sdk.disableLocation();

    if (config.iplookup === false) {
      _this.sdk.disableServerIPLookUp();
    }
    _this.initialized = false;
    return _this;
  }

  babelHelpers.createClass(MediaAnalytics, [{
    key: "setDimensions",
    value: function setDimensions(value) {
      var dimensions = this.config.dimensions;
      for (var key in value) {
        var val = value[key];
        if (val != null) {
          dimensions[key] = val; // TODO: Does this need to be evaluated for bindings?
        }
      }
      this.applyDimensions(dimensions);
      return value;
    }
  }, {
    key: "amaCallbacks",
    value: function amaCallbacks(playerInstance) {
      this.getStreamHeadPosition = function () {
        if (playerInstance) {
          return playerInstance.currentTime;
        }
      };
    }
  }, {
    key: "applyDimensions",
    value: function applyDimensions(dimensions) {
      try {
        for (var key in dimensions) {
          this.sdk.setData(key, this.player.evaluateBindings(dimensions[key]));
        }
      } catch (error) {
        this.logger.error("[AMP MEDIA ANALYTICS ERROR]", "Could not set dimensions:", error);
      }
    }
  }, {
    key: "onready",
    value: function onready(event) {
      this.player.addEventListener("adbreakstart", this.onadbreakstart.bind(this));
      this.player.addEventListener("adbreakend", this.onadbreakend.bind(this));
      this.player.addEventListener("adloaded", this.onadloaded.bind(this));
      this.player.addEventListener("adstarted", this.onadstarted.bind(this));
      this.player.addEventListener("adended", this.onadended.bind(this));
      this.player.addEventListener("adskipped", this.onadbreakskipped.bind(this));
      this.player.addEventListener("aderror", this.onaderror.bind(this));
      this.player.addEventListener("adfirstquartile", this.onadfirstquartile.bind(this));
      this.player.addEventListener("admidpoint", this.onadmidpoint.bind(this));
      this.player.addEventListener("adthirdquartile", this.onadthirdquartile.bind(this));
    }
  }, {
    key: "onplayrequest",
    value: function onplayrequest(event) {
      if (this.initialized == false) {
        this.logger.log("[AMP MA EVENT] - handleSessionInit");
        this.sdk.handleSessionInit(new this.amaCallbacks(this.player));
        this.initialized = true;
      }
    }
  }, {
    key: "onmediasequenceended",
    value: function onmediasequenceended(event) {
      this.logger.log("[AMP MA EVENT] - handlePlayEnd");
      this.sdk.handlePlayEnd();
    }
  }, {
    key: "onplaying",
    value: function onplaying(event) {
      if (this.player.ads != null && this.player.ads.inProgress) return;
      this.logger.log("[AMP MA EVENT] - handlePlaying");
      this.sdk.handlePlaying();
    }
  }, {
    key: "onmediachange",
    value: function onmediachange(event) {
      var media = event.detail;
      var dimensions = null;

      this.initialized = false;
      this.logger.log("[AMP MA EVENT] - setStreamURL", media.src);
      this.sdk.setStreamURL(media.src, true);

      var viewerId = this.config.viewerId || this.config.viewerID || this.config["std:viewerId"];
      if (viewerId != null) {
        this.sdk.setViewerId(viewerId);
      }

      var config = media.mediaanalytics;
      if (config != null && config.dimensions != null) {
        dimensions = akamia.amp.Utils.override(this.config.dimensions, config.dimensions);
      } else {
        dimensions = this.config.dimensions;
      }

      //this.sdk.handleStreamSwitch()
      this.applyDimensions(dimensions);
    }
  }, {
    key: "ondurationchange",
    value: function ondurationchange(event) {
      this.logger.log("[AMP MA EVENT] - setStreamDuration", event.detail);
      this.sdk.setStreamDuration(event.detail);
    }
  }, {
    key: "updateMedia",
    value: function updateMedia(media) {
      this.logger.log("[AMP MA EVENT] - handleTitleSwitch");
      this.sdk.handleTitleSwitch(media);
    }
  }, {
    key: "onfragmentloadstart",
    value: function onfragmentloadstart(event) {
      if (typeof fragmentDownloadStarted === "function") {
        fragmentDownloadStarted(event.detail);
      }
    }
  }, {
    key: "onadbreakstart",
    value: function onadbreakstart() {
      this.player.mediaElement.dataset.isad = true;
    }
  }, {
    key: "onadbreakend",
    value: function onadbreakend() {
      this.player.mediaElement.dataset.isad = false;
    }
  }, {
    key: "onadloaded",
    value: function onadloaded(event) {
      try {
        var adVO = event.data;
        var adObject = {
          adTitle: adVO.title,
          adDuration: adVO.duration,
          adPartnerId: adVO.partner,
          adId: adVO.id
        };
        this.logger.log("[AMP MA EVENT] - handleAdLoaded");
        this.sdk.handleAdLoaded(adObject);
      } catch (error) {
        this.logger.error("[AMP MEDIA ANALYTICS ERROR]", error);
      }
    }
  }, {
    key: "onadstarted",
    value: function onadstarted() {
      this.logger.log("[AMP MA EVENT] - handleAdStarted");
      this.sdk.handleAdStarted();
    }
  }, {
    key: "onadbreakskipped",
    value: function onadbreakskipped() {
      this.logger.log("[AMP MA EVENT] - handleAdSkipped");
      this.sdk.handleAdSkipped();
    }
  }, {
    key: "onaderror",
    value: function onaderror(event) {
      this.logger.log("[AMP MA EVENT] - handleAdError");
      this.sdk.handleAdError(event);
    }
  }, {
    key: "onadfirstquartile",
    value: function onadfirstquartile() {
      this.logger.log("[AMP MA EVENT] - handleAdFirstQuartile");
      this.sdk.handleAdFirstQuartile();
    }
  }, {
    key: "onadmidpoint",
    value: function onadmidpoint() {
      this.logger.log("[AMP MA EVENT] - handleAdMidPoint");
      this.sdk.handleAdMidPoint();
    }
  }, {
    key: "onadthirdquartile",
    value: function onadthirdquartile() {
      this.logger.log("[AMP MA EVENT] - handleAdThirdQuartile");
      this.sdk.handleAdThirdQuartile();
    }
  }, {
    key: "onadended",
    value: function onadended() {
      this.logger.log("[AMP MA EVENT] - handleAdComplete");
      this.sdk.handleAdComplete();
    }
  }, {
    key: "onpause",
    value: function onpause() {
      this.logger.log("[AMP MA EVENT] - handlePause");
      this.sdk.handlePause();
    }
  }, {
    key: "onseeking",
    value: function onseeking() {
      if (this.player.currentTime > 0.1 || this.player.currentTime === 0) {
        this.sdk.handleSeekStart();
        this.logger.log("[AMP MA EVENT] - handleSeekStart");
      }
    }
  }, {
    key: "onseeked",
    value: function onseeked() {
      if (this.player.currentTime > 0.1 || this.player.currentTime === 0) {
        this.sdk.handleSeekEnd();
        this.logger.log("[AMP MA EVENT] - handleSeekEnd");
      }
    }
  }, {
    key: "onerror",
    value: function onerror(event) {
      this.logger.log("[AMP MA EVENT] - handleError", event.detail);
      this.sdk.handleError(event.detail);
    }
  }, {
    key: "onbufferingchange",
    value: function onbufferingchange(event) {
      this.logger.log("[AMP MA EVENT] - handleBufferStart", event.detail);
      if (event.detail === true) this.sdk.handleBufferStart();else this.sdk.handleBufferEnd();
    }
  }, {
    key: "onqualitychange",
    value: function onqualitychange(event) {
      this.logger.log("[AMP MA EVENT] - handleBitRateSwitch", event.data.bitrate);
      this.sdk.handleBitRateSwitch(event.data.bitrate);
    }
  }]);
  return MediaAnalytics;
}(akamai.amp.Plugin);

var MediaAnalyticsFlash = function (_akamai$amp$FlashPlug) {
  babelHelpers.inherits(MediaAnalyticsFlash, _akamai$amp$FlashPlug);

  function MediaAnalyticsFlash(player, config) {
    babelHelpers.classCallCheck(this, MediaAnalyticsFlash);
    return babelHelpers.possibleConstructorReturn(this, (MediaAnalyticsFlash.__proto__ || Object.getPrototypeOf(MediaAnalyticsFlash)).call(this, player, config));
  }

  babelHelpers.createClass(MediaAnalyticsFlash, [{
    key: "setDimensions",
    value: function setDimensions(value) {
      this.player.mediaElement.setPlayerProperty("maDimensions", value);
      return value;
    }
  }, {
    key: "createFlashVars",
    value: function createFlashVars(event) {
      var flashvars = event.detail.flashvars;
      var dimensions = this.player.config && this.player.config.media && this.player.config.media.mediaanalytics;

      if (dimensions != null) {
        for (var key in dimensions) {
          flashvars["report_" + key] = dimensions[key];
        }
      }
    }
  }, {
    key: "createXML",
    value: function createXML(event) {
      var xml = event.detail.xml;
      var application = xml.firstChild;
      var metrics = xml.getElementsByTagName("metrics")[0];
      var element = null;

      if (metrics == null) {
        metrics = xml.createElement("metrics");
        application.appendChild(metrics);
      }

      var vendor = xml.createElement("vendor");
      vendor.setAttribute("id", "akamai");
      metrics.appendChild(vendor);

      if (this.config.config != null) {
        this.createProperty(xml, "MEDIA_ANALYTICS_BEACON", this.config.config, vendor);

        var path = this.config.plugin.swf || "http://79423.analytics.edgesuite.net/csma/plugin/csma.swf";
        this.createProperty(xml, "MEDIA_ANALYTICS_PLUGIN_PATH", path, vendor);
      }

      if (this.config.dimensions != null) {
        this.createProperty(xml, "dimensions", this.config.dimensions, vendor);
      }
    }
  }, {
    key: "onmediachange",
    value: function onmediachange(event) {
      var media = event.detail;
      if (media.mediaanalytics && media.mediaanalytics.dimensions) {
        media.dimensions = [];
        for (var key in media.mediaanalytics.dimensions) {
          media.dimensions.push({ key: key, value: media.mediaanalytics.dimensions[key] });
        }
      }
    }
  }, {
    key: "flashPlugins",
    get: function get() {
      return [{
        id: "OSMFCSMALoader",
        host: "osmf",
        main: "com.akamai.playeranalytics.osmf.OSMFCSMALoaderInfo",
        type: "application/x-shockwave-flash"
      }];
    }
  }]);
  return MediaAnalyticsFlash;
}(akamai.amp.FlashPlugin);

akamai.amp.AMP.registerPlugin("mediaanalytics", "html", akamai.amp.Plugin.createFactory(MediaAnalytics));
akamai.amp.AMP.registerPlugin("mediaanalytics", "flash", akamai.amp.Plugin.createFactory(MediaAnalyticsFlash));

exports.MediaAnalytics = MediaAnalytics;

}((this.akamai.amp.mediaanalytics = this.akamai.amp.mediaanalytics || {})));
//# sourceMappingURL=Mediaanalytics.js.map
