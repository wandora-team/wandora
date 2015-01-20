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
 * 
 * TopicTreeModel.java
 *
 * Created on 27. joulukuuta 2005, 23:04
 *
 */

package org.wandora.application.gui.tree;


import org.wandora.topicmap.SchemaBox;
import org.wandora.topicmap.TMBox;
import java.util.*;
import org.wandora.topicmap.*;
import javax.swing.tree.*;
import org.wandora.application.gui.TopicGuiWrapper;
import org.wandora.utils.GripCollections;




/**
 *
 * @author olli, akivela
 */
public class TopicTreeModel implements TreeModel {

    private HashMap<TopicGuiWrapper, TopicGuiWrapper[]> children;
    private HashSet listeners;
    private TopicGuiWrapper rootNode;
    
    private HashSet<Locator> visibleTopics;
    private int visibleTopicCount = 0;

    private ArrayList<TopicTreeRelation> associations;
    private TopicTree tree;
    
    
    
    public TopicTreeModel(Topic rootTopic, ArrayList<TopicTreeRelation> associations, TopicTree tree) {
        this.associations=associations;
        this.tree=tree;
        children=new HashMap();
        listeners=new HashSet();
        rootNode=new TopicGuiWrapper(rootTopic);
        visibleTopics=new HashSet<Locator>();
        visibleTopicCount = 0;
        try {
            visibleTopics.addAll(rootTopic.getSubjectIdentifiers());
            visibleTopicCount++;
        }
        catch(TopicMapException tme){tme.printStackTrace();}
    }

    
    
    private Object expansionWaiter=new Object();
    public void waitExpansionDone(final TopicGuiWrapper node){
        synchronized(expansionWaiter){
            while(true){
                TopicGuiWrapper[] c=(TopicGuiWrapper[])children.get(node);
                if(c==null || c.length!=1) return;
                if(c.length==1 && !c[0].associationType.equals(TopicGuiWrapper.PROCESSING_TYPE)) return;
                try {
                    expansionWaiter.wait();
                }
                catch(InterruptedException ie){return;}
            }
        }
    }
    
    
    
    public Set<Locator> getVisibleTopics() {
        return visibleTopics;
    }
    
    
    public int getVisibleTopicCount() {
        return visibleTopicCount;
    }
    
    
    public void childrenModified(TopicGuiWrapper node){
        Object debug=children.remove(node);
        childrenModifiedNoRemove(node);
    }
    
    
    private void childrenModifiedNoRemove(final TopicGuiWrapper node){
        Iterator iter=listeners.iterator();
        while(iter.hasNext()){
            javax.swing.event.TreeModelListener l=(javax.swing.event.TreeModelListener)iter.next();
//            l.treeStructureChanged(new javax.swing.event.TreeModelEvent(this,new Object[]{rootNode}));
            l.treeStructureChanged(new javax.swing.event.TreeModelEvent(this,node.path));
        }
        tree.refreshSize();
    }

    
    
    public Object getChildFor(TopicGuiWrapper node,Topic t){
        TopicGuiWrapper[] cs=getChildren(node);
        for(int i=0;i<cs.length;i++){
//                if(cs[i].topic==t) return cs[i];
            if(cs[i].topic.equals(t)) return cs[i];
        }
        return null;
        
    }
    
    
    private TopicGuiWrapper[] getChildren(Object node){
        return getChildren((TopicGuiWrapper)node);
    }
    
    
    private TopicGuiWrapper[] getChildren(final TopicGuiWrapper node) {
        if(children.containsKey(node)) return (TopicGuiWrapper[])children.get(node);
        
        final Object waitObject=new Object();
        final int[] state=new int[1]; state[0]=0; // simple way to wrap a modifyable int in an object
        // 0 - not ready, original waiting
        // 1 - ready, original waiting
        // 2 - not ready, original not waiting
        // 3 - ready, original not waiting
        final Thread originalThread=Thread.currentThread();

        Thread t = new Thread(){
            @Override
            public void run(){
                int i=0;
                ArrayList<TopicGuiWrapper> ts=new ArrayList<TopicGuiWrapper>();
                String instancesIcon=null;
                for(TopicTreeRelation a : associations){
                    if("Instances".equalsIgnoreCase(a.name)){
                        instancesIcon=a.icon;
                        continue;
                    }
                    Collection s=null;
                    try {
                        i = 99999;
                        s = SchemaBox.getSubClassesOf(node.topic,a.subSI,a.assocSI,a.superSI);
                        if(s != null && s.size() > 0) {
                            s = TMBox.sortTopics(s, null);
                            Iterator iter=s.iterator();
                            while(iter.hasNext() && --i > 0){
                                ts.add(new TopicGuiWrapper((Topic)iter.next(),a.icon,a.name,node.path));
                            }
                        }
                    }
                    catch(Exception tme) {
                        tme.printStackTrace();
                    }
                }
                if(instancesIcon!=null){
                    try {
                        i=99999;
                        Collection c=node.topic.getTopicMap().getTopicsOfType(node.topic);
                        if(c != null && c.size() > 0) {
                            c=TMBox.sortTopics(c, null);
                            Iterator iter=c.iterator();
                            while(iter.hasNext() && --i > 0){
                                ts.add(new TopicGuiWrapper((Topic)iter.next(),instancesIcon,"Instances",node.path));
                            }
                        }
                    }
                    catch(Exception tme) {
                        tme.printStackTrace();
                    }
                }
                TopicGuiWrapper[] tsa=new TopicGuiWrapper[0];
                try{
                    tsa = GripCollections.collectionToArray(ts, TopicGuiWrapper.class);
                    synchronized(visibleTopics) {
                        for(int j=0;j<tsa.length;j++) {
                            if(tsa[j] != null && tsa[j].topic != null && !tsa[j].topic.isRemoved()) {
                                visibleTopics.addAll(tsa[j].topic.getSubjectIdentifiers());
                                visibleTopicCount++;
                            }
                        }
                    }
                    //System.out.println("visible topics (locators): "+visibleTopics.size());
                }
                catch(Exception tme) {
                    tme.printStackTrace();
                }
                synchronized(waitObject){
                    synchronized(expansionWaiter) {
                        children.put(node,tsa);
                        state[0]|=1;
                        if((state[0]&2)==0 ){
                            waitObject.notify();
                        }
                        else{
                            childrenModifiedNoRemove(node);
                        }
                        expansionWaiter.notifyAll();
                    }
                }
            }
        };

        synchronized(waitObject){
            //t.run();
            t.start();
            try{
                waitObject.wait(2000);
//                waitObject.wait();
            }catch(InterruptedException ie){ie.printStackTrace();}

            state[0]|=2;
            if((state[0]&1)>0) {
                return (TopicGuiWrapper[])children.get(node);
            }
            else {
                TopicGuiWrapper[] tsa=new TopicGuiWrapper[0];
//                tsa[0]=new TopicGuiWrapper(null,"gui/icons/topictree/cycle01.png",TopicGuiWrapper.PROCESSING_TYPE,node.path);
//                tsa[0]=new TopicGuiWrapper(null,null,TopicGuiWrapper.PROCESSING_TYPE,node.path);
//                children.put(node,tsa);
                return tsa;
            }
        }
    }


    public void update() {
        TopicGuiWrapper node;
        for(Object n : children.keySet()) {
            try {
                node = (TopicGuiWrapper) n;
                getChildren(node);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    @Override
    public void addTreeModelListener(javax.swing.event.TreeModelListener l) {
        listeners.add(l);
    }

    public TopicGuiWrapper getRootNode() {
        return this.rootNode;
    }
    
    
    @Override
    public Object getChild(Object parent, int index) {
        return getChildren(parent)[index];
    }

    
    @Override
    public int getChildCount(Object parent) {
        return getChildren(parent).length;
    }

    
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if(parent==null || child==null) return -1;
        TopicGuiWrapper[] cs=getChildren(parent);
        for(int i=0;i<cs.length;i++){
//                if(cs[i]==child) return i;
            if(cs[i].equals(child)) return i;
        }
        return -1;
    }

    
    @Override
    public Object getRoot() {
        return rootNode;
    }

    
    @Override
    public boolean isLeaf(Object node) {
        TopicGuiWrapper wrapper=(TopicGuiWrapper)node;
        if(wrapper.associationType.equals(TopicGuiWrapper.PROCESSING_TYPE)) return true;
        else return false;
    }

    @Override
    public void removeTreeModelListener(javax.swing.event.TreeModelListener l) {
        listeners.remove(l);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }
    

    public TreePath getPathFor(Topic t) {
        if(t == null) return null;
        try {
            if(!visibleTopics.contains(t.getOneSubjectIdentifier())) return null;
            for(TopicGuiWrapper node : children.keySet()) {
                if(t.mergesWithTopic(node.topic)) {
                    return node.path;
                }
                for(TopicGuiWrapper childNode : children.get(node)) {
                    if(t.mergesWithTopic(childNode.topic)) {
                        return childNode.path;
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    

}