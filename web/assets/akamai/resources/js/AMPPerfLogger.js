function AMPPerfLogger(config) {
  this.config = config;
  this.markers = {};
  this.handlers = {};
  
  document.addEventListener("DOMContentLoaded", this.timeStamp.bind(this, "dom-loaded"));
  window.addEventListener("load", this.onpageload.bind(this));
  
  this.timeStamp("perf-started");
}

AMPPerfLogger.prototype = {
  markers: null,
  handlers: null,
  onpageload: function (event) {
    this.timeStamp("page-loaded");
    this.time("amp-total");
    
    var script = document.createElement("script");
    script.src = this.config.script;
    script.onload = this.onscriptload.bind(this);
    
    this.time("amp-script");
    document.head.appendChild(script);
  },
  onscriptload: function (event) {
    this.timeEnd("amp-script");
    
    var xhr = new XMLHttpRequest();
    xhr.open("GET", this.config.config);
    xhr.onload = this.onconfigload.bind(this);
    this.time("amp-config");
    xhr.send();
  },
  onconfigload: function (event) {
      this.timeEnd("amp-config");
      
      this.time("amp-config-parse");
      var config = JSON.parse(event.currentTarget.responseText);
      this.timeEnd("amp-config-parse");
      
      this.timeStamp("amp-create");
      amp = akamai.amp.AMP.create(this.config.player, config, this.onready.bind(this));
  },
  onready: function (event) {
    this.timeStamp("amp-ready");
    
    this.handlers.playing = this.onplaying.bind(this);
    amp.addEventListener("playing", this.handlers.playing);
    if (amp.ads)
      amp.ads.addEventListener("started", this.handlers.playing);
      
    amp.play();
  },
  onplaying: function (event) {
    this.timeStamp("amp-playing");
    this.timeEnd("amp-total");
    
    amp.removeEventListener("playing", this.handlers.playing);
    if (amp.ads)
      amp.ads.removeEventListener("started", this.handlers.playing);
      
    this.log("Ellapsed", [
      "DOM load: " + this.toSec("dom-loaded") + "s",
      "Page load: " + this.toSec("page-loaded") + "s",
      "AMP create: " + this.toSec("amp-create") + "s",
      "AMP ready: " + this.toSec("amp-ready") + "s",
      "AMP playing: " + this.toSec("amp-playing") + "s"
    ]);
    
    this.log("Diff", [
      "AMP js load: " + this.toSec("amp-script") + "s",
      "AMP config load: " + this.toSec("amp-config") + "s",
      "AMP config parse: " + this.toSec("amp-config-parse") + "s",
      "AMP from create to ready: " + this.diffToSec("amp-create", "amp-ready") + "s",
      "AMP from ready to playing: " + this.diffToSec("amp-ready", "amp-playing") + "s",
      "AMP from create to playing: " + this.diffToSec("amp-create", "amp-playing") + "s",
      "AMP time to video: " + this.toSec("amp-total") + "s"
    ]);
  },
  timeStamp: function (label) {
    this.markers[label] = performance.now();
  },
  diff: function (from, to) {
    return this.markers[to] - this.markers[from];
  },
  toSec: function (value) {
    if (typeof value == "string")
      value = this.markers[value];
    return Number(value / 1000).toFixed(3);
  },
  diffToSec: function (from, to) {
    return this.toSec(this.markers[to] - this.markers[from]);
  },
  log: function (title, logs) {
    var log = "<dl><dt>" + title + "</dt>";
    logs.forEach(function (item) { log += "<dd>" + item + "</dd>"; });
    document.querySelector(this.config.logs).innerHTML += log + "</dl>";
  },
  time: function (label) {
    console.time(label);
    this.timeStamp(label);
  },
  timeEnd: function (label) {
    console.timeEnd(label);
    this.markers[label] = performance.now() - this.markers[label]
  }
}