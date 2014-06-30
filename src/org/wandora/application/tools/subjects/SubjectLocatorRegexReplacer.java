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
 * SubjectLocatorRegexReplacer.java
 *
 * Created on 19. toukokuuta 2006, 13:34
 *
 */

package org.wandora.application.tools.subjects;



import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import java.util.*;



/**
 * Tool class iterates through given context topics and modifies subject locators
 * with given regular expression. Tool execution may result topic merges.
 *
 * @author akivela
 */
public class SubjectLocatorRegexReplacer extends AbstractWandoraTool implements WandoraTool {
    RegularExpressionEditor editor = null;

    
    /** Creates a new instance of SubjectLocatorRegexReplacer */
    public SubjectLocatorRegexReplacer() {
    }
    public SubjectLocatorRegexReplacer(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Subject locator regular expression replacer";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and applies given regular expression to subject locators.";
    }
    
  
    @Override
    public void execute(Wandora admin, Context context) {   
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        try {
            editor = RegularExpressionEditor.getReplaceExpressionEditor(admin);
            editor.approve = false;
            editor.setVisible(true);
            if(editor.approve == true) {

                setDefaultLogger();
                setLogTitle("Subject locator regex replacer");
                log("Transforming subject locators with regular expression.");

                Topic topic = null;
                Locator subjectLocator = null;
                String newSubjectLocatorString = null;
                String subjectLocatorString = null;
                int progress = 0;
                int changed = 0;

                ArrayList<Object> dt = new ArrayList<Object>();
                while(topics.hasNext() && !forceStop()) {
                    dt.add(topics.next());
                }
                topics = dt.iterator();
                
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        progress++;
                        if(topic != null && !topic.isRemoved()) {
                            subjectLocator = topic.getSubjectLocator();
                            if(subjectLocator != null) {
                                subjectLocatorString = subjectLocator.toExternalForm();
                                newSubjectLocatorString = editor.replace(subjectLocatorString);
                                if(newSubjectLocatorString != null && !newSubjectLocatorString.equalsIgnoreCase(subjectLocatorString)) {
                                    log("Applying regular expression. New subject locator is '"+newSubjectLocatorString +"'.");
                                    topic.setSubjectLocator(new Locator(newSubjectLocatorString));
                                    changed++;
                                }
                                else {
                                    hlog("Investigating subject locator '" + subjectLocatorString +"'.");
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                log("Total "+changed+" subject locators changed!");
            }
        }
        catch (Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    

}
