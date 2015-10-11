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
 * MakeSubjectLocatorFromFileContent.java
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import java.util.*;

import java.io.File;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.utils.DataURL;

/**
 *
 * @author akivela
 */
public class MakeSubjectLocatorFromFileContent extends AbstractWandoraTool implements WandoraTool, Runnable {
    

    public MakeSubjectLocatorFromFileContent() {
    }
    public MakeSubjectLocatorFromFileContent(Context proposedContext) {
        setContext(proposedContext);
    }
    
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            Iterator contextTopics = getContext().getContextObjects();
            if(contextTopics == null || !contextTopics.hasNext()) return;

            SimpleFileChooser chooser=UIConstants.getFileChooser();
            chooser.setDialogTitle("Select resource");

            if(chooser.open(wandora, "Select")==SimpleFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if(f == null || !f.canRead() || !f.exists() || f.isDirectory()) return;
                DataURL subjectDataUrl = new DataURL(f);
                String subjectLocatorString = subjectDataUrl.toExternalForm(org.wandora.utils.Base64.DONT_BREAK_LINES);
                while(contextTopics.hasNext() && !forceStop()) {
                    Topic t = (Topic) (contextTopics.next());
                    if(t != null && !t.isRemoved()) {
                        t.setSubjectLocator(new Locator(subjectLocatorString));
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    @Override
    public String getName() {
        return "Copy file content to subject locator";
    }

    @Override
    public String getDescription() {
        return "Pick a file and make a subject locator out of it's content.";
    }
    
}
