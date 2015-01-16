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
 */
package org.wandora.query2;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class Contains extends WhereDirective {
 
    private Operand operand1;
    private Operand operand2;
    private boolean caseInsensitive=true;

    public Contains(Object operand2){
        this(new Identity(),operand2);
    }
    
    public Contains(Object operand1,Object operand2){
        this(operand1, operand2, true);
    }
    public Contains(Object operand1,Object operand2,boolean caseInsensitive){
        this.operand1=Operand.makeOperand(operand1);
        this.operand2=Operand.makeOperand(operand2);
        this.caseInsensitive=caseInsensitive;
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
        Object v1=operand1.getOperandObject(context, row);
        Object v2=operand2.getOperandObject(context, row);
        
        if(v1==null && v2==null) return false;
        else if(v1==null && v2!=null) return false;
        else if(v1!=null && v2==null) return false;
        else {
            String s1=v1.toString();
            String s2=v2.toString();
            if(caseInsensitive) {
                s1=s1.toLowerCase();
                s2=s2.toLowerCase();
            }
            
            return s1.indexOf(s2)>=0;
        }
    }    
}
