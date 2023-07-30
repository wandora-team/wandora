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
 */

package org.wandora.application.tools.topicnames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;



/**
 *
 * @author akivela
 */
public class BasenameWhiteSpaceCollapser extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;


	public BasenameWhiteSpaceCollapser() {
    }
    public BasenameWhiteSpaceCollapser(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Base name white space collapser";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and collapses white spaces in topics' base names";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Collapsing white space characters in base names");
            log("Collapsing white space characters in base names");
            
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String basename = null;
            StringBuffer newBasename = null;
            int c = 0;
            int changed = 0;
            int progress = 0;
            HashMap<Topic,String> changeTopics = new HashMap<Topic,String>();
            
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    c++;
                    if(topic != null) {
                        basename = topic.getBaseName();
                        if(basename != null) {
                            progress++;
                            log("Investigating topic '" + basename + "'.");
                            newBasename = new StringBuffer();
                            char ch = 0;
                            boolean isFirst = true;
                            boolean hasChanged = false;
                            for(int i=0; i<basename.length(); i++) {
                                ch = basename.charAt(i);
                                if(Character.isSpaceChar(ch)) {
                                    if(isFirst) {
                                        newBasename.append(ch);
                                        isFirst = false;
                                    }
                                    else {
                                        hasChanged = true;
                                    }
                                }
                                else {
                                    newBasename.append(ch);
                                    isFirst = true;
                                }
                            }
                            
                            if(hasChanged) {
                                changeTopics.put(topic, newBasename.toString());
                                log("Changing base name to '"+newBasename + "'.");
                                changed++;
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            
            List<Topic> changeList = new ArrayList<>();
            changeList.addAll(changeTopics.keySet());

            for(Topic t : changeList) {
                try {
                    if(forceStop()) break;
                    basename = changeTopics.get(t);
                    t.setBaseName(basename);
                    changed++;
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
