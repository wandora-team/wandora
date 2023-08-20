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
 * AddImplicitDisplayScopeToVariants.java
 *
 * Created on 22.2.2010, 14:21
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
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author akivela
 */
public class AddImplicitDisplayScopeToVariants extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;


	/**
     * Creates a new instance of AddImplicitDisplayScopeToVariants
     */
    public AddImplicitDisplayScopeToVariants() {
    }
    public AddImplicitDisplayScopeToVariants(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public String getName() {
        return "Add implicit display scope to variants";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and adds display scope to variants that have neither display nor sort scope.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Add implicit display scope to variants");
            log("Iterates through selected topics and adds display scope to variants that have neither display nor sort scope.");
            TopicMap tm = wandora.getTopicMap();

            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String variant = null;

            Topic displayScope = tm.getTopic(XTMPSI.DISPLAY);
            if(displayScope == null) return;
            
            Topic sortScope = tm.getTopic(XTMPSI.SORT);

            Collection<Set<Topic>> scopes = null;
            Iterator<Set<Topic>> scopeIterator = null;
            Set<Topic> scope = null;
            Topic scopeTopic = null;
            int progress = 0;
            int count = 0;

            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        progress++;
                        topic = tm.getTopic(topic.getOneSubjectIdentifier()); // Change topic to layer stack topic instead of layer topic!
                        scopes = topic.getVariantScopes();
                        if(scopes != null) {
                            scopeIterator = scopes.iterator();
                            while(scopeIterator.hasNext() && !forceStop()) {
                                try {
                                    scope = (Set<Topic>) scopeIterator.next();
                                    boolean scopeIsComplete = false;
                                    for( Iterator<Topic> scopeTopicIterator = scope.iterator(); scopeTopicIterator.hasNext(); ) {
                                        scopeTopic = scopeTopicIterator.next();
                                        if(scopeTopic != null) {
                                            if(scopeTopic.mergesWithTopic(displayScope)) {
                                                scopeIsComplete = true;
                                                break;
                                            }
                                            else if(sortScope != null && scopeTopic.mergesWithTopic(sortScope)) {
                                                scopeIsComplete = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!scopeIsComplete) { // Incomplete scope... Adding display scope!
                                        variant = topic.getVariant(scope);
                                        if(variant != null) {
                                            topic.removeVariant(scope);
                                            scope.add(displayScope);
                                            topic.setVariant(scope, variant);
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
            log("Found "+count+" variant names without display nor sort scope. Added display scope.");
            log("OK");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }

}
