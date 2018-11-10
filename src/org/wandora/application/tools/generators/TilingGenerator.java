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
import org.wandora.application.gui.WandoraOptionPane;
import static org.wandora.utils.Tuples.T2;
import org.wandora.utils.swing.GuiTools;


/**
 *
 * http://en.wikipedia.org/wiki/Tiling_by_regular_polygons
 * 
 * @author akivela
 */
public class TilingGenerator extends AbstractGenerator implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static String globalSiPattern = "";
    public static String globalBasenamePattern = "";
    public static boolean connectWithWandoraClass = true;
    
    
    /** Creates a new instance of TilingGenerator */
    public TilingGenerator() {
    }


    @Override
    public String getName() {
        return "Tiling graph generator";
    }
    
    @Override
    public String getDescription() {
        return "Tiling graph generator creates simple graphs that resemble plane tilings by regular polygons.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,
            "Tiling graph generator",
            "Tiling graph generator creates simple graphs that resemble plane tilings by regular polygons. "+
            "Created tilings consist of topics and associations. Topics can be thought as tiling vertices and "+
            "associations as tiling edges. Select the type and size of created tiling below. Optionally you "+
            "can set the name and subject identifier patterns for vertex topics as well as the assocation type and "+
            "roles of tiling graph edges. Connecting topics with Wandora class creates some additional topics and "+
            "associations that link the tiling graph with Wandora class topic.",
            true,new String[][]{
            new String[]{"Create square tiling","boolean"},
            new String[]{"Width of square tiling","string"},
            new String[]{"Height of square tiling","string"},
            
            new String[]{"---1","separator"},
            new String[]{"Create triangular tiling","boolean"},
            new String[]{"Depth of triangular tiling","string"},
            
            new String[]{"---2","separator"},
            new String[]{"Create hexagonal tiling","boolean"},
            new String[]{"Depth of hexagonal tiling","string"},
            
            new String[]{"---3","separator"},
            new String[]{"Subject identifier pattern","string",globalSiPattern,"Subject identifier patterns for the created node topics. Part __n__ in patterns is replaced with vertex identifier."},
            new String[]{"Basename pattern","string",globalBasenamePattern,"Basename patterns for the created node topics. Part __n__ in patterns is replaced with vertex identifier."},
            new String[]{"Connect topics with Wandora class","boolean", connectWithWandoraClass ? "true" : "false","Create additional topics and associations that connect created topics with the Wandora class." },
            new String[]{"Association type topic","topic",null,"Optional association type for graph edges."},
            new String[]{"First role topic","topic",null,"Optional role topic for graph edges."},
            new String[]{"Second role topic","topic",null,"Optional role topic for graph edges."},
        },wandora);
        
        god.setSize(700, 620);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> optionsValues = god.getValues();
        
        try {
            globalSiPattern = optionsValues.get("Subject identifier pattern");
            if(globalSiPattern != null && globalSiPattern.trim().length() > 0) {
                if(!globalSiPattern.contains("__n__")) {
                    int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Subject identifier pattern doesn't contain part for topic counter '__n__'. This causes all generated topics to merge. Do you want to use it?", "Missing topic counter part", WandoraOptionPane.WARNING_MESSAGE);
                    if(a != WandoraOptionPane.YES_OPTION) globalSiPattern = null;
                }
            }
            globalBasenamePattern = optionsValues.get("Basename pattern");
            if(globalBasenamePattern != null && globalBasenamePattern.trim().length() > 0) {
                if(!globalBasenamePattern.contains("__n__")) {
                    int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Basename pattern doesn't contain part for topic counter '__n__'. This causes all generated topics to merge. Do you want to use it?", "Missing topic counter part", WandoraOptionPane.WARNING_MESSAGE);
                    if(a != WandoraOptionPane.YES_OPTION) globalBasenamePattern = null;
                }
            }
            connectWithWandoraClass = "true".equalsIgnoreCase(optionsValues.get("Connect topics with Wandora class"));
        }
        catch(Exception e) {
            log(e);
        }
        
        Tiling tiling = null;
        
        int width = 0;
        int height = 0;
        int depth = 0;
        boolean tilingSelected = false;

        setDefaultLogger();
        setLogTitle("Tiling graph generator");
        
        try {
            if("true".equals(optionsValues.get("Create square tiling"))) {
                try {
                    tilingSelected = true;
                    width = Integer.parseInt(optionsValues.get("Width of square tiling"));
                    height = Integer.parseInt(optionsValues.get("Height of square tiling"));
                    tiling = new SquareTiling(width, height);
                    makeTiling(tiling, topicmap, optionsValues);
                }
                catch(NumberFormatException nfe) {
                    log("Square tiling width and height should be positive integer numbers.");
                }
            }
            if("true".equals(optionsValues.get("Create triangular tiling"))) {
                try {
                    tilingSelected = true;
                    depth = Integer.parseInt(optionsValues.get("Depth of triangular tiling"));
                    tiling = new TriangularTiling(depth);
                    makeTiling(tiling, topicmap, optionsValues);
                }
                catch(NumberFormatException nfe) {
                    log("Triangular tiling depth should be positive integer number.");
                }
            }
            if("true".equals(optionsValues.get("Create hexagonal tiling"))) {
                try {
                    tilingSelected = true;
                    depth = Integer.parseInt(optionsValues.get("Depth of hexagonal tiling"));
                    tiling = new HexagonalTiling(depth);
                    makeTiling(tiling, topicmap, optionsValues);
                }
                catch(NumberFormatException nfe) {
                    log("Hexagonal tiling depth should be positive integer number.");
                }
            }
            if(!tilingSelected) {
                log("No tiling selected.");
            }
        }
        catch(Exception e) {
            log(e);
        }

        log("Ready.");
        setState(WAIT);
    }
    
    
    
    public void makeTiling(Tiling tiling, TopicMap topicmap, Map<String,String> optionsValues) {
        if(tiling != null) {
            int progress = 0;
            Collection<T2<String,String>> edges = tiling.getEdges();

            log("Creating "+tiling.getName()+" tiling graph.");

            Topic atype = tiling.getAssociationTypeTopic(topicmap,optionsValues);
            Topic role1 = tiling.getRole1Topic(topicmap,optionsValues);
            Topic role2 = tiling.getRole2Topic(topicmap,optionsValues);
            Association a = null;
            Topic node1 = null;
            Topic node2 = null;

            if(edges.size() > 0) {
                setProgressMax(edges.size());
                for(T2<String,String> edge : edges) {
                    if(edge != null) {
                        node1 = tiling.getVertexTopic(edge.e1, topicmap, optionsValues);
                        node2 = tiling.getVertexTopic(edge.e2, topicmap, optionsValues);
                        if(node1 != null && node2 != null) {
                            try {
                                a = topicmap.createAssociation(atype);
                                a.addPlayer(node1, role1);
                                a.addPlayer(node2, role2);
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        setProgress(progress++);
                    }
                }
                if(connectWithWandoraClass) {
                    log("You'll find created topics under the '"+tiling.getName()+" graph' topic.");
                }
                else {
                    String searchWord = tiling.getName();
                    if(globalBasenamePattern != null && globalBasenamePattern.trim().length() > 0) {
                        searchWord = globalBasenamePattern.replaceAll("__n__", "");
                        searchWord = searchWord.trim();
                    }
                    log("You'll find created topics by searching with a '"+searchWord+"'.");
                }
            }
            else {
                log("Number of tiling edges is zero. Tiling has no nodes either.");
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------------- TILINGS ---
    // -------------------------------------------------------------------------
    
    
    public interface Tiling {
        public String getSIPrefix();
        public String getName();
        public int getSize();
        public Collection<T2<String,String>> getEdges();
        public Collection<String> getVertices();
        public Topic getVertexTopic(String vertex, TopicMap topicmap, Map<String,String> optionsValues);
        public Topic getAssociationTypeTopic(TopicMap topicmap, Map<String,String> optionsValues);
        public Topic getRole1Topic(TopicMap topicmap, Map<String,String> optionsValues);
        public Topic getRole2Topic(TopicMap topicmap, Map<String,String> optionsValues);
    }
    
    
    
    public abstract class AbstractTiling implements Tiling {

        @Override
        public Topic getVertexTopic(String vertex, TopicMap topicmap, Map<String,String> optionsValues) {
            String newBasename = getName()+" vertex "+vertex;
            if(globalBasenamePattern != null && globalBasenamePattern.trim().length() > 0) {
                newBasename = globalBasenamePattern.replaceAll("__n__", vertex);
            }
            
            String newSubjectIdentifier = getSIPrefix()+"vertex-"+vertex;
            if(globalSiPattern != null && globalSiPattern.trim().length() > 0) {
                newSubjectIdentifier = globalSiPattern.replaceAll("__n__", vertex);
            }
      
            Topic t = getOrCreateTopic(topicmap, newSubjectIdentifier, newBasename);
            if(connectWithWandoraClass) {
                try {
                    Topic graphTopic = getOrCreateTopic(topicmap, getSIPrefix(), getName()+" graph");
                    Topic wandoraClass = getOrCreateTopic(topicmap, TMBox.WANDORACLASS_SI);
                    makeSuperclassSubclass(topicmap, wandoraClass, graphTopic);
                    t.addType(graphTopic);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            return t;
        }
        
        @Override
        public Topic getAssociationTypeTopic(TopicMap topicmap, Map<String,String> optionsValues) {
            String atypeStr = null;
            Topic atype = null;
            if(optionsValues != null) {
                atypeStr = optionsValues.get("Association type topic");
            }
            if(atypeStr != null) {
                try {
                    atype = topicmap.getTopic(atypeStr);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if(atype == null) {
                atype = getOrCreateTopic(topicmap, getSIPrefix()+"edge", getName()+" edge");
            }
            return atype;
        }
        
        
        @Override
        public Topic getRole1Topic(TopicMap topicmap, Map<String,String> optionsValues) {
            String roleStr = null;
            Topic role = null;
            if(optionsValues != null) {
                roleStr = optionsValues.get("First role topic");
            }
            if(roleStr != null) {
                try {
                    role = topicmap.getTopic(roleStr);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if(role == null) {
                role = getOrCreateTopic(topicmap, getSIPrefix()+"role-1", "role 1");
            }
            return role;
        }
        

        @Override
        public Topic getRole2Topic(TopicMap topicmap, Map<String,String> optionsValues) {
            String roleStr = null;
            Topic role = null;
            if(optionsValues != null) {
                roleStr = optionsValues.get("Second role topic");
            }
            if(roleStr != null) {
                try {
                    role = topicmap.getTopic(roleStr);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if(role == null) {
                role = getOrCreateTopic(topicmap, getSIPrefix()+"role-2", "role 2");
            }
            return role;
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    private class SquareTiling extends AbstractTiling implements Tiling {
        private int size = 0;
        private int width = 0;
        private int height = 0;
        
        
        public SquareTiling(int w, int h) {
            this.width = w;
            this.height = h;
            this.size = w*h;
        }
        
        @Override
        public String getSIPrefix() {
            return "http://wandora.org/si/tiling/square/";
        }
        
        @Override
        public String getName() {
            return "Square tiling";
        }

        
        
        @Override
        public int getSize() {
            return size;
        }
        
    
        @Override
        public Collection<T2<String,String>> getEdges() {
            ArrayList<T2<String,String>> edges = new ArrayList<>();
            int w = width-1;
            int h = height-1;
            
            for(int x=0; x<w; x++) {
                for(int y=0; y<h; y++) {
                    edges.add(new T2<>(x+"-"+y, x+"-"+(y+1)));
                    edges.add(new T2<>(x+"-"+y, (x+1)+"-"+y));
                }
            }
            for(int x=0; x<w; x++) {
                edges.add(new T2<>(x+"-"+h, (x+1)+"-"+h));
            }
            for(int y=0; y<h; y++) {
                edges.add(new T2<>(w+"-"+y, w+"-"+(y+1)));
            }
            return edges;
        }

        @Override
        public Collection<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<>();
            for(int x=0; x<width; x++) {
                for(int y=0; y<height; y++) {
                    vertices.add(x+"-"+y);
                }
            }
            return vertices;
        }
        
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private class TriangularTiling extends AbstractTiling implements Tiling {
        private int depth = 0;
        
        
        public TriangularTiling(int d) {
            this.depth = d;
        }
        
        @Override
        public String getSIPrefix() {
            return "http://wandora.org/si/tiling/triangular/";
        }
        
        @Override
        public String getName() {
            return "Triangular tiling";
        }
        
        @Override
        public int getSize() {
            int size = 0;
            for(int d=0; d<depth; d++) {
                for(int f=0; f<d; f++) {
                    size++;
                }
            }
            return size;
        }
        
        @Override
        public Collection<T2<String,String>> getEdges() {
            ArrayList<T2<String,String>> edges = new ArrayList<>();

            for(int d=0; d<depth; d++) {
                for(int f=0; f<d; f++) {
                    edges.add(new T2<>(d+"-"+f, (d-1)+"-"+f));
                    edges.add(new T2<>(d+"-"+f, d+"-"+(f+1)));
                    edges.add(new T2<>((d-1)+"-"+f, d+"-"+(f+1)));
                }
            }
            return edges;
        }

        @Override
        public Collection<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<>();
            for(int d=0; d<depth; d++) {
                for(int f=0; f<d; f++) {
                    vertices.add(d+"-"+f);
                }
            }
            return vertices;
        }
        

    }
    
    

    // -------------------------------------------------------------------------
    
    
    
    private class HexagonalTiling extends AbstractTiling implements Tiling {
        private int depth = 0;
        
        public HexagonalTiling(int d) {
            this.depth = d;
        }
        
        @Override
        public String getSIPrefix() {
            return "http://wandora.org/si/tiling/hexagonal/";
        }
        
        @Override
        public String getName() {
            return "Hexagonal tiling";
        }

        @Override
        public int getSize() {
            int size = 0;
            for(int d=0; d<depth; d++) {
                for(int f=0; f<d; f++) {
                    size++;
                }
            }
            return size;
        }
        
        @Override
        public Collection<T2<String,String>> getEdges() {
            ArrayList<T2<String,String>> edges = new ArrayList<>();
            String nc = null;
            String n1 = null;
            String n2 = null;
            String n3 = null;

            for(int d=0; d<depth; d++) {
                for(int f=0; f<d+1; f++) {
                    nc = d+"-"+f+"-c";
                    n2 = d+"-"+f;
                    n3 = d+"-"+(f+1);

                    edges.add(new T2<>(nc, n2));
                    edges.add(new T2<>(nc, n3));

                    if(d == 0) {
                        n1 = d+"-"+f+"-t";
                        edges.add(new T2<>(nc, n1));
                    }
                    else {
                        n1 = (d-1)+"-"+f;
                        edges.add(new T2<>(nc, n1));
                    }
                }
            }
            return edges;
        }
        
        @Override
        public Collection<String> getVertices() {
            HashSet<String> vertices = new LinkedHashSet<>();
            for(int d=0; d<depth; d++) {
                for(int f=0; f<d+1; f++) {
                    vertices.add( d+"-"+f+"-c" );
                    vertices.add( d+"-"+f );
                    vertices.add( d+"-"+(f+1) );

                    if(d == 0) {
                        vertices.add( d+"-"+f+"-t" );
                    }
                    else {
                        vertices.add( (d-1)+"-"+f );
                    }
                }
            }
            return vertices;
        }
    }
}
