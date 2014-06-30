/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * ExtractionHelper.java
 *
 * Created on 24. marraskuuta 2004, 18:08
 */

package org.wandora.application.tools.extractors.datum;
import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import org.wandora.piccolo.Logger;
import java.util.*;
/**
 *
 * @author  olli
 */
public class ExtractionHelper implements SIMaker {
    
    protected Map simap;
    protected SIMaker siMaker;
    
    /** Creates a new instance of ExtractionHelper */
    public ExtractionHelper(Object[] simap) {
        this(simap,null);
    }
    public ExtractionHelper(Object[] simap,SIMaker siMaker) {
        this.simap=new org.wandora.utils.EasyHash(simap);
        this.setSIMaker(siMaker);
    }
    
    public void setSIMaker(SIMaker siMaker){
        this.siMaker=siMaker;
    }
    
    public String makeSI(String field,String value){
        if(siMaker!=null) return siMaker.makeSI(field,value,this);
        else return makeSI(field,value,null);
    }
    
    public String makeSI(String field, String value, SIMaker next){
        return (String)simap.get(field)+TopicTools.cleanDirtyLocator(value);
    }
    
    public Topic getOrCreateTopic(TopicMap tm,String si) throws TopicMapException {
        Topic t=tm.getTopic(si);
        if(t==null) {
            t=tm.createTopic();
            t.addSubjectIdentifier(tm.createLocator(si));
        }
        return t;        
    }
    
    public Topic getOrCreateTopic(Map datum,String field,TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(datum,field,tm,false);
    }
    
    public Topic getOrCreateTopic(Map datum, String field, TopicMap tm, boolean setBaseName) throws TopicMapException {
        Object val=datum.get(field);
        if(val==null) {
            System.out.println("Extraction helper can't find field '"+field+"'.");
            return null;
        }
        if(val instanceof Collection){
            Collection c=(Collection)val;
            Iterator iter=c.iterator();
            String subval=(String)iter.next();
            String si=makeSI(field,subval);
            Topic t=getOrCreateTopic(tm,si);
            if(setBaseName) t.setBaseName(field);
            return t;
        }
        else{
            String si=makeSI(field,((String)val).trim());
            Vector v=new Vector();
            Topic t=getOrCreateTopic(tm,si);
            if(setBaseName) t.setBaseName(((String)val).trim());
            return t;
        }        
    }
    
    public Collection getOrCreateTopics(Map datum,String field,TopicMap tm) throws TopicMapException {
        return getOrCreateTopics(datum,field,tm,false);
    }
    
    public Collection getOrCreateTopics(Map datum,String field,TopicMap tm,boolean setBaseName) throws TopicMapException {
        Object val=datum.get(field);
        if(val==null){
            System.out.println("Extraction helper can't find field '"+field+"'.");
            return null;
        }
        
        if(val instanceof Collection) {
            Collection c=(Collection)val;
            Iterator iter=c.iterator();
            Vector v=new Vector();
            while(iter.hasNext()){
                String subval=(String)iter.next();
                String si=makeSI(field,subval);
                Topic t=getOrCreateTopic(tm,si);
                if(setBaseName) t.setBaseName(subval);
                v.add(t);
            }
            //System.out.println("Extraction helper found field and created collection of topics: " + v.size());
            return v;
        }
        else {
            String si=makeSI(field,((String)val).trim());
            Vector v=new Vector();
            Topic t=getOrCreateTopic(tm,si);
            if(setBaseName) t.setBaseName(((String)val).trim());
            v.add(t);
            //System.out.println("Extraction helper found field and created topic.");
            return v;
        }
    }
    
}
