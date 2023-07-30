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
 * ArrangeLayers.java
 *
 * Created on 26. helmikuuta 2006, 19:12
 *
 */

package org.wandora.application.tools.layers;


import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.LayerTree;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.layered.Layer;


/**
 * <p>
 * Arranged topic map layers in layer stack.
 * Topic map layer can be moved upward, downward, top and bottom. Also, 
 * topic map layer order can be reversed.
 * </p>
 * <p>
 * Exact behavior of tool is specified with <code>options</code> parameter
 * shipped to class constructor. Tool has no public set method for behavior
 * and it can not be altered after construction.
 * </p>
 * <p>
 * Tool does not alter the layer order directly but calls <code>arrangeLayers</code>
 * method in <code>LayerControlPanel</code>.
 * </p>
 * 
 * @author akivela
 */

public class ArrangeLayers extends AbstractLayerTool implements WandoraTool {


	private static final long serialVersionUID = 1L;

	private int option = LayerTree.MOVE_LAYER_UP;



    /** Creates a new instance of ArrangeLayers */
    public ArrangeLayers() {
    }
    public ArrangeLayers(int option) {
        this.option = option;
    }
    
    
    @Override
    public Icon getIcon() {
        switch(option) {
            case LayerTree.MOVE_LAYER_UP: {
                return UIBox.getIcon("gui/icons/move_up.png");
            }
            case LayerTree.MOVE_LAYER_DOWN: {
                return UIBox.getIcon("gui/icons/move_down.png");
            }
            case LayerTree.MOVE_LAYER_TOP: {
                return UIBox.getIcon("gui/icons/move_top.png");
            }
            case LayerTree.MOVE_LAYER_BOTTOM: {
                return UIBox.getIcon("gui/icons/move_bottom.png");
            }
            case LayerTree.REVERSE_LAYERS: {
                return UIBox.getIcon("gui/icons/reverse_order.png.png");
            }
        }
        return UIBox.getIcon("gui/icons/layers_arrange.png");
    }

    @Override
    public String getName() {
        switch(option) {
            case LayerTree.MOVE_LAYER_UP: {
                return "Move layer upward";
            }
            case LayerTree.MOVE_LAYER_DOWN: {
                return "Move layer downward";
            }
            case LayerTree.MOVE_LAYER_TOP: {
                return "Move layer to top";
            }
            case LayerTree.MOVE_LAYER_BOTTOM: {
                return "Move layer to bottom";
            }
            case LayerTree.REVERSE_LAYERS: {
                return "Reverse layer order";
            }
        }
        return "Arrange Layers";
    }

    @Override
    public String getDescription() {
        switch(option) {
            case LayerTree.MOVE_LAYER_UP: {
                return "Move current layer upward.";
            }
            case LayerTree.MOVE_LAYER_DOWN: {
                return "Move current layer downward.";
            }
            case LayerTree.MOVE_LAYER_TOP: {
                return "Move current layer to top.";
            }
            case LayerTree.MOVE_LAYER_BOTTOM: {
                return "Move current layer to bottom.";
            }
            case LayerTree.REVERSE_LAYERS: {
                return "Reverse layer order.";
            }
        }
        return "Tool is used to modify order of topic map layers.";
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {      
        try {
            LayerTree layerTree = wandora.layerTree;
            Layer contextLayer =  solveContextLayer(wandora, context);
            if(contextLayer != null) {
                layerTree.arrangeLayers(contextLayer, option);
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
