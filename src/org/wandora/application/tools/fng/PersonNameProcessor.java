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
 * PersonNameProcessor.java
 *
 * Created on August 26, 2004, 10:21 AM
 */

package org.wandora.application.tools.fng;


import org.wandora.topicmap.TopicInUseException;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.piccolo.WandoraManager;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TMBox;
import org.wandora.piccolo.Logger;
import java.util.*;


/**
 *
 * @author  olli
 */
public class PersonNameProcessor {
    
    /** Creates a new instance of PersonNameProcessor */
    public PersonNameProcessor() {
    }

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying PersonName filter");
        
        Topic actorAppellation=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E82_Actor_Appellation");
        Topic person=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E21_Person");
        Topic fi=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("fi"));
        Topic en=TMBox.getOrCreateTopic(tm,XTMPSI.getLang("en"));
        Topic disp=TMBox.getOrCreateTopic(tm,XTMPSI.DISPLAY);
        Topic indep=TMBox.getOrCreateTopic(tm,WandoraManager.LANGINDEPENDENT_SI);
        Topic hasType=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E55_Type");
        Topic officialName=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#virallinen_nimi");
        Topic isType=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P2B_is_type_of");
/*        Topic ident=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#henkilötunnus");
        Topic identifiedBy=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P78F_is_identified_by");
        Topic indep=TMBox.getOrCreateTopic(tm,WandoraManager.LANGINDEPENDENT_SI);*/
        if(actorAppellation==null || person==null || hasType==null || officialName==null || isType==null /*|| ident==null || identifiedBy==null*/){
            logger.writelog("Couldn't find all needed topics.");
            return tm;
        }
        int counter=0;
        Iterator<Topic> iter=new ArrayList<>(tm.getTopicsOfType(actorAppellation)).iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            
/*            Iterator iter2=new ArrayList(t.getAssociations(hasType,isType)).iterator();
            while(iter2.hasNext()){
                Association a=(Association)iter2.next();
                Topic ty=a.getPlayer(hasType);
                if(ty==ident){
                    Iterator iter3=t.getAssociations(identifiedBy,actorAppellation).iterator();
                    while(iter3.hasNext()){
                        Association a2=(Association)iter3.next();
                        Topic p=a2.getPlayer(person);
                        p.setData(ident,indep,t.getBaseName());
                    }
                }
            }*/
            
            if(t.getTypes().size()==1) {
                try{
                    t.remove();
                    counter++;
                }catch(TopicInUseException tiue){}
            }
            else{
                t.removeType(actorAppellation);
                Iterator<Association> iter2=new ArrayList<>(t.getAssociations(hasType,isType)).iterator();
                while(iter2.hasNext()){
                    Association a=(Association)iter2.next();
                    if(a.getPlayer(hasType)==officialName){
                        a.remove();
                    }
                }
            }
        }
        int counter2=0;
        iter=tm.getTopicsOfType(person).iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            Iterator<Set<Topic>> iter2=new ArrayList<>(t.getVariantScopes()).iterator();
            while(iter2.hasNext()){
                Set<Topic> scope=iter2.next();
                t.removeVariant(scope);
            }
            HashSet<Topic> scope=new HashSet<>();
            scope.add(fi); scope.add(disp);
            t.setVariant(scope,t.getBaseName());
            scope=new HashSet<>();
            scope.add(en); scope.add(disp);
            t.setVariant(scope,t.getBaseName());
            counter2++;
        }
        logger.writelog("Deleted "+counter+" actor appellations. Changed display name in "+counter2+" persons.");
        return tm;
    }    
}
