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
 * MediaWikiExtractor.java
 *
 * Created on 9. maaliskuuta 2007, 11:33
 *
 */

package org.wandora.application.tools.extractors;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import java.util.regex.*;

import org.wandora.topicmap.*;
import org.wandora.application.gui.*;



/**
 * Extracts text and metadata from exported MediaWiki pages. Exported MediaWiki
 * pages are XML dumps. Example of exported MediaWiki page can be found at
 *
 * http://wandora.orgwiki/Special:Export/Wandora
 *
 * @author olli
 */
public class MediaWikiExtractor extends AbstractExtractor {

//    private String extracterUrl = null;

	private static final long serialVersionUID = 1L;
	
	
	private String wikiBaseURL = null;
    private boolean followRedirects = false;



    
    /** Creates a new instance of MediaWikiExtractor */
    public MediaWikiExtractor() {
    }

    public void setWikiBaseURL(String url){
        wikiBaseURL=url;
    }

    public void setFollowRedirects(boolean b){
        followRedirects=b;
    }
    
    @Override
    public String getName() {
        return "MediaWiki extractor";
    }
    @Override
    public String getDescription(){
        return "Extracts text and metadata from exported MediaWiki pages.";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_mediawiki.png");
    }
 
    
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        String urlS = url.toExternalForm();
        return _extractTopicsFrom(url.openStream(),urlS,topicMap);
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        String url = file.toURI().toURL().toExternalForm();
        return _extractTopicsFrom(new FileInputStream(file),url,topicMap);
    }

    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        boolean answer = _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), "http://wandora.org/si/mediawiki-extractor/"+System.currentTimeMillis(), topicMap);
        return answer;
    }


    public boolean _extractTopicsFrom(InputStream in, String url, TopicMap topicMap) throws Exception {
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        WikiParser parserHandler = new WikiParser(url, topicMap, this);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        return true;
    }



    public String extractTopicsFromWiki(String page, TopicMap topicMap) throws Exception {
        if(wikiBaseURL!=null){
            String url=wikiBaseURL.replace("__1__", page.replace(" ", "_"));
            if(_extractTopicsFrom(new URL(url),topicMap)) return url;
            else return null;
        }
        else return null;
    }

    private static class WikiParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        public WikiParser(String wikiUrl, TopicMap tm, MediaWikiExtractor parent){
            this.url = wikiUrl;
            this.tm=tm;
            this.parent=parent;
        }
        
        private String url;
        private TopicMap tm;
        private MediaWikiExtractor parent;

        public static final Pattern redirectPattern=Pattern.compile("#REDIRECT\\s*\\[\\[(.*?)\\]\\]");
        
        public static final String TAG_MEDIAWIKI="mediawiki";
        public static final String TAG_SITEINFO="siteinfo";
        public static final String TAG_SITENAME="sitename";
        public static final String TAG_BASE="base";
        public static final String TAG_GENERATOR="generator"; // not used
        public static final String TAG_CASE="case"; // not used;
        public static final String TAG_NAMESPACES="namespaces";
        public static final String TAG_NAMESPACE="namespace";
        public static final String TAG_PAGE="page";
        public static final String TAG_TITLE="title";
        public static final String TAG_PAGEID="id";
        public static final String TAG_RESTRICTIONS="restrictions";
        public static final String TAG_REVISION="revision";
        public static final String TAG_REVISIONID="id";
        public static final String TAG_TIMESTAMP="timestamp";
        public static final String TAG_CONTRIBUTOR="contributor";
        public static final String TAG_COMMENT="comment"; // not used
        public static final String TAG_USERNAME="username";
        public static final String TAG_CONTRIBUTORID="id";
        public static final String TAG_TEXT="text";

        private static final int STATE_START=0;
        private static final int STATE_MEDIAWIKI=1;
        private static final int STATE_SITEINFO=2;
        private static final int STATE_SITENAME=3;
        private static final int STATE_BASE=4;
        private static final int STATE_NAMESPACES=5;
        private static final int STATE_NAMESPACE=6;
        private static final int STATE_PAGE=7;
        private static final int STATE_TITLE=8;
        private static final int STATE_PAGEID=9;
        private static final int STATE_RESTRICTIONS=10;
        private static final int STATE_REVISION=11;
        private static final int STATE_REVISIONID=12;
        private static final int STATE_TIMESTAMP=13;
        private static final int STATE_CONTRIBUTOR=14;
        private static final int STATE_COMMENT=15;
        private static final int STATE_USERNAME=16;
        private static final int STATE_CONTRIBUTORID=17;
        private static final int STATE_TEXT=18;
        
        private int state=STATE_START;
        
        public static String SIPREFIX="http://wandora.org/si/mediawiki/";
        public static String CONTRIBUTOR_SI=SIPREFIX+"contributor";
        public static String PAGE_SI=SIPREFIX+"page";
        public static String TIMESTAMP_SI=SIPREFIX+"timestamp";
        public static String TEXT_SI=SIPREFIX+"text";
        public static String WIKI_SI=SIPREFIX+"wiki";
        public static String REDIRECT_SI=SIPREFIX+"redirect";
        public static String REDIRECT_FROM_SI=SIPREFIX+"redirect_from";
        public static String REDIRECT_TO_SI=SIPREFIX+"redirect_to";
        
        private String data_sitename;
        private String data_base;
        private Vector<String> data_namespaces;
        private String data_namespace;
        private String data_title;
        private String data_pageid;
        private String data_restrictions;
        private String data_revisionid;
        private String data_timestamp;
        private String data_username;
        private String data_contributorid;
        private HashSet<String> data_contributors;
        private String data_text;
        private int data_latestrevision;
        private String data_latesttext;
        private String data_latesttimestamp;



        private Topic getOrCreateTopic(String si) throws TopicMapException {
            return ExtractHelper.getOrCreateTopic(si, tm);
        }
        private Topic getOrCreateTopic(String si, String bn) throws TopicMapException {
            return ExtractHelper.getOrCreateTopic(si, bn, tm);
        }



        
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
                    if(qName.equals(TAG_MEDIAWIKI)) {
                        state=STATE_MEDIAWIKI;
                        data_sitename=null;
                        data_base=null;
                        data_namespaces=null;
                        data_title=null;
                        data_namespaces=null;
                        data_contributors=null;
                        data_latestrevision=-1;
                        data_latesttext=null;
                        data_latesttimestamp=null;
                    }
                    break;
                case STATE_MEDIAWIKI:
                    if(qName.equals(TAG_SITEINFO)){
                        state=STATE_SITEINFO;
                    }
                    else if(qName.equals(TAG_PAGE)){
                        state=STATE_PAGE;
                        data_contributors=new HashSet<String>();
                        data_latestrevision=-1;
                        data_latesttext=null;
                        data_latesttimestamp=null;
                    }
                    break;
                case STATE_SITEINFO:
                    if(qName.equals(TAG_SITENAME)){
                        state=STATE_SITENAME;
                        data_sitename="";
                    }
                    else if(qName.equals(TAG_BASE)){
                        state=STATE_BASE;
                        data_base="";
                    }
                    else if(qName.equals(TAG_NAMESPACES)){
                        state=STATE_NAMESPACES;
                        data_namespaces=new Vector<String>();
                    }
                    break;
                case STATE_NAMESPACES:
                    if(qName.equals(TAG_NAMESPACE)){
                        state=STATE_NAMESPACE;
                        data_namespace="";
                    }
                    break;
                case STATE_PAGE:
                    if(qName.equals(TAG_TITLE)){
                        state=STATE_TITLE;
                        data_title="";
                    }
                    else if(qName.equals(TAG_PAGEID)){
                        state=STATE_PAGEID;
                        data_pageid="";
                    }
                    else if(qName.equals(TAG_RESTRICTIONS)){
                        state=STATE_RESTRICTIONS;
                        data_restrictions="";
                    }
                    else if(qName.equals(TAG_REVISION)){
                        state=STATE_REVISION;
                        data_revisionid=null;
                        data_timestamp=null;
                        data_username=null;
                        data_contributorid=null;
                        data_text=null;
                    }
                    break;
                case STATE_REVISION:
                    if(qName.equals(TAG_REVISIONID)){
                        state=STATE_REVISIONID;
                        data_revisionid="";
                    }
                    else if(qName.equals(TAG_TIMESTAMP)){
                        state=STATE_TIMESTAMP;
                        data_timestamp="";
                    }
                    else if(qName.equals(TAG_CONTRIBUTOR)){
                        state=STATE_CONTRIBUTOR;
                    }
                    else if(qName.equals(TAG_COMMENT)){
                        state=STATE_COMMENT;
                    }
                    else if(qName.equals(TAG_TEXT)){
                        state=STATE_TEXT;
                        data_text="";
                    }
                    break;
                case STATE_CONTRIBUTOR:
                    if(qName.equals(TAG_USERNAME)){
                        state=STATE_USERNAME;
                        data_username="";
                    }
                    else if(qName.equals(TAG_CONTRIBUTORID)){
                        state=STATE_CONTRIBUTORID;
                        data_contributorid="";
                    }
                    break;
            }
        }
        
        
        
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state){
                case STATE_SITENAME:
                    if(qName.equals(TAG_SITENAME)) state=STATE_SITEINFO;
                    break;
                case STATE_BASE:
                    if(qName.equals(TAG_BASE)) state=STATE_SITEINFO;
                    break;
                case STATE_NAMESPACE:
                    if(qName.equals(TAG_NAMESPACE)){
                        state=STATE_NAMESPACES;
                        data_namespaces.add(data_namespace);
                    }
                    break;
                case STATE_NAMESPACES:
                    if(qName.equals(TAG_NAMESPACES)) state=STATE_SITEINFO;
                    break;
                case STATE_SITEINFO:
                    if(qName.equals(TAG_SITEINFO)) {
                        state=STATE_MEDIAWIKI;
                        if(data_sitename!=null){
                            try{
                                Topic wikiTopic=tm.createTopic();
                                try {
                                    URL si = new URL(url);
                                    String protocol = si.getProtocol();
                                    String host = si.getHost();
                                    wikiTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(protocol+"://"+host));
                                }
                                catch(Exception e) {
                                    wikiTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                                }
                                wikiTopic.setBaseName(data_sitename);
                                wikiTopic.addType(getOrCreateTopic(WIKI_SI,"Wiki"));
                            }catch(TopicMapException tme){
                                parent.log(tme);
                            }
                        }
                    }
                    break;
                case STATE_TITLE:
                    if(qName.equals(TAG_TITLE)) state=STATE_PAGE;
                    break;
                case STATE_PAGEID:
                    if(qName.equals(TAG_PAGEID)) state=STATE_PAGE;
                    break;
                case STATE_RESTRICTIONS:
                    if(qName.equals(TAG_RESTRICTIONS)) state=STATE_PAGE;
                    break;                    
                case STATE_REVISIONID:
                    if(qName.equals(TAG_REVISIONID)) state=STATE_REVISION;
                    break;
                case STATE_TIMESTAMP:
                    if(qName.equals(TAG_TIMESTAMP)) state=STATE_REVISION;
                    break;
                case STATE_USERNAME:
                    if(qName.equals(TAG_USERNAME)) state=STATE_CONTRIBUTOR;
                    break;
                case STATE_CONTRIBUTORID:
                    if(qName.equals(TAG_CONTRIBUTORID)) state=STATE_CONTRIBUTOR;
                    break;
                case STATE_CONTRIBUTOR:
                    if(qName.equals(TAG_CONTRIBUTOR)){
                        state=STATE_REVISION;
                        if(data_username!=null) data_contributors.add(data_username.trim());
                    }
                    break;
                case STATE_COMMENT:
                    if(qName.equals(TAG_COMMENT)) state=STATE_REVISION;
                    break;
                case STATE_TEXT:
                    if(qName.equals(TAG_TEXT)) state=STATE_REVISION;
                    break;
                case STATE_REVISION:
                    if(qName.equals(TAG_REVISION)) {
                        state=STATE_PAGE;
                        int parsedid=Integer.parseInt(data_revisionid);
                        if(parsedid>data_latestrevision){
                            data_latestrevision=parsedid;
                            data_latesttext=data_text;
                            data_latesttimestamp=data_timestamp;
                        }
                    }
                    break;
                case STATE_PAGE:
                    if(qName.equals(TAG_PAGE)) {
                        state=STATE_MEDIAWIKI;
                        try{
                            Topic t=tm.createTopic();
                            if(data_sitename!=null && data_pageid!=null) {
                                t.addSubjectIdentifier(tm.createLocator(url));
                                t.addSubjectIdentifier(tm.createLocator(SIPREFIX+"pages/"+data_sitename+"/"+data_pageid));
                            }
                            else {
                                t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                            }
                            if(data_title!=null) t.setBaseName(data_title.trim());
                            Topic contributor_type=getOrCreateTopic(CONTRIBUTOR_SI,"Wiki Contributor");
                            Topic page_type=getOrCreateTopic(PAGE_SI,"MediaWiki page");
                            Topic text_type=getOrCreateTopic(TEXT_SI,"Wiki content");
                            Topic wiki_type=getOrCreateTopic(WIKI_SI,"Wiki");
                            t.addType(page_type);
                            if(data_sitename!=null){
                                Topic wikiTopic=getOrCreateTopic(null,data_sitename);
                                Association wa=tm.createAssociation(wiki_type);
                                wa.addPlayer(t,page_type);
                                wa.addPlayer(wikiTopic,wiki_type);
                            }
                            for(String contributor : data_contributors){
                                Topic ct=getOrCreateTopic(null,"Wiki contributor: "+contributor);
                                if(!ct.isOfType(contributor_type)) ct.addType(contributor_type);
                                Association a=tm.createAssociation(contributor_type);
                                a.addPlayer(t,page_type);
                                a.addPlayer(ct,contributor_type);
                            }
                            if(data_latestrevision!=-1){
                                t.setData(text_type,getOrCreateTopic(XTMPSI.getLang(null)),data_latesttext);
                                t.setData(getOrCreateTopic(TIMESTAMP_SI,"Wiki timestamp"),getOrCreateTopic(XTMPSI.getLang(null)),data_latesttimestamp);

                                if(parent.followRedirects && parent.wikiBaseURL!=null){
                                    Matcher m=redirectPattern.matcher(data_latesttext);
                                    if(m.find()){
                                        String page=m.group(1);
                                        parent.log("Following redirect to "+page);
                                        try{
                                            String reurl=parent.extractTopicsFromWiki(page, tm);
                                            if(reurl!=null){
                                                Topic to=tm.getTopic(reurl);
                                                if(to!=null){
                                                    Topic redirect_type=getOrCreateTopic(REDIRECT_SI,"Redirect");
                                                    Topic from_type=getOrCreateTopic(REDIRECT_FROM_SI,"Redirect from");
                                                    Topic to_type=getOrCreateTopic(REDIRECT_TO_SI,"Redirect to");
                                                    Association a=tm.createAssociation(redirect_type);
                                                    a.addPlayer(t, from_type);
                                                    a.addPlayer(to, to_type);
                                                }
                                            }
                                        }catch(Exception e){
                                            parent.log(e);
                                        }
                                    }
                                }
                            }
                        }catch(TopicMapException tme){
                            parent.log(tme);
                        }
                    }
                    break;
                case STATE_MEDIAWIKI:
                    break;
            }
        }
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_SITENAME:
                    data_sitename+=new String(ch,start,length);
                    break;
                case STATE_BASE:
                    data_base+=new String(ch,start,length);
                    break;
                case STATE_NAMESPACE:
                    data_namespace+=new String(ch,start,length);
                    break;
                case STATE_TITLE:
                    data_title+=new String(ch,start,length);
                    break;
                case STATE_PAGEID:
                    data_pageid+=new String(ch,start,length);
                    break;
                case STATE_RESTRICTIONS:
                    data_restrictions+=new String(ch,start,length);
                    break;
                case STATE_REVISIONID:
                    data_revisionid+=new String(ch,start,length);
                    break;
                case STATE_TIMESTAMP:
                    data_timestamp+=new String(ch,start,length);
                    break;
                case STATE_USERNAME:
                    data_username+=new String(ch,start,length);
                    break;
                case STATE_CONTRIBUTORID:
                    data_contributorid+=new String(ch,start,length);
                    break;
                case STATE_TEXT:
                    data_text+=new String(ch,start,length);
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
    
    

    public static final String[] contentTypes=new String[] { "application/xml" };
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }


}


