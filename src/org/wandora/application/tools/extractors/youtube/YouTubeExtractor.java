
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
 * YouTubeExtractor.java
 */


package org.wandora.application.tools.extractors.youtube;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.gui.*;

import com.google.gdata.client.youtube.*;
import com.google.gdata.data.*;
import com.google.gdata.data.youtube.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;
import com.google.gdata.data.media.mediarss.*;
import com.google.gdata.data.geo.impl.*;

import java.io.IOException;
import java.io.File;
import java.net.*;
import java.util.*;
import javax.swing.*;


/**
 *
 * @author akivela
 */
public abstract class YouTubeExtractor extends AbstractWandoraTool implements WandoraTool {

    public static final String LANG = "en";
    public static final String VIDEO_DESCRIPTION = "http://wandora.org/si/youtube/video-description";
    public static final String VIDEO_ID = "http://wandora.org/si/youtube/video-id";
    public static final String VIDEO_TYPE = "http://wandora.org/si/youtube/video";
    public static final String VIDEO_FEED_TYPE = "http://wandora.org/si/youtube/video-feed";
    
    public static final String CONTENT_EXPRESSION_BASE = "http://wandora.org/si/youtube/content-expression/";
    public static final String CONTENT_TYPE_BASE = "http://wandora.org/si/youtube/content-type/";
    public static final String MEDIUM_BASE = "http://wandora.org/si/youtube/medium/";
    public static final String LOCATION_BASE = "http://wandora.org/si/youtube/location/";
    public static final String COPYRIGHT_BASE = "http://gdata.youtube.com/feeds/api/copyright/";
    public static final String RATING_BASE = "http://gdata.youtube.com/feeds/api/rating/";
    public static final String PERSON_BASE = "http://gdata.youtube.com/feeds/api/person/";
    public static final String CATEGORY_BASE = "http://gdata.youtube.com/feeds/api/category/";
    public static final String KEYWORD_BASE = "http://gdata.youtube.com/feeds/api/keywords/";
    public static final String VIDEO_BASE = "http://gdata.youtube.com/feeds/api/videos/";
    
    public static final String SCHEMA_BASE = "http://wandora.org/si/youtube/schema/";
    
    private static final String clientID = "ytapi-GripStudiosInter-Wandora-n724s4pt-0";
    private static final String developerKey = "AI39si7wwbGt7XOjn7ma6d0xqOuDwYunZxpWlVG7iO37ZjHbL0C3kTxESwEscM06p8zQGCbrfZ85gxhGlsbfM1VwnsOv6Mhr-Q";
    private String username = null;
    private String password = null;
    
    
    public static final String[] standardVideoFeeds = new String[] {
        "Most Viewed", "http://gdata.youtube.com/feeds/api/standardfeeds/most_viewed",
        "Top Rated", "http://gdata.youtube.com/feeds/api/standardfeeds/top_rated",
        "Recently Featured", "http://gdata.youtube.com/feeds/api/standardfeeds/recently_featured",
        "Watch On Mobile", "http://gdata.youtube.com/feeds/api/standardfeeds/watch_on_mobile",
        "Most Discussed", "http://gdata.youtube.com/feeds/api/standardfeeds/most_discussed",
        "Top Favorites", "http://gdata.youtube.com/feeds/api/standardfeeds/top_favorites",
        //"Most Linked", "http://gdata.youtube.com/feeds/api/standardfeeds/most_linked",
        "Most Responded", "http://gdata.youtube.com/feeds/api/standardfeeds/most_responded",
        "Most Recent", "http://gdata.youtube.com/feeds/api/standardfeeds/most_recent",
    };
    
    
    
    

    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_youtube.png");
    }
    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        YouTubeExtractorConfiguration dialog=new YouTubeExtractorConfiguration(admin,options,this);
        dialog.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
    }
    
    
    
    
    
    protected YouTubeService initializeService(Wandora admin) throws AuthenticationException, CancelledException  {
        YouTubeService service = new YouTubeService(clientID, developerKey);
        if(username == null || password == null) {
            PasswordPrompt passwordPrompt = new PasswordPrompt(admin, true);
            passwordPrompt.setTitle("YouTube authentication");
            passwordPrompt.setLabelText("<html>Please fill in YouTube username and password</html>");
            passwordPrompt.setVisible(true);

            if(passwordPrompt.wasCancelled()) throw new CancelledException();
            username = passwordPrompt.getUsername();
            password = new String( passwordPrompt.getPassword() );
        }
        service.setUserCredentials(username, password);
        return service;
    }
    
    
    
    
    
    public void forgetAuthorization() {
        password = null;
        username = null;
    }
    
    
    // -------------------------------------------------------------------------
    

    private static final String shrinkIdPrefix = "http://gdata.youtube.com/feeds/api/videos/";
    public String shrinkId(String id) {
        String shrinkedId = id;
        if(shrinkedId.startsWith(shrinkIdPrefix)) {
            shrinkedId = shrinkedId.substring(shrinkIdPrefix.length());
        }
        return shrinkedId;
    }
    
    
    
    public Topic getTopicForVideoFeed(VideoFeed videoFeed, URL feedUrl, TopicMap topicMap) throws TopicMapException {
        if(videoFeed == null || topicMap == null || feedUrl == null) return null;
        Locator videoFeedLocator = new Locator(feedUrl.toExternalForm());
        Topic videoFeedTopic = topicMap.getTopic(videoFeedLocator);
        String videoFeedId = videoFeed.getId();
        String videoFeedTitle = videoFeed.getTitle().getPlainText();
        if(videoFeedTopic == null) {
            videoFeedTopic = topicMap.createTopic();
            //videoFeedTopic.addSubjectIdentifier(videoFeedLocator);
            videoFeedTopic.setSubjectLocator(videoFeedLocator);
            videoFeedTopic.setBaseName(videoFeedTitle+" ("+videoFeedId+")");
            videoFeedTopic.setDisplayName(LANG, videoFeedTitle);
            videoFeedTopic.addType(getTopicForVideoFeedType(topicMap));
            
            setData(videoFeedTopic, getOrCreateSchemaTopic(topicMap,"video-feed-id"), LANG, videoFeedId);
        }
        return videoFeedTopic;
    }
    
    
    
    
    public Topic getTopicForVideo(VideoEntry entry, TopicMap topicMap) throws TopicMapException {
        if(entry == null || topicMap == null) return null;
        Locator videoLocator = new Locator(getVideoLocator(entry));
        String videoId = entry.getId();
        String videoTitle = entry.getTitle().getPlainText();
        YouTubeMediaGroup videoMediaGroup = entry.getMediaGroup();
        String videoDescription = videoMediaGroup.getDescription().getPlainTextContent();
        Topic videoTopic = topicMap.getTopic(videoLocator);
        if(videoTopic == null) {
            videoTopic = topicMap.createTopic();
            videoTopic.addSubjectIdentifier(videoLocator);
            videoTopic.setSubjectLocator(videoLocator);
            videoTopic.setBaseName(videoTitle+" ("+shrinkId(videoId)+")");
            videoTopic.setDisplayName(LANG, videoTitle);
            videoTopic.addType(getTopicForVideoType(topicMap));
            
            setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-description"), LANG, videoDescription);
            setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-id"), LANG, videoId);
            setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-duration"), LANG, ""+videoMediaGroup.getDuration());
            
            DateTime dp = entry.getPublished();
            if(dp != null) {
                setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-published"), LANG, dp.toStringRfc822());
            }
            DateTime du = entry.getUpdated();
            if(du != null) {
                setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-updated"), LANG, du.toStringRfc822());
            }
            DateTime de = entry.getEdited();
            if(de != null) {
                setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-edited"), LANG, de.toStringRfc822());
            }
            DateTime dr = entry.getRecorded();
            if(dr != null) {
                setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-recorded"), LANG, dr.toStringRfc822());
            }
            
            TextConstruct summary = entry.getSummary();
            if(summary != null && !summary.isEmpty()) {
                setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-summary"), LANG, summary.getPlainText());
            }
            
            MediaKeywords keywords = entry.getMediaGroup().getKeywords();
            java.util.List<java.lang.String> words = keywords.getKeywords();
            String word = null;
            Topic wordTopic = null;
            for(Iterator<String> i = words.iterator(); i.hasNext(); ) {
                word = i.next();
                wordTopic = getTopicForKeyword(word, topicMap);
                if(wordTopic != null) {
                    createAssociation("YouTube video-keyword", videoTopic, "YouTube video", wordTopic, "YouTube keyword", topicMap);
                }
            }
            
            MediaCategory videoCategory = videoMediaGroup.getYouTubeCategory();
            if(videoCategory != null) {
                Topic categoryTopic = getTopicForCategory(videoCategory, topicMap);
                if(categoryTopic != null) {
                    createAssociation("YouTube video-category", videoTopic, "YouTube video", categoryTopic, "YouTube category", topicMap);
                }
            }
            
            List<Person> authors = entry.getAuthors();
            if(authors != null && authors.size() > 0) {
                Person p = null;
                for(Iterator<Person> i=authors.iterator(); i.hasNext(); ) {
                    p = i.next();
                    Topic personTopic = getTopicForPerson(p, topicMap);
                    if(personTopic != null) {
                        createAssociation("YouTube video-author", videoTopic, "YouTube video", personTopic, "YouTube author", topicMap);
                    }
                }
            }
            
            List<Person> contributors = entry.getContributors();
            if(contributors != null && contributors.size() > 0) {
                Person p = null;
                for(Iterator<Person> i=contributors.iterator(); i.hasNext(); ) {
                    p = i.next();
                    Topic personTopic = getTopicForPerson(p, topicMap);
                    if(personTopic != null) {
                        createAssociation("YouTube video-contributor", videoTopic, "YouTube video", personTopic, "YouTube contributor", topicMap);
                    }
                }
            }
            
            Rating rating = entry.getRating();
            if(rating != null) {
                Topic ratingTopic = getTopicForRating(rating, topicMap);
                if(ratingTopic != null) {
                    createAssociation("YouTube video-rating", videoTopic, "YouTube video", ratingTopic, "YouTube rating", topicMap);
                }
            }
            
            List<com.google.gdata.data.media.mediarss.MediaContent> content = videoMediaGroup.getContents();
            if(content != null && content.size() > 0) {
                com.google.gdata.data.media.mediarss.MediaContent c = null;
                for(Iterator<com.google.gdata.data.media.mediarss.MediaContent> i=content.iterator(); i.hasNext(); ) {
                    c = i.next();
                    Topic contentTopic = getTopicForMediaContent(entry, c, topicMap);
                    if(contentTopic != null) {
                        createAssociation("YouTube video-content", videoTopic, "YouTube video", contentTopic, "YouTube content", topicMap);
                    }
                }
            }
            
            List<MediaThumbnail> thumbs = videoMediaGroup.getThumbnails();
            if(thumbs != null && thumbs.size() > 0) {
                MediaThumbnail thumb = null;
                for(Iterator<MediaThumbnail> i=thumbs.iterator(); i.hasNext(); ) {
                    thumb = i.next();
                    Topic thumbTopic = getTopicForThumbnail(entry, thumb, topicMap);
                    if(thumbTopic != null) {
                        createAssociation("YouTube video-thumbnail", videoTopic, "YouTube video", thumbTopic, "YouTube thumbnail", topicMap);
                    }
                }
            }
            
            MediaCopyright copyright = videoMediaGroup.getCopyright();
            if(copyright != null) {
                Topic copyrightTopic = getCopyrightTopic(copyright, topicMap);
                if(copyrightTopic != null) {
                    createAssociation("YouTube video-copyright", videoTopic, "YouTube video", copyrightTopic, "YouTube copyright", topicMap);
                }
            }
            
            String location = entry.getLocation();
            if(location != null && location.length() > 0) {
                Topic locationTopic = getLocationTopic(location, topicMap);
                if(locationTopic != null) {
                    createAssociation("YouTube video-location", videoTopic, "YouTube video", locationTopic, "YouTube location", topicMap);
                }
            }
            
            GeoRssWhere geo = entry.getGeoCoordinates();
            if(geo != null) {
                Double lat = geo.getLatitude();
                setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-location-latitude"), LANG, lat.toString());
                Double lon = geo.getLongitude();
                setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-location-longitude"), LANG, lon.toString());
                GmlLowerCorner lowerleft = geo.getLowerLeft();
                if(lowerleft != null) {
                    setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-location-lower-left"), LANG, lowerleft.getValue());
                }
                GmlUpperCorner upperright = geo.getUpperRight();
                if(upperright != null) {
                    setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-location-upper-right"), LANG, upperright.getValue());
                }
            }
            
            YtStatistics stats = entry.getStatistics();
            if(stats != null) {
                setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-view-count"), LANG, ""+stats.getViewCount());
                setData(videoTopic, getOrCreateSchemaTopic(topicMap,"video-favourite-count"), LANG, ""+stats.getFavoriteCount());
            }

        }
        return videoTopic;
    }
    
    
    
    
    // ******** TOPICS *********
    
    
    public Topic getLocationTopic(String location, TopicMap tm) throws TopicMapException {
        if(location == null || tm == null) return null;
        Locator locationLocator = new Locator(getLocationLocator(location));
        Topic locationTopic = tm.getTopic(locationLocator);
        if(locationTopic == null) {
            locationTopic = tm.createTopic();
            locationTopic.addSubjectIdentifier(locationLocator);
            locationTopic.setBaseName(location+" (YouTube location)");
            locationTopic.setDisplayName(LANG, ""+location);
            
            locationTopic.addType(getTopicForLocationType(tm));
        }
        return locationTopic;
    }
    
    
    
    public Topic getCopyrightTopic(MediaCopyright copyright, TopicMap tm) throws TopicMapException {
        if(copyright == null || tm == null) return null;
        Locator copyrightLocator = new Locator(getCopyrightLocator(copyright));
        Topic copyrightTopic = tm.getTopic(copyrightLocator);
        if(copyrightTopic == null) {
            copyrightTopic = tm.createTopic();
            copyrightTopic.addSubjectIdentifier(copyrightLocator);
            copyrightTopic.setBaseName(copyright.getContent()+" (YouTube copyright)");
            copyrightTopic.setDisplayName(LANG, ""+copyright.getContent());
        }
        return copyrightTopic;
    }

    
    public Topic getTopicForMediaContent(VideoEntry entry, com.google.gdata.data.media.mediarss.MediaContent c, TopicMap tm) throws TopicMapException {
        if(c == null || tm == null) return null;
        Locator contentLocator = new Locator(c.getUrl());
        Topic contentTopic = tm.getTopic(contentLocator);
        if(contentTopic == null) {
            contentTopic = tm.createTopic();
            contentTopic.addSubjectIdentifier(contentLocator);
            contentTopic.setSubjectLocator(contentLocator);
            contentTopic.setBaseName("Content for "+entry.getTitle().getPlainText()+" ("+c.getUrl()+")");
            
            String medium = c.getMedium();
            if(medium != null && medium.length() > 0) {
                Topic mediumTopic = getTopicForMedium(medium, tm);
                createAssociation("YouTube content-medium", contentTopic, "YouTube content", mediumTopic, "YouTube medium", tm);
            }
            String type = c.getType();
            if(type != null && type.length() > 0) {
                Topic typeTopic = getTopicForContentType(type, tm);
                createAssociation("YouTube content-type", contentTopic, "YouTube content", typeTopic, "YouTube type", tm);
            }
            
            String language = c.getLanguage();
            if(language != null && language.length() > 0) {
                Topic languageTopic = getTopicForContentLanguage(language, tm);
                createAssociation("YouTube content-language", contentTopic, "YouTube content", languageTopic, "YouTube language", tm);
            }
            
            
            com.google.gdata.data.media.mediarss.MediaContent.Expression expression = c.getExpression();
            if(expression != null) {
                Topic expressionTopic = getTopicForContentExpression(expression, tm);
                createAssociation("YouTube content-expression", contentTopic, "YouTube content", expressionTopic, "YouTube expression", tm);
            }
            
            int bitrate = c.getBitrate();
            if(bitrate > 0) {
                setData(contentTopic, getOrCreateSchemaTopic(tm,"content-bitrate"), LANG, ""+bitrate);
            }
            int channels = c.getChannels();
            if(channels > 0) {
                setData(contentTopic, getOrCreateSchemaTopic(tm,"content-channels"), LANG, ""+channels);
            }
            int filesize = c.getChannels();
            if(filesize > 0) {
                setData(contentTopic, getOrCreateSchemaTopic(tm,"content-filesize"), LANG, ""+filesize);
            }
            int ff = c.getFramerate();
            if(ff > 0) {
                setData(contentTopic, getOrCreateSchemaTopic(tm,"content-framerate"), LANG, ""+ff);
            }
            
            contentTopic.addType(getTopicForContentType(tm));
        }
        return contentTopic;
    }
    
    
    
    
    
    public Topic getTopicForThumbnail(VideoEntry entry, MediaThumbnail t, TopicMap tm) throws TopicMapException {
        if(t == null || tm == null) return null;
        Locator thumbnailLocator = new Locator(t.getUrl());
        Topic thumbnailTopic = tm.getTopic(thumbnailLocator);
        if(thumbnailTopic == null) {
            thumbnailTopic = tm.createTopic();
            thumbnailTopic.addSubjectIdentifier(thumbnailLocator);
            thumbnailTopic.setSubjectLocator(thumbnailLocator);
            thumbnailTopic.setBaseName("Thumbnail for "+entry.getTitle().getPlainText()+" ("+t.getUrl()+")");
        
            thumbnailTopic.addType(getTopicForThumbnailType(tm));
        }
        return thumbnailTopic;
    }
    
    
    public Topic getTopicForContentLanguage(String lan, TopicMap tm) throws TopicMapException {
        if(lan == null || tm == null) return null;
        Locator lanLocator = new Locator(XTMPSI.getLang(lan));
        Topic lanTopic = tm.getTopic(lanLocator);
        if(lanTopic == null) {
            lanTopic = tm.createTopic();
            lanTopic.addSubjectIdentifier(lanLocator);
            lanTopic.setBaseName(lan+" (YouTube content language)");
            lanTopic.setDisplayName(LANG, lan);
        }
        return lanTopic;
    }
    
    
    
    public Topic getTopicForContentExpression(com.google.gdata.data.media.mediarss.MediaContent.Expression expression, TopicMap tm) throws TopicMapException {
        if(expression == null || tm == null) return null;
        Locator expressionLocator = new Locator(getContentExpressionLocator(expression));
        Topic expressionTopic = tm.getTopic(expressionLocator);
        if(expressionTopic == null) {
            expressionTopic = tm.createTopic();
            expressionTopic.addSubjectIdentifier(expressionLocator);
            expressionTopic.setBaseName(expression.toString()+" (YouTube content expression)");
            expressionTopic.setDisplayName(LANG, expression.toString());
        }
        return expressionTopic;
    }
    
    
    public Topic getTopicForContentType(String type, TopicMap tm) throws TopicMapException {
        if(type == null || tm == null) return null;
        Locator typeLocator = new Locator(getContentTypeLocator(type));
        Topic typeTopic = tm.getTopic(typeLocator);
        if(typeTopic == null) {
            typeTopic = tm.createTopic();
            typeTopic.addSubjectIdentifier(typeLocator);
            typeTopic.setBaseName(type+" (YouTube content type)");
            typeTopic.setDisplayName(LANG, type);
        }
        return typeTopic;
    }
    
    
    
    
    public Topic getTopicForMedium(String medium, TopicMap tm) throws TopicMapException {
        if(medium == null || tm == null) return null;
        Locator mediumLocator = new Locator(getMediumLocator(medium));
        Topic mediumTopic = tm.getTopic(mediumLocator);
        if(mediumTopic == null) {
            mediumTopic = tm.createTopic();
            mediumTopic.addSubjectIdentifier(mediumLocator);
            mediumTopic.setBaseName(medium+" (YouTube medium)");
            mediumTopic.setDisplayName(LANG, medium);
            
            mediumTopic.addType(getTopicForMediumType(tm));
        }
        return mediumTopic;
    }
    
    
    
    
    public Topic getTopicForRating(Rating rating, TopicMap tm) throws TopicMapException {
        if(rating == null || tm == null) return null;
        Locator ratingLocator = new Locator(getRatingLocator(rating));
        Topic ratingTopic = tm.getTopic(ratingLocator);
        if(ratingTopic == null) {
            ratingTopic = tm.createTopic();
            ratingTopic.addSubjectIdentifier(ratingLocator);
            ratingTopic.setBaseName(Math.round(rating.getAverage())+" (YouTube rating)");
            ratingTopic.setDisplayName(LANG, ""+Math.round(rating.getAverage()));
            
            ratingTopic.addType(getTopicForRatingType(tm));
        }
        return ratingTopic;
    }
    
    
    
    public Topic getTopicForPerson(Person person, TopicMap topicMap) throws TopicMapException {
        if(person == null || topicMap == null) return null;
        Locator personLocator = new Locator(getPersonLocator(person));
        Topic personTopic = topicMap.getTopic(personLocator);
        if(personTopic == null) {
            personTopic = topicMap.createTopic();
            personTopic.addSubjectIdentifier(personLocator);
            String basename = person.getName();
            String email = person.getEmail();
            if(email != null && email.length() > 0) {
                basename += " ("+email+")";
                setData(personTopic, getOrCreateSchemaTopic(topicMap,"person-email"), LANG, email);
            }
            String uri = person.getUri();
            if(uri != null && uri.length() > 0) {
                setData(personTopic, getOrCreateSchemaTopic(topicMap,"person-uri"), LANG, uri);
            }
            personTopic.setBaseName(basename);
            personTopic.setDisplayName(LANG, person.getName());
            
            personTopic.addType(getTopicForPersonType(topicMap));
        }
        return personTopic;
    }
    
    
    
    
    public Topic getTopicForCategory(MediaCategory category, TopicMap topicMap) throws TopicMapException {
        if(category == null || topicMap == null) return null;
        Locator categoryLocator = new Locator(getCategoryLocator(category.getLabel()));
        Topic categoryTopic = topicMap.getTopic(categoryLocator);
        if(categoryTopic == null) {
            categoryTopic = topicMap.createTopic();
            categoryTopic.addSubjectIdentifier(categoryLocator);
            categoryTopic.setBaseName(category.getLabel()+" (YouTube category)");
            categoryTopic.setDisplayName(LANG, category.getLabel());
            
            categoryTopic.addType(getTopicForCategoryType(topicMap));
        }
        return categoryTopic;
    }
    
    
    
    
    public Topic getTopicForKeyword(String w, TopicMap topicMap) throws TopicMapException {
        if(w == null || topicMap == null) return null;
        Locator wordLocator = new Locator(getKeywordLocator(w));
        Topic wordTopic = topicMap.getTopic(wordLocator);
        if(wordTopic == null) {
            wordTopic = topicMap.createTopic();
            wordTopic.addSubjectIdentifier(wordLocator);
            wordTopic.setBaseName(w+" (YouTube keyword)");
            wordTopic.setDisplayName(LANG, w);
            
            wordTopic.addType(getTopicForKeywordType(topicMap));
        }
        return wordTopic;
    }
    
    
    
    
    // **** LOCATORS ****
    
    
    
    
    public String getContentExpressionLocator(com.google.gdata.data.media.mediarss.MediaContent.Expression expression) {
        String l = CONTENT_EXPRESSION_BASE+locatorEncode(expression.toString());
        return l;
    }
    
    
    public String getContentTypeLocator(String type) {
        String l = CONTENT_TYPE_BASE+locatorEncode(type);
        return l;
    }
    
    public String getMediumLocator(String medium) {
        String l = MEDIUM_BASE+locatorEncode(medium);
        return l;
    }
    
    
    public String getLocationLocator(String location) {
        String l = LOCATION_BASE+locatorEncode(location);
        return l;
    }
    
    
    public String getCopyrightLocator(MediaCopyright copyright) {
        String l = copyright.getUrl();
        if(l == null || l.length() < 1) {
            l = COPYRIGHT_BASE+locatorEncode(copyright.getContent());
        }
        return l;
    }
    
    
    public String getRatingLocator(Rating rating) {
        String l = RATING_BASE+locatorEncode(""+Math.round(rating.getAverage()));
        return l;
    }
    
    
    public String getPersonLocator(Person person) {
        String l = person.getUri();
        if(l == null && person.getEmail() != null) l = PERSON_BASE + locatorEncode(person.getEmail());
        if(l == null) l = PERSON_BASE + locatorEncode(person.getName());
        return l;
    }
    
    
    public String getCategoryLocator(String label) {
        return CATEGORY_BASE + locatorEncode(label);
    }
    
    
    
    public String getVideoLocator(VideoEntry entry) {
        Link link=entry.getHtmlLink();
        if(link!=null) {
            String l = link.getHref();
            // l = l.replaceAll("&feature=youtube_gdata", "");
            // l = l.replace("&feature=youtube_gdata", ""); // REMOVE
            return l;
        }
        else {
            String id = entry.getId();
            if(id.startsWith("http://")) return id;
            else return VIDEO_BASE + entry.getId();
        }
    }
    
    
    public String getKeywordLocator(String word) {
        return KEYWORD_BASE + word;
    }
    
    
    
    
    // **** TYPE TOPICS ****
    
    
    public Topic getTopicForCategoryType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube category");
    }
    
    
    public Topic getTopicForKeywordType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube keyword");
    }
    
    
    public Topic getTopicForVideoType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube video");
    }
    
    public Topic getTopicForVideoFeedType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube video feed");
    }
    
    
    public Topic getTopicForPersonType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube profile");
    }
    
    
    public Topic getTopicForRatingType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube rating");
    }
    
    public Topic getTopicForMediumType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube medium");
    }
    
    public Topic getTopicForThumbnailType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube thumbnail");
    }
    
    public Topic getTopicForContentType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube content");
    }
    
    public Topic getTopicForLocationType(TopicMap tm) throws TopicMapException {
        return getTopicForType(tm, "YouTube location");
    }
    
    
    
    public Topic getTopicForType(TopicMap tm, String type) throws TopicMapException {
        if(tm == null || type == null) return null;
        Topic typeTopic = getOrCreateSchemaTopic(tm, type);
        Topic youTubeClass = getOrCreateTopic(tm, "http://www.youtube.com", "YouTube");
        makeSubclassOf(tm, typeTopic, youTubeClass);
        
        Topic wandoraClass = getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
        makeSubclassOf(tm, youTubeClass, wandoraClass);
        return typeTopic;
    }




    
    // *********
    
    
    private static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    private static Topic getOrCreateSchemaTopic(TopicMap tm, String id) throws TopicMapException {
        return getOrCreateTopic(tm, SCHEMA_BASE+locatorEncode(id), id);
    }



    

    private static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }


    private static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }
    

    private static void setData(Topic t, Topic type, String lang, String text) throws TopicMapException {
        if(t != null & type != null && lang != null && text != null) {
            String langsi=XTMPSI.getLang(LANG);
            Topic langT=t.getTopicMap().getTopic(langsi);
            if(langT == null) {
                langT = t.getTopicMap().createTopic();
                langT.addSubjectIdentifier(new Locator(langsi));
                try {
                    langT.setBaseName("Language " + lang.toUpperCase());
                }
                catch (Exception e) {
                    langT.setBaseName("Language " + langsi);
                }
            }
            t.setData(type, langT, text);
        }
    }
    
    
        

        public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2, TopicMap tm) throws TopicMapException {
            Topic associationTypeTopic = getOrCreateSchemaTopic(tm,associationType);
            Association association = tm.createAssociation(associationTypeTopic);
            Topic role1Topic = getOrCreateSchemaTopic(tm,role1);
            Topic role2Topic = getOrCreateSchemaTopic(tm,role2);
            association.addPlayer(player1Topic, role1Topic);
            association.addPlayer(player2Topic, role2Topic);
            return association;
        }
        
        
        public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2, Topic player3Topic, String role3, TopicMap tm) throws TopicMapException {
            Topic associationTypeTopic = getOrCreateSchemaTopic(tm,associationType);
            Association association = tm.createAssociation(associationTypeTopic);
            Topic role1Topic = getOrCreateSchemaTopic(tm,role1);
            Topic role2Topic = getOrCreateSchemaTopic(tm,role2);
            Topic role3Topic = getOrCreateSchemaTopic(tm,role3);
            association.addPlayer(player1Topic, role1Topic);
            association.addPlayer(player2Topic, role2Topic);
            association.addPlayer(player3Topic, role3Topic);
            return association;
        }
        
        
        
        public Association createAssociation(String associationType, Topic player1Topic, String role1, Topic player2Topic, String role2, Topic player3Topic, String role3, Topic player4Topic, String role4, TopicMap tm) throws TopicMapException {
            Topic associationTypeTopic = getOrCreateSchemaTopic(tm,associationType);
            Association association = tm.createAssociation(associationTypeTopic);
            Topic role1Topic = getOrCreateSchemaTopic(tm,role1);
            Topic role2Topic = getOrCreateSchemaTopic(tm,role2);
            Topic role3Topic = getOrCreateSchemaTopic(tm,role3);
            Topic role4Topic = getOrCreateSchemaTopic(tm,role4);
            association.addPlayer(player1Topic, role1Topic);
            association.addPlayer(player2Topic, role2Topic);
            association.addPlayer(player3Topic, role3Topic);
            association.addPlayer(player4Topic, role4Topic);
            return association;
        }
        



        public static String locatorEncode(String str) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            }
            catch(Exception e) {
                return str;
            }
        }
        
    
    
    
    // ---------------------------------------------------------- SUBCLASSES ---
    
    public class CancelledException extends Exception {
        
    }
    
}
