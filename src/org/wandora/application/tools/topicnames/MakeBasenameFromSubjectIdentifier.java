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
 * MakeBasenameFromSubjectIdentifier.java
 *
 * Created on 25. toukokuuta 2006, 10:43
 *
 */

package org.wandora.application.tools.topicnames;

import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;
import java.net.*;



/**
 * Sometimes topic lacks base names. For example <code>SimpleRDFImport</code>
 * produces topics without base names. This tool can be used to fill topic
 * base name with subject identifier's file name. As subject identifier's
 * file name is only partial fraction of URL, applying tool to a numerous
 * topics may result merges.
 *
 * @author akivela
 */


public class MakeBasenameFromSubjectIdentifier extends AbstractWandoraTool implements WandoraTool {
    boolean overWrite = false;
    
    
    /**
     * Creates a new instance of MakeBasenameWithSI
     */
    public MakeBasenameFromSubjectIdentifier() {
    }
    
    public MakeBasenameFromSubjectIdentifier(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Copy SI to topic base name";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and fills empty basenames with subject identifier filename.";
    }

    
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Copying SI to base name");
            log("Copying subject identifier file name to topic base name");
            
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String basename = null;
            Collection sis = null;
            int progress = 0;

            ArrayList<Object> dt = new ArrayList<Object>();
            while(topics.hasNext() && !forceStop()) {
                dt.add(topics.next());
            }
            topics = dt.iterator();
            
            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        basename = topic.getBaseName();
                        if(overWrite || basename == null) {
                            sis = topic.getSubjectIdentifiers();
                            if(sis != null && sis.size() > 0) {
                                basename = ((Locator) sis.iterator().next()).toExternalForm();
                                if(!basename.endsWith("/")) {
                                    if(basename.indexOf("/") != -1) {
                                        String oldBasename = basename;
                                        basename = basename.substring(basename.lastIndexOf("/")+1);
                                        if(basename.length() == 0) {
                                            basename = oldBasename;
                                        }
                                    }
                                    if(basename != null && basename.length() > 0) {
                                        basename = URLDecoder.decode(basename, "UTF-8");
                                    }
                                    log("Adding topic base name '"+basename+"'");
                                    topic.setBaseName(basename);
                                }
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
