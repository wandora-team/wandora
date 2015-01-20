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
 * TopicMapMerger.java
 *
 * Created on August 24, 2004, 10:33 AM
 */

package org.wandora.application.tools.fng;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.*;
import org.wandora.*;
import org.wandora.piccolo.Logger;
import org.wandora.piccolo.utils.*;
import java.util.*;
import java.text.*;
import java.io.*;
/**
 *
 * @author  olli
 */
public class TopicMapMerger {
    
    private String file;
    private boolean overwrite;
    
    /** Creates a new instance of TopicMapMerger */
    public TopicMapMerger(String file) {
        this(file,true);
    }
    public TopicMapMerger(String file,boolean overwrite){
        this.file=file;
        this.overwrite=overwrite;
    }

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        logger.writelog("Applying TopicMapMerger filter");
        
        TopicMap loaded=new org.wandora.topicmap.memory.TopicMapImpl();
        try{
            FileInputStream in=new FileInputStream(file);
            tm.importXTM(in);
            in.close();
        }catch(IOException e){
            logger.writelog("WRN","IOException",e);
            return tm;
        }
        if(overwrite) TMBox.mergeTopicMapInOverwriting(tm, loaded);
        else tm.mergeIn(loaded);
        return tm;
    }    
    
}
