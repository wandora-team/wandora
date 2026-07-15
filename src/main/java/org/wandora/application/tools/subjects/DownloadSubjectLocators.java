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
 * DownloadSubjectLocator.java
 *
 * Created on November 4, 2004, 5:48 PM
 */

package org.wandora.application.tools.subjects;



import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.utils.DataURL;
import org.wandora.utils.IObox;
import org.wandora.utils.MimeTypes;



/**
 * Saves subject locator resources to local file system and optionally updates
 * subject locators.
 *
 * @author  akivela
 */
public class DownloadSubjectLocators extends AbstractWandoraTool implements  WandoraTool {

	private static final long serialVersionUID = 1L;

	private boolean changeSubjectLocator = false;
    private boolean overWriteAll = false;
    private Wandora wandora = null;
    private boolean isCancelled = false;
    
    
    public DownloadSubjectLocators() {
    }
    public DownloadSubjectLocators(Context preferredContext) {
        this(preferredContext, false);
    }

    public DownloadSubjectLocators(boolean changeSubjectLocator) {
        this.changeSubjectLocator = changeSubjectLocator;
    }
    public DownloadSubjectLocators(Context preferredContext, boolean changeSubjectLocator) {
        setContext(preferredContext);
        this.changeSubjectLocator = changeSubjectLocator;
    }
    
    
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        this.wandora = wandora;
        Iterator<Topic> topics = context.getContextObjects();
        File targetPath = null;
        
        if(topics != null && topics.hasNext()) {
            targetPath = selectDirectory("Select download directory", wandora);
            if(targetPath == null) return;
        }
        
        if(topics != null && topics.hasNext()) {
            try {
                
                Topic topic = null;
                int total = 0;
                int count = 0;
                overWriteAll = false;
                isCancelled = false;

                setDefaultLogger();
                setLogTitle("Download subject locators...");
                
                while(topics.hasNext() && !isCancelled && !forceStop()) {
                    try {
                        total++;
                        topic = topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            Locator l = topic.getSubjectLocator();
                            if(l != null) {
                                if(download(topic, l, targetPath)) {
                                    count++;
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        log(e);
                    }
                }
                log("Inspected " + total + " topics.");
                log("Downloaded " + count + " subject locators.");
                log("Ready.");
            }
            catch (Exception e) {
                log(e);
            }
            setState(WAIT);
        }
    }

    
    
    protected boolean download(Topic topic, Locator l, File target) {
        boolean successfulDownload = false;
        
        try {
            String locatorString = l.toExternalForm();
            
            if(DataURL.isDataURL(locatorString)) {
                DataURL dataUrl = new DataURL(locatorString);
                String mimetype = dataUrl.getMimetype();
                String filenameExtension = MimeTypes.getExtension(mimetype);
                String filename = topic.getBaseName();
                if(filename == null) filename = topic.getID();
                File targetFile = new File(target.getPath() + "/" + filename + "." + filenameExtension);
                if(useFile(targetFile)) {
                    log("Saving subject locator content to "+targetFile.getPath());
                    dataUrl.saveToFile(targetFile);
                    successfulDownload = true;
                    if(changeSubjectLocator) {
                        String newLocator = makeFileLocator(targetFile);
                        topic.setSubjectLocator(new Locator(newLocator));
                        log("Changed topic's subject locator to "+newLocator);
                    }
                }
            }
            else {
                URL subjectUrl = new URL(locatorString);
                String filename = subjectUrl.getPath();
                if(filename.indexOf('/') > -1) {
                    filename = filename.substring(filename.lastIndexOf('/'));
                }
                if(filename.length() == 0) {
                    filename = topic.getBaseName();
                    if(filename == null) filename = topic.getID();
                }
                File targetFile = new File(target.getPath() + "/" + filename);
                
                if(useFile(targetFile)) {
                    log("Downloading subject locator content from "+subjectUrl.toExternalForm()+" to "+targetFile.getPath());
                    IObox.moveUrl(subjectUrl, targetFile);
                    successfulDownload = true;
                    if(changeSubjectLocator) {
                        String newLocator = makeFileLocator(targetFile);
                        topic.setSubjectLocator(new Locator(newLocator));
                        log("Changed topic's subject locator to "+newLocator);
                    }
                }
            }
        }
        catch (Exception e) {
            log(e);
        }
        return successfulDownload;
    }
    
    
    
    private boolean useFile(File target) {
        if(!overWriteAll && target.exists()) {
            String filename = target.getPath();
            int a = WandoraOptionPane.showConfirmDialog(wandora, 
                    "File '"+filename+"' already exists. Overwrite file?", 
                    "Overwrite existing file?", 
                    WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
            if(a == WandoraOptionPane.NO_OPTION) {
                return false;
            }
            else if(a == WandoraOptionPane.CANCEL_OPTION) {
                isCancelled = true;
                return false;
            }
            else if(a == WandoraOptionPane.YES_TO_ALL_OPTION) {
                overWriteAll = true;
            }
        }
        return true;
    }
    
    
    private File selectDirectory(String directoryDialogTitle, Wandora admin) {
        File currentDirectory = null;
        try {
            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setDialogTitle(directoryDialogTitle);
            chooser.setApproveButtonText("Select");
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
        return "Download subject locators";
    }

    @Override
    public String getDescription() {
        return "Downloads and saves subject locators resouces to a local directory selected by user.";
    }
    

    
    // -------------------------------------------------------------------------
    
    


    private String makeFileLocator(File f) {
        return f.toURI().toString();
    }


    private String croppedFilename(String filename) {
        if(filename != null) {
            if(filename.length() > 40) filename = filename.substring(0,37) + "...";
            return filename;
        }
        return "";
    }

}
