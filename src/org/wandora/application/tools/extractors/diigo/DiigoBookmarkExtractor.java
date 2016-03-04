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
 * DiigoAPIExtractor.java
 * 
 */



package org.wandora.application.tools.extractors.diigo;

import java.net.*;
import java.io.*;
import java.util.*;
import org.wandora.utils.*;
import org.wandora.application.gui.*;
import java.awt.*;
import javax.swing.*;
import java.text.*;

import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.tools.extractors.geonames.*;
import org.wandora.topicmap.*;

import org.json.*;



/**
 *
 * @author akivela
 */
public class DiigoBookmarkExtractor extends AbstractExtractor {
    private String defaultEncoding = "UTF-8";
    public static String defaultLanguage = "en";
    
    
    
    public static final String DIIGO_SI = "http://www.diigo.com";
    public static final String DIIGO_LOCATION_SI = DIIGO_SI+"/location";
    public static final String DIIGO_USER_SI = DIIGO_SI+"/user";
    public static final String DIIGO_TAG_SI = DIIGO_SI+"/tag";
    public static final String DIIGO_BOOKMARK_SI = DIIGO_SI+"/bookmark";
    public static final String DIIGO_DESCRIPTION_SI = DIIGO_SI+"/description";
    public static final String DIIGO_DATE_SI = DIIGO_SI+"/date";
    public static final String DIIGO_CREATION_DATE_SI = DIIGO_DATE_SI+"/creation";
    public static final String DIIGO_UPDATED_DATE_SI = DIIGO_DATE_SI+"/updated";
    public static final String DIIGO_COMMENT_SI = DIIGO_SI+"/comment";
    public static final String DIIGO_ANNOTATIONS_SI = DIIGO_SI+"/annotation";
    public static final String DIIGO_SHARED_SI = DIIGO_SI+"/shared";
    
    
    
    public DiigoBookmarkExtractor() {
        
    }
    

    @Override
    public String getName() {
        return "Diigo bookmark extractor...";
    }
    
    @Override
    public String getDescription(){
        return "Read Diigo bookmark feed and convert it to a topic map.";
    }
    

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_diigo.png");
    }
    
    private final String[] contentTypes=new String[] { "text/html", "text/javascript", "text/plain", "application/json" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }

    @Override
    public void execute(Wandora wandora, Context context) {
        resetAuthorization();
        super.execute(wandora, context);
    }
    
    
    
    private String password = null;
    private String user = null;
    
    
    public void resetAuthorization() {
        user = null;
        password = null;
    }
    public void setAuthorization(String u, String pass) {
        user = u;
        password = pass;
    }
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(password == null || user == null) {
            if(getWandora() != null) {
                PasswordPrompt pp = new PasswordPrompt(getWandora(), true);
                pp.setTitle(url.toExternalForm());
                getWandora().centerWindow(pp);
                pp.setVisible(true);
                if(!pp.wasCancelled()) {
                    user = pp.getUsername();
                    password = new String(pp.getPassword());
                }
                else {
                    user = null;
                    password = null;
                }
            }
        }
        if(password != null && user != null) {
            String userPassword = user+":"+password;
            String encodedUserPassword = org.wandora.utils.Base64.encodeBytes(userPassword.getBytes());
            URLConnection uc = url.openConnection();
            uc.setUseCaches(false);
            uc.setRequestProperty ("Authorization", "Basic " + encodedUserPassword);
            int res = ((HttpURLConnection) uc).getResponseCode();
            String s = IObox.loadFile(uc.getInputStream(), "UTF-8");
            boolean r = _extractTopicsFrom(s, topicMap);
            hlog("Waiting before next step...");
            try {
                Thread.currentThread().sleep(8000);
            }
            catch(Exception e) {}
            return r;
        }
        return false;
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
            JSONArray inJSON = new JSONArray(in);
            int numberOfBookmarks = handleDiigoData(inJSON, tm);
            
            log("Found "+numberOfBookmarks+" bookmarks");
        }
        catch(Exception e){
            log("Exception when handling request",e);
        }
        return true;
    }
    
    
    public int handleDiigoData(JSONArray data, TopicMap tm) {
        int numberOfBookmarks = 0;
        if(tm == null) return 0;
        JSONObject obj = null;
        Iterator<String> keys = null;
        String key = null;
        Object valo = null;
        int len = data.length();
        Locator tempSI = null;
        Locator finalSI = null;
        String title = null;
        setProgressMax(len);
        
        for(int index=0; index<len; index++) {
            try {
                Topic bookmarkTopic = tm.createTopic();
                finalSI = null;
                tempSI = TopicTools.createDefaultLocator();
                bookmarkTopic.addSubjectIdentifier(tempSI);
                
                Topic bookmarkTypeTopic = getBookmarkTypeTopic(tm);
                bookmarkTopic.addType(bookmarkTypeTopic);
                title = null;
                
                setProgress(numberOfBookmarks++);
                
                obj = data.optJSONObject(index);
                keys = obj.keys();
                while(keys.hasNext()) {
                    key = keys.next();
                    valo = obj.get(key);
                    if(valo == null) continue;
                    
                    //System.out.println("key:'"+key+"'=='"+valo+"'");
                    
                    if("title".equalsIgnoreCase(key)) {
                        title = valo.toString();
                        bookmarkTopic.setDisplayName(defaultLanguage, title);
                        if(finalSI != null) {
                            bookmarkTopic.setBaseName(title + " ("+finalSI.hashCode()+")");
                            bookmarkTopic = tm.getTopic(finalSI); // REFRESH IF MERGED
                        }
                    }
                    else if("url".equalsIgnoreCase(key)) {
                        Locator loc = new Locator(valo.toString());
                        bookmarkTopic.addSubjectIdentifier(loc);
                        bookmarkTopic.setSubjectLocator(loc);
                        bookmarkTopic = tm.getTopic(loc); // REFRESH IF MERGED
                        bookmarkTopic.removeSubjectIdentifier(tempSI);
                        if(title != null) {
                            bookmarkTopic.setBaseName(title + " ("+loc.hashCode()+")");
                            bookmarkTopic = tm.getTopic(loc); // REFRESH IF MERGED
                        }
                        finalSI = loc;
                    }
                    else if("user".equalsIgnoreCase(key)) {
                        String user = valo.toString();
                        if(user != null) {
                            user = user.trim();
                            if(user.length() > 0) {
                                Topic userTopic = getOrCreateTopic(tm, DIIGO_USER_SI+"/"+user, user+" (diigo user)");
                                userTopic.setDisplayName(defaultLanguage, user);

                                Topic userType = getUserTypeTopic(tm);
                                Association a = tm.createAssociation(userType);
                                a.addPlayer(bookmarkTopic, bookmarkTypeTopic);
                                a.addPlayer(userTopic, userType);

                                userTopic.addType(userType);
                            }
                        }
                    }
                    else if("desc".equalsIgnoreCase(key)) {
                        String description = valo.toString();
                        if(description != null && description.length() > 0 && !"null".equalsIgnoreCase(description)) {
                            description = description.trim();
                            if(description.length() > 0) {
                                Topic descriptionTypeTopic = getDescriptionTypeTopic(tm);
                                bookmarkTopic.setData(descriptionTypeTopic, TMBox.getLangTopic(bookmarkTopic, defaultLanguage), description);
                            }
                        }
                    }
                    else if("tags".equalsIgnoreCase(key)) {
                        String keywords = (String) valo;
                        if(keywords.length() > 0) {
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
                                        Topic tagTopic = getOrCreateTopic(tm, DIIGO_TAG_SI+"/"+tag, tag+" (diigo tag)");
                                        tagTopic.setDisplayName(defaultLanguage, tag);

                                        Topic tagType = getTagTypeTopic(tm);
                                        Association a = tm.createAssociation(tagType);
                                        a.addPlayer(bookmarkTopic, bookmarkTypeTopic);
                                        a.addPlayer(tagTopic, tagType);

                                        tagTopic.addType(tagType);
                                    }
                                }
                            }
                        }
                    }
                    else if("shared".equalsIgnoreCase(key)) {
                        String isShared = valo.toString().trim();
                        if(isShared.length() > 0) {
                            Topic isSharedTopic = getOrCreateTopic(tm, DIIGO_SHARED_SI+"/"+isShared, isShared);
                            Topic sharedType = getSharedTypeTopic(tm);
                            Association a = tm.createAssociation(sharedType);
                            a.addPlayer(bookmarkTopic, bookmarkTypeTopic);
                            a.addPlayer(isSharedTopic, sharedType);
                        }
                    }
                    else if("created_at".equalsIgnoreCase(key)) {
                        String date = valo.toString();
                        Topic creationDateType = getCreationDateTypeTopic(tm);
                        createDateAssociation(date, creationDateType, bookmarkTopic, bookmarkTypeTopic, tm);
                    }
                    else if("updated_at".equalsIgnoreCase(key)) {
                        String date = valo.toString();
                        Topic modificationDateType = getUpdatedDateTypeTopic(tm);
                        createDateAssociation(date, modificationDateType, bookmarkTopic, bookmarkTypeTopic, tm);
                    }
                    else if("comments".equalsIgnoreCase(key)) {
                        // TODO
                    }
                    else if("annotations".equalsIgnoreCase(key)) {
                        // TODO
                    }
                }
                
            }
            catch(Exception e) {
                log(e);
            }
        }
        return numberOfBookmarks;
    }
    
    /*
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
                    Topic locationTopic = tm.createTopic();
                    locationTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(DIIGO_LOCATION_SI+"/"+location));
                    locationTopic.setBaseName(location+" (ovi location)");
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
                        Topic authorTopic = tm.createTopic();
                        authorTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(DIIGO_AUTHOR_SI+"/"+author));
                        authorTopic.setBaseName(author+" (ovi author)");
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
                                Topic tagTopic = tm.createTopic();
                                tagTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(DIIGO_TAG_SI+"/"+tag));
                                tagTopic.setBaseName(tag+" (ovi tag)");
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
                        Topic dateTopic = tm.createTopic();
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
                        dateTopic.addSubjectIdentifier(new org.wandora.topicmap.Locator(DIIGO_DATE_SI+"/"+ (dateMillis == 0 ? pubDate : dateMillis) ));
                        dateTopic.setBaseName(pubDate+" (ovi date)");
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
     * */

    
    
    public void createDateAssociation(String d, Topic dateType, Topic bookmarkTopic, Topic bookmarkType, TopicMap tm) throws Exception {
        if(d != null) {
            d = d.trim();
            if(d.length() > 0) {
                long dateMillis = 0;
                try {
                    String dateStr = d;
                    //if(dateStr.endsWith(" GMT")) dateStr = dateStr.substring(0,dateStr.length()-4);
                    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z", Locale.US);
                    Date date = (Date)formatter.parse(dateStr);
                    dateMillis = date.getTime();
                }
                catch(Exception e) {
                    e.printStackTrace();
                    // Unable to parse date string. Using string itself as the SI.
                }
                Topic dateTopic = getOrCreateTopic(tm, DIIGO_DATE_SI+"/"+ (dateMillis == 0 ? d : dateMillis), d+" (diigo date)" );
                dateTopic.setDisplayName(defaultLanguage, d);

                Topic dateRole = getDateTypeTopic(tm);
                Association a = tm.createAssociation(dateType);
                a.addPlayer(bookmarkTopic, bookmarkType);
                a.addPlayer(dateTopic, dateRole);

                dateTopic.addType(dateRole);
            }
        }
    }
    
    
    
    public Topic getTagTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIIGO_TAG_SI, "Diigo tag");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }
    

    protected Topic getLocationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIIGO_LOCATION_SI, "Diigo location");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }

    
    protected Topic getUpdatedDateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIIGO_UPDATED_DATE_SI, "Diigo modification date");
        Topic superClass = getDateTypeTopic(tm);
        makeSubclassOf(tm, type, superClass);
        return type;
    }
    
    protected Topic getCreationDateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIIGO_CREATION_DATE_SI, "Diigo creation date");
        Topic superClass = getDateTypeTopic(tm);
        makeSubclassOf(tm, type, superClass);
        return type;
    }
    
    protected Topic getDateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIIGO_DATE_SI, "Diigo date");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }

    
    protected Topic getUserTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIIGO_USER_SI, "Diigo user");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }
    
    
    protected Topic getBookmarkTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIIGO_BOOKMARK_SI, "Diigo bookmark");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }
    
    
    protected Topic getDescriptionTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIIGO_DESCRIPTION_SI, "Diigo description");
        return type;
    }
    
    protected Topic getSharedTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DIIGO_SHARED_SI, "Diigo shared");
        return type;
    }
    
    
    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }
    

    // --------
    
    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si,String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    
    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    

    
}
