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
 * RandomGraphGenerator.java
 *
 * Created on 30. toukokuuta 2007, 14:11
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
 * @author akivela
 */
public class RandomGraphGenerator extends AbstractGenerator implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static String RANDOM_GRAPH_SI = "http://wandora.org/si/random-graph";

    public static String siPattern = "http://wandora.org/si/random-graph/node/__n__";
    public static String basenamePattern = "Random graph node __n__";
    public static boolean connectWithWandoraClass = true;
    public static boolean ensureNumberOfAssociations = true;
    public static int nodeCounterOffset = 0;
    
    
    /** Creates a new instance of RandomGraphGenerator */
    public RandomGraphGenerator() {
    }

    @Override
    public String getName() {
        return "Random graph generator";
    }
    @Override
    public String getDescription() {
        return "Random graph generator creates a graph of given number of nodes "+
               "and edges between randomly selected graph nodes.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,
            "Random graph generator",
            "Random graph generator creates a graph of given number of nodes "+
              "and edges between randomly selected graph nodes. A node is a topic and "+
              "an edge is an association. "+
              "If 'number of associations' is a valid number "+
              "it overrides 'association probality'. Number is a positive integer. "+
              "Probability is a floating point number between 0.0 and 1.0.",
            true,new String[][]{
            new String[]{"Number of topics","string"},
            new String[]{"Number of random associations","string"},
            new String[]{"Random associations probability","string"},
            new String[]{"---1","separator"},
            new String[]{"Subject identifier pattern","string",siPattern,"Subject identifier patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
            new String[]{"Basename pattern","string",basenamePattern,"Basename patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
            new String[]{"Node counter offset","string",""+nodeCounterOffset,"What is the number of first generated topic node."},
            new String[]{"Connect topics with Wandora class","boolean", connectWithWandoraClass ? "true" : "false","Create additional topics and associations that connect created topics with the Wandora class." },
            new String[]{"Association type of random associations","topic",null,"Optional association type for random graph edges."},
            new String[]{"First role of random associations","topic",null,"Optional role topic for random graph edges."},
            new String[]{"Second role of random associations","topic",null,"Optional role topic for random graph edges."},
        },wandora);
        
        god.setSize(700, 460);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int n = 0;
        int an = 0;
        double ap = 0.0;
        boolean useAssociationNumber = false;
        try {
            n = Integer.parseInt(values.get("Number of topics"));
            String ans = values.get("Number of random associations");
            if(ans != null && ans.length() > 0) {
                an = Integer.parseInt(values.get("Number of random associations"));
                useAssociationNumber = true;
            }
            else {
                ap = Double.parseDouble(values.get("Random associations probability"));
                useAssociationNumber = false;
            }
        }
        catch(NumberFormatException nfe) {
            singleLog("Parse error.\n"+
                      "Number of topics and number of associations should be integers.\n"+
                      "Association probability should be floating point number between 0.0 and 1.0.");
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
                nodeCounterOffset = Integer.parseInt(values.get("Node counter offset"));
            }
            catch(NumberFormatException nfe) {
                singleLog("Parse error. Node counter offset should be an integer number. Using default value (0).");
                nodeCounterOffset = 0;
            }
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        Topic aType = topicmap.getTopic(values.get("Association type of random associations"));
        if(aType == null || aType.isRemoved()) {
            aType = getOrCreateTopic(topicmap, RANDOM_GRAPH_SI+"/"+"association-type", "Random graph association");
        }
        
        Topic role1 = topicmap.getTopic(values.get("First role of random associations"));
        if(role1 == null || role1.isRemoved()) {
            role1 = getOrCreateTopic(topicmap, RANDOM_GRAPH_SI+"/"+"role-1", "Random graph role 1");
        }
        
        Topic role2 = topicmap.getTopic(values.get("Second role of random associations"));
        if(role2 == null || role2.isRemoved()) {
            role2 = getOrCreateTopic(topicmap, RANDOM_GRAPH_SI+"/"+"role-2", "Random graph role 2");
        }

        if(role1.mergesWithTopic(role2)) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Role topics are same. This causes associations to be unary instead of binary. Do you want to continue?", "Role topics are same", WandoraOptionPane.WARNING_MESSAGE);
            if(a != WandoraOptionPane.YES_OPTION) {
                return;
            }
        }
        
        setDefaultLogger();
        setLogTitle("Random graph generator");
        log("Creating topics.");
        
        Topic[] topics = new Topic[n];
        long graphIdentifier = System.currentTimeMillis();
        
        setProgressMax(n);
        for(int i=0; i<n && !forceStop(); i++) {
            setProgress(i);
            int nodeCounter = nodeCounterOffset+i;
            topics[i] = getOrCreateTopic(topicmap, nodeCounter, graphIdentifier);
        }
                
        Association a = null;
        Topic t1 = null;
        Topic t2 = null;
        
        log("Creating random associations.");
        setProgress(0);
        
        // Creating exact number of random associations!
        if(useAssociationNumber) {
            setProgressMax(an);
            HashSet createdAssociations = new HashSet(an);
            for(int j=0; j<an && !forceStop(); j++) {
                int n1 = (int) Math.floor( Math.random() * n );
                int n2 = (int) Math.floor( Math.random() * n );
                if(ensureNumberOfAssociations) {
                    String hash = n1+"-"+n2;
                    int retries = 100;
                    while(createdAssociations.contains(hash) && --retries>0) {
                        n1 = (int) Math.floor( Math.random() * n );
                        n2 = (int) Math.floor( Math.random() * n );
                        hash = n1+"-"+n2;
                    }
                    createdAssociations.add(hash);
                }

                t1 = topics[n1];
                t2 = topics[n2];

                a = topicmap.createAssociation(aType);
                a.addPlayer(t1, role1);
                a.addPlayer(t2, role2);
                setProgress(j);
            }
        }
        
        // Creating associations with given probability!
        else {
            setProgressMax(n*n);
            double rd = 0.0;
            for(int j=0; j<n && !forceStop(); j++) {
                t1 = topics[j];
                for(int k=0; k<n && !forceStop(); k++) {
                    rd = Math.random();
                    if(rd < ap) {
                        t2 = topics[k];

                        a = topicmap.createAssociation(aType);
                        a.addPlayer(t1, role1);
                        a.addPlayer(t2, role2);
                    }
                    setProgress(j*n+k);
                }
            }
        }
        
        if(connectWithWandoraClass) {
            log("You'll find created topics and associations under the 'Random graph' topic.");
        }
        else {
            String searchWord = basenamePattern.replaceAll("__n__", "");
            searchWord = searchWord.trim();
            log("You'll find created topics and associations by searching with a '"+searchWord+"'.");
        }
        log("Ready.");
        
        setState(WAIT);
    }
    
    

    
    private Topic getOrCreateTopic(TopicMap topicmap, int topicIdentifier, long graphIdentifier) {
        String newBasename = basenamePattern.replaceAll("__n__", ""+topicIdentifier);
        String newSubjectIdentifier = siPattern.replaceAll("__n__", ""+topicIdentifier);
        Topic t = getOrCreateTopic(topicmap, newSubjectIdentifier, newBasename);
        if(connectWithWandoraClass) {
            try {
                Topic graphTopic = getOrCreateTopic(topicmap, RANDOM_GRAPH_SI, "Random graph");
                Topic wandoraClass = getOrCreateTopic(topicmap, TMBox.WANDORACLASS_SI);
                makeSuperclassSubclass(topicmap, wandoraClass, graphTopic);
                Topic graphInstanceTopic = getOrCreateTopic(topicmap, RANDOM_GRAPH_SI+"/"+graphIdentifier, "Random graph "+graphIdentifier, graphTopic);
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
