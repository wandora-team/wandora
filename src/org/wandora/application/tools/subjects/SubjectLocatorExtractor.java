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
 * SubjectLocatorExtractor.java
 *
 * Created on 12. lokakuuta 2007, 14:45
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.application.tools.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;

import java.util.*;


/**
 *
 * @author akivela
 */
public class SubjectLocatorExtractor extends AbstractWandoraTool implements WandoraTool, Runnable {
    private AbstractExtractor enclosedExtractor = null;
    
    
    /** Creates a new instance of SubjectLocatorExtractor */
    public SubjectLocatorExtractor(AbstractExtractor extractor) {
        enclosedExtractor = extractor;
    }
    public SubjectLocatorExtractor(AbstractExtractor extractor, Context proposedContext) {
        enclosedExtractor = extractor;
        setContext(proposedContext);
    }
    

    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            log("Extracting with subject locator urls!");
            if(wandora != null) {
                Iterator contextTopics = getContext().getContextObjects();
                int openCount = 0;
                boolean openAll = false;
                boolean openNext = true;
                boolean forceStop = false;
                ArrayList<String> sls = new ArrayList<String>(); 
                
                // ***** Collection urls in context topics *****
                if(contextTopics == null) return;
                while(contextTopics.hasNext() && !forceStop) {
                    Topic t = (Topic) (contextTopics.next());
                    Locator sl = t.getSubjectLocator();
                    if(sl != null) {
                        String slString = sl.toExternalForm();
                        if(slString != null) sls.add(slString);
                    }
                    else {
                        log("No valid subject locator in topic '"+getTopicName(t)+"'.");
                    }
                }
                // ***** Extracting urls resources *****
                if(sls.size() > 0) {
                    log("Found total " + sls.size() + " urls for extraction.");
                    enclosedExtractor.setForceUrls(sls.toArray(new String[] {}));
                    enclosedExtractor.setContext(context);
                    enclosedExtractor.setToolLogger(this.getCurrentLogger());
                    enclosedExtractor.execute(wandora, context.getContextEvent());
                }
                else {
                    log("Context didn't contain valid subject identifiers.");
                }
            }
            else {
                log("Error: Wandora administrator application is not available!");
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
    
    @Override
    public String getName() {
        if(enclosedExtractor != null) {
            return enclosedExtractor.getName();
        }
        else {
            return "Subject locator extractor";
        }
    }
    
    
    @Override
    public String getDescription() {
        if(enclosedExtractor != null) {
            return enclosedExtractor.getDescription();
        }
        else {
            return "Subject locator extractor";
        }
    }
    
}
