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
 * DirectoryStructureExtractor2.java
 *
 *
 */

package org.wandora.application.tools.extractors.files;


import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import javax.swing.Icon;
import org.wandora.application.gui.UIBox;

        

/**
 *
 * @author akivela
 */
public class DirectoryStructureExtractor2 extends AbstractExtractor implements WandoraTool {


	private static final long serialVersionUID = 1L;

	private String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    
    private String defaultLang = "en";
    private String baseLocator = "http://wandora.org/si/file/";
    private int progress = 0;


    

    /** Creates a new instance of DirectoryStructureExtractor2 */
    public DirectoryStructureExtractor2() {
    }
    
    

   
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
    public boolean useURLCrawler() {
        return false;
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
            
            case DONE_FAILED: return "Ready. Extraction failed!";
            case DONE_ONE: return "Ready.";
            case DONE_MANY: return "Ready.";
            
            case LOG_TITLE: return "Directory structure extraction log";
        }
        return "";
    }
    

    @Override
    public void handleFiles(File[] files, TopicMap tm) {
        if(files == null || files.length == 0) return;
        for(int i=0; i<files.length && !forceStop(); i++) {
            if(files[i] != null) {
                // FILES
                if(files[i].isFile()) {
                    try {
                        extractTopicsFrom(files[i], tm);
                    }
                    catch(Exception e) {
                        log(e);
                        log("Extraction from '" + croppedFilename(files[i].getName()) + "' failed.");
                        takeNap(1000);
                    }
                }
                // DIRECTORIES
                if(files[i].isDirectory()) {
                    try {
                        extractTopicsFrom(files[i], tm);
                    }
                    catch (Exception e) {
                        log(e);
                        takeNap(1000);
                    }
                }
            }
        }
     }


    @Override
    public boolean _extractTopicsFrom(File file, TopicMap tm) throws Exception {
        progress = 0;
        Topic rootTopic = iterateFile(file, tm);
        Topic extractionType = createTopic(tm, baseLocator+"extraction-root", "File extraction root");
        if(rootTopic != null && extractionType != null) {
            rootTopic.addType(extractionType);
            Topic wandoraClass = getWandoraClass(tm);
            if(wandoraClass != null) {
                makeSubclassOf(tm, extractionType, wandoraClass);
            }
        }
        return true;
        
    }
    
    
    
    public Topic iterateFile(File file, TopicMap tm) throws Exception {
        if(forceStop()) return null;
        Topic fileTopic = extractFile(file, tm);
        if(file.isDirectory()) {
            File[] filesWithin = file.listFiles();
            for(int i=0; i<filesWithin.length; i++) {
                Topic fileTopicWithin = iterateFile(filesWithin[i], tm);
                if(fileTopicWithin != null) {
                    fileTopicWithin.addType(fileTopic);
                }
                if(forceStop()) break;
            }
        }
        return fileTopic;
    }
    

    
    
    
    public Topic extractFile(File file, TopicMap tm) throws Exception {
        setProgress(++progress);
        if(file != null) {
            try {
                Topic fileType = createTopic(tm, baseLocator+"file", "File");

                Topic fileTopic = null;
                String location = file.toURI().toURL().toExternalForm();
                String name = buildBaseName(file);
                fileTopic = createTopic(tm, location, name, (Topic) null);
                
                // --- SET FILE TOPIC'S VARIANT NAMES ---
                setDisplayName(fileTopic, defaultLang, file.getName());
                fileTopic.setSubjectLocator(new Locator( location ));
                
                // --- ADD LAST MODIFICATION TIME AS OCCURRENCE ---
                try {
                    DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
                    Topic modType = createTopic(tm, baseLocator+"file-modified", "file-modified");
                    String dateString = dateFormatter.format( new Date(file.lastModified()) );
                    setData(fileTopic, modType, defaultLang, dateString);
                }
                catch(Exception e) {
                    log("Exception occurred while setting file topic's modification time!", e);
                }
                
                // --- ADD FILE SIZE AS OCCURRENCE ---
                try {
                    Topic sizeType = createTopic(tm, baseLocator+"file-size", "file-size");
                    setData(fileTopic, sizeType, defaultLang, ""+file.length());
                }
                catch(Exception e) {
                    log("Exception occurred while setting file topic's file size!", e);
                }
                
                // --- ADD INFO ASSOCIATIONS IF FILE IS READABLE/WRITEABLE/HIDDEN... ---
                try {
                    Topic fileInfoType = createTopic(tm, baseLocator+"file-info", "file-info");
                    if(fileInfoType != null) {
                        if(file.canRead()) {
                            Topic readableFileType = createTopic(tm, baseLocator+"is-readable-file", "is-readable-file");
                            createAssociation(tm, fileInfoType, new Topic[] { fileTopic, readableFileType }, new Topic[] { fileType, fileInfoType } );
                        }
                        if(file.canWrite()) {
                            Topic writeableFileType = createTopic(tm, baseLocator+"is-writeable-file", "is-writeable-file");
                            createAssociation(tm, fileInfoType, new Topic[] { fileTopic, writeableFileType }, new Topic[] { fileType, fileInfoType } );
                        }
                        if(file.canExecute()) {
                            Topic executableFileType = createTopic(tm, baseLocator+"is-executable-file", "is-executable-file");
                            createAssociation(tm, fileInfoType, new Topic[] { fileTopic, executableFileType }, new Topic[] { fileType, fileInfoType } );
                        }
                        if(file.isHidden()) {
                            Topic hiddenFileType = createTopic(tm, baseLocator+"is-hidden-file", "is-hidden-file");
                            createAssociation(tm, fileInfoType, new Topic[] { fileTopic, hiddenFileType }, new Topic[] { fileType, fileInfoType } );
                        }
                        if(file.isDirectory()) {
                            Topic dirType = createTopic(tm, baseLocator+"is-directory", "is-directory");
                            createAssociation(tm, fileInfoType, new Topic[] { fileTopic, dirType }, new Topic[] { fileType, fileInfoType } );
                        }
                        int index = file.getName().lastIndexOf(".");
                        if(index != -1) {
                            try {
                                String suffix = file.getName().substring(index+1);
                                if(suffix != null && suffix.length() > 0) {
                                    suffix = suffix.toLowerCase();
                                    Topic fileSuffix = createTopic(tm, baseLocator+"type/"+urlEncode(suffix), suffix);
                                    createAssociation(tm, fileInfoType, new Topic[] { fileTopic, fileSuffix }, new Topic[] { fileType, fileInfoType } );
                                }
                            }
                            catch(Exception e) {
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log("Exception occurred while solving file's accessibility!", e);
                }

                // --- ADD EXTRACTION TIME AS OCCURRENCE ---
                DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
                Topic extractionTimeType = createTopic(tm, "extraction-time");
                String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
                setData(fileTopic, extractionTimeType, defaultLang, dateString);

                return fileTopic;
            }
            catch (Exception e) {
                log(e);
                e.printStackTrace();
            }
        }
        return null;
    }



    // THIS EXTRACTOR DOESN'T SUPPORT STRINGS!
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        throw(new Exception(STRING_EXTRACTOR_NOT_SUPPORTED_MESSAGE));
    }
    
    // THIS EXTRACTOR DOESN'T SUPPORT URLS!
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
        try {
            return file.getName() + " ("+file.toURI().toURL().toExternalForm().hashCode()+")";
        }
        catch(Exception e) {
            return file.getName();
        }
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
                return new Locator(new File(siend).toURI().toURL().toExternalForm());
            }
            catch(Exception e) {
                return new Locator("file:/" + siend);
            }
        }
    }
    
    
    
 
}
