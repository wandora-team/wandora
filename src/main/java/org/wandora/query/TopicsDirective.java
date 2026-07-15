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
 *
 * 
 *
 * TopicsDirective.java
 *
 * Created on 20. helmikuuta 2008, 13:46
 */

package org.wandora.query;
import java.util.ArrayList;

import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;

/**
 * @deprecated
 *
 * @author olli
 */
public class TopicsDirective implements Directive {
    
    private ArrayList<ResultRow> result;
    
    public TopicsDirective(Locator resultType,Locator resultRole,Locator ... topics) {
        result=new ArrayList<ResultRow>();
        for(int i=0;i<topics.length;i++){
            ResultRow r=new ResultRow(resultType,resultRole,topics[i]);
            result.add(r);            
        }
    }
    public TopicsDirective(String resultType,String resultRole,String ... topics) {
        this(new Locator(resultType),new Locator(resultRole),makeLocatorArray(topics));
    }
    public TopicsDirective(Locator ... topics) {
        this(new Locator(XTMPSI.TOPIC),new Locator(XTMPSI.TOPIC),topics);
    }
    public TopicsDirective(String ... topics) {
        this(XTMPSI.TOPIC,XTMPSI.TOPIC,topics);
    }
    public TopicsDirective(String topic1) {
        this(new String[]{topic1});
    }
    public TopicsDirective(String topic1,String topic2) {
        this(new String[]{topic1,topic2});
    }
    public TopicsDirective(String topic1,String topic2,String topic3) {
        this(new String[]{topic1,topic2,topic3});
    }
    
    private static Locator[] makeLocatorArray(String[] topics){
        Locator[] ret=new Locator[topics.length];
        for(int i=0;i<topics.length;i++){
            ret[i]=new Locator(topics[i]);
        }
        return ret;
    }
    
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        return result;
    }
    public boolean isContextSensitive(){
        return false;
    }

}
