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

package org.wandora.application.tools.extractors.guardian;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.HTMLEntitiesCoder;
import org.wandora.utils.IObox;

/**
 *
 * @author
 * Eero
 */


public class GuardianTagSearchExtractor extends AbstractGuardianExtractor {
  private static String defaultLang = "en";
  private static String currentURL = null;


  @Override
  public String getName() {
      return "The Guardian Tag Search API extractor";
  }

  @Override
  public String getDescription(){
      return "Extractor performs an tag search using The Guardian API and "+
              "transforms results to topics and associations.";
  }
  
  // -------------------------------------------------------------------------
  
  @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        currentURL = null;
        String in = IObox.loadFile(f);
        JSONObject json = new JSONObject(in);
        parse(json, tm);
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        currentURL = u.toExternalForm();
        
        log("Tag search extraction with "+currentURL);
        
        String in = IObox.doUrl(u);
        
        System.out.println("The Guardian API returned-------------------------\n"+in+
                         "\n----------------------------------------------------");
        
        JSONObject json = new JSONObject(in);
        
        if (json.has("response")){
            try{
                JSONObject response = json.getJSONObject("response");
                int nResults = response.getJSONArray("results").length();
                if(response.has("didYouMean") && nResults == 0){
                    String dym = response.getString("didYouMean");
                    int didMean = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Did you mean \"" + dym + "\"","Did you mean",WandoraOptionPane.YES_NO_OPTION);
                    if(didMean == 1100){
                        URL newUrl = new URL(currentURL.replaceAll("&q=[^&]*", "&q=" + dym));
                        System.out.println(newUrl.toString());
                        this._extractTopicsFrom(newUrl, tm);
                    } else {
                        parse(response, tm);
                    }
                } else {
                    parse(response, tm);
                }
            } catch (Exception e){
                System.out.println(e);
            }
            
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        currentURL = null;
        JSONObject json = new JSONObject(str);
        if (json.has("response")){
            System.out.println("json has response!");
            JSONObject response = json.getJSONObject("response");
            parse(response, tm);
        }
        return true;
    }
  
    // -------------------------------------------------------------------------
    
    public void parse(JSONObject json, TopicMap tm) throws TopicMapException {
        if(json.has("results")) {
            try {
                JSONArray resultsArray = json.getJSONArray("results");
                for(int i=0; i<resultsArray.length(); i++) {
                    JSONObject result = resultsArray.getJSONObject(i);
                    parseResult(result, tm);
                }
            } 
            catch (JSONException ex) {
                System.out.println(ex);
                log(ex);
            }
        }
        handlePagination(json, tm);
    }
    
    private boolean shouldHandlePagination = true;
    private String defaultPagingOption = null;
    
    private void handlePagination(JSONObject json, TopicMap tm) {
        if(!shouldHandlePagination || forceStop()) return;
        if(json.has("pages")) {
            try {
                int page = json.getInt("currentPage");
                int total = json.getInt("pages");
                if(page < total) {
                    if(currentURL != null) {
                        String[] pagingOptions = new String[] {
                            "Do not extract any more pages",
                            "Extract only next page",
                            "Extract next page",
                            "Extract 10 next pages",
                            "Extract all next pages"
                        };
                        String message = "You have just extracted page "+page+". There is total "+total+" pages available. What would you like to do? "+
                                "Remember The Guardian APIs limit daily requests. Extracting one page takes one request.";
                        if(defaultPagingOption == null) defaultPagingOption = pagingOptions[0];
                        String a = WandoraOptionPane.showOptionDialog(Wandora.getWandora(), message, "Found more pages",  WandoraOptionPane.OK_CANCEL_OPTION, pagingOptions, defaultPagingOption);
                        defaultPagingOption = a;
                        if(a != null) {
                            String originalURL = currentURL;
                            try {
                                if(pagingOptions[1].equals(a)) {
                                    System.out.println("Selected to extract only next page");
                                    String newURL = originalURL.replace("page="+page, "page="+(page+1));
                                    shouldHandlePagination = false;
                                    _extractTopicsFrom(new URL(newURL), tm);
                                }

                                else if(pagingOptions[2].equals(a)) {
                                    System.out.println("Selected to extract next page");
                                    String newURL = originalURL.replace("page="+page, "page="+(page+1));
                                    _extractTopicsFrom(new URL(newURL), tm);
                                }

                                else if(pagingOptions[3].equals(a)) {
                                    System.out.println("Selected to extract 10 next pages");
                                    shouldHandlePagination = false;
                                    setProgress(1);
                                    setProgressMax(10);
                                    int progress = 1;
                                    for(int p=page+1; p<=Math.min(page+10, total) && !forceStop(); p++) {
                                        String newURL = originalURL.replace("page="+page, "page="+p);
                                        if(p == page+10) shouldHandlePagination = true;
                                        _extractTopicsFrom(new URL(newURL), tm);
                                        setProgress(progress++);
                                        nap();
                                    }
                                }

                                else if(pagingOptions[4].equals(a)) {
                                    System.out.println("Selected to extract all pages");
                                    shouldHandlePagination = false;
                                    setProgress(1);
                                    setProgressMax((int) (total));
                                    int progress = 1;
                                    for(int p=page+1; p<=total && !forceStop(); p++) {
                                        String newURL = originalURL.replace("page="+page, "page="+p);
                                        _extractTopicsFrom(new URL(newURL), tm);
                                        setProgress(progress++);
                                        nap();
                                    }
                                    shouldHandlePagination = true;
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                    }
                }
            } 
            catch (JSONException ex) {
                log(ex);
            }
        }
    }
    
    private void nap() {
        try {
            Thread.sleep(200);
        }
        catch(Exception e) {
            // WAKE UP
        }
    }
    
    public void parseResult(JSONObject tag, TopicMap tm) throws JSONException, TopicMapException {
            
        String tagId = tag.getString("id");
        String tagUrl = tag.getString("webUrl");
        String tagTitle = tag.has("webTitle") ? tag.getString("webTitle") : null;
        String tagTtype = tag.has("type") ? tag.getString("type") : null;
        String tagSectId = tag.has("sectionId") ? tag.getString("sectionId") : null;
        String tagSectName = tag.has("sectionName") ? tag.getString("sectionName") : null;


        Topic tagTopic = getOrCreateTopic(tm, TAG_BASE_SI + tagId);
        Topic tagTopicType = getTagTopicType(tm);
        tagTopic.addType(tagTopicType);
        tagTopic.addSubjectIdentifier(new Locator(tagUrl));

        tagTopic.setBaseName(tagId);

        if (tagTtype != null){
            parseTagAssociation(tag, "type", tm, tagTopic, tagTopicType);
        }

        if (tagSectId != null){
            parseTagAssociation(tag, "sectionId", tm, tagTopic, tagTopicType);
        }

        if (tagSectName != null){
            parseTagAssociation(tag, "sectionName", tm, tagTopic, tagTopicType);
        }
    }
    
    private void parseTagAssociation(JSONObject result, String jsonObjectName, TopicMap tm, Topic ct, Topic cty) {
        try{
            String s = result.getString(jsonObjectName);
            if(s != null && s.length() > 0) {
                Topic t = getTagTopic(tm, jsonObjectName, s);
                Topic ty = getTagType(tm, jsonObjectName);
                Association a = tm.createAssociation(ty);
                a.addPlayer(ct, cty);
                a.addPlayer(t, ty);
            } 
        } catch (Exception ex) {
            log(ex);
        }
    }
}
