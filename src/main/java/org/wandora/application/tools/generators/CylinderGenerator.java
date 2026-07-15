/*
 * WANDORA Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2023 Wandora Team
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * CylinderGenerator.java
 *
 * Created on 2012-05-11
 *
 */

package org.wandora.application.tools.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
 * http://en.wikipedia.org/wiki/Tiling_by_regular_polygons
 *
 * @author elehtonen
 */
public class CylinderGenerator extends AbstractGenerator implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static String globalSiPattern = "";
    public static String globalBasenamePattern = "";
    public static boolean connectWithWandoraClass = true;
    
    
    /**
     * Creates a new instance of Cylinder Generator
     */
    public CylinderGenerator() {
    }

    @Override
    public String getName() {
        return "Cylinder graph generator";
    }

    @Override
    public String getDescription() {
        return "Generates cylinder graph topic maps";
    }

    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);

        GenericOptionsDialog god = new GenericOptionsDialog(wandora,
            "Cylinder graph generator",
            "Cylinder graph generator creates simple graphs that resemble cylinders created with regular polygons. "+
            "Created cylinders consist of topics and associations. Topics can be thought as cylinder vertices and "+
            "associations as cylinder edges. Select the type and size of created tiling below. Optionally you "+
            "can set the name and subject identifier patterns for vertex topics as well as the assocation type and "+
            "roles of cylinder graph edges. Connecting topics with Wandora class creates some additional topics and "+
            "associations that link the cylinder graph with Wandora class topic.",
            true, new String[][]{
                new String[]{"Create a cylinder with square tiling", "boolean"},
                new String[]{"Create a cylinder with triangular tiling", "boolean"},
                new String[]{"Create a cylinder with hexagonal tiling", "boolean"},
                new String[]{"Width of cylinder", "string"},
                new String[]{"Height of cylinder", "string"},
                new String[]{"Toroid", "boolean"},

                new String[]{"---3","separator"},
                new String[]{"Subject identifier pattern","string",globalSiPattern,"Subject identifier patterns for the created node topics. Part __n__ in patterns is replaced with vertex identifier."},
                new String[]{"Basename pattern","string",globalBasenamePattern,"Basename patterns for the created node topics. Part __n__ in patterns is replaced with vertex identifier."},
                new String[]{"Connect topics with Wandora class","boolean", connectWithWandoraClass ? "true" : "false","Create additional topics and associations that connect created topics with the Wandora class." },
                new String[]{"Association type topic","topic",null,"Optional association type for graph edges."},
                new String[]{"First role topic","topic",null,"Optional role topic for graph edges."},
                new String[]{"Second role topic","topic",null,"Optional role topic for graph edges."},
            }, 
            wandora);
        
        god.setSize(700, 620);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if (god.wasCancelled()) {
            return;
        }
        Map<String, String> values = god.getValues();

        try {
            globalSiPattern = values.get("Subject identifier pattern");
            if(globalSiPattern != null && globalSiPattern.trim().length() > 0) {
                if(!globalSiPattern.contains("__n__")) {
                    int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Subject identifier pattern doesn't contain part for topic counter '__n__'. This causes all generated topics to merge. Do you want to use it?", "Missing topic counter part", WandoraOptionPane.WARNING_MESSAGE);
                    if(a != WandoraOptionPane.YES_OPTION) globalSiPattern = null;
                }
            }
            globalBasenamePattern = values.get("Basename pattern");
            if(globalBasenamePattern != null && globalBasenamePattern.trim().length() > 0) {
                if(!globalBasenamePattern.contains("__n__")) {
                    int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Basename pattern doesn't contain part for topic counter '__n__'. This causes all generated topics to merge. Do you want to use it?", "Missing topic counter part", WandoraOptionPane.WARNING_MESSAGE);
                    if(a != WandoraOptionPane.YES_OPTION) globalBasenamePattern = null;
                }
            }
            connectWithWandoraClass = "true".equalsIgnoreCase(values.get("Connect topics with Wandora class"));
        }
        catch(Exception e) {
            log(e);
        }
        
        
        ArrayList<Cylinder> cylinders = new ArrayList<>();

        int progress = 0;
        int width = 0;
        int height = 0;
        boolean toggleToroid = false;
        try {
            toggleToroid = "true".equals(values.get("Toroid"));
            width = Integer.parseInt(values.get("Width of cylinder"));
            height = Integer.parseInt(values.get("Height of cylinder"));
            if ("true".equals(values.get("Create a cylinder with square tiling"))) {
                cylinders.add(new SquareCylinder(width, height, toggleToroid));
            }
            if ("true".equals(values.get("Create a cylinder with triangular tiling"))) {
                cylinders.add(new TriangularCylinder(width, height, toggleToroid));
            }
            if ("true".equals(values.get("Create a cylinder with hexagonal tiling"))) {
                cylinders.add(new HexagonalCylinder(width, height, toggleToroid));
            }
        } 
        catch (Exception e) {
            singleLog(e);
            return;
        }
        
        setDefaultLogger();
        setLogTitle("Cylinder graph generator");

        for (Cylinder cylinder : cylinders) {
            Collection<T2<String,String>> edges = cylinder.getEdges();

            log("Creating " + cylinder.getName() + " graph");

            Topic atype = cylinder.getAssociationTypeTopic(topicmap,values);
            Topic role1 = cylinder.getRole1Topic(topicmap,values);
            Topic role2 = cylinder.getRole2Topic(topicmap,values);
            
            Association a = null;
            Topic node1 = null;
            Topic node2 = null;

            if (edges.size() > 0) {
                setProgressMax(edges.size());
                for (T2<String,String> edge : edges) {
                    if (edge != null) {
                        node1 = cylinder.getVertexTopic(edge.e1, topicmap, values);
                        node2 = cylinder.getVertexTopic(edge.e2, topicmap, values);
                        if (node1 != null && node2 != null) {
                            a = topicmap.createAssociation(atype);
                            a.addPlayer(node1, role1);
                            a.addPlayer(node2, role2);
                        }
                        setProgress(progress++);
                    }
                }
                
                if(connectWithWandoraClass) {
                    log("You'll find created topics under the '"+cylinder.getName()+" graph' topic.");
                }
                else {
                    String searchWord = cylinder.getName();
                    if(globalBasenamePattern != null && globalBasenamePattern.trim().length() > 0) {
                        searchWord = globalBasenamePattern.replaceAll("__n__", "");
                        searchWord = searchWord.trim();
                    }
                    log("You'll find created topics by searching with a '"+searchWord+"'.");
                }
            }
            else {
                log("Number of cylinder edges is zero. Cylinder has no vertices neithers.");
            }
        }

        if(cylinders.isEmpty()) {
            log("No cylinder selected.");
        }
        log("Ready.");
        setState(WAIT);
    }

    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------------- CYLINDERS ---
    // -------------------------------------------------------------------------
    
    
    public interface Cylinder {
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
    
    
    public abstract class AbstractCylinder implements Cylinder {

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
    
    
    public class SquareCylinder extends AbstractCylinder implements Cylinder {

        private int size = 0;
        private int width = 0;
        private int height = 0;
        private boolean isToroid = false;

        public SquareCylinder(int w, int h, boolean toroid) {
            this.width = w;
            this.height = h;
            this.size = w * h;
            this.isToroid = toroid;
        }

        @Override
        public String getSIPrefix() {
            return "http://wandora.org/si/cylinder/square/";
        }

        @Override
        public String getName() {
            return "Square-cylinder";
        }

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public Collection<T2<String,String>> getEdges() {
            ArrayList<T2<String,String>> edges = new ArrayList<>();

            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    int ww = (w == width - 1) ? 0 : (w + 1);
                    int hh = (h == height - 1 && isToroid) ? 0 : (h + 1);
                    edges.add(new T2<String,String>(h + "-" + w, h + "-" + ww));
                    edges.add(new T2<String,String>((h + "-" + ww), hh + "-" + ww));
                }
            }
            if (!this.isToroid) {
                for (int w = 0; w < width; w++) {
                    int ww = (w != width - 1) ? (w + 1) : 0;
                    edges.add(new T2<String,String>(height + "-" + w, height + "-" + ww));
                }
            }
            return edges;
        }

        @Override
        public Collection<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<>();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    vertices.add(x + "-" + y);
                }
            }
            return vertices;
        }
    }

    
    
    // -------------------------------------------------------------------------
    
    
    public class TriangularCylinder extends AbstractCylinder implements Cylinder {

        private int depth = 0;
        private int width = 0;
        private boolean isToroid = false;

        public TriangularCylinder(int w, int d, boolean toroid) {
            this.width = w;
            this.depth = d;
            this.isToroid = toroid;
        }

        @Override
        public String getSIPrefix() {
            return "http://wandora.org/si/cylinder/triangular/";
        }

        @Override
        public String getName() {
            return "Triangular-cylinder";
        }

        @Override
        public int getSize() {
            int size = 0;
            for (int d = 0; d < depth; d++) {
                for (int f = 0; f < d; f++) {
                    size++;
                } 
           }
            return size;
        }

        @Override
        public Collection<T2<String,String>> getEdges() {
            ArrayList<T2<String,String>> edges = new ArrayList<>();

            for (int d = 0; d < depth; d++) {
                for (int w = 0; w < width; w++) {
                    int ww = (w == width - 1) ? 0 : (w + 1);
                    int dd = (d == depth - 1 && this.isToroid) ? 0 : (d + 1);
                    edges.add(new T2<String,String>(d + "-" + w, d + "-" + ww));
                    edges.add(new T2<String,String>(d + "-" + w, dd + "-" + ww));
                    edges.add(new T2<String,String>(d + "-" + ww, dd + "-" + ww));
                }
            }

            if (!this.isToroid) {
                for (int w = 0; w < width; w++) {
                    int ww = (w != width - 1) ? (w + 1) : 0;
                    edges.add(new T2<String,String>(depth + "-" + w, depth + "-" + ww));
                }
            }

            return edges;
        }

        @Override
        public Collection<String> getVertices() {
            ArrayList<String> vertices = new ArrayList<>();
            for (int d = 0; d < depth; d++) {
                for (int f = 0; f < d; f++) {
                    vertices.add(d + "-" + f);
                }
            }
            return vertices;
        }
    }

    
    // -------------------------------------------------------------------------
    
    
    public class HexagonalCylinder extends AbstractCylinder implements Cylinder {

        private int depth = 0;
        private int width = 0;
        private boolean isToroid = false;

        public HexagonalCylinder(int w, int d, boolean toroid) {
            this.width = w;
            this.depth = d;
            this.isToroid = toroid;
        }

        @Override
        public String getSIPrefix() {
            return "http://wandora.org/si/cylinder/hexagonal/";
        }

        @Override
        public String getName() {
            return "Hexagonal-cylinder";
        }

        @Override
        public int getSize() {
            int size = 0;
            for (int d = 0; d < depth; d++) {
                for (int f = 0; f < d; f++) {
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

            for (int d = 0; d < depth; d++) {
                for (int w = 0; w < width; w++) {
                    
                    nc = d + "-" + w + "-c";
                    n1 = (d == depth -1 && this.isToroid) ? 0 + "-" + w : (d+1) + "-" + w ;
                    n2 = d + "-" + w;

                    edges.add(new T2<String,String>(nc, n1));
                    edges.add(new T2<String,String>(nc, n2));

                    n3 = (w == width - 1) ? d + "-" + 0 : d + "-" + (w + 1); 

                    edges.add(new T2<String,String>(nc, n3));

                }
            }

            if (!this.isToroid) {
                for (int w = 0; w < width; w++) {
                    nc = depth + "-" + w + "-c";
                    n1 = depth + "-" + w;
                    n2 = (w == width -1 ) ? depth + "-" + 0 : depth + "-" + (w + 1);

                    edges.add(new T2<String,String>(nc, n1));
                    edges.add(new T2<String,String>(nc, n2));
                }
            }

            return edges;
        }

        @Override
        public Collection<String> getVertices() {
            HashSet<String> verticesSet = new LinkedHashSet<>();
            for (int d = 0; d < depth; d++) {
                for (int f = 0; f < d + 1; f++) {
                    verticesSet.add(d + "-" + f + "-c");
                    verticesSet.add(d + "-" + f);
                    verticesSet.add(d + "-" + (f + 1));

                    if (d == 0) {
                        verticesSet.add(d + "-" + f + "-t");
                    } else {
                        verticesSet.add((d - 1) + "-" + f);
                    }
                }
            }
            return verticesSet;
        }
    }
}
