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
 */



package org.wandora.application.tools.r;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.LayeredTopicContext;
import org.wandora.topicmap.*;

import org.wandora.application.tools.extractors.ExtractHelper;



/**
 *
 * @author akivela
 */


public class RHelper {
    
    
    
    private static final String IGRAPH_SI = "http://wandora.org/si/R/igraph";
    private static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
    
    public static class Graph {
        public int numNodes;
        public String labels[];
        public String colors[];
        public int[] edges;

        public Graph(int numNodes, String[] labels, String[] colors, int[] edges) {
            this.numNodes = numNodes;
            this.labels = labels;
            this.colors = colors;
            this.edges = edges;
        }
    }
    
    
    
    public static Graph makeGraph(Topic[] topics){
        try{
            HashMap<String,Integer> indices=new HashMap<String,Integer>();
            ArrayList<String> labels=new ArrayList<String>();
            ArrayList<String> colors=new ArrayList<String>();
            ArrayList<Integer> edges=new ArrayList<Integer>();

            for(Topic t : topics){
                indices.put(t.getOneSubjectIdentifier().toString(),labels.size()+1);
                labels.add(t.getBaseName());
                colors.add("lightblue");
            }

            for(Topic t : topics){
                for(Association a : t.getAssociations()) {

                    Collection<Topic> rs=a.getRoles();
                    Topic player1=a.getPlayer(rs.iterator().next());

                    if(t.mergesWithTopic(player1)){

                        ArrayList<Topic> ps=new ArrayList<Topic>();
                        for(Topic r : rs){
                            Topic p=a.getPlayer(r);
                            if(indices.containsKey(p.getOneSubjectIdentifier().toString()))
                                ps.add(p);
                        }

                        if(ps.size()<=1) continue;
                        else if(ps.size()==2){
                            Iterator<Topic> iter=ps.iterator();
                            edges.add(indices.get(iter.next().getOneSubjectIdentifier().toString()));
                            edges.add(indices.get(iter.next().getOneSubjectIdentifier().toString()));
                        }
                        else {
                            int aind=labels.size();
                            labels.add("");
                            colors.add("gray");
                            Iterator<Topic> iter=ps.iterator();
                            while(iter.hasNext()) {
                                edges.add(indices.get(iter.next().getOneSubjectIdentifier().toString()));
                                edges.add(aind);
                            }
                        }

                    }

                }
            }
            int[] iedges=new int[edges.size()];
            for(int i=0;i<edges.size();i++){ iedges[i]=edges.get(i); }
            return new Graph(labels.size(),labels.toArray(new String[labels.size()]),colors.toArray(new String[colors.size()]),iedges);
        }
        catch(TopicMapException tme){
            tme.printStackTrace();
            return null;
        }
    }

    
    
    public static Association[] getContextAssociations(){
        AssociationContext context=new AssociationContext();
        context.initialize(Wandora.getWandora(), null, null);
        ArrayList<Association> ret=new ArrayList<Association>();
        Iterator iter=context.getContextObjects();
        if(iter!=null){
            while(iter.hasNext()){
                Object o=iter.next();
                if(o instanceof Association) ret.add((Association)o);
            }
        }
        return ret.toArray(new Association[ret.size()]);
    }

    public static Topic[] getContextTopics(){
        LayeredTopicContext context=new LayeredTopicContext(Wandora.getWandora(),null,null);
        ArrayList<Topic> ret=new ArrayList<Topic>();
        Iterator iter=context.getContextObjects();
        if(iter!=null){
            while(iter.hasNext()){
                Object o=iter.next();
                if(o instanceof Topic) ret.add((Topic)o);
            }
        }
        
        return ret.toArray(new Topic[ret.size()]);
    }

    // "next" is a reserved word in R which makes this tricky
    public static Object[] unwrapIterator(Iterator iter){
        ArrayList<Object> ret=new ArrayList<Object>();
        while(iter.hasNext()){
            ret.add(iter.next());
        }
        return ret.toArray();
    }
    
    /*
     * Create a set of topics from vertices connected by associations derived
     * from edges. 'edges' should be a list of tuples.
     */
    
    public static void createTopics(double[][] edges, int[] vertices, String[] bns, HashMap<String,String[]> occ ) throws TopicMapException{
        
        /*
         * Initialization: create type topics for the graph vertices and edges.
         * Also make the type topics subclasses of the Wandora class.
         */
        
        TopicMap tm = Wandora.getWandora().getTopicMap();
        Topic iGraphTypeTopic = getiGraphTypeTopic(tm);
        Topic vertexTypeTopic = getVertexTypeTopic(tm, iGraphTypeTopic);
        Topic edgeTypeTopic   = getEdgeTypeTopic(tm, iGraphTypeTopic);
        
        Topic role1           = getRole1Topic(tm);
        Topic role2           = getRole2Topic(tm);
        
        
        HashMap<Integer,Topic> edgeMap = new HashMap<Integer, Topic>();
        HashMap<String,Topic> occTypeMap = new HashMap<String,Topic>();
        Topic[][] toAssoc = new Topic[edges.length][edges[0].length];
        
        /*
         * - Create a topic for each vertex. 
         * 
         * - Map the ids to topics so we can easily associate the topics after they've
         *   been created.
         */
        
        if(occ != null){
            for(String key : occ.keySet()){
                System.out.println("adding occurrence type " + key);
                occTypeMap.put(key, getOccTypeTopic(tm,key));
            }
        }
        
        for (int i = 0; i <vertices.length; i++) {
            Topic t = getVertexTopic(Integer.toString(vertices[i]), tm, vertexTypeTopic);
            if(bns != null){
                t.setBaseName(bns[i]);
            }
            if(occ != null){
                for(String key : occ.keySet()){
                    addOccurrence(t,occTypeMap.get(key),occ.get(key)[i],tm);
                }
            }
            edgeMap.put(vertices[i], t);
        }
        
        for (int j = 0; j < edges.length; j++) {
            for (int k = 0; k < edges[0].length; k++) {
                toAssoc[j][k] = edgeMap.get((int)edges[j][k]);
            }
        }
        
        /*
         * Finally associate the vertex topics by their edges.
         */
        
        for (int p = 0; p < toAssoc.length; p++) {
            addAssoc(tm, toAssoc[p], edgeTypeTopic, role1, role2);
        }

    }
    
     private static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        Topic lang = ExtractHelper.getOrCreateTopic(LANG_SI,tm);
        return lang;
    }
    
    private static void addOccurrence(Topic t, Topic type, String value, TopicMap tm) throws TopicMapException{
        t.setData(type, getLangTopic(tm), value);
    }
    
    private static Topic getOccTypeTopic(TopicMap tm, String key) throws TopicMapException{
        Topic type=ExtractHelper.getOrCreateTopic( IGRAPH_SI + "/occurrence/" + key, key,tm);
        return type;
    }
    
    private static void addAssoc(TopicMap tm, Topic[] ts, Topic type, Topic role1, Topic role2) throws TopicMapException{
        Association a = tm.createAssociation(type);
        a.addPlayer(ts[0], role1);
        a.addPlayer(ts[1], role2);
    }
    
    private static Topic getVertexTopic(String id, TopicMap tm, Topic typeTopic) throws TopicMapException {
        Topic vertexTopic=ExtractHelper.getOrCreateTopic(IGRAPH_SI+"/vertex/"+id, tm);
        vertexTopic.addType(typeTopic);
        return vertexTopic;
    }
    
    private static Topic getiGraphTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=ExtractHelper.getOrCreateTopic( IGRAPH_SI, "iGraph",tm);
        Topic wandoraClass = ExtractHelper.getWandoraClass(tm);
        ExtractHelper.makeSubclassOf(type, wandoraClass,tm);
        return type;
    }
    
    private static Topic getVertexTypeTopic(TopicMap tm, Topic iGraphTypeTopic) throws TopicMapException {
        Topic type=ExtractHelper.getOrCreateTopic( IGRAPH_SI + "/vertex", "Vertex",tm);
        ExtractHelper.makeSubclassOf(type, iGraphTypeTopic,tm);
        return type;
    }
    
    private static Topic getEdgeTypeTopic(TopicMap tm, Topic iGraphTypeTopic) throws TopicMapException {
        Topic type=ExtractHelper.getOrCreateTopic( IGRAPH_SI + "/edge", "Edge",tm);
        ExtractHelper.makeSubclassOf(type, iGraphTypeTopic,tm);
        return type;
    }
    
    private static Topic getRole1Topic(TopicMap tm) throws TopicMapException{
        Topic role=ExtractHelper.getOrCreateTopic( IGRAPH_SI + "/edgeEnd1", "Edge end 1",tm);
        return role;
    }
    
    private static Topic getRole2Topic(TopicMap tm) throws TopicMapException{
        Topic role=ExtractHelper.getOrCreateTopic( IGRAPH_SI + "/edgeEnd2", "Edge end 2",tm);
        return role;
    }
    
}
