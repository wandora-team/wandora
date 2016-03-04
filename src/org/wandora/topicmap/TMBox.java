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
 * 
 *
 * TMBox.java
 *
 * Created on June 7, 2004, 12:43 PM
 */




package org.wandora.topicmap;

import org.wandora.topicmap.layered.*;
import java.util.*;
import static org.wandora.utils.Tuples.*;
import org.wandora.utils.GripCollections;
import org.wandora.application.gui.*;
/**
 * TMBox contains various topic map related tools and utility methods.
 *
 * @author  olli
 */
public class TMBox {
    
    public static final String WANDORACLASS_SI = "http://wandora.org/si/core/wandora-class";
    public static final String ASSOCIATIONTYPE_SI = "http://wandora.org/si/core/association-type";
    public static final String ASSOCIATIONROLE_SI = "http://wandora.org/si/core/associationrole";
    public static final String ROLE_SI = "http://wandora.org/si/core/role";
    public static final String LANGINDEPENDENT_SI = "http://wandora.org/si/core/lang-independent";
    public static final String ASSOCIATIONROLECATEGORIES_SI = "http://wandora.org/si/core/associationrolecategories";
    public static final String OCCURRENCETYPE_SI = "http://wandora.org/si/core/occurrence-type";
    public static final String HIDELEVEL_SI = "http://wandora.org/si/core/hidelevel";
    public static final String CATEGORYHIERARCHY_SI = "http://wandora.org/si/common/categoryhierarchy";
    public static final String SUPERCATEGORY_SI = "http://wandora.org/si/common/supercategory";
    public static final String SUBCATEGORY_SI = "http://wandora.org/si/common/subcategory";
    public static final String ENTRYTIME_SI = "http://wandora.org/si/common/entrytime";

    public static final String VARIANT_NAME_VERSION_SI = "http://wandora.org/si/core/variant-name-version";
    public static final String LANGUAGE_SI = "http://wandora.org/si/core/language";

    // Language topics, data versions and display scope (moved from WandoraAdminManager)
    
    /**
     * Gets a subject identifier for each topic that represents some language in
     * the topic map. This is done by getting all topics that are of language type.
     * The language topic is assumed to have a subject identifier LANGUAGE_SI.
     */
    public static String[] getLanguageSIs(TopicMap tm) throws TopicMapException {
        Collection<Topic> languageTopics = TMBox.sortTopics(tm.getTopicsOfType(LANGUAGE_SI), "en");       
        if(languageTopics != null && languageTopics.size() > 0) {
            ArrayList languageSIs = new ArrayList();
            for(Topic languageTopic : languageTopics) {
                try {
                    languageSIs.add( languageTopic.getOneSubjectIdentifier().toExternalForm() );
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return (String []) languageSIs.toArray(new String[] { } );
        }
        else {
            return new String[] {};
            //return new String[] { org.wandora.piccolo.WandoraManager.LANGINDEPENDENT_SI };
        }
    }
    
    
    public static Topic[] getLanguageTopics(TopicMap tm) throws TopicMapException {
        Collection<Topic> languageTopics = TMBox.sortTopics(tm.getTopicsOfType(LANGUAGE_SI), "en");       
        if(languageTopics != null && languageTopics.size() > 0) {
            return (Topic []) languageTopics.toArray( new Topic[] { } );
        }
        else {
            return new Topic[] {};
        }
    }
    
    
    
    /**
     * Gets a subject identifier for each topic that represents some variant name version in
     * the topic map. This is done by getting all topics that are of name version type.
     * The name version topic is assumed to have a subject identifier VARIANT_NAME_VERSION_SI.
     */
    public static String[] getNameVersionSIs(TopicMap tm) throws TopicMapException {
        Collection<Topic> variantNameVersionTopics = TMBox.sortTopics(tm.getTopicsOfType(VARIANT_NAME_VERSION_SI), "en");       
        if(variantNameVersionTopics != null && variantNameVersionTopics.size() > 0) {
            ArrayList variantNameVersionSIs = new ArrayList();
            for( Topic variantNameVersionTopic : variantNameVersionTopics ) {
                try {
                    variantNameVersionSIs.add( variantNameVersionTopic.getOneSubjectIdentifier().toExternalForm() );
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return (String []) variantNameVersionSIs.toArray(new String[] { } );
        }
        else {
            return new String[]{
                // XTMPSI.DISPLAY,
                // XTMPSI.SORT
            };
        }
    }
    
    
    
    public static Topic[] getNameVersionTopics(TopicMap tm) throws TopicMapException {
        Collection<Topic> variantNameVersionTopics = TMBox.sortTopics(tm.getTopicsOfType(VARIANT_NAME_VERSION_SI), "en");       
        if(variantNameVersionTopics != null && variantNameVersionTopics.size() > 0) {
            return (Topic []) variantNameVersionTopics.toArray( new Topic[] { } );
        }
        else {
            return new Topic[] { };
        }
    }
    
    
    
    /**
     * Gets display name scope for English language. This scope will contain
     * the English language topic and display name topic, if they exist in
     * the topic map.
     */
    public static HashSet<Topic> getGUINameScope(TopicMap tm) throws TopicMapException {
        HashSet<Topic> nameScope = new LinkedHashSet<Topic>();
        Topic t = tm.getTopic(XTMPSI.getLang("en"));
        if(t != null && !t.isRemoved()) nameScope.add(t);
        t = tm.getTopic(XTMPSI.DISPLAY);
        if(t != null && !t.isRemoved()) nameScope.add(t);
        return nameScope;
    }
    
    
    ////////////////////////////////////////////////////////////

    /**
     * <p>
     * Creates a user readable message from an integer that encodes merge/split
     * information. These kinds of integers are returned by the methods that
     * check if a certain operation will result in a merge or split.
     * </p>
     * <p>
     * The integer uses four bits to encode the information. If bit 1 is set,
     * changes will be visible in the whole layer stack instead of only in an
     * individual layer. For example merge of two topics in the same layer is
     * not visible in layer stack if those topics were already merged.
     * </p>
     * <p>
     * If bit 2 is set there will be a real merge in the selected layer. That is,
     * two topics, both in the selected layer, will be merged together.
     * </p>
     * <p>
     * If bit 4 is set two topics will be merged virtually.
     * There isn't a real merge of topics in any layer but two separate topics
     * are going to be visible as only one topic because of the changes. Note
     * that the topics may be in the same layer or different layers.
     * </p>
     * <p>
     * If bit 8 is set a topic will be split. There is no operation that would
     * cause a single topic in a layer to be split in two (or more) topics 
     * in the same layer. Splits are always virtual, two topics that were visible
     * as one merged topic are going to be visible as separate topics. The topics
     * may be in the same layer or different layers.
     * </p>
     * <p>
     * Value 0 means that no split or merge will occur. Value 1 is not possible
     * but other values up to and including 15 are possible.
     * </p>
     */
    public static String buildMergeMessage(int a){
        String message="";
        if( (a&2)!=0 ) message+="A real merge of two topics in the selected layer.\n";
        if( (a&4)!=0 ) message+="A virtual merge of two topics in two different layers.\n";
        if( (a&8)!=0 ) message+="A split of two topics in two different layers.\n";
        if( (a&1)!=0 ) message+="Changes will be visible in whole layer stack.\n";
        else message+="Changes will not be visible in whole layer stack.\n";
        return message;
    }
    
    
    public static ConfirmResult checkBaseNameChange(java.awt.Component parent,Topic topic,String value) throws TopicMapException{
        return checkBaseNameChange(parent,topic,value,false);
    }
    
    
    /**
     * Checks if the base name of the given topic can be changed to the given value.
     * Checks if the change will result in merges or splits and in such a case
     * warns the user with a dialog. Dialog has buttons to allow the change or
     * cancel the operation. If multiple parameter is true, it will also contain
     * buttons to allow changes for all or deny for all. Value parameter can be
     * null to indicate that current base name is to be removed. Parent parameter
     * is only used as the parent of the possible dialog.
     */
    public static ConfirmResult checkBaseNameChange(java.awt.Component parent,Topic topic,String value,boolean multiple) throws TopicMapException{
        if(value==null) value="";
        value=value.trim();
        String orig=topic.getBaseName();
        if((orig==null && value.length()>0) || (orig!=null && !orig.equals(value))){
            int a=checkBaseNameMerge(topic,value);
            if(a!=0){
                String message="";
                if(value.length()>0) message="New base name \""+value+"\" will cause following:\n";
                else message="Removing base name will cause following:\n";
                message+=buildMergeMessage(a);
                ConfirmResult res=ConfirmDialog.showConfirmDialog(parent,"Merge/split confirmation",message,!multiple);
                return res;
            }
        }
        return ConfirmResult.yes;
    }
    
    
    public static ConfirmResult checkSubjectLocatorChange(java.awt.Component parent,Topic topic,String value) throws TopicMapException{
        return checkSubjectLocatorChange(parent,topic,value,false);
    }
    
    
    /**
     * Like checkBaseNameChange, checks if the subject locator of the topic can be changed.
     */
    public static ConfirmResult checkSubjectLocatorChange(java.awt.Component parent,Topic topic,String value,boolean multiple) throws TopicMapException{
        if(value==null) value="";
        value=value.trim();
        Locator orig=topic.getSubjectLocator();
        if((orig==null && value.length()>0) || (orig!=null && !orig.toExternalForm().equals(value))){
            Locator l=null;
            if(value.length()>0) l=topic.getTopicMap().createLocator(value);
            int a=checkSubjectLocatorMerge(topic,l);
            if(a!=0){
                String message="";
                if(l!=null) message="New subject locator \""+value+"\" will cause following:\n";
                else message="Removing subject locator will cause following:\n";
                message+=buildMergeMessage(a);
                ConfirmResult res=ConfirmDialog.showConfirmDialog(parent,"Merge/split confirmation",message,!multiple);
                return res;
            }
        }
        return ConfirmResult.yes;
    }
    
    
    public static ConfirmResult checkSubjectIdentifierChange(java.awt.Component parent,Topic topic,Locator value,boolean add) throws TopicMapException{
        return checkSubjectIdentifierChange(parent,topic,value,add,false);
    }
    
    
    /**
     * Checks if a subject identifier can be added or removed from a topic. If the
     * operation results in merges or splits, warns the user with a dialog like in
     * checkBaseNameChange. If add parameter is true then a subject identifier is about
     * to be added, otherwise it is to be removed.
     */
    public static ConfirmResult checkSubjectIdentifierChange(java.awt.Component parent,Topic topic,Locator value,boolean add,boolean multiple) throws TopicMapException{
        int a=checkSubjectIdentifierMerge(topic,value,add);
        if(a!=0){
            String message="";
            String valueString = value.toExternalForm();
            if(valueString.length() > 256) {
                valueString = valueString.substring(0, 256) + "...";
            }
            if(add) message="New subject identifier \""+valueString+"\" will cause following:\n";
            else message="Removing subject identifier \""+valueString+"\" will cause following:\n";
            message+=buildMergeMessage(a);
            ConfirmResult res=ConfirmDialog.showConfirmDialog(parent,"Merge/split confirmation",message,!multiple);
            return res;
        }
        return ConfirmResult.yes;        
    }
    
    
    public static ConfirmResult checkTopicRemove(java.awt.Component parent,Topic topic) throws TopicMapException {
        return checkTopicRemove(parent,topic,false);
    }
    
    
    /**
     * Checks if removing a topic will result in a split and warns the user in that case.
     * Like checkBaseNameChange, if multiple parameter is true, warning dialog will
     * have options for allow all or deny all.
     */
    public static ConfirmResult checkTopicRemove(java.awt.Component parent,Topic topic,boolean multiple) throws TopicMapException {
        if(checkTopicRemoveSplit(topic)){
            ConfirmResult res=ConfirmDialog.showConfirmDialog(parent,"Split confirmation","Removing topic will cause the split of two or more topics\nin different layers. Continue?",!multiple);
            return res;
        }
        return ConfirmResult.yes;
    }
    
    
    
    // --------
    
    
    
    private static int layerSplit(HashSet<T2<Topic,Topic>> links,Collection<Topic> topics){
        Stack<Topic> stack=new Stack<Topic>();
        HashSet<Topic> included=new HashSet<Topic>();
        if(topics.isEmpty()) return 0;
        stack.add(topics.iterator().next());
        included.add(stack.peek());
        while(!stack.isEmpty()){
            Topic u=stack.pop();
            for(Topic i : topics){
                if(included.contains(i)) continue;
                if(links.contains(t2(u,i))){
                    stack.push(i);
                    included.add(i);
                }
            }
        }
        return included.size()-topics.size();
    }
    /*
     * Following methods are used by the public check*Change methods.
     *
     * Returned int has different bits set depending on what kind of merge
     * or split occurs. 0 means that no merge or split occurs. 1 is never returned
     * but other values up to and including 15 are possible return values. If the
     * topic given as parameter is not an instance of LayeredTopic then first
     * bit is always set if there are any merges and only other bit that can be
     * set is 2. In other words return value will be 0 if no changes or 3
     * if two topics will be merged and nothing else.
     *
     * 1 = visible in layerstack
     * 2 = merge in a specific layer
     * 4 = merge between topics of different layers
     * 8 = split between topics of different layers
     */
    
    public static int checkBaseNameMerge(Topic t,String newBaseName) throws TopicMapException {
        if(t instanceof LayeredTopic){
            int ret=0;
            LayeredTopic lt=(LayeredTopic)t;
            LayerStack tm=lt.getLayerStack();
            Layer selectedLayer=tm.getSelectedTreeLayer();
            Topic et=tm.getTopicForSelectedTreeLayer(lt);
            
            {
                LayeredTopic u=(LayeredTopic)tm.getTopicWithBaseName(newBaseName);
                if(u!=null){
                    if(!u.mergesWithTopic(lt)) ret|=1;
                }
            }
            {
//                Topic u=tm.getSelectedLayer().getTopicMap().getTopicWithBaseName(newBaseName);
                Topic u=selectedLayer.getTopicMap().getTopicWithBaseName(newBaseName);
                if(u!=null){
//                    Topic et=lt.getTopicForSelectedLayer();
                    if(et==null || !et.mergesWithTopic(u)) ret|=2;
                }
            }
            {
//                Topic et=lt.getTopicForSelectedLayer();
//                for(Layer l : tm.getLayers()){
                for(Layer l : tm.getLeafLayers()){
//                    if(l==tm.getSelectedLayer()) continue;
                    if(l==selectedLayer) continue;
                    Topic u=l.getTopicMap().getTopicWithBaseName(newBaseName);
                    if(u!=null && (et==null || !u.mergesWithTopic(et))) ret|=4;
                }
            }
            {
//                Topic et=lt.getTopicForSelectedLayer();
//                Collection<Topic> topics=lt.getTopicsForAllLayers();
                Collection<Topic> topics=tm.getTopicsForAllLeafLayers(lt);
                
                HashSet<T2<Topic,Topic>> links=new HashSet<T2<Topic,Topic>>();
                for(Topic i : topics){
                    Collection<Locator> isi=i.getSubjectIdentifiers();
                    Locator isl=i.getSubjectLocator();
                    String ibn=i.getBaseName();
                    if(et==i) ibn=newBaseName;
                    for(Topic j : topics){
                        if(i==j) continue;
                        Collection<Locator> jsi=j.getSubjectIdentifiers();
                        Locator jsl=j.getSubjectLocator();
                        String jbn=j.getBaseName();
                        if(et==j) jbn=newBaseName;
                        boolean merges=false;
                        if(ibn!=null && jbn!=null && ibn.equals(jbn)) merges=true;
                        else if(GripCollections.collectionsOverlap(isi,jsi)) merges=true;
                        else if(isl!=null && jsl!=null && isl.equals(jsl)) merges=true;
                        if(merges) links.add(t2(i,j));
                    }
                }
                String oldName=null;
                if(et!=null) oldName=et.getBaseName();
                if(oldName!=null){
                    for(Topic i : topics){
                        if(tm.getLeafLayer(i)==selectedLayer) continue;
                        if(i.getBaseName()!=null && i.getBaseName().equals(oldName)){
                            if(!links.contains(t2(et,i))) ret|=8;
                        }
                    }
                }
                if( (ret&8)!=0  && (ret&1)==0 ){
                    if(layerSplit(links,topics)!=0) ret|=1;
                }
            }
            return ret;
        }
        else{
            Topic u=t.getTopicMap().getTopicWithBaseName(newBaseName);
            if(u!=null && !u.mergesWithTopic(t)) return 3;
            else return 0;
        }        
    }    
    
    
    
    
    public static int checkSubjectLocatorMerge(Topic t, Locator newSubjectLocator) throws TopicMapException {
        if(newSubjectLocator==null) return 0;
        if(t instanceof LayeredTopic){
            int ret=0;
            LayeredTopic lt=(LayeredTopic)t;
            LayerStack tm=lt.getLayerStack();
            
            Layer selectedLayer=tm.getSelectedTreeLayer();
            Topic et=tm.getTopicForSelectedTreeLayer(lt);
            
            {
                LayeredTopic u=(LayeredTopic)tm.getTopicBySubjectLocator(newSubjectLocator);
                if(u!=null){
                    if(!u.mergesWithTopic(lt)) ret|=1;
                }
            }
            {
                Topic u=selectedLayer.getTopicMap().getTopicBySubjectLocator(newSubjectLocator);
                if(u!=null){
                    if(et==null || !et.mergesWithTopic(u)) ret|=2;
                }
            }
            {
                for(Layer l : tm.getLeafLayers()){
                    if(l==selectedLayer) continue;
                    Topic u=l.getTopicMap().getTopicBySubjectLocator(newSubjectLocator);
                    if(u!=null && (et==null || !u.mergesWithTopic(et))) ret|=4;
                }
            }
            {
                Collection<Topic> topics=tm.getTopicsForAllLeafLayers(lt);
                HashSet<T2<Topic,Topic>> links=new HashSet<T2<Topic,Topic>>();
                for(Topic i : topics){
                    Collection<Locator> isi=i.getSubjectIdentifiers();
                    Locator isl=i.getSubjectLocator();
                    String ibn=i.getBaseName();
                    if(et==i) isl=newSubjectLocator;
                    for(Topic j : topics){
                        if(i==j) continue;
                        Collection<Locator> jsi=j.getSubjectIdentifiers();
                        Locator jsl=j.getSubjectLocator();
                        String jbn=j.getBaseName();
                        if(et==j) jsl=newSubjectLocator;
                        boolean merges=false;
                        if(ibn!=null && jbn!=null && ibn.equals(jbn)) merges=true;
                        else if(GripCollections.collectionsOverlap(isi,jsi)) merges=true;
                        else if(isl!=null && jsl!=null && isl.equals(jsl)) merges=true;
                        if(merges) links.add(t2(i,j));
                    }
                }
                Locator oldSL=null;
                if(et!=null) oldSL=et.getSubjectLocator();
                if(oldSL!=null){
                    for(Topic i : topics){
                        if(tm.getLeafLayer(i)==selectedLayer) continue;
                        if(i.getSubjectLocator()!=null && i.getSubjectLocator().equals(oldSL)){
                            if(!links.contains(t2(et,i))) ret|=8;
                        }
                    }
                }
                if( (ret&8)!=0  && (ret&1)==0 ){
                    if(layerSplit(links,topics)!=0) ret|=1;
                }
            }
            return ret;
        }
        else{
            Topic u=t.getTopicMap().getTopicBySubjectLocator(newSubjectLocator);
            if(u!=null && !u.mergesWithTopic(t)) return 3;
            else return 0;
        }        
    }
    
    
    
    public static int checkSubjectIdentifierMerge(Topic t, Locator subjectIdentifier,boolean add) throws TopicMapException {
        if(t instanceof LayeredTopic){
            int ret=0;
            LayeredTopic lt=(LayeredTopic)t;
            LayerStack tm=lt.getLayerStack();
            Layer selectedLayer=tm.getSelectedTreeLayer();
            Topic et=tm.getTopicForSelectedTreeLayer(lt);
            
            if(add) {
                LayeredTopic u=(LayeredTopic)tm.getTopic(subjectIdentifier);
                if(u!=null){
                    if(!u.mergesWithTopic(lt)) ret|=1;
                }
            }
            if(add){
                Topic u=selectedLayer.getTopicMap().getTopic(subjectIdentifier);
                if(u!=null){
                    if(et==null || !et.mergesWithTopic(u)) ret|=2;
                }
            }
            if(add){
                for(Layer l : tm.getLeafLayers()){
                    if(l==selectedLayer) continue;
                    Topic u=l.getTopicMap().getTopic(subjectIdentifier);
                    if(u!=null && (et==null || !u.mergesWithTopic(et))) ret|=4;
                }
            }
            if(!add){
                Collection<Topic> topics=tm.getTopicsForAllLeafLayers(lt);
                HashSet<T2<Topic,Topic>> links=new HashSet<T2<Topic,Topic>>();
                for(Topic i : topics){
                    Collection<Locator> isi=i.getSubjectIdentifiers();
                    Locator isl=i.getSubjectLocator();
                    String ibn=i.getBaseName();
                    if(et==i){
                        Collection<Locator> temp=new ArrayList<Locator>();
                        temp.addAll(isi);
                        temp.remove(subjectIdentifier);
                        isi=temp;
                    }
                    for(Topic j : topics){
                        if(i==j) continue;
                        Collection<Locator> jsi=j.getSubjectIdentifiers();
                        Locator jsl=j.getSubjectLocator();
                        String jbn=j.getBaseName();
                        if(et==j) {
                            Collection<Locator> temp=new ArrayList<Locator>();
                            temp.addAll(jsi);
                            temp.remove(subjectIdentifier);
                            jsi=temp;
                        }
                        boolean merges=false;
                        if(ibn!=null && jbn!=null && ibn.equals(jbn)) merges=true;
                        else if(GripCollections.collectionsOverlap(isi,jsi)) merges=true;
                        else if(isl!=null && jsl!=null && isl.equals(jsl)) merges=true;
                        if(merges) links.add(t2(i,j));
                    }
                }
                for(Topic i : topics){
                    if(tm.getLeafLayer(i)==selectedLayer) continue;
                    if(i.getSubjectIdentifiers().contains(subjectIdentifier)){
                        if(!links.contains(t2(et,i))) ret|=8;
                    }
                }
                if( (ret&8)!=0  && (ret&1)==0 ){
                    if(layerSplit(links,topics)!=0) ret|=1;
                }
            }
            return ret;
        }
        else{
            if(add){
                Topic u=t.getTopicMap().getTopic(subjectIdentifier);
                if(u!=null && !u.mergesWithTopic(t)) return 3;
                else return 0;
            }
            else return 0;
        }        
    }
    
    /*
     * true if a split occurs
     */
    public static boolean checkTopicRemoveSplit(Topic t) throws TopicMapException {
        if(t instanceof LayeredTopic){
            LayeredTopic lt=(LayeredTopic)t;
            LayerStack tm=lt.getLayerStack();
            Topic et=lt.getTopicForSelectedLayer();
            if(et==null) return false;
            HashSet<T2<Topic,Topic>> links=new HashSet<T2<Topic,Topic>>();
            Collection<Topic> topics=lt.getTopicsForAllLayers();
            for(Topic i : topics){
                if(tm.getLayer(i)==tm.getSelectedLayer()){
                    if(et==i) continue;
                }
                for(Topic j : topics){
                    if(i==j) continue;
                    if(tm.getLayer(j)==tm.getSelectedLayer()){
                        if(et==j) continue;
                    }
                    if((i.mergesWithTopic(j))) links.add(t2(i,j));
                }
            }
            ArrayList<Topic> temp=new ArrayList<Topic>();
            temp.addAll(topics);
            temp.remove(et);
            if(layerSplit(links,temp)!=0) return true;
        }
        return false;
    }
    
    
    // -------------------------------------------------------------------------
    
    /**
     * Gets the topic with given subject identifier, if such a topic doesn't
     * exists, makes one. If a new topic is created, it will only contain the
     * subject identifier, no base name or anything else.
     */
    public static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        Topic t=tm.getTopic(si);
        if(t==null) {
            t=tm.createTopic();
            t.addSubjectIdentifier(tm.createLocator(si));
        }
        return t;
    }
    
    /**
     * Gets the topic with given subject identifier, if such a topic doesn't
     * exists, makes one and sets the given base name. If an existing topic is found
     * the name parameter is ignored. That is, the existing topic will not be changed
     * in any way.
     */
    public static Topic getOrCreateTopic(TopicMap tm, String si, String name) throws TopicMapException {
        Topic t=tm.getTopic(si);
        if(t==null) {
            t=tm.createTopic();
            t.addSubjectIdentifier(tm.createLocator(si));
            t.setBaseName(name);
        }
        return t;
    }
    
    /**
     * Gets the topic with given base name, if such a topic doesn't exists,
     * makes one. If a new topic is created, it will contain the base name and
     * a generic unique subject identifier, nothing else.
     */
    public static Topic getOrCreateTopicWithBaseName(TopicMap tm, String name) throws TopicMapException {
        Topic t=tm.getTopicWithBaseName(name);
        if(t==null) {
            t=tm.createTopic();
            t.setBaseName(name);
            t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
        }
        return t;
    }
    
    /**
     * Merges in a topic map using base names and subject locators from the source
     * topic map instead of destination topic map whenever topics merge.
     */
    public static void mergeTopicMapInOverwriting(TopicMap dest, TopicMap src) throws TopicMapException {
        Iterator iter=src.getTopics();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            Topic c=dest.copyTopicIn(t, false);
            dest.copyTopicAssociationsIn(t);
            if(t.getBaseName()!=null) c.setBaseName(t.getBaseName());
            if(t.getSubjectLocator()!=null) c.setSubjectLocator(t.getSubjectLocator());
        }
    }
    
    /**
     * Checks if topic is visible at visibility level of 1. If topic has
     * language independent hide level occurrence with number 1 or greater, it
     * is not visible.
     */
    public static boolean topicVisible(Topic t) throws TopicMapException {
        return topicVisible(t,1);
    }
    /**
     * Checks if topic is visible at given level. Topic is not visible it it has
     * occurrence with type HIDELEVEL_SI and version
     * LANGINDEPENDENT_SI and the integer it contains is
     * smaller than the given visibility level.
     */
    public static boolean topicVisible(Topic t, int level) throws TopicMapException {
        Topic hidelevel=t.getTopicMap().getTopic(HIDELEVEL_SI);
        Topic langI=t.getTopicMap().getTopic(LANGINDEPENDENT_SI);
        if(hidelevel==null || langI==null) return true;
        String hidden=t.getData(hidelevel,langI);
        if(hidden==null) return true;
        int i=Integer.parseInt(hidden);
        return i<level;
    }
    /**
     * Checks if association is visible at visibility level of 1. This is true
     * if its type, all roles and all players are visible at level 1.
     * @see #topicVisible(Topic,int)
     */
    public static boolean associationVisible(Association a) throws TopicMapException {
        return associationVisible(a,1);
    }
    /**
     * Checks if association is visible at given level. For association to be visible
     * its type, all roles and all players must be visible at that level.
     * @see #topicVisible(Topic,int)
     */
    public static boolean associationVisible(Association a, int level) throws TopicMapException {
        if(!topicVisible(a.getType(),level)) return false;
        Iterator roles=a.getRoles().iterator();
        while(roles.hasNext()){
            Topic role=(Topic)roles.next();
            if(!topicVisible(role,level)) return false;
            if(!topicVisible(a.getPlayer(role),level)) return false;
        }
        return true;
    }
    
    
    public static int countAssociationsInCategories(Topic root,Topic assocType,Topic role) throws TopicMapException {
        return countAssociationsInCategories(root,assocType,role,new HashSet()).size();
    }
    
    
    public static HashSet countAssociationsInCategories(Topic root,Topic assocType,Topic role,HashSet topics) throws TopicMapException {
        Iterator iter=root.getAssociations(assocType,role).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Iterator iter2=a.getRoles().iterator();
            while(iter2.hasNext()){
                Topic arole=(Topic)iter2.next();
                if(arole!=role) topics.add(a.getPlayer(arole));
            }
        }
        iter=getSubCategories(root).iterator();
        while(iter.hasNext()){
            countAssociationsInCategories((Topic)iter.next(),assocType,role,topics);
        }
        return topics;        
    }
    
    public static Iterator treeIterator(Topic root,String lang){
        return new TreeIterator(root,lang);
    }
    public static class TreeIterator implements Iterator {
        private Stack stack;
        private Iterator current;
        private TreeIteratorNode lastReturned;
        private Collection children;
        private String lang;
        
        public TreeIterator(Topic root,String lang){
            this.lang=lang;
            ArrayList al=new ArrayList();
            al.add(root);
            current=al.iterator();
            stack=new Stack();
        }
        
        public boolean hasNext() {
            if(current.hasNext()) return true;
            if(lastReturned.doChildren() && children.size()>0) return true;
            if(stack.isEmpty()) return false;
            return true;
        }
        
        public Object next() {
            boolean opened=false;
            boolean closed=false;
            if(lastReturned!=null && lastReturned.doChildren && children.size()>0){
                stack.push(current);
                current=children.iterator();
                opened=true;
            }
            if(!current.hasNext()){
                if(stack.isEmpty()) throw new NoSuchElementException();
                current=(Iterator)stack.pop();
                closed=true;
            }
            if(!current.hasNext()){
                lastReturned=new TreeIteratorNode(null,false,true);
                children=new ArrayList();
                return lastReturned;
            }
            Topic t=(Topic)current.next();
            try{
                children=getSubCategories(t);
            }catch(TopicMapException tme){
                tme.printStackTrace(); // TODO EXCEPTION
                children=new ArrayList();
            }
            children=sortTopics(children,lang);
            lastReturned=new TreeIteratorNode(t,opened,closed);
            return lastReturned;
        }
        
        public void remove() {
        }
        
    }
    public static class TreeIteratorNode {
        private boolean doChildren;
        private Topic topic;
        private boolean closenode;
        private boolean opennode;
        public TreeIteratorNode(Topic topic,boolean open,boolean close){
            this.topic=topic;
            doChildren=false;
            this.topic=topic;
            this.closenode=close;
            this.opennode=open;
        }
        public boolean open(){
            return opennode;
        }
        public boolean close(){
            return closenode;
        }
        public boolean doChildren(){
            return doChildren;
        }
        public void setIterateChildren(){
            doChildren=true;
        }
        public Topic getTopic(){
            return topic;
        }
    }
    
    public static Stack getCategoryPath(Topic t) throws TopicMapException {
        Stack path=new Stack();
        Topic walker=t;
        Collection parents=getSuperCategories(walker);
        while(parents.size()>0){
            path.push(walker);
            walker=(Topic)parents.iterator().next();
            parents=getSuperCategories(walker);
        }
        path.push(walker);
        return path;
    }    
    public static Collection getSubCategoriesRecursive(Topic t) throws TopicMapException {
        return _getSubCategoriesRecursive(t,new HashSet());
    }
    private static Collection _getSubCategoriesRecursive(Topic t,Set c) throws TopicMapException {
        Topic hierarchy=t.getTopicMap().getTopic(CATEGORYHIERARCHY_SI);
        Topic supercat=t.getTopicMap().getTopic(SUPERCATEGORY_SI);
        Topic subcat=t.getTopicMap().getTopic(SUBCATEGORY_SI);
        if(hierarchy==null || supercat==null || subcat==null) return new ArrayList();
        Collection as=t.getAssociations(hierarchy,supercat);
        Iterator iter=as.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic s=a.getPlayer(subcat);
            if(!c.contains(s)){
                c.add(s);
                _getSubCategoriesRecursive(s,c);
            }
        }        
        return c;
    }
    public static Collection getSubCategories(Topic t,Set c) throws TopicMapException {
        Topic hierarchy=t.getTopicMap().getTopic(CATEGORYHIERARCHY_SI);
        Topic supercat=t.getTopicMap().getTopic(SUPERCATEGORY_SI);
        Topic subcat=t.getTopicMap().getTopic(SUBCATEGORY_SI);
        if(hierarchy==null || supercat==null || subcat==null) return new ArrayList();
        Collection as=t.getAssociations(hierarchy,supercat);
        Iterator iter=as.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic s=a.getPlayer(subcat);
            c.add(s);
        }        
        return c;
    }
    public static Collection getSubCategories(Topic t) throws TopicMapException {
        return getSubCategories(t,new HashSet());
    }
    public static Collection getSuperCategories(Topic t) throws TopicMapException {
        HashSet c=new HashSet();
        Topic hierarchy=t.getTopicMap().getTopic(CATEGORYHIERARCHY_SI);
        Topic supercat=t.getTopicMap().getTopic(SUPERCATEGORY_SI);
        Topic subcat=t.getTopicMap().getTopic(SUBCATEGORY_SI);
        if(hierarchy==null || supercat==null || subcat==null) return new ArrayList();
        Collection as=t.getAssociations(hierarchy,subcat);
        Iterator iter=as.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic s=a.getPlayer(supercat);
            c.add(s);
        }        
        return c;        
    }
    
    public static String getTopicSortName(Topic topic,String lang) throws TopicMapException {
        String name=getTopicName(topic,getSortNameTopic(topic),getLangTopic(topic,lang),false);
        if(name==null || name.length() == 0) return getTopicDisplayName(topic,lang);
        else return name;
    }
    public static String getTopicDisplayName(Topic topic,String lang) throws TopicMapException {
        return getTopicName(topic,getDisplayNameTopic(topic),getLangTopic(topic,lang),true);
    }
    public static String getTopicName(Topic topic,Topic s1,Topic s2,boolean forceSomething) throws TopicMapException {
        HashSet scope=new HashSet();
        if(s1!=null) scope.add(s1);
        if(s2!=null) scope.add(s2);
        return getTopicName(topic,scope,forceSomething);
    }
    public static String getTopicName(Topic topic,Set<Topic> scope,boolean forceSomething) throws TopicMapException {
        String name=topic.getVariant(scope);
        String tempName = null;
        if(name==null && forceSomething){
            int maxcount=0;
            Set scopes=topic.getVariantScopes();
            Iterator iter=scopes.iterator();
            while(iter.hasNext()){
                Set s=(Set)iter.next();
                int count=0;
                Iterator iter2=scope.iterator();
                while(iter2.hasNext()){
                    Topic t=(Topic)iter2.next();
                    if(s.contains(t)) count++;
                }
                if(count>maxcount){
                    maxcount=count;
                    tempName = topic.getVariant(s);
                    if(tempName != null && tempName.length() != 0) {
                        name=tempName;
                    }
                }
            }
            if(name==null) name=topic.getBaseName();
            if(name==null){
                Collection sis=topic.getSubjectIdentifiers();
                if(!sis.isEmpty()) name=((Locator)sis.iterator().next()).toExternalForm();
            }
            if(name==null) name="[unnamed]";
        }
        return name;
    }
    
    public static Topic getLangTopic(Topic t,String lang) throws TopicMapException {
        return t.getTopicMap().getTopic(XTMPSI.getLang(lang));
    }
    public static Topic getDisplayNameTopic(Topic t) throws TopicMapException {
        return t.getTopicMap().getTopic(XTMPSI.DISPLAY);
    }
    public static Topic getSortNameTopic(Topic t) throws TopicMapException {
        return t.getTopicMap().getTopic(XTMPSI.SORT);
    }
    
    
    public static Collection getAssociatedTopics(Topic topic,Topic associationType,Topic topicRole,Topic associatedRole,String sortLang) throws TopicMapException {
        Iterator iter=topic.getAssociations(associationType,topicRole).iterator();
        HashSet set=new HashSet();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic p=a.getPlayer(associatedRole);
            if(p!=null) set.add(p);
        }
        if(sortLang==null) return set;
        else return sortTopics(set,sortLang);
    }
    
    
    public static boolean isTopicOfType(Topic topic,Topic type) throws TopicMapException {
        return topic.isOfType(type);
    }
    
    /**
     * Sorts an array of topics by occurrence values.
     */
    public static Collection<Topic> sortTopicsByData(Collection<Topic> topics, Topic dataType, String lang) throws TopicMapException {
        return sortTopicsByData(topics,dataType,lang,false);
    }
    /**
     * Sorts a collection of topics by occurrence values.
     */
    public static Collection<Topic> sortTopicsByData(Collection<Topic> topics, Topic dataType, String lang,boolean desc) throws TopicMapException {
        ArrayList<Topic> al=new ArrayList(topics.size());
        al.addAll(topics);
        Collections.sort(al,new DataValueComparator(dataType,lang,desc));
        return al;        
    }
    
    /**
     * Sorts an array of topics by their display names. If a proper sort name
     * is not found, other variants, base name or subject identifier is used.
     */
    public static Topic[] sortTopics(Topic[] topics,String lang) {
/*        TopicAndName[] a=new TopicAndName[topics.length];
        for(int i=0;i<topics.length;i++){
            a[i]=new TopicAndName(topics[i],lang);
        }
        quickSortTopics(a,0,a.length-1);
        Topic[] ts=new Topic[a.length];
        for(int i=0;i<a.length;i++){
            ts[i]=a[i].topic;
        }
        return ts;        */
        ArrayList<Topic> al=new ArrayList();
        al.addAll(Arrays.asList(topics));
        Collections.sort(al,new TopicNameComparator(lang));
        Topic[] ts=new Topic[topics.length];
        for(int i=0;i<ts.length;i++){
            ts[i]=al.get(i);
        }
        return ts;
    }
 
    /**
     * Sorts a collection of topics by their display names. If a proper sort name
     * is not found, other variants, base name or subject identifier is used.
     */
    public static Collection<Topic> sortTopics(Collection<Topic> topics,String lang) {
/*        TopicAndName[] a=new TopicAndName[topics.size()];
        Iterator iter=topics.iterator();
        int counter=0;
        while(iter.hasNext()){
            a[counter++]=new TopicAndName((Topic)iter.next(),lang);
        }
        quickSortTopics(a,0,a.length-1);
        ArrayList al=new ArrayList(a.length);
        for(int i=0;i<a.length;i++){
            al.add(a[i].topic);
        }
        return al;*/
        ArrayList<Topic> al=new ArrayList();
        if(topics != null) {
            try {
                al.addAll(topics);
                Collections.sort(al,new TopicNameComparator(lang));
            } catch(Exception e){
                org.wandora.piccolo.Logger.getLogger().writelog("WRN","Exception in sort topics.",e);
            }
        }
        return al;
    }
    
    public static Collection<Association> sortAssociations(Collection<Association> associations, String lang, Topic role) {
        ArrayList<Association> al=new ArrayList();
        if(associations != null) {
            al.addAll(associations);
            Collections.sort(al,new AssociationTypeComparator(lang, role));
        }
        return al;
    }
    public static Collection<Association> sortAssociations(Collection<Association> associations, String lang) {
        ArrayList<Association> al=new ArrayList();
        if(associations != null) {
            al.addAll(associations);
            Collections.sort(al,new AssociationTypeComparator(lang));
        }
        return al;
    }
    public static Collection<Association> sortAssociations(Collection<Association> associations, Collection<Topic> ignoreTopics, String lang){
        if(associations != null) {
            try {
                ArrayList<Association> al=new ArrayList(associations.size());
                al.addAll(associations);
                Collections.sort(al,new AssociationTypeComparator(lang, ignoreTopics));
                return al;   
            }
            catch (Exception e) {
                return associations;
            }
        }
        return new ArrayList();
    }
    
    
    /**
     * Associations must be sorted first for this to work!
     */
    public static Collection<Association> removeDuplicateAssociations(Collection<Association> associations, Collection<Topic> ignoreTopics) throws TopicMapException {
        Iterator<Association> iter=associations.iterator();
        Association last=null;
        if(iter.hasNext()) last=(Association)iter.next();
        else return associations;
        while(iter.hasNext()){
            Association a=iter.next();
            boolean duplicate=false;
            if(a.getType()==last.getType()){
                Iterator iter2=last.getRoles().iterator();
                duplicate=true;
                while(iter2.hasNext()){
                    Topic role=(Topic)iter2.next();
                    Topic player1=last.getPlayer(role);
                    Topic player2=a.getPlayer(role);
                    boolean ignore1=ignoreTopics.contains(player1);
                    boolean ignore2=(player2==null?false:ignoreTopics.contains(player2));
                    if(player1!=player2 && (!ignore1 || !ignore2) ){
                        duplicate=false;
                        break;
                    }
                }
            }
            if(duplicate){
                iter.remove();
            }
            else{
                last=a;
            }
        }
        return associations;
    }
    
    
    public static void clearTopicMap(TopicMap topicMap) throws TopicMapException {
        Iterator iter=topicMap.getAssociations();
        ArrayList tobeDeleted=new ArrayList();
        while(iter.hasNext()){
            tobeDeleted.add(iter.next());
        }
        iter=tobeDeleted.iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            a.remove();
        }
        
        
        // we must do several passes because some topics are in use in other topics and can't be deleted right away
        // collect all topics in a new list to avoid ConcurrentModificationException
        iter=topicMap.getTopics();
        tobeDeleted=new ArrayList();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            tobeDeleted.add(t);
        }
        iter=tobeDeleted.iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(t.isDeleteAllowed()){
                try{
                    t.remove();
                }catch(TopicInUseException e){}
            }
            else{
                Iterator iter2=new ArrayList(t.getVariantScopes()).iterator();
                while(iter2.hasNext()){
                    Set c=(Set)iter2.next();
                    t.removeVariant(c);
                }
                iter2=new ArrayList(t.getTypes()).iterator();
                while(iter2.hasNext()){
                    Topic ty=(Topic)iter2.next();
                    t.removeType(ty);
                }
                iter2=new ArrayList(t.getDataTypes()).iterator();
                while(iter2.hasNext()){
                    Topic ty=(Topic)iter2.next();
                    t.removeData(ty);
                }
            }
        }
        iter=topicMap.getTopics();
        tobeDeleted=new ArrayList();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            tobeDeleted.add(t);
        }
        iter=tobeDeleted.iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(t.isDeleteAllowed()){
                try{
                    t.remove();
                }catch(TopicInUseException e){}
            }
        }    
    }
    
/*    private static void quickSortTopics(TopicAndName[] o,int a,int b){
        if(a>=b) return;
        int pivot=(a+b)/2;
        int l=a-1,r=b;
        TopicAndName p=o[pivot];
        TopicAndName t;
        o[pivot]=o[b];
        o[b]=p;
        while(l<r){
            while(o[++l].compareTo(p)<0 && l<r) ;
            while(o[--r].compareTo(p)>0 && l<r) ;
            if(l<r){
                t=o[l];
                o[l]=o[r];
                o[r]=t;
            }
        }
        t=o[l];
        o[l]=o[b];
        o[b]=t;
        quickSortTopics(o,a,l-1);
        quickSortTopics(o,l+1,b);
    }
    */
    
    
    
    /**
     * A topic comparator that orders topics by the lexicographical ordering of
     * specified data values. Constructor needs the data type and version to use.
     */
    public static class DataValueComparator implements Comparator {
        private Topic dataType;
        private Topic lang;
        private Topic langIndep;
        private boolean desc;
        public DataValueComparator(Topic dataType,String lang,boolean desc) throws TopicMapException {
            this(dataType,lang==null?null:dataType.getTopicMap().getTopic(XTMPSI.getLang(lang)),desc);
        }
        public DataValueComparator(Topic dataType,Topic lang,boolean desc) throws TopicMapException {
            this.dataType=dataType;
            this.lang=lang;
            this.desc=desc;
            langIndep=dataType.getTopicMap().getTopic(TMBox.LANGINDEPENDENT_SI);

        }
        public int compare(Object o1, Object o2) {
            Topic t1=(Topic)o1;
            Topic t2=(Topic)o2;
            String d1=null;
            String d2=null;
            try{
                if(lang!=null){
                    d1=t1.getData(dataType,lang);
                    d2=t2.getData(dataType,lang);
                }
                if(d1==null) d1=t1.getData(dataType,langIndep);
                if(d2==null) d2=t2.getData(dataType,langIndep);
            }catch(TopicMapException tme){tme.printStackTrace();}
            int r;
            if(d1!=null && d2!=null) r=d1.compareTo(d2);
            else if(d1==null && d2!=null) r=1;
            else if(d1!=null && d2==null) r=-1;
            else r=0;
            if(desc) return -r;
            else return r;
        }

    }

    /**
     * A topic comparator that orders topics by their names. Sort names are
     * used when available, otherwise display names are 
     * used. If no suitable variant names are present base names are used. You can also
     * force the use of base names by specifying null language.
     */
    public static class TopicNameComparator implements Comparator {

        private HashMap nameCache;
        private String lang;
        private boolean desc;

        public TopicNameComparator(String lang){
            this(lang,false);
        }
        public TopicNameComparator(String lang,boolean desc){
            nameCache=new HashMap();
            this.lang=lang;
            this.desc=desc;
        }

        public String getTopicName(Topic topic) throws TopicMapException {
            String name=(String)nameCache.get(topic);
            if(name!=null) return name;
            if(lang==null) name=topic.getBaseName();
            else name=TMBox.getTopicSortName(topic,lang);
            if(name!=null) name=name.toLowerCase();
            else name="";
            nameCache.put(topic,name);
            return name;
        }

        public int compare(Object o1, Object o2) {
            Topic t1=(Topic)o1;
            Topic t2=(Topic)o2;
            String n1="";
            String n2="";
            try{
                n1=getTopicName(t1);
                n2=getTopicName(t2);
            }catch(TopicMapException tme){
                tme.printStackTrace(); // TODO EXCEPTION
            }
            int c=n1.compareTo(n2);
            if(c==0) c=(t1.hashCode()<t2.hashCode()?-1:(t1.hashCode()==t2.hashCode()?0:1));
            if(desc) return -c;
            else return c;
        }
    }
    
    
    
    public static class TopicBNAndSIComparator implements Comparator {

        private static Locator findLeastLocator(Collection<Locator> c){
            Locator least=null;
            for(Locator l : c){
                if(least==null || l.compareTo(least)<0) least=l;
            }
            return least;
        }
        
        @Override
        public int compare(Object o1, Object o2) {
            try{
                Topic t1=(Topic)o1;
                Topic t2=(Topic)o2;
                String bn1=t1.getBaseName();
                String bn2=t2.getBaseName();
                if(bn2==null && bn1!=null) return -1;
                else if(bn1==null && bn2!=null) return 1;
                else if(bn1!=null && bn2!=null){
                    int c=bn1.compareTo(bn2);
                    if(c!=0) return c;
                }
                
                Locator l1=findLeastLocator(t1.getSubjectIdentifiers());
                Locator l2=findLeastLocator(t2.getSubjectIdentifiers());
                if(l2==null && l1!=null) return -1;
                else if(l1==null && l2!=null) return 1;
                else if(l1==null && l2==null) return 0;
                else return l1.compareTo(l2);
            }
            catch(TopicMapException tme){
                tme.printStackTrace();
                return 0;
            }
        }
    }
    

    
    public static class TopicSIComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            try{
                Topic t1=(Topic)o1;
                Topic t2=(Topic)o2;

                Locator l1=t1.getFirstSubjectIdentifier();
                Locator l2=t2.getFirstSubjectIdentifier();
                if(l2==null && l1!=null) return -1;
                else if(l1==null && l2!=null) return 1;
                else if(l1==null && l2==null) return 0;
                else return l1.compareTo(l2);
            }
            catch(TopicMapException tme){
                tme.printStackTrace();
                return 0;
            }
        }
    }
    
    
    /**
     * Sorts associations by 
     * <ul>
     * <li>first comparing association types</li>
     * <li>then if ignoreTopics was specified, compare players of first roles that are not in ignoreTopics</li>
     * <li>then compare role set sizes</li>
     * <li>finally compare all roles and players, but only if the fullCompare is set to true, otherwise
     *     comparison ends at previous step, this last step is fairly computationally expensive
     *     as the role lists themselves have to be sorted first</li>
     * </ul>
     * Topic comparisons and player ordering is done with the specified topicComparator.
     */
    public static class AssociationTypeComparator implements Comparator {

        private Collection ignoreTopics;
        private Comparator topicComparator;
        private Topic compareRole;
        private boolean fullCompare=false; // full compare can be a very heavy operation, only do it if specifically asked to

        public AssociationTypeComparator(String lang){
            this(new TopicNameComparator(lang));
        }
        public AssociationTypeComparator(String lang,Collection ignoreTopics){
            this(new TopicNameComparator(lang),ignoreTopics);
        }
        public AssociationTypeComparator(Comparator topicComparator){
            this.topicComparator=topicComparator;
        }
        public AssociationTypeComparator(Comparator topicComparator, Collection ignoreTopics){
            this.topicComparator=topicComparator;
            this.ignoreTopics=ignoreTopics;
            this.compareRole=null;
        }
        public AssociationTypeComparator(String lang, Topic roleTopic){
            this(new TopicNameComparator(lang),roleTopic);
        }
        public AssociationTypeComparator(Comparator topicComparator, Topic roleTopic){
            this.topicComparator=topicComparator;
            this.ignoreTopics=null;
            this.compareRole=roleTopic;
        }
        
        public void setFullCompare(boolean full){
            this.fullCompare=full;
        }
        
        public int compare(Object o1, Object o2) {
            Association a1=(Association)o1;
            Association a2=(Association)o2;
            
            if(compareRole != null) {
                try {
                    Topic p1=a1.getPlayer(compareRole);
                    Topic p2=a2.getPlayer(compareRole);
                    if(p1 == null && p2 == null) return 0;
                    if(p1 == null && p2 != null) return -1;
                    if(p1 != null && p2 == null) return 1;
                    return topicComparator.compare(p1,p2);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
            else {
                try{
                    Topic t1=a1.getType();
                    Topic t2=a2.getType();
                    if(t1==null) return -1;
                    else if(t2==null) return 1;
                    int c=topicComparator.compare(t1,t2);
                    if(c==0){
                        if(ignoreTopics!=null){
                            Topic p1=null,p2=null;
                            Topic minRole=null;
                            Iterator iter=a1.getRoles().iterator();
                            while(iter.hasNext()){
                                Topic role=(Topic)iter.next();
                                Topic p=a1.getPlayer(role);
                                if(!ignoreTopics.contains(p) && (minRole==null || topicComparator.compare(role,minRole)<0)) {
                                    minRole=role;
                                    p1=p;
                                }
                            }
                            minRole=null;
                            iter=a2.getRoles().iterator();
                            while(iter.hasNext()){
                                Topic role=(Topic)iter.next();
                                Topic p=a2.getPlayer(role);
                                if(!ignoreTopics.contains(p) && (minRole==null || topicComparator.compare(role,minRole)<0)) {
                                    minRole=role;
                                    p2=p;
                                }
                            }
                            if(p1!=null && p2!=null){
                                c=topicComparator.compare(p1,p2);
                                if(c!=0) return c;
                            }
                        }

                        if(a1.getRoles().size()<a2.getRoles().size()) return -1;
                        else if(a1.getRoles().size()==a2.getRoles().size()) {
                            
                            if(!this.fullCompare) return 0;
                            
                            ArrayList<Topic> r1=new ArrayList<Topic>(a1.getRoles());
                            ArrayList<Topic> r2=new ArrayList<Topic>(a2.getRoles());
                            Collections.sort(r1,topicComparator);
                            Collections.sort(r2,topicComparator);
                            for(int i=0;i<r1.size();i++){
                                c=topicComparator.compare(r1.get(i),r2.get(i));
                                if(c!=0) return c;
                            }
                            
                            for(int i=0;i<r1.size();i++){
                                Topic p1=a1.getPlayer(r1.get(i));
                                Topic p2=a2.getPlayer(r2.get(i));
                                c=topicComparator.compare(p1,p2);
                                if(c!=0) return c;
                            }
                            
                            return 0;
                        }
                        else return 1;
                    }
                    else return c;
                }catch(TopicMapException tme){tme.printStackTrace();return 0;} // TODO EXCEPTION
            }
        }    
    }

    /**
     * Topic comparator that compares by comparing topics playing a specified role
     * in an association of specified type. The players of topics are compared by
     * comparing their sort or display names or base names if no language is specified
     * or no other suitable variant name is found. The first player is used for
     * ordering if there are multiple associations with suitable players (first player
     * when comparing players by their names).
     */
    public static class TopicAssociationPlayerComparator implements Comparator {
        private Topic associationType;
        private Topic playerRole;
        private HashMap<Topic,String> nameMap;
        private String lang;
        public TopicAssociationPlayerComparator(Topic associationType,Topic playerRole,String lang){
            this.associationType=associationType;
            this.playerRole=playerRole;
            this.lang=lang;
            nameMap=new HashMap<Topic,String>();
        }
        
        public String getNameFor(Topic t){
            String ret=nameMap.get(t);
            if(ret==null){
                try{
                    String min=null;
                    for(Association a : t.getAssociations(associationType)){
                        Topic p=a.getPlayer(playerRole);
                        
                        String name=null;
                        if(lang==null) name=p.getBaseName();
                        else name=TMBox.getTopicSortName(p,lang);
                        if(name!=null) name=name.toLowerCase();
                        else name="";
                        
                        if(min==null || name.compareTo(min)<0) min=name;
                    }
                    if(min==null) min="";
                    ret=min;
                }
                catch(TopicMapException tme){
                    tme.printStackTrace();
                    ret="";
                }
                nameMap.put(t,ret);
            }
            return ret;
        }
        
        public int compare(Object o1,Object o2){
            Topic t1=(Topic)o1;
            Topic t2=(Topic)o2;
            return getNameFor(t1).compareTo(getNameFor(t2));
        }
    }
    
    /**
     * Association comparator that compares associations by comparing topics of
     * the specified role. Topics are compared by comparing their sort or display
     * names or base names if language is not specified or no suitable variant
     * name is found.
     */
    public static class AssociationPlayerComparator implements Comparator {
        private Topic playerRole;
        private HashMap<Association,String> nameMap;
        private String lang;
        public AssociationPlayerComparator(Topic playerRole,String lang){
            this.playerRole=playerRole;
            this.lang=lang;
            nameMap=new HashMap<Association,String>();
        }
        
        public String getNameFor(Association a){
            String ret=nameMap.get(a);
            if(ret==null){
                try{
                    Topic p=a.getPlayer(playerRole);

                    ret=null;
                    if(p==null) ret="";
                    else{
                        if(lang==null) ret=p.getBaseName();
                        else ret=TMBox.getTopicSortName(p,lang);
                        if(ret!=null) ret=ret.toLowerCase();
                        else ret="";
                    }
                }
                catch(TopicMapException tme){
                    tme.printStackTrace();
                    ret="";
                }
                nameMap.put(a,ret);
            }
            return ret;
        }
        
        public int compare(Object o1,Object o2){
            Association a1=(Association)o1;
            Association a2=(Association)o2;
            return getNameFor(a1).compareTo(getNameFor(a2));
        }
    }
    
    
    public static class ScopeComparator implements Comparator<Set<Topic>> {

        private Comparator<Topic> topicComparator;
        
        public ScopeComparator(){
            this(new TopicNameComparator(null));
        }
        public ScopeComparator(Comparator<Topic> topicComparator){
            this.topicComparator=topicComparator;
        }
        
        @Override
        public int compare(Set<Topic> o1, Set<Topic> o2) {
            if(o1.size()<o2.size()) return -1;
            else if(o1.size()>o2.size()) return 1;

            ArrayList<Topic> s1=new ArrayList<Topic>(o1);
            ArrayList<Topic> s2=new ArrayList<Topic>(o2);
            Collections.sort(s1,topicComparator);
            Collections.sort(s2,topicComparator);

            for(int i=0;i<s1.size();i++){
                int c=topicComparator.compare(s1.get(i), s2.get(i));
                if(c!=0) return c;
            }
            return 0;
        }
        
    }
}


