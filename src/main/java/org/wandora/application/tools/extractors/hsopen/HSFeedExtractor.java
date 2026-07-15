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
 * RSSExtractor.java
 *
 * Created on 3. marraskuuta 2007, 13:18
 *
 */

package org.wandora.application.tools.extractors.hsopen;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.wandora.application.Wandora;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicTools;
import org.wandora.topicmap.XTMPSI;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 *
 * @author akivela
 */
public class HSFeedExtractor extends AbstractExtractor {
    
	private static final long serialVersionUID = 1L;

	/** Creates a new instance of HSFeedExtractor */
    public HSFeedExtractor() {
    }

    @Override
    public boolean useURLCrawler() {
        return false;
    }

     
    @Override
    public String getName() {
        return "HS Feed Extractor";
    }
    @Override
    public String getDescription(){
        return "Extractor reads HS feed and converts the feed to a topic map.";
    }
    /*
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_hs.png");
    }
    */
    
    private final String[] contentTypes=new String[] { "text/xml", "application/xml", "application/rss+xml", "application/xhtml+xml" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }



    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        URLConnection uc=url.openConnection();
        Wandora.initUrlConnection(uc);
        return _extractTopicsFrom(uc.getInputStream(),topicMap);
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


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
        HSFeedParser parserHandler = new HSFeedParser(topicMap,this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        log("Total " + parserHandler.articleCount + " HS articles processed!");
        return true;
    }
    
    
    
    
    
    

    private static class HSFeedParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public static boolean MAKE_LINK_SUBJECT_LOCATOR = false;
        public static boolean MAKE_SUBCLASS_OF_WANDORA_CLASS = true;
        
        public HSFeedParser(TopicMap tm, HSFeedExtractor parent){
            this.tm=tm;
            this.parent=parent;
        }
        
        public int progress=0;
        public int articleCount = 0;
        
        private TopicMap tm;
        private HSFeedExtractor parent;
        
        public static final String TAG_HS="articles";
        public static final String TAG_ARTICLE="article";
        public static final String TAG_TITLE="mainHeader";
        public static final String TAG_LINK="link";
        public static final String TAG_DESCRIPTION="description";
        public static final String TAG_LANGUAGE="language";
        public static final String TAG_CREATEDDATE="createdDate";
        public static final String TAG_LASTBUILDDATE="lastBuildDate";


        
        private static final int STATE_START=0;
        private static final int STATE_HS=2;
        private static final int STATE_ARTICLE=4;
        private static final int STATE_ARTICLE_TITLE=5;
        private static final int STATE_ARTICLE_LINK=6;
        private static final int STATE_ARTICLE_DESCRIPTION=7;
        private static final int STATE_ARTICLE_LANGUAGE=8;
        private static final int STATE_ARTICLE_CREATEDDATE=9;
        private static final int STATE_ARTICLE_LASTBUILDDATE=10;
        
               
        private int state=STATE_START;
        
        public static String HSFEED_SI = "http://purl.org/hsfeed/";

        public static String SIPREFIX="http://purl.org/hsfeed/";
        public static String ARTICLE_SI=SIPREFIX+"article";
        public static String ARTICLE_LINK_SI=ARTICLE_SI+"/link";
        public static String ARTICLE_DESCRIPTION_SI=ARTICLE_SI+"/description";
        public static String ARTICLE_LANGUAGE_SI=ARTICLE_SI+"/language";
        public static String ARTICLE_CREATEDDATE_SI=ARTICLE_SI+"/CREATEDDATE";
        public static String ARTICLE_LASTBUILDDATE_SI=ARTICLE_SI+"/lastbuilddate";
        
        public static String DATE_SI="http://wandora.org/si/date"; 
        
        private String data_article_title;
        private String data_article_link;
        private String data_article_description;
        private String data_article_language;
        private String data_article_createddate;
        private String data_article_lastbuilddate;

        private Topic theArticle;

        
        
        private Topic getOrCreateTopic(String si) throws TopicMapException {
            return getOrCreateTopic(si, null);
        }
        private Topic getOrCreateTopic(String si,String bn) throws TopicMapException {
            return ExtractHelper.getOrCreateTopic(si, bn, tm);
        }
        
        public void startDocument() throws SAXException {
            articleCount = 0;
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
                    if(qName.equals(TAG_HS)) {
                        state = STATE_HS;
                    }
                    break;
                case STATE_HS:
                    if(qName.equals(TAG_ARTICLE)) {
                        state = STATE_ARTICLE;
                        try {
                            Topic articleType=getOrCreateTopic(ARTICLE_SI,"HS feed article");
                            theArticle = tm.createTopic();
                            theArticle.addSubjectIdentifier(TopicTools.createDefaultLocator());
                            theArticle.addType(articleType);
                            articleCount++;
                            
                            Topic HSFeedClass = getOrCreateTopic(HSFEED_SI, "HS feed");
                            Topic superClass = getOrCreateTopic(XTMPSI.SUPERCLASS, null);
                            Topic subClass = getOrCreateTopic(XTMPSI.SUBCLASS, null);
                            Topic supersubClass = getOrCreateTopic(XTMPSI.SUPERCLASS_SUBCLASS, null);

                            Association supersubClassAssociation = tm.createAssociation(supersubClass);
                            supersubClassAssociation.addPlayer(HSFeedClass, superClass);
                            supersubClassAssociation.addPlayer(articleType, subClass);

                            if(MAKE_SUBCLASS_OF_WANDORA_CLASS) {
                                Topic wandoraClass = getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class");
                                supersubClassAssociation = tm.createAssociation(supersubClass);
                                supersubClassAssociation.addPlayer(wandoraClass, superClass);
                                supersubClassAssociation.addPlayer(HSFeedClass, subClass);
                            }
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                    }
                    break;
                case STATE_ARTICLE:
                    if(qName.equals(TAG_TITLE)) {
                        state = STATE_ARTICLE_TITLE;
                        data_article_title = "";
                    }
                    else if(qName.equals(TAG_LINK)) {
                        state = STATE_ARTICLE_LINK;
                        data_article_link = "";
                    }
                    else if(qName.equals(TAG_DESCRIPTION)) {
                        state = STATE_ARTICLE_DESCRIPTION;
                        data_article_description = "";
                    }
                    else if(qName.equals(TAG_LANGUAGE)) {
                        state = STATE_ARTICLE_LANGUAGE;
                        data_article_language = "";
                    }
                    else if(qName.equals(TAG_CREATEDDATE)) {
                        state = STATE_ARTICLE_CREATEDDATE;
                        data_article_createddate = "";
                    }
                    else if(qName.equals(TAG_LASTBUILDDATE)) {
                        state = STATE_ARTICLE_LASTBUILDDATE;
                        data_article_lastbuilddate = "";
                    }
                    
                    break;
                
            }
        }
        
        
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            //parent.log("  END" + state +" --- " + qName);
            
            switch(state) {
                case STATE_HS: {
                    if(qName.equals(TAG_HS)) {
                        state = STATE_START;
                    }
                    break;
                }
                case STATE_ARTICLE: {
                    if(qName.equals(TAG_ARTICLE)) {
                        state = STATE_HS;
                        if(theArticle != null) {
                        }
                    }
                    break;
                }
                case STATE_ARTICLE_TITLE: {
                    if(qName.equals(TAG_TITLE)) {
                        state = STATE_ARTICLE;
                        if(theArticle != null && data_article_title.length() > 0) {
                            try {
                                theArticle.setBaseName(data_article_title + " (HS feed article)");
                                theArticle.setDisplayName("fi", data_article_title);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_ARTICLE_LINK: {
                    if(qName.equals(TAG_LINK)) {
                        state = STATE_ARTICLE;
                        if(theArticle != null && data_article_link.length() > 0) {
                            try {
                                if(MAKE_LINK_SUBJECT_LOCATOR) {
                                    theArticle.setSubjectLocator(new org.wandora.topicmap.Locator(data_article_link));
                                }
                                else {
                                    Topic linkType = getOrCreateTopic(ARTICLE_LINK_SI,"HS feed Link");
                                    parent.setData(theArticle, linkType, "en", data_article_link);
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_ARTICLE_DESCRIPTION: {
                    if(qName.equals(TAG_DESCRIPTION)) {
                        state = STATE_ARTICLE;
                        if(theArticle != null && data_article_description.length() > 0) {
                            try {
                                Topic descriptionType = getOrCreateTopic(ARTICLE_DESCRIPTION_SI,"HS feed description");
                                parent.setData(theArticle, descriptionType, "fi", data_article_description);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_ARTICLE_LANGUAGE: {
                    if(qName.equals(TAG_LANGUAGE)) {
                        state = STATE_ARTICLE;
                        if(theArticle != null && data_article_language.length() > 0) {
                            try {
                                Topic articleType = getOrCreateTopic(ARTICLE_SI,"HS feed article");
                                Topic languageType = getOrCreateTopic(ARTICLE_LANGUAGE_SI,"RSS Channel Language");
                                Topic theLanguage = getOrCreateTopic(ARTICLE_LANGUAGE_SI + "/" + data_article_language, data_article_language);
                                theLanguage.addType(languageType);
                                Association articleLanguage = tm.createAssociation(languageType);
                                articleLanguage.addPlayer(theArticle, articleType);
                                articleLanguage.addPlayer(theLanguage, languageType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                
                case STATE_ARTICLE_CREATEDDATE: {
                    if(qName.equals(TAG_CREATEDDATE)) {
                        state = STATE_ARTICLE;
                        if(theArticle != null && data_article_createddate.length() > 0) {
                            try {
                                Topic articleType = getOrCreateTopic(ARTICLE_SI,"HS feed article");
                                Topic dateType = getOrCreateTopic(DATE_SI,"Date");
                                Topic createddateType = getOrCreateTopic(ARTICLE_CREATEDDATE_SI,"HS feed article date");
                                Topic theDate = getOrCreateTopic(DATE_SI + "/" + data_article_createddate, data_article_createddate);
                                theDate.addType(dateType);
                                Association articleCreatedDate = tm.createAssociation(createddateType);
                                articleCreatedDate.addPlayer(theArticle, articleType);
                                articleCreatedDate.addPlayer(theDate, dateType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
                case STATE_ARTICLE_LASTBUILDDATE: {
                    if(qName.equals(TAG_LASTBUILDDATE)) {
                        state = STATE_ARTICLE;
                        if(theArticle != null && data_article_lastbuilddate.length() > 0) {
                            try {
                                Topic articleType = getOrCreateTopic(ARTICLE_SI,"HS feed article");
                                Topic dateType = getOrCreateTopic(DATE_SI,"Date");
                                Topic lastbuilddateType = getOrCreateTopic(ARTICLE_LASTBUILDDATE_SI,"HS feed article last build date");
                                Topic theDate = getOrCreateTopic(DATE_SI + "/" + data_article_lastbuilddate, data_article_lastbuilddate);
                                theDate.addType(dateType);
                                Association articlePubDate = tm.createAssociation(lastbuilddateType);
                                articlePubDate.addPlayer(theArticle, articleType);
                                articlePubDate.addPlayer(theDate, dateType);
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                    }
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_ARTICLE_TITLE:
                    data_article_title+=new String(ch,start,length);
                    break;
                case STATE_ARTICLE_LINK:
                    data_article_link+=new String(ch,start,length);
                    break;
                case STATE_ARTICLE_DESCRIPTION:
                    data_article_description+=new String(ch,start,length);
                    break;
                case STATE_ARTICLE_LANGUAGE:
                    data_article_language+=new String(ch,start,length);
                    break;
                case STATE_ARTICLE_CREATEDDATE:
                    data_article_createddate+=new String(ch,start,length);
                    break;
                case STATE_ARTICLE_LASTBUILDDATE:
                    data_article_lastbuilddate+=new String(ch,start,length);
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
