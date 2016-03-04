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
 *
 * 
 *
 * SearchResultItem.java
 *
 * Created on March 12, 2002, 6:46 PM
 */

package org.wandora.piccolo;


import org.wandora.topicmap.*;
import org.wandora.utils.*;


/**
 *
 * @author  olli
 */
public class SearchResultItem extends Object {

    private Topic topic;
    private double score;
    private String lang;
    
    /** Creates new SearchResultItem */
    public SearchResultItem(Topic topic,double score,String lang) {
        this.topic=topic;
        this.lang=lang;
        this.score=score;
    }
    
    public Topic getTopic(){return topic;}
    public double getScore(){return score;}
    public int getIntScore(){return (int)(score*5.0+0.5);}
    public int getIntScore(int max){return (int)(score*max+0.5);}
    
}
