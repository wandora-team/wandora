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
 * SelectInTopicTable.java
 *
 * Created on 21. marraskuuta 2005, 19:50
 *
 */

package org.wandora.application.tools.selections;


import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.tools.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.topicpanels.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import java.awt.*;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.application.gui.tree.TopicTree;



/**
 * Class implements a tool used to select cells in topic tables. Although
 * class name refers only to topic tables tool can be used to make other type
 * selections also. Tool execution flows to <code>doOtherSelection</code> if
 * <code>contextSource</code> is not instance of <code>TopicTable</code>.
 *
 * @author akivela
 */
public class DoSelection extends AbstractWandoraTool implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	public DoSelection() {}
    
    
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            initializeTool();
            SITable siTable = null;
            TopicTable table = null;
            TopicGrid grid = null;
            TopicTree tree = null;
            TopicMapGraphPanel graph = null;
            Object contextSource = context.getContextSource();
            if(contextSource instanceof SITable) {
                siTable = (SITable) contextSource;
                doTableSelection(wandora, siTable);
            }
            else if(contextSource instanceof TopicTable) {
                table = (TopicTable) contextSource;
                doTableSelection(wandora, table);
            }
            else if(contextSource instanceof TopicGrid) {
                grid = (TopicGrid) contextSource;
                doGridSelection(wandora, grid);
            }
            else if(contextSource instanceof TopicTree) {
                tree = (TopicTree) contextSource;
                doTreeSelection(wandora, tree);
            }
            else {
                Component component = wandora.getFocusOwner();
                if(component instanceof TopicTable) {
                    table = (TopicTable) component;
                    doTableSelection(wandora, table);
                }
                else if(component instanceof TopicTree) {
                    tree = (TopicTree) component;
                    doTreeSelection(wandora, tree);
                }
                else if(component instanceof GraphTopicPanel) {
                    graph = ((GraphTopicPanel) component).getGraphPanel();
                    doGraphSelection(wandora, graph);
                }
                else if(component instanceof TopicMapGraphPanel) {
                    graph = (TopicMapGraphPanel) component;
                    doGraphSelection(wandora, graph);
                }
                else if(component != null) {
                    doOtherSelection(wandora, component);
                }
            }           
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    public void doOtherSelection(Wandora wandora, Component component) {
    }

        
    public void doTableSelection(Wandora wandora, SITable siTable) {
        log("Warning: doTableSelection call catched in DoSelection! You should overwrite this method in your own selection class!");
    }
    public void doTableSelection(Wandora wandora, TopicTable table) {
        log("Warning: doTableSelection call catched in DoSelection! You should overwrite this method in your own selection class!");
    }
    
    public void doGraphSelection(Wandora wandora, TopicMapGraphPanel graph) {
        log("Warning: doGraphSelection call catched in DoSelection! You should overwrite this method in your own selection class!");
    }
    
    public void doGridSelection(Wandora wandora, TopicGrid grid) {
        log("Warning: doGridSelection call catched in DoSelection! You should overwrite this method in your own selection class!");
    }
    
    
    public void doTreeSelection(Wandora wandora, TopicTree tree) {
        log("Topic tree doesn't support chosen selection option. "+
            "Please, select topic table type of element and try again.");
    }
    
    
    @Override
    public String getName() {
        return "Make selections (abstract class)";
    }
 
    public void initializeTool() {
        // OVERWRITE IF INITIALIZATION IS REQUIRED!!!
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}

