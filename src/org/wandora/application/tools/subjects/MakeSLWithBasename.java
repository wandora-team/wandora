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
 * MakeSIFromBasename.java
 *
 * Created on 4. heinäkuuta 2006, 11:07
 *
 */

package org.wandora.application.tools.subjects;

import java.util.*;

import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import org.wandora.*;


/**
 * Add context topics a subject locator created using topic's basename.
 * Each added subject locator is created by injecting topic's basename string 
 * into a subject locator template string. Subject locator template string is requested
 * from the user.
 * 
 * @author akivela
 */
public class MakeSLWithBasename extends AbstractWandoraTool implements WandoraTool {
    private static int MAXLEN = 256;
    private String replacement = "";
    private String SLTemplate = "http://wandora.org/si/%BASENAME%";
    
    private boolean askTemplate = true;
    private boolean overWrite = true;
    
    
    /** Creates a new instance of CopyTextdataToSL */
    public MakeSLWithBasename() {
    }
    
    public MakeSLWithBasename(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Make subject locator from base name";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and creates subject locator for each topic with base name.";
    }

    
    @Override
    public void execute(Wandora admin, Context context) {   
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            if(SLTemplate == null || SLTemplate.length() == 0) {
                SLTemplate = "http://wandora.org/si/%BASENAME%";
            }
            if(askTemplate) {
                SLTemplate = WandoraOptionPane.showInputDialog(admin, "Make subject identifier using following template. String '%BASENAME%' is replaced with topic's base name.", SLTemplate);
                if(SLTemplate == null) return;
                if(SLTemplate.length() == 0 || !SLTemplate.contains("%BASENAME%")) {
                    int a = WandoraOptionPane.showConfirmDialog(admin, "Your template string '"+ SLTemplate +"' does not contain '%BASENAME%'. This results identical subject locators and topic merges. Are you sure you want to continue?", "Invalid template given", WandoraOptionPane.YES_NO_CANCEL_OPTION);
                    if(a != WandoraOptionPane.YES_OPTION) return;
                }
            }
            
            setDefaultLogger();
            setLogTitle("Making SL from base name");
            log("Making subject locator from base name");
            
            Topic topic = null;
            String basename = null;
            String SLString = null;
            int progress = 0;
            int progressMax = 0;

            ArrayList<Object> dt = new ArrayList<Object>();
            while(topics.hasNext() && !forceStop()) {
                dt.add(topics.next());
                progressMax++;
            }
            
            setProgressMax(progressMax);
            topics = dt.iterator();
            
            // Iterate through selected topics...
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        if(overWrite || topic.getSubjectLocator() == null) {
                            progress++;
                            setProgress(progress);
                            
                            basename = topic.getBaseName();
                            
                            // Ok, if topic has sufficient basename descent deeper...
                            if(basename != null && basename.length() > 0) {                          
                                if(basename.length() > MAXLEN) {
                                    basename.substring(0, MAXLEN);
                                }
                                if(basename.indexOf("\n") != -1 || basename.indexOf("\r") != -1) {
                                    basename = basename.replaceAll("\r", replacement);
                                    basename = basename.replaceAll("\n", replacement);
                                }
                                basename = basename.trim();

                                SLString = SLTemplate;
                                SLString = SLString.replaceAll("%BASENAME%", basename);
                                SLString = TopicTools.cleanDirtyLocator(SLString);

                                log("Adding topic '"+basename+"' SL '"+SLString+"'.");
                                topic.setSubjectLocator(new Locator(SLString));
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    
}
