/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2013 Wandora Team
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

package org.wandora.application.tools.extractors.discogs;

import java.io.File;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;

/**
 *
 * @author nlaitine
 */


public class DiscogsReleaseExtractor extends AbstractDiscogsExtractor {
    
    private static String defaultLang = "en";
    private static String currentURL = null;
    
    public DiscogsReleaseExtractor () {
    }
    
    @Override
    public String getName() {
        return "Discogs API Release extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor fetches release data from Discogs API.";
    }
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        currentURL = null;
        String in = IObox.loadFile(f);
        JSONObject json = new JSONObject(in);
        parseRelease(json, tm);
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        try {
            currentURL = u.toExternalForm();

            log("Release search extraction with " + currentURL);

            String in = DiscogsSearchExtractor.doUrl(u);

            System.out.println("---------------Discogs API returned------------\n"+in+
                               "\n-----------------------------------------------");

            JSONObject json = new JSONObject(in);
            parseRelease(json, tm);
   
        } catch (Exception e){
           e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        currentURL = null;
        JSONObject json = new JSONObject(str);
        parseRelease(json, tm);
        return true;
    }
    
    
    // ------------------------- PARSING ---------------------------------------
    
    
    public void parseRelease(JSONObject json, TopicMap tm) throws TopicMapException {
        if(json.has("results")) {
            try {
                JSONArray resultsArray = json.getJSONArray("results");
                if (resultsArray.length() > 0) {
                    int count = 0;
                    for(int i=0; i<resultsArray.length(); i++) {
                        JSONObject result = resultsArray.getJSONObject(i);
                        parseResult(result, tm);
                        count++;
                    }
                    log("Search returned " + count + " releases.");
                } else {
                    log("API returned no results.");
                }
            }
            catch (JSONException ex) {
                log(ex);
            }
        } else {
            log("API returned no results.");
        }
    }
    
    public void parseResult(JSONObject result, TopicMap tm) throws JSONException, TopicMapException {
        
        if(result.has("uri") && result.has("id")) {
            String id = result.getString("id");
            String subjectId = DISCOGS_SI + result.getString("uri");
            Topic itemTopic = tm.createTopic();
            itemTopic.addSubjectIdentifier(new Locator(subjectId));
            itemTopic.addType(getReleaseTypeTopic(tm));
            
            Topic releaseTypeTopic = getReleaseTypeTopic(tm);
            
            if(result.has("title")) {
                String value = result.getString("title");
                if(value != null && value.length() > 0) {
                    Topic titleTypeTopic = getTitleTypeTopic(tm);
                    itemTopic.setDisplayName(defaultLang, value);
                    itemTopic.setBaseName(value + " (" + id + ")");
                    Topic langTopic = getLangTopic(tm);
                    itemTopic.setData(titleTypeTopic, langTopic, value);
                    
                    int index = value.lastIndexOf(" - ");
                    value = value.substring(0, index);
                    if (!value.isEmpty()) {
                        Topic artistTopic = tm.getTopicWithBaseName(value);
                        if (artistTopic == null) {
                            artistTopic = tm.createTopic();
                            artistTopic.addSubjectIdentifier(new Locator(DISCOGS_SI + "/artist/" + urlEncode(value)));
                            artistTopic.addType(getArtistTypeTopic(tm));

                            artistTopic.setDisplayName(defaultLang, value);
                            artistTopic.setBaseName(value);
                            artistTopic.setData(titleTypeTopic, langTopic, value);                            
                        }
                        Topic artistTypeTopic = getArtistTypeTopic(tm);
                        Association a = tm.createAssociation(artistTypeTopic);
                        a.addPlayer(artistTopic, artistTypeTopic);
                        a.addPlayer(itemTopic, releaseTypeTopic);
                    }
                }
            }
            
            if(result.has("country")) {
                String value = result.getString("country");
                if(value != null && value.length() > 0) {
                    String subjectValue = COUNTRY_SI + "/" + urlEncode(value);
                    Topic countryTopic = getCountryTopic(subjectValue, tm);
                    Topic countryTypeTopic = getCountryTypeTopic(tm);
                    
                    Association a = tm.createAssociation(countryTypeTopic);
                    a.addPlayer(countryTopic, countryTypeTopic);
                    a.addPlayer(itemTopic, releaseTypeTopic);
                    countryTopic.setBaseName(value);
                }
            }
            
            if(result.has("year")) {
                String value = result.getString("year");
                if(value != null && value.length() > 0) {
                    String subjectValue = YEAR_SI + "/" + urlEncode(value);
                    Topic yearTopic = getYearTopic(subjectValue, tm);
                    Topic yearTypeTopic = getYearTypeTopic(tm);

                    Association a = tm.createAssociation(yearTypeTopic);
                    a.addPlayer(yearTopic, yearTypeTopic);
                    a.addPlayer(itemTopic, releaseTypeTopic);
                    yearTopic.setBaseName(value);
                }
            }
              
            if(result.has("style")) {
                JSONArray values = result.getJSONArray("style");
                if(values.length() > 0) {
                    for(int i=0; i<values.length(); i++) {
                        String value = values.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = STYLE_SI + "/" + urlEncode(value);
                            Topic styleTopic = getStyleTopic(subjectValue, tm);
                            Topic styleTypeTopic = getStyleTypeTopic(tm);
                            
                            Association a = tm.createAssociation(styleTypeTopic);
                            a.addPlayer(styleTopic, styleTypeTopic);
                            a.addPlayer(itemTopic, releaseTypeTopic);
                            styleTopic.setBaseName(value);
                        }
                    }
                }
            }
            
            if(result.has("barcode")) {
                JSONArray values = result.getJSONArray("barcode");
                if(values.length() > 0) {
                    for(int i=0; i<values.length(); i++) {
                        String value = values.getString(i);
                        if(value != null && value.length() > 0) {
                            Topic barcodeTypeTopic = getBarcodeTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            itemTopic.setData(barcodeTypeTopic, langTopic, value);
                        }
                   }
               }
           }
            
           if(result.has("label")) {
                JSONArray values = result.getJSONArray("label");
                if(values.length() > 0) {
                    for(int i=0; i<values.length(); i++) {
                        String value = values.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = LABEL_SI + "/" + urlEncode(value);
                            Topic labelTopic = getLabelTopic(subjectValue, tm);
                            Topic labelTypeTopic = getLabelTypeTopic(tm);
                            
                            Association a = tm.createAssociation(labelTypeTopic);
                            a.addPlayer(labelTopic, labelTypeTopic);
                            a.addPlayer(itemTopic, releaseTypeTopic);
                            labelTopic.setBaseName(value);
                        }
                   }
               }
           }
           
           if(result.has("catno")) {
                String value = result.getString("catno");
                if(value != null && value.length() > 0) {
                    Topic catnoTypeTopic = getCatnoTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    itemTopic.setData(catnoTypeTopic, langTopic, value);
                }
           }
           
           if(result.has("genre")) {
                JSONArray values = result.getJSONArray("genre");
                if(values.length() > 0) {
                    for(int i=0; i<values.length(); i++) {
                        String value = values.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = GENRE_SI + "/" + urlEncode(value);
                            Topic genreTopic = getGenreTopic(subjectValue, tm);
                            Topic genreTypeTopic = getGenreTypeTopic(tm);
                            
                            Association a = tm.createAssociation(genreTypeTopic);
                            a.addPlayer(genreTopic, genreTypeTopic);
                            a.addPlayer(itemTopic, releaseTypeTopic);
                            genreTopic.setBaseName(value);
                        }
                   }
               }
           }
           
           if(result.has("format")) {
                JSONArray values = result.getJSONArray("format");
                if(values.length() > 0) {
                    for(int i=0; i<values.length(); i++) {
                        String value = values.getString(i);
                        if(value != null && value.length() > 0) {
                            String subjectValue = FORMAT_SI + "/" + urlEncode(value);
                            Topic formatTopic = getFormatTopic(subjectValue, tm);
                            Topic formatTypeTopic = getFormatTypeTopic(tm);
                            
                            Association a = tm.createAssociation(formatTypeTopic);
                            a.addPlayer(formatTopic, formatTypeTopic);
                            a.addPlayer(itemTopic, releaseTypeTopic);
                            formatTopic.setBaseName(value);
                        }
                   }
               }
           }
           
           if(result.has("thumb")) {
                String value = result.getString("thumb");
                if(value != null && value.length() > 0) {
                    Topic imageTypeTopic = getImageTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    itemTopic.setData(imageTypeTopic, langTopic, value);
                }
           }
           
        }
    }
    
}