/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://www.wandora.org/
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * RTopicPanel.java
 *
 * Created on 21.9.2011, 14:44:26
 */




package org.wandora.application.gui.topicpanels;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import org.apache.commons.io.FileUtils;
import org.wandora.application.CancelledException;
import org.wandora.application.LocatorHistory;
import org.wandora.application.RefreshListener;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleFileChooser;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleRadioButton;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.simple.SimpleTextConsole;
import org.wandora.application.gui.simple.SimpleTextConsoleListener;
import org.wandora.application.gui.simple.SimpleTextPane;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.application.tools.r.RBridge;
import org.wandora.application.tools.r.RBridgeListener;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;
import org.wandora.utils.IObox;
import org.wandora.utils.Options;

//import jsyntaxpane.DefaultSyntaxKit;
import de.sciss.syntaxpane.DefaultSyntaxKit;





/**
 *
 * @author akivela
 */


public class RTopicPanel extends javax.swing.JPanel implements TopicMapListener, RefreshListener, TopicPanel, ActionListener, ComponentListener, SimpleTextConsoleListener, RBridgeListener {
    public boolean USE_LOCAL_OPTIONS = true;
    public boolean SAVE_SKETCH_TO_GLOBAL_OPTIONS = true;
    
    public static final int NO_SOURCE = 0;
    public static final int OCCURRENCE_SOURCE = 1;
    public static final int FILE_SOURCE = 2;
    
    public static final int DONT_AUTORUN = 0;
    public static final int AUTORUN_OCCURRENCE = 1;
    public static final int AUTORUN_SCRIPT_IN_EDITOR = 2;
    public static final int AUTORUN_FILE = 4;
    
    private static int autorun = 0;
    private static String autorunScriptFile = "";
    private static boolean autoloadFromOccurrence = false;
    
    private int currentScriptSource = NO_SOURCE;
    private String currentScriptFile = null;
    private String currentScript = null;
    
    private static final String R_OCCURRENCE_TYPE = "http://www.r-project.org";
    private static final String optionsPrefix = "options.rpanel";
    private static final String scriptPath = "resources/r/";

    private Options options = null;
    private TopicMap tm;
    private Topic rootTopic;
    private boolean isGuiInitialized = false;
    private RBridge rBridge = null;
    
    private JDialog optionsDialog = null;
    private JFileChooser fc = null;
    private JPopupMenu menu = null;
    
    private static final String defaultMessage = 
            "# \n"+
            "# Welcome to Wandora's R topic panel!\n"+
            "# \n"+
            "# R topic panel is used to write and execute R language scripts.\n"+
            "# R script can access the topic map in Wandora via Wandora's Java API.\n"+
            "# \n"+
            "# Learn R language and Wandora API here\n"+
            "#    http://www.r-project.org/ \n"+
            "#    http://wandora.org/api/ \n"+
            "#\n";
    
    
    
    
    
    private String[] openScriptMenuStruct = new String[] {
        "Open script from occurrence",
        "Open script from file..."
    };
    private String[] saveScriptMenuStruct = new String[] {
        "Save script to occurrence",
        "Save script to file..."
    };
    
    
    /** Creates new form RTopicPanel */
    public RTopicPanel() {
    }

    
    @Override
    public void init() {
        Wandora wandora = Wandora.getWandora();
        tm = wandora.getTopicMap();

        if(options == null) {
            if(USE_LOCAL_OPTIONS) {
                options = new Options(wandora.getOptions());
            }
            else {
                options = wandora.getOptions();
            }
        }

        initComponents();
        this.addComponentListener(this);
        
        DefaultSyntaxKit.initKit();
        rEditor.setContentType("text/plain");
        
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
        rEditor.getInputMap().put(key, "saveOperation");
        Action saveOperation = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveScript();
            }
        };
        rEditor.getActionMap().put("saveOperation", saveOperation);
        rEditor.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

        rBridge = RBridge.getRBridge();
        rBridge.addRBridgeListener(this);

        fc = new JFileChooser();
        fc.setCurrentDirectory(new File(scriptPath));

        readOptions();
        if(currentScript != null) {
            rEditor.setText(currentScript);
        }
        else {
            rEditor.setText(defaultMessage);
        }
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

        optionsPanel = new javax.swing.JPanel();
        optionsTabbedPane = new SimpleTabbedPane();
        autoloadOptionsPanel = new javax.swing.JPanel();
        autoloadPanel = new javax.swing.JPanel();
        autoloadLabel = new SimpleLabel();
        autoloadCheckBox = new SimpleCheckBox();
        autorunOptionsPanel = new javax.swing.JPanel();
        autorunOptionsPanelInner = new javax.swing.JPanel();
        optionsLabel = new SimpleLabel();
        noAutoRunRadioButton = new SimpleRadioButton();
        autoRunOccurrenceRadioButton = new SimpleRadioButton();
        autoRunScriptInEditorRadioButton = new SimpleRadioButton();
        jPanel1 = new javax.swing.JPanel();
        autoRunFileRadioButton = new SimpleRadioButton();
        autoRunFileTextField = new SimpleField();
        autoRunFileBrowseButton = new SimpleButton();
        optionsButtonPanel1 = new javax.swing.JPanel();
        optionsOkButton = new SimpleButton();
        autoRunSource = new javax.swing.ButtonGroup();
        tabPanel = new SimpleTabbedPane();
        editorPanel = new javax.swing.JPanel();
        editorScroller = new SimpleScrollPane();
        rEditor = new SimpleTextPane();
        codeBottomBar = new javax.swing.JPanel();
        runButtonPanel = new javax.swing.JPanel();
        executeBtn = new SimpleButton();
        fillerPanel = new javax.swing.JPanel();
        optionsButtonPanel = new javax.swing.JPanel();
        newBtn = new SimpleButton();
        openBtn = new SimpleButton();
        saveBtn = new SimpleButton();
        jSeparator1 = new javax.swing.JSeparator();
        optionsBtn = new SimpleButton();
        consolePanel = new javax.swing.JPanel();
        rConsole = new javax.swing.JPanel();
        rConsoleScrollPane = new javax.swing.JScrollPane();
        rConsoleTextPane = new SimpleTextConsole(this);

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        autoloadOptionsPanel.setLayout(new java.awt.GridBagLayout());

        autoloadPanel.setLayout(new java.awt.GridBagLayout());

        autoloadLabel.setText("<html>Whether or not to load script automatically from occurrence.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        autoloadPanel.add(autoloadLabel, gridBagConstraints);

        autoloadCheckBox.setText("Autoload script from occurrence");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        autoloadPanel.add(autoloadCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 16, 16);
        autoloadOptionsPanel.add(autoloadPanel, gridBagConstraints);

        optionsTabbedPane.addTab("Autoload", autoloadOptionsPanel);

        autorunOptionsPanel.setLayout(new java.awt.GridBagLayout());

        autorunOptionsPanelInner.setLayout(new java.awt.GridBagLayout());

        optionsLabel.setText("<html>R topic panel autorun options control automated script execution.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        autorunOptionsPanelInner.add(optionsLabel, gridBagConstraints);

        autoRunSource.add(noAutoRunRadioButton);
        noAutoRunRadioButton.setText("Don't autorun");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        autorunOptionsPanelInner.add(noAutoRunRadioButton, gridBagConstraints);

        autoRunSource.add(autoRunOccurrenceRadioButton);
        autoRunOccurrenceRadioButton.setText("Autorun script in occurrence");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        autorunOptionsPanelInner.add(autoRunOccurrenceRadioButton, gridBagConstraints);

        autoRunSource.add(autoRunScriptInEditorRadioButton);
        autoRunScriptInEditorRadioButton.setText("Autorun script in editor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        autorunOptionsPanelInner.add(autoRunScriptInEditorRadioButton, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        autoRunSource.add(autoRunFileRadioButton);
        autoRunFileRadioButton.setText("Autorun script in file");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel1.add(autoRunFileRadioButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(autoRunFileTextField, gridBagConstraints);

        autoRunFileBrowseButton.setText("Browse");
        autoRunFileBrowseButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        autoRunFileBrowseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                autoRunFileBrowseButtonMouseReleased(evt);
            }
        });
        jPanel1.add(autoRunFileBrowseButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        autorunOptionsPanelInner.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 16, 16);
        autorunOptionsPanel.add(autorunOptionsPanelInner, gridBagConstraints);

        optionsTabbedPane.addTab("Autorun", autorunOptionsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        optionsPanel.add(optionsTabbedPane, gridBagConstraints);

        optionsButtonPanel1.setLayout(new java.awt.GridBagLayout());

        optionsOkButton.setText("OK");
        optionsOkButton.setMargin(new java.awt.Insets(1, 4, 1, 4));
        optionsOkButton.setMinimumSize(new java.awt.Dimension(65, 21));
        optionsOkButton.setPreferredSize(new java.awt.Dimension(65, 21));
        optionsOkButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                optionsOkButtonMouseReleased(evt);
            }
        });
        optionsButtonPanel1.add(optionsOkButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionsPanel.add(optionsButtonPanel1, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());

        tabPanel.setMinimumSize(new java.awt.Dimension(300, 74));
        tabPanel.setPreferredSize(new java.awt.Dimension(300, 53));

        editorPanel.setMinimumSize(new java.awt.Dimension(200, 46));
        editorPanel.setPreferredSize(new java.awt.Dimension(200, 25));
        editorPanel.setLayout(new java.awt.GridBagLayout());

        rEditor.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        editorScroller.setViewportView(rEditor);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        editorPanel.add(editorScroller, gridBagConstraints);

        codeBottomBar.setMinimumSize(new java.awt.Dimension(405, 23));
        codeBottomBar.setPreferredSize(new java.awt.Dimension(410, 23));
        codeBottomBar.setLayout(new java.awt.GridBagLayout());

        runButtonPanel.setLayout(new java.awt.GridBagLayout());

        executeBtn.setText("Run");
        executeBtn.setMargin(new java.awt.Insets(2, 6, 2, 6));
        executeBtn.setMaximumSize(new java.awt.Dimension(75, 21));
        executeBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        executeBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        executeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                executeBtnexecuteOnMouseRelease(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        runButtonPanel.add(executeBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        codeBottomBar.add(runButtonPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        codeBottomBar.add(fillerPanel, gridBagConstraints);

        optionsButtonPanel.setLayout(new java.awt.GridBagLayout());

        newBtn.setText("New");
        newBtn.setMaximumSize(new java.awt.Dimension(75, 21));
        newBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        newBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        newBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                newBtnMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        optionsButtonPanel.add(newBtn, gridBagConstraints);

        openBtn.setText("Open");
        openBtn.setMaximumSize(new java.awt.Dimension(75, 21));
        openBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        openBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        openBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                openBtnMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                openBtnMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        optionsButtonPanel.add(openBtn, gridBagConstraints);

        saveBtn.setText("Save");
        saveBtn.setMargin(new java.awt.Insets(2, 4, 2, 4));
        saveBtn.setMaximumSize(new java.awt.Dimension(75, 21));
        saveBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        saveBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                saveBtnMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                saveBtnMouseReleased(evt);
            }
        });
        optionsButtonPanel.add(saveBtn, new java.awt.GridBagConstraints());

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionsButtonPanel.add(jSeparator1, gridBagConstraints);

        optionsBtn.setText("Options");
        optionsBtn.setMaximumSize(new java.awt.Dimension(75, 21));
        optionsBtn.setMinimumSize(new java.awt.Dimension(75, 21));
        optionsBtn.setPreferredSize(new java.awt.Dimension(75, 21));
        optionsBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                optionsBtnMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        optionsButtonPanel.add(optionsBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        codeBottomBar.add(optionsButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        editorPanel.add(codeBottomBar, gridBagConstraints);

        tabPanel.addTab("Script", editorPanel);

        consolePanel.setLayout(new java.awt.GridBagLayout());

        rConsole.setLayout(new java.awt.GridBagLayout());

        rConsoleScrollPane.setViewportView(rConsoleTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        rConsole.add(rConsoleScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        consolePanel.add(rConsole, gridBagConstraints);

        tabPanel.addTab("R console", consolePanel);

        add(tabPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void executeBtnexecuteOnMouseRelease(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_executeBtnexecuteOnMouseRelease
        tabPanel.setSelectedComponent(consolePanel);
        executeScriptInEditor();
}//GEN-LAST:event_executeBtnexecuteOnMouseRelease

    private void newBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newBtnMouseReleased
        newScript();
        invalidate();
}//GEN-LAST:event_newBtnMouseReleased

    private void saveBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMouseReleased
        
}//GEN-LAST:event_saveBtnMouseReleased

    private void optionsOkButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionsOkButtonMouseReleased
        if(optionsDialog != null) {
            optionsDialog.setVisible(false);
        }
        if(noAutoRunRadioButton.isSelected()) autorun = DONT_AUTORUN;
        else if(autoRunOccurrenceRadioButton.isSelected()) autorun = AUTORUN_OCCURRENCE;
        else if(autoRunScriptInEditorRadioButton.isSelected()) autorun = AUTORUN_SCRIPT_IN_EDITOR;
        else if(autoRunFileRadioButton.isSelected()) autorun = AUTORUN_FILE;
        autorunScriptFile = autoRunFileTextField.getText();
        
        autoloadFromOccurrence = autoloadCheckBox.isSelected();
        
        if(options != null) {
            options.put(optionsPrefix+".autorun", ""+autorun);
            options.put(optionsPrefix+".autorunScriptFile", autorunScriptFile);
            options.put(optionsPrefix+".autoload", Boolean.toString(autoloadFromOccurrence));
        }
    }//GEN-LAST:event_optionsOkButtonMouseReleased

    private void optionsBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionsBtnMouseReleased
        openOptionsDialog();
    }//GEN-LAST:event_optionsBtnMouseReleased

    private void openBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openBtnMouseReleased

    }//GEN-LAST:event_openBtnMouseReleased

    private void openBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openBtnMousePressed
        showMenu(openScriptMenuStruct, evt);
    }//GEN-LAST:event_openBtnMousePressed

    private void saveBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMousePressed
        showMenu(saveScriptMenuStruct, evt);
    }//GEN-LAST:event_saveBtnMousePressed

    private void autoRunFileBrowseButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_autoRunFileBrowseButtonMouseReleased
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Select R Script");
        int answer = fc.showDialog(Wandora.getWandora(), "Select");
        if(answer == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if(f != null) {
                autoRunFileTextField.setText(f.getAbsolutePath());
                autoRunFileRadioButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_autoRunFileBrowseButtonMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton autoRunFileBrowseButton;
    private javax.swing.JRadioButton autoRunFileRadioButton;
    private javax.swing.JTextField autoRunFileTextField;
    private javax.swing.JRadioButton autoRunOccurrenceRadioButton;
    private javax.swing.JRadioButton autoRunScriptInEditorRadioButton;
    private javax.swing.ButtonGroup autoRunSource;
    private javax.swing.JCheckBox autoloadCheckBox;
    private javax.swing.JLabel autoloadLabel;
    private javax.swing.JPanel autoloadOptionsPanel;
    private javax.swing.JPanel autoloadPanel;
    private javax.swing.JPanel autorunOptionsPanel;
    private javax.swing.JPanel autorunOptionsPanelInner;
    private javax.swing.JPanel codeBottomBar;
    private javax.swing.JPanel consolePanel;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JScrollPane editorScroller;
    private javax.swing.JButton executeBtn;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton newBtn;
    private javax.swing.JRadioButton noAutoRunRadioButton;
    private javax.swing.JButton openBtn;
    private javax.swing.JButton optionsBtn;
    private javax.swing.JPanel optionsButtonPanel;
    private javax.swing.JPanel optionsButtonPanel1;
    private javax.swing.JLabel optionsLabel;
    private javax.swing.JButton optionsOkButton;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JTabbedPane optionsTabbedPane;
    private javax.swing.JPanel rConsole;
    private javax.swing.JScrollPane rConsoleScrollPane;
    private javax.swing.JTextPane rConsoleTextPane;
    private javax.swing.JEditorPane rEditor;
    private javax.swing.JPanel runButtonPanel;
    private javax.swing.JButton saveBtn;
    private javax.swing.JTabbedPane tabPanel;
    // End of variables declaration//GEN-END:variables

    
    
    
    
    
    private void openOptionsDialog() {
        optionsDialog = new JDialog(Wandora.getWandora(), true);
        optionsDialog.setSize(500,270);
        optionsDialog.add(optionsPanel);
        optionsDialog.setTitle("R topic panel options");
        Wandora.getWandora().centerWindow(optionsDialog);
        optionsDialog.setVisible(true);
    }
    
    
    
    
    
    
    public void showMenu(String[] struct, MouseEvent evt) {
        menu = UIBox.makePopupMenu(struct, this);
        menu.setLocation(evt.getXOnScreen()-2, evt.getYOnScreen()-2);
        menu.show(evt.getComponent(), evt.getX()-2, evt.getY()-2);
    }
    
    

    @Override
    public void doRefresh() throws TopicMapException {
        
    }

    
    
    @Override
    public boolean supportsOpenTopic() {
        return true;
    }
    
    
    
    @Override
    public void open(Topic topic) throws TopicMapException {
	rootTopic = topic;
        
        if(autoloadFromOccurrence) {
            rEditor.setText(getROccurrence());
        }
	autorun();
    }

    
    private void readOptions() {
        autoloadFromOccurrence = options.getBoolean(optionsPrefix+".autoload", autoloadFromOccurrence);
        autoloadCheckBox.setSelected(autoloadFromOccurrence);
        
	autorun = options.getInt(optionsPrefix+".autorun", 0);
        autorunScriptFile = options.get(optionsPrefix+".autorunScriptFile");
        autoRunFileTextField.setText(autorunScriptFile);
        
        currentScript = options.get(optionsPrefix+".currentScript");

        switch(autorun) {
            case DONT_AUTORUN: { noAutoRunRadioButton.setSelected(true); break; }
            case AUTORUN_OCCURRENCE: { autoRunOccurrenceRadioButton.setSelected(true); break; }
            case AUTORUN_SCRIPT_IN_EDITOR: { autoRunScriptInEditorRadioButton.setSelected(true); break; }
            case AUTORUN_FILE: { autoRunFileRadioButton.setSelected(true); break; }
        }
    }
    
    
    
    
    private void autorun() {
        String autorunScript = null;
        switch(autorun) {
            case AUTORUN_OCCURRENCE: {
                autorunScript = getROccurrence();
                break; 
            }
            case AUTORUN_SCRIPT_IN_EDITOR: { 
                autorunScript = rEditor.getText();
                break; 
            }
            case AUTORUN_FILE: {
                try {
                    if(autorunScriptFile != null && autorunScriptFile.length() > 0) {
                       autorunScript = IObox.loadFile(autorunScriptFile);
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                break; 
            }
        }
        if(autorunScript != null) {
            executeScript(autorunScript);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    private String getROccurrence() {
        return getROccurrence(rootTopic);
    }
    
    
    private String getROccurrence(Topic t) {
        String o = null;
        if(t != null) {
            try {
                Topic otype = tm.getTopic(R_OCCURRENCE_TYPE);
                Topic olang = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                if(otype != null && olang != null) {
                    o = t.getData(otype, olang);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return o;
    }
    
    
    
    private void setROccurrence(String o) {
        setROccurrence(rootTopic, o);
    }
    
    private void setROccurrence(Topic t, String o) {
        if(t != null) {
            try {
                Topic otype = tm.getTopic(R_OCCURRENCE_TYPE);
                Topic olang = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                if(otype != null && olang != null) {
                    t.setData(otype, olang, o);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void stop() {
        saveCurrentScriptToOptions();
    }

    @Override
    public LocatorHistory getTopicHistory() {
        return null;
    }
    

    @Override
    public void refresh() throws TopicMapException {

    }

    @Override
    public boolean applyChanges() throws CancelledException, TopicMapException {
        saveCurrentScriptToOptions();
        return true;
    }
    

    @Override
    public JPanel getGui() {
        return this;
    }

    @Override
    public Topic getTopic() throws TopicMapException {
        return rootTopic;
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/topic_panel_r.png");
    }

    
    

    @Override
    public JPopupMenu getViewPopupMenu() {
        return UIBox.makePopupMenu(getViewMenuStruct(), this);
    }
    
    @Override
    public JMenu getViewMenu() {
        return UIBox.makeMenu(getViewMenuStruct(), this);
    }

    @Override
    public Object[] getViewMenuStruct() {
        Object[] menuStructure = new Object[] {
            "New script", this,
            "Open script", this,
            new Object[] {
                "Open script from occurrence", this,
                "Open script from file...", this,
            },
            "Save script", this,
            new Object[] {
                "Save script to occurrence", this,
                "Save script to file...", this,
            },
            "Run script", this,
            "---",
            "Options...", this,
        };
        return menuStructure;
    }
    


    @Override
    public boolean noScroll(){
        return false;
    }
  
    
    @Override
    public String getName() {
        return "R";
    }
    
    @Override
    public String getTitle() {
        if(rootTopic != null) return TopicToString.toString(rootTopic);
        else return getName();
    }
    
    
    @Override
    public int getOrder() {
        return 1000;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String ac = e.getActionCommand();
        if("New script".equalsIgnoreCase(ac)) {
            newScript();
        }
        else if("Open script from occurrence".equalsIgnoreCase(ac)) {
            loadScriptFromOccurrence();
        }
        else if("Open script from file...".equalsIgnoreCase(ac)) {
            loadScriptFromFile();
        }
        else if("Save script to occurrence".equalsIgnoreCase(ac)) {
            saveScriptToOccurrence();
        }
        else if("Save script to file...".equalsIgnoreCase(ac)) {
            saveScriptToFile();
        }
        else if("Options...".equalsIgnoreCase(ac)) {
            openOptionsDialog();
        }
        else if("Run script".equalsIgnoreCase(ac)) {
            executeScriptInEditor();
        }
    }

    
    
    // -------------------------------------------------------------------------
    
    
    private void newScript() {
        int answer = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Erase current script in editor?", "Erase script in editor?", WandoraOptionPane.YES_NO_OPTION);
        if(answer == WandoraOptionPane.YES_OPTION) {
            currentScriptSource = NO_SOURCE;
            currentScriptFile = null;
            rEditor.setText("");
        }
    }
    
    
    
    
    public void executeScriptInEditor() {
        String script = rEditor.getText();
        executeScript(script);
    }
    
    public void executeScript(String script) {
        if(script != null) {
            if(script.trim().length() > 0) {
                String newline = "\n";
                //String commands[] = script.split(newline);
                //script = script.replaceAll("(?m)^[ \t]*\r?\n","");
                script = script.replace("'","\'");
                //script = script.replaceAll("\r\n", ";");
                script = script.replace("\n", "\\n");
                script = script.replace("\r", "\\r");
                script = script.replace("\t", "\\t");
                String out = "source(textConnection('" + script + "'),print.eval=TRUE)";
                SimpleTextConsole console = (SimpleTextConsole) rConsoleTextPane;
                tabPanel.setSelectedComponent(consolePanel);
                console.output(console.handleInput(out));
                //for(int i=0; i<commands.length; i++) {
                    //console.output(commands[i]+newline);
                    //console.output(console.handleInput(commands[i].trim()));
                //}
            }
        }
    }
    
    
    
    private void loadScriptFromOccurrence() {
        try {
            Topic otype = tm.getTopic(R_OCCURRENCE_TYPE);
            Topic olang = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
            if(otype != null && olang != null) {
                currentScriptFile = null;
                String o = rootTopic.getData(otype, olang);
                if(o != null) {
                    rEditor.setText(o);
                }
                else {
                    WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find R occurrence in current topic. Can't restore R script from occurrence.", "Can't restore R occurrence", WandoraOptionPane.INFORMATION_MESSAGE);
                }
            }
            else {
                if(otype == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find R occurrence type. Can't restore R script from occurrence.", "Can't find R occurrence type", WandoraOptionPane.INFORMATION_MESSAGE);
                if(olang == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find Language independent scope topic. Can't restore R script from occurrence.", "Can't find Language independent scope topic", WandoraOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    private void loadScriptFromFile() {
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Open R Script");
        int answer = fc.showDialog(Wandora.getWandora(), "Open");
        if(answer == SimpleFileChooser.APPROVE_OPTION) {
            File scriptFile = fc.getSelectedFile();
            try {
                String script = IObox.loadFile(scriptFile);
                currentScriptFile = scriptFile.getAbsolutePath();
                if(script != null) {
                    rEditor.setText(script);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while restoring R script from file '"+scriptFile.getName()+"'.", "Can't restore R script", WandoraOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    

    
    
    
    private void saveScriptToOccurrence() {
        String script = rEditor.getText();
        try {
            Topic otype = tm.getTopic(R_OCCURRENCE_TYPE);
            Topic olang = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
            if(otype != null && olang != null) {
                boolean storeToOccurrence = true;
                currentScriptSource = OCCURRENCE_SOURCE;
                String oldScript = rootTopic.getData(otype, olang);
                if(oldScript != null) {
                    int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Current topic contains an R script occurrence already. Storing current script erases older. Do you want to store the script to the occurrence?","Topic already has an R script occurrence", WandoraOptionPane.INFORMATION_MESSAGE);
                    if(a != WandoraOptionPane.YES_OPTION) storeToOccurrence = false;
                }
                if(storeToOccurrence) {
                    rootTopic.setData(otype, olang, script);
                }
            }
            else {
                if(otype == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find R occurrence type. Can't save R script to occurrence.", "Can't find R occurrence type", WandoraOptionPane.INFORMATION_MESSAGE);
                if(olang == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find Language independent scope topic. Can't save R script to occurrence.", "Can't find Language independent scope topic", WandoraOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while storing the R script to an occurrence to current topic.", "Can't store R script", WandoraOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    private void saveScriptToFile() {
        File scriptFile = null;
        try {
            fc.setDialogTitle("Save R script");
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            int answer = fc.showDialog(Wandora.getWandora(), "Save");
            if(answer == SimpleFileChooser.APPROVE_OPTION) {
                scriptFile = fc.getSelectedFile();
                String scriptCode = rEditor.getText();
                FileUtils.writeStringToFile(scriptFile, scriptCode, "UTF-8");
                currentScriptSource = FILE_SOURCE;
            }
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while storing R script to file '"+scriptFile.getName()+"'.", "Can't save R script", WandoraOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    
    
    private void saveScript() {
        if(currentScriptSource == OCCURRENCE_SOURCE) {
            saveScriptToOccurrence();
        }
        else if(currentScriptSource == FILE_SOURCE) {
            saveScriptToFile();
        }
    }
    
    
    private void saveCurrentScriptToOptions() {
        if(options != null) {
            options.put(optionsPrefix+".currentScript", rEditor.getText());
        }
        if(USE_LOCAL_OPTIONS && SAVE_SKETCH_TO_GLOBAL_OPTIONS) {
            try {
                Wandora.getWandora().getOptions().put(optionsPrefix+".currentScript", rEditor.getText());
            }
            catch(Exception e) {
                
            }
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    

    @Override
    public String handleInput(String input) {
        //System.out.println("HANDLE INPUT: "+input);
        return rBridge.handleInput(input);
    }

    

    
    
    @Override
    public void output(String output) {
        ((SimpleTextConsole) rConsoleTextPane).output(output);
        ((SimpleTextConsole) rConsoleTextPane).refresh();
        
    }

    
    
    // -------------------------------------------------------------------------
    
    
    

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        
    }

    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        
    }
    
    // -------------------------------------------------------------------------
    
    

    
    @Override
    public void componentResized(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentShown(ComponentEvent e) {
        handleComponentEvent(e);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        handleComponentEvent(e);
    }
    
    
    
    
    private void handleComponentEvent(ComponentEvent e) {
        saveCurrentScriptToOptions();

	revalidate();
        repaint();
    }
    
    
    // -------------------------------------------------------------------------
    
    
}
