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
 * AbstractExportTool.java
 *
 * Created on 24. toukokuuta 2006, 19:02
 *
 */

package org.wandora.application.tools.exporters;


import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.topicmap.memory.*;
import org.wandora.application.contexts.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author akivela
 */
public abstract class AbstractExportTool extends AbstractWandoraTool implements WandoraTool {


    /** Creates a new instance of AbstractExportTool */
    public AbstractExportTool() {
    }

    
    
    @Override
    public WandoraToolType getType(){
        return WandoraToolType.createExportType();
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/export.png");
    }
    
    


    // -------------------------------------------------------------------------


    protected TopicMap makeTopicMapWith(Context context) {
        return makeTopicMapWith(context, false);
    }

    protected TopicMap makeTopicMapWith(Context context, boolean deepCopy) {
        TopicMap tm = new TopicMapImpl();
        try {
            Iterator contextObjects = context.getContextObjects();
            Object contextObject = null;
            Topic contextTopic = null;
            while(contextObjects.hasNext()) {
                contextObject = contextObjects.next();
                if(contextObject != null && contextObject instanceof Topic) {
                    contextTopic = (Topic) contextObject;
                    tm.copyTopicIn(contextTopic, deepCopy);
                    tm.copyTopicAssociationsIn(contextTopic);
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
        return tm;
    }
}
