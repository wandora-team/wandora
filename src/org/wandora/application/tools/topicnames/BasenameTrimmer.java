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
 * BasenameNewlineRemover.java
 *
 * Created on 19. toukokuuta 2006, 12:16
 *
 */

package org.wandora.application.tools.topicnames;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import java.util.*;



/**
 * This is a Tool used to remove surrounding white space characters in basenames.
 *
 * @author akivela
 */
public class BasenameTrimmer extends AbstractWandoraTool implements WandoraTool {


    public BasenameTrimmer() {
    }
    public BasenameTrimmer(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Basename trimmer";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and removes surrounding white space characters in basenames.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Removing surrounding white space characters in basenames");
            log("Removing surrounding white space characters in basenames");
            
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String basename = null;
            String newBasename = null;
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
                            hlog("Investigating topic '" + basename + "'.");
                            newBasename = basename.trim();
                            if(!basename.equals(newBasename)) {
                                changeTopics.put(topic, newBasename);
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }

            ArrayList<Topic> changeList = new ArrayList();
            changeList.addAll(changeTopics.keySet());
            
            for(Topic t : changeList) {
                try {
                    if(forceStop()) break;
                    newBasename = changeTopics.get(t);
                    t.setBaseName(newBasename);
                    log("Changed base name to '"+newBasename + "'.");
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
