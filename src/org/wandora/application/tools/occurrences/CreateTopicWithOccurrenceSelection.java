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
 */


package org.wandora.application.tools.occurrences;


import org.wandora.application.gui.texteditor.OccurrenceTextEditor;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.*;




/**
 *
 * @author akivela
 */
public class CreateTopicWithOccurrenceSelection extends AbstractWandoraTool implements WandoraTool {
    
    public boolean ASSOCIATE_TO_OCCURRENCE_CARRIER = true;
    private boolean requiresRefresh = false;
            
            
    /** Creates a new instance of CreateTopicWithOccurrenceSelection */
    public CreateTopicWithOccurrenceSelection() {
    }
    public CreateTopicWithOccurrenceSelection(boolean associate) {
        ASSOCIATE_TO_OCCURRENCE_CARRIER = associate;
    }
    public CreateTopicWithOccurrenceSelection(Context proposedContext) {
        this.setContext(proposedContext);
    }
    public CreateTopicWithOccurrenceSelection(boolean associate, Context proposedContext) {
        ASSOCIATE_TO_OCCURRENCE_CARRIER = associate;
        this.setContext(proposedContext);
    }

    @Override
    public String getName() {
        return "Create topic with occurrence selection";
    }

    @Override
    public String getDescription() {
        return "Create topic with occurrence selection and optionally associate created topic to occurrence carrier.";
    }

    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        Object source = getContext().getContextSource();
        requiresRefresh = false;
        if(source instanceof OccurrenceTextEditor) {
            try {
                OccurrenceTextEditor oe = (OccurrenceTextEditor) source;
                String text = oe.getSelectedText();
                if(text != null) {
                    text = text.replace("\n", " ");
                    text = text.replace("\r", "");
                    text = text.replace("\t", " ");
                    text = text.trim();
                    if(text.length() > 0) {
                        TopicMap tm = wandora.getTopicMap();
                        Topic topic = tm.getTopicWithBaseName(text);
                        if(topic == null) {
                            topic = tm.createTopic();
                            topic.addSubjectIdentifier(TopicTools.createDefaultLocator());
                            topic.setBaseName(text);
                        }
                        else {
                            log("Topic '"+text+"' already exists.");
                        }
                        if(topic != null) {
                            if(ASSOCIATE_TO_OCCURRENCE_CARRIER) {
                                Topic ocarrier = (Topic) context.getContextObjects().next();
                                if(ocarrier != null) {
                                    Topic atype = getOrCreateTopic(tm, "http://wandora.org/si/occurrence-distilled-association", "Occurrence distilled association");
                                    Association a = tm.createAssociation(atype);
                                    a.addPlayer(ocarrier, getOrCreateTopic(tm, "http://wandora.org/si/occurrence-carrier", "Occurrence carrier"));
                                    a.addPlayer(topic, getOrCreateTopic(tm, "http://wandora.org/si/occurrence-distilled-topic", "Occurrence distilled topic"));
                                    requiresRefresh = true;
                                }
                            }
                        }
                    }
                    else {
                        log("No valid occurrence text selection found.");
                    }
                }
                else {
                    log("No valid occurrence text selection found.");
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        else {
            log("Invalid context source. This tool requires OccurrenceTextEditor as context source!");
        }
    }
    
    
    

    protected Topic getOrCreateTopic(TopicMap tm, String si,String bn) throws TopicMapException {
        if(si!=null){
            Topic t=tm.getTopic(si);
            if(t==null){
                t=tm.createTopic();
                t.addSubjectIdentifier(tm.createLocator(si));
                if(bn!=null) t.setBaseName(bn);
            }
            return t;
        }
        else{
            Topic t=tm.getTopicWithBaseName(bn);
            if(t==null){
                t=tm.createTopic();
                if(bn!=null) t.setBaseName(bn);
                if(si!=null) t.addSubjectIdentifier(tm.createLocator(si));
                else t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
            }
            return t;
        }
    }

    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }

}
