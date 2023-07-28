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
 */

package org.wandora.application.tools.mediawikiapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolLogger;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.GenericOptionsDialog;

import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
/**
 *
 * @author Eero
 */


public class MediaWikiAPIUploader extends MediaWikiAPIHandler implements WandoraTool{

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_TYPE_SI  
            = "http://wandora.org/si/mediawiki/api/content/";
    private static final String DEFAULT_SCOPE_SI  
            = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
    
    
    private static final String TYPE_KEY  = "Occurrence type topic";
    private static final String SCOPE_KEY = "Occurrence scope topic";
    
    private static final String TYPE_SCOPE_DIALOG_TITLE
            = "Occurrence upload options";
    
    private static final String TYPE_SCOPE_DIALOG_DESCRIPTION 
           = "Occurrence upload options. If you want to limit occurrences, "
           + "select occurrence type and scope. Leave both topics empty to "
           + "use the values the MediaWiki API Extractor uses.";
    
    private static final String[][] TYPE_SCOPE_DIALOG_FIELDS = {
        new String[]{
            "Occurrence type topic",
            "topic","",
            "Which occurrences are uploaded. Leave blank to use the occurrence "
           +"type used by the MediaWiki API Extractor."
        },
        new String[]{
            "Occurrence scope topic",
            "topic",
            "",
            "Which occurrences are uploaded. Leave blank to use the scope used "
           +"by the MediaWiki API Extractor."
        }
    };
    
    private Wandora wandora;
    private Context context;
    
    private WandoraToolLogger logger;
    
    private MediaWikiAPIConfig conf;
    
    //--------------------------------------------------------------------------
    
    public MediaWikiAPIUploader(){
        super();
        this.conf = null;
    }
    
    public MediaWikiAPIUploader(Context c){
        super();
        this.setContext(c);
        this.conf = null;
    }
    
    //--------------------------------------------------------------------------
    
    /**
     * Helper for fetching the content type and scope.
     * @param w The Wandora object
     * @return a map of type topics if found, otherwise null
     * @throws TopicMapException 
     */
    private HashMap<String,Topic> getContentTypeAndScope(Wandora w) 
            throws TopicMapException{
        
        /*
         * Display a dialog for the user. Lets the user choose a type and scope
         * for the occurrence to specify an occurrence of a topic to use as the
         * corresponding wiki page content.
         */
        
        GenericOptionsDialog god=new GenericOptionsDialog(w,
                TYPE_SCOPE_DIALOG_TITLE,TYPE_SCOPE_DIALOG_DESCRIPTION,true,
                TYPE_SCOPE_DIALOG_FIELDS,w);
        
        
        god.setVisible(true);
        if(god.wasCancelled()) return null;
        
        /*
         * We should get a map in the form of
         * {
         *      occurrence type topic: <type topic SI>,
         *      occurrence scope topic: <scope topic SI>
         * }
         */
        Map<String,String> values=god.getValues();
        
        /*
         * Solve topics from the SIs fetched above. The dialog 'should' return
         * proper topics but we should be careful anyway...
         */
        HashMap<String, Topic> typeAndScope = new HashMap<String, Topic>();
        TopicMap tm = w.getTopicMap();
        
        if(values.get(TYPE_KEY).length() > 0) 
            typeAndScope.put(TYPE_KEY, tm.getTopic(values.get(TYPE_KEY)));
        else
            typeAndScope.put(TYPE_KEY, tm.getTopic(DEFAULT_TYPE_SI));
        
        if(values.get(SCOPE_KEY).length() > 0) 
            typeAndScope.put(SCOPE_KEY, tm.getTopic(values.get(SCOPE_KEY)));
        else
            typeAndScope.put(SCOPE_KEY, tm.getTopic(DEFAULT_SCOPE_SI));
        
        /*
         * Return null if the map is invalid.
         */
        boolean mapIsValid = (typeAndScope.size() == 2);
        if(!typeAndScope.containsKey(TYPE_KEY)) mapIsValid = false;
        if(!typeAndScope.containsKey(SCOPE_KEY)) mapIsValid = false;
        
        return mapIsValid ? typeAndScope : null;
        
    }
    
    /**
     * Present an UI for the user to input configuration. Update the class
     * configuration if we got an usable configuration.
     * 
     * @return whether we got an usable configuration.
     */
    private boolean initializeConfiguartion() {
        MediaWikiAPIConfigUI ui = new MediaWikiAPIConfigUI();
        ui.open(wandora,this);
        MediaWikiAPIConfig config;
        try {
            config = ui.getConfig();
        } catch (Exception e) {
            log(e.getMessage());
            return false;
        }
        
        this.conf = config;
        return true;
    }
    
    /**
     * Process a single context object (which we want to be a Topic)
     * @param type The content type topic
     * @param scope The content scope topic
     * @param co The context object
     */
    private void processContextObject(Topic type, Topic scope, Object co) {
        
        //Early return in the case the object is not applicable
        if(co == null || !(co instanceof Topic)) return;
        
        Topic ct = (Topic) co;
        
        Collection<Topic> ctDataTypes;
        String content;
        try {
            /*
             * Iterate over all occurrence types of ct, discern hit as a merge
             * between 'type' and the current 'ctType'
             */
            ctDataTypes = ct.getDataTypes();
            Hashtable<Topic, String> occurrence;
            for (Topic ctType : ctDataTypes) {
                if(!ctType.mergesWithTopic(type)) continue;
                /*
                 * Occurrence type matches. Iterate over all scopes of the 
                 * occurrence. Again, discern hit as a merge between 'scope' and
                 * 'ctScope'
                 */
                occurrence = ct.getData(ctType);
                for(Topic ctScope: occurrence.keySet()){
                    if(!ctScope.mergesWithTopic(scope)) continue;
                    
                    
                    /*
                     * We got our content!
                     */
                    
                    try {
                        log("Processing " + ct.getBaseName()); 
                    } catch (Exception e) {
                        log(e.getMessage());
                        e.printStackTrace();
                    }
                    
                    content = occurrence.get(ctScope);
                    boolean uploaded = upload(ct.getBaseName(), content);
                    
                }
            }
        } catch (TopicMapException tme) {
            log(tme);
        }
        
    }
    
    private boolean upload(String title, String content) {
        try {
            
            if(!getLoginStatus()) login(conf);
            getEditToken(conf);
            postContent(conf, title, content);
            return true;

        } catch (Exception e) {
            log(e.getMessage());
        }
        
        return false;
    }
    
    
    
    //--------------------------------------------------------------------------
    
    /**
     * The public API this package exposes.
     * @param w Wandora
     * @param c Context
     * @throws TopicMapException 
     */
    
    @Override
    public void execute(Wandora w, Context c) throws TopicMapException {
        
        this.wandora = w;
        this.context = c;
        
        setDefaultLogger();
        logger = getDefaultLogger();
        Topic contentType;
        Topic contentScope;
        
        //Get the context Objects (Topics)
        Iterator contextObjects = context.getContextObjects();
        
        //Log and early return on context selection failure
        if(!contextObjects.hasNext()){
            log("No context objects found!");
            return;
        }
        
        //Prompt the user for the content occurrence type and scope
        HashMap<String,Topic> typeAndScope = getContentTypeAndScope(w);
        
        //Log and early return on configuration failure
        if(typeAndScope == null) {
            log("Invalid content type and/or scope");
            return;
        }
        
        boolean gotConfig = initializeConfiguartion();
        
        //Log and early return on configuration failure
        if(!gotConfig) {
            log("Invalid configuration parameters!");
            return;
        }
        
        contentType = typeAndScope.get(TYPE_KEY);
        contentScope = typeAndScope.get(SCOPE_KEY);
        
        while(contextObjects.hasNext() && !forceStop()) 
            processContextObject(contentType,contentScope,contextObjects.next());
        
        setState(WAIT);
        
    }
}
