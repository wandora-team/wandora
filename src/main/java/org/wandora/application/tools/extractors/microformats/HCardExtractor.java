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
 * HCardExtractor.java
 *
 */

package org.wandora.application.tools.extractors.microformats;


import org.wandora.utils.IObox;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.xml.sax.*;
import org.w3c.tidy.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.gui.*;


/**
 *
 * @author akivela
 */
public class HCardExtractor extends AbstractExtractor implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	
	/** Creates a new instance of HCardExtractor */
    public HCardExtractor() {
    }

    

    public String getName() {
        return "Hcard microformat extractor";
    }
    
    
    public String getDescription() {
        return "Converts HCard Microformat HTML snippets to Topic Maps.";
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
        //tidyXML = HTMLEntitiesCoder.decode(tidyXML);
        //tidyXML = tidyXML.replace("&amp;deg;", "&#0176;");
        
        System.out.println("------");
        System.out.println(tidyXML);
        System.out.println("------");
        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        HCardParser parserHandler = new HCardParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(new ByteArrayInputStream(tidyXML.getBytes())));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.progress + " HCard entries found!");
        
        return true;
    }
    
    
    
    
    
    
    public class HCardParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {
        private static final boolean debug = true;
        
        private TopicMap tm = null;
        private HCardExtractor parent = null;

        public static final String SI_PREFIX = "http://wandora.org/si/hcard/";

        public static final String ABBR = "abbr";
        
        
        public int progress = 0;

        private static final int STATE_START=11111;
        private static final int STATE_VCARD=10;
        
        private static final int STATE_FN=1000;
        private static final int STATE_N=1010;
        private static final int STATE_N_FAMILY=1011;
        private static final int STATE_N_GIVEN=1012;
        private static final int STATE_N_ADDITIONAL=1013;
        private static final int STATE_N_HONORIFIC_PREFIX=1014;
        private static final int STATE_N_HONORIFIC_SUFFIX=1015;
        
        private static final int STATE_NICKNAME=1020;
        private static final int STATE_SORTSTRING=1030;
        
        private static final int STATE_ADR=20;
        private static final int STATE_ADR_TYPE=21;
        private static final int STATE_ADR_VALUE=22;
        private static final int STATE_ADR_POST_OFFICE_BOX=23;
        private static final int STATE_ADR_EXTENDED_ADDRESS=24;
        private static final int STATE_ADR_STREET_ADDRESS=25;
        private static final int STATE_ADR_LOCALITY=26;
        private static final int STATE_ADR_REGION=27;
        private static final int STATE_ADR_POSTAL_CODE=28;
        private static final int STATE_ADR_COUNTRY_NAME=29;
        
        private static final int STATE_TEL=30;
        private static final int STATE_TEL_TYPE=31;
        private static final int STATE_TEL_VALUE=32;
        private static final int STATE_EMAIL=40;
        private static final int STATE_EMAIL_TYPE=41;
        private static final int STATE_EMAIL_VALUE=72;
        
        private static final int STATE_ORG=50;
        private static final int STATE_ORG_UNIT=51;
        private static final int STATE_ORG_NAME=52;
        
        private static final int STATE_URL=60;
        
        private static final int STATE_LABEL=80;
        
        private static final int STATE_GEO=80;
        private static final int STATE_GEO_LATITUDE=81;
        private static final int STATE_GEO_LONGITUDE=82;
        
        private static final int STATE_TZ=85;
        private static final int STATE_PHOTO=90;
        private static final int STATE_LOGO=91;
        private static final int STATE_SOUND=92;
        private static final int STATE_BDAY=93;
        private static final int STATE_TITLE=100;
        private static final int STATE_ROLE=110;
        private static final int STATE_CATEGORY=120;
        private static final int STATE_NOTE=130;
        private static final int STATE_CLASS=140;
        private static final int STATE_KEY=150;
        private static final int STATE_MAILER=160;
        private static final int STATE_UID=170;
        private static final int STATE_REV=180;
        
        private static final int STATE_OTHER=99999;

        private Name n;
        
        private String nickname;
        private ArrayList<String> nicknames;
        
        private String url;
        private ArrayList<String> urls;
        private Email email;
        private ArrayList<Email> emails;
        private Tel tel;
        private ArrayList<Tel> tels;
        
        private Adr adr;
        private ArrayList<Adr> adrs;
        private String label;
        private ArrayList<String> labels;
        
        private String latitude;
        private String longitude;
        private String tz;
        
        private String photo;
        private ArrayList<String> photos;
        private String logo;
        private ArrayList<String> logos;
        private String sound;
        private ArrayList<String> sounds;
        private String bday;
        
        private String title;
        private ArrayList<String> titles;
        private String role;
        private ArrayList<String> roles;
        private Org org;
        private ArrayList<Org> orgs;
        private String category;
        private ArrayList<String> categories;
        private String note;
        private ArrayList<String> notes;
        
        private String uid;
        private String classs;
        private String key;
        private ArrayList<String> keys;
        private String mailer;
        private ArrayList<String> mailers;
        private String rev;
        private ArrayList<String> revs;
        
        private Collection<Integer> state = null;
        private Stack<Collection<Integer>> stateStack = null;

        
        private int isAbbr = 0;
                
        
        // -------------------------------------------------------------------------
        public HCardParser(TopicMap tm, HCardExtractor parent ) {
            this.tm=tm;
            this.parent=parent;

            initCard();

            stateStack = new Stack<>();
            state = new ArrayList<Integer>();
            state.add(Integer.valueOf(STATE_START));
        }


        // -------------------------------------------------------------------------


        
        public void startDocument() throws SAXException {
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            
            stateStack.push(state);

            if(ABBR.equalsIgnoreCase(qName)) {
                isAbbr++;
            }
            
            String clas = atts.getValue("class");
            String abbrTitle = atts.getValue("title");
            
            if(debug) System.out.print("qname=="+ qName);
            if(debug) System.out.print(", class=="+ clas);
            if(debug) System.out.print(", sstate="+state);
            
            if(clas == null) {
                if(debug) System.out.println(", no class here - rejecting");
                return;
            }

            String[] clases = clas.split(" ");
                       
            Collection<Integer> newState = new ArrayList<>();
            
            for(int i=0; i<clases.length; i++) {
                Iterator<Integer> stateIter = state.iterator();
                while(stateIter.hasNext()) {
                    Integer singleState = stateIter.next();
                    int singleStateInt = singleState.intValue();

                    switch(singleStateInt) {
                        case STATE_START: {
                            if("vcard".equalsIgnoreCase(clases[i])) {
                                initCard();
                                newState.add(Integer.valueOf(STATE_VCARD));
                            }
                            else {
                                newState.add(Integer.valueOf(STATE_START));
                            }
                            break;
                        }

                        case STATE_VCARD: {
                            if("fn".equalsIgnoreCase(clases[i])) {
                                if(n == null) n = new Name();
                                if(isAbbr > 0) {
                                    n.fn = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_FN));
                            }
                            else if("n".equalsIgnoreCase(clases[i])) {
                                if(n == null) n = new Name();
                                if(isAbbr > 0) {
                                    n.name = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_N));
                            }
                            else if("nickname".equalsIgnoreCase(clases[i])) {
                                if(nickname != null) {
                                    nicknames.add(nickname);
                                    nickname = null;
                                }
                                if(isAbbr > 0) {
                                    nickname = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_NICKNAME));
                            }
                            else if("sort-string".equalsIgnoreCase(clases[i])) {
                                if(n == null) n = new Name();
                                if(isAbbr > 0) {
                                    n.sortString = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_SORTSTRING));
                            }
                            else if("url".equalsIgnoreCase(clases[i])) {
                                if(url != null) {
                                    urls.add(url);
                                    url = null;
                                }
                                if(isAbbr > 0) {
                                    url = abbrTitle;
                                }
                                if("a".equalsIgnoreCase(qName)) {
                                    url = atts.getValue("href");
                                }
                                newState.add(Integer.valueOf(STATE_URL));
                            }
                            else if("email".equalsIgnoreCase(clases[i])) {
                                if(email != null) {
                                    emails.add(email);
                                }
                                email = new Email();
                                if(isAbbr > 0) {
                                    email.value = abbrTitle;
                                }
                                if("a".equalsIgnoreCase(qName)) {
                                    email.value = atts.getValue("href");
                                    if(email.value != null) {
                                        if(email.value.startsWith("mailto:")) {
                                            email.value = email.value.substring(7);
                                        }
                                    }
                                }
                                newState.add(Integer.valueOf(STATE_EMAIL));
                            }
                            else if("tel".equalsIgnoreCase(clases[i])) {
                                if(tel != null) {
                                    tels.add(tel);
                                }
                                tel = new Tel();
                                if(isAbbr > 0) {
                                    tel.value = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_TEL));
                            }
                            else if("adr".equalsIgnoreCase(clases[i])) {
                                if(adr != null) {
                                    adrs.add(adr);
                                }
                                adr = new Adr();
                                newState.add(Integer.valueOf(STATE_ADR));
                            }
                            else if("label".equalsIgnoreCase(clases[i])) {
                                if(label != null) {
                                    labels.add(label);
                                    label = null;
                                }
                                if(isAbbr > 0) {
                                    label = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_LABEL));
                            }
                            else if("geo".equalsIgnoreCase(clases[i])) {
                                latitude = null;
                                longitude = null;
                                if(isAbbr > 0 && abbrTitle != null) {
                                    String[] geo = abbrTitle.split(",");
                                    if(geo.length > 1) {
                                        latitude = geo[0];
                                        longitude = geo[1];
                                    }
                                }
                                newState.add(Integer.valueOf(STATE_GEO));
                            }
                            else if("tz".equalsIgnoreCase(clases[i])) {
                                tz = null;
                                if(isAbbr > 0) {
                                    tz = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_TZ));
                            }
                            else if("photo".equalsIgnoreCase(clases[i])) {
                                if(photo != null) {
                                    photos.add(photo);
                                    photo = null;
                                    if(isAbbr > 0) {
                                        photo = abbrTitle;
                                    }
                                    if("img".equalsIgnoreCase(qName)) {
                                        photo = atts.getValue("src");
                                    }
                                    else if("object".equalsIgnoreCase(qName)) {
                                        photo = atts.getValue("data");
                                    }
                                    else if("a".equalsIgnoreCase(qName)) {
                                        photo = atts.getValue("href");
                                    }
                                }
                                newState.add(Integer.valueOf(STATE_PHOTO));
                            }
                            else if("logo".equalsIgnoreCase(clases[i])) {
                                if(logo != null) {
                                    logos.add(logo);
                                    logo = null;
                                    if(isAbbr > 0) {
                                        logo = abbrTitle;
                                    }
                                    if("img".equalsIgnoreCase(qName)) {
                                        logo = atts.getValue("src");
                                    }
                                    else if("object".equalsIgnoreCase(qName)) {
                                        logo = atts.getValue("data");
                                    }
                                    else if("a".equalsIgnoreCase(qName)) {
                                        logo = atts.getValue("href");
                                    }
                                }
                                newState.add(Integer.valueOf(STATE_LOGO));
                            }
                            else if("sound".equalsIgnoreCase(clases[i])) {
                                if(sound != null) {
                                    sounds.add(sound);
                                    sound = null;
                                    if(isAbbr > 0) {
                                        sound = abbrTitle;
                                    }
                                    if("img".equalsIgnoreCase(qName)) {
                                        sound = atts.getValue("src");
                                    }
                                    else if("object".equalsIgnoreCase(qName)) {
                                        sound = atts.getValue("data");
                                    }
                                    else if("a".equalsIgnoreCase(qName)) {
                                        sound = atts.getValue("href");
                                    }
                                }
                                newState.add(Integer.valueOf(STATE_SOUND));
                            }
                            else if("bday".equalsIgnoreCase(clases[i])) {
                                bday = null;
                                if("abbr".equalsIgnoreCase(qName)) {
                                    bday = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_BDAY));
                            }
                            else if("title".equalsIgnoreCase(clases[i])) {
                                if(title != null) {
                                    titles.add(title);
                                    title = null;
                                }
                                if(isAbbr > 0) {
                                    title = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_TITLE));
                            }
                            else if("role".equalsIgnoreCase(clases[i])) {
                                if(role != null) {
                                    roles.add(role);
                                    role = null;
                                }
                                if(isAbbr > 0) {
                                    role = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ROLE));
                            }
                            else if("org".equalsIgnoreCase(clases[i])) {
                                if(org != null) {
                                    orgs.add(org);
                                }
                                org = new Org();
                                if(isAbbr > 0) {
                                    org.name = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ORG));
                            }
                            else if("category".equalsIgnoreCase(clases[i])) {
                                if(category != null) {
                                    categories.add(category);
                                    category = null;
                                }
                                if(isAbbr > 0) {
                                    category = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_CATEGORY));
                            }
                            else if("note".equalsIgnoreCase(clases[i])) {
                                if(note != null) {
                                    notes.add(note);
                                    note = null;
                                }
                                if(isAbbr > 0) {
                                    note = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_NOTE));
                            }
                            else if("class".equalsIgnoreCase(clases[i])) {
                                classs = null;
                                if(isAbbr > 0) {
                                    classs = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_CLASS));
                            }
                            else if("key".equalsIgnoreCase(clases[i])) {
                                if(key != null) {
                                    keys.add(key);
                                    key = null;
                                }
                                if(isAbbr > 0) {
                                    key = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_KEY));
                            }
                            else if("mailer".equalsIgnoreCase(clases[i])) {
                                if(mailer != null) {
                                    mailers.add(mailer);
                                    mailer = null;
                                }
                                if(isAbbr > 0) {
                                    mailer = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_MAILER));
                            }
                            else if("uid".equalsIgnoreCase(clases[i])) {
                                uid = null;
                                if(isAbbr > 0) {
                                    uid = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_UID));
                            }
                            else if("rev".equalsIgnoreCase(clases[i])) {
                                if(rev != null) {
                                    revs.add(rev);
                                    rev = null;
                                }
                                if(isAbbr > 0) {
                                    rev = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_REV));
                            }
                            else if("organization-name".equalsIgnoreCase(clases[i])) {
                                if(org == null) org = new Org();
                                org.name = null;
                                if(isAbbr > 0) {
                                    org.name = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ORG_NAME));
                            }
                            else if("organization-unit".equalsIgnoreCase(clases[i])) {
                                if(org == null) org = new Org();
                                org.unit = null;
                                if(isAbbr > 0) {
                                    org.unit = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ORG_UNIT));
                            }
                            break;
                        }
                        
                        case STATE_N: {
                            if("family-name".equalsIgnoreCase(clases[i])) {
                                n.familyName = null;
                                if(isAbbr > 0) {
                                    n.familyName = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_N_FAMILY));
                            }
                            else if("given-name".equalsIgnoreCase(clases[i])) {
                                n.givenName = null;
                                if(isAbbr > 0) {
                                    n.givenName = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_N_GIVEN));
                            }
                            else if("additional-name".equalsIgnoreCase(clases[i])) {
                                n.additionalName = null;
                                if(isAbbr > 0) {
                                    n.additionalName = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_N_ADDITIONAL));
                            }
                            else if("honorific-prefix".equalsIgnoreCase(clases[i])) {
                                n.honorifixPrefix = null;
                                if(isAbbr > 0) {
                                    n.honorifixPrefix = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_N_HONORIFIC_PREFIX));
                            }
                            else if("honorific-suffix".equalsIgnoreCase(clases[i])) {
                                n.honorifixSuffix = null;
                                if(isAbbr > 0) {
                                    n.honorifixSuffix = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_N_HONORIFIC_SUFFIX));
                            }
                            break;
                        }
                        
                        case STATE_EMAIL: {
                            if("type".equalsIgnoreCase(clases[i])) {
                                email.type = null;
                                if(isAbbr > 0) {
                                    email.type = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_EMAIL_TYPE));
                            }
                            else if("value".equalsIgnoreCase(clases[i])) {
                                email.value = null;
                                if(isAbbr > 0) {
                                    email.value = abbrTitle;
                                }
                                if("a".equalsIgnoreCase(qName)) {
                                    email.value = atts.getValue("href");
                                    if(email.value != null) {
                                        if(email.value.startsWith("mailto:")) {
                                            email.value = email.value.substring(7);
                                        }
                                    }
                                }
                                newState.add(Integer.valueOf(STATE_EMAIL_VALUE));
                            }
                            else {
                                email.type = clases[i];
                            }
                            break;
                        }
                        
                        
                        case STATE_TEL: {
                            if("type".equalsIgnoreCase(clases[i])) {
                                tel.type = null;
                                if(isAbbr > 0) {
                                    tel.type = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_TEL_TYPE));
                            }
                            else if("value".equalsIgnoreCase(clases[i])) {
                                tel.value = null;
                                if(isAbbr > 0) {
                                    tel.value = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_TEL_VALUE));
                            }
                            break;
                        }
                        
                        case STATE_ADR_VALUE:
                        case STATE_ADR: {
                            if("post-office-box".equalsIgnoreCase(clases[i])) {
                                adr.postOfficeBox = null;
                                if(isAbbr > 0) {
                                    adr.postOfficeBox = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ADR_POST_OFFICE_BOX));
                            }
                            else if("extended-address".equalsIgnoreCase(clases[i])) {
                                adr.extendedAddress = null;
                                if(isAbbr > 0) {
                                    adr.extendedAddress = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ADR_EXTENDED_ADDRESS));
                            }
                            else if("street-address".equalsIgnoreCase(clases[i])) {
                                adr.streetAddress = null;
                                if(isAbbr > 0) {
                                    adr.streetAddress = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ADR_STREET_ADDRESS));
                            }
                            else if("locality".equalsIgnoreCase(clases[i])) {
                                adr.locality = null;
                                if(isAbbr > 0) {
                                    adr.locality = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ADR_LOCALITY));
                            }
                            else if("region".equalsIgnoreCase(clases[i])) {
                                adr.region = null;
                                if(isAbbr > 0) {
                                    adr.region = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ADR_REGION));
                            }
                            else if("postal-code".equalsIgnoreCase(clases[i])) {
                                adr.postalCode = null;
                                if(isAbbr > 0) {
                                    adr.postalCode = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ADR_POSTAL_CODE));
                            }
                            else if("country-name".equalsIgnoreCase(clases[i])) {
                                adr.countryName = null;
                                if(isAbbr > 0) {
                                    adr.countryName = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ADR_COUNTRY_NAME));
                            }
                            else if("type".equalsIgnoreCase(clases[i])) {
                                adr.type = null;
                                if(isAbbr > 0) {
                                    adr.type = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ADR_TYPE));
                            }
                            else if("value".equalsIgnoreCase(clases[i])) {
                                if(isAbbr > 0) {
                                    adr.adr = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ADR_VALUE));
                            }
                            break;
                        }

                        case STATE_GEO: {
                            if("latitude".equalsIgnoreCase(clases[i])) {
                                latitude = null;
                                if(isAbbr > 0) {
                                    latitude = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_GEO_LATITUDE));
                            }
                            else if("longitude".equalsIgnoreCase(clases[i])) {
                                latitude = null;
                                if(isAbbr > 0) {
                                    longitude = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_GEO_LONGITUDE));
                            }
                            break;
                        }
                        
                        case STATE_ORG: {
                            if("organization-name".equalsIgnoreCase(clases[i])) {
                                org.name = null;
                                if(isAbbr > 0) {
                                    org.name = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ORG_NAME));
                            }
                            else if("organization-unit".equalsIgnoreCase(clases[i])) {
                                org.unit = null;
                                if(isAbbr > 0) {
                                    org.unit = abbrTitle;
                                }
                                newState.add(Integer.valueOf(STATE_ORG_UNIT));
                            }
                            break;
                        }
                    }
                }
            }
            
            if(newState.size() == 0) {
                newState.addAll(state);
            }
            state = new ArrayList<>();
            Iterator<Integer> newStateIterator = newState.iterator();
            Integer i = null;
            while(newStateIterator.hasNext()) {
                i = newStateIterator.next();
                if(!state.contains(i)) {
                    state.add(i);
                }
            }
            
            if(debug) System.out.println(", nstate="+state);
        }

        
        
        
        
        

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(ABBR.equalsIgnoreCase(qName)) {
                isAbbr--;
            }
            
            Iterator<Integer> stateIter = state.iterator();
            while(stateIter.hasNext()) {
                Integer singleState = stateIter.next();
                int singleStateInt = singleState.intValue();

                switch(singleStateInt) {
                    case STATE_VCARD: {
                        Iterator<Collection<Integer>> statesLeft = stateStack.iterator();
                        boolean doProcessCard = true;
                        while(statesLeft.hasNext()) {
                            Collection<Integer> stateLeft = statesLeft.next();
                            if(stateLeft.contains(Integer.valueOf(STATE_VCARD))) {
                                doProcessCard = false;
                                break;
                            }
                        }
                        if(doProcessCard) processCard();
                        break;
                    }
                }
            }
            
            // **** POP STATE ****
            if(!stateStack.empty()) {
                state = (ArrayList<Integer>) stateStack.pop();
                if(debug) System.out.println("  popping state:"+state);
            }
            else {
                state = new ArrayList<Integer>();
                state.add(Integer.valueOf(STATE_START));
            }
        }



        public void characters(char[] data, int start, int length) throws SAXException {
            if(isAbbr > 0) return;
            
            Iterator<Integer> stateIter = state.iterator();
            while(stateIter.hasNext()) {
                Integer singleState = stateIter.next();
                int singleStateInt = singleState.intValue();

                
                // TODO ..........................................................
                switch(singleStateInt) {
                    
                    // **** NAMES ****
                    case STATE_FN: {
                        n.fn = catenate(n.fn, data, start, length);
                        break;
                    }
                    case STATE_N: {
                        n.name = catenate(n.name, data, start, length);
                        break;
                    }
                    case STATE_N_FAMILY: {
                        n.familyName = catenate(n.familyName, data, start, length);
                        break;
                    }
                    case STATE_N_GIVEN: {
                        n.givenName = catenate(n.givenName, data, start, length);
                        break;
                    }
                    case STATE_N_ADDITIONAL: {
                        n.additionalName = catenate(n.additionalName, data, start, length);
                        break;
                    }
                    case STATE_N_HONORIFIC_PREFIX: {
                        n.honorifixPrefix = catenate(n.honorifixPrefix, data, start, length);
                        break;
                    }
                    case STATE_N_HONORIFIC_SUFFIX: {
                        n.honorifixSuffix = catenate(n.honorifixSuffix, data, start, length);
                        break;
                    }
                    case STATE_NICKNAME: {
                        nickname = catenate(nickname, data, start, length);
                        break;
                    }
                    case STATE_SORTSTRING: {
                        n.sortString = catenate(n.sortString, data, start, length);
                        break;
                    }
                    
                    // **** URL ****
                    case STATE_URL: {
                        url = catenate(url, data, start, length);
                        break;
                    }
                    
                    // **** EMAIL ****
                    case STATE_EMAIL_VALUE:
                    case STATE_EMAIL: {
                        email.value = catenate(email.value, data, start, length);
                        break;
                    }
                    case STATE_EMAIL_TYPE: {
                        email.type = catenate(email.type, data, start, length);
                        break;
                    }
                    
                    // **** TEL ****
                    case STATE_TEL_VALUE:
                    case STATE_TEL: {
                        tel.value = catenate(tel.value, data, start, length);
                        break;
                    }
                    case STATE_TEL_TYPE: {
                        tel.type = catenate(tel.type, data, start, length);
                        break;
                    }
                    
                    // **** ADDRESS ****
                    case STATE_ADR_POST_OFFICE_BOX: {
                        adr.postOfficeBox = catenate(adr.postOfficeBox, data, start, length);
                        break;
                    }
                    case STATE_ADR_EXTENDED_ADDRESS: {
                        adr.extendedAddress = catenate(adr.extendedAddress, data, start, length);
                        break;
                    }
                    case STATE_ADR_STREET_ADDRESS: {
                        adr.streetAddress = catenate(adr.streetAddress, data, start, length);
                        break;
                    }
                    case STATE_ADR_LOCALITY: {
                        adr.locality = catenate(adr.locality, data, start, length);
                        break;
                    }
                    case STATE_ADR_REGION: {
                        adr.region = catenate(adr.region, data, start, length);
                        break;
                    }
                    case STATE_ADR_POSTAL_CODE: {
                        adr.postalCode = catenate(adr.postalCode, data, start, length);
                        break;
                    }
                    case STATE_ADR_COUNTRY_NAME: {
                        adr.countryName = catenate(adr.countryName, data, start, length);
                        break;
                    }
                    case STATE_ADR_TYPE: {
                        adr.type = catenate(adr.type, data, start, length);
                        break;
                    }
                    
                    // **** LABEL ****
                    case STATE_LABEL: {
                        label = catenate(label, data, start, length);
                        break;
                    }
                    
                    // **** GEO ****
                    case STATE_GEO_LATITUDE: {
                        latitude = catenate(latitude, data, start, length);
                        break;
                    }
                    case STATE_GEO_LONGITUDE: {
                        longitude = catenate(longitude, data, start, length);
                        break;
                    }
                    
                    // **** TZ ****
                    case STATE_TZ: {
                        tz = catenate(tz, data, start, length);
                        break;
                    }
                    
                    // **** PHOTO ****
                    case STATE_PHOTO: {
                        photo = catenate(photo, data, start, length);
                        break;
                    }
                    // **** LOGO ****
                    case STATE_LOGO: {
                        logo = catenate(logo, data, start, length);
                        break;
                    }
                    // **** SOUND ****
                    case STATE_SOUND: {
                        sound = catenate(sound, data, start, length);
                        break;
                    }
                    // **** BDAY ****
                    case STATE_BDAY: {
                        bday = catenate(bday, data, start, length);
                        break;
                    }
                    // **** TITLE ****
                    case STATE_TITLE: {
                        title = catenate(title, data, start, length);
                        break;
                    }
                    // **** ROLE ****
                    case STATE_ROLE: {
                        role = catenate(role, data, start, length);
                        break;
                    }
                    
                    // **** ORG ****
                    case STATE_ORG:
                    case STATE_ORG_NAME: {
                        org.name = catenate(org.name, data, start, length);
                        break;
                    }
                    case STATE_ORG_UNIT: {
                        org.unit = catenate(org.unit, data, start, length);
                        break;
                    }
                    
                    // **** CATEGORY ****
                    case STATE_CATEGORY: {
                        category = catenate(category, data, start, length);
                        break;
                    }
                    // **** NOTE ****
                    case STATE_NOTE: {
                        note = catenate(note, data, start, length);
                        break;
                    }
                    
                    // **** CLASS ****
                    case STATE_CLASS: {
                        classs = catenate(classs, data, start, length);
                        break;
                    }
                    // **** KEY ****
                    case STATE_KEY: {
                        key = catenate(key, data, start, length);
                        break;
                    }
                    case STATE_MAILER: {
                        mailer = catenate(mailer, data, start, length);
                        break;
                    }
                    case STATE_UID: {
                        uid = catenate(uid, data, start, length);
                        break;
                    }
                    case STATE_REV: {
                        rev = catenate(rev, data, start, length);
                        break;
                    }
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



        private void initCard() {
            n = new Name();
            nickname = null;
            nicknames = new ArrayList<String>();
            url = null;
            urls = new ArrayList<String>();
            email = null;
            emails = new ArrayList<Email>();
            tel = null;
            tels = new ArrayList<Tel>();
            adr = null;
            adrs = new ArrayList<Adr>();
            label = null;
            labels = new ArrayList<String>();
            
            tz = null;
            photo = null;
            photos = new ArrayList<String>();
            logo = null;
            logos = new ArrayList<String>();
            sound = null;
            sounds = new ArrayList<String>();
            
            bday = null;
            title = null;
            titles = new ArrayList<String>();
            role = null;
            roles = new ArrayList<String>();
            org = null;
            orgs = new ArrayList<Org>();
            category = null;
            categories = new ArrayList<String>();
            note = null;
            notes = new ArrayList<String>();
            
            uid = null;
            classs = null;
            
            key = null;
            keys = new ArrayList<String>();
            mailer = null;
            mailers = new ArrayList<String>();
            rev = null;
            revs = new ArrayList<String>();
            
            latitude = null;
            longitude = null;
        }
        



        public void processCard() {
            if(nickname != null)    { nicknames.add(nickname); nickname = null; }
            if(url != null)         { urls.add(url); url = null; }
            if(email != null)       { emails.add(email); email = null; }
            if(tel != null)         { tels.add(tel); tel = null; }
            if(adr != null)         { adrs.add(adr); adr = null; }
            if(label != null)       { labels.add(label); label = null; }
            if(photo != null)       { photos.add(photo); photo = null; }
            if(logo != null)        { logos.add(logo); logo = null; }
            if(sound != null)       { sounds.add(sound); sound = null; }
            if(title != null)       { titles.add(title);  title = null; }
            if(role != null)        { roles.add(role); role = null; }
            if(org != null)         { orgs.add(org); org = null;}
            if(category != null)    { categories.add(category); category = null; }
            if(note != null)        { notes.add(note); note = null; }
            if(key != null)         { keys.add(key); key = null; }
            if(mailer != null)      { mailers.add(mailer); mailer = null; }
            if(rev != null)         { revs.add(rev); rev = null; }
            
            
            if(n != null || org != null) {
                try {
                    String cardName = "";
                    
                    if(n != null) {
                        if(n.givenName != null) {
                            cardName += n.givenName.trim();
                        }
                        if(n.additionalName != null) {
                            if(cardName.length() > 0) {
                                cardName += " " + n.additionalName.trim();
                            }
                        }
                        if(n.familyName != null) {
                            if(cardName.length() > 0) cardName += " ";
                            cardName += n.familyName.trim();
                        }
                        if(n.honorifixPrefix != null) {
                            if(cardName.length() > 0) {
                                cardName = n.honorifixPrefix.trim() + " " + cardName;
                            }
                        }
                        if(n.honorifixSuffix != null) {
                            if(cardName.length() > 0) {
                                cardName = cardName + " " + n.honorifixPrefix.trim();
                            }
                        }
                        if(cardName.length() == 0) {
                            if(n.fn != null) {
                                System.out.print("n.fn == " + n.fn);
                                n.fn = n.fn.trim();
                                cardName = n.fn;
                                System.out.print("cardName == " + cardName);

                                if(n.fn.indexOf(", ") != -1) {
                                    String[] ns = n.fn.split(", ");
                                    n.familyName = ns[0];
                                    if(ns[1].indexOf(" ") != -1) {
                                        String[] ns2 = ns[1].split(" ");
                                        n.givenName = ns2[0];
                                        n.additionalName = ns2[1];
                                    }
                                    else {
                                        n.givenName = ns[1];
                                    }
                                }
                                else if(n.fn.indexOf(" ") != -1) {
                                    String[] ns = n.fn.split(" ");
                                    n.givenName = ns[0];
                                    if(ns.length > 2) {
                                        n.additionalName = ns[1];
                                        n.familyName = ns[2];
                                    }
                                    else {
                                        n.familyName = ns[1];
                                    }
                                }
                            }
                        }
                    }
                    if(cardName.length() == 0) {
                        if(org != null) {
                            if(org.name != null) {
                                cardName = org.name;
                            }
                            if(org.unit != null) {
                                if(cardName.length() > 0) {
                                    cardName = ", ";
                                }
                                cardName += org.unit;
                            }
                        }
                    }
                    if(cardName.length() == 0) {
                        parent.log("Couldn't generate name for hcard topic.");
                        cardName = "default-hcard-" + System.currentTimeMillis() + this.progress;
                    }

                    parent.log("Creating hcard for '"+cardName+"'.");

                    Topic cardTypeTopic = createTopic(tm, SI_PREFIX+"hcard", "HCard");
                    Topic cardTopic = createTopic(tm, SI_PREFIX+cardName, cardName, new Topic[] { cardTypeTopic } );
                    
                    
                    // **** NAME ****
                    if(n != null) {
                        if(n.familyName != null) {
                            createAssociationFor(n.familyName, "family-name", cardTopic, cardTypeTopic, tm);
                        }
                        if(n.additionalName != null) {
                            createAssociationFor(n.additionalName, "additional-name", cardTopic, cardTypeTopic, tm);
                        }
                        if(n.givenName != null) {
                            createAssociationFor(n.givenName, "given-name", cardTopic, cardTypeTopic, tm);
                        }
                        if(n.honorifixPrefix != null) {
                            createAssociationFor(n.honorifixPrefix, "honorific-prefix", cardTopic, cardTypeTopic, tm);
                        }
                        if(n.honorifixSuffix != null) {
                            createAssociationFor(n.honorifixSuffix, "honorific-suffix", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    
                    
                    // **** NICKNAMES ****
                    if(nicknames.size() > 0) {
                        Iterator<String> nickIterator = nicknames.iterator();
                        while(nickIterator.hasNext()) {
                            createAssociationFor(nickIterator.next(), "nickname", cardTopic, cardTypeTopic, tm);
                        }
                    }

                    // **** PHOTO ****
                    if(photos.size() > 0) {
                        Iterator<String> photoIterator = photos.iterator();
                        while(photoIterator.hasNext()) {
                            createSLAssociationFor(photoIterator.next(), "photo", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    // **** LOGO ****
                    if(logos.size() > 0) {
                        Iterator<String> logoIterator = logos.iterator();
                        while(logoIterator.hasNext()) {
                            createSLAssociationFor(logoIterator.next(), "logo", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    // **** SOUND ****
                    if(sounds.size() > 0) {
                        Iterator<String> soundIterator = sounds.iterator();
                        while(soundIterator.hasNext()) {
                            createSLAssociationFor(soundIterator.next(), "sound", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    // **** URLS ****
                    if(urls.size() > 0) {
                        Iterator<String> urlIterator = urls.iterator();
                        while(urlIterator.hasNext()) {
                            createSLAssociationFor(urlIterator.next(), "url", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    // **** TITLE ****
                    if(titles.size() > 0) {
                        Iterator<String> titleIterator = titles.iterator();
                        while(titleIterator.hasNext()) {
                            createAssociationFor(titleIterator.next(), "title", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    // **** ROLE ****
                    if(roles.size() > 0) {
                        Iterator<String> roleIterator = roles.iterator();
                        while(roleIterator.hasNext()) {
                            createAssociationFor(roleIterator.next(), "role", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    // **** CATEGORY ****
                    if(categories.size() > 0) {
                        Iterator<String> categoryIterator = categories.iterator();
                        while(categoryIterator.hasNext()) {
                            createAssociationFor(categoryIterator.next(), "category", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    if(keys.size() > 0) {
                        Iterator<String> keyIterator = keys.iterator();
                        while(keyIterator.hasNext()) {
                            createAssociationFor(keyIterator.next(), "key", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    if(mailers.size() > 0) {
                        Iterator<String> mailerIterator = mailers.iterator();
                        while(mailerIterator.hasNext()) {
                            createAssociationFor(mailerIterator.next(), "mailer", cardTopic, cardTypeTopic, tm);
                        }
                    }
                    
                    if(emails.size() > 0) {
                        Iterator<Email> emailIterator = emails.iterator();
                        while(emailIterator.hasNext()) {
                            Email email = emailIterator.next();
                            if(email.type == null) {
                                createAssociationFor(email.value, "email-address", cardTopic, cardTypeTopic, tm);
                            }
                            else {
                                createAssociationFor(email.value, "email-address", email.type, "email-address-type", "email-address", cardTopic, cardTypeTopic, tm);
                            }
                        }
                    }
                    
                    if(tels.size() > 0) {
                        Iterator<Tel> telIterator = tels.iterator();
                        while(telIterator.hasNext()) {
                            Tel tel = telIterator.next();
                            if(tel.type == null) {
                                createAssociationFor(tel.value, "telephone-number", cardTopic, cardTypeTopic, tm);
                            }
                            else {
                                createAssociationFor(tel.value, "telephone-number", tel.type, "telephone-number-type", "telephone-number", cardTopic, cardTypeTopic, tm);
                            }
                        }
                    }
                    
                    if(orgs.size() > 0) {
                        Iterator<Org> orgIterator = orgs.iterator();
                        while(orgIterator.hasNext()) {
                            Org org = orgIterator.next();
                            if(org.unit == null) {
                                createAssociationFor(org.name, "organization-name", cardTopic, cardTypeTopic, tm);
                            }
                            else {
                                createAssociationFor(org.name, "organization-name", org.unit, "organization-unit", "organization", cardTopic, cardTypeTopic, tm);
                            }
                        }
                    }
                    
                    if(latitude != null && longitude != null) {
                        createAssociationFor(latitude, "latitude", longitude, "longitude", "geo-location", cardTopic, cardTypeTopic, tm);
                    }
                    
                    createAssociationFor(classs, "class", cardTopic, cardTypeTopic, tm);
                    createAssociationFor(bday, "bday", cardTopic, cardTypeTopic, tm);
                    createAssociationFor(tz, "tz", cardTopic, cardTypeTopic, tm);
                    
                    if(adrs.size() > 0) {
                        int adrcount = 1;
                        Iterator<Adr> adrIterator = adrs.iterator();
                        while(adrIterator.hasNext()) {
                            Adr adr = adrIterator.next();
                            adrcount++;
                            
                            try {
                                Topic addressTypeTopic = createTopic(tm, SI_PREFIX+"adr", "address");
                                String address = "";
                                if(adr.streetAddress != null) address += adr.streetAddress.trim()+", ";
                                if(adr.extendedAddress != null) address += adr.extendedAddress.trim()+", ";
                                if(adr.locality != null) address += adr.locality.trim()+", ";
                                if(adr.region != null) address += adr.region.trim()+", ";
                                if(adr.postalCode != null) address += adr.postalCode.trim()+", ";
                                if(adr.countryName != null) address += adr.countryName.trim()+", ";
                                
                                if(address.length() < 2) address = "Address "+ (adrcount>1?adrcount+" ":"") +"of "+cardName;
                                else address = address.substring(0, address.length()-2);
                                
                                Topic addressTopic = createTopic(tm, SI_PREFIX+"adr/"+address, address, new Topic[] { addressTypeTopic } );

                                createAssociationFor(adr.streetAddress, "street-address", addressTopic, addressTypeTopic, tm);
                                createAssociationFor(adr.postOfficeBox, "post-office-box", addressTopic, addressTypeTopic, tm);
                                createAssociationFor(adr.extendedAddress, "extended-address", addressTopic, addressTypeTopic, tm);
                                createAssociationFor(adr.locality, "locality", addressTopic, addressTypeTopic, tm);
                                createAssociationFor(adr.region, "region", addressTopic, addressTypeTopic, tm);
                                createAssociationFor(adr.postalCode, "postal-code", addressTopic, addressTypeTopic, tm);
                                createAssociationFor(adr.countryName, "country-name", addressTopic, addressTypeTopic, tm);
                                
                                Association cardAddress = tm.createAssociation(addressTypeTopic);
                                cardAddress.addPlayer(cardTopic, cardTypeTopic);
                                cardAddress.addPlayer(addressTopic, addressTypeTopic);
                                
                                if(adr.type != null) {
                                    Topic adrTypeTypeTopic = createTopic(tm, SI_PREFIX+"adr-type/", "address-type");
                                    Topic adrTypeTopic = createTopic(tm, SI_PREFIX+"adr-type/"+adr.type, adr.type,  new Topic[] { adrTypeTypeTopic });
                                    cardAddress.addPlayer(adrTypeTopic, adrTypeTypeTopic);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }

                   
                    // OK, DON'T GENERATE ANY MORE TOPICS...
                    initCard();

                    progress++;
                }
                catch(Exception e) {
                    parent.log(e);
                }
            }
        }

        
        private void createSLAssociationFor(String sl, String associationTypeName, Topic player, Topic role, TopicMap tm) {
            if(sl != null) {
                sl = sl.trim();
                if(sl.length() > 0) {
                    try {
                        Topic associationTypeTopic = createTopic(tm, SI_PREFIX+associationTypeName, associationTypeName);
                        Topic playerTopic = null;
                        if(sl.startsWith("http://")) {
                            playerTopic = createTopic(tm, sl, sl);
                            playerTopic.setSubjectLocator(new org.wandora.topicmap.Locator(sl));
                        }
                        else {
                            // SL IS NOT REAL URL!
                            playerTopic = createTopic(tm, SI_PREFIX+associationTypeName+"/"+sl, sl);
                        }
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

        
        private void createAssociationFor(String basename1, String role1, String basename2, String role2, String associationTypeName, Topic player, Topic role, TopicMap tm) {
            if(basename1 != null && basename2 != null && role1 != null && role2 != null) {
                basename1 = basename1.trim();
                basename2 = basename2.trim();
                role1 = role1.trim();
                role2 = role2.trim();
                if(basename1.length() > 0 && basename2.length() > 0 && role1.length() > 0 && role2.length() > 0) {
                    try {
                        Topic associationTypeTopic = createTopic(tm, SI_PREFIX+associationTypeName, associationTypeName);
                        Topic player1Topic = createTopic(tm, SI_PREFIX+role1+"/"+basename1, basename1);
                        Topic player2Topic = createTopic(tm, SI_PREFIX+role2+"/"+basename2, basename2);
                        Topic role1Topic = createTopic(tm, SI_PREFIX+role1, role1);
                        Topic role2Topic = createTopic(tm, SI_PREFIX+role2, role2);
                        Association association = tm.createAssociation(associationTypeTopic);
                        association.addPlayer(player, role);
                        association.addPlayer(player1Topic, role1Topic);
                        association.addPlayer(player2Topic, role2Topic);
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
    
    
    
    
    
    
    private class Name {
        public String fn = null;
        public String name = null;
        public String familyName = null;
        public String givenName = null;
        public String additionalName = null;
        public String honorifixPrefix = null;
        public String honorifixSuffix = null;
        public String sortString = null;
    }
    
    
    
    private class Adr {
        public String type = null;
        public String postOfficeBox = null;
        public String extendedAddress = null;
        public String streetAddress = null;
        public String locality = null;
        public String region = null;
        public String postalCode = null;
        public String countryName = null;
        public String adr = null;
        
        public Adr() {
            
        }
    }
    
    
    private class Tel {
        public String type = null;
        public String value = null;
        
        public Tel() {
            
        }
    }
    
    private class Email {
        public String type = null;
        public String value = null;
        
        public Email() {
            
        }
    }
    
    private class Org {
        public String name = null;
        public String unit = null;
        
        public Org() {
            
        }
    }
}
