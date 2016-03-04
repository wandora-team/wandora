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
 * TopicOperand.java
 *
 */
package org.wandora.query2;
import org.wandora.topicmap.*;
import static org.wandora.utils.Tuples.*;

/**
 *
 * @author olli
 */
public class TopicOperand extends Operand {
    public T2<Topic,String> cachedTopic;
    public boolean topicIsCached=false;

    public TopicOperand(){}
    
    public TopicOperand(Object operand){
        super(operand);
    }
    
    @Override
    public DirectiveUIHints getUIHints() {
        DirectiveUIHints ret=new DirectiveUIHints(Players.class,new DirectiveUIHints.Constructor[]{
                new DirectiveUIHints.Constructor(new DirectiveUIHints.Parameter[]{
                        new DirectiveUIHints.Parameter(Object.class, false, "operand"),
                }, "")
            },
            Directive.getStandardAddonHints(),
            "TopicOperand",
            "Framework");
        return ret;
    }        

    public static TopicOperand makeTopicOperand(Object o){
        if(o!=null && o instanceof TopicOperand) return (TopicOperand)o;
        else if(o!=null && o instanceof Operand) return new TopicOperand(((Operand)o).operand);
        else return new TopicOperand(o);
    }
    public static TopicOperand[] makeTopicOperands(Object[] os){
        TopicOperand[] ret=new TopicOperand[os.length];
        for(int i=0;i<ret.length;i++){
            ret[i]=makeTopicOperand(os[i]);
        }
        return ret;
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        topicIsCached=false;
        cachedTopic=null;
        return super.startQuery(context);
    }

    public T2<Topic,String> getOperandTopicAndSI(QueryContext context, ResultRow input) throws QueryException {
        if(stat && topicIsCached) return cachedTopic;
        Object o=getOperandObject(context, input);
        T2<Topic,String> ret=null;
        try{
            if(o==null) {
                ret=t2(null,null);
            }
            else if(o instanceof Topic){
                ret=t2((Topic)o,((Topic)o).getOneSubjectIdentifier().toExternalForm());
            }
            else {
                Topic t=context.getTopicMap().getTopic(o.toString());
                ret=t2(t,o.toString());
            }
            if(stat){
                cachedTopic=ret;
                topicIsCached=true;
            }
            return ret;
        }
        catch(TopicMapException tme){
            throw new QueryException(tme);
        }
    }

    public Topic getOperandTopic(QueryContext context, ResultRow input) throws QueryException {
        return getOperandTopicAndSI(context, input).e1;
    }

    public String getOperandSI(QueryContext context,ResultRow input) throws QueryException {
        return getOperandTopicAndSI(context, input).e2;
    }


}
