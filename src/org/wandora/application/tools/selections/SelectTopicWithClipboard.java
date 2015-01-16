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
 * SelectTopicWithClipboard.java
 *
 * Created on 14. heinäkuuta 2006, 12:18
 *
 */

package org.wandora.application.tools.selections;


import org.wandora.topicmap.*;
import org.wandora.utils.*;

import java.util.*;

/**
 * Class implements a tool used to select topics in topic table. Topic is
 * selected if clipboard contains topic's subject identifier, subject locator
 * or base name. Clipboard may contain multiple identifiers separated with
 * newline character. 
 *
 * @author akivela
 */
public class SelectTopicWithClipboard extends DoTopicSelection {
    
    String[] identifiers = null;
    String identifier = null;
    Iterator<Locator> sis = null;
    Locator si = null;
    
    
    @Override
    public void initializeTool() {
        identifier = ClipboardBox.getClipboard();
        if(identifier != null) {
            if(identifier.indexOf("\n") != -1) {
                identifiers = identifier.split("\r*\n\r*");
                for(int i=0; i<identifiers.length; i++) {
                    identifiers[i] = identifiers[i].trim();
                }
            }
            else {
                identifiers = new String[] { identifier };
            }
        }
    }
    
    
    @Override
    public boolean acceptTopic(Topic topic)  {
        try {
            if(identifiers != null && identifiers.length > 0 && topic != null && !topic.isRemoved()) {
                for(int i=0; i<identifiers.length; i++) {
                    identifier = identifiers[i];
                    if(identifier != null || identifier.length() > 0) {
                        if(topic.getSubjectLocator() != null) {
                            if(identifier.equals(topic.getSubjectLocator().toExternalForm())) return true;
                        }
                        if(identifier.equals(topic.getBaseName())) return true;
                        sis = topic.getSubjectIdentifiers().iterator();
                        while(sis.hasNext()) {
                            si = sis.next();
                            if(si != null) {
                                if(identifier.equals(si.toExternalForm())) return true;
                            }
                        }
                    }
                }
            }
        }
        catch(TopicMapException tme) {
            log(tme);
        }
        return false;
    }
    
    
    
    @Override
    public String getName() {
        return "Select topics with clipboard";
    }
    
    
    @Override
    public String getDescription() {
        return "Select topic if clipboard contains topic's subject locator, base name or subject identifier.";
    }
    
}
