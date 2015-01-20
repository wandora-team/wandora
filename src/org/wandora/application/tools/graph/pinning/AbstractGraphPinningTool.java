/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * AbstractGraphPinningTool.java
 *
 * Created on 14. kesäkuuta 2007, 13:04
 *
 */

package org.wandora.application.tools.graph.pinning;


import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.tools.graph.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public abstract class AbstractGraphPinningTool extends AbstractGraphTool {
    
    public static final int SET_PINNED = 10;
    public static final int SET_UNPINNED = 20;
    public static final int REVERSE_PINNING = 30;
    
    
    /** Creates a new instance of AbstractGraphPinningTool */
    public AbstractGraphPinningTool(TopicMapGraphPanel gp) {
        super(gp);
    }
 
    public void setPinning(Iterator nodes, int mode) {
        if(nodes == null) return;
        VNode vn = null;
        while(nodes.hasNext()) {
            try {
                vn = (VNode) nodes.next();
                if(vn != null) {
                    switch(mode) {
                        case SET_PINNED:
                            vn.setPinned(true);
                            break;
                        case SET_UNPINNED:
                            vn.setPinned(false);
                            break;
                        case REVERSE_PINNING:
                            vn.setPinned(!vn.isPinned());
                            break;
                    }
                }
            }
            catch (Exception e) {
                singleLog(e);
            }
        }
    }
}
