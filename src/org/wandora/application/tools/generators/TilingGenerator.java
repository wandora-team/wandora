/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * TilingGenerator.java
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
 * http://en.wikipedia.org/wiki/Tiling_by_regular_polygons
 * 
 * @author akivela
 */
public class TilingGenerator extends AbstractGenerator implements WandoraTool {
    
    
    /** Creates a new instance of TilingGenerator */
    public TilingGenerator() {
    }


    @Override
    public String getName() {
        return "Tiling graph generator";
    }
    
    @Override
    public String getDescription() {
        return "Generates tiling graph topic maps";
    }
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,
            "Tiling graph generator",
            "Tiling graph generator creates tiling abstractions with topic map structures.",
            true,new String[][]{
            new String[]{"Create square tiling","boolean"},
            new String[]{"Width of square tiling","string"},
            new String[]{"Height of square tiling","string"},
            
            new String[]{"Create triangular tiling","boolean"},
            new String[]{"Depth of triangular tiling","string"},
            
            new String[]{"Create hexagonal tiling","boolean"},
            new String[]{"Depth of hexagonal tiling","string"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        Tiling tiling = null;
        int progress = 0;
        int width = 0;
        int height = 0;
        int depth = 0;
        try {
            if("true".equals(values.get("Create square tiling"))) {
                width = Integer.parseInt(values.get("Width of square tiling"));
                height = Integer.parseInt(values.get("Height of square tiling"));
                tiling = new SquareTiling(width, height);
            }
            else if("true".equals(values.get("Create triangular tiling"))) {
                depth = Integer.parseInt(values.get("Depth of triangular tiling"));
                tiling = new TriangularTiling(depth);
            }
            else if("true".equals(values.get("Create hexagonal tiling"))) {
                depth = Integer.parseInt(values.get("Depth of hexagonal tiling"));
                tiling = new HexagonalTiling(depth);
            }
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        if(tiling == null) {
            singleLog("No tiling type selected! Aborting!");
            return;
        }
        
        setDefaultLogger();
        setLogTitle("Tiling graph generator");
                       
        ArrayList<T2> edges = tiling.getEdges();
        
        log("Creating "+tiling.getName()+" graph");
        
        Topic atype = getOrCreateTopic(topicmap, tiling.getSIPrefix()+"edge", tiling.getName()+" edge");
        Topic role1 = getOrCreateTopic(topicmap, tiling.getSIPrefix()+"role1", "role1");
        Topic role2 = getOrCreateTopic(topicmap, tiling.getSIPrefix()+"role2", "role2");
        Association a = null;
        Topic node1 = null;
        Topic node2 = null;
        
        if(edges.size() > 0) {
            setProgressMax(edges.size());
            for(T2 edge: edges) {
                if(edge != null) {
                    node1 = getOrCreateTopic(topicmap, tiling.getSIPrefix()+"vertex-"+edge.e1, tiling.getName()+" "+edge.e1);
                    node2 = getOrCreateTopic(topicmap, tiling.getSIPrefix()+"vertex-"+edge.e2, tiling.getName()+" "+edge.e2);
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
    
    
    private interface Tiling {
        public String getSIPrefix();
        public String getName();
        public int getSize();
        public ArrayList<T2> getEdges();
        public ArrayList<String> getVertices();
    }
    
    // -------------------------------------------------------------------------
    
    
    private class SquareTiling implements Tiling {
        private int size = 0;
        private int width = 0;
        private int height = 0;
        
        
        public SquareTiling(int w, int h) {
            this.width = w;
            this.height = h;
            this.size = w*h;
        }
        
        public String getSIPrefix() {
            return "http://wandora.org/si/tiling/square/";
        }
        
        public String getName() {
            return "square-tiling";
        }

        
        
        public int getSize() {
            return size;
        }
        
    
        public ArrayList<T2> getEdges() {
            ArrayList<T2> edges = new ArrayList<T2>();
            int w = width-1;
            int h = height-1;
            
            for(int x=0; x<w; x++) {
                for(int y=0; y<h; y++) {
                    edges.add(new T2(x+"-"+y, x+"-"+(y+1)));
                    edges.add(new T2(x+"-"+y, (x+1)+"-"+y));
                }
            }
            for(int x=0; x<w; x++) {
                edges.add(new T2(x+"-"+h, (x+1)+"-"+h));
            }
            for(int y=0; y<h; y++) {
                edges.add(new T2(w+"-"+y, w+"-"+(y+1)));
            }
            return edges;
        }
        
        
        public ArrayList<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<String>();
            for(int x=0; x<width; x++) {
                for(int y=0; y<height; y++) {
                    vertices.add(x+"-"+y);
                }
            }
            return vertices;
        }
        
        
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private class TriangularTiling implements Tiling {
        private int depth = 0;
        
        
        public TriangularTiling(int d) {
            this.depth = d;
        }
        
        public String getSIPrefix() {
            return "http://wandora.org/si/tiling/triangular/";
        }
        
        public String getName() {
            return "triangular-tiling";
        }

        
        
        public int getSize() {
            int size = 0;
            for(int d=0; d<depth; d++) {
                for(int f=0; f<d; f++) {
                    size++;
                }
            }
            return size;
        }
        
    
        public ArrayList<T2> getEdges() {
            ArrayList<T2> edges = new ArrayList<T2>();

            for(int d=0; d<depth; d++) {
                for(int f=0; f<d; f++) {
                    edges.add(new T2(d+"-"+f, (d-1)+"-"+f));
                    edges.add(new T2(d+"-"+f, d+"-"+(f+1)));
                    edges.add(new T2((d-1)+"-"+f, d+"-"+(f+1)));
                }
            }
            return edges;
        }
        
        
        public ArrayList<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<String>();
            for(int d=0; d<depth; d++) {
                for(int f=0; f<d; f++) {
                    vertices.add(d+"-"+f);
                }
            }
            return vertices;
        }
        
    }
    
    

    // -------------------------------------------------------------------------
    
    
    
    private class HexagonalTiling implements Tiling {
        private int depth = 0;
        
        
        public HexagonalTiling(int d) {
            this.depth = d;
        }
        
        public String getSIPrefix() {
            return "http://wandora.org/si/tiling/hexagonal/";
        }
        
        public String getName() {
            return "hexagonal-tiling";
        }

        
        
        public int getSize() {
            int size = 0;
            for(int d=0; d<depth; d++) {
                for(int f=0; f<d; f++) {
                    size++;
                }
            }
            return size;
        }
        
    
        public ArrayList<T2> getEdges() {
            ArrayList<T2> edges = new ArrayList<T2>();
            String nc = null;
            String n1 = null;
            String n2 = null;
            String n3 = null;

            for(int d=0; d<depth; d++) {
                for(int f=0; f<d+1; f++) {
                    nc = d+"-"+f+"-c";
                    n2 = d+"-"+f;
                    n3 = d+"-"+(f+1);

                    edges.add(new T2(nc, n2));
                    edges.add(new T2(nc, n3));

                    if(d == 0) {
                        n1 = d+"-"+f+"-t";
                        edges.add(new T2(nc, n1));
                    }
                    else {
                        n1 = (d-1)+"-"+f;
                        edges.add(new T2(nc, n1));
                    }
                }
            }
            return edges;
        }
        
        
        public ArrayList<String> getVertices() {
            HashSet<String> verticesSet = new HashSet<String>();
            for(int d=0; d<depth; d++) {
                for(int f=0; f<d+1; f++) {
                    verticesSet.add( d+"-"+f+"-c" );
                    verticesSet.add( d+"-"+f );
                    verticesSet.add( d+"-"+(f+1) );

                    if(d == 0) {
                        verticesSet.add( d+"-"+f+"-t" );
                    }
                    else {
                        verticesSet.add( (d-1)+"-"+f );
                    }
                }
            }
            ArrayList<String> vertices = new ArrayList<String>();
            vertices.addAll(verticesSet);
            return vertices;
        }
    }
}
