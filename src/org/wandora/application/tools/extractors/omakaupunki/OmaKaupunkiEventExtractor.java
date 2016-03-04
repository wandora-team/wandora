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


public class OmaKaupunkiEventExtractor extends OmaKaupunkiAbstractExtractor {
    
    
    public static boolean MAKE_OCCURRENCES = true;
    public static boolean MAKE_ASSOCIATIONS = true;
    
    
    
    @Override
    public String getName() {
        return "OmaKaupunki event extractor";
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
                    JSONObject singleEvent = dataArray.getJSONObject(i);
                    parseSingleEvent(singleEvent, topicMap);
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
    
    
    
    public void parseSingleEvent(JSONObject json, TopicMap topicMap) {
        try {
            if(json != null) {
                String body = robustGetString(json, "body");
                String title = robustGetString(json, "title");
                String url = robustGetString(json, "url");
                long created_at = robustGetLong(json, "created_at");
                long start_time = robustGetLong(json, "start_time");
                long end_time = robustGetLong(json, "end_time");
                long id = robustGetLong(json, "id");
                long venue = robustGetLong(json, "venue");
                double lat = robustGetDouble(json, "lat");
                double lon = robustGetDouble(json, "lon");

                String basename = title + " ("+id+")";
                Topic defaultLangTopic = getDefaultLangTopic(topicMap);
                Topic eventTopic = getUTopic(url, getEventType(topicMap), topicMap);
                eventTopic.setBaseName(basename);
                eventTopic.setDisplayName(LANG, title);

                if(id != 0) {
                    Topic eventIdentifierType = this.getEventIdentifierType(topicMap);
                    if(eventIdentifierType != null && defaultLangTopic != null) {
                        eventTopic.setData(eventIdentifierType, defaultLangTopic, ""+id);
                    }
                }
                
                if(body != null && body.length() > 0) {
                    Topic eventBodyType = this.getEventBodyType(topicMap);
                    if(eventBodyType != null && defaultLangTopic != null) {
                        eventTopic.setData(eventBodyType, defaultLangTopic, body);
                    }
                }
                if(MAKE_OCCURRENCES) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ");
                    if(start_time != 0) {
                        Date date = new Date(start_time*1000);
                        Topic eventStartTime = this.getEventStartTimeType(topicMap);
                        if(eventStartTime != null && defaultLangTopic != null) {
                            //DateFormat df = SimpleDateFormat.getInstance();
                            eventTopic.setData(eventStartTime, defaultLangTopic, df.format(date));
                        }
                    }
                    if(end_time != 0) {
                        Date date = new Date(end_time*1000);
                        Topic eventEndTime = this.getEventEndTimeType(topicMap);
                        if(eventEndTime != null && defaultLangTopic != null) {
                            //DateFormat df = SimpleDateFormat.getInstance();
                            eventTopic.setData(eventEndTime, defaultLangTopic, df.format(date));
                        }
                    }
                    if(created_at != 0) {
                        Date date = new Date(created_at*1000);
                        Topic eventCreatedTime = this.getEventCreatedType(topicMap);
                        if(eventCreatedTime != null && defaultLangTopic != null) {
                            //DateFormat df = SimpleDateFormat.getInstance();
                            eventTopic.setData(eventCreatedTime, defaultLangTopic, df.format(date));
                        }
                    }
                    if(lat != 0 && lon != 0) {
                        Topic geoCoordinateType = this.getGeoCoordinateType(topicMap);
                        if(geoCoordinateType != null && defaultLangTopic != null) {
                            eventTopic.setData(geoCoordinateType, defaultLangTopic, ""+lat+","+lon);
                        }
                    }
                    if(venue != 0) {
                        Topic eventVenueType = this.getEventVenueType(topicMap);
                        if(eventVenueType != null && defaultLangTopic != null) {
                            eventTopic.setData(eventVenueType, defaultLangTopic, ""+venue);
                        }
                    }
                }
                if(MAKE_ASSOCIATIONS) {
                    if(venue != 0) {
                        Topic eventType = this.getEventType(topicMap);
                        Topic eventVenueType = this.getEventVenueType(topicMap);
                        Topic eventVenueTopic = getEventVenueTopic(venue, topicMap);
                        if(eventType != null && eventVenueType != null && eventVenueTopic != null) {
                            Association a = topicMap.createAssociation(eventVenueType);
                            a.addPlayer(eventVenueTopic, eventVenueType);
                            a.addPlayer(eventTopic, eventType);
                        }
                        if(lat != 0 && lon != 0) {
                            Topic geoCoordinateType = this.getGeoCoordinateType(topicMap);
                            if(geoCoordinateType != null && defaultLangTopic != null) {
                                eventVenueTopic.setData(geoCoordinateType, defaultLangTopic, ""+lat+","+lon);
                            }
                        }
                    }
                    if(start_time != 0) {
                        Date date = new Date(start_time*1000);
                        Topic dateType = this.getDateType(topicMap);
                        Topic eventType = this.getEventType(topicMap);
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        Topic dateTopic = this.getDateTopic(df.format(date), topicMap);
                        if(dateType != null && dateTopic != null && eventType != null) {
                            Association a = topicMap.createAssociation(dateType);
                            a.addPlayer(dateTopic, dateType);
                            a.addPlayer(eventTopic, eventType);
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    /*
        {
            "pagination": {
                "page": 1
            },
            "data": [
                {
                    "body": "Dj:t Bommitommi, Enrico ja VG+ sekä Komposti Sound - dj:t.",
                    "title": "Dance on the Corner",
                    "url": "http://omakaupunki.fi/sornainen/tapahtumat/dance_on_the_corner.9/",
                    "created_at": 1304989145,
                    "id": 63213,
                    "end_time": 1305939600,
                    "start_time": 1305918000
                }, 
                {
                    "body": "Livenä Signmark ja Species Traitor. Dj:t Anna CCCCC, Unelmavävy ja Multitunes. Viittomakielinen tulkkaus.",
                    "title": "Utopia",
                    "url": "http://omakaupunki.fi/sornainen/tapahtumat/utopia.5/",
                    "created_at": 1304989145,
                    "id": 63212,
                    "end_time": 1305921600,
                    "start_time": 1305903600
                }
            ]
        }
    */
    
}


