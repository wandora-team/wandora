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
 * OviMediaExtractor.java
 * 
 */



package org.wandora.application.tools.extractors.ovi;

import java.net.*;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.xml.xpath.*;
import javax.xml.parsers.*;
import org.wandora.utils.*;
import org.wandora.application.gui.*;
import java.awt.*;
import javax.swing.*;
import java.text.*;

import org.wandora.application.tools.extractors.*;
import org.wandora.application.tools.extractors.geonames.*;
import org.wandora.topicmap.*;


/**
 *
 * @author akivela
 */
public class OviMediaExtractor extends AbstractExtractor {
    private String defaultEncoding = "ISO-8859-1";
    private static final String oviMedia="http://media.share.ovi.com";
    public static String defaultLanguage = "en";
    
    public static final String OVI_SI = "http://share.ovi.com/";
    public static final String OVI_LOCATION_SI = OVI_SI+"location";
    public static final String OVI_AUTHOR_SI = OVI_SI+"author";
    public static final String OVI_TAG_SI = OVI_SI+"tag";
    public static final String OVI_ENTRY_SI = OVI_SI+"entry";
    public static final String OVI_DESCRIPTION_SI = OVI_SI+"description";
    public static final String OVI_DATE_SI = OVI_SI+"date";
    
    

    private static final SimpleNamespaceContext namespaceContext;
    static {
        SimpleNamespaceContext c=new SimpleNamespaceContext();
        c.setPrefix("media", "http://search.yahoo.com/mrss/");
        c.setPrefix("atom", "http://www.w3.org/2005/Atom");
        c.setPrefix("dcterms", "http://purl.org/dc/terms/");
        c.setPrefix("georss", "http://www.georss.org/georss");
        namespaceContext=c;
    }

    
    
    public OviMediaExtractor() {
        
    }
    

    @Override
    public String getName() {
        return "Ovi media extractor...";
    }
    
    @Override
    public String getDescription(){
        return "Read shared media feed from Ovi and convert it to a topic map.";
    }
    

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_ovi.png");
    }
    
    private final String[] contentTypes=new String[] { "application/xml", "text/xml", "application/rss+xml" };

    public String[] getContentTypes() {
        return contentTypes;
    }


    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        String in = IObox.doUrl(url);
        return _extractTopicsFrom(in, topicMap);
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception { 
        String str = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(str, topicMap);
    }
    
    
    public boolean _extractTopicsFrom(String in, TopicMap tm) throws Exception {        
        try {
            int process=0;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder=factory.newDocumentBuilder();
            Document doc=builder.parse(new InputSource(new ByteArrayInputStream(in.getBytes())));
            Element e=doc.getDocumentElement();
            XPath xpath=XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(namespaceContext);
            
            NodeList nl=(NodeList)xpath.evaluate("//channel/item", doc,XPathConstants.NODESET);
            for(int i=0;i<nl.getLength();i++) {
                Node n=nl.item(i);
                HashMap<String,String> data = new HashMap<String,String>();
                data.put("guid", (String)xpath.evaluate("guid",n,XPathConstants.STRING));
                data.put("imageURL", (String)xpath.evaluate("media:group/media:content[@medium='image'][1]/@url",n,XPathConstants.STRING));
                data.put("audioURL", (String)xpath.evaluate("media:group/media:content[@medium='audio'][1]/@url",n,XPathConstants.STRING));
                data.put("videoURL", (String)xpath.evaluate("media:group/media:content[@type='video/x-flv'][1]/@url",n,XPathConstants.STRING));
                data.put("title", (String)xpath.evaluate("title",n,XPathConstants.STRING));
                data.put("link", (String)xpath.evaluate("link",n,XPathConstants.STRING));
                data.put("description", (String)xpath.evaluate("description",n,XPathConstants.STRING));
                data.put("keywords", (String)xpath.evaluate("media:keywords",n,XPathConstants.STRING));
                data.put("author", (String)xpath.evaluate("media:credit[@role='author']",n,XPathConstants.STRING));
                data.put("pubDate", (String)xpath.evaluate("pubDate",n,XPathConstants.STRING));
                data.put("location", (String)xpath.evaluate("georss:featurename",n,XPathConstants.STRING));
                data.put("geoLocation", (String)xpath.evaluate("georss:point",n,XPathConstants.STRING));
                
                if(handleOviData(data, tm)) {
                    setProgress(process++);
                }
            }
            log("Found "+process+" media elements");
        }
        catch(Exception e){
            log("Exception when handling request",e);
        }
        return true;
    }
    
    
    public boolean handleOviData(HashMap<String,String> data, TopicMap tm) {
        boolean isok = false;
        if(tm == null) return isok;
        String link = data.get("link");
        if(link != null) {
            try {
                Topic entryTopic = tm.createTopic();
                String imageURL = data.get("imageURL");
                String audioURL = data.get("audioURL");
                String videoURL = data.get("videoURL");
                if(imageURL != null && imageURL.length() > 0) {
                    entryTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(imageURL));
                    entryTopic.setSubjectLocator(new org.wandora.topicmap.Locator(imageURL));
                }
                else if(audioURL != null && audioURL.length() > 0) {
                    entryTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(audioURL));
                    entryTopic.setSubjectLocator(new org.wandora.topicmap.Locator(audioURL));
                }
                else if(videoURL != null && videoURL.length() > 0) {
                    entryTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(videoURL));
                    entryTopic.setSubjectLocator(new org.wandora.topicmap.Locator(videoURL));
                }
                else if(link != null && link.length() > 0) {
                    entryTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(link));
                    entryTopic.setSubjectLocator(new org.wandora.topicmap.Locator(link));
                }
                else {
                    entryTopic.addSubjectIdentifier(TopicTools.createDefaultLocator());
                }
                
                Topic entryTypeTopic = getEntryTypeTopic(tm);
                entryTopic.addType(entryTypeTopic);
                isok = true;
                
                // **** TITLE ****
                String title = data.get("title");
                if(title != null) {
                    title = title.trim();
                    if(title.length() > 0) {
                        String guid = data.get("guid");
                        if(guid == null || guid.length() == 0) guid = "ovi entry";
                        else {
                            if(guid.startsWith("http://share.ovi.com/media/")) {
                                guid = guid.substring("http://share.ovi.com/media/".length());
                            }
                        }
                        entryTopic.setBaseName(title+" ("+guid+")");
                        entryTopic.setDisplayName(defaultLanguage, title);
                    }
                }
                
                // **** DESCRIPTION ****
                String description = data.get("description");
                if(description != null) {
                    description = description.trim();
                    if(description.length() > 0) {
                        Topic descriptionTypeTopic = getDescriptionTypeTopic(tm);
                        entryTopic.setData(descriptionTypeTopic, TMBox.getLangTopic(entryTopic, defaultLanguage), description);
                    }
                }

                // **** LOCATION ****
                String location = data.get("location");
                String geoLocation = data.get("geoLocation");
                if(location != null && location.length() > 0) {
                    Topic locationTopic = getOrCreateTopic(tm, OVI_LOCATION_SI+"/"+location, location+" (ovi location)");
                    locationTopic.setDisplayName(defaultLanguage, location);

                    Topic locationType = getLocationTypeTopic(tm);
                    Association a = tm.createAssociation(locationType);
                    a.addPlayer(entryTopic, entryTypeTopic);
                    a.addPlayer(locationTopic, locationType);

                    locationTopic.addType(locationType);
                    
                    if(geoLocation != null && geoLocation.length() > 0) {
                        String[] geoCoords = geoLocation.split(" ");
                        if(geoCoords.length==2) {
                            if(geoCoords[0].length() > 0 && !"0".equalsIgnoreCase(geoCoords[0]) && geoCoords[1].length() > 0 && !"0".equalsIgnoreCase(geoCoords[1]))
                                AbstractGeoNamesExtractor.makeLatLong(geoCoords[0], geoCoords[1], locationTopic, tm);
                        }
                        else {
                            // Illegal number of GPS coordinates (should have 2)
                        }
                    }
                }
                
                // **** AUTHOR ****
                String author = data.get("author");
                if(author != null) {
                    author = author.trim();
                    if(author.length() > 0) {
                        Topic authorTopic = getOrCreateTopic(tm, OVI_AUTHOR_SI+"/"+author, author+" (ovi author)");
                        authorTopic.setDisplayName(defaultLanguage, author);

                        Topic authorType = getAuthorTypeTopic(tm);
                        Association a = tm.createAssociation(authorType);
                        a.addPlayer(entryTopic, entryTypeTopic);
                        a.addPlayer(authorTopic, authorType);

                        authorTopic.addType(authorType);
                    }
                }
                
                // **** TAGS AKA KEYWORDS ****
                String keywords = data.get("keywords");
                if(keywords != null && keywords.length() > 0) {
                    String[] allKeywords = null;
                    if(keywords.indexOf(",") > -1) {
                        allKeywords = keywords.split(",");
                    }
                    else {
                        allKeywords = new String[] { keywords };
                    }
                    String tag = null;
                    for(int i=0; i<allKeywords.length; i++) {
                        tag = allKeywords[i];
                        if(tag != null) {
                            tag = tag.trim();
                            if(tag.length() > 0) {
                                Topic tagTopic = getOrCreateTopic(tm, OVI_TAG_SI+"/"+tag, tag+" (ovi tag)");
                                tagTopic.setDisplayName(defaultLanguage, tag);

                                Topic tagType = getTagTypeTopic(tm);
                                Association a = tm.createAssociation(tagType);
                                a.addPlayer(entryTopic, entryTypeTopic);
                                a.addPlayer(tagTopic, tagType);

                                tagTopic.addType(tagType);
                            }
                        }
                    }
                }
                
                // **** PUBLICATION DATE ****
                String pubDate = data.get("pubDate");
                if(pubDate != null) {
                    pubDate = pubDate.trim();
                    if(pubDate.length() > 0) {
                        long dateMillis = 0;
                        try {
                            String dateStr = pubDate;
                            //if(dateStr.endsWith(" GMT")) dateStr = dateStr.substring(0,dateStr.length()-4);
                            DateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
                            Date date = (Date)formatter.parse(dateStr);
                            dateMillis = date.getTime();
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                            // Unable to parse date string. Using string itself as the SI.
                        }
                        Topic dateTopic = getOrCreateTopic(tm, OVI_DATE_SI+"/"+ (dateMillis == 0 ? pubDate : dateMillis), pubDate+" (ovi date)");
                        dateTopic.setDisplayName(defaultLanguage, pubDate);

                        Topic dateType = getDateTypeTopic(tm);
                        Association a = tm.createAssociation(dateType);
                        a.addPlayer(entryTopic, entryTypeTopic);
                        a.addPlayer(dateTopic, dateType);

                        dateTopic.addType(dateType);
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        return isok;
    }

    
    public Topic getTagTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, OVI_TAG_SI, "Ovi tag");
        Topic oClass = getOviClass(tm);
        makeSubclassOf(tm, type, oClass);
        return type;
    }
    

    public Topic getLocationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, OVI_LOCATION_SI, "Ovi location");
        Topic oClass = getOviClass(tm);
        makeSubclassOf(tm, type, oClass);
        return type;
    }

    
    public Topic getDateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, OVI_DATE_SI, "Ovi date");
        Topic oClass = getOviClass(tm);
        makeSubclassOf(tm, type, oClass);
        return type;
    }

    
    public Topic getAuthorTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, OVI_AUTHOR_SI, "Ovi author");
        Topic oClass = getOviClass(tm);
        makeSubclassOf(tm, type, oClass);
        return type;
    }
    
    
    public Topic getEntryTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, OVI_ENTRY_SI, "Ovi entry");
        Topic oClass = getOviClass(tm);
        makeSubclassOf(tm, type, oClass);
        return type;
    }
    
    
    public Topic getDescriptionTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, OVI_DESCRIPTION_SI, "Ovi description");
        return type;
    }

    public Topic getOviClass(TopicMap tm) throws TopicMapException {
        Topic o = getOrCreateTopic(tm, OVI_SI, "Ovi");
        o.addType(getWandoraClass(tm));
        return o;
    }


    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }

    

    // --------
    
    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    
    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }
    
    
    
    // -------------------------------------------------------------------------
    
}
