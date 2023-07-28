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
 * RenameLayer.java
 *
 * Created on 21. huhtikuuta 2006, 20:00
 */

package org.wandora.application.tools.layers;


import org.wandora.application.gui.LayerTree;
import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import javax.swing.*;



/**
 * Renames topic map layer.
 * Like many other layer tools, this tool passes the execution to
 * <code>LayerStatusPanel</code>.
 *
 * @author akivela
 */
public class RenameLayer extends AbstractLayerTool implements WandoraTool {
    
	private static final long serialVersionUID = 1L;


	/** Creates a new instance of RenameLayer */
    public RenameLayer() {
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layer_rename.png");
    }
    @Override
    public String getName() {
        return "Rename layer";
    }
    @Override
    public String getDescription() {
        return "Rename current layer.";
    }
    
   
    @Override
    public void execute(Wandora wandora, Context context) {      
        try {
            LayerTree layerTree = wandora.layerTree;           
            Layer selected = solveContextLayer(wandora, context);
            if(selected != null) {
                String newName = WandoraOptionPane.showInputDialog(wandora,"New name for the layer" , selected.getName(),"New layer name" , WandoraOptionPane.QUESTION_MESSAGE );
                if(newName != null) {
                    selected.setName(newName);
                    layerTree.resetLayers();
                }
            }
            else {
                WandoraOptionPane.showMessageDialog(wandora, "There is no current topic map layer. Create a topic map layer first.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
            }
        }
        catch(Exception e) {
            singleLog(e);
        }
    }
}
