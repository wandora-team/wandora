/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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

package org.wandora.application.tools.extractors.palvelukartta;


import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;


/**
 *
 * @author akivela
 */


public class PalvelukarttaDepartmentExtractor extends AbstractPalvelukarttaExtractor {
    


    private int departmentCount = 0;
    
    
    
    @Override
    public String getName() {
        return "Palvelukartta department extractor...";
    }
    @Override
    public String getDescription(){
        return "Convert Palvelukartta feed of departments to a topic map. Palvelukartta (Service Map) is "+
               "a Helsinki/Espoo/Vantaa/Kauniainen region web service providing information regarding the departments and services of cities. "+
               "Helsinki, Espoo, Vantaa and Kauniainen cities locate in Southern Finland. "+
               "Read more at http://www.hel.fi/palvelukartta";
    }
    
    
   

    
    
    public boolean extractFrom(JSONObject json, TopicMap tm) {
        departmentCount = 0;
        log("Parsing JSON object!");
        extractOrganizationFrom(json, tm);
        return true;
    }
    
    
    
    public boolean extractFrom(JSONArray json, TopicMap tm) {
        departmentCount = 0;
        log("Parsing JSON array!");
        extractOrganizationFrom(json, tm);
        log("Found total "+departmentCount+" departments!");
        return true;
    }
    
    
    // -------------------------------------------------------------------------
    

    public boolean extractOrganizationFrom(JSONArray json, TopicMap tm) {
        setProgressMax(json.length());
        for(int i=0; i<json.length() && !forceStop(); i++) {
            try {
                setProgress(i);
                Object ob = json.get(i);
                if(ob != null) {
                    if(ob instanceof JSONObject) {
                        JSONObject jsono = (JSONObject) ob;
                        extractOrganizationFrom(jsono, tm);
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
    
    
    

    public boolean extractOrganizationFrom(JSONObject jsono, TopicMap tm) {
        try {
            if(jsono != null) {
                String id = getStringJSONValue(jsono, "id");
                String name_fi = getStringJSONValue(jsono, "name_fi");
                String name_sv = getStringJSONValue(jsono, "name_sv");
                String name_en = getStringJSONValue(jsono, "name_en");
                String abbr = getStringJSONValue(jsono, "abbr");
                long org_id = getLongJSONValue(jsono, "org_id");

                if(id != null) {
                    departmentCount++;
                    Topic departmentTopic = getDepartmentTopic(id, tm);
                    if(departmentTopic != null) {
                        boolean hasBasename = false;
                        if(name_fi != null) {
                            departmentTopic.setDisplayName("fi", name_fi);
                            if(!hasBasename) {
                                departmentTopic.setBaseName(name_fi + " ("+id+")");
                                hasBasename = true;
                            }
                        }
                        if(name_en != null) {
                            departmentTopic.setDisplayName("en", name_en);
                            if(!hasBasename) {
                                departmentTopic.setBaseName(name_en + " ("+id+")");
                                hasBasename = true;
                            }
                        }
                        if(name_sv != null) {
                            departmentTopic.setDisplayName("sv", name_sv);
                            if(!hasBasename) {
                                departmentTopic.setBaseName(name_sv + " ("+id+")");
                                hasBasename = true;
                            }
                        }
                        if(abbr != null) {
                            Topic abbrType = getAbbrType(tm);
                            Topic langTopic = getDefaultLangTopic(tm);
                            departmentTopic.setData(abbrType, langTopic, abbr);
                        }
                        if(org_id != -1) {
                            Topic organizationTopic = getOrganizationTopic(org_id, tm);
                            if(organizationTopic != null) {
                                Topic departmentOrganizationType = getDepartmentOrganizationType(tm);
                                Topic departmentType = getDepartmentType(tm);
                                Topic organizationType = getOrganizationType(tm);

                                Association a = tm.createAssociation(departmentOrganizationType);
                                a.addPlayer(organizationTopic, organizationType);
                                a.addPlayer(departmentTopic, departmentType);
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
    
    
}
