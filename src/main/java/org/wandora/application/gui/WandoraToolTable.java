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
 * 
 * WandoraToolTable.java
 *
 * Created on June 17, 2004, 3:35 PM
 */


package org.wandora.application.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolManager2;
import org.wandora.application.WandoraToolManager2.ToolInfo;
import org.wandora.application.gui.table.TopicTableSorter;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.utils.swing.TableSorter;



/**
 *
 * @author akivela
 */
public class WandoraToolTable extends JTable implements MouseListener, ActionListener /*, DragSourceListener , DragGestureListener*/ {

    
	private static final long serialVersionUID = 1L;

	private Wandora wandora = null;
    
    private Object[] cols = {
        "Name",
        "Class",
        "Types",
        "Source"
    };
    private int[] colsWidths = {
        170,
        400,
        80,
        200
    };

    private String[] descriptions;
    
    private Object[][] data;

    private TableModel tableModel;
    private WandoraTool[] tools = null;
    
    private Object[] popupStruct = new Object[] {
        "Info...",
        "---",
        "Execute...",
        "Configure...",
        "Release tool locks...",
        "Kill threads...",
    };
    private JPopupMenu toolTableMenu = null;
    private TableSorter sorter;
    
    
    private MouseEvent mouseEvent;
    
    
    public WandoraToolTable(Wandora w) {
        this.wandora = w;
    }
    
    
 
    public void initialize(WandoraTool[] t) {
        WandoraToolManager2 toolManager=wandora.getToolManager();
        try {
            if(t == null) return;
            tools = t;
            this.setAutoCreateColumnsFromModel(false); 
                        
            descriptions = new String[tools.length];
            data = new Object[tools.length][4];
            for(int i=0; i<tools.length; i++) {
                data[i][0]=tools[i].getName();
                data[i][1]=tools[i].getClass().getName();
                data[i][2]=tools[i].getType().toString();
                data[i][3]="unknown";
                if(toolManager!=null){
                    ToolInfo info=toolManager.getToolInfo(tools[i]);
                    if(info!=null) data[i][3]=info.sourceType+":"+info.source;
                }
                descriptions[i] = tools[i].getDescription();
            }

            tableModel = new ToolTableModel();
            sorter = new TopicTableSorter(tableModel);
            sorter.setTableHeader(this.getTableHeader());
            this.setModel(sorter);
            
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            this.setRowSelectionAllowed(true);
            this.setColumnSelectionAllowed(false);
            
            for(int i=0; i<cols.length; i++) {
                this.addColumn(new TableColumn(i,colsWidths[i],new ToolTableCellRenderer(),null));
            }
            this.addMouseListener(this);
            
            toolTableMenu = UIBox.makePopupMenu(popupStruct, this);
            this.setComponentPopupMenu(toolTableMenu);
        }
        catch(Exception e) {
            if(wandora != null) wandora.handleError(e);
            else e.printStackTrace();
        }
        
    }
    
    
    
    public WandoraTool getSelectedTool() {
        int row = this.getSelectedRow();
        if(row >= 0 && row < tools.length) {
            return tools[mapRowIndexToModel(row)];
        }
        return null;
    }
    
    public WandoraTool getToolAt(MouseEvent e) {
        return getToolAt(e.getPoint());
    }
    public WandoraTool getToolAt(int x, int y) {
        return getToolAt(new Point(x,y));
    }
    public WandoraTool getToolAt(Point point) {
        int row=rowAtPoint(point);
        return tools[mapRowIndexToModel(row)];
    }
    
    public int mapRowIndexToModel(int row){
        //return row;
        return sorter.modelIndex(row);
    }
    public int mapRowIndexToView(int row){
        //return row;
        return sorter.viewIndex(row);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
        
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
        if(mouseEvent.getClickCount()>=2){
            WandoraToolManager2 toolManager=wandora.getToolManager();
            ToolInfo info=null;
            WandoraTool tool=getToolAt(mouseEvent);
            if(toolManager!=null) info=toolManager.getToolInfo(tool);
            
            new WandoraToolInfoDialog(wandora, tool, info);
        }
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {

    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    
    
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // System.out.println("Action event == "+e.getActionCommand());
        String c = e.getActionCommand();
        if(c == null) return;
        c = c.toLowerCase();
        
        // ***** EXECUTE *****
        if(c.startsWith("execute")) {
            try {
                WandoraTool t = getSelectedTool();
                if(t != null) {
                    t.execute(wandora);
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            catch(Error er) {
                er.printStackTrace();
            }
        }
        
        // ***** CONFIGURE *****
        else if(c.startsWith("configure")) {
            try {
                WandoraTool t = getSelectedTool();
                if(t != null) {
                    if(t.isConfigurable()) {
                        t.configure(wandora, wandora.getOptions(), wandora.getToolManager().getOptionsPrefix(t));
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(wandora, "Tool is not configurable!", "Not configurable");
                    }
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            catch(Error er) {
                er.printStackTrace();
            }
        }
        
        
        // ***** RELEASE TOOL LOCKS *****
        else if(c.startsWith("release tool locks")) {
            try {
                WandoraTool t = getSelectedTool();
                if(t != null) {
                    String className = t.getClass().getSimpleName();
                    if(t instanceof AbstractWandoraTool && !((AbstractWandoraTool) t).allowMultipleInvocations()) {
                        int a = WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to release tool lock for class '"+className+"'", "Release tool lock");
                        if(a == WandoraOptionPane.YES_OPTION) {
                            boolean released = ((AbstractWandoraTool) t).clearToolLock();
                            if(!released) WandoraOptionPane.showMessageDialog(wandora, "Tool class '"+className+"' was not locked. Couldn't release tool locks.");
                        }
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(wandora, "Tool class '"+className+"' doesn't support tool locks. Can't release tool locks.");
                    }
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            catch(Error er) {
                er.printStackTrace();
            }
        }
        
        // ***** KILL TOOL THREADS *****
        else if(c.startsWith("kill threads")) {
            try {
                WandoraTool t = getSelectedTool();
                if(t != null) {
                    String className = t.getClass().getSimpleName();
                    if(t instanceof AbstractWandoraTool && ((AbstractWandoraTool) t).runInOwnThread()) {
                        int a = WandoraOptionPane.showConfirmDialog(wandora, "Are you sure you want to interrupt tool threads for class '"+className+"'", "Interrupt tool threads");
                        if(a == WandoraOptionPane.YES_OPTION) {
                            ((AbstractWandoraTool) t).interruptThreads();
                        }
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(wandora, "Tool class '"+className+"' doesn't support interrups. Can't kill tool threads.");
                    }
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            catch(Error er) {
                er.printStackTrace();
            }
        }
        
        // ***** TOOL INFO *****
        else if(c.startsWith("info")) {
            try {
                WandoraTool t = getSelectedTool();
                if(t != null) {
                    WandoraToolManager2 toolManager=wandora.getToolManager();
                    ToolInfo info=null;
                    if(toolManager!=null) info=toolManager.getToolInfo(t);
                    
                    new WandoraToolInfoDialog(wandora, t, info);
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------ ToolTableModel --- 
    // -------------------------------------------------------------------------
    
    
    
        
    protected class ToolTableModel extends AbstractTableModel {
        
        
        
        @Override
        public int getColumnCount() {
            if(cols != null) return cols.length;
            return 0;
        }
        
        
        
        @Override
        public int getRowCount() {
            if(data != null) return data.length;
            return 0;
        }
        
        
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                if(data != null && rowIndex >= 0 && columnIndex >= 0) {
                    return data[rowIndex][columnIndex];
                }
            }
            catch (Exception e) {
                if(wandora != null) wandora.handleError(e);
                else e.printStackTrace();
            }
            return null;
        }
        
        @Override
        public String getColumnName(int columnIndex){
            try {
                if(cols != null && columnIndex >= 0 && cols.length > columnIndex && cols[columnIndex] != null) {
                    return cols[columnIndex].toString();
                }
                return "";
            }
            catch (Exception e) {
                if(wandora != null) wandora.handleError(e);
                e.printStackTrace();
            }
            return "ERROR";
        }
        
        
        @Override
        public boolean isCellEditable(int row,int col){
            return false;
        }
        
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public class ToolTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {


        @Override
        public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(c instanceof JLabel) {
                JLabel l = (JLabel) c;
                if(column == 0) {
                    l.setIcon(tools[mapRowIndexToModel(row)].getIcon());
                }
                //l.setToolTipText(Textbox.makeHTMLParagraph(descriptions[row], 40));
            }
            return c;
        }

    }
    
    
    // -------------------------------------------------------------------------
    
    
    public class ToolTableSorter extends TableSorter {


        public ToolTableSorter() {
            super();
        }

        public ToolTableSorter(TableModel tableModel) {
            super(tableModel);
        }

        public ToolTableSorter(TableModel tableModel, JTableHeader tableHeader) {
            super(tableModel, tableHeader);
        }

    }
}
