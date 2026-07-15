/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * PasteSubjectIdentifiers.java
 *
 * Created on 6. tammikuuta 2005, 15:12
 */

package org.wandora.application.tools.subjects;



import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.wandora.application.CancelledException;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.SIContext;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;



/**
 * Adds URLs in system clipboard to a topic as subject identifiers. If
 * topic map already contains topics with added subjects the application
 * merges all topics.
 *
 * @author akivela
 */
public class PasteSubjectIdentifiers extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	public boolean confirm = true;
    
    
    public PasteSubjectIdentifiers() {}
    public PasteSubjectIdentifiers(Context context) {
        setContext(context);
    }
    
    

    @Override
    public String getName() {
        return "Paste subject identifiers";
    }

    @Override
    public String getDescription() {
        return "Adds subject identifiers in clipboard "+
               "to current topics. If same subject identifier is added to many topics, the "+
               "topics are merged automatically by Wandora.";
    }

    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        
        try {
            Collection<Topic> targetTopics = getTargetTopics(context);
            if(targetTopics == null || targetTopics.isEmpty()) {
                log("Found no topics to add subject identifiers to. Select at least one topic and try again.");
                return;
            }

            Collection<Locator> subjectIdentifiers = getSubjectIdentifiersToPaste();
            if(subjectIdentifiers == null || subjectIdentifiers.isEmpty()) {
                log("Found no subject identifiers to paste. Copy URLs to system clipboard and try again.");
                return;
            }

            for(Topic topic : targetTopics) {
                try {
                    if(topic != null && !topic.isRemoved()) {
                        for(Locator subjectIdentifier : subjectIdentifiers) {
                            if(isValidSubjectIdentifier(subjectIdentifier)) {
                                Topic existingTopic = topic.getTopicMap().getTopic(subjectIdentifier);
                                int shouldAdd = WandoraOptionPane.YES_OPTION;
                                if(confirm && existingTopic != null && !existingTopic.equals(topic)) {
                                    shouldAdd = WandoraOptionPane.showConfirmDialog(wandora,"Another topic named as '"+TopicToString.toString(existingTopic)+"' already contains subject identifier '" + subjectIdentifier.toExternalForm() + "'. Merge occurs if subject identifier is added. Do you want to add the subject identifier?",
                                            "Add subject identifier?", 
                                            WandoraOptionPane.YES_NO_CANCEL_OPTION);
                                    if(shouldAdd == WandoraOptionPane.CANCEL_OPTION || shouldAdd == WandoraOptionPane.CLOSED_OPTION) {
                                        return;
                                    }
                                }
                                if(shouldAdd == WandoraOptionPane.YES_OPTION) {
                                    topic.addSubjectIdentifier(subjectIdentifier);
                                }
                            }
                        }
                    }
                }
                catch (CancelledException ce) {
                    break;
                }
                catch (Exception e) {
                    int shouldContinue = WandoraOptionPane.showConfirmDialog(wandora,"Exception '" + e.getMessage() + "' occurred while adding subject identifier to topic '"+TopicToString.toString(topic)+"'. Would you like to continue adding subject indentifiers?","Continue?", WandoraOptionPane.YES_NO_OPTION);
                    if(shouldContinue != WandoraOptionPane.YES_OPTION) return;
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    


    protected Collection<Topic> getTargetTopics(Context context) {
        ArrayList<Topic> topics = new ArrayList<>();
        if(context instanceof SIContext) {
            Iterator<Locator> sis = context.getContextObjects();
            TopicMap topicmap = Wandora.getWandora().getTopicMap();
            Locator si = null;
            Topic t = null;
            while(sis.hasNext()) {
                try {
                    si = sis.next();
                    t = topicmap.getTopic(si);
                    if(!topics.contains(t)) {
                        topics.add(t);
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }
        else {
            Iterator<Topic> topicIterator = context.getContextObjects();
            while(topicIterator.hasNext()) {
                topics.add(topicIterator.next());
            }
        }
        return topics;
    }
    
    
    public Collection<Locator> getSubjectIdentifiersToPaste() {
        ArrayList<Locator> subjectIdentifiers = new ArrayList<>();
        String text = ClipboardBox.getClipboard();
        StringTokenizer tokenizer = new StringTokenizer(text, "\n");
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if(token != null) {
                token = token.trim();
                Locator subjectIdentifier = new Locator(token);
                subjectIdentifiers.add(subjectIdentifier);
            }
        }
        return subjectIdentifiers;
    }
    
    
    protected boolean isValidSubjectIdentifier(Locator locator) throws CancelledException {
        boolean isValid = false;
        
        if(locator != null) {
            String str = locator.toExternalForm();
            if(str != null) {
                if(str.length() > 0) {
                    if(DataURL.isDataURL(str)) {
                        isValid = true;
                    }
                    else {
                        try {
                            new URL(str);
                            isValid = true;
                        }
                        catch(java.net.MalformedURLException mue) {
                            
                        }
                    }
                }
            }
        }
        if(!isValid && confirm) {
            int shouldContinue = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(),
                    "Invalid subject identifier '" + locator + "' given. Would you like to continue?", 
                    "Invalid subject identifier", 
                    WandoraOptionPane.YES_NO_OPTION);
            if(shouldContinue != WandoraOptionPane.YES_OPTION) {
                throw new CancelledException();
            };
        }
        return isValid;
    }
    
}
