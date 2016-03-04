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
 * AddMissingLanguageScope.java
 *
 * Created on 22.2.2010, 14:21
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
public class AddMissingLanguageScope extends AbstractWandoraTool implements WandoraTool {

    /**
     * Creates a new instance of AddMissingLanguageScope
     */
    public AddMissingLanguageScope() {
    }
    public AddMissingLanguageScope(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public String getName() {
        return "Add implicit language scope to variants";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and adds given scope topic to variants that have only display or sort scope.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Add implicit language scope to variants");
            log("Iterates through selected topics and adds given scope topic to variants that have only display or sort scope.");

            TopicMap tm = wandora.getTopicMap();
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String variant = null;

            Topic displayScope = tm.getTopic(XTMPSI.DISPLAY);

            Topic sortScope = tm.getTopic(XTMPSI.SORT);

            Topic langScope = wandora.showTopicFinder("Select language scope topic...");
            if(langScope == null) return;

            Collection scopes = null;
            Iterator scopeIterator = null;
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
                                            // If scope contains anything else than display or sort scope then scope is complete!
                                            if(!scopeTopic.mergesWithTopic(displayScope) && !scopeTopic.mergesWithTopic(sortScope)) {
                                                scopeIsComplete = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!scopeIsComplete) { // Incomplete scope... Adding lang scope!
                                        variant = topic.getVariant(scope);
                                        if(variant != null) {
                                            topic.removeVariant(scope);
                                            scope.add(langScope);
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
            log("Found "+count+" variant names without language scope. Added language scope.");
            log("OK");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }

}
