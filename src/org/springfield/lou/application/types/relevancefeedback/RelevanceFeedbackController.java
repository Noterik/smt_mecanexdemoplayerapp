/* 
* RelevanceFeedbackController.java
* 
* Copyright (c) 2016 Noterik B.V.
* 
* This file is part of smt_mecanexdemoplayerapp, related to the Noterik Springfield project.
*
* smt_mecanexdemoplayerapp is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* smt_mecanexdemoplayerapp is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with smt_mecanexdemoplayerapp.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.springfield.lou.application.types.relevancefeedback;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.types.MecanexdemoplayerApplication;
import org.springfield.lou.application.types.inputform.InputFieldController;
import org.springfield.lou.application.types.inputform.SubmitButtonController;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.events.MecanexEvent;
import org.springfield.lou.screen.Screen;

/**
 * RelevanceFeedbackController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.lou.application.types.relevancefeedback
 * 
 */
public class RelevanceFeedbackController extends Html5Controller {
private static final String AUTHORIZATION_FILE = "/springfield/keys/mecanex-sptool.auth";
	
	private String template;
	private String authorizationKey;
	private JSONObject response;
	private MecanexdemoplayerApplication app;
	
	public RelevanceFeedbackController() {
		response = new JSONObject();
	}
	
	public void attach(String sel) {
		selector = sel;
		
		response.put("language",screen.getLanguageCode());
		response.put("id",screen.getId());
		
		if (screen!=null) {
			FsNode node = getControllerNode(selector);
			if (node!=null) {
				template = node.getProperty("template");
				
				screen.get(selector).loadScript(this);
				screen.get(selector).template(template);
			}
			model.onPathUpdate("/app/videofeedback/","onRelevanceFeedback",this);
			app = (MecanexdemoplayerApplication)screen.getApplication();
		}
		
		loadHtml();
		screen.get("#like").attach(new InputFieldController());
		screen.get("#likeButton").attach(new SubmitButtonController());
		screen.bind("#likeButton", "client", "rateButtonClicked", this);
		screen.get("#dislike").attach(new InputFieldController());
		screen.get("#dislikeButton").attach(new SubmitButtonController());
		screen.bind("#dislikeButton", "client", "rateButtonClicked", this);
		screen.get("#indifferent").attach(new InputFieldController());
		screen.get("#indifferentButton").attach(new SubmitButtonController());
		screen.bind("#indifferentButton", "client", "rateButtonClicked", this);
		
		authorizationKey = getAuthorizationKey();
		
		//Observe for changes
		model.observeNode(this,"/domain/mecanex/app/demoplayer/*");
	}
	
	//TODO: improve handling of what value is clicked
	public void rateButtonClicked(Screen s, JSONObject data) {
		 // turn menu on right away
		
		String like = (String) data.get("like");
		String dislike = (String) data.get("dislike");
		String indifferent = (String) data.get("indifferent");
				
		
		String value = "";
		
		if (like != null) { value = "1"; }
		if (dislike != null) { value = "-1"; }
		if (indifferent != null) { value = "0"; }
		
		//signal player to pause first
		model.setProperty("/app/relevancefeedback/ratebutton/clicked",value);
	}

	private void loadHtml() {		
		screen.get(selector).update(response);
	}
	
	public void treeChanged(String url) {
		url = url.substring(0, url.indexOf(","));
		String updatedDevice = url.substring(url.lastIndexOf("/")+1);
		
		System.out.println("Tree changed for device "+updatedDevice);
		
		if ((model.getProperty("/screen/deviceId")).equals(updatedDevice)) {
			FsNode node = model.getNode(url);
			System.out.println(node.asXML());
			//check if the video has ended to show buttons
		}
	}
	
	private String getAuthorizationKey() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(AUTHORIZATION_FILE));
		    String key = br.readLine();
		    br.close();
		    
		    return key;
		} catch (Exception e) {
			System.out.println("Error reading "+AUTHORIZATION_FILE);
		}
		return "";
	}
	
	public void onRelevanceFeedback(String path, FsNode node) {
		String value = node.getProperty("clicked");
		
		String action = "relevance_feedback";
		
		MecanexEvent event = new MecanexEvent(model.getProperty("/screen/username"),model.getProperty("/screen/videoId"),model.getProperty("/screen/deviceId"), action, value);
		
		ClientResource client = new ClientResource("http://sptool.netmode.ntua.gr/api/v1/videosignals");
		
		Series<Header> headers = (Series<Header>) client.getRequestAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
		if (headers == null) {
			headers = new Series<Header>(Header.class);
			client.getRequestAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headers);
		}
		
		headers.add("X-Authorization", authorizationKey);
		
		FormDataSet form = new FormDataSet();
		form.getEntries().add(new FormData("username", event.username));
		form.getEntries().add(new FormData("video_id", event.videoId));
		form.getEntries().add(new FormData("device_id", event.deviceId));
		form.getEntries().add(new FormData("action", Integer.toString(event.action)));
		form.getEntries().add(new FormData(event.actionName, event.actionValue));
		
		try {
			System.out.println(form.getText());

		} catch (IOException e) {
			System.out.println(e.toString());
		}

		Representation responseObject = client.post(form); 
		
		System.out.println(responseObject.toString());
		try {
			System.out.println(responseObject.getText());
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		JSONObject data = new JSONObject();	
		data.put("command","redirect");
		data.put("url", "http://sptool.netmode.ntua.gr/video/recommendation");
		screen.get("#relevancefeedback").update(data);
	}
}
