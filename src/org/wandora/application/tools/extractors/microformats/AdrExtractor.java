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
 * 
 * AdrExtractor.java
 *
 * Created on 13. joulukuuta 2007, 11:59
 *
 */

package org.wandora.application.tools.extractors.microformats;



import org.wandora.utils.IObox;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import org.xml.sax.*;
import org.w3c.tidy.*;
import javax.swing.*;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;
import org.wandora.application.gui.*;


/**
 *
 * @author akivela
 */
public class AdrExtractor extends AbstractExtractor implements WandoraTool {
    
    /** Creates a new instance of AdrExtractor */
    public AdrExtractor() {
    }

    @Override
    public String getName() {
        return "Adr microformat extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Converts ADR Microformat HTML snippets to topic maps.";
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
        //tidyXML = HTMLEntitiesCoder.decode(tidyXML);
        
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
        AdrParser parserHandler = new AdrParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(new ByteArrayInputStream(tidyXML.getBytes())));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " adr addresses found!");
        
        return true;
    }
    
    
    
    
    
    
    public static class AdrParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {
        private static final boolean debug = true;

        private TopicMap tm = null;
        private AdrExtractor parent = null;

        public static final String SI_PREFIX = "http://wandora.org/si/adr/";

        public int progress = 0;
        public int adrcount = 0;

        private static final int STATE_START=1111;
        private static final int STATE_ADR = 99;
        private static final int STATE_POST_OFFICE_BOX=1;
        private static final int STATE_EXTENDED_ADDRESS=2;
        private static final int STATE_STREET_ADDRESS=3;
        private static final int STATE_LOCALITY=4;
        private static final int STATE_REGION=5;
        private static final int STATE_POSTAL_CODE=6;
        private static final int STATE_COUNTRY_NAME=7;
        private static final int STATE_ABBR=10;
        private static final int STATE_OTHER=9999;

        private int state=STATE_START;

        private String postOfficeBox;
        private String extendedAddress;
        private String streetAddress;
        private String locality;
        private String region;
        private String postalCode;
        private String countryName;
        
        private String location;
        
        private Association association;

        private Stack stateStack;
        

        // -------------------------------------------------------------------------
        public AdrParser(TopicMap tm, AdrExtractor parent ) {
            this.tm=tm;
            this.parent=parent;

            postOfficeBox = null;
            extendedAddress = null;
            streetAddress = null;
            locality = null;
            region = null;
            postalCode = null;
            countryName = null;
            
            location = null;
            
            association = null;
            stateStack = new Stack();
        }


        // -------------------------------------------------------------------------


        
        public void startDocument() throws SAXException {
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stateStack.push(new Integer(state));
            String clas = atts.getValue("class");
            
            if(debug) System.out.print("qname=="+ qName);
            if(debug) System.out.print(", class=="+ clas);
            if(debug) System.out.print(", sstate="+state);
            
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            
            switch(state) {
                case STATE_START: {
                    if("adr".equalsIgnoreCase(clas)) {
                        state = STATE_ADR;
                    }
                    break;
                }
                case STATE_ADR: {
                    if("abbr".equalsIgnoreCase(qName)) {
                        String title = atts.getValue("title");
                        if(clas != null) {
                            if("street-address".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                streetAddress = title;
                            }
                            else if("post-office-box".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                postOfficeBox = title;
                            }
                            else if("extended-address".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                extendedAddress = title;
                            }
                            else if("locality".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                locality = title;
                            }
                            else if("region".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                region = title;
                            }
                            else if("postal-code".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                postalCode = title;
                            }
                            else if("country-name".equalsIgnoreCase(clas)) {
                                state = STATE_ABBR;
                                countryName = title;
                            }
                        }
                    }
                    else {
                        if("street-address".equalsIgnoreCase(clas)) {
                            state = STATE_STREET_ADDRESS;
                            streetAddress = null;
                        }
                        else if("post-office-box".equalsIgnoreCase(clas)) {
                            state = STATE_POST_OFFICE_BOX;
                            postOfficeBox = null;
                        }
                        else if("extended-address".equalsIgnoreCase(clas)) {
                            state = STATE_EXTENDED_ADDRESS;
                            extendedAddress = null;
                        }
                        else if("locality".equalsIgnoreCase(clas)) {
                            state = STATE_LOCALITY;
                            locality = null;
                        }
                        else if("region".equalsIgnoreCase(clas)) {
                            state = STATE_REGION;
                            region = null;
                        }
                        else if("postal-code".equalsIgnoreCase(clas)) {
                            state = STATE_POSTAL_CODE;
                            postalCode = null;
                        }
                        else if("country-name".equalsIgnoreCase(clas)) {
                            state = STATE_COUNTRY_NAME;
                            countryName = null;
                        }
                    }
                    break;
                }
            }
            if(debug) System.out.println(", nstate="+state);
        }


        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_ADR: {
                    if(!stateStack.contains(new Integer(STATE_ADR))) {
                        processAdr();
                    }
                    break;
                }
            }
            popState();
        }

        public void characters(char[] data, int start, int length) throws SAXException {
            switch(state) {
                case STATE_START: {
                    location = catenate(location, data, start, length);
                    break;
                }
                case STATE_STREET_ADDRESS: {
                    streetAddress = catenate(streetAddress, data, start, length);
                    break;
                }
                case STATE_POST_OFFICE_BOX: {
                    postOfficeBox = catenate(postOfficeBox, data, start, length);
                    break;
                }
                case STATE_EXTENDED_ADDRESS: {
                    extendedAddress = catenate(extendedAddress, data, start, length);
                    break;
                }
                case STATE_LOCALITY: {
                    locality = catenate(locality, data, start, length);
                    break;
                }
                case STATE_REGION: {
                    region = catenate(region, data, start, length);
                    break;
                }
                case STATE_POSTAL_CODE: {
                    postalCode = catenate(postalCode, data, start, length);
                    break;
                }
                case STATE_COUNTRY_NAME: {
                    countryName = catenate(countryName, data, start, length);
                    break;
                }
            }
        }

        private String catenate(String base, char[] data, int start, int length) {
            if(base == null) base = "";
            base = base + new String(data,start,length);
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






        public void processAdr() {
            if(streetAddress != null || postOfficeBox != null || extendedAddress != null || locality != null ||
               region != null || postalCode != null || countryName != null ) {
                    adrcount++;
                    
                    try {
                        Topic locationTypeTopic = createTopic(tm, SI_PREFIX+"location", "location");
                        boolean isDefaultLocation = false;
                        if(location != null) location = location.trim();
                        if(location == null || location.length() < 1) {
                            isDefaultLocation = true;
                            location = "unknown-location-"+System.currentTimeMillis()+""+adrcount;
                        }
                        if(isDefaultLocation) {
                            parent.log("Creating address for unknown location '"+location+"'");
                        }
                        else {
                            location = location.trim();
                            if(location.endsWith(":")) location = location.substring(0, location.length()-1);
                            location = location.trim();
                            parent.log("Creating address for '"+location+"'");
                        }
                        Topic locationTopic = createTopic(tm, SI_PREFIX+"location/"+location, location, new Topic[] { locationTypeTopic } );

                        createAssociationFor(streetAddress, "street-address", locationTopic, locationTypeTopic, tm);
                        createAssociationFor(postOfficeBox, "post-office-box", locationTopic, locationTypeTopic, tm);
                        createAssociationFor(extendedAddress, "extended-address", locationTopic, locationTypeTopic, tm);
                        createAssociationFor(locality, "locality", locationTopic, locationTypeTopic, tm);
                        createAssociationFor(region, "region", locationTopic, locationTypeTopic, tm);
                        createAssociationFor(postalCode, "postal-code", locationTopic, locationTypeTopic, tm);
                        createAssociationFor(countryName, "country-name", locationTopic, locationTypeTopic, tm);
                    }
                    catch(Exception e) {
                        parent.log(e);
                    }
                    
                    postOfficeBox = null;
                    extendedAddress = null;
                    streetAddress = null;
                    locality = null;
                    region = null;
                    postalCode = null;
                    countryName = null;

                    location = null;
                    
                    progress++;
            }
            else {
                parent.log("Found adr without data. Rejecting!");
            }
        }

        
        private void createAssociationFor(String basename, String associationTypeName, Topic player, Topic role, TopicMap tm) {
            if(basename != null) {
                basename = basename.trim();
                if(basename.length() > 0) {
                    try {
                        Topic associationTypeTopic = createTopic(tm, SI_PREFIX+associationTypeName, associationTypeName);
                        Topic playerTopic = createTopic(tm, SI_PREFIX+associationTypeName+"/"+basename, basename);
                        Association association = tm.createAssociation(associationTypeTopic);
                        association.addPlayer(player, role);
                        association.addPlayer(playerTopic, associationTypeTopic);
                    }
                    catch(Exception e) {
                        parent.log(e);
                    }
                }
            }
        }

        
        
        
        private void popState() {
            if(!stateStack.empty()) {
                state = ((Integer) stateStack.pop()).intValue();
            }
            else {
                state = STATE_OTHER;
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
