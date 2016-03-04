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
 */

package org.wandora.application.tools.extractors.word;

import java.util.ArrayList;
import java.util.List;
import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


class SimilarityWordConfiguration extends WordConfiguration{
    
    private float THRESHOLD;

    private InterfaceStringMetric STRING_METRIC;
    private final DualHashBidiMap STRING_METRICS;
    
    SimilarityWordConfiguration() {
    
        super();
        
        setAssociateScore(true);
        
        THRESHOLD = 0.5f;
        
        STRING_METRICS = new DualHashBidiMap();
        STRING_METRICS.put("Levenshtein", new Levenshtein());
        STRING_METRICS.put("Jaro", new Jaro());
        STRING_METRICS.put("Jaro Winkler", new JaroWinkler());
        
        STRING_METRIC = new Levenshtein();
        
    }
    
    protected float getThreshold(){
        return THRESHOLD;
    }
    
    protected void setThreshold(float f){
        THRESHOLD = f;
    }
    
    protected void setStringMetric(String s){
        STRING_METRIC = (InterfaceStringMetric) STRING_METRICS.get(s);
    }
    
    protected InterfaceStringMetric getStringMetric(){
        return STRING_METRIC;
    }
    
    protected String getStringMetricName(){
        return (String)STRING_METRICS.getKey(STRING_METRIC);
    }
    
    protected List<String> getSTringMetricNames(){
        List<String> l = new ArrayList();
        l.addAll(STRING_METRICS.keySet());
        return l;
    }
    
}
