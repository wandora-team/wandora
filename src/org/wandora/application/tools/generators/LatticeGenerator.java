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
 * TreeGraphGenerator.java
 *
 * Created on 10.5.2012
 *
 */

package org.wandora.application.tools.generators;

import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import java.util.*;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.utils.swing.GuiTools;

/**
 *
 * @author Eero, akivela
 */


public class LatticeGenerator extends AbstractGenerator implements WandoraTool {
    
    public static String LATTICE_GRAPH_SI = "http://wandora.org/si/lattice/";
    
    public static String siPattern = "http://wandora.org/si/lattice/vertex/__n__";
    public static String basenamePattern = "Lattice vertex __n__";
    public static boolean connectWithWandoraClass = true;
    
    public int[] dimensions = new int[] { 4,4,4 };
    public int[] offsets = new int[] { 0,0,0 };

    
    @Override
    public String getName() {
        return "Lattice graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a lattice graph topic map where topics represents lattice vertices and associations "+
               "lattice edges.";
    }
    
    @Override
    public void execute (Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);
        
        GenericOptionsDialog god = new GenericOptionsDialog(
            wandora,
            "Lattice graph generator",
            "A lattice graph is a square grid graph where topics represent graph vertices and associations "+
            "graph edges. Generator creates lattices in three dimensional and finite size. "+
            "Each vertex has a 3D coordinate in the lattice. "+
            "Offsets are numbers that move the initial 3D coordinate of vertices.",
            true,
            new String[][]{
                new String[]{"Dimension 1 size", "string", ""+dimensions[0]},
                new String[]{"Dimension 2 size", "string", ""+dimensions[1]},
                new String[]{"Dimension 3 size", "string", ""+dimensions[2]},
                new String[]{"Offset for dimension 1", "string", ""+offsets[0]},
                new String[]{"Offset for dimension 2", "string", ""+offsets[0]},
                new String[]{"Offset for dimension 3", "string", ""+offsets[0]},
                new String[]{"---1", "separator"},
                new String[]{"Subject identifier pattern", "string",siPattern, "Subject identifier patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
                new String[]{"Basename pattern", "string",basenamePattern, "Basename patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
                new String[]{"Connect topics with Wandora class", "boolean", connectWithWandoraClass ? "true" : "false", "Create additional topics and associations that connect created topics with the Wandora class." },
                new String[]{"Association type topic", "topic", null, "Optional association type for graph edges."},
                new String[]{"First role topic", "topic", null, "Optional role topic for graph edges."},
                new String[]{"Second role topic", "topic", null, "Optional role topic for graph edges."},
            },
            wandora
        );
        
        god.setSize(700, 500);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values = god.getValues();

        try {
            dimensions[0] = Integer.parseInt(values.get("Dimension 1 size"));
            dimensions[1] = Integer.parseInt(values.get("Dimension 2 size"));
            dimensions[2] = Integer.parseInt(values.get("Dimension 3 size"));
            
            offsets[0] = Integer.parseInt(values.get("Offset for dimension 1"));
            offsets[1] = Integer.parseInt(values.get("Offset for dimension 2"));
            offsets[2] = Integer.parseInt(values.get("Offset for dimension 3"));
        }
        catch(NumberFormatException nfe) {
            singleLog("Parse error. Hypercube dimensions and offsets should be integer numbers. Cancelling.", nfe);
            return;
        }
        catch(Exception e) {
            singleLog(e);
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
        
 
        Topic aType = topicmap.getTopic(values.get("Association type topic"));
        if(aType == null || aType.isRemoved()) {
            aType = getOrCreateTopic(topicmap, LATTICE_GRAPH_SI+"/"+"association-type", "Lattice association");
        }
        
        Topic rType1 = topicmap.getTopic(values.get("First role topic"));
        if(rType1 == null || rType1.isRemoved()) {
            rType1 = getOrCreateTopic(topicmap, LATTICE_GRAPH_SI+"/"+"role-1", "Lattice role 1");
        }
        
        Topic rType2 = topicmap.getTopic(values.get("Second role topic"));
        if(rType2 == null || rType2.isRemoved()) {
            rType2 = getOrCreateTopic(topicmap, LATTICE_GRAPH_SI+"/"+"role-2", "Lattice role 2");
        }

        if(rType1.mergesWithTopic(rType2)) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Role topics are same. This causes associations to be unary instead of binary. Do you want to continue?", "Role topics are same", WandoraOptionPane.WARNING_MESSAGE);
            if(a != WandoraOptionPane.YES_OPTION) {
                return;
            }
        }
        
        setDefaultLogger();
        setLogTitle("Lattice graph generator");
        
        for(int dimension : dimensions) {
            if( dimension < 1) {
                log("Dimension " + dimension + " is out of bounds. Using default value (1).");
                dimension = 1;
            }
        }
            
        for(int offset : offsets) {
            if( offset < 0) {
                log("Offset " + offset + " is out of bounds. Using default value (0).");
                offset = 0;
            }
        }
       
        
        long graphIdentifier = System.currentTimeMillis();

        log("Creating lattice graph.");
        
        //HashMap<String,Topic> topics = new HashMap();
        Topic[][][] topics;
        topics = new Topic[dimensions[0]+offsets[0]][dimensions[1]+offsets[1]][dimensions[2]+offsets[2]];
        Association a;
        
        for(int i = offsets[0]; i < dimensions[0]+offsets[0] && !forceStop(); i++){
            for(int j = offsets[1]; j < dimensions[1]+offsets[1] && !forceStop(); j++){
                for(int k = offsets[2]; k < dimensions[2]+offsets[2] && !forceStop(); k++){
                    String id = i + "-" + j + "-" + k;
                    
                    String newBasename = basenamePattern.replaceAll("__n__", id);
                    String newSubjectIdentifier = siPattern.replaceAll("__n__", id);
                    Topic curTopic = getOrCreateTopic(topicmap, newSubjectIdentifier, newBasename);
                    connectWithWandoraClass(curTopic, topicmap, graphIdentifier);
                    
                    topics[i][j][k] = curTopic;

                    if( i > 0 && topics[i-1][j][k] != null ){
                        Topic prevTopic = topics[i-1][j][k];
                        a = topicmap.createAssociation(aType);
                        a.addPlayer(prevTopic,rType1);
                        a.addPlayer(curTopic,rType2);
                    }
                    if( i < dimensions[0]+offsets[0]-1 && topics[i+1][j][k] != null ){
                        Topic nextTopic = topics[i+1][j][k];
                        a = topicmap.createAssociation(aType);
                        a.addPlayer(nextTopic,rType1);
                        a.addPlayer(curTopic,rType2);
                    }
                    if( j > 0 && topics[i][j-1][k] != null ){
                        Topic prevTopic = topics[i][j-1][k];
                        a = topicmap.createAssociation(aType);
                        a.addPlayer(prevTopic,rType1);
                        a.addPlayer(curTopic,rType2);
                    }
                    if( j < dimensions[1]+offsets[1]-1 && topics[i][j+1][k] != null ){
                        Topic nextTopic = topics[i][j+1][k];
                        a = topicmap.createAssociation(aType);
                        a.addPlayer(nextTopic,rType1);
                        a.addPlayer(curTopic,rType2);
                    }
                    if( k > 0 && topics[i][j][k-1] != null ){
                        Topic prevTopic = topics[i][j][k-1];
                        a = topicmap.createAssociation(aType);
                        a.addPlayer(prevTopic,rType1);
                        a.addPlayer(curTopic,rType2);
                    }
                    if( k < dimensions[2]+offsets[2]-1 && topics[i][j][k+1] != null ){
                        Topic nextTopic = topics[i][j][k+1];
                        a = topicmap.createAssociation(aType);
                        a.addPlayer(nextTopic,rType1);
                        a.addPlayer(curTopic,rType2);
                    }
                }
            }
        }
        
        if(connectWithWandoraClass) {
            log("You'll find created topics and associations under the 'Lattice graph' topic.");
        }
        else {
            String searchWord = basenamePattern.replaceAll("__n__", "");
            searchWord = searchWord.trim();
            log("You'll find created topics and associations by searching '"+searchWord+"'.");
        }
        log("Ready.");
        
        setState(WAIT);
    }
    
    
    

    private void connectWithWandoraClass(Topic t, TopicMap tm, long graphIdentifier) {
        if(connectWithWandoraClass) {
            try {
                Topic treeGraphTopic = getOrCreateTopic(tm, LATTICE_GRAPH_SI, "Lattice graph");
                Topic wandoraClass = getOrCreateTopic(tm, TMBox.WANDORACLASS_SI);
                makeSuperclassSubclass(tm, wandoraClass, treeGraphTopic);
                Topic treeGraphInstanceTopic = getOrCreateTopic(tm, LATTICE_GRAPH_SI+"/"+graphIdentifier, "Lattice graph "+graphIdentifier, treeGraphTopic);
                treeGraphInstanceTopic.addType(treeGraphTopic);
                t.addType(treeGraphInstanceTopic);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
