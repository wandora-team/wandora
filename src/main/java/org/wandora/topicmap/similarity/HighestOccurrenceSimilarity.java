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

import java.util.Hashtable;
import org.wandora.topicmap.Topic;
import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 *
 * @author akivela
 */


public class HighestOccurrenceSimilarity implements TopicSimilarity {

    private InterfaceStringMetric stringMetric = null;
    
    
    public HighestOccurrenceSimilarity() {
        stringMetric = new Levenshtein();
    }
    
    public HighestOccurrenceSimilarity(InterfaceStringMetric metric) {
        stringMetric = metric;
    }
    
    @Override
    public String getName() {
        return "Highest occurrence similarity";
    }
    
    @Override
    public double similarity(Topic t1, Topic t2) {
        double highestSimilarity = -1;
        double similarity = -1;
        try {
            for(Topic type1 : t1.getDataTypes()) {
                Hashtable<Topic, String> o1s = t1.getData(type1);
                for(Topic o1sk : o1s.keySet()) {
                    String o1 = o1s.get(o1sk);
                    for(Topic type2 : t2.getDataTypes()) {
                        Hashtable<Topic, String> o2s = t2.getData(type2);
                        for(Topic o2sk : o2s.keySet()) {
                            String o2 = o2s.get(o2sk);
                            similarity = stringMetric.getSimilarity(o1, o2);
                            if(similarity > highestSimilarity) {
                                highestSimilarity = similarity;
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {}
        return highestSimilarity;
    }
    
}
