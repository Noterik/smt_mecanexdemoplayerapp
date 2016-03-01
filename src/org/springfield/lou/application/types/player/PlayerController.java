/* 
* PlayerController.java
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
package org.springfield.lou.application.types.player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;
import org.springfield.fs.FSListManager;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.controllers.Html5Controller;
import org.springfield.lou.events.MecanexEvent;
import org.springfield.lou.screen.Screen;

/**
 * PlayerController.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2015
 * @package org.springfield.lou.application.types.player
 * 
 */
public class PlayerController extends Html5Controller {
	
	private static final String AUTHORIZATION_FILE = "/springfield/keys/mecanex-sptool.auth";
	
	private String template;
	private String authorizationKey;
	private JSONObject response;
	
	public PlayerController() {
		response = new JSONObject();
	}
	
	public void attach(String sel) {
		selector = sel;
		
		screen.bind("#player", "client", "playerEvent", this);
		
		response.put("language",screen.getLanguageCode());
		response.put("id",screen.getId());

		if (screen!=null) {	
			loadScreen();
		}

		String deviceId = (String) screen.getProperty("deviceId");
		
		authorizationKey = getAuthorizationKey();
		//Observe for changes
		model.observeNode(this,"/domain/mecanex/app/demoplayer/*");
	}
	
	private void loadScreen() {
		String videoSrc = "";
		String videoPoster = "";
		
		FsNode node = getControllerNode(selector);
		if (node!=null) {
			template = node.getProperty("template");
				
			screen.get(selector).loadScript(this);
			screen.get(selector).template(template);
		}
			
		//getting video source
		String videoId = (String) screen.getProperty("videoId");
		System.out.println("In video player controller got video id = "+videoId);
		List<FsNode> raws = FSListManager.getNodes("/domain/mecanex/user/luce/video/"+videoId+"/rawvideo", 1, 0, 1);
		if (raws.size() > 0) {
			FsNode raw = raws.get(0);
				
			String mount = raw.getProperty("mount");
			String extension = raw.getProperty("extension");
				
			String videoFile = mount;
				
			if (mount.indexOf(",") > -1) {
				mount = mount.substring(0, mount.indexOf(","));
			}
				
			//getting ticket for Noterik videos
			if (mount.indexOf("http://") == -1) {
				String path = raw.getPath();
				path = path.replace("mecanex", "euscreenxl");
				path = path.replace("luce", "eu_luce");
					
				videoFile = "/"+mount+path+ "/raw."+ extension;
				String ticket = getTicket(videoFile);
				videoSrc = "http://" + mount + ".noterik.com/progressive/"+videoFile+"?ticket="+ticket;
			} else if(mount.indexOf(".noterik.com/progressive") > -1) {
				if (mount.indexOf("/luce/") > -1) {
					String tmp = mount.substring(0, mount.indexOf("/luce/"))+"/eu_"+mount.substring(mount.indexOf("/luce/")+1);
					mount = tmp;
				}
					
				videoFile = mount.substring(mount.indexOf("progressive")+11);
				String ticket = getTicket(videoFile);
				videoSrc = mount+"?ticket="+ticket;
			}
		} else {
			System.out.println("no results for /domain/mecanex/user/luce/video/"+videoId+"/rawvideo");
		}
			
		FsNode videoNode = Fs.getNode("/domain/mecanex/user/luce/video/"+videoId);
		if (videoNode != null) {
			videoPoster = videoNode.getProperty("screenshot");
				
			if (videoPoster.indexOf("/luce/") > -1) {
				String tmp = videoPoster.substring(0, videoPoster.indexOf("/luce/"))+"/eu_"+videoPoster.substring(videoPoster.indexOf("/luce")+1);
				videoPoster = tmp;
			}
		}
		response.put("videoSrc", videoSrc);
		response.put("videoPoster", videoPoster);
		
		loadHtml();		
	}
	
	private String getTicket(String video) {		
		Random randomGenerator = new Random();
		Integer random= randomGenerator.nextInt(100000000);
		String ticket = Integer.toString(random);
		
		try{							
			sendTicket(video,"0.0.0.0",ticket);}
		catch (Exception e){}
		
		return ticket;
	}
	
	private void sendTicket(String videoFile, String ipAddress, String ticket) throws IOException {
		URL serverUrl = new URL("http://82.94.187.227:8001/acl/ticket");
		HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();
		
		Long Sytime = System.currentTimeMillis();
		Sytime = Sytime / 1000;
		String expiry = Long.toString(Sytime+(15*60));
		
		// Indicate that we want to write to the HTTP request body
		urlConnection.setDoOutput(true);
		urlConnection.setRequestMethod("POST");
		videoFile=videoFile.substring(1);

		
		// Writing the post data to the HTTP request body
		BufferedWriter httpRequestBodyWriter = 
		new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
		String content = "<fsxml><properties><ticket>"+ticket+"</ticket>"
			+ "<uri>/"+videoFile+"</uri><ip>"+ipAddress+"</ip> "
			+ "<role>user</role>"
			+ "<expiry>"+expiry+"</expiry><maxRequests>1</maxRequests></properties></fsxml>";

		httpRequestBodyWriter.write(content);
		httpRequestBodyWriter.close();
		
		// Reading from the HTTP response body
		Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
		while(httpResponseScanner.hasNextLine()) {
			System.out.println(httpResponseScanner.nextLine());
		}
		httpResponseScanner.close();		
	}
	
	public void playerEvent(Screen s, JSONObject data) {
		String action = data.get("action").toString();
		Object v = data.get("value");
		String value = "";
		
		System.out.println(data.toString());

		if (v instanceof Long) {
			value = Long.toString((Long) v);
		} else if (v instanceof Double) {
			value = Double.toString((Double) v);
		} else if (v instanceof String) {
			value = (String) v;
		} else {
			System.out.println(v);
			System.out.println(v.getClass());
		}
		
		System.out.println("Value is "+value);
		
		if (action.equals("video_ended")) {
			//Enable relevance feedback button once video has ended
			String deviceId = (String) screen.getProperty("deviceId");
			
			System.out.println("Video has ended for device "+deviceId+", change value to propegate for additional actions");

			model.setProperty("/domain/mecanex/app/demoplayer/device/"+deviceId, "video_ended", "true");
			return;
			
		} else if (action.equals("video_play")) {
			//Disable relevance feedback button once video is playing
			String deviceId = (String) screen.getProperty("deviceId");
			model.setProperty("/domain/mecanex/app/demoplayer/device/"+deviceId, "video_ended", "false");
		}
 		
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
		if (action.equals("video_stop")) {
			System.out.println("Video stop action");
			Object d = data.get("duration");
			if (d instanceof String) {
				form.getEntries().add(new FormData("duration", (String) d));
				System.out.println("Adding duration String");
			} else if (d instanceof Long) {
				form.getEntries().add(new FormData("duration", Long.toString((Long) d)));
				System.out.println("Adding duration long");
			} else if (d instanceof Double) {
				form.getEntries().add(new FormData("duration", Double.toString((Double) d)));
				System.out.println("Adding duration double");
			}
		}
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
	
	public void treeChanged(String url) {
		url = url.substring(0, url.indexOf(","));
		String updatedDevice = url.substring(url.lastIndexOf("/")+1);
		
		//Something changed on our device
		if (((String) screen.getProperty("deviceId")).equals(updatedDevice)) {
			//check if the video changed
			FsNode node = model.getNode(url);
			String videoId = node.getProperty("videoId");
			
			if (videoId != null) {
				if (!screen.getProperty("videoId").equals(videoId)) {
					screen.setProperty("videoId", videoId);
					loadScreen();
				}
			}
		}
	}
	
	private void loadHtml() {
		screen.get(selector).update(response);
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
