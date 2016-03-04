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
 * LocatorTable.java
 *
 * Created on 23. lokakuuta 2007, 11:02
 *
 */

package org.wandora.application.gui.table;



import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.*;

import org.wandora.application.*;
import org.wandora.application.gui.Clipboardable;
import org.wandora.application.gui.DnDHelper;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import org.wandora.application.gui.simple.SimpleTable;
import org.wandora.utils.swing.anyselectiontable.TableSelectionModel;





/**
 *
 * @author akivela
 */
public class LocatorTable extends SimpleTable implements MouseListener, ActionListener, Clipboardable /*, DropTargetListener, DragGestureListener*/ {
    

    

    protected Wandora wandora = null;
    protected MouseEvent mouseEvent;
    
    
    
    /** Creates a new instance of LocatorTable */
    public LocatorTable(Wandora parent) {
        this.wandora = parent;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void setSort(int column) {
    }
    
    
    
    
    public void initialize(Locator[] tableLocators, String columnTitle, Color[] tableColors) {
        Locator[][] extendedTableLocators = new Locator[tableLocators.length][1];
        Color[][] extendedTableColors = new Color[tableColors.length][1];
        for(int i=0; i<tableLocators.length; i++) {
            extendedTableLocators[i][0] = tableLocators[i];
        }
        for(int i=0; i<tableColors.length; i++) {
            extendedTableColors[i][0] = tableColors[i];
        }
        initialize(extendedTableLocators, new String[] { columnTitle }, extendedTableColors );
    }
    
    
    
    public void initialize(Locator[][] tableLocators, String[] columnTitles, Color[][] tableColors) {
        try {
            if(tableLocators == null || columnTitles == null) return;

            LocatorTableModel model = new LocatorTableModel(tableLocators, columnTitles, tableColors);
            setModel(model);
            setRowSorter(new LocatorTableRowSorter(model));

            createDefaultTableSelectionModel();
            
            setDefaultRenderer(Locator.class, new LocatorTableCellRenderer(this));

            addMouseListener(this);
            JPopupMenu popup = UIBox.makePopupMenu(getPopupStruct(), wandora);
            setComponentPopupMenu(popup);

            JPopupMenu headerPopup = UIBox.makePopupMenu(getHeaderPopupStruct(), wandora);
            getTableHeader().setComponentPopupMenu(headerPopup);
            
            setDragEnabled(true);
            setTransferHandler(new LocatorTableTransferHandler());

            getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    mouseEvent = e;
                }
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    mouseEvent = e; // FOR POPUP
                }
                
            });
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
        
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public Object[] getHeaderPopupStruct() {
        return null;
    }
    
    public Object[] getPopupStruct() {
        return null;
    }
    
    
    
    @Override
    public String getToolTipText(MouseEvent e) {
        Locator l = getLocatorAt(getTablePoint(e));
        try {
            if(l == null) {
                return null;
            }
            else {
                return l.toExternalForm();
            }
        }
        catch(Exception ex) {
            ex.printStackTrace(); // TODO EXCEPTION
        }
        return null;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void selectLocators(Locator[] locators) {
        if(locators == null || locators.length == 0) return;
        for(Locator l : locators) {
            selectLocator(l);
        }
    }
    
    public void selectLocator(Locator locator) {
        int c = this.getColumnCount();
        int r = this.getRowCount();
        Locator tableLocator = null;
        for(int x=0; x<c; x++) {
            for(int y=0; y<r; y++) {
                tableLocator = this.getLocatorAt(y, x);
                try {
                    if(tableLocator != null) {
                        if(tableLocator.equals(locator)) {
                            selectCell(x, y);
                            return;
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------

    
    
    

    
    public void cut() {
        copy();
    }
    public void paste() {
    }
    
    public void copy() {
        String c = getCopyString();
        ClipboardBox.setClipboard( c );
    }
    
    public String getCopyString() {
        StringBuilder copyString = new StringBuilder("");
        try {
            Locator[] selectedLocators = getSelectedLocators();
            if(selectedLocators != null && selectedLocators.length > 0) {
                for(int i=0; i<selectedLocators.length; i++) {
                    copyString.append(selectedLocators[i].toExternalForm());
                    copyString.append("\n");
                }
            }
            else {
                Point loc = getTablePoint(mouseEvent);
                Locator l = getLocatorAt(loc);
                copyString.append(l.toExternalForm());
            }
        }
        catch(Exception ex){
            wandora.handleError(ex);
        }
        return copyString.toString();
    }
    
    
    
    //---------

    

    public Point getTablePoint() {
        return getTablePoint(mouseEvent);
    }
    public Point getTablePoint(java.awt.event.MouseEvent e) {
        if(e == null) return null;
        try {
            java.awt.Point p=e.getPoint();
            int y=rowAtPoint(p);
            int x=columnAtPoint(p);
            return new Point(x, y);
        }
        catch (Exception ex) {
            wandora.handleError(ex);
            return null;
        }
    }
    
    
    
    public ArrayList<int[]> getSelectedCells() {
        ArrayList<int[]> selected = new ArrayList();
        
        TableSelectionModel selection = getTableSelectionModel();
        int colCount = this.getColumnCount();
        int rowCount = this.getRowCount();
        //System.out.println("----");
        for(int c=0; c<colCount; c++) {
            int cc = convertColumnIndexToModel(c);
            ListSelectionModel columnSelectionModel = selection.getListSelectionModelAt(cc);
            if(columnSelectionModel != null && !columnSelectionModel.isSelectionEmpty()) {
                for(int r=0; r<rowCount; r++) {
                    if(columnSelectionModel.isSelectedIndex(r)) {
                        selected.add( new int[] { r, c } );
                        //System.out.println("found cell "+cc+","+r);
                    }
                }
            }
        }
        return selected;
    }
    
    
    
    
    public Locator[] getSelectedLocators() {
        ArrayList<Locator> locators = new ArrayList<Locator>();
        ArrayList<int[]> selectedCells = getSelectedCells();
        for(int[] cell : selectedCells) {
            locators.add( getLocatorAt(cell[0], cell[1]) );
        }
        if(locators.isEmpty()) {
            Point loc = getTablePoint();
            Locator t = getLocatorAt(loc);
            if(t != null) {
                locators.add( t );
            }
        }
        return locators.toArray( new Locator[] {} );
    }
    
    
    
    
    
    public int getCurrentRow() {
        Point point = getTablePoint();
        if(point != null) return convertRowIndexToModel(point.x);
        return -1;
    }
    public int[] getCurrentRows() {
        int[] rows = getSelectedRows();
        if(rows == null || rows.length == 0) {
            int r = getCurrentRow();
            if(r != -1) return new int[] { r };
            else return new int[] {};
        }
        else {
            for(int i=0; i<rows.length; i++) {
                rows[i] = convertRowIndexToModel(rows[i]);
            }
            return rows;
        }
    }


    
    
     public Object getValueAt(MouseEvent e) {
         if(e == null) return null;
        return getValueAt(getTablePoint(e));
    }
     public Object getValueAt(Point p) {
         if(p == null) return null;
        return getValueAt(p.y, p.x);
    }
    @Override
    public Object getValueAt(int y, int x) {
        try {
            if(x >= 0 && x < getModel().getColumnCount() && y >= 0 && y < getModel().getRowCount()) {
                int cx = convertColumnIndexToModel(x);
                int cy = convertRowIndexToModel(y);
                return getModel().getValueAt(cy, cx);
            }
        }
        catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    
    
    
    
    public Locator getLocatorAt(MouseEvent e) {
        return getLocatorAt(getTablePoint(e));
    }
    public Locator getLocatorAt(Point point) {
        if(point == null) return null;
        return getLocatorAt(point.x, point.y);
    }
    public Locator getLocatorAt(int x, int y) {
        Object object = getValueAt(x, y);
        if(object instanceof Locator) {
            return (Locator) object;
        }
        return null;
    }
    
    
    
    public Color getColorFor(int row, int col) {
        try {
            int cx = convertColumnIndexToModel(col);
            int cy = convertRowIndexToModel(row);
            return ((LocatorTableModel) getModel()).getColorAt(cy, cx);
        }
        catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    
    
    public void selectColumn() {
        if(getSelectedRow() != -1) {
            setRowSelectionInterval(0, getRowCount()-1);
        }
    }
    
    
    
    
    
    
    @Override
    public int convertRowIndexToModel(int row) {
        return getRowSorter().convertRowIndexToModel(row);
    }
    @Override
    public int convertRowIndexToView(int row) {
        return getRowSorter().convertRowIndexToView(row);
    }
    @Override
    public int convertColumnIndexToModel(int col) {
        return super.convertColumnIndexToModel(col);
    }
    @Override
    public int convertColumnIndexToView(int col) {
        return super.convertColumnIndexToView(col);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public void refreshGUI() {
        try {
            wandora.doRefresh();
        }
        catch(Exception e) { 
            wandora.handleError(e);
        }
    }
    
    // -------------------------------------------------------------------------
    
    
        
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
    
    
    
    
 
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------------- DRAG & DROP ---
    // -------------------------------------------------------------------------
    
    
  
    private class LocatorTableTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                   support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            String str = getCopyString();
            DnDHelper.GenericTransferable ret=new DnDHelper.GenericTransferable();
            ret.addData(DnDBox.uriListFlavor, str);
            ret.addData(DataFlavor.stringFlavor, str);
            return ret;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try {
                if(support.isDataFlavorSupported(DnDHelper.topicDataFlavor)) {
                    java.util.List<Topic> topics = (java.util.List<Topic>)support.getTransferable().getTransferData(DnDHelper.topicDataFlavor);
                    for( Topic topic : topics ) {
                        Collection<Locator> sis = topic.getSubjectIdentifiers();
                        for(Locator si : sis) {
                            processDrop(si.toExternalForm());
                        }
                    }
                }
                else if(support.isDataFlavorSupported(DnDBox.uriListFlavor)){
                    String data=(String)support.getTransferable().getTransferData(DnDBox.uriListFlavor);
                    String[] split=data.split("\n");
                    for(int i=0;i<split.length;i++){
                        processDrop(split[i].trim());
                    }
                }
                else if(support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                    java.util.List<File> files = (java.util.List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    int ret=WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Make DataURI out of given file content? Answering no uses filename as an URI.","Make DataURI?", WandoraOptionPane.YES_NO_OPTION);
                    if(ret==WandoraOptionPane.YES_OPTION) {
                        for( File file : files ) {
                            DataURL dataURL = new DataURL(file);
                            String dataUrlLocator = dataURL.toExternalForm(Base64.DONT_BREAK_LINES);
                            processDrop(dataUrlLocator);
                        }
                    }
                    else if(ret==WandoraOptionPane.NO_OPTION) {
                        for( File file : files ) {
                            processDrop(file.toURI().toString());
                        }
                    }
                }
                else if(support.isDataFlavorSupported(DataFlavor.stringFlavor)){
                    String data=(String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    processDrop(data);
                }
            }
            catch(TopicMapException tme){tme.printStackTrace();}
            catch(UnsupportedFlavorException ufe){ufe.printStackTrace();}
            catch(IOException ioe){ioe.printStackTrace();}
//            catch(CancelledException ce){}
            return false;
        }

    }
    
    
    
    
    public void processDrop(String data) {
        // Override this method in extending implementations! 
        // Look at the SITable class for an example.
    }


    
    @Override
    public void actionPerformed(ActionEvent e) {
        String c = e.getActionCommand();
    }
    

    

}
