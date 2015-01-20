/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * OccurrenceTableAll.java
 *
 * Created on August 17, 2004, 12:07 PM
 */

package org.wandora.application.gui;



import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.wandora.application.*;
import org.wandora.application.contexts.ApplicationContext;
import org.wandora.application.gui.simple.*;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.occurrences.*;
import org.wandora.application.tools.occurrences.DeleteOccurrence;
import org.wandora.topicmap.*;
import org.wandora.utils.*;
import org.wandora.utils.language.GoogleTranslateBox;
import org.wandora.utils.language.MicrosoftTranslateBox;
import org.wandora.utils.language.WatsonTranslateBox;
import org.wandora.utils.swing.*;


/**
 *
 * @author  olli, akivela
 */
public class OccurrenceTableAll extends SimpleTable implements OccurrenceTable, MouseListener, Clipboardable {
    
    public String tableType = VIEW_SCHEMA;
    
    private Topic topic;
    private Topic[] langs;
    private Topic[] types;
    private String[][] data;
    private Color[][] colors;
    private TableSorter sorter;
    private Wandora wandora;
    private Options options;
    private TableTopicWrapper[] wrappedTypes;
    private HashSet removedTypes;
    private String[][] originalData;
    private int rowHeight = 1;
    
    private Object[] popupStruct;
    private MouseEvent mouseEvent;
    
    
    /** Creates a new instance of OccurrenceTableAll */
    public OccurrenceTableAll(Topic topic, Options options, Wandora wandora) throws TopicMapException {
        this.wandora = wandora;
        this.topic = topic;
        this.options = options;
        
        try {
            Options opts = options;
            if(opts == null) opts = wandora.getOptions();
            if(opts != null) {
                tableType = opts.get(VIEW_OPTIONS_KEY);
                if(tableType == null || tableType.length() == 0) tableType = VIEW_SCHEMA;
                
                rowHeight = opts.getInt(ROW_HEIGHT_OPTIONS_KEY);
                if(rowHeight < 1) rowHeight = 1;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        types=(Topic[])topic.getDataTypes().toArray(new Topic[0]);

        ArrayList<Topic> langArray = new ArrayList<Topic>();
        if(VIEW_USED.equalsIgnoreCase(tableType) || VIEW_USED_AND_SCHEMA.equalsIgnoreCase(tableType)) {
            Hashtable<Topic,String> occs = null;
            Topic langTopic = null;
            for(int i=0; i<types.length; i++) {
                occs = topic.getData(types[i]);
                for(Enumeration<Topic> keys = occs.keys(); keys.hasMoreElements(); ) {
                    langTopic = keys.nextElement();
                    if(!langArray.contains(langTopic)) {
                        langArray.add(langTopic);
                    }
                }
            }
        }
        if(VIEW_SCHEMA.equalsIgnoreCase(tableType) || VIEW_USED_AND_SCHEMA.equalsIgnoreCase(tableType)) {
            String[] langSIs=TMBox.getLanguageSIs(wandora.getTopicMap());
            Topic langTopic = null;
            for(int i=0;i<langSIs.length;i++){
                langTopic = wandora.getTopicMap().getTopic(langSIs[i]);
                if(langTopic != null && !langArray.contains(langTopic)) {
                    langArray.add( langTopic );
                }
            }
        }
        langs=langArray.toArray( new Topic[langArray.size()] );
        
        wrappedTypes=new TableTopicWrapper[types.length];
        removedTypes=new HashSet();
        
        data=new String[types.length][langs.length];
        originalData=new String[types.length][langs.length];
        colors=new Color[types.length][langs.length];

        for(int i=0;i<types.length;i++) {
            if(types[i] != null) {
                wrappedTypes[i]=new TableTopicWrapper(types[i]);
                for(int j=0;j<langs.length;j++){
                    if(langs[j] != null) {
                        data[i][j]=topic.getData(types[i],langs[j]);
                        if(data[i][j]==null) data[i][j]="";
                        originalData[i][j]=data[i][j];
                        colors[i][j]=wandora.topicHilights.getOccurrenceColor(topic,types[i],langs[j]);
                    }
                }
            }
        }
        this.setColumnSelectionAllowed(false);
        this.setRowSelectionAllowed(false);
        sorter=new TableSorter(new DataTableModel());
        
        final Color[] columnColors=new Color[langs.length];
        for(int i=0;i<columnColors.length;i++){
            columnColors[i]=wandora.topicHilights.get(langs[i]);
            if(columnColors[i]==null) columnColors[i]=wandora.topicHilights.getLayerColor(langs[i]);
        }
        final TableCellRenderer oldRenderer=this.getTableHeader().getDefaultRenderer();
        this.getTableHeader().setDefaultRenderer(new TableCellRenderer(){
            public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus, int row, int column){
                Component c=oldRenderer.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                column=convertColumnIndexToModel(column);
                if(column>0 && column-1<columnColors.length && columnColors[column-1]!=null){
                    c.setForeground(columnColors[column-1]);
                }
                return c;
            }
        });
        
        this.setRowHeight(2+rowHeight*16);
        this.setAutoCreateColumnsFromModel(false);
        this.setModel(sorter);
        TableColumn column=new TableColumn(0,80,new TopicCellRenderer(),new TopicCellEditor());
        this.addColumn(column);
        for(int i=0;i<langs.length;i++){
            this.addColumn(new TableColumn(i+1,80,new DataCellRenderer(),new DataCellEditor()));
        }
        column=new TableColumn(langs.length+1,80,new DeleteCellRenderer(),new DeleteCellEditor());
        column.setMaxWidth(30);
        column.setMinWidth(30);
        this.addColumn(column);
        sorter.setTableHeader(this.getTableHeader());
        
        
        initPopupStructures();
        JPopupMenu popup = UIBox.makePopupMenu(popupStruct, wandora);
        this.setComponentPopupMenu(popup);
        this.addMouseListener(this);
        
        this.setDragEnabled(true);
        this.setTransferHandler(new OccurrencesTableTransferHandler());
        this.setDropMode(DropMode.ON);
        this.createDefaultTableSelectionModel();
    }
    
    
    
    
    
    public boolean applyChanges(Topic t, Wandora parent) throws TopicMapException {
        boolean changed=false;
        for(int i=0;i<types.length;i++) {
            if(removedTypes.contains(types[i])) continue;
            for(int j=0;j<langs.length;j++){
                String text=data[i][j];
                if(text != null) {
                    // String orig=topic.getData(types[i],langs[j]);
                    String orig=originalData[i][j];
                    if(orig==null) orig="";
                    if(!orig.equals(text)) {
                        changed=true;
                        if(text.length()==0) {
                            topic.removeData(types[i],langs[j]);
                        }
                        else {
                            topic.setData(types[i], langs[j], text );
                        }
                    }
                }
            }
        }
        return changed;
    }
    
    
    public Topic getTopic() {
        return topic;
    }
    
    
    @Override
    public String getToolTipText(java.awt.event.MouseEvent e) {
        java.awt.Point p=e.getPoint();
        int row=rowAtPoint(p);
        int col=columnAtPoint(p);
        int realCol=convertColumnIndexToModel(col);
        if(realCol==langs.length+1) return null;
        String tooltipText = sorter.getValueAt(row,realCol).toString();
        if(tooltipText != null && tooltipText.length() > 5) {
            if(tooltipText.length() > 1000) tooltipText = tooltipText.substring(0,999)+"...";
            return Textbox.makeHTMLParagraph(tooltipText, 40);
        }
        return null;
    }
    
    
    
    public int getRowHeightOption() {
        return rowHeight;
    }
    
    
    
    
    public Object getOccurrenceTableType() {
        return tableType;
    }

        

    
    private void initPopupStructures() {
        try {
            popupStruct = WandoraMenuManager.getOccurrenceTableMenu(this, options);
        }
        catch(Exception tme){
            wandora.handleError(tme);
        }
    }
    
    
    
    public String getPointedOccurrence() {
        try {
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol > 0 && realRow < types.length && realCol < langs.length+1) {
                return topic.getData(types[realRow], langs[realCol-1]);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public Topic getPointedOccurrenceType() {
        try {
            Point p = getTablePoint(mouseEvent);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realRow < types.length) {
                return types[realRow];
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    
    public Topic getPointedOccurrenceLang() {
        try {
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            if(realCol > 0 && realCol < langs.length+1) {
                return langs[realCol-1];
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    
    
    
    public void cut() {
        try {
            copy();
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol > 0 && realRow < types.length && realCol < langs.length+1) {
                topic.removeData(types[realRow], langs[realCol-1]);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            if(wandora != null) wandora.handleError(e);
        }
    }
    
    
    
    
    
    
    public void paste() {
        try {
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol > 0 && realRow < types.length && realCol < langs.length+1) {
                topic.setData(types[realRow], langs[realCol-1], ClipboardBox.getClipboard());
            }
        }
        catch(Exception e) {
            if(wandora != null) wandora.handleError(e);
        }
    }
    
    
    
    public void append() {
        try {
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol > 0 && realRow < types.length && realCol < langs.length+1) {
                String oldOccurrence = topic.getData(types[realRow], langs[realCol-1]);
                if(oldOccurrence == null) oldOccurrence = "";
                topic.setData(types[realRow], langs[realCol-1], oldOccurrence + ClipboardBox.getClipboard());
            }
        }
        catch(Exception e) {
            if(wandora != null) wandora.handleError(e);
        }
    }
    
    
    
    public void copy() {
        ClipboardBox.setClipboard(getCopyString());
    }
    public String getCopyString() {
        Object cellContent;
        try {
            Point loc = getTablePoint(mouseEvent);
            cellContent = getValueAt(loc);
            return cellContent.toString();
        }
        catch(Exception e){
            if(wandora != null) wandora.handleError(e);
        }
        return "";
    }

    
    
    public void delete() {
        try {
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol >= 0 && realRow < types.length && realCol < langs.length+1) {
                if(realCol == 0) {
                    topic.removeData(types[realRow]);
                }
                else {
                    topic.removeData(types[realRow], langs[realCol-1]);
                }
            }
        }
        catch(Exception e) {
            if(wandora != null) wandora.handleError(e);
        }
    }
    
    
    
    public void changeType() {
        try {
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol >= 0 && realRow < types.length && realCol <= langs.length) {
                Topic type = types[realRow];
                Topic selectedScope = null;
                if(realCol > 0 && realCol <= langs.length) {
                    selectedScope = langs[realCol-1];
                }
                if(type != null && !type.isRemoved()) {
                    Topic newType = wandora.showTopicFinder("Select new occurrence type");
                    if(newType != null && !newType.isRemoved()) {
                        Hashtable<Topic,String> os = topic.getData(type);
                        if(os != null && !os.isEmpty()) {
                            for(Topic scope : os.keySet()) {
                                if(scope != null && !scope.isRemoved()) {
                                    if(selectedScope == null) {
                                        String occurrenceText = os.get(scope);
                                        topic.setData(newType, scope, occurrenceText);
                                    }
                                    else if(selectedScope.mergesWithTopic(scope)) {
                                        String occurrenceText = os.get(scope);
                                        topic.setData(newType, scope, occurrenceText);
                                    }
                                }
                            }
                            if(selectedScope == null) {
                                topic.removeData(type);
                            }
                            else {
                                topic.removeData(type, selectedScope);
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            if(wandora != null) wandora.handleError(e);
        }
    }
    
    
    
    public void duplicateType() {
        try {
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol >= 0 && realRow < types.length && realCol < langs.length+1) {
                Topic type = types[realRow];
                Topic selectedScope = null;
                if(realCol > 0 && realCol <= langs.length) {
                    selectedScope = langs[realCol-1];
                }
                if(type != null && !type.isRemoved()) {
                    Topic newType = wandora.showTopicFinder("Select new occurrence type");
                    if(newType != null && !newType.isRemoved()) {
                        Hashtable<Topic,String> os = topic.getData(type);
                        if(os != null && !os.isEmpty()) {
                            for(Topic scope : os.keySet()) {
                                if(scope != null && !scope.isRemoved()) {
                                    if(selectedScope == null) {
                                        String occurrenceText = os.get(scope);
                                        topic.setData(newType, scope, occurrenceText);
                                    }
                                    else if(selectedScope.mergesWithTopic(scope)) {
                                        String occurrenceText = os.get(scope);
                                        topic.setData(newType, scope, occurrenceText);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            if(wandora != null) wandora.handleError(e);
        }
    }
    
    
    
    public void openURLOccurrence() {
        try {
            String errorMessage = null;
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol > 0 && realRow < types.length && realCol < langs.length+1) {
                String occurrence = topic.getData(types[realRow], langs[realCol-1]);
                if(occurrence != null) {
                    occurrence = occurrence.trim();
                    if(occurrence.length() > 0) {
                        try {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.browse(new URI(occurrence));
                        }
                        catch(Exception e) {
                            if(occurrence.length() > 80) occurrence = occurrence.substring(0, 80)+"...";
                            errorMessage = "Exception occurred while starting external browser for occurrence. Check if occurrence text is a valid URL.";
                            e.printStackTrace();
                        }
                    }
                }
            }
            if(errorMessage != null) {
                WandoraOptionPane.showMessageDialog(Wandora.getWandora(), errorMessage, "Error while opening URL");
            }
        }
        catch(Exception e) {
            if(wandora != null) wandora.handleError(e);
        }
    }
    
    
    
    public void downloadURLOccurrence() {
        try {
            Point p = getTablePoint(mouseEvent);
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol > 0 && realRow < types.length && realCol < langs.length+1) {
                String occurrence = topic.getData(types[realRow], langs[realCol-1]);
                if(occurrence != null) {
                    occurrence = occurrence.trim();
                    if(occurrence.length() > 0) {
                        try {
                            String occurrenceContent = IObox.doUrl(new URL(occurrence));
                            topic.setData(types[realRow], langs[realCol-1], occurrenceContent);
                        }
                        catch(Exception e) {
                            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while downloading URL '"+occurrence+"'.", "Error downloading URL", WandoraOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            if(wandora != null) wandora.handleError(e);
        }
    }
    
    
    public void spread() {
        try {
            Point p = getTablePoint(mouseEvent);
            boolean overrideAll = false;
            if(mouseEvent != null && mouseEvent.isAltDown()) overrideAll = true;
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol > 0 && realRow < types.length && realCol < langs.length+1) {
                String occurrenceText = topic.getData(types[realRow], langs[realCol-1]);
                Topic type = types[realRow];
                for(int i=0; i<langs.length; i++) {
                    if(i != realCol-1) {
                        int override = WandoraOptionPane.YES_OPTION;
                        if(!overrideAll && topic.getData(type, langs[i]) != null) {
                            override = WandoraOptionPane.showConfirmDialog(wandora, "Override existing "+langs[i].getDisplayName()+" occurrence?", "Override existing occurrence?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                            if(override == WandoraOptionPane.YES_TO_ALL_OPTION) overrideAll = true;
                            if(override == WandoraOptionPane.CANCEL_OPTION) return;
                        }
                        if(override == WandoraOptionPane.YES_OPTION || override == WandoraOptionPane.YES_TO_ALL_OPTION) {
                            topic.setData(type, langs[i], occurrenceText);
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            if(wandora != null) wandora.handleError(e);
        }
    }


    public void translate(int translationService) {
        try {
            Point p = getTablePoint(mouseEvent);
            boolean markTranslation = true;
            boolean overrideAll = false;
            if(mouseEvent != null && mouseEvent.isShiftDown()) markTranslation = false;
            if(mouseEvent != null && mouseEvent.isAltDown()) overrideAll = true;
            int realCol=convertColumnIndexToModel(p.y);
            int realRow=sorter.modelIndex(p.x);
            if(realRow >= 0 && realCol > 0 && realRow < types.length && realCol < langs.length+1) {
                Topic sourceLangTopic = langs[realCol-1];
                String occurrenceText = topic.getData(types[realRow], sourceLangTopic);
                Topic type = types[realRow];
                for(int i=0; i<langs.length; i++) {
                    if(i != realCol-1) {
                        Topic targetLangTopic = langs[i];
                        String translatedOccurrenceText = null;
                        if(translationService == GOOGLE_TRANSLATE) {
                            translatedOccurrenceText = GoogleTranslateBox.translate(occurrenceText, sourceLangTopic, targetLangTopic, markTranslation);
                        }
                        else if(translationService == MICROSOFT_TRANSLATE) {
                            translatedOccurrenceText = MicrosoftTranslateBox.translate(occurrenceText, sourceLangTopic, targetLangTopic, markTranslation);
                        }
                        else if(translationService == WATSON_TRANSLATE) {
                            translatedOccurrenceText = WatsonTranslateBox.translate(occurrenceText, sourceLangTopic, targetLangTopic, markTranslation);
                        }
                        if(translatedOccurrenceText != null) {
                            int override = WandoraOptionPane.YES_OPTION;
                            if(!overrideAll && topic.getData(type, targetLangTopic) != null) {
                                override = WandoraOptionPane.showConfirmDialog(wandora, "Override existing "+targetLangTopic.getDisplayName()+" occurrence?", "Override existing occurrence?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                                if(override == WandoraOptionPane.YES_TO_ALL_OPTION) overrideAll = true;
                                if(override == WandoraOptionPane.CANCEL_OPTION) return;
                            }
                            if(override == WandoraOptionPane.YES_OPTION || override == WandoraOptionPane.YES_TO_ALL_OPTION) {
                                topic.setData(type, targetLangTopic, translatedOccurrenceText);
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            if(wandora != null) wandora.handleError(e);
        }
    }

    
    // -------------------------------------------------------------------------
    
    
    public Point getTablePoint() {
        return getTablePoint(mouseEvent);
    }
    public Point getTablePoint(java.awt.event.MouseEvent e) {
        try {
            java.awt.Point p=e.getPoint();
            int row=rowAtPoint(p);
            int col=columnAtPoint(p);
            //int realCol=convertColumnIndexToModel(col);
            return new Point(row, col);
        }
        catch (Exception ex) {
            wandora.handleError(ex);
            return null;
        }
    }

    public Object getValueAt(MouseEvent e) {
        return getValueAt(getTablePoint(e));
    }
    public Object getValueAt(Point p) {
        return getValueAt(p.x, p.y);
    }
    @Override
    public Object getValueAt(int x, int y) {
        try { return getModel().getValueAt(x, convertColumnIndexToModel(y)); }
        catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
    
        
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
        //System.out.println("Mouse clicked!");
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
    
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    private class DataCellRenderer implements TableCellRenderer {
        private SimpleTextPane label;
        public DataCellRenderer(){
            label=new SimpleTextPane();
            label.setOpaque(true);
            label.setMargin(new Insets(0,0,0,0));
            Font f=label.getFont();
            label.setFont(new Font(f.getName(),Font.PLAIN,f.getSize()));
            label.setBackground(Color.WHITE);
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            label.setText(value.toString());
            column=convertColumnIndexToModel(column);            
            Color c=colors[row][column-1];
            if(c!=null) label.setForeground(c);
            else label.setForeground(Color.BLACK);
            return label;
        }
    }
        
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    private class DataCellEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.MouseListener {        
        private Topic type,version;
        private SimpleTextPane label;
        private String editedText;
        int realCol,realRow;
        

        
        
        public DataCellEditor(){
            label=new SimpleTextPane();
            label.setMargin(new Insets(0,0,0,0));
            Font f=label.getFont();
            label.setFont(new Font(f.getName(),Font.PLAIN,f.getSize()));
            label.addMouseListener(this);
        }
        
        
        
        
        public Object getCellEditorValue() {
            return editedText;
        }
        
        
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            realCol=convertColumnIndexToModel(column);
            realRow=sorter.modelIndex(row);
            type=types[realRow];
            version=langs[realCol-1];
            label.setText(value.toString());
            editedText=value.toString();;
            return label;
        }
        
        public void mouseReleased(java.awt.event.MouseEvent e) {
            if(label.contains(e.getPoint())){
                OccurrenceTextEditor ed = new OccurrenceTextEditor(wandora, true, editedText, topic, type, version);
                String typeName = wandora.getTopicGUIName(type);
                String topicName = wandora.getTopicGUIName(topic);
                ed.setTitle("Edit occurrence text of '"+typeName+"' attached to '"+topicName+"'");
                ed.setVisible(true);
                
                if(ed.acceptChanges) {
                    if(ed.text!=null) {
                        editedText=ed.text;
                        data[realRow][realCol-1]=editedText;
                    }
                    try {
                        boolean changed = applyChanges(topic, wandora);
                        if(changed) {
                            wandora.doRefresh();
                        }
                    }
                    catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            fireEditingStopped();
        }
        
        public void mouseClicked(java.awt.event.MouseEvent e) {}
        public void mouseEntered(java.awt.event.MouseEvent e) {}
        public void mouseExited(java.awt.event.MouseEvent e) {}
        public void mousePressed(java.awt.event.MouseEvent e) {}

    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    private class DeleteCellEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.ActionListener {        
        private JButton button;
        private Topic type;
        
        public DeleteCellEditor() {
            button = new SimpleButton(UIBox.getIcon("gui/icons/delete_occurrence.png"));
            button.setPreferredSize(new Dimension(16,16));
            button.setSize(new Dimension(16,16));
            button.setBounds(0, 0, 0, 0);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setBackground(UIConstants.buttonBarBackgroundColor);
            button.setFont(new Font("SansSerif",Font.PLAIN, 10));
            button.setToolTipText("Delete occurrence");
            button.addActionListener(this);
        }
        
        public Object getCellEditorValue() {
            return "";
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            type=types[sorter.modelIndex(row)];
            return button;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            try {
                DeleteOccurrence tool=new DeleteOccurrence(type);
                tool.setContext(new ApplicationContext());
                tool.execute(wandora);
            }
            catch(TopicMapException tme) {
                tme.printStackTrace();
            } // TODO EXCEPTION
        }
    }
    
    
    private class DeleteCellRenderer implements TableCellRenderer {        
        private JButton button;
        
        public DeleteCellRenderer() {
            button = new SimpleButton(UIBox.getIcon("gui/icons/delete_occurrence.png"));
            button.setPreferredSize(new Dimension(16,16));
            button.setSize(new Dimension(16,16));
            button.setBounds(0, 0, 0, 0);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setBackground(UIConstants.buttonBarBackgroundColor);
            button.setFont(new Font("SansSerif",Font.PLAIN, 10));
            button.setToolTipText("Delete occurrence");
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return button;
        }
        
    }

    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    private class TopicCellRenderer implements TableCellRenderer {
        private JLabel label;
        
        public TopicCellRenderer(){
            label=new JLabel("");
            label.setOpaque(true);
            Font f=label.getFont();
            label.setFont(new Font(f.getName(),Font.PLAIN,f.getSize()));
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Topic topic=((TableTopicWrapper)value).topic;
            if(topic==null) label.setText("--");
            else {
                try {
                    label.setText(TopicToString.toString(topic));
                }
                catch(Exception tme){
                    tme.printStackTrace(); // TODO EXCEPTION
                    label.setText("[exception retrieving occurrence]");
                }
            }
            Color c=wandora.topicHilights.get(topic);
            if(c==null) c=wandora.topicHilights.getLayerColor(topic);
            if(c!=null) label.setForeground(c);
            else label.setForeground(Color.BLACK);
            if(column > 0) {
                label.setBackground(Color.WHITE);
            }
            return label;
        }
    }
    
    
    

    private class TopicCellEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.MouseListener {        
        private Topic topic;
        private JLabel label;
        
        public TopicCellEditor(){
            label= new JLabel();
            label.setOpaque(true);
            Font f=label.getFont();
            label.setFont(new Font(f.getName(),Font.PLAIN,f.getSize()));
            label.addMouseListener(this);
        }
        
        public Object getCellEditorValue() {
            if(topic==null) return "--";
            else {
                try{
                    return TopicToString.toString(topic);
                }catch(Exception tme){
                    tme.printStackTrace(); // TODO EXCEPTION
                    return "Exception retrieving name";
                }
            }
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            topic=((TableTopicWrapper)value).topic;
            if(topic==null) label.setText("--");
            else {
                try{
                    label.setText(topic.getBaseName());
                }catch(TopicMapException tme){
                    tme.printStackTrace(); // TODO EXCEPTION
                    label.setText("Exception retrieving name");
                }
            }
            return label;
        }
        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
            fireEditingStopped();
            if(label.contains(e.getPoint()) && topic!=null)
                wandora.openTopic(topic);
        }
        
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {}
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {}
        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {}
        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {}
        
    }
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    private class DataTableModel extends AbstractTableModel {
        
        
        public int getColumnCount() {
            if(langs == null) return 0;
            return langs.length+1;
        }
        
        public int getRowCount() {
            if(types == null) return 0;
            return types.length;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(columnIndex==0){
                return wrappedTypes[rowIndex];
            }
            else if(columnIndex==langs.length+1) return "";
            else return data[rowIndex][columnIndex-1];
        }
        
        
        
        
        @Override
        public String getColumnName(int columnIndex){
            if(columnIndex==0) return "Occurrence type";
            else if(columnIndex==langs.length+1) return "";
            if(langs[columnIndex-1] == null) return "No topic";
            else {
                String name = "[null]";
                try {
                    Topic t = langs[columnIndex-1];
                    if(t != null && !t.isRemoved()) {
                        name = wandora.getTopicGUIName(t);
                        //name = t.getBaseName();
                        if(name == null) {
                            name = t.getOneSubjectIdentifier().toExternalForm();
                        }
                    }
                    if(t.isRemoved()) {
                        name = "[removed]";
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                return name;
            }
        }
        
        
        @Override
        public boolean isCellEditable(int row,int col) {
            return true;
        }
        
    }
    
    
    
    
    
    private class TableTopicWrapper {
        private Topic topic;
        private String name;
        public TableTopicWrapper(Topic t){
            topic=t;
        }
        @Override
        public String toString(){
            if(topic==null) {
                try {
                    return topic.getOneSubjectIdentifier().toExternalForm();
                }
                catch(Exception e) {
                    return "[null]";
                }
            }
            else {
                try {
                    name = topic.getBaseName();
                    if(name != null) {
                        return name;
                    }
                    else {
                        return topic.getOneSubjectIdentifier().toExternalForm();
                    }
                }
                catch(TopicMapException tme){
                    tme.printStackTrace(); // TODO EXCEPTION
                    return "[Exception retrieving name]";
                }
            }
        }

    }    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private class OccurrencesTableTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor) ||
                   support.isDataFlavorSupported(DnDHelper.topicDataFlavor);
        }

        
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            String occurrenceText = null;
            try {
                Point p = getTablePoint(mouseEvent);
                int realCol=convertColumnIndexToModel(p.y);
                int realRow=sorter.modelIndex(p.x);
                if(realRow >= 0 && realCol > 0 && realRow < types.length && realCol < langs.length+1) {
                    Topic sourceLangTopic = langs[realCol-1];
                    occurrenceText = topic.getData(types[realRow], sourceLangTopic);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            
            if(occurrenceText == null) {
                return null;
            }
            else {
                return new StringSelection(occurrenceText);
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try {
                Transferable transferable = support.getTransferable();
                Point p = support.getDropLocation().getDropPoint();
                int realRow=rowAtPoint(p);
                int realCol=columnAtPoint(p);
                if(realRow >= 0 && realCol >= 0 && realRow < types.length && realCol < langs.length+1) {
                    if(transferable.isDataFlavorSupported(DnDHelper.topicDataFlavor)) {
                        TopicMap tm=wandora.getTopicMap();
                        ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                        if(topics==null) return false;
                        
                        // DROP ON TYPE COLUMN ==> DUPLICATE/CHANGE TYPE
                        if(realCol == 0) {
                            Topic type = types[realRow];
                            if(type != null && !type.isRemoved()) {
                                Hashtable<Topic,String> os = topic.getData(type);
                                if(os != null && !os.isEmpty()) {
                                    for(Topic scope : os.keySet()) {
                                        if(scope != null && !scope.isRemoved()) {
                                            String occurrenceText = os.get(scope);
                                            for(Topic newType : topics) {
                                                if(newType != null && !newType.isRemoved()) {
                                                    topic.setData(newType, scope, occurrenceText);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                                
                        else if(realCol > 0) {
                            Topic type = types[realRow];
                            Topic scope = langs[realCol-1];
                            
                            if(type != null && scope != null && !type.isRemoved() && !scope.isRemoved()) {
                                for(Topic t : topics) {
                                    if(t != null && !t.isRemoved()) {
                                        String occurrenceText = t.getData(type, scope);
                                        if(occurrenceText != null) {
                                            topic.setData(type, scope, occurrenceText);
                                        }
                                    }
                                }
                            }
                        }
                        
                    }
                    else if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        try {
                            java.util.List<File> fileList = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                            if(fileList != null && fileList.size() > 0) {
                                for( File occurrenceFile : fileList ) {

                                    Reader inputReader = null;
                                    String content = "";

                                    String filename = occurrenceFile.getPath().toLowerCase();
                                    String extension = filename.substring(Math.max(filename.lastIndexOf(".")+1, 0));

                                    // --- handle rtf files ---
                                    if("rtf".equals(extension)) {
                                        content=Textbox.RTF2PlainText(new FileInputStream(occurrenceFile));
                                        inputReader = new StringReader(content);
                                    }

                                    // --- handle pdf files ---
                                    if("pdf".equals(extension)) {
                                        try {
                                            PDDocument doc = PDDocument.load(occurrenceFile);
                                            PDFTextStripper stripper = new PDFTextStripper();
                                            content = stripper.getText(doc);
                                            doc.close();
                                            inputReader = new StringReader(content);
                                        }
                                        catch(Exception e) {
                                            System.out.println("No PDF support!");
                                        }
                                    }

                                    // --- handle MS office files ---
                                    if("doc".equals(extension) ||
                                       "ppt".equals(extension) ||
                                       "xsl".equals(extension) ||
                                       "vsd".equals(extension) 
                                       ) {
                                            content = MSOfficeBox.getText(occurrenceFile);
                                            if(content != null) {
                                                inputReader = new StringReader(content);
                                            }
                                    }
                                    
                                    if("docx".equals(extension)) {
                                            content = MSOfficeBox.getDocxText(occurrenceFile);
                                            if(content != null) {
                                                inputReader = new StringReader(content);
                                            }
                                    }



                                    // --- handle everything else ---
                                    if(inputReader == null) {
                                        inputReader = new FileReader(occurrenceFile);
                                    }

                                    String occurrenceText = IObox.loadFile(inputReader);
                                    if(occurrenceText != null) {
                                        topic.setData(types[realRow], langs[realCol-1], occurrenceText);
                                    }
                                }
                            }
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        try {
                            Object occurrenceText = support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                            if(occurrenceText != null) {
                                topic.setData(types[realRow], langs[realCol-1], occurrenceText.toString());
                            }
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    wandora.doRefresh();
                }
            }
            catch(TopicMapException tme){tme.printStackTrace();}
            catch(Exception ce){}
            return false;
        }

    }
    
    
}
