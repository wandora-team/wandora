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
 * ClearTopicMap.java
 *
 * Created on 20.6.2006, 11:42
 *
 */

package org.wandora.application.tools.layers;


import org.wandora.topicmap.layered.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import javax.swing.*;


/**
 * <p>
 * Clears topic map in selected layer. Clearing a topic map may be
 * necessary with layer implementations where layer deletion is
 * not possible or costly operation.
 * </p>
 *
 * @author olli
 */
public class ClearTopicMap extends AbstractLayerTool {
    

	private static final long serialVersionUID = 1L;


	/** Creates a new instance of ClearTopicMap */
    public ClearTopicMap() {
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layer_topicmap_clear.png");
    }
    
    @Override
    public String getName() {
        return "Clear layer's topic map";
    }

    @Override
    public String getDescription() {
        return "You are about to clear the topic map in selected layer. Clearing deletes all topics "+
               "and associations in the topic map. "+
               "Operation is undoable. Are you sure?";
    }

    @Override
    public boolean requiresRefresh(){
        return true;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        Layer l = solveContextLayer(wandora, context);
        
        if(l != null) {
            int answer = WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to clear layer \""+l.getName()+"\"? Everything in the topic map will be deleted.", "Clear topic map", WandoraOptionPane.YES_NO_OPTION);
            if(answer != WandoraOptionPane.YES_OPTION ) return;
            
            setDefaultLogger();
            log("Deleting content of topic map in layer '"+l.getName()+"'.");
            l.getTopicMap().clearTopicMap();
            TopicMap tm=wandora.getTopicMap();
            if(tm instanceof LayerStack){
                ((LayerStack)tm).clearTopicIndex();
            }
            setState(CLOSE);
        }
        else {
            WandoraOptionPane.showMessageDialog(wandora, "There is no current topic map layer. Create a topic map layer first.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
        }
    }    
}
