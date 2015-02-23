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
 * FullyConnectedGraphGenerator.java
 *
 * Created on 31. toukokuuta 2007, 11:33
 *
 */

package org.wandora.application.tools.generators;




import java.util.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.utils.swing.GuiTools;




/**
 *
 * @author akivela
 */
public class FullyConnectedGraphGenerator extends AbstractGenerator implements WandoraTool {
    public static String CONNECTED_GRAPH_SI = "http://wandora.org/si/connected-graph";

    public static String siPattern = "http://wandora.org/si/connected-graph/node/__n__";
    public static String basenamePattern = "Connected graph node __n__";
    public static boolean connectWithWandoraClass = true;
    public static int initialTopicCounter = 0;
    
    
    
    
    /** Creates a new instance of FullyConnectedGraphGenerator */
    public FullyConnectedGraphGenerator() {
    }

    @Override
    public String getName() {
        return "Fully connected graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a fully connected graph topic map.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,
            "Fully connected graph generator",
            "Fully connected graph generator creates a subgraph of topics where every created topic is connected with every other created topic.",
            true,new String[][]{
            new String[]{"Number of connected topics","string"},
            new String[]{"Directional associations","boolean","false","Create 2 associations per topic pair? Created association differ in role order."},
            new String[]{"---1","separator"},
            new String[]{"Subject identifier pattern","string",siPattern,"Subject identifier patterns for the created node topics. Part __n__ in patterns is replaced with node counter."},
            new String[]{"Basename pattern","string",basenamePattern,"Basename patterns for the created node topics. Part __n__ in patterns is replaced with node counter."},
            new String[]{"Initial node counter","string",""+initialTopicCounter,"What is the number of first generated topic node."},
            new String[]{"Connect topics with Wandora class","boolean", connectWithWandoraClass ? "true" : "false","Create additional topics and associations that connect created topics with the Wandora class." },
            new String[]{"Association type topic","topic",null,"Optional association type for random graph edges."},
            new String[]{"First role topic","topic",null,"Optional role topic for graph edges."},
            new String[]{"Second role topic","topic",null,"Optional role topic for graph edges."},
        },wandora);
        
        god.setSize(700, 420);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int n = 0;
        boolean directional = false;
        try {
            n = Integer.parseInt(values.get("Number of connected topics"));
            directional = Boolean.parseBoolean(values.get("Directional associations"));
        }
        catch(NumberFormatException nfe) {
            singleLog("Parse error. Number of connected topics should be integer number.");
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
            
            try {
                initialTopicCounter = Integer.parseInt(values.get("Initial node counter"));
            }
            catch(NumberFormatException nfe) {
                singleLog("Parse error. Initial node counter should be an integer number. Cancelling.");
                return;
            }
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        Topic aType = topicmap.getTopic(values.get("Association type topic"));
        if(aType == null || aType.isRemoved()) {
            aType = getOrCreateTopic(topicmap, CONNECTED_GRAPH_SI+"/"+"association-type", "Connected graph association");
        }
        
        Topic role1 = topicmap.getTopic(values.get("First role topic"));
        if(role1 == null || role1.isRemoved()) {
            role1 = getOrCreateTopic(topicmap, CONNECTED_GRAPH_SI+"/"+"role-1", "Connected graph role 1");
        }
        
        Topic role2 = topicmap.getTopic(values.get("Second role topic"));
        if(role2 == null || role2.isRemoved()) {
            role2 = getOrCreateTopic(topicmap, CONNECTED_GRAPH_SI+"/"+"role-2", "Connected graph role 2");
        }
        
        if(role1.mergesWithTopic(role2)) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Role topics are same. This causes associations to be unary instead of binary. Do you want to continue?", "Role topics are same", WandoraOptionPane.WARNING_MESSAGE);
            if(a != WandoraOptionPane.YES_OPTION) {
                return;
            }
        }
        
        setDefaultLogger();
        setLogTitle("Fully connected graph generator");
        log("Creating topics.");
        
        Topic[] topics = new Topic[n];
        long graphIdentifier = System.currentTimeMillis();
        
        setProgressMax(n);
        for(int i=0; i<n && !forceStop(); i++) {
            setProgress(n);
            int nodeCounter = initialTopicCounter+i;
            topics[i] = getOrCreateTopic(topicmap, nodeCounter, graphIdentifier);
        }
              
        Topic t1 = null;
        Topic t2 = null;
        Association a = null;
        
        log("Creating associations.");
        setProgress(0);
        setProgressMax(n*n);
        int nk = 0;
        for(int j=0; j<n && !forceStop(); j++) {
            if(directional) nk = 0;
            else nk = j;
            t1 = topics[j];
            for(int k=nk; k<n && !forceStop(); k++) {
                t2 = topics[k];
                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(j*n+k);
            }
        }
        
        
        if(connectWithWandoraClass) {
            log("You'll find created topics and associations under the 'Connected graph' topic.");
        }
        else {
            String searchWord = basenamePattern.replaceAll("__n__", "");
            searchWord = searchWord.trim();
            log("You'll find created topics and associations by searching with a '"+searchWord+"'.");
        }
        log("Ok.");
        
        setState(WAIT);
    }
    
    
    
    
    private Topic getOrCreateTopic(TopicMap topicmap, int topicIdentifier, long graphIdentifier) {
        String newBasename = basenamePattern.replaceAll("__n__", ""+topicIdentifier);
        String newSubjectIdentifier = siPattern.replaceAll("__n__", ""+topicIdentifier);
        Topic t = getOrCreateTopic(topicmap, newSubjectIdentifier, newBasename);
        if(connectWithWandoraClass) {
            try {
                Topic graphTopic = getOrCreateTopic(topicmap, CONNECTED_GRAPH_SI, "Connected graph");
                Topic wandoraClass = getOrCreateTopic(topicmap, TMBox.WANDORACLASS_SI);
                makeSuperclassSubclass(topicmap, wandoraClass, graphTopic);
                Topic graphInstanceTopic = getOrCreateTopic(topicmap, CONNECTED_GRAPH_SI+"/"+graphIdentifier, "Connected graph "+graphIdentifier, graphTopic);
                graphInstanceTopic.addType(graphTopic);
                t.addType(graphInstanceTopic);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }
    
    
    
}
