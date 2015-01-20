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
 * 
 * DeleteTopicsWithBasenameRegex.java
 *
 * Created on 18. heinäkuuta 2006, 12:56
 *
 */

package org.wandora.application.tools;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;



/**
 *
 * @author akivela
 */
public class DeleteTopicsWithBasenameRegex extends DeleteTopics implements WandoraTool {
    
     RegularExpressionEditor editor = null;
    
    
    /** Creates a new instance of DeleteTopicsWithBasenameRegex */
    public DeleteTopicsWithBasenameRegex() {
    }
    public DeleteTopicsWithBasenameRegex(Context preferredContext) {
        setContext(preferredContext);
    }
    
        
    @Override
    public void execute(Wandora admin, Context context) throws TopicMapException  {
        editor = RegularExpressionEditor.getMatchExpressionEditor(admin);
        editor.approve = false;
        editor.setVisible(true);
        if(editor.approve == true) {
            super.execute(admin, context);
        }
    }
    

    @Override
    public String getName() {
        return "Delete topics with base name regex";
    }

    @Override
    public String getDescription() {
        return "Delete topics with base name matching to given regular expression.";
    }
    
    @Override
    public boolean shouldDelete(Topic topic)  throws TopicMapException {
        try {
            if(topic != null && !topic.isRemoved()) {
                if(topic.getBaseName() != null) {
                    if(editor.matches(topic.getBaseName())) {
                        return true;
                    }
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return false;
    }
    
    
    
}
