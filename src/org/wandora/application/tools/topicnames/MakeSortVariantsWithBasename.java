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
 * MakeDisplayVariantsWithBasename.java
 *
 * Created on 22. toukokuuta 2006, 14:55
 *
 */

package org.wandora.application.tools.topicnames;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;




/**
 * <code>MakeDisplayVariantsWithBasename</code> copies topics base name to
 * topic's variant names. If topic already contain variant name, boolean
 * variable <code>overWrite</code> defines if the existing variant name
 * is over written. Tool solves available variant name scopes and types using
 * <code>WandoraAdminManager</code>. Only names with available scopes and types
 * are set.
 * 
 * 
 * @author akivela
 */



public class MakeSortVariantsWithBasename extends AbstractWandoraTool implements WandoraTool {
    boolean overWrite = false;
    
    /**
     * Creates a new instance of MakeSortVariantsWithBasename
     */
    public MakeSortVariantsWithBasename() {
    }
    public MakeSortVariantsWithBasename(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Makes sort variant names using topic's base name.";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies topic's base name to variant names.";
    }
    
  
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Copying base name to topic variant names");
            log("Copying base name to topic variant names");
            
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String basename = null;
            String variant = null;
            
            Iterator languageIterator = null;
            Topic language = null;
            Set<Topic> scope = null;
            Collection languages = wandora.getTopicMap().getTopicsOfType(TMBox.LANGUAGE_SI);
            Topic displayScope = wandora.getTopicMap().getTopic(XTMPSI.SORT);
            int progress = 0;
            
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        basename = topic.getBaseName();
                        if(languages != null) {
                            languageIterator = languages.iterator();
                            while(languageIterator.hasNext()) {
                                try {
                                    language = (Topic) languageIterator.next();
                                    scope = new HashSet();
                                    scope.add(language);
                                    scope.add(displayScope);
                                    variant = topic.getVariant(scope);
                                    if(variant == null || overWrite) {
                                        topic.setVariant(scope, basename);
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
            log("OK.");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    

}
