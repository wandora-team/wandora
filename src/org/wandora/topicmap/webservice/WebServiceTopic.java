/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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
 */
package org.wandora.topicmap.webservice;
import java.util.*;
import org.wandora.topicmap.*;

import static org.wandora.topicmap.webservice.TopicMapServiceStub.WSAssociation;
import static org.wandora.topicmap.webservice.TopicMapServiceStub.WSOccurrence;
import static org.wandora.topicmap.webservice.TopicMapServiceStub.WSPlayer;
import static org.wandora.topicmap.webservice.TopicMapServiceStub.WSTopic;
import static org.wandora.topicmap.webservice.TopicMapServiceStub.ArrayOfString;
/**
 *
 * @author olli
 */
public class WebServiceTopic extends Topic {

    private WSTopic wstopic;
    private WebServiceTopicMap tm;

    private Collection<Association> associations;
    private Hashtable<Topic,Hashtable<Topic,String>> occurrences;

    Collection<Topic> instances;

    public WebServiceTopic(WebServiceTopicMap tm,TopicMapServiceStub.WSTopic wstopic) throws TopicMapException {
        this.tm=tm;
        this.wstopic=wstopic;
    }

    public void makeFull() throws TopicMapException{
        if(!wstopic.getFull()){
            try{
                wstopic=tm.getWebService().getTopic(wstopic.getSubjectIdentifiers()[0], true, null);
            }catch(java.rmi.RemoteException re){
                throw new TopicMapException(re);
            }
        }
    }

    @Override
    public void addSubjectIdentifier(Locator l) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void addType(Topic t) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public Collection<Association> getAssociations() throws TopicMapException {
        if(associations!=null) return associations;
        makeFull();
        associations=new ArrayList<Association>();
        WSAssociation[] wsas=wstopic.getAssociations();
        if(wsas!=null){
            HashSet<String> preload=new LinkedHashSet<String>();
            for(int i=0;i<wsas.length;i++){
                preload.add(wsas[i].getType());
                WSPlayer[] players=wsas[i].getPlayers();
                for(int j=0;j<players.length;j++){
                    preload.add(players[j].getRole());
                    preload.add(players[j].getMember());
                }
            }
            tm.getTopics(preload.toArray(new String[preload.size()]),false);
            for(int i=0;i<wsas.length;i++){
                WebServiceAssociation a=new WebServiceAssociation(tm,wsas[i]);
                associations.add(a);
            }
        }
        return associations;
    }

    @Override
    public Collection<Association> getAssociations(Topic type) throws TopicMapException {
        ArrayList<Association> ret=new ArrayList<Association>();
        for(Association a : getAssociations()){
            if(a.getType().mergesWithTopic(type)) ret.add(a);
        }
        return ret;
    }

    @Override
    public Collection<Association> getAssociations(Topic type, Topic role) throws TopicMapException {
        ArrayList<Association> ret=new ArrayList<Association>();
        for(Association a : getAssociations()){
            if(a.getType().mergesWithTopic(type)) {
                Topic t=a.getPlayer(role);
                if(t!=null && t.mergesWithTopic(this)) ret.add(a);
            }
        }
        return ret;

    }

    @Override
    public Collection<Association> getAssociationsWithRole() throws TopicMapException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Association> getAssociationsWithType() throws TopicMapException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getBaseName() throws TopicMapException {
        return wstopic.getBaseName();
    }

    @Override
    public String getData(Topic type, Topic version) throws TopicMapException {
        Hashtable<Topic,String> versions=getData(type);
        for(Topic t : versions.keySet()){
            if(t.mergesWithTopic(version)){
                return versions.get(t);
            }
        }
        return null;
    }

    private void fillOccurrences() throws TopicMapException {
        if(occurrences==null){
            makeFull();
            occurrences=new Hashtable<Topic,Hashtable<Topic,String>>();
            WSOccurrence[] os=wstopic.getOccurrences();
            if(os!=null){
                for(int i=0;i<os.length;i++){
                    Topic otype=tm.getTopic(os[i].getType());
                    Topic over=tm.getTopic(os[i].getVersion());
                    if(!occurrences.containsKey(otype)){
                        occurrences.put(otype,new Hashtable<Topic,String>());
                    }
                    Hashtable<Topic,String> versions=occurrences.get(otype);
                    versions.put(over, os[i].getContent());
                }
            }
        }
    }

    @Override
    public Hashtable<Topic, String> getData(Topic type) throws TopicMapException {
        fillOccurrences();
        for(Topic t : occurrences.keySet()){
            if(t.mergesWithTopic(type)){
                return occurrences.get(t);
            }
        }
        return new Hashtable<Topic,String>();
    }

    @Override
    public Collection<Topic> getDataTypes() throws TopicMapException {
        fillOccurrences();
        return occurrences.keySet();
    }

    @Override
    public long getDependentEditTime() throws TopicMapException {
        return 0;
    }

    @Override
    public long getEditTime() throws TopicMapException {
        return 0;
    }

    @Override
    public String getID() throws TopicMapException {
        return wstopic.getSubjectIdentifiers()[0];
    }

    @Override
    public Collection<Locator> getSubjectIdentifiers() throws TopicMapException {
        ArrayList<Locator> ret=new ArrayList<Locator>();
        String[] sis=wstopic.getSubjectIdentifiers();
        for(int i=0;i<sis.length;i++){
            ret.add(tm.createLocator(sis[i]));
        }
        return ret;
    }

    @Override
    public Locator getSubjectLocator() throws TopicMapException {
        String sl=wstopic.getSubjectLocator();
        if(sl==null) return null;
        else return tm.createLocator(sl);
    }

    @Override
    public TopicMap getTopicMap() {
        return tm;
    }

    @Override
    public Collection<Topic> getTopicsWithDataType() throws TopicMapException {
        return new ArrayList<Topic>();
    }

    @Override
    public Collection<Topic> getTopicsWithDataVersion() throws TopicMapException {
        return new ArrayList<Topic>();
    }

    @Override
    public Collection<Topic> getTopicsWithVariantScope() throws TopicMapException {
        return new ArrayList<Topic>();
    }

    
    
    @Override
    public Collection<Topic> getTypes() throws TopicMapException {
        String[] ts=wstopic.getTypes();
        ArrayList<Topic> ret=new ArrayList<Topic>();
        if(ts!=null){
            for(int i=0;i<ts.length;i++){
                ret.add(tm.getTopic(ts[i]));
            }
        }
        return ret;
    }

    private boolean collectionContains(Collection<? extends Topic> col,Topic t) throws TopicMapException{
        for(Topic c : col){
            if(c.mergesWithTopic(t)) return true;
        }
        return false;
    }

    @Override
    public String getVariant(Set<Topic> scope) throws TopicMapException {
        makeFull();
        if(scope.size()!=2) return null;
        HashSet<Set<Topic>> ret=new LinkedHashSet<Set<Topic>>();
        String[] types=wstopic.getVariantTypes();
        if(types!=null){
            for(int i=0;i<types.length;i++){
                Topic typeT=tm.getTopic(types[i]);
                if(!collectionContains(scope,typeT)) continue;
                String[] langs=wstopic.getVariantLanguages()[i].getArray();
                for(int j=0;j<langs.length;j++){
                    Topic langT=tm.getTopic(langs[j]);
                    if(collectionContains(scope,langT)){
                        return wstopic.getVariantNames()[i].getArray()[j];
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Set<Set<Topic>> getVariantScopes() throws TopicMapException {
        makeFull();
        HashSet<Set<Topic>> ret=new LinkedHashSet<Set<Topic>>();
        String[] types=wstopic.getVariantTypes();
        if(types!=null){
            for(int i=0;i<types.length;i++){
                Topic typeT=tm.getTopic(types[i]);
                String[] langs=wstopic.getVariantLanguages()[i].getArray();
                for(int j=0;j<langs.length;j++){
                    HashSet<Topic> scope=new LinkedHashSet<Topic>();
                    scope.add(typeT);
                    scope.add(tm.getTopic(langs[j]));
                }
            }
        }
        return ret;
    }

    @Override
    public boolean isDeleteAllowed() throws TopicMapException {
        return false;
    }

    @Override
    public boolean isOfType(Topic t) throws TopicMapException {
        for(Topic type : getTypes()){
            if(type.mergesWithTopic(t)) return true;
        }
        return false;
    }

    @Override
    public boolean isRemoved() throws TopicMapException {
        return false;
    }

    @Override
    public void remove() throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeData(Topic type, Topic version) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeData(Topic type) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeSubjectIdentifier(Locator l) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeType(Topic t) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void removeVariant(Set<Topic> scope) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setBaseName(String name) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setData(Topic type, Hashtable<Topic, String> versionData) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setData(Topic type, Topic version, String value) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setDependentEditTime(long time) throws TopicMapException {
    }

    @Override
    public void setEditTime(long time) throws TopicMapException {
    }

    @Override
    public void setSubjectLocator(Locator l) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

    @Override
    public void setVariant(Set<Topic> scope, String name) throws TopicMapException {
        throw new TopicMapReadOnlyException();
    }

}
