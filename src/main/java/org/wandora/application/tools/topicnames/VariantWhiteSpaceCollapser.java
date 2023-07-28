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



import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;





/**
 *
 * @author akivela
 */
public class VariantWhiteSpaceCollapser extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;


	/**
     * Creates a new instance of VariantWhiteSpaceCollapser
     */
    public VariantWhiteSpaceCollapser() {
    }
    public VariantWhiteSpaceCollapser(Context preferredContext) {
        setContext(preferredContext);
    }
    

    @Override
    public String getName() {
        return "Variant white space collapser";
    }


    @Override
    public String getDescription() {
        return "Iterates through selected topics and converts continuous white spaces to a single white space character in variant names.";
    }
    
  
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Collapsing white space characters in variant names");
            log("Collapsing white space characters in variant names");
            
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String variant = null;
            StringBuffer newVariant = null;
            
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
                                        newVariant = new StringBuffer();
                                        char ch = 0;
                                        boolean isFirst = true;
                                        boolean hasChanged = false;
                                        for(int i=0; i<variant.length(); i++) {
                                            ch = variant.charAt(i);
                                            if(Character.isSpaceChar(ch)) {
                                                if(isFirst) {
                                                    newVariant.append(ch);
                                                    isFirst = false;
                                                }
                                                else {
                                                    hasChanged = true;
                                                }
                                            }
                                            else {
                                                newVariant.append(ch);
                                                isFirst = true;
                                            }
                                        }

                                        if(hasChanged) {
                                            topic.setVariant(scope, newVariant.toString());
                                            log("Changed variant to '"+newVariant + "'.");
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
