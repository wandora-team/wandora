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
 */


package org.wandora.application.tools.extractors.youtube;


import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;

import java.net.*;
import java.util.*;

import com.google.gdata.client.youtube.*;
import com.google.gdata.data.youtube.*;
import com.google.gdata.util.*;


/**
 *
 * @author akivela
 */
public class YouTubeVideoFeedExtractor extends YouTubeExtractor {

    private String baseLocator = "http://wandora.org/si/youtube";

    

    
   
    
    @Override
    public String getName() {
        return "YouTube video feed extractor...";
    }

    
    @Override
    public String getDescription(){
        return "Convert YouTube Video Feed to a Topic Map";
    }
    
    
    public void execute(Wandora admin, Context context) {
        int counter = 0;
        YouTubeVideoFeedSelector feedSelector = null;
        try {
            YouTubeService service = initializeService(admin);
            feedSelector = new YouTubeVideoFeedSelector(admin, this);
            feedSelector.setVisible(true);
            setDefaultLogger();
            if(feedSelector.isAccepted()) {
                counter = feedSelector.processVideoFeeds(admin, context, service, this);
                log("Total "+counter+" videos found and extracted from feed.");
                log("Ready.");
            }
            if(!feedSelector.isAccepted() || forceStop()) {
                log("Break!");
            }
        }
        catch(CancelledException ce) {
            // singleLog("User cancelled the tool.");
        }
        catch(AuthenticationException ae) {
            singleLog("Invalid username or password.");
            forgetAuthorization();
        }
        catch(Exception e) {
            singleLog(e);
        }
        if(feedSelector != null && feedSelector.isAccepted()) {
            setState(WAIT);
        }
    }

    
    
    public int extract(VideoFeed videoFeed, URL feedUrl, Wandora admin, TopicMap topicMap) throws TopicMapException  {
        int counter = 0;
        VideoEntry entry = null;
        List<VideoEntry> entries = videoFeed.getEntries();
        for(Iterator<VideoEntry> i = entries.iterator(); i.hasNext() && !forceStop(); ) {
            entry = i.next();
            extract(entry, videoFeed, feedUrl, admin, topicMap);
            counter++;
        }
        return counter;
    }
    
    
    
    public Topic extract(VideoEntry entry, VideoFeed videoFeed, URL feedUrl, Wandora admin, TopicMap topicMap) throws TopicMapException  {
        if(entry != null) {
            log("Extracting video '"+entry.getTitle().getPlainText()+"'.");
        }
        Topic videoFeedTopic = null;
        if(videoFeed!=null) videoFeedTopic = getTopicForVideoFeed(videoFeed, feedUrl, topicMap);
        Topic videoTopic = getTopicForVideo(entry, topicMap);
        if(videoFeedTopic != null && videoTopic != null) {
            createAssociation("YouTube video feed", videoFeedTopic, "YouTube video feed", videoTopic, "YouTube video", topicMap);
        }
        return videoTopic;
    }
    
    
}
