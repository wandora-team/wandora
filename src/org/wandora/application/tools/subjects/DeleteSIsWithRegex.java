/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * DeleteSIsWithRegex.java
 *
 * Created on 21. heinäkuuta 2006, 16:54
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.*;

import java.util.*;




/**
 * Deletes subject identifiers that match a regular expression given by the user.
 * Only context topics are examined for subject identifier deletion.
 *
 * @author akivela
 */
public class DeleteSIsWithRegex extends DeleteSIs implements WandoraTool {
    private RegularExpressionEditor editor;    
    
    
    public DeleteSIsWithRegex() {
    }
    public DeleteSIsWithRegex(Context context) {
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
    public Collection<Locator> collectSubjectIdentifiers(Topic topic) throws TopicMapException {
        ArrayList sisToDelete = new ArrayList();
        Collection sis = topic.getSubjectIdentifiers();
        int s = sis.size();
        if(s > 1) {
            Iterator sii = sis.iterator();
            Locator l = null;
            while(sii.hasNext() && s > 1) {
                l = (Locator) sii.next();
                String ls = l.toExternalForm();
                if(editor.matches(ls)) {
                    sisToDelete.add(l);
                }
            }
            sii = sisToDelete.iterator();
        }
        return sisToDelete;
    }
    
    
    
    @Override
    public boolean shouldDelete(Topic topic, Locator si)  throws TopicMapException {
        String ls = si.toExternalForm();
        if(editor.matches(ls)) {
            if(confirm) {
                return confirmDelete(topic, si);
            }
            else {
                return true;
            }
        }
        return false;
    }
    
}
