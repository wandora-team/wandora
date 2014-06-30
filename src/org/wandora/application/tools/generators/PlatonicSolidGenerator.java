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
 * PlatonicSolidGenerator.java
 *
 * Created on 12. joulukuuta 2007, 12:16
 *
 */

package org.wandora.application.tools.generators;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public class PlatonicSolidGenerator extends AbstractGenerator implements WandoraTool {
    
    /** Creates a new instance of PlatonicSolidGenerator */
    public PlatonicSolidGenerator() {
    }

    @Override
    public String getName() {
        return "Platonic solid generator";
    }
    @Override
    public String getDescription() {
        return "Generates topic map graphs for platonic solids i.e. convex regular polyhedrons such as tetrahedron and cube.";
    }
    
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(admin,
            "Platonic solid generator",
            "Platonic solid generator creates a topic map graph for platonic solids."+
            "Select one or more graphs to create.",
            true,new String[][]{
            new String[]{"Tetrahedron graph","boolean"},
            new String[]{"Cube graph","boolean"},
            new String[]{"Octahedron graph","boolean"},
            new String[]{"Dodecahedron graph","boolean"},
            new String[]{"Icosahedron graph","boolean"},
        },admin);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
       
        setDefaultLogger();
        setLogTitle("Platonic solid generator");
        
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
        
        setState(WAIT);
    }
    
    
    
    
    
    public void generateTetrahedron(TopicMap topicmap) {
        String SI_PREFIX = "http://wandora.org/si/tetrahedron/";
        
        log("Creating Tetrahedron");
        setProgressMax(4+6);
        int progress = 0;
        
        Topic[] nodes = new Topic[4];
        
        for(int i=0; i<4 && !forceStop(); i++) {
            nodes[i] = getOrCreateTopic(topicmap, SI_PREFIX+"vertex"+i, "Tetrahedron Vertex "+i);
            setProgress(++progress);
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, SI_PREFIX+"edge", "Tetrahedron Edge");
        Topic role1 = getOrCreateTopic(topicmap, SI_PREFIX+"role1", "Tetrahedron Role 1");
        Topic role2 = getOrCreateTopic(topicmap, SI_PREFIX+"role2", "Tetrahedron Role 2");
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
        log("Ok.");
    }
    

    
    
    public void generateCube(TopicMap topicmap) {
        String SI_PREFIX = "http://wandora.org/si/cube/";
        
        log("Creating Cube");
        setProgressMax(8+12);
        int progress = 0;
        
        Topic[] nodes = new Topic[8];
        
        for(int i=0; i<8 && !forceStop(); i++) {
            nodes[i] = getOrCreateTopic(topicmap, SI_PREFIX+"vertex"+i, "Cube Vertex "+i);
            setProgress(++progress);
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, SI_PREFIX+"edge", "Cube Edge");
        Topic role1 = getOrCreateTopic(topicmap, SI_PREFIX+"role1", "Cube Role 1");
        Topic role2 = getOrCreateTopic(topicmap, SI_PREFIX+"role2", "Cube Role 2");
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
        log("Ok.");
    }
    
    

    public void generateOctahedron(TopicMap topicmap) {
        String SI_PREFIX = "http://wandora.org/si/octahedron/";
        
        log("Creating Octahedron");
        setProgressMax(6+12);
        int progress = 0;
        
        Topic[] nodes = new Topic[6];
        
        for(int i=0; i<6 && !forceStop(); i++) {
            nodes[i] = getOrCreateTopic(topicmap, SI_PREFIX+"vertex"+i, "Octahedron Vertex "+i);
            setProgress(++progress);
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, SI_PREFIX+"edge", "Octahedron Edge");
        Topic role1 = getOrCreateTopic(topicmap, SI_PREFIX+"role1", "Octahedron Role 1");
        Topic role2 = getOrCreateTopic(topicmap, SI_PREFIX+"role2", "Octahedron Role 2");
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
        log("Ok.");
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
    
    
    public void generateDodecahedron(TopicMap topicmap) {
        String SI_PREFIX = "http://wandora.org/si/dodecahedron/";
        
        log("Creating Dodecahedron");
        setProgressMax(20+30);
        int progress = 0;
        
        Topic[] nodes = new Topic[20];
        
        for(int i=0; i<20 && !forceStop(); i++) {
            nodes[i] = getOrCreateTopic(topicmap, SI_PREFIX+"vertex"+i, "Dodecahedron Vertex "+i);
            setProgress(++progress);
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, SI_PREFIX+"edge", "Dodecahedron Edge");
        Topic role1 = getOrCreateTopic(topicmap, SI_PREFIX+"role1", "Dodecahedron Role 1");
        Topic role2 = getOrCreateTopic(topicmap, SI_PREFIX+"role2", "Dodecahedron Role 2");
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
        log("Ok.");
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

    public void generateIcosahedron(TopicMap topicmap) {
        String SI_PREFIX = "http://wandora.org/si/icosahedron/";
        
        log("Creating Icosahedron");
        setProgressMax(12+30);
        int progress = 0;
        
        Topic[] nodes = new Topic[12];
        
        for(int i=0; i<12 && !forceStop(); i++) {
            nodes[i] = getOrCreateTopic(topicmap, SI_PREFIX+"vertex"+i, "Icosahedron Vertex "+i);
            setProgress(++progress);
        }
        
        Topic t1 = null;
        Topic t2 = null;
        Topic aType = getOrCreateTopic(topicmap, SI_PREFIX+"edge", "Icosahedron Edge");
        Topic role1 = getOrCreateTopic(topicmap, SI_PREFIX+"role1", "Icosahedron Role 1");
        Topic role2 = getOrCreateTopic(topicmap, SI_PREFIX+"role2", "Icosahedron Role 2");
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
        log("Ok.");
    }
}
