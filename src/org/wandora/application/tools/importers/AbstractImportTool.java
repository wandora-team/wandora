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
 * AbstractImportTool.java
 *
 * Created on 24. toukokuuta 2006, 13:15
 */

package org.wandora.application.tools.importers;



import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import org.wandora.piccolo.utils.crawler.*;
import java.io.*;
import java.net.*;
import org.wandora.utils.*;
import javax.swing.*;


/**
 *
 * @author akivela
 */
public abstract class AbstractImportTool extends AbstractWandoraTool implements WandoraTool {
    // Importer types...
    public final static int CUSTOM_IMPORTER = 1;
    public final static int RAW_IMPORTER = 2;
    public final static int FILE_IMPORTER = 4;
    public final static int URL_IMPORTER = 8;
    
    private AbstractImportDialog importSourceDialog;
    private int selectedImportSource = FILE_IMPORTER;
    
    
    
    public static final int TOPICMAP_RESET_FIRST = 1;
    public static final int TOPICMAP_DIRECT_MERGE = 2;
    public static final int TOPICMAP_MAKE_NEW_LAYER = 4;
    
    public static final int WEB_IMPORT = 256;
    public static final int ASK_SOURCE = 512; // SOURCE == INTERNET | LOCAL FILE SYSTEM
   
    public static final int CLOSE_LOGS = 1024;
    

    public static final int FILE_DIALOG_TITLE_TEXT = 100;
    public static final int URL_DIALOG_MESSAGE_TEXT = 110;
    
    
    protected boolean resetWandoraFirst = false;
    protected boolean directMerge = true;
    protected boolean newLayer = false;
    
    
    protected boolean webImport = false;
    protected boolean askSource = false;
    
    protected boolean closeLogs = false;
    
    
    public Object forceFiles = null;
    

    
    
    
    
    /** Creates a new instance of AbstractImportTool */
    public AbstractImportTool() {
    }
    public AbstractImportTool(int options) {
        setOptions(options);
    }
    
    
    public void setOptions(int options) {
        resetWandoraFirst = (options & TOPICMAP_RESET_FIRST) != 0;
        directMerge = (options & TOPICMAP_DIRECT_MERGE) != 0;
        newLayer = (options & TOPICMAP_MAKE_NEW_LAYER) != 0;
        
        webImport = (options & WEB_IMPORT) != 0;
        askSource = (options & ASK_SOURCE) != 0;
        
        closeLogs = (options & CLOSE_LOGS) != 0;
    }
    
    public int getOptions(){
        int options=0;
        if(resetWandoraFirst) options|=TOPICMAP_RESET_FIRST;
        if(directMerge) options|=TOPICMAP_DIRECT_MERGE;
        if(newLayer) options|=TOPICMAP_MAKE_NEW_LAYER;
        if(webImport) options|=WEB_IMPORT;
        if(askSource) options|=ASK_SOURCE;
        if(closeLogs) options|=CLOSE_LOGS;
        return options;
    }
 
    @Override
    public WandoraToolType getType(){
        return WandoraToolType.createImportType();
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/import.png");
    }
 
    
        
    public void execute(Wandora wandora, Context context) {
        if(forceFiles != null && forceFiles instanceof File) {
            importFile(wandora, (File) forceFiles);
        }
        else if(forceFiles != null && forceFiles instanceof File[]) {
            importFiles(wandora, (File []) forceFiles);
        }
        else if(forceFiles != null && forceFiles instanceof URL) {
            importUrl(wandora, (URL) forceFiles);
        }
        else {
            requestImports(wandora);
        }
    }
    
    
    
    
    
    
    public void requestImports(Wandora wandora) {
        importSourceDialog = new AbstractImportDialog(wandora, true);
        importSourceDialog.initialize(this);
        importSourceDialog.registerFileSource();
        importSourceDialog.registerUrlSource();
        importSourceDialog.registerRawSource();
        
        if(webImport) {
            importSourceDialog.registerUrlSource();
        }
        
        importSourceDialog.setVisible(true);

        if(!importSourceDialog.wasAccepted()) return;

        selectedImportSource = importSourceDialog.getSelectedSource();

        if(selectedImportSource == URL_IMPORTER) {
            String[] urlSources = importSourceDialog.getURLSources();
            if(urlSources != null && urlSources.length > 0 ) {
                String urlSource = null;
                for(int i=0; i<urlSources.length; i++) {
                    urlSource = urlSources[i];
                    if(urlSource != null && urlSource.length() > 0) {
                        try {
                            importUrl(wandora, new URL(urlSource));
                        }
                        catch(Exception e) {
                            log(e);
                            closeLogs = false;
                        }
                    }
                }
            }
        }
        else if(selectedImportSource == FILE_IMPORTER) {
            File[] fileSources = importSourceDialog.getFileSources();
            if(fileSources != null && fileSources.length > 0) {
                importFiles(wandora, fileSources);
            }
        }
        else if(selectedImportSource == RAW_IMPORTER) {
            String stringSource = importSourceDialog.getContent();
            if(stringSource != null && stringSource.length() > 0) {
                try {
                    InputStream streamSource = new ByteArrayInputStream(stringSource.getBytes());
                    importStream(wandora, "Raw data", streamSource);
                }
                catch(Exception e) {
                    log(e);
                    closeLogs = false;
                }
            }
        }
        else {
            System.out.println("Illegal import source: "+selectedImportSource);
        }
    }
    
    
    
    
    public void importUrl(Wandora wandora, URL forceURL) {
        initializeImport(wandora);
        try {
            URLConnection urlConnection = null;
            HttpAuthorizer httpAuthorizer = wandora.wandoraHttpAuthorizer;
            if(httpAuthorizer != null) {
                 urlConnection = httpAuthorizer.getAuthorizedAccess(forceURL);
            }
            else {
                urlConnection = forceURL.openConnection();
                Wandora.initUrlConnection(urlConnection);
            }
            String contentType = urlConnection.getContentType();
            String filename = null;
            if(contentType.startsWith("text/html")) {
                log("Given URL contains HTML text. Importing linked files instead.");
                if(!newLayer && !directMerge) {
                    int a = WandoraOptionPane.showConfirmDialog(wandora, "Should Wandora make new layer for each imported file?", "Make new layers?");
                    if(a == WandoraOptionPane.YES_OPTION) {
                        newLayer = true;
                    }
                    else {
                        directMerge = true;
                    }
                }
                HTMLParser p=new HTMLParser((String[]) null);
                p.parse(forceURL, new InputStreamReader(urlConnection.getInputStream()));
                URL[] newUrls = p.getNewURLs();
                URL newUrl = null;
                int count = 0;
                for(int i=0; i<newUrls.length && !forceStop(); i++) {
                    newUrl = newUrls[i];
                    try {
                        URLConnection urlSubConnection = null;
                        if(httpAuthorizer != null) {
                            urlSubConnection = httpAuthorizer.getAuthorizedAccess(newUrl);
                        }
                        else {
                            urlSubConnection = newUrl.openConnection();
                            Wandora.initUrlConnection(urlConnection);
                        }
                        filename = newUrl.getFile();
                        int index = filename.lastIndexOf('/');
                        if(index > -1 && index < filename.length()) {
                            filename = filename.substring(index+1);
                        }
                        log("Reading linked URL '" + filename + "'.");
                        importStream(wandora, filename, urlSubConnection.getInputStream());
                        count++;
                    }
                    catch(Exception e) {
                        log(e);
                        closeLogs = false;
                    }
                }
                log("Total " + count + " files imported.");
            }
            else {
                filename = forceURL.getFile();
                int index = filename.lastIndexOf('/');
                if(index > -1 && index < filename.length()) {
                    filename = filename.substring(index+1);
                }
                log("Reading URL '" + filename + "'.");
                importStream(wandora, filename, urlConnection.getInputStream());
            }
        }
        catch(Exception e) {
            log(e);
            closeLogs = false;
        }
        finalizeImport(wandora);
    }
    
   
    
    public void importFile(Wandora wandora, File file) {
        initializeImport(wandora);
        if(file.exists() && file.canRead()) {
            try {
                String filename = file.getName();
                log("Reading file '" + filename + "'.");
                InputStream in=new FileInputStream(file);
                importStream(wandora, filename, in);
                in.close();
            }
            catch(Exception e) {
                closeLogs = false;
            }
        }
        else {
            log("File '" + file.getPath() + "' doesn't exists or can not be read!");
        }
        finalizeImport(wandora);
    }
    
    
       
    public void importFiles(Wandora wandora, File[] files) {
        initializeImport(wandora);
        int count = 0;
        for(int i=0; i<files.length && !forceStop(); i++) {
            if(files[i] != null) {
                String filename = files[i].getName();
                log("Reading file '" + filename + "'.");
                if(files[i].exists() && files[i].canRead()) {
                    try {
                        InputStream in=new FileInputStream(files[i]);
                        importStream(wandora, files[i].getPath(), in);
                        count++;
                        in.close();
                    }
                    catch(Exception e) {
                        closeLogs = false;
                    }
                }
                else {
                    log("File '" + files[i].getPath() + "' doesn't exists or can not be read!");
                    closeLogs = false;
                }
            }
        }
        log("Total " + count + " files imported!");
        finalizeImport(wandora);
    }
    

    
    
    // -------------------------------------------------------------------------
    // --- EXTENDING CLASS SHOULD OVERWRITE THIS METHOD!!! ---------------------
    // -------------------------------------------------------------------------
    public abstract void importStream(Wandora admin, String streamName, InputStream inputStream);
    
    
    
    
    public void initializeImport(Wandora wandora) {
        setDefaultLogger();
        if(resetWandoraFirst) {
            log("Resetting Wandora!");
            wandora.resetWandora();
            resetWandoraFirst = false;
        }
    }
    

    public void finalizeImport(Wandora wandora) {
        log("Done");
        if(closeLogs) setState(AbstractWandoraTool.CLOSE);
        else setState(AbstractWandoraTool.WAIT);
    }
    
    
    
    
    public String getGUIText(int textType) {
        switch(textType) {
            case FILE_DIALOG_TITLE_TEXT: {
                return "Select file to import";
            }
            case URL_DIALOG_MESSAGE_TEXT: {
                return "Type the internet address of a document to be imported";
            }
        }
        return "";
    }
    
}
