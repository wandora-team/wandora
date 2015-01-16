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
 * TemplateRemover.java
 *
 * Created on August 26, 2004, 10:03 AM
 */

package org.wandora.application.tools.fng;
import org.wandora.topicmap.TopicInUseException;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.*;
import org.wandora.*;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.utils.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author  olli
 */
public class TemplateRemover {
    
    /** Creates a new instance of TemplateRemover */
    public TemplateRemover() {
    }
    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying TemplateRemover filter");
        
        Topic supersub=tm.getTopic("http://www.topicmaps.org/xtm/1.0/index.html#psi-at-superclass-subclass");
        Topic subc=tm.getTopic("http://www.topicmaps.org/xtm/1.0/index.html#psi-role-subclass");
        Topic superc=tm.getTopic("http://www.topicmaps.org/xtm/1.0/index.html#psi-role-superclass");
        Topic templatet=tm.getTopic("http://wandora.org/si/waonder/wandora.xtm#templatetopic");
        if(supersub==null || subc==null || superc==null || templatet==null){
            logger.writelog("Couldn't find all needed topics.");
            return tm;
        }
        int counter=0;
        Iterator iter=new ArrayList(templatet.getAssociations(supersub,superc)).iterator();
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            Topic player=a.getPlayer(subc);
            try{
                player.remove();
            }catch(TopicInUseException tiue){}
        }
        try{
            templatet.remove();
        }catch(TopicInUseException tiue){}
        logger.writelog("Removed "+counter+" template topics.");
        return tm;
    }    
}
