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
 * SelectionInfo.java
 *
 * Created on 12. huhtikuuta 2006, 10:27
 *
 */

package org.wandora.application.tools.selections;


import javax.swing.ListSelectionModel;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolLogger;
import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.gui.table.ClassTable;
import org.wandora.application.gui.table.InstanceTable;
import org.wandora.application.gui.table.SITable;
import org.wandora.application.gui.table.TopicGrid;
import org.wandora.application.gui.table.TopicTable;
import org.wandora.application.gui.topicpanels.graphpanel.TopicMapGraphPanel;
import org.wandora.application.gui.topicpanels.graphpanel.VModel;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.gui.tree.TopicTree;
import org.wandora.application.gui.tree.TopicTreeModel;
import org.wandora.utils.swing.anyselectiontable.TableSelectionModel;



/**
 * Class implements a special selection tool that does not modify current
 * selection but opens an info dialog about the selection. Tool is useful
 * to count selection size for example. Current tool supports only topic table
 * components. Tool has no defined behavior in context of text components.
 *
 * @author akivela
 */


public class SelectionInfo extends DoSelection {
    

	private static final long serialVersionUID = 1L;

	
	
	/** Creates a new instance of SelectionInfo */
    public SelectionInfo() {
    }
    


    @Override
    public void doTableSelection(Wandora admin, SITable table) {
        setDefaultLogger();
        setLogTitle("Subject identifier table selection info");
        int rowsCounter = table.getRowCount();
        int colsCounter = table.getColumnCount();
        //int rowSelectionCounter = 0;
        //int colSelectionCounter = 0;
        int cellSelectionCounter = 0;
        
        TableSelectionModel selection = table.getTableSelectionModel();
        for(int c=0; c<colsCounter; c++) {
            ListSelectionModel columnSelectionModel = selection.getListSelectionModelAt(c);
            if(columnSelectionModel != null && !columnSelectionModel.isSelectionEmpty()) {
                for(int r=0; r<rowsCounter; r++) {
                    if(columnSelectionModel.isSelectedIndex(r)) {
                        cellSelectionCounter++;
                    }
                }
            }
        }
        String message = "Subject identifier table contains " + rowsCounter + " rows";
        if(colsCounter > 1) message += " and " + (rowsCounter*colsCounter) + " cells.";
        else message += ".";
        if(cellSelectionCounter == 1) message += "\nSelection contains " + cellSelectionCounter + " cell.";
        if(cellSelectionCounter > 1) message += "\nSelection contains " + cellSelectionCounter + " cells.";

        log(message);
        setState(WandoraToolLogger.WAIT);
    }
    
    
    
    

    @Override
    public void doTableSelection(Wandora admin, TopicTable table) {
        setDefaultLogger();
        setLogTitle("Table selection info");
        int rowsCounter = table.getRowCount();
        int colsCounter = table.getColumnCount();
        //int rowSelectionCounter = 0;
        //int colSelectionCounter = 0;
        int cellSelectionCounter = 0;
        
        TableSelectionModel selection = table.getTableSelectionModel();
        for(int c=0; c<colsCounter; c++) {
            ListSelectionModel columnSelectionModel = selection.getListSelectionModelAt(c);
            if(columnSelectionModel != null && !columnSelectionModel.isSelectionEmpty()) {
                for(int r=0; r<rowsCounter; r++) {
                    if(columnSelectionModel.isSelectedIndex(r)) {
                        cellSelectionCounter++;
                    }
                }
            }
        }
        String message = getTableName(table) + " contains " + rowsCounter + " rows";
        if(colsCounter > 1) message += " and " + (rowsCounter*colsCounter) + " cells.";
        else message += ".";
        if(cellSelectionCounter == 1) message += "\nSelection contains " + cellSelectionCounter + " cell.";
        if(cellSelectionCounter > 1) message += "\nSelection contains " + cellSelectionCounter + " cells.";

        log(message);
        setState(WandoraToolLogger.WAIT);
    }
    
    

    
    private String getTableName(TopicTable t) {
        if(t == null) {
            return "null";
        }
        if(t instanceof AssociationTable) {
            AssociationTable at = (AssociationTable) t;
            return "Association table '"+TopicToString.toString(at.getAssociationTypeTopic())+"'";
        }
        if(t instanceof ClassTable) {
            return "Classes table";
        }
        if(t instanceof InstanceTable) {
            return "Instances table";
        }
        return "Topic table";
    }
    
    
    

    @Override
    public void doGridSelection(Wandora admin, TopicGrid grid) {
        setDefaultLogger();
        setLogTitle("Grid selection info");
        int rowsCounter = grid.getRowCount();
        int colsCounter = grid.getColumnCount();
        //int rowSelectionCounter = 0;
        //int colSelectionCounter = 0;
        int cellSelectionCounter = 0;
        
        TableSelectionModel selection = grid.getTableSelectionModel();
        for(int c=0; c<colsCounter; c++) {
            ListSelectionModel columnSelectionModel = selection.getListSelectionModelAt(c);
            if(columnSelectionModel != null && !columnSelectionModel.isSelectionEmpty()) {
                for(int r=0; r<rowsCounter; r++) {
                    if(columnSelectionModel.isSelectedIndex(r)) {
                        cellSelectionCounter++;
                    }
                }
            }
        }
        String message =  "Grid contains " + rowsCounter + " rows";
        if(colsCounter > 1) message += " and " + (rowsCounter*colsCounter) + " cells.";
        else message += ".";
        if(cellSelectionCounter == 1) message += "\nSelection contains " + cellSelectionCounter + " cell.";
        if(cellSelectionCounter > 1) message += "\nSelection contains " + cellSelectionCounter + " cells.";

        log(message);
        setState(WandoraToolLogger.WAIT);
    }
    
    
    
    
    @Override
    public void doGraphSelection(Wandora admin, TopicMapGraphPanel graph) {
        setDefaultLogger();
        setLogTitle("Graph selection info");
        if(graph != null) {
            VModel model = graph.getModel();
            if(model != null) {
                int nodeCount = model.getNodes().size();
                int edgeCount = model.getEdges().size();
                int nodeSelectionCount = model.getSelectedNodes().size();
                int edgeSelectionCount = model.getSelectedEdges().size();
                String message = "Graph contains " + nodeCount + " nodes and "+ edgeCount + " edges.";
                message += "\nSelection contains " + nodeSelectionCount + " nodes and "+ edgeSelectionCount + " edges.";
                log(message);
                setState(WandoraToolLogger.WAIT);
            }
        }
    }
    
    
    @Override
    public void doTreeSelection(Wandora admin, TopicTree tree) {
        setDefaultLogger();
        setLogTitle("Tree selection info");
        if(tree != null) {
            TopicTreeModel model = (TopicTreeModel) tree.getModel();
            if(model != null) {
                int topicCount = model.getVisibleTopicCount();
                String message = "Tree contains " + topicCount + " topics at the moment.";
                message += "\nTree selection contains always one topic.";
                message += "\nAt the moment selected tree topic is '" + tree.getCopyString() + "'.";
                log(message);
                setState(WandoraToolLogger.WAIT);
            }
        }
    }
    
    @Override
    public String getName() {
        return "Topic selection info";
    }
    
    
}
