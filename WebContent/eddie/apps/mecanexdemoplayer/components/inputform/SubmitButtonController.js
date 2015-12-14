var SubmitButtonController = function() {}; // needed for detection

SubmitButtonController.update = function(vars, data) {
	var targetId = '#'+data['targetid']; 

	// render the new html using mustache and the data from the server and show it
	var rendered = Mustache.render(vars["template"],data);
    $(targetId).html(rendered);
    
    $(targetId).on('click', function() {    	
    	var fields = vars["controller/fieldsToSubmit"].split(",");
    	var obj = {};
    	
    	fields.forEach(function (item, index, array) {
    		obj[item] = $("#"+item +" input").val();
    	});
    	eddie.sendEvent(data['targetid'],"submitButtonClicked",obj);
    });
};