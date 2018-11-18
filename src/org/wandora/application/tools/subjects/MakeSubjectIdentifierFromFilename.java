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
 * PickFileSL.java
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

/**
 *
 * @author akivela
 */
public class MakeSubjectIdentifierFromFilename extends AbstractWandoraTool implements WandoraTool, Runnable {
    

	private static final long serialVersionUID = 1L;

	public MakeSubjectIdentifierFromFilename() {
    }
    public MakeSubjectIdentifierFromFilename(Context proposedContext) {
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
                if(f == null) return;
                String subject = f.toURI().toString();
                while(contextTopics.hasNext() && !forceStop()) {
                    Topic t = (Topic) (contextTopics.next());
                    if(t != null && !t.isRemoved()) {
                        t.addSubjectIdentifier(new Locator(subject));
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
        return "Pick a file as subject identifier";
    }

    @Override
    public String getDescription() {
        return "Pick a file and make a subject identifier out of it.";
    }
    
}
