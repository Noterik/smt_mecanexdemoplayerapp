var VideoPlayerController = function(options) {}; // needed for detection
var currentTime = 0.0;

VideoPlayerController.update = function(vars, data) {
	console.log(data);
	var targetId = '#'+data['targetid']; 
	
	// render the new html using mustache and the data from the server and show it
	var rendered = Mustache.render(vars["template"],data);
	$(targetId).html(rendered);
	
	$(targetId+" > video").on('play', function() {	
		var obj = {};
		obj['action'] = "video_play";
		obj['value'] = currentTime;
		eddie.sendEvent(data['targetid'],"playerEvent",obj);
	});
	
	$(targetId+" > video").on('pause', function() {
		var obj = {};
		obj['action'] = "video_stop";
		obj['value'] = currentTime;
		eddie.sendEvent(data['targetid'],"playerEvent",obj);
	});
	
	$(targetId+" > video").on('timeupdate', function() {
		currentTime = this.currentTime;
	});
	
	$(targetId+" > video").on('ended', function() {
		var obj = {};
		obj['action'] = "video_ended";
		obj['value'] = currentTime;
		eddie.sendEvent(data['targetid'],"playerEvent",obj);
	});
};