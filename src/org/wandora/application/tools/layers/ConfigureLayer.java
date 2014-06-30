/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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




import org.wandora.topicmap.layered.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;


import javax.swing.*;


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
    
    
    public void execute(Wandora admin, Context context) {      
        try {
            Layer contextLayer =  solveContextLayer(admin, context);
            
            if(contextLayer == null) {
                WandoraOptionPane.showMessageDialog(admin, "There is no current topic map layer. Create a topic map layer first.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
                return;
            }
            
            boolean found=false;
            java.util.List<Layer> layers=admin.getTopicMap().getTreeLayers();
            for(Layer l : layers){
                if(l==contextLayer){
                    found=true;
                    break;
                }
            }
            if(!found){
                int dummy=1;
            }
            
            admin.layerTree.modifyLayer(contextLayer);
        }
        catch(Exception e) {
            singleLog(e);
        }
    }
}
