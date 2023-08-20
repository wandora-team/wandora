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
 * ConnectNodesTool.java
 *
 * Created on 16.7.2007, 14:43
 *
 */

package org.wandora.application.tools.graph;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;

/**
 *
 * @author olli
 */
public class ConnectNodesTool extends AbstractGraphTool {
    
	private static final long serialVersionUID = 1L;

	/** Creates a new instance of ConnectNodesTool */
    public ConnectNodesTool(TopicMapGraphPanel gp) {
        super(gp);
    }
    
    @Override
    public String getName(){
        return "Connect visible nodes";
    }
    
    public void executeSynchronized(Wandora admin, Context context) {
        solveModel(admin,context).connectAllNodes();
    }    
}
