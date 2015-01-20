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
 * BasenameRemover.java
 *
 * Created on 25. toukokuuta 2006, 11:17
 *
 */

package org.wandora.application.tools.topicnames;



import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;




/**
 * Although Wandora relies that a topic has a basename and it is not
 * encouraged to create a topic without a basename, it may sometimes be
 * necessary to remove topic's basename. Removing may be useful for example
 * if the basename is later filled with subject identifier or occurrence string.
 * This class implements a tool used to remove basenames of context topics.
 *
 * @author akivela
 */


public class BasenameRemover extends AbstractWandoraTool implements WandoraTool {

    

    public BasenameRemover() {
    }
    public BasenameRemover(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Topic base name remover";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and removes all base names.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            
            if(WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to remove all base names of selected topics?","Confirm base name remove", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){

                setDefaultLogger();
                setLogTitle("Removing all base names");
                log("Removing all base names");

                Topic topic = null;
                int progress = 0;

                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            progress++;
                            topic.setBaseName(null);
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
                log("Done.");
                setState(WAIT);
            }
        }
        catch (Exception e) {
            log(e);
        }
    }
    
 
}
