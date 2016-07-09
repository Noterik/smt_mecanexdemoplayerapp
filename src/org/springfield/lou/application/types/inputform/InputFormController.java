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

	
	public InputFormController() {
	}
	
	public void attach(String sel) {
		selector = sel;
		

		
		String username = "";
		String videoId = "";
		
		if (screen!=null) {
			username = model.getProperty("/screen/username");
			videoId = model.getProperty("/screen/videoId");
					
			FsNode node = getControllerNode(selector);
			if (node!=null) {
				JSONObject data = new JSONObject();
				data.put("language",screen.getLanguageCode());
				data.put("id",screen.getId());
				data.put("videoid", videoId);
				data.put("username", username);
				data.put("usernameText", node.getProperty("usernameText"));
				data.put("videoidText", node.getProperty("videoidText"));
		 		screen.get(selector).parsehtml(data);
			}
		}
		
		screen.get("#submitButton").on("mouseup","username,videoid","submitButtonClicked",this);
		
		//Observe for changes
		model.observeNode(this,"/domain/mecanex/app/demoplayer/*");
	}
	
	public void submitButtonClicked(Screen s, JSONObject data) {
		String username = (String) data.get("username");
		String videoId = (String) data.get("videoid");
		System.out.println("USERNAME="+username);
		System.out.println("VIDEOID="+videoId);

		String currentVideo = model.getProperty("/screen/videoId");
		
		//update in screen properties 
		model.setProperty("/screen/username", username);
		//update of videoid screen property done in PlayerController
		
		//reload video if new video is given
		if (!currentVideo.equals(videoId)) {
			String deviceId = model.getProperty("/screen/deviceId");
			model.setProperty("/domain/mecanex/app/demoplayer/device/"+deviceId+"/videoId", videoId);
		}
	}
	
	
	public void treeChanged(String url) {
		url = url.substring(0, url.indexOf(","));
		String updatedDevice = url.substring(url.lastIndexOf("/")+1);
		
		System.out.println("Tree changed for device "+updatedDevice);
		
		if ((model.getProperty("/screen/deviceId")).equals(updatedDevice)) {
			FsNode node = model.getNode(url);
			System.out.println(node.asXML());
		}
	}
}
