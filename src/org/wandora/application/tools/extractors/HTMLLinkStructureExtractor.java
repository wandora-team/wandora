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
 * HTMLLinkStructureExtractor.java
 *
 * Created on 28. toukokuuta 2006, 19:24
 *
 */

package org.wandora.application.tools.extractors;



import org.wandora.application.tools.browserextractors.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.piccolo.utils.crawler.*;

import java.io.*;
import java.net.*;
import javax.swing.*;



/**
 *
 * @author akivela
 */
public class HTMLLinkStructureExtractor extends AbstractExtractor implements WandoraTool, BrowserPluginExtractor {
   
    

	private static final long serialVersionUID = 1L;

	/** Creates a new instance of HTMLLinkStructureExtractor */
    public HTMLLinkStructureExtractor() {
    }
    
    
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR | RAW_EXTRACTOR;
    }
    @Override
    public String getName() {
        return "Extract HTML links...";
    }
    @Override
    public String getDescription(){
        return "Extracts HTML links out of given HTML pages, creates a topic for each HTML page and associates linked pages!";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf121);
    }
    
    

    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select file(s) or directories!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking links!";
        
            case FILE_PATTERN: return ".*";
            
            case DONE_FAILED: return "Ready. No extractions!";
            case DONE_ONE: return "Ready. Successful extraction!";
            case DONE_MANY: return "Ready. Total %0 successful extractions!";
            
            case LOG_TITLE: return "HTML Link Structure Extraction Log";
        }
        return "";
    }
    
    
    
    
    // ---------------------------------------------------- PLUGIN EXTRACTOR ---
    
    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        try {
            String url=request.getSource();
            String content=request.getSelection();
            if(content==null) {
                content=request.getContent();
            }
            TopicMap tm=wandora.getTopicMap();
            InputStream in=new ByteArrayInputStream(content.getBytes("ISO-8859-1"));

            HTMLParser p=new HTMLParser((String[]) null);
            try {
                p.parse(new URL(url), new InputStreamReader(in));
            }
            catch(IOException e) {
                return BrowserPluginExtractor.RETURN_ERROR+"Unable to extract links";
            }
            associateUrls(url, p.getNewURLs(), tm);
            wandora.doRefresh();
            return null;
        }
        catch(Exception e){
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }
    }

    
    @Override
    public boolean acceptBrowserExtractRequest(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        if(request.getContent()!=null) {
            return true;
        }
        else {
            return false;
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        throw(new Exception(STRING_EXTRACTOR_NOT_SUPPORTED_MESSAGE));
    }
    
    
    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file != null) {
            return _extractTopicsFrom(file.toURI().toURL(), topicMap);
        }
        return false;
    }


    

    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        HTMLParser p=new HTMLParser((String[]) null);
        try {
            URLConnection uc = null;
            if(getWandora() != null) {
                uc = getWandora().wandoraHttpAuthorizer.getAuthorizedAccess(url);
            }
            else {
                uc = url.openConnection();
                Wandora.initUrlConnection(uc);
            }
            p.parse(url, new InputStreamReader(uc.getInputStream()));
        }
        catch(IOException e) {
            return false;
        }
        associateUrls(url.toExternalForm(), p.getNewURLs(), topicMap);

        return true;
    }
    
    
    
    public void associateUrls(String url, URL[] linkedUrls, TopicMap topicMap) {
        try {
            Topic topic = getOrCreateTopic(topicMap, url, null);
            if(topic != null) {
                topic.setSubjectLocator(new Locator( url ));
                URL newUrl = null;
                Topic linkedTopic = null;
                for(int i=0; i<linkedUrls.length; i++) {
                    newUrl = linkedUrls[i];
                    if(newUrl != null) {
                        linkedTopic = getOrCreateTopic(topicMap, newUrl.toExternalForm(), null);
                        if(linkedTopic != null) {
                            linkedTopic.setSubjectLocator(new Locator( newUrl.toExternalForm() ));
                            Topic docType = getOrCreateTopic(topicMap, "http://wandora.org/si/document", "Document");
                            Topic linkType = getOrCreateTopic(topicMap, "http://wandora.org/si/link", "HTML link");
                            if(linkType != null) {
                                linkedTopic.addType(linkType);
                                makeSubclassOfWandoraClass(linkType, topicMap);
                            }
                            if(docType != null) {
                                topic.addType(docType);
                                makeSubclassOfWandoraClass(docType, topicMap);
                            }
                            if(docType != null && linkType != null) {
                                Association association = topicMap.createAssociation(linkType);
                                association.addPlayer(topic, docType);
                                association.addPlayer(linkedTopic, linkType);
                            }
                        }
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
                if(basename != null) {
                    topic.setBaseName(basename);
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return topic;
    }
    
  
    public String buildBaseName(File file) {
        return file.getPath();
    }
    
    
    @Override
    public Locator buildSI(String siend) {
        try {
            return new Locator(new File(siend).toURI().toString());
        }
        catch(Exception e) {
            return new Locator("file:/" + siend);
        }
    }
    
    

    private final String[] contentTypes=new String[] { "text/html" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }

 
}
