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


package org.wandora.application.tools.occurrences.refine;



import java.net.URL;
import java.net.URLEncoder;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.extractors.geonames.FindNearByGeoNames;
import org.wandora.application.tools.extractors.geonames.GeoNamesExtractorSelector;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.xml.sax.SAXException;


/**
 *
 * @author akivela
 */
public class FindNearByGeoNamesOccurrence extends AbstractOccurrenceExtractor {

	private static final long serialVersionUID = 1L;
	private static int rows = 1000;
    private static int radius = 1;
    private static String lang = "en";


    


    public FindNearByGeoNamesOccurrence() {
    }
    public FindNearByGeoNamesOccurrence(Context preferredContext) {
        super(preferredContext);
    }




    @Override
    public String getName() {
        return "Find near by occurrence extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts near by locations of given geo coordinate occurrences using GeoNames web service.";
    }







    public boolean _extractTopicsFrom(String occurrenceData, Topic masterTopic, TopicMap topicMap, Wandora wandora) throws Exception {
        if(occurrenceData != null && occurrenceData.length() > 0) {
            String[] coords = solveGeoCoordinates(occurrenceData);
            if(coords != null) {
                if(coords.length > 1) {
                    try {
                        FindNearByGeoNames e = new FindNearByGeoNames();
                        if(masterTopic != null) {
                            e.setMasterSubject(masterTopic);
                        }

                        String urlStr = GeoNamesExtractorSelector.BASE_URL+"findNearby?style=full";
                        urlStr += "&lang="+lang;
                        urlStr += "&lat="+URLEncoder.encode(coords[0], "utf-8");
                        urlStr += "&lng="+URLEncoder.encode(coords[1], "utf-8");
                        urlStr += "&maxRows="+rows;
                        urlStr += "&radius="+radius;

                        log("Finding geolocations near by "+coords[0]+", "+coords[1]);
                        System.out.println("Extracting: "+urlStr);

                        e.setForceUrls( new String[] { urlStr } );
                        e.setToolLogger(getDefaultLogger());
                        e._extractTopicsFrom(new URL(urlStr), topicMap);

                    }
                    catch (Exception e) {
                        if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                    }
                }
            }
            else {
                log("Found no geo coordinates in occurrence!");
            }
        }
        return true;
    }


    public String[] solveGeoCoordinates(String coords) {
        coords = coords.trim();
        if(coords.indexOf(';') > -1) {
            return coords.split(";");
        }
        else if(coords.indexOf(',') > -1) {
            return coords.split(",");
        }
        else if(coords.indexOf(':') > -1) {
            return coords.split(":");
        }
        return null;
    }

}
