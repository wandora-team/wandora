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
 * MakeSubjectIdentifierFromSubjectLocator.java
 *
 * Created on 22. toukokuuta 2006, 14:43
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import static org.wandora.application.gui.ConfirmResult.*;
import org.wandora.application.tools.*;

import java.util.*;


/**
 * <code>MakeSubjectIdentifierFromSubjectLocator</code> adds context topics new subject identifier equal
 * to topic's subject locator. If subject locator is not available, topic's
 * identifiers are not touched.
 *
 * @author akivela
 */
public class MakeSubjectIdentifierFromSubjectLocator extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;


	public MakeSubjectIdentifierFromSubjectLocator() {
    }
    public MakeSubjectIdentifierFromSubjectLocator(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Copy subject locator to subject identifier";
    }

    @Override
    public String getDescription() {
        return "Adds each topic new subject identifier identical to topic's subject locator.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        
        if(context instanceof ApplicationContext) {
            Iterator<Topic> topics = context.getContextObjects();
            try {
                while(topics.hasNext()) {
                    Topic topic = topics.next();
                    Locator subjectLocator = topic.getSubjectLocator();
                    if(subjectLocator != null) {
                        topic.addSubjectIdentifier(subjectLocator);
                    }
                }
            }
            catch(Exception e) {
                log(e);
            }
        }
        
        else {
            Iterator<Topic> topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            setDefaultLogger();
            try {
                log("Copying topic's subject locator to the topic as a subject identifier.");

                Topic topic = null;
                Locator subjectLocator = null;
                String subjectLocatorString = null;
                Locator locator = null;
                ConfirmResult result = yes;
                int progress = 0;
                int addCount = 0;

                while(topics.hasNext() && !forceStop(result)) {
                    try {
                        topic = topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            setProgress(progress++);
                            subjectLocator = topic.getSubjectLocator();
                            if(subjectLocator != null) {
                                subjectLocatorString = subjectLocator.toExternalForm();
                                if(subjectLocatorString != null) {
                                    log("Adding topic '"+getTopicName(topic)+"' new subject identifief:\n"+subjectLocatorString +"");
                                    locator = new Locator(subjectLocatorString);
                                    if(result != yestoall) {
                                        result = TMBox.checkSubjectIdentifierChange(wandora,topic,locator,true, true);
                                    }
                                    if(result == yes || result == yestoall) {
                                        addCount++;
                                        topic.addSubjectIdentifier(locator);
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                if(addCount == 0) {
                    log("No subject locators copied.");
                }
                else if(addCount == 1) {
                    log("Copied one subject locator.");
                }
                else {
                    log("Copied "+addCount+" subject locators.");
                }
            }
            catch (Exception e) {
                log(e);
            }
            log("Ready.");
            setState(WAIT);
        }
    }
}
