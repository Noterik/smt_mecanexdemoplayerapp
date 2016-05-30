var RelevanceFeedbackController = function(options) {}; // needed for detection

RelevanceFeedbackController.update = function(vars, data) {
	
	var targetId = '#'+data['targetid']; 
	
	var command = data['command'];
	if (command=="redirect") {
		window.location.href = data['url'];
	} else {
		// render the new html using mustache and the data from the server and show it
		var rendered = Mustache.render(vars["template"],data);
		$(targetId).html(rendered);
	}
};