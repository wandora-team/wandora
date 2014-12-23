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
 * TopicGrid.java
 *
 * Created on 14. lokakuuta 2005, 10:59
 */

package org.wandora.application.gui.table;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.MOVE;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraMenuManager;
import org.wandora.application.gui.Clipboardable;
import org.wandora.application.gui.DnDHelper;
import org.wandora.application.gui.TopicGuiWrapper;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.gui.simple.SimpleTable;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.importers.SimpleRDFImport;
import org.wandora.application.tools.selections.SelectionInfo;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.IObox;
import org.wandora.utils.Textbox;
import org.wandora.utils.Tuples.T2;
import org.wandora.utils.swing.anyselectiontable.TableSelectionModel;

/**
 *
 * @author akivela
 */


public class TopicGrid extends SimpleTable implements Clipboardable, MouseListener, ActionListener {
    public static final int LEFT = 201;
    public static final int UP = 202;
    public static final int RIGHT = 203;
    public static final int DOWN = 204;
    
    
    
    private Wandora wandora;
    private MouseEvent mouseEvent;
    private int gridWidth = 0;
    private int gridHeight = 0;
    HashMap<T2<Integer,Integer>, Topic> gridData;

    
    
    public TopicGrid(Wandora w) {
        wandora = w;
        gridData = new HashMap<T2<Integer,Integer>, Topic>();
        
        this.setDragEnabled(true);
        this.setTransferHandler(new TopicGridTransferHandler());
    }
    

    public void initialize(int width, int height) {
        try {
            this.gridWidth = width;
            this.gridHeight = height;
            
            setDefaultRenderer(Topic.class, new TopicGridCellRenderer(this)); 
            
            TopicGridModel model = new TopicGridModel(this);
            this.setModel(model);

            this.addMouseListener(this);
            //gridPopup = UIBox.makePopupMenu(getPopupStruct(), this);
            //this.setComponentPopupMenu(popup);

            //final JPopupMenu rolePopup = UIBox.makePopupMenu(getRolePopupStruct(), wandora);
            //this.getTableHeader().setComponentPopupMenu(rolePopup);
            
            this.getTableHeader().setReorderingAllowed(false);
            
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
                    /*
                    int columnNumber = convertColumnIndexToModel(columnAtPoint(e.getPoint()));
                    if(!e.isPopupTrigger()) {
                        if(e.isShiftDown()) {
                            deselectColumn(columnNumber);
                        }
                        else {
                            selectColumn(columnNumber);
                        }
                    }
                    */
                    
                    /*if(e.isPopupTrigger()) {
                        Object o=getColumnAt(e.getX());
                        if(o instanceof Topic){
                            rolePopup.show(e.getComponent(),e.getX(),e.getY());
                        }
                        e.consume();
                    }*/
                }
            });
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }
    
    
    private Object[] getPopupStruct() {
        return new Object[] {
            "Open in",
            WandoraMenuManager.getOpenInMenu(),
            "---",
            "Cut",
            "Copy",
            "Paste",
            "---",
            "Select", WandoraMenuManager.getDefaultSelectMenuStruct(wandora, this),
            "Selection info", new SelectionInfo(),
            "---",
            "Insert rows",
            "Insert columns",
            "Delete rows",
            "Delete columns",
            "---",
            "Clear selected",
            "Clear all",
            "---",
            "Rotate CV",
            "Rotate CCV",
            "Flip horizontal",
            "Flip vertical",
            "---",
            "Sort rows",
            "Sort columns",
            "---",
            "Expand",
            new Object[] {
                "Expand instances",
                "Expand classes",
                "Expand superclasses",
                "Expand subclasses",
                "Expand association types",
                "Expand associated topics",
                "Expand association roles",
            },

            //"---",
            //"Add to topic", WandoraMenuManager.getDefaultAddToTopicMenuStruct(wandora, this),
            //"Delete from topic", WandoraMenuManager.getDefaultDeleteFromTopicMenuStruct(wandora, this),
            "---",
            "Make associations",
            new Object[] {
                "Make class-instance chain",
                "Make class-instances using tree layout",
                "---",
                "Make associations using Wandora layout",
                "Make associations using LTM layout",
                "Make associations using RDF triplet layout",
                "Make associations using player layout",
            },
            "Topics", WandoraMenuManager.getDefaultTopicMenuStruct(wandora, this),
            "---",
            "Import grid...",
            "Merge grid...",
            "Export grid...",
        };
    }
    
    
    
    
    public int getGridRowCount() {
        return gridHeight;
    }
    
    public int getGridColumnCount() {
        return gridWidth;
    }
    
    public void setCurrentTopic(Topic t) {
        ArrayList<int[]> cells = this.getSelectedCells();
        for(int[] c : cells) {
            gridData.put(new T2(c[0], c[1]), t);
        }
    }
    
    public void setTopicAt(Topic t, int column, int row) {
        gridData.put(new T2(row, column), t);
    }
    
    
    
    public Topic[] getCurrentTopics() {
        ArrayList<int[]> cells = this.getSelectedCells();
        ArrayList<Topic> ts = new ArrayList();
        Topic t;
        for(int[] c : cells) {
            t = gridData.get(new T2(c[0], c[1]));
            if(t != null) {
                ts.add(t);
            }
        }
        return ts.toArray(new Topic[] {});
    }
    
    
    

    public void selectTopics(Topic[] topics) {
        if(topics == null || topics.length == 0) return;
        for(Topic t : topics) {
            selectTopic(t);
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
    
    
    
    private void moveSelection(int[] newOrigo) {
        if(newOrigo != null) {
            ArrayList<int[]> selectedCells = getSelectedCells();
            if(selectedCells != null && !selectedCells.isEmpty()) {
                int[] oldOrigo = getOrigo(selectedCells);
                clearSelection();
                for(int[] c : selectedCells) {
                    int n0 = c[0] - oldOrigo[0] + newOrigo[0];
                    int n1 = c[1] - oldOrigo[1] + newOrigo[1];
                    selectCell(n1, n0);
                }
            }
        }
    }

    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public String getToolTipText(MouseEvent e) {
        if(e != null) {
            Point p = getTablePoint(e);
            Topic t = getTopicAt(p);
            if(t != null) {
                String tooltipText = TopicToString.toString(t);
                return Textbox.makeHTMLParagraph(tooltipText, 60);
            }
        }
        return null;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    

    @Override
    public void cut() {
        copy();
        clearSelectedCells();
    }
    
    
    
    private boolean autoCreateTopicsInPaste = false;
    
    
    @Override
    public void paste() {
        String tabText = ClipboardBox.getClipboard();
        StringTokenizer tabLines = new StringTokenizer(tabText, "\n");
        int ocol = this.getSelectedColumn();
        int orow = this.getSelectedRow();
        int row = orow;
        autoCreateTopicsInPaste = false;
        while(tabLines.hasMoreTokens()) {
            String tabLine = tabLines.nextToken();
            StringTokenizer topicIdentifiers = new StringTokenizer(tabLine, "\t");
            int col = ocol;
            try {
                String topicIdentifier = null;
                while(topicIdentifiers.hasMoreTokens()) {
                    topicIdentifier = topicIdentifiers.nextToken();
                    if(topicIdentifier != null && topicIdentifier.length() > 0) {
                        Topic topic = getTopicForIdentifier(topicIdentifier);
                        if(topic == null) {
                            boolean createTopicInPaste = false;
                            if(!autoCreateTopicsInPaste) {
                                int a = WandoraOptionPane.showConfirmDialog(wandora, "Can't find a topic for identifier '"+topicIdentifier+"'. Would you like to create a topic for '"+topicIdentifier+"'?", "Create new topic?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                                if(a == WandoraOptionPane.YES_OPTION) {
                                    createTopicInPaste = true;
                                }
                                else if(a == WandoraOptionPane.YES_TO_ALL_OPTION) {
                                    autoCreateTopicsInPaste = true;
                                }
                                else if(a == WandoraOptionPane.CANCEL_OPTION) {
                                    return;
                                }
                            }
                            if(autoCreateTopicsInPaste || createTopicInPaste) {
                                TopicMap tm = wandora.getTopicMap();
                                boolean identifierIsURL = false;
                                try {
                                    URL u = new URL(topicIdentifier);
                                    identifierIsURL = true;
                                }
                                catch(Exception e) {}
                                topic = tm.createTopic();
                                if(identifierIsURL) {
                                    topic.addSubjectIdentifier(new Locator(topicIdentifier));
                                }
                                else {
                                    topic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                                    topic.setBaseName(topicIdentifier);
                                }
                            }
                        }
                        if(topic != null) {
                            _setTopicAt(topic, row, col);
                        }
                    }
                    col++;
                }
            }
            catch(Exception e) {
                
            }
            row++;
        }
    }
    
    
    @Override
    public void copy() {
        ClipboardBox.setClipboard(getCopyString());
    }
    
    
    public void clearSelectedCells() {
        ArrayList<int[]> cells = getSelectedCells();
        for(int[] c : cells) {
            _setTopicAt(null, c[0], c[1]);
        }
    }
    
    
    public void clearAllCells() {
        gridData.clear();
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
                        if(row+1<selectedValues[col].length) {
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
    
    
    
    
    
    public void insertRows() {
        ArrayList<int[]> cells = getSelectedCells();
        HashMap<Integer, ArrayList<Integer>> rowsByColumn = new LinkedHashMap();
        for(int[] c : cells) {
            Integer column = new Integer(c[1]);
            Integer row = new Integer(c[0]);
            ArrayList<Integer> columnRows = rowsByColumn.get(column);
            if(columnRows == null) {
                columnRows = new ArrayList<Integer>();
            }
            columnRows.add(row);
            rowsByColumn.put(column, columnRows);
        }

        for(Integer column : rowsByColumn.keySet()) {
            ArrayList<Integer> rows = rowsByColumn.get(column);
            Collections.sort(rows);
            for(Integer row : rows) {
                HashMap<T2<Integer,Integer>, Topic> subData = new LinkedHashMap();   
                for(T2<Integer,Integer> coords : gridData.keySet()) {
                    if(column.equals(coords.e2)) {
                        if(row <= coords.e1) {
                            subData.put(coords, gridData.get(coords));
                        }
                    }
                }
                for(T2<Integer,Integer> coords : subData.keySet()) {
                    gridData.remove(coords);
                }
                for(T2<Integer,Integer> coords : subData.keySet()) {
                    gridData.put(new T2(coords.e1+1, coords.e2), subData.get(coords));
                }
            }
        }
        
        int numberOfNewRows = 0;
        for(Integer column : rowsByColumn.keySet()) {
            ArrayList<Integer> rows = rowsByColumn.get(column);
            if(rows.size() > numberOfNewRows) {
                numberOfNewRows = rows.size();
            }
        }
        gridHeight += numberOfNewRows;
    }
    
    

    
    
    
    public void deleteRows() {
        ArrayList<int[]> cells = getSelectedCells();
        HashMap<Integer, ArrayList<Integer>> rowsByColumn = new LinkedHashMap();
        for(int[] c : cells) {
            Integer column = new Integer(c[1]);
            Integer row = new Integer(c[0]);
            ArrayList<Integer> columnRows = rowsByColumn.get(column);
            if(columnRows == null) {
                columnRows = new ArrayList<Integer>();
            }
            columnRows.add(row);
            rowsByColumn.put(column, columnRows);
        }

        for(Integer column : rowsByColumn.keySet()) {
            ArrayList<Integer> rows = rowsByColumn.get(column);
            Collections.sort(rows);
            Collections.reverse(rows);
            for(Integer row : rows) {
                HashMap<T2<Integer,Integer>, Topic> subData = new LinkedHashMap();   
                for(T2<Integer,Integer> coords : gridData.keySet()) {
                    if(column.equals(coords.e2)) {
                        if(row < coords.e1) {
                            subData.put(coords, gridData.get(coords));
                        }
                    }
                }
                if(subData.isEmpty()) {
                    gridData.remove(new T2(row, column));
                }
                else {
                    for(T2<Integer,Integer> coords : subData.keySet()) {
                        gridData.remove(coords);
                    }
                    for(T2<Integer,Integer> coords : subData.keySet()) {
                        gridData.put(new T2(coords.e1-1, coords.e2), subData.get(coords));
                    }
                }
            }
        }
    }
    
    
    
    public void insertColumns() {
        ArrayList<int[]> cells = getSelectedCells();
        HashMap<Integer, ArrayList<Integer>> columnsByRow = new LinkedHashMap();
        for(int[] c : cells) {
            Integer column = new Integer(c[1]);
            Integer row = new Integer(c[0]);
            ArrayList<Integer> rowColumns = columnsByRow.get(row);
            if(rowColumns == null) {
                rowColumns = new ArrayList<Integer>();
            }
            rowColumns.add(column);
            columnsByRow.put(row, rowColumns);
        }

        for(Integer row : columnsByRow.keySet()) {
            ArrayList<Integer> columns = columnsByRow.get(row);
            Collections.sort(columns);
            for(Integer column : columns) {
                HashMap<T2<Integer,Integer>, Topic> subData = new LinkedHashMap();   
                for(T2<Integer,Integer> coords : gridData.keySet()) {
                    if(row.equals(coords.e1)) {
                        if(column <= coords.e2) {
                            subData.put(coords, gridData.get(coords));
                        }
                    }
                }
                for(T2<Integer,Integer> coords : subData.keySet()) {
                    gridData.remove(coords);
                }
                for(T2<Integer,Integer> coords : subData.keySet()) {
                    gridData.put(new T2(coords.e1, coords.e2+1), subData.get(coords));
                }
            }
        }
        
        int numberOfNewColumns = 0;
        for(Integer row : columnsByRow.keySet()) {
            ArrayList<Integer> columns = columnsByRow.get(row);
            if(columns.size() > numberOfNewColumns) {
                numberOfNewColumns = columns.size();
            }
        }
        gridWidth += numberOfNewColumns;
    }
    
    
    
    public void deleteColumns() {
        ArrayList<int[]> cells = getSelectedCells();
        HashMap<Integer, ArrayList<Integer>> columnsByRow = new LinkedHashMap();
        for(int[] c : cells) {
            Integer column = new Integer(c[1]);
            Integer row = new Integer(c[0]);
            ArrayList<Integer> rowColumns = columnsByRow.get(row);
            if(rowColumns == null) {
                rowColumns = new ArrayList<Integer>();
            }
            rowColumns.add(column);
            columnsByRow.put(row, rowColumns);
        }
        for(Integer row : columnsByRow.keySet()) {
            ArrayList<Integer> columns = columnsByRow.get(row);
            Collections.sort(columns);
            Collections.reverse(columns);
            for(Integer column : columns) {
                HashMap<T2<Integer,Integer>, Topic> subData = new LinkedHashMap();   
                for(T2<Integer,Integer> coords : gridData.keySet()) {
                    if(row.equals(coords.e1)) {
                        if(column < coords.e2) {
                            subData.put(coords, gridData.get(coords));
                        }
                    }
                }
                if(subData.isEmpty()) {
                    gridData.remove(new T2(row, column));
                }
                else {
                    for(T2<Integer,Integer> coords : subData.keySet()) {
                        gridData.remove(coords);
                    }
                    for(T2<Integer,Integer> coords : subData.keySet()) {
                        gridData.put(new T2(coords.e1, coords.e2-1), subData.get(coords));
                    }
                }
            }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    

    
    public Point getTablePoint() {
        return getTablePoint(mouseEvent);
    }
    public Point getTablePoint(java.awt.event.MouseEvent e) {
        if(e == null) return null;
        else return getTablePoint(e.getPoint());
    }
    public Point getTablePoint(Point screenPoint) {
        try {
            int y=rowAtPoint(screenPoint);
            int x=columnAtPoint(screenPoint);
            return new Point(x, y);
        }
        catch (Exception ex) {
            wandora.handleError(ex);
            return null;
        }
    }
    
    
    public int[] getSelectionOrigo() {
        return getOrigo(getSelectedCells());
    }

    
    
    private int[] getOrigo(ArrayList<int[]> cells) {
        int[] o = null;
        if(cells != null && !cells.isEmpty()) {
            for(int[] c : cells) {
                if(o == null) o = c;
                else {
                    try {
                        if(c[0] < o[0]) o = c;
                        else if(c[1] < o[1]) o = c;
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return o;
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
    
    
    
    public Topic[][] getSelectedTopicsNormalized() {
        ArrayList<int[]> selectedCells = getSelectedCells();
        int ymax = 0;
        int xmax = 0;
        int ymin = Integer.MAX_VALUE;
        int xmin = Integer.MAX_VALUE;
        for(int[] cell : selectedCells) {
            if(xmax < cell[0]) xmax = cell[0];
            if(ymax < cell[1]) ymax = cell[1];
            if(xmin > cell[0]) xmin = cell[0];
            if(ymin > cell[1]) ymin = cell[1];
        }
        Topic[][] selectedValues = new Topic[xmax-xmin+1][ymax-ymin+1];
        for(int[] cell : selectedCells) {
            selectedValues[cell[0]-xmin][cell[1]-ymin] = getTopicAt( cell[0], cell[1] );
        }
        return selectedValues;
    }
    
    
    
    
    public Topic[] getSelectedTopics() {
        ArrayList<Topic> topics = new ArrayList<Topic>();
        ArrayList<int[]> selectedCells = getSelectedCells();
        for(int[] cell : selectedCells) {
            topics.add( getTopicAt(cell[0], cell[1]) );
        }
        if(topics.isEmpty()) {
            Point loc = getTablePoint();
            Topic t = getTopicAt(loc);
            if(t != null) {
                topics.add( t );
            }
        }
        return topics.toArray( new Topic[] {} );
    }
    
    
    
    

    
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
        return ((TopicGridModel) getModel()).getColumnObjectAt(x);
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
    
    

    
    
    public Topic _getTopicAt(int col, int row) {
        return gridData.get(new T2(col, row));
    }
    
    public void _setTopicAt(Topic t, int col, int row) {
        if(t != null) {
            gridData.put(new T2(col, row), t);
        }
        else {
            gridData.remove(new T2(col, row));
        }
    }
    
    
    
      
    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        this.mouseEvent = e;
        if(e != null) {
            if(e.isPopupTrigger()) {
                JPopupMenu pm = UIBox.makePopupMenu(getPopupStruct(), this);
                pm.show(e.getComponent(), e.getX(), e.getY());
                e.consume();
            }
        }
    }
    
    
    
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {

    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        mouseClicked(e);
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
        mouseClicked(e);
    }
    
    
    
    
    
    
    
    
    

    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e == null) return;
        boolean shouldRepaint = false;
        boolean shouldUpdateEverything = false;
        String c = e.getActionCommand();
        if(c != null) {
            c = c.toLowerCase();
            if("cut".equals(c)) {
                cut();
                shouldRepaint = true;
            }
            else if("copy".equals(c)) {
                copy();
            }
            else if("paste".equals(c)) {
                paste();
                shouldRepaint = true;
            }
            else if("clear selected".equals(c)) {
                clearSelectedCells();
                shouldRepaint = true;
            }
            else if("clear all".equals(c)) {
                clearAllCells();
                shouldRepaint = true;
            }
            else if("insert rows".equals(c)) {
                insertRows();
                shouldRepaint = true;
            }
            else if("delete rows".equals(c)) {
                deleteRows();
                shouldRepaint = true;
            }
            else if("insert columns".equals(c)) {
                insertColumns();
                shouldRepaint = true;
            }
            else if("delete columns".equals(c)) {
                deleteColumns();
                shouldRepaint = true;
            }
            
            
            // ***** EXPAND *****
            
            else if("expand instances".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 && (e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandInstances(LEFT);
                }
                else if((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandInstances(RIGHT);
                }
                else if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    expandInstances(UP);
                }
                else {
                    expandInstances(DOWN);
                }
                shouldRepaint = true;
            }
            else if("expand classes".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 && (e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandClasses(LEFT);
                }
                else if((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandClasses(RIGHT);
                }
                else if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    expandClasses(UP);
                }
                else {
                    expandClasses(DOWN);
                }
                shouldRepaint = true;
            }
            else if("expand superclasses".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 && (e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandSuperclasses(LEFT);
                }
                else if((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandSuperclasses(RIGHT);
                }
                else if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    expandSuperclasses(UP);
                }
                else {
                    expandSuperclasses(DOWN);
                }
                shouldRepaint = true;
            }
            else if("expand subclasses".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 && (e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandSubclasses(LEFT);
                }
                else if((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandSubclasses(RIGHT);
                }
                else if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    expandSubclasses(UP);
                }
                else {
                    expandSubclasses(DOWN);
                }
                shouldRepaint = true;
            }
            else if("expand association types".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 && (e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandAssociationTypes(LEFT);
                }
                else if((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandAssociationTypes(RIGHT);
                }
                else if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    expandAssociationTypes(UP);
                }
                else {
                    expandAssociationTypes(DOWN);
                }
                shouldRepaint = true;
            }
            else if("expand associated topics".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 && (e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandAssociatedTopics(LEFT);
                }
                else if((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandAssociatedTopics(RIGHT);
                }
                else if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    expandAssociatedTopics(UP);
                }
                else {
                    expandAssociatedTopics(DOWN);
                }
                shouldRepaint = true;
            }
            else if("expand association roles".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 && (e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandAssociationRoles(LEFT);
                }
                else if((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                    expandAssociationRoles(RIGHT);
                }
                else if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    expandAssociationRoles(UP);
                }
                else {
                    expandAssociationRoles(DOWN);
                }
                shouldRepaint = true;
            }
            
            // ***** MAKE ASSOCIATIONS *****
            
            
            else if("make class-instance chain".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    makeClassInstanceChains(rotateCV(getSelectedTopicsNormalized()));
                }
                else {
                    makeClassInstanceChains(getSelectedTopicsNormalized());
                }
                shouldUpdateEverything = true;
            }
            
            else if("make class-instances using tree layout".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    makeClassInstancesUsingTreeLayout(rotateCV(getSelectedTopicsNormalized()));
                }
                else {
                    makeClassInstancesUsingTreeLayout(getSelectedTopicsNormalized());
                }
                shouldUpdateEverything = true;
            }

            else if("make associations using wandora layout".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    makeAssociationsUsingWandoraLayout(flipVertical(rotateCV(getSelectedTopicsNormalized())));
                }
                else {
                    makeAssociationsUsingWandoraLayout(getSelectedTopicsNormalized());
                }
                shouldUpdateEverything = true;
            }
            else if("make associations using ltm layout".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    makeAssociationsUsingLTMLayout(rotateCV(getSelectedTopicsNormalized()));
                }
                else {
                    makeAssociationsUsingLTMLayout(getSelectedTopicsNormalized());
                }
                shouldUpdateEverything = true;
            }
            else if("make associations using rdf triplet layout".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    makeAssociationsUsingRDFLayout(rotateCV(getSelectedTopicsNormalized()));
                }
                else {
                    makeAssociationsUsingRDFLayout(getSelectedTopicsNormalized());
                }
                shouldUpdateEverything = true;
            }
            else if("make associations using player layout".equals(c)) {
                if((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    makeAssociationsUsingPlayerLayout(rotateCV(getSelectedTopicsNormalized()));
                }
                else {
                    makeAssociationsUsingPlayerLayout(getSelectedTopicsNormalized());
                }
                shouldUpdateEverything = true;
            }

            // ****** ROTATE AND FLIP TOPICS *****
            
            else if("flip vertical".equals(c)) {
                Topic[][] selection = getSelectedTopicsNormalized();
                int[] origo = getSelectionOrigo();
                clearSelectedCells();
                pasteAt(origo, flipVertical(selection));
                shouldRepaint = true;
            }
            else if("flip horizontal".equals(c)) {
                Topic[][] selection = getSelectedTopicsNormalized();
                int[] origo = getSelectionOrigo();
                clearSelectedCells();
                pasteAt(origo, flipHorizontal(selection));
                shouldRepaint = true;
            }
            else if("rotate cv".equals(c)) {
                Topic[][] selection = getSelectedTopicsNormalized();
                int[] origo = getSelectionOrigo();
                clearSelectedCells();
                pasteAt(origo, rotateCV(selection));
                shouldRepaint = true;
            }
            else if("rotate ccv".equals(c)) {
                Topic[][] selection = getSelectedTopicsNormalized();
                int[] origo = getSelectionOrigo();
                clearSelectedCells();
                pasteAt(origo, rotateCCV(selection));
                shouldRepaint = true;
            }
            else if("sort rows".equals(c)) {
                Topic[][] selection = getSelectedTopicsNormalized();
                int[] origo = getSelectionOrigo();
                pasteAt(origo, sortRows(selection));
                shouldRepaint = true;
            }
            
            
            // ****** EXPORT AND IMPORT *****
            
            else if("export grid...".equals(c)) {
                save();
                shouldRepaint = true;
            }
            else if("import grid...".equals(c)) {
                load();
                shouldRepaint = true;
            }
            else if("merge grid...".equals(c)) {
                merge();
                shouldRepaint = true;
            }
        }
        wandora.addUndoMarker();
        if(shouldUpdateEverything) {
            wandora.doRefresh();
        }
        else if(shouldRepaint) {
            repaint();
        }
    }
    
    
    
    public void expandInstances(int direction) {
        TopicMap tm = wandora.getTopicMap();
        ArrayList<int[]> selectedCells = getSelectedCells();
        if(selectedCells != null && !selectedCells.isEmpty()) {
            for(int[] cell : selectedCells) {
                try {
                    Topic t = getTopicAt(cell[0], cell[1]);
                    if(t != null) {
                        Collection<Topic> instances = tm.getTopicsOfType(t);
                        paste(instances, cell[1], cell[0], direction);
                    }
                }
                catch(Exception e) {

                }
            }
        }
    }
    
    
    public void expandClasses(int direction) {
        ArrayList<int[]> selectedCells = getSelectedCells();
        if(selectedCells != null && !selectedCells.isEmpty()) {
            for(int[] cell : selectedCells) {
                try {
                    Topic t = getTopicAt(cell[0], cell[1]);
                    if(t != null) {
                        Collection<Topic> classes = t.getTypes();
                        paste(classes, cell[1], cell[0], direction);
                    }
                }
                catch(Exception e) {

                }
            }
        }
    }
    
    
    public void expandAssociationTypes(int direction) {
        ArrayList<int[]> selectedCells = getSelectedCells();
        if(selectedCells != null && !selectedCells.isEmpty()) {
            for(int[] cell : selectedCells) {
                try {
                    Topic t = getTopicAt(cell[0], cell[1]);
                    if(t != null) {
                        Collection<Topic> associationTypes = new LinkedHashSet<Topic>();
                        Collection<Association> associations = t.getAssociations();
                        for(Association a : associations) {
                            associationTypes.add(a.getType());
                        }
                        paste(associationTypes, cell[1], cell[0], direction);
                    }
                }
                catch(Exception e) {

                }
            }
        }
    }
    
    
    public void expandAssociationRoles(int direction) {
        ArrayList<int[]> selectedCells = getSelectedCells();
        if(selectedCells != null && !selectedCells.isEmpty()) {
            for(int[] cell : selectedCells) {
                try {
                    Topic t = getTopicAt(cell[0], cell[1]);
                    if(t != null) {
                        Collection<Topic> associationRoles = new LinkedHashSet<Topic>();
                        Collection<Association> associations = t.getAssociations();
                        for(Association a : associations) {
                            Collection<Topic> roles = a.getRoles();
                            for(Topic role : roles) {
                                if(t.mergesWithTopic(a.getPlayer(role))) {
                                    associationRoles.add( role );
                                }
                            }
                        }
                        paste(associationRoles, cell[1], cell[0], direction);
                    }
                }
                catch(Exception e) {

                }
            }
        }
    }
    
    
    public void expandAssociatedTopics(int direction) {
        try {
            Topic associationType = wandora.showTopicFinder(wandora, "Select association type topic");
            if(associationType != null) {
                Topic role = wandora.showTopicFinder(wandora, "Select role topic");
                if(role != null) {
                    expandAssociatedTopics(direction, associationType, role);
                }
            }
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }
    
    
    
    public void expandAssociatedTopics(int direction, Topic associationType, Topic role) {
        ArrayList<int[]> selectedCells = getSelectedCells();
        if(selectedCells != null && !selectedCells.isEmpty()) {
            for(int[] cell : selectedCells) {
                try {
                    Topic t = getTopicAt(cell[0], cell[1]);
                    if(t != null) {
                        Collection<Topic> associatedTopics = new LinkedHashSet<Topic>();
                        Collection<Association> associations = t.getAssociations(associationType);
                        for(Association a : associations) {
                            Topic associatedTopic = a.getPlayer(role);
                            if(associatedTopic != null) {
                                
                                // A special case when topic plays given role in association.
                                if(associatedTopic.mergesWithTopic(t)) {
                                    Collection<Topic> roles = a.getRoles();
                                    boolean shouldAdd = false;
                                    for(Topic r : roles) {
                                        if(!r.mergesWithTopic(role) && t.mergesWithTopic(a.getPlayer(r))) {
                                            shouldAdd = true;
                                        }
                                    }
                                    if(shouldAdd) {
                                        associatedTopics.add( associatedTopic );
                                    }
                                }
                                else {
                                    associatedTopics.add( associatedTopic );
                                }
                            }
                        }
                        paste(associatedTopics, cell[1], cell[0], direction);
                    }
                }
                catch(Exception e) {

                }
            }
        }
    }
    
    
    
    public void expandSuperclasses(int direction) {
        try {
            TopicMap tm = wandora.getTopicMap();
            Topic associationType = tm.getTopic(new Locator(XTMPSI.SUPERCLASS_SUBCLASS));
            if(associationType != null) {
                Topic role = tm.getTopic(new Locator(XTMPSI.SUPERCLASS));
                if(role != null) {
                    expandAssociatedTopics(direction, associationType, role);
                }
            }
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }
    
    
    
    public void expandSubclasses(int direction) {
        try {
            TopicMap tm = wandora.getTopicMap();
            Topic associationType = tm.getTopic(new Locator(XTMPSI.SUPERCLASS_SUBCLASS));
            if(associationType != null) {
                Topic role = tm.getTopic(new Locator(XTMPSI.SUBCLASS));
                if(role != null) {
                    expandAssociatedTopics(direction, associationType, role);
                }
            }
        }
        catch(Exception e) {
            wandora.handleError(e);
        }
    }
    
    
    
    public void paste(Collection<Topic> topics, int column, int row, int direction) {
        if(topics == null || topics.isEmpty()) return;
        
        switch(direction) {
            case LEFT: {
                pasteLeft(column, row, topics);
                break;
            }
            case UP: {
                pasteUp(column, row, topics);
                break;
            }
            case RIGHT: {
                pasteRight(column, row, topics);
                break;
            }
            case DOWN: {
                pasteDown(column, row, topics);
                break;
            }
        }
    }
    
    
    private void pasteDown(int column, int row, Collection<Topic> topics) {
        for(Topic t : topics) {
            _setTopicAt(t, ++row, column);
        }
    }
    
    private void pasteUp(int column, int row, Collection<Topic> topics) {
        for(Topic t : topics) {
            _setTopicAt(t, --row, column);
        }
    }
    
    private void pasteRight(int column, int row, Collection<Topic> topics) {
        for(Topic t : topics) {
            _setTopicAt(t, row, ++column);
        }
    }
    
    private void pasteLeft(int column, int row, Collection<Topic> topics) {
        for(Topic t : topics) {
            _setTopicAt(t, row, --column);
        }
    }
    
    public void pasteAt(int[] o, Topic[][] topics) {
        if(o != null && o.length == 2 && topics != null) {
            for(int r=0; r<topics.length; r++) {
                for(int c=0; c<topics[r].length; c++) {
                    _setTopicAt(topics[r][c], o[0]+r, o[1]+c);
                }
            }
        }
    }
    
    
    public void pasteAt(int[] o, Topic[] topics) {
        if(o != null && o.length == 2 && topics != null) {
            for(int r=0; r<topics.length; r++) {
                _setTopicAt(topics[r], o[0]+r, o[1]);
            }
        }
    }
    
    public void pasteAt(int[] o, ArrayList<Topic> topics) {
        if(o != null && o.length == 2 && topics != null) {
            int r = 0;
            for(Topic t : topics) {
                _setTopicAt(t, o[0]+r, o[1]);
                r++;
            }
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    // --------------------------------------------------- MAKE ASSOCIATIONS ---
    // -------------------------------------------------------------------------
    
    
    
    public void makeClassInstanceChains(Topic[][] data) {
        if(data != null && data.length > 0) {
            for(int i=0; i<data.length; i++) {
                Topic[] row = data[i];
                if(row != null) {
                    Topic classTopic = null;
                    for(int j=0; j<row.length; j++) {
                        Topic t = row[j];
                        if(classTopic != null) {
                            try {
                                t.addType(classTopic);
                            }
                            catch(Exception e) {}
                        }
                        classTopic = t;
                    }
                }
            }
        }
    }
    
    
    public void makeClassInstancesUsingTreeLayout(Topic[][] data) {
        if(data != null && data.length > 0) {
            Topic[] hierarchy = new Topic[1000];
            for(int i=0; i<data.length; i++) {
                Topic[] row = data[i];
                if(row != null) {
                    for(int j=0; j<row.length; j++) {
                        Topic t = row[j];
                        if(t != null) {
                            for(int k=j-1; k>=0; k--) {
                                Topic ct = hierarchy[k];
                                if(ct != null) {
                                    try {
                                        t.addType(ct);
                                        break;
                                    }
                                    catch(Exception e) {}
                                }
                            }
                            hierarchy[j] = t;
                            for(int k=j+1; k<1000; k++) {
                                hierarchy[k] = null;
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    
    
    public void makeAssociationsUsingWandoraLayout(Topic[][] data) {
        int associationCount = 0;
        TopicMap tm = wandora.getTopicMap();
        if(data != null && data.length > 2) {
            Topic associationType = null;
            Topic[] roles = null;
            try {
                associationType = data[0][0];
                if(associationType != null) {
                    roles = data[1];
                    if(roles != null) {
                        for(int i=2; i<data.length; i++) {
                            Topic[] players = data[i];
                            if(players != null && players.length == roles.length) {
                                Association a = tm.createAssociation(associationType);
                                if(a != null) {
                                    boolean hasPlayers = false;
                                    for(int j=0; j<roles.length; j++) {
                                        try {
                                            Topic role = roles[j];
                                            Topic player = players[j];
                                            if(role != null && player != null) {
                                                a.addPlayer(player, role);
                                                hasPlayers = true;
                                            }
                                        }
                                        catch(Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if(hasPlayers) {
                                        associationCount++;
                                    }
                                    else {
                                        a.remove();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Has less than three rows. Can't build an association.");
        }
        System.out.println("Created "+associationCount+" associations.");
    }
    
    
    
    
    
    public void makeAssociationsUsingLTMLayout(Topic[][] data) {
        int associationCount = 0;
        TopicMap tm = wandora.getTopicMap();
        if(data != null && data.length > 0) {
            for(int i=0; i<data.length; i++) {
                Topic[] associationData = data[i];
                if(associationData != null && associationData.length > 0) {
                    Topic associationType = associationData[0];
                    if(associationType != null) {
                        try {
                            Association a = tm.createAssociation(associationType);
                            boolean hasPlayers = false;
                            for(int j=1; j+1<associationData.length; j=j+2) {
                                try {
                                    Topic role = associationData[j+1];
                                    Topic player = associationData[j];
                                    if(role != null && player != null) {
                                        a.addPlayer(player, role);
                                        hasPlayers = true;
                                    }
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if(hasPlayers) {
                                associationCount++;
                            }
                            else {
                                a.remove();
                            }
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Has no rows. Can't build an association.");
        }
        System.out.println("Created "+associationCount+" associations.");
    }
    
    

    public void makeAssociationsUsingRDFLayout(Topic[][] data) {
        int associationCount = 0;
        TopicMap tm = wandora.getTopicMap();
        if(data != null && data.length > 0) {
            for(int i=0; i<data.length; i++) {
                Topic[] associationData = data[i];
                if(associationData != null && associationData.length > 2) {
                    Topic subject = associationData[0];
                    Topic predicate = associationData[1];
                    Topic object = associationData[2];
                    if(subject != null && predicate != null && object != null) {
                        Association a = null;
                        try {
                            a = tm.createAssociation(predicate);
                            
                            Topic subjectRole = tm.createTopic();
                            subjectRole.addSubjectIdentifier(new Locator(SimpleRDFImport.subjectTypeSI));
                            Topic objectRole = tm.createTopic();
                            objectRole.addSubjectIdentifier(new Locator(SimpleRDFImport.objectTypeSI));
                            
                            a.addPlayer(subject, subjectRole);
                            a.addPlayer(object, objectRole);
                            
                            associationCount++;
                        }
                        catch(Exception e) {
                            try {
                                if(a != null) a.remove();
                            }
                            catch(Exception ex) {}
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Has no rows. Can't build an association.");
        }
        System.out.println("Created "+associationCount+" associations.");
    }
    

    

    public void makeAssociationsUsingPlayerLayout(Topic[][] data) {
        int associationCount = 0;
        TopicMap tm = wandora.getTopicMap();
        if(data != null && data.length > 0) {
            for(int i=0; i<data.length; i++) {
                Topic[] associationData = data[i];
                if(associationData != null && associationData.length > 0) {
                    Association a = null;
                    try {
                        Topic defaultAssociationType = tm.createTopic();
                        defaultAssociationType.addSubjectIdentifier(new Locator("http://wandora.org/si/core/default-association"));
                        a = tm.createAssociation(defaultAssociationType);
                        associationCount++;
                        for(int j=0; j<associationData.length; j++) {
                            Topic player = associationData[j];
                            if(player != null) {
                                Topic role = tm.createTopic();
                                role.addSubjectIdentifier(new Locator("http://wandora.org/si/core/default-role-"+(j+1)));
                                a.addPlayer(player, role);
                            }
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            System.out.println("Has no rows. Can't build an association.");
        }
        System.out.println("Created "+associationCount+" associations.");
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // ---------------------------------------------------------- ARRAY MODS ---
    // -------------------------------------------------------------------------
    
    
    public Topic[][] sortRows(Topic[][] data) {
        if(data != null) {
            data = rotateCV(data);
            for(int i=0; i<data.length; i++) {
                for(int j=0; j<data[i].length-1; j++) {
                    for(int k=j+1; k<data[i].length; k++) {
                        if(compareTopics(data[i][j],data[i][k]) > 0) {
                            Topic t = data[i][j];
                            data[i][j] = data[i][k];
                            data[i][k] = t;
                        }
                    }
                }
            }
            data = rotateCV(data);
        }
        return data;
    }
    
    
    public Topic[][] sortColumns(Topic[][] data) {
        if(data != null) {
            for(int i=0; i<data.length; i++) {
                for(int j=0; j<data[i].length-1; j++) {
                    for(int k=j+1; k<data[i].length; k++) {
                        if(compareTopics(data[i][j],data[i][k]) > 0) {
                            Topic t = data[i][j];
                            data[i][j] = data[i][k];
                            data[i][k] = t;
                        }
                    }
                }
            }
            
        }
        return data;
    }
    
    private int compareTopics(Topic t1, Topic t2) {
        if(t1 == null && t2 == null) return 0;
        if(t1 != null && t2 == null) return 1;
        if(t1 == null && t2 != null) return -1;
        if(t1 != null && t2 != null) {
            String s1 = TopicToString.toString(t1);
            String s2 = TopicToString.toString(t2);
            if(s1 == null && s2 == null) return 0;
            if(s1 != null && s2 == null) return 1;
            if(s1 == null && s2 != null) return -1;
            if(s1 != null && s2 != null) {
                return s1.compareTo(s2);
            }
        }
        return 0;
    }
    
    
    
    private Topic[][] rotateCCV(Topic[][] data) {
        Topic[][] newData = null;
        if(data != null) {
            int maxLen = 0;
            for(int i=0; i<data.length; i++) {
                if(maxLen < data[i].length) maxLen = data[i].length;
            }
            newData = new Topic[maxLen][data.length];
            for(int i=0; i<data.length; i++) {
                for(int j=0; j<data[i].length; j++) {
                    newData[maxLen-j-1][i] = data[i][j];
                }
                for(int j=data[i].length; j<maxLen; j++) {
                    newData[maxLen-j-1][i] = null;
                }
            }
        }
        return newData;
    }
    
    
    
    private Topic[][] rotateCV(Topic[][] data) {
        Topic[][] newData = null;
        if(data != null) {
            int maxLen = 0;
            for(int i=0; i<data.length; i++) {
                if(maxLen < data[i].length) maxLen = data[i].length;
            }
            newData = new Topic[maxLen][data.length];
            for(int i=0; i<data.length; i++) {
                for(int j=0; j<data[i].length; j++) {
                    newData[j][data.length-i-1] = data[i][j];
                }
                for(int j=data[i].length; j<maxLen; j++) {
                    newData[j][data.length-i-1] = null;
                }
            }
        }
        return newData;
    }
    
        
        
    
    private Topic[][] flipDiagonally(Topic[][] data) {
        Topic[][] newData = null;
        if(data != null) {
            int maxLen = 0;
            for(int i=0; i<data.length; i++) {
                if(maxLen < data[i].length) maxLen = data[i].length;
            }
            newData = new Topic[maxLen][data.length];
            for(int i=0; i<data.length; i++) {
                for(int j=0; j<data[i].length; j++) {
                    newData[j][i] = data[i][j];
                }
                for(int j=data[i].length; j<maxLen; j++) {
                    newData[j][i] = null;
                }
            }
        }
        return newData;
    }
    
    
    
    
    private Topic[][] flipHorizontal(Topic[][] data) {
        Topic[][] newData = null;
        if(data != null) {
            int maxLen = 0;
            for(int i=0; i<data.length; i++) {
                if(maxLen < data[i].length) maxLen = data[i].length;
            }
            newData = new Topic[data.length][maxLen];
            for(int i=0; i<data.length; i++) {
                for(int j=0; j<data[i].length; j++) {
                    newData[i][maxLen-j-1] = data[i][j];
                }
                for(int j=data[i].length; j<maxLen; j++) {
                    newData[i][maxLen-j-1] = null;
                }
            }
        }
        return newData;
    }
    
    
    
    
    private Topic[][] flipVertical(Topic[][] data) {
        Topic[][] newData = null;
        if(data != null) {
            newData = new Topic[data.length][];
            for(int i=0; i<data.length; i++) {
                newData[data.length-i-1] = new Topic[data[i].length];
                for(int j=0; j<data[i].length; j++) {
                    newData[data.length-i-1][j] = data[i][j];
                }
            }
        }
        return newData;
    }
    
    

    // -------------------------------------------------------------------------
    // ----------------------------------------------------------------- DND ---
    // -------------------------------------------------------------------------

    
    public static Component dragSourceComponent = null;
    
    private class TopicGridTransferHandler extends TransferHandler {

        
        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor) ||
                   support.isDataFlavorSupported(DnDHelper.topicArrayDataFlavor) ||
                   support.isDataFlavorSupported(DnDHelper.topicGridDataFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            dragSourceComponent = TopicGrid.this;
            Transferable trans = DnDHelper.makeTopicTransferable(TopicGrid.this);
            return trans;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if(!support.isDrop()) return false;
            try {
                int action = support.getDropAction();
                DropLocation dropLocation = support.getDropLocation();
                Point tablePoint = getTablePoint(dropLocation.getDropPoint());
                int dropColumn = tablePoint.x;
                int dropRow = tablePoint.y;
                TopicMap tm=Wandora.getWandora().getTopicMap();
                if(support.isDataFlavorSupported(DnDHelper.topicGridDataFlavor)) {
                    //System.out.println("Pasted topic grid");
                    Transferable trans = support.getTransferable();
                    Object transData = trans.getTransferData(DnDHelper.topicGridDataFlavor);
                    if(transData == null) return false;
                    
                    if(transData instanceof Topic[][]) {
                        Topic[][] topics = (Topic[][]) transData;
                        if(action == MOVE && TopicGrid.this.equals(dragSourceComponent)) {
                            clearSelectedCells();
                        }
                        pasteAt(new int[] {dropRow,dropColumn}, topics);
                    }
                    moveSelection(new int[] { dropRow,dropColumn } );
                }
                else {
                    ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                    if(topics==null) return false;
                    pasteAt(new int[] {dropRow,dropColumn}, topics);
                }
                Wandora.getWandora().doRefresh();
                return true;
            }
            catch(TopicMapException tme){ tme.printStackTrace(); }
            catch(Exception ce){ ce.printStackTrace(); }
            return false;
        }
        
        
        
        
        @Override
        protected void exportDone(JComponent c, Transferable t, int action) {
            dragSourceComponent = null;
        }

    }
    
    
    // --------------------------------------------------------- SAVE & LOAD ---
    
    
    
    public void save() {
        StringBuilder data = new StringBuilder("");
        for(T2<Integer,Integer> coord : gridData.keySet()) {
            if(coord != null) {
                Topic t = gridData.get(coord);
                if(t != null) {
                    try {
                        data.append(coord.e1);
                        data.append("\t");
                        data.append(coord.e2);
                        data.append("\t");
                        data.append(t.getOneSubjectIdentifier().toExternalForm());
                        data.append("\n");
                    } 
                    catch (TopicMapException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        SimpleFileChooser chooser = UIConstants.getWandoraProjectFileChooser();
        chooser.setDialogTitle("Save topic grid data");
        if(chooser.open(wandora, SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            File f = IObox.addFileExtension(chooser.getSelectedFile(), "txt");
            if(f.exists()) {
                int overWrite = WandoraOptionPane.showConfirmDialog(wandora, "Overwrite existing file '"+f.getName()+"'?", "Overwrite file?", WandoraOptionPane.YES_NO_OPTION);
                if(overWrite == WandoraOptionPane.NO_OPTION) return;
            }
            try {
                IObox.saveFile(f, data.toString());
            } 
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    public void load() {
        SimpleFileChooser chooser = UIConstants.getWandoraProjectFileChooser();
        chooser.setDialogTitle("Load topic grid data");
        if(chooser.open(wandora, SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if(f.exists()) {
                try {
                    String data = IObox.loadFile(f);
                    HashMap<T2<Integer,Integer>, Topic> newGridData = parse(data);
                    gridData = newGridData;
                } 
                catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } 
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    

    public void merge() {
        SimpleFileChooser chooser = UIConstants.getWandoraProjectFileChooser();
        chooser.setDialogTitle("Merge topic grid data");
        if(chooser.open(wandora, SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if(f.exists()) {
                try {
                    String data = IObox.loadFile(f);
                    HashMap<T2<Integer,Integer>, Topic> newGridData = parse(data);
                    for(T2<Integer,Integer> c : newGridData.keySet()) {
                        gridData.put(c, newGridData.get(c));
                    }
                } 
                catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } 
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    
    
    public HashMap<T2<Integer,Integer>, Topic> parse(String data) {
        TopicMap tm = wandora.getTopicMap();
        HashMap<T2<Integer,Integer>, Topic> newGridData = new HashMap();

        StringTokenizer dataLines = new StringTokenizer(data, "\n");
        while(dataLines.hasMoreTokens()) {
            String dataLine = dataLines.nextToken();
            StringTokenizer topicData = new StringTokenizer(dataLine, "\t");
            if(topicData.countTokens() == 3) {
                String e1 = topicData.nextToken();
                String e2 = topicData.nextToken();
                String si = topicData.nextToken();

                try {
                    int e1i = Integer.parseInt(e1);
                    int e2i = Integer.parseInt(e2);
                    Topic t = tm.getTopic(si);

                    if(t != null && !t.isRemoved()) {
                        newGridData.put(new T2(e1i, e2i), t);
                    }
                    else {
                        System.out.println("Couldn't find a topic for subject identifier '"+si+"'. Skipping topic.");
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return newGridData;
    }
    
    
    
}
