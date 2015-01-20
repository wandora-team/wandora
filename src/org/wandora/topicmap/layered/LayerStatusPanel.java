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
 * 
 *
 * LayerStatus.java
 *
 * Created on 20. lokakuuta 2005, 13:29
 */

package org.wandora.topicmap.layered;

import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.tools.importers.TopicMapImport;
import org.wandora.topicmap.*;
import org.wandora.topicmap.memory.*;
import org.wandora.topicmap.database.*;
import org.wandora.topicmap.remote.*;
import org.wandora.utils.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.net.*;



/**
 * 
 * @deprecated
 * @author  olli
 */
public class LayerStatusPanel extends javax.swing.JPanel implements ActionListener, ChangeListener {
    
    public static final String VISIBLE_ICON = "gui/icons/view.png";
    public static final String INVISIBLE_ICON = "gui/icons/view_no.png";
    
    public static final String LOCKED_ICON = "gui/icons/locked.png";
    public static final String UNLOCKED_ICON = "gui/icons/unlocked.png";
    
    public static final String MEMORY_TYPE = "gui/icons/layerinfo/layer_type_memory.png";
    public static final String DATABASE_TYPE = "gui/icons/layerinfo/layer_type_database.png";
    public static final String REMOTE_TYPE = "gui/icons/layerinfo/layer_type_remote.png";
    public static final String TOPICMAP_CHANGED = "gui/icons/layerinfo/topicmap_changed.png";
    
    private Layer layer;
    private LayerStack layerStack;
    private LayerControlPanel controlPanel;
    private Wandora wandora;

    private Color brokenColor = new java.awt.Color(192,32,32);
    private Color brokenColorSelected = new java.awt.Color(160,96,96);
    
    
    
    
    /** Creates new form LayerStatusPanel */
    public LayerStatusPanel(Wandora w, Layer l,LayerStack layerStack,LayerControlPanel controlPanel) {
        this.layer=l;
        this.layerStack=layerStack;
        this.controlPanel=controlPanel;
        this.wandora = w;
        
        initComponents();
        setIndent(0);
        
        if(l.getBroken()) {
            dragPanel.setBackground(brokenColor);
            nameLabel.setBackground(brokenColor);
            infoPanel.setBackground(brokenColor);
        }
        else {
            dragPanel.setBackground(UIConstants.defaultInactiveBackground);
            nameLabel.setBackground(UIConstants.defaultInactiveBackground);
            infoPanel.setBackground(UIConstants.defaultInactiveBackground);
        }
        
        visibleToggle.setSelected(l.isVisible());
        lockToggle.setSelected(l.isReadOnly());
        if(nameLabel instanceof SimpleLabelField) {
            SimpleLabelField labelField = (SimpleLabelField) nameLabel;
            labelField.setText(l.getName());
            labelField.addChangeListener(this);
        }
        if(l==layerStack.getSelectedLayer()) {
            if(l.getBroken()){
                dragPanel.setBackground(brokenColorSelected);
                nameLabel.setBackground(brokenColorSelected);
                infoPanel.setBackground(brokenColorSelected);
            }
            else {
                dragPanel.setBackground(UIConstants.defaultActiveBackground);
                nameLabel.setBackground(UIConstants.defaultActiveBackground);
                infoPanel.setBackground(UIConstants.defaultActiveBackground);
            }
        }
        setComponentPopupMenu(getContextMenu());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dragPanel.setTransferHandler(new LayerTransferHandler());
        initInfoIcons();
    }   
    
    
    
    
    public void setIndent(int indent){
        java.awt.Dimension d=new java.awt.Dimension(indent*10,1);
        indentPanel.setPreferredSize(d);
        indentPanel.setMinimumSize(d);
        indentPanel.setMaximumSize(d);
    }
    
    public void initInfoIcons() {
        infoPanel.removeAll();
        TopicMap topicMap = layer.getTopicMap();
        JLabel iconLabel = null;

        boolean changed=false;
        try {
            changed=topicMap.isTopicMapChanged();
        }
        catch(TopicMapException tme){tme.printStackTrace();} // TODO EXCEPTION
        
        if(changed) {
            iconLabel = new JLabel(UIBox.getIcon(LayerStatusPanel.TOPICMAP_CHANGED));
            iconLabel.setToolTipText("Layer's topic map has changed!");
            infoPanel.add(iconLabel);
        }
        
        
        if(topicMap instanceof RemoteTopicMap) {
            iconLabel = new JLabel(UIBox.getIcon(LayerStatusPanel.REMOTE_TYPE));
            iconLabel.setToolTipText("Layer contains remote topic map!");
            infoPanel.add(iconLabel);
        }
        else if(topicMap instanceof DatabaseTopicMap) {
            iconLabel = new JLabel(UIBox.getIcon(LayerStatusPanel.DATABASE_TYPE));
            iconLabel.setToolTipText("Layer contains database topic map!");
            infoPanel.add(iconLabel);
        }
        else if(topicMap instanceof TopicMapImpl) {
            iconLabel = new JLabel(UIBox.getIcon(LayerStatusPanel.MEMORY_TYPE));
            iconLabel.setToolTipText("Layer contains local memory topic map!");
            infoPanel.add(iconLabel);
        }
    }
    
    
    
    
    public javax.swing.JPopupMenu getContextMenu(){
        /*
        TopicMapType type=TopicMapTypeManager.getType(layer.getTopicMap());
        javax.swing.JMenuItem[] m=type.getTopicMapMenu(layer.getTopicMap(),admin);
        
        Object[] menu=UIBox.fillMenuTemplate("___TOPICMAPMENU___",m,controlPanel.menuStructure);
        
        JMenu importToLayerMenu = new WandoraMenu("Merge to layer", null);
        JMenu generateLayerMenu = new WandoraMenu("Generate to layer", null);
        JMenu exportLayerMenu = new WandoraMenu("Export layer", null);
        admin.toolManager.getImportMenu(importToLayerMenu);
        admin.toolManager.getGeneratorMenu(generateLayerMenu);
        admin.toolManager.getExportMenu(exportLayerMenu);
        
        menu=UIBox.fillMenuTemplate("___IMPORTMENU___", new Object[] { importToLayerMenu }, menu);
        menu=UIBox.fillMenuTemplate("___GENERATEMENU___", new Object[] { generateLayerMenu }, menu);
        menu=UIBox.fillMenuTemplate("___EXPORTMENU___", new Object[] { exportLayerMenu }, menu);
        return UIBox.makePopupMenu(menu, admin);
         */
        return null;
    }

    
    public Layer getLayer() {
        return layer;
    }
    
    
    public void toggleVisibility() {
        if(controlPanel.layersChanging()){
            layer.setVisible(visibleToggle.isSelected());
            controlPanel.resetLayers();
        }
        else{
            // Does this raise an event, if it does it will cause a stack overflow
            visibleToggle.setSelected(!visibleToggle.isSelected());
        }
    }
    
    
    
    public void toggleLock() {
//        if(controlPanel.layersChanging()){
            layer.setReadOnly(!layer.isReadOnly());
            lockToggle.setSelected(layer.isReadOnly());
//            controlPanel.resetLayers();
//        }
//        else{
//            //lockToggle.setSelected(!lockToggle.isSelected());
//        }
    }
    
    
    
    public void editName() {
        if(nameLabel instanceof SimpleLabelField) {
            ((SimpleLabelField) nameLabel).setUpGui(SimpleLabelField.FIELD);
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dragPanel = new javax.swing.JPanel();
        indentPanel = new javax.swing.JPanel();
        visibleToggle = new org.wandora.application.gui.simple.SimpleToggleButton(this.VISIBLE_ICON, this.INVISIBLE_ICON, true);
        lockToggle = new org.wandora.application.gui.simple.SimpleToggleButton(this.LOCKED_ICON, this.UNLOCKED_ICON, false);
        nameLabel = new org.wandora.application.gui.simple.SimpleLabelField();
        infoPanel = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(2147483647, 30));
        setMinimumSize(new java.awt.Dimension(76, 30));
        setPreferredSize(new java.awt.Dimension(76, 30));
        setLayout(new java.awt.BorderLayout());

        dragPanel.setInheritsPopupMenu(true);
        dragPanel.setLayout(new java.awt.GridBagLayout());

        indentPanel.setOpaque(false);
        indentPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        dragPanel.add(indentPanel, gridBagConstraints);

        visibleToggle.setActionCommand("toggleVisibility");
        visibleToggle.setBorder(null);
        visibleToggle.setMargin(new java.awt.Insets(0, 0, 0, 0));
        visibleToggle.setPreferredSize(new java.awt.Dimension(16, 16));
        visibleToggle.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(1, 8, 1, 1);
        dragPanel.add(visibleToggle, gridBagConstraints);

        lockToggle.setActionCommand("toggleLock");
        lockToggle.setBorder(null);
        lockToggle.setMargin(new java.awt.Insets(0, 0, 0, 0));
        lockToggle.setPreferredSize(new java.awt.Dimension(16, 16));
        lockToggle.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(1, 4, 1, 1);
        dragPanel.add(lockToggle, gridBagConstraints);

        nameLabel.setInheritsPopupMenu(true);
        nameLabel.setMinimumSize(new java.awt.Dimension(10, 20));
        nameLabel.setPreferredSize(new java.awt.Dimension(10, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        dragPanel.add(nameLabel, gridBagConstraints);

        infoPanel.setPreferredSize(new java.awt.Dimension(20, 20));
        infoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 2);
        dragPanel.add(infoPanel, gridBagConstraints);

        add(dragPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dragPanel;
    private javax.swing.JPanel indentPanel;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JToggleButton lockToggle;
    private javax.swing.JPanel nameLabel;
    private javax.swing.JToggleButton visibleToggle;
    // End of variables declaration//GEN-END:variables
 
    
    
    public void actionPerformed(ActionEvent event) {
        String actionCommand = event.getActionCommand();
        //System.out.println("actionCommand " + actionCommand + " performed!");
        
        if("ToggleVisibility".equalsIgnoreCase(actionCommand)) {
            toggleVisibility();
        }
        
        else if("ToggleLock".equalsIgnoreCase(actionCommand)) {
            toggleLock();
        }
    }
   
    
    public void stateChanged(ChangeEvent e) {
        //System.out.println("STATE CHANGED!");
        try {
            if(e.getSource().equals(nameLabel)) {
                if(nameLabel instanceof SimpleLabelField) {
                    String newName = ((SimpleLabelField) nameLabel).getText();
                    if(layerStack.getLayer(newName) == null) {
                        layer.setName(newName);
                    }
                    else {
                        WandoraOptionPane.showMessageDialog(this, "Layer '" + newName + "' already exists! Please give layer another name.", "Layer already exists", WandoraOptionPane.WARNING_MESSAGE);
                        ((SimpleLabelField) nameLabel).setUpGui(SimpleLabelField.FIELD);
                    }
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    

    /* -------------------------------------------------------------------------
     *
     * Drag and drop of LayerStatusPanel is managed in LayerTree.
     * Next code fragment is not used!
     *
     * -------------------------------------------------------------------------
     */
    
    private class LayerTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
                   support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
//            return DnDHelper.makeTopicTableTransferable(data,getSelectedRows(),getSelectedColumns());
            return null;
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
                if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    java.util.List<File> fileList = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if(fileList != null && fileList.size() > 0) {
                        Layer l = getLayer();
                        processFileList(fileList, l);
                    }
                }
                
                if(transferable.isDataFlavorSupported(DnDBox.uriListFlavor)) {
                    java.util.List<URI> uriList = (java.util.List<URI>) support.getTransferable().getTransferData(DnDBox.uriListFlavor);
                    if(uriList != null && uriList.size() > 0) {
                        Layer l = getLayer();
                        processURIList(uriList, l);
                    }
                }
                
                else if(transferable.isDataFlavorSupported(DnDHelper.topicDataFlavor)) {
                    TopicMap source = wandora.getTopicMap();
                    Layer l = getLayer();
                    TopicMap target = l.getTopicMap();
                    if(source != null && target != null) {
                        ArrayList<Topic> topics=DnDHelper.getTopicList(support, source, true);
                        if(topics==null) return false;
                        for(Topic t : topics) {
                            if(t != null && !t.isRemoved()) {
                                //target.copyTopicIn(t, false);
                                Topic nt = target.createTopic();
                                nt.addSubjectIdentifier(t.getOneSubjectIdentifier());
                            }
                        }
                        wandora.doRefresh();
                    }
                }
                
                else if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    // TODO
                }
            }
            catch(TopicMapException tme){tme.printStackTrace();}
            catch(CancelledException ce){}
            catch(Exception e){
                e.printStackTrace();
            }
            return false;
        }
    }
    
    
    
    private void processFileList(java.util.List<File> files, Layer dropLayer) throws Exception {
        ArrayList<WandoraTool> importTools=WandoraToolManager.getImportTools(files, org.wandora.application.tools.importers.AbstractImportTool.TOPICMAP_DIRECT_MERGE);
        for(WandoraTool t : importTools){
            if(t==null){
                WandoraOptionPane.showMessageDialog(wandora, "A file with unsupported file type dropped! Wandora supports wpr, xtm, ltm, jtm, rdf(s), n3, and obo files. Extractors may support other file types also.", "Unsupported file type", WandoraOptionPane.ERROR_MESSAGE);                
                break;
            }
        }
        //System.out.println("drop context == " + dropContext);
        ActionEvent fakeEvent = new ActionEvent(dropLayer != null ? dropLayer : wandora, 0, "merge");
        org.wandora.application.tools.ChainExecuter chainExecuter = new org.wandora.application.tools.ChainExecuter(importTools);
        chainExecuter.execute(wandora, fakeEvent);
    }
    
    
    
    
    private void processURIList(java.util.List<URI> uris, Layer dropLayer) throws Exception {
        ArrayList<WandoraTool> importTools=WandoraToolManager.getURIImportTools(uris, org.wandora.application.tools.importers.AbstractImportTool.TOPICMAP_DIRECT_MERGE);
        for(WandoraTool t : importTools){
            if(t==null){
                WandoraOptionPane.showMessageDialog(wandora, "An URI with unsupported file type dropped! Wandora supports wpr, xtm, ltm, jtm, rdf(s), n3, and obo files. Extractors may support other file types also.", "Unsupported file type", WandoraOptionPane.ERROR_MESSAGE);                
                break;
            }
        }
        //System.out.println("drop context == " + dropContext);
        ActionEvent fakeEvent = new ActionEvent(dropLayer != null ? dropLayer : wandora, 0, "merge");
        org.wandora.application.tools.ChainExecuter chainExecuter = new org.wandora.application.tools.ChainExecuter(importTools);
        chainExecuter.execute(wandora, fakeEvent);
    }

}
