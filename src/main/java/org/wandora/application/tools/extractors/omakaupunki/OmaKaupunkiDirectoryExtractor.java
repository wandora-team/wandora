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
 */



package org.wandora.application.tools.extractors.omakaupunki;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author akivela
 */


public class OmaKaupunkiDirectoryExtractor extends OmaKaupunkiAbstractExtractor {

	private static final long serialVersionUID = 1L;

	public static boolean MAKE_OCCURRENCES = true;
    public static boolean MAKE_ASSOCIATIONS = true;
    
    
    
    @Override
    public String getName() {
        return "OmaKaupunki directory extractor";
    }
    
    
    public boolean parseOmaKaupunki(JSONObject json, TopicMap topicMap) {
        try {
            if(json.has("error")) {
                handleError(json);
                return false;
            }
            if(json.has("data")) {
                JSONArray dataArray = json.getJSONArray("data");
                for(int i=0; i<dataArray.length(); i++) {
                    JSONObject singleService = dataArray.getJSONObject(i);
                    parseSingleService(singleService, topicMap);
                }
            }
            if(json.has("pagination")) {
                JSONObject pagination = json.getJSONObject("pagination");
                handlePagination(pagination, topicMap);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    
    
    
    public void parseSingleService(JSONObject json, TopicMap topicMap) {
        try {
            if(json != null) {
                String description = robustGetString(json, "description");
                String title = robustGetString(json, "title");
                String url = robustGetString(json, "url");
                String phone = robustGetString(json, "phone");
                String address = robustGetString(json, "address");
                String tags = robustGetString(json, "tags");
                String homepage = robustGetString(json, "homepage");
                long category = robustGetLong(json, "category");
                long id = robustGetLong(json, "id");
                long created_at = robustGetLong(json, "created_at");
                double lat = robustGetDouble(json, "lat");
                double lon = robustGetDouble(json, "lon");

                String basename = title + " ("+id+")";
                Topic defaultLangTopic = getDefaultLangTopic(topicMap);
                Topic serviceTopic = getUTopic(url, getServiceType(topicMap), topicMap);
                serviceTopic.setBaseName(basename);
                serviceTopic.setDisplayName(LANG, title);
                
                if(id != 0) {
                    Topic serviceIdentifierType = this.getServiceIdentifierType(topicMap);
                    if(serviceIdentifierType != null && defaultLangTopic != null) {
                        serviceTopic.setData(serviceIdentifierType, defaultLangTopic, ""+id);
                    }
                }
                
                if(description != null && description.length() > 0) {
                    Topic serviceDescriptionType = this.getServiceDescriptionType(topicMap);
                    if(serviceDescriptionType != null && defaultLangTopic != null) {
                        serviceTopic.setData(serviceDescriptionType, defaultLangTopic, description);
                    }
                }
                if(MAKE_OCCURRENCES) {
                    if(created_at != 0) {
                        Date date = new Date(created_at*1000);
                        Topic serviceCreatedTime = this.getServiceCreatedType(topicMap);
                        if(serviceCreatedTime != null && defaultLangTopic != null) {
                            DateFormat df = SimpleDateFormat.getInstance();
                            serviceTopic.setData(serviceCreatedTime, defaultLangTopic, df.format(date));
                        }
                    }
                    if(lat != 0 && lon != 0) {
                        Topic geoCoordinateType = this.getGeoCoordinateType(topicMap);
                        if(geoCoordinateType != null && defaultLangTopic != null) {
                            serviceTopic.setData(geoCoordinateType, defaultLangTopic, ""+lat+","+lon);
                        }
                    }
                    if(category != 0) {
                        Topic serviceCategoryType = this.getServiceCategoryType(topicMap);
                        if(serviceCategoryType != null && defaultLangTopic != null) {
                            serviceTopic.setData(serviceCategoryType, defaultLangTopic, ""+category);
                        }
                    }
                    if(phone != null && phone.length() > 0) {
                        Topic servicePhoneType = this.getServicePhoneType(topicMap);
                        if(servicePhoneType != null && defaultLangTopic != null) {
                            serviceTopic.setData(servicePhoneType, defaultLangTopic, phone);
                        }
                    }
                    if(address != null && address.length() > 0 && !"null".equals(address)) {
                        Topic serviceAddressType = this.getServiceAddressType(topicMap);
                        if(serviceAddressType != null && defaultLangTopic != null) {
                            serviceTopic.setData(serviceAddressType, defaultLangTopic, address);
                        }
                    }
                    if(homepage != null && homepage.length() > 0 && !"null".equals(homepage)) {
                        Topic serviceHomepageType = this.getServiceHomepageType(topicMap);
                        if(serviceHomepageType != null && defaultLangTopic != null) {
                            serviceTopic.setData(serviceHomepageType, defaultLangTopic, homepage);
                        }
                    }
                }
                if(MAKE_ASSOCIATIONS) {
                    if(category != 0) {
                        Topic serviceType = this.getServiceType(topicMap);
                        Topic serviceCategoryType = this.getServiceCategoryType(topicMap);
                        Topic serviceCategoryTopic = getServiceCategoryTopic(category, topicMap);
                        if(serviceType != null && serviceCategoryType != null && serviceCategoryTopic != null) {
                            Association a = topicMap.createAssociation(serviceCategoryType);
                            a.addPlayer(serviceCategoryTopic, serviceCategoryType);
                            a.addPlayer(serviceTopic, serviceType);
                        }
                    }
                    if(tags != null && tags.length() > 0) {
                        Topic serviceType = this.getServiceType(topicMap);
                        Topic serviceTagType = this.getServiceTagType(topicMap);
                        String[] tagsArray = tags.split(",");
                        if(tagsArray.length == 1) tagsArray = tags.split(" ");
                        for(int i=0; i<tagsArray.length; i++) {
                            String singleTag = tagsArray[i];
                            if(singleTag != null) {
                                singleTag = singleTag.trim();
                                if(singleTag.length() > 0) {
                                    Topic serviceTagTopic = getServiceTagTopic(singleTag, topicMap);
                                    if(serviceType != null && serviceTagType != null && serviceTagTopic != null) {
                                        Association a = topicMap.createAssociation(serviceTagType);
                                        a.addPlayer(serviceTagTopic, serviceTagType);
                                        a.addPlayer(serviceTopic, serviceType);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
}
