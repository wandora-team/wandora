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
 * DatabaseTopic.java
 *
 * Created on 7. marraskuuta 2005, 11:28
 */

package org.wandora.topicmap.database2;


import static org.wandora.utils.Tuples.t2;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicInUseException;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapReadOnlyException;
import org.wandora.topicmap.TopicRemovedException;
import org.wandora.utils.Tuples.T2;


/**
 *
 * @author olli
 */
public class DatabaseTopic extends Topic {
    
    protected boolean full;
    // flags indicating what kind of data has been fecthed from the database
    protected boolean sisFetched;
    protected boolean dataFetched;
    protected boolean variantsFetched;
    protected boolean typesFetched;
//    protected boolean associationsFetched;
    
    protected DatabaseTopicMap topicMap;
    protected String baseName;
    protected String id;
    protected Locator subjectLocator;
    protected Set<Locator> subjectIdentifiers;
    protected Map<Topic,Map<Topic,String>> data;
    //                  scope                value ,variantid
    protected Map<Set<Topic>,T2<String,String>> variants;
    protected Set<Topic> types;
    //                  type ,          role of this topic
    //protected Hashtable<Topic,Hashtable<Topic,Collection<Association>>> associations;
    protected WeakReference<Map<Topic,Map<Topic,Collection<Association>>>> storedAssociations;
    protected boolean removed=false;
    
    
    /** Creates a new instance of DatabaseTopic */
    public DatabaseTopic(DatabaseTopicMap tm) {
        this.topicMap=tm;
        full=false;
        sisFetched=false;
        dataFetched=false;
        variantsFetched=false;
        typesFetched=false;
        id=null;
//        associationsFetched=false;
    }
    
    
    public DatabaseTopic(String id, DatabaseTopicMap tm) {
        this(tm);
        this.id=id;
    }
    
    
    public DatabaseTopic(Map<String,Object> row, DatabaseTopicMap tm)  throws TopicMapException {
        this(tm);
        Object o=row.get("BASENAME");
        if(o!=null) {
            internalSetBaseName(o.toString());
        }
        
        o=row.get("SUBJECTLOCATOR");
        if(o!=null) {
            subjectLocator=topicMap.createLocator(o.toString());
        }
        
        o=row.get("TOPICID");
        if(o!=null) {
            id=o.toString();
        }
    }
    
    
    public DatabaseTopic(Object baseName, Object subjectLocator, Object id, DatabaseTopicMap tm) throws TopicMapException {
        this(tm);
        if(baseName!=null) {
            internalSetBaseName(baseName.toString());
        }
        if(subjectLocator!=null) {
            this.subjectLocator=topicMap.createLocator(subjectLocator.toString());
        }
        if(id!=null) {
            this.id=id.toString();
        }
        else {
            System.out.println("Warning: DatabaseTopic id is not set: "+this.id+" != "+id);
        }
    }
    
    
    void initialize(Object baseName, Object subjectLocator) throws TopicMapException {
        initialize((String)baseName, (String)subjectLocator);
    }
    
    
    /**
     * Initializes this DatabaseTopic object setting the basename and subject locator
     * but does not modify the actual database.
     */
    void initialize(String baseName, String subjectLocator) throws TopicMapException {
        internalSetBaseName(baseName);
        if(subjectLocator!=null) {
            this.subjectLocator=topicMap.createLocator(subjectLocator);
        }
    }
    
    
    /**
     * Sets the base name in this DatabaseTopic object but does not modify the database.
     */
    protected void internalSetBaseName(String bn)  throws TopicMapException {
        String old=baseName;
        baseName=bn;
        topicMap.topicBNChanged(this, old);
    }
    
    
    private static int idcounter=0;    
    /**
     * Makes an ID string that can be used as an identifier in the database.
     * It will be unique if all identifiers are generated using the same Java
     * virtual machine but if they are being generated by several virtual machine
     * collisions may occur, however they will be extremely unlikely.
     */
    protected static synchronized String makeID(){
        if(idcounter>=100000) idcounter=0;
        return "T"+System.currentTimeMillis()+"-"+(idcounter++);
    }
    
    
    /**
     * Inserts a new topic in the database with the data currently set in this
     * DatabaseTopic object. A new DatabaseTopic is not inserted in the database
     * until this method is called.
     */
    void create() throws TopicMapException {
        subjectIdentifiers=Collections.synchronizedSet(new LinkedHashSet<Locator>());
        data=Collections.synchronizedMap(new LinkedHashMap<Topic,Map<Topic,String>>());
        variants=new HashMap<Set<Topic>,T2<String,String>>();
        types=Collections.synchronizedSet(new LinkedHashSet<Topic>());
//        associations=new Hashtable<Topic,Hashtable<Topic,Collection<Association>>>();
        
        full=true;
        sisFetched=true;
        dataFetched=true;
        variantsFetched=true;
        typesFetched=true;
        if(id == null) {
            id=makeID();
        }
        topicMap.executeUpdate("insert into TOPIC (TOPICID) values ('"+escapeSQL(id)+"')");
    }
    
    

    
    protected String escapeSQL(String s){
        return topicMap.escapeSQL(s);
    }
    
    
    /**
     * Sets subject identifiers in this DatabaseTopic but does not modify
     * the underlying database.
     */
    protected void setSubjectIdentifiers(HashSet<Locator> sis) {
        Set<Locator> oldSIs = subjectIdentifiers;
        subjectIdentifiers = sis;
        if(oldSIs!=null && !oldSIs.isEmpty()) {
            Set<Locator> removed = new LinkedHashSet<Locator>();
            removed.addAll(oldSIs);
            removed.removeAll(subjectIdentifiers);
            for(Locator l : removed){
                topicMap.topicSIChanged(this,l,null);
            }
            Set<Locator> added = new LinkedHashSet<Locator>();
            added.addAll(subjectIdentifiers);
            added.removeAll(oldSIs);
            for(Locator l : added){
                topicMap.topicSIChanged(this,null,l);
            }
        }
        else{
            for(Locator l : subjectIdentifiers){
                topicMap.topicSIChanged(this,null,l);
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------------------- FETCH ---
    // -------------------------------------------------------------------------
    
    
    
    protected void fetchSubjectIdentifiers() throws TopicMapException {
        if(id == null) {
            System.out.println("topic's id is null.");
            System.out.println(this.baseName);
            //System.out.println(this);
        }
        
        Collection<Map<String,Object>> res=topicMap.executeQuery("select * from SUBJECTIDENTIFIER where TOPIC='"+escapeSQL(id)+"'");
        HashSet<Locator> newSIs=new LinkedHashSet<Locator>();
        for(Map<String,Object> row : res){
            newSIs.add(topicMap.createLocator(row.get("SI").toString()));
        }
        setSubjectIdentifiers(newSIs);
        sisFetched=true;
    }
    
    
    protected void fetchData() throws TopicMapException {
        Collection<Map<String,Object>> res=topicMap.executeQuery(
                "select DATA.*, " +
                "Y.TOPICID as TYPEID, Y.BASENAME as TYPEBN, Y.SUBJECTLOCATOR as TYPESL," +
                "V.TOPICID as VERSIONID, V.BASENAME as VERSIONBN, V.SUBJECTLOCATOR as VERSIONSL " +
                "from DATA,TOPIC as Y,TOPIC as V where "+
                "DATA.TYPE=Y.TOPICID and DATA.VERSION=V.TOPICID and "+
                "DATA.TOPIC='"+escapeSQL(id)+"'");
        data=new LinkedHashMap<Topic,Map<Topic,String>>();
        for(Map<String,Object> row : res){
            Topic type=topicMap.buildTopic(row.get("TYPEID"),row.get("TYPEBN"),row.get("TYPESL"));
            Topic version=topicMap.buildTopic(row.get("VERSIONID"),row.get("VERSIONBN"),row.get("VERSIONSL"));
            Map<Topic,String> td=data.get(type);
            if(td==null){
                td=new LinkedHashMap<Topic,String>();
                data.put(type,td);
            }
            td.put(version,row.get("DATA").toString());
        }
        dataFetched=true;
    }
    
    
    protected void fetchVariants() throws TopicMapException {
        Collection<Map<String,Object>> res=topicMap.executeQuery(
                "select TOPIC.*,VARIANT.* from TOPIC,VARIANT,VARIANTSCOPE where "+
                "TOPICID=VARIANTSCOPE.TOPIC and VARIANTSCOPE.VARIANT=VARIANTID and "+
                "VARIANT.TOPIC='"+escapeSQL(id)+"' order by VARIANTID");
        variants=new HashMap<Set<Topic>,T2<String,String>>();
        HashSet<Topic> scope=null;
        String lastVariant=null;
        String lastName=null;
        for(Map<String,Object> row : res) {
            if(lastVariant==null || !lastVariant.equals(row.get("VARIANTID"))){
                if(lastVariant!=null){
                    variants.put(scope,t2(lastName,lastVariant));
                }
                scope=new LinkedHashSet<Topic>();
                lastVariant=row.get("VARIANTID").toString();
                lastName=row.get("VALUE").toString();
            }
            scope.add(topicMap.buildTopic(row));
        }
        if(lastVariant!=null){
            variants.put(scope,t2(lastName,lastVariant));
        }
        variantsFetched=true;
    }
    
    
    protected void fetchTypes() throws TopicMapException {
        Collection<Map<String,Object>> res=topicMap.executeQuery(
                "select TOPIC.* from TOPIC,TOPICTYPE where TYPE=TOPICID and "+
                "TOPIC='"+escapeSQL(id)+"'");
        types=new LinkedHashSet<Topic>();
        for(Map<String,Object> row : res){
            types.add(topicMap.buildTopic(row));
        }
        typesFetched=true;
    }
    
    
    static void fetchAllSubjectIdentifiers(Collection<Map<String,Object>> res, Map<String,DatabaseTopic> topics,DatabaseTopicMap topicMap) throws TopicMapException {
        String topicID = null;
        HashSet<Locator> subjectIdentifiers = new LinkedHashSet<Locator>();
        for(Map<String,Object> row : res) {
            if(topicID == null || !topicID.equals(row.get("TOPIC"))) {
                if(topicID != null) {
                    DatabaseTopic dbt=topics.get(topicID);
                    if(dbt != null && !dbt.sisFetched){
                        dbt.sisFetched = true;
                        dbt.setSubjectIdentifiers(subjectIdentifiers);
                    }
                }
                subjectIdentifiers = new LinkedHashSet<Locator>();
                if(row.get("TOPIC") != null) {
                    topicID = row.get("TOPIC").toString();
                }
            }
            if(row.get("SI") != null) {
                subjectIdentifiers.add(topicMap.createLocator(row.get("SI").toString()));
            }
        }
        if(topicID != null) {
            DatabaseTopic dbt=topics.get(topicID);
            if(dbt != null && !dbt.sisFetched) {
                dbt.sisFetched = true;
                dbt.setSubjectIdentifiers(subjectIdentifiers);
            }
        }
    }
    
    
    protected Map<Topic,Map<Topic,Collection<Association>>> fetchAssociations() throws TopicMapException {
        if(storedAssociations != null) {
            Map<Topic,Map<Topic,Collection<Association>>> associations = storedAssociations.get();
            if(associations != null) return associations;
        }
        Collection<Map<String,Object>> res = topicMap.executeQuery(
                "select R.TOPICID as ROLEID,R.BASENAME as ROLEBN,R.SUBJECTLOCATOR as ROLESL," +
                "T.TOPICID as TYPEID,T.BASENAME as TYPEBN,T.SUBJECTLOCATOR as TYPESL,ASSOCIATIONID " +
                "from MEMBER,ASSOCIATION,TOPIC as T,TOPIC as R where "+
                "R.TOPICID=MEMBER.ROLE and MEMBER.ASSOCIATION=ASSOCIATION.ASSOCIATIONID and "+
                "ASSOCIATION.TYPE=T.TOPICID and MEMBER.PLAYER='"+escapeSQL(id)+"' "+
                "order by R.TOPICID"
                );
        Map<Topic,Map<Topic,Collection<Association>>> associations = new HashMap<Topic,Map<Topic,Collection<Association>>>();
        Map<String,DatabaseTopic> collectedTopics = new LinkedHashMap<String,DatabaseTopic>();
        Map<String,DatabaseAssociation> collectedAssociations = new LinkedHashMap<String,DatabaseAssociation>();
        for(Map<String,Object> row : res) {
            DatabaseTopic type=topicMap.buildTopic(row.get("TYPEID"),row.get("TYPEBN"),row.get("TYPESL"));
            DatabaseTopic role=topicMap.buildTopic(row.get("ROLEID"),row.get("ROLEBN"),row.get("ROLESL"));
            Map<Topic,Collection<Association>> as = associations.get(type);
            if(as == null) {
                as = new HashMap<Topic,Collection<Association>>();
                associations.put(type,as);
            }
            Collection<Association> c = as.get(role);
            if(c == null) {
                c = new ArrayList<Association>();
                as.put(role,c);
            }
            Object associationId = row.get("ASSOCIATIONID");
            if(associationId != null) {
                DatabaseAssociation da = topicMap.buildAssociation(associationId.toString(),type);
                c.add(da);            
                collectedAssociations.put(da.getID(),da);
                collectedTopics.put(type.getID(),type);
                collectedTopics.put(role.getID(),role);
            }
        }
        {
            collectedTopics.putAll(DatabaseAssociation.makeFullAll(topicMap.executeQuery(
                "select R.TOPICID as ROLEID,R.BASENAME as ROLEBN,R.SUBJECTLOCATOR as ROLESL," +
                "P.TOPICID as PLAYERID,P.BASENAME as PLAYERBN,P.SUBJECTLOCATOR as PLAYERSL, OM.ASSOCIATION " +
                "from MEMBER as LM,MEMBER as OM,TOPIC as P,TOPIC as R where "+
                "R.TOPICID=OM.ROLE and P.TOPICID=OM.PLAYER and OM.ASSOCIATION=LM.ASSOCIATION and "+
                "LM.PLAYER='"+escapeSQL(id)+"' order by OM.ASSOCIATION"
                ),collectedAssociations,topicMap));
            
/*            DatabaseTopic.fetchAllSubjectIdentifiers(topicMap.executeQuery(
                    "select * from SUBJECTIDENTIFIER where TOPIC in (select TYPE "+
                    "from MEMBER as LM,ASSOCIATION where "+
                    "ASSOCIATIONID=LM.ASSOCIATION and "+
                    "LM.PLAYER='"+escapeSQL(id)+"' "+
                    "union "+
                    "select OM.ROLE "+
                    "from MEMBER as LM,MEMBER as OM where "+
                    "OM.ASSOCIATION=LM.ASSOCIATION and "+
                    "LM.PLAYER='"+escapeSQL(id)+"' "+
                    "union "+
                    "select OM.PLAYER "+
                    "from MEMBER as LM,MEMBER as OM where "+
                    "OM.ASSOCIATION=LM.ASSOCIATION and "+
                    "LM.PLAYER='"+escapeSQL(id)+"') order by TOPIC"
                    ),collectedTopics,topicMap);
            */
            DatabaseTopic.fetchAllSubjectIdentifiers(topicMap.executeQuery(
                    "select SUBJECTIDENTIFIER.* from SUBJECTIDENTIFIER, ("+
                    "select TOPIC.TOPICID "+
                    "from MEMBER as LM,ASSOCIATION,TOPIC where "+
                    "ASSOCIATIONID=LM.ASSOCIATION and "+
                    "LM.PLAYER='"+escapeSQL(id)+"' and TOPIC.TOPICID=ASSOCIATION.TYPE "+
                    "union "+
                    "select TOPIC.TOPICID "+
                    "from MEMBER as LM,MEMBER as OM,TOPIC where "+
                    "OM.ASSOCIATION=LM.ASSOCIATION and "+
                    "LM.PLAYER='"+escapeSQL(id)+"' and TOPIC.TOPICID=OM.ROLE "+
                    "union "+
                    "select TOPIC.TOPICID "+
                    "from MEMBER as LM,MEMBER as OM,TOPIC where "+
                    "OM.ASSOCIATION=LM.ASSOCIATION and "+
                    "LM.PLAYER='"+escapeSQL(id)+"' and TOPIC.TOPICID=OM.PLAYER "+
                    ") as TEMP where SUBJECTIDENTIFIER.TOPIC=TEMP.TOPICID order by TOPIC"
                    ),collectedTopics,topicMap);
            
        }
//        associationsFetched=true;
        storedAssociations=new WeakReference(associations);
        return associations;
    }
    
    
    
    /**
     * Fetch all information from database that hasn't already been fetched.
     */
    void makeFull() throws TopicMapException {
        if(!sisFetched) {
            fetchSubjectIdentifiers();
        }
        if(!dataFetched) {
            fetchData();
        }
        if(!variantsFetched) {
            fetchVariants();
        }
        if(!typesFetched) {
            fetchTypes();
        }
//        if(!associationsFetched) fetchAssociations();
        full=true;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public String getID() {
        return id;
    }
    
    
    @Override
    public Collection<Locator> getSubjectIdentifiers() throws TopicMapException {
        if(!sisFetched) {
            fetchSubjectIdentifiers();
        }
        return subjectIdentifiers;
    }
    
    
    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if( topicMap.isReadOnly() ) throw new TopicMapReadOnlyException();
        
        if( l == null ) return; 
        if(!sisFetched) fetchSubjectIdentifiers();
        if(!subjectIdentifiers.contains(l)) {
            Topic t=topicMap.getTopic(l);
            if(t!=null) {
                mergeIn(t);
            }
            else {
                subjectIdentifiers.add(l);
//                System.out.println("Inserting si "+l.toExternalForm());
                boolean ok = topicMap.executeUpdate("insert into SUBJECTIDENTIFIER (TOPIC,SI) values ('"+
                    escapeSQL(id)+"','"+escapeSQL(l.toExternalForm())+"')");
                if(!ok) System.out.println("Failed to add si "+l.toExternalForm());
                topicMap.topicSIChanged(this, null,l);
                topicMap.topicSubjectIdentifierChanged(this,l,null);
            }
        }
//        topicMap.topicChanged(this);
    }
    
    
    public void mergeIn(Topic t)  throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if( t == null ) return;
        Collection<Map<String,Object>> tempRes;
        DatabaseTopic ti=(DatabaseTopic)t;
        // add data
        tempRes=topicMap.executeQuery("select D1.* from DATA as D1,DATA as D2 where D1.VERSION=D2.VERSION and "+
                "D1.TYPE=D2.TYPE and D1.TOPIC='"+escapeSQL(t.getID())+"' and D2.TOPIC='"+escapeSQL(id)+"'");
        for(Map<String,Object> row : tempRes){
            topicMap.executeUpdate("delete from DATA where TOPIC='"+escapeSQL((String)row.get("TOPIC"))+"' and "+
                    "TYPE='"+escapeSQL((String)row.get("TYPE"))+"' and "+
                    "VERSION='"+escapeSQL((String)row.get("VERSION"))+"'");
        }
        topicMap.executeUpdate("update DATA set TOPIC='"+escapeSQL(id)+"' where "+
                "TOPIC='"+escapeSQL(t.getID())+"'");
        
        // set base name
        if(ti.getBaseName()!=null) {
            String name=ti.getBaseName();
            ti.setBaseName(null);
            this.setBaseName(name);
        }
        // set subject locator
        if(ti.getSubjectLocator()!=null){
            Locator l=ti.getSubjectLocator();
            ti.setSubjectLocator(null);
            this.setSubjectLocator(l);
        }
        // for now just update all associations,roles,types etc and remove duplicates later
        // set types
        tempRes=topicMap.executeQuery("select T1.* from TOPICTYPE as T1,TOPICTYPE as T2 where T1.TYPE=T2.TYPE and "+
                "T1.TOPIC='"+escapeSQL(t.getID())+"' and T2.TOPIC='"+escapeSQL(id)+"'");
        for(Map<String,Object> row : tempRes){
            topicMap.executeUpdate("delete from TOPICTYPE where TOPIC='"+escapeSQL((String)row.get("TOPIC"))+"' and "+
                    "TYPE='"+escapeSQL((String)row.get("TYPE"))+"'");
        }
        topicMap.executeUpdate("update TOPICTYPE set TOPIC='"+escapeSQL(id)+"' where "+
                "TOPIC='"+escapeSQL(t.getID())+"'");
        
        
        tempRes=topicMap.executeQuery("select T1.* from TOPICTYPE as T1,TOPICTYPE as T2 where T1.TOPIC=T2.TOPIC and "+
                "T1.TYPE='"+escapeSQL(t.getID())+"' and T2.TYPE='"+escapeSQL(id)+"'");
        for(Map<String,Object> row : tempRes){
            topicMap.executeUpdate("delete from TOPICTYPE where TOPIC='"+escapeSQL((String)row.get("TOPIC"))+"' and "+
                    "TYPE='"+escapeSQL((String)row.get("TYPE"))+"'");
        }
        topicMap.executeUpdate("update TOPICTYPE set TYPE='"+escapeSQL(id)+"' where "+
                "TYPE='"+escapeSQL(t.getID())+"'");
        

        // set variant names
        topicMap.executeUpdate("update VARIANT set TOPIC='"+escapeSQL(id)+"' where "+
                "TOPIC='"+escapeSQL(t.getID())+"'");
        // set subject identifiers
        topicMap.executeUpdate("update SUBJECTIDENTIFIER set TOPIC='"+escapeSQL(id)+"' where "+
                "TOPIC='"+escapeSQL(t.getID())+"'");

        // change association players
        topicMap.executeUpdate("update MEMBER set PLAYER='"+escapeSQL(id)+"' where "+
                "PLAYER='"+escapeSQL(t.getID())+"'");
        // change association types
        topicMap.executeUpdate("update ASSOCIATION set TYPE='"+escapeSQL(id)+"' where "+
                "TYPE='"+escapeSQL(t.getID())+"'");
        // change data types
        tempRes=topicMap.executeQuery("select D1.* from DATA as D1,DATA as D2 where D1.VERSION=D2.VERSION and "+
                "D1.TOPIC=D2.TOPIC and D1.TYPE='"+escapeSQL(t.getID())+"' and D2.TYPE='"+escapeSQL(id)+"'");
        for(Map<String,Object> row : tempRes){
            topicMap.executeUpdate("delete from DATA where TOPIC='"+escapeSQL((String)row.get("TOPIC"))+"' and "+
                    "TYPE='"+escapeSQL((String)row.get("TYPE"))+"' and "+
                    "VERSION='"+escapeSQL((String)row.get("VERSION"))+"'");
        }
        topicMap.executeUpdate("update DATA set TYPE='"+escapeSQL(id)+"' where "+
                "TYPE='"+escapeSQL(t.getID())+"'");
        // change data versions
        tempRes=topicMap.executeQuery("select D1.* from DATA as D1,DATA as D2 where D1.TYPE=D2.TYPE and "+
                "D1.TOPIC=D2.TOPIC and D1.VERSION='"+escapeSQL(t.getID())+"' and D2.VERSION='"+escapeSQL(id)+"'");
        for(Map<String,Object> row : tempRes){
            topicMap.executeUpdate("delete from DATA where TOPIC='"+escapeSQL((String)row.get("TOPIC"))+"' and "+
                    "TYPE='"+escapeSQL((String)row.get("TYPE"))+"' and "+
                    "VERSION='"+escapeSQL((String)row.get("VERSION"))+"'");
        }
        topicMap.executeUpdate("update DATA set VERSION='"+escapeSQL(id)+"' where "+
                "VERSION='"+escapeSQL(t.getID())+"'");
        // change role types
        tempRes=topicMap.executeQuery("select M1.* from MEMBER as M1,MEMBER as M2 where M1.ASSOCIATION=M2.ASSOCIATION and "+
                "M1.ROLE='"+escapeSQL(t.getID())+"' and M2.ROLE='"+escapeSQL(id)+"'");
        for(Map<String,Object> row : tempRes){
            topicMap.executeUpdate("delete from MEMBER where ASSOCIATION='"+escapeSQL((String)row.get("ASSOCIATION"))+"' and "+
                    "ROLE='"+escapeSQL((String)row.get("ROLE"))+"'");
        }
        topicMap.executeUpdate("update MEMBER set ROLE='"+escapeSQL(id)+"' where "+
                "ROLE='"+escapeSQL(t.getID())+"'");
        // change variant scopes
        tempRes=topicMap.executeQuery("select V1.* from VARIANTSCOPE as V1,VARIANTSCOPE as V2 where "+
                "V1.VARIANT=V2.VARIANT and V1.TOPIC='"+escapeSQL(t.getID())+"' and V2.TOPIC='"+escapeSQL(id)+"'");
        for(Map<String,Object> row : tempRes){
            topicMap.executeUpdate("delete from VARIANTSCOPE where VARIANT='"+escapeSQL((String)row.get("VARIANT"))+"' and "+
                    "TOPIC='"+escapeSQL((String)row.get("TOPIC"))+"'");
        }
        topicMap.executeUpdate("update VARIANTSCOPE set TOPIC='"+escapeSQL(id)+"' where "+
                "TOPIC='"+escapeSQL(t.getID())+"'");
        // remove everything where old topic is still used (these are duplicates)
        topicMap.executeUpdate("delete from TOPICTYPE where TOPIC='"+escapeSQL(t.getID())+"'");
        topicMap.executeUpdate("delete from DATA where TOPIC='"+escapeSQL(t.getID())+"'");
        topicMap.executeUpdate("delete from DATA where TYPE='"+escapeSQL(t.getID())+"'");
        topicMap.executeUpdate("delete from DATA where VERSION='"+escapeSQL(t.getID())+"'");
        topicMap.executeUpdate("delete from MEMBER where ROLE='"+escapeSQL(t.getID())+"'");
        topicMap.executeUpdate("delete from VARIANTSCOPE where TOPIC='"+escapeSQL(t.getID())+"'");
        
        { // check for duplicate associations 
            Collection<Map<String,Object>> res=topicMap.executeQuery(
                        "select M1.*,ASSOCIATION.TYPE from MEMBER as M1, ASSOCIATION, MEMBER as M2 where "+
                        "M1.ASSOCIATION=ASSOCIATIONID and ASSOCIATIONID=M2.ASSOCIATION and "+
                        "M2.PLAYER='"+escapeSQL(id)+"' order by M1.ASSOCIATION");
            //         type  ,associations{ role  ,player}
            HashSet<T2<String,Collection<T2<String,String>>>> associations=new LinkedHashSet<T2<String,Collection<T2<String,String>>>>();
            HashSet<String> delete=new LinkedHashSet<String>();
            String oldAssociation="dummy";
            String oldType="";
            Collection<T2<String,String>> association=null;
            for(Map<String,Object> row : res){
                String aid=(String)row.get("ASSOCIATION");
                if(!aid.equals(oldAssociation)){
                    if(association!=null){
                        if(associations.contains(t2(oldType,association))) delete.add(oldAssociation);
                        else associations.add(t2(oldType,association));
                    }
                    association=new LinkedHashSet<T2<String,String>>();
                    oldAssociation=aid;
                    oldType=(String)row.get("TYPE");
                }
                association.add(t2((String)row.get("ROLE"),(String)row.get("PLAYER")));
            }
            if(association!=null){
                if(associations.contains(t2(oldType,association))) delete.add(oldAssociation);
            }
            if(delete.size()>0){
                String col=topicMap.collectionToSQL(delete);
                topicMap.executeUpdate("delete from MEMBER where ASSOCIATION in "+col);
                topicMap.executeUpdate("delete from ASSOCIATION where ASSOCIATIONID in "+col);
            }
        }
        { // check for duplicate variants
            Collection<Map<String,Object>> res=topicMap.executeQuery(
                        "select VARIANTSCOPE.* from VARIANT,VARIANTSCOPE where "+
                        "VARIANT.TOPIC='"+escapeSQL(id)+"' and VARIANTID=VARIANT order by VARIANTID");
            HashSet<Collection<String>> scopes=new LinkedHashSet<Collection<String>>();
            HashSet<String> delete=new LinkedHashSet<String>();
            String oldVariant="dummy";
            Collection<String> scope=null;
            for(Map<String,Object> row : res){
                String vid=(String)row.get("VARIANT");
                if(!vid.equals(oldVariant)){
                    if(scope!=null){
                        if(scopes.contains(scope)) delete.add(oldVariant);
                        else scopes.add(scope);
                    }
                    scope=new LinkedHashSet<String>();
                    oldVariant=vid;
                }
                scope.add((String)row.get("TOPIC"));
            }
            if(scope!=null){
                if(scopes.contains(scope)) delete.add(oldVariant);
            }
            if(!delete.isEmpty()){
                String col=topicMap.collectionToSQL(delete);
                topicMap.executeUpdate("delete from VARIANTSCOPE where VARIANT in "+col);
                topicMap.executeUpdate("delete from VARIANT where VARIANTID in "+col);
            }            
        }
        
        // remove merged topic
        try{
            t.remove();
        }catch(TopicInUseException e){
            System.out.println("ERROR couldn't delete merged topic, topic in use. There is a bug in the code if this happens. "+e.getReason());
        }
        
        sisFetched=false;
        full=false;
        makeFull();
        topicMap.topicChanged(this);
    }
    
    
    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if(l == null) return;
        subjectIdentifiers.remove(l);
        topicMap.executeUpdate("delete from SUBJECTIDENTIFIER where SI='"+escapeSQL(l.toExternalForm())+"'");
        topicMap.topicSubjectIdentifierChanged(this,null,l);
        topicMap.topicSIChanged(this, l,null);
    }
    
    
    @Override
    public String getBaseName(){
        return baseName;
    }
    
    
    @Override
    public void setBaseName(String name) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if(name==null){
            if(baseName!=null){
                String old=baseName;
                internalSetBaseName(null);
                topicMap.executeUpdate("update TOPIC "
                        +"set BASENAME=null "
                        +"where TOPICID='"+escapeSQL(id)+"'");
                topicMap.topicBaseNameChanged(this,null,old);
            }
            return;
        }
        if(baseName==null || !baseName.equals(name)){
            Topic t=topicMap.getTopicWithBaseName(name);
            if(t!=null){
                mergeIn(t);
            }
            String old=baseName;
            internalSetBaseName(name);
            topicMap.executeUpdate("update TOPIC "
                    +"set BASENAME='"+escapeSQL(name)+"' "
                    +"where TOPICID='"+escapeSQL(id)+"'");
            topicMap.topicBaseNameChanged(this,name,old);
        }        
    }
    
    
    @Override
    public Collection<Topic> getTypes() throws TopicMapException {
        if(!full) makeFull();
        return types;
    }
    
    
    @Override
    public void addType(Topic t) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if(t == null) return;
        if(!full) makeFull();
        if(!types.contains(t)){
            types.add(t);
            topicMap.executeUpdate("insert into TOPICTYPE (TOPIC,TYPE) values ('"+
                escapeSQL(id)+"','"+escapeSQL(t.getID())+"')");
            topicMap.topicTypeChanged(this,t,null);
        }
    }
    
    
    @Override
    public void removeType(Topic t) throws TopicMapException  {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if(t == null) return;
        if(!full) makeFull();
        if(types.contains(t)){
            types.remove(t);
            topicMap.executeUpdate("delete from TOPICTYPE "
                    +"where TOPIC='"+escapeSQL(id)+"' "
                    +"and TYPE='"+escapeSQL(t.getID())+"'");
            topicMap.topicTypeChanged(this,null,t);
        }        
    }
    
    
    @Override
    public boolean isOfType(Topic t)  throws TopicMapException {
        if(t == null) return false;
        if(!full) makeFull();
        return types.contains(t);
    }
    
    
    @Override
    public String getVariant(Set<Topic> scope) throws TopicMapException {
        if(!full) makeFull();
        T2<String,String> variant = variants.get(scope);
        if(variant != null) return variant.e1;
        return null;
    }
    
    
    @Override
    public void setVariant(Set<Topic> scope,String name) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if(!full) makeFull();
        String old=null;
        if(variants.get(scope)!=null){
            String vid=variants.get(scope).e2;
            topicMap.executeUpdate("update VARIANT set VALUE='"+escapeSQL(name)+"' where VARIANTID='"+escapeSQL(vid)+"'");
            old=variants.put(scope,t2(name,vid)).e1;
        }
        else{
            String vid=makeID();
            topicMap.executeUpdate("insert into VARIANT (VARIANTID,TOPIC,VALUE) values ('"+
                        escapeSQL(vid)+"','"+escapeSQL(id)+"','"+escapeSQL(name)+"')");
            for(Topic s : scope){
                topicMap.executeUpdate("insert into VARIANTSCOPE (VARIANT,TOPIC) values ('"+
                        escapeSQL(vid)+"','"+escapeSQL(s.getID())+"')");
            }
            variants.put(scope,t2(name,vid));
        }
        topicMap.topicVariantChanged(this,scope,name,old);
    }
    
    
    @Override
    public Set<Set<Topic>> getVariantScopes() throws TopicMapException {
        if(!full) makeFull();
        return variants.keySet();
    }
    
    
    @Override
    public void removeVariant(Set<Topic> scope) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if(!full) makeFull();
        T2<String,String> v=variants.get(scope);
        if(v!=null){
            topicMap.executeUpdate("delete from VARIANTSCOPE where VARIANT='"+escapeSQL(v.e2)+"'");
            topicMap.executeUpdate("delete from VARIANT where VARIANTID='"+escapeSQL(v.e2)+"'");
            variants.remove(scope);
            topicMap.topicVariantChanged(this,scope,null,v.e1);
        }
    }
    
    
    @Override
    public String getData(Topic type,Topic version) throws TopicMapException {
        if(type == null || version == null) return null;
        if(!full) makeFull();
        Map<Topic,String> td=data.get(type);
        if(td==null) return null;
        return td.get(version);
    }
    
    
    @Override
    public Hashtable<Topic,String> getData(Topic type) throws TopicMapException {
        if(!full) makeFull();
        if(data.containsKey(type)) {
            Hashtable<Topic,String> ht = new Hashtable<>();
            Map<Topic,String> m = data.get(type);
            ht.putAll(m);
            return ht;
        }
        else {
            return new Hashtable<>();
        }
    }
    
    
    @Override
    public Collection<Topic> getDataTypes() throws TopicMapException {
        if(!full) makeFull();
        return data.keySet();
    }
    
    
    @Override
    public void setData(Topic type,Hashtable<Topic,String> versionData) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if( topicMap.isReadOnly() ) throw new TopicMapReadOnlyException();
        for(Map.Entry<Topic,String> e : versionData.entrySet()){
            setData(type,e.getKey(),e.getValue());
        }
    }
    
    
    @Override
    public void setData(Topic type,Topic version,String value) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        
        if(!dataFetched) {
            fetchData();
        }
        
        if(getData(type,version)!=null){
            topicMap.executeUpdate("update DATA set DATA='"+escapeSQL(value)+"' "
                    +"where TOPIC='"+escapeSQL(id)+"' "
                    +"and TYPE='"+escapeSQL(type.getID())+"' "
                    +"and VERSION='"+escapeSQL(version.getID())+"'");
        }
        else{
            topicMap.executeUpdate("insert into DATA (DATA,TOPIC,TYPE,VERSION) values ("+
                    "'"+escapeSQL(value)+"','"+escapeSQL(id)+"','"+escapeSQL(type.getID())+"','"+
                    escapeSQL(version.getID())+"')");            
        }
            
        Map<Topic,String> dt=data.get(type);
        if(dt==null) {
            dt=new LinkedHashMap<Topic,String>();
            data.put(type,dt);
        }
        String old=dt.put(version,value);
        topicMap.topicDataChanged(this,type,version,value,old);
    }
    
    
    @Override
    public void removeData(Topic type,Topic version)  throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if( topicMap.isReadOnly() ) throw new TopicMapReadOnlyException();
        
        if(type == null || version == null) return;
        if(!full) makeFull();
        if(getData(type,version) != null) {
            topicMap.executeUpdate("delete from DATA "
                    +"where TOPIC='"+escapeSQL(id)+"' "
                    +"and TYPE='"+escapeSQL(type.getID())+"' "
                    +"and VERSION='"+escapeSQL(version.getID())+"'");
            String old=data.get(type).remove(version);
            
            Map<Topic,String> datas = data.get(type);
            if(datas != null && datas.isEmpty()) {
                data.remove(type);
            }
            topicMap.topicDataChanged(this,type,version,null,old);
        }        
    }
    
    
    @Override
    public void removeData(Topic type) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        if(type == null) return;
        if(!full) makeFull();
        if(getData(type) != null) {
            topicMap.executeUpdate("delete from DATA "
                    +"where TOPIC='"+escapeSQL(id)+"' "
                    +"and TYPE='"+escapeSQL(type.getID())+"'");
            Map<Topic,String> old = data.remove(type);
            for(Map.Entry<Topic,String> e : old.entrySet()){
                topicMap.topicDataChanged(this,type,e.getKey(),null,e.getValue());
            }
        }
    }
    
    
    @Override
    public Locator getSubjectLocator(){
        return subjectLocator;
    }
    
    
    @Override
    public void setSubjectLocator(Locator l) throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        if(l==null){
            if(subjectLocator!=null){
                Locator old=subjectLocator;
                subjectLocator=null;
                topicMap.executeUpdate("update TOPIC set SUBJECTLOCATOR=null where TOPICID='"+escapeSQL(id)+"'");
                topicMap.topicSubjectLocatorChanged(this,l,old);
            }
            return;
        }
        if(subjectLocator==null || !subjectLocator.equals(l)){
            Topic t=topicMap.getTopicBySubjectLocator(l);
            if(t!=null){
                mergeIn(t);
            }
            Locator old=subjectLocator;
            subjectLocator=l;
            topicMap.executeUpdate("update TOPIC set SUBJECTLOCATOR='"+escapeSQL(l.toExternalForm())+"' where TOPICID='"+escapeSQL(id)+"'");
            topicMap.topicSubjectLocatorChanged(this,l,old);
        }
    }
    
    
    @Override
    public TopicMap getTopicMap(){
        return topicMap;
    }
    
    
    @Override
    public Collection<Association> getAssociations() throws TopicMapException {
//        if(!full) makeFull();
        Set<Association> ret=new LinkedHashSet<>();
        Map<Topic,Map<Topic,Collection<Association>>> associations=fetchAssociations();
        for(Map.Entry<Topic,Map<Topic,Collection<Association>>> e : associations.entrySet()){
            for(Map.Entry<Topic,Collection<Association>> e2 : e.getValue().entrySet()){
                for(Association a : e2.getValue()){
                    ret.add(a);
                }
            }
        }
        return ret;
    }
    
    
    @Override
    public Collection<Association> getAssociations(Topic type) throws TopicMapException {
        if(type == null) return null;
//        if(!full) makeFull();
        Set<Association> ret=new LinkedHashSet<>();
        Map<Topic,Map<Topic,Collection<Association>>> associations=fetchAssociations();
        if(associations.get(type)==null) {
            return new ArrayList<Association>();
        }
        for(Map.Entry<Topic,Collection<Association>> e2 : associations.get(type).entrySet()){
            for(Association a : e2.getValue()){
                ret.add(a);
            }
        }
        return ret;        
    }
    
    
    @Override
    public Collection<Association> getAssociations(Topic type,Topic role) throws TopicMapException {
        if(type == null || role == null) return null;
//        if(!full) makeFull();
        Map<Topic,Map<Topic,Collection<Association>>> associations=fetchAssociations();
        Map<Topic,Collection<Association>> as=associations.get(type);
        if(as==null) {
            return new ArrayList<Association>();
        }
        Collection<Association> ret=as.get(role);
        if(ret==null) {
            return new ArrayList<Association>();
        }
        return ret;
    }
    
    
    @Override
    public void remove()  throws TopicMapException {
        if( removed ) throw new TopicRemovedException();
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        if(!isDeleteAllowed()) throw new TopicInUseException(this);
        String eid=escapeSQL(id);
        
        Collection<Map<String,Object>> res=topicMap.executeQuery("select distinct ASSOCIATIONID "
                +"from ASSOCIATION,MEMBER "
                +"where MEMBER.PLAYER='"+eid+"' "
                +"and MEMBER.ASSOCIATION=ASSOCIATION.ASSOCIATIONID");
        String[] aid=new String[res.size()];
        int counter=0;
        for(Map<String,Object> row : res){
            aid[counter++]=row.get("ASSOCIATIONID").toString();
        }
        
        topicMap.executeUpdate("delete from VARIANTSCOPE where VARIANT in (select VARIANTID from VARIANT where TOPIC='"+eid+"')");
        topicMap.executeUpdate("delete from VARIANT where VARIANTID='"+eid+"'");
        topicMap.executeUpdate("delete from DATA where TOPIC='"+eid+"'");
        topicMap.executeUpdate("delete from TOPICTYPE where TOPIC='"+eid+"'");
        topicMap.executeUpdate("delete from SUBJECTIDENTIFIER where TOPIC='"+eid+"'");
        
        for(int i=0;i<aid.length;i+=50){
            String c="";
            for(int j=i;j<aid.length && j<i+50;j++){
                if(c.length()>0) c+=",";
                c+="'"+escapeSQL(aid[j])+"'";
            }
            topicMap.executeUpdate("delete from MEMBER where ASSOCIATION in ("+c+")");
            topicMap.executeUpdate("delete from ASSOCIATION where ASSOCIATIONID in ("+c+")");
        }
        
        topicMap.executeUpdate("delete from TOPIC where TOPICID='"+eid+"'");
        removed=true;
        topicMap.topicRemoved(this);
    }
    
    
    @Override
    public long getEditTime(){
        return 0; // TODO: what to do with edittimes? store in db?
    }
    
    
    @Override
    public void setEditTime(long time){
    }
    
    
    @Override
    public long getDependentEditTime(){
        return 0;
    }
    
    
    @Override
    public void setDependentEditTime(long time){
    }
    
    
    @Override
    public boolean isRemoved(){
        return removed;
    }
    
    
    @Override
    public boolean isDeleteAllowed() throws TopicMapException {
        String eid=escapeSQL(id);
        Collection<Map<String,Object>> res=topicMap.executeQuery(
                "select TOPICID from TOPIC where TOPICID='"+eid+"' and ("+
                "exists (select * from VARIANTSCOPE where TOPIC='"+eid+"') or "+
                "exists (select * from DATA where TYPE='"+eid+"' or VERSION='"+eid+"') or "+
                "exists (select * from TOPICTYPE where TYPE='"+eid+"') or "+
//                "exists (select * from MEMBER where PLAYER='"+eid+"' or ROLE='"+eid+"') or "+
                "exists (select * from MEMBER where ROLE='"+eid+"') or "+
                "exists (select * from ASSOCIATION where TYPE='"+eid+"') )");
        if(!res.isEmpty()) return false;
        return true;
    }
    
    
    @Override
    public Collection<Topic> getTopicsWithDataType() throws TopicMapException {
        return topicMap.queryTopic("select TOPIC.* "
                + "from TOPIC,DATA "
                + "where TOPICID=TOPIC "
                + "and TYPE='"+escapeSQL(id)+"'");
    }
    
    
    @Override
    public Collection<Association> getAssociationsWithType() throws TopicMapException {
        return topicMap.queryAssociation("select ASSOCIATION.* "
                + "from ASSOCIATION "
                + "where TYPE='"+escapeSQL(id)+"'");
    }
    
    
    @Override
    public Collection<Association> getAssociationsWithRole() throws TopicMapException {
        return topicMap.queryAssociation("select ASSOCIATION.* "
                +"from ASSOCIATION,MEMBER "
                +"where ASSOCIATION=ASSOCIATIONID "
                +"and ROLE='"+escapeSQL(id)+"'");
    }

    
    @Override
    public Collection<Topic> getTopicsWithDataVersion() throws TopicMapException {
        return topicMap.queryTopic("select TOPIC.* "
                +"from TOPIC,DATA "
                +"where TOPICID=TOPIC "
                +"and VERSION='"+escapeSQL(id)+"'");
    }

    
    @Override
    public Collection<Topic> getTopicsWithVariantScope() throws TopicMapException {
        return topicMap.queryTopic("select distinct TOPIC.* "
                +"from TOPIC,VARIANT,VARIANTSCOPE "
                +"where TOPIC.TOPICID=VARIANT.TOPIC "
                +"and VARIANT.VARIANTID=VARIANTSCOPE.VARIANT "
                +"and VARIANTSCOPE.TOPIC='"+escapeSQL(id)+"'");
    }
    
    
    void associationChanged(DatabaseAssociation a,Topic type,Topic oldType,Topic role,Topic oldRole){
        Map<Topic,Map<Topic,Collection<Association>>> associations=null;
        if(storedAssociations!=null){
            associations=storedAssociations.get();
            if(associations==null) return;
        }
        else return;
        Map<Topic,Collection<Association>> t=null;
        Collection<Association> c=null;
        if(oldType!=null){
            t=associations.get(oldType);
            c=t.get(oldRole);
            c.remove(a);
            // remove c from t if it becomes empty?
        }
        
        if(type!=null){
            t=associations.get(type);
            if(t==null){
                t=new HashMap<Topic,Collection<Association>>();
                associations.put(type,t);
            }
            c=t.get(role);
            if(c==null){
                c=new LinkedHashSet<Association>();
                t.put(role,c);
            }
            c.add(a);
        }
        storedAssociations=new WeakReference<>(associations);
    }
    
    
    void removeDuplicateAssociations() throws TopicMapException {
        if(topicMap.isReadOnly()) throw new TopicMapReadOnlyException();
        Set<EqualAssociationWrapper> as=new LinkedHashSet<EqualAssociationWrapper>();
        Set<Association> delete=new LinkedHashSet<Association>();
        for(Association a : getAssociations()) {
            if(!as.add(new EqualAssociationWrapper((DatabaseAssociation)a))){
                delete.add(a);
            }
        }
        for(Association a : delete) a.remove();
    }
    
    
    /*
    public int hashCode(){
        return id.hashCode();
    }
    public boolean equals(Object o){
        if(o instanceof DatabaseTopic){
            return ((DatabaseTopic)o).id.equals(id);
        }
        else return false;
    }*/
    
    
    private class EqualAssociationWrapper {
        public DatabaseAssociation a;
        private int hashCode=0;
        public EqualAssociationWrapper(DatabaseAssociation a){
            this.a=a;
        }
        @Override
        public boolean equals(Object o){
            if(o==null) return false;
            if(hashCode()!=o.hashCode()) return false;
            return a._equals(((EqualAssociationWrapper)o).a);
        }
        @Override
        public int hashCode(){
            if(hashCode!=0) return hashCode;
            else {
                hashCode=a._hashCode();
                return hashCode;
            }
        }
    }
    
}

