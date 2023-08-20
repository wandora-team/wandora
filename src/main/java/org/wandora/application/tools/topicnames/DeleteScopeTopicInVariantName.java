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
public class DeleteScopeTopicInVariantName extends AbstractWandoraTool {

	private static final long serialVersionUID = 1L;

	private Topic t = null;
    private Set<Topic> scope = null;
    private Topic scopeTopic = null;


    public DeleteScopeTopicInVariantName(Topic variantCarrier, Set<Topic> variantScope, Topic st) {
        t = variantCarrier;
        scope = variantScope;
        scopeTopic = st;
    }

    @Override
    public String getName() {
        return "Delete scope topic in variant name";
    }

    @Override
    public String getDescription() {
        return "Deletes scope topic in variant name of a topic and a scope specified in constructor.";
    }



    public void execute(Wandora wandora, Context context) {
        try {
            if(t != null && !t.isRemoved() && scopeTopic != null && !scopeTopic.isRemoved()) {
                if(scope != null) {
                    String n = t.getVariant(scope);
                    if(n != null) {
                        t.removeVariant(scope);
                        Set<Topic> newScope = new LinkedHashSet<Topic>();
                        newScope.addAll(scope);
                        newScope.remove(scopeTopic);
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

