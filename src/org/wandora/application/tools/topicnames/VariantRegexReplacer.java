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
 * VariantRegexReplacer.java
 *
 * Created on 22. toukokuuta 2006, 14:31
 *
 */

package org.wandora.application.tools.topicnames;


import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.util.*;


/**
 * <code>VariantRegexReplacer</code> implements tool modifying topic's
 * variant names with given regular expression. Regular expression is
 * applied to every variant name available in the topic. Regular expression
 * can be used to carry out complex name replace operations for example.
 *
 * @author akivela
 */


public class VariantRegexReplacer extends AbstractWandoraTool implements WandoraTool {
    RegularExpressionEditor editor = null;
 
    
    /** Creates a new instance of VariantRegexReplacer */
    public VariantRegexReplacer() {
        setContext(new TopicContext());
    }
    public VariantRegexReplacer(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Variant regular expression replacer";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and applies given regular expression to variant names.";
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
                log("Transforming variant names with regular expression.");

                Topic topic = null;
                String variant = null;
                String newVariant = null;

                Collection scopes = null;
                Iterator scopeIterator = null;
                Set<Topic> scope = null;
                int progress = 0;
                int count = 0;
                
                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            progress++;
                            scopes = topic.getVariantScopes();
                            if(scopes != null) {
                                scopeIterator = scopes.iterator();
                                while(scopeIterator.hasNext()) {
                                    try {
                                        scope = (Set<Topic>) scopeIterator.next();
                                        variant = topic.getVariant(scope);
                                        if(variant != null) {
                                            hlog("Investigating variant '" + variant + "'.");
                                            newVariant = editor.replace(variant);
                                            if(newVariant != null && !variant.equalsIgnoreCase(newVariant)) {
                                                log("Regex matches! New variant name is '"+newVariant + "'." );
                                                topic.setVariant(scope, newVariant);
                                                count++;
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
                log("Total "+count+" variant names changed!");
            }
        }
        catch (Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    
}
