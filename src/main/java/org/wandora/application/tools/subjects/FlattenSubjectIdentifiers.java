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
 * FlattenSubjectIdentifiers.java
 *
 * Created on 6. tammikuuta 2005, 13:38
 */

package org.wandora.application.tools.subjects;

import static org.wandora.application.gui.ConfirmResult.yes;
import static org.wandora.application.gui.ConfirmResult.yestoall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.TopicContext;
import org.wandora.application.gui.ConfirmResult;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;



/**
 * Playing with topics and subject identifiers gradually accumulates
 * subject identifiers. This slows down the topic map implementation little
 * by little. <code>FlattenSubjectIdentifiers</code> offers very brutal solution to the
 * accumulation problem. Class implements a tool that removes all
 * but one subject identifier of context topics. User can not decide which
 * subject identifier remains which is usually undesirable.
 * 
 * 
 * @author akivela
 */
public class FlattenSubjectIdentifiers extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;


	public FlattenSubjectIdentifiers() {
        setContext(new TopicContext());
    }
    public FlattenSubjectIdentifiers(Context context) {
        setContext(context);
    }
    

    @Override
    public String getName() {
        return "Flatten subject identifiers";
    }

    @Override
    public String getDescription() {
        return "Deletes all but one subject identifier of context topics.";
    }


    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        Iterator contextTopics = getContext().getContextObjects();
        if(contextTopics == null || !contextTopics.hasNext()) return;
        
        if(WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to delete all but one subject identifiers of context topics?","Delete subject identifiers", WandoraOptionPane.YES_NO_OPTION)== WandoraOptionPane.YES_OPTION){
            setDefaultLogger();
            Topic topic = null;
            Collection<Locator> tsis = null;
            List<Locator >lv = null;
            Locator l = null;
            Iterator<Locator> it = null;
            ConfirmResult result = yes;
            int progress = 0;
            int sc = 0;
            int tc = 0;
            int ic = 0;
            
            while(contextTopics.hasNext() && !forceStop(result)) {
                topic = (Topic) contextTopics.next();
                if(topic != null && !topic.isRemoved()) {
                    setProgress(progress++);
                    hlog("Investigating topic '" + getTopicName(topic) + "'.");
                    ic++;
                    tsis=topic.getSubjectIdentifiers();
                    if(tsis.size() > 1) {
                        try {
                            tc++;
                            lv = new ArrayList<>();
                            it=tsis.iterator();
                            it.next(); // Hop over == save first locator
                            while(it.hasNext()) {
                                l = (Locator) it.next();
                                if(l != null) {
                                    lv.add(l);
                                }
                            }
                            for(int i=0; i<lv.size() && !forceStop(result); i++) {
                                try {
                                    l = (Locator) lv.get(i);
                                    if(l != null) {
                                        if(result != yestoall) {
                                            result = TMBox.checkSubjectIdentifierChange(wandora,topic,l,false, true);
                                        }
                                        if(result == yes || result == yestoall) {
                                            topic.removeSubjectIdentifier(l);
                                            hlog("Removing subject identifier '" + l.toExternalForm()+"'.");
                                            sc++;
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    log(e);
                                }
                            }
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                }
            }
            log("Inspected "+ic+" topics.");
            log("Flattened "+tc+" topics.");
            log("Removed "+sc+" subject identifiers.");
            log("Ready.");
            setState(WAIT);
        }
    }
    

}
