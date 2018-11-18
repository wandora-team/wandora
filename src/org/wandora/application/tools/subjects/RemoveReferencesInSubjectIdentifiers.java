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
 * RemoveReferencesInSubjectIdentifiers.java
 *
 * Created on 28. toukokuuta 2006, 21:15
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import java.util.*;




/**
 * <code>RemoveReferencesInSubjectIdentifiers</code> implements a tool that removes URL
 * reference of each subject identifier in context topics. URL reference of
 * subject identifier is a part of URL that begins with a hash (#) character.
 * Generally the URL reference is used to address anchors in web pages. This tool
 * can be used to merge topics referring same web pages for example.
 *
 * @author akivela
 */
public class RemoveReferencesInSubjectIdentifiers extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;


	public RemoveReferencesInSubjectIdentifiers() {
        setContext(new TopicContext());
    }
    public RemoveReferencesInSubjectIdentifiers(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public String getName() {
        return "SI reference (anchor) remover";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and removes references in SI URLs.";
    }
    
  
    public void execute(Wandora admin, Context context) {   
        setDefaultLogger();
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        try {
            log("Removing references in SIs.");

            Topic topic = null;
            Locator subjectLocator = null;
            String newSIString = null;
            String SIString = null;
            int progress = 0;
            Collection<Locator> sis = null;
            Locator l = null;
            int refPos = -1;

            ArrayList<Object> dt = new ArrayList<Object>();
            while(topics.hasNext() && !forceStop()) {
                dt.add(topics.next());
            }
            topics = dt.iterator();
            
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        sis = topic.getSubjectIdentifiers();
                        for(Iterator<Locator> siIterator = sis.iterator(); siIterator.hasNext(); ) {
                            l = (Locator) siIterator.next();
                            if(l != null) {
                                SIString = l.toExternalForm();
                                refPos = SIString.indexOf("#");
                                if(refPos > 0) {
                                    newSIString = SIString.substring(0, refPos);
                                    if(newSIString != null && !newSIString.equalsIgnoreCase(SIString)) {
                                        log("Changing SI '"+SIString+"' to\n'"+newSIString +"'.");
                                        topic.addSubjectIdentifier(new Locator(newSIString));
                                        topic.removeSubjectIdentifier(l);
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
