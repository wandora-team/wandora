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
 * 
 * 
 * WandoraToolManagerPanel2.java
 *
 * Created on 16. helmikuuta 2009, 17:11
 */

package org.wandora.application;


import java.awt.*;
import java.awt.event.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import java.util.*;
import javax.swing.*;




/**
 *
 * @author  akivela
 */
public class WandoraToolManagerPanel2 extends javax.swing.JPanel {

    private Wandora wandora;
    private WandoraToolManager2 manager;
    private JDialog parent;
    private WandoraToolSet currentToolSet = null;
    private WandoraToolTree currentToolTree = null;
    
            
    /** Creates new form WandoraToolManagerPanel2 */
    public WandoraToolManagerPanel2(WandoraToolManager2 m, JDialog d, Wandora w) {
        this.wandora = w;
        this.parent = d;
        this.manager = m;
        initComponents();
        initContent();
        parent.invalidate();
    }
    
    
    
    private void initContent() {
        initToolPaths();
        initJarPaths();
        
        initAllTools();
        initToolSets();
    }
    
    
    public void initAllTools() {
        ArrayList<WandoraTool> allTools = manager.getAllTools();
        WandoraToolTable toolTable = new WandoraToolTable(wandora);
        toolTable.initialize(allTools.toArray( new WandoraTool[] {} ));

        allToolsTablePanel.removeAll();
        allToolsTablePanel.add(toolTable, BorderLayout.NORTH);
        allToolsScrollPane.setColumnHeaderView(toolTable.getTableHeader());
//        allToolsTitlePanel.removeAll();
//        allToolsTitlePanel.add(toolTable.getTableHeader(), BorderLayout.CENTER);
    }
    
    
    // ---------------------------------------------------------- TOOL PATHS ---
    
    
    public void initToolPaths() {
        ArrayList<String> toolPaths = manager.getToolPaths();
        StringBuilder pathString = new StringBuilder("");
        for( String path : toolPaths ) {
            if(path != null) {
                pathString.append(path).append("\n");
            }
        }
        pathTextPane.setText(pathString.toString());
    }
    
    public void initJarPaths(){
        ArrayList<String> jarPaths = manager.getJarPaths();
        StringBuilder pathString = new StringBuilder("");
        for( String path : jarPaths ) {
            if(path != null) {
                pathString.append(path).append("\n");
            }
        }
        jarTextPane.setText(pathString.toString());        
    }
    

    public void saveToolPaths() {
        String pathString = pathTextPane.getText();
        String[] paths = pathString.split("\n");
        String path = null;
        ArrayList<String> toolPaths = new ArrayList<String>();
        for(int i=0; i<paths.length; i++) {
            path = paths[i];
            if(path != null) {
                path = path.trim();
                if(path.length() > 0) {
                    toolPaths.add(path);
                }
            }
        }
        boolean saveToOptions = true;
        if(toolPaths.isEmpty()) {
            int a = WandoraOptionPane.showConfirmDialog(parent, "No tool paths found. Do you want to delete all path saved in Wandora options?", "Delete all paths");
            if(a == WandoraOptionPane.NO_OPTION) saveToOptions = false;
            else if(a == WandoraOptionPane.CLOSED_OPTION) saveToOptions = false;
            else if(a == WandoraOptionPane.CANCEL_OPTION) saveToOptions = false;
        }
        if(saveToOptions) {
            manager.writeToolPaths(toolPaths);
        }
    }
    
    public void saveJarPaths() {
        String pathString = jarTextPane.getText();
        String[] paths = pathString.split("\n");
        String path = null;
        ArrayList<String> jarPaths = new ArrayList<String>();
        for(int i=0; i<paths.length; i++) {
            path = paths[i];
            if(path != null) {
                path = path.trim();
                if(path.length() > 0) {
                    jarPaths.add(path);
                }
            }
        }
        boolean saveToOptions = true;
        if(jarPaths.isEmpty()) {
            int a = WandoraOptionPane.showConfirmDialog(parent, "No jar tool paths found. Do you want to delete all jar path saved in Wandora options?", "Delete all jar paths");
            if(a == WandoraOptionPane.NO_OPTION) saveToOptions = false;
            else if(a == WandoraOptionPane.CLOSED_OPTION) saveToOptions = false;
            else if(a == WandoraOptionPane.CANCEL_OPTION) saveToOptions = false;
        }
        if(saveToOptions) {
            manager.writeJarPaths(jarPaths);
        }
    }
    
    
    public void scanToolPaths() {
        saveToolPaths();
        saveJarPaths();
        manager.scanAllTools();
        initAllTools();
    }

    
    
    // ----------------------------------------------------------- TOOL SETS ---
    
    
    
    public void initToolSets() {
        initToolSets(null);
    }
    public void initToolSets(WandoraToolSet selectedSet) {
        ArrayList<WandoraToolSet> toolSets = manager.getToolSets();
        toolSetsComboBox.setEditable(false);
        toolSetsComboBox.removeAllItems();
        int selectedIndex = 0;
        int index = 0;
        
        if(toolSets.size() > 0) {
            for(WandoraToolSet toolSet : toolSets ) {
                if(toolSet.equals(selectedSet)) selectedIndex = index;
                toolSetsComboBox.addItem(toolSet.getName());
                index++;
            }
            toolSetsComboBox.setSelectedIndex(selectedIndex);
            selectToolSet(toolSets.get(selectedIndex));
        }
    }
    
    public void selectToolSet() {
        int setIndex = Math.max(0, toolSetsComboBox.getSelectedIndex());
        ArrayList<WandoraToolSet> toolSets = manager.getToolSets();
        selectToolSet(toolSets.get(Math.min(setIndex, toolSets.size()-1)));
    }
    
    public void selectToolSet(WandoraToolSet toolSet) {
        currentToolSet = toolSet;
        currentToolTree = new WandoraToolTree(wandora);
        currentToolTree.initialize(toolSet);
        toolSetContainerPanel.removeAll();
        toolSetContainerPanel.add(currentToolTree, BorderLayout.NORTH);

        toolSetContainerPanel.validate();
        toolSetsScrollPane.validate();
        toolSetsScrollPane.repaint();
    }
    

   
    
    
    public void addToolSet() {
        String name = WandoraOptionPane.showInputDialog(wandora, "Name of tool set", "", "Name of tool set");
        if(name != null) {
            WandoraToolSet set = manager.getToolSet(name);
            if(set == null) {
                set = manager.createToolSet(name);
                initToolSets(set);
            }
            else {
                WandoraOptionPane.showMessageDialog(wandora, "Tool set name should be unique. Tool set with name '"+name+"' already exists.", "Set name already exists!");
            }
        }
    }
    
    
    
    public void deleteToolSet() {
        boolean deleteAllowed = manager.allowDelete(currentToolSet);
        if(deleteAllowed) {
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Delete tool set '"+currentToolSet.getName()+"'?", "Delete tool set '"+currentToolSet.getName()+"'?", WandoraOptionPane.OK_CANCEL_OPTION);
            if(a == WandoraOptionPane.OK_OPTION) {
                manager.deleteToolSet(currentToolSet);
                initToolSets();
            }
        }
        else {
            WandoraOptionPane.showMessageDialog(wandora, "You can't delete Wandora's base tool set '"+currentToolSet.getName()+"'. Delete cancelled.");
        }
    }
    
        
    
    // -------------------------------------------------------------------------
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedPane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        toolSetsPanel = new javax.swing.JPanel();
        toolSetsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        selectSetPanel = new javax.swing.JPanel();
        toolSetsComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        newSetButton = new org.wandora.application.gui.simple.SimpleButton();
        deleteSetButton = new org.wandora.application.gui.simple.SimpleButton();
        toolSetsScrollPane = new org.wandora.application.gui.simple.SimpleScrollPane();
        toolSetContainerPanel = new javax.swing.JPanel();
        toolSetsButtonPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        addToolButton = new org.wandora.application.gui.simple.SimpleButton();
        addGroupButton = new org.wandora.application.gui.simple.SimpleButton();
        deleteButton = new org.wandora.application.gui.simple.SimpleButton();
        renameButton = new org.wandora.application.gui.simple.SimpleButton();
        allToolsPanel = new javax.swing.JPanel();
        allToolsLabel = new org.wandora.application.gui.simple.SimpleLabel();
        allToolsTitlePanel = new javax.swing.JPanel();
        allToolsScrollPane = new org.wandora.application.gui.simple.SimpleScrollPane();
        allToolsTablePanel = new javax.swing.JPanel();
        pathPanel = new javax.swing.JPanel();
        pathLabel = new org.wandora.application.gui.simple.SimpleLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        pathTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        pathButtonPanel = new javax.swing.JPanel();
        pathButtonFillerPanel = new javax.swing.JPanel();
        savePathsButton = new org.wandora.application.gui.simple.SimpleButton();
        scanButton = new org.wandora.application.gui.simple.SimpleButton();
        jarPanel = new javax.swing.JPanel();
        pathLabel1 = new org.wandora.application.gui.simple.SimpleLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jarTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        jarButtonPanel = new javax.swing.JPanel();
        pathButtonFillerPanel1 = new javax.swing.JPanel();
        saveJarsButton = new org.wandora.application.gui.simple.SimpleButton();
        scanJarsButton = new org.wandora.application.gui.simple.SimpleButton();
        bottomButtonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        closeButton = new org.wandora.application.gui.simple.SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        toolSetsPanel.setLayout(new java.awt.GridBagLayout());

        toolSetsLabel.setText("<html>User can create and edit tool sets in this tab.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        toolSetsPanel.add(toolSetsLabel, gridBagConstraints);

        selectSetPanel.setLayout(new java.awt.GridBagLayout());

        toolSetsComboBox.setPreferredSize(new java.awt.Dimension(29, 21));
        toolSetsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolSetsComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        selectSetPanel.add(toolSetsComboBox, gridBagConstraints);

        newSetButton.setText("New");
        newSetButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        newSetButton.setPreferredSize(new java.awt.Dimension(60, 21));
        newSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        selectSetPanel.add(newSetButton, gridBagConstraints);

        deleteSetButton.setText("Delete");
        deleteSetButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        deleteSetButton.setPreferredSize(new java.awt.Dimension(60, 21));
        deleteSetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSetButtonActionPerformed(evt);
            }
        });
        selectSetPanel.add(deleteSetButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        toolSetsPanel.add(selectSetPanel, gridBagConstraints);

        toolSetContainerPanel.setBackground(new java.awt.Color(255, 255, 255));
        toolSetContainerPanel.setLayout(new java.awt.BorderLayout());
        toolSetsScrollPane.setViewportView(toolSetContainerPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        toolSetsPanel.add(toolSetsScrollPane, gridBagConstraints);

        toolSetsButtonPanel.setLayout(new java.awt.GridBagLayout());

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        toolSetsButtonPanel.add(jSeparator1, gridBagConstraints);

        addToolButton.setText("Add tool");
        addToolButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        addToolButton.setPreferredSize(new java.awt.Dimension(75, 21));
        addToolButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToolButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        toolSetsButtonPanel.add(addToolButton, gridBagConstraints);

        addGroupButton.setText("Add group");
        addGroupButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        addGroupButton.setPreferredSize(new java.awt.Dimension(75, 21));
        addGroupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGroupButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        toolSetsButtonPanel.add(addGroupButton, gridBagConstraints);

        deleteButton.setText("Delete");
        deleteButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        deleteButton.setPreferredSize(new java.awt.Dimension(75, 21));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        toolSetsButtonPanel.add(deleteButton, gridBagConstraints);

        renameButton.setText("Rename");
        renameButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        renameButton.setPreferredSize(new java.awt.Dimension(75, 21));
        renameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameButtonActionPerformed(evt);
            }
        });
        toolSetsButtonPanel.add(renameButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        toolSetsPanel.add(toolSetsButtonPanel, gridBagConstraints);

        tabbedPane.addTab("Tool sets", toolSetsPanel);

        allToolsPanel.setLayout(new java.awt.GridBagLayout());

        allToolsLabel.setText("<html>All known tools are listed here. The list contains also unfinished, buggy and deprecated tools. Running such tool may cause exceptions and unpredictable behaviour. We suggest you don't run tools listed here unless you really know what you are doing.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        allToolsPanel.add(allToolsLabel, gridBagConstraints);

        allToolsTitlePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        allToolsPanel.add(allToolsTitlePanel, gridBagConstraints);

        allToolsTablePanel.setLayout(new java.awt.BorderLayout());
        allToolsScrollPane.setViewportView(allToolsTablePanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        allToolsPanel.add(allToolsScrollPane, gridBagConstraints);

        tabbedPane.addTab("All tools", allToolsPanel);

        pathPanel.setLayout(new java.awt.GridBagLayout());

        pathLabel.setText("<html>This tab is used to view and edit class paths Wandora scans for tool classes.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        pathPanel.add(pathLabel, gridBagConstraints);

        jScrollPane1.setViewportView(pathTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pathPanel.add(jScrollPane1, gridBagConstraints);

        pathButtonPanel.setPreferredSize(new java.awt.Dimension(100, 23));
        pathButtonPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout pathButtonFillerPanelLayout = new javax.swing.GroupLayout(pathButtonFillerPanel);
        pathButtonFillerPanel.setLayout(pathButtonFillerPanelLayout);
        pathButtonFillerPanelLayout.setHorizontalGroup(
            pathButtonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pathButtonFillerPanelLayout.setVerticalGroup(
            pathButtonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        pathButtonPanel.add(pathButtonFillerPanel, new java.awt.GridBagConstraints());

        savePathsButton.setText("Save");
        savePathsButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        savePathsButton.setPreferredSize(new java.awt.Dimension(60, 21));
        savePathsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                savePathsButtonMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        pathButtonPanel.add(savePathsButton, gridBagConstraints);

        scanButton.setText("Scan");
        scanButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        scanButton.setPreferredSize(new java.awt.Dimension(60, 21));
        scanButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                scanButtonMousePressed(evt);
            }
        });
        pathButtonPanel.add(scanButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        pathPanel.add(pathButtonPanel, gridBagConstraints);

        tabbedPane.addTab("Paths", pathPanel);

        jarPanel.setLayout(new java.awt.GridBagLayout());

        pathLabel1.setText("<html>This tab is used to view and edit paths to JAR files or directories containing JAR files where Wandora scans for tool classes.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        jarPanel.add(pathLabel1, gridBagConstraints);

        jScrollPane2.setViewportView(jarTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jarPanel.add(jScrollPane2, gridBagConstraints);

        jarButtonPanel.setPreferredSize(new java.awt.Dimension(100, 23));
        jarButtonPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout pathButtonFillerPanel1Layout = new javax.swing.GroupLayout(pathButtonFillerPanel1);
        pathButtonFillerPanel1.setLayout(pathButtonFillerPanel1Layout);
        pathButtonFillerPanel1Layout.setHorizontalGroup(
            pathButtonFillerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pathButtonFillerPanel1Layout.setVerticalGroup(
            pathButtonFillerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jarButtonPanel.add(pathButtonFillerPanel1, new java.awt.GridBagConstraints());

        saveJarsButton.setText("Save");
        saveJarsButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        saveJarsButton.setPreferredSize(new java.awt.Dimension(60, 21));
        saveJarsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                saveJarsButtonMousePressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jarButtonPanel.add(saveJarsButton, gridBagConstraints);

        scanJarsButton.setText("Scan");
        scanJarsButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        scanJarsButton.setPreferredSize(new java.awt.Dimension(60, 21));
        scanJarsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                scanJarsButtonMousePressed(evt);
            }
        });
        jarButtonPanel.add(scanJarsButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        jarPanel.add(jarButtonPanel, gridBagConstraints);

        tabbedPane.addTab("JAR paths", jarPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tabbedPane, gridBagConstraints);

        bottomButtonPanel.setPreferredSize(new java.awt.Dimension(100, 23));
        bottomButtonPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout buttonFillerPanelLayout = new javax.swing.GroupLayout(buttonFillerPanel);
        buttonFillerPanel.setLayout(buttonFillerPanelLayout);
        buttonFillerPanelLayout.setHorizontalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        buttonFillerPanelLayout.setVerticalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        bottomButtonPanel.add(buttonFillerPanel, gridBagConstraints);

        closeButton.setText("Close");
        closeButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        closeButton.setMaximumSize(new java.awt.Dimension(70, 23));
        closeButton.setMinimumSize(new java.awt.Dimension(70, 23));
        closeButton.setPreferredSize(new java.awt.Dimension(70, 23));
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                closeButtonMousePressed(evt);
            }
        });
        bottomButtonPanel.add(closeButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(bottomButtonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void closeButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeButtonMousePressed
    parent.setVisible(false);
}//GEN-LAST:event_closeButtonMousePressed

private void scanButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scanButtonMousePressed
    scanToolPaths();
}//GEN-LAST:event_scanButtonMousePressed

private void savePathsButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_savePathsButtonMousePressed
    saveToolPaths();
}//GEN-LAST:event_savePathsButtonMousePressed

private void toolSetsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolSetsComboBoxActionPerformed
    this.selectToolSet();
}//GEN-LAST:event_toolSetsComboBoxActionPerformed

private void addToolButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToolButtonActionPerformed
    currentToolTree.actionPerformed(new ActionEvent(this, 0, "Add tool"));
}//GEN-LAST:event_addToolButtonActionPerformed

private void newSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSetButtonActionPerformed
    addToolSet();
}//GEN-LAST:event_newSetButtonActionPerformed

private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
    currentToolTree.actionPerformed(new ActionEvent(this, 0, "Delete"));
}//GEN-LAST:event_deleteButtonActionPerformed

private void deleteSetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSetButtonActionPerformed
    deleteToolSet();
}//GEN-LAST:event_deleteSetButtonActionPerformed

private void addGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGroupButtonActionPerformed
    currentToolTree.actionPerformed(new ActionEvent(this, 0, "Add group"));
}//GEN-LAST:event_addGroupButtonActionPerformed

private void renameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameButtonActionPerformed
    currentToolTree.actionPerformed(new ActionEvent(this, 0, "Rename"));
}//GEN-LAST:event_renameButtonActionPerformed

    private void saveJarsButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveJarsButtonMousePressed
        saveJarPaths();
    }//GEN-LAST:event_saveJarsButtonMousePressed

    private void scanJarsButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_scanJarsButtonMousePressed
        scanToolPaths();
    }//GEN-LAST:event_scanJarsButtonMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addGroupButton;
    private javax.swing.JButton addToolButton;
    private javax.swing.JLabel allToolsLabel;
    private javax.swing.JPanel allToolsPanel;
    private javax.swing.JScrollPane allToolsScrollPane;
    private javax.swing.JPanel allToolsTablePanel;
    private javax.swing.JPanel allToolsTitlePanel;
    private javax.swing.JPanel bottomButtonPanel;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton deleteSetButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel jarButtonPanel;
    private javax.swing.JPanel jarPanel;
    private javax.swing.JTextPane jarTextPane;
    private javax.swing.JButton newSetButton;
    private javax.swing.JPanel pathButtonFillerPanel;
    private javax.swing.JPanel pathButtonFillerPanel1;
    private javax.swing.JPanel pathButtonPanel;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel pathLabel1;
    private javax.swing.JPanel pathPanel;
    private javax.swing.JTextPane pathTextPane;
    private javax.swing.JButton renameButton;
    private javax.swing.JButton saveJarsButton;
    private javax.swing.JButton savePathsButton;
    private javax.swing.JButton scanButton;
    private javax.swing.JButton scanJarsButton;
    private javax.swing.JPanel selectSetPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel toolSetContainerPanel;
    private javax.swing.JPanel toolSetsButtonPanel;
    private javax.swing.JComboBox toolSetsComboBox;
    private javax.swing.JLabel toolSetsLabel;
    private javax.swing.JPanel toolSetsPanel;
    private javax.swing.JScrollPane toolSetsScrollPane;
    // End of variables declaration//GEN-END:variables

}
