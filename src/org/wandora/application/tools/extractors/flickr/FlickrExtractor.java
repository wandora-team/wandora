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
 * FlickrExtractor.java
 *
 * Created on March 19, 2008, 12:34 AM
 */

package org.wandora.application.tools.extractors.flickr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolType;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.topicmap.TMBox;
import org.wandora.utils.IObox;

import javax.swing.Icon;
import org.wandora.application.gui.UIBox;
        
/**
 *
 * @author anttirt
 */
public abstract class FlickrExtractor extends AbstractWandoraTool {
    
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    
    
    @Override
    public String getName() {
        return "Flickr api extractor (" + getDescription() + ")";
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_flickr.png");
    }
    
    
    protected TopicMap currentMap;
    
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    
    @Override
    public void configure(Wandora admin, org.wandora.utils.Options options, String prefix) {
        if(staticState == null)
            staticState = new FlickrState();
        
        AuthConfigDialog dlg = new AuthConfigDialog(admin, true, getFlickrState());
        dlg.setVisible(true);
    }
    
    
    @Override
    public void execute(Wandora admin, Context context) {
        if(staticState == null)
            staticState = new FlickrState();
        
        currentMap = admin.getTopicMap();
        
        try {
            setDefaultLogger();
            extract(admin, context);
        }
        catch(ExtractionFailure e) {
            log(e);
        }
        
        setState(WAIT);
    }
    
    
    protected static class ExtractionFailure extends Exception {
        public ExtractionFailure(String message) { super(message); }
        public ExtractionFailure(Throwable cause) { super(cause); }
        public ExtractionFailure(String message, Throwable cause) { super(message, cause); }
    }
    
    
    protected static class RequestFailure extends Exception {
        public RequestFailure(String message) { super(message); }
        public RequestFailure(Throwable cause) { super(cause); }
        public RequestFailure(String message, Throwable cause) { super(message, cause); }
    }

    
    public static class UserCancellation extends Exception {
        public UserCancellation(String message) { super(message); }
        public UserCancellation(Throwable cause) { super(cause); }
        public UserCancellation(String message, Throwable cause) { super(message, cause); }
    }
    

    public static void main(String[] args) {
        try {
            JSONObject obj = new JSONObject(
                    "{\"foo\" : {" +
                        " \"bar\" : [ 1, 2, 3, 4 ]," +
                        "\"baz\" : \"hablabl\" }" +
                    "}");
            
            int foo = FlickrUtils.searchInt(obj, "foo.bar[2]");
            String baz = FlickrUtils.searchString(obj, "foo.baz");
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
   
    
    protected static String url(String str) {
        if(str == null)
            return str;
        try {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch(Exception e) {
            return str;
        }
    }

    public TopicMap getCurrentMap() {
        return currentMap;
    }
    
    
    protected static String createSignature(SortedMap<String, String> arguments) throws RequestFailure {
        StringBuilder builder = new StringBuilder(FlickrState.ApiSecret);
        for(SortedMap.Entry<String, String> e : arguments.entrySet()) {
            builder.append(e.getKey());
            builder.append(e.getValue());
        }
        //logger.log("Created signature: " + builder.toString());
        MessageDigest md = null;
        try { md = MessageDigest.getInstance("MD5"); } catch(NoSuchAlgorithmException e) { throw new RequestFailure("MD5 not available", e); }
        Charset ASCII = Charset.forName("ISO-8859-1");
        md.update(builder.toString().getBytes(ASCII));
        builder.setLength(0);
        byte[] hash = md.digest();
        for(int i = 0; i < hash.length; ++i) {
            builder.append(String.format("%02x", hash[i]));
        }
        String ret = builder.toString();
        return ret;
    }


    protected static String getFrob() throws RequestFailure {
        TreeMap<String, String> args = new TreeMap<String, String>();
        args.put("api_key", FlickrState.ApiKey);
        args.put("method", "flickr.auth.getFrob");
        args.put("format", "json");
        args.put("nojsoncallback", "1");
        args.put("api_sig", createSignature(args));

        try {
            JSONObject obj = new JSONObject(IObox.doUrl(new URL(FlickrState.makeRESTURL(args))));

            if(!obj.getString("stat").equals("ok"))
                throw new RequestFailure(String.valueOf(obj.getInt("code")) + ": " + obj.getString("message"));

            return FlickrUtils.searchString(obj, "frob._content");
        }
        catch(JSONException e) {
            throw new RequestFailure("Invalid json in frob response", e);
        }
        catch(MalformedURLException e) {
            throw new RequestFailure("Attempted to construct malformed URL", e);
        }
        catch(IOException e) {
            throw new RequestFailure("IOException while requesting frob", e);
        }
    }
    
    public Topic getLanguage(String id) throws TopicMapException {
        Topic lanT = currentMap.getTopic(XTMPSI.getLang(id));
        if(lanT == null) {
            lanT = currentMap.createTopic();
            lanT.addSubjectIdentifier(new Locator(XTMPSI.getLang(id)));
            lanT.setBaseName("");
        }
        return lanT;
    }
    
    
    public Topic getFlickrClass() throws TopicMapException {
        Locator loc = new Locator("http://www.flickr.com");
        Topic flickrClass = currentMap.getTopic(loc);
        if(flickrClass == null) {
            flickrClass = currentMap.createTopic();
            flickrClass.addSubjectIdentifier(new Locator("http://www.flickr.com"));
            flickrClass.setBaseName("Flickr");
            flickrClass.addType(getWandoraClass());
        }
        return flickrClass;
    }


    public Topic getWandoraClass() throws TopicMapException {
        Locator loc = new Locator(TMBox.WANDORACLASS_SI);
        Topic wandoraClass = currentMap.getTopic(loc);
        if(wandoraClass == null) {
            wandoraClass = currentMap.createTopic();
            wandoraClass.addSubjectIdentifier(
                new Locator(TMBox.WANDORACLASS_SI));
            wandoraClass.setBaseName("Wandora class");
        }
        return wandoraClass;
    }


    public Topic getLicenseTopic(int licenseID) throws TopicMapException {
        Topic licenseT = FlickrUtils.createTopic(currentMap, "license", getFlickrClass());
        Topic retT = null;
        switch(licenseID) {
            case 0:
                retT = FlickrUtils.createTopic(currentMap, "allRightsReserved", " (license)", "All rights reserved", licenseT);
                break;
            case 1:
                retT = FlickrUtils.createRaw(currentMap,
                        "http://creativecommons.org/licenses/by-nc-sa/2.0/", " (license)",
                        "Attribution-NonCommercial-ShareAlike License", licenseT);
                break;
            case 2:
                retT = FlickrUtils.createRaw(currentMap,
                        "http://creativecommons.org/licenses/by-nc/2.0/", " (license)",
                        "Attribution-NonCommercial License", licenseT);
                break;
            case 3:
                retT = FlickrUtils.createRaw(currentMap,
                        "http://creativecommons.org/licenses/by-nc-nd/2.0/", " (license)",
                        "Attribution-NonCommercial-NoDerivs License", licenseT);
                break;
            case 4:
                retT = FlickrUtils.createRaw(currentMap,
                        "http://creativecommons.org/licenses/by/2.0/", " (license)",
                        "Attribution License", licenseT);
                break;
            case 5:
                retT = FlickrUtils.createRaw(currentMap,
                        "http://creativecommons.org/licenses/by-sa/2.0/", " (license)",
                        "Attribution-ShareAlike License", licenseT);
                break;
            case 6:
                retT = FlickrUtils.createRaw(currentMap,
                        "http://creativecommons.org/licenses/by-nd/2.0/", " (license)",
                        "Attribution-NoDerivs License", licenseT);
                break;
            default:
        }
        
        return retT;
    }
    
    
    
    public Topic getTopic(FlickrTopic topicClass) throws TopicMapException {
        Topic flickrClass = getFlickrClass();
        
        switch(topicClass) {
            case Group:
                return FlickrUtils.createTopic(currentMap, "flickrGroup", flickrClass);
            case Profile:
                return FlickrUtils.createTopic(currentMap, "flickrProfile", flickrClass);
            case Photo:
                return FlickrUtils.createTopic(currentMap, "flickrPhoto", flickrClass);
            case Tag:
                return FlickrUtils.createTopic(currentMap, "flickrTag", flickrClass);
            default:
                return null;
        }
    }
    
    
    public Topic getOccurrence(FlickrOccur occurrenceClass) throws TopicMapException {
        Topic flickrClass = getFlickrClass();
        
        switch(occurrenceClass) {
            case Description:
                return FlickrUtils.createTopic(currentMap, "description", flickrClass);
            case MemberCount:
                return FlickrUtils.createTopic(currentMap, "memberCount", flickrClass);
            case NSID:
                return FlickrUtils.createTopic(currentMap, "NSID", flickrClass);
            case Location:
                return FlickrUtils.createTopic(currentMap, "location", flickrClass);
            case PhotoID:
                return FlickrUtils.createTopic(currentMap, "photoID", flickrClass);
            default:
                return null;
        }
    }
    
    
    
    public Topic getAssociation(FlickrAssoc assocClass) throws TopicMapException {
        Topic flickrClass = getFlickrClass();
        
        switch(assocClass) {
            case Membership:
                return FlickrUtils.createTopic(currentMap, "isMember", flickrClass);
            case Ownership:
                return FlickrUtils.createTopic(currentMap, "isOwner", flickrClass);
            case Description:
                return FlickrUtils.createTopic(currentMap, "describes", flickrClass);
            case Favorite:
                return FlickrUtils.createTopic(currentMap, "isFavorite", flickrClass);
            case License:
                return FlickrUtils.createTopic(currentMap, "governs", flickrClass);
            case InGroupPool:
                return FlickrUtils.createTopic(currentMap, "inGroupPool", flickrClass);
            default:
                return null;
        }
    }
    

    protected static void throwOnAPIError(JSONObject obj) throws RequestFailure {
        try {
            if(!obj.getString("stat").equals("ok"))
                throw new RequestFailure(String.valueOf(obj.getInt("code")) + ": " + obj.getString("message"));
        }
        catch(JSONException e) {
            throw new RequestFailure("Invalid JSON response structure", e);
        }
    }
    
    
    private static FlickrState staticState;
    protected FlickrState getFlickrState() {
        return staticState;
    }

    
    protected abstract boolean extract(Wandora admin, Context context) throws ExtractionFailure;
    
    
    public final Collection<Topic> getWithType( Context context, Topic type) {
        Iterator objs = context.getContextObjects();
        ArrayList<Topic> topicList = new ArrayList();
        if(type == null) return topicList;
            
        try {
            while(objs.hasNext()) {
                Topic t = (Topic)objs.next();
                if(t == null) continue;
                boolean isRightType = false;
                for(Topic typeT : t.getTypes()) {
                    if(typeT.mergesWithTopic(type)) {
                        isRightType = true;
                        break;
                    }
                }
                if(isRightType)
                    topicList.add(t);
            }
        }
        catch(TopicMapException e) {
            log(e);
        }
        return topicList;
    }
}
