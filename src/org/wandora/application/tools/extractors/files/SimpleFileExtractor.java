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
 * SimpleDocumentExtractor.java
 *
 * Created on 31. lokakuuta 2007, 16:07
 *
 */

package org.wandora.application.tools.extractors.files;



import org.wandora.application.tools.browserextractors.*;
import org.wandora.topicmap.TMBox;
import org.wandora.utils.Textbox;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.utils.*;


import java.util.*;
import java.text.*;
import java.io.*;
import java.net.*;
import javax.swing.Icon;


import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;


/**
 *
 * @author akivela
 */
public class SimpleFileExtractor extends AbstractExtractor implements WandoraTool, BrowserPluginExtractor {
    protected static String TOPIC_SI = "http://wandora.org/si/topic";
    protected static String SOURCE_SI = "http://wandora.org/si/source";
    protected static String DOCUMENT_SI = "http://wandora.org/si/document";

    protected static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private String defaultLang = "en";
    private Wandora admin = null;

    private ArrayList<String> visitedDirectories = new ArrayList<String>();
    
    
    /** Creates a new instance of SimpleFileExtractor */
    public SimpleFileExtractor() {
    }
    
    
    @Override
    public String getName() {
        return "Simple File Extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Creates topics for given files. The file URI is set as topic's subject locator.";
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf016);
    }
    
    
    @Override
    public boolean useTempTopicMap(){
        return false;
    }
    
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    
    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select files(s) or directories containing files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking files!";
        
            case FILE_PATTERN: return ".*";
            
            case DONE_FAILED: return "Done! No extractions! %1 file(s) crawled!";
            case DONE_ONE: return "Done! Successful extraction! %1 file(s) crawled!";
            case DONE_MANY: return "Done! Total %0 successful extractions! %1 files crawled!";
            
            case LOG_TITLE: return "Simple File Extraction Log";
        }
        return "";
    }
    

    
    

    
    // ---------------------------------------------------- PLUGIN EXTRACTOR ---


    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        String url=request.getSource();
        try {
            TopicMap tm=wandora.getTopicMap();
            Topic theTopic=null;
            String content = request.getSelection();

            // SOURCE IS A FRACTION OF URL
            if(content!=null) {
                String tidyContent = XMLbox.cleanUp( content );
                if(tidyContent != null && tidyContent.length() > 0) {
                    content = XMLbox.getAsText(tidyContent, "ISO-8859-1");
                }

                Topic sourceTopic=tm.getTopicBySubjectLocator(url);
                if(sourceTopic==null) {
                    sourceTopic=tm.createTopic();
                    Locator l = tm.createLocator(url);
                    sourceTopic.addSubjectIdentifier(l);
                    sourceTopic.setSubjectLocator(l);
                }
                theTopic = tm.createTopic();
                theTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());

                Association a = tm.createAssociation(getSourceType(tm));
                a.addPlayer(theTopic, getDocumentType(tm));
                a.addPlayer(sourceTopic, getSourceType(tm));
            }
            // SOURCE IS A COMPLETE URL
            else {
                content = ExtractHelper.getContent(request);
                String tidyContent = XMLbox.cleanUp(content);
                if(tidyContent != null && tidyContent.length() > 0) {
                    content = XMLbox.getAsText(tidyContent, "ISO-8859-1");
                }

                theTopic=tm.getTopicBySubjectLocator(url);
                if(theTopic==null) {
                    theTopic=tm.createTopic();
                    Locator l = tm.createLocator(url);
                    theTopic.addSubjectIdentifier(l);
                    theTopic.setSubjectLocator(l);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }
        wandora.doRefresh();
        return null;
    }

    
    @Override
    public boolean browserExtractorConsumesPlainText() {
        return true;
    }




    // -------------------------------------------------------------------------



    
    @Override
    public void execute(Wandora wandora, Context context) {
        visitedDirectories = new ArrayList<String>();
        super.execute(wandora, context);
    }



    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        if(str == null || str.length() == 0) return false;

        try {
            int hash = str.hashCode();
            Topic documentType = this.getDocumentType(topicMap);
            String locator = "http://wandora.org/si/simple-file-extractor/"+hash;

            String name = null;
            if(str.length() > 80) {
                name = str.substring(0, 80) + " ("+hash+")";
            }
            else {
                name = str;
            }

            Topic documentTopic = topicMap.getTopic(locator);
            if(documentTopic == null) documentTopic = topicMap.createTopic();
            documentTopic.addSubjectIdentifier(new Locator( locator ));
            documentTopic.setBaseName(name);
            documentTopic.addType(documentType);

            if(DataURL.isDataURL(str)) {
                documentTopic.setSubjectLocator(new Locator(str));
            }
            
            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(documentTopic, extractionTimeType, defaultLang, dateString);

            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from input string.", e);
            takeNap(1000);
        }
        return false;
    }



    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null || url.toExternalForm().length() == 0) return false;
        
        try {
            Topic textType = this.getDocumentType(topicMap);
            String locator = url.toExternalForm();
            int hash = locator.hashCode();
            String name = url.getFile();
            if(name != null && name.length() > 0) {
                if(name.lastIndexOf("/") > -1) {
                    name = name.substring(name.lastIndexOf("/")+1);
                }
            }
            else {
                name = locator;
            }

            Topic documentTopic = topicMap.getTopic(locator);
            if(documentTopic == null) documentTopic = topicMap.createTopic();
            documentTopic.addSubjectIdentifier(new Locator( locator ));
            documentTopic.setBaseName(name + " ("+hash+")");
            documentTopic.setSubjectLocator(new Locator( locator ));
            documentTopic.addType(textType);

            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(documentTopic, extractionTimeType, defaultLang, dateString);

            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from url '" + url.toExternalForm()+"'.", e);
            takeNap(1000);
        }
        return false;
    }

    
    
    
    
    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file == null) return false;
        
        try {
            if(file.isDirectory()) {
                /*
                if(!visitedDirectories.contains(file.getAbsolutePath())) {
                    visitedDirectories.add(file.getAbsolutePath());
                    File[] fs = file.listFiles();
                    for(int i=0; i<fs.length && !forceStop(); i++) {
                        _extractTopicsFrom(fs[i], topicMap);
                    }
                }
                */
                return true;
            }
        }
        catch(Exception e) {
            log(e);
        }

        try {
            Topic textType = this.getDocumentType(topicMap);
            String locator = file.toURI().toURL().toExternalForm();
            int hash = locator.hashCode();

            Topic fileTopic = topicMap.getTopic(locator);
            if(fileTopic == null) fileTopic = topicMap.createTopic();
            fileTopic.addSubjectIdentifier(new Locator( locator ));
            fileTopic.setBaseName(file.getName() + " ("+hash+")");
            fileTopic.setSubjectLocator(new Locator( locator ));
            fileTopic.addType(textType);

            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(fileTopic, extractionTimeType, defaultLang, dateString);
            
            // --- ADD MODIFICATION TIME AS OCCURRENCE ---
            long lastModified = file.lastModified();
            Topic modificationTimeType = createTopic(topicMap, "modification-time");
            String modificationDateString = dateFormatter.format( new Date(lastModified) );
            setData(fileTopic, modificationTimeType, defaultLang, modificationDateString);
            
            // --- ADD ABSOLUTE FILE NAME AS OCCURRENCE ---
            Topic fileType = createTopic(topicMap, "file-name");
            String fileString = file.getAbsolutePath();
            setData(fileTopic, fileType, defaultLang, fileString);

            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from file '" + file.getName()+"'.", e);
            takeNap(1000);
        }
        return false;
    }


    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    

    public String solveTitle(String content) {
        if(content == null || content.length() == 0) return "empty-document";

        boolean forceTrim = false;
        String title = null;
        int i = content.indexOf("\n");
        if(i > 0) title = content.substring(0, i);
        else {
            title = content.substring(0, Math.min(80, content.length()));
            forceTrim = true;
        }

        if(title != null && (forceTrim || title.length() > 80)) {
            title = title.substring(0, Math.min(80, title.length()));
            while(!title.endsWith(" ") && title.length()>10) {
                title = title.substring(0, title.length()-1);
            }
            title = Textbox.trimExtraSpaces(title) + "...";
        }
        return title;
    }



    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }

    public Topic getTopicType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TOPIC_SI, "Topic");
    }

    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SOURCE_SI, "Source");
    }

    public Topic getDocumentType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, DOCUMENT_SI, "Document");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }

    // --------

    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }


    // -------------------------------------------------------------------------
    
    
    public static final String[] contentTypes=new String[] { "application/pdf", "text/plain", "text/html", "application/rtf", "application/xml", "application/msword" };
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    
}
