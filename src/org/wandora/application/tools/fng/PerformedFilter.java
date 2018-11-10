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
 * PerformedFilter.java
 *
 * Created on August 26, 2004, 11:19 AM
 */

package org.wandora.application.tools.fng;

import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.piccolo.Logger;
import java.util.*;
import java.util.regex.*;


/**
 *
 * @author  olli
 */
public class PerformedFilter {
    
    /** Creates a new instance of PerformedFilter */
    public PerformedFilter() {
    }

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying PerformedFilter filter");
        
        Topic performed=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P14B_performed");
        Topic timeAppellation=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E49_Time_Appellation");
        Topic person=tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-henkilö");
        Topic carriedOutBy=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P14F_carried_out_by");
        Topic untimed=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#ajoittamaton");
        Topic string=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E62_String");
        Topic inRole=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P14.1F_in_the_role_of");
        if(performed==null || timeAppellation==null || person==null || untimed==null || inRole==null || carriedOutBy==null){
            logger.writelog("Couldn't find all needed topics.");
            return tm;
        }
        int counter=0,counter2=0,counter3=0;
        Iterator<Association> iter=new ArrayList(tm.getAssociationsOfType(performed)).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            if(a.isRemoved()) continue;
            Topic event=a.getPlayer(performed);
            if(event==null) continue;
            Topic time=a.getPlayer(timeAppellation);
            if(time==null){
                String year=null;
                String n=event.getBaseName();
                if(n!=null){
                    Pattern p=Pattern.compile(".*(^|[^\\d])(\\d\\d\\d\\d)([^\\d]|$).*");
                    Matcher m=p.matcher(n);
                    if(m.matches()){
                        year=m.group(2);
                    }
                }
                if(year!=null) year=year.trim();
                if(year==null || year.length()<4){
                    a.addPlayer(untimed, timeAppellation);
                    time=untimed;
                }
                else{
                    Topic nt=tm.createTopic();
                    nt.setBaseName(year);
                    nt.addType(timeAppellation);
                    nt.addType(string);
                    a.addPlayer(nt,timeAppellation);
                    time=nt;
                }
                counter++;
            }
            Topic role=a.getPlayer(inRole);
            if(role==null){
                a.addPlayer(person, inRole);
            }
            Topic per=a.getPlayer(carriedOutBy);
            if(per==null) {
                continue;
            }
            String ts=time.getBaseName();
            String es=event.getBaseName().trim();
            String ps=per.getBaseName();
            if(ts==null || es==null || ps==null) {
                continue;
            }
            if(es.equals(ts+" ("+ps+")")){
                a.removePlayer(performed);
                a.addPlayer(role,performed);
                event=role;
                es=event.getBaseName();
                counter2++;
            }
            
            Pattern p=Pattern.compile("^\\s*(\\d{4})?(.*?)(\\d{4})?\\s*$");
            Matcher m=p.matcher(es);
            m.matches();
            String temp=m.group(2);
            if(temp!=null && !temp.matches(".*((\\d\\d)|(\\d{1,2}\\.\\d{1,2})).*")){
                if(m.group(1)!=null && m.group(1).length()>0 && m.group(1).trim().equals(ts)){
                    es=es.replaceFirst("^\\s*(\\d{4})","");
                    setName(event,es);
                    counter3++;
                }
                else if(m.group(3)!=null && m.group(3).length()>0 && m.group(3).trim().equals(ts)){
                    es=es.replaceFirst("(\\d{4})\\s*$","");
                    setName(event,es);
                    counter3++;
                }
            }
        }
        logger.writelog("Added "+counter+" time appellations. Replaced "+counter2+" redundant events. Removed year from "+counter3+" events");
        return tm;
    }
    
    
    
    
    public void setName(Topic t,String name) throws TopicMapException { 
        String oldName=t.getBaseName();
        t.setBaseName(name);
        Iterator<Set<Topic>> iter=new ArrayList(t.getVariantScopes()).iterator();
        while(iter.hasNext()){
            Set<Topic> scope=iter.next();
            String n=t.getVariant(scope);
            if(n.equals(oldName)){
                t.removeVariant(scope);
                t.setVariant(scope,name);
            }
        }
    }
}
