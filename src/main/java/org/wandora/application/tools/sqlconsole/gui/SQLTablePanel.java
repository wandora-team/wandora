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
 * SQLTablePanel.java
 *
 * Created on November 12, 2004, 3:43 PM
 */

package org.wandora.application.tools.sqlconsole.gui;


import org.wandora.utils.Delegate;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.Textbox;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import org.wandora.application.tools.sqlconsole.data.*;
import org.wandora.application.tools.sqlconsole.data.utils.*;
import org.wandora.application.gui.UIBox;
//import com.gripstudios.applications.sqlconsole.beanshell.*;

/**
 *
 * @author  akivela
 */
public class SQLTablePanel extends JPanel implements MouseListener, ActionListener, Scrollable {
    
	private static final long serialVersionUID = 1L;
	
	private static Color PATTERN_BACKGROUND = new Color(250, 245, 245);
    private static Color PATTERN_FOREGROUND = new Color(10, 0, 0);
    
    private SQLTable guiTable;
    private PatternFilteredTableView dataTable;
    
    private boolean tableChanged;
    private JTableHeader header;
    private MouseEvent mouseEvent;
    //private Kirjava kirjava;
    private JPopupMenu headerPopup;
    
    private String componentid;
    
    private Delegate<?,JTableHeader> headerListener;
    
    private boolean headerVisible=true;
    
    private Object[] headerPopupStruct = new Object[] {
        "Muokkaa sarakkeen suodatinta...",
        "Poista sarakkeen suodatin",
        "Poista kaikki suodattimet",
        "---",
        "Valitse suodatusten leikkaus (and)",
        "Valitse suodatusten unioni (or)",
    };
    
    private Object[] popupStruct = new Object[] {
        "Muokkaa",
            new Object[] {
           "Muokkaa...",
            "---",
            "Kopioi",
            "Leikkaa",
            "Liit�",
            "---",
            "Valitse kaikki",
            "Valitse rivi(t)",
            "Valitse sarake(et)",
            "---",
            "Laske rivit...",
        },
        "Suodattimet",
            new Object[] {
                "Muokkaa sarakkeen suodatinta...",
                "Poista sarakkeen suodatin",
                "Poista kaikki suodattimet",
                "---",
                "Valitse suodatusten leikkaus",
                "Valitse suodatusten unioni",
            }
    };
    

       
    public SQLTablePanel(TableView table) {
        this(table,null);
    }
    public SQLTablePanel(TableView table, String componentid) {
        this.componentid = componentid;
        changeTable(table);
    }
    
    public Collection<Integer> getEditedRows(){
        Vector<Integer> converted=new Vector<Integer>();
        Collection<Integer> original=guiTable.getEditedRows();
        for(int e : original){
            converted.add(guiTable.convertRowIndexToView(e));
        }
        return converted;
    }
    public String[] getRowData(int r){
        return guiTable.getRow(guiTable.convertRowIndexToModel(r));
    }
    public Object[] getHiddenData(int r){
        return dataTable.getHiddenData(guiTable.convertRowIndexToModel(r));
    }
    
    public int convertRowIndexToModel(int r){
        return guiTable.convertRowIndexToModel(r);
    }
    // -------------------------------------------------------------------------
    
    public JTableHeader getTableHeader(){
        return header;
    }
    public void setHeaderVisible(boolean value){
        if(headerVisible==value) return;
        headerVisible=value;
        if(value){
            add(header, BorderLayout.PAGE_START);            
        }
        else{
            remove(header);            
        }
    }
    public void setHeaderListener(Delegate<?,JTableHeader> headerChanged){
        this.headerListener=headerChanged;
    }
        
    public void changeTable(TableView newTable) {
        if(! (newTable instanceof PatternFilteredTableView)) {
            dataTable = new PatternFilteredTableView(newTable);
        }
        else {
            dataTable = (PatternFilteredTableView) newTable;
        }
        initGui();
    }
    

    
    public void initGui() {
        TableColumnModel columnModel = null;
        if(guiTable != null) {
            columnModel = guiTable.getColumnModel();
            remove(guiTable);
            remove(header);
        }
        
        setLayout(new BorderLayout());
        String[] columnNames = dataTable.getColumnNames();
        guiTable = new SQLTable(dataTable.getView(), columnNames);
        guiTable.addMouseListener(this);
        //guiTable.addFocusListener(this);
        
        guiTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        for(int i=0;i<columnNames.length;i++){
            guiTable.setColumnEditable(i,dataTable.isColumnEditable(i));
        }
        
        header = guiTable.getTableHeader();
        headerPopup= UIBox.makePopupMenu(headerPopupStruct, this);
        header.addMouseListener(this);
        if(headerListener!=null) headerListener.invoke(header);
        if(headerVisible) add(header, BorderLayout.PAGE_START);
        add(guiTable, BorderLayout.CENTER);
//        setBorder(new javax.swing.border.EtchedBorder());

        JPopupMenu popup = UIBox.makePopupMenu(popupStruct, this);
        
        /*
        if(componentid!=null){
            HashMap<String,JMenu> subMenus=new HashMap();
            final KirjavaTablePanel thisf=this;
            BSHLibrary bshLibrary = kirjava.getBSHLibrary();
            if(bshLibrary != null) {
                popup.add(new JSeparator());
                Vector<BSHComponent> components = bshLibrary.getComponentsByName(componentid+".table");
                for(BSHComponent bshc : components) {
                    Object o=bshc.makeNew(kirjava);
                    Collection<TableContextMenuTool> tools=null;
                    if(o instanceof Collection) tools=(Collection<TableContextMenuTool>)o;
                    else if(o instanceof TableContextMenuTool) {tools=new Vector(); tools.add((TableContextMenuTool)o);}
                    for(TableContextMenuTool tool_ : tools){
                        final TableContextMenuTool tool=tool_;                        
                        String[] labels=tool.getLabel().split("/");
                        JMenu menu=null;
                        String path="";
                        for(int i=0;i<labels.length-1;i++){
                            path+=labels[i];
                            JMenu m=subMenus.get(path);
                            if(m==null){
                                m=new JMenu();
                                m.setFont(Kirjava.menuFont);
                                m.setText(labels[i]);
                                subMenus.put(path,m);
                                if(menu==null) popup.add(m);
                                else menu.add(m);
                            }
                            menu=m;
                        }
                        String label=labels[labels.length-1];

                        if(label.equals("---")){
                            if(menu==null) popup.add(new JSeparator());
                            else menu.add(new JSeparator());
                        }
                        else{
                            JMenuItem menuItem=new JMenuItem();
                            menuItem.setFont(kirjava.menuFont);
                            menuItem.setText(label);
                            menuItem.setActionCommand(label);
                            menuItem.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent actionEvent){
                                    tool.actionPerformed(kirjava,thisf);
                                }
                            });
                            if(menu==null) popup.add(menuItem);
                            else menu.add(menuItem);
                        }
                    }
                }
            }
        }
        */
        
        guiTable.setComponentPopupMenu(popup);
        tableChanged = false;
        
        
        for(int i=0; i<dataTable.getColumnCount(); i++) {
            if(dataTable.columnHasPattern(i)) {
                guiTable.setColumnBackground(i, PATTERN_BACKGROUND);
                guiTable.setColumnForeground(i, PATTERN_FOREGROUND);
            }
        }
        if(columnModel != null) {
            guiTable.setColumnModel(columnModel);
            for(Enumeration<TableColumn> e = columnModel.getColumns(); e.hasMoreElements(); ) {
                TableColumn column = (TableColumn) e.nextElement();
                column.setHeaderValue(columnNames[column.getModelIndex()]);
            }
        }
        //if(kirjava != null) kirjava.refresh();
    }
    
    

    
    
    // -------------------------------------------------------------------------
    
    
    
    
    
    
    public Vector<String> getHiddenColumnFromSelectedRows(int hiddenColumn) {
        if(getSelectedRow() != -1) {
            try {
                int[] r=getSelectedRows();
                Vector<String> colData=new Vector<>();
                for(int i=0;i<r.length;i++){
                    Object[] hidden=getHiddenData(r[i]);
                    colData.add(hidden[hiddenColumn].toString());
                }
                return colData;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    
    public int[] getSelectedRows(){
        return guiTable.getSelectedRows();
    }

    public int getSelectedRow(){
        return guiTable.getSelectedRow();
    }
    
    public int getCurrentColumn() {
        int col;
        if(guiTable.getSelectedColumn() != -1) {
            col = guiTable.getSelectedColumn();
        }
        else {
            col = guiTable.columnAtPoint(mouseEvent.getPoint());
        }
        col = guiTable.convertColumnIndexToModel(col);
        return col;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    

    
    public void setValueAt(Point p, String newValue) {
        setValueAt(p.x, p.y, newValue);
    }
    
    public void setValueAt(int x, int y, String newValue) {
        if( guiTable.isCellEditable(x,y) ){
            try { guiTable.getModel().setValueAt(newValue, x, y); }
            catch (Exception e) {}
        }
    }
 
    public String getValueAt(Point p) {
        return getValueAt(p.x, p.y);
    }
    
    public String getValueAt(int x, int y) {
        try { return (String) guiTable.getModel().getValueAt(x, guiTable.convertColumnIndexToModel(y)); }
        catch (Exception e) {}
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
       
    
    
    public void addRows() {
        int[] rows = guiTable.getSelectedRows();
        int number = rows.length > 0 ? rows.length : 1;
        int pos = rows.length > 0 ? rows[0] : guiTable.getTablePoint(mouseEvent).x;
        int p;
        if(number > 0) {
            dataTable.insertRows(pos, number);
            initGui();
        }
    }
    
    
    public void deleteRows() {
        int[] rows = guiTable.getSelectedRows();
        if(rows == null || rows.length == 0) rows = new int[] { guiTable.getTablePoint(mouseEvent).x };

        dataTable.deleteRows(rows);
        initGui();
    }
    
    
    
    
   // -------------------------------------------------------------------------
    
   
        
    public void cut() {
        copy(true);
    }
    
    
    public void copy() {
        copy(false);
    }
    
    
    public void copy(boolean doCut) {
        int[] cols = guiTable.getSelectedColumns();
        int[] rows = guiTable.getSelectedRows();
        String cellContent;
        if(cols.length > 0 && rows.length > 0) {
            StringBuffer sb = new StringBuffer();
            for(int j=0; j<rows.length; j++) {
                for(int i=0; i<cols.length; i++) {
                    cellContent = getValueAt(rows[j],cols[i]);
                    if(cellContent == null) cellContent = "";
                    sb.append(cellContent);
                    if(i < cols.length-1) sb.append("\t");
                    if(doCut) setValueAt(rows[j],cols[i], "");
                }
                if(j < rows.length-1) sb.append("\n");
            }
            ClipboardBox.setClipboard(sb.toString());
        }
        else {
            Point loc = guiTable.getTablePoint(mouseEvent);
            cellContent = getValueAt(loc);
            if(cellContent == null) cellContent = "";
            ClipboardBox.setClipboard(cellContent);
            if(doCut) setValueAt(loc, "");
        }
    }
    

    
    public void paste() {
        String s = ClipboardBox.getClipboard();
        int[] cols = guiTable.getSelectedColumns();
        int[] rows = guiTable.getSelectedRows();
        if(cols.length > 0 && rows.length > 0) {
            String[][] st = Textbox.makeStringTable(s);
            for(int j=0; j<rows.length && j < st.length; j++) {
                for(int i=0; i<cols.length && i < st[0].length; i++) {
                    try {
                        setValueAt(rows[j], cols[i], st[j][i]);
                    }
                    catch (Exception ex) {}
                }
            }
        }
        else {
            Point loc = guiTable.getTablePoint(mouseEvent);
            String clipboardContent = ClipboardBox.getClipboard();
            if(clipboardContent != null) { 
                setValueAt(loc, clipboardContent); 
            }
        }
    }
   
    
    
    public void selectColumn() {
        if(guiTable.getSelectedRow() != -1) {
            guiTable.setRowSelectionInterval(0, guiTable.getRowCount()-1);
        }
    }
    
    
    
    
    public String[][] copyToStringArray(boolean doCut) {
        int[] cols = guiTable.getSelectedColumns();
        int[] rows = guiTable.getSelectedRows();
        String[][] copied = new String[rows.length][cols.length];
        String cellContent;
        if(cols.length > 0 && rows.length > 0) {
            for(int j=0; j<rows.length; j++) {
                for(int i=0; i<cols.length; i++) {
                    cellContent = getValueAt(rows[j],cols[i]);
                    if(cellContent == null) cellContent = "";
                    copied[j][i] = cellContent;
                    if(doCut) setValueAt(rows[j],cols[i], "");
                }
            }
        }
        else {
            copied = new String[1][1];
            Point loc = guiTable.getTablePoint(mouseEvent);
            cellContent = getValueAt(loc);
            if(cellContent == null) cellContent = "";
            copied[0][0] = cellContent;
            if(doCut) setValueAt(loc, "");
        }
        return copied;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    public void editPattern() {
        int col = getCurrentColumn();
        PatternEditor pe = new PatternEditor();
        pe.show(this, col, getPatternForColumn(col), isAnd());
    }
    
    
   
    
    
    public void applyPattern(int col, SQLPattern pattern, boolean mode) {
        dataTable.setMode(mode);
        dataTable.setPattern(col, pattern);
        //Logger.println("new pattern set " + pattern);
        initGui();
    }
    
   
    
    
    public void resetPattern() {
        int col = getCurrentColumn();
        dataTable.setPattern(col, (SQLPattern) null);
    }
    
    
    public void resetPatterns() {
        dataTable.resetView();
    }
    
    
    public SQLPattern getPatternForColumn(int col) {
        return dataTable.getPattern(col);
    }
    
    
    public void andPatterns() {
        dataTable.setMode(PatternFilteredTableView.AND_MODE);
        dataTable.updateRowIndex();
        initGui();
        //Logger.println("and mode!");
    }
    public void orPatterns() {
        dataTable.setMode(PatternFilteredTableView.OR_MODE);
        dataTable.updateRowIndex();
        initGui();
        //Logger.println("or mode!");
    }
    
    public boolean isAnd() {
        if(dataTable.getMode()==PatternFilteredTableView.AND_MODE) return true;
        return false;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        try {
            String command = actionEvent.getActionCommand();
            Point loc = guiTable.getTablePoint(mouseEvent);

            if("Muokkaa...".equalsIgnoreCase(command)) {
                int[] cols = guiTable.getSelectedColumns();
                int[] rows = guiTable.getSelectedRows();
                if(cols.length > 0 && rows.length > 0) {
                    guiTable.editCellAt(cols[0], rows[0]);
                }
                else guiTable.editCellAt(loc.x, loc.y);
            }
            // ----- leikep�yt� ------
            else if("Kopioi".equalsIgnoreCase(command)) {
                copy();
            }
            else if("Leikkaa".equalsIgnoreCase(command)) {
                cut();
            }
            else if("Liit�".equalsIgnoreCase(command)) {
                paste();
            }
            else if("Laske rivit...".equalsIgnoreCase(command)) {
                int rowsCounter = guiTable.getRowCount();
                String message = "Taulussa on " + rowsCounter + " rivi�!";
                JOptionPane.showMessageDialog(this, message, "Taulussa rivej�", JOptionPane.INFORMATION_MESSAGE);
            }
            /*
            else if("Lis�� rivi...".equalsIgnoreCase(command)) {
                addRows();
            }
            else if("Poista rivi...".equalsIgnoreCase(command)) {
                deleteRows();
            }
            */
            
            // ------ valinnat ------
            else if("Valitse kaikki".equalsIgnoreCase(command)) {
                guiTable.selectAll();
            }
            else if("Valitse rivi(t)".equalsIgnoreCase(command)) {
                if(guiTable.getSelectedRow() != -1) 
                    guiTable.setColumnSelectionInterval(0, guiTable.getColumnCount()-1);
            }
            else if("Valitse sarake(et)".equalsIgnoreCase(command)) {
                selectColumn();
            }
            
            // ----- hahmot (regexp) ------
            else if("Muokkaa sarakkeen suodatinta...".equalsIgnoreCase(command)) {
                editPattern();
            }
            else if("Poista sarakkeen suodatin".equalsIgnoreCase(command)) {
                resetPattern();
                initGui();
            }
            else if("Poista kaikki suodattimet".equalsIgnoreCase(command)) {
                resetPatterns();
                initGui();
            }
            else if("Valitse suodatusten leikkaus".equalsIgnoreCase(command)) {
                andPatterns();
            }
            else if("Valitse suodatusten unioni".equalsIgnoreCase(command)) {
                orPatterns();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }    
    
    
    
    
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
        //Logger.println("mouse clicked at table panel!" + mouseEvent.getButton());
        Point hitCell = guiTable.getTablePoint(mouseEvent);
        if(hitCell != null) {
            if(hitCell.x == 0 && mouseEvent.getButton() == MouseEvent.BUTTON3) {
                headerPopup.show(mouseEvent.getComponent(),mouseEvent.getX(), mouseEvent.getY());
                mouseEvent.consume();
            }
        }
    }
    
    
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    
    
    
    
    // --- Kirjava component! --------------------------------------------------
    
    
    
    
    // -------------------------------------------------------------------------

    
    
   
   
   
    public Dimension getPreferredScrollableViewportSize(){
        return super.getPreferredSize();
    }
    public int getScrollableBlockIncrement(Rectangle visibleRect,int orientation, int direction){
        return getScrollableUnitIncrement(visibleRect,orientation,direction);
    }
    public boolean getScrollableTracksViewportHeight(){
        return false;
    }
    public boolean getScrollableTracksViewportWidth(){
        return true;
    }
    public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int derection){
        return 20;
    }
}
