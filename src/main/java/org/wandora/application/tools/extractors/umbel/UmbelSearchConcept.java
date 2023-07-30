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
 *
 * 
 */


package org.wandora.application.tools.extractors.umbel;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;


/**
 *
 * @author akivela
 */


public class UmbelSearchConcept extends UmbelGetConcept {

	private static final long serialVersionUID = 1L;

	public static final String API_URL = "http://umbel.org/ws/search/";
    public static final int MAX_PAGE_INDEX = 100;

    
    @Override
    public String getName(){
        return "Umbel concept search";
    }
    
    @Override
    public String getDescription(){
        return "Search concepts from Umbel knowledge graph.";
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        String query = WandoraOptionPane.showInputDialog(wandora, "Search for Umbel concepts with query", "", "Search for Umbel concepts", WandoraOptionPane.QUESTION_MESSAGE);
        int pageIndex = 0;
        int numberOfPages = 1;
        ArrayList<JSONObject> allResults = new ArrayList<>();
        if(query != null && query.length()>0) {
            do {
                String requestUrl = getApiRequestUrlFor(query);
                requestUrl = requestUrl + "/page/" + pageIndex;
                JSONObject response = performRequest(requestUrl, query);
                if(response != null) {
                    if(response.has("nb-results")) {
                        try {
                            int numberOfResults = response.getInt("nb-results");
                            numberOfPages = (numberOfResults / 10) + 1;
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(response.has("results")) {
                        try {
                            JSONArray pageResults = response.getJSONArray("results");
                            for(int i=0; i<pageResults.length(); i++) {
                                allResults.add(pageResults.getJSONObject(i));
                            }
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                pageIndex++;
            }
            while(pageIndex < (numberOfPages-1) && pageIndex < MAX_PAGE_INDEX);

            if(allResults.size() > 0) {
                UmbelSearchConceptSelector selector = new UmbelSearchConceptSelector(allResults, wandora);
                selector.open(wandora);

                if(selector.wasAccepted()) {
                    ArrayList<JSONObject> selectedConcepts = selector.getSelection();
                    if(selectedConcepts != null && selectedConcepts.size() > 0) {
                        TopicMap tm = wandora.getTopicMap();
                        for(JSONObject conceptJSON : selectedConcepts) {
                            try {
                                System.out.println("selected concept: "+conceptJSON.getString("uri"));
                                String uri = conceptJSON.getString("uri");
                                String prefLabel = conceptJSON.getString("pref-label");
                                Topic conceptTopic = getConceptTopic(uri, prefLabel, tm);

                                if(conceptJSON.has("description")) {
                                    String description = conceptJSON.getString("description");
                                    if(description != null && description.length() > 0) {
                                        Topic descriptionTypeTopic = getTopic(UMBEL_DEFINITION_URI, tm);
                                        conceptTopic.setData(descriptionTypeTopic, getTopic(XTMPSI.getLang(LANG), tm), description);
                                    }
                                }

                                if(conceptJSON.has("type")) {
                                    ArrayList<String> typeUris = getAsStringArray(conceptJSON.get("type"));
                                    for(String typeUri : typeUris) {
                                        if(typeUri != null) {
                                            Topic typeConceptTopic = conceptTopic;
                                            if(!uri.equals(typeUri)) {
                                                typeConceptTopic = getConceptTopic(typeUri, tm);
                                            }
                                            Topic typeTypeTopic = getTypeTypeTopic(tm);
                                            Association a = tm.createAssociation(typeTypeTopic);
                                            a.addPlayer(typeConceptTopic, typeTypeTopic);
                                            a.addPlayer(conceptTopic, getConceptTypeTopic(tm));
                                        }
                                    }
                                }

                                if(conceptJSON.has("alt-labels")) {
                                    ArrayList<String> labels = getAsStringArray(conceptJSON.get("alt-labels"));
                                    StringBuilder labelsBuilder = new StringBuilder("");
                                    boolean firstLabel = true;
                                    for(String label : labels) {
                                        if(label != null) {
                                            if(!firstLabel) labelsBuilder.append(" ; ");
                                            labelsBuilder.append(label);
                                            firstLabel = false;
                                        }
                                    }
                                    if(labelsBuilder.length() > 0) {
                                        Topic altLabelTypeTopic = getTopic(UMBEL_ALT_LABEL_URI, tm);
                                        conceptTopic.setData(altLabelTypeTopic, getTopic(XTMPSI.getLang(LANG), tm), labelsBuilder.toString());
                                    }
                                }
                            }
                            catch(JSONException jsone) {
                                log(jsone);
                            }
                            catch(TopicMapException tme) {
                                log(tme);
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                    }
                }
            }
            else {
                log("Found no Umbel concepts with query '"+query+"'.");
            }
        }
    }
    
    
    private ArrayList<String> getAsStringArray(Object o) throws JSONException {
        ArrayList<String> array = new ArrayList();
        if(o != null) {
            if(o instanceof String) {
                array.add(o.toString());
            }
            else if(o instanceof JSONArray) {
                JSONArray a = (JSONArray) o;
                for(int i=0; i<a.length(); i++) {
                    String typeUri = a.getString(i);
                    array.add(typeUri);
                }
            }
        }
        return array;
    }
    
    
    
    @Override
    protected JSONObject performRequest(String urlStr, String query) {
        JSONObject response = null;
        if(urlStr != null) {
            try {
                URL url = new URL(urlStr);
                URLConnection urlConnection = url.openConnection();
                urlConnection.addRequestProperty("Accept", "application/json");
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);

                if(urlConnection instanceof HttpURLConnection) {
                    ((HttpURLConnection) urlConnection).setRequestMethod("GET");
                }
                
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder inputBuffer = new StringBuilder("");
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    inputBuffer.append(inputLine);
                in.close();
                
                return new JSONObject(inputBuffer.toString());
            }
            catch(FileNotFoundException fnfe) {
                log("Can't find Umbel query for '"+query+"'.");
            }
            catch(IOException fnfe) {
                log("IOException occurred while reading URL "+urlStr+" . Skipping query '"+query+"'.");
            } 
            catch (JSONException ex) {
                log("Can't parse received Umbel JSON. Skipping concept '"+query+"'.");
            }
            catch(Exception e) {
                log(e);
            }
        }
        return response;
    }
    
    
    
    
    
    
    @Override
    public String getApiRequestUrlFor(String str) {
        return API_URL + urlEncode(str);
    }
    
    
    
    
    
    

    

    
}
