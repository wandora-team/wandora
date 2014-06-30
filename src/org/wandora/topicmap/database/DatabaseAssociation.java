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
 * 
 *
 * DatabaseAssociation.java
 *
 * Created on 7. marraskuuta 2005, 11:28
 */

package org.wandora.topicmap.database;
import org.wandora.topicmap.*;
import java.util.*;
import java.sql.*;

/**
 *
 * @author olli
 */
public class DatabaseAssociation implements Association {
    
    protected DatabaseTopicMap topicMap;
    protected String id;
    protected boolean full;
    protected DatabaseTopic type;
    protected Hashtable<Topic,Topic> players;
    protected boolean removed=false;
    
    /** Creates a new instance of DatabaseAssociation */
    public DatabaseAssociation(DatabaseTopic type,DatabaseTopicMap topicMap) {
        this.type=type;
        this.topicMap=topicMap;
        full=false;
    }
    public DatabaseAssociation(DatabaseTopic type,String id,DatabaseTopicMap topicMap) {
        this(type,topicMap);
        this.id=id;
    }
    public DatabaseAssociation(String id,DatabaseTopicMap topicMap) {
        this.topicMap=topicMap;
        this.id=id;
        full=false;
    }
    public DatabaseAssociation(Map<String,Object> row,DatabaseTopicMap topicMap) throws TopicMapException {
        this(topicMap.buildTopic(row),topicMap);
        this.id=row.get("ASSOCIATIONID").toString();
    }
    
    void initialize(DatabaseTopic type){
        this.type=type;
    }
    
    protected String escapeSQL(String s){
        return topicMap.escapeSQL(s);
    }

    private static int idcounter=0;    
    protected static synchronized String makeID(){
        if(idcounter>=100000) idcounter=0;
        return "A"+System.currentTimeMillis()+"-"+(idcounter++);
    }
    
    public String getID(){
        return id;
    }
    
    void create() throws TopicMapException {
        players=new Hashtable<Topic,Topic>();
        full=true;
        id=makeID();
        topicMap.executeUpdate("insert into ASSOCIATION (ASSOCIATIONID,TYPE) values ('"+escapeSQL(id)+"','"+escapeSQL(type.getID())+"')");
    }

    static HashMap<String,DatabaseTopic> makeFullAll(Collection<Map<String,Object>> res,HashMap<String,DatabaseAssociation> associations,DatabaseTopicMap topicMap) throws TopicMapException {
        HashMap<String,DatabaseTopic> collected=new HashMap<String,DatabaseTopic>();
        String associationID=null;
        Hashtable<Topic,Topic> players=null;
        for(Map<String,Object> row : res){
            if(associationID==null || !associationID.equals(row.get("ASSOCIATION"))){
                if(associationID!=null){
                    DatabaseAssociation dba=associations.get(associationID);
                    if(dba!=null && !dba.full){
                        dba.full=true;
                        dba.players=players;
                    }
                }
                players=new Hashtable<Topic,Topic>();
                associationID=row.get("ASSOCIATION").toString();
            }
            DatabaseTopic player=topicMap.buildTopic(row.get("PLAYERID"),row.get("PLAYERBN"),row.get("PLAYERSL"));
            DatabaseTopic role=topicMap.buildTopic(row.get("ROLEID"),row.get("ROLEBN"),row.get("ROLESL"));            
            collected.put(player.getID(),player);
            collected.put(role.getID(),role);
            players.put(role,player);
        }
        if(associationID!=null){
            DatabaseAssociation dba=associations.get(associationID);
            if(dba!=null && !dba.full){
                dba.full=true;
                dba.players=players;
            }
        }
        return collected;
    }
    
    void makeFull() throws TopicMapException {
        full=true;
        Collection<Map<String,Object>> res=
                topicMap.executeQuery("select P.TOPICID as PLAYERID, P.BASENAME as PLAYERNAME, P.SUBJECTLOCATOR as PLAYERSL, "+
                              "R.TOPICID as ROLEID, R.BASENAME as ROLENAME, R.SUBJECTLOCATOR as ROLESL from "+
                              "TOPIC as P,TOPIC as R, MEMBER as M where P.TOPICID=M.PLAYER and "+
                              "R.TOPICID=M.ROLE and M.ASSOCIATION='"+escapeSQL(id)+"'");
        players=new Hashtable<Topic,Topic>();
        for(Map<String,Object> row : res){
            Topic role=topicMap.buildTopic(row.get("ROLEID"),row.get("ROLENAME"),row.get("ROLESL"));
            Topic player=topicMap.buildTopic(row.get("PLAYERID"),row.get("PLAYERNAME"),row.get("PLAYERSL"));
            players.put(role,player);
        }
    }
    
    public Topic getType() throws TopicMapException {
        return type;
    }
    public void setType(Topic t) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(!full) makeFull();
        for(Map.Entry<Topic,Topic> e : players.entrySet()){
            Topic role=e.getKey();
            Topic player=e.getValue();
            ((DatabaseTopic)player).associationChanged(this, t, type, role, role);
        }
        Topic old=type;
        type=(DatabaseTopic)t;
        topicMap.executeUpdate("update ASSOCIATION set TYPE='"+escapeSQL(type.getID())+"' where ASSOCIATIONID='"+escapeSQL(id)+"'");
        topicMap.associationTypeChanged(this,t,old);
    }
    public Topic getPlayer(Topic role) throws TopicMapException {
        if(!full) makeFull();
        return players.get(role);
    }
    public void addPlayer(Topic player,Topic role) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(!full) makeFull();
        Topic old=players.get(role);
        if(old!=null){
            ((DatabaseTopic)old).associationChanged(this, null, type,null, role);
            topicMap.executeUpdate("update MEMBER set PLAYER='"+escapeSQL(player.getID())+"' where "+
                               "ASSOCIATION='"+escapeSQL(id)+"' and ROLE='"+escapeSQL(role.getID())+"'");
        }
        else{
            topicMap.executeUpdate("insert into MEMBER (ASSOCIATION,PLAYER,ROLE) values "+
                    "('"+escapeSQL(id)+"','"+escapeSQL(player.getID())+"','"+escapeSQL(role.getID())+"')");
        }
        ((DatabaseTopic)player).associationChanged(this,type,null,role,null);
        players.put(role,player);
        if(topicMap.getConsistencyCheck()) checkRedundancy();
        topicMap.associationPlayerChanged(this,role,player,old);
    }
    public void addPlayers(Map<Topic,Topic> players) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        boolean changed=false;
        for(Map.Entry<Topic,Topic> e : players.entrySet()){
            DatabaseTopic role=(DatabaseTopic)e.getKey();
            DatabaseTopic player=(DatabaseTopic)e.getValue();
            Topic old=this.players.get(role);
            if(old!=null){
                ((DatabaseTopic)old).associationChanged(this, null, type,null, role);
                topicMap.executeUpdate("update MEMBER set PLAYER='"+escapeSQL(player.getID())+"' where "+
                                   "ASSOCIATION='"+escapeSQL(id)+"' and ROLE='"+escapeSQL(role.getID())+"'");
            }
            else{
                topicMap.executeUpdate("insert into MEMBER (ASSOCIATION,PLAYER,ROLE) values "+
                        "('"+escapeSQL(id)+"','"+escapeSQL(player.getID())+"','"+escapeSQL(role.getID())+"')");
            }
            ((DatabaseTopic)player).associationChanged(this,type,null,role,null);
            this.players.put(role,player);
            topicMap.associationPlayerChanged(this,role,player,old);
        }
        if(topicMap.getConsistencyCheck()) checkRedundancy();
    }    
    public void removePlayer(Topic role) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(!full) makeFull();
        Topic old=players.get(role);
        if(old!=null){
            ((DatabaseTopic)old).associationChanged(this, null, type,null, role);
            topicMap.executeUpdate("delete from MEMBER where "+
                               "ASSOCIATION='"+escapeSQL(id)+"' and ROLE='"+escapeSQL(role.getID())+"'");
        }
        players.remove(role);
        checkRedundancy();
        topicMap.associationPlayerChanged(this,role,null,old);
    }
    public Collection<Topic> getRoles() throws TopicMapException {
        if(!full) makeFull();
        return players.keySet();
    }
    public TopicMap getTopicMap(){
        return topicMap;
    }
    public void remove() throws TopicMapException {
        if(!full) makeFull();
        removed=true;
        for(Map.Entry<Topic,Topic> e : players.entrySet()){
            Topic role=e.getKey();
            Topic player=e.getValue();
            ((DatabaseTopic)player).associationChanged(this, null, type, null, role);
        }        
        topicMap.executeUpdate("delete from MEMBER where ASSOCIATION='"+escapeSQL(id)+"'");
        topicMap.executeUpdate("delete from ASSOCIATION where ASSOCIATIONID='"+escapeSQL(id)+"'");
        topicMap.associationRemoved(this);
    }
    public boolean isRemoved() throws TopicMapException {
        return removed;
    }
    
    public void checkRedundancy() throws TopicMapException {
        if(players.isEmpty()) return;
        if(type==null) return;
        Collection<Association> smallest=null;
        for(Topic role : players.keySet()){
            Topic player=players.get(role);
            Collection<Association> c=player.getAssociations(type,role);
            if(smallest==null || c.size()<smallest.size()) smallest=c;
            // getAssociations may be a timeconsuming method so don't check everything if not necessary
            if(smallest!=null && smallest.size()<50) break; 
        }
        HashSet<Association> delete=new HashSet();
        for(Association a : smallest){
            if(a==this) continue;
            if(((DatabaseAssociation)a)._equals(this)){
                delete.add(a);
            }
        }
        for(Association a : delete){ 
            a.remove();
        }        
    }
    
    boolean _equals(DatabaseAssociation a){
        return (players.equals(a.players)&&(type==a.type));
    }
    int _hashCode(){
        return players.hashCode()+type.hashCode();
    }
    
    /*
    public int hashCode(){
        return id.hashCode();
    }
    public boolean equals(Object o){
        if(o instanceof DatabaseAssociation){
            return id.equals(((DatabaseAssociation)o).id);
        }
        else return false;
    }*/
    
}
