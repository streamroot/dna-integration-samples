export default class AmpCaf extends akamai.amp.EventDispatcher {

  /**
   *
   */
  constructor(config = {}, readyHandler) {
    super()
    this.config = config
    this.debug = (config.debug === true)
    this.logger = akamai.amp.Logger.instance = new akamai.amp.Logger(config.debug)

    this.transformer = new akamai.amp.Transformer()
    this.context = cast.framework.CastReceiverContext.getInstance()
    this.context.addCustomMessageListener("urn:x-cast:com.akamai.amp.cast", (message) => {
      this.logger.log("[AMP CHROMECAST MESSAGE]", message)
    })

    // listen to all Events
    this.handleEvent = this.handleEvent.bind(this)
    this.playerManager = this.context.getPlayerManager()
    this.playerManager.addEventListener(cast.framework.events.category.CORE, this.handleEvent)
    this.playerManager.addEventListener(cast.framework.events.category.DEBUG, this.handleEvent)
    this.playerManager.addEventListener(cast.framework.events.category.FINE, this.handleEvent)
    this.playerManager.setMessageInterceptor(
        cast.framework.messages.MessageType.LOAD,
        request => {
          return this.transform(akamai.amp.TransformType.MEDIA, request.media.customData.media)
            .then((media) => {
              let src = media.src
              const auth = media.authorization
              if (auth && auth.token) {
                src += (!/\?/.test(src)) ? "?" : "&"
                if (auth.key != null) {
                  src += `${auth.key}=`
                }
                src += auth.token
              }
              request.media.contentId = src
              return request
            })
            .catch((error) => this.logger.error(error))
        })

    const timeTransform = (status) => {
      if (status.currentTime != null) {
        return this.transform(akamai.amp.TransformType.TIME, status.currentTime)
          .then((time) => {
            status.currentTime = time
            return status
          })
          .catch((error) => this.logger.error(error))
      }
      return status
    }

    this.playerManager.setMessageInterceptor(cast.framework.messages.MessageType.MEDIA_STATUS, timeTransform)
    this.playerManager.setMessageInterceptor(cast.framework.messages.MessageType.DISPLAY_STATUS, timeTransform)
    this.playerManager.setMessageInterceptor(
        cast.framework.messages.MessageType.SEEK,
        request => {
          return this.transform(akamai.amp.TransformType.TIME, request.currentTime)
            .then((time) => {
              request.currentTime = time
              return request
            })
            .catch((error) => this.logger.error(error))
        })

    if (readyHandler) {
      this.addEventListener("ready", readyHandler)
    }

    this.loadPlugins()
      .then(() => {
        this.dispatchEvent(new akamai.amp.Event("ready"))

        const requestHandler = (requestInfo) => {
          const auth = this.media.authorization
          if (auth && auth.token)
            requestInfo.withCredentials = true
        }
        const playbackConfig = new cast.framework.PlaybackConfig()
        playbackConfig.manifestRequestHandler = requestHandler
        playbackConfig.segmentRequestHandler = requestHandler
        this.context.start({playbackConfig})
      })
      .catch((error) => this.logger.error(error))
  }

  /**
   *
   */
  get mediaElement() {
    return document.querySelector("cast-media-player").shadowRoot.querySelector("video")
  }

  /**
   *
   */
  get src() {
    return this.media.src
  }

  /**
   *
   */
  get mode() {
    return "html"
  }

  /**
   *
   */
  get autoplay() {
    return true
  }

  /**
   *
   */
  loadPlugins(plugins = this.config.plugins) {
    const instances = []
    const type = "html"

    if (plugins == null) {
      return Promise.resolve([])
    }

    for (let key in plugins) {
      let config = plugins[key]
      if (config == null || config.enabled === false || config.disabled === true) {
        continue
      }
      instances.push(this.loadPlugin(config, key, type))
    }

    return Promise.all(instances)
  }

  /**
   *
   */
  loadPlugin(config, key, type) {
    return this.loadResources(config.resources)
      .then(() => {
        const def = akamai.amp.AMP.plugins[key][type]
        if (def == null) {
          throw new Error(`[AMP] Plugin could not be found: ${key}`)
        }
        return def(this, config, key)
          .then((plugin) => {
            this[key] = plugin
            if (plugin.feature != null) {
              this[plugin.feature] = plugin
            }
            this.logger.log(`[AMP] Plugin registered: ${key}`)
            return plugin
          })
      })
  }

  /**
   *
   */
  loadResources(resources = this.config.resources) {
    if (resources == null || resources.length == 0) {
      return Promise.resolve()
    }

    for (let resource of resources) {
      resource.src = this.evaluatePaths(resource.src)
      if (resource.debug != null) {
        resource.debug = this.evaluatePaths(resource.debug)
      }
    }
    return akamai.amp.AMP.addResources(resources)
  }

  /**
   *
   */
  evaluatePaths(path) {
    if (path == null) {
      return
    }

    const paths = this.config.paths

    if (paths != null) {
      path = akamai.amp.DataBinding.eval(path, {paths})
    }

    return path
  }

  /**
   *
   */
  evaluateBindings(value, context) {
    const data = {
      media: this.media,
      player: {
        mode: "html5"
      },
      now: Date.now()
    }
    return akamai.amp.DataBinding.evaluateBindings(value, akamai.amp.Utils.override(data, context))
  }

  /**
   *
   */
  handleEvent(event) {
    const EventType = cast.framework.events.EventType
    let type, detail

    switch (event.type) {
      case EventType.REQUEST_LOAD:
        this.readyState = 0
        this.currentTime = 0
        this.ended = false
        break

      case EventType.PLAYER_LOADING:
        type = "mediasequencestarted"
        this.media = event.media.customData.media
        this.dispatch("mediachange", this.media)
        this.dispatch("playrequest")
        break

      case EventType.ABORT:
        type = "mediasequenceaborted"
        break

      case EventType.PLAY:
        type = "play"
        this.paused = false
        break

      case EventType.PLAYING:
        type = "playing"
        break

      case EventType.SEEKING:
        type = "seeking"
        this.seeking = true
        break

      case EventType.SEEKED:
        type = "seeked"
        detail = this.currentTime
        this.seeking = false
        break

      case EventType.PAUSE:
        type = "pause"
        this.paused = true
        break

      case EventType.DURATION_CHANGE:
        const duration = this.playerManager.getMediaInformation().duration
        if (duration != this.duration) {
          this.duration = duration
          type = "durationchange"
          detail = this.duration
        }
        break

      case EventType.TIME_UPDATE:
        this.currentTime = event.currentMediaTime
        type = "timeupdate"
        detail = this.currentTime
        break

      case EventType.PROGRESS:
        type = "progress"
        break

      case EventType.CAN_PLAY:
        type = "canplay"
        this.readyState = 3
        break

      case EventType.CAN_PLAY_THROUGH:
        type = "canplaythrough"
        this.readyState = 4
        break

      case EventType.LOADED_METADATA:
        type = "loadedmetadata"
        this.duration = this.playerManager.getMediaInformation().duration
        this.readyState = 1
        break

      case EventType.LOADED_DATA:
        type = "loadeddata"
        this.readyState = 2
        break

      case EventType.LOAD_START:
        type = "loadstart"
        break

      case EventType.BUFFERING:
        type = "bufferingchange"
        detail = event.isBuffering
        break

      case EventType.BITRATE_CHANGED:
        type = "qualitychange"
        detail = {bitrate: event.totalBitrate}
        break

      case EventType.WAITING:
        type = "waiting"
        break

      case EventType.STALLED:
        type = "stalled"
        break

      case EventType.ID3:
        type = "timedmetadata"
        detail = {
          startTime: this.currentTime,
          endTime: this.currentTime,
          value: akamai.amp.ID3.getID3(event.segmentData)
        }
        break

      case EventType.ENDED:
        type = "mediasequenceended"
        this.ended = true
        this.dispatch("ended")
        break

      case EventType.ERROR:
        type = "error"
        break
    }

    if (type != null) {
      this.dispatch(type, detail)
    }
    else {
      this.logger.log(`[CHROMECAST] ${event.type}`, event)
    }
  }

  /**
   *
   */
  dispatchEvent(event) {
    if (event.type != "timeupdate" && event.type != "progress") {
      this.logger.log(`[AMP EVENT] ${event.type}`, event)
    }
    super.dispatchEvent(event)
  }

  /**
   * Adds a transform for a given type.
   *
   * @param {!string}  type  A string representing the event's type.
   * @param {!Function} func  A function to call when the event is triggered.
   */
  addTransform(type, transform) {
    return this.transformer.addTransform(type, transform)
  }

  /**
   * Performs a transform for a given type
   *
   * @param {!String} type The tranform type
   * @param {!Object} value The value to be transformed
   */
  transform(type, value) {
    return this.transformer.transform(type, value)
  }

  /**
   * Removes a transform for a given type.
   *
   * @param {!string}  type  A string representing the tranform's type.
   * @param {!Function} transform  A function or Transform object to call when the type is triggered.
   * @return {?Function} the transform that was removed if any
   */
  removeTransform(type, transform) {
    return this.transformer.removeTransform(type, tranform)
  }

}
