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
 * HCalendarExtractor.java
 *
 * Created on 13. joulukuuta 2007, 11:59
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
 *
 * @author akivela
 */
public class HCalendarExtractor extends AbstractExtractor implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	
	/** Creates a new instance of AdrExtractor */
    public HCalendarExtractor() {
    }

    public String getName() {
        return "HCalendar microformat extractor";
    }
    
    
    public String getDescription() {
        return "Converts HCalendar Microformat HTML snippets to topic maps.";
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
        

        System.out.println("------");
        System.out.println(tidyXML);
        System.out.println("------");
          
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        HCalendarParser parserHandler = new HCalendarParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(new ByteArrayInputStream(tidyXML.getBytes())));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.eventcount + " HCalendar events found!");
        
        return true;
    }
    
    
    
    
    
    
    public static class HCalendarParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        private static final boolean debug = true;

        private TopicMap tm = null;
        private HCalendarExtractor parent = null;

        public static final String SI_PREFIX = "http://wandora.org/si/hcalendar/";

        public int progress = 0;
        public int eventcount = 0;

        private static final int STATE_START=1111;
        
        private static final int STATE_VCALENDAR = 999;
        private static final int STATE_VEVENT = 998;
        private static final int STATE_SUMMARY=1;
        private static final int STATE_LOCATION=2;
        private static final int STATE_URL=3;
        private static final int STATE_URL_A=35;
        private static final int STATE_DTEND=4;
        private static final int STATE_DURATION=5;
        private static final int STATE_RDATE=6;
        private static final int STATE_RRULE=7;
        private static final int STATE_CATEGORY=10;
        private static final int STATE_DESCRIPTION=11;
        private static final int STATE_UID=12;
        private static final int STATE_LATITUDE=13;
        private static final int STATE_LONGITUDE=14;
        private static final int STATE_DTSTART=15;
        
        private static final int STATE_ABBR=9998;
        private static final int STATE_OTHER=9999;

        private int state=STATE_START;

        private String dtstart;
        private String summary;
        private String location;
        private String url;
        private String url_href;
        private String dtend;
        private String duration;
        private String rdate;
        private String rrule;
        private String category;
        private String description;
        private String uid;
        private String latitude;
        private String longitude;
        
        private Association association;

        private Stack<Integer> stateStack;
        

        // -------------------------------------------------------------------------
        public HCalendarParser(TopicMap tm, HCalendarExtractor parent ) {
            this.tm=tm;
            this.parent=parent;

            dtstart = null;
            summary = null;
            location = null;
            url = null;
            url_href = null;
            dtend = null;
            duration = null;
            rdate = null;
            rrule = null;
            category = null;
            description = null;
            uid = null;
            latitude = null;
            longitude = null;
            
            association = null;
            stateStack = new Stack<>();
        }


        // -------------------------------------------------------------------------


        
        public void startDocument() throws SAXException {
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stateStack.push(Integer.valueOf(state));
            String clas = atts.getValue("class");
            
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }

            if(debug) System.out.print("qname=="+ qName);
            if(debug) System.out.print(", class=="+ clas);
            if(debug) System.out.print(", sstate="+state);
            
            switch(state) {
                case STATE_START: {
                    if("vcalendar".equalsIgnoreCase(clas)) {
                        state = STATE_VCALENDAR;
                    }
                    else if("vevent".equalsIgnoreCase(clas)) {
                        state = STATE_VEVENT;
                    }
                    break;
                }
                
                case STATE_VCALENDAR: {
                    if("vevent".equalsIgnoreCase(clas)) {
                        state = STATE_VEVENT;
                    }
                    break;
                }
                
                case STATE_VEVENT: {
                    if("abbr".equalsIgnoreCase(qName)) {
                        String title = atts.getValue("title");
                        if(clas != null) {
                            if("dtstart".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                dtstart = title;
                            }
                            else if("summary".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                summary = title;
                            }
                            else if("location".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                location = title;
                            }
                            else if("url".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                url = title;
                                url_href = atts.getValue("href");
                            }
                            else if("dtend".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                dtend = title;
                            }
                            else if("duration".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                duration = title;
                            }
                            else if("rdate".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                rdate = title;
                            }
                            else if("rrule".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                rrule = title;
                            }
                            else if("category".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                category = title;
                            }
                            else if("description".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                description = title;
                            }
                            else if("uid".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                uid = title;
                            }
                            else if("latitude".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                latitude = title;
                            }
                            else if("longitude".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                longitude = title;
                            }
                        }
                    }
                    else {
                        if(clas != null) {
                            if("dtstart".equalsIgnoreCase(clas)) {
                                state = STATE_DTSTART;
                            }
                            else if("summary".equalsIgnoreCase(clas)) {
                                state = STATE_SUMMARY;
                            }
                            else if("location".equalsIgnoreCase(clas)) {
                                state = STATE_LOCATION;
                            }
                            else if("url".equalsIgnoreCase(clas)) {
                                url_href = atts.getValue("href");
                                state = STATE_URL;
                            }
                            else if("dtend".equalsIgnoreCase(clas)) {
                                state = STATE_DTEND;
                            }
                            else if("duration".equalsIgnoreCase(clas)) {
                                state = STATE_DURATION;
                            }
                            else if("rdate".equalsIgnoreCase(clas)) {
                                state = STATE_RDATE;
                            }
                            else if("rrule".equalsIgnoreCase(clas)) {
                                state = STATE_RRULE;
                            }
                            else if("category".equalsIgnoreCase(clas)) {
                                state = STATE_CATEGORY;
                            }
                            else if("description".equalsIgnoreCase(clas)) {
                                state = STATE_DESCRIPTION;
                            }
                            else if("uid".equalsIgnoreCase(clas)) {
                                state = STATE_UID;
                            }
                            else if("latitude".equalsIgnoreCase(clas)) {
                                state = STATE_LATITUDE;
                            }
                            else if("longitude".equalsIgnoreCase(clas)) {
                                state = STATE_LONGITUDE;
                            }
                        }
                    }
                    break;
                }
                
                case STATE_URL: {
                    if("a".equalsIgnoreCase(qName)) {
                        url_href = atts.getValue("href");
                        state = STATE_URL_A;
                        break;
                    }
                }
            }

            if(debug) System.out.println(", nstate="+state);
        }


        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_VCALENDAR: {
                    if(!stateStack.contains(Integer.valueOf(STATE_VCALENDAR))) {
                        processEvent();
                    }
                    break;
                }
                case STATE_VEVENT: {
                    if(!stateStack.contains(Integer.valueOf(STATE_VEVENT))) {
                        processEvent();
                    }
                    break;
                }
            }
            popState();
        }

        
        public void characters(char[] data, int start, int length) throws SAXException {
            switch(state) {
                case STATE_DTSTART: {
                    dtstart = catenate(dtstart, data, start, length);
                    break;
                }
                case STATE_SUMMARY: {
                    summary = catenate(summary, data, start, length);
                    break;
                }
                case STATE_LOCATION: {
                    location = catenate(location, data, start, length);
                    break;
                }
                case STATE_URL: {
                    url = catenate(url, data, start, length);
                    break;
                }
                case STATE_DTEND: {
                    dtend = catenate(dtend, data, start, length);
                    break;
                }
                case STATE_DURATION: {
                    duration = catenate(duration, data, start, length);
                    break;
                }
                case STATE_RDATE: {
                    rdate = catenate(rdate, data, start, length);
                    break;
                }
                case STATE_RRULE: {
                    rrule = catenate(rrule, data, start, length);
                    break;
                }
                case STATE_CATEGORY: {
                    category = catenate(category, data, start, length);
                    break;
                }
                case STATE_DESCRIPTION: {
                    description = catenate(description, data, start, length);
                    break;
                }
                case STATE_UID: {
                    uid = catenate(uid, data, start, length);
                    break;
                }
                case STATE_LATITUDE: {
                    latitude = catenate(latitude, data, start, length);
                    break;
                }
                case STATE_LONGITUDE: {
                    longitude = catenate(longitude, data, start, length);
                    break;
                }
            }
        }
        
        
        
        
        

        private String catenate(String base, char[] data, int start, int length) {
            if(base == null) base = "";
            base = base + new String(data,start,length);
            if(debug) System.out.println("  string=="+base);
            return base;
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






        public void processEvent() {
            if(dtstart != null && summary != null) {
                    eventcount++;
                    
                    try {
                        Topic eventTypeTopic = createTopic(tm, SI_PREFIX+"event", "event");
                        Topic eventTopic = createTopic(tm, SI_PREFIX+"event/"+dtstart, dtstart+" - "+summary, new Topic[] { eventTypeTopic } );
                        
                        parent.log("Creating HCalendar event '"+getTopicName(eventTopic)+"'.");
                        
                        if(summary != null) { // **** ALWAYS TRUE ****
                            Topic summaryType = createTopic(tm, SI_PREFIX+"summary", "summary");
                            parent.setData(eventTopic, summaryType, "en", summary);
                        }
                        
                        if(dtstart != null) { // **** ALWAYS TRUE ****
                            try {
                                Topic associationTypeTopic = createTopic(tm, SI_PREFIX+"dtstart", "event-start-time");
                                Topic playerTopic = createTopic(tm, SI_PREFIX+"dtstart/"+dtstart, dtstart);
                                association = tm.createAssociation(associationTypeTopic);
                                association.addPlayer(eventTopic, eventTypeTopic);
                                association.addPlayer(playerTopic, associationTypeTopic);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(location != null) {
                            try {
                                Topic associationTypeTopic = createTopic(tm, SI_PREFIX+"location", "location");
                                Topic playerTopic = createTopic(tm, SI_PREFIX+"location/"+location, location);
                                association = tm.createAssociation(associationTypeTopic);
                                association.addPlayer(eventTopic, eventTypeTopic);
                                association.addPlayer(playerTopic, associationTypeTopic);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(url != null || url_href != null) {
                            try {
                                if(url_href != null) url = url_href;
                                eventTopic.setSubjectLocator(new org.wandora.topicmap.Locator(url));
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(dtend != null) {
                            try {
                                Topic associationTypeTopic = createTopic(tm, SI_PREFIX+"dtend", "event-end-time");
                                Topic playerTopic = createTopic(tm, SI_PREFIX+"dtend/"+dtend, dtend);
                                association = tm.createAssociation(associationTypeTopic);
                                association.addPlayer(eventTopic, eventTypeTopic);
                                association.addPlayer(playerTopic, associationTypeTopic);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(duration != null) {
                            try {
                                Topic associationTypeTopic = createTopic(tm, SI_PREFIX+"duration", "event-duration");
                                Topic playerTopic = createTopic(tm, SI_PREFIX+"duration/"+duration, duration);
                                association = tm.createAssociation(associationTypeTopic);
                                association.addPlayer(eventTopic, eventTypeTopic);
                                association.addPlayer(playerTopic, associationTypeTopic);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(rdate != null) {
                            try {
                                Topic associationTypeTopic = createTopic(tm, SI_PREFIX+"rdate", "repeat-date");
                                Topic playerTopic = createTopic(tm, SI_PREFIX+"rdate/"+rdate, rdate);
                                association = tm.createAssociation(associationTypeTopic);
                                association.addPlayer(eventTopic, eventTypeTopic);
                                association.addPlayer(playerTopic, associationTypeTopic);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(rrule != null) {
                            try {
                                Topic associationTypeTopic = createTopic(tm, SI_PREFIX+"rrule", "repeat-rule");
                                Topic playerTopic = createTopic(tm, SI_PREFIX+"rrule/"+rrule, rrule);
                                association = tm.createAssociation(associationTypeTopic);
                                association.addPlayer(eventTopic, eventTypeTopic);
                                association.addPlayer(playerTopic, associationTypeTopic);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(category != null) {
                            try {
                                Topic associationTypeTopic = createTopic(tm, SI_PREFIX+"category", "category");
                                Topic playerTopic = createTopic(tm, SI_PREFIX+"category/"+category, category);
                                association = tm.createAssociation(associationTypeTopic);
                                association.addPlayer(eventTopic, eventTypeTopic);
                                association.addPlayer(playerTopic, associationTypeTopic);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(description != null) {
                            try {
                                Topic descriptionType = createTopic(tm, SI_PREFIX+"description", "description");
                                parent.setData(eventTopic, descriptionType, "en", description);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(uid != null) {
                            try {
                                eventTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(SI_PREFIX+"uid/"+uid));
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        if(latitude != null && longitude != null) {
                            Topic associationTypeTopic = createTopic(tm, SI_PREFIX+"geo-location", "geo-location");
                            Topic latitudeTypeTopic = createTopic(tm, SI_PREFIX+"latitude", "latitude");
                            Topic longitudeTypeTopic = createTopic(tm, SI_PREFIX+"longitude", "longitude");
                            
                            Topic latitudeTopic = createTopic(tm, SI_PREFIX+"latitude/"+latitude, latitude, new Topic[] { latitudeTypeTopic });
                            Topic longitudeTopic = createTopic(tm, SI_PREFIX+"longitude/"+longitude, longitude, new Topic[] { longitudeTypeTopic });

                            association = tm.createAssociation(associationTypeTopic);
                            association.addPlayer(latitudeTopic, latitudeTypeTopic);
                            association.addPlayer(longitudeTopic, longitudeTypeTopic);
                            association.addPlayer(eventTopic, eventTypeTopic);
                        }
                    }
                    catch(Exception e) {
                        parent.log(e);
                    }
                    
                    dtstart = null;
                    summary = null;
                    location = null;
                    url = null;
                    url_href = null;
                    dtend = null;
                    duration = null;
                    rdate = null;
                    rrule = null;
                    category = null;
                    description = null;
                    uid = null;
                    latitude = null;
                    longitude = null;
                    
                    progress++;
            }
            else {
                parent.log("Rejecting HCalendar event without obligatory dtstart or/and summary data!");
            }
        }

        
        private void popState() {
            if(!stateStack.empty()) {
                state = ((Integer) stateStack.pop()).intValue();
                if(debug) System.out.println("  popping state:"+state);
            }
            else {
                state = STATE_START;
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
