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
 * AssociationRowTableExtractor.java
 *
 * Created on 7. marraskuuta 2007, 12:54
 *
 */

package org.wandora.application.tools.extractors.table;

import org.wandora.application.tools.extractors.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import java.util.*;
import java.io.*;
import java.net.*;

import javax.swing.text.html.*;
import javax.swing.text.*;
import javax.swing.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;



/**
 * Converts an HTML table to a set of associations. Each table row (except first row)
 * represents one association. First row defines role topics used in associations.
 *
 * @author akivela
 */
public class AssociationRowTableExtractor extends AbstractExtractor implements WandoraTool {
   

    private URL basePath = null;
    public static String SI_PREFIX = "http://wandora.org/si/table";
    
    
    /** Creates a new instance of AssociationRowTableExtractor */
    public AssociationRowTableExtractor() {
    }

    
    
    @Override
    public String getName() {
        return "Association tow table extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Converts HTML tables to associations. "+
                "Extractor creates one association for each table row. "+
                "First row contains association roles.";
    }
    
    
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_html.png");
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
        
        AssociationRowTableParseListener parserListener = new AssociationRowTableParseListener(topicMap, this);
        parser.parse(new InputStreamReader(in), parserListener, true);
        
        log("Total " + parserListener.associationCount + " associations created for " + parserListener.tableCount + " tables.");
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
            return super.getParser();
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    private class AssociationRowTableParseListener extends HTMLEditorKit.ParserCallback {
        public boolean ADD_TABLE_AS_PLAYER = false;
        public boolean USE_TABLE_AS_TYPE = true;
        public boolean FIRST_ROW_CONTAINS_ROLES = true;
        
        public int tableCount = 0;
        public int associationCount = 0;

        private TopicMap tm;
        private AssociationRowTableExtractor parent;
        
        private TableState state;
        private ArrayList<TableState> stateStack;
        
        private static final int STATE_OTHER=9999;

        private static final int STATE_TABLE=0;
        private static final int STATE_TBODY=1;
        private static final int STATE_TH=2;
        private static final int STATE_TR=3;
        private static final int STATE_TD=4;

        private String defaultAssociationType;


        
        // -------------------------------------------------------------------------
        
        
        public AssociationRowTableParseListener(TopicMap tm, AssociationRowTableExtractor parent){
            this.tm = tm;
            this.parent = parent;
            defaultAssociationType = "default-association-type";
            state = null;
            stateStack = new ArrayList();
        }
        

        // -------------------------------------------------------------------------

        
        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if(t == HTML.Tag.TR) {
                if(state == null) {
                    tableCount++;
                    state = new TableState();
                }
                state.cellCount = 0;
                state.state = STATE_TR;
            }
            else if(t == HTML.Tag.TH) {
                if(state == null) {
                    tableCount++;
                    state = new TableState();
                }
                state.cellString = "";
                state.cellLink = "";
                state.state = STATE_TH;
            }
            else if(t == HTML.Tag.TD) {
                if(state == null) {
                    tableCount++;
                    state = new TableState();
                }
                state.cellString="";
                state.cellLink="";
                state.state = STATE_TD;
            }
            else if(t == HTML.Tag.TABLE) {
                if(state != null) {
                    stateStack.add(state);
                }
                tableCount++;
                state = new TableState();
                state.state = STATE_TABLE;
            }
            else if(t == HTML.Tag.A) {
                if(state!= null && a != null) {
                    state.cellLink = (String) a.getAttribute(HTML.Attribute.HREF);
                }
            }
        }
        

        @Override
        public void handleEndTag(HTML.Tag t,int pos) {
            if(t == HTML.Tag.TR) {
                if(state != null) {
                    state.rowCount++;
                    state.state = STATE_TABLE;
                    if(FIRST_ROW_CONTAINS_ROLES && state.rowCount == 1) { // FIRST ROW!
                        state.roles = state.row;
                        state.roleLinks = state.links;
                        state.row = new ArrayList();
                        state.links = new ArrayList();
                        return;
                    }
                    if(state.row.size() > 0) {
                        try {
                            Association association = null;

                            Topic tableRole = tm.getTopic(new Locator(SI_PREFIX));
                            if(tableRole == null) {
                                tableRole = tm.createTopic();
                                tableRole.addSubjectIdentifier(new Locator(SI_PREFIX));
                                tableRole.setBaseName("Table");
                                Topic wandoraClass = parent.createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora Class");
                                ExtractHelper.makeSubclassOf(tableRole, wandoraClass, tm);
                            }
                            Topic tableTopic = tm.getTopic(new Locator(SI_PREFIX + "/" + state.tableIdentifier));
                            if(tableTopic == null) {
                                tableTopic = tm.createTopic();
                                tableTopic.addSubjectIdentifier(new Locator(SI_PREFIX + "/" + state.tableIdentifier));
                                tableTopic.setBaseName("Table-"+state.tableIdentifier);
                                tableTopic.addType(tableRole);
                            }

                            if(USE_TABLE_AS_TYPE) {
                                association = tm.createAssociation(tableTopic);
                                associationCount++;
                            }
                            else {
                                Topic associationTypeTopic = TMBox.getOrCreateTopic(tm, defaultAssociationType);
                                association = tm.createAssociation(associationTypeTopic);
                                associationCount++;
                            }
                            if(ADD_TABLE_AS_PLAYER) {
                                association.addPlayer(tableTopic, tableRole);
                            }
                            int c=0;
                            for(String player : state.row) {
                                try {
                                    String link = tm.makeSubjectIndicatorAsLocator().toExternalForm();
                                    String role = "table-role-" + state.tableIdentifier + "-" + (c+1);
                                    String roleLink = SI_PREFIX + "/" + state.tableIdentifier + "/role-" + (c+1);
                                    // Next we solve name of the role topic.
                                    if(state.roles != null && state.roles.size() > c) {
                                        String proposedRole = state.roles.get(c);
                                        if(proposedRole != null && proposedRole.length() > 0) {
                                            role = proposedRole;
                                        }
                                    }
                                    // Next we solve the subject identifier for the role topic
                                    if(state.roleLinks != null && state.roleLinks.size() > c) {
                                        String proposedRoleLink = state.roleLinks.get(c);
                                        if(proposedRoleLink != null && proposedRoleLink.length() > 0) {
                                            roleLink = parent.getSubjectFor(proposedRoleLink);
                                        }
                                    }
                                    // Next we are going to solve the subject identifier if possible
                                    if(state.links != null && state.links.size() > c) {
                                        String proposedLink = state.links.get(c);
                                        if(proposedLink != null && proposedLink.length() > 0) {
                                            link = parent.getSubjectFor(proposedLink);
                                        }
                                    }
                                    // If both role and player are valid lets create an association
                                    if(role != null && role.length() > 0 && player != null && player.length() > 0) {
                                        Topic roleTopic = parent.createTopic(tm, roleLink, role);
                                        Topic playerTopic = parent.createTopic(tm, link, player);
                                        association.addPlayer(playerTopic, roleTopic);
                                    }
                                    c++;
                                }
                                catch(Exception e) {
                                    parent.log(e);
                                }
                            }
                            state.row = new ArrayList();
                            state.links = new ArrayList();
                        }
                        catch(Exception e) {
                            parent.log(e);
                        }
                    }
                }
            }
            else if(t == HTML.Tag.TD || t == HTML.Tag.TH) {
                if(state != null) {
                    state.row.add(state.cellString);
                    state.links.add(state.cellLink);
                    state.cellString = "";
                    state.cellLink = "";
                    state.cellCount++;
                    state.state = STATE_TR;
                }
            }
            else if(t == HTML.Tag.TABLE) {
                state.state = STATE_OTHER;
                if(!stateStack.isEmpty()) {
                    state = stateStack.remove(stateStack.size()-1);
                }
                else {
                    state = null;
                }
            }
        }
    

        @Override
        public void handleSimpleTag(HTML.Tag t,MutableAttributeSet a,int pos) {
            
        }
        

        @Override
        public void handleText(char[] data,int pos) {
            if(state != null) {
                switch(state.state) {
                    case STATE_TD: {
                        state.cellString = state.cellString + new String(data);
                        break;
                    }
                    case STATE_TH: {
                        state.cellString = state.cellString + new String(data);
                        break;
                    }
                }
            }
        }
        

        @Override
        public void handleError(String errorMsg,int pos) {
            System.out.println("TableExtractor: " + errorMsg);
        }
        

        
        
        
        private class TableState {
            public int state = STATE_OTHER;
            public ArrayList<String> roles = new ArrayList();
            public ArrayList<String> roleLinks = new ArrayList();
            public ArrayList<String> row = new ArrayList();
            public ArrayList<String> links = new ArrayList();
            public String cellString = "";
            public String cellLink = "";
            public int cellCount = 0;
            public int rowCount = 0;
            public String tableIdentifier = System.currentTimeMillis() + "-" + Math.round(Math.random()*9999);
            
        }
        
        
    }
}
