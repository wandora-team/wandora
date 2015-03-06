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
 * DownloadOccurrence.java
 */

package org.wandora.application.tools.occurrences;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;

import org.wandora.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;


/**
 * Downloads URL occurrence resource and stores resource data into a file or
 * an occurrence.
 *
 * @author akivela
 */
public class DownloadOccurrence extends AbstractWandoraTool implements WandoraTool {

    public static final int TARGET_FILE = 1;
    public static final int TARGET_OCCURRENCE = 2;

    private int target = TARGET_FILE;
    
    private boolean changeOccurrence = false;
    private boolean overWriteAll = false;



    public DownloadOccurrence() {
    }
    public DownloadOccurrence(Context preferredContext) {
        this(preferredContext, false);
    }

    public DownloadOccurrence(int t) {
        target = t;
    }
    
    public DownloadOccurrence(int t, boolean changeOccurrence) {
        this.target = t;
        this.changeOccurrence = changeOccurrence;
    }
    public DownloadOccurrence(Context preferredContext, boolean changeOccurrence) {
        setContext(preferredContext);
        this.changeOccurrence = changeOccurrence;
    }



    private Topic typeTopic = null;
    private Topic langTopic = null;


    @Override
    public void execute(Wandora admin, Context context) {
        Object contextSource = context.getContextSource();
        
        // ***** TARGET OCCURRENCE ***** 
        if(target == TARGET_OCCURRENCE) {
            if(contextSource instanceof OccurrenceTable) {
                OccurrenceTable ot = (OccurrenceTable) contextSource;
                ot.downloadURLOccurrence();
            }
        }
                
        // ***** TARGET FILE ***** 
        else {
            overWriteAll = false;
            if(contextSource instanceof OccurrenceTable) {
                try {
                    File targetPath = null;
                    OccurrenceTable ot = (OccurrenceTable) contextSource;
                    String occurrence = ot.getPointedOccurrence();
                    Topic topic = ot.getTopic();
                    typeTopic = ot.getPointedOccurrenceType();
                    langTopic = ot.getPointedOccurrenceLang();
                    if(occurrence != null) {
                        if(occurrence.startsWith("data:")) {
                            File targetFile = selectFile("Select target file", admin);
                            if(targetFile == null) return;
                            DataURL.saveToFile(occurrence, targetFile);
                        }
                        else {
                            String url = extractURLFromOccurrence(occurrence);
                            if(url != null) {
                                if(targetPath == null) {
                                    targetPath = selectDirectory("Select download directory", admin);
                                    if(targetPath == null) return;
                                }
                                download(admin, topic, url, targetPath);
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            else {
                Iterator topics = context.getContextObjects();
                File targetPath = null;
                TopicMap tm = admin.getTopicMap();

                if(topics != null && topics.hasNext()) {
                    try {
                        Topic topic = null;
                        int total = 0;
                        int count = 0;
                        boolean cont = true;
                        overWriteAll = false;

                        if(topics.hasNext()) {
                            GenericOptionsDialog god=new GenericOptionsDialog(admin,
                                "Download occurrences",
                                "To download occurrences please give occurrence type and scope topics.",true,new String[][]{
                                new String[]{"Type of downloaded occurrences","topic","","Type topic of occurrences"},
                                new String[]{"Scope of downloaded occurrences","topic","","Scope topic i.e. language of occurrences"},
                            },admin);
                            god.setVisible(true);
                            if(god.wasCancelled()) return;

                            Map<String,String> values=god.getValues();
                            typeTopic = tm.getTopic(values.get("Type of downloaded occurrences"));
                            langTopic = tm.getTopic(values.get("Scope of downloaded occurrences"));
                        }

                        while(cont && topics.hasNext() && !forceStop()) {
                            try {
                                total++;
                                topic = (Topic) topics.next();
                                if(topic != null && !topic.isRemoved()) {
                                    String occurrence = topic.getData(typeTopic, langTopic);
                                    String url = extractURLFromOccurrence(occurrence);
                                    if(url != null) {
                                        if(targetPath == null) {
                                            setState(INVISIBLE);
                                            targetPath = selectDirectory("Select download directory", admin);
                                            setState(VISIBLE);
                                            if(targetPath == null) break;
                                        }
                                        setDefaultLogger();
                                        setLogTitle("Download URL occurrences...");
                                        cont = download(admin, topic, url, targetPath);
                                        count++;
                                    }
                                }
                            }
                            catch (Exception e) {
                                log(e);
                            }
                        }
                        log("Total " + total + " topics browsed!");
                        log("Total " + count + " occurrences downloaded!");
                    }
                    catch (Exception e) {
                        log(e);
                    }
                    setState(WAIT);
                }
            }
        }
    }


    public String extractURLFromOccurrence(String occurrence) {
        if(occurrence == null) return null;
        occurrence = occurrence.trim();
        if(occurrence.length() == 0) return null;
        try {
            URL u = new URL(occurrence);
            return occurrence;
        }
        catch(Exception e) {
            // DO NOTHING
        }
        return null;
    }



    public boolean download(Wandora admin, Topic topic, String url, File target) {
        try {
            URL subjectUrl = new URL(url);
            String filename = subjectUrl.getPath();
            String filenameWithoutExtension = filename;
            String filenameExtension = "";
            if(filename.indexOf('/') > -1) filename = filename.substring(filename.lastIndexOf('/'));
            if(filename.indexOf('.') > 0) {
                filenameWithoutExtension = filename.substring(0, filename.lastIndexOf('.')-1);
                filenameExtension = filename.substring(filename.lastIndexOf('.'));
            }
            File targetFile = new File(target.getPath() + "/" + filename);
            if(!overWriteAll && targetFile.exists()) {
                int c = 1;
                File newFile = null;
                do {
                    c++;
                    newFile = new File(target.getPath()+"/"+filenameWithoutExtension+"_"+c+filenameExtension);
                }
                while(newFile.exists() && c < 999);
                int a = WandoraOptionPane.showConfirmDialog(admin, "File '"+filename+"' already exists. Overwrite file? Selecting No renames the file to "+newFile.getPath()+".", "Overwrite existing file?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                if(a == WandoraOptionPane.NO_OPTION) targetFile = newFile;
                else if(a == WandoraOptionPane.CANCEL_OPTION) return false;
                else if(a == WandoraOptionPane.YES_TO_ALL_OPTION) overWriteAll = true;
            }
            log("Downloading occurrence content from "+subjectUrl.toExternalForm()+" to "+targetFile.getPath());
            IObox.moveUrl(subjectUrl, targetFile);
            if(changeOccurrence) {
                String newOccurrence = makeFileLocator(targetFile);
                if(typeTopic != null || langTopic != null) {
                    topic.setData(typeTopic, langTopic, newOccurrence);
                    log("Topic's occurrence has been updated to "+newOccurrence);
                }
            }
        }
        catch (Exception e) {
            log(e);
        }
        return true;
    }





    private File selectDirectory(String directoryDialogTitle, Wandora admin) {
        File currentDirectory = null;
        try {
            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setDialogTitle(directoryDialogTitle);
            chooser.setApproveButtonText("Select directory");
            chooser.setFileSelectionMode(SimpleFileChooser.DIRECTORIES_ONLY);

            if(chooser.open(admin, SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
                currentDirectory = chooser.getSelectedFile();
            }
            else {
                currentDirectory = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return currentDirectory;
    }

    
    

    private File selectFile(String directoryDialogTitle, Wandora admin) {
        File currentFile = null;
        try {
            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setDialogTitle(directoryDialogTitle);
            chooser.setApproveButtonText("Select file");
            chooser.setFileSelectionMode(SimpleFileChooser.FILES_ONLY);

            if(chooser.open(admin, SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
                currentFile = chooser.getSelectedFile();
            }
            else {
                currentFile = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return currentFile;
    }
    
    

    @Override
    public String getName() {
        return "Download occurrences";
    }

    @Override
    public String getDescription() {
        return "Downloads occurrences to local directory selected by user.";
    }



    // -------------------------------------------------------------------------




    public String makeFileLocator(File f) {
        return f.toURI().toString();
//        return "file://" + f.getPath().replace('\\', '/');
    }


    private String croppedFilename(String filename) {
        if(filename != null) {
            if(filename.length() > 40) filename = filename.substring(0,37) + "...";
            return filename;
        }
        return "";
    }


    private String croppedFilename(File file) {
        if(file != null) { return croppedFilename(file.getPath()); }
        return "";
    }


    private String croppedUrlString(String urlString) {
        return (urlString.length() > 60 ? urlString.substring(0,59) + "..." : urlString);
    }




    // -------------------------------------------------------------------------



}
