/* 
* MecanexEvent.java
* 
* Copyright (c) 2015 Noterik B.V.
* 
* This file is part of lou2, related to the Noterik Springfield project.
*
* lou2 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* lou2 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with lou2.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.springfield.lou.events;

import org.json.simple.JSONObject;

/**
 * MecanexEvent.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2015
 * @package org.springfield.lou.events
 * 
 */
public class MecanexEvent {
	public String username = "";
	public String videoId = "";
	public String deviceId = "";
	public int action = -1;
	public String actionName = "";
	public String actionValue = "";
	
	public MecanexEvent() {
		//constructor
	}
	
	public MecanexEvent(String username, String videoId, String deviceId, String action, String actionValue) {
		this.username = username;
		this.videoId = videoId;
		this.deviceId = deviceId;
		if (action.equals("video_play")) {
			this.action = 1;
			this.actionName = "time";
		} else if (action.equals("video_stop")) {
			this.action = 2;
			this.actionName = "time";
		} else if (action.equals("relevance_feedback")) {
			this.action = 6;
			this.actionName = "value";
		}
		this.actionValue = actionValue;
	}
	
	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("username", username);
		data.put("videoId", videoId);
		data.put("deviceId", deviceId);
		data.put("action", action);
		data.put(actionName, actionValue);
		
		return data;
	}
}
