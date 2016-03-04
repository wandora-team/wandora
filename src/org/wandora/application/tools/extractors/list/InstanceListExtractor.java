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
 * */



package org.wandora.application.tools.extractors.list;


import org.wandora.application.tools.browserextractors.*;
import org.wandora.utils.IObox;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.tools.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.*;
import org.wandora.utils.*;


import java.util.*;
import java.text.*;
import java.lang.*;
import java.io.*;
import java.net.*;

import javax.swing.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.*;
import org.w3c.tidy.*;

/**
/**
 * <p>
 * Reads HTML document or HTML fragment and converts included HTML list structure
 * to a Topic Map. A topic is created for each list element (content of LI element).
 * Created topic is related to outer list element with instance-of relation.
 * Outer element is considered as a type. As an example consider simple list:
 * </p>
 * 
 * <ul>
 * <li>Movies</li>
 * <ul>
 * <li>Blade Runner</li>
 * <li>2001: A Space Odyssey</li>
 * <li>Spaceballs</li>
 * </ul>
 * <li>Directors</li>
 * <ul>
 * <li>Ridley Scott</li>
 * <li>Stanley Kubrick<li>
 * <li>Mel Brooks<li>
 * </ul>
 * </ul>
 * 
 * @author akivela
 */
public class InstanceListExtractor extends AbstractExtractor implements WandoraTool, BrowserPluginExtractor {

    private URL basePath = null;
    private static int listCounter = 0;
    
    
    
    
    @Override
    public String getName() {
        return "HTML instance list extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Converts HTML lists to instance relations.";
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
        
        ListParseListener parserListener = new ListParseListener(topicMap, this);
        parser.parse(new InputStreamReader(new ByteArrayInputStream(tidyHTML.getBytes())), parserListener, true);
        
        log("Total " + parserListener.progress + " instance relations created!");
        setState(WAIT);
        basePath = null;
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
    
    
    
    private class ListParseListener extends HTMLEditorKit.ParserCallback {
        public int progress = 0;
        
        public static final String SI_PREFIX = "http://wandora.org/si/list/";
        
        private TopicMap tm;
        private InstanceListExtractor parent;
        
        private int state=STATE_OTHER;
        private static final int STATE_OTHER=9999;

        private static final int STATE_UL=0;
        private static final int STATE_OL=1;
        private static final int STATE_LI=2;
        
        private String currentTopicUrl;
        private String currentTopicName;
        private Topic currentTopic;
        private Topic parentTopic;
        private String listId;

        private Stack stateStack;
        private Stack parentTopics;
        
        private Topic listRoot = null;
        
        
        // ---------------------------------------------------------------------
        
        
        
        public ListParseListener(TopicMap tm, InstanceListExtractor parent) {
            this.tm=tm;
            this.parent=parent;
            currentTopicName = null;
            currentTopicUrl = null;
                      
            stateStack = new Stack();
            parentTopics = new Stack();
            
            parentTopic = null;
            currentTopic = null;
            
            listId = null;
            
            try {
                listRoot = createTopic(tm, SI_PREFIX, "List");
                Topic wandoraClass = createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora Class");
                ExtractHelper.makeSubclassOf(listRoot, wandoraClass, tm);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        

        // -------------------------------------------------------------------------

        
        
        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if(t == HTML.Tag.UL) {
                if(parentTopic != null) {
                    parentTopics.push(parentTopic);
                }
                try {
                    if(state != STATE_OTHER && currentTopic != null) {
                        processCapturedData();
                        parentTopic = currentTopic;
                        parentTopic.addType(listRoot);
                    }
                    else {
                        parentTopic = createListTopic();
                    }
                }
                catch(Exception e) {}
                stateStack.push(new Integer(state));
                state = STATE_UL;
            }
            else if(t == HTML.Tag.OL) {
                if(parentTopic != null) {
                    parentTopics.push(parentTopic);
                }
                try {
                    if(state != STATE_OTHER && currentTopic != null) {
                        processCapturedData();
                        parentTopic = currentTopic;
                        parentTopic.addType(listRoot);
                    }
                    else {
                        parentTopic = createListTopic();
                    }
                }
                catch(Exception e) {}
                stateStack.push(new Integer(state));
                state = STATE_OL;
            }
            else if(t == HTML.Tag.LI) {
                if(parentTopic == null) {
                    parentTopic = createListTopic();
                }
                currentTopicUrl = null;
                stateStack.push(new Integer(state));
                state = STATE_LI;
            }
            else if(t == HTML.Tag.A) {
                try {
                    currentTopicUrl = (String) a.getAttribute(HTML.Attribute.HREF);
                }
                catch(Exception e) {
                    // IGNORE
                }
            }
        }
        

        
        
        
        @Override
        public void handleEndTag(HTML.Tag t,int pos) {
            if(t == HTML.Tag.LI) {
                processCapturedData();
                popState();
            }
            else if(t == HTML.Tag.UL) {
                popState();
                popParent();
            }
            else if(t == HTML.Tag.OL) {
                popState();
                popParent();
            }
        }
        
        
              
    
               
        @Override
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            
        }
        
        
        @Override
        public void handleText(char[] data, int pos) {
            switch(state) {
                case STATE_LI: {
                    if(currentTopicName == null) currentTopicName = "";
                    currentTopicName = currentTopicName + new String(data);
                    break;
                }
            }
        }
        
        
        @Override
        public void handleError(String errorMsg,int pos) {
            // System.out.println("InstanceListExtractor: " + errorMsg);
        }
        
        
        
        // ---------------------------------------------------------------------
        
        
        
        
        
        private Topic createListTopic() {
            Topic t = null;
            try {
                long stamp = System.currentTimeMillis();
                listCounter++;
                t = createTopic(tm, SI_PREFIX+"list-"+stamp+"-"+listCounter, "List-"+stamp+"-"+listCounter);
                t.addType(listRoot);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return t;
        }
        
        
        private void processCapturedData() {
            if(currentTopicName != null) {
                currentTopicName = currentTopicName.trim();
                if(currentTopicName.length() > 0) {
                    try {
                        // MAKE TOPIC FOR CURRENT TOPIC NAME AND ASSOCIATE NEW TOPIC TO PARENT
                        currentTopic = createListItemTopic(tm, getSubjectForListItem(), currentTopicName);

                        if(parentTopic != null) {
                            currentTopic.addType(parentTopic);
                            log("Creating instance relation '"+getTopicName(parentTopic)+"' -- '"+getTopicName(currentTopic)+"'.");
                            progress++;
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                    currentTopicName = null;
                    currentTopicUrl = null;
                }
            }
        }
        
        
        
        private String getSubjectForListItem() {
            if(currentTopicUrl != null) {
                if(currentTopicUrl.startsWith("http:") || currentTopicUrl.startsWith("https:") || currentTopicUrl.startsWith("ftp:") || currentTopicUrl.startsWith("ftps:")) {
                    return currentTopicUrl;
                }
                if(basePath != null) {
                    if(currentTopicUrl.startsWith("/")) {
                        return basePath.toExternalForm().substring(0, basePath.toExternalForm().indexOf(basePath.getPath()))+currentTopicUrl;
                    }
                    return basePath.toExternalForm() + currentTopicUrl;
                }
                if(SI_PREFIX.endsWith("/") && currentTopicUrl.startsWith("/")) {
                    currentTopicUrl = currentTopicUrl.substring(1);
                }
                return SI_PREFIX + "item/" + currentTopicUrl;
            }
            String subjectPath = currentTopicName;
            if(subjectPath.length() > 50) {
                subjectPath = subjectPath.substring(0,50);
                subjectPath = subjectPath + "-" + currentTopicName.hashCode();
            }
            try {
                return SI_PREFIX + "item/" + URLEncoder.encode(subjectPath, "UTF-8");
            }
            catch(Exception e) {
                return SI_PREFIX + "item/" + subjectPath;
            }
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
        
        
        
        public Topic createListItemTopic(TopicMap topicMap, String si, String listItem) throws TopicMapException {
            Topic t = createTopic(topicMap, si, listItem, null);
            
            try {
                Topic occurrenceType = topicMap.createTopic();
                occurrenceType.addSubjectIdentifier(new Locator(SI_PREFIX + "item-text"));
                occurrenceType.setBaseName("List item text");
                Topic lang = topicMap.getTopic(XTMPSI.LANG_INDEPENDENT);
                if(lang == null) lang = topicMap.getTopic(XTMPSI.getLang("en"));
                if(lang != null) {
                    t.setData(occurrenceType, lang, listItem);
                }
            }
            catch(Exception e) {}
            
            return t;
        }


        public Topic createTopic(TopicMap topicMap, String si, String baseName) throws TopicMapException {
            return createTopic(topicMap, si, baseName, null);
        }

        
        

        public Topic createTopic(TopicMap topicMap, String si, String baseName, Topic[] types) throws TopicMapException {
            Topic t = null;
            if(baseName != null && baseName.length() > 0 && si != null && si.length() > 0) {
                if(baseName.length() > 512) baseName = baseName.substring(0, 509) + "...";
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
                log("Failed to create topic for a basename '"+baseName+"' and a subject '"+si+"'.");
            }
            return t;
        }

        
    }
    
}
