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
 * VariantNewlineRemover.java
 *
 * Created on 22. toukokuuta 2006, 14:21
 *
 */

package org.wandora.application.tools.topicnames;



import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;


/**
 * As well as base names also variant names containing new line characters
 * are partially invisible in <code>Wandora</code>. It is recommended
 * that variant names with new lines are fixed with this tool.
 *
 * @author akivela
 */
public class VariantNewlineRemover extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public String replacement = "";
    
    /**
     * Creates a new instance of VariantNewlineRemover
     */
    public VariantNewlineRemover() {
    }
    public VariantNewlineRemover(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Variant name new line remover";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and removes new line characters in variant names.";
    }
    
  
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Removing new line characters in variant names");
            log("Removing new line characters in variant names");
            
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String variant = null;
            String newVariant = null;
            
            Collection<Set<Topic>> scopes = null;
            Iterator<Set<Topic>> scopeIterator = null;
            Set<Topic> scope = null;
            int progress = 0;
            
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
                                        if(variant.indexOf("\n") != -1 || variant.indexOf("\r") != -1) {
                                            newVariant = variant.replaceAll("\r", replacement);
                                            newVariant = newVariant.replaceAll("\n", replacement);
                                            log("Changing variant '"+variant+"' to\n"+newVariant );
                                            topic.setVariant(scope, newVariant);
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
