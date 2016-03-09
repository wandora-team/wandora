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
package org.wandora.application.gui.previews.formats;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.PreviewUtils;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.table.TopicTableRowSorter;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;


/**
 *
 * @author akivela
 */
public class ApplicationZip implements PreviewPanel, ActionListener {
    
    
    private final String locator;
    JPanel ui = null;
    JTable table = null;
    
    
    
    public ApplicationZip(String zipLocator) {
        this.locator = zipLocator;
    }

    @Override
    public void stop() {

    }

    @Override
    public void finish() {
        
    }

    @Override
    public JPanel getGui() {
        if(ui == null) {
            ui = new JPanel();
            ui.setLayout(new BorderLayout(8,8));
            
            JPanel tablePaneWrapper = new JPanel();
            tablePaneWrapper.setLayout(new BorderLayout());
        
            tablePaneWrapper.setPreferredSize(new Dimension(640, 300));
            tablePaneWrapper.setMaximumSize(new Dimension(640, 300));
            tablePaneWrapper.setSize(new Dimension(640, 300));
            
            try {
                table = new ZipTable(locator);
                JScrollPane scrollPane = new SimpleScrollPane(table);
                tablePaneWrapper.add(scrollPane, BorderLayout.CENTER);

                JPanel toolbarWrapper = new JPanel();
                toolbarWrapper.add(getJToolBar());

                ui.add(tablePaneWrapper, BorderLayout.CENTER);
                ui.add(toolbarWrapper, BorderLayout.SOUTH);
            }
            catch(Exception e) {
                PreviewUtils.previewError(ui, "Can't initialize text viewer. Exception occurred.", e);
            }

        }
        return ui;
    }

    

    @Override
    public boolean isHeavy() {
        return false;
    }
    
    
    

    protected JComponent getJToolBar() {
        return UIBox.makeButtonContainer(new Object[] {
            "Open ext", PreviewUtils.ICON_OPEN_EXT, this,
            "Copy location", PreviewUtils.ICON_COPY_LOCATION, this,
            "Save", PreviewUtils.ICON_SAVE, this,
        }, this);
    }
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        
        if(c.startsWith("Open ext")) {
            PreviewUtils.forkExternalPlayer(locator);
        }
        else if(c.equalsIgnoreCase("Copy location")) {
            if(locator != null) {
                ClipboardBox.setClipboard(locator);
            }
        }
        else if(c.startsWith("Save")) {
            PreviewUtils.saveToFile(locator);
        }
    }
    
    
    

    // -------------------------------------------------------------------------
    
    public static boolean canView(String url) {
        return PreviewUtils.isOfType(url, 
                new String[] { 
                    "application/zip",                   
                },
                new String[] { 
                    "zip"
                }
        );
    }
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------------ ZipTable ---
    // -------------------------------------------------------------------------
    
    
    
    public class ZipTable extends JTable implements ActionListener {
        

        public ZipTable(String locator) {
            super();
            ZipTableModel model = new ZipTableModel(locator);
            this.setModel(model);
            TableRowSorter rowSorter = new TableRowSorter(model);
            this.setRowSorter(rowSorter);
            rowSorter.setSortsOnUpdates(true);
            
            JPopupMenu popup = UIBox.makePopupMenu(getPopupStruct(), this);
            setComponentPopupMenu(popup);
            
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        
        public Object[] getPopupStruct() {
            return new Object[] {
                "Save to file...",
                "Save to occurrence..."    
            };
        }
        
        
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command != null) {
                if(command.startsWith("Save to file")) {
                    
                }
                else if(command.startsWith("Save to occurrence")) {
                    
                }
                else if(command.startsWith("Create topic for files")) {
                    
                }
            }
        }
        
        
        // ---------------------------------------------------------------------
        
        
        public class ZipTableModel extends DefaultTableModel {
            ArrayList<ZipTableRow> zipData;
            int numberOfFields = 6;
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            
            public ZipTableModel(String locator) {
                zipData = createModel(locator);
            }
            
            @Override
            public int getColumnCount() {
                return numberOfFields;
            }

            @Override
            public Class getColumnClass(int col) {
                switch(col) {
                    case 0: return String.class;
                    case 1: return String.class;
                    case 2: return Long.class;
                    case 3: return Long.class;
                    case 4: return String.class;
                    case 5: return String.class;
                }
                return String.class;
            }

            @Override
            public int getRowCount() {
                if(zipData != null) return zipData.size();
                return 0;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                try {
                    if(zipData != null && rowIndex >= 0 && columnIndex >= 0 && columnIndex < getColumnCount() && rowIndex < getRowCount()) {
                        ZipTableRow zipDataRow = zipData.get(rowIndex);
                        
                        switch(columnIndex) {
                            case 0: return zipDataRow.filename;
                            case 1: return zipDataRow.isFolder ? "Folder" : "File";
                            case 2: return zipDataRow.compressedSize;
                            case 3: return zipDataRow.size;
                            case 4: return zipDataRow.creationTime == 0 ? "" : dateFormatter.format(new Date(zipDataRow.creationTime));
                            case 5: return zipDataRow.modifiedTime == 0 ? "" : dateFormatter.format(new Date(zipDataRow.modifiedTime));
                        }
                    }
                }
                catch (Exception e) {}
                return "";
            }

            @Override
            public String getColumnName(int columnIndex) {
                switch(columnIndex) {
                    case 0: return "Filename";
                    case 1: return "Type";
                    case 2: return "Compressed size";
                    case 3: return "Size";
                    case 4: return "Creation time";
                    case 5: return "Modified time";
                }
                return "";
            }

            @Override
            public boolean isCellEditable(int row,int col){
                return false;
            }
            
            private ArrayList<ZipTableRow> createModel(String locator) {
                ArrayList<ZipTableRow> zipModel = new ArrayList<ZipTableRow>();
                try {
                    ZipInputStream zipInputStream = null;

                    if(DataURL.isDataURL(locator)) {
                        DataURL dataUrl = new DataURL(locator);
                        zipInputStream = new ZipInputStream(dataUrl.getDataStream());
                    }
                    else {
                        zipInputStream = new ZipInputStream(new URL(locator).openStream());
                    }
                    ZipEntry zipEntry = null;
                    do {
                        zipEntry = zipInputStream.getNextEntry();
                        if(zipEntry != null) {
                            ZipTableRow zipTableRow = new ZipTableRow();
                            zipTableRow.filename = zipEntry.getName();
                            zipTableRow.compressedSize = zipEntry.getCompressedSize();
                            zipTableRow.size = zipEntry.getSize();
                            zipTableRow.time = zipEntry.getTime();
                            if(zipEntry.getCreationTime() != null) {
                                zipTableRow.creationTime = zipEntry.getCreationTime().toMillis();
                            }
                            if(zipEntry.getLastModifiedTime() != null) {
                                zipTableRow.modifiedTime = zipEntry.getLastModifiedTime().toMillis();
                            }
                            zipTableRow.isFolder = zipEntry.isDirectory();
                            zipModel.add(zipTableRow);
                        }
                    }
                    while(zipEntry != null);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                return zipModel;
            }

            public class ZipTableRow {
                public String filename = null; 
                public long compressedSize = 0;
                public long size = 0;
                public long time = 0;
                public long creationTime = 0;
                public long modifiedTime = 0;
                public boolean isFolder = false;
            }
        }
    }
}
