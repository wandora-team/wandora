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
 * 
 * GeoExtractor.java
 *
 * Created on 13. joulukuuta 2007, 12:00
 *
 */

package org.wandora.application.tools.extractors.microformats;


import org.wandora.utils.IObox;
import java.net.*;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.w3c.tidy.*;
import javax.swing.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.gui.*;



/**
 * This class implements a Wandora extractor for geo-location microformat
 * described in http://microformats.org/wiki/geo
 *
 * @author akivela
 */

public class GeoExtractor extends AbstractExtractor implements WandoraTool {


	private static final long serialVersionUID = 1L;
	
	
	/** Creates a new instance of GeoExtractor */
    public GeoExtractor() {
    }
    
    

    public String getName() {
        return "Geo microformat extractor";
    }
    
    
    public String getDescription() {
        return "Converts Geo Microformat HTML snippets to Topic Map associations.";
    }
    
    
    
    @Override
    public boolean useTempTopicMap(){
        return false;
    }

    
    public static final String[] contentTypes=new String[] { "text/html" };
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_microformat.png");
    }
    
    

    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(url.openStream(),topicMap);
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        Tidy tidy = null;
        String tidyXML = null;
        
        try {
            Properties tidyProps = new Properties();
            tidyProps.put("trim-empty-elements", "no");
            
            tidy = new Tidy();
            tidy.setConfigurationFromProps(tidyProps);
            tidy.setXmlOut(true);
            tidy.setXmlPi(true);
            tidy.setTidyMark(false);

            ByteArrayOutputStream tidyOutput = null;
            tidyOutput = new ByteArrayOutputStream();       
            tidy.parse(in, tidyOutput);
            tidyXML = tidyOutput.toString();
        }
        catch(Error er) {
            log("Unable to preprocess HTML with JTidy!");
            log(er);
        }
        catch(Exception e) {
            log("Unable to preprocess HTML with JTidy!");
            log(e);
        }
        if(tidyXML == null) {
            log("Trying to read HTML without preprocessing!");
            tidyXML = IObox.loadFile(new InputStreamReader(in));
        }
        
        //tidyXML = tidyXML.replace("&", "&amp;");
        tidyXML = tidyXML.replace("&amp;deg;", "&#0176;");
        
        /*
        System.out.println("------");
        System.out.println(tidyXML);
        System.out.println("------");
        */
          
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        GeoParser parserHandler = new GeoParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(new ByteArrayInputStream(tidyXML.getBytes())));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " geo coordinates found!");
        
        return true;
    }
    
    
    
    
    
    
    public static class GeoParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {


        private TopicMap tm = null;
        private GeoExtractor parent = null;

        public static final String SI_PREFIX = "http://wandora.org/si/geo/";

        public int progress = 0;

        private static final int STATE_START=1111;
        private static final int STATE_LATITUDE=1;
        private static final int STATE_LONGITUDE=2;
        private static final int STATE_OTHER=9999;

        private int state=STATE_START;

        private String latitude;
        private String longitude;
        private String location;
        private Association association;


        // -------------------------------------------------------------------------
        public GeoParser(TopicMap tm, GeoExtractor parent ) {
            this.tm=tm;
            this.parent=parent;

            latitude = null;
            longitude = null;
            location = null;
            association = null;

        }


        // -------------------------------------------------------------------------


        
        public void startDocument() throws SAXException {
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            //System.out.println("qname=="+ qName);
            //System.out.println("atts=="+ atts);
            
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            if("span".equalsIgnoreCase(qName)) {
                String attribute = atts.getValue("class");
                if(attribute != null) {
                    if("latitude".equalsIgnoreCase(attribute.toString())) {
                        state = STATE_LATITUDE;
                        latitude = null;
                    }
                    else if("longitude".equalsIgnoreCase(attribute.toString())) {
                        state = STATE_LONGITUDE;
                        longitude = null;
                    }
                }
            }
            else if("abbr".equalsIgnoreCase(qName)) {
                String clas = atts.getValue("class");
                String title = atts.getValue("title");
                if(clas != null) {
                    if("latitude".equalsIgnoreCase(clas)) {
                        latitude = title;
                    }
                    else if("longitude".equalsIgnoreCase(clas)) {
                        longitude = title;
                    }
                    else if("geo".equalsIgnoreCase(clas)) {
                        if(title != null) {
                            String[] coords = title.split(";");
                            if(coords.length > 0) {
                                latitude = coords[0];
                            }
                            if(coords.length > 1) {
                                longitude = coords[1];
                            }
                        }
                    }
                }
            }
        }


        public void endElement(String uri, String localName, String qName) throws SAXException {
            //System.out.println("handleEndTag: " + qName);
            if("span".equalsIgnoreCase(qName)) {
                processCoordinates();
                state = STATE_OTHER;
            }
            if("abbr".equalsIgnoreCase(qName)) {
                processCoordinates();
            }
        }



        public void characters(char[] data, int start, int length) throws SAXException {
            switch(state) {
                case STATE_LONGITUDE: {
                    if(longitude == null) longitude = "";
                    longitude = longitude + new String(data,start,length);
                    break;
                }
                case STATE_LATITUDE: {
                    if(latitude == null) latitude = "";
                    latitude = latitude + new String(data,start,length);
                    break;
                }
                case STATE_START: {
                    if(location == null) location = "";
                    location = location + new String(data,start,length);
                    break;
                }
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






        public void processCoordinates() {
            if(latitude != null && longitude != null) {
                if(latitude.length() > 0 && longitude.length() > 0) {
                    try {
                        latitude = latitude.trim();
                        longitude = longitude.trim();
                        
                        Topic associationTypeTopic = createTopic(tm, SI_PREFIX+"geo-location", "geo-location");
                        Topic latitudeTypeTopic = createTopic(tm, SI_PREFIX+"latitude", "latitude");
                        Topic longitudeTypeTopic = createTopic(tm, SI_PREFIX+"longitude", "longitude");
                        Topic locationTypeTopic = createTopic(tm, SI_PREFIX+"location", "location");

                        if(location != null) location = location.trim();
                        if(location == null || location.length() < 1) {
                            location = "location at "+latitude+","+longitude;
                        }
                        location = location.trim();
                        if(location.endsWith(":")) location = location.substring(0, location.length()-1);
                        location = location.trim();

                        parent.log("Creating geo-location for '"+location+"' at "+latitude+";"+longitude);
                        
                        Topic locationTopic = createTopic(tm, SI_PREFIX+"location/"+location, location, new Topic[] { locationTypeTopic } );
                        Topic latitudeTopic = createTopic(tm, SI_PREFIX+"latitude/"+latitude, latitude, new Topic[] { latitudeTypeTopic });
                        Topic longitudeTopic = createTopic(tm, SI_PREFIX+"longitude/"+longitude, longitude, new Topic[] { longitudeTypeTopic });

                        association = tm.createAssociation(associationTypeTopic);
                        association.addPlayer(latitudeTopic, latitudeTypeTopic);
                        association.addPlayer(longitudeTopic, longitudeTypeTopic);
                        association.addPlayer(locationTopic, locationTypeTopic);

                        latitude = null;
                        longitude = null;
                        location = null;

                        progress++;
                    }
                    catch(Exception e) {
                        parent.log(e);
                    }
                }
            }
        }




        // --------------------


        public Topic createTopic(TopicMap topicMap, String si, String baseName) throws TopicMapException {
            return createTopic(topicMap, si, baseName, null);
        }


        public Topic createTopic(TopicMap topicMap, String si, String baseName, Topic[] types) throws TopicMapException {
            Topic t = null;
            if(baseName != null && baseName.length() > 0 && si != null && si.length() > 0) {
                si = TopicTools.cleanDirtyLocator(si);
                t = topicMap.getTopic(si);
                if(t == null) {
                    t = topicMap.getTopicWithBaseName(baseName);
                    if(t == null) {
                        t = topicMap.createTopic();
                        t.setBaseName(baseName);
                    }
                    t.addSubjectIdentifier(new org.wandora.topicmap.Locator(si));
                }
                if(types != null) {
                    for(int i=0; i<types.length; i++) {
                        Topic typeTopic = types[i];
                        if(typeTopic != null) {
                            t.addType(typeTopic);
                        }
                    }
                }
            }
            if(t == null) {
                System.out.println("Failed to create topic for basename '"+baseName+"' and si '"+si+"'.");
            }
            return t;
        }
    }
    
}
