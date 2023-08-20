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
 * MakeSubjectIdentifierFromBasename.java
 *
 * Created on 4.7.2006, 11:07
 *
 */

package org.wandora.application.tools.subjects;

import java.util.ArrayList;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicTools;


/**
 * Add context topics a subject identifier created using topic's basename.
 * Each added subject identifier is created by injecting topic's basename string 
 * into a subject identifier template string. Tool asks the subject identifier template
 * string from the user.
 *
 * @author akivela
 */
public class MakeSubjectIdentifierFromBasename extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private static int MAXLEN = 256;
    private String replacement = "";
    private String SITemplate = "http://wandora.org/si/%BASENAME%";
    
    private boolean askTemplate = true;
    
    
    /** Creates a new instance of MakeSIWithBasename */
    public MakeSubjectIdentifierFromBasename() {
    }
    
    public MakeSubjectIdentifierFromBasename(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Make SI from base name";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and adds each topic new subject identifier made with base name.";
    }

    
    @Override
    public void execute(Wandora admin, Context context) {   
        try {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            if(SITemplate == null || SITemplate.length() == 0) {
                SITemplate = "http://wandora.org/si/%BASENAME%";
            }
            if(askTemplate) {
                SITemplate = WandoraOptionPane.showInputDialog(admin, "Make subject identifier using following template. String '%BASENAME%' is replaced with topic's base name.", SITemplate);
                if(SITemplate == null) return;
                if(SITemplate.length() == 0 || !SITemplate.contains("%BASENAME%")) {
                    int a = WandoraOptionPane.showConfirmDialog(admin, "Your template string '"+ SITemplate +"' does not contain '%BASENAME%'. This results identical subject identifiers and topic merges. Are you sure you want to continue?", "Invalid template given", WandoraOptionPane.YES_NO_CANCEL_OPTION);
                    if(a != WandoraOptionPane.YES_OPTION) return;
                }
            }
            
            setDefaultLogger();
            setLogTitle("Making SI from base name");
            log("Making subject identifier from base name");
            
            Topic topic = null;
            String basename = null;
            String SIString = null;
            int progress = 0;
            int progressMax = 0;

            ArrayList<Object> dt = new ArrayList<Object>();
            while(topics.hasNext() && !forceStop()) {
                dt.add(topics.next());
                progressMax++;
            }
            topics = dt.iterator();
            setProgressMax(progressMax);
            
            // Iterate through selected topics...
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        progress++;
                        setProgress(progress);
                        
                        basename = topic.getBaseName();
                        
                        // Ok, if topic has sufficient base name descent deeper...
                        if(basename != null && basename.length() > 0) {                          
                            if(basename.length() > MAXLEN) {
                                basename.substring(0, MAXLEN);
                            }
                            if(basename.indexOf("\n") != -1 || basename.indexOf("\r") != -1) {
                                basename = basename.replaceAll("\r", replacement);
                                basename = basename.replaceAll("\n", replacement);
                            }
                            basename = basename.trim();
                            
                            SIString = SITemplate;
                            SIString = SIString.replaceAll("%BASENAME%", basename);
                            SIString = TopicTools.cleanDirtyLocator(SIString);
                            
                            log("Adding topic '"+basename+"' SI '"+SIString+"'.");
                            topic.addSubjectIdentifier(new Locator(SIString));
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
