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

/**
 *
 * @author Eero
 */


public class LatticeGenerator extends AbstractGenerator implements WandoraTool {
    
    public static String SI_PREFIX = "http://wandora.org/si/topic/";
    
    @Override
    public String getName() {
        return "Lattice graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a lattice graph topic map.";
    }
    
    @Override
    public void execute (Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        GenericOptionsDialog god = new GenericOptionsDialog(
                admin,
                "Lattice graph generator",
                "Lattice graph generator options",
                true,
                new String[][]{
                    new String[]{"Dimension 1","string"},
                    new String[]{"Dimension 2","string"},
                    new String[]{"Dimension 3","string"},
                    new String[]{"Offset for Dimension 1","string"},
                    new String[]{"Offset for Dimension 2","string"},
                    new String[]{"Offset for Dimension 3","string"},
                },
                admin
                );
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values = god.getValues();
        
        
        int[] dimensions = new int[3];
        dimensions[0] = dimensions[1] = dimensions[2] = 0;
        int[] offsets = new int[3];
        offsets[0] = offsets[1] = offsets[2] = 0;
        int d1=0,d2=0,d3=0;
        int d1offset=0, d2offset=0, d3offset=0;

        try {
            dimensions[0] = Integer.parseInt(values.get("Dimension 1"));
            dimensions[1] = Integer.parseInt(values.get("Dimension 2"));
            dimensions[2] = Integer.parseInt(values.get("Dimension 3"));
            
            offsets[0] = Integer.parseInt(values.get("Offset for Dimension 1"));
            offsets[1] = Integer.parseInt(values.get("Offset for Dimension 2"));
            offsets[2] = Integer.parseInt(values.get("Offset for Dimension 3"));
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        for(int dimension : dimensions){
            if( dimension < 1) {
                singleLog("Dimension " + dimension + " is out of bounds. Using default value (1)");
                dimension = 1;
            }
        }
            
        for(int offset : offsets){
            if( offset < 0) {
                singleLog("Dimension " + offset + " is out of bounds. Using default value (0)");
                offset = 0;
            }
        }
        
        setDefaultLogger();
        setLogTitle("Lattice graph generator");
        log("Default logger initialized");
        
        Topic aType = getOrCreateTopic(topicmap, SI_PREFIX+"associationType", "Association");
        Topic rType1 = getOrCreateTopic(topicmap, SI_PREFIX+"roleType1", "RoleType1");
        Topic rType2 = getOrCreateTopic(topicmap, SI_PREFIX+"roleType2", "RoleType2");
        
        //HashMap<String,Topic> topics = new HashMap();
        Topic[][][] topics;
        topics = new Topic[dimensions[0]+offsets[0]][dimensions[1]+offsets[1]][dimensions[2]+offsets[2]];
        Association a;
        
        for(int i = offsets[0]; i < dimensions[0]+offsets[0] && !forceStop(); i++){
            for(int j = offsets[1]; j < dimensions[1]+offsets[1] && !forceStop(); j++){
                for(int k = offsets[2]; k < dimensions[2]+offsets[2] && !forceStop(); k++){
                    String id = i + "-" + j + "-" + k;
                    topics[i][j][k] = getOrCreateTopic(topicmap, SI_PREFIX+id, "Topic "+id);
                    Topic curTopic = topics[i][j][k];
                    
                    if( i > 0 && topics[i-1][j][k] != null ){
                        Topic prevTopic = topics[i-1][j][k];
                        a = topicmap.createAssociation(aType);
                        a.addPlayer(prevTopic,rType1);
                        a.addPlayer(curTopic,rType2);
                    }
                    if( i < d1+d1offset-1 && topics[i+1][j][k] != null ){
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
                    if( j < d2+d2offset-1 && topics[i][j+1][k] != null ){
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
                    if( k < d3+d3offset-1 && topics[i][j][k+1] != null ){
                        Topic nextTopic = topics[i][j][k+1];
                        a = topicmap.createAssociation(aType);
                        a.addPlayer(nextTopic,rType1);
                        a.addPlayer(curTopic,rType2);
                    }
                }
            }
        }
        
        setState(CLOSE);
        
    }
    
}
