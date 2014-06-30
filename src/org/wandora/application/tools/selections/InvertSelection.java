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
 * InvertSelection.java
 *
 * Created on 12. huhtikuuta 2006, 10:22
 *
 */

package org.wandora.application.tools.selections;

import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;

import java.util.*;
import javax.swing.*;
import org.wandora.application.gui.table.TopicGrid;



/**
 * Tool inverts current selection in current topic table. Inversion deselects
 * all selected and selects all unselected cells. Tool supports currently only
 * topic table context and has no specified behavior with text components for
 * example.
 *
 * @author akivela
 */


public class InvertSelection extends DoSelection {
    
    /** Creates a new instance of InvertSelection */
    public InvertSelection() {
    }
    
    
    
    @Override
    public void doTableSelection(Wandora admin, SITable siTable) {
        siTable.invertSelection();
    }
    
    
    
    @Override
    public void doTableSelection(Wandora admin, TopicTable table) {
        table.invertSelection();
    }
    
    
    @Override
    public void doGridSelection(Wandora wandora, TopicGrid grid) {
        grid.invertSelection();
    }
    
    
    
    @Override
    public void doGraphSelection(Wandora admin, TopicMapGraphPanel graph) {
        if(graph != null) {
            VModel model = graph.getModel();
            if(model != null) {
                VNode vnode = null;
                for(Iterator vnodes = model.getNodes().iterator(); vnodes.hasNext(); ) {
                    vnode = (VNode) vnodes.next();
                    if(vnode != null) {
                        if( vnode.isSelected() ) {
                            model.deselectNode(vnode);
                        }
                        else {
                            model.addSelection(vnode);
                        }
                    }
                }
            }
        }
    }
    
    
    @Override
    public String getName() {
        return "Invert selection";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/selection_invert.png");
    }
}
