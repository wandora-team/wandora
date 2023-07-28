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
 * ExpandNodeTool.java
 *
 * Created on 6.6.2007, 15:16
 *
 */

package org.wandora.application.tools.graph;


import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class ExpandNodeTool extends AbstractGraphTool {

	private static final long serialVersionUID = 1L;

	public ExpandNodeTool(TopicMapGraphPanel gp) {
        super(gp);
        this.setContext(new GraphNodeContext());
    }
    
    
    @Override
    public String getName(){
        return "Open graph node";
    }
    
    public void executeSynchronized(Wandora wandora, Context context) {
        VModel model = null;
        VNode node = null;
        for(Iterator iter = context.getContextObjects(); iter.hasNext(); ) {
            try {
                node = (VNode) iter.next();
                if(node != null) {
                    model = node.getModel();
                    if(model != null) {
                        model.openNode(node);
                    }
                }
            }
            catch(Exception e) {
                singleLog(e);
            }
        }
    }
    
}
