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
 * AllVariantRemover.java
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
 * Tool removes all variant names in context topics. Deletion is confirmed.
 *
 * @author akivela
 */
public class AllVariantRemover extends AbstractWandoraTool implements WandoraTool {
    

    public AllVariantRemover() {
    }
    public AllVariantRemover(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "All variant name remover";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and removes all variant names.";
    }
    
  
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;
            
            if(WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to remove all variant names of selected topics?","Confirm variant name remove", WandoraOptionPane.YES_NO_OPTION)==WandoraOptionPane.YES_OPTION){

                setDefaultLogger();
                setLogTitle("Removing all variant names");
                log("Removing all variant names");

                Topic topic = null;
                String variant = null;

                ArrayList<Set<Topic>> deleteScopes;
                Collection scopes = null;
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
                                deleteScopes = new ArrayList();
                                scopeIterator = scopes.iterator();
                                while(scopeIterator.hasNext()) {
                                    try {
                                        scope = scopeIterator.next();
                                        variant = topic.getVariant(scope);
                                        if(variant != null) {
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
                                        scope = scopeIterator.next();
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
                setState(WAIT);
            }
        }
        catch (Exception e) {
            log(e);
        }
    }
    

    
}
