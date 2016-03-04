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
 * StringsDirective.java
 *
 */
package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class StringsDirective implements Directive {

    private ArrayList<ResultRow> result;
    public static final String STRING_SI="http://wandora.org/si/query/string";

    public StringsDirective(Locator resultType,Locator resultRole,String ... strings) {
        if(resultType==null) resultType=new Locator(STRING_SI);
        if(resultRole==null) resultRole=new Locator(STRING_SI);
        result=new ArrayList<ResultRow>();
        for(int i=0;i<strings.length;i++){
            ResultRow r=new ResultRow(resultType,resultRole,strings[i]);
            result.add(r);
        }
    }
    public StringsDirective(String resultType,String resultRole,String ... strings) {
        this(resultType==null?null:new Locator(resultType),
             resultRole==null?null:new Locator(resultRole),strings);
    }
    public StringsDirective(String string) {
        this((Locator)null,(Locator)null,string);
    }

    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        return result;
    }
    public boolean isContextSensitive(){
        return false;
    }

}
