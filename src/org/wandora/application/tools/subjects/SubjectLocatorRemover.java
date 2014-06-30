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
 * SubjectLocatorRemover.java
 *
 * Created on 25. toukokuuta 2006, 11:17
 *
 */

package org.wandora.application.tools.subjects;



import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;




/**
 *
 * @author akivela
 */


public class SubjectLocatorRemover extends AbstractWandoraTool implements WandoraTool {


    public SubjectLocatorRemover() {
        setContext(new TopicContext());
    }
    public SubjectLocatorRemover(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Topic subject locator remover";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and removes subject locators from each topic.";
    }
    
  
    @Override
    public void execute(Wandora admin, Context context) {   
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            setDefaultLogger();
            setLogTitle("Removing subject locators");

            Topic topic = null;
            int progress = 0;
            int locatorCount = 0;
            int deleteCount = 0;
            Locator l = null;
            boolean removeNext = false;
            boolean removeAll = false;

            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        progress++;
                        setProgress(progress);
                        l = topic.getSubjectLocator();
                        if(l != null) {
                            locatorCount++;
                            removeNext = false;
                            if(!removeAll) {
                                setState(INVISIBLE);
                                int a = WandoraOptionPane.showConfirmDialog(admin, "Are you sure you want to remove subject locator\n"+l.toExternalForm()+"?","Confirm SL remove", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                                setState(VISIBLE);
                                if(a == WandoraOptionPane.YES_OPTION) removeNext = true;
                                else if(a == WandoraOptionPane.YES_TO_ALL_OPTION) removeAll = true;
                                else if(a == WandoraOptionPane.CLOSED_OPTION) break;
                                else if(a == WandoraOptionPane.CANCEL_OPTION) break;
                            }
                            if(removeAll || removeNext) {
                                deleteCount++;
                                log("Removing subject locator '"+l.toExternalForm()+"'");
                                topic.setSubjectLocator(null);
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            if(progress == 0) {
                log("Context didn't contain topics. Nothing removed.");
            }
            else if(locatorCount == 0) {
                log("Context didn't contain valid subject locators. Nothing removed.");
            }
            log("Done.");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
 
}
