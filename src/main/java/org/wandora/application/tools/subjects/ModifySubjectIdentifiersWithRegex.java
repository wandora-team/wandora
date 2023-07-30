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
 * ModifySubjectIdentifiersWithRegex.java
 *
 * Created on 19. toukokuuta 2006, 13:48
 *
 */

package org.wandora.application.tools.subjects;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.TopicContext;
import org.wandora.application.gui.RegularExpressionEditor;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;


/**
 *
 * @author akivela
 */
public class ModifySubjectIdentifiersWithRegex extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	RegularExpressionEditor editor = null;

    

    public ModifySubjectIdentifiersWithRegex() {
        setContext(new TopicContext());
    }
    public ModifySubjectIdentifiersWithRegex(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    @Override
    public String getName() {
        return "SI regular expression replacer";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and applies given regular expression to subject identifiers.";
    }
    
  
    public void execute(Wandora admin, Context context) {   
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        try {
            editor = RegularExpressionEditor.getReplaceExpressionEditor(admin);
            editor.approve = false;
            editor.setVisible(true);
            if(editor.approve == true) {

                setDefaultLogger();
                setLogTitle("SI regex replacer");
                log("Transforming subject identifiers with regular expression.");

                Topic topic = null;
                String newSIString = null;
                String SIString = null;
                int progress = 0;
                int changed = 0;
                Collection<Locator> sis = null;
                Locator l = null;
                
                ArrayList<Locator> lv = null;
                Iterator<Locator> it = null;

                ArrayList<Object> dt = new ArrayList<Object>();
                while(topics.hasNext() && !forceStop()) {
                    dt.add(topics.next());
                }
                topics = dt.iterator();
                
                while(topics.hasNext() && !forceStop()) {
                    try {
                        progress++;
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            sis = topic.getSubjectIdentifiers();
                            
                            // First copy sis to safe vector
                            lv = new ArrayList<Locator>();
                            it=sis.iterator();
                            while(it.hasNext()) {
                                l = (Locator) it.next();
                                if(l != null) {
                                    lv.add(l);
                                }
                            }
                            int s = lv.size();
                            
                            // Them iterate through sis and do what is supposed
                            for(int i=0; i<s; i++) {
                                l = lv.get(i);
                                if(l != null) {
                                    SIString = l.toExternalForm();
                                    newSIString = editor.replace(SIString);
                                    if(newSIString != null && !newSIString.equalsIgnoreCase(SIString)) {
                                        log("Applying regular expression. New SI is '"+newSIString + "'.");
                                        topic.addSubjectIdentifier(new Locator(newSIString));
                                        topic.removeSubjectIdentifier(l);
                                        changed++;
                                    }
                                    else {
                                        hlog("Investigating SI '" + SIString + "'.");
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                log("Total " + changed + " SIs changed.");
            }
        }
        catch (Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    

}
