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
 * DeleteSubjectLocator.java
 *
 * Created on 25. toukokuuta 2006, 11:17
 *
 */

package org.wandora.application.tools.subjects;



import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.TopicContext;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;




/**
 *
 * @author akivela
 */


public class DeleteSubjectLocator extends AbstractWandoraTool implements WandoraTool {


    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public DeleteSubjectLocator() {
        setContext(new TopicContext());
    }
    public DeleteSubjectLocator(Context preferredContext) {
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
                            String locatorString = l.toExternalForm();
                            if(locatorString.length() > 256) {
                                locatorString = locatorString.substring(0, 256) + "...";
                            }
                            if(!removeAll) {
                                int a = WandoraOptionPane.showConfirmDialog(admin, "Are you sure you want to remove subject locator\n"+locatorString+"?","Confirm subject locator remove", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                                if(a == WandoraOptionPane.YES_OPTION) removeNext = true;
                                else if(a == WandoraOptionPane.YES_TO_ALL_OPTION) removeAll = true;
                                else if(a == WandoraOptionPane.CLOSED_OPTION) break;
                                else if(a == WandoraOptionPane.CANCEL_OPTION) break;
                            }
                            if(removeAll) {
                                if(deleteCount==0) {
                                    setDefaultLogger();
                                    setLogTitle("Removing subject locators");
                                }
                                log("Removing subject locator '"+locatorString+"'");
                            }
                            if(removeAll || removeNext) {
                                deleteCount++;
                                topic.setSubjectLocator(null);
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            if(removeAll) {
                if(deleteCount==1) {
                    log("Removed one subject locator.");
                }
                else {
                    log("Removed "+deleteCount+" subject locators.");
                }
                log("Ready.");
            }
            else {
                if(progress > 1) {
                    log("Inspected "+progress+" topics and removed "+deleteCount+" subject locators.");
                }
            }
        }
        catch (Exception e) {
            log(e);
        }
    }
    
 
}
