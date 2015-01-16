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
 * MakeSLWithSI.java
 *
 * Created on 23. lokakuuta 2007, 17:34
 *
 */

package org.wandora.application.tools.subjects;

import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import static org.wandora.application.gui.ConfirmResult.*;
import org.wandora.application.tools.*;
import org.wandora.*;

import java.util.*;


/**
 * Adds selected subject identifier to topic as a subject locator.
 *
 * @author akivela
 */
public class MakeSLWithSI extends AbstractWandoraTool implements WandoraTool {


    public MakeSLWithSI() {
    }
    public MakeSLWithSI(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Make SL with SI";
    }

    @Override
    public String getDescription() {
        return "Adds selected subject identifier to topic as a subject locator.";
    }
    
  
    public void execute(Wandora admin, Context context) {
        if(context instanceof SIContext) {
            Iterator sis = context.getContextObjects();
            if(sis.hasNext()) {
                try {
                    Locator si = (Locator) sis.next();
                    TopicMap topicmap = admin.getTopicMap();
                    Topic t = topicmap.getTopic(si);
                    t.setSubjectLocator(si);
                    if(sis.hasNext()) {
                        setDefaultLogger();
                        log("Only one subject identifier was added to the topic as a subject locator!");
                        setState(WAIT);
                    }
                }
                catch(Exception e) {
                    singleLog(e);
                }
            }
        }
        
        
        else if(context instanceof LayeredTopicContext) {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            setDefaultLogger();
            try {
                log("Adding topics subject identifier to topic as a subject locator.");

                Collection subjectIdentifiers = null;
                Topic topic = null;
                Locator subjectLocator = null;
                String subjectLocatorString = null;
                Locator locator = null;
                ConfirmResult result = yes;
                int progress = 0;

                while(topics.hasNext() && !forceStop(result)) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            setProgress(progress++);

                            subjectIdentifiers = topic.getSubjectIdentifiers();
                            if(subjectIdentifiers.size() > 0) {
                                subjectLocator = (Locator) subjectIdentifiers.iterator().next();
                                if(subjectLocator != null) {
                                    subjectLocatorString = subjectLocator.toExternalForm();
                                    if(subjectLocatorString != null) {
                                        log("Adding topic '"+getTopicName(topic)+"' new subject locator\n"+subjectLocatorString +"");
                                        locator = new Locator(subjectLocatorString);
                                        if(result != yestoall) {
                                            result = TMBox.checkSubjectIdentifierChange(admin,topic,locator,true, true);
                                        }
                                        if(result == yes || result == yestoall) {
                                            topic.setSubjectLocator(locator);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
            catch (Exception e) {
                log(e);
            }
            setState(WAIT);
        }
    }
    
}
