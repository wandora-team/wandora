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
 * DeleteSIs.java
 *
 * Created on 21. heinäkuuta 2006, 16:57
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;

import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;

import org.wandora.*;

import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;


/**
 * Deletes subject identifiers. Exact behavior depends on given context. If
 * context is <code>SIContext</code> then tools class deletes all context subject
 * identifiers of owner topics (unless the subject identifier is the only subject
 * identifier in the topic). If context contains topics then tool deletes all 
 * subject identifiers of context topics but confirms all deletions from the
 * user. Again, only or last subject identifier of topic is never deleted. 
 * Topic must always have at least one subject identifier.
 *
 * @author akivela
 */
public class DeleteSIs extends AbstractWandoraTool implements WandoraTool {
    
    protected boolean forceDelete = true;
    protected boolean confirm = true;
    protected boolean shouldContinue = true;
    
    protected Wandora wandora = null;
       
    
    
    public DeleteSIs() {
    }
    public DeleteSIs(Context context) {
        setContext(context);
    }
    

    @Override
    public String getName() {
        return "Delete SIs";
    }

    @Override
    public String getDescription() {
        return "Removes subject identifiers in context topics.";
    }
    
    
    
    @Override
    public void execute(Wandora w, Context context) throws TopicMapException {
        this.wandora = w;
        
        setDefaultLogger();
        
        if(context instanceof SIContext) {
            Iterator sii = context.getContextObjects();
            if(sii.hasNext()) {
                int count = 0;
                Locator si = (Locator) sii.next();
                TopicMap topicmap = wandora.getTopicMap();
                Topic topic = topicmap.getTopic(si);
                if(topic.getSubjectIdentifiers().size() > 1) {
                    if(shouldDelete(topic, si)) {
                        topic.removeSubjectIdentifier(si);
                        count++;
                    }
                    while(sii.hasNext() && shouldContinue && !forceStop()) {
                        si = (Locator) sii.next();
                        if(topic.getSubjectIdentifiers().size() > 1) {
                            if(shouldDelete(topic, si)) {
                                topic.removeSubjectIdentifier((Locator) si);
                                count++;
                            }
                        }
                        else {
                            log("Topic has one subject identifier left. Deletion not allowed!");
                        }
                    }
                    log("Total " + count + " subject identifiers deleted.");
                }
                else {
                    log("Topic has only one subject identifier. Deletion not allowed!");
                }
            }
            else {
                log("No subject identifiers found in context.");
            }
        }
        
        // ***** HANDLE OTHER CONTEXTS *****
        else {
            Iterator topics = getContext().getContextObjects();
            Topic topic = null;
            Topic ltopic = null;
            int count = 0;
            int tcount = 0;
            int icount = 0;
            ConfirmResult r_all = null;
            ConfirmResult r = null;
            yesToAll = false;
            shouldContinue = true;
            String topicName = null;

            if(topics != null && topics.hasNext()) {
                while(topics.hasNext() && shouldContinue && !forceStop()) {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        if(topic instanceof LayeredTopic) {
                            ltopic = ((LayeredTopic) topic).getTopicForSelectedLayer();
                            if(ltopic == null || ltopic.isRemoved()) {
                                setState(INVISIBLE);
                                int answer = WandoraOptionPane.showConfirmDialog(wandora,"Topic '"+TopicToString.toString(topic)+"' doesn't exist in selected layer.", "Topic not in selected layer", WandoraOptionPane.OK_CANCEL_OPTION);
                                setState(VISIBLE);
                                if(answer == WandoraOptionPane.CANCEL_OPTION) shouldContinue = false;
                                continue;
                            }
                            else {
                                topic = ltopic;
                            }
                        }
                        topicName = topic.getBaseName();
                        if(topicName == null) topicName = topic.getOneSubjectIdentifier().toExternalForm();
                        hlog("Investigating topic '" + topicName + "'.");
                        icount++;

                        Collection<Locator> sis = collectSubjectIdentifiers(topic);
                        Iterator<Locator> sii = sis.iterator();

                        if(sii.hasNext()) {
                            Locator l = null;
                            ArrayList sisToDelete = new ArrayList();

                            while(sii.hasNext()) {
                                l = sii.next();
                                if(shouldDelete(topic, l)) {
                                    try {
                                        if(r_all == null) r = TMBox.checkSubjectIdentifierChange(wandora,topic,l, false);
                                        else r = r_all;

                                        if(r == ConfirmResult.cancel) break;
                                        else if(r == ConfirmResult.no) continue;
                                        else if(r == ConfirmResult.notoall) { r_all = r; continue; }
                                        else if(r == ConfirmResult.yestoall) { r_all = r; }

                                        sisToDelete.add(l);
                                        count++;
                                    }
                                    catch(Exception e2) {
                                        log(e2);
                                    }
                                }
                            }

                            if(sisToDelete.size() > 0) {
                                tcount++;
                                sii = sisToDelete.iterator();
                                while(sii.hasNext()) {
                                    topic.removeSubjectIdentifier(sii.next());
                                }
                            }
                        }
                    }
                }
            }
            log("Total " + icount + " topics investigated.");
            log("Total " + count + " subject identifiers deleted in "+tcount+" topics.");
        }
        
        setState(WAIT);
    }
    
    
    
    
    
    public Collection<Locator> collectSubjectIdentifiers(Topic topic) throws TopicMapException {
        ArrayList<Locator> sis = new ArrayList();
        Iterator<Locator> sii = topic.getSubjectIdentifiers().iterator();
        sii.next(); // Hop over == save first locator
        while(sii.hasNext()) {
            sis.add(sii.next());
        }
        return sis;
    }
    
    
    public boolean shouldDelete(Topic topic, Locator si)  throws TopicMapException {
        if(confirm) {
            return confirmDelete(topic, si);
        }
        else {
            return true;
        }
    }
    
    public boolean yesToAll = false;
    public boolean confirmDelete(Topic topic, Locator si) throws TopicMapException  {
        String topicName = null;
        if(topic != null) {
            topicName = topic.getBaseName();
            if(topicName == null) topicName = topic.getOneSubjectIdentifier().toExternalForm();
        }
        
        if(yesToAll) {
            if(topicName != null) {
                hlog("Deleting topic's '"+ topicName +"' SI '"+si.toExternalForm()+"'.");
            }
            else {
                hlog("Deleting SI '"+si.toExternalForm()+"'.");
            }
            return true;
        }
        else {
            setState(INVISIBLE);

            String confirmMessage;
            if(topicName != null) {
                confirmMessage = "Would you like delete topic's '" + topicName + "' subject identifier '"+si.toExternalForm()+"'?";
            }
            else {
                confirmMessage = "Would you like delete subject identifier '"+si.toExternalForm()+"'?";
            }
            int answer = WandoraOptionPane.showConfirmDialog(wandora, confirmMessage, "Confirm delete", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
            setState(VISIBLE);
            if(answer == WandoraOptionPane.YES_OPTION) {
                return true;
            }
            if(answer == WandoraOptionPane.YES_TO_ALL_OPTION) {
                yesToAll = true;
                return true;
            }
            else if(answer == WandoraOptionPane.CANCEL_OPTION) {
                shouldContinue = false;
            }
            return false;
        }
    }
    
}
