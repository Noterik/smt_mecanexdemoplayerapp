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
	private MecanexdemoplayerApplication app;
	
	public RelevanceFeedbackController() {
	}
	
	public void attach(String sel) {
		selector = sel;
		
		if (screen!=null) {
			JSONObject data = new JSONObject();
			data.put("language",screen.getLanguageCode());
			data.put("id",screen.getId());
	 		screen.get(selector).parsehtml(data);
			
			model.onPathUpdate("/app/videofeedback/","onRelevanceFeedback",this);
			app = (MecanexdemoplayerApplication)screen.getApplication();
		}
		
		//loadHtml();
		screen.get("#likeButton").on("mouseup","likeClicked",this);
		screen.get("#dislikeButton").on("mouseup","dislikeClicked",this);
		screen.get("#indifferentButton").on("mouseup","indifferentClicked",this);
				
		authorizationKey = getAuthorizationKey();
		
	}
	
	
	public void likeClicked(Screen s, JSONObject data) {
		System.out.println("like clicked");
		model.setProperty("/app/relevancefeedback/ratebutton/clicked","1");
	}
	
	public void dislikeClicked(Screen s, JSONObject data) {
		System.out.println("dislike clicked");
		model.setProperty("/app/relevancefeedback/ratebutton/clicked","-1");
	}
	
	public void indifferentClicked(Screen s, JSONObject data) {
		System.out.println("indifferent clicked");
		model.setProperty("/app/relevancefeedback/ratebutton/clicked","0");
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
		
		screen.get("#screen").location("http://sptool.netmode.ntua.gr/video/recommendation");
	}
}
