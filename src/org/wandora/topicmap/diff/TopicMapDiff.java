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

package org.wandora.topicmap.diff;
import org.wandora.topicmap.packageio.ZipPackageInput;
import org.wandora.topicmap.packageio.PackageInput;
import org.wandora.topicmap.*;
import org.wandora.topicmap.memory.*;
import org.wandora.topicmap.layered.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author olli
 */
public class TopicMapDiff {

    public TopicMapDiff(){
        
    }
    
    public ArrayList<TopicDiffEntry> compareTopics(Topic t1,Topic t2) throws TopicMapException {
        ArrayList<TopicDiffEntry> ret=new ArrayList<TopicDiffEntry>();
        
        TopicMap tm1=t1.getTopicMap();
        TopicMap tm2=t2.getTopicMap();
        
        String bn1=t1.getBaseName();
        String bn2=t2.getBaseName();
        if(bn1==null && bn2!=null) ret.add(new BNChanged(bn2,bn1));
        else if(bn1!=null && bn2==null) ret.add(new BNChanged(bn2,bn1));
        else if(bn1==null && bn2==null) {}
        else if(! bn1.equals(bn2)) ret.add(new BNChanged(bn2,bn1));
        
        Locator sl1=t1.getSubjectLocator();
        Locator sl2=t2.getSubjectLocator();
        if(sl1==null && sl2!=null) ret.add(new SLChanged(sl2,sl1));
        else if(sl1!=null && sl2==null) ret.add(new SLChanged(sl2,sl1));
        else if(sl1==null && sl2==null) {}
        else if(! sl1.equals(sl2)) ret.add(new SLChanged(sl2,sl1));
        
        Collection<Locator> sis1=t1.getSubjectIdentifiers();
        Collection<Locator> sis2=t2.getSubjectIdentifiers();
        
        ArrayList<Locator> ls=new ArrayList<Locator>(sis2);
        ls.removeAll(sis1);
        for(Locator l : ls){
            ret.add(new SIAdded(l));
        }
        ls=new ArrayList<Locator>(sis1);
        ls.removeAll(sis2);
        for(Locator l : ls){
            ret.add(new SIDeleted(l));
        }
        
        for(Topic type1 : t1.getTypes()){
            Topic m2=getSingleMerging(tm2, type1);
            if(m2==null) ret.add(new TypeDeleted(type1));
            else{
                Topic m1=getSingleMerging(tm1,m2);
                if(m1==null || !t2.isOfType(m2)) ret.add(new TypeDeleted(type1));
            }
        }
        for(Topic type2 : t2.getTypes()){
            Topic m1=getSingleMerging(tm1, type2);
            if(m1==null) ret.add(new TypeAdded(type2));
            else{
                Topic m2=getSingleMerging(tm2,m1);
                if(m2==null || !t1.isOfType(m1)) ret.add(new TypeAdded(type2));
            }
        }
        
        Set<Set<Topic>> scopes1=t1.getVariantScopes();
        for(Set<Topic> s1 : scopes1){
            String v1=t1.getVariant(s1);
            Set<Topic> s2=getScope(s1,tm2);
            if(s2==null) ret.add(new VariantChanged(s1,null,v1));
            else{
                String v2=t2.getVariant(s2);
                if(v2==null || !v2.equals(v1)) ret.add(new VariantChanged(s2,v2,v1));
            }
        }
        Set<Set<Topic>> scopes2=t2.getVariantScopes();
        for(Set<Topic> s2 : scopes2){
            String v2=t2.getVariant(s2);
            Set<Topic> s1=getScope(s2,tm1);
            if(s1==null) ret.add(new VariantChanged(s2,v2,null));
            else{
                String v1=t1.getVariant(s1);
                if(v1==null) ret.add(new VariantChanged(s2,v2,null));
                // !v1.equals(v2) should be covered by previous loop
            }
        }
        
        return ret;
    }
    
    public ArrayList<TopicDiffEntry> newTopic(Topic t) throws TopicMapException {
        ArrayList<TopicDiffEntry> ret=new ArrayList<TopicDiffEntry>();
        
        for(Locator l : t.getSubjectIdentifiers()){
            ret.add(new SIAdded(l));
        }
        Locator sl=t.getSubjectLocator();
        if(sl!=null) ret.add(new SLChanged(sl,null));
        String bn=t.getBaseName();
        if(bn!=null) ret.add(new BNChanged(bn,null));
        
        for(Topic type : t.getTypes()){
            ret.add(new TypeAdded(type));
        }
        
        Set<Set<Topic>> scopes=t.getVariantScopes();
        for(Set<Topic> s : scopes){
            String variant=t.getVariant(s);
            ret.add(new VariantChanged(s,variant,null));
        }        
        
        return ret;
    }
    
    public ArrayList<TopicDiffEntry> deletedTopic(Topic t) throws TopicMapException {
        ArrayList<TopicDiffEntry> ret=new ArrayList<TopicDiffEntry>();
        
        for(Locator l : t.getSubjectIdentifiers()){
            ret.add(new SIDeleted(l));
        }
        Locator sl=t.getSubjectLocator();
        if(sl!=null) ret.add(new SLChanged(null,sl));
        String bn=t.getBaseName();
        if(bn!=null) ret.add(new BNChanged(null,bn));
        
        for(Topic type : t.getTypes()){
            ret.add(new TypeDeleted(type));
        }
        
        Set<Set<Topic>> scopes=t.getVariantScopes();
        for(Set<Topic> s : scopes){
            String v=t.getVariant(s);
            ret.add(new VariantChanged(s,null,v));
        }        
        
        return ret;
    }
    
    public Set<Topic> getScope(Set<Topic> scope,TopicMap tm) throws TopicMapException {
        HashSet<Topic> ret=new HashSet<Topic>();
        
        for(Topic t : scope){
            Collection<Topic> merging=tm.getMergingTopics(t);
            if(merging.size()!=1) return null;
            Topic m1=merging.iterator().next();
            Collection<Topic> merging2=t.getTopicMap().getMergingTopics(m1);
            if(merging2.size()!=1) return null;
            ret.add(m1);
        }
        
        return ret;
    }
    
    public boolean makeDiff(TopicMap tm1,TopicMap tm2,DiffOutput output) throws TopicMapException {
        output.startCompare();
        boolean cont=true;
        Iterator<Topic> topics1=tm1.getTopics();
        while(topics1.hasNext() && cont){
            Topic t1=topics1.next();
            Topic m2=getSingleMerging(tm2,t1);
            if(m2!=null){
                Topic m1=getSingleMerging(tm1,m2);
                if(m1!=null){
                    ArrayList<TopicDiffEntry> tDiff=compareTopics(t1, m2);
                    if(tDiff.size()>0) cont=output.outputDiffEntry(new TopicChanged(t1,tDiff));
                    else output.noDifferences(t1);
                }
                else cont=output.outputDiffEntry(new TopicDeleted(t1,deletedTopic(t1)));
            }
            else cont=output.outputDiffEntry(new TopicDeleted(t1,deletedTopic(t1)));
        }
        if(!cont) return cont;
        
        Iterator<Topic> topics2=tm2.getTopics();
        while(topics2.hasNext() && cont){
            Topic t2=topics2.next();
            Topic m1=getSingleMerging(tm1,t2);
            if(m1!=null){
                Topic m2=getSingleMerging(tm2,m1);
                if(m2==null) cont=output.outputDiffEntry(new TopicAdded(newTopic(t2)));
                else output.noDifferences(t2);
            }
            else cont=output.outputDiffEntry(new TopicAdded(newTopic(t2)));
        }
        if(!cont) return cont;
        
        Iterator<Association> associations1=tm1.getAssociations();
        while(associations1.hasNext() && cont){
            Association a1=associations1.next();
            if(!findAssociation(tm2,a1)) cont=output.outputDiffEntry(new AssociationDeleted(a1));
            else output.noDifferences(a1);
        }
        if(!cont) return cont;
        
        Iterator<Association> associations2=tm2.getAssociations();
        while(associations2.hasNext() && cont){
            Association a2=associations2.next();
            if(!findAssociation(tm1,a2)) cont=output.outputDiffEntry(new AssociationAdded(a2));
            else output.noDifferences(a2);
        }
        
        output.endCompare();
        return cont;
    }
    
    public boolean findAssociation(TopicMap tm2,Association a1) throws TopicMapException {
        TopicMap tm1=a1.getType().getTopicMap();
        Topic type2=getSingleMerging(tm2,a1.getType());
        if(type2==null || getSingleMerging(tm1,type2)==null) return false;
        HashMap<Topic,Topic> players2=new HashMap<Topic,Topic>();
        for(Topic role1 : a1.getRoles()){
            Topic role2=getSingleMerging(tm2,role1);
            Topic player2=getSingleMerging(tm2,a1.getPlayer(role1));
            if(role2==null || getSingleMerging(tm1,role2)==null ||
               player2==null || getSingleMerging(tm1,player2)==null ){
                return false;
            }
            players2.put(role2,player2);
        }
        ArrayList<Association> as2=null;
        for(Map.Entry<Topic,Topic> e : players2.entrySet()){
            if(as2==null){
                as2=new ArrayList<Association>(e.getValue().getAssociations(type2, e.getKey()));
                ArrayList<Association> next=new ArrayList<Association>();
                for(Association test : as2){
                    if(test.getRoles().size()==a1.getRoles().size()) next.add(test);
                }
                as2=next;
            }
            else{
                ArrayList<Association> next=new ArrayList<Association>();
                for(Association test : as2){
                    Topic testPlayer=test.getPlayer(e.getKey());
                    if(testPlayer!=null && testPlayer.mergesWithTopic(e.getValue()))
                        next.add(test);
                }
                as2=next;
            }
            if(as2.size()==0) break;
        }
        
        if(as2!=null && as2.size()==1) return true;
        return false;
    }
    
    public Topic getSingleMerging(TopicMap tm,Topic t) throws TopicMapException {
        Topic ret=null;
        for(Locator l : t.getSubjectIdentifiers()){
            Topic found=tm.getTopic(l);
            if(found!=null){
                if(ret!=null) {
                    if(!ret.mergesWithTopic(found)) return null;
                }
                else ret=found;
            }
        }

        Locator sl=t.getSubjectLocator();
        if(sl!=null){
            Topic found=tm.getTopicBySubjectLocator(sl);
            if(found!=null){
                if(ret!=null) {
                    if(!ret.mergesWithTopic(found)) return null;
                }
                else ret=found;
            }
        }

        String bn=t.getBaseName();
        if(bn!=null){
            Topic found=tm.getTopicWithBaseName(bn);
            if(found!=null){
                if(ret!=null) {
                    if(!ret.mergesWithTopic(found)) return null;
                }
                else ret=found;
            }
        }
        
        return ret;
    }
    
    public Topic getTopic(Object identifier,TopicMap tm) throws TopicMapException {
        if(identifier instanceof Locator) return tm.getTopic((Locator)identifier);
        else return tm.getTopicWithBaseName(identifier.toString());
    }
    
    public Association getAssociation(Object[] a,TopicMap tm) throws TopicMapException {
        Topic t=getTopic(a[0],tm);
        if(t==null) return null;
        ArrayList<Association> as2=null;
        for(int i=1;i+1<a.length;i+=2){
            Topic role=getTopic(a[i],tm);
            Topic player=getTopic(a[i+1],tm);
            if(role==null || player==null) return null;
            
            if(as2==null){
                as2=new ArrayList<Association>(player.getAssociations(t, role));
            }
            else{
                ArrayList<Association> next=new ArrayList<Association>();
                for(Association test : as2){
                    Topic testPlayer=test.getPlayer(role);
                    if(testPlayer!=null && testPlayer.mergesWithTopic(player))
                        next.add(test);
                }
                as2=next;
            }
            if(as2.size()==0) return null;
        }
        if(as2.size()==1) {
            Association a2=as2.get(0);
            if(a2.getRoles().size()==(a.length-1)/2) return a2;
        }
        return null;
    }
    
    public Object getTopicIdentifier(ArrayList<TopicDiffEntry> diff){
        Locator si=null;
        for(TopicDiffEntry d : diff){
            if(d instanceof SIAdded){
                si=((SIAdded)d).si;
            }
            else if(d instanceof BNChanged){
                return ((BNChanged)d).bn;
            }
        }
        return si;
    }
    
    public boolean applyTopicDiff(ArrayList<TopicDiffEntry> diff,Topic t,TopicMap tm,int phase,PatchExceptionHandler eHandler) throws TopicMapException {
        Diff: for(TopicDiffEntry d : diff){
            if(d instanceof SIAdded){
                if(phase==0 || phase==3){
                    SIAdded d2=(SIAdded)d;
                    if(t.getSubjectIdentifiers().contains(d2.si)){
                        if(!eHandler.handleException(new PatchException(PatchException.MINOR,"Topic already contains added subject identifier "+d2.si,t)))
                            return false;
                    }
                    else if(tm.getTopic(d2.si)!=null) {
                        if(!eHandler.handleException(new PatchException(PatchException.MODERATE,"Adding subject identifier "+d2.si+" to a topic but topic map already contains a topic with that idetnifier.",t)))
                            return false;
                    }
                    t.addSubjectIdentifier(d2.si);
                }
            }
            else if(d instanceof SIDeleted){
                if(phase==0 || phase==3){
                    SIDeleted d2=(SIDeleted)d;
                    if(t.getSubjectIdentifiers().contains(d2.si)){
                        if(!eHandler.handleException(new PatchException(PatchException.MINOR,"Topic does not contain removed subject identifier "+d2.si,t)))
                            return false;
                    }
                    t.removeSubjectIdentifier(d2.si);
                }                
            }
            else if(d instanceof SLChanged){
                if(phase==0 || phase==4){
                    SLChanged d2=(SLChanged)d;
                    Locator l=t.getSubjectLocator();
                    boolean nochange=false;
                    if( (l==null && d2.oldsl!=null) || (l!=null && d2.oldsl==null) || 
                        (l!=null && d2.oldsl!=null && !l.equals(d2.oldsl)) ) {
                        if( (l!=null && d2.sl!=null && l.equals(d2.sl)) || (l==null && d2.sl==null)) {
                            if(!eHandler.handleException(new PatchException(PatchException.MINOR,"Changing subject locator to "+d2.sl+". Old subject locator is "+l+" expected "+d2.oldsl,t)))
                                return false;
                            nochange=true;
                        }
                        else if(!eHandler.handleException(new PatchException(PatchException.MODERATE,"Changing subject locator to "+d2.sl+". Old subject locator is "+l+" expected "+d2.oldsl,t)))
                                return false;
                    }
                    if(!nochange && d2.sl!=null && tm.getTopicBySubjectLocator(d2.sl)!=null)
                        if(!eHandler.handleException(new PatchException(PatchException.MODERATE,"Adding subject locator "+d2.sl+" to a topic but topic map already contains a topic with that locator.",t)))
                            return false;
                    t.setSubjectLocator(d2.sl);
                }                
            }
            else if(d instanceof BNChanged){
                if(phase==0 || phase==3){
                    BNChanged d2=(BNChanged)d;
                    String bn=t.getBaseName();
                    boolean nochange=false;
                    if( (bn==null && d2.oldbn!=null) || (bn!=null && d2.oldbn==null) || 
                        (bn!=null && d2.oldbn!=null && !bn.equals(d2.oldbn)) ) {
                        if( (bn!=null && d2.bn!=null && bn.equals(d2.bn)) || (bn==null && d2.bn==null)) {
                            if(!eHandler.handleException(new PatchException(PatchException.MINOR,"Changing base name to "+d2.bn+". Old base name is "+bn+" expected "+d2.oldbn,t)))
                                return false;
                            nochange=true;
                        }
                        else if(!eHandler.handleException(new PatchException(PatchException.MODERATE,"Changing base name to "+d2.bn+". Old base name is "+bn+" expected "+d2.oldbn,t)))
                                return false;
                    }
                    if(!nochange && d2.bn!=null && tm.getTopicWithBaseName(d2.bn)!=null)
                        if(!eHandler.handleException(new PatchException(PatchException.MODERATE,"Adding base name "+d2.bn+" to a topic but topic map already contains a topic with that base name.",t)))
                            return false;
                    t.setBaseName(d2.bn);
                }                
            }
            else if(d instanceof VariantChanged){
                if(phase==0 || phase==1 || phase==4){
                    VariantChanged d2=(VariantChanged)d;
                    if(phase==1 && d2.name!=null) continue;
                    else if(phase==4 && d2.name==null) continue;
                    if(d2.scope==null){
                        d2.scope=new HashSet<Topic>();
                        for(Object o : d2.scope2){
                            Topic s=getTopic(o,tm);
                            if(s==null){
                                if(!eHandler.handleException(new PatchException(PatchException.SEVERE,"Changing variant to "+d2.name+". Could not resolve scope topic "+o,t)))
                                    return false;
                                continue Diff;
                            }
                            d2.scope.add(s);
                        }                        
                    }
                    String v=t.getVariant(d2.scope);
                    if( (v==null && d2.oldname!=null) || (v!=null && d2.oldname==null) || 
                        (v!=null && d2.oldname!=null && !v.equals(d2.oldname)) ) {
                        if( (v!=null && d2.name!=null && v.equals(d2.name)) || (v==null && d2.name==null)){
                            if(!eHandler.handleException(new PatchException(PatchException.MINOR,"Changing variant to "+d2.name+". Old variant is "+v+" expected "+d2.oldname,t)))
                                return false;
                        }
                        else if(!eHandler.handleException(new PatchException(PatchException.MODERATE,"Changing variant to "+d2.name+". Old variant is "+v+" expected "+d2.oldname,t)))
                                return false;
                    }
                    
                    if(d2.name==null) t.removeVariant(d2.scope);
                    else t.setVariant(d2.scope, d2.name);
                }
            }
            else if(d instanceof TypeAdded){
                if(phase==0 || phase==4){
                    TypeAdded d2=(TypeAdded)d;
                    if(d2.t==null) d2.t=getTopic(d2.t2,tm);
                    if(d2.t==null){
                        if(!eHandler.handleException(new PatchException(PatchException.MODERATE,"Could not resolve added type topic "+d2.t2,t)))
                            return false;
                    }
                    else {
                        if(t.isOfType(d2.t))
                            if(!eHandler.handleException(new PatchException(PatchException.MINOR,"Topic is already of the added type "+d2.t2,t)))
                                return false;
                        t.addType(d2.t);                    
                    }
                }                
            }
            else if(d instanceof TypeDeleted){
                if(phase==0 || phase==1){
                    TypeDeleted d2=(TypeDeleted)d;
                    if(d2.t==null) d2.t=getTopic(d2.t2,tm);
                    if(d2.t==null){
                        if(!eHandler.handleException(new PatchException(PatchException.MODERATE,"Could not resolve removed type topic "+d2.t2,t)))
                            return false;
                    }
                    else {
                        if(!t.isOfType(d2.t))
                            if(!eHandler.handleException(new PatchException(PatchException.MINOR,"Topic is not of the removed type "+d2.t2,t)))
                                return false;
                        t.removeType(d2.t);
                    }
                }                
            }
        }
        return true;
    }
    
    public ArrayList<PatchException> applyDiff(ArrayList<DiffEntry> diff,TopicMap tm) throws TopicMapException {
        final ArrayList<PatchException> ret=new ArrayList<PatchException>();
        applyDiff(diff,tm,new PatchExceptionHandler(){
            public boolean handleException(PatchException e){
                ret.add(e);
                return true;
            }
        });
        return ret;
    }
    
    public void applyDiff(ArrayList<DiffEntry> diff,TopicMap tm,PatchExceptionHandler eHandler) throws TopicMapException {
        /* Phases:
         *  1 remove associations and variants and types in topics so we can then safely remove topics
         *  2 remove topics so we can then safely apply other changes
         *  3 create new topics, change old topics, but do only base names and subject identifiers
         *  4 all needed topics are in now, do everything else
         */
        for(int phase=1;phase<=4;phase++){
            Diff: for(DiffEntry d : diff){
                if(d instanceof TopicChanged){
                    if(phase==1 || phase==3 || phase==4){
                        TopicChanged d2=(TopicChanged)d;
                        if(d2.topic==null) d2.topic=getTopic(d2.topic2,tm);
                        if(d2.topic==null){
                            if(eHandler.handleException(new PatchException(PatchException.SEVERE,"Could not resolve changed topic "+d2.topic2)))
                                    return;
                        }
                        else if(!applyTopicDiff(d2.diff,d2.topic,tm,phase,eHandler)) return;
                    }
                }
                else if(d instanceof TopicAdded){
                    TopicAdded d2=(TopicAdded)d;
                    if(phase==3){
                        Topic t=tm.createTopic();
                        if(!applyTopicDiff(d2.diff,t,tm,phase,eHandler)) return;
                    }
                    else if(phase==4){
                        Object o=getTopicIdentifier(d2.diff);
                        Topic t=getTopic(o,tm);
                        if(t==null) {
                            // this is critical since the topic should be there as it has been added earlier
                            // in other words, something strange is going on if this happens
                            if(eHandler.handleException(new PatchException(PatchException.CRITICAL,"Could not resolve added topic "+o)))
                                return;
                        }
                        else if(!applyTopicDiff(d2.diff,t,tm,phase,eHandler)) return;
                    }
                }
                else if(d instanceof TopicDeleted){
                    if(phase==1 || phase==2){
                        TopicDeleted d2=(TopicDeleted)d;
                        if(d2.topic==null) d2.topic=getTopic(d2.topic2,tm);
                        if(d2.topic==null) {
                            if(phase==1) 
                                if(eHandler.handleException(new PatchException(PatchException.MINOR,"Could not find deleted topic "+d2.topic2)))
                                    return;
                        }
                        else{
                            if(phase==1) {
                                if(!applyTopicDiff(d2.diff,d2.topic,tm,phase,eHandler)) return;
                            }
                            else {
                                try{
                                    d2.topic.remove();
                                }catch(TopicInUseException tiue){
                                    if(eHandler.handleException(new PatchException(PatchException.SEVERE,"Could not delete topic "+d2.topic2+". Topic is in use as a type or variant scope.")))
                                        return;
                                }
                            }
                        }
                    }
                }
                else if(d instanceof AssociationAdded){
                    if(phase==4){
                        AssociationAdded d2=(AssociationAdded)d;
                        if(d2.a==null){
                            d2.a=new Topic[d2.a2.length];
                            for(int i=0;i<d2.a2.length;i++){
                                d2.a[i]=getTopic(d2.a2[i],tm);
                                if(d2.a[i]==null){
                                    String type="player";
                                    if(i==0) type="type";
                                    else if((i%2)==1) type="role";
                                    if(eHandler.handleException(new PatchException(PatchException.SEVERE,"Could not resolve association "+type+" topic "+d2.a2[i])))
                                        return;
                                    continue Diff;
                                }
                            }
                        }
                        if(getAssociation(d2.a2,tm)!=null){
                            if(eHandler.handleException(new PatchException(PatchException.MINOR,"Added association already exists.")))
                                return;
                        }
                        else{
                            Association a=tm.createAssociation(d2.a[0]);
                            for(int i=1;i+1<d2.a.length;i+=2){
                                a.addPlayer(d2.a[i+1], d2.a[i]);
                            }
                        }
                    }
                }
                else if(d instanceof AssociationDeleted){
                    if(phase==1){
                        AssociationDeleted d2=(AssociationDeleted)d;
                        if(d2.a==null) d2.a=getAssociation(d2.a2,tm);
                        if(d2.a==null){
                            if(eHandler.handleException(new PatchException(PatchException.MINOR,"Could not find deleted association.")))
                                return;
                        }
                        else d2.a.remove();
                    }
                }            
            }
        }
    }
    
    public Object getTopicIdentifier(Topic t) throws TopicMapException {
        String bn=t.getBaseName();
        if(bn!=null) return bn;
        else return t.getOneSubjectIdentifier();
    }
    
    public ArrayList<TopicDiffEntry> makeInverseTopicDiff(ArrayList<TopicDiffEntry> diff) throws TopicMapException {
        ArrayList<TopicDiffEntry> inverse=new ArrayList<TopicDiffEntry>();
        for(TopicDiffEntry d : diff){
            if(d instanceof SIAdded){
                SIAdded d2=(SIAdded)d;
                inverse.add(new SIDeleted(d2.si));
            }
            else if(d instanceof SIDeleted){
                SIDeleted d2=(SIDeleted)d;
                inverse.add(new SIAdded(d2.si));
            }
            else if(d instanceof SLChanged){
                SLChanged d2=(SLChanged)d;
                inverse.add(new SLChanged(d2.oldsl,d2.sl));
            }
            else if(d instanceof BNChanged){
                BNChanged d2=(BNChanged)d;
                inverse.add(new BNChanged(d2.oldbn,d2.bn));
            }
            else if(d instanceof VariantChanged){
                VariantChanged d2=(VariantChanged)d;
                Set<Object> scope=d2.scope2;
                if(scope==null){
                    scope=new HashSet<Object>();
                    for(Topic t : d2.scope){
                        scope.add(getTopicIdentifier(t));
                    }
                }
                inverse.add(new VariantChanged(scope,d2.oldname,d2.name,true));
            }
            else if(d instanceof TypeAdded){
                TypeAdded d2=(TypeAdded)d;
                if(d2.t2!=null) inverse.add(new TypeDeleted(d2.t2));
                else inverse.add(new TypeDeleted(getTopicIdentifier(d2.t)));
            }
            else if(d instanceof TypeDeleted){
                TypeDeleted d2=(TypeDeleted)d;
                if(d2.t2!=null) inverse.add(new TypeAdded(d2.t2));
                else inverse.add(new TypeAdded(getTopicIdentifier(d2.t)));
            }
        }
        
        return inverse;
    }
    
    public ArrayList<DiffEntry> makeInverse(ArrayList<DiffEntry> diff) throws TopicMapException {
        ArrayList<DiffEntry> inverse=new ArrayList<DiffEntry>();
        for(DiffEntry d : diff){
            if(d instanceof TopicChanged){
                TopicChanged d2=(TopicChanged)d;
                if(d2.topic2!=null) inverse.add(new TopicChanged(d2.topic2,makeInverseTopicDiff(d2.diff)));
                else inverse.add(new TopicChanged(getTopicIdentifier(d2.topic),makeInverseTopicDiff(d2.diff)));
            }
            else if(d instanceof TopicAdded){
                TopicAdded d2=(TopicAdded)d;
                inverse.add(new TopicDeleted(getTopicIdentifier(d2.diff),makeInverseTopicDiff(d2.diff)));
            }
            else if(d instanceof TopicDeleted){
                TopicDeleted d2=(TopicDeleted)d;
                inverse.add(new TopicAdded(makeInverseTopicDiff(d2.diff)));
            }
            else if(d instanceof AssociationAdded){
                AssociationAdded d2=(AssociationAdded)d;
                if(d2.a2!=null) inverse.add(new AssociationDeleted(d2.a2));
                else inverse.add(new AssociationDeleted(d2.a));
            }
            else if(d instanceof AssociationDeleted){
                AssociationDeleted d2=(AssociationDeleted)d;
                if(d2.a2!=null) inverse.add(new AssociationAdded(d2.a2));
                else inverse.add(new AssociationAdded(d2.a));
            }                        
        }
        return inverse;
    }
    
    public ArrayList<PatchException> tryTopicDiff(ArrayList<TopicDiffEntry> diff,Topic t,TopicMap tm,int phase,HashSet<Object> addedTopics,HashSet<Object> deletedTopics) throws TopicMapException {
        ArrayList<PatchException> ret=new ArrayList<PatchException>();
        for(TopicDiffEntry d : diff){
            if(d instanceof SIAdded){
                if(phase==0 || phase==3){
                    SIAdded d2=(SIAdded)d;
                    Object o=tryGetTopic(d2.si, tm, addedTopics, deletedTopics);
                    if(o!=null){
                        ret.add(new PatchException(PatchException.SEVERE,
                                "Adding subject identifier "+d2.si+" would cause a topic merge.",t));
                    }
                    if(t.getSubjectIdentifiers().contains(d2.si)){
                        ret.add(new PatchException(PatchException.MINOR,
                                "Topic already contains subject identifier "+d2.si,t));
                    }
                    addedTopics.add(d2.si);
                }
            }
            else if(d instanceof SIDeleted){
                if(phase==0 || phase==3){
                    SIDeleted d2=(SIDeleted)d;
                    if(!t.getSubjectIdentifiers().contains(d2.si)){
                        ret.add(new PatchException(PatchException.MINOR,
                                "Topic does not contains deleted subject identifier "+d2.si,t));                        
                    }
                    deletedTopics.add(d2.si);
                }                
            }
            else if(d instanceof SLChanged){
                if(phase==0 || phase==4){
                    SLChanged d2=(SLChanged)d;
                    Topic o=tm.getTopicBySubjectLocator(d2.sl);
                    if(o!=null){
                        ret.add(new PatchException(PatchException.SEVERE,
                                "Setting subject locator "+d2.sl+" would cause a topic merge.",t));                        
                    }
                    if( (d2.oldsl==null && t.getSubjectLocator()!=null) ||
                        (d2.oldsl!=null && (t.getSubjectLocator()==null || !d2.oldsl.equals(t.getSubjectLocator())) ) ) {
                        ret.add(new PatchException(PatchException.MODERATE,
                                "Old subject locator is "+t.getSubjectLocator()+", expected "+d2.oldsl,t));
                    }
                }                
            }
            else if(d instanceof BNChanged){
                if(phase==0 || phase==3){
                    BNChanged d2=(BNChanged)d;
                    Topic o=tm.getTopicWithBaseName(d2.bn);
                    if(o!=null){
                        ret.add(new PatchException(PatchException.SEVERE,
                                "Setting base name "+d2.bn+" would cause a topic merge.",t));
                    }
                    if( (d2.oldbn==null && t.getBaseName()!=null) ||
                        (d2.oldbn!=null && (t.getBaseName()==null || !d2.oldbn.equals(t.getBaseName())) ) ) {
                        ret.add(new PatchException(PatchException.MODERATE,
                                "Old base name is "+t.getBaseName()+", expected "+d2.oldbn,t));
                    }
                    if(d2.bn!=null) addedTopics.add(d2.bn);
                    if(d2.oldbn!=null) deletedTopics.add(d2.oldbn);
                }                
            }
            else if(d instanceof VariantChanged){
                if(phase==0 || phase==1 || phase==4){
                    VariantChanged d2=(VariantChanged)d;
                    if(phase==1 && d2.name!=null) continue;
                    else if(phase==4 && d2.name==null) continue;
                    HashSet<Topic> scope=new HashSet<Topic>();
                    for(Object o : d2.scope2){
                        Object u=tryGetTopic(o,tm,addedTopics,deletedTopics);
                        if(u==null){
                            ret.add(new PatchException(PatchException.MODERATE,
                                "Could not resolve variant scope.",t));                            
                        }
                        if(u!=null && scope!=null && u instanceof Topic) {
                            scope.add((Topic)u);
                        }
                        else scope=null;
                    }                   
                    if(scope!=null){
                        String v=t.getVariant(scope);
                        if( (d2.oldname==null && v!=null) ||
                            (d2.oldname!=null && (v==null || !d2.oldname.equals(v)) ) ) {
                            ret.add(new PatchException(PatchException.MODERATE,
                                    "Old variant name is "+v+", expected "+d2.oldname,t));
                        }
                    }
                }
            }
            else if(d instanceof TypeAdded){
                if(phase==0 || phase==4){
                    TypeAdded d2=(TypeAdded)d;
                    Object o=tryGetTopic(d2.t2,tm,addedTopics,deletedTopics);
                    if(o==null){
                        ret.add(new PatchException(PatchException.MODERATE,
                                "Could not resolve added type "+d2.t2,t));                        
                    }
                    else if(o instanceof Topic && t.isOfType((Topic)o)){
                        ret.add(new PatchException(PatchException.MINOR,
                                "Topic is already of the added type "+d2.t2,t));                                                
                    }
                }                
            }
            else if(d instanceof TypeDeleted){
                if(phase==0 || phase==1){
                    TypeDeleted d2=(TypeDeleted)d;
                    Object o=tryGetTopic(d2.t2,tm,addedTopics,deletedTopics);
                    if(o==null){
                        ret.add(new PatchException(PatchException.MODERATE,
                                "Could not resolve removed type "+d2.t2,t));                                                
                    }
                    else if(o instanceof Topic && !t.isOfType((Topic)o)){
                        ret.add(new PatchException(PatchException.MINOR,
                                "Topic is not of the removed type "+d2.t2,t));
                    }
                }                
            }
        }
        return ret;
    }
    
    public Object tryGetTopic(Object identifier,TopicMap tm,HashSet<Object> added,HashSet<Object> deleted) throws TopicMapException {
        if(deleted.contains(identifier)){
            if(added.contains(identifier)) return Boolean.TRUE;
            else return null;
        }
        if(added.contains(identifier)) return Boolean.TRUE;
        Topic t=getTopic(identifier,tm);
        return t;
    }
    
    public ArrayList<PatchException> tryPatch(ArrayList<DiffEntry> diff,TopicMap tm) throws TopicMapException {
        ArrayList<PatchException> ret=new ArrayList<PatchException>();
        /* Phases:
         *  1 remove associations and variants and types in topics so we can then safely remove topics
         *  2 remove topics so we can then safely apply other changes
         *  3 create new topics, change old topics, but do only base names and subject identifiers
         *  4 all needed topics are in now, do everything else
         */
        HashSet<Object> addedTopics=new HashSet<Object>();
        HashSet<Object> deletedTopics=new HashSet<Object>();
        for(int phase=1;phase<=4;phase++){
            for(DiffEntry d : diff){
                if(d instanceof TopicChanged){
                    if(phase==1 || phase==3 || phase==4){
                        TopicChanged d2=(TopicChanged)d;
                        Object t=tryGetTopic(d2.topic2,tm,addedTopics,deletedTopics);
                        if(t==null){
                            ret.add(new PatchException(PatchException.SEVERE,
                                    "Could not resolve changed topic "+d2.topic2));                                                                            
                        }
                        if(t instanceof Topic) 
                            ret.addAll(tryTopicDiff(d2.diff,(Topic)t,tm,phase,addedTopics,deletedTopics));
                    }
                }
                else if(d instanceof TopicAdded){
                    TopicAdded d2=(TopicAdded)d;
                    if(phase==3){
                        ret.addAll(tryTopicDiff(d2.diff,null,tm,phase,addedTopics,deletedTopics));
                    }
                    else if(phase==4){
                        Object o=getTopicIdentifier(d2.diff);
                        Object t=tryGetTopic(o,tm,addedTopics,deletedTopics);
                        if(t==null){
                            ret.add(new PatchException(PatchException.CRITICAL,
                                    "Could not resolve added topic"));
                        }
                        if(t instanceof Topic)
                            ret.addAll(tryTopicDiff(d2.diff,(Topic)t,tm,phase,addedTopics,deletedTopics));
                    }
                }
                else if(d instanceof TopicDeleted){
                    if(phase==1 || phase==2){
                        TopicDeleted d2=(TopicDeleted)d;
                        Object t=tryGetTopic(d2.topic2,tm,addedTopics,deletedTopics);
                        if(t==null){
                            ret.add(new PatchException(PatchException.MINOR,
                                    "Could not resolve deleted topic "+d2.topic2));
                        }
                        if(t instanceof Topic) {
                            deletedTopics.addAll(((Topic)t).getSubjectIdentifiers());
                            if(((Topic)t).getBaseName()!=null) deletedTopics.add(((Topic)t).getBaseName());
                            tryTopicDiff(d2.diff,(Topic)t,tm,phase,addedTopics,deletedTopics);
                            // tryTopicDiff checks if all changes in d2.diff are in the deleted topic
                            // need to also check if topic contains something not in d2.diff
                        }
                    }
                }
                else if(d instanceof AssociationAdded){
                    if(phase==4){
                        AssociationAdded d2=(AssociationAdded)d;
                        for(int i=0;i<d2.a2.length;i++){
                            Object t=tryGetTopic(d2.a2[i],tm,addedTopics,deletedTopics);
                            if(t==null){
                                if(i==0)
                                    ret.add(new PatchException(PatchException.SEVERE,
                                        "Could not resolve association type "+d2.a2[i]));
                                else if((i%2)==1)
                                    ret.add(new PatchException(PatchException.SEVERE,
                                        "Could not resolve association role "+d2.a2[i]));
                                else
                                    ret.add(new PatchException(PatchException.SEVERE,
                                        "Could not resolve association player "+d2.a2[i]));                                
                            }
                        }
                    }
                }
                else if(d instanceof AssociationDeleted){
                    if(phase==1){
                        AssociationDeleted d2=(AssociationDeleted)d;
                        Association a=getAssociation(d2.a2,tm);
                        if(a==null){
                            ret.add(new PatchException(PatchException.MINOR,
                                "Could not resolve deleted association"));
                        }
                    }
                }            
            }
        }
        return ret;
    }
    
    public static interface PatchExceptionHandler {
        public boolean handleException(PatchException e);
    }
    
    public static class PatchException {
        /** Minor inconsistency with patch. For example changes that seem to
         * have been already made. */
        public static final int MINOR=1;
        /** Moderate inconsistency with patch. 
         * Changes are still patched but some old values may be overwritten that
         * were not there when the patch was made. This may indicate conflicting
         * edits in the topic map. Unexpected merging of topics will also cause
         * a warning of this level. */
        public static final int MODERATE=2;
        /** Severe inconsistency with patch. Unable to make changes described in
          * the patch because the topic map is incompatible with the patch. */
        public static final int SEVERE=3;
        /** Critical failure with patch. */
        public static final int CRITICAL=4;
        public int level;
        public String message;
        public Object context;
        public PatchException(int level,String message){
            this(level,message,null);
        }
        public PatchException(int level,String message,Object context){
            this.level=level;
            this.message=message;
            this.context=context;
        }
    }
    
    public abstract static class DiffEntry {
        
    }
    public static class TopicChanged extends DiffEntry{
        public Topic topic;
        public Object topic2;
        public ArrayList<TopicDiffEntry> diff;
        public TopicChanged(){}
        public TopicChanged(Topic topic,ArrayList<TopicDiffEntry> diff){
            this.topic=topic;
            this.diff=diff;
        }
        public TopicChanged(Object topic2,ArrayList<TopicDiffEntry> diff){
            this.topic2=topic2;
            this.diff=diff;
        }
    }
    public static class TopicDeleted extends DiffEntry{
        public Topic topic;
        public Object topic2;
        public ArrayList<TopicDiffEntry> diff;
        public TopicDeleted(){}
        public TopicDeleted(Topic topic,ArrayList<TopicDiffEntry> diff){
            this.topic=topic;
            this.diff=diff;
        }
        public TopicDeleted(Object topic2,ArrayList<TopicDiffEntry> diff){
            this.topic2=topic2;
            this.diff=diff;
        }
    }
    public static class TopicAdded extends DiffEntry{
        public ArrayList<TopicDiffEntry> diff;
        public TopicAdded(){}
        public TopicAdded(ArrayList<TopicDiffEntry> diff){this.diff=diff;}
    }
    
    public abstract static class TopicDiffEntry {
        
    }
    public static class SIAdded extends TopicDiffEntry {
        public Locator si;
        public SIAdded(){}
        public SIAdded(Locator si){this.si=si;}
    }
    public static class SIDeleted extends TopicDiffEntry {
        public Locator si;
        public SIDeleted(){}        
        public SIDeleted(Locator si){this.si=si;}        
    }
    public static class TypeAdded extends TopicDiffEntry {
        public Topic t;
        public Object t2;
        public TypeAdded(){}
        public TypeAdded(Topic t){this.t=t;}
        public TypeAdded(Object t2){this.t2=t2;}
    }
    public static class TypeDeleted extends TopicDiffEntry {
        public Topic t;
        public Object t2;
        public TypeDeleted(){}        
        public TypeDeleted(Topic t){this.t=t;}        
        public TypeDeleted(Object t2){this.t2=t2;}
    }
    public static class SLChanged extends TopicDiffEntry {
        public Locator sl;
        public Locator oldsl;
        public SLChanged(){}
        public SLChanged(Locator sl,Locator oldsl){
            this.sl=sl;
            this.oldsl=oldsl;
        }        
    }
    public static class BNChanged extends TopicDiffEntry {
        public String bn;
        public String oldbn;
        public BNChanged(){}
        public BNChanged(String bn,String oldbn){
            this.bn=bn;
            this.oldbn=oldbn;
        }        
    }
    public static class VariantChanged extends TopicDiffEntry {
        public Set<Topic> scope;
        public Set<Object> scope2;
        public String name;
        public String oldname;
        public VariantChanged(){}
        public VariantChanged(Set<Topic> scope,String name,String oldname){
            this.scope=scope;
            this.name=name;
            this.oldname=oldname;
        }
        public VariantChanged(Set<Object> scope2,String name,String oldname,boolean dummy){
            // dummy only to get different method signature
            this.scope2=scope2;
            this.name=name;
            this.oldname=oldname;
        }
    }
    public static class AssociationDeleted extends DiffEntry {
        public Association a;
        public Object[] a2;
        public AssociationDeleted(){}
        public AssociationDeleted(Association a){this.a=a;}
        public AssociationDeleted(Object[] a2){
            this.a2=a2;
        }
    }
    public static class AssociationAdded extends DiffEntry {
        public Topic[] a;
        public Object[] a2;
        public AssociationAdded(){}
        public AssociationAdded(Association association) throws TopicMapException {
            a=new Topic[association.getRoles().size()*2+1];
            a[0]=association.getType();
            int i=1;
            for(Topic r : association.getRoles()){
                a[i++]=r;
                a[i++]=association.getPlayer(r);
            }
        }        
        public AssociationAdded(Topic[] a){this.a=a;}        
        public AssociationAdded(Object[] a2){
            this.a2=a2;
        }
    }
    
    
    //////// Command line tool //////////////
    
    public static TopicMap openFile(String f) throws IOException, TopicMapException {
        String extension="";
        int ind=f.lastIndexOf(".");
        if(ind>-1) extension=f.substring(ind+1);
        if(extension.equalsIgnoreCase("wpr")){
            PackageInput in=new ZipPackageInput(f);
            TopicMapType type=TopicMapTypeManager.getType(LayerStack.class);
            TopicMap tm=type.unpackageTopicMap(in,"",null,null);
            in.close();
            return tm;
        }
        else if(extension.equalsIgnoreCase("ltm")){
            TopicMap tm=new TopicMapImpl();
            tm.importLTM(f);
            return tm;
        }
        else{ // xtm
            TopicMap tm=new TopicMapImpl();
            tm.importXTM(f);
            return tm;
        }
    }
    
    public static void printUsage(){
        System.out.println("java org.wandora.topicmap.diff.DiffTool <command> <options>");
        System.out.println("Commands:");
        System.out.println("  c    Compare two topic maps.");
        System.out.println("  p    Patch a topic map.");
        System.out.println("  P    Perform an inverse patch.");
        System.out.println("  i    Inverse a patch file.");
        System.out.println("  h    Calculate a hash code for a topic map.");
        System.out.println("Options:");
        System.out.println("  <filename> Input file. For comparison enter two topic maps.");
        System.out.println("             For patching enter a topic map and a patch file in that order.");
        System.out.println("             For hash code enter a single topic map.");
        System.out.println("  -o   Output in the specified file.");
        System.out.println("  -e   Character encoding for patch files, default is UTF-8.");
    }
        
    public static void main(String[] args) throws Exception {
        if(args.length<1){
            printUsage();
            return;
        }
        char cmd=args[0].charAt(0);
        
        String encoding="UTF-8";
        String file1=null;
        String file2=null;
        String outfile=null;
        for(int i=1;i<args.length;i++){
            if(args[i].equals("-o")){
                if(i+1<args.length) {
                    outfile=args[i+1];
                    i++;
                }
                else {
                    printUsage();
                    return;
                }
            }
            else if(file1==null) file1=args[i];
            else if(file2==null) file2=args[i];
            else {
                printUsage();
                return;
            }
        }
        
        if(cmd=='c'){
            if(file1==null || file2==null){
                printUsage();
                return;
            }
            TopicMap tm1=openFile(file1);
            TopicMap tm2=openFile(file2);
            TopicMapDiff tmDiff=new TopicMapDiff();
            Writer out=null;
            if(outfile!=null) out=new OutputStreamWriter(new FileOutputStream(outfile),encoding);
            else out=new OutputStreamWriter(System.out);
            tmDiff.makeDiff(tm1, tm2, new BasicDiffOutput(new PatchDiffEntryFormatter(),out));
            out.close();
        }
        else if(cmd=='p' || cmd=='P'){
            if(file1==null || file2==null){
                printUsage();
                return;
            }
            TopicMap tm1=openFile(file1);
            PatchDiffParser parser=new PatchDiffParser(new InputStreamReader(new FileInputStream(file2),encoding));
            ArrayList<DiffEntry> diff=parser.parse();
            TopicMapDiff tmDiff=new TopicMapDiff();
            if(cmd=='P') diff=tmDiff.makeInverse(diff);
            tmDiff.applyDiff(diff, tm1);
            OutputStream out=null;
            if(outfile!=null) out=new FileOutputStream(outfile);
            else out=System.out;
            tm1.exportXTM(out);
            out.close();
        }
        else if(cmd=='i'){
            if(file1==null || file2!=null){
                printUsage();
                return;
            }
            PatchDiffParser parser=new PatchDiffParser(new InputStreamReader(new FileInputStream(file1),encoding));
            ArrayList<DiffEntry> diff=parser.parse();
            TopicMapDiff tmDiff=new TopicMapDiff();
            diff=tmDiff.makeInverse(diff);
            Writer out=null;
            if(outfile!=null) out=new OutputStreamWriter(new FileOutputStream(outfile),encoding);
            else out=new OutputStreamWriter(System.out);
            BasicDiffOutput diffOut=new BasicDiffOutput(new PatchDiffEntryFormatter(),out);
            diffOut.outputDiff(diff);
            out.close();
        }
        else if(cmd=='h'){
            if(file1==null || file2!=null){
                printUsage();
                return;
            }
            TopicMap tm1=openFile(file1);
            long hash=TopicMapHashCode.getTopicMapHashCode(tm1);
            System.out.println(Long.toHexString(hash));
        }
    }
}
