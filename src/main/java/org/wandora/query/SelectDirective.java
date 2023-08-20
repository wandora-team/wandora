/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * 
 *
 * SelectDirective.java
 *
 * Created on 25. lokakuuta 2007, 11:04
 */

package org.wandora.query;
import java.util.ArrayList;
import java.util.Collection;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.GripCollections;

/**
 * @deprecated
 *
 * @author olli
 */
public class SelectDirective implements Directive {
    
    private Locator type;
    private Locator rewriteType;
    private ArrayList<Locator> roles;
    private ArrayList<Locator> rewriteRoles;
    
    /** Creates a new instance of SelectDirective */
    public SelectDirective(Locator type,Collection<Locator> roles) {
        this.type=type;
        this.roles=new ArrayList<Locator>(roles);
        processTempRoles();
    }
    public SelectDirective(String type,Collection<String> roles) {
        this(new Locator(type),QueryTools.makeLocatorArray(roles));
    }
    public SelectDirective(String type,String ... roles){
        this(new Locator(type),QueryTools.makeLocatorArray(GripCollections.arrayToCollection(roles)));
    }
    public SelectDirective(String type,String role1){
        this(type,new String[]{role1});
    }
    public SelectDirective(String type,String role1,String role2){
        this(type,new String[]{role1,role2});
    }
    public SelectDirective(String type,String role1,String role2,String role3){
        this(type,new String[]{role1,role2,role3});
    }
    public SelectDirective(Locator type,Collection<Locator> roles,Collection<Locator> rewriteRoles) {
        this(type,roles);
        this.rewriteRoles=new ArrayList<Locator>(rewriteRoles);
    }
    public SelectDirective(String type,Collection<String> roles,Collection<String> rewriteRoles) {
        this(new Locator(type),QueryTools.makeLocatorArray(roles),QueryTools.makeLocatorArray(rewriteRoles));
    }
    public SelectDirective(Locator type,Locator rewriteType,Collection<Locator> roles,Collection<Locator> rewriteRoles) {
        this(type,roles,rewriteRoles);
        this.rewriteType=rewriteType;
    }
    public SelectDirective(String type,String rewriteType,Collection<String> roles,Collection<String> rewriteRoles) {
        this(new Locator(type),rewriteType==null?null:new Locator(rewriteType),QueryTools.makeLocatorArray(roles),QueryTools.makeLocatorArray(rewriteRoles));
    }
    
    private void processTempRoles(){
        boolean process=false;
        for(Locator l : roles) if(l.toString().startsWith("~") /*|| l.toString().startsWith("|")*/) { process=true; break; }
        if(!process) return;
        
        this.rewriteRoles=new ArrayList<Locator>();
        ArrayList<Locator> newRoles=new ArrayList<Locator>();
        for(Locator l : roles){
            rewriteRoles.add(l);
            if(l.toString().startsWith("~")) newRoles.add(new Locator(l.toString().substring(1)));
//            if(l.toString().startsWith("|")) newRoles.add(new Locator(l.toString().substring(1)));
            else newRoles.add(l);
        }
        this.roles=newRoles;
    }
    
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        Topic contextTopic=context.getContextTopic();
        TopicMap tm=contextTopic.getTopicMap();
        Topic typeTopic=tm.getTopic(type);
        if(typeTopic==null) return new ArrayList<ResultRow>();
        ArrayList<Topic> roleTopics=new ArrayList<Topic>();
        for(Locator r : roles){
            Topic role=tm.getTopic(r);
            if(role==null) return new ArrayList<ResultRow>();
            roleTopics.add(role);
        }
        
        
        Collection<Association> associations=contextTopic.getAssociations(typeTopic);
        
        ArrayList<ResultRow> results=new ArrayList<ResultRow>();
        AssociationLoop: for(Association a : associations){
            ArrayList<Object> players=new ArrayList<Object>();
            for(int i=0;i<roleTopics.size();i++){
                Topic role=roleTopics.get(i);
                Topic player=a.getPlayer(role);
/*                if(player==null) {
                    if(rewriteRoles==null || !rewriteRoles.get(i).toString().startsWith("|"))
                        continue AssociationLoop;
                }*/
                players.add(player==null?null:player.getOneSubjectIdentifier());
            }
            ResultRow row=new ResultRow(rewriteType!=null?rewriteType:type,
                                        rewriteRoles!=null?rewriteRoles:roles,
                                        players);
            results.add(row);
        }
        return results;
    }
    public boolean isContextSensitive(){
        return true;
    }

}
