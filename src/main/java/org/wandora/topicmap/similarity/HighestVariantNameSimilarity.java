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

import java.util.Set;
import org.wandora.topicmap.Topic;
import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;


/**
 *
 * @author akivela
 */


public class HighestVariantNameSimilarity implements TopicSimilarity {

    private InterfaceStringMetric stringMetric = null;
    
    
    public HighestVariantNameSimilarity() {
        stringMetric = new Levenshtein();
    }
    
    public HighestVariantNameSimilarity(InterfaceStringMetric metric) {
        stringMetric = metric;
    }
    
    @Override
    public String getName() {
        return "Highest variant name similarity";
    }
    
    @Override
    public double similarity(Topic t1, Topic t2) {
        double highestSimilarity = -1;
        double similarity = -1;
        try {
            for(Set<Topic> s1 : t1.getVariantScopes()) {
                for(Set<Topic> s2 : t2.getVariantScopes()) {
                    similarity = stringMetric.getSimilarity(t1.getVariant(s1), t2.getVariant(s2));
                    if(similarity > highestSimilarity) {
                        highestSimilarity = similarity;
                    }
                }
            }
        }
        catch(Exception e) {}
        return highestSimilarity;
    }
}
