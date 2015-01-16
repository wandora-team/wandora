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
 * Compare.java
 *
 */
package org.wandora.query2;
import java.util.*;
import org.wandora.topicmap.*;
/**
 *
 * @author olli
 */
public class Compare extends WhereDirective {

    public static final int EQ=0;
    public static final int NE=1;
    public static final int LT=2;
    public static final int GT=3;
    public static final int LE=4;
    public static final int GE=5;

    public static final int OP_MASK=0xFF;
    public static final int TYPE_MASK=0xFF00;
    public static final int TYPE_STRING=0;
    public static final int TYPE_NUMERIC=256;
    public static final int TYPE_TOPIC=512;

    private Operand operand1;
    private Operand operand2;
    private int operator;

    public Compare(Object operand1,int operator,Object operand2){
        if( (operator&TYPE_MASK)==TYPE_TOPIC ) {
            this.operand1=TopicOperand.makeTopicOperand(operand1);
            this.operand2=TopicOperand.makeTopicOperand(operand2);
        }
        else {
            this.operand1=Operand.makeOperand(operand1);
            this.operand2=Operand.makeOperand(operand2);
        }
        this.operator=operator;
    }

    public Compare(Object operand1,String operator,Object operand2){
        this(operand1,parseOperator(operator),operand2);
    }

    public static int parseOperator(String operator){
        int mode=0;
        operator=operator.toLowerCase();
        if(operator.startsWith("n")) { mode=TYPE_NUMERIC; operator=operator.substring(1); }
        else if(operator.startsWith("t")) { mode=TYPE_TOPIC; operator=operator.substring(1); }
        else if(operator.startsWith("s")) { mode=TYPE_STRING; operator=operator.substring(1); }

        if(operator.equals("=") || operator.equals("==")) return mode|EQ;
        else if(operator.equals("!=") || operator.equals("<>") || operator.equals("~=")) return mode|NE;
        else if(operator.equals("<")) return mode|LT;
        else if(operator.equals(">")) return mode|GT;
        else if(operator.equals("<=")) return mode|LE;
        else if(operator.equals(">=")) return mode|GE;
        else return EQ;
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        operand1.endQuery(context);
        operand2.endQuery(context);
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        return operand1.startQuery(context) & operand2.startQuery(context);
    }


    @Override
    public boolean includeRow(QueryContext context,ResultRow row) throws QueryException {
        int mode=(operator&TYPE_MASK);

        Object v1=null;
        Object v2=null;

        TopicMap tm=context.getTopicMap();

        int compare=0;
        boolean cond=false;
        if(v1==null || v2==null) cond=true;

        if(mode==TYPE_NUMERIC){
            v1=operand1.getOperandObject(context, row);
            v2=operand2.getOperandObject(context, row);
            if(v1!=null) {
                try{
                    v1=Double.parseDouble(v1.toString());
                }catch(NumberFormatException nfe){v1=null;}
            }
            if(v2!=null) {
                try{
                    v2=Double.parseDouble(v2.toString());
                }catch(NumberFormatException nfe){v2=null;}
            }
            if(v1==null && v2==null) compare=0;
            else if(v1==null && v2!=null) compare=-1;
            else if(v1!=null && v2==null) compare=1;
            else compare=Double.compare((Double)v1, (Double)v2);
        }
        else if(mode==TYPE_TOPIC){
            Topic t1=((TopicOperand)operand1).getOperandTopic(context, row);
            Topic t2=((TopicOperand)operand2).getOperandTopic(context, row);
            try{
                if(t1==null || t2==null) compare=-1;
                else if(t1.mergesWithTopic(t2)) compare=0;
                else compare=1;
            }catch(TopicMapException tme){
                throw new QueryException(tme);
            }
        }
        else {
            v1=operand1.getOperandObject(context, row);
            v2=operand2.getOperandObject(context, row);
            if(v1==null && v2==null) compare=0;
            else if(v1==null && v2!=null) compare=-1;
            else if(v1!=null && v2==null) compare=1;
            else compare=v1.toString().compareTo(v2.toString());
        }

        switch(operator&OP_MASK){
            case EQ:
                if(compare==0) return true;
                break;
            case NE:
                if(compare!=0) return true;
                break;
            case LT:
                if(compare<0) return true;
                break;
            case GT:
                if(compare>0) return true;
                break;
            case LE:
                if(compare<=0) return true;
                break;
            case GE:
                if(compare>=0) return true;
                break;
        }

        return false;
    }


}
