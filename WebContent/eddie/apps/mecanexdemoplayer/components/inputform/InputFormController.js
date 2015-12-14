var InputFormController = function(options) {}; // needed for detection

InputFormController.update = function(vars, data) {
	
	var targetId = '#'+data['targetid']; 
	
	// render the new html using mustache and the data from the server and show it
	var rendered = Mustache.render(vars["template"],data);
	$(targetId).html(rendered);
};