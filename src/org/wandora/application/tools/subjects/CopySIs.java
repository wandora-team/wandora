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
 * CopySIs.java
 *
 * Created on 22. huhtikuuta 2006, 16:03
 *
 */

package org.wandora.application.tools.subjects;




import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.*;
import org.wandora.utils.*;

import java.util.*;


/**
 * Copies subject identifiers to system clipboard as a newline character
 * separated list.
 *
 * @author akivela
 */
public class CopySIs extends AbstractWandoraTool implements WandoraTool {
    
    /** Creates a new instance of CopySIs */
    public CopySIs() {
    }
    public CopySIs(Context context) {
        setContext(context);
    }

    @Override
    public String getName() {
        return "Copy Topic SIs";
    }

    @Override
    public String getDescription() {
        return "Copy topic's subject identifiers to system clipboard.";
    }
    

    @Override
    public void execute(Wandora wandora, Context context) {
        StringBuilder sis = new StringBuilder("");
        Collection topicSIs = null;
        Iterator SIIterator = null;
        Locator SI = null;
        int progress = 0;

        if(context instanceof SIContext) {
            //System.out.println("CopySIs & SIContext");
            SIIterator = context.getContextObjects();
            while(SIIterator.hasNext()) {
                SI = (Locator) SIIterator.next();
                //System.out.println("  si="+SI.toExternalForm());
                if(SI != null) {
                    sis.append(SI.toExternalForm()).append("\n");
                }
            }
        }
        
        
        else {
            Iterator topics = context.getContextObjects();
            if(topics != null && topics.hasNext()) {
                Topic topic = null;
                while(topics.hasNext()) {
                    try {
                        topic = (Topic) topics.next();
                        if(topic != null && !topic.isRemoved()) {
                            topicSIs = topic.getSubjectIdentifiers();
                            setProgress(progress++);
                            if(topicSIs != null) {
                                SIIterator = topicSIs.iterator();
                                while(SIIterator.hasNext()) {
                                    SI = (Locator) SIIterator.next();
                                    if(SI != null) {
                                        sis.append(SI.toExternalForm()).append("\n");
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        singleLog(e);
                    }
                }
            }
            
        }

        ClipboardBox.setClipboard(sis.toString());
    }

    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
