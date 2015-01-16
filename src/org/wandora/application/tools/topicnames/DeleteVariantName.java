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
 */



package org.wandora.application.tools.topicnames;

import java.util.Set;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Topic;

/**
 *
 * @author akivela
 */
public class DeleteVariantName extends AbstractWandoraTool {

    private Topic t = null;
    private Set<Topic> scope = null;


    public DeleteVariantName(Topic variantCarrier, Set<Topic> variantScope) {
        t = variantCarrier;
        scope = variantScope;
    }

    @Override
    public String getName() {
        return "Delete single variant name of given topic";
    }

    @Override
    public String getDescription() {
        return "Deletes single variant name of a topic and a scope specified in constructor.";
    }

    

    public void execute(Wandora wandora, Context context) {
        try {
            if(t != null && !t.isRemoved()) {
                if(scope != null) {
                    t.removeVariant(scope);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}



