 /*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 */

package org.wandora.application.tools.extractors.mediawikiapi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.WandoraToolLogger;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;


/**
 *
 * @author Eero
 */


public class MediaWikiAPIPageExtractor extends AbstractMediaWikiAPIExtractor{    

	private static final long serialVersionUID = 1L;
	
	private int nExtracted;
    private String baseURL;
    private String queryURL;
    private boolean crawlClasses;
    private String[] qType;
    
    
    private int progress;
    private WandoraToolLogger logger;
    
    
    
    MediaWikiAPIPageExtractor(String baseURL, String[] qType, boolean crawl){
        super();
        nExtracted = 0;
        this.baseURL = baseURL;
        this.qType = qType;
        this.crawlClasses = crawl;
        
    }
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    @Override
    public boolean runInOwnThread(){
        return false;
    }
    
    protected void setQueryUrl(String u){
        this.queryURL = u;
    }
    
    protected String getBaseUrl(){
        return this.baseURL;
    }
    
    protected String getQueryUrl(){
        return this.queryURL;
    }
    
    protected void incrementExtractions(){
        nExtracted++;
    }
    
    private final String[] contentTypes 
        = new String[] { "text/plain", "text/json", "application/json" };
    
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported."); 
    }

    
    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        return extractTopicsFromURL(u, t);
    }

    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {    
        return extractTopicsFromString(str, t);
    }
    
    
    private boolean extractTopicsFromString(String str, TopicMap t){
        
        String[] titles = str.split(",");
        List<String> titleList = Arrays.asList(titles);
        
        for(String title : titleList){
            try {
                parsePage(title,t);
            } catch (Exception e) {
                log(e);
            }
        }
        
        return true;
    }
    
    
    
    private boolean extractTopicsFromURL(URL u, TopicMap t){
        HttpResponse<JsonNode> resp;
        JsonNode body;
        JSONObject contObject;
        boolean shouldContinue;
        
        try {
            resp = Unirest.get(u.toExternalForm())
                .header("accept", "application/json")
                .asJson();
            body = resp.getBody();
            contObject = parse(body.getObject(),t);
            
            String msg =  "Extracted " + nExtracted + " pages in total.\n"
                       +  "Should we continue the extraction?";
            
            
            try {
            if(contObject != null  && !forceStop())
                continueExtraction(contObject,t);
            
            } catch (Exception e) {
              log("Nothing more to extract");
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            return false;
        }
        
        return true;
    }
    
    
    private void continueExtraction(JSONObject contObject, TopicMap t) 
            throws Exception{
        
        String typeName = qType[0], typePrefix = qType[1];
        
        String cont;
        if(contObject.has(typeName)){
             cont = "&" + typePrefix + "from=" 
                  + contObject.getJSONObject(typeName).getString(typePrefix + "from");
            
            
        } else if(contObject.has(typePrefix + "continue")){
            cont = "&" + typePrefix + "continue=" 
                 + contObject.getString(typePrefix + "continue");
        } else {
            throw new Exception("Failed to get the continuation parameter");            
        }
        
        System.out.println(this.queryURL + cont);
        
        URL u = new URL(this.queryURL + cont);
        extractTopicsFromURL(u,t);
        
    }
    
    
    private JSONObject parse(JSONObject body, TopicMap tm)
            throws JSONException, TopicMapException, IOException{
        
        logger = getDefaultLogger();
        progress = 0;

        if(body.has("error")){
            printError(body);
            return null;
        }
        
        if(body.has("warnings")) printWarnings(body);
        
        JSONObject query = body.getJSONObject("query");
        JSONArray pages = null;
        
        String typeName = qType[0], typePrefix = qType[1];
        
        if(query.has(typeName)) 
            pages = query.getJSONArray(typeName);
        
        if(pages == null) throw new JSONException("No suitable data in JSON");
        
        JSONObject page;
        
        logger.setProgressMax(pages.length());
        logger.setProgress(0);
        for (int i = 0; i < pages.length(); i++) {
            if(forceStop()) break;
            page = pages.getJSONObject(i);
            parsePage(page,tm);
        }
        
        if(body.has("query-continue")) 
            return body.getJSONObject("query-continue");
        else if(body.has("continue"))
            return body.getJSONObject("continue");
        return null;
    }
    
    
    private void parsePage(JSONObject page, TopicMap tm) 
            throws JSONException, TopicMapException, IOException{
        
        String title = page.getString("title");
        parsePage(title,tm);
        
    }
    
    
    private void parsePage(String title, TopicMap tm)
            throws JSONException, TopicMapException, IOException{
        
        log("Adding page #" + (nExtracted+1) + ": \"" + title + "\"");
        
        HashMap<String,String> info  = getArticleInfo(title);
        String body = getArticleBody(title);
        List<String> classes = getArticleClasses(title);
        
        if(info == null || body == null){
            log("Failed to get page: " + title);
            return;
        }
        
        String lang = (info.containsKey("pagelanguage")) ? 
                info.get("pagelanguage") : "en";
        
        Topic mediaWikiClass = getMediaWikiClass(tm);
        Topic langTopic = getLangTopic(tm,lang);
        Topic typeTopic = getContentTypeTopic(tm);
        
        Topic pageTopic = getOrCreateTopic(tm, info.get("fullurl") , title);
        pageTopic.setSubjectLocator(new Locator(info.get("fullurl")));
        makeSubclassOf(tm, pageTopic, mediaWikiClass);
        
        if(this.crawlClasses && classes != null){
           for(String articleClass: classes){
                HashMap<String,String> classInfo = getArticleInfo(articleClass);
                String classURL = classInfo.get("fullurl");
                Topic classTopic = getOrCreateTopic(tm, classURL, articleClass);
                makeSubclassOf(tm,classTopic,mediaWikiClass);
                makeSubclassOf(tm, pageTopic, classTopic);
            } 
        }
        
        pageTopic.setData(typeTopic, langTopic, body);
        
        this.incrementExtractions();
        getDefaultLogger().setProgress(progress++);
    }
    
    
    private String getArticleBody(String title) throws IOException{
        StringBuilder queryBuilder = new StringBuilder(this.baseURL)
            .append("/index.php?action=raw&title=")
            .append(URLEncoder.encode(title));

        HttpResponse<InputStream> resp;
        try {
          resp = Unirest.get(queryBuilder.toString())
                  .header("accept", "text/x-wiki")
                  .asBinary();
        } catch (UnirestException ex) {
          throw new IOException(ex);
        }

        InputStream body = resp.getBody();
        String bodyString = IOUtils.toString(body,"UTF-8"); 
        
        return bodyString;
    }
    
    
    private HashMap<String,String> getArticleInfo(String title) 
            throws IOException{
        
        Map<String,Object> fields = new HashMap<String,Object>();
        
        fields.put("action", "query");
        fields.put("prop","info");
        fields.put("format","json");
        fields.put("inprop","url");
        fields.put("titles",title);
        
        HttpResponse<JsonNode> resp;

        try {
            
            HashMap<String,String> info = new HashMap<String, String>();
          try {            
            resp = Unirest.post(this.baseURL + "/api.php")
                    .fields(fields)
                    .asJson();
          } catch (UnirestException ex) {
            throw new IOException(ex);
          }

            JsonNode body = resp.getBody();
            
            JSONObject bodyObject = body.getObject();
            if(!bodyObject.has("query")) return null;
            JSONObject q = bodyObject.getJSONObject("query");
            JSONObject pages = q.getJSONObject("pages");
            Iterator<String> pageKeys = pages.keys();
            while(pageKeys.hasNext()){
                JSONObject page = pages.getJSONObject(pageKeys.next());
                if(!page.getString("title").equals(title)) continue;
                
                Iterator<String> valueKeys = page.keys();
                while(valueKeys.hasNext()){
                    String key = valueKeys.next();
                    info.put(key, page.get(key).toString());
                }
                return info;
                
            }
            
        } catch (JSONException jse) {
            jse.printStackTrace();
            log(jse.getMessage());
        }
                
        return null;
    }
    
    
    private List<String> getArticleClasses(String title) throws IOException{
        
        Map<String,Object> fields = new HashMap<String,Object>();
        
        fields.put("action", "query");
        fields.put("prop","categories");
        fields.put("format","json");
        fields.put("cllimit","100");
        fields.put("titles",title);
        
        HttpResponse<JsonNode> resp;

        try {
            try {            
              resp = Unirest.post(this.baseURL + "/api.php")
                      .fields(fields)
                      .asJson();
            } catch (UnirestException ex) {
              throw new IOException(ex);
            }
            
            JsonNode body = resp.getBody();
            JSONObject bodyObject = body.getObject();
            if(!bodyObject.has("query")) return null;
            JSONObject q = bodyObject.getJSONObject("query");
            JSONObject pages = q.getJSONObject("pages");
            Iterator<String> pageKeys = pages.keys();
            while(pageKeys.hasNext()){
                JSONObject page = pages.getJSONObject(pageKeys.next());
                if(!page.getString("title").equals(title)) continue;
                
                /*
                 * Got the correct key (There should only be a single key anyway)
                 */
                if(!page.has("categories")) break;
                JSONArray categories = page.getJSONArray("categories");
                List<String> categoryList = new ArrayList<String>();
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject category = categories.getJSONObject(i);
                    categoryList.add(category.getString("title"));
                }
                return categoryList;
                
            }
            
        } catch (JSONException jse) {
            jse.printStackTrace();
            log(jse.getMessage());
        }
                
        return null;
    }
    
    
    private void printError(JSONObject body) throws JSONException{
        JSONObject e = body.getJSONObject("error");
        log(e.getString("info"));
    }
    
    
    private void printWarnings(JSONObject body) throws JSONException{
        JSONObject warnings = body.getJSONObject("warnings");
        Iterator warningCategoryKeys = warnings.keys();
        String categoryKey;
        JSONObject category;
        while(warningCategoryKeys.hasNext()){
            categoryKey = (String)warningCategoryKeys.next();
            category = warnings.getJSONObject(categoryKey);
            Iterator warningKeys = category.keys();
            String warningKey;
            String warning;
            while(warningKeys.hasNext()){
                warningKey = (String)warningKeys.next();
                warning = category.getString(warningKey);
                log("warning of type " + categoryKey + ": " + warning);
            }
        }
    }

}
