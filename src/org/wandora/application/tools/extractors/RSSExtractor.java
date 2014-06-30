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
 * RSSExtractor.java
 *
 * Created on 3. marraskuuta 2007, 13:18
 *
 */

package org.wandora.application.tools.extractors;



import java.net.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.utils.IObox;


/**
 *
 * @author akivela
 */
public class RSSExtractor extends AbstractExtractor {
    
    /** Creates a new instance of RSSExtractor */
    public RSSExtractor() {
    }

    @Override
    public boolean useURLCrawler() {
        return false;
    }

     
    @Override
    public String getName() {
        return "RSS 2.0 Extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads RSS 2.0 feed and converts the feed to a topic map.";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_rss.png");
    }
    
    private final String[] contentTypes=new String[] { "text/xml", "application/xml", "application/rss+xml", "application/xhtml+xml" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }


    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        try {
            setWandora(wandora);
            String urlstr = request.getSource();

            //System.out.println("FOUND URL: "+urlstr);

            URL url = new URL(urlstr);
            URLConnection uc = url.openConnection();
            String type = uc.getContentType();
            
            //System.out.println("FOUND TYPE: "+type);

            if(type != null && type.indexOf("text/html") > -1) {
                String htmlContent = IObox.doUrl(url);
                Pattern p1 = Pattern.compile("\\<link [^\\>]+\\>");
                Pattern p2 = Pattern.compile("href\\=\"([^\"]+)\"");
                
                Matcher m1 = p1.matcher(htmlContent);
                int sp = 0;
                while(m1.find(sp)) {
                    String linktag = m1.group();
                    if(linktag != null && linktag.length() > 0) {
                        if(linktag.indexOf("application/rss+xml") > -1 || linktag.indexOf("application/xml") > -1) {
                            Matcher m2 = p2.matcher(linktag);
                            if(m2.find()) {
                                try {
                                    String rssfeed = m2.group(1);
                                    _extractTopicsFrom(new URL(rssfeed), wandora.getTopicMap());
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
            else if(type != null && type.indexOf("application/rss+xml") > -1) {
                _extractTopicsFrom(url, wandora.getTopicMap());
                wandora.doRefresh();
                return null;
            }
            else if(type != null && type.indexOf("text/xml") > -1) {
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


    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        URLConnection uc=url.openConnection();
        Wandora.initUrlConnection(uc);
        return _extractTopicsFrom(uc.getInputStream(),topicMap);
    }
    
    
    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
        return answer;
    }


    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        RSSParser parserHandler = new RSSParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.channelCount + " RSS channels processed!");
        log("Total " + parserHandler.itemCount + " RSS channel items processed!");
        return true;
    }
    
    
    
    
    
    

    private static class RSSParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public static boolean MAKE_LINK_OCCURRENCE = true;
        public static boolean MAKE_LINK_SUBJECT_IDENTIFIER = true;
        public static boolean MAKE_LINK_SUBJECT_LOCATOR = false;
        public static boolean MAKE_SUBCLASS_OF_WANDORA_CLASS = true;
        
        public RSSParser(TopicMap tm, RSSExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        public int itemCount = 0;
        public int channelCount = 0;
        
        private TopicMap tm;
        private RSSExtractor parent;
        
        public static final String TAG_RSS="rss";
        public static final String TAG_CHANNEL="channel";
        public static final String TAG_TITLE="title";
        public static final String TAG_LINK="link";
        public static final String TAG_DESCRIPTION="description";
        public static final String TAG_LANGUAGE="language";
        public static final String TAG_PUBDATE="pubDate";
        public static final String TAG_LASTBUILDDATE="lastBuildDate";
        public static final String TAG_DOCS="docs";
        public static final String TAG_GENERATOR="generator";
        public static final String TAG_MANAGINGEDITOR="managingEditor";
        public static final String TAG_WEBMASTER="webMaster";
        public static final String TAG_TTL="ttl";
        public static final String TAG_TEXTINPUT="textInput";
        public static final String TAG_COPYRIGHT="copyright";
        public static final String TAG_RATING="rating";
               
        public static final String TAG_ITEM="item";
        public static final String TAG_GUID="guid";
        public static final String TAG_CATEGORY="category";
        public static final String TAG_AUTHOR="author";
        
        public static final String TAG_IMAGE="image";
        public static final String TAG_URL="url";
        
        
        
        private static final int STATE_START=0;
        private static final int STATE_RSS=2;
        private static final int STATE_CHANNEL=4;
        private static final int STATE_CHANNEL_TITLE=5;
        private static final int STATE_CHANNEL_LINK=6;
        private static final int STATE_CHANNEL_DESCRIPTION=7;
        private static final int STATE_CHANNEL_LANGUAGE=8;
        private static final int STATE_CHANNEL_PUBDATE=9;
        private static final int STATE_CHANNEL_LASTBUILDDATE=10;
        private static final int STATE_CHANNEL_DOCS=11;
        private static final int STATE_CHANNEL_GENERATOR=12;
        private static final int STATE_CHANNEL_MANAGINGEDITOR=13;
        private static final int STATE_CHANNEL_WEBMASTER=14;
        private static final int STATE_CHANNEL_TTL=15;
        private static final int STATE_CHANNEL_TEXTINPUT=16;
        private static final int STATE_CHANNEL_COPYRIGHT=17;
        private static final int STATE_CHANNEL_CATEGORY=18;
        private static final int STATE_CHANNEL_RATING=19;
        
        private static final int STATE_CHANNEL_IMAGE=31;
        private static final int STATE_CHANNEL_IMAGE_URL=32;
        private static final int STATE_CHANNEL_IMAGE_TITLE=33;
        private static final int STATE_CHANNEL_IMAGE_LINK=34;
        private static final int STATE_CHANNEL_IMAGE_DESCRIPTION=35;
        
        private static final int STATE_CHANNEL_ITEM=120;
        private static final int STATE_CHANNEL_ITEM_TITLE=121;
        private static final int STATE_CHANNEL_ITEM_LINK=122;
        private static final int STATE_CHANNEL_ITEM_DESCRIPTION=123;
        private static final int STATE_CHANNEL_ITEM_PUBDATE=124;
        private static final int STATE_CHANNEL_ITEM_GUID=125;
        private static final int STATE_CHANNEL_ITEM_CATEGORY=126;
        private static final int STATE_CHANNEL_ITEM_AUTHOR=127;
               
        private int state=STATE_START;
        
        public static String RSS_SI = "http://wandora.org/si/rss/2.0/";

        public static String SIPREFIX="http://wandora.org/si/rss/2.0/";
        public static String CHANNEL_SI=SIPREFIX+"channel";
        public static String CHANNEL_LINK_SI=CHANNEL_SI+"/link";
        public static String CHANNEL_DESCRIPTION_SI=CHANNEL_SI+"/description";
        public static String CHANNEL_LANGUAGE_SI=CHANNEL_SI+"/language";
        public static String CHANNEL_PUBDATE_SI=CHANNEL_SI+"/pubdate";
        public static String CHANNEL_LASTBUILDDATE_SI=CHANNEL_SI+"/lastbuilddate";
        public static String CHANNEL_DOCS_SI=CHANNEL_SI+"/docs";
        public static String CHANNEL_GENERATOR_SI=CHANNEL_SI+"/generator";
        public static String CHANNEL_MANAGINGEDITOR_SI=CHANNEL_SI+"/managingeditor";
        public static String CHANNEL_WEBMASTER_SI=CHANNEL_SI+"/webmaster";
        public static String CHANNEL_TTL_SI=CHANNEL_SI+"/ttl";
        public static String CHANNEL_TEXTINPUT_SI=CHANNEL_SI+"/textinput";
        public static String CHANNEL_COPYRIGHT_SI=CHANNEL_SI+"/copyright";
        public static String CHANNEL_CATEGORY_SI=CHANNEL_SI+"/category";
        public static String CHANNEL_RATING_SI=CHANNEL_SI+"/rating";
        
        public static String CHANNEL_ITEM_SI=CHANNEL_SI+"/item";
        public static String CHANNEL_ITEM_TITLE_SI=CHANNEL_ITEM_SI+"/title";
        public static String CHANNEL_ITEM_LINK_SI=CHANNEL_ITEM_SI+"/link";
        public static String CHANNEL_ITEM_DESCRIPTION_SI=CHANNEL_ITEM_SI+"/description";
        public static String CHANNEL_ITEM_PUBDATE_SI=CHANNEL_ITEM_SI+"/pubdate";
        public static String CHANNEL_ITEM_GUID_SI=CHANNEL_ITEM_SI+"/guid";
        public static String CHANNEL_ITEM_CATEGORY_SI=CHANNEL_ITEM_SI+"/category";
        public static String CHANNEL_ITEM_AUTHOR_SI=CHANNEL_ITEM_SI+"/author";
        
        public static String CHANNEL_IMAGE_SI=CHANNEL_SI+"/image";
        public static String CHANNEL_IMAGE_LINK_SI=CHANNEL_IMAGE_SI+"/link";
        public static String CHANNEL_IMAGE_DESCRIPTION_SI=CHANNEL_IMAGE_SI+"/description";
        
        public static String DATE_SI="http://wandora.org/si/date"; 
        
        private String data_channel_title;
        private String data_channel_link;
        private String data_channel_description;
        private String data_channel_language;
        private String data_channel_pubdate;
        private String data_channel_lastbuilddate;
        private String data_channel_docs;
        private String data_channel_generator;
        private String data_channel_managingeditor;
        private String data_channel_webmaster;
        private String data_channel_ttl;
        private String data_channel_copyright;
        private String data_channel_category;
        private String data_channel_rating;
        
        private String data_channel_item_title;
        private String data_channel_item_link;
        private String data_channel_item_description;
        private String data_channel_item_pubdate;
        private String data_channel_item_guid;
        private String data_channel_item_category;
        private String data_channel_item_author;
        
        private String data_channel_image_url;
        private String data_channel_image_title;
        private String data_channel_image_link;
        private String data_channel_image_description;
        
        private Topic theChannel;
        private Topic theItem;

        
        
        private Topic getOrCreateTopic(String si) throws TopicMapException {
            return getOrCreateTopic(si, null);
        }
        private Topic getOrCreateTopic(String si,String bn) throws TopicMapException {
            return ExtractHelper.getOrCreateTopic(si, bn, tm);
        }
        
        @Override
        public void startDocument() throws SAXException {
            channelCount = 0;
            itemCount = 0;
        }
        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            //parent.log("START" + state +" --- " + qName);
            
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START:
                    if(qName.equals(TAG_RSS)) {
                        state = STATE_RSS;
                    }
                    break;
                case STATE_RSS:
                    if(qName.equals(TAG_CHANNEL)) {
                        state = STATE_CHANNEL;
                        try {
                            Topic channelType=getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                            theChannel = tm.createTopic();
                            theChannel.addSubjectIdentifier(TopicTools.createDefaultLocator());
                            theChannel.addType(channelType);
                            channelCount++;
                            
                            Topic rssClass = getOrCreateTopic(RSS_SI, "RSS");
                            Topic superClass = getOrCreateTopic(XTMPSI.SUPERCLASS, null);
                            Topic subClass = getOrCreateTopic(XTMPSI.SUBCLASS, null);
                            Topic supersubClass = getOrCreateTopic(XTMPSI.SUPERCLASS_SUBCLASS, null);

                            Association supersubClassAssociation = tm.createAssociation(supersubClass);
                            supersubClassAssociation.addPlayer(rssClass, superClass);
                            supersubClassAssociation.addPlayer(channelType, subClass);

                            if(MAKE_SUBCLASS_OF_WANDORA_CLASS) {
                                Topic wandoraClass = getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class");
                                supersubClassAssociation = tm.createAssociation(supersubClass);
                                supersubClassAssociation.addPlayer(wandoraClass, superClass);
                                supersubClassAssociation.addPlayer(rssClass, subClass);
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                    }
                    break;
                case STATE_CHANNEL:
                    if(qName.equals(TAG_TITLE)) {
                        state = STATE_CHANNEL_TITLE;
                        data_channel_title = "";
                    }
                    else if(qName.equals(TAG_LINK)) {
                        state = STATE_CHANNEL_LINK;
                        data_channel_link = "";
                    }
                    else if(qName.equals(TAG_DESCRIPTION)) {
                        state = STATE_CHANNEL_DESCRIPTION;
                        data_channel_description = "";
                    }
                    else if(qName.equals(TAG_LANGUAGE)) {
                        state = STATE_CHANNEL_LANGUAGE;
                        data_channel_language = "";
                    }
                    else if(qName.equals(TAG_PUBDATE)) {
                        state = STATE_CHANNEL_PUBDATE;
                        data_channel_pubdate = "";
                    }
                    else if(qName.equals(TAG_LASTBUILDDATE)) {
                        state = STATE_CHANNEL_LASTBUILDDATE;
                        data_channel_lastbuilddate = "";
                    }
                    else if(qName.equals(TAG_DOCS)) {
                        state = STATE_CHANNEL_DOCS;
                        data_channel_docs = "";
                    }
                    else if(qName.equals(TAG_GENERATOR)) {
                        state = STATE_CHANNEL_GENERATOR;
                        data_channel_generator = "";
                    }
                    else if(qName.equals(TAG_MANAGINGEDITOR)) {
                        state = STATE_CHANNEL_MANAGINGEDITOR;
                        data_channel_managingeditor = "";
                    }
                    else if(qName.equals(TAG_WEBMASTER)) {
                        state = STATE_CHANNEL_WEBMASTER;
                        data_channel_webmaster = "";
                    }
                    else if(qName.equals(TAG_TTL)) {
                        state = STATE_CHANNEL_TTL;
                        data_channel_ttl = "";
                    }
                    else if(qName.equals(TAG_TEXTINPUT)) {
                        state = STATE_CHANNEL_TEXTINPUT;
                    }
                    else if(qName.equals(TAG_COPYRIGHT)) {
                        state = STATE_CHANNEL_COPYRIGHT;
                        data_channel_copyright = "";
                    }
                    else if(qName.equals(TAG_CATEGORY)) {
                        state = STATE_CHANNEL_CATEGORY;
                        data_channel_category = "";
                    }
                    else if(qName.equals(TAG_RATING)) {
                        state = STATE_CHANNEL_RATING;
                        data_channel_rating = "";
                    }
                    else if(qName.equals(TAG_IMAGE)) {
                        state = STATE_CHANNEL_IMAGE;
                        data_channel_image_title = "";
                        data_channel_image_url = "";
                        data_channel_image_link = "";
                    }
                    else if(qName.equals(TAG_ITEM)) {
                        state = STATE_CHANNEL_ITEM;
                        try {
                            Topic itemType=getOrCreateTopic(CHANNEL_ITEM_SI,"RSS Channel Item");
                            theItem = tm.createTopic();
                            theItem.addSubjectIdentifier(TopicTools.createDefaultLocator());
                            theItem.addType(itemType);
                            itemCount++;
                            
                            Topic rssClass = getOrCreateTopic(RSS_SI, "RSS");
                            Topic superClass = getOrCreateTopic(XTMPSI.SUPERCLASS, null);
                            Topic subClass = getOrCreateTopic(XTMPSI.SUBCLASS, null);
                            Topic supersubClass = getOrCreateTopic(XTMPSI.SUPERCLASS_SUBCLASS, null);

                            Association supersubClassAssociation = tm.createAssociation(supersubClass);
                            supersubClassAssociation.addPlayer(rssClass, superClass);
                            supersubClassAssociation.addPlayer(itemType, subClass);

                            if(MAKE_SUBCLASS_OF_WANDORA_CLASS) {
                                Topic wandoraClass = getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class");
                                supersubClassAssociation = tm.createAssociation(supersubClass);
                                supersubClassAssociation.addPlayer(wandoraClass, superClass);
                                supersubClassAssociation.addPlayer(rssClass, subClass);
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                        data_channel_item_title = "";
                        data_channel_item_link = "";
                        data_channel_item_description = "";
                        data_channel_item_pubdate = "";
                        data_channel_item_guid = "";
                        data_channel_item_category = "";
                        data_channel_item_author = "";
                    }
                    break;
                case STATE_CHANNEL_ITEM:
                    if(qName.equals(TAG_TITLE)) {
                        data_channel_item_title = "";
                        state = STATE_CHANNEL_ITEM_TITLE;
                    }
                    else if(qName.equals(TAG_LINK)) {
                        data_channel_item_link = "";
                        state = STATE_CHANNEL_ITEM_LINK;
                    }
                    else if(qName.equals(TAG_DESCRIPTION)) {
                        data_channel_item_description = "";
                        state = STATE_CHANNEL_ITEM_DESCRIPTION;
                    }
                    else if(qName.equals(TAG_PUBDATE)) {
                        data_channel_item_pubdate = "";
                        state = STATE_CHANNEL_ITEM_PUBDATE;
                    }
                    else if(qName.equals(TAG_GUID)) {
                        data_channel_item_guid = "";
                        state = STATE_CHANNEL_ITEM_GUID;
                    }
                    else if(qName.equals(TAG_CATEGORY)) {
                        data_channel_item_category = "";
                        state = STATE_CHANNEL_ITEM_CATEGORY;
                    }
                    else if(qName.equals(TAG_AUTHOR)) {
                        data_channel_item_author = "";
                        state = STATE_CHANNEL_ITEM_AUTHOR;
                    }
                    break;
                case STATE_CHANNEL_IMAGE:
                    if(qName.equals(TAG_TITLE)) {
                        data_channel_image_title = "";
                        state = STATE_CHANNEL_IMAGE_TITLE;
                    }
                    else if(qName.equals(TAG_URL)) {
                        data_channel_image_url = "";
                        state = STATE_CHANNEL_IMAGE_URL;
                    }
                    else if(qName.equals(TAG_LINK)) {
                        data_channel_image_link = "";
                        state = STATE_CHANNEL_IMAGE_LINK;
                    }
                    else if(qName.equals(TAG_DESCRIPTION)) {
                        data_channel_image_description = "";
                        state = STATE_CHANNEL_IMAGE_DESCRIPTION;
                    }
                    break;
            }
        }
        
        
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            //parent.log("  END" + state +" --- " + qName);
            
            switch(state) {
                case STATE_RSS: {
                    if(qName.equals(TAG_RSS)) {
                        state = STATE_START;
                    }
                    break;
                }
                case STATE_CHANNEL: {
                    if(qName.equals(TAG_CHANNEL)) {
                        state = STATE_RSS;
                        if(theChannel != null) {
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_TITLE: {
                    if(qName.equals(TAG_TITLE)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_title.length() > 0) {
                            try {
                                theChannel.setBaseName(data_channel_title + " (RSS channel)");
                                theChannel.setDisplayName("en", data_channel_title);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_LINK: {
                    if(qName.equals(TAG_LINK)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_link.length() > 0) {
                            try {
                                if(MAKE_LINK_SUBJECT_IDENTIFIER) {
                                    org.wandora.topicmap.Locator tempSI = theChannel.getOneSubjectIdentifier();
                                    theChannel.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_channel_link));
                                    theChannel.removeSubjectIdentifier(tempSI);
                                }
                                if(MAKE_LINK_SUBJECT_LOCATOR) {
                                    theChannel.setSubjectLocator(new org.wandora.topicmap.Locator(data_channel_link));
                                }
                                if(MAKE_LINK_OCCURRENCE) {
                                    Topic linkType = getOrCreateTopic(CHANNEL_LINK_SI,"RSS Channel Link");
                                    parent.setData(theChannel, linkType, "en", data_channel_link);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_DESCRIPTION: {
                    if(qName.equals(TAG_DESCRIPTION)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_description.length() > 0) {
                            try {
                                Topic descriptionType = getOrCreateTopic(CHANNEL_DESCRIPTION_SI,"RSS Channel Description");
                                parent.setData(theChannel, descriptionType, "en", data_channel_description);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_LANGUAGE: {
                    if(qName.equals(TAG_LANGUAGE)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_language.length() > 0) {
                            try {
                                Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                Topic languageType = getOrCreateTopic(CHANNEL_LANGUAGE_SI,"RSS Channel Language");
                                Topic theLanguage = getOrCreateTopic(CHANNEL_LANGUAGE_SI + "/" + data_channel_language, data_channel_language);
                                theLanguage.addType(languageType);
                                Association channelLanguage = tm.createAssociation(languageType);
                                channelLanguage.addPlayer(theChannel, channelType);
                                channelLanguage.addPlayer(theLanguage, languageType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_COPYRIGHT: {
                    if(qName.equals(TAG_COPYRIGHT)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_copyright.length() > 0) {
                            try {
                                if(true) {
                                    Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                    Topic copyrightType = getOrCreateTopic(CHANNEL_COPYRIGHT_SI,"RSS Channel Copyright");
                                    Topic theCopyright = getOrCreateTopic(CHANNEL_COPYRIGHT_SI + "/" + data_channel_copyright, data_channel_copyright);
                                    theCopyright.addType(copyrightType);
                                    Association channelCopyright = tm.createAssociation(copyrightType);
                                    channelCopyright.addPlayer(theChannel, channelType);
                                    channelCopyright.addPlayer(theCopyright, copyrightType);
                                }
                                else {
                                    Topic copyrightType = getOrCreateTopic(CHANNEL_COPYRIGHT_SI,"RSS Channel Copyright");
                                    parent.setData(theChannel, copyrightType, "en", data_channel_copyright);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_PUBDATE: {
                    if(qName.equals(TAG_PUBDATE)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_pubdate.length() > 0) {
                            try {
                                Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                Topic dateType = getOrCreateTopic(DATE_SI,"Date");
                                Topic pubdateType = getOrCreateTopic(CHANNEL_PUBDATE_SI,"RSS Channel Publish Date");
                                Topic theDate = getOrCreateTopic(DATE_SI + "/" + data_channel_pubdate, data_channel_pubdate);
                                theDate.addType(dateType);
                                Association channelPubDate = tm.createAssociation(pubdateType);
                                channelPubDate.addPlayer(theChannel, channelType);
                                channelPubDate.addPlayer(theDate, dateType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_LASTBUILDDATE: {
                    if(qName.equals(TAG_LASTBUILDDATE)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_lastbuilddate.length() > 0) {
                            try {
                                Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                Topic dateType = getOrCreateTopic(DATE_SI,"Date");
                                Topic lastbuilddateType = getOrCreateTopic(CHANNEL_LASTBUILDDATE_SI,"RSS Channel Last Build Date");
                                Topic theDate = getOrCreateTopic(DATE_SI + "/" + data_channel_lastbuilddate, data_channel_lastbuilddate);
                                theDate.addType(dateType);
                                Association channelPubDate = tm.createAssociation(lastbuilddateType);
                                channelPubDate.addPlayer(theChannel, channelType);
                                channelPubDate.addPlayer(theDate, dateType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_DOCS: {
                    if(qName.equals(TAG_DOCS)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_docs.length() > 0) {
                            try {
                                Topic docsType = getOrCreateTopic(CHANNEL_DOCS_SI,"Channel Docs");
                                parent.setData(theChannel, docsType, "en", data_channel_docs);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_GENERATOR: {
                    if(qName.equals(TAG_GENERATOR)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_generator.length() > 0) {
                            try {
                                Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                Topic generatorType = getOrCreateTopic(CHANNEL_GENERATOR_SI,"RSS Channel Generator");
                                Topic theGenerator = getOrCreateTopic(CHANNEL_GENERATOR_SI + "/" + data_channel_generator, data_channel_generator);
                                theGenerator.addType(generatorType);
                                Association channelGenerator = tm.createAssociation(generatorType);
                                channelGenerator.addPlayer(theChannel, channelType);
                                channelGenerator.addPlayer(theGenerator, generatorType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_MANAGINGEDITOR: {
                    if(qName.equals(TAG_MANAGINGEDITOR)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_managingeditor.length() > 0) {
                            try {
                                Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                Topic managerType = getOrCreateTopic(CHANNEL_MANAGINGEDITOR_SI,"RSS Channel Managing Editor");
                                Topic theManager = getOrCreateTopic(CHANNEL_MANAGINGEDITOR_SI + "/" + data_channel_managingeditor, data_channel_managingeditor);
                                theManager.addType(managerType);
                                Association channelManager = tm.createAssociation(managerType);
                                channelManager.addPlayer(theChannel, channelType);
                                channelManager.addPlayer(theManager, managerType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_WEBMASTER: {
                    if(qName.equals(TAG_WEBMASTER)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_webmaster.length() > 0) {
                            try {
                                Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                Topic webmasterType = getOrCreateTopic(CHANNEL_WEBMASTER_SI,"RSS Channel Web Master");
                                Topic theWebmaster = getOrCreateTopic(CHANNEL_WEBMASTER_SI + "/" + data_channel_webmaster, data_channel_webmaster);
                                theWebmaster.addType(webmasterType);
                                Association channelWebmaster = tm.createAssociation(webmasterType);
                                channelWebmaster.addPlayer(theChannel, channelType);
                                channelWebmaster.addPlayer(theWebmaster, webmasterType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_TTL: {
                    if(qName.equals(TAG_TTL)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_ttl.length() > 0) {
                            try {
                                Topic ttlType = getOrCreateTopic(CHANNEL_TTL_SI,"RSS Channel Time To Live");
                                parent.setData(theChannel, ttlType, "en", data_channel_ttl);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_CATEGORY: {
                    if(qName.equals(TAG_CATEGORY)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_category.length() > 0) {
                            try {
                                Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                Topic categoryType = getOrCreateTopic(CHANNEL_CATEGORY_SI,"RSS Channel Category");
                                Topic theCategory = getOrCreateTopic(CHANNEL_CATEGORY_SI + "/" + data_channel_category, data_channel_category);
                                theCategory.addType(categoryType);
                                Association channelCategory = tm.createAssociation(categoryType);
                                channelCategory.addPlayer(theChannel, channelType);
                                channelCategory.addPlayer(theCategory, categoryType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_RATING: {
                    if(qName.equals(TAG_RATING)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null && data_channel_rating.length() > 0) {
                            try {
                                Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                Topic ratingType = getOrCreateTopic(CHANNEL_RATING_SI,"RSS Channel Rating");
                                Topic theRating = getOrCreateTopic(CHANNEL_RATING_SI + "/" + data_channel_rating, data_channel_rating);
                                theRating.addType(ratingType);
                                Association channelRating = tm.createAssociation(ratingType);
                                channelRating.addPlayer(theChannel, channelType);
                                channelRating.addPlayer(theRating, ratingType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_TEXTINPUT: {
                    if(qName.equals(TAG_TEXTINPUT)) {
                        state = STATE_CHANNEL;
                    }
                    break;
                }
                case STATE_CHANNEL_ITEM: {
                    if(qName.equals(TAG_ITEM)) {
                        state = STATE_CHANNEL;
                        if(theItem != null) {
                            if(theChannel != null) {
                                try {
                                    Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel");
                                    Topic itemType = getOrCreateTopic(CHANNEL_ITEM_SI,"RSS Channel Item");

                                    Association channelItem = tm.createAssociation(itemType);
                                    channelItem.addPlayer(theChannel, channelType);
                                    channelItem.addPlayer(theItem, itemType);
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                            }
                        }
                    }
                    break;
                }
                
                
                // **** ITEM ****
                
                case STATE_CHANNEL_ITEM_TITLE: {
                    if(qName.equals(TAG_TITLE)) {
                        state = STATE_CHANNEL_ITEM;
                        if(theItem != null && data_channel_item_title.length() > 0) {
                            try {
                                theItem.setBaseName(data_channel_item_title + " (RSS item)");
                                theItem.setDisplayName("en", data_channel_item_title);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_ITEM_LINK: {
                    if(qName.equals(TAG_LINK)) {
                        state = STATE_CHANNEL_ITEM;
                        if(theItem != null && data_channel_item_link.length() > 0) {
                            try {
                                if(MAKE_LINK_SUBJECT_IDENTIFIER) {
                                    org.wandora.topicmap.Locator tempSI = theItem.getOneSubjectIdentifier();
                                    theItem.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_channel_item_link));
                                    theItem.removeSubjectIdentifier(tempSI);
                                }
                                if(MAKE_LINK_SUBJECT_LOCATOR) {
                                    theItem.setSubjectLocator(new org.wandora.topicmap.Locator(data_channel_item_link));
                                }
                                if(MAKE_LINK_OCCURRENCE) {
                                    Topic linkType = getOrCreateTopic(CHANNEL_ITEM_LINK_SI,"RSS Channel Item Link");
                                    parent.setData(theItem, linkType, "en", data_channel_item_link);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_ITEM_DESCRIPTION: {
                    if(qName.equals(TAG_DESCRIPTION)) {
                        state = STATE_CHANNEL_ITEM;
                        if(theItem != null && data_channel_item_description.length() > 0) {
                            try {
                                Topic descriptionType = getOrCreateTopic(CHANNEL_ITEM_DESCRIPTION_SI,"RSS Channel Item Description");
                                parent.setData(theItem, descriptionType, "en", data_channel_item_description);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_ITEM_PUBDATE: {
                    if(qName.equals(TAG_PUBDATE)) {
                        state = STATE_CHANNEL_ITEM;
                        if(theItem != null && data_channel_item_pubdate.length() > 0) {
                            try {
                                Topic itemType = getOrCreateTopic(CHANNEL_ITEM_SI,"RSS Channel Item");
                                Topic dateType = getOrCreateTopic(DATE_SI,"Date");
                                Topic pubdateType = getOrCreateTopic(CHANNEL_ITEM_PUBDATE_SI,"RSS Channel Item Publish Date");
                                Topic theDate = getOrCreateTopic(DATE_SI + "/" + data_channel_item_pubdate, data_channel_item_pubdate);
                                theDate.addType(dateType);
                                Association itemPubdate = tm.createAssociation(pubdateType);
                                itemPubdate.addPlayer(theItem, itemType);
                                itemPubdate.addPlayer(theDate, dateType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_ITEM_GUID: {
                    if(qName.equals(TAG_GUID)) {
                        state = STATE_CHANNEL_ITEM;
                        if(theItem != null && data_channel_item_guid.length() > 0) {
                            try {
                                //theItem.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_channel_item_guid));
                                Topic guidType = getOrCreateTopic(CHANNEL_ITEM_GUID_SI,"RSS Channel Item GUID");
                                parent.setData(theItem, guidType, "en", data_channel_item_guid);

                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_ITEM_CATEGORY: {
                    if(qName.equals(TAG_CATEGORY)) {
                        state = STATE_CHANNEL_ITEM;
                        if(theItem != null && data_channel_item_category.length() > 0) {
                            try {
                                Topic itemType = getOrCreateTopic(CHANNEL_ITEM_SI,"RSS Channel Item");
                                Topic categoryType = getOrCreateTopic(CHANNEL_ITEM_CATEGORY_SI,"RSS Channel Item Category");
                                Topic theCategory = getOrCreateTopic(CHANNEL_ITEM_CATEGORY_SI + "/" + data_channel_item_category, data_channel_item_category);
                                theCategory.addType(categoryType);
                                Association itemCategory = tm.createAssociation(categoryType);
                                itemCategory.addPlayer(theItem, itemType);
                                itemCategory.addPlayer(theCategory, categoryType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_CHANNEL_ITEM_AUTHOR: {
                    if(qName.equals(TAG_AUTHOR)) {
                        state = STATE_CHANNEL_ITEM;
                        if(theItem != null && data_channel_item_author.length() > 0) {
                            try {
                                Topic itemType = getOrCreateTopic(CHANNEL_ITEM_SI,"RSS Channel Item");
                                Topic authorType = getOrCreateTopic(CHANNEL_ITEM_AUTHOR_SI,"RSS Channel Item Author");
                                Topic theAuthor = getOrCreateTopic(CHANNEL_ITEM_AUTHOR_SI + "/" + data_channel_item_author, data_channel_item_author);
                                theAuthor.addType(authorType);
                                Association itemAuthor = tm.createAssociation(authorType);
                                itemAuthor.addPlayer(theItem, itemType);
                                itemAuthor.addPlayer(theAuthor, authorType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                
                
                // ***** IMAGE *****
                case STATE_CHANNEL_IMAGE: {
                    if(qName.equals(TAG_IMAGE)) {
                        state = STATE_CHANNEL;
                        if(theChannel != null)
                            if(data_channel_image_title != null && data_channel_image_title.length() > 0) {
                                try {
                                    Topic channelType = getOrCreateTopic(CHANNEL_SI,"RSS Channel Item");
                                    Topic imageType = getOrCreateTopic(CHANNEL_IMAGE_SI,"RSS Channel Image");
                                    Topic theImage = getOrCreateTopic(CHANNEL_IMAGE_SI + "/" + data_channel_image_title, data_channel_image_title + " (RSS channel image)");
                                    if(data_channel_image_url != null && data_channel_image_url.length() > 0) {
                                        theImage.setSubjectLocator(new org.wandora.topicmap.Locator(data_channel_image_url));
                                    }
                                    
                                    theImage.addType(imageType);
                                    Association channelImage = tm.createAssociation(imageType);
                                    channelImage.addPlayer(theChannel, channelType);
                                    channelImage.addPlayer(theImage, imageType);
                                    
                                    if(data_channel_image_link != null && data_channel_image_link.length() > 0) {
                                        Topic linkType = getOrCreateTopic(CHANNEL_IMAGE_LINK_SI,"Channel Image Link");
                                        parent.setData(theImage, linkType, "en", data_channel_image_link);
                                    }
                                    if(data_channel_image_description != null && data_channel_image_description.length() > 0) {
                                        Topic descriptionType = getOrCreateTopic(CHANNEL_IMAGE_DESCRIPTION_SI,"Channel Image Description");
                                        parent.setData(theImage, descriptionType, "en", data_channel_image_description);
                                    }
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                            }
                        }
                    }
                    break;
                case STATE_CHANNEL_IMAGE_LINK:
                    if(qName.equals(TAG_LINK)) {
                        state = STATE_CHANNEL_IMAGE;
                    }
                    break;
                case STATE_CHANNEL_IMAGE_URL:
                    if(qName.equals(TAG_URL)) {
                        state = STATE_CHANNEL_IMAGE;
                    }
                    break;
                case STATE_CHANNEL_IMAGE_TITLE:
                    if(qName.equals(TAG_TITLE)) {
                        state = STATE_CHANNEL_IMAGE;
                    }
                    break;
                case STATE_CHANNEL_IMAGE_DESCRIPTION:
                    if(qName.equals(TAG_DESCRIPTION)) {
                        state = STATE_CHANNEL_IMAGE;
                    }
                    break;
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_CHANNEL_TITLE:
                    data_channel_title+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_LINK:
                    data_channel_link+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_DESCRIPTION:
                    data_channel_description+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_LANGUAGE:
                    data_channel_language+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_PUBDATE:
                    data_channel_pubdate+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_LASTBUILDDATE:
                    data_channel_lastbuilddate+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_DOCS:
                    data_channel_docs+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_GENERATOR:
                    data_channel_generator+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_MANAGINGEDITOR:
                    data_channel_managingeditor+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_WEBMASTER:
                    data_channel_webmaster+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_TTL:
                    data_channel_ttl+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_COPYRIGHT:
                    data_channel_copyright+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_CATEGORY:
                    data_channel_category+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_RATING:
                    data_channel_rating+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_ITEM_TITLE:
                    data_channel_item_title+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_ITEM_LINK:
                    data_channel_item_link+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_ITEM_DESCRIPTION:
                    data_channel_item_description+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_ITEM_PUBDATE:
                    data_channel_item_pubdate+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_ITEM_GUID:
                    data_channel_item_guid+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_ITEM_CATEGORY:
                    data_channel_item_category+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_ITEM_AUTHOR:
                    data_channel_item_author+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_IMAGE_TITLE:
                    data_channel_image_title+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_IMAGE_LINK:
                    data_channel_image_link+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_IMAGE_URL:
                    data_channel_image_url+=new String(ch,start,length);
                    break;
                case STATE_CHANNEL_IMAGE_DESCRIPTION:
                    data_channel_image_description+=new String(ch,start,length);
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
}
