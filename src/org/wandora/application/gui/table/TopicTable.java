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
 * TopicTable.java
 *
 * Created on 14. lokakuuta 2005, 10:59
 */

package org.wandora.application.gui.table;



import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.*;
import org.wandora.application.*;
import static org.wandora.application.WandoraMenuManager.getOpenInMenu;
import org.wandora.application.gui.Clipboardable;
import org.wandora.application.gui.DnDHelper;
import org.wandora.application.gui.TopicGuiWrapper;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleTable;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.*;
import org.wandora.application.tools.navigate.OpenTopic;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import org.wandora.utils.swing.anyselectiontable.TableSelectionModel;





/**
 *
 * @author akivela
 */
public class TopicTable extends SimpleTable implements MouseListener, ActionListener, Clipboardable {

    private Wandora wandora = null;
    private MouseEvent mouseEvent;

    
    
    
    public TopicTable(Wandora w) {
        this.wandora = w;
        this.setUpdateSelectionOnSort(true);
        this.setDragEnabled(true);
        this.setTransferHandler(new TopicTableTransferHandler());
    }
    

    
    
    // -------------------------------------------------------------------------

    public void initialize(Collection<Topic> tableTopics) {
        initialize(tableTopics.toArray( new Topic[] {} ), "");
    }
    
    
    
    public void initialize(Collection<Topic> tableTopics, Object columnTopic) {
        initialize(tableTopics.toArray( new Topic[] {} ), columnTopic);
    }
    
    
    
    public void initialize(Topic[] tableTopics, Object columnTopic) {
        Topic[][] extendedTableTopics = new Topic[tableTopics.length][1];
        for(int i=0; i<tableTopics.length; i++) {
            extendedTableTopics[i][0] = tableTopics[i];
        }
        initialize(extendedTableTopics, new Object[] { columnTopic } );
    }
    
    
    

    
    
    public void initialize(Topic[][] tableTopics, Object[] columnObjects) {
        try {
            if(tableTopics == null || columnObjects == null) return;

            setDefaultRenderer(Topic.class, new TopicTableCellRenderer(this)); 
            
            TopicTableModel model = new TopicTableModel(tableTopics, columnObjects);
            this.setModel(model);
            
            TopicTableRowSorter rowSorter = new TopicTableRowSorter(this, model);
            this.setRowSorter(rowSorter);
            rowSorter.setSortsOnUpdates(true);

            this.addMouseListener(this);

            this.getTableHeader().setPreferredSize(new Dimension(100, DEFAULT_ROW_HEIGHT));
            this.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    mouseClicked(e);
                }
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    mouseClicked(e);
                }
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    mouseEvent = e;
                    if(e.isPopupTrigger()) {
                        Point loc = getTablePoint(mouseEvent);
                        Object o = getColumnAt(loc.x);
                        if(o != null && o instanceof Topic) {
                            JPopupMenu rolePopup = UIBox.makePopupMenu(getHeaderPopupStruct(), wandora);
                            rolePopup.show(e.getComponent(),e.getX(),e.getY());
                        }
                        e.consume();
                    }
                }
            });
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }
    
    
    // -------------------------------------------------------------------------
    


    @Override
    public void sorterChanged(RowSorterEvent e) {
        ArrayList<int[]> modelCells = new ArrayList();
        
        if(e.getType().equals(RowSorterEvent.Type.SORTED)) {
            ArrayList<int[]> selectedCells = getSelectedCells();
            if(selectedCells != null && !selectedCells.isEmpty()) {
                modelCells = new ArrayList();
                for(int[] cell : selectedCells) {
                    int i = e.convertPreviousRowIndexToModel(cell[0]);
                    cell[0] = (i != -1 ? i : cell[0]);
                    modelCells.add(cell);
                }
            }
        }
        
        super.sorterChanged(e);

        if(e.getType().equals(RowSorterEvent.Type.SORTED)) {
            if(modelCells != null && !modelCells.isEmpty()) {
                clearSelection();
                ArrayList<int[]> viewCells = new ArrayList();
                for(int[] cell : modelCells) {
                    cell[0] = convertRowIndexToView(cell[0]);
                    cell[1] = convertColumnIndexToView(cell[1]);
                    viewCells.add(cell);
                }
                selectCells(viewCells);
            }
        }
    }

    
    
    // -------------------------------------------------------------------------
    
    
    protected Object[] getHeaderPopupStruct() {
        Object header = this.getTableHeader();
        return new Object[] {
            "Open topic", new OpenTopic(),
            "Open topic in",
            WandoraMenuManager.getOpenInMenu(),
            "---",
            "Duplicate topic", new DuplicateTopics(),
            "Split topic", new Object[] {
                "Split topic with subject identifiers", new SplitTopics(),
                "Split topic with base name...", new SplitTopicsWithBasename(),
            },
            "---",
            "Add to topic", WandoraMenuManager.getDefaultAddToTopicMenuStruct(wandora, header),
            "Delete from topic", WandoraMenuManager.getDefaultDeleteFromTopicMenuStruct(wandora, header),
            "---",
            "Copy", WandoraMenuManager.getDefaultCopyMenuStruct(wandora, header),
            "Copy also", WandoraMenuManager.getDefaultCopyAlsoMenuStruct(wandora, header),
            "Paste", WandoraMenuManager.getDefaultPasteMenuStruct(wandora, header),
            "Paste also", WandoraMenuManager.getDefaultPasteAlsoMenuStruct(wandora, header),
            "---",
            "Subject locators", WandoraMenuManager.getDefaultSLMenuStruct(wandora, header),
            "Subject identifiers", WandoraMenuManager.getDefaultSIMenuStruct(wandora, header),
            "Base names", WandoraMenuManager.getDefaultBasenameMenuStruct(wandora, header),
            "Variant names", WandoraMenuManager.getDefaultVariantNameMenuStruct(wandora, header),
            "Associations", WandoraMenuManager.getDefaultAssociationMenuStruct(wandora, header),
            "Occurrences", WandoraMenuManager.getDefaultOccurrenceMenuStruct(wandora, header),
        };
    }
    
    
    
    protected Object[] getPopupStruct() {
        return new Object[] {
            "Open topic", new OpenTopic(),
            "Open topic in",
            WandoraMenuManager.getOpenInMenu(),
            "---",
            "Select", WandoraMenuManager.getDefaultSelectMenuStruct(wandora, this),
            "Topics", WandoraMenuManager.getDefaultTopicMenuStruct(wandora, this),
        };
    }
    
    
    
    
    protected Object[] getPopupStruct(Object[] subStruct) {
        int i=0;
        Object[] popupStruct = new Object[] {
            "Open topic", new OpenTopic(),
            "Open topic in",
            WandoraMenuManager.getOpenInMenu(),
            "---",
            "Select", WandoraMenuManager.getDefaultSelectMenuStruct(wandora, this),
            "Topics", WandoraMenuManager.getDefaultTopicMenuStruct(wandora, this),
        };
        Object[] newPopupStruct = new Object[popupStruct.length + subStruct.length];
        for(; i<popupStruct.length; i++) {
            newPopupStruct[i] = popupStruct[i];
        }
        for(int subi=0; subi<subStruct.length; subi++) {
            newPopupStruct[i+subi] = subStruct[subi];
        }
        return newPopupStruct;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public void selectTopics(Topic[] topics) {
        if(topics == null || topics.length == 0) return;

        HashSet topicHash = new HashSet();
        for(int i=0; i<topics.length; i++) {
            topicHash.add(topics[i]);
        }

        int c = this.getColumnCount();
        int r = this.getRowCount();
        Topic tableTopic = null;
        for(int x=0; x<c; x++) {
            for(int y=0; y<r; y++) {
                tableTopic = this.getTopicAt(y, x);
                try {
                    if(tableTopic != null && !tableTopic.isRemoved()) {
                        if(topicHash.contains(tableTopic)) {
                            selectCell(x, y);
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    
    public void selectTopic(Topic topic) {
        int c = this.getColumnCount();
        int r = this.getRowCount();
        Topic tableTopic = null;
        for(int x=0; x<c; x++) {
            for(int y=0; y<r; y++) {
                tableTopic = this.getTopicAt(y, x);
                try {
                    if(tableTopic != null && !tableTopic.isRemoved()) {
                        if(tableTopic.mergesWithTopic(topic)) {
                            selectCell(x, y);
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
    
    
    @Override
    public String getToolTipText(MouseEvent e) {
        Point p = getTablePoint(e);
        Topic t = getTopicAt(p);
        String tooltipText = TopicToString.toString(t);
        return Textbox.makeHTMLParagraph(tooltipText, 60);
    }
    
   
    
    
    public void toggleSortOrder(int s) {
        RowSorter sorter = getRowSorter();
        if(sorter != null) {
            sorter.toggleSortOrder(s);
        }
    }
    
    
    
    
    
    
    
    @Override
    public void cut() {
        copy();
    }
    
    @Override
    public void paste() {
    }
    
    @Override
    public void copy() {
        ClipboardBox.setClipboard(getCopyString());
    }
    public String getCopyString() {
        StringBuilder copyString = new StringBuilder("");
        try {
            Object[][] selectedValues = getSelectedValues();
            if(selectedValues.length > 0) {
                for(int col=0; col<selectedValues.length; col++) {
                    boolean rowHasContent = false;
                    StringBuilder rawString = new StringBuilder("");
                    for(int row=0; row<selectedValues[col].length; row++) {
                        if(selectedValues[col][row] != null) {
                            rawString.append(TopicToString.toString((Topic) selectedValues[col][row]));
                            rowHasContent = true;
                        }
                        if(rowHasContent && row+1<selectedValues[col].length) {
                            rawString.append("\t");
                        }
                    }
                    if(rowHasContent) {
                        copyString.append(rawString);
                        copyString.append("\n");
                    };
                }
            }
            else {
                Point loc = getTablePoint(mouseEvent);
                Topic t = getTopicAt(loc);
                copyString.append(TopicToString.toString(t));
            }
        } 
        catch(Exception tme){
            wandora.handleError(tme);
        }
        return copyString.toString();
    }
    
    
    
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
    
    
    
    public Object[][] getSelectedValues() {
        ArrayList<int[]> selectedCells = getSelectedCells();
        int rlen = 0;
        int clen = 0;
        for(int[] cell : selectedCells) {
            if(clen < cell[0]) clen = cell[0];
            if(rlen < cell[1]) rlen = cell[1];
        }
        Object[][] selectedValues = new Object[clen+1][rlen+1];
        for(int[] cell : selectedCells) {
            selectedValues[cell[0]][cell[1]] = getValueAt( cell[0], cell[1] );
        }
        return selectedValues;
    }
    
    
    
    
    public Topic[] getSelectedTopics() {
        ArrayList<Topic> topics = new ArrayList<Topic>();
        ArrayList<int[]> selectedCells = getSelectedCells();
        for(int[] cell : selectedCells) {
            Topic t = getTopicAt(cell[0], cell[1]);
            try {
                if( t != null && !t.isRemoved() ) {
                    topics.add( t );
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(topics.isEmpty()) {
            Point loc = getTablePoint();
            Topic t = getTopicAt(loc);
            try {
                if(t != null && !t.isRemoved() ) {
                    topics.add( t );
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return topics.toArray( new Topic[] {} );
    }
    
    
    
    
    public Topic getSelectedHeaderTopic() {
        if(mouseEvent != null) {
            int c = this.getTableHeader().columnAtPoint(mouseEvent.getPoint());
            int realc = convertColumnIndexToModel(c);
            Object o = ((TopicTableModel) getModel()).getColumnObjectAt(realc);
            if(o instanceof Topic) return (Topic) o;
        }
        return null;
    }
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
     public Object getValueAt(MouseEvent e) {
        return getValueAt(getTablePoint(e));
    }
     public Object getValueAt(Point p) {
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
    
    public Object getColumnAt(int x) {
        return ((TopicTableModel) getModel()).getColumnObjectAt(x);
    }
    
    
    public Topic getTopicAt(MouseEvent e) {
        if(e == null) return null;
        return getTopicAt(getTablePoint(e));
    }
    public Topic getTopicAt(Point point) {
        if(point == null) return null;
        return getTopicAt(point.y, point.x);
    }
    public Topic getTopicAt(int y, int x) {
        Object object = getValueAt(y, x);
        if(object instanceof Topic) {
            return (Topic) object;
        }
        if(object instanceof TopicGuiWrapper) {
            TopicGuiWrapper wrapper = (TopicGuiWrapper) object;
            if(wrapper != null) return wrapper.topic;
        }
        return null;
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
        catch(Exception ce) { 
            //ce.printStackTrace();
        }
    }
    
    // -------------------------------------------------------------------------
    
    
        
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        this.mouseEvent = e;
        if(e.getClickCount()>=2){
            wandora.applyChangesAndOpen(getTopicAt(e));
        }
        if(e.isPopupTrigger()) {
            JPopupMenu popup = UIBox.makePopupMenu(getPopupStruct(), wandora);
            popup.show(e.getComponent(), e.getX(), e.getY());
            e.consume();
        }
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
    }
    
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        this.mouseEvent = e;
        mouseClicked(mouseEvent);
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        this.mouseEvent = e;
        mouseClicked(mouseEvent);
    }
    
    
    
    
    
    
    
    
    

    
    @Override
    public void actionPerformed(ActionEvent e) {
    }
    

    
    protected Topic getTopicForIdentifier(String id) {
        TopicMap tm = wandora.getTopicMap();
        Topic t = null;
        try {
            t = tm.getTopicWithBaseName(id);
            if(t == null) {
                t = tm.getTopic(id);
                if(t == null) {
                    t = tm.getTopicBySubjectLocator(new Locator(id));
                }
            }
        }
        catch(Exception e) {
            
        }
        return t;
    }
    

    

    

    // -------------------------------------------------------------------------
    // ----------------------------------------------------------------- DND ---
    // -------------------------------------------------------------------------

    
    private class TopicTableTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            return false;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return DnDHelper.makeTopicTableTransferable(TopicTable.this);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            return false;
        }

    }
    
}
