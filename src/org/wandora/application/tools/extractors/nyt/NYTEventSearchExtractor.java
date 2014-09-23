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
package org.wandora.application.tools.extractors.nyt;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.HTMLEntitiesCoder;
import org.wandora.utils.IObox;
import org.wandora.utils.XMLbox;

/**
 *
 * @author
 * Eero
 */
public class NYTEventSearchExtractor extends AbstractNYTExtractor {

    private static String defaultLang = "en";
    private static String currentURL = null;

    @Override
    public String getName() {
        return "New York Times Event Search API extractor";
    }

    @Override
    public String getDescription() {
        return "Extractor performs an event search using The New York Times API and "
                + "transforms results to topics and associations.";
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
        try {
            currentURL = u.toExternalForm();

            log("Event search extraction with " + currentURL);

            String in = IObox.doUrl(u);

            System.out.println("New York Times API returned-------------------------\n" + in
                    + "\n----------------------------------------------------");
            
            JSONObject json = new JSONObject(in);
            if (json.get("num_results").toString().equals("0")){
                log("No results returned.");
            } else {
                parse(json, tm); 
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        currentURL = null;
        JSONObject json = new JSONObject(str);
        parse(json, tm);
        return true;
    }

    public void parse(JSONObject json, TopicMap tm) throws TopicMapException {
        if (json.has("results")) {
            try {
                JSONArray resultsArray = json.getJSONArray("results");
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject result = resultsArray.getJSONObject(i);
                    parseResult(result, tm);
                }
            } catch (JSONException ex) {
                log(ex);
                System.out.println(ex);
            }
        }
    }
       
    public void parseResult(JSONObject result, TopicMap tm) throws JSONException, TopicMapException {
      
        if (result.has("event_detail_url")) {
            String url = result.getString("event_detail_url");
            Topic eventTopic = tm.createTopic();
            eventTopic.addSubjectIdentifier(new Locator(url));
            eventTopic.addType(getEventTypeTopic(tm));

            if (result.has("event_name")) {
                String title = result.getString("event_name");
                if (title != null && title.length() > 0) {
                    eventTopic.setDisplayName(defaultLang, title);
                    eventTopic.setBaseName(title + " (NYT event)");
                }
            }

            if (result.has("web_description")) {
                String description = result.getString("web_description");
                if (description != null && description.length() > 0) {
                    description = HTMLEntitiesCoder.decode(XMLbox.naiveGetAsText(description).trim());
                    Topic descriptionTypeTopic = getDescriptionTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    eventTopic.setData(descriptionTypeTopic, langTopic, description);
                }
            }

            if (result.has("geocode_latitude")) {
                String latitude = result.getString("geocode_latitude");
                if (latitude != null && latitude.length() > 0) {
                    Topic latitudeTypeTopic = getLatitudeTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    eventTopic.setData(latitudeTypeTopic, langTopic, latitude);
                }
            }

            if (result.has("geocode_longitude")) {
                String longitude = result.getString("geocode_longitude");
                if (longitude != null && longitude.length() > 0) {
                    Topic longitudeTypeTopic = getLongitudeTypeTopic(tm);
                    Topic langTopic = getLangTopic(tm);
                    eventTopic.setData(longitudeTypeTopic, langTopic, longitude);
                }
            }

            if (result.has("venue_name")) {
                String venue = result.getString("venue_name");
                if (venue != null) {
                    venue = venue.trim();
                    if (venue.length() > 0) {
                        Topic venueTopic = getVenueTopic(venue, tm);
                        Topic eventTypeTopic = getEventTypeTopic(tm);
                        Topic venueTypeTopic = getVenueTypeTopic(tm);
                        if (venueTopic != null && eventTypeTopic != null && venueTypeTopic != null) {
                            Association a = tm.createAssociation(venueTypeTopic);
                            a.addPlayer(venueTopic, venueTypeTopic);
                            a.addPlayer(eventTopic, eventTypeTopic);
                        }
                    }

                }
            }

            if (result.has("category")) {
                String category = result.getString("category");
                if (category != null) {
                    category = category.trim();
                    if (category.length() > 0) {
                        Topic categoryTopic = getCategoryTopic(category, tm);
                        Topic eventTypeTopic = getEventTypeTopic(tm);
                        Topic categoryTypeTopic = getCategoryTypeTopic(tm);
                        if (categoryTopic != null && eventTypeTopic != null && categoryTypeTopic != null) {
                            Association a = tm.createAssociation(categoryTypeTopic);
                            a.addPlayer(categoryTopic, categoryTypeTopic);
                            a.addPlayer(eventTopic, eventTypeTopic);
                        }
                    }
                }
            }
            
            if (result.has("event_date_list")){
              String dates = "";
              JSONArray resultJSONArray = result.getJSONArray("event_date_list");
              Locale locale = new Locale("ENGLISH");
              SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd",locale);
              SimpleDateFormat output = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss '-0500'",locale);
              for( int i = 0; i < resultJSONArray.length(); i++ ){
                if(i != 0) {
                  dates += ",";
                }
                String dateString = resultJSONArray.getString(i);
                try{
                  Date date = input.parse(dateString);
                  dateString = output.format(date);
                } catch (Exception e){
                  
                }
                dates += dateString;
              }
              if (!dates.isEmpty()){
                Topic dateTypeTopic = getEventDateTypeTopic(tm);
                Topic langTopic = getLangTopic(tm);
                eventTopic.setData(dateTypeTopic, langTopic, dates);
              }
            } else if (result.has("recurring_start_date")) {
              Locale locale = new Locale("ENGLISH");
              SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",locale);
              SimpleDateFormat output = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss '-0500'",locale);
              String startDateString = result.getString("recurring_start_date");
              try{
                Date startDate = input.parse(startDateString);
                startDateString = output.format(startDate);
              } catch (Exception e){
                System.out.println("dateparseerror");
              }
              
              if(startDateString != null && startDateString.length() > 0) {
                  Topic dateStartTypeTopic = getStartDateTypeTopic(tm);
                  Topic langTopic = getLangTopic(tm);
                  eventTopic.setData(dateStartTypeTopic, langTopic, startDateString);
              }
              
              if (result.has("recur_days")){
                JSONArray recurDays = result.getJSONArray("recur_days");
                for (int i = 0; i < recurDays.length(); i++) {
                  String abbr = recurDays.getString(i);
                  
                  Topic recurDayTopic = getRecurringDayTopic(tm, abbr);
                  Topic eventTypeTopic = getEventTypeTopic(tm);
                  Topic recurDayTypeTopic = getRecurringDayTypeTopic(tm);
                  Topic dayTypeTopic = getWeekdayTypeTopic(tm);
                  if (recurDayTopic != null && eventTypeTopic != null && recurDayTypeTopic != null) {
                      Association a = tm.createAssociation(recurDayTypeTopic);
                      a.addPlayer(recurDayTopic, dayTypeTopic);
                      a.addPlayer(eventTopic, eventTypeTopic);
                  }
                }
                
              }
              
              if (result.has("recurring_end_date")) {
                String endDateString = result.getString("recurring_end_date");
                try {
                   Date endDate = input.parse(endDateString);
                   endDateString = output.format(endDate);
                } catch (Exception e){
                  System.out.println("dateparseerror");
                }
                if(endDateString != null && endDateString.length() > 0) {
                  Topic dateEndTypeTopic = getEndDateTypeTopic(tm);
                  Topic langTopic = getLangTopic(tm);
                  eventTopic.setData(dateEndTypeTopic, langTopic, endDateString);
                }
              }    
            }
        }
    }
}
