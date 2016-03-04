/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * CheckSubjectLocator.java
 *
 * Created on November 4, 2004, 5:48 PM
 */

package org.wandora.application.tools.subjects;



import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.*;
import org.wandora.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;



/**
 * Checks if subject locators resolves. Produces textual report of resolving, 
 * non-resolving, datauri and missing subject locators.
 *
 * @author  akivela
 */

public class CheckSubjectLocator extends AbstractWandoraTool implements WandoraTool {

    
    /**
     * <code>reportType</code> contains characters defining tool
     * generated reports. Possible report characters are:
     *
     * m = report missing subject locator
     * s = report valid subject locators (resource found)
     * d = report datauri subject locators
     * i = report invalid subject locators (resource not found, exception occurred)
     * n = report when topic has no subject locator
     * e = report topic errors (topic == null || exceptions)
     */
    String reportType = "mid";

    private File currentDirectory = null;
    private Iterator topicsToCheck;
    
    
    public CheckSubjectLocator() {}
    public CheckSubjectLocator(Context context) {
        setContext(context);
    }
    public CheckSubjectLocator(Collection topics) {
        topicsToCheck = topics.iterator();
    }
    public CheckSubjectLocator(Iterator topics) {
        topicsToCheck = topics;
    }
    
    
    
    @Override
    public void execute(Wandora admin, Context context) {
        setDefaultLogger();
        
        if(topicsToCheck == null) {
            topicsToCheck = context.getContextObjects();
        }
        if(topicsToCheck != null) {
            try {
                setLogTitle("Checking subject locators");
                Topic topic = null;
                int resourceFoundCounter = 0;
                int dataUrlFoundCounter = 0;
                int resourceNotFoundCounter = 0;
                int topicsCheckedCount = 0;
                int noSubjectLocatorCounter = 0;
                int r = 0;

                while(topicsToCheck.hasNext() && !forceStop()) {
                    try {
                        topicsCheckedCount++;
                        topic = (Topic) topicsToCheck.next();
                        if(topic != null && !topic.isRemoved()) {
                            r = checkSubjectLocator(topic);
                            if(r == 1 || r == 2) resourceFoundCounter++;
                            if(r == 2) dataUrlFoundCounter++;
                            if(r == -1) resourceNotFoundCounter++;
                            if(r == 0) noSubjectLocatorCounter++;
                        }
                    }
                    catch (Exception e) {
                        log(e);
                    }
                    setLogTitle("Checking subject locators. Total " + topicsCheckedCount + " topics checked...");
                }
                log("Total " + topicsCheckedCount + " topics checked.");
                log((topicsCheckedCount-noSubjectLocatorCounter) + " topics had subject locator.");
                log(resourceFoundCounter + " subject locator resources found.");
                log(dataUrlFoundCounter + " datauri subject locators found.");
                log(resourceNotFoundCounter + " subject locator resources missing or invalid.");
                log(noSubjectLocatorCounter + " topics had no subject locator.");
            }
            catch (Exception e) {
                log(e);
            }
            setState(WAIT);
            topicsToCheck = null;
        }
    }
    
    
    
    
    @Override
    public String getName() {
        return "Check subject locators";
    }
    

    @Override
    public String getDescription() {
        return "Checks if subject locators resolve.";
    }
    
    
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }

    
    /*
    public void actionPerformed(ActionEvent event) {
        String com = event.getActionCommand();
        if("closelog".equalsIgnoreCase(com)) {
            logFrame.setVisible(false);
            logFrame = null;
        }
    }
     * */
    
    
    
    

    public int checkSubjectLocator(Topic t)  throws TopicMapException {
        if(t != null) {
            String topicName = TopicToString.toString(t);
            Locator l = t.getSubjectLocator();
            String locatorString = l.toExternalForm();
            if(l != null) {
                try {
                    if(DataURL.isDataURL(locatorString)) {
                        if(reportAbout('d')) {
                            log("'"+topicName+"' - found datauri subject locator.");
                        }
                        return 2;
                    }
                    else {
                        URL subjectUrl = new URL(locatorString);
                        if(IObox.urlExists(subjectUrl)) {
                            if(reportAbout('s')) {
                                log("'"+topicName+"' - found subject locator resource "+locatorString);
                            }
                            return 1;
                        }
                        else {
                            if(reportAbout('m')) {
                                log("'"+topicName+"' - unable to resolve subject locator resource "+locatorString);
                            }
                            return -1;
                        }
                    }
                }
                catch (Exception e) {
                    if(reportAbout('i')) {
                        log("'"+topicName+"' - invalid url used as subject locator: "+locatorString);
                        log("\t"+e.toString());
                        return -1;
                    }
                }
            }
            else {
                if(reportAbout('n')) { 
                    log("'"+topicName+"' - has no subject locator at all.");
                }
            }
        }
        else {
            if(reportAbout('e')) { 
                log("INVALID TOPIC.");
            }
        }
        return 0;
    }

    
    
    
    private boolean reportAbout(char reportCode) {
        return ( reportType.indexOf(reportCode) != -1 );
    }
    

}
