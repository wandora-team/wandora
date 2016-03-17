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
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.UIConstants;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.previews.PreviewPanel;
import org.wandora.application.gui.previews.PreviewUtils;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.table.TopicTableRowSorter;
import org.wandora.application.tools.extractors.files.SimpleFileExtractor;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.ClipboardBox;
import org.wandora.utils.DataURL;
import org.wandora.utils.IObox;
import org.wandora.utils.MimeTypes;


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
        private ZipTableModel model = null;
        TableRowSorter rowSorter = null;
        

        public ZipTable(String locator) {
            super();
            model = new ZipTableModel(locator);
            this.setModel(model);
            rowSorter = new TableRowSorter(model);
            this.setRowSorter(rowSorter);
            rowSorter.setSortsOnUpdates(true);
            
            JPopupMenu popup = UIBox.makePopupMenu(getPopupStruct(), this);
            setComponentPopupMenu(popup);
            
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        
        public Object[] getPopupStruct() {
            return new Object[] {
                "Save to file...",
                "Save to occurrence...",
                "Save to document topic..."
            };
        }
        
        
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("actionPerformed: "+e.getActionCommand());
            String command = e.getActionCommand();
            if(command != null) {
                if("Save to file...".equalsIgnoreCase(command)) {
                    int[] selectedRows = this.getSelectedRows();
                    saveToFile(selectedRows);
                }
                else if("Save to occurrence...".equalsIgnoreCase(command)) {
                    int[] selectedRows = this.getSelectedRows();
                    saveToOccurrence(selectedRows);
                }
                else if("Save to document topic...".equalsIgnoreCase(command)) {
                    int[] selectedRows = this.getSelectedRows();
                    saveToTopic(selectedRows);
                }
            }
        }
        
        
        // ---------------------------------------------------------------------
        
        
        private void saveToFile(int[] rows) {
            if(rows != null) {
                String savePath = null;
                if(rows.length > 1) {
                    savePath = getSavePath();
                    if(savePath == null) {
                        return;
                    }
                }
                for(int i=0; i<rows.length; i++) {
                    int selectedRow = rows[i];
                    ZipTableRow zipTableRow = model.getZipTableRowAt(selectedRow);
                    if(zipTableRow != null) {
                        byte[] zipData = model.getData(zipTableRow.filename);
                        if(zipData != null) {
                            saveToFile(savePath, zipTableRow.filename, zipData);
                        }
                    }
                }
            }
        }
        
        
        
        private void saveToFile(String savePath, String originalFilename, byte[] data) {
            Wandora wandora = Wandora.getWandora();
            try {
                if(savePath == null) {
                    SimpleFileChooser chooser = UIConstants.getFileChooser();
                    chooser.setDialogTitle("Save file");
                    String originalFilenamePart = originalFilename.substring(originalFilename.lastIndexOf(File.pathSeparator)+1);
                    chooser.setSelectedFile(new File(originalFilenamePart));

                    if(chooser.open(wandora,SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
                        IObox.saveBFile(chooser.getSelectedFile().getAbsolutePath(), data);
                    }
                }
                else {
                    String saveFilename = savePath+File.pathSeparator+originalFilename;
                    IObox.createPathFor(new File(saveFilename));
                    IObox.saveBFile(saveFilename, data);
                }
            }
            catch(Exception e) {
                wandora.handleError(e);
            }
        }
        
        
        
        private String getSavePath() {
            SimpleFileChooser chooser = UIConstants.getFileChooser();
            chooser.setDialogTitle("Select folder");
            chooser.setApproveButtonText("Select");
            if(chooser.open(Wandora.getWandora(),SimpleFileChooser.SAVE_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                if(selected != null) {
                    if(!selected.isDirectory()) {
                        selected = selected.getParentFile();
                    }
                    return selected.getAbsolutePath();
                }
            }
            return null;
        }
        
        
        
        
        // ---------------------------------------------------------------------
        
        
        
        private void saveToOccurrence(int[] rows) {
            if(rows != null) {
                for(int i=0; i<rows.length; i++) {
                    int selectedRow = rows[i];
                    ZipTableRow zipTableRow = model.getZipTableRowAt(selectedRow);
                    if(zipTableRow != null) {
                        byte[] zipData = model.getData(zipTableRow.filename);
                        if(zipData != null) {
                            saveToOccurrence(zipTableRow.filename, zipData);
                        }
                    }
                }
            }
        }
        
        
        
        private void saveToOccurrence(String filename, byte[] data) {
            Wandora wandora = Wandora.getWandora();
            try {
                Topic topic = wandora.getOpenTopic();
                if(topic != null) {
                    Topic type = wandora.showTopicFinder(wandora, "Select occurrence type for "+filename);
                    if(type == null) return;
                    
                    Topic scope = wandora.showTopicFinder(wandora, "Select occurrence scope for "+filename);
                    if(scope == null) return;
                    
                    int makeDataUrl = WandoraOptionPane.showConfirmDialog(wandora, "Make data url occurrence for '"+filename+"'?", "Make data url occurrence?", WandoraOptionPane.YES_NO_OPTION);
                    if(makeDataUrl == WandoraOptionPane.YES_OPTION) {
                        DataURL dataUrl = new DataURL(data);
                        String mimetype = MimeTypes.getMimeType(filename);
                        if(mimetype != null) {
                            dataUrl.setMimetype(mimetype);
                        }
                        topic.setData(type, scope, dataUrl.toExternalForm());
                    }
                    else {
                        topic.setData(type, scope, new String(data));
                    }
                    wandora.doRefresh();
                }
            }
            catch(Exception e) {
                wandora.handleError(e);
            }
        }
        
        
        
        // ---------------------------------------------------------------------
        
        
        private void saveToTopic(int[] rows) {
            if(rows != null) {
                for(int i=0; i<rows.length; i++) {
                    int selectedRow = rows[i];
                    ZipTableRow zipTableRow = model.getZipTableRowAt(selectedRow);
                    if(zipTableRow != null) {
                        byte[] zipData = model.getData(zipTableRow.filename);
                        if(zipData != null) {
                            saveToTopic(zipTableRow.filename, zipData);
                        }
                    }
                }
            }
        }
        
        
        private void saveToTopic(String filename, byte[] data) {
            Wandora wandora = Wandora.getWandora();
            try {
                TopicMap topicMap = wandora.getTopicMap();
                DataURL dataUrl = new DataURL(data);
                String mimetype = MimeTypes.getMimeType(filename);
                if(mimetype != null) {
                    dataUrl.setMimetype(mimetype);
                }

                SimpleFileExtractor simpleFileExtractor = new SimpleFileExtractor();
                simpleFileExtractor._extractTopicsFrom(dataUrl.toExternalForm(), topicMap);
                
                wandora.doRefresh();
            }
            catch(Exception e) {
                wandora.handleError(e);
            }
        }
        
        
        
        // ---------------------------------------------------------------------
        
        
        
        public class ZipTableModel extends DefaultTableModel {
            ArrayList<ZipTableRow> zipData;
            int numberOfFields = 6;
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String locator = null;
            
            
            public ZipTableModel(String locator) {
                this.locator = locator;
                this.zipData = createModel(locator);
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
            
            
            public ZipTableRow getZipTableRowAt(int rowIndex) {
                try {
                    return zipData.get(rowIndex);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                return null;
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
                ZipInputStream zipInputStream = null;
                try {
                    if(DataURL.isDataURL(locator)) {
                        DataURL dataUrl = new DataURL(locator);
                        zipInputStream = new ZipInputStream(dataUrl.getDataStream());
                    }
                    else {
                        zipInputStream = new ZipInputStream(new URL(locator).openStream());
                    }
                    ZipEntry zipEntry = zipInputStream.getNextEntry();
                    while(zipEntry != null) {
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
                        
                        zipInputStream.closeEntry();
                        zipEntry = zipInputStream.getNextEntry();
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                if(zipInputStream != null) {
                    try {
                        zipInputStream.close();
                    }
                    catch(Exception e) {}
                }
                return zipModel;
            }
            
            
            
            public byte[] getData(String entryName) {
                ZipInputStream zipInputStream = null;
                byte[] entryData = null;
                try {
                    if(DataURL.isDataURL(locator)) {
                        DataURL dataUrl = new DataURL(locator);
                        zipInputStream = new ZipInputStream(dataUrl.getDataStream());
                    }
                    else {
                        zipInputStream = new ZipInputStream(new URL(locator).openStream());
                    }
                    ZipEntry zipEntry = zipInputStream.getNextEntry();
                    while(zipEntry != null && entryData == null) {
                        String zipEntryName = zipEntry.getName();
                        if(zipEntryName.equals(entryName)) {
                            int n = 0;
                            byte[] buf = new byte[1024];
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            while ((n = zipInputStream.read(buf, 0, 1024)) > -1) {
                                out.write(buf, 0, n);
                            }
                            out.close();
                            zipInputStream.closeEntry();
                            entryData = out.toByteArray();
                        }
                        zipEntry = zipInputStream.getNextEntry();
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                try {
                    zipInputStream.close();
                }
                catch(Exception e) {}
                return entryData;
            }
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
