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
 */

package org.wandora.application.tools.extractors.list;



import org.wandora.application.tools.browserextractors.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.utils.*;


import java.util.*;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.text.html.*;
import javax.swing.text.*;
import org.w3c.tidy.*;

/**
 * <p>
 * Tool reads a HTML fragment with definition lists defined with
 * DL, DT and DD elements and creates topics and occurrences with
 * extracted information.
 * </p>
 * <p>
 * Extractor creates a topic for each faced definition list title (DT)
 * and adds following definitions (DD) as an occurrence to the previous
 * title topic. If there is multiple sequential definitions (multiple DD elements) following
 * the title, Wandora adds multiple numbered occurrences to the title
 * topic. If there is multiple sequential definition titles (multiple DT
 * elements), Wandora adds following definition occurrences to all these
 * title topics.
 * </p>

 * @author akivela
 */
public class DefinitionListExtractor extends AbstractExtractor implements WandoraTool, BrowserPluginExtractor {

    @Override
    public String getName() {
        return "HTML definition list extractor";
    }
    
    @Override
    public String getDescription() {
        return "Converts HTML lists to instance relations with definition occurrences.";
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
    public Icon getIcon() {
        return UIBox.getIcon(0xf121);
    }
    
    
        
    // -------------------------------------------------------------------------
  
    
    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(url.openStream(),topicMap);
    }
    
    
    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
    }
    

    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        Tidy tidy = null;
        String tidyHTML = null;
        
        try {
            Properties tidyProps = new Properties();
            tidyProps.put("trim-empty-elements", "no");
            
            tidy = new Tidy();
            tidy.setConfigurationFromProps(tidyProps);
            tidy.setXmlOut(false);
            tidy.setXmlPi(false);
            tidy.setTidyMark(false);

            ByteArrayOutputStream tidyOutput = null;
            tidyOutput = new ByteArrayOutputStream();       
            tidy.parse(in, tidyOutput);
            tidyHTML = tidyOutput.toString();
        }
        catch(Error er) {
            log("Unable to preprocess HTML with JTidy!");
            log(er);
        }
        catch(Exception e) {
            log("Unable to preprocess HTML with JTidy!");
            log(e);
        }
        if(tidyHTML == null) {
            log("Trying to read HTML without preprocessing!");
            tidyHTML = IObox.loadFile(new InputStreamReader(in));
        }

        HTMLDocument htmlDoc = new HTMLDocument();
        HTMLEditorKit.Parser parser = new HTMLParse().getParser();
        htmlDoc.setParser(parser);
        
        DefinitionListParseListener parserListener = new DefinitionListParseListener(topicMap, this);
        parser.parse(new InputStreamReader(new ByteArrayInputStream(tidyHTML.getBytes())), parserListener, true);
        parserListener.processCapturedData();
                
        log("Total " + parserListener.progress + " definitions created!");
        setState(WAIT);
        return true;
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
    private class DefinitionListParseListener extends HTMLEditorKit.ParserCallback {
        public int progress = 0;
        
        public static final String SI_PREFIX = "http://wandora.org/si/list/";
        
        private TopicMap tm;
        private DefinitionListExtractor parent;
        
        private int previousStartState=STATE_OTHER;
        private int state=STATE_OTHER;
        private static final int STATE_OTHER=9999;

        private static final int STATE_DL=0;
        private static final int STATE_DT=1;
        private static final int STATE_DD=2;
        
        private String currentName;
        private String currentUrl;
        private String currentDefinition;
        private HashMap<String,String> currentUrls;
        private ArrayList<String> currentNames;
        private ArrayList<String> currentDefinitions;
        
        private Topic currentTopic;
        private Topic parentTopic;
        private Topic definitionTypeTopic;
        private Topic definitionScopeTopic;
        
        private Stack stateStack;
        private Stack parentTopics;
        
        
        
        // -------------------------------------------------------------------------
        public DefinitionListParseListener(TopicMap tm, DefinitionListExtractor parent) {
            this.tm=tm;
            this.parent=parent;
            
            currentDefinition = null;
            currentName = null;
            currentUrl = null;
            currentNames = new ArrayList<String>();
            currentUrls = new HashMap<String,String>();
            currentDefinitions = new ArrayList<String>();   

            definitionTypeTopic = null;
            definitionScopeTopic = null;
            
            stateStack = new Stack();
            parentTopics = new Stack();
            
            parentTopic = null;
            try {
                Topic listRoot = createTopic(tm, SI_PREFIX, "List");
                Topic wandoraClass = createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora Class");
                ExtractHelper.makeSubclassOf(listRoot, wandoraClass, tm);

                long stamp = System.currentTimeMillis();
                parentTopic = createTopic(tm, SI_PREFIX + "list-"+stamp, "List-"+stamp);
                parentTopic.addType(listRoot);

                currentTopic = parentTopic;
            }
            catch(Exception e) {
            }
        }
        

        // -------------------------------------------------------------------------

        
        
        @Override
        public void handleStartTag(HTML.Tag t,MutableAttributeSet a,int pos) {
            if(t == HTML.Tag.DL) {
                processCapturedData();
                previousStartState = STATE_DL;
                stateStack.push(new Integer(state));
                state = STATE_DL;
            }
            else if(t == HTML.Tag.DT) {
                if(currentName != null) {
                    currentNames.add(currentName);
                    if(currentUrl != null) {
                        currentUrls.put(currentName, currentUrl);
                        currentUrl = null;
                    }
                    currentName = null;
                }
                if(previousStartState == STATE_DD) {
                    processCapturedData();
                }
                previousStartState = STATE_DT;
                stateStack.push(new Integer(state));
                state = STATE_DT;
            }
            else if(t == HTML.Tag.DD) {
                if(currentDefinition != null) {
                    currentDefinitions.add(currentDefinition);
                    currentDefinition = null;
                }
                previousStartState = STATE_DD;
                stateStack.push(new Integer(state));
                state = STATE_DD;
            }
            else if(t == HTML.Tag.A) {
                if(state == STATE_DT) {
                    try {
                        currentUrl = (String) a.getAttribute(HTML.Attribute.HREF);
                    }
                    catch(Exception e) {
                        // IGNORE
                    }
                }
            }
        }
        

        @Override
        public void handleEndTag(HTML.Tag t,int pos) {
            if(t == HTML.Tag.DL) {
                processCapturedData();
                popState();
            }
            else if(t == HTML.Tag.DT) {
                popState();
            }
            else if(t == HTML.Tag.DD) {
                popState();
            }
        }
        
        
              
    
               
        @Override
        public void handleSimpleTag(HTML.Tag t,MutableAttributeSet a,int pos) {
            
        }
        
        
        @Override
        public void handleText(char[] data,int pos) {
            switch(state) {
                case STATE_DT: {
                    if(currentName == null) currentName = "";
                    currentName = currentName + new String(data);
                    break;
                }
                case STATE_DD: {
                    if(currentDefinition == null) currentDefinition = "";
                    currentDefinition = currentDefinition + new String(data);
                    break;
                }
            }
        }
        
        
        @Override
        public void handleError(String errorMsg,int pos) {
            System.out.println("DefinitionListExtractor: " + errorMsg);
        }
        
        
        
        // --------------------
        
        
        
        
        public void processCapturedData() {
            if(currentName != null) {
                currentNames.add(currentName);
                if(currentUrl != null) {
                    currentUrls.put(currentName, currentUrl);
                    currentUrl = null;
                }
                currentName = null;
            }
            if(currentDefinition != null) {
                currentDefinitions.add(currentDefinition);
                currentDefinition = null;
            }
            
            if(currentNames.size() > 0) {
                for(Iterator<String> names = currentNames.iterator(); names.hasNext(); ) {
                    currentName = names.next();
                    if(currentName == null) continue;
                    currentName = currentName.trim();
                    if(currentName.length() > 0) {
                        try {
                            // MAKE TOPIC FOR CURRENT TOPIC NAME AND ASSOCIATE NEW TOPIC TO PARENT
                            currentTopic = createTopic(tm, SI_PREFIX + currentName, currentName);
                            currentUrl = currentUrls.get(currentName);
                            if(currentUrl != null) {
                                if(currentUrl.startsWith("http://") || currentUrl.startsWith("https://") || currentUrl.startsWith("ftp://") || currentUrl.startsWith("ftps://") || currentUrl.startsWith("mailto://")) {
                                    try {
                                        currentTopic.addSubjectIdentifier(new Locator(currentUrl));
                                    }
                                    catch(Exception e) {
                                        log(e);
                                    }
                                }
                                currentUrl = null;
                            }

                            if(parentTopic != null) {
                                currentTopic.addType(parentTopic);                           
                            }

                            if(currentDefinitions.size() > 0) {
                                int defcount = 0;
                                String definitionType = null;
                                for(Iterator<String> definitions=currentDefinitions.iterator(); definitions.hasNext();) {
                                    currentDefinition = definitions.next();
                                    if(currentDefinition != null) {
                                        currentDefinition = currentDefinition.trim();
                                        if(currentDefinition.length() > 0) {
                                            definitionType = "Definition" + ( defcount>0 ? " "+(1+defcount) : "" );
                                            definitionTypeTopic = createTopic(tm, SI_PREFIX + definitionType, definitionType);
                                            if(definitionScopeTopic == null) definitionScopeTopic = createTopic(tm, XTMPSI.getLang("en"), "Language EN");
                                            currentTopic.setData(definitionTypeTopic, definitionScopeTopic, currentDefinition);
                                            log("Creating definition "+( defcount>0 ? "("+(1+defcount)+") " : "" )+"occurrence for '"+getTopicName(currentTopic)+"'.");
                                            progress++;
                                            defcount++;
                                        }
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                }
            }
            currentName = null;
            currentDefinition = null;
            currentUrl = null;
            currentNames = new ArrayList<String>();
            currentDefinitions = new ArrayList<String>();
            currentUrls = new HashMap<String,String>();
        }
        
        
        
        
        private void popStateAndParent() {
            popState();
            popParent();
        }
        
        
        private void popState() {
            if(!stateStack.empty()) {
                state = ((Integer) stateStack.pop()).intValue();
            }
            else {
                state = STATE_OTHER;
            }
        }


        private void popParent() {
            currentTopic = parentTopic;
            if(!parentTopics.empty()) {
                parentTopic = (Topic) parentTopics.pop();
            }
            else {
                parentTopic = null;
            }
        }


        public Topic createTopic(TopicMap topicMap, String si, String baseName) throws TopicMapException {
            return createTopic(topicMap, si, baseName, null);
        }

        
        

        public Topic createTopic(TopicMap topicMap, String si, String baseName, Topic[] types) throws TopicMapException {
            Topic t = null;
            if(baseName != null && baseName.length() > 0 && si != null && si.length() > 0) {
                si = TopicTools.cleanDirtyLocator(si);
                t = topicMap.getTopic(si);
                if(t == null) {
                    t = topicMap.getTopicWithBaseName(baseName);
                    if(t == null) {
                        t = topicMap.createTopic();
                        t.setBaseName(baseName);
                    }
                    t.addSubjectIdentifier(new org.wandora.topicmap.Locator(si));
                }
                if(types != null) {
                    for(int i=0; i<types.length; i++) {
                        Topic typeTopic = types[i];
                        if(typeTopic != null) {
                            t.addType(typeTopic);
                        }
                    }
                }
            }
            if(t == null) {
                System.out.println("Failed to create topic for basename '"+baseName+"' and si '"+si+"'.");
            }
            return t;
        }

        
    }
    
}
