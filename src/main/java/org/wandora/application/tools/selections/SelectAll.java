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
 * SelectAll.java
 *
 * Created on 12. huhtikuuta 2006, 10:17
 *
 */

package org.wandora.application.tools.selections;


import java.awt.Component;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;
import org.wandora.application.gui.topicpanels.graphpanel.VModel;
import org.wandora.application.gui.topicpanels.graphpanel.VNode;


/**
 * Class implements select tool that tries to select all available elements.
 * In case of <code>TopicTable</code> tool selects all cells in current
 * focused topic table. In case of text fields and other text containers
 * tool selects entire text.
 *
 * @author akivela
 */


public class SelectAll extends DoSelection {
       
	private static final long serialVersionUID = 1L;
	
	
	@Override
    public void doOtherSelection(Wandora wandora, Component component) {
        if(component instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) component;
            tc.requestFocus();
            tc.selectAll();
        }
        else if(component instanceof SimpleField) {
            SimpleField f = (SimpleField) component;
            f.requestFocus();
            f.selectAll();
        }
        else if(component instanceof JTable) {
            JTable jt = (JTable) component;
            jt.requestFocus();
            jt.selectAll();
        }
    }
    
    @Override
    public void doTableSelection(Wandora wandora, SITable siTable) {
        siTable.selectAll();
    }
    
    @Override
    public void doTableSelection(Wandora wandora, TopicTable table) {
        table.selectAll();
    }
    
    @Override
    public void doGridSelection(Wandora wandora, TopicGrid grid) {
        grid.selectAll();
    }
    
    
    @Override
    public void doGraphSelection(Wandora wandora, TopicMapGraphPanel graph) {
        if(graph != null) {
            VModel model = graph.getModel();
            if(model != null) {
                model.deselectAll();
                VNode vnode = null;
                for(Iterator<VNode> vnodes = model.getNodes().iterator(); vnodes.hasNext(); ) {
                    vnode = (VNode) vnodes.next();
                    if(vnode != null) {
                        model.addSelection(vnode);
                    }
                }
            }
        }
    }
 
    @Override
    public String getName() {
        return "Select all";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/select_all.png");
    }
}
