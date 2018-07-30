function PerfLogger() {
	this.timer = (new Date).getTime(); 
	this.time = new Object();
	this.time.domContentLoaded = performance.now();
	if(window.addEventListener) {
		window.addEventListener("load", this.onload.bind(this));
	}
	else if(window.attachEvent) {
		window.addEventListener("onload", this.onload.bind(this));
	}
}
PerfLogger.constructor = PerfLogger;
PerfLogger.prototype = {
	timer: null,
	time: null,
	active: false,
	amp: null,
	host: null,
	instance: 0,
	instances: [],
	events: [{context:"amp", type:"flashCreated"}, {context:"amp", type:"ready"}, {context:"amp", type:"mediaPlayerSwfInitialized"}, {context:"amp", type:"mediaPlayerSwfLoaded"}, {context:"amp", type:"mediaPlayerAssetsInitialized"},
			{context:"amp", type:"mediaPlayerViewInitialized"}, {context:"amp", type:"mediaPlayerOsmfPluginsLoaded"}, {context:"amp", type:"mediaPlayerPluginLoaded"}, {context:"amp", type:"mediaLoadStateUninitialized"},
			{context:"amp", type:"mediaLoadStateLoadError"}, {context:"amp", type:"mediaLoadStateLoading"}, {context:"amp", type:"mediaLoadStateReady"}, {context:"amp", type:"mediaLoadStateUnloading"},
			{context:"amp", type:"mediaPlayerConfigLoaded"}, {context:"amp", type:"mediaPlayerConfigProcessed"}, {context:"amp", type:"mediaPlayerUpdateTokenContext"}, {context:"amp", type:"mediaPlayerTokenContextUpdated"}, 
			{context:"amp", type:"loadfeed"}, {context:"amp", type:"mediaPlayerDataFeedUpdated"}, {context:"amp", type:"mediachange"}, {context:"amp", type:"mediaPlayerResourceResolutionComplete"}, {context:"amp", type:"mediaPlayerResourceDependencyLoaded"},
			{context:"amp", type:"mediaPlayerResourceDependenciesLoaded"}, {context:"amp", type:"mediaPlayerInitialized"}, {context:"amp", type:"ready"}, {context:"amp", type:"playrequest"}, {context:"amp", type:"mediaPlayerPlaybackInitialized"}, 
			{context:"amp.ads", type:"adComponentLoaded"}, {context:"amp.ads", type:"adComponentAdsRequest"}, {context:"amp.ads", type:"loaded"}, {context:"amp.ads", type:"error"}, {context:"amp.ads", type:"adComponentInitialized"}, 
			{context:"amp.ads", type:"started"}, {context:"amp.ads", type:"impression"}, {context:"amp.ads", type:"ended"}, {context:"amp", type:"started"}, {context:"amp", type:"canplay"}, {context:"amp", type:"ended"}],
	onload: function() {
		this.time.pageloaded = typeof(this.time.pageloaded) == 'undefined' ? performance.now() : (new Date).getTime() - this.timer;
		this.time.basetime = this.time.pageloaded;
		this.time.playbasetime = 0;
	},
	start: function(source, host) {
		this.active = true;
		this.amp = source;
		this.host = host;
		this.setupEventListeners(true, this.events);
	},
	stop: function() {
		this.active = false;
	},
	setupEventListeners: function(add, events) {
		if(this.amp) {
			var i = 0;
			for(; i < this.events.length; i++) {
				var event = this.events[i], scope = this, context = event["context"].split("."), g = 0;
				for(; g < context.length; g++) {
					scope = scope[context[g]] || scope;
				}
				if(scope)
				{
					if(add) {
						scope.addEventListener(event["type"], this.eventHandler.bind(this));
					}
					else {
						scope.removeEventListener(event["type"], this.eventHandler.bind(this));
					}
				}
			}
		}
	},
	eventHandler: function(event) {
		if(this.active) {
			var type = event.type;
			//
			// Handle special cases
			//
			// Handle plugin events
			if(type == "mediaPlayerPluginLoaded") {
				this.time[event.data.plugin.id] = this.getTimeDelta(this.time, event.data.plugin.id);
				return;
			}
			if(type == "mediaPlayerResourceDependencyLoaded") {
				this.time[event.data.resourceName] = this.getTimeDelta(this.time, event.data.resourceName);
				return;
			}
			// Handle ad events
			if(this.amp && this.amp.ads && (event.target === this.amp.ads)) {
				type = "ad"+type;
				if(type == "adended") {
					this.time["playbasetime"] = performance.now() - this.time.basetime;
				}
			}
			// Standard event case
			this.time[type] = this.getTimeDelta(this.time, type);
			// Log performance when media begins to play
			if(type == "started") {
				this.logPerformance();
			}
		}
	},
	logPerformance: function() {
			var col = [];
			for(var key in this.time)
				col.push(key + ": " + this.time[key]);
			col.sort(function(a,b){
				return a-b;
			});
			this.amp.logger.log("[AMP EVENT] " + "performance", {perf: col});
	},
	getTimeDelta: function(host, key) {
		if(host && key) {
			var rawDelta = typeof(host[key]) == 'undefined' ? performance.now() : (new Date).getTime() - this.timer;
			var baseDelta = rawDelta - Math.max(host.playbasetime, host.basetime);
			return "WHOLE - " + rawDelta + " | REAL - " + baseDelta;
		}
	},
};