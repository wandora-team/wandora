/*
 * WANDORA - Knowledge Extraction, Management, and Publishing Application
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
 * AtomExtractor.java
 *
 * Created on 28. marraskuuta 2008, 13:18
 *
 */

package org.wandora.application.tools.extractors;




import java.net.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import org.wandora.application.Wandora;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.utils.*;



/**
 * http://www.atomenabled.org/developers/syndication/
 *
 * @author akivela
 */



public class AtomExtractor extends AbstractExtractor {
    
    public static final String DEFAULT_LANG = "en";
    private String baseUrl = null;
    
    
    /** Creates a new instance of AtomExtractor */
    public AtomExtractor() {
    }
    
    @Override
    public String getName() {
        return "Atom Extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extractor reads Atom news feed and converts the feed to a topic map.";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_atom.png");
    }
    
    private final String[] contentTypes=new String[] { "text/xml", "application/xml", "application/atom+xml", "application/xhtml+xml" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }


    @Override
    public boolean useURLCrawler() {
        return false;
    }

    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        baseUrl = solveBaseUrl(url.toExternalForm());
        boolean answer = _extractTopicsFrom(url.openStream(), topicMap);
        baseUrl = null;
        return answer;
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        baseUrl = solveBaseUrl(file.toURI().toURL().toExternalForm());
        boolean answer = _extractTopicsFrom(new FileInputStream(file), topicMap);
        baseUrl = null;
        return answer;
    }



    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
        baseUrl = null;
        return answer;
    }
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        AtomParser parserHandler = new AtomParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try {
            reader.parse(new InputSource(in));
        }
        catch(Exception e) {
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.entryCount + " Atom entries processed!");
        baseUrl = null;
        return true;
    }
    
    
    
    private String solveBaseUrl(String url) {
        if(url != null) {
            int index = url.lastIndexOf('/');
            if(index > 0) {
                url = url.substring(0, index);
            }
        }
        return url;
    }




    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        try {
            setWandora(wandora);
            String urlstr = request.getSource();

            // System.out.println("FOUND URL: "+urlstr);

            URL url = new URL(urlstr);
            URLConnection uc = url.openConnection();
            String type = uc.getContentType();
            // System.out.println("FOUND TYPE: "+type);

            if(type != null && type.indexOf("text/html") > -1) {
                String htmlContent = IObox.doUrl(url);
                Pattern p1 = Pattern.compile("\\<link [^\\>]+\\>");
                Pattern p2 = Pattern.compile("href\\=\"([^\"]+)\"");

                Matcher m1 = p1.matcher(htmlContent);
                int sp = 0;
                while(m1.find(sp)) {
                    String linktag = m1.group();
                    if(linktag != null && linktag.length() > 0) {
                        if(linktag.indexOf("application/atom+xml") > -1) {
                            Matcher m2 = p2.matcher(linktag);
                            if(m2.find()) {
                                try {
                                    String atomfeed = m2.group(1);
                                    _extractTopicsFrom(new URL(atomfeed), wandora.getTopicMap());
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    sp = m1.end();
                }
                wandora.doRefresh();
                return null;
            }
            else if(type != null && type.indexOf("application/atom+xml") > -1) {
                _extractTopicsFrom(url, wandora.getTopicMap());
                wandora.doRefresh();
                return null;
            }
            else {
                return BrowserPluginExtractor.RETURN_ERROR + "Couldn't solve browser extractor content. Nothing extracted.";
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }
    }





    /* ---------------------------------------------------------------------- */



    private class AtomParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public boolean MAKE_LINK_SUBJECT_LOCATOR = false;
        public boolean MAKE_SUBCLASS_OF_WANDORA_CLASS = true;
        
        public AtomParser(TopicMap tm, AtomExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        public int entryCount = 0;
        public int feedCount = 0;
        
        private TopicMap tm;
        private AtomExtractor parent;
        
        public static final String TAG_FEED="feed";
        public static final String TAG_TITLE="title";
        public static final String TAG_LINK="link";
        public static final String TAG_UPDATED="updated";
        public static final String TAG_AUTHOR="author";
        public static final String TAG_NAME="name";
        public static final String TAG_EMAIL="email";
        public static final String TAG_URI="uri";
        public static final String TAG_ID="id";
        
        public static final String TAG_ENTRY="entry";
        public static final String TAG_SUMMARY="summary";
        
        public static final String TAG_CATEGORY="category";
        public static final String TAG_CONTRIBUTOR="contributor";
        
        public static final String TAG_GENERATOR="generator";
        public static final String TAG_ICON="icon";
        public static final String TAG_LOGO="logo";
        public static final String TAG_RIGHTS="rights";
        public static final String TAG_SUBTITLE="subtitle";
        
        public static final String TAG_CONTENT="content";
        public static final String TAG_PUBLISHED="published";
        
        public static final String TAG_SOURCE="source";


       
        
        private static final int STATE_START=0;
        private static final int STATE_FEED=2;
        private static final int STATE_FEED_TITLE=4;
        private static final int STATE_FEED_LINK=5;
        private static final int STATE_FEED_UPDATED=6;
        private static final int STATE_FEED_AUTHOR=7;
        private static final int STATE_FEED_AUTHOR_NAME=71;
        private static final int STATE_FEED_AUTHOR_URI=72;
        private static final int STATE_FEED_AUTHOR_EMAIL=73;
        
        private static final int STATE_FEED_ID=8;
        private static final int STATE_FEED_CATEGORY=9;
        private static final int STATE_FEED_GENERATOR=15;
        private static final int STATE_FEED_CONTRIBUTOR=10;
        private static final int STATE_FEED_CONTRIBUTOR_NAME=101;
        private static final int STATE_FEED_CONTRIBUTOR_EMAIL=102;
        private static final int STATE_FEED_CONTRIBUTOR_URI=103;
        
        private static final int STATE_FEED_ICON=11;
        private static final int STATE_FEED_LOGO=12;
        private static final int STATE_FEED_RIGHTS=13;
        private static final int STATE_FEED_SUBTITLE=14;
        
        private static final int STATE_FEED_ENTRY = 1000;
        private static final int STATE_FEED_ENTRY_ID = 1001;
        private static final int STATE_FEED_ENTRY_TITLE = 1002;
        private static final int STATE_FEED_ENTRY_UPDATED = 1003;
        private static final int STATE_FEED_ENTRY_AUTHOR = 1004;
        private static final int STATE_FEED_ENTRY_AUTHOR_NAME = 10041;
        private static final int STATE_FEED_ENTRY_AUTHOR_EMAIL = 10042;
        private static final int STATE_FEED_ENTRY_AUTHOR_URI = 10043;
        
        private static final int STATE_FEED_ENTRY_CONTENT = 1005;
        private static final int STATE_FEED_ENTRY_LINK = 1006;
        private static final int STATE_FEED_ENTRY_SUMMARY = 1007;
        
        private static final int STATE_FEED_ENTRY_CATEGORY=1008;
        private static final int STATE_FEED_ENTRY_CONTRIBUTOR=1009;
        private static final int STATE_FEED_ENTRY_CONTRIBUTOR_NAME=10091;
        private static final int STATE_FEED_ENTRY_CONTRIBUTOR_EMAIL=10092;
        private static final int STATE_FEED_ENTRY_CONTRIBUTOR_URI=10093;
        
        private static final int STATE_FEED_ENTRY_PUBLISHED = 1010;
        
        private static final int STATE_FEED_ENTRY_SOURCE = 1011;
        private static final int STATE_FEED_ENTRY_SOURCE_ID = 10111;
        private static final int STATE_FEED_ENTRY_SOURCE_TITLE = 10112;
        private static final int STATE_FEED_ENTRY_SOURCE_UPDATED = 10113;
        private static final int STATE_FEED_ENTRY_SOURCE_RIGHTS = 10114;
        
        private static final int STATE_FEED_ENTRY_RIGHTS = 1012;
        
        
        
        
        private int state=STATE_START;
        
        public String ATOMSI = "http://www.w3.org/2005/Atom/";

        public String SIPREFIX="http://www.w3.org/2005/Atom/";
        public String FEED_ID_SI=SIPREFIX+"id";
        public String FEED_SI=SIPREFIX+"channel";
        public String FEED_UPDATED_SI=SIPREFIX+"updated";
        public String FEED_AUTHOR_SI=SIPREFIX+"author";
        public String FEED_CONTRIBUTOR_SI=SIPREFIX+"contributor";
        public String FEED_CATEGORY_SI=SIPREFIX+"category";
        public String FEED_SUBTITLE_SI=SIPREFIX+"subtitle";
        
        public String EMAIL_ADDRESS_SI=SIPREFIX+"email";
        public String SCHEME_SI=SIPREFIX+"scheme";
        
        
        
        
        
        public String FEED_LINK_SI=FEED_SI+"/link";
        public String FEED_GENERATOR_SI=FEED_SI+"/generator";
        public String FEED_GENERATOR_URI_SI=FEED_SI+"/generator-uri";
        public String FEED_GENERATOR_VERSION_SI=FEED_SI+"/generator-version";
        
        public String FEED_RIGHTS_SI=FEED_SI+"/rights";
        public String FEED_ICON_SI=FEED_SI+"/icon";
        public String FEED_LOGO_SI=FEED_SI+"/logo";
        
        
        public String FEED_ENTRY_SI=FEED_SI+"/entry";
        public String FEED_ENTRY_ID_SI=FEED_ENTRY_SI+"/id";
        public String FEED_ENTRY_TITLE_SI=FEED_ENTRY_SI+"/title";
        public String FEED_ENTRY_LINK_SI=FEED_ENTRY_SI+"/link";
        public String FEED_ENTRY_CONTRIBUTOR_SI=FEED_ENTRY_SI+"/contributor";
        public String FEED_ENTRY_SUMMARY_SI=FEED_ENTRY_SI+"/summary";
        public String FEED_ENTRY_CONTENT_SI=FEED_ENTRY_SI+"/content";
        
        public String FEED_ENTRY_PUBLISHED_SI=FEED_ENTRY_SI+"/published";
        public String FEED_ENTRY_UPDATED_SI=FEED_ENTRY_SI+"/updated";
        public String FEED_ENTRY_CATEGORY_SI=FEED_ENTRY_SI+"/category";
        public String FEED_ENTRY_AUTHOR_SI=FEED_ENTRY_SI+"/author";
        public String FEED_ENTRY_RIGHTS_SI=FEED_ENTRY_SI+"/rights";
        
        public String FEED_ENTRY_SOURCE_SI=FEED_ENTRY_SI+"/source";
        
        public String LINK_HREF_SI=SIPREFIX+"link/href"; 
        public String LINK_REL_SI=SIPREFIX+"link/rel";  
        public String LINK_TYPE_SI=SIPREFIX+"link/type";
        public String LINK_HREF_LANG_SI=SIPREFIX+"link/href-lang"; 
        
        public String DATE_SI="http://wandora.org/si/date"; 
        public String RIGHTS_SI="http://wandora.org/si/rights"; 
        public String LINK_SI="http://wandora.org/si/link";
        
        
        
        
        private String data_feed_id;
        private String data_feed_title;
        private String data_feed_title_type;
        private Link data_feed_link;
        
        private String data_feed_updated;
        
        private String data_feed_author_name;
        private String data_feed_author_uri;
        private String data_feed_author_email;
        private Category data_feed_category;
        
        private String data_feed_contributor_name;
        private String data_feed_contributor_uri;
        private String data_feed_contributor_email;
                
        private String data_feed_generator;
        private String data_feed_generator_uri;
        private String data_feed_generator_version;
        
        private String data_feed_icon;
        private String data_feed_logo;
        private String data_feed_rights;
        private String data_feed_rights_type;
        private String data_feed_subtitle;
        private String data_feed_subtitle_type;
        
        private String data_entry_id;
        private String data_entry_title;
        private String data_entry_title_type;
                
        private String data_entry_updated;
        private String data_entry_published;
        
        private String data_entry_author_name;
        private String data_entry_author_uri;
        private String data_entry_author_email;
        private String data_entry_content;
        private String data_entry_content_type;
        private String data_entry_content_src;
        
        private Link data_entry_link;
        
        private String data_entry_summary;
        private String data_entry_summary_src;
        private String data_entry_summary_type;
        
        private Category data_entry_category;
        
        private String data_entry_contributor_name;
        private String data_entry_contributor_uri;
        private String data_entry_contributor_email;
        
        private String data_entry_source_id;
        private String data_entry_source_title;
        private String data_entry_source_updated;
        private String data_entry_source_rights;
        private String data_entry_source_rights_type;
        
        private String data_entry_rights;
        private String data_entry_rights_type;
        
        private Topic theFeed;
        private Topic theEntry;
        
        private org.wandora.topicmap.Locator theFeedSI = null;
        private org.wandora.topicmap.Locator theEntrySI = null;

        
        
        private Topic getOrCreateTopic(String si) throws TopicMapException {
            return getOrCreateTopic(si,null);
        }
        private Topic getOrCreateTopic(String si,String bn) throws TopicMapException {
            if(si!=null){
                si = TopicTools.cleanDirtyLocator(si);
                Topic t=tm.getTopic(si);
                if(t==null){
                    t=tm.createTopic();
                    t.addSubjectIdentifier(tm.createLocator(si));
                    if(bn!=null) t.setBaseName(bn);
                }
                return t;
            }
            else{
                Topic t=tm.getTopicWithBaseName(bn);
                if(t==null){
                    t=tm.createTopic();
                    t.setBaseName(bn);
                    if(si!=null) t.addSubjectIdentifier(tm.createLocator(si));
                    else t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                }
                return t;
            }
        }
        
        
        private Topic getFeedType() {
            try {
                return getOrCreateTopic(FEED_SI, "Atom Feed");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        private Topic getEntryType() {
            try {
                return getOrCreateTopic(FEED_ENTRY_SI, "Atom Entry");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        private Topic getDateType() {
            try {
                return getOrCreateTopic(DATE_SI, "Date");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        private Topic getDateTopic(String d) {
            Topic dateTopic = null;
            try {
                dateTopic = tm.getTopic(DATE_SI+d);
                if(dateTopic == null) {
                    dateTopic = tm.createTopic();
                    dateTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(DATE_SI+d));
                    dateTopic.setBaseName(d);
                    dateTopic.setDisplayName(DEFAULT_LANG, d);
                    dateTopic.addType(getDateType());
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            return dateTopic;
        }
        
        

        
        private Topic getRightsType() {
            try {
                return getOrCreateTopic(RIGHTS_SI, "Rights");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        private Topic getRightsTopic(String r) {
            Topic rightsTopic = null;
            try {
                rightsTopic = tm.getTopic(RIGHTS_SI+r);
                if(rightsTopic == null) {
                    rightsTopic = tm.createTopic();
                    rightsTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(RIGHTS_SI+r));
                    rightsTopic.setBaseName(r);
                    rightsTopic.setDisplayName(DEFAULT_LANG, r);
                    rightsTopic.addType(getRightsType());
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            return rightsTopic;
        }
        
        
        
        
        private Topic getLinkType() {
            try {
                return getOrCreateTopic(LINK_SI, "Atom Link");
            }
            catch(Exception e) {
                parent.log(e);
            }
            return null;
        }
        
        
        private String makeUrl(String url) {
            if(url != null) {
                if( !url.matches("[a-zA-Z0-9]+\\:\\/\\/.*?") ) {
                    if(parent.baseUrl != null) {
                        if(!url.startsWith("/")) url =  "/" + url;
                        url = parent.baseUrl + url;
                    }
                }
            }
            return url;
        }

        
        private void createLinkStruct(Link link, Topic player, Topic role) throws TopicMapException  {
            String href = link.getHref();
            if(link != null && isValid(href)) {
                Topic linkType = getLinkType();
                Topic hrefTopic = getOrCreateTopic(makeUrl(href));
                Topic hrefType = getOrCreateTopic(LINK_HREF_SI, "Atom Link Href");
                if(isValid(link.getTitle())) {
                    hrefTopic.setBaseName(link.getTitle());
                    hrefTopic.setDisplayName(DEFAULT_LANG, link.getTitle());
                }
                Association linka = tm.createAssociation(linkType);
                linka.addPlayer(player, role);
                linka.addPlayer(hrefTopic, hrefType);
                
                if(isValid(link.getRel())) {
                    Topic relTopic = getOrCreateTopic(link.getRel());
                    Topic relType = getOrCreateTopic(LINK_REL_SI, "Atom Link Rel");
                    linka.addPlayer(relTopic, relType);
                }
                if(isValid(link.getType())) {
                    Topic hrefTypeTopic = getOrCreateTopic(LINK_TYPE_SI+"/"+link.getType(), link.getType());
                    hrefTopic.addType(hrefTypeTopic);
                }
                if(isValid(link.getHrefLang())) {
                    Topic hrefLangType = getOrCreateTopic(LINK_HREF_LANG_SI, "Atom Link Href Lang");
                    Topic hrefLangTopic = getOrCreateTopic(LINK_HREF_LANG_SI+"/"+link.getHrefLang(), link.getHrefLang());
                    Association la = tm.createAssociation(hrefLangType);
                    la.addPlayer(hrefLangTopic, hrefLangType);
                    la.addPlayer(hrefTopic, hrefType);
                }
            }
        }
        
        
        
        
        private String postProcessFeedText(String txt, String type) {
            if(txt != null && type!=null && type.length()>0) {
                if("text".equals(type)) {}
                else if("html".equals(type)) { txt = HTMLEntitiesCoder.decode(txt); }
                else if("xhtml".equals(type)) {
                    txt = txt.replaceAll("\\&lt\\;", "<");
                    txt = txt.replaceAll("\\&gt\\;", ">");
                    txt = txt.replaceAll("\\&amp\\;", "&");
                }
            }
            return txt;
        }
        
        
        
        private boolean isValid(String str) {
            if(str == null || str.trim().length() == 0) return false;
            return true;
        }
        
        
        
        
        // ---------------------------------------------------------------------
        
        
        
        public void startDocument() throws SAXException {
            feedCount = 0;
            entryCount = 0;
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            //parent.log("START" + state +" --- " + qName);
            
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START:
                    if(qName.equals(TAG_FEED)) {
                        try {
                            Topic feedType=getFeedType();
                            theFeed = tm.createTopic();
                            theFeedSI = TopicTools.createDefaultLocator();
                            theFeed.addSubjectIdentifier(theFeedSI);
                            theFeed.addType(feedType);
                            feedCount++;
                            
                            Topic atomClass = getOrCreateTopic(ATOMSI, "Atom");
                            Topic superClass = getOrCreateTopic(XTMPSI.SUPERCLASS);
                            Topic subClass = getOrCreateTopic(XTMPSI.SUBCLASS);
                            Topic supersubClass = getOrCreateTopic(XTMPSI.SUPERCLASS_SUBCLASS);

                            Association supersubClassAssociation = tm.createAssociation(supersubClass);
                            supersubClassAssociation.addPlayer(atomClass, superClass);
                            supersubClassAssociation.addPlayer(feedType, subClass);

                            if(MAKE_SUBCLASS_OF_WANDORA_CLASS) {
                                Topic wandoraClass = getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class");
                           
                                supersubClassAssociation = tm.createAssociation(supersubClass);
                                supersubClassAssociation.addPlayer(wandoraClass, superClass);
                                supersubClassAssociation.addPlayer(atomClass, subClass);
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        
                        data_feed_id = "";
                        data_feed_title = "";
                        data_feed_title_type = "";
                        data_feed_updated = "";
                        data_feed_category = null;
                        data_feed_link = null;
                        data_feed_generator = "";
                        data_feed_icon = "";
                        data_feed_logo = "";
                        data_feed_rights = "";
                        data_feed_subtitle = "";
                        data_feed_subtitle_type = "";
                        state = STATE_FEED;
                    }
                    break;
                case STATE_FEED:
                    if(qName.equals(TAG_TITLE)) {
                        data_feed_title_type = atts.getValue("type");
                        data_feed_title = "";
                        state = STATE_FEED_TITLE;
                    }
                    else if(qName.equals(TAG_LINK)) {
                        String href = atts.getValue("href");
                        String rel = atts.getValue("rel");
                        String type = atts.getValue("type");
                        String hreflang = atts.getValue("hreflang");
                        String title = atts.getValue("title");
                        String length = atts.getValue("length");
                        data_feed_link = new Link(href, rel, type, hreflang, title, length);
                        state = STATE_FEED_LINK;
                    }
                    else if(qName.equals(TAG_UPDATED)) {
                        data_feed_updated = "";
                        state = STATE_FEED_UPDATED;
                    }
                    else if(qName.equals(TAG_AUTHOR)) {
                        data_feed_author_name = "";
                        data_feed_author_uri = "";
                        data_feed_author_email = "";
                        state = STATE_FEED_AUTHOR;
                    }
                    else if(qName.equals(TAG_ID)) {
                        data_feed_id = "";
                        state = STATE_FEED_ID;
                    }
                    else if(qName.equals(TAG_CATEGORY)) {
                        String term = atts.getValue("term");
                        String scheme = atts.getValue("scheme");
                        String label = atts.getValue("label");
                        data_feed_category = new Category(term, scheme, label);
                        state = STATE_FEED_CATEGORY;
                    }
                    else if(qName.equals(TAG_CONTRIBUTOR)) {
                        data_feed_contributor_name = "";
                        data_feed_contributor_uri = "";
                        data_feed_contributor_email = "";
                        state = STATE_FEED_CONTRIBUTOR;
                    }
                    else if(qName.equals(TAG_GENERATOR)) {
                        data_feed_generator = "";
                        data_feed_generator_uri = atts.getValue("uri");
                        data_feed_generator_version = atts.getValue("version");
                        state = STATE_FEED_GENERATOR;
                    }
                    else if(qName.equals(TAG_ICON)) {
                        data_feed_icon = "";
                        state = STATE_FEED_ICON;
                    }
                    else if(qName.equals(TAG_LOGO)) {
                        data_feed_logo = "";
                        state = STATE_FEED_LOGO;
                    }
                    else if(qName.equals(TAG_RIGHTS)) {
                        data_feed_rights = "";
                        data_feed_rights_type = atts.getValue("type");
                        state = STATE_FEED_RIGHTS;
                    }
                    else if(qName.equals(TAG_SUBTITLE)) {
                        data_feed_subtitle_type = atts.getValue("type");
                        data_feed_subtitle = "";
                        state = STATE_FEED_SUBTITLE;
                    }
                    else if(qName.equals(TAG_ENTRY)) {
                        try {
                            Topic entryType=getEntryType();
                            theEntry = tm.createTopic();
                            theEntrySI = TopicTools.createDefaultLocator();
                            theEntry.addSubjectIdentifier(theEntrySI);
                            theEntry.addType(entryType);
                            entryCount++;
                            parent.setProgress(entryCount);
                            
                            Topic atomClass = getOrCreateTopic(ATOMSI, "Atom");
                            Topic superClass = getOrCreateTopic(XTMPSI.SUPERCLASS);
                            Topic subClass = getOrCreateTopic(XTMPSI.SUBCLASS);
                            Topic supersubClass = getOrCreateTopic(XTMPSI.SUPERCLASS_SUBCLASS);

                            Association supersubClassAssociation = tm.createAssociation(supersubClass);
                            supersubClassAssociation.addPlayer(atomClass, superClass);
                            supersubClassAssociation.addPlayer(entryType, subClass);

                            if(MAKE_SUBCLASS_OF_WANDORA_CLASS) {
                                Topic wandoraClass = getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class");
                                supersubClassAssociation = tm.createAssociation(supersubClass);
                                supersubClassAssociation.addPlayer(wandoraClass, superClass);
                                supersubClassAssociation.addPlayer(atomClass, subClass);
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        
                        data_entry_id = "";
                        data_entry_title = "";
                        data_entry_title_type = "";
                        
                        data_entry_updated = "";
                        data_entry_published = "";
                        
                        data_entry_author_name = "";
                        data_entry_author_uri = "";
                        data_entry_author_email = "";
                        
                        data_entry_content = "";
                        data_entry_content_type = "";
                        data_entry_content_src = "";

                        data_entry_link = null;

                        data_entry_summary = "";
                        data_entry_summary_src = "";
                        data_entry_summary_type = "";

                        data_entry_category = null;

                        data_entry_contributor_name = "";
                        data_entry_contributor_uri = "";
                        data_entry_contributor_email = "";

                        data_entry_source_id = "";
                        data_entry_source_title = "";
                        data_entry_source_updated = "";
                        data_entry_source_rights = "";
                        data_entry_source_rights_type = "";

                        data_entry_rights = "";
                        data_entry_rights_type = "";
                        
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                    
                    
                case STATE_FEED_AUTHOR:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_AUTHOR_NAME;
                        data_feed_author_name = "";
                    }
                    else if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_AUTHOR_URI;
                        data_feed_author_uri = "";
                    }
                    else if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_AUTHOR_EMAIL;
                        data_feed_author_email = "";
                    }
                    break;
                
                case STATE_FEED_CONTRIBUTOR:
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_CONTRIBUTOR_NAME;
                        data_feed_contributor_name = "";
                    }
                    else if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_CONTRIBUTOR_URI;
                        data_feed_contributor_uri = "";
                    }
                    else if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_CONTRIBUTOR_EMAIL;
                        data_feed_contributor_email = "";
                    }
                    break;
                    
                    
                case STATE_FEED_ENTRY:
                    if(qName.equals(TAG_ID)) {
                        state = STATE_FEED_ENTRY_ID;
                        data_entry_id = "";
                    }
                    else if(qName.equals(TAG_TITLE)) {
                        state = STATE_FEED_ENTRY_TITLE;
                        data_entry_title = "";
                        data_entry_title_type = atts.getValue("type");
                    }
                    else if(qName.equals(TAG_UPDATED)) {
                        state = STATE_FEED_ENTRY_UPDATED;
                        data_entry_updated = "";
                    }
                    else if(qName.equals(TAG_AUTHOR)) {
                        data_entry_author_name = "";
                        data_entry_author_uri = "";
                        data_entry_author_email = "";
                        state = STATE_FEED_ENTRY_AUTHOR;
                    }
                    else if(qName.equals(TAG_CONTENT)) {
                        state = STATE_FEED_ENTRY_CONTENT;
                        data_entry_content = "";
                        data_entry_content_src = atts.getValue("src");
                        data_entry_content_type = atts.getValue("type");
                    }
                    else if(qName.equals(TAG_LINK)) {
                        state = STATE_FEED_ENTRY_LINK;
                        String href = atts.getValue("href");
                        String rel = atts.getValue("rel");
                        String type = atts.getValue("type");
                        String hreflang = atts.getValue("hreflang");
                        String title = atts.getValue("title");
                        String length = atts.getValue("length");
                        data_entry_link = new Link(href, rel, type, hreflang, title, length);
                    }
                    else if(qName.equals(TAG_SUMMARY)) {
                        state = STATE_FEED_ENTRY_SUMMARY;
                        data_entry_summary = "";
                        data_entry_summary_src = atts.getValue("src");
                        data_entry_summary_type = atts.getValue("type");
                    }
                    else if(qName.equals(TAG_CATEGORY)) {
                        state = STATE_FEED_ENTRY_CATEGORY;
                        String term = atts.getValue("term");
                        String scheme = atts.getValue("scheme");
                        String label = atts.getValue("label");
                        data_entry_category = new Category(term, scheme, label);
                    }
                    else if(qName.equals(TAG_CONTRIBUTOR)) {
                        state = STATE_FEED_ENTRY_CONTRIBUTOR;
                        data_entry_contributor_name = "";
                        data_entry_contributor_email = "";
                        data_entry_contributor_uri = "";
                    }
                    else if(qName.equals(TAG_PUBLISHED)) {
                        state = STATE_FEED_ENTRY_PUBLISHED;
                        data_entry_published = "";
                    }
                    else if(qName.equals(TAG_SOURCE)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                        data_entry_source_id = "";
                        data_entry_source_title = "";
                        data_entry_source_updated = "";
                        data_entry_source_rights = "";
                        data_entry_source_rights_type = "";
                    }
                    else if(qName.equals(TAG_RIGHTS)) {
                        state = STATE_FEED_ENTRY_RIGHTS;
                        data_entry_rights = "";
                        data_entry_rights_type = atts.getValue("type");
                    }
                    break;
                case STATE_FEED_ENTRY_AUTHOR:
                    if(qName.equals(TAG_NAME)) {
                        data_entry_author_name = "";
                        state = STATE_FEED_ENTRY_AUTHOR_NAME;
                    }
                    else if(qName.equals(TAG_URI)) {
                        data_entry_author_uri = "";
                        state = STATE_FEED_ENTRY_AUTHOR_URI;
                    }
                    else if(qName.equals(TAG_EMAIL)) {
                        data_entry_author_email = "";
                        state = STATE_FEED_ENTRY_AUTHOR_EMAIL;
                    }
                    break;
                    
                case STATE_FEED_ENTRY_CONTRIBUTOR:
                    if(qName.equals(TAG_NAME)) {
                        data_entry_contributor_name = "";
                        state = STATE_FEED_ENTRY_CONTRIBUTOR_NAME;
                    }
                    else if(qName.equals(TAG_URI)) {
                        data_entry_contributor_uri = "";
                        state = STATE_FEED_ENTRY_CONTRIBUTOR_URI;
                    }
                    else if(qName.equals(TAG_EMAIL)) {
                        data_entry_contributor_email = "";
                        state = STATE_FEED_ENTRY_CONTRIBUTOR_EMAIL;
                    }
                    break;
                    
                case STATE_FEED_ENTRY_SOURCE:
                    if(qName.equals(TAG_ID)) {
                        data_entry_source_id = "";
                        state = STATE_FEED_ENTRY_SOURCE_ID;
                    }
                    else if(qName.equals(TAG_TITLE)) {
                        data_entry_source_title = "";
                        state = STATE_FEED_ENTRY_SOURCE_TITLE;
                    }
                    else if(qName.equals(TAG_UPDATED)) {
                        data_entry_source_updated = "";
                        state = STATE_FEED_ENTRY_SOURCE_UPDATED;
                    }
                    else if(qName.equals(TAG_RIGHTS)) {
                        data_entry_source_rights_type = atts.getValue("type");
                        data_entry_source_rights = "";
                        state = STATE_FEED_ENTRY_SOURCE_RIGHTS;
                    }
                    break;
            }
        }
        
        
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            //parent.log("  END" + state +" --- " + qName);
            
            switch(state) {
                case STATE_FEED: {
                    if(qName.equals(TAG_FEED)) {
                        state = STATE_START;
                    }
                    break;
                }
                case STATE_FEED_TITLE: {
                    if(qName.equals(TAG_TITLE)) {
                        if(theFeed != null && isValid(data_feed_title)) {
                            try {
                                data_feed_title = postProcessFeedText(data_feed_title, data_feed_title_type);
                                theFeed.setBaseName(data_feed_title + " (Atom feed)");
                                theFeed.setDisplayName(DEFAULT_LANG, data_feed_title);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_LINK: {
                    if(qName.equals(TAG_LINK)) {
                        if(theFeed != null && data_feed_link != null) {
                            try {
                                createLinkStruct(data_feed_link, theFeed, getFeedType());
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_UPDATED: {
                    if(qName.equals(TAG_UPDATED)) {
                        if(theFeed != null && isValid(data_feed_updated)) {
                            try {
                                Topic updatedType = getOrCreateTopic(FEED_UPDATED_SI,"Atom Feed Updated");
                                Topic dateTopic = getDateTopic(data_feed_updated);
                                Association a = tm.createAssociation(updatedType);
                                a.addPlayer(theFeed, getFeedType());
                                a.addPlayer(dateTopic, updatedType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_AUTHOR: {
                    if(qName.equals(TAG_AUTHOR)) {
                        if(theFeed != null && isValid(data_feed_author_name)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic authorType = getOrCreateTopic(FEED_AUTHOR_SI,"Atom Feed Author");
                                String authorSI = !isValid(data_feed_author_uri) ? FEED_AUTHOR_SI + "/" + data_feed_author_name : data_feed_author_uri;
                                Topic theAuthor = getOrCreateTopic(authorSI, data_feed_author_name);
                                theAuthor.addType(authorType);
                                Association feedAuthor = tm.createAssociation(authorType);
                                feedAuthor.addPlayer(theFeed, feedType);
                                feedAuthor.addPlayer(theAuthor, authorType);
                                
                                if(isValid(data_feed_author_email)) {
                                    Topic emailType = getOrCreateTopic(EMAIL_ADDRESS_SI,"Email address");
                                    parent.setData(theAuthor, emailType, DEFAULT_LANG, data_feed_author_email);
                                }
                                
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_AUTHOR_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_AUTHOR_URI: {
                    if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_AUTHOR_EMAIL: {
                    if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_ID: {
                    if(qName.equals(TAG_ID)) {
                        if(theFeed != null && isValid(data_feed_id)) {
                            try {                              
                                if(data_feed_id.startsWith("http://")) {
                                    theFeed.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_feed_id));
                                    theFeed.removeSubjectIdentifier(theFeedSI);
                                    theFeed = tm.getTopic(data_feed_id);
                                }
                                else {
                                    Topic idType = getOrCreateTopic(FEED_ID_SI,"Atom Feed Id");
                                    parent.setData(theFeed, idType, DEFAULT_LANG, data_feed_id);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_CATEGORY: {
                    if(qName.equals(TAG_CATEGORY)) {
                        if(theFeed != null && data_feed_category != null) {
                            try {
                                Topic feedType = getFeedType();
                                Topic categoryType = getOrCreateTopic(FEED_CATEGORY_SI,"Atom Feed Category");
                                String term = data_feed_category.getTerm();
                                String label = data_feed_category.getLabel();
                                String scheme = data_feed_category.getScheme();
                                String categorySI = FEED_CATEGORY_SI + "/" + term;
                                String categoryBasename = term;
                                
                                if(isValid(term) && isValid(scheme)) categorySI = scheme + term;
                                if(isValid(label)) categoryBasename = label;
                                
                                Topic theCategory = getOrCreateTopic(categorySI, categoryBasename);
                                theCategory.addType(categoryType);
                                Association feedCategory = tm.createAssociation(categoryType);
                                feedCategory.addPlayer(theFeed, feedType);
                                feedCategory.addPlayer(theCategory, categoryType);
                                
                                if(isValid(scheme)) {
                                    Topic schemeType = getOrCreateTopic(SCHEME_SI,"Scheme");
                                    parent.setData(theCategory, schemeType, DEFAULT_LANG, scheme);
                                }
                                if(isValid(label)) {
                                    theCategory.setDisplayName(DEFAULT_LANG, label);
                                }
                                
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_CONTRIBUTOR: {
                    if(qName.equals(TAG_CONTRIBUTOR)) {
                        if(theFeed != null && isValid(data_feed_contributor_name)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic contributorType = getOrCreateTopic(FEED_CONTRIBUTOR_SI,"Atom Feed Contributor");
                                String contributorSI = !isValid(data_feed_contributor_uri) ? FEED_CONTRIBUTOR_SI + "/" + data_feed_contributor_name : data_feed_contributor_uri;
                                Topic theContributor = getOrCreateTopic(contributorSI, data_feed_contributor_name);
                                theContributor.addType(contributorType);
                                Association feedContributor = tm.createAssociation(contributorType);
                                feedContributor.addPlayer(theFeed, feedType);
                                feedContributor.addPlayer(theContributor, contributorType);
                                
                                if(isValid(data_feed_contributor_email)) {
                                    Topic emailType = getOrCreateTopic(EMAIL_ADDRESS_SI,"Email address");
                                    parent.setData(theContributor, emailType, DEFAULT_LANG, data_feed_contributor_email);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_CONTRIBUTOR_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_CONTRIBUTOR_URI: {
                    if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_CONTRIBUTOR_EMAIL: {
                    if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_GENERATOR: {
                    if(qName.equals(TAG_GENERATOR)) {
                        if(theFeed != null && isValid(data_feed_generator)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic generatorType = getOrCreateTopic(FEED_GENERATOR_SI,"Atom Feed Generator");
                                String generatorSI = FEED_GENERATOR_SI + "/" + data_feed_generator;
                                Topic theGenerator = getOrCreateTopic(generatorSI, data_feed_generator);
                                theGenerator.addType(generatorType);
                                Association feedGenerator = tm.createAssociation(generatorType);
                                feedGenerator.addPlayer(theFeed, feedType);
                                feedGenerator.addPlayer(theGenerator, generatorType);
                                
                                if(isValid(data_feed_generator_uri)) {
                                    Topic uriType = getOrCreateTopic(FEED_GENERATOR_URI_SI,"Atom Feed Generator URI");
                                    parent.setData(theGenerator, uriType, DEFAULT_LANG, data_feed_generator_uri);
                                }
                                if(isValid(data_feed_generator_version)) {
                                    Topic versionType = getOrCreateTopic(FEED_GENERATOR_VERSION_SI,"Atom Feed Generator Version");
                                    parent.setData(theGenerator, versionType, DEFAULT_LANG, data_feed_generator_version);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_ICON: {
                    if(qName.equals(TAG_ICON)) {
                        if(theFeed != null && isValid(data_feed_icon)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic iconType = getOrCreateTopic(FEED_ICON_SI,"Atom Feed Icon");
                                String iconSI = FEED_ICON_SI + "/" + data_feed_icon;
                                if(data_feed_icon.startsWith("http://")) {
                                    iconSI = data_feed_icon;
                                }
                                else if(data_feed_icon.startsWith("/") && data_feed_id.startsWith("http://")) {
                                    String prefix = data_feed_id;
                                    if(prefix.endsWith("/")) prefix = prefix.substring(0, prefix.length()-1); 
                                    iconSI = prefix + data_feed_icon;
                                }
                                Topic theIcon = getOrCreateTopic(iconSI, null);
                                theIcon.setSubjectLocator(new org.wandora.topicmap.Locator(iconSI));
                                theIcon.addType(iconType);
                                Association feedIcon = tm.createAssociation(iconType);
                                feedIcon.addPlayer(theFeed, feedType);
                                feedIcon.addPlayer(theIcon, iconType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_LOGO: {
                    if(qName.equals(TAG_LOGO)) {
                        if(theFeed != null && isValid(data_feed_logo)) {
                            try {
                                Topic feedType = getFeedType();
                                Topic logoType = getOrCreateTopic(FEED_LOGO_SI,"Atom Feed Logo");
                                String logoSI = FEED_LOGO_SI + "/" + data_feed_logo;
                                
                                if(data_feed_icon.startsWith("http://")) {
                                    logoSI = data_feed_logo;
                                }
                                else if(data_feed_logo.startsWith("/") && data_feed_id.startsWith("http://")) {
                                    String prefix = data_feed_id;
                                    if(prefix.endsWith("/")) prefix = prefix.substring(0, prefix.length()-1); 
                                    logoSI = prefix + data_feed_logo;
                                }
                                
                                Topic theLogo = getOrCreateTopic(logoSI, null);
                                theLogo.setSubjectLocator(new org.wandora.topicmap.Locator(logoSI));
                                theLogo.addType(logoType);
                                Association feedLogo = tm.createAssociation(logoType);
                                feedLogo.addPlayer(theFeed, feedType);
                                feedLogo.addPlayer(theLogo, logoType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_RIGHTS: {
                    if(qName.equals(TAG_RIGHTS)) {
                        if(theFeed != null && isValid(data_feed_rights)) {
                            try {
                                data_feed_rights = postProcessFeedText(data_feed_rights, data_feed_rights_type);
                                Topic rightsType = getRightsType();
                                Topic rightsTopic = getRightsTopic(data_feed_rights);
                                
                                if(rightsType != null && rightsTopic != null) {
                                    Association a = tm.createAssociation(rightsType);
                                    a.addPlayer(theFeed, getFeedType());
                                    a.addPlayer(rightsTopic, rightsType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_SUBTITLE: {
                    if(qName.equals(TAG_SUBTITLE)) {
                        if(theFeed != null && isValid(data_feed_subtitle)) {
                            try {
                                data_feed_subtitle = this.postProcessFeedText(data_feed_subtitle, data_feed_subtitle_type);
                                Topic subtitleType = getOrCreateTopic(FEED_SUBTITLE_SI,"Atom Feed Subtitle");
                                parent.setData(theFeed, subtitleType, DEFAULT_LANG, data_feed_subtitle);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                
                
                // **************************************************
                // ********************* ENTRY **********************
                // **************************************************
                
                
                
                
                case STATE_FEED_ENTRY: {
                    if(qName.equals(TAG_ENTRY)) {
                        if(theEntry != null && theFeed != null) {
                            try {
                                Topic entryType = getEntryType();
                                Topic feedType = getFeedType();
                                Association a = tm.createAssociation(entryType);
                                a.addPlayer(theEntry, entryType);
                                a.addPlayer(theFeed, feedType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_ID: {
                    if(qName.equals(TAG_ID)) {
                        if(theEntry != null && isValid(data_entry_id)) {
                            try {                                
                                if(data_entry_id.startsWith("http://")) {
                                    theEntry.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_entry_id));
                                    theEntry.removeSubjectIdentifier(theEntrySI);
                                    theEntry = tm.getTopic(data_entry_id);
                                }
                                else {
                                    Topic idType = getOrCreateTopic(FEED_ENTRY_ID_SI,"Atom Entry Id");
                                    parent.setData(theEntry, idType, DEFAULT_LANG, data_entry_id);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_TITLE: {
                    if(qName.equals(TAG_TITLE)) {
                        if(theEntry != null && isValid(data_entry_title)) {
                            try {
                                data_entry_title = postProcessFeedText(data_entry_title, data_entry_title_type);
                                theEntry.setBaseName(data_entry_title + " (Atom entry)");
                                theEntry.setDisplayName(DEFAULT_LANG, data_entry_title);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_UPDATED: {
                    if(qName.equals(TAG_UPDATED)) {
                        if(theEntry != null && isValid(data_entry_updated)) {
                            try {
                                Topic updatedType = getOrCreateTopic(FEED_ENTRY_UPDATED_SI,"Atom Entry Updated");
                                Topic dateTopic = getDateTopic(data_entry_updated);
                                if(dateTopic != null && updatedType != null) {
                                    Association a = tm.createAssociation(updatedType);
                                    a.addPlayer(theEntry, getEntryType());
                                    a.addPlayer(dateTopic, updatedType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_AUTHOR: {
                    if(qName.equals(TAG_AUTHOR)) {
                        if(theEntry != null && isValid(data_entry_author_name)) {
                            try {
                                Topic entryType = getEntryType();
                                Topic authorType = getOrCreateTopic(FEED_ENTRY_AUTHOR_SI,"Atom Entry Author");
                                String authorSI = data_entry_author_uri == null || data_entry_author_uri.length() == 0 ? FEED_ENTRY_AUTHOR_SI + "/" + data_entry_author_name : data_entry_author_uri;
                                Topic theAuthor = getOrCreateTopic(authorSI, data_entry_author_name);
                                theAuthor.addType(authorType);
                                Association entryAuthor = tm.createAssociation(authorType);
                                entryAuthor.addPlayer(theEntry, entryType);
                                entryAuthor.addPlayer(theAuthor, authorType);
                                
                                if(data_entry_author_email != null && data_entry_author_email.length() > 0) {
                                    Topic emailType = getOrCreateTopic(EMAIL_ADDRESS_SI,"Email address");
                                    parent.setData(theAuthor, emailType, DEFAULT_LANG, data_entry_author_email);
                                }
                                
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_AUTHOR_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_ENTRY_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_AUTHOR_URI: {
                    if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_ENTRY_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_AUTHOR_EMAIL: {
                    if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_ENTRY_AUTHOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTENT: {
                    if(qName.equals(TAG_CONTENT)) {
                        if(theEntry != null && (isValid(data_entry_content) || isValid(data_entry_content_src ))) {
                            try {
                                if(!isValid(data_entry_content)) {
                                    data_entry_content = IObox.doUrl(new URL(data_entry_content_src));
                                }
                                data_entry_content = this.postProcessFeedText(data_entry_content, data_entry_content_type);
                                Topic contentType = getOrCreateTopic(FEED_ENTRY_CONTENT_SI,"Atom Entry Content");
                                parent.setData(theEntry, contentType, DEFAULT_LANG, data_entry_content);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_LINK: {
                    if(qName.equals(TAG_LINK)) {
                        if(theEntry != null && data_entry_link != null) {
                            try {
                                createLinkStruct(data_entry_link, theEntry, getEntryType());
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SUMMARY: {
                    if(qName.equals(TAG_SUMMARY)) {
                        if(theEntry != null && (isValid(data_entry_summary) || isValid(data_entry_summary_src))) {
                            try {
                                if(!isValid(data_entry_summary)) {
                                    data_entry_summary = IObox.doUrl(new URL(data_entry_summary_src));
                                }
                                data_entry_summary = this.postProcessFeedText(data_entry_summary, data_entry_summary_type);
                                Topic summaryType = getOrCreateTopic(FEED_ENTRY_SUMMARY_SI,"Atom Entry Summary");
                                parent.setData(theEntry, summaryType, DEFAULT_LANG, data_entry_summary);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CATEGORY: {
                    if(qName.equals(TAG_CATEGORY)) {
                        if(theFeed != null && data_entry_category != null) {
                            try {
                                Topic entryType = getEntryType();
                                Topic categoryType = getOrCreateTopic(FEED_CATEGORY_SI,"Atom Entry Category");
                                String term = data_entry_category.getTerm();
                                String label = data_entry_category.getLabel();
                                String scheme = data_entry_category.getScheme();
                                String categorySI = FEED_ENTRY_CATEGORY_SI + "/" + term;
                                String categoryBasename = term;
                                if(isValid(scheme) && isValid(term)) categorySI = scheme+term;
                                if(isValid(label)) categoryBasename = label;
                                
                                Topic theCategory = getOrCreateTopic(categorySI, categoryBasename);
                                theCategory.addType(categoryType);
                                Association entryCategory = tm.createAssociation(categoryType);
                                entryCategory.addPlayer(theEntry, entryType);
                                entryCategory.addPlayer(theCategory, categoryType);
                                
                                if(scheme != null && scheme.length() > 0) {
                                    Topic schemeType = getOrCreateTopic(SCHEME_SI,"Scheme");
                                    parent.setData(theCategory, schemeType, DEFAULT_LANG, scheme);
                                }
                                if(label != null && label.length() > 0) {
                                    theCategory.setDisplayName(DEFAULT_LANG, label);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTRIBUTOR: {
                    if(qName.equals(TAG_CONTRIBUTOR)) {
                        if(theEntry != null && isValid(data_entry_contributor_name)) {
                            try {
                                Topic entryType = getEntryType();
                                Topic contributorType = getOrCreateTopic(FEED_ENTRY_CONTRIBUTOR_SI,"Atom Entry Contributor");
                                String contributorSI = data_entry_contributor_uri == null || data_entry_contributor_uri.length() == 0 ? FEED_CONTRIBUTOR_SI + "/" + data_entry_contributor_name : data_entry_contributor_uri;
                                Topic theContributor = getOrCreateTopic(contributorSI, data_entry_contributor_name);
                                theContributor.addType(contributorType);
                                Association feedContributor = tm.createAssociation(contributorType);
                                feedContributor.addPlayer(theEntry, entryType);
                                feedContributor.addPlayer(theContributor, contributorType);
                                
                                if(data_entry_contributor_email != null && data_entry_contributor_email.length() > 0) {
                                    Topic emailType = getOrCreateTopic(EMAIL_ADDRESS_SI,"Email address");
                                    parent.setData(theContributor, emailType, DEFAULT_LANG, data_entry_contributor_email);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTRIBUTOR_NAME: {
                    if(qName.equals(TAG_NAME)) {
                        state = STATE_FEED_ENTRY_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTRIBUTOR_URI: {
                    if(qName.equals(TAG_URI)) {
                        state = STATE_FEED_ENTRY_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_CONTRIBUTOR_EMAIL: {
                    if(qName.equals(TAG_EMAIL)) {
                        state = STATE_FEED_ENTRY_CONTRIBUTOR;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_PUBLISHED: {
                    if(qName.equals(TAG_PUBLISHED)) {
                        if(theEntry != null && isValid(data_entry_published)) {
                            try {
                                Topic publishedType = getOrCreateTopic(FEED_ENTRY_PUBLISHED_SI,"Atom Entry Published");
                                Topic dateTopic = getDateTopic(data_entry_published);
                                if(dateTopic != null && publishedType != null) {
                                    Association a = tm.createAssociation(publishedType);
                                    a.addPlayer(theEntry, getEntryType());
                                    a.addPlayer(dateTopic, publishedType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE: {
                    if(qName.equals(TAG_SOURCE)) {
                        if(theEntry != null && isValid(data_entry_source_id)) {
                            try {
                                Topic sourceType = getOrCreateTopic(FEED_ENTRY_SOURCE_SI,"Atom Entry Source");
                                Topic theSource = tm.createTopic();
                                Topic theSourceRights = null;
                                Topic theSourceUpdated = null;
                                if(data_entry_source_id.startsWith("http://")) {
                                    theSource.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_entry_source_id));
                                    theSource.setSubjectLocator(new org.wandora.topicmap.Locator(data_entry_source_id));
                                }
                                else {
                                    theSource.addSubjectIdentifier(new org.wandora.topicmap.Locator(FEED_ENTRY_SOURCE_SI+"/"+data_entry_source_id));
                                }
                                if(isValid(data_entry_source_title)) {
                                    theSource.setBaseName(data_entry_source_title);
                                }
                                theSource.addType(sourceType);
                                
                                Association a = tm.createAssociation(sourceType);
                                a.addPlayer(theEntry, getEntryType());
                                a.addPlayer(theSource, sourceType);
                                
                                if(isValid(data_entry_source_rights)) {
                                    Topic rightsType = getRightsType();
                                    theSourceRights = getRightsTopic(postProcessFeedText(data_entry_source_rights, data_entry_source_rights_type));
                                    a.addPlayer(theSourceRights, rightsType);
                                }
                                if(isValid(data_entry_source_updated)) {
                                    Topic dateType = getDateType();
                                    theSourceUpdated = getDateTopic(data_entry_source_updated);
                                    a.addPlayer(theSourceUpdated, dateType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE_ID: {
                    if(qName.equals(TAG_ID)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE_TITLE: {
                    if(qName.equals(TAG_TITLE)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE_UPDATED: {
                    if(qName.equals(TAG_UPDATED)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_SOURCE_RIGHTS: {
                    if(qName.equals(TAG_RIGHTS)) {
                        state = STATE_FEED_ENTRY_SOURCE;
                    }
                    break;
                }
                case STATE_FEED_ENTRY_RIGHTS: {
                    if(qName.equals(TAG_RIGHTS)) {
                        if(theEntry != null && isValid(data_entry_rights)) {
                            try {
                                data_entry_rights = postProcessFeedText(data_entry_rights, data_entry_rights_type);
                                Topic rightsType = getRightsType();
                                Topic rightsTopic = getRightsTopic(data_entry_rights);
                                if(rightsType != null && rightsTopic != null) {
                                    Association a = tm.createAssociation(rightsType);
                                    a.addPlayer(theEntry, getEntryType());
                                    a.addPlayer(rightsTopic, rightsType);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        state = STATE_FEED_ENTRY;
                    }
                    break;
                }
            }
        }
        
        
        
        
        
        
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_FEED_ID:
                    data_feed_id+=new String(ch,start,length);
                    break;
                case STATE_FEED_TITLE:
                    data_feed_title+=new String(ch,start,length);
                    break;
                case STATE_FEED_UPDATED:
                    data_feed_updated+=new String(ch,start,length);
                    break;
                case STATE_FEED_AUTHOR_NAME:
                    data_feed_author_name+=new String(ch,start,length);
                    break;
                case STATE_FEED_AUTHOR_URI:
                    data_feed_author_uri+=new String(ch,start,length);
                    break;
                case STATE_FEED_AUTHOR_EMAIL:
                    data_feed_author_email+=new String(ch,start,length);
                    break;
                case STATE_FEED_CONTRIBUTOR_NAME:
                    data_feed_contributor_name+=new String(ch,start,length);
                    break;
                case STATE_FEED_CONTRIBUTOR_URI:
                    data_feed_contributor_uri+=new String(ch,start,length);
                    break;
                case STATE_FEED_CONTRIBUTOR_EMAIL:
                    data_feed_contributor_email+=new String(ch,start,length);
                    break;
                case STATE_FEED_GENERATOR:
                    data_feed_generator+=new String(ch,start,length);
                    break;
                case STATE_FEED_ICON:
                    data_feed_icon+=new String(ch,start,length);
                    break; 
                case STATE_FEED_LOGO:
                    data_feed_logo+=new String(ch,start,length);
                    break; 
                case STATE_FEED_RIGHTS:
                    data_feed_rights+=new String(ch,start,length);
                    break;
                case STATE_FEED_SUBTITLE:
                    data_feed_subtitle+=new String(ch,start,length);
                    break;
                    
                // *********************** ENTRY *************************
                
                case STATE_FEED_ENTRY_ID:
                    data_entry_id+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_TITLE:
                    data_entry_title+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_UPDATED:
                    data_entry_updated+=new String(ch,start,length);
                    break; 
                case STATE_FEED_ENTRY_AUTHOR_NAME:
                    data_entry_author_name+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_AUTHOR_URI:
                    data_entry_author_uri+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_AUTHOR_EMAIL:
                    data_entry_author_email+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_CONTENT:
                    data_entry_content+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SUMMARY:
                    data_entry_summary+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_CONTRIBUTOR_NAME:
                    data_entry_contributor_name+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_CONTRIBUTOR_URI:
                    data_entry_contributor_uri+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_CONTRIBUTOR_EMAIL:
                    data_entry_contributor_email+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_PUBLISHED:
                    data_entry_published+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SOURCE_ID:
                    data_entry_source_id+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SOURCE_TITLE:
                    data_entry_source_title+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SOURCE_UPDATED:
                    data_entry_source_updated+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_SOURCE_RIGHTS:
                    data_entry_source_rights+=new String(ch,start,length);
                    break;
                case STATE_FEED_ENTRY_RIGHTS:
                    data_entry_rights+=new String(ch,start,length);
                    break;
            }
        }
        
        public void warning(SAXParseException exception) throws SAXException {
            parent.log("Warning while parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
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
    
    
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------- HELPER CLASSES ----
    // -------------------------------------------------------------------------
    
    
    private class Content {
        private String text;
        private String src;
        private String type;
        
        public Content(String t, String s, String type) {
            this.text = t;
            this.src = s;
            this.type = type;
        }
        
        public String getContent() {
            return text;
        }
        public String getType() {
            return type;
        }
        public String getSrc() {
            return src;
        }
    }
    
    
    private class Category {
        private String term;
        private String scheme;
        private String label;
        
        public Category(String t, String s, String l) {
            this.term = t;
            this.scheme = s;
            this.label = l;
        }
        
        public String getTerm() {
            return term;
        }
        public String getScheme() {
            return scheme;
        }
        public String getLabel() {
            return label;
        }
    }
    
    
    private class Text {
        private String text;
        private String type;
        
        public Text(String t, String type) {
            this.text = t;
            this.type = type;
        }
        
        public String getText() {
            return text;
        }
        public String getType() {
            return type;
        }
    }
    
    
    private class Person {
        private String name;
        private String uri;
        private String email;
        
        public Person(String n, String u, String e) {
            this.name = n;
            this.uri = u;
            this.email = e;
        }
        
        public String getName() {
            return name;
        }
        public String getUri() {
            return uri;
        }
        public String getEmail() {
            return email;
        }
    }
    
    
    private class Link {
        private String href;
        private String rel;
        private String type;
        private String hreflang;
        private String title;
        private String length;
        
        public Link(String h, String r, String ty, String hl, String ti, String len) {
            this.href = h;
            this.rel = r;
            this.type = ty;
            this.hreflang = hl;
            this.title = ti;
            this.length = len;
        }
        
        public String getHref() {
            return href;
        }
        public String getRel() {
            return rel;
        }
        public String getType() {
            return type;
        }
        public String getHrefLang() {
            return hreflang;
        }
        public String getTitle() {
            return title;
        }
        public String getLength() {
            return length;
        }
    }
    
}
