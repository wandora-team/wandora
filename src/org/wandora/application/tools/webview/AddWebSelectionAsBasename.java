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


import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;



/**
 *
 * @author akivela
 */


public class AddWebSelectionAsBasename extends AbstractWebViewTool {

    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        try {
            String selection = getSelectedText(context);
            if(selection != null && selection.length() > 0) {
                Topic topic = getTopic(context);
                if(topic != null && !topic.isRemoved()) {
                    selection = selection.replace('\n', ' ');
                    selection = selection.replace('\r', ' ');
                    selection = selection.replace('\b', ' ');
                    selection = selection.replace('\t', ' ');
                    topic.setBaseName(selection);
                }
            }
            else {
                log("No text selected, aborting.");
            }
        }
        catch(Exception ex) {
            log(ex);
        }
    }
    
    
    @Override
    public String getDescription() {
        return "Adds selected text as a basename to the current topic.";
    }
    
}
