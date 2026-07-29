/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://www.wandora.org/
 * 
 * Copyright (C) 2004-2026 Wandora Team
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
 * OllamaPanel.java
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
import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.wandora.application.tools.ollama.OllamaUtilities;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;
import org.wandora.utils.Options;

//import jsyntaxpane.DefaultSyntaxKit;
import de.sciss.syntaxpane.DefaultSyntaxKit;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.request.ThinkMode;
import io.github.ollama4j.models.response.OllamaAsyncResultStreamer;






/**
 *
 * @author akivela
 */


public class OllamaPanel extends javax.swing.JPanel implements RefreshListener, TopicPanel, ActionListener, ComponentListener, SimpleTextConsoleListener {
    private static final long serialVersionUID = 1L;
    
    public boolean USE_LOCAL_OPTIONS = true;
    public boolean SAVE_SKETCH_TO_GLOBAL_OPTIONS = true;
    
    public static final int NO_SOURCE = 0;
    public static final int OCCURRENCE_SOURCE = 1;
    public static final int FILE_SOURCE = 2;
    
    public static final int DONT_AUTORUN = 0;
    public static final int AUTORUN_OCCURRENCE = 1;
    public static final int AUTORUN_PROMPT_IN_EDITOR = 2;
    public static final int AUTORUN_FILE = 4;
    
    public static final String PROMPT_OCCURRENCE_TYPE = "https://wandora.org/prompt";
    
    private static int autorun = 0;
    private static String autorunPromptFile = "";
    private static boolean autoloadFromOccurrence = false;
    private static boolean shouldOutputInput = true;
    private static boolean showPromptInConsole = true;
    
    private int currentPromptSource = NO_SOURCE;
    private String currentPromptFile = null;
    private String currentPrompt = null;
    
    private static final String optionsPrefix = "options.ollamapanel";
    private static final String promptPath = "resources/ollama/prompts";

    private Options options = null;
    private TopicMap tm;
    private Topic rootTopic;
    private boolean isGuiInitialized = false;
    
    private Ollama ollama;
    private String ollamaModel;
    private long ollamaPollIntervalMilliseconds;
    private StringBuilder capturedOutput;
    
    private JDialog optionsDialog = null;
    private JFileChooser fc = null;
    private JPopupMenu menu = null;
    
    private static final String defaultMessage = 
            "# \n"+
            "# Welcome to Wandora's Ollama topic panel!\n"+
            "#\n";
    
    
    
    
    
    private String[] openPromptMenuStruct = new String[] {
        "Open prompt from occurrence",
        "Open prompt from file..."
    };
    private String[] savePromptMenuStruct = new String[] {
        "Save prompt to occurrence",
        "Save prompt to file..."
    };
    
    
    /** Creates new form OllamaTopicPanel */
    public OllamaPanel() {
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
        promptEditor.setContentType("text/plain");
        
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        promptEditor.getInputMap().put(key, "saveOperation");
        Action saveOperation = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePrompt();
            }
        };
        promptEditor.getActionMap().put("saveOperation", saveOperation);
        promptEditor.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

        fc = new JFileChooser();
        fc.setCurrentDirectory(new File(promptPath));

        readOptions();
        if(currentPrompt != null) {
            promptEditor.setText(currentPrompt);
        }
        else {
            promptEditor.setText(defaultMessage);
        }
        
        try {
        	ollama = OllamaUtilities.setUp();
        	ollamaModel = "gemma4:12b";
            ollama.pullModel(ollamaModel);
            ollamaPollIntervalMilliseconds = 500l;
        }
        catch(Exception e) {
        	e.printStackTrace();
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
        autoRunPromptInEditorRadioButton = new SimpleRadioButton();
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
        promptEditor = new SimpleTextPane();
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
        ollamaConsole = new javax.swing.JPanel();
        ollamaConsoleScrollPane = new javax.swing.JScrollPane();
        ollamaConsoleTextPane = new SimpleTextConsole(this);

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        autoloadOptionsPanel.setLayout(new java.awt.GridBagLayout());

        autoloadPanel.setLayout(new java.awt.GridBagLayout());

        autoloadLabel.setText("<html>Whether or not to load prompt automatically from occurrence.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        autoloadPanel.add(autoloadLabel, gridBagConstraints);

        autoloadCheckBox.setText("Autoload prompt from occurrence");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        autoloadPanel.add(autoloadCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 16, 16);
        autoloadOptionsPanel.add(autoloadPanel, gridBagConstraints);

        optionsTabbedPane.addTab("Autoload", autoloadOptionsPanel);

        autorunOptionsPanel.setLayout(new java.awt.GridBagLayout());

        autorunOptionsPanelInner.setLayout(new java.awt.GridBagLayout());

        optionsLabel.setText("<html>Ollama topic panel autorun options control automated prompt execution.</html>");
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
        autoRunOccurrenceRadioButton.setText("Autorun prompt in occurrence");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        autorunOptionsPanelInner.add(autoRunOccurrenceRadioButton, gridBagConstraints);

        autoRunSource.add(autoRunPromptInEditorRadioButton);
        autoRunPromptInEditorRadioButton.setText("Autorun prompt in editor");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        autorunOptionsPanelInner.add(autoRunPromptInEditorRadioButton, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        autoRunSource.add(autoRunFileRadioButton);
        autoRunFileRadioButton.setText("Autorun prompt in file");
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

        promptEditor.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        editorScroller.setViewportView(promptEditor);

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

        tabPanel.addTab("Prompt", editorPanel);

        consolePanel.setLayout(new java.awt.GridBagLayout());

        ollamaConsole.setLayout(new java.awt.GridBagLayout());

        ollamaConsoleScrollPane.setViewportView(ollamaConsoleTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        ollamaConsole.add(ollamaConsoleScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        consolePanel.add(ollamaConsole, gridBagConstraints);

        tabPanel.addTab("Ollama console", consolePanel);

        add(tabPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void executeBtnexecuteOnMouseRelease(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_executeBtnexecuteOnMouseRelease
        tabPanel.setSelectedComponent(consolePanel);
        executePromptInEditor();
}//GEN-LAST:event_executeBtnexecuteOnMouseRelease

    private void newBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newBtnMouseReleased
        newPrompt();
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
        else if(autoRunPromptInEditorRadioButton.isSelected()) autorun = AUTORUN_PROMPT_IN_EDITOR;
        else if(autoRunFileRadioButton.isSelected()) autorun = AUTORUN_FILE;
        autorunPromptFile = autoRunFileTextField.getText();
        
        autoloadFromOccurrence = autoloadCheckBox.isSelected();
        
        if(options != null) {
            options.put(optionsPrefix+".autorun", ""+autorun);
            options.put(optionsPrefix+".autorunPromptFile", autorunPromptFile);
            options.put(optionsPrefix+".autoload", Boolean.toString(autoloadFromOccurrence));
        }
    }//GEN-LAST:event_optionsOkButtonMouseReleased

    private void optionsBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionsBtnMouseReleased
        openOptionsDialog();
    }//GEN-LAST:event_optionsBtnMouseReleased

    private void openBtnMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openBtnMouseReleased

    }//GEN-LAST:event_openBtnMouseReleased

    private void openBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openBtnMousePressed
        showMenu(openPromptMenuStruct, evt);
    }//GEN-LAST:event_openBtnMousePressed

    private void saveBtnMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveBtnMousePressed
        showMenu(savePromptMenuStruct, evt);
    }//GEN-LAST:event_saveBtnMousePressed

    private void autoRunFileBrowseButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_autoRunFileBrowseButtonMouseReleased
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Select Ollama Prompt");
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
    private javax.swing.JRadioButton autoRunPromptInEditorRadioButton;
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
    private javax.swing.JPanel ollamaConsole;
    private javax.swing.JScrollPane ollamaConsoleScrollPane;
    private javax.swing.JTextPane ollamaConsoleTextPane;
    private javax.swing.JEditorPane promptEditor;
    private javax.swing.JPanel runButtonPanel;
    private javax.swing.JButton saveBtn;
    private javax.swing.JTabbedPane tabPanel;
    // End of variables declaration//GEN-END:variables

    
    
    
    
    
    private void openOptionsDialog() {
        optionsDialog = new JDialog(Wandora.getWandora(), true);
        optionsDialog.setSize(500,270);
        optionsDialog.add(optionsPanel);
        optionsDialog.setTitle("Ollama topic panel options");
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
            promptEditor.setText(getPromptOccurrence());
        }
        autorun();
    }

    
    private void readOptions() {
        autoloadFromOccurrence = options.getBoolean(optionsPrefix+".autoload", autoloadFromOccurrence);
        autoloadCheckBox.setSelected(autoloadFromOccurrence);
        
        autorun = options.getInt(optionsPrefix+".autorun", 0);
        autorunPromptFile = options.get(optionsPrefix+".autorunPromptFile");
        autoRunFileTextField.setText(autorunPromptFile);
        
        currentPrompt = options.get(optionsPrefix+".currentPrompt");

        switch(autorun) {
            case DONT_AUTORUN: { noAutoRunRadioButton.setSelected(true); break; }
            case AUTORUN_OCCURRENCE: { autoRunOccurrenceRadioButton.setSelected(true); break; }
            case AUTORUN_PROMPT_IN_EDITOR: { autoRunPromptInEditorRadioButton.setSelected(true); break; }
            case AUTORUN_FILE: { autoRunFileRadioButton.setSelected(true); break; }
        }
    }
    
    
    
    
    private void autorun() {
        String autorunPrompt = null;
        switch(autorun) {
            case AUTORUN_OCCURRENCE: {
                autorunPrompt = getPromptOccurrence();
                break; 
            }
            case AUTORUN_PROMPT_IN_EDITOR: { 
                autorunPrompt = promptEditor.getText();
                break; 
            }
            case AUTORUN_FILE: {
                try {
                    if(autorunPromptFile != null && autorunPromptFile.length() > 0) {
                       autorunPrompt = IObox.loadFile(autorunPromptFile);
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                break; 
            }
        }
        if(autorunPrompt != null) {
            executePrompt(autorunPrompt);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    private String getPromptOccurrence() {
        return getPromptOccurrence(rootTopic);
    }
    
    
    private String getPromptOccurrence(Topic t) {
        String o = null;
        if(t != null) {
            try {
                Topic otype = tm.getTopic(PROMPT_OCCURRENCE_TYPE);
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
    
    
    
    private void setPromptOccurrence(String o) {
        setPromptOccurrence(rootTopic, o);
    }
    
    private void setPromptOccurrence(Topic t, String o) {
        if(t != null) {
            try {
                Topic otype = tm.getTopic(PROMPT_OCCURRENCE_TYPE);
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
        saveCurrentPromptToOptions();
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
        saveCurrentPromptToOptions();
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
        return UIBox.getIcon("gui/icons/topic_panel_llamacpp.png");
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
            "New prompt", this,
            "Open prompt", this,
            new Object[] {
                "Open prompt from occurrence", this,
                "Open prompt from file...", this,
            },
            "Save prompt", this,
            new Object[] {
                "Save prompt to occurrence", this,
                "Save prompt to file...", this,
            },
            "Run prompt", this,
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
        return "Ollama";
    }
    
    @Override
    public String getTitle() {
        if(rootTopic != null) return TopicToString.toString(rootTopic);
        else return getName();
    }
    
    
    @Override
    public int getOrder() {
        return 2000;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String ac = e.getActionCommand();
        if("New prompt".equalsIgnoreCase(ac)) {
            newPrompt();
        }
        else if("Open prompt from occurrence".equalsIgnoreCase(ac)) {
            loadPromptFromOccurrence();
        }
        else if("Open prompt from file...".equalsIgnoreCase(ac)) {
            loadPromptFromFile();
        }
        else if("Save prompt to occurrence".equalsIgnoreCase(ac)) {
            savePromptToOccurrence();
        }
        else if("Save prompt to file...".equalsIgnoreCase(ac)) {
            savePromptToFile();
        }
        else if("Options...".equalsIgnoreCase(ac)) {
            openOptionsDialog();
        }
        else if("Run prompt".equalsIgnoreCase(ac)) {
            executePromptInEditor();
        }
    }

    
    
    // -------------------------------------------------------------------------
    
    
    private void newPrompt() {
        int answer = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Erase current prompt in editor?", "Erase prompt in editor?", WandoraOptionPane.YES_NO_OPTION);
        if(answer == WandoraOptionPane.YES_OPTION) {
            currentPromptSource = NO_SOURCE;
            currentPromptFile = null;
            promptEditor.setText("");
        }
    }
    
    
    
    
    public void executePromptInEditor() {
        String prompt = promptEditor.getText();
        executePrompt(prompt);
    }
    
    public void executePrompt(String prompt) {
        if(prompt != null) {
            if(prompt.trim().length() > 0) {
                String newline = "\n";
                //String commands[] = prompt.split(newline);
                //prompt = prompt.replaceAll("(?m)^[ \t]*\r?\n","");
                prompt = prompt.replace("'","\'");
                //prompt = prompt.replaceAll("\r\n", ";");
                prompt = prompt.replace("\n", "\\n");
                prompt = prompt.replace("\r", "\\r");
                prompt = prompt.replace("\t", "\\t");
                SimpleTextConsole console = (SimpleTextConsole) ollamaConsoleTextPane;
                tabPanel.setSelectedComponent(consolePanel);
                console.handleInput(prompt);
            }
        }
    }
    
    
    
    private void loadPromptFromOccurrence() {
        try {
            Topic otype = tm.getTopic(PROMPT_OCCURRENCE_TYPE);
            Topic olang = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
            if(otype != null && olang != null) {
                currentPromptFile = null;
                String o = rootTopic.getData(otype, olang);
                if(o != null) {
                    promptEditor.setText(o);
                }
                else {
                    WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find R occurrence in current topic. Can't restore prompt from occurrence.", "Can't restore prompt", WandoraOptionPane.INFORMATION_MESSAGE);
                }
            }
            else {
                if(otype == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find R occurrence type. Can't restore prompt from occurrence.", "Can't find prompt occurrence type", WandoraOptionPane.INFORMATION_MESSAGE);
                if(olang == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find Language independent scope topic. Can't restore prompt from occurrence.", "Can't find Language independent scope topic", WandoraOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    
    private void loadPromptFromFile() {
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setDialogTitle("Open Prompt");
        int answer = fc.showDialog(Wandora.getWandora(), "Open");
        if(answer == SimpleFileChooser.APPROVE_OPTION) {
            File promptFile = fc.getSelectedFile();
            try {
                String prompt = IObox.loadFile(promptFile);
                currentPromptFile = promptFile.getAbsolutePath();
                if(prompt != null) {
                    promptEditor.setText(prompt);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while restoring prompt from file '"+promptFile.getName()+"'.", "Can't restore prompt", WandoraOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    

    
    
    
    private void savePromptToOccurrence() {
        String prompt = promptEditor.getText();
        try {
            Topic otype = tm.getTopic(PROMPT_OCCURRENCE_TYPE);
            Topic olang = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
            if(otype != null && olang != null) {
                boolean storeToOccurrence = true;
                currentPromptSource = OCCURRENCE_SOURCE;
                String oldPrompt = rootTopic.getData(otype, olang);
                if(oldPrompt != null) {
                    int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Current topic contains prompt occurrence already. Storing current prompt erases older. Do you want to store the prompt to the occurrence?","Topic already has a prompt occurrence", WandoraOptionPane.INFORMATION_MESSAGE);
                    if(a != WandoraOptionPane.YES_OPTION) storeToOccurrence = false;
                }
                if(storeToOccurrence) {
                    rootTopic.setData(otype, olang, prompt);
                }
            }
            else {
                if(otype == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find prompt occurrence type. Can't save prompt to occurrence.", "Can't find R occurrence type", WandoraOptionPane.INFORMATION_MESSAGE);
                if(olang == null) WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Can't find Language independent scope topic. Can't save prompt to occurrence.", "Can't find Language independent scope topic", WandoraOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while storing the prompt to an occurrence to current topic.", "Can't store prompt", WandoraOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    private void savePromptToFile() {
        File promptFile = null;
        try {
            fc.setDialogTitle("Save prompt");
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            int answer = fc.showDialog(Wandora.getWandora(), "Save");
            if(answer == SimpleFileChooser.APPROVE_OPTION) {
                promptFile = fc.getSelectedFile();
                String promptCode = promptEditor.getText();
                FileUtils.writeStringToFile(promptFile, promptCode, "UTF-8");
                currentPromptSource = FILE_SOURCE;
            }
        }
        catch(Exception e) {
            WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Exception '"+e.getMessage()+"' occurred while storing prompt to file '"+promptFile.getName()+"'.", "Can't save prompt", WandoraOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    
    
    private void savePrompt() {
        if(currentPromptSource == OCCURRENCE_SOURCE) {
            savePromptToOccurrence();
        }
        else if(currentPromptSource == FILE_SOURCE) {
            savePromptToFile();
        }
    }
    
    
    private void saveCurrentPromptToOptions() {
        if(options != null) {
            options.put(optionsPrefix+".currentPrompt", promptEditor.getText());
        }
        if(USE_LOCAL_OPTIONS && SAVE_SKETCH_TO_GLOBAL_OPTIONS) {
            try {
                Wandora.getWandora().getOptions().put(optionsPrefix+".currentPrompt", promptEditor.getText());
            }
            catch(Exception e) {
                
            }
        }
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    

    public String handleInput(final String input) {
        System.out.println("HANDLE INPUT: "+input);
        capturedOutput = new StringBuilder("");
        
        Runnable handleInputRunnable = new Runnable() {
        	final StringBuilder runnablesCapturedOutput = capturedOutput;
        	
			@Override
			public void run() {
				Wandora.getWandora().setAnimated(true, this);
				if(shouldOutputInput) {
		        	output(input);
		        	if(!input.endsWith("\n")) {
		        		output("\n");
		        	}
		        }
		        try {
		        	OllamaAsyncResultStreamer resultStreamer =
		                    ollama.generateAsync(ollamaModel, input, false, ThinkMode.ENABLED);
		        	while (true) {
		        		try {
		        			Thread.sleep(ollamaPollIntervalMilliseconds);
		        		}
		        		catch(Exception es) {}
		        		try {
		        			if (!resultStreamer.isAlive()) break;
			                String responseTokens = resultStreamer.getResponseStream().poll();
			                if(responseTokens != null) {
			                	runnablesCapturedOutput.append(responseTokens);
			                	output(responseTokens);
			                }
		        		}
		        		catch(Exception e1) {
		        			output(ExceptionUtils.getStackTrace(e1));
		        		}
		            }
		        	output("\n> ");
		        }
		        catch (Exception e2) {
		        	output(ExceptionUtils.getStackTrace(e2));
				}
		        finally {
		        	Wandora.getWandora().setAnimated(false, this);
		        }
			}
        };
        
        new Thread(handleInputRunnable).start();

        System.out.println("EXITING HANDLE INPUT");
        return "";
    }

    

    
    
    
    public void output(String output) {
    	System.out.println("OUTPUT: "+output);
        ((SimpleTextConsole) ollamaConsoleTextPane).output(output);
        ((SimpleTextConsole) ollamaConsoleTextPane).refresh();
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
        saveCurrentPromptToOptions();
        revalidate();
        repaint();
    }


    
    
    // -------------------------------------------------------------------------
    
    
    
    
    
    
    
    
    
}
