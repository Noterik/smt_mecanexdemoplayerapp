/* 
* MecanexdemoplayerApplication.java
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
package org.springfield.lou.application.types;

import java.util.UUID;

import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.application.types.inputform.InputFormController;
import org.springfield.lou.application.types.player.PlayerController;
import org.springfield.lou.application.types.relevancefeedback.RelevanceFeedbackController;
import org.springfield.lou.controllers.ScreenController;
import org.springfield.lou.model.Model;
import org.springfield.lou.screen.Screen;

/**
 * MecanexdemoplayerApplication.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2015
 * @package org.springfield.lou.application.types
 * 
 */
public class MecanexdemoplayerApplication extends Html5Application {
	
	public MecanexdemoplayerApplication(String id) {
		super(id);
		this.setSessionRecovery(true);
		this.addToRecoveryList("username");
		this.addToRecoveryList("videoId");
		this.addToRecoveryList("deviceId");
	}
	
	 public void onNewScreen(Screen s) {
		 
		 loadStyleSheet(s,"player");
		 
		 String deviceId = s.getModel().getProperty("/screen/deviceId");
		 if (deviceId == null) { 
			 deviceId = UUID.randomUUID().toString();
			 s.getModel().setProperty("/screen/deviceId", deviceId);
			 
			 FsNode device = new FsNode("device", deviceId);
			 Fs.insertNode(device, "/domain/mecanex/app/demoplayer/device/"+deviceId);
		 }
		 //Allow to override through parameters in url
		if (s.getParameter("username") != null) {
			System.out.println("Username is set through parameter to "+s.getParameter("username"));
			s.getModel().setProperty("/screen/username", s.getParameter("username"));
		}
		if (s.getParameter("videoid") != null) {
			System.out.println("Video id is set through parameter to "+s.getParameter("videoid"));
			s.getModel().setProperty("/screen/videoId", s.getParameter("videoid"));
		}
		
		 
		 s.get("#screen").setViewProperty("template", "screen.mst");
		 s.get("#screen").attach(new ScreenController());
		 s.get("#inputform").attach(new InputFormController());
		 s.get("#player").attach(new PlayerController());
		 s.get("#relevancefeedback").attach(new RelevanceFeedbackController());
	 }
}