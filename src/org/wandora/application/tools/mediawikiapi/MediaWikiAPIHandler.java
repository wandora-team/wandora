/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wandora.application.tools.mediawikiapi;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import org.wandora.application.tools.AbstractWandoraTool;


/**
 *
 * @author Eero
 */


abstract class MediaWikiAPIHandler extends AbstractWandoraTool{

    private static boolean loggedIn = false;
    
    private static String token = null;
    private static String editToken = null;
    
    private static final String VERSION = "1.0";
    private static final String USER_AGENT = "MediaWikiAPIHandler " + VERSION;
    
    private static final String API_ENDPOINT = "/api.php";
    
    private JSONObject postJSON(String u, Map<String,Object> f) throws Exception{
        
        HttpResponse<JsonNode> resp = Unirest.post(u).fields(f).asJson();
        
        JSONObject respJSON = resp.getBody().getObject();
        
        return respJSON;
    }
    
    protected boolean getLoginStatus(){
        return loggedIn;
    }
    
    protected boolean login(MediaWikiAPIConfig config) throws Exception{
        
        String url = config.getURL();
        String uname = config.getUName();
        String password = config.getPassword();
        String tokenCandidate = token;
        
        StringBuilder requestBuilder = new StringBuilder()
            .append(url)
            .append(API_ENDPOINT);
        Map<String,Object> requestFields = new HashMap<String, Object>();
        requestFields.put("action", "login");
        requestFields.put("format", "json");
        requestFields.put("lgname", uname);
        requestFields.put("lgpassword",password);
        
        if(tokenCandidate != null) requestBuilder
                .append("&lgtoken=")
                .append(tokenCandidate);
        
        String requestURL = requestBuilder.toString();
        
        JSONObject respJSON = postJSON(requestURL,requestFields);
        
        JSONObject loginJSON = respJSON.getJSONObject("login");
        
        String result = loginJSON.getString("result");
        if(result.equals("NeedToken")){
            
            tokenCandidate = loginJSON.getString("token");
            
            requestFields.put("lgtoken",tokenCandidate);
            
            requestURL = requestBuilder.toString();
            respJSON = postJSON(requestURL,requestFields);
            
            loginJSON = respJSON.getJSONObject("login");
            
            result = loginJSON.getString("result");
        }
        
        if(result.equals("Success")){
            
            token = tokenCandidate;
            loggedIn = true;
            return true;
        }
        
        return false;
    }
 
    protected boolean getEditToken(MediaWikiAPIConfig config) throws Exception{
        
        if(editToken != null) return true;
        
        String url = config.getURL();
        
        StringBuilder requestBuilder = new StringBuilder()
            .append(url)
            .append(API_ENDPOINT);
        
        String requestURL = requestBuilder.toString();
        
        Map<String,Object> requestFields = new HashMap<String, Object>();
        requestFields.put("action", "tokens");
        requestFields.put("format", "json");
        
        JSONObject respJSON = postJSON(requestURL,requestFields);
        
        JSONObject tokens = respJSON.getJSONObject("tokens");
        
        String editTokenCandidate = tokens.getString("edittoken");
        
        if(editTokenCandidate.length() > 0){
            editToken = editTokenCandidate;
            return true;
        }
       
        return false;
    }
    
    protected boolean postContent(MediaWikiAPIConfig config, String title, 
            String content) throws Exception{
    
        String url = config.getURL();
        
        StringBuilder requestBuilder = new StringBuilder()
            .append(url)
            .append(API_ENDPOINT);
        
        String requestURL = requestBuilder.toString();
        
        Map<String,Object> requestFields = new HashMap<String, Object>();
        requestFields.put("action", "edit");
        requestFields.put("format", "json");
        requestFields.put("title",title);
        requestFields.put("text",content);
        requestFields.put("token",editToken);

        JSONObject respJSON = postJSON(requestURL,requestFields);
        
        if(respJSON.has("error")){
            JSONObject error = respJSON.getJSONObject("error");
            StringBuilder errorBuilder = new StringBuilder()
                    .append("API returned error code ")
                    .append(error.getString("code"))
                    .append(": ")
                    .append(error.getString("info"));
            
            log(errorBuilder.toString());
            return false;
        } else if(respJSON.has("edit")){
            JSONObject edit = respJSON.getJSONObject("edit");
            if(edit.has("result") && edit.getString("result").equals("Success")){
                log("Succesfully added article: " + title);
                return true;
            } else {
                log("Unknown response from API");
            }
        } else {
            log("Unknown response from API");
        }
        
        return false;
    }
    
}
