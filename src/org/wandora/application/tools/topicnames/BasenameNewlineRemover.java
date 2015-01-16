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
 * This is a WandoraAdminTool used to remove new line characters in base names.
 * Current Wandora GUI represent topics using their base names and multiline
 * representation is rarely available due to used GUI elements. As a result
 * multiline base name is partially invisible in Wandora.
 *
 * @author akivela
 */
public class BasenameNewlineRemover extends AbstractWandoraTool implements WandoraTool {


    public BasenameNewlineRemover() {
    }
    public BasenameNewlineRemover(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Base name newline remover";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and removes new line characters in base names.";
    }
    
  
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Removing newline characters in base names");
            log("Removing newline characters in base names");
            
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
                            if(basename.indexOf("\n") != -1 || basename.indexOf("\r") != -1) {
                                newBasename = basename.replaceAll("\r", "");
                                newBasename = newBasename.replaceAll("\n", "");
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
