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


import javax.swing.Icon;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.layered.Layer;
import org.wandora.topicmap.layered.LayerStack;


/**
 * <p>
 * Clears all indexes of current topic map. Indexes are used to cache some
 * elements in topic map. Caching elements may prevent the user in seeing
 * all possible changes in topic maps.
 * </p>
 * <p>
 * It should be noted that it depends on topic map implementation if
 * it uses indexing and if clearing indexes really clears them. For example,
 * memory topic map implementation <code>TopicMapImpl</code> doesn't use
 * indexing.
 * </p>
 *
 * @author olli, akivela
 */

public class ClearTopicMapIndexes extends AbstractLayerTool {

	
	private static final long serialVersionUID = 1L;


	/** Creates a new instance of ClearTopicMapIndexes */
    public ClearTopicMapIndexes() {
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/layer_index_clear.png");
    }
    @Override
    public String getName() {
        return "Clear topic map indexes";
    }
    @Override
    public String getDescription() {
        return "Clears topic map indexes of selected layer. Indexes are used to "+
               "cache topic map elements. Indexing may prevent Wandora user "+
               "seeing modifications of co-users in distributed environments. Clearing "+
               "index causes Wandora to update topic map elements.";
    }
    @Override
    public boolean requiresRefresh(){
        return true;
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        Layer l = solveContextLayer(wandora, context);
        
        if(l == null) {
            WandoraOptionPane.showMessageDialog(wandora, "There is no current topic map layer. Create a topic map layer first.", "No layer selected", WandoraOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TopicMap tm=wandora.getTopicMap();

        if(l != null) {
            int answer = WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to clear layer's '"+l.getName()+"' topic map indexes?", "Clear topic map indexes", WandoraOptionPane.YES_NO_OPTION);
            if(answer != WandoraOptionPane.YES_OPTION ) return;
            
            l.getTopicMap().clearTopicMapIndexes();
            if(tm instanceof LayerStack) { ((LayerStack)tm).clearTopicIndex(); }
        }

    }    
}
