function createAccessibilityMenu(div, player) {
  if (typeof div == "string") {
    div = document.getElementById(div) || document.querySelector(div);
  }
  div.classList.add("acc_menu")

  var items = [];
  var data = [
    {text: "Play", action: function () {
      amp.play();
    }},
    {text: "Pause", action: function () {
      amp.pause();
    }},
    {text: "Forward", action: function () {
      var time = Math.min(amp.currentTime + (amp.duration * 0.2), amp.duration);
      amp.currentTime = time;
    }},
    {text: "Back", action: function () {
      var time = Math.max(amp.currentTime - (amp.duration * 0.2), 0);
      amp.currentTime = time;
    }},
    {text: "Stop", action: function () {
      amp.end();
    }},
    {text: "Volume Up", action: function () {
      amp.volume = amp.volume + 0.1;
    }},
    {text: "Volume Down", action: function () {
      amp.volume = amp.volume - 0.1;
    }},
    {text: "Mute", action: function () {
      amp.mute();
    }},
    {text: "Unmute", action: function () {
      amp.unmute();
    }},
    {text: "Fullscreen", action: function (event) {
      amp.enterFullScreen()
    }}
  ];

  function keypressHandler(action, event) {
    if (event.keyCode == 32 || event.keyCode == 13) {
      action(event);
    }
  }

  function createItem(data, index) {
    var item = document.createElement("button");
    var id = data.text.toLowerCase().replace(/\W+(.)/g, function(match, chr) { return chr.toUpperCase(); });
    item.className = "acc_menu_item";
    item.id = id + "Button";
    item.dataset.id = id;
    item.dataset.data = data;
    item.title = data.text;
    item.textContent = data.text;
    item.tabindex = index;
    item.onclick = data.action;
    item.onkeypress = keypressHandler.bind(item, data.action);
    return item;
  }

  function render() {
    var paused = (amp.ads.inProgress) ? amp.ads.paused : amp.paused
    items.play.hidden = !paused;
    items.pause.hidden = paused;
    items.volumeDown.disabled = amp.volume == 0;
    items.volumeUp.disabled = amp.volume == 1;
    items.mute.hidden = amp.volume == 0;
    items.unmute.hidden = amp.volume != 0;
  }

  data.forEach(function (data, index) {
    var item = createItem(data, index);
    items.push(item);
    items[item.dataset.id] = item;
    div.appendChild(item);
  })

  player.addEventListener("playing", render);
  player.addEventListener("pause", render);
  player.addEventListener("ended", render);
  player.addEventListener("volumechange", render);
  player.addEventListener("playstatechange", render);
  render();
}
