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
 * DeleteOccurrence.java
 *
 * Created on 23. toukokuuta 2006, 11:24
 *
 */

package org.wandora.application.tools.occurrences;

import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.*;


import java.util.*;



/**
 *
 * @author olli
 */
public class DeleteOccurrence  extends AbstractWandoraTool implements WandoraTool {
    
    private Topic occurrenceType;
    private boolean deleteAll = false;
    private boolean forceStop = false;
    
    
    /** Creates a new instance of DeleteOccurrence */
    public DeleteOccurrence() {
        this.occurrenceType=null;
    }
    public DeleteOccurrence(Context proposedContext) {
        this.setContext(proposedContext);
        this.occurrenceType=null;
    }
    public DeleteOccurrence(Context proposedContext, Topic occurrenceType) {
        this.setContext(proposedContext);
        this.occurrenceType=occurrenceType;
    }
    public DeleteOccurrence(Topic occurrenceType) {
        this.occurrenceType=occurrenceType;
    }

    @Override
    public String getName() {
        return "Delete occurrence";
    }


    @Override
    public String getDescription() {
        return "Delete occurrence of given type.";
    }

    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Object contextSource = context.getContextSource();
        
        if(contextSource instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) contextSource;
            ot.delete();
        }
        else {
            Iterator topics = getContext().getContextObjects();
            Topic topic = null;
            int count = 0;
            Topic type=occurrenceType;
            deleteAll = false;
            forceStop = false;
            
            ArrayList<Topic> allOccurrenceTypes = new ArrayList<Topic>();
            
            if(topics!= null && topics.hasNext()) {
                if(type == null) {
                    while(topics.hasNext() && !forceStop() && !forceStop) {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            Collection<Topic> types=topic.getDataTypes();
                            if(types.isEmpty()) continue;
                            for(Topic t : types) {
                                if(!allOccurrenceTypes.contains(t)) {
                                    allOccurrenceTypes.add(t);
                                }
                            }
                        }
                    }
                    if(allOccurrenceTypes.size() > 0) {
                        Object[] values=new Object[allOccurrenceTypes.size()];
                        int counter=0;
                        for(Topic t : allOccurrenceTypes){
                            values[counter++]=new ComboBoxTopicWrapper(t);
                        }
                        Object selected=WandoraOptionPane.showOptionDialog(admin, "Which occurrence you want to delete?", "Delete occurrence", WandoraOptionPane.QUESTION_MESSAGE, values, values[0]);

                        if(selected==null) return;
                        type=((ComboBoxTopicWrapper)selected).topic;
                        topics = getContext().getContextObjects();
                    }
                    else {
                        singleLog("No occurrences found in selected topics!");
                    }
                }
                if(type == null) return;
                
                while(topics.hasNext() && !forceStop() && !forceStop) {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        if(shouldDelete(topic,type,admin)) {
                            try {
                                Iterator iter=new ArrayList(topic.getData(type).keySet()).iterator();
                                while(iter.hasNext()){
                                    Topic version=(Topic)iter.next();
                                    topic.removeData(type,version);
                                }
                                count++;
                            }
                            catch(Exception e2) {
                                log(e2);
                            }
                        }
                    }
                }
            }
        }
    }   
    
    
    
    private boolean shouldDelete(Topic topic,Topic type,Wandora parent){
        if(deleteAll) return true;
        else {
            int answer = WandoraOptionPane.showConfirmDialog(parent, 
                    "Do you want to delete occurrences of type '"+parent.getTopicGUIName(type)+"' from topic '"+parent.getTopicGUIName(topic)+"'?",
                    "Confirm delete", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
            if(answer == WandoraOptionPane.YES_TO_ALL_OPTION) {
                deleteAll = true;
                return true;
            }
            else if(answer == WandoraOptionPane.YES_OPTION) {
                return true;
            }
            else {
                if(answer == WandoraOptionPane.CANCEL_OPTION) {
                    forceStop = true;
                }
                return false;
            }
        }
    }
}
