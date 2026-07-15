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
 * CountDirective.java
 *
 * Created on 26. marraskuuta 2007, 10:09
 *
 */

package org.wandora.query;
import java.util.ArrayList;

import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TopicMapException;

/**
 * @deprecated
 *
 * @author olli
 */
public class CountDirective implements Directive {

    private Directive query;
    private Locator type;
    private Locator role;

    public static final String TYPE_SI="http://wandora.org/si/query/counttype";
    public static final String ROLE_SI="http://wandora.org/si/query/countrole";
    
    /** Creates a new instance of CountDirective */
    public CountDirective(Directive query) {
        this(query,TYPE_SI,ROLE_SI);
    }

    public CountDirective(Directive query,Locator type,Locator role) {
        this.query=query;
        this.type=type;
        this.role=role;
    }
    public CountDirective(Directive query,String type,String role) {
        this(query,new Locator(type),new Locator(role));
    }
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        ArrayList<ResultRow> inner=query.query(context);
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();
        res.add(new ResultRow(type,role,""+inner.size()));
        return res;
    }
    public boolean isContextSensitive(){
        return query.isContextSensitive();
    }

}
