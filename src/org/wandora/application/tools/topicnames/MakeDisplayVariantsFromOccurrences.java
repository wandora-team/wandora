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
 */

package org.wandora.application.tools.topicnames;



import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;



/**
 *
 * @author akivela
 */
public class MakeDisplayVariantsFromOccurrences extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	boolean overWrite = false;
    
    /**
     * Creates a new instance of MakeDisplayVariantsWithOccurrences
     */
    public MakeDisplayVariantsFromOccurrences() {
    }
    public MakeDisplayVariantsFromOccurrences(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Make display variants with occurrences";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies occurrence datas to variant names.";
    }
    
  
    public void execute(Wandora wandora, Context context) {
        try {           
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String occurrence = null;
            String variant = null;
            
            Iterator<Topic> languageIterator = null;
            Topic language = null;
            Set<Topic> scope = null;
            Collection<Topic> languages = wandora.getTopicMap().getTopicsOfType(TMBox.LANGUAGE_SI);
            Topic displayScope = wandora.getTopicMap().getTopic(XTMPSI.DISPLAY);
            int progress = 0;
            
            Topic type = null;
            if(topics.hasNext()) {
                type = wandora.showTopicFinder("Select occurrence type...");
            }
            if(type == null) return;
            
            setDefaultLogger();
            setLogTitle("Copying occurrences to variant names");
            log("Copying occurrences to variant names");
            
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        if(languages != null) {
                            languageIterator = languages.iterator();
                            while(languageIterator.hasNext()) {
                                try {
                                    language = (Topic) languageIterator.next();
                                    occurrence = topic.getData(type, language);
                                    if(occurrence != null) {
                                        occurrence = occurrence.replace("\n", " ");
                                        scope = new LinkedHashSet<>();
                                        scope.add(language);
                                        scope.add(displayScope);
                                        variant = topic.getVariant(scope);
                                        if(variant == null || overWrite) {
                                            topic.setVariant(scope, occurrence);
                                        }
                                    }
                                }
                                catch(Exception e) {
                                    log(e);
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
        catch (Exception e) {
            log(e);
        }
    }
}