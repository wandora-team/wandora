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
 * 
 * TopicClusteringCoefficient.java
 *
 * Created on 30. toukokuuta 2007, 13:34
 *
 */
package org.wandora.application.tools.statistics;

/**
 *
 * @author
 * Eero
 */

import java.util.HashMap;
import java.lang.String;

public class TopicMapDiamaterArray {
    
    //private SparseDoubleMatrix2D map;
    private HashMap<String,Integer> map;
    public TopicMapDiamaterArray(int size){
        map = new HashMap<String, Integer>();
    }
    
    public void put(int x, int y, int val){
        String place = Integer.toString(x) + "-" + Integer.toString(y);
        map.put(place,val);
    }
    
    public int get(int x,int y){
        String place = Integer.toString(x) + "-" + Integer.toString(y);
        if(this.map.containsKey(place)){
            return this.map.get(place);
        }
        return -1;
    }
}
