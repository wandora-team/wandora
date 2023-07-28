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
 * DeleteTopicsWithSIRegex.java
 *
 * Created on 18.7.2006, 12:43
 *
 */

package org.wandora.application.tools;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;

import java.util.*;


/**
 *
 * @author akivela
 */
public class DeleteTopicsWithSIRegex extends DeleteTopics implements WandoraTool {
    
     RegularExpressionEditor editor = null;
    
    
    /** Creates a new instance of DeleteTopicsWithSIRegex */
    public DeleteTopicsWithSIRegex() {
    }
    public DeleteTopicsWithSIRegex(Context preferredContext) {
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
        return "Delete topics with SI regex";
    }

    @Override
    public String getDescription() {
        return "Delete topics with subject identifier matching to given regular expression.";
    }
    
    @Override
    public boolean shouldDelete(Topic topic)  throws TopicMapException {
        try {
            if(topic != null && !topic.isRemoved()) {
                Iterator<Locator> sii = topic.getSubjectIdentifiers().iterator();
                Locator l = null;
                while(sii.hasNext()) {
                    l = sii.next();
                    if(editor.matches(l.toExternalForm())) {
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
