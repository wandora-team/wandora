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
 * TechniqueSplitter.java
 *
 * Created on August 25, 2004, 3:22 PM
 */

package org.wandora.application.tools.fng;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.*;
import org.wandora.*;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.utils.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author  olli
 */
public class TechniqueSplitter {
    
    /** Creates a new instance of TechniqueSplitter */
    public TechniqueSplitter() {
    }

    public TopicMap process(TopicMap tm,Logger logger)throws TopicMapException {
        logger.writelog("Applying TechniqueSplitter filter");
        
        Topic techniqueOf=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#rP32B_was_technique_of");
        Topic work=tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-teos");
        if(work==null || techniqueOf==null){
            logger.writelog("Couldn't find all needed topics.");
            return tm;
        }
        
        int counter=0;
        Iterator iter=new ArrayList(tm.getTopicsOfType(techniqueOf)).iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            String name=t.getBaseName();
            if(name==null) continue;
            if(name.matches(".*/.*")) continue;
            if(name.matches(".*(ja|JA|Ja).*")) continue;
            if(name.matches(".*,\\s*jo.*")) continue;
            if(name.indexOf(',')==-1) continue;
            
            Collection works=new Vector();
            Iterator iter2=new ArrayList(t.getAssociations(techniqueOf,techniqueOf)).iterator();
            while(iter2.hasNext()){
                Association a=(Association)iter2.next();
                Topic w=a.getPlayer(work);
                works.add(w);
            }
            
            StringTokenizer st=new StringTokenizer(name,",");
            String base=st.nextToken().trim();
            setNewName(t,base);
            while(st.hasMoreTokens()){
                String token=st.nextToken().trim();
                Topic nt=tm.getTopicWithBaseName(token);
                if(nt==null) nt=tm.createTopic();
                setNewName(nt,token);
                iter2=works.iterator();
                while(iter2.hasNext()){
                    Topic w=(Topic)iter2.next();
                    Association na=tm.createAssociation(techniqueOf);
                    na.addPlayer(nt,techniqueOf);
                    na.addPlayer(w,work);
                }
                counter++;
            }
        }
        logger.writelog("Split "+counter+" techniques.");
        return tm;
    }    
    
    public void setNewName(Topic t,String newName) throws TopicMapException {
        String oldName=t.getBaseName();
        t.setBaseName(newName);
        Iterator iter=new ArrayList(t.getVariantScopes()).iterator();
        while(iter.hasNext()){
            Set scope=(Set)iter.next();
            String v=t.getVariant(scope);
            if(v.equals(oldName)){
                t.setVariant(scope, newName);
            }
        }
        while(t.getOneSubjectIdentifier()!=null){
            t.removeSubjectIdentifier(t.getOneSubjectIdentifier());
        }
        t.addSubjectIdentifier(t.getTopicMap().createLocator(makeLocatorFor(newName)));
    }
    
    public String makeLocatorFor(String name){
        String prefix="http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#";
        name=name.replaceAll("( |\\?|\\&)","_");
        return prefix+name;
    }
    
}
