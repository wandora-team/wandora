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
 * 
 * 
 * MergeTopics.java
 *
 * Created on 2. elokuuta 2006, 17:37
 *
 */

package org.wandora.application.tools;




import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import java.util.*;



/**
 * WandoraTool merging all context topics. Merge is performed by adding one 
 * subject identifier from one context topic to all other context topics.
 * Wandora's topic map model merges topics automatically whenever topics
 * share a subject identifier.
 *
 * @author akivela
 */
public class MergeTopics extends AbstractWandoraTool implements WandoraTool {
    
    /** Creates a new instance of MergeTopics */
    public MergeTopics() {
    }

    @Override
    public String getName() {
        return "Merge topics";
    }

    @Override
    public String getDescription() {
        return "Inserts same subject identifier to all selected topics. All selected topics are merged.";
    }
    

    @Override
    public void execute(Wandora wandora, Context context) {
        Iterator topics = context.getContextObjects();
        
        int progress = 0;

        ArrayList<Topic> mergeList = new ArrayList<Topic>();
        Topic t = null;
        while(topics.hasNext()) {
            try {
                t = (Topic) topics.next();
                if(t != null && !t.isRemoved()) {
                    if(!mergeList.contains(t)) mergeList.add(t);
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        
        topics = mergeList.iterator();

        if(topics != null && topics.hasNext()) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to merge selected topics? Restoring topics may be very difficult!", "Confirm Merge", WandoraOptionPane.YES_NO_OPTION);
            if(a == WandoraOptionPane.YES_OPTION) {
                Topic topic = null;
                String si = null;
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            if(si == null) {
                                si = topic.getOneSubjectIdentifier().toExternalForm();
                                continue;
                            }
                            setProgress(progress++);
                            if(si != null) {
                                topic.addSubjectIdentifier(new Locator(si));
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
        }
    }
}
