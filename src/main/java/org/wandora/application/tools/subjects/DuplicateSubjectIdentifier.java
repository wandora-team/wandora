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
 * DuplicateSubjectIdentifier.java
 *
 * Created on 2012-01-18
 *
 */
package org.wandora.application.tools.subjects;

import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.SIContext;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;

/**
 * <p>
 * Add context topics a new subject identifier if topic has already a matching 
 * subject identifier. Matching subject is recognized with a regular expression
 * that was given by the user. Added subject identifier will be same as matching 
 * subject identifier but has an extension '_copy'.
 * </p>
 * <p>
 * This is a handy tool when you need to sculpt a new subject identifier to
 * certain topics and need to keep old subject identifiers intact. Pattern goes:
 * First create these certain topics a new subject identifier that is based on
 * old subject. Then use regular expressions to modify new subject identifiers.
 * </p>
 * 
 * @author akivela
 */
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;



public class DuplicateSubjectIdentifier extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;
	
	protected Wandora wandora = null;
       
    
    
    public DuplicateSubjectIdentifier() {
    }
    public DuplicateSubjectIdentifier(Context context) {
        setContext(context);
    }
    

    @Override
    public String getName() {
        return "Duplicate SIs";
    }

    @Override
    public String getDescription() {
        return "Add topics a new subject identifier if topic has already a matching subject identifier. "+
               "Added subject identifier will be same as matching subject identifier but has an extension _copy.";
    }
    
    
    
    @Override
    public void execute(Wandora w, Context context) throws TopicMapException {
        this.wandora = w;
        if(context.getContextObjects() == null || !context.getContextObjects().hasNext()) {
            return;
        }
        
        if(context instanceof SIContext) {
            setDefaultLogger();
            Iterator sii = context.getContextObjects();
            int count = 0;
            while(sii.hasNext() && !forceStop()) {
                Locator si = (Locator) sii.next();
                if(si != null) {
                    TopicMap topicmap = wandora.getTopicMap();
                    Topic topic = topicmap.getTopic(si);
                    if(topic != null && !topic.isRemoved()) {
                        String sis = si.toExternalForm();
                        String copysis = buildCopySubject(topic, sis);
                        topic.addSubjectIdentifier(new Locator(copysis));
                        count++;
                    }
                }
                else {
                    log("Given subject identifier was null! Rejecting.");
                }
            }
            if(count > 0) {
                log("Total " + count + " subject identifiers added.");
            }
            else {
                log("No subject identifiers found in context.");
            }
        }
        
        // ***** HANDLE OTHER CONTEXTS *****
        else {
            String regularExpression = WandoraOptionPane.showInputDialog(wandora, "Give regular expression used to match source identifier.", ".*", "Source identifier", WandoraOptionPane.QUESTION_MESSAGE);
            if(regularExpression == null || regularExpression.length() == 0) return;

            setDefaultLogger();
            
            Iterator topics = getContext().getContextObjects();
            Topic topic = null;
            int count = 0;
            int tcount = 0;
            int icount = 0;
            boolean siAdded = false;

            if(topics != null && topics.hasNext()) {
                while(topics.hasNext() && !forceStop()) {
                    topic = (Topic) topics.next();
                    siAdded = false;
                    icount++;
                    if(topic != null && !topic.isRemoved()) {
                        for(Locator si : topic.getSubjectIdentifiers()) {
                            String sis = si.toExternalForm();
                            if(sis != null && sis.matches(regularExpression)) {
                                String copysis = buildCopySubject(topic, sis);
                                topic.addSubjectIdentifier(new Locator(copysis));
                                count++;
                                siAdded = true;
                            }
                        }
                        if(siAdded) {
                            tcount++;
                        }
                    }
                }
            }
            log("Total " + icount + " topics investigated.");
            log("Total " + count + " subject identifiers duplicated in "+tcount+" topics.");
        }
        setState(WAIT);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    
    protected String buildCopySubject(Topic topic, String originalSI) {
        return originalSI + "_copy";
    }
    

}
