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
 */

package org.wandora.topicmap.similarity;


import java.util.Collection;

import org.wandora.topicmap.Topic;


/**
 *
 * @author akivela
 */


public class TypeSimilarity implements TopicSimilarity {

    private double errorDivider = 2.0;
    
    
    public TypeSimilarity() {
    }
    
    public TypeSimilarity(double ed) {
        errorDivider = ed;
    }
    
    @Override
    public String getName() {
        return "Topic type (class) similarity";
    }
    
    @Override
    public double similarity(Topic t1, Topic t2) {
        double similarity = 1;
        try {
            Collection<Topic> types1 = t1.getTypes();
            Collection<Topic> types2 = t2.getTypes();
            
            if(types1 == null || types1.isEmpty()) return 0;
            if(types2 == null || types2.isEmpty()) return 0;
            
            for(Topic type1 : types1) {
                if(!types2.contains(type1)) similarity = similarity / errorDivider;
            }
            for(Topic type2 : types2) {
                if(!types1.contains(type2)) similarity = similarity / errorDivider;
            }
        }
        catch(Exception e) {}
        return similarity;
    }
}
