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
 * 
 * GeoNamesNearByWeather.java
 * 
 */


package org.wandora.application.tools.extractors.geonames;

import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;
import java.net.*;
import java.util.regex.*;


/**
 *
 * @author akivela
 */
public class GeoNamesNearByWeather extends AbstractGeoNamesExtractor {
    public String dataLang = "en";
    public String requestGeoObject = null;
    
    
    @Override
    public String getName() {
        return "GeoNames find near by extractor";
    }
    
    
    @Override
    public String getDescription(){
        return "Extractor finds near by geo locations from GeoNames web api and converts found results to a topic map.";
    }
    
    public void setRequestGeoObject(String p) {
        this.requestGeoObject = p;
    }
    

    @Override
    public synchronized void extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        try {
            String str = url.toExternalForm();
            Pattern p = Pattern.compile("geonameId=(\\w+)");
            Matcher m = p.matcher(str);
            if(m.find()) {
                String geoid = m.group(1);
                if(geoid != null && geoid.length() > 0) {
                    setRequestGeoObject(geoid);
                }
            }
        }
        catch(Exception e) {}
        super.extractTopicsFrom(url, topicMap);
    }

    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        NearByWeatherParser parserHandler = new NearByWeatherParser(topicMap,this,dataLang);
        parserHandler.setRequestGeoObject(requestGeoObject);
        
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        if(parserHandler.progress == 0) log("No near by weather observations found!");
        else if(parserHandler.progress == 1) log("One near by weather observation found!");
        else log("Total " + parserHandler.progress + " near by weather observations found!");
        requestGeoObject = null; // FORCE NULL AS THIS OBJECT IS REUSED.
        return true;
    }
    

    
    
    
    
    // -------------------------------------------------------------------------
    // --- GEONAMES XML FEED PARSER --------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    private class NearByWeatherParser extends AbstractGeoNamesWeatherParser {

        Topic theObservation = null;

        
        public  NearByWeatherParser(TopicMap tm, AbstractGeoNamesExtractor parent, String lang){
            super(tm, parent, lang);
        }
        
        
        
        public void handleObservationElement() {
            if(data_observation != null && data_observation.length() > 0) {
                try {
                    theObservation=getWeatherObservationTopic(tm, data_observation, lang);
                    parent.setProgress(progress++);

                    try {
                        if(isValid(data_observationtime)) {
                            Topic observationTimeTypeTopic = getWeatherObservationTimeTypeTopic(tm);
                            theObservation.setData(observationTimeTypeTopic, TMBox.getLangTopic(theObservation, lang), data_observationtime);
                        }
                        if(isValid(data_stationname)) {
                            Topic stationTopic = getWeatherStationTopic(tm, data_stationname, lang);
                            makeWeatherStation(theObservation, stationTopic, tm);
                            if(data_countrycode != null && data_countrycode.length() > 0) {
                                Topic countryTopic=getCountryTopic(tm, data_countrycode, lang);
                                makeGeoCountry(stationTopic, countryTopic, tm);
                            }
                            if(isValid(data_lat) &&
                               isValid(data_lng)) {
                                 makeLatLong(data_lat, data_lng, stationTopic, tm);
                            }
                            if(isValid(data_icao)) {
                                stationTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(WEATHER_ICAO_SI+"/"+data_icao));
                            }
                            if(isValid(data_elevation)) {
                                Topic elevationTopic=getElevationTopic(tm, data_elevation, lang);
                                makeGeoElevation(stationTopic, elevationTopic, tm);
                            }
                        }


                        if(isValid(data_temperature )) {
                            Topic temperatureTypeTopic = getWeatherTemperatureTypeTopic(tm);
                            theObservation.setData(temperatureTypeTopic, TMBox.getLangTopic(theObservation, lang), data_temperature);
                        }
                        if(isValid(data_dewpoint)) {
                            Topic dewPointTypeTopic = getWeatherDewPointTypeTopic(tm);
                            theObservation.setData(dewPointTypeTopic, TMBox.getLangTopic(theObservation, lang), data_dewpoint);
                        }
                        if(isValid(data_humidity)) {
                            Topic humidityTypeTopic = getWeatherHumidityTypeTopic(tm);
                            theObservation.setData(humidityTypeTopic, TMBox.getLangTopic(theObservation, lang), data_humidity);
                        }
                        if(isValid(data_clouds)) {
                            Topic cloudsTopic = getWeatherCloudsTopic(tm, data_clouds, lang);
                            makeWeatherClouds(theObservation, cloudsTopic, tm);
                        }
                        if(isValid(data_weathercondition)) {
                            Topic weatherConditionTypeTopic = getWeatherConditionTypeTopic(tm);
                            theObservation.setData(weatherConditionTypeTopic, TMBox.getLangTopic(theObservation, lang), data_weathercondition);
                        }
                        if(isValid(data_hectopascaltimeter)) {
                            Topic hectoPascAltimeterTypeTopic = getWeatherHectoPascAltimeterTypeTopic(tm);
                            theObservation.setData(hectoPascAltimeterTypeTopic, TMBox.getLangTopic(theObservation, lang), data_hectopascaltimeter);
                        }
                        if(isValid(data_winddirection)) {
                            Topic windDirectionTypeTopic = getWeatherWindDirectionTypeTopic(tm);
                            theObservation.setData(windDirectionTypeTopic, TMBox.getLangTopic(theObservation, lang), data_winddirection);
                        }
                        if(isValid(data_windspeed)) {
                            Topic windSpeedTypeTopic = getWeatherWindSpeedTypeTopic(tm);
                            theObservation.setData(windSpeedTypeTopic, TMBox.getLangTopic(theObservation, lang), data_windspeed);
                        }
                    }
                    catch(Exception e) {
                        parent.log(e);
                    }
                }
                catch(TopicMapException tme){
                    parent.log(tme);
                }
            }
        }
    }
    

}
