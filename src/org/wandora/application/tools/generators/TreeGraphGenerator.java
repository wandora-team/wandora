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
 * TreeGraphGenerator.java
 *
 * Created on 31. toukokuuta 2007, 13:21
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
public class TreeGraphGenerator extends AbstractGenerator implements WandoraTool {
    public static String TREE_GRAPH_SI = "http://wandora.org/si/tree-graph/";
    
    public static String siPattern = "http://wandora.org/si/tree-graph/node/__n__";
    public static String basenamePattern = "Tree graph node __n__";
    public static boolean connectWithWandoraClass = true;
    public static int d = 5; // Tree depth
    public static int n = 2; // Number of child nodes
    
    
    /** Creates a new instance of TreeGraphGenerator */
    public TreeGraphGenerator() {
    }
    

    @Override
    public String getName() {
        return "Tree graph generator";
    }
    @Override
    public String getDescription() {
        return "Tree graph generator creates a set of topics and associations that resembles a graph tree where "+
               "topics are graph nodes and associations graph edges between child and parent nodes. A tree has "+
               "one root node. A tree node is connected with equal number of child nodes and one parent node.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,
            "Tree graph generator",
            "Tree graph generator creates a set of topics and associations that resembles a graph tree where "+
                "topics are graph nodes and associations graph edges between child and parent nodes. A tree has "+
                "one root node. A tree node is connected with equal number of child nodes and one parent node."+
                "Tree depth and number of child nodes should be positive integer numbers.",
            true,new String[][]{
            new String[]{"Tree depth","string",""+d},
            new String[]{"Number of child nodes","string",""+n},
            /* new String[]{"Add root additional branch","boolean","false","Should the root contain +1 edges?"}, */
            new String[]{"---1","separator"},
            new String[]{"Subject identifier pattern","string",siPattern,"Subject identifier patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
            new String[]{"Basename pattern","string",basenamePattern,"Basename patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
            new String[]{"Connect topics with Wandora class","boolean", connectWithWandoraClass ? "true" : "false","Create additional topics and associations that connect created topics with the Wandora class." },
            new String[]{"Association type of tree edges","topic",null,"Optional association type for graph edges."},
            new String[]{"Parent role in tree edges","topic",null,"Optional role topic for parent topics in tree graph."},
            new String[]{"Child role in tree edges","topic",null,"Optional role topic for child topics in tree graph."},
        },wandora);
        
        god.setSize(700, 460);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        

        boolean additionalRootBranch = false;
        try {
            d = Integer.parseInt(values.get("Tree depth"));
            n = Integer.parseInt(values.get("Number of child nodes"));
            // additionalRootBranch=Boolean.parseBoolean(values.get("Add root additional branch"));
        }
        catch(Exception e) {
            singleLog("Parse error. Tree depth and number of child nodes should be integer numbers. Cancelling.", e);
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
 
        Topic aType = topicmap.getTopic(values.get("Association type of tree edges"));
        if(aType == null || aType.isRemoved()) {
            aType = getOrCreateTopic(topicmap, TREE_GRAPH_SI+"/"+"association-type", "Tree graph association");
        }
        
        Topic parentT = topicmap.getTopic(values.get("Parent role in tree edges"));
        if(parentT == null || parentT.isRemoved()) {
            parentT = getOrCreateTopic(topicmap, TREE_GRAPH_SI+"/"+"parent", "Tree graph parent");
        }
        
        Topic childT = topicmap.getTopic(values.get("Child role in tree edges"));
        if(childT == null || childT.isRemoved()) {
            childT = getOrCreateTopic(topicmap, TREE_GRAPH_SI+"/"+"child", "Tree graph child");
        }

        if(parentT.mergesWithTopic(childT)) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Parent and child role topics are same. This causes associations to be unary instead of binary. Do you want to continue?", "Role topics are same", WandoraOptionPane.WARNING_MESSAGE);
            if(a != WandoraOptionPane.YES_OPTION) {
                return;
            }
        }
        
        setDefaultLogger();
        setLogTitle("Tree graph generator");
        log("Creating tree graph");
        
        ArrayList<Topic> topics = new ArrayList<Topic>();
        ArrayList<Topic> nextTopics = null;
        Topic parent = null;
        Topic child = null;
        int j = 0;
        String id = "";
        
        Association a = null;
        long graphIdentifier = System.currentTimeMillis();
        
        String newBasename = basenamePattern.replaceAll("__n__", "0");
        String newSubjectIdentifier = siPattern.replaceAll("__n__", "0");
        Topic rootTopic = getOrCreateTopic(topicmap, newSubjectIdentifier, newBasename);
        connectWithWandoraClass(rootTopic, topicmap, graphIdentifier);
        topics.add(rootTopic);
        
        setProgressMax((int) (Math.pow(n, (d+1))-1)/(n-1));
        int progress=0;
        int nn = n;
        for(int dep=1; dep<=d && !forceStop(); dep++) {
            j = 0;
            nextTopics = new ArrayList<Topic>();
            if(additionalRootBranch && dep == 0) nn = n+1;
            else nn = n;
            Iterator<Topic> topicIterator = topics.iterator();
            while( topicIterator.hasNext() && !forceStop() ) {
                parent = topicIterator.next();
                for(int k=0; k<nn && !forceStop(); k++) {
                    id = dep+"-"+j;
                    
                    newBasename = basenamePattern.replaceAll("__n__", id);
                    newSubjectIdentifier = siPattern.replaceAll("__n__", id);
                    child = getOrCreateTopic(topicmap, newSubjectIdentifier, newBasename);
                    connectWithWandoraClass(child, topicmap, graphIdentifier);
                    
                    nextTopics.add(child);
                    
                    a = topicmap.createAssociation(aType);
                    a.addPlayer(parent, parentT);
                    a.addPlayer(child, childT);
                    
                    setProgress(progress++);
                    
                    j++;
                }
            }
            topics = nextTopics;
        }
        
        if(connectWithWandoraClass) {
            log("You'll find created topics and associations under the 'Tree graph' topic.");
        }
        else {
            String searchWord = basenamePattern.replaceAll("__n__", "");
            searchWord = searchWord.trim();
            log("You'll find created topics and associations by searching '"+searchWord+"'.");
        }
        String rootTopicName = basenamePattern.replaceAll("__n__", "0");
        log("Root of the tree is topic '"+rootTopicName+"'.");
        log("Ok.");
        setState(WAIT);
    }
    
    
    
    
    private void connectWithWandoraClass(Topic t, TopicMap tm, long graphIdentifier) {
        if(connectWithWandoraClass) {
            try {
                Topic treeGraphTopic = getOrCreateTopic(tm, TREE_GRAPH_SI, "Tree graph");
                Topic wandoraClass = getOrCreateTopic(tm, TMBox.WANDORACLASS_SI);
                makeSuperclassSubclass(tm, wandoraClass, treeGraphTopic);
                Topic treeGraphInstanceTopic = getOrCreateTopic(tm, TREE_GRAPH_SI+"/"+graphIdentifier, "Tree graph "+graphIdentifier, treeGraphTopic);
                treeGraphInstanceTopic.addType(treeGraphTopic);
                t.addType(treeGraphInstanceTopic);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
