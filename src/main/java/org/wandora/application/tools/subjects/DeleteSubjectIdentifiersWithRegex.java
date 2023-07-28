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
 * DeleteSubjectIdentifiersWithRegex.java
 *
 * Created on 21.7.2006, 16:54
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;

import java.util.*;




/**
 * Deletes subject identifiers that match a regular expression given by the user.
 * Only context topics are examined for subject identifier deletion.
 *
 * @author akivela
 */
public class DeleteSubjectIdentifiersWithRegex extends DeleteSubjectIdentifiers implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
	private RegularExpressionEditor editor;    
    
    
    public DeleteSubjectIdentifiersWithRegex() {
    }
    public DeleteSubjectIdentifiersWithRegex(Context context) {
        setContext(context);
    }
    

    @Override
    public String getName() {
        return "Delete SIs with regex";
    }

    @Override
    public String getDescription() {
        return "Removes all subject identifier of context topics that match given regex. "+
               "SI is not removed if topic has only one SI.";
    }

    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        editor = RegularExpressionEditor.getMatchExpressionEditor(wandora);
        editor.approve = false;
        editor.setVisible(true);
        if(editor.approve == true) {
            super.execute(wandora, context);
        }
    }
    
    
    
    @Override
    protected Collection<Locator> getSubjectIdentifiers(Iterator<Locator> subjectIdentifiers) throws TopicMapException {
        ArrayList<Locator> subjectIdentifiersToDelete = new ArrayList<>();
        while(subjectIdentifiers.hasNext()) {
            Locator subjectIdentifier = subjectIdentifiers.next();
            if(subjectIdentifier != null) {
                String subjectIdetifierString = subjectIdentifier.toExternalForm();
                if(editor.matches(subjectIdetifierString)) {
                    subjectIdentifiersToDelete.add(subjectIdentifier);
                }
            }
        }
        return subjectIdentifiersToDelete;
    }
    
    
    @Override
    protected Collection<Locator> getSubjectIdentifiers(Topic topic) throws TopicMapException {
        List<Locator> subjectIdentifiersToDelete = new ArrayList<>();
        Collection<Locator> subjectIdentifiersOfTopic = topic.getSubjectIdentifiers();
        for(Locator subjectIdentifier : subjectIdentifiersOfTopic) {
            String subjectIdentifierString = subjectIdentifier.toExternalForm();
            if(editor.matches(subjectIdentifierString)) {
                subjectIdentifiersToDelete.add(subjectIdentifier);
            }
        }
        return subjectIdentifiersToDelete;
    }

}
