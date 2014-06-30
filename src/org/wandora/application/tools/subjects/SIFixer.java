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
 * SIFixer.java
 *
 * Created on 6. tammikuuta 2005, 13:38
 */

package org.wandora.application.tools.subjects;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import static org.wandora.application.gui.ConfirmResult.*;
import org.wandora.*;

import java.util.*;



/**
 * <code>SIFixer</code> iterates through all subject identifiers in context
 * topics and replaces all invalid characters in SI's with underline
 * character (_). Invalid characters in subject identifiers may cause
 * problems if subject identifiers are used to aquire topics within
 * web application.
 *
 * Subject identifier changes may cause topic merges.
 *
 * @author  akivela
 */
public class SIFixer extends AbstractWandoraTool implements WandoraTool {
    
    boolean quiet = false;

    public SIFixer() {
    }
    public SIFixer(Context context) {
        setContext(context);
    }
    

    @Override
    public String getName() {
        return "SI Fixer";
    }

    @Override
    public String getDescription() {
        return "Fixes all subject identifiers of context topics. "+
               "Fix includes illegal character removal.";
    }
    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Iterator contextTopics = context.getContextObjects();
        if(contextTopics != null && contextTopics.hasNext()) {
            if(WandoraOptionPane.showConfirmDialog(admin, "Are you sure you want to clean subject identifiers?","Confirm SI clean", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){
                setDefaultLogger();
                setLogTitle("Cleaning SIs");
                Topic topic = null;
                Locator nl = null;
                Locator l = null;
                Collection sis = null;
                ConfirmResult result = yes;
                int progress = 0;

                ArrayList<Object> dt = new ArrayList<Object>();
                while(contextTopics.hasNext() && !forceStop()) {
                    dt.add(contextTopics.next());
                }
                contextTopics = dt.iterator();
                        
                while(contextTopics.hasNext() && !forceStop(result)) {
                    try {
                        topic = (Topic) contextTopics.next();
                        if(topic != null  && !topic.isRemoved()) {
                            setProgress(progress++);
                            sis = topic.getSubjectIdentifiers();
                            if(sis.isEmpty()) {
                                log("Adding SI " + nl.toExternalForm());
                                topic.addSubjectIdentifier(TopicTools.createDefaultLocator());
                            }
                            else {
                                for(Iterator siIterator = sis.iterator(); siIterator.hasNext(); ) {
                                    l = (Locator) siIterator.next();
                                    if(l != null) {
                                        hlog("Investigating SI\n"+l.toExternalForm());
                                        if(TopicTools.isDirtyLocator(l)) {
                                            nl = fixSI(topic, l, admin);
                                            if(nl != null) {
                                                log("Fixed SI\n" + nl.toExternalForm());
                                            }
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
                setState(WAIT);
            }
        }
    }
                    

    
    
    public Locator fixSI(Topic topic, Locator l, Wandora admin)  throws TopicMapException {
        boolean addNewSI = true;
        boolean removeOldSI = true;
        Locator newl = TopicTools.cleanDirtyLocator(l);

        int counter = 1;
        Topic anotherTopic = topic.getTopicMap().getTopic(newl);
        while(anotherTopic != null && !anotherTopic.equals(topic)) {
            String newls = newl.toExternalForm();
            newl = new Locator(newls + "_" + counter);
            anotherTopic = topic.getTopicMap().getTopic(newl);
        }
        //System.out.println("new si: "+newl);
        //System.out.println("old si: "+l);
        if(!newl.equals(l)) {
            topic.addSubjectIdentifier(newl);
            topic.removeSubjectIdentifier(l);
        }
        return newl;
    }

    
}
