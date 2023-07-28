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
 * Instances.java
 *
 *
 */
package org.wandora.query2;

import java.util.*;
import org.wandora.topicmap.*;
/**
 *
 * @author olli
 */
public class Instances extends Directive implements DirectiveUIHints.Provider {

    public Instances(){
    }

    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Instances.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Instances",
            "Topic map");
        return ret;
    }      
        
    @Override
    public ArrayList<ResultRow> query(QueryContext context, ResultRow input) throws QueryException {
        Object o=input.getActiveValue();
        if(o==null) return new ArrayList<ResultRow>();

        try{
            if(!(o instanceof Topic)){
                TopicMap tm=context.getTopicMap();
                o=tm.getTopic(o.toString());
                if(o==null) return new ArrayList<ResultRow>();
            }

            Collection<Topic> instances=((Topic)o).getTopicMap().getTopicsOfType(((Topic)o));
            ArrayList<ResultRow> ret=new ArrayList<ResultRow>();
            for(Topic t : instances){
                ret.add(input.addValue(Directive.DEFAULT_COL, t));
            }
            return ret;
        }catch(TopicMapException tme){
            throw new QueryException(tme);
        }
    }

}
