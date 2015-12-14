/* 
* InputFieldController.java
* 
* Copyright (c) 2015 Noterik B.V.
* 
* This file is part of smt_mecanexdashboardapp, related to the Noterik Springfield project.
*
* smt_mecanexdashboardapp is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* smt_mecanexdashboardapp is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with smt_mecanexdashboardapp.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.springfield.lou.application.types.inputform;

import org.json.simple.JSONObject;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;

/**
 * InputFieldController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2015
 * @package org.springfield.lou.application.types.workflow.element.form
 * 
 */
public class InputFieldController extends Html5Controller {	
	public String nodepath;
	public String template;
	public String value;
	public int size = -1;
	public int maxlength = -1;
	public boolean disabled = false;
	
	public InputFieldController() {
		//constructor
	}
	
	public InputFieldController(String value) {
		this.value = value;
	}
	
	public void attach(String s) {
		selector = s;
		if (screen!=null) {
			FsNode node = getControllerNode(selector);
			if (node!=null) {
				//mandatory fields
				template = node.getProperty("template");				
								
				//optional fields
				nodepath = node.getProperty("nodepath");
				value = value == null ? node.getProperty("value") : value;
				size = node.getProperty("size") == null ? -1 : Integer.parseInt(node.getProperty("size"));
				maxlength = node.getProperty("maxlength") == null ? -1 : Integer.parseInt(node.getProperty("maxlength"));
				disabled = node.getProperty("disabled") == null ? false : Boolean.parseBoolean(node.getProperty("disabled"));
				
				if (nodepath != null) {
					model.observeTree(this,nodepath);
				}
				
				screen.get(selector).loadScript(this);
				screen.get(selector).template(template);
				updateInputField();
				
				screen.get(selector).syncvars("controller/validRegex");
				screen.get(selector).syncvars("controller/invalidRegex");
			}
		}
	}
		
	public void treeChanged(String url) {
		updateInputField();
	}
		
	public void languageChanged() {
		updateInputField();	
	}
		
	public void updateInputField() {
		JSONObject data;
		
		if (nodepath != null) {
			FsNode node = Fs.getNode(nodepath);
			data = node.toJSONObject(screen.getLanguageCode(), nodepath.substring(nodepath.lastIndexOf("/")+1));
			data.put("nodepath",nodepath);
		} else {
			data = new JSONObject();
			if (value != null) { data.put("value", value); }
		}
		
		//set optional fields
		if (size != -1) { data.put("size", size); }
		if (maxlength != -1) { data.put("maxlength", maxlength); }
		if (disabled) { data.put("disabled", disabled); }
		
		data.put("targetid",selector.substring(1));
		screen.get(selector).update(data);
	}
}
