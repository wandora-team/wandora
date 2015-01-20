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
package org.wandora.application.tools.extractors.palvelukartta_v2;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;


/**
 * See http://www.hel.fi/palvelukarttaws/rest/ver2.html
 *
 * @author akivela
 * @author Eero Lehtonen
 */


public class PalvelukarttaServiceExtractor extends AbstractPalvelukarttaExtractor {
    


    private int serviceCount = 0;
    
    
    
    @Override
    public String getName() {
        return "Palvelukartta service extractor...";
    }
    @Override
    public String getDescription(){
        return "Convert Palvelukartta feed of services to a topic map. Palvelukartta (Service Map) is "+
               "a Helsinki/Espoo/Vantaa/Kauniainen region web service providing information regarding the departments and services of cities. "+
               "Helsinki, Espoo, Vantaa and Kauniainen cities locate in Southern Finland. "+
               "Read more at http://www.hel.fi/palvelukartta";
    }
    

    
    
    public boolean extractFrom(JSONObject json, TopicMap tm) {
        serviceCount = 0;
        log("Parsing JSON object!");
        extractServiceFrom(json, null, tm);
        return true;
    }
    
    
    public boolean extractFrom(JSONArray json, TopicMap tm) {
        serviceCount = 0;
        log("Parsing JSON array!");
        extractServiceFrom(json, null, tm);
        log("Found total "+serviceCount+" services!");
        return true;
    }
    
    
    // -------------------------------------------------------------------------
    

    public boolean extractServiceFrom(JSONArray json, Topic parentServiceTopic, TopicMap tm) {
        setProgressMax(json.length());
        for(int i=0; i<json.length() && !forceStop(); i++) {
            try {
                setProgress(i);
                Object ob = json.get(i);
                if(ob != null) {
                    if(ob instanceof JSONObject) {
                        JSONObject jsono = (JSONObject) ob;
                        extractServiceFrom(jsono, parentServiceTopic, tm);
                    }
                }
            }
            catch(Exception e) {
                log(e);
                e.printStackTrace();
            }
        }
        return true;
    }
    
    
    
    
    
    public boolean extractServiceFrom(JSONObject json, Topic parentServiceTopic, TopicMap tm) {

        long id = getLongJSONValue(json, "id");
        String name_fi = getStringJSONValue(json, "name_fi");
        String name_sv = getStringJSONValue(json, "name_sv");
        String name_en = getStringJSONValue(json, "name_en");
        Object child_ids = getJSONValue(json, "child_ids");
        Object unit_ids = getJSONValue(json, "unit_ids");

        try {
            if(id != -1) {
                serviceCount++;
                Topic serviceTopic = getServiceTopic(id, tm);
                if(serviceTopic != null) {
                    if(parentServiceTopic != null) {
                        associateServices(parentServiceTopic, serviceTopic, tm);
                    }
                    boolean hasBasename = false;
                    if(name_fi != null) {
                        serviceTopic.setDisplayName("fi", name_fi);
                        if(!hasBasename) {
                            serviceTopic.setBaseName(name_fi + " ("+id+")");
                            hasBasename = true;
                        }
                    }
                    if(name_en != null) {
                        serviceTopic.setDisplayName("en", name_en);
                        if(!hasBasename) {
                            serviceTopic.setBaseName(name_en + " ("+id+")");
                            hasBasename = true;
                        }
                    }
                    if(name_sv != null) {
                        serviceTopic.setDisplayName("sv", name_sv);
                        if(!hasBasename) {
                            serviceTopic.setBaseName(name_sv + " ("+id+")");
                            hasBasename = true;
                        }
                    }
                    if(child_ids != null) {
                        if(child_ids instanceof JSONArray) {
                            JSONArray childArray = (JSONArray) child_ids;
                            for(int i=0; i<childArray.length(); i++) {
                                try {
                                    long child_id = childArray.getLong(i);
                                    Topic childTopic = getServiceTopic(child_id, tm);
                                    associateServices(serviceTopic, childTopic, tm);
                                }
                                catch(Exception e) {
                                    log(e);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if(unit_ids != null) {
                        if(unit_ids instanceof JSONArray) {
                            JSONArray unitArray = (JSONArray) unit_ids;
                            for(int i=0; i<unitArray.length(); i++) {
                                try {
                                    long unit_id = unitArray.getLong(i);
                                    Topic unitTopic = getUnitTopic(unit_id, tm);
                                    Topic unitServiceType = getUnitServiceType(tm);
                                    Topic unitType = getUnitType(tm);
                                    Topic serviceType = getServiceType(tm);

                                    Association a = tm.createAssociation(unitServiceType);
                                    a.addPlayer(unitTopic, unitType);
                                    a.addPlayer(serviceTopic, serviceType);
                                }
                                catch(Exception e) {
                                    log(e);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    
                }
            }
        }
        catch(Exception e) {
            log(e);
            e.printStackTrace();
        }
        return true;
    }
    
    
    
    // -------------------------------------------------------------------------
    

}
