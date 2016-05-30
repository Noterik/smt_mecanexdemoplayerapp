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

import org.json.simple.JSONObject;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
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

	private String template;
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
		}
	}
}
