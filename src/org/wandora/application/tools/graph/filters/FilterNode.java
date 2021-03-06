/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * FilterNode.java
 *
 * Created on 6. kes�kuuta 2007, 15:38
 *
 */

package org.wandora.application.tools.graph.filters;

import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.tools.graph.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class FilterNode extends AbstractGraphTool {
    private GraphFilter filter;
    
    public FilterNode(GraphFilter filter) {
        this.filter=filter;
        this.setContext(new GraphNodeContext());
    }
    
    public FilterNode(NodeFilter filter) {
        this((GraphFilter)filter);
    }
    
    @Override
    public String getName(){
        return "Filter graph node";
    }
    

    
    public void executeSynchronized(Wandora admin, Context context) {
        VModel model = null;
        VNode node = null;
        for(Iterator iter = context.getContextObjects(); iter.hasNext(); ) {
            try {
                Object o = iter.next();
                if(o instanceof VNode){
                    node = (VNode) o;
                    if(node != null) {
                        model = node.getModel();
                        if(model != null) {
                            model.removeNode(node);
                            filter.filterNode(node);
                        }
                    }
                }
            }
            catch(Exception e) {
                singleLog(e);
            }
        }
    }
}
