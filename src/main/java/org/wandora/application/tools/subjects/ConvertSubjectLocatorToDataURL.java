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
 */
package org.wandora.application.tools.subjects;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */
public class ConvertSubjectLocatorToDataURL extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private Iterator<Topic> topicsToProcess;
    
    
    public ConvertSubjectLocatorToDataURL() {}
    public ConvertSubjectLocatorToDataURL(Context context) {
        setContext(context);
    }
    public ConvertSubjectLocatorToDataURL(Collection<Topic> topics) {
        topicsToProcess = topics.iterator();
    }
    public ConvertSubjectLocatorToDataURL(Iterator<Topic> topics) {
        topicsToProcess = topics;
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        setDefaultLogger();
        
        if(topicsToProcess == null) {
            topicsToProcess = context.getContextObjects();
        }
        if(topicsToProcess != null) {
            try {
                setLogTitle("Converting subject locators to data urls.");
                Topic topic = null;
                int convertCount = 0;

                while(topicsToProcess.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topicsToProcess.next();
                        if(topic != null && !topic.isRemoved()) {
                            Locator subjectLocator = topic.getSubjectLocator();
                            if(subjectLocator != null) {
                                String subjectLocatorString = subjectLocator.toExternalForm();
                                if(subjectLocatorString.startsWith("file")) {
                                    DataURL dataUrl = new DataURL(new File(new URL(subjectLocatorString).toURI()));
                                    topic.setSubjectLocator(new Locator(dataUrl.toExternalForm()));
                                    convertCount++;
                                }
                                else if(subjectLocatorString.startsWith("http")) {
                                    DataURL dataUrl = new DataURL(new URL(subjectLocatorString));
                                    topic.setSubjectLocator(new Locator(dataUrl.toExternalForm()));
                                    convertCount++;
                                }
                                else if(subjectLocatorString.startsWith("data:")) {
                                    // Do nothing. Subject locator is a data url already. 
                                }
                                else {
                                    
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        log(e);
                    }
                    setLogTitle("Converting subject locators to data urls. Converted " + convertCount + " locators...");
                }
                log("Converted " + convertCount + " subject locators.");
            }
            catch (Exception e) {
                log(e);
            }
            setState(WAIT);
            topicsToProcess = null;
        }
    }
    
    
    
    
    @Override
    public String getName() {
        return "Convert subject locators to data urls";
    }
    

    @Override
    public String getDescription() {
        return "Convert subject locators to data urls.";
    }
    
    
    
    @Override
    public boolean requiresRefresh() {
        return true;
    }

    
}
