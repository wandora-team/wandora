/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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
 * Occurrence.java
 *
 *
 */
package org.wandora.query2;
import org.wandora.topicmap.*;
import java.util.*;
/**
 *
 * @author olli
 */
public class Occurrence extends Directive implements DirectiveUIHints.Provider {

    private TopicOperand type;
    private TopicOperand version;

    public Occurrence(){
        this(null,null);
    }
    public Occurrence(Object type){
        this(type,null);
    }
    public Occurrence(Object type,Object version){
        this.type=(type==null?null:new TopicOperand(type));
        this.version=(version==null?null:new TopicOperand(version));
    }
    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Players.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{}, ""),
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(TopicOperand.class, false, "type"),
                }, ""),
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(TopicOperand.class, false, "type"),
                        new DirectiveUIHints.Parameter(TopicOperand.class, false, "version"),
                }, "")
            },
            Directive.getStandardAddonHints(),
            "Occurrence",
            "Topic map");
        return ret;
    }         

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        boolean ret=true;
        if(type!=null) ret&=type.startQuery(context);
        if(version!=null) ret&=type.startQuery(context);
        return ret;
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        if(type!=null) type.endQuery(context);
        if(version!=null) type.endQuery(context);
    }

    @Override
    public ResultIterator queryIterator(QueryContext context, ResultRow input) throws QueryException {
        Object o=input.getActiveValue();
        if(o==null) return new ResultIterator.EmptyIterator();
        try{
            if(!(o instanceof Topic)){
                TopicMap tm=context.getTopicMap();
                o=tm.getTopic(o.toString());
                if(o==null) return new ResultIterator.EmptyIterator();
            }
            if(type!=null && version!=null){
                Topic typeT=type.getOperandTopic(context, input);
                Topic versionT=version.getOperandTopic(context, input);
                if(typeT==null || versionT==null) return input.addValue(DEFAULT_COL, null).toIterator();

                String occ=((Topic)o).getData(typeT, versionT);
                return input.addValue(DEFAULT_COL, occ).toIterator();
            }
            else if(type!=null){
                Topic typeT=type.getOperandTopic(context,input);
                if(typeT==null) return new ResultIterator.EmptyIterator();
                Hashtable<Topic,String> data=((Topic)o).getData(typeT);
                ArrayList<ResultRow> ret=new ArrayList<ResultRow>();
                if(data!=null){
                    for(Map.Entry<Topic,String> e : data.entrySet()){
                        ret.add(input.addValues(new String[]{DEFAULT_NS+"occurrence_version",DEFAULT_COL}, new Object[]{e.getKey(),e.getValue()}));
                    }
                }
                return new ResultIterator.ListIterator(ret);
            }
            else {
                ArrayList<ResultRow> ret=new ArrayList<ResultRow>();
                for(Topic typeT : ((Topic)o).getDataTypes()){
                    Hashtable<Topic,String> data=((Topic)o).getData(typeT);
                    if(data!=null){
                        for(Map.Entry<Topic,String> e : data.entrySet()){
                            ret.add(input.addValues(new String[]{DEFAULT_NS+"occurrence_type",DEFAULT_NS+"occurrence_version",DEFAULT_COL},
                                                    new Object[]{typeT,e.getKey(),e.getValue()}));
                        }
                    }
                }
                return new ResultIterator.ListIterator(ret);
            }
        }catch(TopicMapException tme){
            throw new QueryException(tme);
        }

    }


}
