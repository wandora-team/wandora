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
 * AbstractLayerTool.java
 *
 * Created on 13.6.2006, 18:47
 *
 */

package org.wandora.application.tools.layers;


import org.wandora.application.gui.LayerTree;
import org.wandora.application.*;
import org.wandora.application.tools.*;
import org.wandora.application.contexts.*;
import org.wandora.topicmap.layered.*;


/**
 *
 * @author akivela
 */
public abstract class AbstractLayerTool extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	
	protected Layer solveContextLayer(Wandora wandora, Context context) {
        LayerTree layerTree = wandora.layerTree;
        Layer layer=null;
        if(context.getContextSource() instanceof LayerTree){
            layer = ((LayerTree)context.getContextSource()).getLastClickedLayer();
        }
        if(layer == null) layer = layerTree.getSelectedLayer();
        return layer;
    }

}
