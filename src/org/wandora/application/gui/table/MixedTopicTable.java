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
 * MixedTopicTable.java
 */

package org.wandora.application.gui.table;



import org.wandora.application.gui.topicstringify.TopicToString;
import java.awt.Point;
import org.wandora.application.tools.navigate.OpenTopic;

import java.awt.event.*;
import javax.swing.*;
import java.awt.datatransfer.*;

import java.util.ArrayList;
import org.wandora.application.*;
import org.wandora.application.gui.Clipboardable;
import org.wandora.application.gui.DnDHelper;
import org.wandora.application.gui.UIBox;
import org.wandora.topicmap.*;
import org.wandora.utils.swing.*;
import org.wandora.application.tools.*;
import org.wandora.utils.Textbox;


/**
 *
 * @author olli, akivela
 */
public class MixedTopicTable extends TopicTable implements MouseListener, ActionListener, Clipboardable /*, DragSourceListener , DragGestureListener*/  {


    
    public Wandora wandora = null;
    public TableSorter sorter;

    private Object[] popupStruct;
    private Object[] rolePopupStruct;



    public MixedTopicTable(Wandora w) {
        super(w);
        this.wandora = w;
        this.setDragEnabled(true);
        this.setTransferHandler(new MixedTopicTableTransferHandler());
    }


    @Override
    protected Object[] getPopupStruct() {
        return new Object[] {
            "Copy", this,
            "---",
            "Open topic", new OpenTopic(),
            "Open topic in", WandoraMenuManager.getOpenInMenu(),
            "---",
            "Select", WandoraMenuManager.getDefaultSelectMenuStruct(wandora, this),
            "Topics", WandoraMenuManager.getDefaultTopicMenuStruct(wandora, this),
        };
    }
        
        
    @Override
    protected Object[] getHeaderPopupStruct() {
        Object header = this.getTableHeader();
        return new Object[] {
            "Open topic", new OpenTopic(),
            "Open topic in", WandoraMenuManager.getOpenInMenu(),
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

    
    
    
    // -------------------------------------------------------------------------

    
    
    public void initialize(Object[] tableTopics, Object columnTopic) {
        Object[][] extendedTableTopics = new Topic[tableTopics.length][1];
        for(int i=0; i<tableTopics.length; i++) {
            extendedTableTopics[i][0] = tableTopics[i];
        }
        initialize(extendedTableTopics, new Object[] { columnTopic } );
    }



    
    
    public void initialize(Object[][] rawData, Object[] columnObjects) {
        try {
            if(rawData == null || columnObjects == null) return;

            setDefaultRenderer(Topic.class, new MixedTopicTableCellRenderer(this)); 
            setDefaultRenderer(String.class, new MixedTopicTableCellRenderer(this));
            
            MixedTopicTableModel model = new MixedTopicTableModel(rawData, columnObjects);
            this.setModel(model);
            this.setRowSorter(new MixedTopicTableRowSorter(model));

            this.createDefaultTableSelectionModel();

            this.addMouseListener(this);

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
                    if(e.isPopupTrigger()) {
                        Object o=getColumnAt(e.getX());
                        if(o instanceof Topic){
                            JPopupMenu rolePopup = UIBox.makePopupMenu(getHeaderPopupStruct(), wandora);
                            rolePopup.show(e.getComponent(),e.getX(),e.getY());
                        }
                    }
                }
            });
        }
        catch(Exception e) {
            wandora.handleError(e);
        }

    }


    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------


    @Override
    public String getToolTipText(MouseEvent e) {
        Object o = getValueAt(getTablePoint(e));
        if(o != null) {
            try {
                if(o instanceof String) {
                    return Textbox.makeHTMLParagraph(o.toString(), 60);
                }
                if(o instanceof Topic) {
                    Topic t = (Topic) o;
                    String tooltipText = TopicToString.toString(t);
                    return Textbox.makeHTMLParagraph(tooltipText, 60);
                }
            }
            catch(Exception ex) {}
        }
        return "";
    }
    
    

    @Override
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
                            rawString.append(selectedValues[col][row]);
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
                copyString.append( getValueAt(getTablePoint()) );
            }
        } 
        catch(Exception tme){
            wandora.handleError(tme);
        }
        return copyString.toString();
    }
    
    
    
    
    @Override
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
    
    
    
    

    // -------------------------------------------------------------------------





    @Override
    public void actionPerformed(ActionEvent e) {
        String c = e.getActionCommand();
        if("Copy".equalsIgnoreCase(c)) {
            this.copy();
        }
    }




    // -------------------------------------------------------------------------
    // ----------------------------------------------------------------- DND ---
    // -------------------------------------------------------------------------


    private class MixedTopicTableTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            return false;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return DnDHelper.makeTopicTableTransferable(MixedTopicTable.this);
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
