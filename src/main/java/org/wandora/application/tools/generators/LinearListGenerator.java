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
 * LinearListGenerator.java
 *
 * Created on 31. toukokuuta 2007, 16:28
 *
 */

package org.wandora.application.tools.generators;





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
import org.wandora.utils.swing.GuiTools;


/**
 *
 * @author akivela
 */
public class LinearListGenerator extends AbstractGenerator implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static String LIST_GRAPH_SI = "http://wandora.org/si/linear-list/";
    
    public static String siPattern = "http://wandora.org/si/linear-list/node/__n__";
    public static String basenamePattern = "Linear list vertex __n__";
    public static boolean connectWithWandoraClass = true;
    public static int n = 10;
    public static int topicCounterOffset = 0;
    public static boolean makeCycle = false;
    
    
    
    /** Creates a new instance of LinearListGenerator */
    public LinearListGenerator() {
    }


    @Override
    public String getName() {
        return "Linear list graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a linear list graph topic map.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,
            "Linear list graph generator",
            "Linear list graph generator creates a topic map of given number of " +
            "topics associated like a linked list.",
            true,new String[][]{
            new String[]{"Number of list nodes","string",""+n},
            new String[]{"Make cycle","boolean", makeCycle?"true":"false","Link last and first node?"},
            new String[]{"---1","separator"},
            new String[]{"Subject identifier pattern","string",siPattern,"Subject identifier patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
            new String[]{"Basename pattern","string",basenamePattern,"Basename patterns for the created node topics. Part __n__ in patterns is replaced with node identifier."},
            new String[]{"Topic counter offset","string",""+topicCounterOffset,"What is the number of first generated topic node."},
            new String[]{"Connect topics with Wandora class","boolean", connectWithWandoraClass ? "true" : "false","Create additional topics and associations that connect created topics with the Wandora class." },
            new String[]{"Association type for edges of the linear list","topic",null,"Optional association type for graph edges."},
            new String[]{"Role topic for the previous node","topic",null,"Optional role topic for parent topics in tree graph."},
            new String[]{"Role topic for the next node","topic",null,"Optional role topic for child topics in tree graph."},
        },wandora);
        
        god.setSize(700, 460);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        try {
            n = Integer.parseInt(values.get("Number of list nodes"));
            makeCycle=Boolean.parseBoolean(values.get("Make cycle"));
        }
        catch(Exception e) {
            singleLog("Number of list nodes is not a number. Cancelling.", e);
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
                topicCounterOffset = Integer.parseInt(values.get("Topic counter offset"));
            }
            catch(NumberFormatException nfe) {
                singleLog("Parse error. Topic counter offset should be an integer number. Cancelling.");
                return;
            }
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
               
        Topic aType = topicmap.getTopic(values.get("Association type for edges of the linear list"));
        if(aType == null || aType.isRemoved()) {
            aType = getOrCreateTopic(topicmap, LIST_GRAPH_SI+"/"+"association-type", "Linear list association");
        }
        
        Topic previousT = topicmap.getTopic(values.get("Role topic for the previous node"));
        if(previousT == null || previousT.isRemoved()) {
            previousT = getOrCreateTopic(topicmap, LIST_GRAPH_SI+"/"+"previous", "Previous in list");
        }
        
        Topic nextT = topicmap.getTopic(values.get("Role topic for the next node"));
        if(nextT == null || nextT.isRemoved()) {
            nextT = getOrCreateTopic(topicmap, LIST_GRAPH_SI+"/"+"next", "Next in list");
        }

        if(previousT.mergesWithTopic(nextT)) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Previous and next role topics are same. This causes associations to be unary instead of binary. Do you want to continue?", "Role topics are same", WandoraOptionPane.WARNING_MESSAGE);
            if(a != WandoraOptionPane.YES_OPTION) {
                return;
            }
        }
        
        setDefaultLogger();
        setLogTitle("Linear list graph generator");
        log("Creating linear list graph");
               
        Association a = null;
        long graphIdentifier = System.currentTimeMillis();
        Topic first = null;
        Topic previous = null;
        Topic next = null;
        
        setProgressMax(n);
        int progress=0;
        for(int i=topicCounterOffset; i<topicCounterOffset+n && !forceStop(); i++) {
            next = getOrCreateTopic(topicmap, i, graphIdentifier);
            if(previous != null) {
                a = topicmap.createAssociation(aType);
                a.addPlayer(previous, previousT);
                a.addPlayer(next, nextT);
            }
            else {
                first = next;
            }
            setProgress(progress++);
            previous = next;
        }
        if(makeCycle) {
            a = topicmap.createAssociation(aType);
            a.addPlayer(previous, previousT);
            a.addPlayer(first, nextT);
        }

        if(connectWithWandoraClass) {
            log("You'll find created topics and associations under the 'Linear list' topic.");
        }
        else {
            String searchWord = basenamePattern.replaceAll("__n__", "");
            searchWord = searchWord.trim();
            log("You'll find created topics and associations by searching '"+searchWord+"'.");
        }
        log("Ready.");
        setState(WAIT);
    }


    
 
    private Topic getOrCreateTopic(TopicMap tm, int topicIdentifier, long graphIdentifier) {
        String newBasename = basenamePattern.replaceAll("__n__", ""+topicIdentifier);
        String newSubjectIdentifier = siPattern.replaceAll("__n__", ""+topicIdentifier);
        Topic t = getOrCreateTopic(tm, newSubjectIdentifier, newBasename);
        if(connectWithWandoraClass) {
            try {
                Topic listGraphTopic = getOrCreateTopic(tm, LIST_GRAPH_SI, "Linear list");
                Topic wandoraClass = getOrCreateTopic(tm, TMBox.WANDORACLASS_SI);
                makeSuperclassSubclass(tm, wandoraClass, listGraphTopic);
                Topic listGraphInstanceTopic = getOrCreateTopic(tm, LIST_GRAPH_SI+"/"+graphIdentifier, "Linear list "+graphIdentifier, listGraphTopic);
                listGraphInstanceTopic.addType(listGraphTopic);
                t.addType(listGraphInstanceTopic);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }
}
    

