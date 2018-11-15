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
 * MediawikiUploader.java
 *
 * Created on 2013-04-25
 *
 */

package org.wandora.application.tools.mediawiki;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author nlaitine
 */


public class MediawikiSubjectLocatorUploader extends MediawikiHandler implements WandoraTool {
    
	private static final long serialVersionUID = 1L;
	
	private static MediawikiUploaderConfigurationUI configurationUI = null;
    private static MediawikiUploaderConfiguration config = new MediawikiUploaderConfiguration();

    private boolean requiresRefresh = false;
    private boolean isConfigured = false;
    private boolean cancelled = false;
    
    public MediawikiSubjectLocatorUploader() {
    }
    
    public MediawikiSubjectLocatorUploader(Context proposedContext) {
        this.setContext(proposedContext);
    }
    
    @Override
    public String getName() {
        return "Upload occurrence to Mediawiki.";
    }

    @Override
    public String getDescription() {
        return "Upload occurrence to Mediawiki server.";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        requiresRefresh = false;
        isConfigured = false;
        cancelled = false;
        String loc = null;
        Topic carrier = null;
        Locator lo = null;
        
        try {    
            Iterator contextObjects = context.getContextObjects();
            if (!contextObjects.hasNext()) {
                return;
            }

            boolean wasUploaded = false;
            int uploadCounter = 0;
            
            setDefaultLogger();
            while (contextObjects.hasNext() && !forceStop() && !cancelled) {
                Object co = contextObjects.next();
                
                if (co != null) {
                    if (co instanceof Topic) {
                        carrier = (Topic) co;
                        
                        config = setupUI(wandora);
                        if(config != null) {
                            loginToWiki();
                            
                            if (forceStop()) {
                                break;
                            }
                            if (cancelled) {
                                break;
                            }

                            lo = carrier.getSubjectLocator();
                            if (lo != null) {
                                loc = lo.toExternalForm();
                                wasUploaded = mediaWikiUpload(loc);
                                if (wasUploaded) {
                                    uploadCounter++;
                                }
                                if (cancelled) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if(uploadCounter > 0) {
                log("Total "+uploadCounter+" subject locators uploaded to Mediawiki.");
            }
            else if(uploadCounter == 0) {
                log("No subject locators uploaded to Mediawiki.");
            }
            
            if(config != null) {
                logoutOfWiki();
            }
            log("Ready.");
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    public MediawikiUploaderConfiguration setupUI(Wandora wandora) throws TopicMapException {        

        if(!isConfigured) {            
            isConfigured = true;
            if(configurationUI == null) {
                configurationUI = new MediawikiUploaderConfigurationUI();
            }
            
            configurationUI.hideFilename();
            configurationUI.hideDescription();
            configurationUI.showFileExtension();
            
            configurationUI.open(wandora, this);
            if(!configurationUI.wasAccepted()) {
                cancelled = true;
                return null;
            }
        }
        
        config.setWikiUrl(configurationUI.getWikiUrl().trim());
        config.setWikiUser(configurationUI.getUser().trim());
        config.setWikiPasswd(configurationUI.getPasswd().trim());
        config.setWikiStream(configurationUI.getStream());
        config.setWikiFileExtension(configurationUI.getFileExtension().trim());
        
        return config;
    }
    
    private boolean loginToWiki() {
        boolean logged = login(config.getWikiUrl(), config.getWikiUser(), config.getWikiPasswd());
        return logged;
    }
    
    private boolean logoutOfWiki() {
        boolean loggedOut = logout(config.getWikiUrl());
        return loggedOut;
    }
    
    public boolean mediaWikiUpload(String loc) throws TopicMapException {    
        boolean success = false;
        
        String apiWikiUrl = config.getWikiUrl();
        URL apiFileUrl = null;
        String apiFilename = null;
        String apiDescription = config.getWikiDescription();
        boolean apiStream = config.getWikiStream();
        
        if(loc != null && !loc.equals("")) {
            apiFileUrl = stringToUrl(loc);
            apiFilename = filenameFromUrl(apiFileUrl, config.getWikiFileExtension());
        } else {
            apiFileUrl = config.getWikiFileUrl();
            apiFilename = config.getWikiFilename();
        }
        
        success = callWiki(apiWikiUrl, apiFileUrl, apiFilename, apiDescription, apiStream);
        
        return success;
    }
    
   /*
    * Helper methods
    */
   
    private URL stringToUrl(String fileString) {
        URL url = null;
        try {
            File file = new File(fileString);
            if(file.exists()) {
                url = file.toURI().toURL();
            } else {
                try {
                    url = new URL(fileString);
                } catch (Exception e) {
                    log(e.getMessage());                
                }
            }
        } catch (Exception ex) {
            log(ex.getMessage());
        }
        return url;
    }
    
    private String filenameFromUrl(URL url, String extension) {
         String filename = url.getFile();
         filename = filename.substring( filename.lastIndexOf("/")+1, filename.length() );
         filename = filename.replaceAll("/[^a-z0-9\\040\\.]|:/i", "_");

         if(extension != null && !extension.equals("")) {
             //Get file extension
             if(filename.substring(filename.length()-7).equalsIgnoreCase(".tar.gz")) {
                 //special case
             } else {
                 int lastIndex = filename.lastIndexOf(".");
                 if(lastIndex == -1) {
                     lastIndex = 0;
                 }
                 String fileExtension = filename.substring(lastIndex, filename.length());
                 if(fileExtension.length() > 4 || fileExtension.length() < 2) {
                     //Generate filename for url
                     if(url.getProtocol().equals("http") && filename.length() > 20) {
                         filename = "file" + Integer.toString( url.hashCode() );
                     }
                     
                     //Add default file extension
                     if(extension.startsWith(".")) {
                         filename = filename + extension;
                     } else {
                         filename = filename + "." + extension;
                     }
                 }
             }
         }
         filename = filename.trim();
         
         return filename;
     }
}
