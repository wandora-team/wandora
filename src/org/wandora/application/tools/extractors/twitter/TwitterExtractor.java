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
 * TwitterExtractor.java
 */


package org.wandora.application.tools.extractors.twitter;


import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.TopicMap;
import twitter4j.Query;


/**
 *
 * @author akivela
 */


public class TwitterExtractor extends AbstractTwitterExtractor {
    
    private TwitterExtractorUI ui = null;

    
    
    
    
    @Override
    public String getName() {
        return "Twitter search extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Extractor performs Twitter search and converts results to topics and associations.";
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            if(ui == null) {
                ui = new TwitterExtractorUI();
            }
            ui.openDialog(wandora, this);

            if(ui.wasAccepted()) {
                setDefaultLogger();
                if(ui.shouldResetTwitter()) {
                    log("Resetting Twitter authorization.");
                    resetTwitter();
                }
                log("Executing Twitter API request...");
                TopicMap tm = wandora.getTopicMap();
                Query[] query = ui.getSearchQuery();
                int pages = ui.getPages();
                if(query != null && query.length > 0) {
                    try {
                        searchTwitter(query, pages, tm);
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                else {
                    log("Given search query is null or zero length!");
                }
            }
            else {
                // log("User cancelled the extraction!");
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
        if(ui != null && ui.wasAccepted()) setState(WAIT);
        else setState(CLOSE);
    }
    
    
    
    
}
