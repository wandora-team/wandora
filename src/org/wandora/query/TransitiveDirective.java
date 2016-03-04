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
 */

package org.wandora.query;
import org.wandora.topicmap.*;
import org.wandora.utils.GripCollections;
import java.util.*;

/**
 * @deprecated
 *
 * @author olli
 */
public class TransitiveDirective implements Directive {

    private Locator associationType;
    private Locator role1;
    private Locator role2;
    
    public TransitiveDirective(Locator associationType,Locator role1,Locator role2){
        this.associationType=associationType;
        this.role1=role1;
        this.role2=role2;
    } 
    public TransitiveDirective(String associationType,String role1,String role2){
        this(new Locator(associationType),new Locator(role1),new Locator(role2));
    } 
    
    private ArrayList<ResultRow> query(Topic context,Locator atypel,Locator r1l,Locator r2l) throws TopicMapException{
        TopicMap tm=context.getTopicMap();
        Topic atype=tm.getTopic(atypel);
        Topic r1=tm.getTopic(r1l);
        Topic r2=tm.getTopic(r2l);
        if(atype==null || r1==null || r2==null) return new ArrayList<ResultRow>();
        ArrayList<Locator> collected=new ArrayList<Locator>();
        HashSet<Locator> processed=new HashSet<Locator>();
        ArrayList<Topic> unprocessed=new ArrayList<Topic>();
        unprocessed.add(context);
        while(!unprocessed.isEmpty()){
            ArrayList<Topic> next=new ArrayList<Topic>();
            for(Topic t : unprocessed){
                Collection<Association> as=t.getAssociations(atype, r1);
                for(Association a : as){
                    Topic p=a.getPlayer(r2);
                    if(p==null) continue;
                    Locator si=p.getOneSubjectIdentifier();
                    if(si==null) continue;
                    if(processed.contains(si)) continue;
                    processed.add(si);
                    next.add(p);
                    collected.add(si);
                }
            }
            unprocessed=next;
        }
        
        ArrayList<ResultRow> ret=new ArrayList<ResultRow>();
        Locator cl=context.getOneSubjectIdentifier();
        for(Locator l : collected){
            ResultRow row=new ResultRow(atypel,r1l,cl,r2l,l);
            ret.add(row);
        }
        return ret;
    }
    
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        Topic contextTopic=context.getContextTopic();
        TopicMap tm=contextTopic.getTopicMap();
        ArrayList<ResultRow> ret=query(contextTopic,associationType,role1,role2);
        ret.addAll(query(contextTopic,associationType,role2,role1));
        return ret;
    }
    public boolean isContextSensitive(){
        return true;
    }

}
