(function (exports) {
'use strict';

var GoogleAnalytics = function (_akamai$amp$Plugin) {
  babelHelpers.inherits(GoogleAnalytics, _akamai$amp$Plugin);

  function GoogleAnalytics(player, config) {
    babelHelpers.classCallCheck(this, GoogleAnalytics);

    var _this = babelHelpers.possibleConstructorReturn(this, (GoogleAnalytics.__proto__ || Object.getPrototypeOf(GoogleAnalytics)).call(this, player, config));

    ga('create', _this.config.trackingId, 'auto');
    ga('send', 'pageview');
    return _this;
  }

  babelHelpers.createClass(GoogleAnalytics, [{
    key: 'generateGoogleAnalyticsVO',
    value: function generateGoogleAnalyticsVO(eventName) {
      var type = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : "video";

      if (eventName == null) return;

      var events = this.config.events[type];
      if (events == null) return;

      var event = events.filter(function (event) {
        return event != null && event.type == eventName;
      })[0];
      if (event == null) return;

      var data = event.data || event.detail;
      if (data == null) return;

      var vo = {
        trackingid: this.config.trackingId
      };

      for (var key in data) {
        var value = this.player.evaluateBindings(data[key]);
        if (key === "customDimension") {
          var cdim = new Object();
          for (var dim in value) {
            var dimValue = value[dim];
            if (dimValue === "viewDuration") dimValue = this.viewDuration;
            ga("set", dim, dimValue);
            cdim[dim] = dimValue;
          }
          value = cdim;
        }

        vo[key.toLowerCase()] = value;
      }

      return vo;
    }
  }, {
    key: 'trackEvent',
    value: function trackEvent(eventName) {
      var type = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : "video";

      var obj = this.generateGoogleAnalyticsVO(eventName, type);
      if (obj == null) return;

      ga("send", "event", obj.eventcategory, obj.eventaction, obj.eventlabel, obj.eventvalue);

      if (obj.customdimension == null) return;

      for (var dim in obj.customdimension) {
        // TODO: Why is this being set to null? "set" is called in the VO function with a non-null value
        ga("set", dim, null);
      }
    }
  }, {
    key: 'onready',
    value: function onready() {
      this.bindHandlers(["onadstarted", "onadended"]);
      this.player.addEventListener("adstarted", this.onadstarted);
      this.player.addEventListener("adended", this.onadended);
    }
  }, {
    key: 'onstarted',
    value: function onstarted() {
      this.started = true;
      this.currentTime = 0;
      this.viewDuration = 0;
      this.trackEvent("started");
    }
  }, {
    key: 'onmediachange',
    value: function onmediachange() {
      this.onended();
    }
  }, {
    key: 'onended',
    value: function onended() {
      if (this.started === false) return;

      this.started = false;
      this.trackEvent("ended");
    }
  }, {
    key: 'ontimeupdate',
    value: function ontimeupdate(event) {
      var currentTime = Math.round(event.detail);
      if (currentTime > this.currentTime) {
        this.viewDuration++;
        this.currentTime = currentTime;
      }
    }
  }, {
    key: 'onadstarted',
    value: function onadstarted(event) {
      this.trackEvent("started", "ads");
    }
  }, {
    key: 'onadended',
    value: function onadended(adVO) {
      this.trackEvent("ended", "ads");
    }
  }]);
  return GoogleAnalytics;
}(akamai.amp.Plugin);

akamai.amp.AMP.registerPlugin("googleanalytics", akamai.amp.Plugin.createFactory(GoogleAnalytics));

exports.GoogleAnalytics = GoogleAnalytics;

}((this.akamai.amp.googleanalytics = this.akamai.amp.googleanalytics || {})));
//# sourceMappingURL=Googleanalytics.js.map
