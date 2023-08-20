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
 */



package org.wandora.application.tools.topicnames;

import java.util.LinkedHashSet;
import java.util.Set;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;



/**
 *
 * @author akivela
 */
public class AddScopeTopicToVariantName extends AbstractWandoraTool {

	private static final long serialVersionUID = 1L;

	private Topic t = null;
    private Set<Topic> scope = null;
    private Topic predefinedScopeTopic = null;


    public AddScopeTopicToVariantName(Topic variantCarrier, Set<Topic> variantScope, Topic st) {
        t = variantCarrier;
        scope = variantScope;
        predefinedScopeTopic = st;
    }
    public AddScopeTopicToVariantName(Topic variantCarrier, Set<Topic> variantScope) {
        t = variantCarrier;
        scope = variantScope;
    }

    @Override
    public String getName() {
        return "Add scope topic to variant name";
    }

    @Override
    public String getDescription() {
        return "Add scope topic to variant name of a topic and a scope specified in constructor.";
    }



    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Topic scopeTopic = null;
            if(predefinedScopeTopic == null) {
                scopeTopic = wandora.showTopicFinder(wandora, "Select scope topic");
            }
            else {
                scopeTopic = predefinedScopeTopic;
            }
            if(t != null && !t.isRemoved() && scopeTopic != null) {
                if(scope != null) {
                    String n = t.getVariant(scope);
                    if(n != null) {
                        t.removeVariant(scope);
                        Set<Topic> newScope = new LinkedHashSet<Topic>();
                        newScope.addAll(scope);
                        newScope.add(scopeTopic);
                        t.setVariant(newScope, n);
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}

