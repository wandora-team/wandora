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
 * AdjacencyListImport.java
 *
 * Created on 2008-09-20, 16:42
 *
 */


package org.wandora.application.tools.importers.graphs;


import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import static org.wandora.utils.Tuples.T2;


/**
 *
 * @author akivela
 */
public class AdjacencyListImport extends AbstractWandoraTool implements WandoraTool {
    
    public final static String SI_PREFIX = "http://wandora.org/si/topic/";
    
    public static String nodeDelimiter = ",";
    public static String edgeDelimiter = "";
            
    
    /** Creates a new instance of AdjacencyListImport */
    public AdjacencyListImport() {
    }


    
    
    
    
    @Override
    public String getName() {
        return "Adjacency list import";
    }
    
    @Override
    public String getDescription() {
        return "Generates topics and associations from given adjacency list.";
    }
    
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException {
        TopicMap topicmap = solveContextTopicMap(admin, context);
        
        AdjacencyListImportDialog edgeSourceDialog = new AdjacencyListImportDialog(admin, this, true);
        edgeSourceDialog.setVisible(true);
        if(!edgeSourceDialog.wasAccepted()) return;
        
        setDefaultLogger();
        setLogTitle("Adjacency list import");
        
        log("Reading adjacency list!");
        String edgeData = edgeSourceDialog.getContent();
        
        log("Parsing adjacency list!");
        EdgeParser edgeParser = new EdgeParser(edgeData, topicmap);
        edgeParser.parse();
        log("Total "+edgeParser.counter+" associations created!");
        
        log("Ok!");
        setState(WAIT);
    }
    
    
    
    protected void createEdgeAssociation(ArrayList<String> edge, TopicMap topicmap) {
        try {
            if(topicmap != null && edge != null && edge.size() > 0) {
                hlog("Processing association "+edge);
                Topic atype = getOrCreateTopic(topicmap, SI_PREFIX+"edge", "edge");
                Association a = null;
                Topic player = null;
                Topic role = null;
                a = topicmap.createAssociation(atype);
                int rc = 0;
                for(String node : edge) {
                    if(node != null && node.length() > 0) {
                        player = getOrCreateTopic(topicmap, SI_PREFIX+node, node);
                        role = getOrCreateTopic(topicmap, SI_PREFIX+"role"+rc, "role"+rc);
                        if(player != null && !player.isRemoved()) {
                            if(role != null && !role.isRemoved()) {
                                a.addPlayer(player, role);
                            }
                        }
                        rc++;
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    public Topic getOrCreateTopic(TopicMap map, String si, String basename) {
        Topic topic = null;
        try {
            topic = map.getTopic(si);
            if(topic == null) {
                topic = map.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
                if(basename != null && basename.length() > 0) topic.setBaseName(basename);
            }
        }
        catch(Exception e) {
            log(e);
            e.printStackTrace();
        }
        return topic;
    }
    
    
    
    
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createImportType();
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/import_adjacency_list.png");
    }
    
    
    
    
    
    
    
    
    
    private class EdgeParser {

        private String data = null;
        private int index = 0;
        private int len = 0;
        private TopicMap topicmap = null;
        public int counter = 0;
        
        
        public EdgeParser(String data, TopicMap topicmap) {
            this.data = data;
            this.topicmap = topicmap;
            this.index = 0;
            this.len = data.length();
            setProgressMax(len);
        }
        


        private void parse() {
            ArrayList<String> edge = null;
            while(index < len && !forceStop()) {
                eatSpaces();
                while(index < len && !Character.isLetterOrDigit(data.charAt(index))) {
                    index++;
                    eatSpaces();
                }
                edge = parseEdge();
                if(edge != null && edge.size() > 0) {
                    createEdgeAssociation(edge, topicmap);
                    counter++;
                }
                eatSpaces();
                setProgress(len);
            }
        }

        
        
        
        private ArrayList<String> parseEdge() {
            ArrayList<String> edge = new ArrayList<String>();
            String node = null;
            do {
                eatSpaces();
                node = parseNode();
                if(node != null && node.length() > 0) {
                    edge.add(node);
                }
                parseNodeDelimiter();
            }
            while(!parseEdgeDelimiter() && !forceStop());
            return edge;
        }

        
        
        private String parseNode() {
            StringBuffer node = new StringBuffer("");
            while(index < len && Character.isLetterOrDigit(data.charAt(index)) && !forceStop()) {
                node.append(data.charAt((index)));
                index++;
            }
            return node.toString();
        }
        
        
        
        private boolean parseNodeDelimiter() {
            index++;
            eatJustSpaces();
            if(index < len && ",-|\t".indexOf(data.charAt(index)) != -1) {
                index++;
                return true;
            }
            return false;
        }
        
        
        
        private boolean parseEdgeDelimiter() {
            eatJustSpaces();
            boolean delimiterFound = false;
            while(index < len && "\n\r;:()[]{}<>/".indexOf(data.charAt(index)) != -1) {
                index++;
                delimiterFound=true;
            }
            if(index >= len) delimiterFound = true;
            return delimiterFound;
        }
        
        
        
        private void eatSpaces() {
            if(data == null) return;
            while(index < len && Character.isSpaceChar(data.charAt(index))) {
                index++;
            }
        }
        
        private void eatJustSpaces() {
            if(data == null) return;
            while(index < len && data.charAt(index)==' ') {
                index++;
            }
        }
    }
}