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
 * PlatonicSolidGenerator.java
 *
 * Created on 12. joulukuuta 2007, 12:16
 *
 */

package org.wandora.application.tools.generators;

import java.util.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.utils.swing.GuiTools;

/**
 *
 * @author akivela
 */
public class PlatonicSolidGenerator extends AbstractGenerator implements WandoraTool {
    public static boolean connectWithWandoraClass = true;
    public static String PLATONIC_SOLID_GRAPH_SI = "http://wandora.org/si/platonic-solid";
    
    
    
    /** Creates a new instance of PlatonicSolidGenerator */
    public PlatonicSolidGenerator() {
    }

    @Override
    public String getName() {
        return "Platonic solid generator";
    }
    @Override
    public String getDescription() {
        return "Generates topic map graphs for platonic solids such as tetrahedron and cube.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,
            "Platonic solid generator",
            "Platonic solid generator creates topic map graphs for platonic solids. "+
            "Topic map graph consists of topics and associations. Topics are graph nodes and "+
            "associations graph edges. "+
            "Select one or more graphs to create.",
            true,new String[][]{
            new String[]{"Tetrahedron graph","boolean"},
            new String[]{"Cube graph","boolean"},
            new String[]{"Octahedron graph","boolean"},
            new String[]{"Dodecahedron graph","boolean"},
            new String[]{"Icosahedron graph","boolean"},
            new String[]{"---1","separator"},
            new String[]{"Connect topics with Wandora class","boolean", connectWithWandoraClass ? "true" : "false","Create additional topics and associations that connect created topics with the Wandora class." },
        },wandora);
        
        god.setSize(600, 400);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        try {
            connectWithWandoraClass = "true".equalsIgnoreCase(values.get("Connect topics with Wandora class"));        
        }
        catch(Exception e) {
            log(e);
            return;
        }
        
        setDefaultLogger();
        setLogTitle("Platonic solid generator");
        
        try {
            if("true".equals(values.get("Tetrahedron graph"))) {
                generateTetrahedron(topicmap);
            }
            if("true".equals(values.get("Cube graph"))) {
                generateCube(topicmap);
            }
            if("true".equals(values.get("Octahedron graph"))) {
                generateOctahedron(topicmap);
            }
            if("true".equals(values.get("Dodecahedron graph"))) {
                generateDodecahedron(topicmap);
            }
            if("true".equals(values.get("Icosahedron graph"))) {
                generateIcosahedron(topicmap);
            }
        }
        catch(Exception e) {
            log(e);
        }
        
        setState(WAIT);
    }
    
    
    
    
    
    public void generateTetrahedron(TopicMap topicmap) throws TopicMapException {
        String TETRAHEDRON_SI = "http://wandora.org/si/platonic-solid/tetrahedron/";
        
        log("Creating tetrahedron.");
        setProgressMax(4+6);
        int progress = 0;
        long graphIdentifier = System.currentTimeMillis();
        
        Topic[] nodes = new Topic[4];
        
        for(int i=0; i<4 && !forceStop(); i++) {
            nodes[i] = getOrCreateTopic(topicmap, TETRAHEDRON_SI+graphIdentifier+"/vertex/"+i, "Tetrahedron vertex "+i+" ("+graphIdentifier+")");
            setProgress(++progress);
            if(connectWithWandoraClass) {
                Topic graphTopic = getOrCreateTopic(topicmap, TETRAHEDRON_SI+graphIdentifier, "Tetrahedron graph "+graphIdentifier);
                connect(topicmap, nodes[i], graphTopic);
            }
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, TETRAHEDRON_SI+"edge", "Tetrahedron edge");
        Topic role1 = getOrCreateTopic(topicmap, TETRAHEDRON_SI+"role-1", "Tetrahedron role 1");
        Topic role2 = getOrCreateTopic(topicmap, TETRAHEDRON_SI+"role-2", "Tetrahedron role 2");
        Association a = null;
        for(int i=0; i<3 && !forceStop(); i++) {
            try {
                t1 = nodes[i];
                t2 = nodes[(i+1) % 3];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);
                
                t1 = nodes[i];
                t2 = nodes[3];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);
            }
            catch(Exception e) {
                log(e);
            }
        }
        log("Ready.");
    }
    

    
    
    public void generateCube(TopicMap topicmap) throws TopicMapException {
        String CUBE_SI = "http://wandora.org/si/platonic-solid/cube/";
        
        log("Creating cube.");
        setProgressMax(8+12);
        int progress = 0;
        long graphIdentifier = System.currentTimeMillis();
        
        Topic[] nodes = new Topic[8];
        
        for(int i=0; i<8 && !forceStop(); i++) {
            nodes[i] = getOrCreateTopic(topicmap, CUBE_SI+graphIdentifier+"/vertex/"+i, "Cube vertex "+i+" ("+graphIdentifier+")");
            setProgress(++progress);
            if(connectWithWandoraClass) {
                Topic graphTopic = getOrCreateTopic(topicmap, CUBE_SI+graphIdentifier, "Cube graph "+graphIdentifier);
                connect(topicmap, nodes[i], graphTopic);
            }
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, CUBE_SI+"edge", "Cube edge");
        Topic role1 = getOrCreateTopic(topicmap, CUBE_SI+"role-1", "Cube role 1");
        Topic role2 = getOrCreateTopic(topicmap, CUBE_SI+"role-2", "Cube role 2");
        Association a = null;
        for(int i=0; i<4 && !forceStop(); i++) {
            try {
                // ***** first rectangle
                t1 = nodes[i];
                t2 = nodes[(i+1) % 4];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);

                // ***** second rectangle
                t1 = nodes[4 + i];
                t2 = nodes[4 + ((i+1) % 4)];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);

                // ***** edge between first and second rectangle
                t1 = nodes[i];
                t2 = nodes[4 + ((i+1) % 4)];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);
            }
            catch(Exception e) {
                log(e);
            }
        }
        log("Ready.");
    }
    
    

    public void generateOctahedron(TopicMap topicmap) throws TopicMapException {
        String OCTAHEDRON_SI = "http://wandora.org/si/platonic-solid/octahedron/";
        
        log("Creating octahedron.");
        setProgressMax(6+12);
        int progress = 0;
        long graphIdentifier = System.currentTimeMillis();
        
        Topic[] nodes = new Topic[6];
        
        for(int i=0; i<6 && !forceStop(); i++) {
            setProgress(++progress);
            nodes[i] = getOrCreateTopic(topicmap, OCTAHEDRON_SI+graphIdentifier+"/vertex/"+i, "Octahedron vertex "+i+" ("+graphIdentifier+")");
            if(connectWithWandoraClass) {
                Topic graphTopic = getOrCreateTopic(topicmap, OCTAHEDRON_SI+graphIdentifier, "Octahedron graph "+graphIdentifier);
                connect(topicmap, nodes[i], graphTopic);
            }
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, OCTAHEDRON_SI+"edge", "Octahedron edge");
        Topic role1 = getOrCreateTopic(topicmap, OCTAHEDRON_SI+"role-1", "Octahedron role 1");
        Topic role2 = getOrCreateTopic(topicmap, OCTAHEDRON_SI+"role-2", "Octahedron role 2");
        Association a = null;
        for(int i=0; i<4 && !forceStop(); i++) {
            try {
                t1 = nodes[i];
                t2 = nodes[i+1==4 ? 0 : i+1];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);
                
                t1 = nodes[i];
                t2 = nodes[4];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);
                
                
                t1 = nodes[i];
                t2 = nodes[5];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);
            }
            catch(Exception e) {
                log(e);
            }
        }
        log("Ready.");
    }
    
    
    int[][] dodecahedronEdges = new int[][] {
        { 1,2 },
        { 2,3 },
        { 3,4 },
        { 4,5 },
        { 5,1 },
        { 1,14 },
        { 2,12 },
        { 3,10 },
        { 4,8 },
        { 5,6 },
        { 6,7 },
        { 7,8 },
        { 8,9 },
        { 9,10 },
        { 10,11 },
        { 11,12 },
        { 12,13 },
        { 13,14 },
        { 14,15 },
        { 15,6 },
        { 7,17 },
        { 9,18 },
        { 11,19 },
        { 13,20 },
        { 15,16 },
        { 16,17 },
        { 17,18 },
        { 18,19 },
        { 19,20 },
        { 20,16 },
    };
    
    
    public void generateDodecahedron(TopicMap topicmap) throws TopicMapException {
        String DODECAHEDRON_SI = "http://wandora.org/si/platonic-solid/dodecahedron/";
        
        log("Creating dodecahedron.");
        setProgressMax(20+30);
        int progress = 0;
        long graphIdentifier = System.currentTimeMillis();
        
        Topic[] nodes = new Topic[20];
        
        for(int i=0; i<20 && !forceStop(); i++) {
            setProgress(++progress);
            nodes[i] = getOrCreateTopic(topicmap, DODECAHEDRON_SI+graphIdentifier+"/vertex/"+i, "Dodecahedron vertex "+i+" ("+graphIdentifier+")");
            if(connectWithWandoraClass) {
                Topic graphTopic = getOrCreateTopic(topicmap, DODECAHEDRON_SI+graphIdentifier, "Dodecahedron graph "+graphIdentifier);
                connect(topicmap, nodes[i], graphTopic);
            }
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, DODECAHEDRON_SI+"edge", "Dodecahedron edge");
        Topic role1 = getOrCreateTopic(topicmap, DODECAHEDRON_SI+"role-1", "Dodecahedron role 1");
        Topic role2 = getOrCreateTopic(topicmap, DODECAHEDRON_SI+"role-2", "Dodecahedron role 2");
        Association a = null;
        for(int i=0; i<dodecahedronEdges.length && !forceStop(); i++) {
            try {
                t1 = nodes[dodecahedronEdges[i][0] - 1];
                t2 = nodes[dodecahedronEdges[i][1] - 1];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);
            }
            catch(Exception e) {
                log(e);
            }
        }
        log("Ready.");
    }
    
    
    int[][] icosahedronEdges = new int[][] {
        { 1,2 },
        { 1,6 },
        { 1,7 },
        { 1,8 },
        { 3,8 },
        { 3,9 },
        { 3,4 },
        { 2,4 },
        { 2,5 },
        { 2,6 },
        { 2,3 },
        { 3,1 },
        { 4,5 },
        { 5,6 },
        { 6,7 },
        { 7,8 },
        { 8,9 },
        { 9,4 },
        { 4,10 },
        { 5,10 },
        { 5,11 },
        { 6,11 },
        { 7,11 },
        { 7,12 },
        { 8,12 },
        { 9,12 },
        { 9,10 },
        { 10,11 },
        { 11,12 },
        { 12,10 }
    };

    public void generateIcosahedron(TopicMap topicmap) throws TopicMapException {
        String ICOSAHEDRON_SI = "http://wandora.org/si/platonic-solid/icosahedron/";
        
        log("Creating icosahedron.");
        setProgressMax(12+30);
        int progress = 0;
        long graphIdentifier = System.currentTimeMillis();
                
        Topic[] nodes = new Topic[12];
        
        for(int i=0; i<12 && !forceStop(); i++) {
            setProgress(++progress);
            nodes[i] = getOrCreateTopic(topicmap, ICOSAHEDRON_SI+graphIdentifier+"/vertex/"+i, "Icosahedron vertex "+i+" ("+graphIdentifier+")");
            if(connectWithWandoraClass) {
                Topic graphTopic = getOrCreateTopic(topicmap, ICOSAHEDRON_SI+graphIdentifier, "Icosahedron graph "+graphIdentifier);
                connect(topicmap, nodes[i], graphTopic);
            }
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, ICOSAHEDRON_SI+"edge", "Icosahedron edge");
        Topic role1 = getOrCreateTopic(topicmap, ICOSAHEDRON_SI+"role-1", "Icosahedron role 1");
        Topic role2 = getOrCreateTopic(topicmap, ICOSAHEDRON_SI+"role-2", "Icosahedron role 2");
        
        Association a = null;
        for(int i=0; i<icosahedronEdges.length && !forceStop(); i++) {
            try {
                t1 = nodes[icosahedronEdges[i][0] - 1];
                t2 = nodes[icosahedronEdges[i][1] - 1];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(++progress);
            }
            catch(Exception e) {
                log(e);
            }
        }
        log("Ready.");
    }
    
    
    

    private void connect(TopicMap topicmap, Topic vertexTopic, Topic platonicSolidInstance) {
        try {
            Topic platonicSolidType = getOrCreateTopic(topicmap, PLATONIC_SOLID_GRAPH_SI, "Platonic solid");
            Topic wandoraClass = getOrCreateTopic(topicmap, TMBox.WANDORACLASS_SI);
            makeSuperclassSubclass(topicmap, wandoraClass, platonicSolidType);
            platonicSolidInstance.addType(platonicSolidType);
            vertexTopic.addType(platonicSolidInstance);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
}
