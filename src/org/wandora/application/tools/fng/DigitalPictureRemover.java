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
 * DigitalPictureRemover.java
 *
 * Created on August 19, 2004, 2:07 PM
 */

package org.wandora.application.tools.fng;
import org.wandora.topicmap.TopicInUseException;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TMBox;
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
public class DigitalPictureRemover {
    
    /** Creates a new instance of DigitalPictureRemover */
    public DigitalPictureRemover() {
    }

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying DigitalPictureRemover filter");
        Topic photograph=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E73_Information_ObjectValokuvanro");
        Topic digikuva=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E73_Information_ObjectDigikuvanro");
        Topic work=tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-teos");
        Topic desc=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#sisältökuvaus");
        Topic name=tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-nimi");
        Topic hasType=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P2F_has_type");
        Topic hasTitle=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#rP102F_has_title");
        Topic depicts=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#rP62F_depicts");
        Topic depictedBy=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#rP62B_is_depicted_by");
        Topic performed=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#rP14B_performed");
        Topic technique=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#rP32B_was_technique_of");
        Topic producedBy=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P108B_was_produced_by");
        if(desc!=null){
            desc.addSubjectIdentifier(tm.createLocator("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#sisältökuvaus_(1)"));
            desc.addSubjectIdentifier(tm.createLocator("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#sisältökuvaus_(2)"));
            desc.addSubjectIdentifier(tm.createLocator("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#sisältökuvaus_(3)"));
            desc.removeSubjectIdentifier(tm.createLocator("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#sisältökuvaus_(1)"));
            desc.removeSubjectIdentifier(tm.createLocator("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#sisältökuvaus_(2)"));
            desc.removeSubjectIdentifier(tm.createLocator("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#sisältökuvaus_(3)"));
        }
        if(photograph==null || digikuva==null || work==null || desc==null || name==null || 
            hasType==null || hasTitle==null || depicts==null || depictedBy==null ||
            performed==null || technique==null || producedBy==null ){
            logger.writelog("Coludn't find all needed topics, aborting.");
            return tm;            
        }
        Topic display=TMBox.getOrCreateTopic(tm, XTMPSI.DISPLAY);
        Topic langfi=TMBox.getOrCreateTopic(tm, XTMPSI.getLang("fi"));
        
        int counter=0;
        HashSet pictures=new HashSet();
        Iterator iter=tm.getTopicsOfType(photograph).iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(!t.isOfType(work)) pictures.add(t);
        }
        iter=tm.getTopicsOfType(digikuva).iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(!t.isOfType(work)) pictures.add(t);
        }
        Vector descriptions=new Vector();
        iter=pictures.iterator();
        int counter3=0;
        while(iter.hasNext()){
            Topic pict=(Topic)iter.next();
            Topic depict=null;
            String descS="";
//            Iterator iter2=pict.getAssociations(name,hasTitle).iterator();
            Iterator iter2=pict.getAssociations().iterator();
            Collection tobedeleted=new Vector();
            while(iter2.hasNext()){
                Association a=(Association)iter2.next();
                
                if(a.getType()==producedBy){
                    tobedeleted.add(a);
                    continue;
                }
                
                if(a.getType()!=name || a.getPlayer(hasTitle)!=pict){
                    continue;
                }
                
                Topic descT=a.getPlayer(name);
                if(descT==null || a.getPlayer(hasType)!=desc) continue;
                if(descT.getBaseName()==null) continue;
                descriptions.add(descT);
                if(descS.length()!=0) descS+="; ";
                descS+=descT.getBaseName();
            }
            iter2=tobedeleted.iterator();
            while(iter2.hasNext()){
                Association a=(Association)iter2.next();
                if(a.isRemoved()) continue;
                Topic player=a.getPlayer(performed);
                if(player!=null) try{player.remove();counter3++;}catch(TopicInUseException tiue){}
                player=a.getPlayer(technique);
                if(player!=null) try{player.remove();counter3++;}catch(TopicInUseException tiue){}
            }
            iter2=pict.getAssociations(depicts,depicts).iterator();
            while(iter2.hasNext()){
                Association a=(Association)iter2.next();
                depict=a.getPlayer(depictedBy);
                if(depict!=null) break;
            }
            descS=descS.trim();
            if(depict!=null && descS.length()>0){
                depict.setData(desc,langfi,descS);
                counter++;
            }
            try{
                pict.remove();
            }catch(TopicInUseException tiue){}
        }
        int counter2=0;
        iter=descriptions.iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            try{
                t.remove();
                counter2++;
            }catch(TopicInUseException tiue){}
        }
        logger.writelog("Deleted "+pictures.size()+" topics. Set description to "+counter+" topics. Removed "+counter2+" description topics and "+counter3+" authors or techniques");
        return tm;
    }    
}
