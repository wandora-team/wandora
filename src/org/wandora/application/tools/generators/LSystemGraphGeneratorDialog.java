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
 * EdgeGeneratorDialog.java
 *
 * Created on 2008-09-22, 13:14
 */

package org.wandora.application.tools.generators;


import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.utils.*;
import org.wandora.utils.Tuples.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;






/**
 *
 * @author  akivela
 */
public class LSystemGraphGeneratorDialog extends javax.swing.JDialog {
    public static final int L_SYSTEM = 11;
    public static final int RAW_RESULT = 12;
    
    
    private Wandora parent = null;
    private WandoraTool parentTool = null;
    private boolean wasAccepted = false;
    
    public static final String optionsPrefix = "lsystems";
    private ArrayList<T2<String,String>> lsystems = new ArrayList<T2<String,String>>();
    
    
    
    
    /** Creates new form AbstractExtractorDialog */
    public LSystemGraphGeneratorDialog(Wandora admin, WandoraTool parentTool, boolean modal) {
        super(admin, modal);
        this.parent = admin;
        initComponents();
        initialize(parentTool);
        lSystemComboBox.setEditable(false);
        restoreStoredLSystems();
    }
    
    
    
    public void initialize(WandoraTool parentTool) {
        this.parentTool = parentTool;
        wasAccepted = false;
        ((SimpleTextPane) fileTextPane).dropFileNames(true);
        ((SimpleTextPane) fileTextPane).setLineWrap(false);
        ((SimpleTextPane) urlTextPane).setLineWrap(false);

        setTitle("L-system generator");
        setSize(700,500);
        if(parent != null) parent.centerWindow(this);
    }
    
    
    
    // -------------------------------------------------------------------------
    
    

    public void restoreStoredLSystems() {
        if(parent == null) return;
        Options options = parent.getOptions();
        if(options == null) return;
        int i=0;
        String lsystemName = null;
        String lsystemSystem = null;
        do {
            lsystemName = options.get(optionsPrefix + ".lsystem[" + i +"].name");
            lsystemSystem = options.get(optionsPrefix + ".lsystem[" + i +"].system");
            if(lsystemName != null && lsystemSystem != null) {
                lsystems.add(new T2(lsystemName, lsystemSystem));
                lSystemComboBox.addItem(lsystemName);
            }
            i++;
        }
        while(lsystemName != null && lsystemSystem != null && i<1000);
    }
    
    public void restoreLSystem() {
        try {
            int lsystemIndex =  lSystemComboBox.getSelectedIndex();
            if(lsystems.size() > lsystemIndex) {
                T2 namedLSystem = (T2) lsystems.get(lsystemIndex);
                if(namedLSystem != null) {
                    String lsystemSystem = (String) namedLSystem.e2;
                    if(lsystemSystem != null) {
                        lSystemTextPane.setText(lsystemSystem);
                    }
                }
            }
        }
        catch(Exception e) {
            parent.handleError(e);
        }
    }
    
    
    
    public void storeLSystem() {
        try {
            String newLSystemSystem = lSystemTextPane.getText();
            String newLSystemName = WandoraOptionPane.showInputDialog(parent, "Name of created L-system?", "", "Name of created L-system?");
            if(newLSystemSystem != null && newLSystemName.length()>0) {
                lSystemComboBox.addItem(newLSystemName);
                lsystems.add(new T2(newLSystemName, newLSystemSystem));
                lSystemComboBox.setSelectedIndex(lSystemComboBox.getItemCount()-1);
                if(parent != null && parent.getOptions() != null) {
                    Options options = parent.getOptions();
                    String lsystemSystem = null;
                    String lsystemName = null;
                    int i=0;
                    do {
                        lsystemName = options.get(optionsPrefix + ".lsystem[" + i +"].name");
                        lsystemSystem = options.get(optionsPrefix + ".lsystem[" + i +"].system");
                        i++;
                    }
                    while(lsystemName != null && lsystemSystem != null && i<1000);
                    options.put(optionsPrefix + ".lsystem[" + i +"].name", newLSystemName);
                    options.put(optionsPrefix + ".lsystem[" + i +"].system", newLSystemSystem);

                }
                else {
                    WandoraOptionPane.showMessageDialog(parent, "Unable to store L-system to application options!", WandoraOptionPane.WARNING_MESSAGE);
                }
            }
            else {
                WandoraOptionPane.showMessageDialog(parent, "No name given. L-system was not stored!", WandoraOptionPane.WARNING_MESSAGE);
            }
        }
        catch(Exception e) {
            parent.handleError(e);
        }
    }
    
    
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void setAccepted(boolean accepted) {
        this.wasAccepted = accepted;
    }
    
    public boolean wasAccepted() {
        return wasAccepted;
    }
    

    public int getDepth() {
        try {
            return Integer.parseInt(this.depthTextField.getText());
        }
        catch(Exception e) {
            return 0;
        }
    }
    
    
    public int getContentType() {
        Component selectedComponent = tabbedSourcePane.getSelectedComponent();
        if(lSystemPanel.equals(selectedComponent)) {
            return L_SYSTEM;
        }
        else if(rawPanel.equals(selectedComponent)) {
            return RAW_RESULT;
        }
        return 0;
    }
    
    
    
    // --- CONTENT ---
    public String getContent() {
        Component selectedComponent = tabbedSourcePane.getSelectedComponent();
        
        if(lSystemPanel.equals(selectedComponent)) {
            return lSystemTextPane.getText();
        }
        else if(rawPanel.equals(selectedComponent)) {
            return rawTextPane.getText();
        }
        else if(filePanel.equals(selectedComponent)) {
            File[] files = getFileSources();
            StringBuilder sb = new StringBuilder("");
            for(int i=0; i<files.length; i++) {
                try {
                    sb.append(IObox.loadFile(files[i]));
                }
                catch(Exception e) {
                    parentTool.log(e);
                }
            }
        }
        else if(urlPanel.equals(selectedComponent)) {
            String[] urls = getURLSources();
            StringBuilder sb = new StringBuilder("");
            for(int i=0; i<urls.length; i++) {
                try {
                    sb.append(IObox.doUrl(new URL(urls[i])));
                }
                catch(Exception e) {
                    parentTool.log(e);
                }
            }
        }
        
        return null;
    }
    
    
    
    
    
    // --- FILE SOURCE ---
    public File[] getFileSources() {
        String input = fileTextPane.getText();
        String[] filenames = splitText(input);
        ArrayList<File> files = new ArrayList<File>();
        File f = null;
        for(int i=0; i<filenames.length; i++) {
            f = new File(filenames[i]);
            if(f.exists()) files.add(f);
            else {
                if(parentTool != null) parentTool.log("File '"+filenames[i]+"' not found!");
            }
        }
        return files.toArray( new File[] {} );
    }
    
    
    
    // --- URL SOURCE ---
    public String[] getURLSources() {
        String input = urlTextPane.getText();
        String[] urls = splitText(input);
        return urls;
    }
    
    
    
    private String[] splitText(String str) {
        if(str == null) return null;
        if(str.indexOf("\n") != -1) {
            String[] s = str.split("\n");
            for(int i=0; i<s.length; i++) {
                s[i] = s[i].trim();
            }
            return s;
        }
        else {
            return new String[] { str.trim() };
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private void selectFiles() {
        SimpleFileChooser chooser = UIConstants.getFileChooser();
        chooser.setMultiSelectionEnabled(true);
        //chooser.setDialogTitle(getGUIText(SELECT_DIALOG_TITLE));
        chooser.setApproveButtonText("Select");
        chooser.setFileSelectionMode(SimpleFileChooser.FILES_AND_DIRECTORIES);
        //if(accessoryPanel != null) { chooser.setAccessory(accessoryPanel); }

        if(chooser.open(parent, SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            File f = null;
            String fs = "";
            for(int i=0; i<files.length; i++) {
                f = files[i];
                fs = fs + f.getAbsolutePath();
                if(i<files.length-1) fs = fs + "\n";
            }
            String s = fileTextPane.getText();
            if(s == null || s.length() == 0) s = fs;
            else s = s + "\n" + fs;
            fileTextPane.setText(s);
        }
    }
    
    
    private void selectContextSLFiles() {
        if(parentTool == null) return;
        Context context = parentTool.getContext();
        Iterator iter = context.getContextObjects();
        Object o = null;
        Topic t = null;
        Locator locator = null;
        StringBuilder sb = new StringBuilder("");
        while(iter.hasNext()) {
            try {
                o = iter.next();
                if(o == null) continue;
                if(o instanceof Topic) {
                    t = (Topic) o;
                    if(!t.isRemoved()) {
                        locator = t.getSubjectLocator();
                        if(locator != null) {
                            String locatorStr = locator.toExternalForm();
                            if(locatorStr.startsWith("file:")) {
                                locatorStr = IObox.getFileFromURL(locatorStr);
                                sb.append(locatorStr).append("\n");
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                parentTool.log(e);
            }
        }
        String s = urlTextPane.getText();
        if(s == null || s.length() == 0) s = sb.toString();
        else s = s + "\n" + sb.toString();
        fileTextPane.setText(s);
    }
    
    
    private void selectContextSLs() {
        if(parentTool == null) return;
        Context context = parentTool.getContext();
        Iterator iter = context.getContextObjects();
        Object o = null;
        Topic t = null;
        Locator locator = null;
        StringBuilder sb = new StringBuilder("");
        while(iter.hasNext()) {
            try {
                o = iter.next();
                if(o == null) continue;
                if(o instanceof Topic) {
                    t = (Topic) o;
                    if(!t.isRemoved()) {
                        locator = t.getSubjectLocator();
                        if(locator != null) {
                            String locatorStr = locator.toExternalForm();
                            sb.append(locatorStr).append("\n");
                        }
                    }
                }
            }
            catch(Exception e) {
                parentTool.log(e);
            }
        }
        String s = urlTextPane.getText();
        if(s == null || s.length() == 0) s = sb.toString();
        else s = s + "\n" + sb.toString();
        urlTextPane.setText(s);
    }
    
    

    private void selectContextSIs() {
        if(parentTool == null) return;
        Context context = parentTool.getContext();
        Iterator iter = context.getContextObjects();
        Object o = null;
        Topic t = null;
        Locator locator = null;
        StringBuilder sb = new StringBuilder("");
        while(iter.hasNext()) {
            try {
                o = iter.next();
                if(o == null) continue;
                if(o instanceof Topic) {
                    t = (Topic) o;
                    if(!t.isRemoved()) {
                        Collection<Locator> ls = t.getSubjectIdentifiers();
                        Iterator<Locator> ils = ls.iterator();
                        while(ils.hasNext()) {
                            locator = ils.next();
                            if(locator != null) {
                                String locatorStr = locator.toExternalForm();
                                sb.append(locatorStr).append("\n");
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                parentTool.log(e);
            }
        }
        String s = urlTextPane.getText();
        if(s == null || s.length() == 0) s = sb.toString();
        else s = s + "\n" + sb.toString();
        urlTextPane.setText(s);
    }
    
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        urlPanel = new javax.swing.JPanel();
        urlLabel = new org.wandora.application.gui.simple.SimpleLabel();
        urlScrollPane = new javax.swing.JScrollPane();
        urlTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        urlButtonPanel = new javax.swing.JPanel();
        urlGetSIButton = new org.wandora.application.gui.simple.SimpleButton();
        urlGetSLButton = new org.wandora.application.gui.simple.SimpleButton();
        urlClearButton = new org.wandora.application.gui.simple.SimpleButton();
        filePanel = new javax.swing.JPanel();
        fileLabel = new org.wandora.application.gui.simple.SimpleLabel();
        fileScrollPane = new javax.swing.JScrollPane();
        fileTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        fileButtonPanel = new javax.swing.JPanel();
        fileBrowseButton = new org.wandora.application.gui.simple.SimpleButton();
        fileGetSLButton = new org.wandora.application.gui.simple.SimpleButton();
        fileClearButton = new org.wandora.application.gui.simple.SimpleButton();
        tabbedSourcePane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        lSystemPanel = new javax.swing.JPanel();
        lSystemLabel = new org.wandora.application.gui.simple.SimpleLabel();
        selectLSystemPanel = new javax.swing.JPanel();
        lSystemComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        saveLSystemButton = new org.wandora.application.gui.simple.SimpleButton();
        lSystemScrollPane = new javax.swing.JScrollPane();
        lSystemTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        depthPanel = new javax.swing.JPanel();
        depthTextField = new org.wandora.application.gui.simple.SimpleField();
        depthLabel = new org.wandora.application.gui.simple.SimpleLabel();
        rawPanel = new javax.swing.JPanel();
        rawLabel = new org.wandora.application.gui.simple.SimpleLabel();
        rawScrollPane = new javax.swing.JScrollPane();
        rawTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        buttonPanel = new javax.swing.JPanel();
        infoButton = new SimpleButton();
        fillerPanel = new javax.swing.JPanel();
        generateButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        urlPanel.setLayout(new java.awt.GridBagLayout());

        urlLabel.setText("<html>This tab is used to address URL resources with edge data. Please write URL addresses below or get subject identifiers of context topics.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        urlPanel.add(urlLabel, gridBagConstraints);

        urlScrollPane.setPreferredSize(new java.awt.Dimension(10, 100));
        urlScrollPane.setViewportView(urlTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        urlPanel.add(urlScrollPane, gridBagConstraints);

        urlButtonPanel.setLayout(new java.awt.GridBagLayout());

        urlGetSIButton.setText("Get SIs");
        urlGetSIButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        urlGetSIButton.setPreferredSize(new java.awt.Dimension(60, 21));
        urlGetSIButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                urlGetSIButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        urlButtonPanel.add(urlGetSIButton, gridBagConstraints);

        urlGetSLButton.setText("Get SLs");
        urlGetSLButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        urlGetSLButton.setPreferredSize(new java.awt.Dimension(60, 21));
        urlGetSLButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                urlGetSLButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        urlButtonPanel.add(urlGetSLButton, gridBagConstraints);

        urlClearButton.setText("Clear");
        urlClearButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        urlClearButton.setPreferredSize(new java.awt.Dimension(60, 21));
        urlClearButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                urlClearButtonMouseReleased(evt);
            }
        });
        urlButtonPanel.add(urlClearButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        urlPanel.add(urlButtonPanel, gridBagConstraints);

        filePanel.setLayout(new java.awt.GridBagLayout());

        fileLabel.setText("<html>This tab is used to address files with edge data. Please browse files with edge data or get subject locator files.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        filePanel.add(fileLabel, gridBagConstraints);

        fileScrollPane.setPreferredSize(new java.awt.Dimension(10, 100));
        fileScrollPane.setViewportView(fileTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        filePanel.add(fileScrollPane, gridBagConstraints);

        fileButtonPanel.setLayout(new java.awt.GridBagLayout());

        fileBrowseButton.setText("Browse");
        fileBrowseButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        fileBrowseButton.setPreferredSize(new java.awt.Dimension(60, 21));
        fileBrowseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fileBrowseButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        fileButtonPanel.add(fileBrowseButton, gridBagConstraints);

        fileGetSLButton.setText("Get SLs");
        fileGetSLButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        fileGetSLButton.setPreferredSize(new java.awt.Dimension(60, 21));
        fileGetSLButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fileGetSLButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        fileButtonPanel.add(fileGetSLButton, gridBagConstraints);

        fileClearButton.setText("Clear");
        fileClearButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        fileClearButton.setPreferredSize(new java.awt.Dimension(60, 21));
        fileClearButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fileClearButtonMouseReleased(evt);
            }
        });
        fileButtonPanel.add(fileClearButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        filePanel.add(fileButtonPanel, gridBagConstraints);

        getContentPane().setLayout(new java.awt.GridBagLayout());

        lSystemPanel.setLayout(new java.awt.GridBagLayout());

        lSystemLabel.setText("<html>An L-system or Lindenmayer system is a parallel rewriting system and a type of formal grammar. \nAn L-system consists of an alphabet of symbols that can be used to make strings, a collection of production rules that expand each \nsymbol into some larger string of symbols, an initial \"axiom\" string from which to begin construction, and a mechanism for translating the \ngenerated strings into graph structures. Write your L-system to the textarea below or choose an L-system with the selector.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        lSystemPanel.add(lSystemLabel, gridBagConstraints);

        selectLSystemPanel.setLayout(new java.awt.GridBagLayout());

        lSystemComboBox.setMinimumSize(new java.awt.Dimension(28, 21));
        lSystemComboBox.setPreferredSize(new java.awt.Dimension(28, 21));
        lSystemComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lSystemComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        selectLSystemPanel.add(lSystemComboBox, gridBagConstraints);

        saveLSystemButton.setText("new");
        saveLSystemButton.setMargin(new java.awt.Insets(0, 3, 0, 3));
        saveLSystemButton.setMaximumSize(new java.awt.Dimension(50, 21));
        saveLSystemButton.setMinimumSize(new java.awt.Dimension(50, 21));
        saveLSystemButton.setPreferredSize(new java.awt.Dimension(50, 21));
        saveLSystemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveLSystemButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        selectLSystemPanel.add(saveLSystemButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        lSystemPanel.add(selectLSystemPanel, gridBagConstraints);

        lSystemScrollPane.setPreferredSize(new java.awt.Dimension(10, 100));

        lSystemTextPane.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        lSystemScrollPane.setViewportView(lSystemTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        lSystemPanel.add(lSystemScrollPane, gridBagConstraints);

        depthPanel.setLayout(new java.awt.GridBagLayout());

        depthTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        depthTextField.setText("5");
        depthTextField.setMinimumSize(new java.awt.Dimension(35, 20));
        depthTextField.setPreferredSize(new java.awt.Dimension(35, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        depthPanel.add(depthTextField, gridBagConstraints);

        depthLabel.setText("iterations");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        depthPanel.add(depthLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        lSystemPanel.add(depthPanel, gridBagConstraints);

        tabbedSourcePane.addTab("L-system", lSystemPanel);

        rawPanel.setLayout(new java.awt.GridBagLayout());

        rawLabel.setText("<html>Transform any generated L-system string into a topic map graph. Paste or write you L-system string below. Press Generate button to start transformation.</html>");
        rawLabel.setToolTipText("<html>\nParser vocabulary is<br><pre>\na         create topic and association it with previous one\n[A-V]     create named topic and association it with previous\n[eiuoy]  create topic and association it with previous one using named schema\n:[a-zA-Z] change global association type and roles\n:0        reset association type and roles\n\n(         start sequential block\n)         close sequential block\n[         start parallel block\n]         close parallel block\n{         start cycle block\n}         close cycle block\n\n0         reset topic counter</pre>\n-         substract topic counter by one\n+        add topic counter bt one</pre>\n</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        rawPanel.add(rawLabel, gridBagConstraints);

        rawScrollPane.setPreferredSize(new java.awt.Dimension(10, 100));

        rawTextPane.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        rawScrollPane.setViewportView(rawTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        rawPanel.add(rawScrollPane, gridBagConstraints);

        tabbedSourcePane.addTab("Parser", rawPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        getContentPane().add(tabbedSourcePane, gridBagConstraints);
        tabbedSourcePane.getAccessibleContext().setAccessibleName("");

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        infoButton.setText("Info");
        infoButton.setToolTipText("Get more information about L-system generator. Opens web browser at  http://wandora.org/wiki/L-system_generator");
        infoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(infoButton, new java.awt.GridBagConstraints());

        fillerPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout fillerPanelLayout = new javax.swing.GroupLayout(fillerPanel);
        fillerPanel.setLayout(fillerPanelLayout);
        fillerPanelLayout.setHorizontalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 364, Short.MAX_VALUE)
        );
        fillerPanelLayout.setVerticalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(fillerPanel, gridBagConstraints);

        generateButton.setText("Generate");
        generateButton.setPreferredSize(new java.awt.Dimension(80, 23));
        generateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                generateButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(generateButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(80, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
    wasAccepted = false;
    setVisible(false);
}//GEN-LAST:event_cancelButtonMouseReleased

private void generateButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_generateButtonMouseReleased
    wasAccepted = true;
    setVisible(false);
}//GEN-LAST:event_generateButtonMouseReleased

private void urlClearButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlClearButtonMouseReleased
    this.urlTextPane.setText("");
}//GEN-LAST:event_urlClearButtonMouseReleased

private void urlGetSLButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlGetSLButtonMouseReleased
    selectContextSLs();
}//GEN-LAST:event_urlGetSLButtonMouseReleased

private void urlGetSIButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlGetSIButtonMouseReleased
    selectContextSIs();
}//GEN-LAST:event_urlGetSIButtonMouseReleased

private void fileClearButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileClearButtonMouseReleased
    this.fileTextPane.setText("");
}//GEN-LAST:event_fileClearButtonMouseReleased

private void fileGetSLButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileGetSLButtonMouseReleased
    selectContextSLFiles();
}//GEN-LAST:event_fileGetSLButtonMouseReleased

private void fileBrowseButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileBrowseButtonMouseReleased
    selectFiles();
}//GEN-LAST:event_fileBrowseButtonMouseReleased

private void saveLSystemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveLSystemButtonActionPerformed
    storeLSystem();
}//GEN-LAST:event_saveLSystemButtonActionPerformed

private void lSystemComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lSystemComboBoxActionPerformed
    if((evt.getModifiers() | java.awt.event.ActionEvent.ACTION_PERFORMED) != 0) {
        restoreLSystem();
    }
}//GEN-LAST:event_lSystemComboBoxActionPerformed

    private void infoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoButtonActionPerformed
        Desktop desktop = Desktop.getDesktop();
        if(desktop != null) {
            try {
                desktop.browse(new URI("http://wandora.org/wiki/L-system_generator"));
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_infoButtonActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel depthLabel;
    private javax.swing.JPanel depthPanel;
    private javax.swing.JTextField depthTextField;
    private javax.swing.JButton fileBrowseButton;
    private javax.swing.JPanel fileButtonPanel;
    private javax.swing.JButton fileClearButton;
    private javax.swing.JButton fileGetSLButton;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JPanel filePanel;
    private javax.swing.JScrollPane fileScrollPane;
    private javax.swing.JTextPane fileTextPane;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JButton generateButton;
    private javax.swing.JButton infoButton;
    private javax.swing.JComboBox lSystemComboBox;
    private javax.swing.JLabel lSystemLabel;
    private javax.swing.JPanel lSystemPanel;
    private javax.swing.JScrollPane lSystemScrollPane;
    private javax.swing.JTextPane lSystemTextPane;
    private javax.swing.JLabel rawLabel;
    private javax.swing.JPanel rawPanel;
    private javax.swing.JScrollPane rawScrollPane;
    private javax.swing.JTextPane rawTextPane;
    private javax.swing.JButton saveLSystemButton;
    private javax.swing.JPanel selectLSystemPanel;
    private javax.swing.JTabbedPane tabbedSourcePane;
    private javax.swing.JPanel urlButtonPanel;
    private javax.swing.JButton urlClearButton;
    private javax.swing.JButton urlGetSIButton;
    private javax.swing.JButton urlGetSLButton;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JPanel urlPanel;
    private javax.swing.JScrollPane urlScrollPane;
    private javax.swing.JTextPane urlTextPane;
    // End of variables declaration//GEN-END:variables

}
