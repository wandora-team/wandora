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
 * AbstractImportDialog.java
 *
 * Created on 21.1.2009, 13:14
 */

package org.wandora.application.tools.importers;


import org.wandora.topicmap.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.utils.IObox;

import java.awt.*;
import java.util.*;
import java.io.*;





/**
 *
 * @author  akivela
 */
public class AbstractImportDialog extends javax.swing.JDialog {

	private static final long serialVersionUID = 1L;

	private Wandora wandora = null;
    private WandoraTool parentTool = null;
    private boolean wasAccepted = false;
    private HashMap<Component,Integer> registeredSources = null;
    
    
    
    /** Creates new form AbstractImportDialog */
    public AbstractImportDialog(Wandora wandora, boolean modal) {
        super(wandora, modal);
        this.wandora = wandora;
        initComponents();
        initialize(null);
    }
    
    
    public void initialize(WandoraTool parentTool) {
        this.parentTool = parentTool;
        wasAccepted = false;
        ((SimpleTextPane) fileTextPane).dropFileNames(true);
        ((SimpleTextPane) fileTextPane).setLineWrap(false);
        ((SimpleTextPane) urlTextPane).setLineWrap(false);

        if(parentTool != null) setTitle(parentTool.getName());
        else setTitle("Select imported resources");
        setSize(640,400);
        if(wandora != null) wandora.centerWindow(this);
        registeredSources = new HashMap<Component,Integer>();
    }
    
    
    
    public boolean wasAccepted() {
        return wasAccepted;
    }
    
    public int getSelectedSource() {
        Component selectedComponent = tabbedSourcePane.getSelectedComponent();
        Integer source = registeredSources.get(selectedComponent);
        return source.intValue();
    }
    
    
    public void registerSource(String name, Component component, int id) {
        if(component == null) return;
        if(registeredSources.get(component) == null) {
            registeredSources.put(component, Integer.valueOf(id));
            tabbedSourcePane.addTab(name, component);
        }
    }
    
    
    public void registerUrlSource() {
        registerSource("Urls", urlPanel, AbstractImportTool.URL_IMPORTER);
    }
    public void registerFileSource() {
        registerSource("Files", filePanel, AbstractImportTool.FILE_IMPORTER);
    }    
    public void registerRawSource() {
        registerSource("Raw", rawPanel, AbstractImportTool.RAW_IMPORTER);
    }
    
    public void selectUrlSource() {
        tabbedSourcePane.setSelectedComponent(urlPanel);
    }
    
    
    
    // --- CONTENT ---
    public String getContent() {
        return rawTextPane.getText();
    }
    
    
    
    
    
    // --- FILE SOURCE ---
    public File[] getFileSources() {
        String input = fileTextPane.getText();
        String[] filenames = splitText(input);
        ArrayList<File> files = new ArrayList<File>();
        File f = null;
        for(int i=0; i<filenames.length; i++) {
            if(filenames[i] != null && filenames[i].trim().length() > 0) {
                f = new File(filenames[i]);
                if(f.exists()) files.add(f);
                else {
                    if(parentTool != null) parentTool.log("File '"+filenames[i]+"' not found!");
                }
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
        chooser.setDialogTitle("Import");
        chooser.setApproveButtonText("Select");
        chooser.setFileSelectionMode(SimpleFileChooser.FILES_AND_DIRECTORIES);
        //if(accessoryPanel != null) { chooser.setAccessory(accessoryPanel); }

        if(chooser.open(wandora, SimpleFileChooser.OPEN_DIALOG)==SimpleFileChooser.APPROVE_OPTION) {
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
        rawPanel = new javax.swing.JPanel();
        rawLabel = new org.wandora.application.gui.simple.SimpleLabel();
        rawScrollPane = new javax.swing.JScrollPane();
        rawTextPane = new org.wandora.application.gui.simple.SimpleTextPane();
        tabbedSourcePane = new org.wandora.application.gui.simple.SimpleTabbedPane();
        buttonPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();
        importButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();

        urlPanel.setLayout(new java.awt.GridBagLayout());

        urlLabel.setText("<html>Select URL resources to be imported. Enter URLs below or get subject identifiers or subject locators from context topics.</html>");
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

        urlGetSIButton.setText("Get subject identifiers");
        urlGetSIButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        urlGetSIButton.setPreferredSize(new java.awt.Dimension(140, 21));
        urlGetSIButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                urlGetSIButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        urlButtonPanel.add(urlGetSIButton, gridBagConstraints);

        urlGetSLButton.setText("Get subject locators");
        urlGetSLButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        urlGetSLButton.setPreferredSize(new java.awt.Dimension(140, 21));
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

        fileLabel.setText("<html>Select files to be imported. Enter or browse or get subject locator files. Text area accepts file drops too.</html>");
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
        fileBrowseButton.setPreferredSize(new java.awt.Dimension(70, 21));
        fileBrowseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fileBrowseButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
        fileButtonPanel.add(fileBrowseButton, gridBagConstraints);

        fileGetSLButton.setText("Get subject locators");
        fileGetSLButton.setMargin(new java.awt.Insets(1, 6, 1, 6));
        fileGetSLButton.setPreferredSize(new java.awt.Dimension(130, 21));
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

        rawPanel.setLayout(new java.awt.GridBagLayout());

        rawLabel.setText("<html>Enter, paste or drop the actual content for the importer.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        rawPanel.add(rawLabel, gridBagConstraints);

        rawScrollPane.setPreferredSize(new java.awt.Dimension(10, 100));
        rawScrollPane.setViewportView(rawTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        rawPanel.add(rawScrollPane, gridBagConstraints);

        getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        getContentPane().add(tabbedSourcePane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        fillerPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout fillerPanelLayout = new javax.swing.GroupLayout(fillerPanel);
        fillerPanel.setLayout(fillerPanelLayout);
        fillerPanelLayout.setHorizontalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
        );
        fillerPanelLayout.setVerticalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(fillerPanel, gridBagConstraints);

        importButton.setText("Import");
        importButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        importButton.setPreferredSize(new java.awt.Dimension(70, 23));
        importButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                importButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(importButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
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

private void urlClearButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlClearButtonMouseReleased
    this.urlTextPane.setText("");
}//GEN-LAST:event_urlClearButtonMouseReleased

private void fileClearButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileClearButtonMouseReleased
    this.fileTextPane.setText("");
}//GEN-LAST:event_fileClearButtonMouseReleased

private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
    wasAccepted = false;
    setVisible(false);
}//GEN-LAST:event_cancelButtonMouseReleased

private void importButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_importButtonMouseReleased
    wasAccepted = true;
    setVisible(false);
}//GEN-LAST:event_importButtonMouseReleased

private void fileBrowseButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileBrowseButtonMouseReleased
    selectFiles();
}//GEN-LAST:event_fileBrowseButtonMouseReleased

private void fileGetSLButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileGetSLButtonMouseReleased
    selectContextSLFiles();
}//GEN-LAST:event_fileGetSLButtonMouseReleased

private void urlGetSLButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlGetSLButtonMouseReleased
    selectContextSLs();
}//GEN-LAST:event_urlGetSLButtonMouseReleased

private void urlGetSIButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_urlGetSIButtonMouseReleased
    selectContextSIs();
}//GEN-LAST:event_urlGetSIButtonMouseReleased



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton fileBrowseButton;
    private javax.swing.JPanel fileButtonPanel;
    private javax.swing.JButton fileClearButton;
    private javax.swing.JButton fileGetSLButton;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JPanel filePanel;
    private javax.swing.JScrollPane fileScrollPane;
    private javax.swing.JTextPane fileTextPane;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel rawLabel;
    private javax.swing.JPanel rawPanel;
    private javax.swing.JScrollPane rawScrollPane;
    private javax.swing.JTextPane rawTextPane;
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
