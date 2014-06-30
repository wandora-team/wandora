/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
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

import org.wandora.topicmap.Topic;
import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 *
 * @author akivela
 */


public class BasenameSimilarity implements TopicSimilarity {

    private InterfaceStringMetric stringMetric = null;
    
    
    public BasenameSimilarity() {
        stringMetric = new Levenshtein();
    }
    
    public BasenameSimilarity(InterfaceStringMetric metric) {
        stringMetric = metric;
    }
    
    @Override
    public String getName() {
        return "Basename similarity";
    }
    
    @Override
    public double similarity(Topic t1, Topic t2) {
        try {
            String n1 = t1.getBaseName();
            String n2 = t2.getBaseName();
            if(n1 == null && n2 == null) return 1;
            if(n1 == null && "".equals(n2)) return 0;
            if("".equals(n1) && n2 == null) return 0;
            
            if(n1.equals(n2)) return 1;
            
            return stringMetric.getSimilarity(n2, n1);
        }
        catch(Exception e) {}
        return 0;
    }
    
}
