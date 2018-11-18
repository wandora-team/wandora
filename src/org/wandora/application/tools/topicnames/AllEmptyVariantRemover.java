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
 * AllEmptyVariantRemover.java
 *
 * Created on 22. toukokuuta 2006, 17:04
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
 * Tool removes all variant empty names of context topics. Empty variant name
 * has a name length of 0.
 *
 * @author akivela
 */
public class AllEmptyVariantRemover extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;


	public AllEmptyVariantRemover() {
    }
    public AllEmptyVariantRemover(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "All empty variant name remover";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and removes all empty variant names.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            
            if(WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to remove all empty variant names of selected topics?","Confirm variant name remove", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){

                setDefaultLogger();
                setLogTitle("Removing all empty variant names");
                log("Removing all empty variant names");

                Topic topic = null;
                String variant = null;

                List<Set<Topic>> deleteScopes = null;
                Collection<Set<Topic>> scopes = null;
                Iterator<Set<Topic>> scopeIterator = null;
                Set<Topic> scope = null;
                int progress = 0;
                int deleted = 0;

                while(topics.hasNext() && !forceStop()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            progress++;
                            scopes = topic.getVariantScopes();
                            if(scopes != null) {
                                deleteScopes = new ArrayList<>();
                                scopeIterator = scopes.iterator();
                                while(scopeIterator.hasNext()) {
                                    try {
                                        scope = (Set<Topic>) scopeIterator.next();
                                        variant = topic.getVariant(scope);
                                        if(variant != null && variant.length() == 0) {
                                            deleteScopes.add(scope);
                                        }
                                    }
                                    catch(Exception e) {
                                        log(e);
                                    }
                                }
                                scopeIterator = deleteScopes.iterator();
                                while(scopeIterator.hasNext()) {
                                    try {
                                        scope = (Set<Topic>) scopeIterator.next();
                                        topic.removeVariant(scope);
                                        deleted++;
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
                log("Total " + progress + " topics inspected.");
                log("Total " + deleted + " variant names removed.");
                setState(WAIT);
            }
        }
        catch (Exception e) {
            log(e);
        }
    }
    

    
}
