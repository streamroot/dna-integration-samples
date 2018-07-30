var AEP = {
	counter: 1,
	folder: null,
	playerReady: false,
	getJSON: function(id) {
		if (id > $('#mycarousel li').size()) 
			id = 1;
		
		AEP.counter = id;
		
		$.ajax({
			url: 'data/' + AEP.folder + '/' + id + '.js',
			dataType: 'json',
			contentType: "application/json; charset=utf-8", 
			beforeSend: function() {}, 
			success: function(data) {
				if ($("#force-coast").attr("checked")) {
					data.data.coast = $("#coast :selected").val();
				}
				
				var jsonStr = JSON.stringify( data, null, ' ' );
				$("#debug p").html('<h3>JSON result (button #' + id + '):</h3>' + jsonStr);
				
				AEP.eventProgress(data);
				return data;
			},
			error: function(xhr) {
				alert("error: " + xhr.responseText);
			}
		});
	},
	getJSONFromURL: function(path) {
		$.ajax({
			url: path,
			dataType: 'json',
			contentType: "application/json; charset=utf-8", 
			beforeSend: function() {}, 
			success: function(data) {
				if ($("#force-coast").attr("checked")) {
					data.data.coast = $("#coast :selected").val();
				}
				
				var jsonStr = JSON.stringify( data, null, ' ' );
				AEP.eventProgress(data);
				return data;
			},
			error: function(xhr) {
				alert("error: " + xhr.responseText);
			}
		});
	},
	setCarouselData: function(name) {
		$.ajax({
			url: 'data/carousels/' + name + '.html',
			dataType: 'html',
			contentType: "txt/html; charset=utf-8", 
			beforeSend: function() {}, 
			success: function(data) {
				$("#mycarousel").html(data);
				resetCarousel();
				$('#mycarousel li img').click(function() {
					var id = $(this).attr('id');
					AEP.getJSON(id);
				});
			},
			error: function(xhr) {
				alert("error: " + xhr.responseText);
			}
		});
	},
	findSWF: function(movieName) {
		return ($.browser.msie) ? window[movieName] : document[movieName];
	},
	playNextVideo: function() {
		this.getJSON(++this.counter);
	},
	eventProgress: function(data) {
		var player = this.findSWF("akamaiStandardPlayer");
		player.playFile(data, $("#autoplay").attr("checked"));
	},
	callPlayer: function(str) {
		
		var player = this.findSWF("akamaiStandardPlayer");
		var val = null;
		var param = 30;
		
		var clear = false;
		switch (str) {
			case 'stop':
				player.stopPlayer();
				val = 'stopped';
				break;
				
			case 'pause':
				player.pause();
				val = 'paused';
				break;
				
			case 'unpause':
				player.unpause();
				val = 'unpaused';
				break;
				
			case 'mute':
				player.mute();
				val = 'muted';
				break;
				
			case 'unmute':
				player.unmute();
				val = 'unmuted';
				break;
				
			case 'playerState':
				val = player.playerState();
				break;
				
			case 'isPlayerPlaying':
				val = player.isPlayerPlaying();
				break;
				
			case 'isStopped':
				val = player.isStopped();
				break;
				
			case 'buffering':
				val = player.buffering();
				break;
				
			case 'currentTime':
				val = player.getCurrentTime();
				break;
				
			case 'duration':
				val = player.getDuration();
				break;
				
			case 'playbackKbps':
				val = player.playbackKbps();
				break;
				
			case 'jumpToTime':
				param = $('#input-jumpToTime').val();
				player.jumpToTime(param);
				val = param;
				break;
                
            case 'setShareEnabledState':
				param = $('#input-setShareEnabledState').val();
				player.setShareEnabledState(param);
				val = param;
				break;
				
			case 'goLive':
				player.goLive();
				break;
				
			case 'playFile':
				param = $('#input-playFile').val();
				if( param != "" )
				{
					player.playFile(param, $("#autoplay").attr("checked"));
				}
				val = param;
				break;
			case 'loadURL':
				param = $('#input-loadURL').val()
				if( param != "" )
				{
					player.loadURL(param);
				}
				val = param;
				break;
		}
		
		$('.player-input').each(function(){$(this).val('');});
		if (val != null) 
			$('#output-' + str).val(val);
	},
    handler: null,
	jsCallbackHandler: function(objId, eventName, body) 
	{
		//console.log("Notification received: " + eventName + (eventName == "mediaPlayerStateChange" ? ("State: " + body.state) : ""));
        
        if (this.handler) 
        {
            this.handler(eventName, body);
        }
        
		if ( eventName == "jsApiReady") 
		{
			playerReady = true;
			
			// Event listeners or other routines relevant to
			// API readiness should be placed here.
			// Also note that properties of the underlying
			// media player are exposed. By default, these
			// will be the properties of the OSMF MediaPlayer 
			var player = this.findSWF("akamaiStandardPlayer");
			setInterval(function() {inspectPlayhead()}, 1000);
        }
    }
};

function inspectPlayhead()
{
	var d = new Date();
	var currentSystemTime = d.getTime()/1000;
	currentTime = AEP.findSWF("akamaiStandardPlayer").getCurrentTimeUTC();  
	var diff = Math.abs (Math.round(currentSystemTime)  - Math.round(currentTime));               
	document.getElementById('current-Time').innerHTML = ("Current stream UTC time: " + Math.round(currentTime) );
	document.getElementById('sys-Time').innerHTML = ("System time in UTC     : " + Math.round(currentSystemTime) );
	document.getElementById('diff-Time').innerHTML = ("Difference in seconds  : " + diff);	
}


function retrieveToken(guid, url)
{
	var player = AEP.findSWF("akamaiStandardPlayer");
	var val = "&primaryToken=st=1326108173~exp=1412508173~acl=/*~hmac=65cccba0ebd08e4780899156c8ac28c819043ad493ba8abba970d7c4d4c1de5d";
	player.tokenRetrievalSuccess(val);
	
    return true;
}

function playNextVideo() {
	AEP.playNextVideo();
}