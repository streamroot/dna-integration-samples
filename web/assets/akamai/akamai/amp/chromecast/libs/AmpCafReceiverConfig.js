export default {
  debug: true,
  plugins: {
    mediaanalytics: {
      debug: true,
      resources: [
        {src: "../akamai/amp/mediaanalytics/Mediaanalytics.js", type: "text/javascript"},
        {src: "../akamai/amp/mediaanalytics/javascript_malibrary.js", type: "text/javascript"}
      ],
      config: "//ma188-r.analytics.edgesuite.net/config/beacon-2114.xml",
      plugin: {
        swf: "//79423.analytics.edgesuite.net/csma/plugin/csma.swf"
      },
      iplookup: false,
      dimensions: {
        eventName: "AMP Sample Event",
        title: "AMP Sample Title",
        playerId: "#{player.mode} Player"
      }
    }
  }
}
