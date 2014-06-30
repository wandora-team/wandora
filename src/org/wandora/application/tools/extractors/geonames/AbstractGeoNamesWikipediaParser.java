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
public abstract class AbstractGeoNamesWikipediaParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {
    protected String lang = "en";
    protected int progress = 0;
    protected TopicMap tm;
    protected AbstractGeoNamesExtractor parent;
    protected String requestGeoObject = null;


    public AbstractGeoNamesWikipediaParser(TopicMap tm, AbstractGeoNamesExtractor parent, String lang){
        this.lang=lang;
        this.tm=tm;
        this.parent=parent;
    }


    private static final String TAG_GEONAMES="geonames";
    private static final String TAG_STATUS="status";
    private static final String TAG_ENTRY="entry";
    private static final String TAG_LANG="lang";
    private static final String TAG_TITLE="title";
    private static final String TAG_SUMMARY="summary";
    private static final String TAG_FEATURE="feature";
    private static final String TAG_COUNTRYCODE="countryCode";
    private static final String TAG_LAT="lat";
    private static final String TAG_LNG="lng";
    private static final String TAG_POPULATION="population";
    private static final String TAG_ELEVATION="elevation";
    private static final String TAG_WIKIPEDIAURL="wikipediaUrl";
    private static final String TAG_CLOUDS="clouds";
    private static final String TAG_THUMBNAILIMG="thumbnailImg";

    private static final int STATE_START=0;
    private static final int STATE_GEONAMES=2;
    private static final int STATE_GEONAMES_STATUS=200;
    private static final int STATE_ENTRY=3;
    private static final int STATE_ENTRY_LANG=4;
    private static final int STATE_ENTRY_LAT=5;
    private static final int STATE_ENTRY_LNG=6;
    private static final int STATE_ENTRY_TITLE=7;
    private static final int STATE_ENTRY_SUMMARY=8;
    private static final int STATE_ENTRY_FEATURE=9;
    private static final int STATE_ENTRY_COUNTRYCODE=10;
    private static final int STATE_ENTRY_POPULATION=11;
    private static final int STATE_ENTRY_ELEVATION=12;
    private static final int STATE_ENTRY_WIKIPEDIAURL=13;
    private static final int STATE_ENTRY_THUMBNAILIMG=14;


    private int state=STATE_START;


    protected String data_lang;
    protected String data_lat;
    protected String data_lng;
    protected String data_title;
    protected String data_summary;
    protected String data_feature;
    protected String data_countrycode;
    protected String data_population;
    protected String data_elevation;
    protected String data_wikipediaurl;
    protected String data_thumbnailimg;




    public void setRequestGeoObject(String p) {
        requestGeoObject = p;
    }


    // ********** OVERRIDE THIS METHOD IN EXTENDING CLASSSES! ********** 
    public abstract void handleEntryElement();



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
                else if(qName.equals(TAG_ENTRY)) {
                    data_lang = "";
                    data_lat = "";
                    data_lng = "";
                    data_title = "";
                    data_summary = "";
                    data_feature = "";
                    data_countrycode = "";
                    data_population = "";
                    data_elevation = "";
                    data_wikipediaurl = "";
                    data_thumbnailimg = "";

                    state = STATE_ENTRY;
                }
                break;
            case STATE_ENTRY:
                if(qName.equals(TAG_LANG)) {
                    state = STATE_ENTRY_LANG;
                    data_lang = "";
                }
                else if(qName.equals(TAG_LAT)) {
                    state = STATE_ENTRY_LAT;
                    data_lat = "";
                }
                else if(qName.equals(TAG_LNG)) {
                    state = STATE_ENTRY_LNG;
                    data_lng = "";
                }
                else if(qName.equals(TAG_TITLE)) {
                    state = STATE_ENTRY_TITLE;
                    data_title = "";
                }
                else if(qName.equals(TAG_SUMMARY)) {
                    state = STATE_ENTRY_SUMMARY;
                    data_summary = "";
                }
                else if(qName.equals(TAG_FEATURE)) {
                    state = STATE_ENTRY_FEATURE;
                    data_feature = "";
                }
                else if(qName.equals(TAG_COUNTRYCODE)) {
                    state = STATE_ENTRY_COUNTRYCODE;
                    data_countrycode = "";
                }
                else if(qName.equals(TAG_POPULATION)) {
                    state = STATE_ENTRY_POPULATION;
                    data_population = "";
                }
                else if(qName.equals(TAG_ELEVATION)) {
                    state = STATE_ENTRY_ELEVATION;
                    data_elevation= "";
                }
                else if(qName.equals(TAG_WIKIPEDIAURL)) {
                    state = STATE_ENTRY_WIKIPEDIAURL;
                    data_wikipediaurl = "";
                }
                else if(qName.equals(TAG_THUMBNAILIMG)) {
                    state = STATE_ENTRY_THUMBNAILIMG;
                    data_thumbnailimg = "";
                }
                break;
        }
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch(state) {
            case STATE_ENTRY: {
                if(TAG_ENTRY.equals(qName)) {
                    handleEntryElement();
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
            case STATE_ENTRY_LANG: {
                if(TAG_LANG.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
            case STATE_ENTRY_LAT: {
                if(TAG_LAT.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
            case STATE_ENTRY_LNG: {
                if(TAG_LNG.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
            case STATE_ENTRY_TITLE: {
                if(TAG_TITLE.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
            case STATE_ENTRY_SUMMARY: {
                if(TAG_SUMMARY.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
            case STATE_ENTRY_FEATURE: {
                if(TAG_FEATURE.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
            case STATE_ENTRY_COUNTRYCODE: {
                if(TAG_COUNTRYCODE.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
            case STATE_ENTRY_POPULATION: {
                if(TAG_POPULATION.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
            case STATE_ENTRY_ELEVATION: {
                if(TAG_ELEVATION.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }

            case STATE_ENTRY_WIKIPEDIAURL: {
                if(TAG_WIKIPEDIAURL.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
            case STATE_ENTRY_THUMBNAILIMG: {
                if(TAG_THUMBNAILIMG.equals(qName)) 
                    state=STATE_ENTRY;
                break;
            }
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        switch(state){
            case STATE_ENTRY_LANG:
                data_lang+=new String(ch,start,length);
                break;
            case STATE_ENTRY_LAT:
                data_lat+=new String(ch,start,length);
                break;
            case STATE_ENTRY_LNG:
                data_lng+=new String(ch,start,length);
                break;
            case STATE_ENTRY_TITLE:
                data_title+=new String(ch,start,length);
                break;
            case STATE_ENTRY_SUMMARY:
                data_summary+=new String(ch,start,length);
                break;
            case STATE_ENTRY_FEATURE:
                data_feature+=new String(ch,start,length);
                break;
            case STATE_ENTRY_COUNTRYCODE:
                data_countrycode+=new String(ch,start,length);
                break;
            case STATE_ENTRY_POPULATION:
                data_population+=new String(ch,start,length);
                break;
            case STATE_ENTRY_ELEVATION:
                data_elevation+=new String(ch,start,length);
                break;
            case STATE_ENTRY_WIKIPEDIAURL:
                data_wikipediaurl+=new String(ch,start,length);
                break;
            case STATE_ENTRY_THUMBNAILIMG:
                data_thumbnailimg+=new String(ch,start,length);
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
    
    

