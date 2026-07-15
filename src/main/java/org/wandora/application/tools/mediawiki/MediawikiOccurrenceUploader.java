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
 * MediawikiOccurrenceUploader.java
 *
 * Created on 2013-04-25
 *
 */

package org.wandora.application.tools.mediawiki;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.OccurrenceTable;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author nlaitine
 */


public class MediawikiOccurrenceUploader extends MediawikiHandler implements WandoraTool {
    
	private static final long serialVersionUID = 1L;
	
	private static MediawikiUploaderConfigurationUI configurationUI = null;
    private static MediawikiUploaderConfiguration config = new MediawikiUploaderConfiguration();
    
    private boolean requiresRefresh = false;
    private boolean isConfigured = false;
    private boolean cancelled = false;
    private boolean uploadAll = false;
    
    public MediawikiOccurrenceUploader() {
    }
    
    public MediawikiOccurrenceUploader(boolean upAll) {
        this.uploadAll = upAll;
    }
    
    public MediawikiOccurrenceUploader(Context proposedContext) {
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
        Object source = context.getContextSource();
        String o = null;
        Collection<Topic> dataTypes;
        HashSet<Topic> typeSet = new HashSet<>();
        Topic carrier = null;
        Topic type = null;
        Topic lang = null;
        
        
        try {
            boolean wasUploaded = false;
            
            if(source instanceof OccurrenceTable) {
                setDefaultLogger();
                OccurrenceTable ot = (OccurrenceTable) source;
                o = ot.getPointedOccurrence();
                lang = ot.getPointedOccurrenceLang();
                carrier = ot.getTopic();
                dataTypes = carrier.getDataTypes();
                typeSet.addAll(dataTypes);
                
                if(isValidResourceReference(o)) {
                    config = setupUI(wandora, typeSet, o, carrier, lang, false);
                    if(config != null) {
                        loginToWiki();
                        wasUploaded = mediaWikiUpload(null);
                        logoutOfWiki();
                    }
                    if(wasUploaded) {
                        log("Occurrence uploaded to Mediawiki.");
                    } else {
                        log("No occurrences uploaded to Mediawiki.");
                    }
                }
                else {
                    log("Occurrence resource is not an URL or a file.");
                    log("No occurrences uploaded.");
                }
                log("Ready.");
            }
            else {
                
                Iterator contextObjects = context.getContextObjects();
                if(!contextObjects.hasNext()) return;
                
                int uploadCounter = 0;
                Topic uploadType = null;
                Topic uploadLang = null;
                if(!uploadAll) {
                    GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Occurrence upload options","Occurrence upload options. If you want to limit occurrences, select occurrence type and scope. Leave both topics empty to upload all occurrences.",true,new String[][]{
                        new String[]{"Occurrence type topic","topic","","Which occurrences are uploaded. Leave blank to include all occurrence types."},
                        new String[]{"Occurrence scope topic","topic","","Which occurrences are uploaded. Leave blank to include all occurrence scopes."},
                    },wandora);
                    god.setVisible(true);
                    if(god.wasCancelled()) return;
                    Map<String,String> values=god.getValues();
                    if(values.get("Occurrence type topic")!=null && values.get("Occurrence type topic").length()>0) {
                        uploadType=wandora.getTopicMap().getTopic(values.get("Occurrence type topic"));
                    }
                    if(values.get("Occurrence scope topic")!=null && values.get("Occurrence scope topic").length()>0) {
                        uploadLang=wandora.getTopicMap().getTopic(values.get("Occurrence scope topic"));
                    }
                }
                if(uploadLang == null) return;
                setDefaultLogger();
                
                Iterator objects = context.getContextObjects();
                Object cx = null;
                Topic topic = null;
                Collection<Topic> occurranceTypes = null;
                
                while(objects.hasNext()) {
                    cx = objects.next();
                    topic = (Topic) cx;
                    occurranceTypes = topic.getDataTypes();
                    if (occurranceTypes != null) {
                        typeSet.addAll(occurranceTypes);
                    }
                }
                
                while(contextObjects.hasNext() && !forceStop() && !cancelled) {
                    Object co = contextObjects.next();
                    
                    if(co != null) {
                        if(co instanceof Topic) {
                            carrier = (Topic) co;
                            dataTypes = carrier.getDataTypes();
                            Iterator<Topic> dataTypeObjects = dataTypes.iterator();
                            
                            config = setupUI(wandora, typeSet, null, carrier, uploadLang, true);
                            if(config != null) {
                                loginToWiki();
                            
                            
                                while(dataTypeObjects.hasNext()) {
                                    Topic otype = (Topic) dataTypeObjects.next();

                                    if(forceStop()) break;
                                    if(cancelled) break;
                                    type = otype;
                                    if(uploadType == null || uploadType.mergesWithTopic(type)) {
                                        Hashtable<Topic,String> occurrences = carrier.getData(type);
                                        for(Enumeration<Topic> langs = occurrences.keys() ; langs.hasMoreElements() ; ) {
                                            lang = langs.nextElement();

                                            if(lang != null) {
                                                if(uploadLang == null || lang.mergesWithTopic(uploadLang)) {
                                                    o = occurrences.get(lang);
                                                    if(isValidResourceReference(o)) {
                                                        wasUploaded = mediaWikiUpload(o);
                                                        if(wasUploaded) uploadCounter++;
                                                        if(cancelled) break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(uploadCounter > 0) {
                    log("Total "+uploadCounter+" occurrences uploaded to Mediawiki.");
                }
                else if(uploadCounter == 0) {
                    log("No occurrences uploaded to Mediawiki.");
                }
                
                if(config != null) {
                    logoutOfWiki();
                }
                log("Ready.");
            }
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
    
    public MediawikiUploaderConfiguration setupUI(Wandora wandora, HashSet<Topic> dataTypes, String o, Topic carrier, Topic lang, boolean batch) throws TopicMapException {        
        URL fileUrl = null;
        
        if(!isConfigured) {            
            isConfigured = true;
            if(configurationUI == null) {
                configurationUI = new MediawikiUploaderConfigurationUI();
            }

            configurationUI.resetDescriptionTypeItems();
            if (dataTypes.size() > 1) {
                configurationUI.addDescriptionTypeItem("none");
                for (Iterator<Topic> it = dataTypes.iterator(); it.hasNext();) {
                    Topic dataType = (Topic) it.next();
                    configurationUI.addDescriptionTypeItem(dataType);
                }
                configurationUI.enableDescriptionType();
            }
            configurationUI.showDescription();
            
            if(!batch) {
                configurationUI.showFilename();
                fileUrl = stringToUrl(o);
                String filename = filenameFromUrl(fileUrl, null);
                configurationUI.setFilename(filename);
                configurationUI.hideFileExtension();
            } else {
                configurationUI.hideFilename();
                configurationUI.showFileExtension();
            }
            
            configurationUI.open(wandora, this);
            if(!configurationUI.wasAccepted()) {
                cancelled = true;
                return null;
            }
        }
        
        config.setWikiUrl(configurationUI.getWikiUrl().trim());
        config.setWikiUser(configurationUI.getUser().trim());
        config.setWikiPasswd(configurationUI.getPasswd().trim());
        config.setWikiFileUrl(fileUrl);
        config.setWikiFilename(configurationUI.getFilename().trim());
        config.setWikiStream(configurationUI.getStream());
        config.setWikiFileExtension(configurationUI.getFileExtension().trim());
        
        Object selected = configurationUI.getDescriptionType();
        if (selected instanceof Topic) {
            Topic descType = (Topic) selected;
            String desc = carrier.getData(descType, lang);
            
            if (desc != null) {
                desc = desc.trim();
                config.setWikiDescription(desc);
            }
        }
        
        return config;
    }
    
    private boolean loginToWiki() {
        login(config.getWikiUrl(), config.getWikiUser(), config.getWikiPasswd());
        return true;
    }
    
    private boolean logoutOfWiki() {
        boolean loggedOut = logout(config.getWikiUrl());
        return loggedOut;
    }
    
    public boolean mediaWikiUpload(String o) throws TopicMapException {    
        boolean success = false;
        
        String apiWikiUrl = config.getWikiUrl();
        URL apiFileUrl = null;
        String apiFilename = null;
        String apiDescription = config.getWikiDescription();
        boolean apiStream = config.getWikiStream();
        
        if(o != null && !o.equals("")) {
            apiFileUrl = stringToUrl(o);
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
