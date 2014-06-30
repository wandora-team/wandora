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
 * MakeSIWithSL.java
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
import org.wandora.*;

import java.util.*;


/**
 * <code>MakeSIWithSL</code> adds context topics new subject identifier equal
 * to topic's subject locator. If subject locator is not available topic's
 * identifiers are not touched.
 *
 * @author akivela
 */
public class MakeSIWithSL extends AbstractWandoraTool implements WandoraTool {

    public MakeSIWithSL() {
    }
    public MakeSIWithSL(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Make subject identifier with subject lcoator";
    }

    @Override
    public String getDescription() {
        return "Adds each topic new subject identifier identical to topic's subject locator.";
    }
    
  
    @Override
    public void execute(Wandora admin, Context context) {   
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        setDefaultLogger();
        try {
            log("Adding topics a subject identifier identical to topic's subject locator.");

            Topic topic = null;
            Locator subjectLocator = null;
            String subjectLocatorString = null;
            Locator locator = null;
            ConfirmResult result = yes;
            int progress = 0;
            int addCount = 0;
            
            while(topics.hasNext() && !forceStop(result)) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        subjectLocator = topic.getSubjectLocator();
                        if(subjectLocator != null) {
                            subjectLocatorString = subjectLocator.toExternalForm();
                            if(subjectLocatorString != null && !subjectLocatorString.equalsIgnoreCase(subjectLocatorString)) {
                                log("Adding topic '"+getTopicName(topic)+"' new subject identifief:\n"+subjectLocatorString +"");
                                locator = new Locator(subjectLocatorString);
                                if(result != yestoall) {
                                    result = TMBox.checkSubjectIdentifierChange(admin,topic,locator,true, true);
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
                log("No subject identifiers added to topics.");
            }
            else {
                log("Added "+addCount+" subject identifiers to topics.");
            }
        }
        catch (Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
}
