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
 * PropertyTableExtractor.java
 *
 * Created on 1. marraskuuta 2007, 10:42
 *
 */

package org.wandora.application.tools.extractors.table;


import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.text.html.*;
import javax.swing.text.*;
import javax.swing.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;




/**
 * Converts property-table to a set of topics and *one* association. Property-table is
 * a HTML table structure with two columns. First column contains property names.
 * Second column of property table contains property values. Each property name will
 * be mapped to a role, and each property value to a role player.
 *
 * @author akivela
 */
public class PropertyTableExtractor extends AbstractExtractor implements WandoraTool {
   
    private URL basePath = null;
    public static String SI_PREFIX = "http://wandora.org/si/table";
    
    
    
    /** Creates a new instance of PropertyTableExtractor */
    public PropertyTableExtractor() {
    }

    
    @Override
    public String getName() {
        return "Property table extractor";
    }
    
    @Override
    public String getDescription() {
        return "Converts HTML tables to associations. "+
                "Extractor assumes each row in the table has two cells, containing "+
                "association role and association player. First table column contains association roles and second column association players.";
    }
    
    

    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf121);
    }
    
    
    @Override
    public boolean useTempTopicMap(){
        return false;
    }

    
    
    
    public static final String[] contentTypes=new String[] { "text/html" };
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    
    
    
    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        try {
            basePath = new URL(request.getSource());
        }
        catch(Exception e) { e.printStackTrace(); }
        String s = super.doBrowserExtract(request, wandora);
        basePath = null;
        return s;
    }

    
    
    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        basePath = url;
        boolean r = _extractTopicsFrom(url.openStream(),topicMap);
        basePath = null;
        return r;
    }
    
    
    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        basePath = file.toURI().toURL();
        boolean r = _extractTopicsFrom(new FileInputStream(file),topicMap);
        basePath = null;
        return r;
    }


    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
    }
    
    
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        HTMLDocument htmlDoc = new HTMLDocument();
        HTMLEditorKit.Parser parser = new HTMLParse().getParser();
        htmlDoc.setParser(parser);
        
        PropertyTableParseListener parserListener = new PropertyTableParseListener(topicMap, this);
        parser.parse(new InputStreamReader(in), parserListener, true);
        
        log("Total " + parserListener.associationCount + " associations and " + parserListener.playerCount + " association players created.");
        return true;
    }
    
    


    private String getSubjectFor(String str) {
        if(str != null) {
            if(str.startsWith("http:") || str.startsWith("https:") || str.startsWith("ftp:") || str.startsWith("ftps:")) {
                return str;
            }
            if(basePath != null) {
                if(str.startsWith("/")) {
                    return basePath.toExternalForm().substring(0, basePath.toExternalForm().indexOf(basePath.getPath()))+str;
                }
                return basePath.toExternalForm() + str;
            }
            if(SI_PREFIX.endsWith("/") && str.startsWith("/")) {
                str = str.substring(1);
            }
            String subjectPath = str;
            if(subjectPath.length() > 50) {
                subjectPath = subjectPath.substring(0,50);
                subjectPath = subjectPath + "-" + str.hashCode();
            }
            try {
                return SI_PREFIX + "/cell/" + URLEncoder.encode(subjectPath, "UTF-8");
            }
            catch(Exception e) {
                return SI_PREFIX + "/cell/" + subjectPath;
            }
        }
        return SI_PREFIX + "/cell/" + System.currentTimeMillis() + "-" + Math.round(Math.random() * 9999);
    }

    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    

    private static class HTMLParse extends HTMLEditorKit {
        /**
        * Call to obtain a HTMLEditorKit.Parser object.
        * @return A new HTMLEditorKit.Parser object.
        */
        @Override
        public HTMLEditorKit.Parser getParser() {
            HTMLEditorKit.Parser parser = super.getParser();
            return parser;
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private class PropertyTableParseListener extends HTMLEditorKit.ParserCallback {
        public int associationCount = 0;
        public int playerCount = 0;
        
        private TopicMap tm;
        private PropertyTableExtractor parent;
        
        private TableState state;
        private ArrayList<TableState> stateStack;
        
        private static final int STATE_OTHER=9999;

        private static final int STATE_TABLE=0;
        private static final int STATE_TBODY=1; // Not used at the moment
        private static final int STATE_TH=2; // Not used at the moment
        private static final int STATE_TR=3;
        private static final int STATE_TD=4;


        
        
        
        // -------------------------------------------------------------------------
        
        
        public PropertyTableParseListener(TopicMap tm, PropertyTableExtractor parent){
            this.tm = tm;
            this.parent = parent;
            this.state = null;
            this.stateStack = new ArrayList();
        }
        

        // -------------------------------------------------------------------------

        
        @Override
        public void handleStartTag(HTML.Tag t,MutableAttributeSet a,int pos) {
            if(t == HTML.Tag.TR) {
                if(state == null) state = new TableState();
                state.state = STATE_TR;
            }
            else if(t == HTML.Tag.TD) {
                if(state == null) state = new TableState();
                state.currentCell = null;
                state.currentLink = null;
                state.state = STATE_TD;
            }
            else if(t == HTML.Tag.TABLE) {
                if(state != null) stateStack.add(state);
                state = new TableState();
                state.state = STATE_TABLE;
            }
            else if(t == HTML.Tag.A) {
                if(a != null) {
                    if(state != null) {
                        state.currentLink = (String) a.getAttribute(HTML.Attribute.HREF);
                    }
                }
            }
        }
        


        @Override
        public void handleEndTag(HTML.Tag t,int pos) {
            if(t == HTML.Tag.TR) {
                if(state != null) {
                    if(state.cells != null && state.cells.size() > 1) {
                        try {
                            if(state.association == null) {
                                Topic associationTypeTopic = parent.createTopic(tm, getSubjectFor(state.associationTypeLink), state.associationType);
                                Topic tableRoot = tm.getTopic(new Locator(SI_PREFIX));
                                if(tableRoot == null) {
                                    tableRoot = tm.createTopic();
                                    tableRoot.addSubjectIdentifier(new Locator(SI_PREFIX));
                                    tableRoot.setBaseName("Table");
                                    Topic wandoraClass = parent.createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora Class");
                                    ExtractHelper.makeSubclassOf(tableRoot, wandoraClass, tm);
                                }
                                associationTypeTopic.addType(tableRoot);

                                state.association = tm.createAssociation(associationTypeTopic);
                                associationCount++;
                            }

                            Topic propertyNameTopic = parent.createTopic(tm, getSubjectFor(state.links.get(0)), state.cells.get(0));
                            Topic propertyValueTopic = parent.createTopic(tm, getSubjectFor(state.links.get(1)), state.cells.get(1));

                            state.association.addPlayer(propertyValueTopic, propertyNameTopic);
                            playerCount++;
                            
                            state.cells = new ArrayList();
                            state.links = new ArrayList();
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                    }
                    else if(state.cells.size() == 1) {
                        state.associationType = state.cells.get(0);
                        state.associationTypeLink = state.links.get(0);
                        state.cells = new ArrayList();
                        state.links = new ArrayList();
                    }
                    state.state = STATE_TABLE;
                }
            }
            else if(t == HTML.Tag.TD) {
                if(state != null) {
                    state.cells.add(state.currentCell);
                    state.links.add(state.currentLink);
                    state.currentCell = null;
                    state.currentLink = null;
                    state.state = STATE_TR;
                }
            }
            else if(t == HTML.Tag.TABLE) {
                if(state != null) {
                    state.association = null;
                    state.cells = new ArrayList();
                    state.links = new ArrayList();
                    state.state = STATE_OTHER;
                    if(!stateStack.isEmpty()) {
                        state = stateStack.remove(stateStack.size()-1);
                    }
                    else {
                        state = null;
                    }
                }
            }
        }
    

        @Override
        public void handleSimpleTag(HTML.Tag t,MutableAttributeSet a,int pos) {
            
        }
        


        @Override
        public void handleText(char[] data, int pos) {
            if(state != null) {
                switch(state.state) {
                    case STATE_TD: {
                        if(state.currentCell == null) state.currentCell = "";
                        state.currentCell = state.currentCell + new String(data);
                        break;
                    }
                }
            }
        }
        

        @Override
        public void handleError(String errorMsg,int pos) {
            // System.out.println("PropertyTableExtractor: " + errorMsg);
        }
        
        
        
        // ---------------------------------------------------------------------
        
        
        
        private class TableState {  
            public ArrayList<String> cells = new ArrayList();
            public ArrayList<String> links = new ArrayList();
            
            private String associationTypeSeed = System.currentTimeMillis() + "-" + Math.round(Math.random()*9999);
            public String associationType = "Table-"+associationTypeSeed;
            public String associationTypeLink = SI_PREFIX + "/" + associationTypeSeed;
            public String currentCell = null;
            public String currentLink = null;
            public int state = STATE_OTHER;
            public Association association = null;
        }
        
        
    }
    
}
