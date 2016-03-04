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
 * DownloadAllOccurrences.java
 * 
 */


package org.wandora.application.tools.occurrences;

/**
 *
 * @author akivela
 */
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.*;

import org.wandora.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * <p>
 * Download resources addressed by occurrences that can be recognized as URLs.
 * Download folder is asked from the user.
 * </p>
 * 
 * @author akivela
 */
public class DownloadAllOccurrences extends AbstractWandoraTool implements  WandoraTool {


    public boolean changeOccurrence = false;
    public boolean overWriteAll = false;



    public DownloadAllOccurrences() {
    }
    public DownloadAllOccurrences(Context preferredContext) {
        this(preferredContext, false);
    }

    public DownloadAllOccurrences(boolean changeOccurrence) {
        this.changeOccurrence = changeOccurrence;
    }
    public DownloadAllOccurrences(Context preferredContext, boolean changeOccurrence) {
        setContext(preferredContext);
        this.changeOccurrence = changeOccurrence;
    }




    private Topic typeTopic = null;
    private Topic langTopic = null;



    @Override
    public void execute(Wandora admin, Context context) {
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

                setDefaultLogger();
                setLogTitle("Download URL occurrences...");

                while(cont && topics.hasNext() && !forceStop()) {
                    try {
                        total++;
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            Collection<Topic> occurrenceTypes = topic.getDataTypes();
                            for(Topic type: occurrenceTypes) {
                                typeTopic = type;
                                Hashtable<Topic,String> scopedOccurrences = topic.getData(type);
                                Enumeration<Topic> scopeTopics = scopedOccurrences.keys();

                                while(scopeTopics.hasMoreElements()) {
                                    langTopic = scopeTopics.nextElement();
                                    String occurrence = scopedOccurrences.get(langTopic);
                                    String url = extractURLFromOccurrence(occurrence);
                                    if(url != null) {
                                        if(targetPath == null) {
                                            setState(INVISIBLE);
                                            targetPath = selectDirectory("Select download directory", admin);
                                            setState(VISIBLE);
                                            if(targetPath == null) break;
                                        }
                                        cont = download(admin, topic, url, targetPath);
                                        count++;
                                    }
                                }
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


    public String extractURLFromOccurrence(String occurrence) {
        if(occurrence == null) return null;
        occurrence = occurrence.trim();
        if(occurrence.length() == 0) return null;
        if(occurrence.startsWith("http://") || occurrence.startsWith("https://") ||
           occurrence.startsWith("ftp://") || occurrence.startsWith("ftps://")) {
            return occurrence;
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
                if(typeTopic != null && langTopic != null) {
                    topic.setSubjectLocator(new Locator(newOccurrence));
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


    @Override
    public String getName() {
        return "Download all occurrences";
    }

    @Override
    public String getDescription() {
        return "Downloads all occurrences to local directory selected by user.";
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
