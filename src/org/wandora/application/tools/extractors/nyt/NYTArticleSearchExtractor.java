/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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


package org.wandora.application.tools.extractors.nyt;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author akivela
 */


public class NYTArticleSearchExtractor extends AbstractNYTExtractor {

    
    private static String defaultLang = "en";
    private static String currentURL = null;
    

    @Override
    public String getName() {
        return "New York Times Article Search API extractor";
    }

    @Override
    public String getDescription(){
        return "Extractor performs an article search using The New York Times API and "+
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
        
        log("Article search extraction with "+currentURL);
        
        String in = IObox.doUrl(u);
        
        System.out.println("New York Times API returned-------------------------\n"+in+
                         "\n----------------------------------------------------");
        
        JSONObject json = new JSONObject(in);
        parse(json, tm);
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        currentURL = null;
        JSONObject json = new JSONObject(str);
        parse(json, tm);
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
                log(ex);
            }
            
        }
        handlePagination(json, tm);
    }
    
    
    
    
    
    private boolean shouldHandlePagination = true;
    private String defaultPagingOption = null;
    private void handlePagination(JSONObject json, TopicMap tm) {
        if(!shouldHandlePagination || forceStop()) return;
        if(json.has("total")) {
            try {
                int page = 0;
                if(json.has("offset")) {
                    page = json.getInt("offset");
                }
                int total = json.getInt("total");
                int totalPages = (total/10) + (total%10==0 ? 0 : 1);
                if(page < totalPages) {
                    if(currentURL != null) {
                        String[] pagingOptions = new String[] {
                            "Do not extract any more pages",
                            "Extract only next page",
                            "Extract next page",
                            "Extract 10 next pages",
                            "Extract all next pages"
                        };
                        String message = "You have just extracted page "+(page+1)+". There is total "+totalPages+" pages available. What would you like to do? "+
                                "Remember New York Times APIs limit daily requests. Extracting one page takes one request.";
                        if(defaultPagingOption == null) defaultPagingOption = pagingOptions[0];
                        String a = WandoraOptionPane.showOptionDialog(Wandora.getWandora(), message, "Found more pages",  WandoraOptionPane.OK_CANCEL_OPTION, pagingOptions, defaultPagingOption);
                        defaultPagingOption = a;
                        if(a != null) {
                            String originalURL = currentURL;
                            try {
                                if(pagingOptions[1].equals(a)) {
                                    System.out.println("Selected to extract only next page");
                                    String newURL = originalURL.replace("offset="+page, "offset="+(page+1));
                                    shouldHandlePagination = false;
                                    _extractTopicsFrom(new URL(newURL), tm);
                                }

                                else if(pagingOptions[2].equals(a)) {
                                    System.out.println("Selected to extract next page");
                                    String newURL = originalURL.replace("offset="+page, "offset="+(page+1));
                                    _extractTopicsFrom(new URL(newURL), tm);
                                }

                                else if(pagingOptions[3].equals(a)) {
                                    System.out.println("Selected to extract 10 next pages");
                                    shouldHandlePagination = false;
                                    setProgress(1);
                                    setProgressMax(10);
                                    int progress = 1;
                                    for(int p=page+1; p<=Math.min(page+10, totalPages) && !forceStop(); p++) {
                                        String newURL = originalURL.replace("offset="+page, "offset="+p);
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
                                    setProgressMax((int) (total-page));
                                    int progress = 1;
                                    for(int p=page+1; p<=totalPages && !forceStop(); p++) {
                                        String newURL = originalURL.replace("offset="+page, "offset="+p);
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
    
    
    
    
    
    
    public void parseResult(JSONObject result, TopicMap tm) throws JSONException, TopicMapException {
        
        if(result.has("url")) { // All results should contain an url at least.
            String url = result.getString("url");
            Topic articleTopic = tm.createTopic();
            articleTopic.addSubjectIdentifier(new Locator(url));
            articleTopic.addType(getArticleTypeTopic(tm));

            if(result.has("body")) {
                String body = result.getString("body");
                if(body != null && body.length() > 0) {
                    body = HTMLEntitiesCoder.decode(body);
                    Topic bodyTypeTopic = getBodyTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    articleTopic.setData(bodyTypeTopic, langTopic, body);
                }
            }
            if(result.has("abstract")) {
                String abst = result.getString("abstract");
                if(abst != null && abst.length() > 0) {
                    abst = HTMLEntitiesCoder.decode(abst);
                    Topic abstTypeTopic = getAbstractTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    articleTopic.setData(abstTypeTopic, langTopic, abst);
                }
            }
            if(result.has("text")) {
                String text = result.getString("text");
                if(text != null && text.length() > 0) {
                    text = HTMLEntitiesCoder.decode(text);
                    Topic textTypeTopic = getTextTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    articleTopic.setData(textTypeTopic, langTopic, text);
                }
            }
            if(result.has("lead_paragraph")) {
                String lead_paragraph = result.getString("lead_paragraph");
                if(lead_paragraph != null && lead_paragraph.length() > 0) {
                    lead_paragraph = HTMLEntitiesCoder.decode(lead_paragraph);
                    Topic leadParagraphTypeTopic = getLeadParagraphTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    articleTopic.setData(leadParagraphTypeTopic, langTopic, lead_paragraph);
                }
            }
            if(result.has("date")) {
                String date = result.getString("date");
                if(date != null && date.length() > 0) {
                    Topic dateTypeTopic = getDateTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    articleTopic.setData(dateTypeTopic, langTopic, date);
                }
            }
            if(result.has("title")) {
                String title = result.getString("title");
                if(title != null && title.length() > 0) {
                    articleTopic.setDisplayName(defaultLang, title);
                    articleTopic.setBaseName(title + " (NYT article)");
                }
            }

            if(result.has("des_facet")) {
                JSONArray facetArray = result.getJSONArray("des_facet");
                parseFacets(facetArray, getDesFacetTypeTopic(tm), articleTopic, tm);
            }
            
            if(result.has("geo_facet")) {
                JSONArray facetArray = result.getJSONArray("geo_facet");
                parseFacets(facetArray, getGeoFacetTypeTopic(tm), articleTopic, tm);
            }
            
            if(result.has("org_facet")) {
                JSONArray facetArray = result.getJSONArray("org_facet");
                parseFacets(facetArray, getOrgFacetTypeTopic(tm), articleTopic, tm);
            }
            
            if(result.has("per_facet")) {
                JSONArray facetArray = result.getJSONArray("per_facet");
                parseFacets(facetArray, getPerFacetTypeTopic(tm), articleTopic, tm);
            }
            
            if(result.has("classifiers_facet")) {
                JSONArray facetArray = result.getJSONArray("classifiers_facet");
                parseFacets(facetArray, getClassifierFacetTypeTopic(tm), articleTopic, tm);
            }
            
            if(result.has("source_facet")) {
                JSONArray facetArray = result.getJSONArray("source_facet");
                parseFacets(facetArray, getSourceFacetTypeTopic(tm), articleTopic, tm);
            }
            
            if(result.has("column_facet")) {
                String facetStr = result.getString("column_facet");
                if(facetStr.length() > 0) {
                    Topic facetTopic = getFacetTopic(facetStr, tm);
                    Topic articleTypeTopic = getArticleTypeTopic(tm);
                    Topic facetTypeTopic = getColumnFacetTypeTopic(tm);

                    if(facetTopic != null && facetTypeTopic != null && articleTopic != null && articleTypeTopic != null) {
                        Association a = tm.createAssociation(facetTypeTopic);
                        a.addPlayer(facetTopic, facetTypeTopic);
                        a.addPlayer(articleTopic, articleTypeTopic);
                    }
                }
            }
            
            if(result.has("material_type_facet")) {
                JSONArray facetArray = result.getJSONArray("material_type_facet");
                parseFacets(facetArray, getMaterialTypeFacetTypeTopic(tm), articleTopic, tm);
            }
            
            if(result.has("dbpedia_resource_url")) {
                JSONArray resources = result.getJSONArray("dbpedia_resource_url");
                if(resources.length() > 0) {
                    for(int i=0; i<resources.length(); i++) {
                        String res = resources.getString(i);
                        if(res != null) {
                            res = res.trim();
                            if(res.length() > 0) {
                                Topic resourceTopic = getDBpediaResourceTopic(res, tm);
                                Topic articleTypeTopic = getArticleTypeTopic(tm);
                                Topic resourceTypeTopic = getDBpediaResourceTypeTopic(tm);

                                if(resourceTopic != null && resourceTypeTopic != null && articleTopic != null && articleTypeTopic != null) {
                                    Association a = tm.createAssociation(resourceTypeTopic);
                                    a.addPlayer(resourceTopic, resourceTypeTopic);
                                    a.addPlayer(articleTopic, articleTypeTopic);
                                }
                            }
                        }
                    }
                }
            }
            
            if(result.has("byline")) {
                String byline = result.getString("byline");
                if(byline != null && byline.length() > 0) {
                    Topic bylineTopic = getBylineTopic(byline, tm);
                    Topic bylineTypeTopic = getBylineTypeTopic(tm);
                    Topic articleTypeTopic = getArticleTypeTopic(tm);

                    Association a = tm.createAssociation(bylineTypeTopic);
                    a.addPlayer(bylineTopic, bylineTypeTopic);
                    a.addPlayer(articleTopic, articleTypeTopic);
                }
            }
        }
    }
    
    
    
    
    
    public void parseFacets(JSONArray facetArray, Topic facetTypeTopic, Topic articleTopic, TopicMap tm) throws JSONException, TopicMapException {
        if(facetArray != null) {
            for(int i=0; i<facetArray.length(); i++) {
                Object facet = facetArray.get(i);
                if(facet != null) {
                    String facetStr = facet.toString();
                    if(facetStr.length() > 0) {
                        Topic facetTopic = getFacetTopic(facetStr, tm);
                        Topic articleTypeTopic = getArticleTypeTopic(tm);

                        if(facetTopic != null && facetTypeTopic != null && articleTopic != null && articleTypeTopic != null) {
                            Association a = tm.createAssociation(facetTypeTopic);
                            a.addPlayer(facetTopic, facetTypeTopic);
                            a.addPlayer(articleTopic, articleTypeTopic);
                        }
                    }
                }
            }
        }
    }
    
    
    
    
}
