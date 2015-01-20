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
 * NewLayer.java
 *
 * Created on 26. helmikuuta 2006, 18:35
 *
 */

package org.wandora.application.tools.layers;



import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.layered.*;
import javax.swing.*;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.undowrapper.UndoTopicMap;



/**
 * Create new topic map layer. Topic map layer is a single topic map wrapped
 * into a layer container. Wandora can handle several topic map layers at once.
 * Separate layers are stored in a special stack structure.
 * 
 * Tool passes the execution to <code>LayerControlPanel.createLayer</code>.
 *
 * @author akivela
 */
public class NewLayer extends AbstractLayerTool implements WandoraTool {
    
    /** Creates a new instance of NewLayer */
    public NewLayer() {
    }
    
    
   
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layer_create.png");
    }
    @Override
    public String getName() {
        return "New layer";
    }
    @Override
    public String getDescription() {
        return "Creates new topic map layer.";
    }
    
   
    @Override
    public void execute(Wandora wandora, Context context) {      
        try {
            Layer selected = solveContextLayer(wandora, context);
            ContainerTopicMap container=null;
            if(selected != null) {
                if(selected.getTopicMap() instanceof UndoTopicMap) {
                    TopicMap wrapped = ((UndoTopicMap) selected.getTopicMap()).getWrappedTopicMap();
                    if(wrapped != null && wrapped instanceof ContainerTopicMap) {
                        container = (ContainerTopicMap) wrapped;
                    }
                }
                if(container == null) {
                    if(selected.getTopicMap() instanceof ContainerTopicMap) {
                        container=(ContainerTopicMap)selected.getTopicMap();
                    }
                    else {
                        container=selected.getContainer();
                    }
                }
            }
            if(container == null) {
                container=wandora.getTopicMap();
            }
            
            try {
                wandora.getTopicMap().clearTopicMapIndexes();
            }
            catch(Exception e) {
                log(e);
            }
            wandora.layerTree.createLayer(container);
        }
        catch(Exception e) {
            singleLog(e);
        }
    }
}
