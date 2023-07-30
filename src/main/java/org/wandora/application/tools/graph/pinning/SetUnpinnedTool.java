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
 * SetUnpinnedTool.java
 *
 * Created on 14.6.2007, 13:13
 *
 */

package org.wandora.application.tools.graph.pinning;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.contexts.GraphNodeContext;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;


/**
 *
 * @author akivela
 */

public class SetUnpinnedTool extends AbstractGraphPinningTool {
    

	private static final long serialVersionUID = 1L;



	/** Creates a new instance of SetUnpinnedTool */
    public SetUnpinnedTool(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
    }
    public SetUnpinnedTool(TopicMapGraphPanel gp, Context proposedContext) {
        super(gp);
        this.setContext(proposedContext);
    }
    
    
    @Override
    public String getName(){
        return "Set node unpinned";
    }
    
    

    public void executeSynchronized(Wandora wandora, Context context) {
        if(context != null) {
            setPinning(context.getContextObjects(), SET_UNPINNED);
        }
    }
    
}
