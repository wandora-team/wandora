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
 */


package org.wandora.application.tools.webview;

import java.util.Map;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public class AddWebSourceAsOccurrence extends AbstractWebViewTool {
    public boolean USE_SELECTION_SOURCE = false;
    
    public AddWebSourceAsOccurrence() {}
    
    public AddWebSourceAsOccurrence(boolean selectionSource) {
        USE_SELECTION_SOURCE = selectionSource;
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        try {
            String source = null;
            if(USE_SELECTION_SOURCE) {
                source = getSelectedSource(context);
            }
            else {
                source = getSource(context);
            }
            if(source != null && source.length() > 0) {
                Topic topic = getTopic(context);
                if(topic != null && !topic.isRemoved()) {
                    Topic otype = null;
                    Topic oversion = null;
                    TopicMap tm = wandora.getTopicMap();
                    GenericOptionsDialog god = new GenericOptionsDialog(
                        wandora,
                        "Select occurrence type and scope","Select occurrence type and scope",
                        true,
                        new String[][]{
                            new String[] { "Occurrence type topic","topic","","Select occurrence type topic." },
                            new String[] { "Occurrence scope topic","topic","","Select occurrence scope topic ie. language" },
                        },
                        wandora
                    );
                    god.setVisible(true);
                    if(god.wasCancelled()) return;

                    Map<String,String> values=god.getValues();
                    otype = tm.getTopic(values.get("Occurrence type topic"));
                    oversion = tm.getTopic(values.get("Occurrence scope topic"));
                    if(otype != null && oversion != null) {
                        topic.setData(otype, oversion, source);
                    }
                }
            }
            else {
                log("Unable to solve web source, aborting.");
            }
        }
        catch(Exception ex) {
            log(ex);
        }
    }
    
    
    
    
    @Override
    public String getDescription() {
        if(USE_SELECTION_SOURCE) {
            return "Add selection source to the current topic as an occurrence.";
        }
        else {
            return "Add source to the current topic as an occurrence.";
        }
    }
}