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
 * 
 *
 * TopicMapStatData.java
 *
 * Created on 25. toukokuuta 2006, 18:26
 *
 */

package org.wandora.topicmap;

/**
 *
 * @author akivela
 */
public class TopicMapStatData {
    Object[] data = null;
    
    /** Creates a new instance of TopicMapStatData */
    public TopicMapStatData() {
    }
    public TopicMapStatData(int intData) {
        data = new Object[] { new Integer(intData) };
    }
    public TopicMapStatData(int intData1, int intData2) {
        data = new Object[] { new Integer(intData1),  new Integer(intData2) };
    }
    public TopicMapStatData(TopicMapStatData newData) {
        data = new Object[] { newData };
    }
    public TopicMapStatData(TopicMapStatData newData1, TopicMapStatData newData2) {
        data = new Object[] { newData1, newData2 };
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public String toString() {
        if(data != null && data.length > 0) {
            StringBuilder sb = new StringBuilder("");
            for(int i=0; i<data.length; i++) {
                sb.append( data[i].toString() );
                if(i+1<data.length) sb.append("/");
            }
            return sb.toString();
        }
        else {
            return "n.a.";
        }
    }
    
}
