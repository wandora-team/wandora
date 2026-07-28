/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 *
 * Copyright (C) 2004-2026 Wandora Team
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
import java.util.LinkedHashMap;
import java.util.List;

import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;



/**
 *
 * @author Eero Lehtonen
 */
class SimilarityWordConfiguration extends WordConfiguration{
    
    private float threshold;

    private InterfaceStringMetric stringMetric;
    private final LinkedHashMap<String,InterfaceStringMetric> stringMetrics;
    
    
    
    SimilarityWordConfiguration() {
        super();
        setAssociateScore(true);
        
        threshold = 0.5f;
        
        stringMetrics = new LinkedHashMap<>();
        stringMetrics.put("Levenshtein", new Levenshtein());
        stringMetrics.put("Jaro", new Jaro());
        stringMetrics.put("Jaro Winkler", new JaroWinkler());
        
        stringMetric = new Levenshtein();
    }
    
    
    protected float getThreshold(){
        return threshold;
    }
    
    protected void setThreshold(float f){
    	threshold = f;
    }
    
    protected void setStringMetric(String s){
    	stringMetric = stringMetrics.get(s);
    }
    
    protected InterfaceStringMetric getStringMetric(){
        return stringMetric;
    }
    
    protected List<String> getStringMetricNames(){
        List<String> l = new ArrayList<>();
        l.addAll(stringMetrics.keySet());
        return l;
    }
    
}
