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
 */
package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * @deprecated
 *
 * @author olli
 */
public class ContextTopicDirective implements Directive {

    private Locator resultType;
    private Locator resultRole;
    
    public ContextTopicDirective(Locator resultType,Locator resultRole) {
        this.resultType=resultType;
        this.resultRole=resultRole;
    }
    public ContextTopicDirective(String resultType,String resultRole){
        this(new Locator(resultType),new Locator(resultRole));
    }
    
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        ArrayList<ResultRow> result=new ArrayList<ResultRow>();
        ResultRow r=new ResultRow(resultType,resultRole,context.getContextTopic().getOneSubjectIdentifier());
        result.add(r);
        return result;
    }
    public boolean isContextSensitive(){
        return true;
    }

}
