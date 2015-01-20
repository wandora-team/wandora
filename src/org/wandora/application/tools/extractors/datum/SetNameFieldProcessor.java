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
 * SetNameFieldProcessor.java
 *
 * Created on 24. marraskuuta 2004, 18:40
 */

package org.wandora.application.tools.extractors.datum;
import org.wandora.topicmap.*;
import java.util.*;
/**
 *
 * @author  olli
 */
public class SetNameFieldProcessor implements FieldProcessor {
    
    public static final int FLAG_SETBASE=1;
    public static final int FLAG_SETVARIANT=2;
    
    protected ExtractionHelper helper;
    protected String targetField;
    protected Collection scope;
    protected int flags;
    
    /** Creates a new instance of SetNameFieldProcessor */
    public SetNameFieldProcessor(ExtractionHelper helper,String targetField,Collection scope) {
        this(helper,targetField,scope,FLAG_SETVARIANT);
    }
    public SetNameFieldProcessor(ExtractionHelper helper,String targetField,Collection scope,int flags) {
        this.helper=helper;
        this.targetField=targetField;
        this.scope=scope;
        this.flags=flags;
    }
    
    public void processDatum(java.util.Map datum, String field, org.wandora.topicmap.TopicMap tm, org.wandora.piccolo.Logger logger) throws ExtractionException {
        try{
            HashSet tscope=new HashSet();
            Collection ts=helper.getOrCreateTopics(datum, targetField, tm);
            if(ts==null){
                logger.writelog("WRN","Null value for field "+targetField);
                return;
            }
            String name=null;
            Object o=datum.get(field);
            if(o instanceof String) name=(String)o;
            else if( ((Collection)o).isEmpty() ) return;
            else name=(String)((Collection)o).iterator().next();
            name=name.trim();
            if(name == null || name.length() == 0) {
                return;
            }
            //System.out.println("NAME: " + name);

            if((flags&FLAG_SETVARIANT)!=0){
                Iterator iter=scope.iterator();
                while(iter.hasNext()){
                    String si=(String)iter.next();
                    tscope.add(helper.getOrCreateTopic(tm,si));
                }
                iter=ts.iterator();
                while(iter.hasNext()){
                    Topic t=(Topic)iter.next();
                    t.setVariant(tscope, name);
                }
            }
            if((flags&FLAG_SETBASE)!=0){
                Iterator iter=ts.iterator();
                if(iter.hasNext()){ // do not set base name to all, would merge all topics which probably is not what was intended
                    Topic t=(Topic)iter.next();
                    t.setBaseName(name);
                }
            }
        }catch(TopicMapException tme){throw new ExtractionException(tme);}

    }
    
}
