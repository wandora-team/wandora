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



package org.wandora.application.tools.extractors.palvelukartta_v2;



import org.wandora.application.tools.extractors.palvelukartta.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;


/**
 * http://www.hel.fi/palvelukarttaws/rest/v1/organization/
 *
 * @author akivela
 */


public class PalvelukarttaOrganizationExtractor extends AbstractPalvelukarttaExtractor {
    


    private int organizationCount = 0;
    
    
    
    @Override
    public String getName() {
        return "Palvelukartta organization extractor...";
    }
    @Override
    public String getDescription(){
        return "Convert Palvelukartta feed of organizations to a topic map. Palvelukartta (Service Map) is "+
               "a Helsinki/Espoo/Vantaa/Kauniainen region web service providing information regarding the departments and services of cities. "+
               "Helsinki, Espoo, Vantaa and Kauniainen cities locate in Southern Finland. "+
               "Read more at http://www.hel.fi/palvelukartta";
    }
    
    

    
    public boolean extractFrom(JSONObject json, TopicMap tm) {
        organizationCount = 0;
        log("Parsing JSON object!");
        extractOrganizationsFrom(json, tm);
        return true;
    }
    
    
    public boolean extractFrom(JSONArray json, TopicMap tm) {
        organizationCount = 0;
        log("Parsing JSON array!");
        extractOrganizationsFrom(json, tm);
        log("Found total "+organizationCount+" organizations!");
        return true;
    }
    
    
    // -------------------------------------------------------------------------
    

    public boolean extractOrganizationsFrom(JSONArray json, TopicMap tm) {
        setProgressMax(json.length());
        for(int i=0; i<json.length() && !forceStop(); i++) {
            try {
                setProgress(i);
                Object ob = json.get(i);
                if(ob != null) {
                    if(ob instanceof JSONObject) {
                        JSONObject jsono = (JSONObject) ob;
                        extractOrganizationsFrom(jsono, tm);
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
    

    
    public boolean extractOrganizationsFrom(JSONObject jsono, TopicMap tm) {
        try {
            if(jsono != null) {
                long id = getLongJSONValue(jsono, "id");
                String name_fi = getStringJSONValue(jsono, "name_fi");
                String name_sv = getStringJSONValue(jsono, "name_sv");
                String name_en = getStringJSONValue(jsono, "name_en");
                String data_source_url = getStringJSONValue(jsono, "data_source_url");

                if(id != -1) {
                    organizationCount++;
                    Topic organizationTopic = getOrganizationTopic(id, tm);
                    if(organizationTopic != null) {
                        boolean hasBasename = false;
                        if(name_fi != null) {
                            organizationTopic.setDisplayName("fi", name_fi);
                            if(!hasBasename) {
                                organizationTopic.setBaseName(name_fi + " ("+id+")");
                                hasBasename = true;
                            }
                        }
                        if(name_en != null) {
                            organizationTopic.setDisplayName("en", name_en);
                            if(!hasBasename) {
                                organizationTopic.setBaseName(name_en + " ("+id+")");
                                hasBasename = true;
                            }
                        }
                        if(name_sv != null) {
                            organizationTopic.setDisplayName("sv", name_sv);
                            if(!hasBasename) {
                                organizationTopic.setBaseName(name_sv + " ("+id+")");
                                hasBasename = true;
                            }
                        }
                        if(data_source_url != null) {
                            Topic dataSourceType = getDataSourceType(tm);
                            Topic langTopic = getDefaultLangTopic(tm);
                            organizationTopic.setData(dataSourceType, langTopic, data_source_url);
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
