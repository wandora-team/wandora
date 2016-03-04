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
public abstract class AbstractGeoNamesParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {
    protected String lang = "en";
    protected int progress = 0;
    protected TopicMap tm;
    protected AbstractGeoNamesExtractor parent;

    protected String requestGeoObject = null;
    protected String masterSubject = null;


    public AbstractGeoNamesParser(TopicMap tm, AbstractGeoNamesExtractor parent, String lang){
        this.lang=lang;
        this.tm=tm;
        this.parent=parent;
    }


    private static final String TAG_GEONAMES="geonames";
    private static final String TAG_STATUS="status";
    private static final String TAG_GEONAME="geoname";
    private static final String TAG_NAME="name";
    private static final String TAG_LAT="lat";
    private static final String TAG_LNG="lng";
    private static final String TAG_GEONAMEID="geonameId";
    private static final String TAG_COUNTRYCODE="countryCode";
    private static final String TAG_COUNTRYNAME="countryName";
    private static final String TAG_FCL="fcl";
    private static final String TAG_FCODE="fcode";
    private static final String TAG_FCLNAME="fclName";
    private static final String TAG_FCODENAME="fcodeName";
    private static final String TAG_POPULATION="population";
    private static final String TAG_ALTERNATENAMES="alternateNames";
    private static final String TAG_ELEVATION="elevation";
    private static final String TAG_CONTINENTCODE="continentCode";
    private static final String TAG_ADMINCODE1="adminCode1";
    private static final String TAG_ADMINNAME1="adminName1";
    private static final String TAG_ADMINCODE2="adminCode2";
    private static final String TAG_ADMINNAME2="adminName2";
    private static final String TAG_ALTERNATENAME="alternateName";
    private static final String TAG_TIMEZONE="timezone";

    private static final int STATE_START=0;
    private static final int STATE_GEONAMES=2;
    private static final int STATE_GEONAMES_STATUS=200;
    private static final int STATE_GEONAME=3;
    private static final int STATE_GEONAME_NAME=4;
    private static final int STATE_GEONAME_LAT=5;
    private static final int STATE_GEONAME_LNG=6;
    private static final int STATE_GEONAME_GEONAMEID=7;
    private static final int STATE_GEONAME_COUNTRYCODE=8;
    private static final int STATE_GEONAME_COUNTRYNAME=9;
    private static final int STATE_GEONAME_FCL=10;
    private static final int STATE_GEONAME_FCODE=11;
    private static final int STATE_GEONAME_FCLNAME=12;
    private static final int STATE_GEONAME_FCODENAME=13;
    private static final int STATE_GEONAME_POPULATION=14;
    private static final int STATE_GEONAME_ALTERNATENAMES=15;
    private static final int STATE_GEONAME_ELEVATION=16;
    private static final int STATE_GEONAME_CONTINENTCODE=17;
    private static final int STATE_GEONAME_ADMINCODE1=18;
    private static final int STATE_GEONAME_ADMINNAME1=19;
    private static final int STATE_GEONAME_ADMINCODE2=20;
    private static final int STATE_GEONAME_ADMINNAME2=21;
    private static final int STATE_GEONAME_ALTERNATENAME=22;
    private static final int STATE_GEONAME_TIMEZONE=23;


    private int state=STATE_START;


    protected String data_name;
    protected String data_lat;
    protected String data_lng;
    protected String data_geonameid;
    protected String data_countrycode;
    protected String data_countryname;
    protected String data_fcl;
    protected String data_fcode;
    protected String data_fclname;
    protected String data_fcodename;
    protected String data_population;
    protected String data_alternatenames;
    protected String data_elevation;
    protected String data_continentcode;
    protected String data_admincode1;
    protected String data_adminname1;
    protected String data_admincode2;
    protected String data_adminname2;
    protected String data_alternatename;
    protected String data_alternatename_lang;
    protected HashMap<String,String> data_alternatename_all;
    protected String data_dstoffset;
    protected String data_gmtoffset;
    protected String data_timezone;





    protected Topic theGeoObject;
    protected String theGeoObjectSI;


    public void setMasterSubject(String subject) {
        masterSubject = subject;
    }

    public void setRequestGeoObject(String p) {
        requestGeoObject = p;
    }


    // ********** OVERRIDE THIS METHOD IN EXTENDING CLASSES! ********** 
    public abstract void handleGeoNameElement();



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
                else if(qName.equals(TAG_GEONAME))  {
                    data_name = "";
                    data_lat = "";
                    data_lng = "";
                    data_geonameid = "";
                    data_countrycode = "";
                    data_countryname = "";
                    data_fcl = "";
                    data_fcode = "";
                    data_fclname = "";
                    data_fcodename = "";
                    data_population = "";
                    data_alternatenames = "";
                    data_elevation = "";
                    data_continentcode = "";
                    data_admincode1 = "";
                    data_adminname1 = "";
                    data_admincode2 = "";
                    data_adminname2 = "";
                    data_alternatename = "";
                    data_alternatename_lang = "";
                    data_alternatename_all = new HashMap<String, String>();
                    data_dstoffset = "";
                    data_gmtoffset = "";
                    data_timezone = "";

                    state = STATE_GEONAME;
                }
                break;
            case STATE_GEONAME:
                if(qName.equals(TAG_NAME)) {
                    state = STATE_GEONAME_NAME;
                    data_name = "";
                }
                else if(qName.equals(TAG_LAT)) {
                    state = STATE_GEONAME_LAT;
                    data_lat = "";
                }
                else if(qName.equals(TAG_LNG)) {
                    state = STATE_GEONAME_LNG;
                    data_lng = "";
                }
                else if(qName.equals(TAG_GEONAMEID)) {
                    state = STATE_GEONAME_GEONAMEID;
                    data_geonameid = "";
                }
                else if(qName.equals(TAG_COUNTRYCODE)) {
                    state = STATE_GEONAME_COUNTRYCODE;
                    data_countrycode = "";
                }
                else if(qName.equals(TAG_COUNTRYNAME)) {
                    state = STATE_GEONAME_COUNTRYNAME;
                    data_countryname = "";
                }
                else if(qName.equals(TAG_FCL)) {
                    state = STATE_GEONAME_FCL;
                    data_fcl = "";
                }
                else if(qName.equals(TAG_FCODE)) {
                    state = STATE_GEONAME_FCODE;
                    data_fcode = "";
                }
                else if(qName.equals(TAG_FCLNAME)) {
                    state = STATE_GEONAME_FCLNAME;
                    data_fclname = "";
                }
                else if(qName.equals(TAG_FCODENAME)) {
                    state = STATE_GEONAME_FCODENAME;
                    data_fcodename = "";
                }
                else if(qName.equals(TAG_POPULATION)) {
                    state = STATE_GEONAME_POPULATION;
                    data_population = "";
                }
                else if(qName.equals(TAG_ALTERNATENAMES)) {
                    state = STATE_GEONAME_ALTERNATENAMES;
                    data_alternatenames = "";
                }
                else if(qName.equals(TAG_ELEVATION)) {
                    state = STATE_GEONAME_ELEVATION;
                    data_elevation = "";
                }
                else if(qName.equals(TAG_CONTINENTCODE)) {
                    state = STATE_GEONAME_CONTINENTCODE;
                    data_continentcode = "";
                }
                else if(qName.equals(TAG_ADMINCODE1)) {
                    state = STATE_GEONAME_ADMINCODE1;
                    data_admincode1 = "";
                }
                else if(qName.equals(TAG_ADMINNAME1)) {
                    state = STATE_GEONAME_ADMINNAME1;
                    data_adminname1 = "";
                }
                else if(qName.equals(TAG_ADMINCODE2)) {
                    state = STATE_GEONAME_ADMINCODE2;
                    data_admincode2 = "";
                }
                else if(qName.equals(TAG_ADMINNAME2)) {
                    state = STATE_GEONAME_ADMINNAME2;
                    data_adminname2 = "";
                }
                else if(qName.equals(TAG_ALTERNATENAME)) {
                    state = STATE_GEONAME_ALTERNATENAME;
                    data_alternatename_lang = atts.getValue("lang");
                    data_alternatename = "";
                }
                else if(qName.equals(TAG_TIMEZONE)) {
                    state = STATE_GEONAME_TIMEZONE;
                    data_timezone = "";
                    data_dstoffset = atts.getValue("dstOffset");
                    data_gmtoffset = atts.getValue("gmtOffset");
                }
                break;
        }
    }
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch(state) {
            case STATE_GEONAME: {
                if(TAG_GEONAME.equals(qName)) {
                    handleGeoNameElement();
                    state=STATE_GEONAMES;
                }
                break;
            }
            case STATE_GEONAMES_STATUS:
                if(TAG_STATUS.equals(qName)) {
                    state=STATE_GEONAMES;
                }
                break;
            case STATE_GEONAMES:
                state=STATE_START;
                break;
            case STATE_GEONAME_NAME: {
                if(TAG_NAME.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_LAT: {
                if(TAG_LAT.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_LNG: {
                if(TAG_LNG.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_GEONAMEID: {
                if(TAG_GEONAMEID.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_COUNTRYCODE: {
                if(TAG_COUNTRYCODE.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_COUNTRYNAME: {
                if(TAG_COUNTRYNAME.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_FCL: {
                if(TAG_FCL.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_FCODE: {
                if(TAG_FCODE.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_FCLNAME: {
                if(TAG_FCLNAME.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }

            case STATE_GEONAME_FCODENAME: {
                if(TAG_FCODENAME.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_POPULATION: {
                if(TAG_POPULATION.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_ALTERNATENAMES: {
                if(TAG_ALTERNATENAMES.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_ELEVATION: {
                if(TAG_ELEVATION.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_CONTINENTCODE: {
                if(TAG_CONTINENTCODE.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_ADMINCODE1: {
                if(TAG_ADMINCODE1.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_ADMINNAME1: {
                if(TAG_ADMINNAME1.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_ADMINCODE2: {
                if(TAG_ADMINCODE2.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_ADMINNAME2: {
                if(TAG_ADMINNAME2.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
            case STATE_GEONAME_ALTERNATENAME: {
                if(TAG_ALTERNATENAME.equals(qName)) {
                    if(data_alternatename_lang != null && data_alternatename_lang.length() > 0 &&
                       data_alternatename != null && data_alternatename.length() > 0) {
                           data_alternatename_all.put(data_alternatename_lang, data_alternatename);
                    }
                    state=STATE_GEONAME;
                }
                break;
            }
            case STATE_GEONAME_TIMEZONE: {
                if(TAG_TIMEZONE.equals(qName)) 
                    state=STATE_GEONAME;
                break;
            }
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        switch(state){
            case STATE_GEONAME_NAME:
                data_name+=new String(ch,start,length);
                break;
            case STATE_GEONAME_LAT:
                data_lat+=new String(ch,start,length);
                break;
            case STATE_GEONAME_LNG:
                data_lng+=new String(ch,start,length);
                break;
            case STATE_GEONAME_GEONAMEID:
                data_geonameid+=new String(ch,start,length);
                break;
            case STATE_GEONAME_COUNTRYCODE:
                data_countrycode+=new String(ch,start,length);
                break;
            case STATE_GEONAME_COUNTRYNAME:
                data_countryname+=new String(ch,start,length);
                break;
            case STATE_GEONAME_FCL:
                data_fcl+=new String(ch,start,length);
                break;
            case STATE_GEONAME_FCODE:
                data_fcode+=new String(ch,start,length);
                break;
            case STATE_GEONAME_FCLNAME:
                data_fclname+=new String(ch,start,length);
                break;
            case STATE_GEONAME_FCODENAME:
                data_fcodename+=new String(ch,start,length);
                break;
            case STATE_GEONAME_POPULATION:
                data_population+=new String(ch,start,length);
                break;
            case STATE_GEONAME_ALTERNATENAMES:
                data_alternatenames+=new String(ch,start,length);
                break;
            case STATE_GEONAME_ELEVATION:
                data_elevation+=new String(ch,start,length);
                break;
            case STATE_GEONAME_CONTINENTCODE:
                data_continentcode+=new String(ch,start,length);
                break;
            case STATE_GEONAME_ADMINCODE1:
                data_admincode1+=new String(ch,start,length);
                break;
            case STATE_GEONAME_ADMINNAME1:
                data_adminname1+=new String(ch,start,length);
                break;
            case STATE_GEONAME_ADMINCODE2:
                data_admincode2+=new String(ch,start,length);
                break;
            case STATE_GEONAME_ADMINNAME2:
                data_adminname2+=new String(ch,start,length);
                break;
            case STATE_GEONAME_ALTERNATENAME:
                data_alternatename+=new String(ch,start,length);
                break;
            case STATE_GEONAME_TIMEZONE:
                data_timezone+=new String(ch,start,length);
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
    
    

