/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * AbstractTwitterExtractor.java
 */


package org.wandora.application.tools.extractors.twitter;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JDialog;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolType;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author akivela
 */





public class AbstractTwitterExtractor extends AbstractExtractor {
    public static final int SLEEP_TIME_BETWEEN_SEARCHES = 3000;
    
    public static String DEFAULT_LANG = "en";
    
    public static final String TWEET_SI_BODY = "http://twitter.com/";
    public static final String TWITTER_USER_SI_BODY = "http://twitter.com/";
    public static final String HASHTAG_SI_BODY = "http://twitter.com/search?q=";
    
    public static final String TWEET_TYPE_SI = "http://wandora.org/si/twitter/tweet";
    public static final String TWITTER_USER_TYPE_SI = "http://wandora.org/si/twitter/user";
    public static final String TWITTER_FROM_USER_TYPE_SI = "http://wandora.org/si/twitter/from-user";
    public static final String TWITTER_TO_USER_TYPE_SI = "http://wandora.org/si/twitter/to-user";
    public static final String TWITTER_DATE_TYPE = "http://wandora.org/si/twitter/date";
    public static final String TWITTER_LANG_TYPE = "http://wandora.org/si/twitter/lang";
    public static final String TWITTER_GEO_LOCATION_TYPE = "http://wandora.org/si/twitter/geo-location";
    public static final String TWITTER_HASH_TAG_TYPE = "http://wandora.org/si/twitter/hashtag";
    public static final String TWITTER_MEDIA_ENTITY_TYPE = "http://wandora.org/si/twitter/media-entity";
    public static final String TWITTER_URL_ENTITY_TYPE = "http://wandora.org/si/twitter/url-entity";
    
    public static final String TWITTER_SEARCH_QUERY_TYPE = "http://wandora.org/si/twitter-search/query";
    public static final String TWITTER_SEARCH_DATE_TYPE = "http://wandora.org/si/twitter-search/date";
    
    public static final String TWITTER_TYPE_SI = "http://twitter.com";
    public static final String DATE_SI_BODY = "http://wandora.org/date/";

    public static final String TWITTER_SEARCH_BODY = "http://wandora.org/si/twitter-search";
    
    private Twitter twitter = null;
    private RequestToken requestToken = null;
    private String pin = null;
    


    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_twitter.png");
    }
    
    @Override
    public boolean isConfigurable(){
        return false;
    }

    
    
    
    // -------------------------------------------------------------------------
    
    
    public void resetTwitter() {
        twitter = null;
        pin = null;
    }
    


    
    public Twitter initializeTwitter() {
        if(pin == null || twitter == null) {
            twitter = (new TwitterFactory()).getInstance();
            try {
                twitter.setOAuthConsumer("Br1jn80Zi3uUoVhDRN4xCQ", "fAArzdx1Jtl1lYauG4Pc1nlpJeq76wyKq9q6tSUwU");
                requestToken = twitter.getOAuthRequestToken();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return twitter;
    }
    
    
    
   
    
    // -------------------------------------------------------------------------
    

    
    
    public void searchTwitter(Query[] queries, int pages, TopicMap tm) {
        if(tm == null || queries == null || queries.length == 0) return;

        initializeTwitter();

        if(pin == null || pin.length() == 0) {
            try {
                AccessToken accessToken = null;
                String authorizationURL = requestToken.getAuthorizationURL();
                
                TwitterAuthorizationDialog authorizer = new TwitterAuthorizationDialog();
                authorizer.open(authorizationURL);

                if(authorizer.wasAccepted()) {
                    pin = authorizer.getPin();
                }
                else {
                    return;
                }
                
                if(pin != null && pin.length() > 0){
                    accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                }
                else {
                    accessToken = twitter.getOAuthAccessToken();
                }
            }
            catch (TwitterException ex) {
                log("Invalid Twitter authorization. Please, check the PIN code '"+pin+"' and try again.");
                //log(ex);
                pin = null;
                return;
            }
        }
        
        try {
            Topic twitterSearchTopic = tm.createTopic();
            if(twitterSearchTopic != null) {
                long stamp = System.currentTimeMillis();
                twitterSearchTopic.addSubjectIdentifier(new Locator(TWITTER_SEARCH_BODY + "/" + stamp));
                twitterSearchTopic.setBaseName("Twitter search "+stamp);
                twitterSearchTopic.addType(getTwitterSearchType(tm));

                Topic twitterSearchDateType = getTwitterSearchDateType(tm);
                if(twitterSearchDateType != null) {
                    Date d = new Date(stamp);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dStr = sdf.format(d);
                    twitterSearchTopic.setData(twitterSearchDateType, TMBox.getLangTopic(twitterSearchTopic, DEFAULT_LANG), dStr);
                }
                
                for(Query currentQuery : queries) {
                    if(currentQuery == null) continue;
                    int currentPage = 0;
                    log("Processing Twitter query '"+currentQuery.getQuery()+"'");
                    while(currentQuery != null) {
                        currentPage++;
                        hlog("Requesting search result page "+currentPage+(pages == 0 ? "" : " of "+pages));
                        
                        QueryResult result = twitter.search(currentQuery);

                        Topic twitterSearchQueryType = getTwitterSearchQueryType(tm);
                        if(twitterSearchQueryType != null) {
                            twitterSearchTopic.setData(twitterSearchQueryType, TMBox.getLangTopic(twitterSearchTopic, DEFAULT_LANG), currentQuery.getQuery());
                        }

                        ArrayList tweets = (ArrayList) result.getTweets();
                        for(Object tweet : tweets) {
                            if(tweet != null && tweet instanceof Status) {
                                Status t = (Status) tweet;
                                Topic topic = reifyTweet(t, tm);
                                topic.addType(twitterSearchTopic);
                            }
                        }

                        try {
                            Thread.currentThread().sleep(SLEEP_TIME_BETWEEN_SEARCHES);
                        }
                        catch(Exception e) {
                            // ...WAKE UP...
                        }
                        
                        if(pages == 0 || currentPage < pages) {
                            currentQuery = result.nextQuery();
                        }
                        else {
                            currentQuery = null;
                        }
                    }
                    log("Number of processed search result pages is "+currentPage);
                }
            }
        }
        catch(TopicMapException tme) {
            log("A topic map exception "+tme.getMessage()+" occurred while searching Twitter messages.");
            tme.printStackTrace();
        }
        catch(TwitterException te) {
            log("A Twitter exception "+te.getMessage()+" occurred while searching messages.");
            te.printStackTrace();
        };
    }

  
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        Query q = new Query(str);
        searchTwitter(new Query[] { q }, 1, tm);
        return true;
    }
    
    

    // -------------------------------------------------------------------------
    
    
    
    public Topic reifyTweet(Status t, TopicMap tm) {
        Topic tweetTopic = null;
        try {
            long tId = t.getId();
            String msg = t.getText();
            User user = t.getUser();
            
            if(user == null) {
                tweetTopic = reifyTweet(tId, null, msg, tm);
            }
            
            else {
                String userScreenName = user.getScreenName();

                tweetTopic = reifyTweet(tId, userScreenName, msg, tm);
    
                Topic userTopic = reifyTwitterUser(user, tm);

                if(tweetTopic != null && userTopic != null) {
                    Association a = tm.createAssociation(getTwitterFromUserType(tm));
                    a.addPlayer(tweetTopic, getTweetType(tm));
                    a.addPlayer(userTopic, getTwitterUserType(tm));
                }
            }
            
            /*
            String toUser = t.getToUser();
            if(toUser != null) {
                long toUid = t.getToUserId();       
                Topic toUserTopic = reifyTwitterUser(toUser, toUid, tm);
                if(tweetTopic != null && toUserTopic != null) {
                    Association a = tm.createAssociation(getTwitterToUserType(tm));
                    a.addPlayer(tweetTopic, getTweetType(tm));
                    a.addPlayer(toUserTopic, getTwitterUserType(tm));
                }
            }
            */

            Date d = t.getCreatedAt();
            if(tweetTopic != null && d != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateStr = df.format(d);
                tweetTopic.setData(getTweetDateType(tm), TMBox.getLangTopic(tweetTopic, DEFAULT_LANG), dateStr);
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                dateStr = sdf.format(d);
                Topic dateTopic = ExtractHelper.getOrCreateTopic(DATE_SI_BODY+dateStr, dateStr, getTweetDateType(tm), tm);
                if(dateTopic != null) {
                    Association a = tm.createAssociation(getTweetDateType(tm));
                    a.addPlayer(tweetTopic, getTweetType(tm));
                    a.addPlayer(dateTopic, getTweetDateType(tm));
                }
            }
            
            /*
            String l = t.getIsoLanguageCode();
            if(l != null) {
                Topic tweetLangTopic = TMBox.getLangTopic(tweetTopic, l);
                if(tweetLangTopic != null) {
                    Association a = tm.createAssociation(getTweetLangType(tm));
                    a.addPlayer(tweetTopic, getTweetType(tm));
                    a.addPlayer(tweetLangTopic, getTweetLangType(tm));
                }
            }
            */
            
            GeoLocation geo = t.getGeoLocation();
            if(geo != null) {
                double lat = geo.getLatitude();
                double lon = geo.getLongitude();
                String geoLocStr = lat+","+lon;
                tweetTopic.setData(getTweetGeoLocationType(tm), TMBox.getLangTopic(tweetTopic, DEFAULT_LANG), geoLocStr);
            }
            
            HashtagEntity[] entities = t.getHashtagEntities();
            if(entities != null && entities.length > 0) {
                for(int i=0; i<entities.length; i++) {
                    Topic entityTopic = reifyHashtagEntity(entities[i], tm);
                    if(entityTopic != null) {
                        Association a = tm.createAssociation(getHashtagType(tm));
                        a.addPlayer(tweetTopic, getTweetType(tm));
                        a.addPlayer(entityTopic, getHashtagType(tm));
                    }
                }
            }
            
            MediaEntity[] mediaEntities = t.getMediaEntities();
            if(mediaEntities != null && mediaEntities.length > 0) {
                for(int i=0; i<mediaEntities.length; i++) {
                    Topic entityTopic = reifyMediaEntity(mediaEntities[i], tm);
                    if(entityTopic != null) {
                        Association a = tm.createAssociation(getMediaEntityType(tm));
                        a.addPlayer(tweetTopic, getTweetType(tm));
                        a.addPlayer(entityTopic, getMediaEntityType(tm));
                    }
                }
            }
            
            URLEntity[] urlEntities = t.getURLEntities();
            if(urlEntities != null && urlEntities.length > 0) {
                for(int i=0; i<urlEntities.length; i++) {
                    Topic entityTopic = reifyUrlEntity(urlEntities[i], tm);
                    if(entityTopic != null) {
                        Association a = tm.createAssociation(getURLEntityType(tm));
                        a.addPlayer(tweetTopic, getTweetType(tm));
                        a.addPlayer(entityTopic, getURLEntityType(tm));
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return tweetTopic;
    }
    
    
    public Topic reifyTweet(long tid, String from, String msg, TopicMap tm) {
        String si = TWEET_SI_BODY+from+"/status/"+tid;
        Topic tweetTopic = null;
        try {
            tweetTopic = tm.getTopic(si);
            if(tweetTopic == null) {
                tweetTopic = tm.createTopic();
                tweetTopic.addSubjectIdentifier(new Locator(si));
                tweetTopic.setBaseName(msg + " ("+tid+")");
                tweetTopic.setDisplayName(DEFAULT_LANG, msg);
                tweetTopic.addType(getTweetType(tm));
            }
        }
        catch(Exception e) {
            log(e);
        }
        return tweetTopic;
    }
    
    
    public Topic reifyTwitterUser(User user, TopicMap tm) {
        String userName = user.getName();
        String screenName = user.getScreenName();
        String userURL = user.getURL();
        long uid = user.getId(); 
                
        String si = TWITTER_USER_SI_BODY + urlEncode(screenName);

        Topic twitterUserTopic = null;
        try {
            twitterUserTopic = tm.getTopic(si);
            if(twitterUserTopic == null) {
                twitterUserTopic = tm.createTopic();
                twitterUserTopic.addSubjectIdentifier(new Locator(si));
                twitterUserTopic.setBaseName(screenName + " ("+uid+")");
                twitterUserTopic.setDisplayName(DEFAULT_LANG, userName);
                twitterUserTopic.addType(getTwitterUserType(tm));
            }
        }
        catch(Exception e) {
            log(e);
        }
        return twitterUserTopic;
    }
    
    
    
    public Topic reifyHashtagEntity(HashtagEntity e, TopicMap tm) {
        Topic entityTopic = null;
        try {
            String str = e.getText();
            if(str != null && str.length() > 0) {
                String si = HASHTAG_SI_BODY + urlEncode(str);
                entityTopic = tm.getTopic(si);
                if(entityTopic == null) {
                    entityTopic = tm.createTopic();
                    entityTopic.addSubjectIdentifier(new Locator(si));
                    entityTopic.setBaseName(str);
                    entityTopic.setDisplayName(DEFAULT_LANG, str);
                    entityTopic.addType(getHashtagType(tm));
                }
            }
        }
        catch(Exception ec) {
            log(ec);
        }
        return entityTopic;
    }
    
    
    
    public Topic reifyMediaEntity(MediaEntity e, TopicMap tm) {
        Topic entityTopic = null;
        try {
            String url = e.getDisplayURL();
            if(url != null && url.length() > 0) {
                if(!url.startsWith("http://")) {
                    url = "http://"+url;
                }
                entityTopic = tm.getTopic(url);
                if(entityTopic == null) {
                    entityTopic = tm.createTopic();
                    entityTopic.addSubjectIdentifier(new Locator(url));
                    entityTopic.addType(getMediaEntityType(tm));
                }
            }
        }
        catch(Exception ec) {
            log(ec);
        }
        return entityTopic;
    }
    
    
    public Topic reifyUrlEntity(URLEntity e, TopicMap tm) {
        Topic entityTopic = null;
        try {
            String url = e.getDisplayURL();
            if(url != null && url.length() > 0) {
                if(!url.startsWith("http://")) {
                    url = "http://"+url;
                }
                entityTopic = tm.getTopic(url);
                if(entityTopic == null) {
                    entityTopic = tm.createTopic();
                    entityTopic.addSubjectIdentifier(new Locator(url));
                    entityTopic.addType(getURLEntityType(tm));
                }
            }
        }
        catch(Exception ec) {
            log(ec);
        }
        return entityTopic;
    }
    
    
    // --------------------------------------------------------------- TYPES ---

    
    
    public Topic getTweetType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWEET_TYPE_SI, "Tweet", getTwitterType(tm), tm);
        return type;
    }
    
    public Topic getTwitterUserType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_USER_TYPE_SI, "Twitter user", getTwitterType(tm), tm);
        return type;
    }
    
    public Topic getTwitterFromUserType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_FROM_USER_TYPE_SI, "Twitter from user", getTwitterType(tm), tm);
        return type;
    }
    
    public Topic getTwitterToUserType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_TO_USER_TYPE_SI, "Twitter to user", getTwitterType(tm), tm);
        return type;
    }
    
    public Topic getTwitterType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_TYPE_SI, "Twitter", getWandoraType(tm), tm);
        return type;
    }
    
    public Topic getWandoraType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class", tm);
        return type;
    }
    
    public Topic getTweetDateType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_DATE_TYPE, "Tweet date", getTwitterType(tm), tm);
        return type;
    }
    
    public Topic getTweetLangType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_LANG_TYPE, "Tweet lang", getTwitterType(tm), tm);
        return type;
    }
    
    public Topic getTweetGeoLocationType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_GEO_LOCATION_TYPE, "Tweet geo location", tm);
        return type;
    }
    
    public Topic getHashtagType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_HASH_TAG_TYPE, "Twitter hashtag", getTwitterType(tm), tm);
        return type;
    }
    
    
    public Topic getMediaEntityType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_MEDIA_ENTITY_TYPE, "Twitter media entity", getTwitterType(tm), tm);
        return type;
    }
    
    public Topic getURLEntityType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_URL_ENTITY_TYPE, "Twitter URL entity", getTwitterType(tm), tm);
        return type;
    }
    
    
    public Topic getTwitterSearchType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_SEARCH_BODY, "Twitter search", getTwitterType(tm), tm);
        return type;
    }
    public Topic getTwitterSearchQueryType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_SEARCH_QUERY_TYPE, "Twitter search query", getTwitterType(tm), tm);
        return type;
    }
    public Topic getTwitterSearchDateType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(TWITTER_SEARCH_DATE_TYPE, "Twitter search date", getTwitterType(tm), tm);
        return type;
    }
}
