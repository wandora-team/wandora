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
 * SelectCellsInTopicTable.java
 *
 * Created on 14. hein�kuuta 2006, 11:51
 *
 */

package org.wandora.application.tools.selections;


import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.util.*;
import javax.swing.*;
import org.wandora.application.gui.table.TopicGrid;



/**
 *
 * @author akivela
 */
public abstract class DoTopicSelection extends DoSelection implements WandoraTool {
    

	private static final long serialVersionUID = 1L;

	
	@Override
    public void doTableSelection(Wandora admin, TopicTable table) {
        table.clearSelection();
        int colCount = table.getColumnCount();
        int rowCount = table.getRowCount();
        for(int c=0; c<colCount; c++) {
            for(int r=0; r<rowCount; r++) {
                if(acceptTopic(table.getTopicAt(r, c))) {
                    table.selectCell(c,r);
                    //table.addColumnSelectionInterval(j, j);
                    //table.addRowSelectionInterval(i, i);
                }
            }
        }
    }
    
    
    @Override
    public void doGridSelection(Wandora wandora, TopicGrid grid) {
        grid.clearSelection();
        int colCount = grid.getColumnCount();
        int rowCount = grid.getRowCount();
        for(int c=0; c<colCount; c++) {
            for(int r=0; r<rowCount; r++) {
                if(acceptTopic(grid.getTopicAt(r, c))) {
                    grid.selectCell(c,r);
                    //table.addColumnSelectionInterval(j, j);
                    //table.addRowSelectionInterval(i, i);
                }
            }
        }
    }
    
    
    
    @Override
    public void doGraphSelection(Wandora admin, TopicMapGraphPanel graph) {
        if(graph != null) {
            VModel model = graph.getModel();
            if(model != null) {
                model.deselectAll();
                VNode vnode = null;
                Topic t = null;
                for(Iterator vnodes = model.getNodes().iterator(); vnodes.hasNext(); ) {
                    vnode = (VNode) vnodes.next();
                    if(vnode != null) {
                        Node n = vnode.getNode();
                        if(n instanceof TopicNode) {
                            t = ((TopicNode) n).getTopic();
                            if(t != null) {
                                if(acceptTopic(t)) {
                                    model.addSelection(vnode);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    // ***** EXTEND THIS METHOD IN YOUR SELECTION TOOL *****
    public abstract boolean acceptTopic(Topic topic);
    
    
    
    @Override
    public String getName() {
        return "Abstract topic selection tool.";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/select_topics.png");
    }
}
