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
 * Topic.java
 *
 * Created on June 10, 2004, 11:12 AM
 */

package org.wandora.topicmap;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <p>
 * The abstract Topic class. Each topic implementation should extend this class.
 * </p><p>
 * Note that you should not store Topic objects except for very short times. Topics may be removed or
 * merged at any time and the topic objects will become invalid. If you are doing something where this 
 * can happen, be sure to check if the topic is invalid with the isRemoved method. If it returns true,
 * the topic may have been removed from the topic map or it might have been merged with some other topic.
 * You can get this possible other topic with TopicMap.getTopic which will need the subject identifier
 * of the topic, which means that usually it is better to just store the subject identifiers instead
 * of the topics in the first place.
 * </p><p>
 * Many methods may cause topic merges in the topic map. Such methods include addSubjectIdentifier,
 * setBaseName and setSubjectLocator. If a merge occurs, the merge will be handled in such a way that
 * the object whose method was called will stay a valid topic object. In other words, other merging
 * topics will be merged into it instead of the other way around.
 * </p>
 * @author  olli
 */
public abstract class Topic {
    
    /**
     * Gets the topic ID. Topic id is unique in the topic map. It cannot be changed
     * after topic creation and is generally not visible to the user. It can be used
     * when exporting topic map into XTM or storing it in a database.
     */
    public abstract String getID() throws TopicMapException ;
    /** Gets all subject identifiers of the topic. */
    public abstract Collection<Locator> getSubjectIdentifiers() throws TopicMapException ;
    /** Adds a subject identifier for the topic. This may cause a topic merge. */
    public abstract void addSubjectIdentifier(Locator l) throws TopicMapException ;
    /** Removes a subject identifier. */
    public abstract void removeSubjectIdentifier(Locator l) throws TopicMapException ;
    /** Gets the topic base name or null if no base name has been set. */
    public abstract String getBaseName() throws TopicMapException ;
    /** Sets the topic base name. This may cause a topic merge. */
    public abstract void setBaseName(String name) throws TopicMapException ;
    /** Gets all topic types. */
    public abstract Collection<Topic> getTypes() throws TopicMapException ;
    /** Adds a topic type. If topic already has that type, does nothing.*/
    public abstract void addType(Topic t) throws TopicMapException ;
    /** Removes a topic type. If topic is not of the specified type, does nothing. */
    public abstract void removeType(Topic t) throws TopicMapException ;
    /** Checks if this topic is of the specified type. */
    public abstract boolean isOfType(Topic t) throws TopicMapException ;
    /** 
     * Gets a variant with the specified scope. The scope must be a Collection of Topics.
     * Returns null if there is no variant with the specified scope.
     */
    public abstract String getVariant(Set<Topic> scope) throws TopicMapException ;
    /**
     * Sets the variant with the specified scope. Will overwrite previous value
     * if there already is a variant name with the specified scope.
     */
    public abstract void setVariant(Set<Topic> scope,String name) throws TopicMapException ;
    /** Gets the scopes of all variant names. */
    public abstract Set<Set<Topic>> getVariantScopes() throws TopicMapException ;
    /** Removes a variant name with the specified scope. If no such variant exists, does nothing. */
    public abstract void removeVariant(Set<Topic> scope) throws TopicMapException ;
    /** Gets data with the specified type and version. Returns null if data is not found. */
    public abstract String getData(Topic type,Topic version) throws TopicMapException ;
    /** Returns a Hashtable mapping data versions to data content. */
    public abstract Hashtable<Topic,String> getData(Topic type) throws TopicMapException ;
    /** Gets all used data types. */
    public abstract Collection<Topic> getDataTypes() throws TopicMapException ;
    /** Sets several data values. Each value is set with same type, the other parameter
        should contain versions mapped to the actual values. */
    public abstract void setData(Topic type,Hashtable<Topic,String> versionData) throws TopicMapException ;
    /** Sets data with specified type and version. */
    public abstract void setData(Topic type,Topic version,String value) throws TopicMapException ;
    /** Removes data with specified type and version. If no such data value is set, does nothing. */
    public abstract void removeData(Topic type,Topic version) throws TopicMapException ;
    /** Removes all data with the specified type. */
    public abstract void removeData(Topic type) throws TopicMapException ;
    /** Gets the topic subject locator or null if it has not been set. */
    public abstract Locator getSubjectLocator() throws TopicMapException ;
    /** Sets the topic subject locator overwriting possible previous value. */
    public abstract void setSubjectLocator(Locator l) throws TopicMapException ;
    /** Gets the topic map this topic belongs to. */
    public abstract TopicMap getTopicMap();
    /** Gets all associations where this topic is a player. */
    public abstract Collection<Association> getAssociations() throws TopicMapException ;
    /** Gets all associations of specified type where this topic is a player. */
    public abstract Collection<Association> getAssociations(Topic type) throws TopicMapException ;
    /** Gets associations of the specified type where this topic is in the specified role */
    public abstract Collection<Association> getAssociations(Topic type,Topic role) throws TopicMapException ;
    /**
     * Removes this topic. If this topic is used in any association as the association type or role of any member
     * or this topic is used in any topic in variant scope, data version, data type or topic type,
     * a TopicInUseException is thrown. This topic may however be used as a player of associations. All such
     * associations are deleted along with this topic.
     */
    public abstract void remove() throws TopicMapException;
    public abstract long getEditTime() throws TopicMapException ;
    public abstract void setEditTime(long time) throws TopicMapException ;
    public abstract long getDependentEditTime() throws TopicMapException ;
    public abstract void setDependentEditTime(long time) throws TopicMapException ;
    /** 
     * Returns true if this topic has been removed from the topic map it belonged to. Note that it might
     * have been removed as a result of topics being merged and still exists in the topic map but as another
     * topic object.
     */
    public abstract boolean isRemoved() throws TopicMapException ;
    /**
     * Returns true if and only if remove() can be called without it throwing TopicInUseException. This
     * is exactly when this topic is not used in any association as the association type or role of any member,
     * and this topic is not used in any topic in variant scope, data version, data type or topic type.
     * This topic may however be used as a player of associations.
     */
    public abstract boolean isDeleteAllowed() throws TopicMapException ;
    
    /** Gets topics which have data with this topic as type. */
    public abstract Collection<Topic> getTopicsWithDataType() throws TopicMapException ;
    /** Gets topics which have data with this topic as version. */
    public abstract Collection<Topic> getTopicsWithDataVersion() throws TopicMapException ;
    /** Gets associations that have this topic as type. */
    public abstract Collection<Association> getAssociationsWithType() throws TopicMapException ;
    /** Gets associations that have this topic as role. */
    public abstract Collection<Association> getAssociationsWithRole() throws TopicMapException ;
    /** Gets topics which have variants with this topic in scope. */
    public abstract Collection<Topic> getTopicsWithVariantScope() throws TopicMapException ;
    
    /**
     * Gets data with the specified type and language version. If data with the specified language is not found
     * tries to get data with language independent version (WandoraManage.LANGINDEPENDENT_SI). If that is not
     * found tries to get data of the specified type with any version. Use getData(Topic,Topic) to get the
     * data with specified type and version or null if it does not exist.
     */
    public String getData(Topic type,String lang) throws TopicMapException {
        String langsi=XTMPSI.getLang(lang);
        Topic langT=getTopicMap().getTopic(langsi);
        String data=null;
        if(langT!=null) {
            data=getData(type,langT);
        }
        if(data==null){
            langT=getTopicMap().getTopic(TMBox.LANGINDEPENDENT_SI);
            if(langT!=null){
                data=getData(type,langT);
            }
            if(data==null){
                Hashtable<Topic,String> ht=getData(type);
                if(ht==null || ht.isEmpty()) return null;
                Iterator<String> iter=ht.values().iterator();
                if(iter.hasNext()) data=iter.next();
            }
        }
        return data;
    }
    
    /**
     * Sets a variant with scope containing the display topic and the language topic of the specified
     * language.
     */
    public void setDisplayName(String lang,String name) throws TopicMapException {
        if(getTopicMap().isReadOnly()) throw new TopicMapReadOnlyException();
        String langsi=XTMPSI.getLang(lang);
        Topic langT=getTopicMap().getTopic(langsi);
        String dispsi=XTMPSI.DISPLAY;
        Topic dispT=getTopicMap().getTopic(dispsi);
        Set<Topic> scope=new HashSet<>();
        if(langT==null) {
            langT=getTopicMap().createTopic();
            langT.addSubjectIdentifier(getTopicMap().createLocator(langsi));
        }
        scope.add(langT);
        if(dispT==null) {
            dispT=getTopicMap().createTopic();
            dispT.addSubjectIdentifier(getTopicMap().createLocator(dispsi));            
        }
        scope.add(dispT);
        setVariant(scope,name);
    }
    
    /**
     * Gets a display name for English language. Calls getDisplayName(String) with parameter
     * "en".
     * @see #getDisplayName(String)
     */
    public String getDisplayName() throws TopicMapException  {
        return getDisplayName("en");
    }
    
    /**
     * Gets a name suitable for display in the specified language. This method will call getName with
     * a scope containing the display topic and the language topic of the specified language. Will first
     * try to get a variant with this exact scope but if one is not found, may return a variant with almost
     * this scope or base name or subject identifier.
     */
    public String getDisplayName(String lang) throws TopicMapException {
        String langsi=XTMPSI.getLang(lang);
        Topic langT=getTopicMap().getTopic(langsi);
        String dispsi=XTMPSI.DISPLAY;
        Topic dispT=getTopicMap().getTopic(dispsi);
        Set<Topic> scope=new HashSet<>();
        if(langT!=null) scope.add(langT);
        if(dispT!=null) scope.add(dispT);
        return getName(scope);
    }
    /**
     * Gets a name suitable for sorting in the specified language. This method will call getName with
     * a scope containing the sort topic and the language topic of the specified language. Will first
     * try to get a variant with this exact scope but if one is not found, may return a variant with almost
     * this scope or base name or subject identifier.
     */
    public String getSortName(String lang) throws TopicMapException {
        String langsi=XTMPSI.getLang(lang);
        Topic langT=getTopicMap().getTopic(langsi);
        String sortsi=XTMPSI.SORT;
        Topic sortT=getTopicMap().getTopic(sortsi);
        Set<Topic> scope=new HashSet<>();
        if(langT!=null) scope.add(langT);
        if(sortT!=null) scope.add(sortT);
        return getName(scope);
    }
    
    /**
     * Gets a name for the topic. Tries to get a name that matches the scope as much as possible.
     * If a suitable name is not found in variants, returns the base name, if it is null returns
     * one of the subject identifiers, if there is none returns "[unnamed]".
     */
    public String getName(Set<Topic> scope) throws TopicMapException {
        String name=null;
        try {
            if(scope != null) name=getVariant(scope);
            if(name==null || name.trim().length()==0){
                int maxcount=0;
                Set<Set<Topic>> scopes=getVariantScopes();
                if(scopes != null) {
                    Iterator<Set<Topic>> iter=scopes.iterator();
                    while(iter.hasNext()){
                        Set<Topic> s= iter.next();
                        String vname=getVariant(s);
                        if(vname==null || vname.trim().length()==0) continue;
                        int count=0;
                        if(scope != null) {
                            Iterator<Topic> iter2=scope.iterator();
                            while(iter2.hasNext()){
                                Topic t=iter2.next();
//                                if(s.contains(t)) count++;
                                Iterator<Topic> iter3=s.iterator();
                                while(iter3.hasNext()){
                                    Topic st=iter3.next();
                                    if(st.mergesWithTopic(t)){
                                        count++;
                                        break;
                                    }
                                }
                            }
                        }
                        if(count>maxcount){
                            maxcount=count;
                            name=getVariant(s);
                        }
                    }
                }
                if(name==null || name.trim().length()==0) name=getBaseName();
                if(name==null || name.trim().length()==0){
                    Collection<Locator> sis=getSubjectIdentifiers();
                    if(!sis.isEmpty()) name=(sis.iterator().next()).toExternalForm();
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if(name==null || name.trim().length()==0) name="[unnamed]";
        return name;
    }
    
    /** Returns one of the subject identifiers of this topic or null if none exists. */
    public Locator getOneSubjectIdentifier() throws TopicMapException {
        Collection<Locator> c=getSubjectIdentifiers();
        if(c.size()>0) return c.iterator().next();
        else return null;
    }
    
    /**
     * Returns the subject identifier for the topic that is the first in
     * lexicographical ordering. This guarantees that you'll always
     * get the same subject identifier as long as the subject identifiers of the
     * topic have not changed between calls. This can be desirable in some
     * situations. However, there is a performance penalty in using this as
     * opposed to getOneSubjectIdentifier.
     */
    public Locator getFirstSubjectIdentifier() throws TopicMapException {
        List<Locator> ls=new ArrayList<>(getSubjectIdentifiers());
        if(ls.isEmpty()) return null;
        if(ls.size()==1) return ls.get(0);
        Locator least=null;
        for(Locator l : ls) {
            if(least==null || l.compareTo(least)<0) least=l;
        }
        return least;
    }
    
    /** 
     * Checks if this topic would merge with the topic given as parameter. 
     * The topic given as parameter may be from this or another topic map.
     */
    public boolean mergesWithTopic(Topic topic) throws TopicMapException {
        if(topic==null) return false;
        if(this==topic) return true;
        if(getBaseName()!=null && topic.getBaseName()!=null && getBaseName().equals(topic.getBaseName())) 
            return true;
        Collection<Locator> tsis=topic.getSubjectIdentifiers();
        for(Locator l : getSubjectIdentifiers()) {
            if(tsis.contains(l)) return true;
        }
        if(getSubjectLocator()!=null && topic.getSubjectLocator()!=null && getSubjectLocator().equals(topic.getSubjectLocator())) 
            return true;            
        return false;
    }
    
    /**
     * Gets name of this topic suitable for display. Uses getDisplayName to do this.
     * @see #getDisplayName()
     */
    @Override
    public String toString(){
        try{
            return getDisplayName();
        }
        catch(TopicMapException tme){
            tme.printStackTrace();
            return "<Exception>";
        }
    }
}
