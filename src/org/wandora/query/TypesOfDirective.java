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
 */
package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * @deprecated
 *
 * @author olli
 */
public class TypesOfDirective implements Directive {
    private Locator role;
    
    public TypesOfDirective() {
        this(XTMPSI.CLASS);
    }
    public TypesOfDirective(String role) {
        this(new Locator(role));
    }
    public TypesOfDirective(Locator role) {
        this.role=role;
    }
    
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        Topic contextTopic=context.getContextTopic();
        TopicMap tm=contextTopic.getTopicMap();
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();
        for(Topic t : contextTopic.getTypes()){
            res.add(new ResultRow(role,role,t.getOneSubjectIdentifier()));            
        }
        return res;
    }
    public boolean isContextSensitive(){
        return true;
    }

}
