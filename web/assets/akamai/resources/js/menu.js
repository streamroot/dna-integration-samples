function SimpleMenu(element, items) {
  element.innerHTML = items.map(function (item, index) {
    return "<a style=\"cursor:pointer;\" onclick=\"loadVideo(" + index + ")\">" + item.title + "</a>";
  }).join(" | ");
}

function Menu() {
  this.transforms = [];
  this.items = [];
}
Menu.startIndex = (function () {
  var index = parseInt(location.hash.replace("#", ""));
  if (isNaN(index)) {
    index = parseInt(location.search.replace(/.*item=([0-9]+).*/, "$1"));
    if (isNaN(index)) {
      index = 0;
    }
  }
  return index;
}());
Menu.constructor = Menu;
Menu.prototype = {
  view: null,
  selected: null,
  selectedIndex: null,
  value: null,
  items: null,
  data: null,
  columnCount: 3,
  loading: false,
  select: function (index, select) {
    if (select === undefined)
      select = true;
    if (this.selected != null)
      this.selected.className = this.selected.className.replace(" selected", "");

    if (index >= this.data.length)
      index = 0;

    if (index < 0)
      index = this.data.length - 1;

    if (index == this.selectedIndex)
      return;

    this.selectedIndex = index;
    this.value = this.data[this.selectedIndex];

    console.log("[AMP] Menu item selected", this.selectedIndex, this.value);

    this.selected = this.items[this.selectedIndex];
    this.selected.className += " selected";

    if (this.isSelectable() === false && select !== true)
      return

    this.onselected({index: this.selectedIndex, value: this.value});

    if (typeof this.onselect == "function")
      this.onselect({index: this.selectedIndex, value: this.value});
  },
  init: function (index, select) {
    if (index == null)
      index = Menu.startIndex;
    if (select === undefined)
      select = false

    this.select(index, select);
  },
  onselected: function () {
    if (amp.feed != null) {
      if (typeof this.value == "string") {
        amp.feed.url = this.value;
      }
      else {
        amp.feed.data = this.value;
      }
    }
    else {
      amp.media = this.value;
    }
  },
  create: function (view, data, isFeed) {
    this.view = view;
    this.data = data;
    this.view.innerHTML = "";

    if (typeof data[0] == "string") {
      this.load(data);
      return;
    }

    var item, i, xhr;

    for (i = 0; i < this.data.length; i++) {
      item = this.data[i];

      if (isFeed) {
        item = this.transform(item);
      }

      akamai.amp.Utils.chain(this.transforms, item)
        .then(this.add.bind(this, i))
        .catch(this.add.bind(this, i, item));
    }
  },
  load: function (feeds) {
    this.loading = true;

    var items   = [],
      index   = -1,
      menu  = this;

    function loadFeed(url) {
      akamai.amp.Utils.getFeed(url)
        .then(function (feed) {
          items.push(feed);
          nextFeed();
        })
        .catch(function (error) {
          console.log(error);
          nextFeed();
        });
    }

    function nextFeed() {
      ++index;

      if (index >= feeds.length) {
        this.loading = false;
        menu.create(menu.view, items, true);
        if (typeof this.onload == "function")
          this.onload();
      }
      else {
        loadFeed(feeds[index]);
      }
    }
    nextFeed = nextFeed.bind(this);
    nextFeed();
  },
  add: function (index, item) {
    var element = document.createElement("a"),
        c = (index % this.columnCount) + 1,
        r = Math.floor(index / this.columnCount) + 1;

    element.className = "sample-menu-item sample-menu-r" + r + " sample-menu-c" + c;
    element.addEventListener("click", this.select.bind(this, index, true));

    var innerHTML = "";
    if (item.poster) {
      innerHTML += '<img class="sample-item-thumb" src="'+item.poster+'" />';
    }
    element.innerHTML = innerHTML + '<div class="sample-item-title">'+item.title+'</div>';

    this.view.appendChild(element);
    this.items.splice(index, 0, element);

    cn     = this.view.className;
    rows  = "rows-"+r;
    regexp   = new RegExp(rows);

    if (!regexp.test(cn)) {
      cn = cn.replace(/ rows-\d/, "");
      this.view.className = cn + " " + rows;
    }
  },
  transform: function (feed) {
    var item, thumb;

    item = (feed.channel.item.length > 0) ? feed.channel.item[0] : feed.channel.item;
    thumb = item["media-thumbnail"] || item["media-group"]["media-thumbnail"];
    if (thumb && thumb["@attributes"])
      thumb = thumb["@attributes"].url;

    return {
      title: item.title,
      poster: thumb
    };
  },
  isSelectable: function() {
    return !(amp.config.media != null || amp.config.feed != null || amp.media.src != null || (amp.feed != null && (amp.feed.data != null || amp.feed.url != null)))
  }
};

document.addEventListener("DOMContentLoaded", function(event) {
  // create the menu before onload so the sample pages have the ability to override menu functionality
  window.menu = new Menu();

  window.addEventListener("load", function () {
    var element = document.querySelector(".sample-menu");
    var items = window.videos || window.feeds || window.media;

    if (element == null || items == null)
      return;

    var isFeed = (typeof items == "string") || (typeof items == "object" && items[0].channel != null)
    menu.create(element, items, isFeed);

    // This isn't the cleanest, but the samples init the player in too many different ways to use a standard API
    var interval = setInterval(function () {
      if (menu.loading || amp == null || amp.playState != "ready")
        return;

      clearInterval(interval);

      if (!menu.isSelectable())
        return menu.init(0, false);

      setTimeout(menu.init.bind(menu), 10);
    }, 10);
  });
});
