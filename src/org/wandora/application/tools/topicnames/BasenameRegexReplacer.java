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
 * BasenameRegexReplacer.java
 *
 * Created on 19. toukokuuta 2006, 12:51
 */

package org.wandora.application.tools.topicnames;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;

import java.util.*;



/**
 * This is a WandoraAdminTool used to modify context topic base names with
 * a given regular expression. Used regular expression is given with the
 * <code>RegularExpressionEditor</code>.
 * 
 * 
 * @author akivela
 */
public class BasenameRegexReplacer extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	RegularExpressionEditor editor = null;

 

    public BasenameRegexReplacer() {
    }
    public BasenameRegexReplacer(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Base name regular expression replacer";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and applies given regular expression to base names.";
    }
    
  
    public void execute(Wandora wandora, Context context) {
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;
        try {
            editor = RegularExpressionEditor.getReplaceExpressionEditor(wandora);
            editor.approve = false;
            editor.setVisible(true);
            if(editor.approve == true) {
                setDefaultLogger();
                
                setLogTitle("Base name regex replacer");
                log("Transforming base names with regular expression.");

                Topic topic = null;
                String basename = null;
                String newBasename = null;
                int progress = 0;
                int count = 0;

                HashMap<Topic,String> changeTopics = new HashMap<Topic,String>();

                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            basename = topic.getBaseName();
                            if(basename != null) {
                                progress++;
                                hlog("Investigating base name '" + basename + "'.");
                                newBasename = editor.replace(basename);
                                if(newBasename != null && !basename.equalsIgnoreCase(newBasename)) {
                                    changeTopics.put(topic, newBasename);
                                    log("Regex matches! New base name is '"+newBasename +"'.");
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
                    if(forceStop()) break;
                    newBasename = changeTopics.get(t);
                    t.setBaseName(newBasename);
                    count++;
                }

                log("Total "+count+" base names changed!");
            }
        }
        catch (Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    

}
