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
 * TestTool.java
 *
 * Created on 7. maaliskuuta 2006, 16:01
 */

package org.wandora.application.tools;

import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.database.DatabaseTopicMap;
import org.wandora.application.*;
import org.wandora.application.contexts.*;

import java.io.*;
import java.util.*;
import org.wandora.utils.swing.*;
import java.awt.*;

/**
 *
 * @author olli
 */
public class TestTool extends AbstractWandoraTool implements WandoraTool  {
    
    /** Creates a new instance of TestTool */
    public TestTool() {
    }

    @Override
    public String getName() {
        return "Test Tool (Make Consistent)";
    }

   
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        Object contextSource = context.getContextSource();
        if(contextSource!=null && contextSource instanceof LayerStatusPanel){
            LayerStatusPanel p=(LayerStatusPanel)context;
            TopicMap tm=p.getLayer().getTopicMap();
            Iterator<Association> iter=tm.getAssociations();
            int counter=0;
            while(iter.hasNext()){
                Association a=iter.next();
                Iterator iter2=a.getRoles().iterator();
                while(iter2.hasNext()){
                    Topic role=(Topic)iter2.next();
                    a.getPlayer(role);
                }
                counter++;
                if(counter%100==0){
                    log("" + counter);
                    if(tm instanceof DatabaseTopicMap){
                        ((DatabaseTopicMap)tm).printIndexDebugInfo();
                    }
                }
            }
        }        
    }    
}
