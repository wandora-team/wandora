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
 * 
 * 
 * GeoNamesCities.java
 * 
 * 
 */

package org.wandora.application.tools.extractors.geonames;

import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;


/**
 * See http://ws.geonames.org/cities?north=44.1&south=-9.9&east=-22.4&west=55.2
 *
 * @author akivela
 */
public class GeoNamesCities extends AbstractGeoNamesExtractor {

	private static final long serialVersionUID = 1L;

	public String dataLang = "en";
    
    
    @Override
    public String getName() {
        return "GeoNames cities extractor";
    }
    
    
    @Override
    public String getDescription(){
        return "Get cities within given bounding box and convert city data to a topic map.";
    }
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        CitiesParser parserHandler = new CitiesParser(topicMap,this,dataLang);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        if(parserHandler.progress == 0) log("No cities found!");
        else if(parserHandler.progress == 1) log("One city found!");
        else log("Total " + parserHandler.progress + " cities found!");
        return true;
    }
    



    // -------------------------------------------------------------------------
    // --- GEONAMES XML FEED PARSER --------------------------------------------
    // -------------------------------------------------------------------------


    private static class CitiesParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        
        
        public CitiesParser(TopicMap tm, GeoNamesCities parent, String lang){
            this.lang=lang;
            this.tm=tm;
            this.parent=parent;
        }
        
        private String lang = "en";
        public int progress=0;
        private TopicMap tm;
        private GeoNamesCities parent;
        
        public static final String TAG_GEONAMES="geonames";
        public static final String TAG_STATUS="status";
        public static final String TAG_GEONAME="geoname";
        public static final String TAG_NAME="name";
        public static final String TAG_LAT="lat";
        public static final String TAG_LNG="lng";
        public static final String TAG_GEONAMEID="geonameId";
        public static final String TAG_COUNTRYCODE="countryCode";
        public static final String TAG_COUNTRYNAME="countryName";
        public static final String TAG_FCL="fcl";
        public static final String TAG_FCODE="fcode";
        
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

        private int state=STATE_START;

        
        private String data_name;
        private String data_lat;
        private String data_lng;
        private String data_geonameid;
        private String data_countrycode;
        private String data_countryname;
        private String data_fcl;
        private String data_fcode;


        private Topic theCity;
        
        
        
        public void startDocument() throws SAXException {
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if(parent.forceStop()){
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
                    else if(qName.equals(TAG_GEONAME)) {
                        data_name = "";
                        data_lat = "";
                        data_lng = "";
                        data_geonameid = "";
                        data_countrycode = "";
                        data_countryname = "";
                        data_fcl = "";
                        data_fcode = "";
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
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_GEONAME: {
                    if(TAG_GEONAME.equals(qName)) {
                        if(data_geonameid.length() > 0 || data_name.length() > 0) {
                            try {
                                theCity=getCityTopic(tm, data_geonameid, data_name, lang);
                                parent.setProgress(progress++);

                                try {
                                    if(isValid(data_fcode)) {
                                        Topic fcodeTopic = getFCLTopic(tm, data_fcode, null);
                                        theCity.addType(fcodeTopic);
                                    }
                                    if(isValid(data_fcl)) {
                                        Topic fclTopic = getFCLTopic(tm, data_fcl, null);
                                        theCity.addType(fclTopic);
                                    }
                                    if(isValid(data_countrycode) && isValid(data_countryname)) {
                                        Topic countryTopic=getCountryTopic(tm, data_countrycode, data_countryname, lang);
                                        makeGeoCountry(theCity, countryTopic, tm);
                                    }
                                    if(isValid(data_lat) && isValid(data_lng)) {
                                         makeLatLong(data_lat, data_lng, theCity, tm);
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
                        state=STATE_GEONAMES;
                    }
                    break;
                }
                case STATE_GEONAMES_STATUS:
                    if(qName.equals(TAG_STATUS)) {
                        state = STATE_GEONAMES;
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
        
    }
    
    
    
    public static boolean isValid(String str) {
        if(str != null && str.length() > 0) return true;
        else return false;
    }
        
    
}
