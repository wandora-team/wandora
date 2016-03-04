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
 * FiniteGroupGenerator.java
 *
 * Created on 31. toukokuuta 2007, 17:01
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
public class FiniteGroupGenerator extends AbstractGenerator implements WandoraTool {
    public static String FINITE_GROUP_SI = "http://wandora.org/si/finite-group";

    public static String siPattern = "http://wandora.org/si/finite-group/node/__n__";
    public static String basenamePattern = "Topic __n__";
    public static boolean connectWithWandoraClass = true;
    public static int topicCounterOffset = 0;
    
    
    /** Creates a new instance of FiniteGroupGenerator */
    public FiniteGroupGenerator() {
    }

    @Override
    public String getName() {
        return "Finite group graph generator";
    }
    @Override
    public String getDescription() {
        return "Generates a finite group (algebra) topic map.";
    }
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(wandora, context);
        
        GenericOptionsDialog god=new GenericOptionsDialog(wandora,
            "Finite group graph generator",
            "Finite group graph generator options",
            true,new String[][]{
            new String[]{"Number of group elements","string"},
            new String[]{"---1","separator"},
            new String[]{"Subject identifier pattern","string",siPattern,"Subject identifier patterns for the created node topics. Part __n__ in patterns is replaced with node counter."},
            new String[]{"Basename pattern","string",basenamePattern,"Basename patterns for the created node topics. Part __n__ in patterns is replaced with node counter."},
            new String[]{"Topic counter offset","string",""+topicCounterOffset,"What is the number of first generated topic node."},
            new String[]{"Connect topics with Wandora class","boolean", connectWithWandoraClass ? "true" : "false","Create additional topics and associations that connect created topics with the Wandora class." },
        },wandora);
        
        god.setSize(700, 420);
        GuiTools.centerWindow(god,wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        Map<String,String> values=god.getValues();
        
        int n = 0;
        try {
            n = Integer.parseInt(values.get("Number of group elements"));
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
                topicCounterOffset = Integer.parseInt(values.get("Topic counter offset"));
            }
            catch(NumberFormatException nfe) {
                singleLog("Parse error. Counter offset should be an integer number. Using default value (0).");
                topicCounterOffset = 0;
            }
        }
        catch(Exception e) {
            singleLog(e);
            return;
        }
        
        
        setDefaultLogger();
        setLogTitle("Finite group graph generator");
        log("Creating a topic map for finite group (algebra).");
        
        Topic[] topics = new Topic[n];
        Topic operationType = getOrCreateTopic(topicmap, FINITE_GROUP_SI+"operation", "Operation");
        Topic operand1T = getOrCreateTopic(topicmap, FINITE_GROUP_SI+"operand-1", "Operand 1");
        Topic operand2T = getOrCreateTopic(topicmap, FINITE_GROUP_SI+"operand-2", "Operand 2");
        Topic resultT = getOrCreateTopic(topicmap, FINITE_GROUP_SI+"result", "Result");
        Association a = null;
        
        Topic o1;
        Topic o2;
        Topic r;
        long graphIdentifier = System.currentTimeMillis();
        
        setProgressMax(n);
        for(int i=0; i<n && !forceStop(); i++) {
            setProgress(n);
            topics[i] = getOrCreateTopic(topicmap, topicCounterOffset+i, graphIdentifier);
        }
        
        setProgressMax(n*n);
        int progress=0;
        for(int i=0; i<n && !forceStop(); i++) {
            for(int j=0; j<n && !forceStop(); j++) {
                o1 = topics[i];
                o2 = topics[j];
                r = topics[((i+j)%n)];
                a = topicmap.createAssociation(operationType);
                a.addPlayer(o1, operand1T);
                a.addPlayer(o2, operand2T);
                a.addPlayer(r, resultT);
                setProgress(progress++);
            }
        }
        
        if(connectWithWandoraClass) {
            log("You'll find created topics and associations under the 'Finite group graph' topic.");
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
                Topic graphTopic = getOrCreateTopic(topicmap, FINITE_GROUP_SI, "Finite group graph");
                Topic wandoraClass = getOrCreateTopic(topicmap, TMBox.WANDORACLASS_SI);
                makeSuperclassSubclass(topicmap, wandoraClass, graphTopic);
                Topic graphInstanceTopic = getOrCreateTopic(topicmap, FINITE_GROUP_SI+"/"+graphIdentifier, "Finite group graph "+graphIdentifier, graphTopic);
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

