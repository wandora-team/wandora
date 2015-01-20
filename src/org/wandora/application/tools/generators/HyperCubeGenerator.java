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
 * HyperCubeGenerator.java
 *
 * Created on 2008-09-19, 16:28
 *
 */


package org.wandora.application.tools.generators;


import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import java.util.*;
import static org.wandora.utils.Tuples.T2;



/**
 *
 * @author akivela
 */
public class HyperCubeGenerator extends AbstractGenerator implements WandoraTool {
    public static String SI_PREFIX = "http://wandora.org/si/hypercube/";
    
    
    /** Creates a new instance of HyperCubeGenerator */
    public HyperCubeGenerator() {
    }


    @Override
    public String getName() {
        return "Hypercube graph generator";
    }
    
    @Override
    public String getDescription() {
        return "Generates hypercube graph topic maps";
    }
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,
            "Hypercube graph generator",
            "Hypercube graph generator creates hypercube abstractions with topic map structures.",
            true,new String[][]{
            new String[]{"Dimension of hypercube","string"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int progress = 0;
        int n = 0;
        try {
            n = Integer.parseInt(values.get("Dimension of hypercube"));
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        setDefaultLogger();
        setLogTitle("Hypercube graph generator");
        log("Creating hypercube graph");
                       
        HyperCube hypercube = new HyperCube(n);
        ArrayList<T2> edges = hypercube.getEdges();
        
        Topic atype = getOrCreateTopic(topicmap, SI_PREFIX+"edge", "hypercube edge");
        Topic role1 = getOrCreateTopic(topicmap, SI_PREFIX+"role1", "role1");
        Topic role2 = getOrCreateTopic(topicmap, SI_PREFIX+"role2", "role2");
        Association a = null;
        Topic node1 = null;
        Topic node2 = null;
        
        if(edges.size() > 0) {
            setProgressMax(edges.size());
            for(T2 edge: edges) {
                if(edge != null) {
                    node1 = getOrCreateTopic(topicmap, SI_PREFIX+"vertex-"+edge.e1, "hypercube vertex "+edge.e1);
                    node2 = getOrCreateTopic(topicmap, SI_PREFIX+"vertex-"+edge.e2, "hypercube vertex "+edge.e2);
                    if(node1 != null && node2 != null) {
                        a = topicmap.createAssociation(atype);
                        a.addPlayer(node1, role1);
                        a.addPlayer(node2, role2);
                    }
                    setProgress(progress++);
                }
            }
        }
        setState(CLOSE);
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
        
    
        public ArrayList<T2> getEdges() {
            ArrayList<T2> edges = new ArrayList<T2>();
            if(this.dimension == 0) {}
            else if(this.dimension == 1) {
                edges.add(new T2("0", "0-1"));
            }
            else {
                if(parent != null) {
                    ArrayList<T2> parentEdges = parent.getEdges();
                    edges.addAll(parentEdges);
                    
                    for(T2 edge : parentEdges) {
                        if(edge != null) {
                            edges.add( new T2( edge.e1+"-"+this.dimension, edge.e2+"-"+this.dimension ));
                        }
                    }
                    
                    ArrayList<String> parentVertices = parent.getVertices();
                    for(String vertex : parentVertices) {
                        if(vertex != null) {
                            edges.add( new T2(vertex+"-"+this.dimension, vertex) );
                        }
                    }
                    //System.out.println("edges: dim="+this.dimension+", edges="+edges.size());
                }
            }
            return edges;
        }
        
        
        public ArrayList<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<String>();
            if(this.dimension == 0) {
                vertices.add( "0" );
            }
            else {
                if(parent != null) {
                    ArrayList<String> parentVertices = parent.getVertices();
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
