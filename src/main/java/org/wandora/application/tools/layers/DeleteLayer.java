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
 * DeleteLayer.java
 *
 * Created on 26. helmikuuta 2006, 18:39
 *
 */

package org.wandora.application.tools.layers;

import org.wandora.application.gui.LayerTree;
import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.topicmap.linked.LinkedTopicMap;
import org.wandora.application.gui.*;
import javax.swing.*;


/**
 * <p>
 * Deletes topic map layer. Layer deletion is confirmed by the <code>LayerControlPanel</code>. 
 * </p>
 * 
 * @author akivela
 */


public class DeleteLayer extends AbstractLayerTool implements WandoraTool {
    
	private static final long serialVersionUID = 1L;

	
	/** Creates a new instance of DeleteLayer */
    public DeleteLayer() {
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layer_delete.png");
    }

    @Override
    public String getName() {
        return "Delete layer";
    }

    @Override
    public String getDescription() {
        return "Delete current topic map layer.";
    }
   
    @Override
    public void execute(Wandora wandora, Context context) {
        LayerTree layerTree = wandora.layerTree;
        Layer contextLayer =  solveContextLayer(wandora, context);
        
        if(contextLayer == null) {
            WandoraOptionPane.showMessageDialog(wandora, "There is no current topic map layer.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TopicMap contextTM=contextLayer.getTopicMap();
        
        for(Layer l : layerTree.getRootStack().getTreeLayers()){
            TopicMap tm=l.getTopicMap();
            if(tm instanceof LinkedTopicMap){
                if(((LinkedTopicMap)tm).getLinkedTopicMap()==contextTM){
                    WandoraOptionPane.showMessageDialog(wandora, "Layer is used in the linked topic layer \""+l.getName()+"\" and cannot be deleted.","Delete layer",WandoraOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        
        try {
        	wandora.getTopicMap().clearTopicMapIndexes();
        }
        catch(Exception e) {
            singleLog(e);
        }
        layerTree.deleteLayer(contextLayer);
    }
}
