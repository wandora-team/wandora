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
 */
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import bibliothek.gui.DockController;
import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.DefaultDockable;
import bibliothek.gui.dock.SplitDockStation;
import bibliothek.gui.dock.StackDockStation;
import bibliothek.gui.dock.station.split.SplitDockGrid;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 *
 * @author olli
 */


public class QueryEditorDockPanel extends JPanel {
    protected DockController dockController;
    protected SplitDockStation dockStation;
    
    protected QueryEditorComponent queryEditor;
    protected DirectivesListPanel directivesList;
    protected QueryEditorInspectorPanel inspector;
    protected QueryLibraryPanel queryLibrary;
    
    protected ResultsPanel resultsPanel;
    
    protected Dockable resultsDockable;
    protected Dockable editorDockable;
    
    public QueryEditorDockPanel() {
        dockController=new DockController();
        dockStation=new SplitDockStation();
        dockController.add(dockStation);
        
        queryEditor=new QueryEditorComponent();
        directivesList=new DirectivesListPanel();
        inspector=new QueryEditorInspectorPanel();
        queryLibrary=new QueryLibraryPanel();
        resultsPanel=new ResultsPanel();
        
        StackDockStation mainStack=new StackDockStation();
        editorDockable=new DefaultDockable(queryEditor, "Graph Editor");
        mainStack.add(editorDockable, 0);
        resultsDockable=new DefaultDockable(resultsPanel, "Query results");
        mainStack.add(resultsDockable, 1);
        mainStack.setFrontDockable(editorDockable);
        
        
        StackDockStation topRight=new StackDockStation();
        DefaultDockable directivesDockable=new DefaultDockable(directivesList, "Directives list");
        topRight.add(directivesDockable, 0);
        topRight.add(new DefaultDockable(queryLibrary, "QueryLibrary"), 1);
        topRight.setFrontDockable(directivesDockable);
        
        
        SplitDockGrid grid=new SplitDockGrid();
        grid.addDockable(0,0,2,2, mainStack);
        grid.addDockable(2,0,1,1, topRight);
        grid.addDockable(2,1,1,1, new DefaultDockable(inspector, "Inspector"));
        dockStation.dropTree( grid.toTree() );
        
        
        
        this.setLayout(new BorderLayout());
        this.add(dockStation.getComponent());
    }

    public QueryEditorComponent getQueryEditor() {
        return queryEditor;
    }

    public DirectivesListPanel getDirectivesList() {
        return directivesList;
    }

    public QueryEditorInspectorPanel getInspector() {
        return inspector;
    }

    public QueryLibraryPanel getQueryLibrary() {
        return queryLibrary;
    }
    
    public ResultsPanel getResultsPanel() {
        return resultsPanel;
    }
    
    public void bringEditorFront(){
        DockStation station=editorDockable.getDockParent();
        station.setFrontDockable(editorDockable);        
    }
    
    public void bringResultsFront(){
        DockStation station=resultsDockable.getDockParent();
        station.setFrontDockable(resultsDockable);
    }
    
}
