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
 * ModifyFieldProcessor.java
 *
 * Created on 21. hein�kuuta 2005, 11:18
 */

package org.wandora.application.tools.extractors.datum;

import org.wandora.topicmap.*;
import java.util.*;
import java.util.regex.*;


/**
 *
 * @author olli
 */
public class ModifyFieldProcessor implements FieldProcessor {
    
    protected String prefix;
    protected String postfix;
    protected FieldProcessor next;
    protected String key;
    protected Pattern regex;
            
    /** Creates a new instance of ModifyFieldProcessor */
    public ModifyFieldProcessor(String prefix,String postfix,String key,FieldProcessor next) {
        this.prefix=prefix;
        this.postfix=postfix;
        this.next=next;
        this.key=key;
    }
    public ModifyFieldProcessor(String regex,String key,FieldProcessor next){
        this.regex=Pattern.compile(regex);
        this.key=key;
        this.next=next;
    }

    public void processDatum(Map datum, String field, TopicMap tm, org.wandora.piccolo.Logger logger) throws ExtractionException {
        Object o=datum.get(field);
        Collection c=null;
        if(o instanceof Collection) c=(Collection)o;
        else{
            c=new Vector();
            c.add(o.toString());
        }
        Iterator iter=c.iterator();
        while(iter.hasNext()){
            String value=iter.next().toString();
            if(prefix!=null) value=prefix+value;
            if(postfix!=null) value+=postfix;
            HashMap newDatum=new HashMap();
            newDatum.putAll(datum);
            newDatum.put(key,value);
            next.processDatum(newDatum,key,tm,logger);
        }
    }
    
}
