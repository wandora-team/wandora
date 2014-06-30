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
 * ClearSelection.java
 *
 * Created on 12. huhtikuuta 2006, 10:22
 *
 */

package org.wandora.application.tools.selections;

import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;
import org.wandora.application.gui.table.TopicGrid;

/**
 * Class implements <code>WandoraAdminTool</code> used to clear current selection.
 * In topic table context all selected cells are unselected and in text component
 * context any text selections are cleared.
 *
 * @author akivela
 */
public class ClearSelection extends DoSelection {
    
    /** Creates a new instance of ClearSelection */
    public ClearSelection() {
    }
    
    
    @Override
    public void doTableSelection(Wandora admin, SITable siTable) {
        siTable.clearSelection();
    }
    
    @Override
    public void doTableSelection(Wandora admin, TopicTable table) {
        table.clearSelection();
    }
    
    
    @Override
    public void doGridSelection(Wandora wandora, TopicGrid grid) {
        grid.clearSelection();
    }
    
    
    @Override
    public void doGraphSelection(Wandora admin, TopicMapGraphPanel graph) {
        if(graph != null) {
            VModel model = graph.getModel();
            if(model != null) {
                model.deselectAll();
            }
        }
    }
    
    
    @Override
    public void doOtherSelection(Wandora admin, Component component) {
        if(component instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) component;
            tc.requestFocus();
            tc.setSelectionEnd(0);
        }
        else if(component instanceof JTable) {
            JTable jt = (JTable) component;
            jt.requestFocus();
            jt.clearSelection();
        }
    }
    
    
    @Override
    public String getName() {
        return "Clear selection";
    }
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/selection_clear.png");
    }
    
}
