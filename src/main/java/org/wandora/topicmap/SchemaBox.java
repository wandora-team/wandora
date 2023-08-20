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
 * SchemaBox.java
 *
 * Created on August 10, 2004, 2:47 PM
 */

package org.wandora.topicmap;


import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * SchemaBox contains many high level methods related to the proprietary topic
 * map schema used by Wandora. Some of these methods include:
 * <ul>
 * <li>Methods to get subclasses and superclasses of a topic. The standard
 * superclass-subclass, superclass and subclass topics are used as association type
 * and association roles respectively.</li>
 * <li>Methods to check if a topic has as one of its types a specific class topic
 * or any of its subclasses, subsubclasses etc. That is, subclasses behave as if
 * they inherited all properties of their super classes. Thus if a topic is an
 * instance of a class topic, it is also instance of all the superclasses of the
 * classtopic, and instance of superclasses of those.</li>
 * <li>Methods to get content types of a topic taking into account inheritance
 * through superclasses. Topic A is content type of topic B if A is of type
 * content type and B is an instance of A as specified in previous point in this
 * list. That is B doesn't have to be a direct instance of A but can be an instance
 * of some other topic that is a subclass (possibly through many associations) of A.</li>
 * <li>Methods to get possible association types for a topic. Each content type
 * defines what associations that type of topic may have. Possible association types
 * for a topic are all the association types specified by all its content types.</li>
 * <li>Similar methods for occurrence types.</li>
 * <li>A method to get role topics for an association type. The association type
 * topic specifies which topics can be used as roles of associations of that type.
 * This method gets those role topics.</li>
 * </ul>
 *
 * @author  olli
 */
public class SchemaBox {

    public static final String ROLECLASS_SI="http://wandora.org/si/core/role-class";
    public static final String CONTENTTYPE_SI="http://wandora.org/si/core/content-type";
    public static final String ASSOCIATIONTYPE_SI=TMBox.ASSOCIATIONTYPE_SI;
    public static final String OCCURRENCETYPE_SI=TMBox.OCCURRENCETYPE_SI;
    public static final String ROLE_SI=TMBox.ROLE_SI;
/*    public static final String COUNT_SI="http://wandora.org/si/core/count";
    public static final String ENUM_SI="http://wandora.org/si/core/enum";
    public static final String COUNTMULTIPLE_SI="http://wandora.org/si/core/contentmultiple";
    public static final String COUNTSINGLE_SI="http://wandora.org/si/core/contentsingle";
    public static final String COUNTATMOSTONE_SI="http://wandora.org/si/core/contentatmostone";
    public static final String COUNTATLEASTONE_SI="http://wandora.org/si/core/contentatleastone";
    public static final String BOOLEANTRUE_SI="http://wandora.org/si/core/booleantrue";
    public static final String BOOLEANFALSE_SI="http://wandora.org/si/core/booleanfalse";
    public static final String ASSOCIATIONDEFAULT_SI="http://wandora.org/si/core/associationdefault";
    public static final String BOOLEAN_SI="http://wandora.org/si/core/boolean";
    public static final String DEFAULTPLAYER_SI="http://wandora.org/si/core/defaultplayer";*/

    public static final String DEFAULT_OCCURRENCE_SI = "http://wandora.org/si/core/default-occurrence";

    public static final String DEFAULT_ASSOCIATION_SI = "http://wandora.org/si/core/default-association";
    public static final String DEFAULT_ROLE_1_SI = "http://wandora.org/si/core/default-role-1";
    public static final String DEFAULT_ROLE_2_SI = "http://wandora.org/si/core/default-role-2";

    public static final String ANY_SI="http://wandora.org/si/core/any";
    
    public static final int COUNT_SINGLE=1;
    public static final int COUNT_ATMOSTONE=2;
    public static final int COUNT_ATLEASTONE=3;
    public static final int COUNT_MULTIPLE=4;
    
    /** Creates a new instance of SchemaBox */
    public SchemaBox() {
    }
    
    /**
     * Checks if a topic is instance of the specified class in the schema. The
     * Topic doesn't have to be a direct instance of the class for this method
     * to return true. It can be an instance of some other class that is a
     * subclass of the specified class or a subclass of a subclass of the specified
     * class etc.
     */
    public static boolean isInstanceOf(Topic topic,Topic cls) throws TopicMapException {
        return isInstanceOf(topic,cls,new LinkedHashSet<>());
    }
    private static boolean isInstanceOf(Topic topic,Topic cls,Collection<Topic> processed) throws TopicMapException {
        if(topic.isOfType(cls)) return true;
        else{
            processed.add(cls);
            Iterator<Topic> iter=getSubClassesOf(cls).iterator();
            while(iter.hasNext()){
                Topic subcls=(Topic)iter.next();
                if(!processed.contains(subcls))
                    if(isInstanceOf(topic,subcls,processed)) return true;
            }
            return false;
        }        
    }
    
    /**
     * Gets all instances of a class topic. Here instances doesn't mean only
     * direct instances but also instances of all subclasses, instances of their
     * subclasses etc.
     */
    public static Collection<Topic> getInstancesOf(Topic topic) throws TopicMapException {
        Set<Topic> topics=new LinkedHashSet<>();
        Set<Topic> processed=new LinkedHashSet<>();
        getInstancesOf(topic,topics,processed);
        return topics;
    }
    
    private static void getInstancesOf(Topic topic,Collection<Topic> topics,Collection<Topic> processed) throws TopicMapException {
        processed.add(topic);
        TopicMap tm=topic.getTopicMap();
        topics.addAll(tm.getTopicsOfType(topic));
        Iterator<Topic> iter=getSubClassesOf(topic).iterator();
        while(iter.hasNext()){
            Topic c=(Topic)iter.next();
            if(!processed.contains(c))
                getInstancesOf(c,topics,processed);
        }
    }
    
    /**
     * Gets the content type topic each topic should be an instance of when
     * they are in the specified role in an association. The schema may
     * specify a content type for each role with an association. If such an
     * association doesn't exist then the role itself is returned and topics
     * with this role in an association should be instances of the role topic
     * itself.
     */
    public static Topic getRoleClass(Topic role) throws TopicMapException {
        TopicMap tm=role.getTopicMap();
        Topic roleClassType=tm.getTopic(ROLECLASS_SI);
        Topic roleType=tm.getTopic(ROLE_SI);
        Topic contentType=tm.getTopic(CONTENTTYPE_SI);
        if(roleClassType==null || roleType==null) return role;
        Collection<Association> as=role.getAssociations(roleClassType,roleType);
        if(as.isEmpty()) return role;
        Association a=(Association)as.iterator().next();
        // WAS: Topic p=a.getPlayer(contentType);
        Topic p=a.getPlayer(roleClassType);
        return p;
    }
    /**
     * Gets sub classes of a topic using the standard topics for association type
     * and role topics. Only gets direct sub classes of the topic.
     */
    public static Collection<Topic> getSubClassesOf(Topic topic) throws TopicMapException {
        return getSubClassesOf(topic,XTMPSI.SUBCLASS,XTMPSI.SUPERCLASS_SUBCLASS,XTMPSI.SUPERCLASS);
    }
    /**
     * Gets sub classes of a topic using custom topics for association type and
     * role topics in the association. You need to give a topic for the association type,
     * the role topic for the sub class and the role topic for the super class.
     * Only gets direct subclasses of the topic.
     */
    public static Collection<Topic> getSubClassesOf(Topic topic,String subSI,String assocSI,String superSI) throws TopicMapException {
        Set<Topic> topics=new LinkedHashSet<>();
        TopicMap tm=topic.getTopicMap();
        Topic supersub=tm.getTopic(assocSI);
        if(supersub==null) return topics;
        Topic superc=tm.getTopic(superSI);
        if(superc==null) return topics;
        Topic subc=tm.getTopic(subSI);
        if(subc==null) return topics;
        Iterator<Association> iter=topic.getAssociations(supersub,superc).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic t=a.getPlayer(subc);
            if(t!=null){
                topics.add(t);
            }
        }
        return topics;
    }
    /**
     * Gets super classes of a topic using the standard topics for association type
     * and role topics. Only gets direct superclasses of the topic.
     */
    public static Collection<Topic> getSuperClassesOf(Topic topic) throws TopicMapException {
        return getSuperClassesOf(topic,XTMPSI.SUBCLASS,XTMPSI.SUPERCLASS_SUBCLASS,XTMPSI.SUPERCLASS);      
    }
    /**
     * Gets super classes of a topic using custom topics for association type and
     * role topics in the association. You need to give a topic for the association type,
     * the role topic for the sub class and the role topic for the super class.
     * Only gets direct superclasses of the topic.
     */
    public static Collection<Topic> getSuperClassesOf(Topic topic,String subSI,String assocSI,String superSI) throws TopicMapException {
        Set<Topic> topics=new LinkedHashSet<>();
        TopicMap tm=topic.getTopicMap();
        Topic supersub=tm.getTopic(assocSI);
        if(supersub==null) return topics;
        Topic superc=tm.getTopic(superSI);
        if(superc==null) return topics;
        Topic subc=tm.getTopic(subSI);
        if(subc==null) return topics;
        Iterator<Association> iter=topic.getAssociations(supersub,subc).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic t=a.getPlayer(superc);
            if(t!=null){
                topics.add(t);
            }
        }
        return topics;
    }
    
    /**
     * Recursively gets all super classes of a topic using the standard association
     * type and association roles. That is gets direct super classes of the topic
     * and all their super classes etc.
     */
    public static Collection<Topic> getSuperClassesOfRecursive(Topic topic) throws TopicMapException {
        Set<Topic> processed=new LinkedHashSet<>();
        Set<Topic> classes=new LinkedHashSet<>();
        getSuperClassesOfRecursive(topic,processed,classes);
        return classes;
    }
    private static void getSuperClassesOfRecursive(Topic topic, Set<Topic> processed, Set<Topic> classes) throws TopicMapException {
        if(processed.contains(topic)) return;
        processed.add(topic);
        Collection<Topic> cs=getSuperClassesOf(topic);
        classes.addAll(cs);
        Iterator<Topic> iter=cs.iterator();
        while(iter.hasNext()){
            Topic c=(Topic)iter.next();
            getSuperClassesOfRecursive(c,processed,classes);
        }
    }
    
    /**
     * Gets all content types of this class is an instance of.
     * Instance of here means that the topic is
     * either a direct instance of the class or it is an instance of a class that
     * is a subclass (possibly through many associations) of the content type.
     */
    public static Collection<Topic> getContentTypesOf(Topic topic) throws TopicMapException {
/*        HashSet topics=new HashSet();
        Topic contenttype=topic.getTopicMap().getTopic(CONTENTTYPE_SI);
        if(contenttype==null) return topics;
        
        Iterator iter=topic.getTypes().iterator();
        while(iter.hasNext()){
            Topic type=(Topic)iter.next();
            if(type.isOfType(contenttype)) topics.add(type);
        }
        return topics;*/
        
        Set<Topic> topics=new LinkedHashSet<>();
        Set<Topic> processed=new LinkedHashSet<>();
        Topic contenttype=topic.getTopicMap().getTopic(CONTENTTYPE_SI);
        if(contenttype==null) return topics;
        getContentTypesOfNoSupers(topic,processed,topics,contenttype);
        
        Collection<Topic> supers=getSuperClassesOfRecursive(topic);
        Iterator<Topic> iter=supers.iterator();
        while(iter.hasNext()){
            Topic type=(Topic)iter.next();
            getContentTypesOfNoSupers(type,processed,topics,contenttype);
        }
        
        return topics;
    }
    
    private static void getContentTypesOfNoSupers(Topic topic,Collection<Topic> processed, Collection<Topic> types,Topic contenttype) throws TopicMapException {
        if(processed.contains(topic)) return;
        processed.add(topic);

        Iterator<Topic> iter=topic.getTypes().iterator();
        while(iter.hasNext()){
            Topic type=(Topic)iter.next();
            if(type.isOfType(contenttype)) types.add(type);
            Collection<Topic> supers=getSuperClassesOfRecursive(type);
            Iterator<Topic> iter2=supers.iterator();
            while(iter2.hasNext()){
                Topic type2=(Topic)iter2.next();
                if(type2.isOfType(contenttype)) types.add(type2);
            }
        }
    }
    
    /**
     * Gets all association types that can be used with the specified topic.
     * Possible association types are specified in the content type topics. This
     * method collects all such association types from all content types of the
     * specified topic. Content types of the topic are resolved with <code>getContentTypesOf</code>.
     */
    public static Collection<Topic> getAssociationTypesFor(Topic topic) throws TopicMapException {
        Set<Topic> topics=new LinkedHashSet<>();
        Topic atype=topic.getTopicMap().getTopic(ASSOCIATIONTYPE_SI);
        if(atype==null) return topics;
        Topic ctype=topic.getTopicMap().getTopic(CONTENTTYPE_SI);
        if(ctype==null) return topics;
        Collection<Topic> ctypes=getContentTypesOf(topic);
        Iterator<Topic> iter=ctypes.iterator();
        while(iter.hasNext()){
            Topic type=(Topic)iter.next();
            Iterator<Association> iter2=type.getAssociations(atype,ctype).iterator();
            while(iter2.hasNext()){
                Association a=(Association)iter2.next();
                Topic player=a.getPlayer(atype);
                if(player!=null){
                    topics.add(player);
                }
            }
        }
        return topics;
    }
    
    /**
     * Gets all occurrence types that can be used with the specified topic.
     * Possible occurrence types are specified in the content type topics. This
     * method collects all such occurrence types from all content types of the
     * specified topic. Content types of the topic are resolved with <code>getContentTypesOf</code>.
     */
    public static Collection<Topic> getOccurrenceTypesFor(Topic topic) throws TopicMapException {
        Set<Topic> topics=new LinkedHashSet<>();
        Topic otype=topic.getTopicMap().getTopic(OCCURRENCETYPE_SI);
        if(otype==null) return topics;
        Topic ctype=topic.getTopicMap().getTopic(CONTENTTYPE_SI);
        if(ctype==null) return topics;
        Collection<Topic> ctypes=getContentTypesOf(topic);
        Iterator<Topic> iter=ctypes.iterator();
        while(iter.hasNext()){
            Topic type=(Topic)iter.next();
            Iterator<Association> iter2=type.getAssociations(otype,ctype).iterator();
            while(iter2.hasNext()){
                Association a=(Association)iter2.next();
                Topic player=a.getPlayer(otype);
                if(player!=null){
                    topics.add(player);
                }
            }
        }
        return topics;        
    }
    
    /**
     * Creates a new superclass-subclass association with the specified topics as
     * the sub class and the super class.
     */
    public static void setSuperClass(Topic subclass,Topic superclass) throws TopicMapException {
        TopicMap tm=subclass.getTopicMap();
        Topic supersub=TMBox.getOrCreateTopic(tm,XTMPSI.SUPERCLASS_SUBCLASS);
        Topic superc=TMBox.getOrCreateTopic(tm,XTMPSI.SUPERCLASS);
        Topic subc=TMBox.getOrCreateTopic(tm,XTMPSI.SUBCLASS);
        Association a=tm.createAssociation(supersub);
        a.addPlayer(subclass, subc);
        a.addPlayer(superclass, superc);
    }
    /*
    public static int getAssociationTypeCount(Topic associationType){
        TopicMap tm=associationType.getTopicMap();
        Topic count=tm.getTopic(COUNT_SI);
        if(count==null) return COUNT_MULTIPLE;
        Topic atype=tm.getTopic(ASSOCIATIONTYPE_SI);
        if(atype==null) return COUNT_MULTIPLE;
        Iterator iter=associationType.getAssociations(count,atype).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic t=a.getPlayer(count);
            if(t!=null){
                if(t.getSubjectIdentifiers().contains(tm.createLocator(COUNTSINGLE_SI))) return COUNT_SINGLE;
                else if(t.getSubjectIdentifiers().contains(tm.createLocator(COUNTATMOSTONE_SI))) return COUNT_ATMOSTONE;
                else if(t.getSubjectIdentifiers().contains(tm.createLocator(COUNTATLEASTONE_SI))) return COUNT_ATLEASTONE;
                else if(t.getSubjectIdentifiers().contains(tm.createLocator(COUNTMULTIPLE_SI))) return COUNT_MULTIPLE;
            }
        }
        return COUNT_MULTIPLE;
    }
    
    public static int getOccurrenceTypeCount(Topic occurrenceType){
        TopicMap tm=occurrenceType.getTopicMap();
        Topic count=tm.getTopic(COUNT_SI);
        if(count==null) return COUNT_MULTIPLE;
        Topic otype=tm.getTopic(OCCURRENCETYPE_SI);
        if(otype==null) return COUNT_MULTIPLE;
        Iterator iter=occurrenceType.getAssociations(count,otype).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic t=a.getPlayer(count);
            if(t!=null){
                if(t.getSubjectIdentifiers().contains(tm.createLocator(COUNTSINGLE_SI))) return COUNT_SINGLE;
                else if(t.getSubjectIdentifiers().contains(tm.createLocator(COUNTATMOSTONE_SI))) return COUNT_ATMOSTONE;
                else if(t.getSubjectIdentifiers().contains(tm.createLocator(COUNTATLEASTONE_SI))) return COUNT_ATLEASTONE;
                else if(t.getSubjectIdentifiers().contains(tm.createLocator(COUNTMULTIPLE_SI))) return COUNT_MULTIPLE;
            }
        }
        return COUNT_MULTIPLE;
    }
    
    public static boolean isAssociationTypeEnum(Topic associationType){
        TopicMap tm=associationType.getTopicMap();
        Topic enu=tm.getTopic(ENUM_SI);
        if(enu==null) return false;
        Topic atype=tm.getTopic(ASSOCIATIONTYPE_SI);
        if(atype==null) return false;
        Iterator iter=associationType.getAssociations(enu,atype).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic t=a.getPlayer(enu);
            if(t!=null){
                if(t.getSubjectIdentifiers().contains(tm.createLocator(BOOLEANTRUE_SI))) return true;
                else if(t.getSubjectIdentifiers().contains(tm.createLocator(BOOLEANFALSE_SI))) return false;
            }
        }
        return false;        
    }*/
    
    /**
     * Gets all roles that can be used with the specified association type.
     * Each association type topic specifies all roles that can (and should)
     * be used whit associations of that type. This method gets those roles.
     */
    public static Collection<Topic> getAssociationTypeRoles(Topic associationType) throws TopicMapException {
        Set<Topic> topics=new LinkedHashSet<>();
        TopicMap tm=associationType.getTopicMap();
        Topic role=tm.getTopic(ROLE_SI);
        if(role==null) return topics;
        Topic atype=tm.getTopic(ASSOCIATIONTYPE_SI);
        if(atype==null) return topics;
        Iterator<Association> iter=associationType.getAssociations(role,atype).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic t=a.getPlayer(role);
            if(t!=null){
                topics.add(t);
            }
        }
        return topics;
    }
}
