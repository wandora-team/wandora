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
 * HyperCubeGenerator.java
 *
 * Created on 2008-09-19, 16:28
 *
 */


package org.wandora.application.tools.generators;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Tuples.T2;
import org.wandora.utils.swing.GuiTools;



/**
 *
 * @author akivela
 */
public class HyperCubeGenerator extends AbstractGenerator implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static String HYPERCUBE_GRAPH_SI = "http://wandora.org/si/hypercube/";
    
    public static String siPattern = "http://wandora.org/si/hypercube/vertex/__n__";
    public static String basenamePattern = "Hypercube vertex __n__";
    public static boolean connectWithWandoraClass = true;
    public static int n = 4;
    
    
    /** Creates a new instance of HyperCubeGenerator */
    public HyperCubeGenerator() {
    }


    @Override
    public String getName() {
        return "Hypercube graph generator";
    }
    
    @Override
    public String getDescription() {
        return "Hypercube graph generator creates hypercube graphs with topic map structures.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,
            "Hypercube graph generator",
            "Hypercube graph generator creates hypercube graphs with topic map structures. "+
                    "Topics represent hypercube vertices. Associations represent hypercube edges. In a "+
                    "hypercube each vertex is connected with other vertices. Number of connections per vertex "+
                    "is equal to the dimension of the hypercube. For example, in a three dimensional hypercube "+
                    "each vertex is connected with three other vertices.",
            true,new String[][]{
            new String[]{"Dimension of hypercube","string",""+n},
            new String[]{"---1","separator"},
            new String[]{"Subject identifier pattern","string",siPattern,"Subject identifier patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
            new String[]{"Basename pattern","string",basenamePattern,"Basename patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
            new String[]{"Connect topics with Wandora class","boolean", connectWithWandoraClass ? "true" : "false","Create additional topics and associations that connect created topics with the Wandora class." },
            new String[]{"Association type topic","topic",null,"Optional association type for graph edges."},
            new String[]{"First role topic","topic",null,"Optional role topic for graph edges."},
            new String[]{"Second role topic","topic",null,"Optional role topic for graph edges."},
        },wandora);
        
        god.setSize(700, 420);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int progress = 0;
        try {
            n = Integer.parseInt(values.get("Dimension of hypercube"));
        }
        catch(Exception e) {
            singleLog("Parse error. Hypercube dimension should be an integer number. Cancelling.", e);
            return;
        }
        
        try {
            siPattern = values.get("Subject identifier pattern");
            if(!siPattern.contains("__n__")) {
                int a = WandoraOptionPane.showConfirmDialog(wandora, "Subject identifier pattern doesn't contain part for topic counter '__n__'. This causes all generated topics to merge. Do you want to continue?", "Missing topic counter part", WandoraOptionPane.WARNING_MESSAGE);
                if(a != WandoraOptionPane.YES_OPTION) return;
            }
            basenamePattern = values.get("Basename pattern");
            if(!basenamePattern.contains("__n__")) {
                int a = WandoraOptionPane.showConfirmDialog(wandora, "Basename pattern doesn't contain part for topic counter '__n__'. This causes all generated topics to merge. Do you want to continue?", "Missing topic counter part", WandoraOptionPane.WARNING_MESSAGE);
                if(a != WandoraOptionPane.YES_OPTION) return;
            }
            connectWithWandoraClass = "true".equalsIgnoreCase(values.get("Connect topics with Wandora class"));
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        Topic atype = topicmap.getTopic(values.get("Association type topic"));
        if(atype == null || atype.isRemoved()) {
            atype = getOrCreateTopic(topicmap, HYPERCUBE_GRAPH_SI+"/"+"association-type", "Hypercube edge");
        }
        
        Topic role1 = topicmap.getTopic(values.get("First role topic"));
        if(role1 == null || role1.isRemoved()) {
            role1 = getOrCreateTopic(topicmap, HYPERCUBE_GRAPH_SI+"/"+"role-1", "Hypercube edge role 1");
        }
        
        Topic role2 = topicmap.getTopic(values.get("Second role topic"));
        if(role2 == null || role2.isRemoved()) {
            role2 = getOrCreateTopic(topicmap, HYPERCUBE_GRAPH_SI+"/"+"role-2", "Hypercube edge role 2");
        }
        
        if(role1.mergesWithTopic(role2)) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Role topics are same. This causes associations to be unary instead of binary. Do you want to continue?", "Role topics are same", WandoraOptionPane.WARNING_MESSAGE);
            if(a != WandoraOptionPane.YES_OPTION) {
                return;
            }
        }
        
        setDefaultLogger();
        setLogTitle("Hypercube graph generator");
        log("Creating hypercube graph.");
                       
        HyperCube hypercube = new HyperCube(n);
        Collection<T2<String,String>> edges = hypercube.getEdges();
        
        Association a = null;
        Topic node1 = null;
        Topic node2 = null;
        long graphIdentifier = System.currentTimeMillis();
        
        if(!edges.isEmpty()) {
            setProgressMax(edges.size());
            for(T2<String,String> edge : edges) {
                if(edge != null) {
                    node1 = getOrCreateTopic(topicmap, edge.e1, graphIdentifier);
                    node2 = getOrCreateTopic(topicmap, edge.e2, graphIdentifier);
                    if(node1 != null && node2 != null) {
                        a = topicmap.createAssociation(atype);
                        a.addPlayer(node1, role1);
                        a.addPlayer(node2, role2);
                    }
                    setProgress(progress++);
                }
            }
        }
        
        if(connectWithWandoraClass) {
            log("You'll find created topics and associations under the 'Hypercube graph' topic.");
        }
        else {
            String searchWord = basenamePattern.replaceAll("__n__", "");
            searchWord = searchWord.trim();
            log("You'll find created topics and associations by searching with a '"+searchWord+"'.");
        }
        log("Ready.");
        setState(WAIT);
    }
    
    
    
    
    
    private Topic getOrCreateTopic(TopicMap tm, String vertexIdentifier, long graphIdentifier) {
        String newBasename = basenamePattern.replaceAll("__n__", vertexIdentifier);
        String newSubjectIdentifier = siPattern.replaceAll("__n__", vertexIdentifier);
        Topic t = getOrCreateTopic(tm, newSubjectIdentifier, newBasename);
        if(connectWithWandoraClass) {
            try {
                Topic graphTopic = getOrCreateTopic(tm, HYPERCUBE_GRAPH_SI, "Hypercube graph");
                Topic wandoraClass = getOrCreateTopic(tm, TMBox.WANDORACLASS_SI);
                makeSuperclassSubclass(tm, wandoraClass, graphTopic);
                Topic graphInstanceTopic = getOrCreateTopic(tm, HYPERCUBE_GRAPH_SI+"/"+graphIdentifier, "Hypercube graph "+graphIdentifier, graphTopic);
                graphInstanceTopic.addType(graphTopic);
                t.addType(graphInstanceTopic);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    private class HyperCube {
        private int dimension = 0;
        private HyperCube parent = null;

                
        
        public HyperCube(int d) {
            this.dimension = d;
            if(d > 0) {
                this.parent = new HyperCube(d-1);
            }
        }
        
        public int getDimension() {
            return dimension;
        }
        
    
        public Collection<T2<String,String>> getEdges() {
            ArrayList<T2<String,String>> edges = new ArrayList<T2<String,String>>();
            if(this.dimension == 0) {}
            else if(this.dimension == 1) {
                edges.add(new T2<String,String>("0", "0-1"));
            }
            else {
                if(parent != null) {
                    Collection<T2<String,String>> parentEdges = parent.getEdges();
                    edges.addAll(parentEdges);
                    
                    for(T2<String,String> edge : parentEdges) {
                        if(edge != null) {
                            edges.add( new T2<String,String>( edge.e1+"-"+this.dimension, edge.e2+"-"+this.dimension ));
                        }
                    }
                    
                    Collection<String> parentVertices = parent.getVertices();
                    for(String vertex : parentVertices) {
                        if(vertex != null) {
                            edges.add( new T2<String,String>(vertex+"-"+this.dimension, vertex) );
                        }
                    }
                    //System.out.println("edges: dim="+this.dimension+", edges="+edges.size());
                }
            }
            return edges;
        }
        
        
        public Collection<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<String>();
            if(this.dimension == 0) {
                vertices.add( "0" );
            }
            else {
                if(parent != null) {
                    Collection<String> parentVertices = parent.getVertices();
                    vertices.addAll(parentVertices);
                    for(String vertex : parentVertices) {
                        vertices.add(vertex+"-"+this.dimension);
                    }
                    //System.out.println("dim="+this.dimension+", vertices="+vertices.size());
                }
            }
            return vertices;
        }
        

    }
    
}
