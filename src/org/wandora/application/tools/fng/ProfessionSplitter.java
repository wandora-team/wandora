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
 * ProfessionSplitter.java
 *
 * Created on August 18, 2004, 4:04 PM
 */

package org.wandora.application.tools.fng;
import org.wandora.piccolo.utils.URLEncoder;
import org.wandora.topicmap.TopicInUseException;
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
public class ProfessionSplitter {
    
    /** Creates a new instance of ProfessionSplitter */
    public ProfessionSplitter() {
    }
    
    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying ProfessionSplitter filter");
        Topic luokitus=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P2F_has_type");
        Topic edustaja=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P2B_is_type_of");
        Topic ammatti=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#ammatti");
        // note that these two subject identifiers are the wrong way on purpose
        Topic carriedOut=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#rP14B_performed");
        Topic performed=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#rP14F_carried_out_by");
        Topic hadPurpose=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P21F_had_general_purpose");
        if(luokitus==null || edustaja==null || ammatti==null || carriedOut==null ||
            performed==null || hadPurpose==null){
            logger.writelog("Coludn't find all needed topics, aborting.");
            return tm;
        }
        URLEncoder ue=new URLEncoder();
        Vector v=new Vector();
        Iterator iter=tm.getAssociationsOfType(luokitus).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic player=a.getPlayer(luokitus);
            if(player==ammatti) v.add(a);
        }
        iter=v.iterator();
        int counter=0;
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            if(a.getRoles().size()>2) continue;
            Topic prof=a.getPlayer(edustaja);
            a.remove();
            String name=prof.getBaseName().toLowerCase().trim();
            if(name.startsWith("ammatti")) name=name.substring("ammatti".length()).trim();
            StringTokenizer st=new StringTokenizer(name,",");
            Vector newProfs=new Vector();
            while(st.hasMoreTokens()){
                String token=st.nextToken().trim();
                token=token.substring(0,1).toUpperCase()+token.substring(1);
                token=token.replaceAll("\\s*(\\d{2,4}(/\\d{1,2})?-)?\\d{2,4}(/\\d{1,2})?$","");
                token=token.trim();
                if(token.length()==0) continue;
                Topic newProf=tm.getTopicWithBaseName(token);
                if(newProf==null){
                    newProf=tm.createTopic();
                    newProf.setBaseName(token);
                    counter++;
                }
                if(newProf.getSubjectIdentifiers().isEmpty())
                    newProf.addSubjectIdentifier(tm.createLocator("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#ammatti_"+ue.encode(token)));
                newProf.addType(ammatti);
                newProfs.add(newProf);
            }
            
            Vector u=new Vector();
            Iterator iter2=prof.getAssociations(performed,performed).iterator();
            while(iter2.hasNext()){
                Association a2=(Association)iter2.next();
                if(a2.getRoles().size()!=3) continue;
                if(a2.getPlayer(hadPurpose)!=ammatti) continue;
                if(a2.getPlayer(carriedOut)==null) continue;
                u.add(a2);
            }
            iter2=u.iterator();
            while(iter2.hasNext()){
                Association a2=(Association)iter2.next();
                Topic player=a2.getPlayer(carriedOut);
                a2.remove();
                Iterator iter3=newProfs.iterator();
                while(iter3.hasNext()){
                    Topic newProf=(Topic)iter3.next();
                    Association newa=tm.createAssociation(ammatti);
                    newa.addPlayer(newProf,ammatti);
                    newa.addPlayer(player,carriedOut);
                }
            }
            try{
                prof.remove();
            }catch(TopicInUseException tiue){}
        }
        logger.writelog("Splitted "+v.size()+" professions into "+counter+" new topics");
        return tm;
    }
}
