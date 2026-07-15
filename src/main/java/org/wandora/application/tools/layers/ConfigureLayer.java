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
 * ConfigureLayer.java
 *
 * Created on 21. huhtikuuta 2006, 19:58
 *
 */

package org.wandora.application.tools.layers;




import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.layered.Layer;


/**
 * <p>
 * Configures existing topic map layer. For example, configuring a database 
 * topic map layer allows the user to change database options such as
 * database server address.
 * </p>
 * <p>
 * Notice that some topic map layer implementations such as memory topic map
 * doesn't support configuration.
 * </p>
 * 
 * @author akivela
 */


public class ConfigureLayer extends AbstractLayerTool implements WandoraTool {
    
	
	private static final long serialVersionUID = 1L;


	/** Creates a new instance of ConfigureLayer */
    public ConfigureLayer() {
    }

    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layer_configure.png");
    }

    @Override
    public String getName() {
        return "Configure Layer";
    }

    @Override
    public String getDescription() {
        return "Allows Wandora user to reconfigures current layer. Reconfiguration is required when database settings have changed, for example.";
    }
    
    
    public void execute(Wandora wandora, Context context) {      
        try {
            Layer contextLayer =  solveContextLayer(wandora, context);
            
            if(contextLayer == null) {
                WandoraOptionPane.showMessageDialog(wandora, "There is no current topic map layer. Create a topic map layer first.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
                return;
            }
            
            wandora.layerTree.modifyLayer(contextLayer);
        }
        catch(Exception e) {
            singleLog(e);
        }
    }
}
