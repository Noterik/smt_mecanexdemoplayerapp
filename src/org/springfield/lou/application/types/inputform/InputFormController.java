/* 
* InputFormController.java
* 
* Copyright (c) 2015 Noterik B.V.
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
package org.springfield.lou.application.types.inputform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.json.simple.JSONObject;
import org.restlet.data.Form;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.events.MecanexEvent;
import org.springfield.lou.screen.Screen;

/**
 * InputFormController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2015
 * @package org.springfield.lou.application.types.inputform
 * 
 */
public class InputFormController extends Html5Controller {
	
	private static final String AUTHORIZATION_FILE = "/springfield/keys/mecanex-sptool.auth";
	
	private String template;
	private String authorizationKey;
	private JSONObject response;
	
	public InputFormController() {
		response = new JSONObject();
	}
	
	public void attach(String sel) {
		selector = sel;
		
		response.put("language",screen.getLanguageCode());
		response.put("id",screen.getId());
		
		String username = "";
		String videoId = "";
		
		if (screen!=null) {
			username = (String) screen.getProperty("username");
			videoId = (String) screen.getProperty("videoId");
					
			FsNode node = getControllerNode(selector);
			if (node!=null) {
				template = node.getProperty("template");

				response.put("usernameText", node.getProperty("usernameText"));
				response.put("videoidText", node.getProperty("videoidText"));
				
				screen.get(selector).loadScript(this);
				screen.get(selector).template(template);
			}
		}
		
		loadHtml();
		screen.get("#username").attach(new InputFieldController(username));
		screen.get("#videoid").attach(new InputFieldController(videoId));
		screen.get("#submitButton").attach(new SubmitButtonController());
		screen.bind("#submitButton", "client", "submitButtonClicked", this);
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
	
	public void submitButtonClicked(Screen s, JSONObject data) {
		String username = (String) data.get("username");
		String videoId = (String) data.get("videoid");

		String currentVideo = (String) s.getProperty("videoId");
		
		//update in screen properties 
		s.setProperty("username", username);
		//update of videoid screen property done in PlayerController
		
		//reload video if new video is given
		if (!currentVideo.equals(videoId)) {
			String deviceId = (String) screen.getProperty("deviceId");
			model.setProperty("/domain/mecanex/app/demoplayer/device/"+deviceId, "videoId", videoId);
		}
	}
	
	//TODO: improve handling of what value is clicked
	public void rateButtonClicked(Screen s, JSONObject data) {
		String like = (String) data.get("like");
		String dislike = (String) data.get("dislike");
		String indifferent = (String) data.get("indifferent");
				
		String action = "relevance_feedback";
		String value = "";
		
		if (like != null) { value = "1"; }
		if (dislike != null) { value = "-1"; }
		if (indifferent != null) { value = "0"; }
		
		MecanexEvent event = new MecanexEvent((String) screen.getProperty("username"), (String) screen.getProperty("videoId"), (String) screen.getProperty("deviceId"), action, value);
		
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
		
	}
	
	private void loadHtml() {		
		screen.get(selector).update(response);
	}
	
	public void treeChanged(String url) {
		url = url.substring(0, url.indexOf(","));
		String updatedDevice = url.substring(url.lastIndexOf("/")+1);
		
		System.out.println("Tree changed for device "+updatedDevice);
		
		if (((String) screen.getProperty("deviceId")).equals(updatedDevice)) {
			FsNode node = model.getNode(url);
			System.out.println(node.asXML());
			//check if the video has ended to show buttons
			String videoEnded = node.getProperty("video_ended");
			if (videoEnded.equals("true")) {
				screen.get("#likeButton").show();
				screen.get("#dislikeButton").show();
				screen.get("#indifferentButton").show();
			} else {
				screen.get("#likeButton").hide();
				screen.get("#dislikeButton").hide();
				screen.get("#indifferentButton").hide();
			}
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
}
