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


package org.wandora.application.tools.extractors.geonames;

import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;
import java.net.*;
import java.util.regex.*;



/**
 *
 * @author akivela
 */
public abstract class AbstractGeoNamesWeatherParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {
    protected String lang = "en";
    protected int progress = 0;
    protected TopicMap tm;
    protected AbstractGeoNamesExtractor parent;
    protected String requestGeoObject = null;


    public AbstractGeoNamesWeatherParser(TopicMap tm, AbstractGeoNamesExtractor parent, String lang){
        this.lang=lang;
        this.tm=tm;
        this.parent=parent;
    }


    private static final String TAG_GEONAMES="geonames";
    private static final String TAG_STATUS="status";
    private static final String TAG_OBSERVATION="observation";
    private static final String TAG_OBSERVATIONTIME="observationTime";
    private static final String TAG_STATIONNAME="stationName";
    private static final String TAG_ICAO="ICAO";
    private static final String TAG_COUNTRYCODE="countryCode";
    private static final String TAG_ELEVATION="elevation";
    private static final String TAG_LAT="lat";
    private static final String TAG_LNG="lng";
    private static final String TAG_TEMPERATURE="temperature";
    private static final String TAG_DEWPOINT="dewPoint";
    private static final String TAG_HUMIDITY="humidity";
    private static final String TAG_CLOUDS="clouds";
    private static final String TAG_WEATHERCONDITION="weatherCondition";
    private static final String TAG_HECTOPASCALTIMETER="hectoPascAltimeter";
    private static final String TAG_WINDDIRECTION="windDirection";
    private static final String TAG_WINDSPEED="windSpeed";

    private static final int STATE_START=0;
    private static final int STATE_GEONAMES=2;
    private static final int STATE_GEONAMES_STATUS=200;
    private static final int STATE_OBSERVATION=3;
    private static final int STATE_OBSERVATION_OBSERVATION=4;
    private static final int STATE_OBSERVATION_LAT=5;
    private static final int STATE_OBSERVATION_LNG=6;
    private static final int STATE_OBSERVATION_OBSERVATIONTIME=7;
    private static final int STATE_OBSERVATION_STATIONNAME=8;
    private static final int STATE_OBSERVATION_ICAO=9;
    private static final int STATE_OBSERVATION_COUNTRYCODE=10;
    private static final int STATE_OBSERVATION_ELEVATION=11;
    private static final int STATE_OBSERVATION_TEMPERATURE=12;
    private static final int STATE_OBSERVATION_DEWPOINT=13;
    private static final int STATE_OBSERVATION_HUMIDITY=14;
    private static final int STATE_OBSERVATION_CLOUDS=15;
    private static final int STATE_OBSERVATION_WEATHERCONDITION=16;
    private static final int STATE_OBSERVATION_HECTOPASCALTIMETER=17;
    private static final int STATE_OBSERVATION_WINDDIRECTION=18;
    private static final int STATE_OBSERVATION_WINDSPEED=19;


    private int state=STATE_START;


    protected String data_observation;
    protected String data_lat;
    protected String data_lng;
    protected String data_observationtime;
    protected String data_stationname;
    protected String data_icao;
    protected String data_countrycode;
    protected String data_elevation;
    protected String data_temperature;
    protected String data_dewpoint;
    protected String data_humidity;
    protected String data_clouds;
    protected String data_weathercondition;
    protected String data_hectopascaltimeter;
    protected String data_winddirection;
    protected String data_windspeed;




    public void setRequestGeoObject(String p) {
        requestGeoObject = p;
    }


    // ********** OVERRIDE THIS METHOD IN EXTENDING CLASSSES! ********** 
    public abstract void handleObservationElement();



    public void startDocument() throws SAXException {
        progress = 0;
    }
    public void endDocument() throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if(parent != null && parent.forceStop()){
            throw new SAXException("User interrupt");
        }
        switch(state){
            case STATE_START:
                if(qName.equals(TAG_GEONAMES)) {
                    state = STATE_GEONAMES;
                    break;
                }
            case STATE_GEONAMES:
                if(qName.equals(TAG_STATUS)) {
                    state = STATE_GEONAMES_STATUS;
                    String msg = atts.getValue("message");
                    String val = atts.getValue("value");
                    if(parent != null) {
                        parent.log("GeoNames web service says: "+msg+" ("+val+")");
                    }
                    else {
                        System.out.println("GeoNames web service says: "+msg+" ("+val+")");
                    }
                }
                else if(qName.equals(TAG_OBSERVATION)) {
                    data_observation = "";
                    data_lat = "";
                    data_lng = "";
                    data_observationtime = "";
                    data_stationname = "";
                    data_icao = "";
                    data_countrycode = "";
                    data_elevation = "";
                    data_temperature = "";
                    data_dewpoint = "";
                    data_humidity = "";
                    data_clouds = "";
                    data_weathercondition = "";
                    data_hectopascaltimeter = "";
                    data_winddirection = "";
                    data_windspeed = "";

                    state = STATE_OBSERVATION;
                }
                break;
            case STATE_OBSERVATION:
                if(qName.equals(TAG_OBSERVATION)) {
                    state = STATE_OBSERVATION_OBSERVATION;
                    data_observation = "";
                }
                else if(qName.equals(TAG_LAT)) {
                    state = STATE_OBSERVATION_LAT;
                    data_lat = "";
                }
                else if(qName.equals(TAG_LNG)) {
                    state = STATE_OBSERVATION_LNG;
                    data_lng = "";
                }
                else if(qName.equals(TAG_OBSERVATIONTIME)) {
                    state = STATE_OBSERVATION_OBSERVATIONTIME;
                    data_observationtime = "";
                }
                else if(qName.equals(TAG_COUNTRYCODE)) {
                    state = STATE_OBSERVATION_COUNTRYCODE;
                    data_countrycode = "";
                }
                else if(qName.equals(TAG_STATIONNAME)) {
                    state = STATE_OBSERVATION_STATIONNAME;
                    data_stationname = "";
                }
                else if(qName.equals(TAG_ICAO)) {
                    state = STATE_OBSERVATION_ICAO;
                    data_icao = "";
                }
                else if(qName.equals(TAG_ELEVATION)) {
                    state = STATE_OBSERVATION_ELEVATION;
                    data_elevation = "";
                }
                else if(qName.equals(TAG_TEMPERATURE)) {
                    state = STATE_OBSERVATION_TEMPERATURE;
                    data_temperature = "";
                }
                else if(qName.equals(TAG_DEWPOINT)) {
                    state = STATE_OBSERVATION_DEWPOINT;
                    data_dewpoint = "";
                }
                else if(qName.equals(TAG_HUMIDITY)) {
                    state = STATE_OBSERVATION_HUMIDITY;
                    data_humidity = "";
                }
                else if(qName.equals(TAG_CLOUDS)) {
                    state = STATE_OBSERVATION_CLOUDS;
                    data_clouds = "";
                }
                else if(qName.equals(TAG_WEATHERCONDITION)) {
                    state = STATE_OBSERVATION_WEATHERCONDITION;
                    data_weathercondition = "";
                }
                else if(qName.equals(TAG_HECTOPASCALTIMETER)) {
                    state = STATE_OBSERVATION_HECTOPASCALTIMETER;
                    data_hectopascaltimeter = "";
                }
                else if(qName.equals(TAG_WINDDIRECTION)) {
                    state = STATE_OBSERVATION_WINDDIRECTION;
                    data_winddirection = "";
                }
                else if(qName.equals(TAG_WINDSPEED)) {
                    state = STATE_OBSERVATION_WINDSPEED;
                    data_windspeed = "";
                }
                break;
        }
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch(state) {
            case STATE_OBSERVATION: {
                if(TAG_OBSERVATION.equals(qName)) {
                    handleObservationElement();
                    state=STATE_GEONAMES;
                }
                break;
            }
            case STATE_GEONAMES_STATUS: {
                if(qName.equals(TAG_STATUS)) {
                    state = STATE_GEONAMES;
                }
                break;
            }
            case STATE_GEONAMES:
                state=STATE_START;
                break;
            case STATE_OBSERVATION_OBSERVATION: {
                if(TAG_OBSERVATION.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_LAT: {
                if(TAG_LAT.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_LNG: {
                if(TAG_LNG.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_OBSERVATIONTIME: {
                if(TAG_OBSERVATIONTIME.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_COUNTRYCODE: {
                if(TAG_COUNTRYCODE.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_STATIONNAME: {
                if(TAG_STATIONNAME.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_ICAO: {
                if(TAG_ICAO.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_ELEVATION: {
                if(TAG_ELEVATION.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_TEMPERATURE: {
                if(TAG_TEMPERATURE.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }

            case STATE_OBSERVATION_DEWPOINT: {
                if(TAG_DEWPOINT.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_HUMIDITY: {
                if(TAG_HUMIDITY.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_CLOUDS: {
                if(TAG_CLOUDS.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_WEATHERCONDITION: {
                if(TAG_WEATHERCONDITION.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_HECTOPASCALTIMETER: {
                if(TAG_HECTOPASCALTIMETER.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_WINDDIRECTION: {
                if(TAG_WINDDIRECTION.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }
            case STATE_OBSERVATION_WINDSPEED: {
                if(TAG_WINDSPEED.equals(qName)) 
                    state=STATE_OBSERVATION;
                break;
            }

        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        switch(state){
            case STATE_OBSERVATION_OBSERVATION:
                data_observation+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_LAT:
                data_lat+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_LNG:
                data_lng+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_OBSERVATIONTIME:
                data_observationtime+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_COUNTRYCODE:
                data_countrycode+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_STATIONNAME:
                data_stationname+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_ICAO:
                data_icao+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_ELEVATION:
                data_elevation+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_TEMPERATURE:
                data_temperature+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_DEWPOINT:
                data_dewpoint+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_HUMIDITY:
                data_humidity+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_CLOUDS:
                data_clouds+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_WEATHERCONDITION:
                data_weathercondition+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_HECTOPASCALTIMETER:
                data_hectopascaltimeter+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_WINDDIRECTION:
                data_winddirection+=new String(ch,start,length);
                break;
            case STATE_OBSERVATION_WINDSPEED:
                data_windspeed+=new String(ch,start,length);
                break;
        }
    }

    public void warning(SAXParseException exception) throws SAXException {
    }

    public void error(SAXParseException exception) throws SAXException {
        parent.log("Error parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        parent.log("Fatal error parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
    }


    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
    public void processingInstruction(String target, String data) throws SAXException {}
    public void startPrefixMapping(String prefix, String uri) throws SAXException {}
    public void endPrefixMapping(String prefix) throws SAXException {}
    public void setDocumentLocator(org.xml.sax.Locator locator) {}
    public void skippedEntity(String name) throws SAXException {}

    
    public boolean isValid(String str) {
        if(str != null && str.length() > 0) return true;
        else return false;
    }
        
    
}
    
    

