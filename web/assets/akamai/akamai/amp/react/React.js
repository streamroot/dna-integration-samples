(function (exports) {
'use strict';

var ClassList = function () {
  function ClassList(component) {
    var tokens = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : [];
    babelHelpers.classCallCheck(this, ClassList);

    this.component = component;
    this.tokens = tokens;
  }

  babelHelpers.createClass(ClassList, [{
    key: 'add',
    value: function add(token) {
      var prepend = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;

      if (this.contains(token)) return;

      if (prepend === true) {
        this.tokens.unshift(token);
      } else {
        this.tokens.push(token);
      }
    }
  }, {
    key: 'contains',
    value: function contains(token) {
      return this.tokens.indexOf(token) !== -1;
    }
  }, {
    key: 'item',
    value: function item(index) {
      return this.tokens[index] || null;
    }
  }, {
    key: 'remove',
    value: function remove(token) {
      var i = this.tokens.indexOf(token);
      if (i === -1) return;
      this.tokens.splice(i, 1);
    }
  }, {
    key: 'toggle',
    value: function toggle(token) {
      if (this.contains(token)) {
        this.remove(token);
      } else {
        this.add(token);
      }
    }
  }, {
    key: 'update',
    value: function update(tokenMap) {
      for (var token in tokenMap) {
        if (tokenMap[token]) {
          this.add(token);
        } else {
          this.remove(token);
        }
      }

      var className = this.toString();
      if (className != this.component.state.className) {
        this.component.setState({ className: className });
      }
    }
  }, {
    key: 'toString',
    value: function toString() {
      return this.tokens.join(' ');
    }
  }]);
  return ClassList;
}();

var Component = function (_React$Component) {
  babelHelpers.inherits(Component, _React$Component);

  function Component(props, context) {
    babelHelpers.classCallCheck(this, Component);

    var _this = babelHelpers.possibleConstructorReturn(this, (Component.__proto__ || Object.getPrototypeOf(Component)).call(this, props, context));

    _this.state = {
      l10n: _this.player.l10n
    };
    _this.classList = new ClassList(_this, _this.props.classList);
    return _this;
  }

  babelHelpers.createClass(Component, [{
    key: "getLocalizedString",
    value: function getLocalizedString(key) {
      return this.player.getLocalizedString(key);
    }
  }, {
    key: "onlanguagechange",
    value: function onlanguagechange() {
      this.setState({ l10n: this.player.l10n });
    }
  }, {
    key: "bindHandlers",
    value: function bindHandlers(handlers) {
      var _this2 = this;

      if (handlers == null || handlers.length <= 0) return;

      handlers.forEach(function (handler) {
        if (typeof _this2[handler] != "function") return;
        _this2[handler] = _this2[handler].bind(_this2);
      });
    }
  }, {
    key: "shouldComponentUpdate",
    value: function shouldComponentUpdate(nextProps, nextState) {
      var state = this.state;
      var props = this.props;

      if (nextState != null) {
        for (var key in nextState) {
          if (nextState[key] != state[key]) {
            return true;
          }
        }
      }

      // TODO: Is this needed?
      if (nextProps != null) {
        for (var _key in nextProps) {
          if (nextProps[_key] != props[_key]) {
            return true;
          }
        }
      }

      return false;
    }
  }, {
    key: "componentWillMount",
    value: function componentWillMount() {
      var _this3 = this;

      this.playerEventMap.forEach(function (event) {
        _this3[event.key] = _this3[event.key].bind(_this3);
        _this3.player.addEventListener(event.type, _this3[event.key]);
      });
    }
  }, {
    key: "componentWillUnmount",
    value: function componentWillUnmount() {
      var _this4 = this;

      this.playerEventMap.forEach(function (event) {
        _this4.player.removeEventListener(event.type, _this4[event.key]);
      });
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement(
        "div",
        babelHelpers.extends({ ref: "element" }, this.propsList, { className: this.className }),
        this.textContent,
        this.children
      );
    }
  }, {
    key: "player",
    get: function get() {
      return this.props.player || this.context.player;
    }
  }, {
    key: "config",
    get: function get() {
      return this.props.config || this.context.config;
    }
  }, {
    key: "l10n",
    get: function get() {
      return this.state.l10n;
    }
  }, {
    key: "plugin",
    get: function get() {
      return this.context.plugin;
    }
  }, {
    key: "className",
    get: function get() {
      var className = this.props.className || "";
      className += " " + this.classList;

      return className.trim();
    }
  }, {
    key: "textContent",
    get: function get() {
      return this.props.textContent || this.state.textContent;
    }
  }, {
    key: "children",
    get: function get() {
      return this.props.children || [];
    }
  }, {
    key: "element",
    get: function get() {
      return this.refs.element;
    }
  }, {
    key: "dom",
    get: function get() {
      return this.element || ReactDOM.findNode(this);
    }
  }, {
    key: "data",
    get: function get() {
      return this.props.data || {};
    }
  }, {
    key: "playerEventMap",
    get: function get() {
      var _this5 = this;

      var events = [];

      if (this.player == null) return events;

      Object.values(akamai.amp.Events).forEach(function (type) {
        var key = "on" + type;
        if (typeof _this5[key] != "function") return;
        events.push({ key: key, type: type });
      });

      return events;
    }
  }, {
    key: "propsList",
    get: function get() {
      var _props = this.props,
          className = _props.className,
          classList = _props.classList,
          data = _props.data,
          textContent = _props.textContent,
          props = babelHelpers.objectWithoutProperties(_props, ["className", "classList", "data", "textContent"]);

      return props;
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return {
        player: PropTypes.object,
        config: PropTypes.object,
        plugin: PropTypes.object
      };
    }
  }]);
  return Component;
}(React.Component);

var Container = function (_Component) {
  babelHelpers.inherits(Container, _Component);

  function Container(props, context) {
    babelHelpers.classCallCheck(this, Container);

    var _this = babelHelpers.possibleConstructorReturn(this, (Container.__proto__ || Object.getPrototypeOf(Container)).call(this, props, context));

    _this.state.components = [];
    return _this;
  }

  // TODO: HACK! This is a hack to get around IE 9-10 not inheriting static properties.
  //       Remove this when these browsers are dropped, along with similar code
  //       in other classes that extend Component and Container.


  babelHelpers.createClass(Container, [{
    key: "addComponent",
    value: function addComponent(component) {
      var components = this.state.components.slice();
      if (component.key == null) component.key = akamai.amp.Utils.createUID();
      components.push(component);
      this.setState({ components: components });
    }
  }, {
    key: "removeComponent",
    value: function removeComponent(component) {
      var components = this.state.components.filter(function (item) {
        return item != component;
      });
      this.setState({ components: components });
    }
  }, {
    key: "clear",
    value: function clear() {
      this.setState({ components: [] });
    }
  }, {
    key: "appendChild",
    value: function appendChild(element) {
      var parent = this.dom;
      if (parent == null) return;
      parent.appendChild(element);
    }
  }, {
    key: "removeChild",
    value: function removeChild(element) {
      var parent = this.dom;
      if (parent == null) return;
      parent.removeChild(element);
    }
  }, {
    key: "components",
    get: function get() {
      return this.state.components;
    }
  }, {
    key: "children",
    get: function get() {
      return babelHelpers.get(Container.prototype.__proto__ || Object.getPrototypeOf(Container.prototype), "children", this).concat(this.state.components);
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Container.__proto__ || Object.getPrototypeOf(Container), "contextTypes", this);
    }
  }]);
  return Container;
}(Component);

var Control = function (_Component) {
  babelHelpers.inherits(Control, _Component);

  function Control(props, context) {
    babelHelpers.classCallCheck(this, Control);

    var _this = babelHelpers.possibleConstructorReturn(this, (Control.__proto__ || Object.getPrototypeOf(Control)).call(this, props, context));

    _this.classList.add("amp-icon");
    _this.classList.add("amp-control");
    _this.mouseFocus = false;
    _this.focused = false;
    _this.state.altText = _this.altText;

    _this.bindHandlers(["onMouseDown", "onMouseUp", "onMouseOver", "onMouseOut", "onFocus", "onBlur", "onKeyDown", "onClick", "onChange"]);
    return _this;
  }

  babelHelpers.createClass(Control, [{
    key: "onMouseDown",
    value: function onMouseDown(event) {
      this.mouseFocus = true;
    }
  }, {
    key: "onMouseUp",
    value: function onMouseUp(event) {
      this.mouseFocus = false;
    }
  }, {
    key: "onMouseOver",
    value: function onMouseOver(event) {}
  }, {
    key: "onMouseOut",
    value: function onMouseOut(event) {}
  }, {
    key: "onFocus",
    value: function onFocus(event) {
      if (this.mouseFocus == true) return;

      this.focused = true;
      this.classList.update({ "amp-focus": true });
      //Hack to Prevent overflow hidden elemnts to scroll up on tabbing
      this.player.viewComponent.scrollTop = 0;
    }
  }, {
    key: "onBlur",
    value: function onBlur(event) {
      if (this.mouseFocus == true) return;

      this.focused = false;
      this.classList.update({ "amp-focus": false });
    }
  }, {
    key: "onKeyDown",
    value: function onKeyDown(event) {
      if (this.mouseFocus == true) return;

      this.onKeyPress(event);

      if (event.type == "keypress" && (event.keyCode == 13 || event.keyCode == 32)) this.onAction();
    }
  }, {
    key: "onAction",
    value: function onAction() {
      this.onClick();
    }
  }, {
    key: "onKeyPress",
    value: function onKeyPress(event) {}
  }, {
    key: "onClick",
    value: function onClick(event) {}
  }, {
    key: "change",
    value: function change(value) {
      var onChange = this.propsList.onChange || this.onChange;
      if (typeof onChange != "function") return;
      onChange(value);
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement(
        "button",
        babelHelpers.extends({ ref: "element" }, this.propsList, { className: this.className }),
        this.textContent,
        this.children
      );
    }
  }, {
    key: "aria",
    get: function get() {
      return {
        "aria-label": this.title,
        role: "button"
      };
    }
  }, {
    key: "propsList",
    get: function get() {
      var _this2 = this;

      var _babelHelpers$get = babelHelpers.get(Control.prototype.__proto__ || Object.getPrototypeOf(Control.prototype), "propsList", this),
          altText = _babelHelpers$get.altText,
          props = babelHelpers.objectWithoutProperties(_babelHelpers$get, ["altText"]);

      props.className = this.className;
      props.tabIndex = "0";
      props.onMouseDown = function (event) {
        return (_this2.props.onMouseDown || _this2.onMouseDown)(event);
      };
      props.onMouseUp = function (event) {
        return (_this2.props.onMouseUp || _this2.onMouseUp)(event);
      };
      props.onMouseOver = function (event) {
        return (_this2.props.onMouseOver || _this2.onMouseOver)(event);
      };
      props.onMouseOut = function (event) {
        return (_this2.props.onMouseOut || _this2.onMouseOut)(event);
      };
      props.onFocus = function (event) {
        return (_this2.props.onFocus || _this2.onFocus)(event);
      };
      props.onBlur = function (event) {
        return (_this2.props.onBlur || _this2.onBlur)(event);
      };
      props.onKeyDown = function (event) {
        return (_this2.props.onKeyDown || _this2.onKeyDown)(event);
      };
      props.onClick = function (event) {
        return (_this2.props.onClick || _this2.onClick)(event);
      };

      var aria = this.aria;
      for (var key in aria) {
        props[key] = aria[key];
      }

      return props;
    }
  }, {
    key: "altText",
    get: function get() {
      return this.props.altText || this.state.altText;
    }
  }, {
    key: "title",
    get: function get() {
      return this.player.getLocalizedString(this.altText);
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Control.__proto__ || Object.getPrototypeOf(Control), "contextTypes", this);
    }
  }]);
  return Control;
}(Component);

var PanelControl = function (_Control) {
  babelHelpers.inherits(PanelControl, _Control);

  function PanelControl(props, context) {
    babelHelpers.classCallCheck(this, PanelControl);
    return babelHelpers.possibleConstructorReturn(this, (PanelControl.__proto__ || Object.getPrototypeOf(PanelControl)).call(this, props, context));
  }

  babelHelpers.createClass(PanelControl, [{
    key: "onClick",
    value: function onClick() {
      this.panel.toggle();
    }
  }, {
    key: "onKeyDown",
    value: function onKeyDown(event) {
      this.panel.onKeyDown(event);
    }
  }, {
    key: "propsList",
    get: function get() {
      var _babelHelpers$get = babelHelpers.get(PanelControl.prototype.__proto__ || Object.getPrototypeOf(PanelControl.prototype), "propsList", this),
          panel = _babelHelpers$get.panel,
          props = babelHelpers.objectWithoutProperties(_babelHelpers$get, ["panel"]);

      return props;
    }
  }, {
    key: "panel",
    get: function get() {
      return this.plugin.refs[this.props.panel];
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(PanelControl.__proto__ || Object.getPrototypeOf(PanelControl), "contextTypes", this);
    }
  }]);
  return PanelControl;
}(Control);

var PlayPause = function (_Control) {
  babelHelpers.inherits(PlayPause, _Control);

  function PlayPause(props, context) {
    babelHelpers.classCallCheck(this, PlayPause);

    var _this = babelHelpers.possibleConstructorReturn(this, (PlayPause.__proto__ || Object.getPrototypeOf(PlayPause)).call(this, props, context));

    _this.classList.add("amp-playpause", true);
    _this.state.altText = "MSG_PLAY";
    return _this;
  }

  babelHelpers.createClass(PlayPause, [{
    key: "onplaying",
    value: function onplaying() {
      this.setState({ altText: "MSG_PAUSE" });
    }
  }, {
    key: "onpause",
    value: function onpause() {
      this.setState({ altText: "MSG_PLAY" });
    }
  }, {
    key: "onended",
    value: function onended() {
      this.setState({ altText: "MSG_REPLAY" });
    }
  }, {
    key: "onClick",
    value: function onClick() {
      var player = this.player;
      switch (player.playState) {
        case "ended":
          this.plugin.replay();
          break;

        case "ready":
        case "paused":
          player.play();
          break;

        default:
          player.pause();
          break;
      }
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(PlayPause.__proto__ || Object.getPrototypeOf(PlayPause), "contextTypes", this);
    }
  }]);
  return PlayPause;
}(Control);

var PauseOverlay = function (_Control) {
  babelHelpers.inherits(PauseOverlay, _Control);

  function PauseOverlay(props, context) {
    babelHelpers.classCallCheck(this, PauseOverlay);

    var _this = babelHelpers.possibleConstructorReturn(this, (PauseOverlay.__proto__ || Object.getPrototypeOf(PauseOverlay)).call(this, props, context));

    _this.classList.add("amp-pause-overlay", true);
    _this.state.altText = "MSG_PLAY";
    return _this;
  }

  babelHelpers.createClass(PauseOverlay, [{
    key: "onClick",
    value: function onClick() {
      var player = this.player;
      switch (player.playState) {
        case "ended":
          this.plugin.replay();
          break;

        default:
          player.play();
          break;
      }
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(PauseOverlay.__proto__ || Object.getPrototypeOf(PauseOverlay), "contextTypes", this);
    }
  }]);
  return PauseOverlay;
}(Control);

var BufferingOverlay = function (_Component) {
  babelHelpers.inherits(BufferingOverlay, _Component);

  function BufferingOverlay(props, context) {
    babelHelpers.classCallCheck(this, BufferingOverlay);

    var _this = babelHelpers.possibleConstructorReturn(this, (BufferingOverlay.__proto__ || Object.getPrototypeOf(BufferingOverlay)).call(this, props, context));

    _this.classList.add("amp-buffering-overlay", true);
    _this.classList.add("amp-icon");
    return _this;
  }

  babelHelpers.createClass(BufferingOverlay, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(BufferingOverlay.__proto__ || Object.getPrototypeOf(BufferingOverlay), "contextTypes", this);
    }
  }]);
  return BufferingOverlay;
}(Component);

var Utils = function () {
  function Utils() {
    babelHelpers.classCallCheck(this, Utils);
  }

  babelHelpers.createClass(Utils, null, [{
    key: "formatTimecode",


    /**
     * Takes a time in seconds and converts it to timecode.
     *
     * @param   {Number}  time  The time in seconds to be formatted.
     * @return  {String}  A SMTP formatted string.
     */
    value: function formatTimecode(time, duration) {
      time = parseInt(time);
      if (isNaN(time)) {
        return "00:00";
      }
      var strTime = Utils.formatZeroFill(time % 60);
      time = parseInt(time / 60);
      strTime = Utils.formatZeroFill(time % 60) + ":" + strTime;
      time = parseInt(time / 60);
      if (time > 0) {
        strTime = Utils.formatZeroFill(time) + ":" + strTime;
      }
      if (duration >= 3600 && strTime.length === 5) {
        strTime = "00:" + strTime;
      }
      return strTime;
    }

    /**
     * Converts a time in seconds to a string and adds a zero in front of any number lower than 10.
     *
     * @param {Number} time The number to be zero filled.
     */

  }, {
    key: "formatZeroFill",
    value: function formatZeroFill(time) {
      var str = time.toString();
      if (time < 10) {
        str = "0" + str;
      }
      return str;
    }

    /**
     * @param {Number} number
     *    The number to be rounded.
     *
     * @param {Number} precision
     *    The level of precision. Positive 1 would round to 1 decimal place and -1 would round to the tens.
     *
     * @return {Number}
     *    The rounded number.
     */

  }, {
    key: "round",
    value: function round(number, precision) {
      var factor = Math.pow(10, precision);
      return Math.round(number * factor) / factor;
    }
  }, {
    key: "getEventPos",
    value: function getEventPos(event) {
      return {
        x: event.pageX - (window.scrollX || window.pageXOffset) || event.targetTouches[0].pageX - window.scrollX,
        y: event.pageY - (window.scrollY || window.pageYOffset) || event.targetTouches[0].pageY - window.scrollY
      };
    }
  }, {
    key: "componentEnabled",
    value: function componentEnabled(plugin, component) {
      var config = plugin.config[component];
      return plugin.player[component] != null && plugin.player[component].enabled !== false || config != null && config !== false && config.enabled !== false && config.disabled !== true;
    }
  }, {
    key: "isNumber",
    value: function isNumber(n) {
      return !isNaN(parseFloat(n)) && isFinite(n);
    }
  }]);
  return Utils;
}();

var CurrentTime = function (_Component) {
  babelHelpers.inherits(CurrentTime, _Component);

  function CurrentTime(props, context) {
    babelHelpers.classCallCheck(this, CurrentTime);

    var _this = babelHelpers.possibleConstructorReturn(this, (CurrentTime.__proto__ || Object.getPrototypeOf(CurrentTime)).call(this, props, context));

    _this.state.currentTime = _this.player.currentTime;
    _this.state.duration = _this.player.duration;
    return _this;
  }

  babelHelpers.createClass(CurrentTime, [{
    key: "onmediachange",
    value: function onmediachange(event) {
      this.setState({ currentTime: 0 });
    }
  }, {
    key: "ontimeupdate",
    value: function ontimeupdate(event) {
      this.setState({ currentTime: Math.round(event.detail) });
    }
  }, {
    key: "onseeking",
    value: function onseeking() {
      this.setState({ currentTime: Math.round(this.player.currentTime) });
    }
  }, {
    key: "ondurationchange",
    value: function ondurationchange(event) {
      this.setState({ duration: Math.round(event.detail) });
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement(
        "span",
        { className: "amp-current-time" },
        Utils.formatTimecode(this.state.currentTime, this.state.duration)
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(CurrentTime.__proto__ || Object.getPrototypeOf(CurrentTime), "contextTypes", this);
    }
  }]);
  return CurrentTime;
}(Component);

var Live = function (_Component) {
  babelHelpers.inherits(Live, _Component);

  function Live(props, context) {
    babelHelpers.classCallCheck(this, Live);

    var _this = babelHelpers.possibleConstructorReturn(this, (Live.__proto__ || Object.getPrototypeOf(Live)).call(this, props, context));

    _this.state.isLive = true;
    return _this;
  }

  babelHelpers.createClass(Live, [{
    key: "onislive",
    value: function onislive(event) {
      this.setState({ isLive: event.detail });
    }
  }, {
    key: "liveClickHandler",
    value: function liveClickHandler() {
      if (!this.state.isLive) this.player.goLive();
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement(
        "span",
        { ref: "element", className: "amp-live-label", onClick: this.liveClickHandler.bind(this) },
        this.getLocalizedString(this.state.isLive ? "MSG_LIVE" : "MSG_GO_LIVE")
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Live.__proto__ || Object.getPrototypeOf(Live), "contextTypes", this);
    }
  }]);
  return Live;
}(Component);

var Duration = function (_Component) {
  babelHelpers.inherits(Duration, _Component);

  function Duration(props, context) {
    babelHelpers.classCallCheck(this, Duration);

    var _this = babelHelpers.possibleConstructorReturn(this, (Duration.__proto__ || Object.getPrototypeOf(Duration)).call(this, props, context));

    _this.state.duration = _this.player.duration;
    return _this;
  }

  babelHelpers.createClass(Duration, [{
    key: "onmediachange",
    value: function onmediachange(event) {
      this.setState({ duration: Math.round(this.player.duration) });
    }
  }, {
    key: "ondurationchange",
    value: function ondurationchange(event) {
      this.setState({ duration: Math.round(event.detail) });
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement(
        "span",
        { className: "amp-duration" },
        Utils.formatTimecode(this.state.duration, this.state.duration)
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Duration.__proto__ || Object.getPrototypeOf(Duration), "contextTypes", this);
    }
  }]);
  return Duration;
}(Component);

var Slider = function (_Control) {
  babelHelpers.inherits(Slider, _Control);

  function Slider(props, context) {
    babelHelpers.classCallCheck(this, Slider);

    var _this = babelHelpers.possibleConstructorReturn(this, (Slider.__proto__ || Object.getPrototypeOf(Slider)).call(this, props, context));

    _this.state.percent = 0;
    _this.state.value = 0;
    _this.state.min = 0;
    _this.state.range = 0;
    _this.state.max = 1;
    _this.state.dragging = false;
    _this.increment = 0.1;
    _this.classList.add("amp-slider");
    _this.direction = "";

    _this.bindHandlers(["onHandleMouseDown", "onHandleMouseUp", "onHandleMouseMove"]);
    return _this;
  }

  babelHelpers.createClass(Slider, [{
    key: "format",
    value: function format(value) {
      return value;
    }
  }, {
    key: "update",
    value: function update(values) {
      if (values.percent == null && values.value != null) values.percent = values.value / this.max;

      if (values.value == null && values.percent != null) values.value = Math.round(values.percent * this.max);

      if (values.valueText == null && values.value != null) values.valueText = this.format(values.value) + " " + this.getLocalizedString("MSG_OF") + " " + this.format(this.max);

      // Handles NaN cases that cause unpredictable seek results
      if (!Utils.isNumber(values.value)) delete values.value;

      if (!Utils.isNumber(values.percent)) delete values.percent;

      this.setState(values);
      return values;
    }
  }, {
    key: "move",
    value: function move(event) {
      var offset = this.refs.element.getBoundingClientRect();
      var pos = Utils.getEventPos(event);
      if (this.direction === "vertical") {
        this.percent = 1 - (pos.y - offset.top) / offset.height;
      } else {
        this.percent = (pos.x - offset.left) / offset.width;
      }
    }
  }, {
    key: "onClick",
    value: function onClick(event) {
      if (this.player.playState != "ready") this.move(event);
    }
  }, {
    key: "onAction",
    value: function onAction() {}
  }, {
    key: "onKeyPress",
    value: function onKeyPress(event) {
      var code = event.keyCode;
      // left arrow 37
      // up arrow 38
      // right arrow 39
      // down arrow 40
      if (code == 39 || code == 38) {
        this.percent += this.increment;
      } else if (code == 37 || code == 40) {
        this.percent -= this.increment;
      }
    }
  }, {
    key: "onHandleMouseDown",
    value: function onHandleMouseDown(event) {
      if (this.player.playState == "ready") return;

      event.stopPropagation();
      event.preventDefault();
      this.dragging = true;
      window.addEventListener("mouseup", this.onHandleMouseUp);
      window.addEventListener("mousemove", this.onHandleMouseMove);
      window.addEventListener("touchend", this.onHandleMouseUp);
      window.addEventListener("touchmove", this.onHandleMouseMove);
    }
  }, {
    key: "onHandleMouseMove",
    value: function onHandleMouseMove(event) {
      this.move(event);
    }
  }, {
    key: "onHandleMouseUp",
    value: function onHandleMouseUp(event) {
      event.stopPropagation();
      event.preventDefault();
      this.dragging = false;
      window.removeEventListener("mouseup", this.onHandleMouseUp);
      window.removeEventListener("mousemove", this.onHandleMouseMove);
      window.removeEventListener("touchend", this.onHandleMouseUp);
      window.removeEventListener("touchmove", this.onHandleMouseMove);
      this.change(this.update({ value: this.value, percent: this.percent }));
    }
  }, {
    key: "render",
    value: function render() {
      var percent = Utils.round(this.percent * 100, 2) + "%";
      var range = Utils.round(this.range * 100, 2) + "%";
      return React.createElement(
        "div",
        babelHelpers.extends({ ref: "element" }, this.propsList, { onMouseMove: this.onMouseMove }),
        React.createElement("div", { ref: "track", className: "amp-track" }),
        React.createElement("div", { ref: "range", className: "amp-range", style: { width: range } }),
        React.createElement("div", { ref: "value", className: "amp-value", style: { width: percent } }),
        this.children,
        React.createElement("div", { ref: "handle", className: "amp-handle", style: { left: percent }, onMouseDown: this.onHandleMouseDown, onTouchStart: this.onHandleMouseDown })
      );
    }
  }, {
    key: "percent",
    set: function set(value) {
      value = akamai.amp.Utils.clamp(value, 0, 1);

      if (this.percent == value) return;

      this.change(this.update({ percent: value }));
    },
    get: function get() {
      return this.state.percent;
    }
  }, {
    key: "value",
    set: function set(value) {
      value = akamai.amp.Utils.clamp(value, this.min, this.max);

      if (this.value == value) return;

      this.change(this.update({ value: value }));
    },
    get: function get() {
      return this.state.value;
    }
  }, {
    key: "range",
    set: function set(value) {
      value = akamai.amp.Utils.clamp(value, this.min, this.max);

      if (this.range == value) return;

      this.setState({ range: value });
    },
    get: function get() {
      return this.state.range;
    }
  }, {
    key: "min",
    set: function set(value) {
      if (this.min == value) return;

      this.setState({ min: value });
    },
    get: function get() {
      return this.state.min;
    }
  }, {
    key: "max",
    set: function set(value) {
      if (this.max == value) return;

      this.setState({ max: value });
    },
    get: function get() {
      return this.state.max;
    }
  }, {
    key: "dragging",
    set: function set(value) {
      if (this.dragging == value) return;

      this.setState({ dragging: value });
      this.classList.update({ "amp-dragging": value });
      this.plugin.dragging = value;
    },
    get: function get() {
      return this.state.dragging;
    }
  }, {
    key: "markers",
    get: function get() {
      return this.refs.markers;
    }
  }, {
    key: "aria",
    get: function get() {
      return Object.assign(babelHelpers.get(Slider.prototype.__proto__ || Object.getPrototypeOf(Slider.prototype), "aria", this), {
        role: "slider",
        "aria-valuemin": this.state.min,
        "aria-valuemax": Math.round(this.state.max),
        "aria-valuenow": Math.round(this.state.value),
        "aria-valuetext": this.state.valueText
      });
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Slider.__proto__ || Object.getPrototypeOf(Slider), "contextTypes", this);
    }
  }]);
  return Slider;
}(Control);

var Progress = function (_Slider) {
  babelHelpers.inherits(Progress, _Slider);

  function Progress(props, context) {
    babelHelpers.classCallCheck(this, Progress);

    var _this = babelHelpers.possibleConstructorReturn(this, (Progress.__proto__ || Object.getPrototypeOf(Progress)).call(this, props, context));

    _this.classList.add("amp-progress", true);
    _this.state.altText = "MSG_SEEK";
    _this.state.textVisible = "visible";
    _this.bindHandlers(["onMouseMove", "onaddurationchange", "onadtimeupdate", "onaddurationchange", "onadmanagerloaded"]);
    _this.isTouch = akamai.amp.Utils.isTouch;
    return _this;
  }

  babelHelpers.createClass(Progress, [{
    key: "format",
    value: function format(value) {
      return Utils.formatTimecode(value, this.max);
    }
  }, {
    key: "onready",
    value: function onready() {
      this.player.addEventListener("adbreakend", this.ondurationchange);
      this.player.addEventListener("adtimeupdate", this.onadtimeupdate);
      this.player.addEventListener("addurationchange", this.onaddurationchange);
    }
  }, {
    key: "onmediachange",
    value: function onmediachange() {
      this.update({ value: 0 });
      this.range = 0;
    }
  }, {
    key: "ontimeupdate",
    value: function ontimeupdate() {
      if (this.dragging) return;
      this.update({ value: this.player.currentTime });
    }
  }, {
    key: "onseeking",
    value: function onseeking() {
      this.update({ value: this.player.currentTime });
    }
  }, {
    key: "onprogress",
    value: function onprogress(event) {
      this.range = event.detail;
    }
  }, {
    key: "oncueschange",
    value: function oncueschange(event) {
      var cues = event.detail;
      var markers = this.markers;

      markers.clear();

      if (cues == null || cues.length <= 0) return;

      var duration = this.player.duration;
      cues.forEach(function (cue, index) {
        if (cue.startTime <= 0) return;
        markers.addComponent(React.createElement("div", { className: "amp-marker", style: { left: cue.startTime / duration * 100 + "%" }, key: index }));
      });
    }
  }, {
    key: "ondurationchange",
    value: function ondurationchange(event) {
      this.increment = 10 / this.player.duration;
      this.max = this.player.duration;
      this.update({ value: event.type == "breakend" && event.data.type == "preroll" ? 0 : this.player.currentTime });
    }
  }, {
    key: "onadtimeupdate",
    value: function onadtimeupdate(event) {
      if (this.player.playState == "ready") return;
      this.update({ value: event.detail });
    }
  }, {
    key: "onaddurationchange",
    value: function onaddurationchange(event) {
      this.max = event.detail.duration;
    }
  }, {
    key: "onMouseMove",
    value: function onMouseMove(event) {
      var offset = this.refs.element.getBoundingClientRect();
      var pos = Utils.getEventPos(event);
      var percent = (pos.x - offset.left) / offset.width;
      this.setState({ textPercent: akamai.amp.Utils.clamp(Utils.round(percent, 4), 0, 1) });
    }
  }, {
    key: "onKeyPress",
    value: function onKeyPress(event) {
      if (this.player.ads && this.player.ads.inProgress || this.player.playState == "ready") {
        return;
      }
      babelHelpers.get(Progress.prototype.__proto__ || Object.getPrototypeOf(Progress.prototype), "onKeyPress", this).call(this, event);
    }
  }, {
    key: "onHandleMouseDown",
    value: function onHandleMouseDown(event) {
      babelHelpers.get(Progress.prototype.__proto__ || Object.getPrototypeOf(Progress.prototype), "onHandleMouseDown", this).call(this, event);
      if (this.isTouch) this.setState({ textVisible: "visible" });
    }
  }, {
    key: "onHandleMouseMove",
    value: function onHandleMouseMove(event) {
      babelHelpers.get(Progress.prototype.__proto__ || Object.getPrototypeOf(Progress.prototype), "onHandleMouseMove", this).call(this, event);
      this.onMouseMove(event);
    }
  }, {
    key: "onHandleMouseUp",
    value: function onHandleMouseUp(event) {
      babelHelpers.get(Progress.prototype.__proto__ || Object.getPrototypeOf(Progress.prototype), "onHandleMouseUp", this).call(this, event);
      if (this.isTouch) this.setState({ textVisible: "hidden" });
    }
  }, {
    key: "onChange",
    value: function onChange(states) {
      if (this.dragging) return;
      this.player.currentTime = states.value;
    }
  }, {
    key: "propsList",
    get: function get() {
      var props = babelHelpers.get(Progress.prototype.__proto__ || Object.getPrototypeOf(Progress.prototype), "propsList", this);
      props.onMouseMove = this.onMouseMove;
      return props;
    }
  }, {
    key: "markers",
    get: function get() {
      return this.refs.markers;
    }
  }, {
    key: "children",
    get: function get() {
      var textPercent = this.state.textPercent * 100 + "%";
      var minClampValue = this.refs.element ? this.refs.text.clientWidth / 1.3 * 100 / this.refs.element.clientWidth : 0;
      var maxClampValue = 100 - minClampValue;
      var textValue = akamai.amp.Utils.clamp(Utils.round(this.state.textPercent * 100, 2), minClampValue, maxClampValue) + "%";

      return React.createElement(
        "div",
        null,
        React.createElement(Container, { ref: "markers", className: "amp-markers" }),
        React.createElement(
          "div",
          { ref: "text", className: "amp-text", style: { left: textValue, visibility: this.state.textVisible } },
          this.format(this.state.textPercent * this.player.duration)
        )
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Progress.__proto__ || Object.getPrototypeOf(Progress), "contextTypes", this);
    }
  }]);
  return Progress;
}(Slider);

var Volume = function (_Slider) {
  babelHelpers.inherits(Volume, _Slider);

  function Volume(props, context) {
    babelHelpers.classCallCheck(this, Volume);

    var _this = babelHelpers.possibleConstructorReturn(this, (Volume.__proto__ || Object.getPrototypeOf(Volume)).call(this, props, context));

    _this.classList.add("amp-volume", true);
    _this.state.min = 0;
    _this.state.max = 100;
    _this.state.percent = _this.player.volume;
    _this.state.value = _this.player.volume * 100;
    _this.state.altText = "MSG_VOLUME";
    _this._open = true;
    _this.timeout = null;
    if (_this.config.volume) {
      _this.direction = _this.config.volume.direction ? _this.config.volume.direction : "";
      if (_this.direction === "vertical") {
        _this.classList.add("amp-volume-" + _this.direction, true);
        _this.classList.add("amp-slider-" + _this.direction, true);
        _this.classList.remove("amp-slider", true);
      }
    }
    return _this;
  }

  babelHelpers.createClass(Volume, [{
    key: "onready",
    value: function onready(event) {
      this.open = this.direction !== "vertical";
      this.classList.update({ "amp-open": this._open });
    }
  }, {
    key: "onseeked",
    value: function onseeked(event) {
      this.open = false;
    }
  }, {
    key: "onmediachange",
    value: function onmediachange(event) {
      this.open = false;
    }
  }, {
    key: "format",
    value: function format(value) {
      return Math.round(value);
    }
  }, {
    key: "onvolumechange",
    value: function onvolumechange(event) {
      this.update({ percent: event.detail });
    }
  }, {
    key: "onClick",
    value: function onClick(event) {
      this.move(event);
    }
  }, {
    key: "onChange",
    value: function onChange(value) {
      this.player.volume = value.percent;
    }
  }, {
    key: "onMouseOver",
    value: function onMouseOver(event) {
      this.plugin.dispatchEvent(new akamai.amp.Event("volumemouseover"));
      this.open = true;
      clearTimeout(this.timeout);
    }
  }, {
    key: "onMouseOut",
    value: function onMouseOut(event) {
      var _this2 = this;

      this.plugin.dispatchEvent(new akamai.amp.Event("volumemouseout"));
      clearTimeout(this.timeout);
      this.timeout = setTimeout(function () {
        _this2.open = false;
      }, 350);
    }
  }, {
    key: "onHandleMouseDown",
    value: function onHandleMouseDown(event) {
      event.stopPropagation();
      event.preventDefault();
      this.dragging = true;
      window.addEventListener("mouseup", this.onHandleMouseUp);
      window.addEventListener("mousemove", this.onHandleMouseMove);
      window.addEventListener("touchend", this.onHandleMouseUp);
      window.addEventListener("touchmove", this.onHandleMouseMove);
    }
  }, {
    key: "render",
    value: function render() {
      if (akamai.amp.Utils.isVolumeSettable() == false) return null;

      babelHelpers.get(Volume.prototype.__proto__ || Object.getPrototypeOf(Volume.prototype), "render", this).call(this);

      var percent = Utils.round(this.percent * 100, 2) + "%";
      var range = Utils.round(this.range * 100, 2) + "%";
      var retval = React.createElement(
        "div",
        babelHelpers.extends({ ref: "element" }, this.propsList, { onMouseMove: this.onMouseMove }),
        React.createElement("div", { ref: "track", className: "amp-track" }),
        React.createElement("div", { ref: "range", className: "amp-range", style: { width: range } }),
        React.createElement("div", { ref: "value", className: "amp-value", style: { width: percent } }),
        this.children,
        React.createElement("div", { ref: "handle", className: "amp-handle", style: { left: percent }, onMouseDown: this.onHandleMouseDown, onTouchStart: this.onHandleMouseDown })
      );
      if (this.direction == "vertical") {
        retval = React.createElement(
          "div",
          babelHelpers.extends({ ref: "element" }, this.propsList, { onMouseMove: this.onMouseMove }),
          React.createElement("div", { ref: "panel", className: "amp-volume-panel" }),
          React.createElement("div", { ref: "track", className: "amp-track" }),
          React.createElement("div", { ref: "range", className: "amp-range", style: { height: range } }),
          React.createElement("div", { ref: "value", className: "amp-value", style: { height: percent } }),
          this.children,
          React.createElement("div", { ref: "handle", className: "amp-handle", style: { bottom: percent }, onMouseDown: this.onHandleMouseDown, onTouchStart: this.onHandleMouseDown })
        );
      }
      return retval;
    }
  }, {
    key: "open",
    set: function set(value) {
      if (this.direction == "vertical") {
        this._open = value;
        this.classList.update({ "amp-open": this._open });
      }
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Volume.__proto__ || Object.getPrototypeOf(Volume), "contextTypes", this);
    }
  }]);
  return Volume;
}(Slider);

var Toggle = function (_Control) {
  babelHelpers.inherits(Toggle, _Control);

  function Toggle(props, context) {
    babelHelpers.classCallCheck(this, Toggle);

    var _this = babelHelpers.possibleConstructorReturn(this, (Toggle.__proto__ || Object.getPrototypeOf(Toggle)).call(this, props, context));

    _this.state.toggled = false;
    _this.state.onlabel = "";
    _this.state.offlabel = "";
    return _this;
  }

  babelHelpers.createClass(Toggle, [{
    key: "toggle",
    value: function toggle() {
      return this.toggled = !this.toggled;
    }
  }, {
    key: "onClick",
    value: function onClick(event) {
      this.toggle();
    }
  }, {
    key: "toggled",
    set: function set(value) {
      if (value === this.state.toggled) return value;

      this.setState({ toggled: value });
      this.change(value);
    },
    get: function get() {
      return this.state.toggled;
    }
  }, {
    key: "propsList",
    get: function get() {
      var props = babelHelpers.get(Toggle.prototype.__proto__ || Object.getPrototypeOf(Toggle.prototype), "propsList", this);
      props["aria-checked"] = this.state.toggled;
      props["role"] = "switch";
      return props;
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Toggle.__proto__ || Object.getPrototypeOf(Toggle), "contextTypes", this);
    }
  }]);
  return Toggle;
}(Control);

var Mute = function (_Toggle) {
  babelHelpers.inherits(Mute, _Toggle);

  function Mute(props, context) {
    babelHelpers.classCallCheck(this, Mute);

    var _this = babelHelpers.possibleConstructorReturn(this, (Mute.__proto__ || Object.getPrototypeOf(Mute)).call(this, props, context));

    _this.state.level = "";
    _this.classList.add("amp-mute", true);
    _this.state.altText = "MSG_MUTE";
    return _this;
  }

  babelHelpers.createClass(Mute, [{
    key: "onChange",
    value: function onChange() {
      var player = this.player;
      player.muted = !player.muted;
    }
  }, {
    key: "onMouseOver",
    value: function onMouseOver(event) {
      this.plugin.dispatchEvent(new akamai.amp.Event("mutemouseover"));
    }
  }, {
    key: "onMouseOut",
    value: function onMouseOut(event) {
      this.plugin.dispatchEvent(new akamai.amp.Event("mutemouseout"));
    }
  }, {
    key: "onvolumechange",
    value: function onvolumechange() {
      var level = "";

      if (this.player.muted) {
        level = "amp-muted";
      } else if (this.player.volume < 0.5) {
        level = "amp-low";
      }

      this.setState({ level: level });
    }
  }, {
    key: "className",
    get: function get() {
      return babelHelpers.get(Mute.prototype.__proto__ || Object.getPrototypeOf(Mute.prototype), "className", this) + " " + this.state.level;
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Mute.__proto__ || Object.getPrototypeOf(Mute), "contextTypes", this);
    }
  }]);
  return Mute;
}(Toggle);

var Unmute = function (_Control) {
  babelHelpers.inherits(Unmute, _Control);

  function Unmute(props, context) {
    babelHelpers.classCallCheck(this, Unmute);

    var _this = babelHelpers.possibleConstructorReturn(this, (Unmute.__proto__ || Object.getPrototypeOf(Unmute)).call(this, props, context));

    _this.classList.add("amp-unmute", true);
    _this.state.altText = "MSG_UNMUTE";
    _this.state.dismissed = false;
    return _this;
  }

  babelHelpers.createClass(Unmute, [{
    key: "onmutechange",
    value: function onmutechange(event) {
      if (event.detail === false) this.setState({ dismissed: true });
    }
  }, {
    key: "onClick",
    value: function onClick(event) {
      this.player.muted = false;
      event.stopPropagation();
    }
  }, {
    key: "render",
    value: function render() {
      if (this.state.dismissed === true) return null;

      return React.createElement(
        "div",
        { className: "amp-unmute-layer", onClick: this.propsList.onClick },
        babelHelpers.get(Unmute.prototype.__proto__ || Object.getPrototypeOf(Unmute.prototype), "render", this).call(this)
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Unmute.__proto__ || Object.getPrototypeOf(Unmute), "contextTypes", this);
    }
  }]);
  return Unmute;
}(Control);

var Fullscreen = function (_Control) {
  babelHelpers.inherits(Fullscreen, _Control);

  function Fullscreen(props, context) {
    babelHelpers.classCallCheck(this, Fullscreen);

    var _this = babelHelpers.possibleConstructorReturn(this, (Fullscreen.__proto__ || Object.getPrototypeOf(Fullscreen)).call(this, props, context));

    _this.classList.add("amp-fullscreen", true);
    return _this;
  }

  babelHelpers.createClass(Fullscreen, [{
    key: "onfullscreenchange",
    value: function onfullscreenchange(event) {
      var key = event.detail ? "MSG_EXIT_FULLSCREEN" : "MSG_ENTER_FULLSCREEN";
      this.setState({ altText: key });
    }
  }, {
    key: "onClick",
    value: function onClick() {
      if (this.player.displayState == "normal") {
        this.player.enterFullScreen();
      } else {
        this.player.exitFullScreen();
      }
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Fullscreen.__proto__ || Object.getPrototypeOf(Fullscreen), "contextTypes", this);
    }
  }]);
  return Fullscreen;
}(Control);

var SettingsButton = function (_PanelControl) {
  babelHelpers.inherits(SettingsButton, _PanelControl);

  function SettingsButton(props, context) {
    babelHelpers.classCallCheck(this, SettingsButton);

    var _this = babelHelpers.possibleConstructorReturn(this, (SettingsButton.__proto__ || Object.getPrototypeOf(SettingsButton)).call(this, props, context));

    _this.classList.add("amp-settings", true);
    _this.state.altText = "MSG_SETTINGS";
    return _this;
  }

  babelHelpers.createClass(SettingsButton, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(SettingsButton.__proto__ || Object.getPrototypeOf(SettingsButton), "contextTypes", this);
    }
  }]);
  return SettingsButton;
}(PanelControl);

var ListItem = function (_Control) {
  babelHelpers.inherits(ListItem, _Control);

  function ListItem(props, context) {
    babelHelpers.classCallCheck(this, ListItem);

    var _this = babelHelpers.possibleConstructorReturn(this, (ListItem.__proto__ || Object.getPrototypeOf(ListItem)).call(this, props, context));

    _this.classList.add("amp-list-item");
    _this.classList.remove("amp-icon");
    _this.classList.remove("amp-control");
    _this.state.direction = _this.props.direction || "none";
    return _this;
  }

  babelHelpers.createClass(ListItem, [{
    key: "focus",
    value: function focus() {
      this.refs.element.focus();
    }
  }, {
    key: "onKeyDown",
    value: function onKeyDown(event) {
      if (this.mouseFocus == true) return;
      this.onKeyPress(event);
      if (event.type == "keydown") {
        //13, 32 space Enter
        //37, 39 left and right arrow
        //38, 40 up down
        switch (event.keyCode) {
          case 13:
          case 32:
            this.props.onClick(event) || this.onClick(event);
            break;
          case 37:
            if (this.state.direction === "left") this.props.onClick(event) || this.onClick(event);
            break;
          case 39:
            if (this.state.direction === "right") this.props.onClick(event) || this.onClick(event);
            break;
          case 38:
            this.element.previousElementSibling !== null && this.element.previousElementSibling.focus();
            break;
          case 40:
            this.element.nextElementSibling !== null && this.element.nextElementSibling.focus();
            break;
        }

        if ((event.keyCode == 9 || event.keyCode == 40) && event.shiftKey == false && this.element.nextElementSibling === null || (event.keyCode == 9 || event.keyCode == 38) && event.shiftKey == true && this.element.previousElementSibling === null || event.keyCode == 27) {
          var tabIndex = void 0;
          if (this.refs.element.parentNode.className.indexOf("share") >= 0) {
            tabIndex = 4;
            this.plugin.refs.sharePanel.close();
          } else {
            tabIndex = 2;
            this.plugin.refs.settingsPanel.close();
          }
          var tabable = document.getElementsByClassName("amp-control");
          setTimeout(function () {
            tabable[tabable.length - tabIndex].focus();
          }, 1);
        }
      }

      event.stopPropagation();
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement(
        "div",
        babelHelpers.extends({ ref: "element" }, this.propsList, { role: "menuitem" }),
        this.children
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(ListItem.__proto__ || Object.getPrototypeOf(ListItem), "contextTypes", this);
    }
  }]);
  return ListItem;
}(Control);

var Panel = function (_Component) {
  babelHelpers.inherits(Panel, _Component);

  function Panel(props, context) {
    babelHelpers.classCallCheck(this, Panel);

    var _this = babelHelpers.possibleConstructorReturn(this, (Panel.__proto__ || Object.getPrototypeOf(Panel)).call(this, props, context));

    _this.closeHandler = _this.closeHandler.bind(_this);
    _this._open = false;
    return _this;
  }

  babelHelpers.createClass(Panel, [{
    key: "closeHandler",
    value: function closeHandler(event) {
      if (this.element === undefined || this.element.contains(event.target)) return;

      this.open = false;
      document.removeEventListener("click", this.closeHandler);
    }
  }, {
    key: "toggle",
    value: function toggle() {
      this.open = !this.open;
    }
  }, {
    key: "close",
    value: function close() {
      this.open = false;
    }
  }, {
    key: "isOptionSelected",
    value: function isOptionSelected(item, index) {
      return index === this.state.activeIndex;
    }
  }, {
    key: "clickHandler",
    value: function clickHandler(object, index) {
      this.setState({ activeIndex: index });
    }
  }, {
    key: "backHandler",
    value: function backHandler(event) {
      this.props.gotoSettings(event);
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      var items = this.listOptions.map(function (object, index) {
        return React.createElement(
          ListItem,
          { key: index, onClick: _this2.clickHandler.bind(_this2, object, index) },
          React.createElement("button", { tabIndex: "-1", className: _this2.isOptionSelected(object, index) ? 'amp-icon amp-list-item-selected' : 'amp-icon' }),
          React.createElement(
            "span",
            null,
            object.label
          )
        );
      });

      return React.createElement(
        "div",
        { ref: "element" },
        React.createElement(
          ListItem,
          { className: "amp-list-title", onClick: this.backHandler.bind(this), direction: "left" },
          React.createElement("button", { tabIndex: "-1", className: "amp-icon amp-icon-left" }),
          React.createElement(
            "span",
            null,
            this.state.title
          )
        ),
        items
      );
    }
  }, {
    key: "open",
    set: function set(value) {
      if (value == this._open) return;

      this._open = value;
      this.classList.update({ "amp-open": this._open });
      this.plugin.open = value;

      if (this._open) {
        document.addEventListener("click", this.closeHandler);
        this.context.plugin.activeState = "active";
        this.context.plugin.stopIdleTimeout();
        if (this.props.onopen) this.props.onopen(this);
      } else {
        this.context.plugin.startIdleTimeout();
        document.removeEventListener("click", this.closeHandler);
        if (this.props.onclose) this.props.onclose(this);
      }
    },
    get: function get() {
      return this._open;
    }
  }, {
    key: "propsList",
    get: function get() {
      var _babelHelpers$get = babelHelpers.get(Panel.prototype.__proto__ || Object.getPrototypeOf(Panel.prototype), "propsList", this),
          onclose = _babelHelpers$get.onclose,
          onopen = _babelHelpers$get.onopen,
          props = babelHelpers.objectWithoutProperties(_babelHelpers$get, ["onclose", "onopen"]);

      return props;
    }
  }, {
    key: "title",
    get: function get() {
      return "TITLE";
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Panel.__proto__ || Object.getPrototypeOf(Panel), "contextTypes", this);
    }
  }]);
  return Panel;
}(Component);

var PanelMenu = function (_Panel) {
  babelHelpers.inherits(PanelMenu, _Panel);

  function PanelMenu(props, context) {
    babelHelpers.classCallCheck(this, PanelMenu);

    var _this = babelHelpers.possibleConstructorReturn(this, (PanelMenu.__proto__ || Object.getPrototypeOf(PanelMenu)).call(this, props, context));

    _this.state.keyDown = false;
    return _this;
  }

  babelHelpers.createClass(PanelMenu, [{
    key: "onKeyDown",
    value: function onKeyDown(event) {
      if (event.keyCode == 13 || event.keyCode == 32) {
        this.state.keyDown = true;
        this.toggle();
      }
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(PanelMenu.__proto__ || Object.getPrototypeOf(PanelMenu), "contextTypes", this);
    }
  }]);
  return PanelMenu;
}(Panel);

var Home = function (_Panel) {
  babelHelpers.inherits(Home, _Panel);

  function Home(props, context) {
    babelHelpers.classCallCheck(this, Home);
    return babelHelpers.possibleConstructorReturn(this, (Home.__proto__ || Object.getPrototypeOf(Home)).call(this, props, context));
  }

  babelHelpers.createClass(Home, [{
    key: "clickHandler",
    value: function clickHandler(object, index) {
      this.setState({ activeIndex: index });
      this.props.settingsChange({ language: this.listOptions[index] });
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      this.listOptions = [{ label: "Off", value: "off" }];
      this.player.captioning.tracks.forEach(function (track) {
        var lang = track.language;
        _this2.listOptions.push({ label: _this2.player.localization.getLanguageString(lang), value: lang });
      });

      var OptionsList = this.listOptions.map(function (object, index) {
        var selected = !_this2.player.captioning.hidden && _this2.player.captioning.track && object.value == _this2.player.captioning.track.language;
        if (_this2.player.captioning.hidden && index === 0) {
          selected = true;
        }
        return React.createElement(
          ListItem,
          { key: index, onClick: _this2.clickHandler.bind(_this2, object, index) },
          React.createElement("button", { className: selected ? 'amp-icon amp-list-item-selected' : 'amp-icon', tabIndex: "-1" }),
          React.createElement(
            "span",
            null,
            object.label
          )
        );
      });

      return React.createElement(
        "div",
        { ref: "element", className: "amp-captioning-home" },
        React.createElement(
          ListItem,
          { className: "amp-list-highlight", onClick: this.props.gotoSettings.bind(this), direction: "left" },
          React.createElement("button", { className: "amp-icon amp-icon-left", tabIndex: "-1" }),
          React.createElement(
            "span",
            null,
            "Subtitles/CC"
          )
        ),
        React.createElement(
          ListItem,
          { onClick: function onClick(event) {
              return _this2.props.viewChange(event, "options");
            }, className: "amp-option-button" },
          "Options"
        ),
        OptionsList
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Home.__proto__ || Object.getPrototypeOf(Home), "contextTypes", this);
    }
  }]);
  return Home;
}(Panel);

var Options = function (_Panel) {
  babelHelpers.inherits(Options, _Panel);

  function Options(props, context) {
    babelHelpers.classCallCheck(this, Options);

    var _this = babelHelpers.possibleConstructorReturn(this, (Options.__proto__ || Object.getPrototypeOf(Options)).call(this, props, context));

    _this.listOptions = [{ label: "Font Family", key: "fontFamily", value: "Arial" }, { label: "Font Color", key: "fontColor", value: "Red" }, { label: "Font Size", key: "size", value: "100%" }, { label: "Font Opacity", key: "fontOpacity", value: "0%" }, { label: "Background Color", key: "backgroundColor", value: "Black" }, { label: "Background Opacity", key: "backgroundOpacity", value: "0%" }, { label: "Window Color", key: "windowColor", value: "Red" }, { label: "Window Opacity", key: "windowOpacity", value: "0%" }, { label: "Character Edge Style", key: "edgeType", value: "None" }, { label: "Edge Color", key: "edgeColor", value: "Red" }, { label: "Edge Opacity", key: "edgeOpacity", value: "Red" }, { label: "Scroll", key: "scroll", value: "Pop-out" }];
    return _this;
  }

  babelHelpers.createClass(Options, [{
    key: "render",
    value: function render() {
      var _this2 = this;

      var OptionsList = this.listOptions.map(function (object, index) {
        return React.createElement(
          ListItem,
          { key: object.key, onClick: function onClick(event) {
              return _this2.props.viewChange(event, object.key);
            }, direction: "right" },
          React.createElement(
            "span",
            { className: "amp-label" },
            object.label
          ),
          React.createElement("button", { className: "amp-icon amp-icon-right amp-right", tabIndex: "-1" }),
          React.createElement(
            "span",
            { className: "amp-right" },
            _this2.props.captionsettings[object.key + "Label"]
          )
        );
      });

      return React.createElement(
        "div",
        { className: "amp-captioning-options" },
        React.createElement(
          ListItem,
          { className: "amp-list-title", onClick: function onClick(event) {
              return _this2.props.viewChange(event, "home");
            }, direction: "left" },
          React.createElement("button", { className: "amp-icon amp-icon-left", tabIndex: "-1" }),
          React.createElement(
            "span",
            null,
            "Options"
          )
        ),
        OptionsList
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Options.__proto__ || Object.getPrototypeOf(Options), "contextTypes", this);
    }
  }]);
  return Options;
}(Panel);

var OptionsPanel = function (_Panel) {
  babelHelpers.inherits(OptionsPanel, _Panel);

  function OptionsPanel(props, context) {
    babelHelpers.classCallCheck(this, OptionsPanel);
    return babelHelpers.possibleConstructorReturn(this, (OptionsPanel.__proto__ || Object.getPrototypeOf(OptionsPanel)).call(this, props, context));
  }

  babelHelpers.createClass(OptionsPanel, [{
    key: "select",
    value: function select() {
      var options = this.listOptions;
      var props = this.props.captionsettings;
      var len = options.length;
      for (var i = 0; i < len; i++) {
        if (/color/i.test(this.setting) && options[i].value.r == props[this.setting].r && options[i].value.g == props[this.setting].g && options[i].value.b == props[this.setting].b || /opacity/i.test(this.setting) && options[i].value == props[this.setting.replace("Opacity", "Color")].a || !/opacity|color/i.test(this.setting) && options[i].value == props[this.setting]) {
          this.state.activeIndex = i;
          break;
        }
      }
    }
  }, {
    key: "clickHandler",
    value: function clickHandler(object, index) {
      babelHelpers.get(OptionsPanel.prototype.__proto__ || Object.getPrototypeOf(OptionsPanel.prototype), "clickHandler", this).call(this, object, index);
      this.props.settingsChange(babelHelpers.defineProperty({}, this.setting, object));
    }
  }, {
    key: "backHandler",
    value: function backHandler(event) {
      this.props.viewChange(event, "options");
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(OptionsPanel.__proto__ || Object.getPrototypeOf(OptionsPanel), "contextTypes", this);
    }
  }]);
  return OptionsPanel;
}(Panel);

var Color = function () {
  function Color(r, g, b) {
    var a = arguments.length > 3 && arguments[3] !== undefined ? arguments[3] : 1;
    babelHelpers.classCallCheck(this, Color);

    if (typeof r == "string") {
      var parts = r.match(/[0-9\.]+/g);
      r = parseInt(parts[0]);
      g = parseInt(parts[1]);
      b = parseInt(parts[2]);
      a = parts[3] != null ? parseFloat(parts[3]) : 1;
    }

    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }

  babelHelpers.createClass(Color, [{
    key: "toString",
    value: function toString(radix) {
      return "rgba(" + this.r + ", " + this.g + ", " + this.b + ", " + this.a + ")";
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Color.__proto__ || Object.getPrototypeOf(Color), "contextTypes", this);
    }
  }]);
  return Color;
}();

var BackgroundColor = function (_OptionsPanel) {
  babelHelpers.inherits(BackgroundColor, _OptionsPanel);

  function BackgroundColor(props, context) {
    babelHelpers.classCallCheck(this, BackgroundColor);

    var _this = babelHelpers.possibleConstructorReturn(this, (BackgroundColor.__proto__ || Object.getPrototypeOf(BackgroundColor)).call(this, props, context));

    _this.state.title = "Background Color"; // TODO: This needs to be localized
    _this.setting = "backgroundColor";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(BackgroundColor, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(BackgroundColor.__proto__ || Object.getPrototypeOf(BackgroundColor), "contextTypes", this);
    }
  }]);
  return BackgroundColor;
}(OptionsPanel);

var BackgroundOpacity = function (_OptionsPanel) {
  babelHelpers.inherits(BackgroundOpacity, _OptionsPanel);

  function BackgroundOpacity(props, context) {
    babelHelpers.classCallCheck(this, BackgroundOpacity);

    var _this = babelHelpers.possibleConstructorReturn(this, (BackgroundOpacity.__proto__ || Object.getPrototypeOf(BackgroundOpacity)).call(this, props, context));

    _this.state.title = "Background Opacity"; // TODO: This needs to be localized
    _this.setting = "backgroundOpacity";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(BackgroundOpacity, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(BackgroundOpacity.__proto__ || Object.getPrototypeOf(BackgroundOpacity), "contextTypes", this);
    }
  }]);
  return BackgroundOpacity;
}(OptionsPanel);

var CharacterEdgeStyle = function (_OptionsPanel) {
  babelHelpers.inherits(CharacterEdgeStyle, _OptionsPanel);

  function CharacterEdgeStyle(props, context) {
    babelHelpers.classCallCheck(this, CharacterEdgeStyle);

    var _this = babelHelpers.possibleConstructorReturn(this, (CharacterEdgeStyle.__proto__ || Object.getPrototypeOf(CharacterEdgeStyle)).call(this, props, context));

    _this.state.title = "Character Edge Style"; // TODO: This needs to be localized
    _this.setting = "edgeType";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(CharacterEdgeStyle, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(CharacterEdgeStyle.__proto__ || Object.getPrototypeOf(CharacterEdgeStyle), "contextTypes", this);
    }
  }]);
  return CharacterEdgeStyle;
}(OptionsPanel);

var FontColor = function (_OptionsPanel) {
  babelHelpers.inherits(FontColor, _OptionsPanel);

  function FontColor(props, context) {
    babelHelpers.classCallCheck(this, FontColor);

    var _this = babelHelpers.possibleConstructorReturn(this, (FontColor.__proto__ || Object.getPrototypeOf(FontColor)).call(this, props, context));

    _this.state.title = "Font Color"; // TODO: This needs to be localized
    _this.setting = "fontColor";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(FontColor, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(FontColor.__proto__ || Object.getPrototypeOf(FontColor), "contextTypes", this);
    }
  }]);
  return FontColor;
}(OptionsPanel);

var FontFamily = function (_OptionsPanel) {
  babelHelpers.inherits(FontFamily, _OptionsPanel);

  function FontFamily(props, context) {
    babelHelpers.classCallCheck(this, FontFamily);

    var _this = babelHelpers.possibleConstructorReturn(this, (FontFamily.__proto__ || Object.getPrototypeOf(FontFamily)).call(this, props, context));

    _this.state.title = "Font Family"; // TODO: This needs to be localized
    _this.setting = "fontFamily";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(FontFamily, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(FontFamily.__proto__ || Object.getPrototypeOf(FontFamily), "contextTypes", this);
    }
  }]);
  return FontFamily;
}(OptionsPanel);

var FontOpacity = function (_OptionsPanel) {
  babelHelpers.inherits(FontOpacity, _OptionsPanel);

  function FontOpacity(props, context) {
    babelHelpers.classCallCheck(this, FontOpacity);

    var _this = babelHelpers.possibleConstructorReturn(this, (FontOpacity.__proto__ || Object.getPrototypeOf(FontOpacity)).call(this, props, context));

    _this.state.title = "Font Opacity"; // TODO: This needs to be localized
    _this.setting = "fontOpacity";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(FontOpacity, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(FontOpacity.__proto__ || Object.getPrototypeOf(FontOpacity), "contextTypes", this);
    }
  }]);
  return FontOpacity;
}(OptionsPanel);

var FontSize = function (_OptionsPanel) {
  babelHelpers.inherits(FontSize, _OptionsPanel);

  function FontSize(props, context) {
    babelHelpers.classCallCheck(this, FontSize);

    var _this = babelHelpers.possibleConstructorReturn(this, (FontSize.__proto__ || Object.getPrototypeOf(FontSize)).call(this, props, context));

    _this.state.title = "Font Size"; // TODO: This needs to be localized
    _this.setting = "size";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(FontSize, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(FontSize.__proto__ || Object.getPrototypeOf(FontSize), "contextTypes", this);
    }
  }]);
  return FontSize;
}(OptionsPanel);

var WindowColor = function (_OptionsPanel) {
  babelHelpers.inherits(WindowColor, _OptionsPanel);

  function WindowColor(props, context) {
    babelHelpers.classCallCheck(this, WindowColor);

    var _this = babelHelpers.possibleConstructorReturn(this, (WindowColor.__proto__ || Object.getPrototypeOf(WindowColor)).call(this, props, context));

    _this.state.title = "Window Color"; // TODO: This needs to be localized
    _this.setting = "windowColor";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(WindowColor, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(WindowColor.__proto__ || Object.getPrototypeOf(WindowColor), "contextTypes", this);
    }
  }]);
  return WindowColor;
}(OptionsPanel);

var WindowOpacity = function (_OptionsPanel) {
  babelHelpers.inherits(WindowOpacity, _OptionsPanel);

  function WindowOpacity(props, context) {
    babelHelpers.classCallCheck(this, WindowOpacity);

    var _this = babelHelpers.possibleConstructorReturn(this, (WindowOpacity.__proto__ || Object.getPrototypeOf(WindowOpacity)).call(this, props, context));

    _this.state.title = "Window Opacity"; // TODO: This needs to be localized
    _this.setting = "windowOpacity";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(WindowOpacity, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(WindowOpacity.__proto__ || Object.getPrototypeOf(WindowOpacity), "contextTypes", this);
    }
  }]);
  return WindowOpacity;
}(OptionsPanel);

var ScrollType = function (_OptionsPanel) {
  babelHelpers.inherits(ScrollType, _OptionsPanel);

  function ScrollType(props, context) {
    babelHelpers.classCallCheck(this, ScrollType);

    var _this = babelHelpers.possibleConstructorReturn(this, (ScrollType.__proto__ || Object.getPrototypeOf(ScrollType)).call(this, props, context));

    _this.state.title = "Scroll Type"; // TODO: This needs to be localized
    _this.setting = "scroll";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(ScrollType, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(ScrollType.__proto__ || Object.getPrototypeOf(ScrollType), "contextTypes", this);
    }
  }]);
  return ScrollType;
}(OptionsPanel);

var EdgeColor = function (_OptionsPanel) {
  babelHelpers.inherits(EdgeColor, _OptionsPanel);

  function EdgeColor(props, context) {
    babelHelpers.classCallCheck(this, EdgeColor);

    var _this = babelHelpers.possibleConstructorReturn(this, (EdgeColor.__proto__ || Object.getPrototypeOf(EdgeColor)).call(this, props, context));

    _this.state.title = "Edge Color"; // TODO: This needs to be localized
    _this.setting = "edgeColor";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(EdgeColor, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(EdgeColor.__proto__ || Object.getPrototypeOf(EdgeColor), "contextTypes", this);
    }
  }]);
  return EdgeColor;
}(OptionsPanel);

var EdgeOpacity = function (_OptionsPanel) {
  babelHelpers.inherits(EdgeOpacity, _OptionsPanel);

  function EdgeOpacity(props, context) {
    babelHelpers.classCallCheck(this, EdgeOpacity);

    var _this = babelHelpers.possibleConstructorReturn(this, (EdgeOpacity.__proto__ || Object.getPrototypeOf(EdgeOpacity)).call(this, props, context));

    _this.state.title = "Edge Opacity"; // TODO: This needs to be localized
    _this.setting = "edgeOpacity";
    _this.listOptions = props.labels[_this.setting];
    _this.select();
    return _this;
  }

  babelHelpers.createClass(EdgeOpacity, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(EdgeOpacity.__proto__ || Object.getPrototypeOf(EdgeOpacity), "contextTypes", this);
    }
  }]);
  return EdgeOpacity;
}(OptionsPanel);

var CaptionData = function CaptionData() {
  var _ref;

  babelHelpers.classCallCheck(this, CaptionData);

  return _ref = {
    fontFamily: [{ label: "Monospaced Serif", value: "'Courier New', Courier, 'Nimbus Mono L', 'Cutive Mono', monospace" }, { label: "Proportional Serif", value: "'Times New Roman', Times, Georgia, Cambria, 'PT Serif Caption', serif" }, { label: "Monospaced Sans-Serif", value: "'Deja Vu Sans Mono', 'Lucida Console', Monaco, Consolas, 'PT Mono', monospace" }, { label: "Proportional Sans-Serif", value: "Roboto, 'Arial Unicode Ms', Arial, Helvetica, Verdana, 'PT Sans Caption', sans-serif" }, { label: "Casual", value: "'Comic Sans MS', Impact, Handlee, fantasy" }, { label: "Cursive", value: "'Monotype Corsiva', 'URW Chancery L', 'Apple Chancery', 'Dancing Script', cursive" }, { label: "Small Capitals", value: "'Arial Unicode Ms', Arial, Helvetica, Verdana, 'Marcellus SC', sans-serif; font-variant: small-caps" }],
    fontColor: [{ label: "White", value: new Color(255, 255, 255) }, { label: "Yellow", value: new Color(255, 255, 0) }, { label: "Green", value: new Color(0, 128, 0) }, { label: "Cyan", value: new Color(0, 255, 255) }, { label: "Blue", value: new Color(0, 0, 255) }, { label: "Magenta", value: new Color(255, 0, 255) }, { label: "Red", value: new Color(255, 0, 0) }, { label: "Black", value: new Color(0, 0, 0) }],
    size: [{ label: "70%", value: "x-small" }, { label: "100%", value: "small" }, { label: "150%", value: "medium" }, { label: "200%", value: "large" }, { label: "300%", value: "x-large" }],
    fontOpacity: [{ label: "25%", value: 0.25 }, { label: "50%", value: 0.5 }, { label: "75%", value: 0.75 }, { label: "100%", value: 1 }],
    backgroundColor: [{ label: "White", value: new Color(255, 255, 255) }, { label: "Yellow", value: new Color(255, 255, 0) }, { label: "Green", value: new Color(0, 128, 0) }, { label: "Cyan", value: new Color(0, 255, 255) }, { label: "Blue", value: new Color(0, 0, 255) }, { label: "Magenta", value: new Color(255, 0, 255) }, { label: "Red", value: new Color(255, 0, 0) }, { label: "Black", value: new Color(0, 0, 0) }],
    backgroundOpacity: [{ label: "0%", value: 0 }, { label: "25%", value: 0.25 }, { label: "50%", value: 0.5 }, { label: "75%", value: 0.75 }, { label: "100%", value: 1 }],
    edgeType: [{ label: "None", value: "text-shadow: 0px 0px 0px" }, { label: "Depressed", value: "text-shadow: 0px 1px 0px" }, { label: "Left Drop Shadow", value: "text-shadow: -3px 3px 2px" }, { label: "Raised", value: "text-shadow: 0px 1px 1px" }, { label: "Right Drop Shadow", value: "text-shadow: 3px 3px 2px" }, { label: "Uniform", value: "text-shadow: 0px 0px 4px" }],
    edgeColor: [{ label: "White", value: new Color(255, 255, 255) }, { label: "Yellow", value: new Color(255, 255, 0) }, { label: "Green", value: new Color(0, 128, 0) }, { label: "Cyan", value: new Color(0, 255, 255) }, { label: "Blue", value: new Color(0, 0, 255) }, { label: "Magenta", value: new Color(255, 0, 255) }, { label: "Red", value: new Color(255, 0, 0) }, { label: "Black", value: new Color(0, 0, 0) }],
    edgeOpacity: [{ label: "0%", value: 0 }, { label: "25%", value: 0.25 }, { label: "50%", value: 0.5 }, { label: "75%", value: 0.75 }, { label: "100%", value: 1 }]
  }, babelHelpers.defineProperty(_ref, "fontOpacity", [{ label: "25%", value: 0.25 }, { label: "50%", value: 0.5 }, { label: "75%", value: 0.75 }, { label: "100%", value: 1 }]), babelHelpers.defineProperty(_ref, "scroll", [{ label: "Pop-out", value: "popout" }, { label: "Roll-on", value: "rollon" }, { label: "Paint-on", value: "painton" }]), babelHelpers.defineProperty(_ref, "windowColor", [{ label: "White", value: new Color(255, 255, 255) }, { label: "Yellow", value: new Color(255, 255, 0) }, { label: "Green", value: new Color(0, 128, 0) }, { label: "Cyan", value: new Color(0, 255, 255) }, { label: "Blue", value: new Color(0, 0, 255) }, { label: "Magenta", value: new Color(255, 0, 255) }, { label: "Red", value: new Color(255, 0, 0) }, { label: "Black", value: new Color(0, 0, 0) }]), babelHelpers.defineProperty(_ref, "windowOpacity", [{ label: "0%", value: 0 }, { label: "25%", value: 0.25 }, { label: "50%", value: 0.5 }, { label: "75%", value: 0.75 }, { label: "100%", value: 1 }]), _ref;
};

var CaptionSettings = function (_Panel) {
  babelHelpers.inherits(CaptionSettings, _Panel);

  function CaptionSettings(props, context) {
    babelHelpers.classCallCheck(this, CaptionSettings);

    var _this = babelHelpers.possibleConstructorReturn(this, (CaptionSettings.__proto__ || Object.getPrototypeOf(CaptionSettings)).call(this, props, context));

    _this.state.languages = [];
    _this.state.keyDown = false;

    var settings = Object.assign({}, _this.player.settings.captions);
    delete settings.track;

    for (var key in settings) {
      if (/Color$/.test(key)) {
        settings[key] = new Color(settings[key]);
        settings[key.replace("Color", "Opacity")] = settings[key].a;
      }
    }
    _this.state.captionsettings = settings;

    _this.state.viewChange = _this.changeView.bind(_this);
    _this.state.settingsChange = _this.changeSettings.bind(_this);
    _this.state.level = "home";

    _this.state.labels = new CaptionData();

    _this.initSettingLabels();
    return _this;
  }

  babelHelpers.createClass(CaptionSettings, [{
    key: "componentWillMount",
    value: function componentWillMount() {
      this.open = true;
    }
  }, {
    key: "focus",
    value: function focus(event) {
      this.refs.element.firstChild.children[2].focus();
    }
  }, {
    key: "changeView",
    value: function changeView(event, toState) {
      var _this2 = this;

      if (/keydown/.test(event.type) && (event.keyCode == 13 || event.keyCode == 32 || event.keyCode == 37 || event.keyCode == 39)) {
        this.keyDown = true;
      }
      setTimeout(function () {
        _this2.setState({ level: toState });
      }, 100);
    }
  }, {
    key: "initSettingLabels",
    value: function initSettingLabels() {
      var _this3 = this;

      var _loop = function _loop(key) {
        _this3.state.labels[key].forEach(function (item) {
          if (/color/i.test(key) && item.value.r == _this3.state.captionsettings[key].r && item.value.g == _this3.state.captionsettings[key].g && item.value.b == _this3.state.captionsettings[key].b || /opacity/i.test(key) && item.value == _this3.state.captionsettings[key.replace("Opacity", "Color")].a || _this3.state.captionsettings[key] == item.value) {
            _this3.state.captionsettings[key + "Label"] = item.label;
          }
        });
      };

      for (var key in this.state.labels) {
        _loop(key);
      }
    }
  }, {
    key: "changeSettings",
    value: function changeSettings(object) {
      for (var key in object) {
        var setting = object[key];
        var value = setting.value;
        switch (key) {
          case "language":
            var hidden = setting.value == "off";
            if (!hidden) this.player.captioning.selectTrackByLanguage(setting.value);
            this.player.captioning.hidden = hidden;
            break;

          case "backgroundColor":
          case "windowColor":
          case "fontColor":
          case "edgeColor":
            value.a = this.state.captionsettings[key].a;
            break;

          case "backgroundOpacity":
          case "fontOpacity":
          case "windowOpacity":
          case "edgeOpacity":
            var color = this.state.captionsettings[key.replace("Opacity", "Color")];
            color.a = value;
            value = color;
            break;

        }
        this.state.captionsettings[key.replace("Opacity", "Color")] = value;
        this.state.captionsettings[key + "Label"] = setting.label;
      }

      var settings = Object.assign({}, this.state.captionsettings);
      for (var _key in settings) {
        if (/Label$/.test(_key) || /Opacity$/.test(_key) || /language/.test(_key)) {
          delete settings[_key];
        } else if (/Color$/.test(_key)) {
          settings[_key] = settings[_key].toString();
        }
      }
      settings.visible = !this.player.captioning.hidden;
      this.player.captioning.changeSettings(settings);
    }
  }, {
    key: "componentDidUpdate",
    value: function componentDidUpdate(prevProps, prevState) {
      if (this.keyDown) {
        this.refs.element.firstChild.children[1].focus();
        this.keyDown = false;
      }
    }
  }, {
    key: "getCurrentPage",
    value: function getCurrentPage() {

      switch (this.state.level) {
        case "home":
          return React.createElement(Home, babelHelpers.extends({}, this.state, { gotoSettings: this.props.gotoSettings.bind(this) }));
          break;
        case "options":
          return React.createElement(Options, this.state);
          break;
        case "fontFamily":
          return React.createElement(FontFamily, this.state);
          break;
        case "fontColor":
          return React.createElement(FontColor, this.state);
          break;
        case "size":
          return React.createElement(FontSize, this.state);
          break;
        case "fontOpacity":
          return React.createElement(FontOpacity, this.state);
          break;
        case "backgroundColor":
          return React.createElement(BackgroundColor, this.state);
          break;
        case "backgroundOpacity":
          return React.createElement(BackgroundOpacity, this.state);
          break;
        case "windowColor":
          return React.createElement(WindowColor, this.state);
          break;
        case "windowOpacity":
          return React.createElement(WindowOpacity, this.state);
          break;
        case "edgeType":
          return React.createElement(CharacterEdgeStyle, this.state);
          break;
        case "edgeColor":
          return React.createElement(EdgeColor, this.state);
          break;
        case "edgeOpacity":
          return React.createElement(EdgeOpacity, this.state);
          break;
        case "scroll":
          return React.createElement(ScrollType, this.state);
      }
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement(
        "div",
        { ref: "element", role: "menu" },
        this.getCurrentPage()
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(CaptionSettings.__proto__ || Object.getPrototypeOf(CaptionSettings), "contextTypes", this);
    }
  }]);
  return CaptionSettings;
}(Panel);

var Quality = function (_Panel) {
  babelHelpers.inherits(Quality, _Panel);

  function Quality(props, context) {
    babelHelpers.classCallCheck(this, Quality);

    var _this = babelHelpers.possibleConstructorReturn(this, (Quality.__proto__ || Object.getPrototypeOf(Quality)).call(this, props, context));

    _this.listOptions = [{ label: "Auto", value: "0.25" // TODO: Auto needs to be localized
    }];
    _this.qualityLevels = [];
    _this.state.title = "Quality"; // TODO: This needs to be localized
    _this.state.activeIndex = -1;
    return _this;
  }

  babelHelpers.createClass(Quality, [{
    key: "componentWillMount",
    value: function componentWillMount() {
      babelHelpers.get(Quality.prototype.__proto__ || Object.getPrototypeOf(Quality.prototype), "componentWillMount", this).call(this);
      this.onqualitylevelsloaded({ detail: this.player.qualityLevels });
      this.onqualitychanging({ detail: this.player.qualityLevels[this.player.quality] });
      this.open = true;
    }
  }, {
    key: "clickHandler",
    value: function clickHandler(object, index) {
      this.player.quality = index - 1;
      if (!index) this.onqualitychanging();
    }
  }, {
    key: "onqualitylevelsloaded",
    value: function onqualitylevelsloaded(event) {
      var _this2 = this;

      this.qualityLevels = event.detail;
      this.qualityLevels.forEach(function (item) {
        _this2.listOptions.push({ "label": Math.round(item.bitrate / 1000) + " Kbps" });
      });
      this.setState({ redraw: true });
    }
  }, {
    key: "onqualitychanging",
    value: function onqualitychanging(event) {
      var index = -1,
          autoLevel = false;
      try {
        autoLevel = this.player.qualityMode;
        index = autoLevel == "auto" ? -1 : event.detail ? this.qualityLevels.findIndex(function (levelObject) {
          return levelObject.qualityIndex == event.detail.qualityIndex && levelObject.level == event.detail.level;
        }) : this.props.quality.quality;
      } catch (e) {}

      this.setState({ activeIndex: index });
    }
  }, {
    key: "isOptionSelected",
    value: function isOptionSelected(item, index) {
      return index - 1 === this.state.activeIndex;
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Quality.__proto__ || Object.getPrototypeOf(Quality), "contextTypes", this);
    }
  }]);
  return Quality;
}(Panel);

var PlaybackRate = function (_Panel) {
  babelHelpers.inherits(PlaybackRate, _Panel);

  function PlaybackRate(props, context) {
    babelHelpers.classCallCheck(this, PlaybackRate);

    var _this = babelHelpers.possibleConstructorReturn(this, (PlaybackRate.__proto__ || Object.getPrototypeOf(PlaybackRate)).call(this, props, context));

    _this.state.title = "Speed"; // TODO: This needs to be localized
    _this.listOptions = [{ label: "0.25", value: 0.25 }, { label: "0.5", value: 0.5 }, { label: "Normal", value: 1 }, { label: "1.25", value: 1.25 }, { label: "1.5", value: 1.5 }, { label: "2", value: 2 }];

    _this.state.activeIndex = 2;
    _this.listOptions.forEach(function (option, index) {
      if (option.value == _this.player.playbackRate) {
        _this.state.activeIndex = index;
      }
    });
    _this.classList.add("amp-playback-rate");
    return _this;
  }

  babelHelpers.createClass(PlaybackRate, [{
    key: "clickHandler",
    value: function clickHandler(object, index) {
      this.player.playbackRate = object.value;
      babelHelpers.get(PlaybackRate.prototype.__proto__ || Object.getPrototypeOf(PlaybackRate.prototype), "clickHandler", this).call(this, object, index);
    }
  }, {
    key: "isOptionSelected",
    value: function isOptionSelected(item, index) {
      return parseFloat(item.value) === parseFloat(this.player.playbackRate);
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(PlaybackRate.__proto__ || Object.getPrototypeOf(PlaybackRate), "contextTypes", this);
    }
  }]);
  return PlaybackRate;
}(Panel);

var AudioTracks = function (_Panel) {
  babelHelpers.inherits(AudioTracks, _Panel);

  function AudioTracks(props, context) {
    babelHelpers.classCallCheck(this, AudioTracks);

    var _this = babelHelpers.possibleConstructorReturn(this, (AudioTracks.__proto__ || Object.getPrototypeOf(AudioTracks)).call(this, props, context));

    _this.listOptions = [];
    _this.audioTracks = [];
    _this.activeIds = [];
    _this.state.title = "Audio"; // TODO: Should be localized
    _this.state.activeIndex = -1;
    return _this;
  }

  babelHelpers.createClass(AudioTracks, [{
    key: "componentWillMount",
    value: function componentWillMount() {
      babelHelpers.get(AudioTracks.prototype.__proto__ || Object.getPrototypeOf(AudioTracks.prototype), "componentWillMount", this).call(this);
      this.audioTracks = this.player.audioTracks;
      this.oncanplaythrough({ detail: this.audioTracks });
      this.open = true;
    }
  }, {
    key: "clickHandler",
    value: function clickHandler(object, index) {
      this.player.audioTracks[index].enabled = true;
      this.setState({ activeIndex: index - 1 });
      this.setState({ redraw: true });
    }
  }, {
    key: "onmediachange",
    value: function onmediachange(event) {
      this.listOptions = [];
      this.activeIds = [];
    }
  }, {
    key: "oncanplaythrough",
    value: function oncanplaythrough(event) {
      var _this2 = this;

      this.audioTracks = this.player.audioTracks;
      this.audioTracks.forEach(function (item) {
        if (_this2.activeIds[item.id] == null) {
          var lang = _this2.player.localization.getLanguageString(item.language) || item.language;
          var label = item.label || lang || item.id || _this2.player.localization.getString("MSG_UNKNOWN");
          _this2.activeIds[item.id] = true;
          _this2.listOptions.push({ label: label });
        }
      });
      this.setState({ redraw: true });
    }
  }, {
    key: "isOptionSelected",
    value: function isOptionSelected(item, index) {
      return this.player.audioTracks[index].enabled;
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(AudioTracks.__proto__ || Object.getPrototypeOf(AudioTracks), "contextTypes", this);
    }
  }]);
  return AudioTracks;
}(Panel);

var SettingsPanel = function (_PanelMenu) {
  babelHelpers.inherits(SettingsPanel, _PanelMenu);

  function SettingsPanel(props, context) {
    babelHelpers.classCallCheck(this, SettingsPanel);

    var _this = babelHelpers.possibleConstructorReturn(this, (SettingsPanel.__proto__ || Object.getPrototypeOf(SettingsPanel)).call(this, props, context));

    _this.state.settingsLevel = 0;
    _this.state.bitrateLabel = "Auto";
    _this.state.captioningSettingState = null;
    return _this;
  }

  babelHelpers.createClass(SettingsPanel, [{
    key: "onready",
    value: function onready(event) {
      this.setState({ "autoplay": this.player.autoplay });
    }
  }, {
    key: "onseeked",
    value: function onseeked(event) {
      this.open = false;
    }
  }, {
    key: "toggle",
    value: function toggle() {
      babelHelpers.get(SettingsPanel.prototype.__proto__ || Object.getPrototypeOf(SettingsPanel.prototype), "toggle", this).call(this);
      this.state.settingsLevel = 0;
    }
  }, {
    key: "onqualitychanging",
    value: function onqualitychanging(event) {
      var brLabel = this.player.qualityMode === "auto" ? "Auto" : Math.round(event.detail.bitrate / 1000) + "Kbps"; // TODO: Needs to be localized
      this.state.quality = this.state.qualityLevels ? this.state.qualityLevels.findIndex(function (levelObject) {
        return levelObject.qualityIndex == event.detail.qualityIndex && levelObject.level == event.detail.level;
      }) : -1;
      this.setState({ bitrateLabel: brLabel });
    }
  }, {
    key: "changeState",
    value: function changeState(value, event) {
      var _this2 = this;

      if (event.type == "keydown" && (event.keyCode == 13 || event.keyCode == 32 || event.keyCode == 37 || event.keyCode == 38 || event.keyCode == 39 || event.keyCode == 40) && this.mouseFocus != true) {
        if (event.keyCode == 37 && this.state.settingsLevel == 0) return;
        this.state.keyDown = true;
      }
      setTimeout(function () {
        _this2.setState({ settingsLevel: value });
      }, 100);
    }
  }, {
    key: "autoplayToggle",
    value: function autoplayToggle() {
      this.setState({ "autoplay": this.player.autoplay = !this.player.autoplay });
    }
  }, {
    key: "componentWillUpdate",
    value: function componentWillUpdate(nextProps, nextState) {
      if (nextState.settingsLevel == 1 && this.refs.firstElem.state.captionsettings) this.setState({ 'captioningSettingState': this.refs.firstElem.state.captionsettings });else if (this.refs.firstElem != null && !isNaN(this.refs.firstElem.state.activeIndex)) this.setState({ 'playbackSettingState': this.refs.firstElem.state.activeIndex });
    }
  }, {
    key: "componentDidUpdate",
    value: function componentDidUpdate(prevProps, prevState) {
      if (this.state.keyDown) {
        if (typeof this.refs.firstElem.focus === "function") {
          this.refs.firstElem.focus();
        } else {
          this.refs.firstElem.refs.element.childNodes[1].focus();
        }
        this.state.keyDown = false;
      }
    }
  }, {
    key: "currentAudioTrackLabel",
    value: function currentAudioTrackLabel() {
      var audioTracks = this.player.audioTracks;
      for (var i = 0; i < audioTracks.length; i++) {
        var item = audioTracks[i];
        if (item != null && item.enabled) {
          var lang = this.player.localization.getLanguageString(item.language) || item.language;
          var label = item.label || lang || item.id || this.player.localization.getString("MSG_UNKNOWN");
          return label;
          console.log("xxxx");
        }
      }
      return "";
    }
  }, {
    key: "render",
    value: function render() {
      switch (this.state.settingsLevel) {
        case 0:
          var isCaptionEnabled = Utils.componentEnabled(this.plugin, "captioning");
          var isQualityEnabled = this.player.qualityLevels != null && this.player.qualityLevels.length > 1;
          var audioTracksAvailable = this.player.audioTracks != null && this.player.audioTracks.length > 1;
          var qualityRef = void 0,
              playbackRef = void 0;
          if (!isCaptionEnabled && isQualityEnabled) qualityRef = "firstElem";else if (!isCaptionEnabled && !isQualityEnabled) playbackRef = "firstElem";

          return React.createElement(
            "div",
            babelHelpers.extends({ ref: "element" }, this.propsList, { className: "amp-settings amp-panel " + this.className, role: "menu" }),
            isCaptionEnabled && React.createElement(
              ListItem,
              { ref: "firstElem", className: "amp-subtitles", onClick: this.changeState.bind(this, 1), direction: "right" },
              React.createElement(
                "span",
                { className: "amp-label" },
                "Subtitles"
              ),
              React.createElement("button", { className: "amp-icon amp-icon-right amp-right", tabIndex: "-1" }),
              React.createElement(
                "span",
                { className: "amp-right" },
                this.player.captioning.hidden ? "Off" : this.player.localization.getLanguageString(this.player.captioning.track.language)
              )
            ),
            isQualityEnabled && React.createElement(
              ListItem,
              { ref: qualityRef, onClick: this.changeState.bind(this, 2), direction: "right" },
              React.createElement(
                "span",
                { className: "amp-label" },
                "Quality"
              ),
              React.createElement("button", { className: "amp-icon amp-icon-right amp-right", tabIndex: "-1" }),
              React.createElement(
                "span",
                { className: "amp-right" },
                this.state.bitrateLabel
              )
            ),
            this.player.mode != "flash" && React.createElement(
              ListItem,
              { ref: playbackRef, className: "amp-playback-rate-button", onClick: this.changeState.bind(this, 3), direction: "right" },
              React.createElement(
                "span",
                { className: "amp-label" },
                "Speed"
              ),
              React.createElement("button", { className: "amp-icon amp-icon-right amp-right", tabIndex: "-1" }),
              React.createElement(
                "span",
                { className: "amp-right" },
                this.player.playbackRate === 1 ? 'Normal' : this.player.playbackRate
              )
            ),
            audioTracksAvailable && React.createElement(
              ListItem,
              { ref: "audiotracks", className: "amp-playback-rate-button", direction: "right", onClick: this.changeState.bind(this, 4) },
              React.createElement(
                "span",
                { className: "amp-label" },
                "Audio"
              ),
              React.createElement("button", { className: "amp-icon amp-icon-right amp-right", tabIndex: "-1" }),
              React.createElement(
                "span",
                { className: "amp-right" },
                this.currentAudioTrackLabel()
              )
            ),
            React.createElement(
              ListItem,
              { ref: "autoplay", className: this.state.autoplay ? "amp-autoplay-on" : "amp-autoplay-off", direction: "right", onClick: this.autoplayToggle.bind(this) },
              React.createElement(
                "span",
                { className: "amp-label" },
                "Autoplay"
              ),
              React.createElement("button", { className: "amp-right amp-icon amp-toggle" })
            )
          );
          break;
        case 1:
          return React.createElement(
            "div",
            { ref: "element", className: "amp-captioning-settings amp-panel " + this.className },
            React.createElement(CaptionSettings, { ref: "firstElem", captionState: this.state.captioningSettingState, gotoSettings: this.changeState.bind(this, 0) })
          );
          break;
        case 2:
          return React.createElement(
            "div",
            { ref: "element", className: "amp-bitrate amp-panel " + this.className },
            React.createElement(Quality, { ref: "firstElem", gotoSettings: this.changeState.bind(this, 0) })
          );
          break;
        case 3:
          return React.createElement(
            "div",
            { ref: "element", className: "amp-playback-rate amp-panel " + this.className },
            React.createElement(PlaybackRate, { ref: "firstElem", gotoSettings: this.changeState.bind(this, 0) })
          );
          break;
        case 4:
          return React.createElement(
            "div",
            { ref: "element", className: "amp-audio-tracks amp-panel " + this.className },
            React.createElement(AudioTracks, { ref: "firstElem", gotoSettings: this.changeState.bind(this, 0) })
          );
          break;
      }
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(SettingsPanel.__proto__ || Object.getPrototypeOf(SettingsPanel), "contextTypes", this);
    }
  }]);
  return SettingsPanel;
}(PanelMenu);

var ShareButton = function (_PanelControl) {
  babelHelpers.inherits(ShareButton, _PanelControl);

  function ShareButton(props, context) {
    babelHelpers.classCallCheck(this, ShareButton);

    var _this = babelHelpers.possibleConstructorReturn(this, (ShareButton.__proto__ || Object.getPrototypeOf(ShareButton)).call(this, props, context));

    _this.classList.add("amp-share", true);
    _this.state.altText = "MSG_SHARE";
    return _this;
  }

  babelHelpers.createClass(ShareButton, null, [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(ShareButton.__proto__ || Object.getPrototypeOf(ShareButton), "contextTypes", this);
    }
  }]);
  return ShareButton;
}(PanelControl);

var SharePanel = function (_PanelMenu) {
  babelHelpers.inherits(SharePanel, _PanelMenu);

  function SharePanel(props, context) {
    babelHelpers.classCallCheck(this, SharePanel);

    var _this = babelHelpers.possibleConstructorReturn(this, (SharePanel.__proto__ || Object.getPrototypeOf(SharePanel)).call(this, props, context));

    _this.listOptions = [{ label: "Facebook", id: "facebook", url: "www.facebook.html" }, { label: "Twitter", id: "twitter", url: "www.twitter.com" }, { label: "Email", id: "email", url: "" }, { label: "Embed", id: "embed", url: "" }];
    return _this;
  }

  babelHelpers.createClass(SharePanel, [{
    key: "clickHandler",
    value: function clickHandler(object, index, event) {
      var media = this.player.media;
      this.player.dispatch("share", { provider: object, link: media.link, embed: media.embed });
    }
  }, {
    key: "componentDidUpdate",
    value: function componentDidUpdate(prevProps, prevState) {
      if (this.state.keyDown) {
        this.refs.element.childNodes[0].focus();
        this.state.keyDown = false;
      }
    }
  }, {
    key: "render",
    value: function render() {
      var _this2 = this;

      var OptionsList = this.listOptions.map(function (object, index) {
        return React.createElement(
          ListItem,
          { key: index, className: "amp-list-item", onClick: _this2.clickHandler.bind(_this2, object, index) },
          React.createElement("button", { className: "amp-icon amp-" + object.id, tabIndex: "-1" }),
          React.createElement(
            "span",
            null,
            object.label
          )
        );
      });

      return React.createElement(
        "div",
        { ref: "element", className: "amp-share amp-panel " + this.className },
        OptionsList
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(SharePanel.__proto__ || Object.getPrototypeOf(SharePanel), "contextTypes", this);
    }
  }]);
  return SharePanel;
}(PanelMenu);

var CaptionToggleButton = function (_Toggle) {
  babelHelpers.inherits(CaptionToggleButton, _Toggle);

  function CaptionToggleButton(props, context) {
    babelHelpers.classCallCheck(this, CaptionToggleButton);

    var _this = babelHelpers.possibleConstructorReturn(this, (CaptionToggleButton.__proto__ || Object.getPrototypeOf(CaptionToggleButton)).call(this, props, context));

    _this.classList.add("amp-cc");
    _this.state.altText = "MSG_CLOSED_CAPTIONING";
    _this.state.toggled = _this.player.captioning ? !_this.player.captioning.hidden : false;
    return _this;
  }

  babelHelpers.createClass(CaptionToggleButton, [{
    key: "onmediachange",
    value: function onmediachange() {
      setTimeout(this.forceUpdate.bind(this), 1);
    }
  }, {
    key: "onChange",
    value: function onChange(event) {
      this.player.captioning.hidden = !this.player.captioning.hidden;
    }
  }, {
    key: "render",
    value: function render() {
      if (!Utils.componentEnabled(this.plugin, "captioning")) return null;

      return babelHelpers.get(CaptionToggleButton.prototype.__proto__ || Object.getPrototypeOf(CaptionToggleButton.prototype), "render", this).call(this);
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(CaptionToggleButton.__proto__ || Object.getPrototypeOf(CaptionToggleButton), "contextTypes", this);
    }
  }]);
  return CaptionToggleButton;
}(Toggle);

var JumpBack = function (_Control) {
  babelHelpers.inherits(JumpBack, _Control);

  function JumpBack(props, context) {
    babelHelpers.classCallCheck(this, JumpBack);

    var _this = babelHelpers.possibleConstructorReturn(this, (JumpBack.__proto__ || Object.getPrototypeOf(JumpBack)).call(this, props, context));

    _this.classList.add("amp-jump-back", true);
    _this.state.altText = "MSG_JUMP_BACK";
    return _this;
  }

  babelHelpers.createClass(JumpBack, [{
    key: "onClick",
    value: function onClick() {
      var player = this.player;
      if (player.playState === "playing" || player.playState === "paused") {
        this.player.currentTime = Math.max(this.player.currentTime - 10, 0);
      } else if (player.ended === true) {
        this.player.replay();
      }
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(JumpBack.__proto__ || Object.getPrototypeOf(JumpBack), "contextTypes", this);
    }
  }]);
  return JumpBack;
}(Control);

var JumpForward = function (_Control) {
  babelHelpers.inherits(JumpForward, _Control);

  function JumpForward(props, context) {
    babelHelpers.classCallCheck(this, JumpForward);

    var _this = babelHelpers.possibleConstructorReturn(this, (JumpForward.__proto__ || Object.getPrototypeOf(JumpForward)).call(this, props, context));

    _this.classList.add("amp-jump-forward", true);
    _this.state.altText = "MSG_JUMP_AHEAD";
    return _this;
  }

  babelHelpers.createClass(JumpForward, [{
    key: "onClick",
    value: function onClick(event) {
      var player = this.player;
      if (!player.ended && (player.playState == "playing" || player.playState == "paused")) {
        player.currentTime = Math.min(this.player.currentTime + 10, this.player.duration - 1);
      }
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(JumpForward.__proto__ || Object.getPrototypeOf(JumpForward), "contextTypes", this);
    }
  }]);
  return JumpForward;
}(Control);

var Poster = function (_Component) {
  babelHelpers.inherits(Poster, _Component);

  function Poster() {
    babelHelpers.classCallCheck(this, Poster);
    return babelHelpers.possibleConstructorReturn(this, (Poster.__proto__ || Object.getPrototypeOf(Poster)).apply(this, arguments));
  }

  babelHelpers.createClass(Poster, [{
    key: "onmediachange",
    value: function onmediachange(event) {
      this.setState({ src: this.player.evaluateBindings(this.player.media.poster) });
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement("img", { className: "amp-poster", src: this.state.src });
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Poster.__proto__ || Object.getPrototypeOf(Poster), "contextTypes", this);
    }
  }]);
  return Poster;
}(Component);

var Title = function (_Component) {
  babelHelpers.inherits(Title, _Component);

  function Title(props, context) {
    babelHelpers.classCallCheck(this, Title);

    var _this = babelHelpers.possibleConstructorReturn(this, (Title.__proto__ || Object.getPrototypeOf(Title)).call(this, props, context));

    _this.classList.add("amp-title", true);
    return _this;
  }

  babelHelpers.createClass(Title, [{
    key: "onmediachange",
    value: function onmediachange(event) {
      this.setState({ textContent: this.player.media.title });
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Title.__proto__ || Object.getPrototypeOf(Title), "contextTypes", this);
    }
  }]);
  return Title;
}(Component);

var ErrorMsg = function (_Component) {
  babelHelpers.inherits(ErrorMsg, _Component);

  function ErrorMsg() {
    babelHelpers.classCallCheck(this, ErrorMsg);
    return babelHelpers.possibleConstructorReturn(this, (ErrorMsg.__proto__ || Object.getPrototypeOf(ErrorMsg)).apply(this, arguments));
  }

  babelHelpers.createClass(ErrorMsg, [{
    key: "onerror",
    value: function onerror(event) {
      this.setState({ error: this.l10n.MSG_ERROR_DEFAULT });
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement(
        "div",
        { className: "amp-error-msg" },
        React.createElement("div", { dangerouslySetInnerHTML: { __html: this.state.error } })
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(ErrorMsg.__proto__ || Object.getPrototypeOf(ErrorMsg), "contextTypes", this);
    }
  }]);
  return ErrorMsg;
}(Component);

var ContextMenu = function (_Component) {
  babelHelpers.inherits(ContextMenu, _Component);

  function ContextMenu(props, context) {
    babelHelpers.classCallCheck(this, ContextMenu);
    return babelHelpers.possibleConstructorReturn(this, (ContextMenu.__proto__ || Object.getPrototypeOf(ContextMenu)).call(this, props, context));
  }

  babelHelpers.createClass(ContextMenu, [{
    key: "render",
    value: function render() {
      var menuItems = [React.createElement(
        "li",
        { className: "amp-context-item amp-bold", key: "0" },
        "Akamai Adaptive Media Player"
      ), React.createElement(
        "li",
        { className: "amp-context-item", key: "1" },
        this.player.version
      )];

      if (typeof akamaiGetViewerId == "function" && akamaiGetViewerId() != null) menuItems.push(React.createElement(
        "li",
        { className: "amp-context-item amp-selectable", key: "2" },
        "Viewer ID: ",
        akamaiGetViewerId()
      ));

      var style = {
        top: this.props.y + "px",
        left: this.props.x + "px"
      };

      return React.createElement(
        "ul",
        { ref: "element", className: "amp-context-menu", style: style },
        menuItems
      );
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(ContextMenu.__proto__ || Object.getPrototypeOf(ContextMenu), "contextTypes", this);
    }
  }]);
  return ContextMenu;
}(Component);

var Ads = function (_Component) {
  babelHelpers.inherits(Ads, _Component);

  function Ads(props, context) {
    babelHelpers.classCallCheck(this, Ads);

    var _this = babelHelpers.possibleConstructorReturn(this, (Ads.__proto__ || Object.getPrototypeOf(Ads)).call(this, props, context));

    _this.classList.add("amp-ads", true);
    _this.state.msg = "";
    _this.state.time = "";
    _this.bindHandlers(["onadbreakstart", "onadbreakend", "onadstarted", "onadtimeremaining", "onadimpression"]);
    return _this;
  }

  babelHelpers.createClass(Ads, [{
    key: "onready",
    value: function onready() {
      this.controlsMode = this.plugin.mode;
      this.player.addEventListener("adbreakstart", this.onadbreakstart);
      this.player.addEventListener("adbreakend", this.onadbreakend);
      this.player.addEventListener("adstarted", this.onadstarted);
      this.player.addEventListener("adtimeremaining", this.onadtimeremaining);
      this.player.addEventListener("adimpression", this.onadimpression);
    }
  }, {
    key: "onmediachange",
    value: function onmediachange() {
      this.onadbreakend();
    }
  }, {
    key: "onadimpression",
    value: function onadimpression() {
      this.plugin.container.classList.update({ "amp-ad-overlaymode": true });
      this.player.ads.resize();
    }
  }, {
    key: "onadbreakstart",
    value: function onadbreakstart(event) {
      this.plugin.container.classList.update({ "amp-ad-break": true });
      this.player.container.classList.add("amp-ad-break");
      var adVO = event.detail;
      this.setState({ msg: "", time: "" });
      this.controlsMode = this.plugin.mode;
      this.plugin.mode = this.plugin.adMode;
    }
  }, {
    key: "onadstarted",
    value: function onadstarted(event) {
      var adVO = event.detail || {};
      var msg = adVO.position == null || adVO.totalAds == null ? "" : this.getLocalizedString("MSG_AD") + " " + adVO.position + " " + this.getLocalizedString("MSG_OF") + " " + adVO.totalAds;
      this.setState({ time: adVO.duration, msg: msg });
    }
  }, {
    key: "onadbreakend",
    value: function onadbreakend(event) {
      this.setState({ msg: "", time: "" });
      this.plugin.container.classList.update({ "amp-ad-break": false });
      this.player.container.classList.remove("amp-ad-break");
      this.plugin.mode = this.controlsMode;
    }
  }, {
    key: "onadtimeremaining",
    value: function onadtimeremaining(event) {
      if (this.player.playState == "ready") return;
      this.setState({ time: event.detail });
    }
  }, {
    key: "render",
    value: function render() {
      var time = this.state.time != "" ? Utils.formatTimecode(Math.ceil(this.state.time)) : this.state.time;

      return React.createElement(
        "div",
        { ref: "containerNode", className: "amp-ads" },
        React.createElement("div", { ref: "container", className: "amp-ad-container" }),
        React.createElement(
          "div",
          { className: "amp-bar amp-ad-info" },
          React.createElement(
            "div",
            { ref: "count", className: "amp-ad-component amp-ad-count" },
            this.state.msg
          ),
          React.createElement("div", { ref: "spacerLeft", className: "amp-spacer" }),
          React.createElement("div", { ref: "spacerRight", className: "amp-spacer" }),
          React.createElement(
            "div",
            { ref: "time", className: "amp-ad-component amp-ad-time" },
            time
          )
        )
      );
    }
  }, {
    key: "container",
    get: function get() {
      return this.refs.container;
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Ads.__proto__ || Object.getPrototypeOf(Ads), "contextTypes", this);
    }
  }]);
  return Ads;
}(Component);

var Airplay = function (_Control) {
  babelHelpers.inherits(Airplay, _Control);

  function Airplay(props, context) {
    babelHelpers.classCallCheck(this, Airplay);

    var _this = babelHelpers.possibleConstructorReturn(this, (Airplay.__proto__ || Object.getPrototypeOf(Airplay)).call(this, props, context));

    _this.state.available = false;
    _this.state.playing = false;
    _this.classList.add("amp-airplay", true);
    return _this;
  }

  babelHelpers.createClass(Airplay, [{
    key: "onready",
    value: function onready(event) {
      var _this2 = this;

      if (this.isAvailable === false) return;

      var video = this.player.mediaElement;
      video.setAttribute("x-webkit-airplay", "allow");
      video.addEventListener("webkitplaybacktargetavailabilitychanged", function (event) {
        _this2.state.available = event.availability == "available";
        _this2.playing = video.webkitCurrentPlaybackTargetIsWireless;
        _this2.forceUpdate();
      });
      window.addEventListener("webkitcurrentplaybacktargetiswireless", function (event) {
        _this2.playing = video.webkitCurrentPlaybackTargetIsWireless;
      });
    }
  }, {
    key: "onClick",
    value: function onClick() {
      this.player.mediaElement.webkitShowPlaybackTargetPicker();
    }
  }, {
    key: "render",
    value: function render() {
      if (this.available === false) return null;

      return babelHelpers.get(Airplay.prototype.__proto__ || Object.getPrototypeOf(Airplay.prototype), "render", this).call(this);
    }
  }, {
    key: "isAvailable",
    get: function get() {
      return window.WebKitPlaybackTargetAvailabilityEvent != null;
    }
  }, {
    key: "available",
    get: function get() {
      return this.isAvailable && this.state.available;
    }
  }, {
    key: "playing",
    set: function set(value) {
      this.state.playing = value;
      this.plugin.container.classList.update({ "amp-playback-target-airplay": value });
    },
    get: function get() {
      return this.state.playing;
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(Airplay.__proto__ || Object.getPrototypeOf(Airplay), "contextTypes", this);
    }
  }]);
  return Airplay;
}(Control);

var PiP = function (_Control) {
  babelHelpers.inherits(PiP, _Control);

  function PiP(props, context) {
    babelHelpers.classCallCheck(this, PiP);

    var _this = babelHelpers.possibleConstructorReturn(this, (PiP.__proto__ || Object.getPrototypeOf(PiP)).call(this, props, context));

    _this.classList.add("amp-pip", true);
    return _this;
  }

  babelHelpers.createClass(PiP, [{
    key: "onplaying",
    value: function onplaying(event) {
      this.forceUpdate();
    }
  }, {
    key: "onClick",
    value: function onClick(event) {
      var mediaElement = this.player.mediaElement;
      mediaElement.webkitSetPresentationMode(mediaElement.webkitPresentationMode === "picture-in-picture" ? "inline" : "picture-in-picture");
    }
  }, {
    key: "render",
    value: function render() {
      if (!this.available) return null;

      return babelHelpers.get(PiP.prototype.__proto__ || Object.getPrototypeOf(PiP.prototype), "render", this).call(this);
    }
  }, {
    key: "available",
    get: function get() {
      var mediaElement = this.player.mediaElement;
      if (mediaElement == null || typeof mediaElement.webkitSupportsPresentationMode != "function" || typeof mediaElement.webkitSetPresentationMode != "function") return false;else return mediaElement.webkitSupportsPresentationMode("picture-in-picture");
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(PiP.__proto__ || Object.getPrototypeOf(PiP), "contextTypes", this);
    }
  }]);
  return PiP;
}(Control);

var ChromeCast = function (_Control) {
  babelHelpers.inherits(ChromeCast, _Control);

  function ChromeCast(props, context) {
    babelHelpers.classCallCheck(this, ChromeCast);

    var _this = babelHelpers.possibleConstructorReturn(this, (ChromeCast.__proto__ || Object.getPrototypeOf(ChromeCast)).call(this, props, context));

    _this.state.available = false;
    _this.classList.add("amp-chromecast", true);
    return _this;
  }

  babelHelpers.createClass(ChromeCast, [{
    key: "onready",
    value: function onready() {
      if (!Utils.componentEnabled(this.plugin, "chromecast")) return;

      this.setState({ available: this.player.chromecast.available });
    }
  }, {
    key: "onplaybacktargetavailabilitychange",
    value: function onplaybacktargetavailabilitychange(event) {
      if (event.detail.target === "chromecast") {
        this.setState({ available: event.detail.available });
      }
    }
  }, {
    key: "onClick",
    value: function onClick() {
      this.player.chromecast.launch();
    }
  }, {
    key: "render",
    value: function render() {
      if (this.available === false) return null;

      return babelHelpers.get(ChromeCast.prototype.__proto__ || Object.getPrototypeOf(ChromeCast.prototype), "render", this).call(this);
    }
  }, {
    key: "available",
    get: function get() {
      return this.state.available;
    }
  }], [{
    key: "contextTypes",
    get: function get() {
      return babelHelpers.get(ChromeCast.__proto__ || Object.getPrototypeOf(ChromeCast), "contextTypes", this);
    }
  }]);
  return ChromeCast;
}(Control);

var Mode = function () {
  function Mode() {
    babelHelpers.classCallCheck(this, Mode);
  }

  babelHelpers.createClass(Mode, null, [{
    key: "AUTO",
    get: function get() {
      return "auto";
    }
  }, {
    key: "FIXED",
    get: function get() {
      return "fixed";
    }
  }, {
    key: "PERSISTENT",
    get: function get() {
      return "persistent";
    }
  }]);
  return Mode;
}();

var ActiveState = function () {
  function ActiveState() {
    babelHelpers.classCallCheck(this, ActiveState);
  }

  babelHelpers.createClass(ActiveState, null, [{
    key: "ACTIVE",
    get: function get() {
      return "active";
    }
  }, {
    key: "INACTIVE",
    get: function get() {
      return "inactive";
    }
  }, {
    key: "IDLE",
    get: function get() {
      return "idle";
    }
  }]);
  return ActiveState;
}();

var UI = function (_Component) {
  babelHelpers.inherits(UI, _Component);

  function UI(props) {
    babelHelpers.classCallCheck(this, UI);

    var _this = babelHelpers.possibleConstructorReturn(this, (UI.__proto__ || Object.getPrototypeOf(UI)).call(this, props));

    _this.feature = "ui";
    _this.state.playState = "ready";
    _this.state.open = false;
    _this.state.dragging = false;
    _this.state.mode = _this.config.mode;
    _this.isTouchDevice = akamai.amp.Utils.isTouch;
    _this.contextMenuClick = _this.contextMenuClick.bind(_this);
    _this.autoHide = _this.config.autoHide != null ? _this.config.autoHide : 3;
    _this.timeout = null;

    var dispatcher = new akamai.amp.EventDispatcher(_this);
    for (var key in dispatcher) {
      var value = dispatcher[key];
      if (typeof value == "function") {
        _this[key] = value.bind(dispatcher);
      }
    }
    _this.dispatcher = dispatcher;

    _this.bindHandlers(["onMouseEnter", "onMouseLeave", "onFocus", "onBlur", "onKeyDown", "onContextMenu", "onClick"]);
    return _this;
  }

  babelHelpers.createClass(UI, [{
    key: "getChildContext",
    value: function getChildContext() {
      return {
        player: this.props.player,
        config: this.props.config,
        plugin: this
      };
    }
  }, {
    key: "onready",
    value: function onready(event) {
      var _this2 = this;

      if (this.config.mode != null) this.mode = this.config.mode;

      this.container.classList.add("amp-" + this.player.mode);
      this.container.classList.update({
        "amp-cc-active": this.player.captioning && !this.player.captioning.hidden,
        "amp-touch": this.isTouchDevice
      });

      if (this.player.captioning != null) {
        this.player.captioning.addEventListener("visibilitychange", this.onvisibilitychange.bind(this));
      }

      this.player.container.addEventListener("mouseenter", this.onMouseEnter);
      this.player.container.addEventListener("mouseleave", this.onMouseLeave);

      // Workaround for UI flicker and FOUT
      setTimeout(function () {
        _this2.container.element.style.display = "block";
      }, 10);
    }
  }, {
    key: "ondestroy",
    value: function ondestroy() {
      this.stopIdleTimeout();
      this.player.container.removeEventListener("mouseenter", this.onMouseEnter);
      this.player.container.removeEventListener("mouseleave", this.onMouseLeave);
    }
  }, {
    key: "onvisibilitychange",
    value: function onvisibilitychange(event) {
      this.container.classList.update({ "amp-cc-active": event.detail });
    }
  }, {
    key: "onplaybacktargetchange",
    value: function onplaybacktargetchange(event) {
      this.container.classList.update({ "amp-remote-playback": event.detail.value != "amp" });
    }
  }, {
    key: "onmediachange",
    value: function onmediachange(event) {
      var media = this.player.media;
      this.container.classList.update({ "amp-audio": media.medium == "audio" });
      this.container.classList.update({ "amp-text-tracks": media.tracks && media.tracks.length });
    }
  }, {
    key: "onmutemouseover",
    value: function onmutemouseover(event) {
      this.stopIdleTimeout();
      this.refs.volume.open = true;
    }
  }, {
    key: "onmutemouseout",
    value: function onmutemouseout(event) {
      var _this3 = this;

      this.stopIdleTimeout();
      this.timeout = setTimeout(function () {
        _this3.refs.volume.open = false;
      }, 350);
    }
  }, {
    key: "onvolumemouseover",
    value: function onvolumemouseover(event) {
      this.stopIdleTimeout();
    }
  }, {
    key: "onfullscreenchange",
    value: function onfullscreenchange(event) {
      this.container.classList.update({ "amp-full-screen": event.detail });
    }
  }, {
    key: "onmutechange",
    value: function onmutechange(event) {
      this.container.classList.update({ "amp-muted": event.detail });
    }
  }, {
    key: "onplaystatechange",
    value: function onplaystatechange(event) {
      var _updates;

      var updates = (_updates = {}, babelHelpers.defineProperty(_updates, "amp-waiting", false), babelHelpers.defineProperty(_updates, "amp-" + event.detail.previous, false), babelHelpers.defineProperty(_updates, "amp-" + event.detail.value, true), _updates);
      this.container.classList.update(updates);
    }

    // onmediasequenceinitialized(event) {
    //   const detail = event.detail
    //   if (detail.autoplay === true && detail.muted === true) {
    //     const container = this.player.getContainer()
    //     const unmute = (event) => {
    //       if (!event.target.classList.contains("amp-mute")) {
    //         this.player.muted = false
    //       }
    //       container.removeEventListener("click", unmute)
    //     }
    //     container.addEventListener("click", unmute)
    //   }
    // }

  }, {
    key: "oncanplaythrough",
    value: function oncanplaythrough(event) {
      this.container.classList.update({ "amp-can-play": true });
    }
  }, {
    key: "onplaying",
    value: function onplaying(event) {
      this.container.classList.update({ "amp-waiting": false });
      this.container.classList.remove("amp-qualitychange-manual");
      this.container.classList.remove("amp-qualitychange-auto");
      this.startIdleTimeout();
    }
  }, {
    key: "onpause",
    value: function onpause(event) {
      this.stopIdleTimeout();
    }
  }, {
    key: "onwaiting",
    value: function onwaiting(event) {
      var _this4 = this;

      this.container.classList.update({ "amp-waiting": true });
      this.player.once("timeupdate", function () {
        if (_this4.player.seeking === false) {
          _this4.container.classList.update({ "amp-waiting": false });
        }
      });
    }
  }, {
    key: "onbusy",
    value: function onbusy(event) {
      this.container.classList.update({ "amp-busy": event.detail });
    }
  }, {
    key: "onseeking",
    value: function onseeking(event) {
      this.container.classList.update({ "amp-waiting": true });
    }
  }, {
    key: "onseeked",
    value: function onseeked(event) {
      this.container.classList.update({ "amp-waiting": false });
    }
  }, {
    key: "onerror",
    value: function onerror(event) {
      this.container.classList.update({ "amp-error": true });
    }
  }, {
    key: "onislive",
    value: function onislive(event) {
      this.container.classList.update({ "amp-is-live": event.detail });
    }
  }, {
    key: "ontemporaltypechange",
    value: function ontemporaltypechange(event) {
      var _updates2;

      var updates = (_updates2 = {}, babelHelpers.defineProperty(_updates2, "amp-" + event.detail.previous, false), babelHelpers.defineProperty(_updates2, "amp-" + event.detail.value, true), _updates2);
      this.container.classList.update(updates);
    }
  }, {
    key: "onqualitychanging",
    value: function onqualitychanging(event) {
      this.container.classList.add("amp-qualitychange-" + this.player.qualityMode);
    }
  }, {
    key: "onqualitychange",
    value: function onqualitychange(event) {
      this.container.classList.remove("amp-qualitychange-" + this.player.qualityMode);
    }
  }, {
    key: "onqualityswitched",
    value: function onqualityswitched(event) {
      this.onqualitychange(event);
    }
  }, {
    key: "togglePlayPause",
    value: function togglePlayPause() {
      if (this.player.playState == "ready" || this.player.paused) {
        this.player.play();
      } else {
        this.player.pause();
      }
    }
  }, {
    key: "startIdleTimeout",
    value: function startIdleTimeout() {
      var _this5 = this;

      if (this.refs.settingsPanel.open) {
        this.refs.settingsPanel.close();
        this.refs.volume.open = false;
      }
      this.stopIdleTimeout();
      this.timeout = setTimeout(function () {
        _this5.activeState = ActiveState.IDLE;
      }, this.autoHide * 1000);
    }
  }, {
    key: "stopIdleTimeout",
    value: function stopIdleTimeout() {
      clearTimeout(this.timeout);
    }
  }, {
    key: "onMouseEnter",
    value: function onMouseEnter(event) {
      if (this.isTouchDevice) return;

      this.stopIdleTimeout();
      this.activeState = ActiveState.ACTIVE;
    }
  }, {
    key: "onMouseLeave",
    value: function onMouseLeave(event) {
      var _this6 = this;

      if (this.isTouchDevice) return;

      var element = document.elementFromPoint(event.clientX, event.clientY);
      if (this.player.playState == "ready" || this.open || this.player.container.contains(element)) return;

      if (this.dragging) {
        var handler = function handler() {
          window.removeEventListener("mouseup", handler);
          if (!akamai.amp.Utils.isMouseOverElement(_this6.container.element)) _this6.onMouseLeave(event);
        };
        window.addEventListener("mouseup", handler);
        return;
      }

      this.activeState = ActiveState.INACTIVE;
    }
  }, {
    key: "onClick",
    value: function onClick() {
      if (this.isTouchDevice) {
        if (this.activeState == ActiveState.ACTIVE) {
          this.activeState = ActiveState.IDLE;
          this.stopIdleTimeout();
        } else {
          this.activeState = ActiveState.ACTIVE;
          this.startIdleTimeout();
        }
      } else {
        this.togglePlayPause();
      }
    }
  }, {
    key: "isFocused",
    value: function isFocused(element) {
      var container = this.container.element;
      return element == container || container.contains(element);
    }
  }, {
    key: "onFocus",
    value: function onFocus(event) {
      var focused = this.isFocused(event.target);
      this.focused = event.target.nodeName != "BUTTON" && focused;
      if (focused) this.container.classList.add("amp-active");
    }
  }, {
    key: "onBlur",
    value: function onBlur(event) {
      var focused = this.isFocused(event.target);
      this.focused = event.target.nodeName != "BUTTON" && focused;
      if (!focused) this.onMouseLeave(event);
    }
  }, {
    key: "onKeyDown",
    value: function onKeyDown(event) {
      switch (event.keyCode) {
        case 77:
          // M
          this.player.muted = !this.player.muted;
          break;

        case 67:
          // C
          this.player.captioning.hidden = !this.player.captioning.hidden;
          break;

        case 70:
          // F
          this.player.displayState == this.player.displayState == 'full-screen' ? this.player.exitFullScreen() : this.player.enterFullScreen();
          break;
      }

      if (this.focused == false) return;

      switch (event.keyCode) {
        case 13: // Enter
        case 32:
          // Space
          this.togglePlayPause();
          break;

        case 39: // Left
        case 37:
          // Right
          if (this.refs.volume.focused === false) this.refs.progress.onKeyDown(event);
          break;

        case 38: // Up
        case 40:
          // Down
          if (this.refs.progress.focused === false) this.refs.volume.onKeyDown(event);
          break;
      }
    }
  }, {
    key: "contextMenuClick",
    value: function contextMenuClick(event) {
      if (/selectable/.test(event.target.className)) return false;
      this.removeContextMenu(event);
      return false;
    }
  }, {
    key: "removeContextMenu",
    value: function removeContextMenu(event) {
      document.body.removeEventListener("click", this.contextMenuClick);
      var parent = document.body;
      parent.removeChild(this.contextMenu);
      this.contextMenu = null;
    }
  }, {
    key: "onContextMenu",
    value: function onContextMenu(event) {
      if (this.debug === true) return;

      event.preventDefault();

      var parent = document.body;
      var contextMenu = React.createElement(ContextMenu, { player: this.player, x: event.pageX, y: event.pageY });

      if (this.contextMenu != null) this.removeContextMenu();

      this.contextMenu = document.createElement("div");
      parent.appendChild(this.contextMenu);
      document.body.addEventListener("click", this.contextMenuClick);
      ReactDOM.render(contextMenu, this.contextMenu);
    }
  }, {
    key: "replay",
    value: function replay() {
      this.player.replay();
    }
  }, {
    key: "render",
    value: function render() {
      return React.createElement(
        Container,
        { ref: "container",
          tabIndex: "0",
          style: { display: "none" },
          classList: ["amp-react", "amp-ui", "amp-ready", "amp-active"],
          onFocus: this.onFocus,
          onBlur: this.onBlur,
          onKeyDown: this.onKeyDown,
          onContextMenu: this.onContextMenu
        },
        React.createElement("div", { className: "amp-interactive", onClick: this.onClick }),
        React.createElement(Poster, { ref: "poster" }),
        React.createElement(Title, { ref: "title" }),
        React.createElement(Ads, { ref: "ads" }),
        React.createElement(PauseOverlay, { ref: "pauseOverlay" }),
        React.createElement(BufferingOverlay, { ref: "bufferingOverlay" }),
        React.createElement(Progress, { ref: "progress" }),
        React.createElement(
          Container,
          { ref: "controls", className: "amp-controls amp-bar" },
          React.createElement(PlayPause, { ref: "playpause" }),
          React.createElement(JumpBack, { ref: "jumpback" }),
          React.createElement(JumpForward, { ref: "jumpforward" }),
          React.createElement(
            "div",
            { ref: "timeDisplay", className: "amp-time-display" },
            React.createElement(CurrentTime, { ref: "currentTime" }),
            React.createElement("span", { ref: "timeSeparator", className: "amp-time-separator" }),
            React.createElement(Duration, { ref: "duration" }),
            React.createElement(Live, { ref: "live" })
          ),
          React.createElement(Mute, { ref: "mute" }),
          React.createElement(Volume, { ref: "volume" }),
          React.createElement("div", { ref: "spacerLeft", className: "amp-spacer" }),
          React.createElement("div", { ref: "spacerRight", className: "amp-spacer" }),
          React.createElement(ShareButton, { ref: "share", panel: "sharePanel" }),
          React.createElement(CaptionToggleButton, { ref: "cc" }),
          React.createElement(SettingsButton, { ref: "settings", panel: "settingsPanel" }),
          React.createElement(PiP, { ref: "pip" }),
          React.createElement(Airplay, { ref: "airplay" }),
          React.createElement(ChromeCast, { ref: "chromecast" }),
          React.createElement(Fullscreen, { ref: "fullscreen" })
        ),
        React.createElement(Unmute, { ref: "unmute" }),
        React.createElement(SharePanel, { ref: "sharePanel" }),
        React.createElement(SettingsPanel, { ref: "settingsPanel" }),
        React.createElement(ErrorMsg, { ref: "error" }),
        this.state.contextMenu
      );
    }
  }, {
    key: "destroy",
    value: function destroy() {
      ReactDOM.unmountComponentAtNode(this._container);
    }
  }, {
    key: "adMode",
    get: function get() {
      return this.config.adMode != null ? this.config.adMode : Mode.AUTO;
    }
  }, {
    key: "debug",
    get: function get() {
      return this.config.debug != null ? this.config.debug : this.player.config.debug;
    }
  }, {
    key: "container",
    get: function get() {
      return this.refs.container;
    }
  }, {
    key: "ads",
    get: function get() {
      return this.refs.ads;
    }
  }, {
    key: "components",
    get: function get() {
      return this.refs;
    }
  }, {
    key: "controls",
    get: function get() {
      return this.refs.controls;
    }
  }, {
    key: "mode",
    get: function get() {
      return this.state.mode || Mode.AUTO;
    },
    set: function set(value) {
      var _container$classList$,
          _this7 = this;

      value = value || Mode.AUTO;
      var mode = this.state.mode;
      this.container.classList.update((_container$classList$ = {}, babelHelpers.defineProperty(_container$classList$, "amp-controls-" + mode, false), babelHelpers.defineProperty(_container$classList$, "amp-controls-" + value, true), _container$classList$));
      this.setState({ mode: value });

      if (value === Mode.AUTO) {
        if (this.idleUtil == null && !this.isTouchDevice) {
          this.idleUtil = new akamai.amp.IdleUtil(this.player.viewComponent, this.autoHide * 1000);
          this.idleUtil.addEventListener("activechange", function (event) {
            var inactive = _this7.activeState == ActiveState.INACTIVE ? ActiveState.IDLE : ActiveState.INACTIVE;
            _this7.activeState = event.detail.active !== true ? inactive : ActiveState.ACTIVE;
          });
        }
        if (this.idleUtil != null) {
          this.idleUtil.start();
        }
      } else {
        if (this.idleUtil != null) {
          this.idleUtil.stop();
        }
      }

      // TODO: Remove these when controls plugin is completely removed from amp-web
      this.player.container.classList.remove("amp-controls-" + mode);
      this.player.container.classList.add("amp-controls-" + value);
    }
  }, {
    key: "dragging",
    get: function get() {
      return this.state.dragging;
    },
    set: function set(value) {
      if (value == true) {
        this.stopIdleTimeout();
      } else {
        this.startIdleTimeout();
      }
      this.container.classList.update(babelHelpers.defineProperty({}, "amp-dragging", value));
      this.setState({ dragging: value });
    }
  }, {
    key: "open",
    get: function get() {
      return this.state.open;
    },
    set: function set(value) {
      this.container.classList.update(babelHelpers.defineProperty({}, "amp-open", value));
      this.setState({ open: value });
    }
  }, {
    key: "activeState",
    set: function set(value) {
      var active = value == ActiveState.ACTIVE;
      var inactive = value == ActiveState.INACTIVE;
      var idle = value == ActiveState.IDLE;
      this.container.classList.update({ "amp-active": active, "amp-inactive": inactive, "amp-idle": idle });

      // TODO: Remove these when controls plugin is completely removed from amp-web
      var action = active ? "add" : "remove";
      this.player.container.classList[action]("amp-active");

      action = inactive ? "add" : "remove";
      this.player.container.classList[action]("amp-inactive");

      if (inactive === true) {
        for (var key in this.refs) {
          var _value = this.refs[key];
          if (_value instanceof Panel && _value.open === true) {
            _value.open = false;
          }
        }
        this.startIdleTimeout();
      }
    },
    get: function get() {
      switch (true) {
        case this.container.classList.contains("amp-active"):
          return ActiveState.ACTIVE;

        case this.container.classList.contains("amp-inactive"):
          return ActiveState.INACTIVE;

        default:
          return ActiveState.IDLE;
      }
    }
  }, {
    key: "adInProgress",
    get: function get() {
      return this.player.ads && this.player.ads.inProgress;
    }
  }], [{
    key: "create",
    value: function create(player, config, key) {
      if (config.native === true) {
        var ui = { config: config };
        player.once("ready", function () {
          return player.mediaElement.controls = true;
        });
        if (player.ads != null) {
          ui.ads = {
            container: document.createElement("div")
          };
          player.container.appendChild(ui.ads.container);
        }
        return Promise.resolve(ui);
      }

      return new Promise(function (resolve, reject) {
        player.once("ready", function () {
          if (player.mediaElement.setPlayerProperty != null) player.mediaElement.setPlayerProperty("controlsMode", "none");
        });

        var ui = ReactDOM.render(React.createElement(UI, { player: player, config: config }), player.container);
        player.adContainer = ui.ads.container;
        resolve(ui);
      });
    }
  }, {
    key: "childContextTypes",
    get: function get() {
      return {
        player: PropTypes.object,
        config: PropTypes.object,
        plugin: PropTypes.object
      };
    }
  }]);
  return UI;
}(Component);

akamai.amp.AMP.registerPlugin("react", ["html", "flash", "external"], UI.create);

exports.UI = UI;
exports.Component = Component;
exports.Container = Container;
exports.ClassList = ClassList;
exports.Panel = Panel;
exports.PanelMenu = PanelMenu;
exports.PanelControl = PanelControl;
exports.ListItem = ListItem;
exports.Mode = Mode;

}((this.akamai.amp.react = this.akamai.amp.react || {})));
//# sourceMappingURL=React.js.map
