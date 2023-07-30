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
 *
 *
 */


package org.wandora.application.tools.occurrences;


import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;


/**
 *
 * @author akivela
 */
public class MakeOccurrenceFromSubjectIdentifier extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static boolean overWrite = false;
    
    private int mode = COPY_ONE;
    
    public static int COPY_ONE = 0;
    public static int COPY_ONE_WITH_REGEX = 1;
    public static int COPY_ALL = 2;
    

    /**
     * Creates a new instance of MakeOccurrenceFromSubjectIdentifier
     */
    public MakeOccurrenceFromSubjectIdentifier() {
    }
    public MakeOccurrenceFromSubjectIdentifier(Context preferredContext) {
        setContext(preferredContext);
    }



    @Override
    public String getName() {
        return "Make occurrence using topic's subject identifier.";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies topic's subject identifier to occurrence.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Copying subject identifier to topic occurrence");
            log("Copying subject identifier to topic occurrence");

            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String sistr = null;
            Locator si = null;
            Collection<Locator> sis = null;
            String occurrence = null;

            Topic type = wandora.showTopicFinder("Select occurrence type...");
            if(type == null) return;

            Topic language = wandora.showTopicFinder("Select occurrence language...");
            if(language == null) return;

            int progress = 0;
            int count = 0;
            Pattern siPattern = null;

            while(topics.hasNext() && !forceStop()) {
                sistr = null;
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        if(mode == COPY_ALL) {
                            sis = topic.getSubjectIdentifiers();
                            StringBuilder sb = new StringBuilder("");
                            for(Locator s : sis) {
                                sb.append(s.toExternalForm());
                                sb.append("\n");
                            }
                            sistr = sb.toString();
                        }
                        else if(mode == COPY_ONE_WITH_REGEX) {
                            if(siPattern == null) {
                                String siPatternStr = WandoraOptionPane.showInputDialog(wandora, "Enter regular expression pattern used to regocnize subject identifiers", ".*", "Enter regular expression pattern", WandoraOptionPane.QUESTION_MESSAGE);
                                if(siPatternStr == null) return;
                                siPattern = Pattern.compile(siPatternStr);
                            }
                            if(siPattern != null) {
                                sis = topic.getSubjectIdentifiers();
                                for(Locator s : sis) {
                                    String str = s.toExternalForm();
                                    if(siPattern.matcher(str).find()) {
                                        sistr = str;
                                        break;
                                    }
                                }
                            }
                        }
                        else {
                            si = topic.getOneSubjectIdentifier();
                            sistr = si.toExternalForm();
                        }
                        
                        
                        if(sistr != null && sistr.length() > 0) {
                            occurrence = topic.getData(type, language);
                            if(occurrence == null || overWrite) {
                                topic.setData(type, language, sistr);
                                count++;
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            log("Ready.");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    
    // ---------------------------------------------------------- CONFIGURE ----
    
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {

        GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Make occurrence out of subject identifier options","Make occurrence out of subject identifier options",true,new String[][]{
            new String[]{"Copy one subject identifier regocnized by a regular expression?","boolean",(mode == COPY_ONE_WITH_REGEX ? "true" : "false"),null },    
            new String[]{"Copy all subject identifiers?","boolean",(mode == COPY_ALL ? "true" : "false"), null },
            
        },wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return;
        
        Map<String, String> values = god.getValues();
        
        boolean copyOneWithRegex = ("true".equals(values.get("Copy one subject identifier regocnized by a regular expression?")) ? true : false );
        boolean copyAll = ("true".equals(values.get("Copy all subject identifiers?")) ? true : false );
                
        if(copyAll) {
            mode = COPY_ALL;
        }
        else if(copyOneWithRegex) {
            mode = COPY_ONE_WITH_REGEX;
        }
        else {
            mode = COPY_ONE;
        }
    }

}
