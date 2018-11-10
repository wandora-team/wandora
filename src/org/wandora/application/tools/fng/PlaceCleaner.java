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
 * PlaceCleaner.java
 *
 * Created on August 25, 2004, 12:21 PM
 */

package org.wandora.application.tools.fng;


import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TMBox;
import org.wandora.piccolo.Logger;
import java.util.*;


/**
 *
 * @author  olli
 */
public class PlaceCleaner {
    private String[] split;
    
    /** Creates a new instance of PlaceCleaner */
    public PlaceCleaner(String[] split) {
        this.split=split;
    }

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying PlaceCleaner filter");
        
//        Topic place=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E53_Place");
        Topic contains=TMBox.getOrCreateTopic(tm,"http://wandora.org/si/common/contains");
        Topic container=TMBox.getOrCreateTopic(tm,"http://wandora.org/si/common/container");
        Topic element=TMBox.getOrCreateTopic(tm,"http://wandora.org/si/common/element");
/*        if(place==null){
            logger.writelog("Couldn't find all needed topics.");
            return tm;
        }*/
        
        Topic[] sts=new Topic[split.length];
        for(int i=0;i<sts.length;i++){
            sts[i]=tm.getTopic(split[i]);
            if(sts[i]==null){
                logger.writelog("Couldn't find topic "+split[i]+". Aborting.");
                return tm;
            }
        }
        
        int counter=0;
        for(int i=0;i<sts.length;i++){
            Topic t=sts[i];
            String name=t.getBaseName();
            // NOTE: do not put topics in split list where semicolon has been used the correct way, this filter will break such topics
            StringTokenizer st=new StringTokenizer(name,",;");
            String base=st.nextToken().trim();
            setNewName(t,base);
            HashSet<Topic> types=new HashSet<>();
            types.addAll(t.getTypes());
            Topic last=t;
            while(st.hasMoreTokens()){
                String token=st.nextToken().trim();
                Topic nt=tm.createTopic();
                setNewName(nt,token);
                
                Iterator<Topic> iter=types.iterator();
                while(iter.hasNext()){
                    Topic type=(Topic)iter.next();
                    nt.addType(type);
                }
                Association na=tm.createAssociation(contains);
                na.addPlayer(last,element);
                na.addPlayer(nt,container);
                last=nt;
                counter++;
            }
        }
        logger.writelog("Made "+counter+" new topics");
        return tm;
    }    
    
    public void setNewName(Topic t,String newName) throws TopicMapException {
        String oldName=t.getBaseName();
        t.setBaseName(newName);
        Iterator<Set<Topic>> iter=new ArrayList<>(t.getVariantScopes()).iterator();
        while(iter.hasNext()){
            Set<Topic> scope=iter.next();
            String v=t.getVariant(scope);
            if(v.equals(oldName)){
                t.setVariant(scope, newName);
            }
        }
        t.addSubjectIdentifier(t.getTopicMap().createLocator(makeLocatorFor(newName)));
    }
    
    public String makeLocatorFor(String name){
        String prefix="http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#";
        name=name.replaceAll("( |\\?|\\&)","_");
        return prefix+name;
    }
}
