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
 * DirectoryStructureExtractor.java
 *
 * Created on 15. toukokuuta 2006, 15:39
 *
 */

package org.wandora.application.tools.extractors.files;


import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Icon;

import org.wandora.application.WandoraTool;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

        

/**
 *
 * @author akivela
 */
public class DirectoryStructureExtractor extends AbstractExtractor implements WandoraTool {


	private static final long serialVersionUID = 1L;

	
	private String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private String defaultLang = "en";
    public static boolean USE_SUPERSUBCLASS_RELATION = true;



    

    /** Creates a new instance of DirectoryStructureExtractor */
    public DirectoryStructureExtractor() {
    }
    
    

    private String baseLocator = "http://wandora.org/si/directory-structure-extractor/";


    
   
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR;
    }
    
    
    @Override
    public String getName() {
        return "Extract directory structure";
    }
    
    
    @Override
    public String getDescription(){
        return "Converts given directory stucture to a topic map where directories are associated with files and directories within.";
    }
    

    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf114);
    }
    
    
    
    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select file(s) or directories!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking files!";
        
            case FILE_PATTERN: return ".*";
            
            case DONE_FAILED: return "Ready. No extractions! %2 file(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %2 file(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %2 files crawled!";
            
            case LOG_TITLE: return "Directory Structure Extraction Log";
        }
        return "";
    }
    




    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        
        if(file != null) {
            try {
                Topic dirType = createTopic(topicMap, baseLocator+"directory", "Directory");
                Topic fileType = createTopic(topicMap, baseLocator+"file", "File");

                Topic wandoraClass = getWandoraClass(topicMap);
                makeSubclassOf(topicMap, dirType, wandoraClass);
                makeSubclassOf(topicMap, fileType, wandoraClass);

                Topic fileTopic = null;
                String location = file.toURI().toURL().toExternalForm();
                String name = buildBaseName(file);
                Topic typeTopic = null;
                if(file.isDirectory()) {
                    typeTopic = dirType;
                }
                else {
                    typeTopic = fileType;
                }
                fileTopic = createTopic(topicMap, location, name, typeTopic );
                
                // --- SET FILE TOPIC'S VARIANT NAMES ---
                setDisplayName(fileTopic, defaultLang, file.getName());
                fileTopic.setSubjectLocator(new Locator( location ));
                
                // --- ADD LAST MODIFICATION TIME AS OCCURRENCE ---
                try {
                    DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
                    Topic modType = createTopic(topicMap, baseLocator+"file-modified", "file-modified");
                    String dateString = dateFormatter.format( new Date(file.lastModified()) );
                    setData(fileTopic, modType, defaultLang, dateString);
                }
                catch(Exception e) {
                    log("Exception occurred while setting file topic's modification time!", e);
                }
                
                // --- ADD FILE SIZE AS OCCURRENCE ---
                try {
                    Topic sizeType = createTopic(topicMap, baseLocator+"file-size", "file-size");
                    setData(fileTopic, sizeType, defaultLang, ""+file.length());
                }
                catch(Exception e) {
                    log("Exception occurred while setting file topic's file size!", e);
                }
                
                // --- ADD TYPES IF FILE IS READABLE/WRITEABLE/HIDDEN ---
                try {
                    if(file.canRead()) {
                        Topic accessRightsType = createTopic(topicMap, baseLocator+"accessibility", "accessibility");
                        Topic readableFileType = createTopic(topicMap, baseLocator+"readable-file", "readable-file");
                        createAssociation(topicMap, accessRightsType, new Topic[] { fileTopic, readableFileType }, new Topic[] { fileType, accessRightsType } );
                    }
                    if(file.canWrite()) {
                        Topic accessRightsType = createTopic(topicMap, baseLocator+"accessibility", "accessibility");
                        Topic writeableFileType = createTopic(topicMap, baseLocator+"writeable-file", "writeable-file");
                        createAssociation(topicMap, accessRightsType, new Topic[] { fileTopic, writeableFileType }, new Topic[] { fileType, accessRightsType } );
                    }
                    if(file.isHidden()) {
                        Topic accessRightsType = createTopic(topicMap, baseLocator+"accessibility", "accessibility");
                        Topic hiddenFileType = createTopic(topicMap, baseLocator+"hidden-file", "hidden-file");
                        createAssociation(topicMap, accessRightsType, new Topic[] { fileTopic, hiddenFileType }, new Topic[] { fileType, accessRightsType } );
                    }
                }
                catch(Exception e) {
                    log("Exception occurred while solving file's accessibility!", e);
                }

                
                // --- ADD ASSOCIATION TO PARENT FILE ---
                if(file.getParentFile() != null) {
                    File parent = file.getParentFile();
                    if(parent != null && parent.exists()) {
                        Topic parentDirectoryTopic = createTopic(topicMap, buildBaseName(parent), dirType );
                        setDisplayName(parentDirectoryTopic, defaultLang, parent.getName());
                        if(USE_SUPERSUBCLASS_RELATION) {
                            makeSubclassOf(topicMap, fileTopic, parentDirectoryTopic);
                        }
                        else {
                            Topic locatedType = createTopic(topicMap, baseLocator+"is-located", "is-located");
                            createAssociation(topicMap, locatedType, new Topic[] { fileTopic, parentDirectoryTopic }, new Topic[] { fileType, dirType } );
                        }
                    }
                }

                // --- ADD EXTRACTION TIME AS OCCURRENCE ---
                DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
                Topic extractionTimeType = createTopic(topicMap, "extraction-time");
                String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
                setData(fileTopic, extractionTimeType, defaultLang, dateString);

                return true;
            }
            catch (Exception e) {
                log(e);
                e.printStackTrace();
            }
        }
        return false;
    }




    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        throw(new Exception(STRING_EXTRACTOR_NOT_SUPPORTED_MESSAGE));
    }
    
    // THIS TOOL SUPPORTS NO URL EXTRACTING!
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return false;
    }




    // -------------------------------------------------------------------------
    

    

    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }

    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    // -----------
    
  
    public String buildBaseName(File file) {
        if(file == null) return "[null]";
        return file.getName() + " ("+file.getAbsolutePath().hashCode()+")";
    }
    
    
    @Override
    public Locator buildSI(String siend) {
        if(siend.startsWith("http://")) {
            return new Locator(siend);
        }
        else if(siend.startsWith("file:/")) {
            return new Locator(siend);
        }
        else {
            try {
                return new Locator(new File(siend).toURI().toString());
            }
            catch(Exception e) {
                return new Locator("file:/" + siend);
            }
        }
    }
    
    
    
 
}
