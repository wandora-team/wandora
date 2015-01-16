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
 * SubjectLocatorChecker.java
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
 * Tool checks if subject locators of context topics exists i.e. refer to existing
 * web resources. Tool produces textual report of missing, existing and invalid
 * subject locators.
 *
 * @author  akivela
 */
public class SubjectLocatorChecker extends AbstractWandoraTool implements WandoraTool {

    String urlExists = "EXISTS\t'<locator/>'";
    String urlDoesntExists = "MISSING\t'<locator/>'";
    String noSubjectLocator = "NO SL\t'<topic/>'.";
    String illegalSubjectLocator = "INVALID\t'<topic/>'.";
    String topicError = "TOPIC ERROR\t'<topic/>'.";

    
    /**
     * <code>reportType</code> contains characters defining tool
     * generated reports. Possible report characters are:
     *
     * m = report missing sl
     * s = report existing sl (successful)
     * i = report invalid sl (exception occurred)
     * n = report when topic has no sl
     * e = report topic errors (topic == null || exceptions)
     */
    String reportType = "mi";

    File currentDirectory = null;
    Iterator topicsToCheck;
    
    
    public SubjectLocatorChecker() {}
    public SubjectLocatorChecker(Context context) {
        setContext(context);
    }
    public SubjectLocatorChecker(Collection topics) {
        topicsToCheck = topics.iterator();
    }
    public SubjectLocatorChecker(Iterator topics) {
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
                setLogTitle("Check subject locators of topics...");
                Topic topic = null;
                int valid = 0;
                int invalid = 0;
                int total = 0;
                int nosl = 0;
                int r = 0;

                while(topicsToCheck.hasNext() && !forceStop()) {
                    try {
                        total++;
                        topic = (Topic) topicsToCheck.next();
                        if(topic != null && !topic.isRemoved()) {
                            r = checkSubjectLocator(topic);
                            if(r == 1) valid++;
                            if(r == -1) invalid++;
                            if(r == 0) nosl++;
                        }
                    }
                    catch (Exception e) {
                        log(e);
                    }
                    setLogTitle("Check subject locators in topics... " + total);
                }
                log("Total " + total + " topics checked.");
                log(nosl + " topics had no subject locator.");
                log((total-nosl) + " topics had subject locator.");
                log(valid + " valid and existing subject locators found.");
                log(invalid + " invalid or missing subject locators found.");
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
        return "Check Subject Locators";
    }
    

    @Override
    public String getDescription() {
        return "Tool checks if subject locators of context topics really exists.";
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
            Locator l = t.getSubjectLocator();
            if(l != null) {
                try {
                    URL SubjectUrl = new URL(l.toExternalForm());
                    if(IObox.urlExists(SubjectUrl)) {
                        if(reportAbout('s')) {
                            log(urlExists.replaceAll("<locator/>", SubjectUrl.toExternalForm()));
                        }
                        return 1;
                    }
                    else {
                        if(reportAbout('m')) {
                            log(urlDoesntExists.replaceAll("<locator/>", SubjectUrl.toExternalForm()));
                        }
                        return -1;
                    }
                }
                catch (Exception e) {
                    if(reportAbout('i')) {
                        log(illegalSubjectLocator.replaceAll("<topic/>", TopicToString.toString(t)));
                        log("\t"+e.toString());
                        return -1;
                    }
                }
            }
            else {
                if(reportAbout('n')) { 
                    log(noSubjectLocator.replaceAll("<topic/>", TopicToString.toString(t))); 
                }
            }
        }
        else {
            if(reportAbout('e')) { 
                log(noSubjectLocator.replaceAll("<topic/>", "TOPIC IS NULL")); 
            }
        }
        return 0;
    }

    
    
    
    private boolean reportAbout(char reportCode) {
        return ( reportType.indexOf(reportCode) != -1 );
    }
    

}
