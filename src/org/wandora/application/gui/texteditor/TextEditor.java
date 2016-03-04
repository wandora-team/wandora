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
 * TextEditor.java
 *
 * Created on 5. toukokuuta 2006, 20:29
 */

package org.wandora.application.gui.texteditor;


import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.*;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.*;
import org.wandora.utils.*;


/**
 *
 * @author  akivela
 */
public class TextEditor extends javax.swing.JDialog implements ActionListener {
    
    public static final int MAX_TEXT_SIZE = 999999;

    public static final String optionPrefix = "textEditor.";
    
    protected boolean acceptChanges;
    private boolean shouldWrapLines = true;
    private String fontFace = "Sans Serif";
    private int fontSize = 12;
    private boolean inverseColors = false;
    
    protected SimpleTextPane simpleTextPane = null;

    protected Wandora wandora = null;
    
    protected JMenu fileMenu;
    protected JMenu editMenu;
    protected JMenu formatMenu;

    protected JDialog replaceDialog= null;
    
   
    
    
    public TextEditor(Wandora wandora, boolean modal, String initText) {
        this(wandora,modal,initText,null);
    }
    
    
    public TextEditor(Wandora wandora, boolean modal, String initText, String contentType) {
        super(wandora, modal);
        this.wandora = wandora;
        initComponents();
        simpleTextPane = (SimpleTextPane) textPane;
        if(contentType!=null) simpleTextPane.setContentType(contentType);
        simpleTextPane.setForeground(Color.BLACK);
        simpleTextPane.setBackground(Color.WHITE);
        simpleTextPane.setSuperText(initText);
        simpleTextPane.setCaretPosition(0);
        simpleTextPane.setFocusTraversalKeysEnabled(false);
        initWindow(wandora);
        infoLabel.setText("Editing text");
    }
    
    
    /** Creates new form TextEditor */
    public TextEditor(Wandora wandora, boolean modal) {
        super(wandora, modal);
        this.wandora = wandora;
        initComponents();
        initWindow(wandora);
    }
    
    
    
    public void setCancelButtonVisible(boolean v){
        cancelButton.setVisible(v);
    }
    

    public String getOptionsPrefix(){
        return optionPrefix;
    }
    
    public void initMenuBar(){
        menuBar.add(getFileMenu());
        menuBar.add(getEditMenu());
        menuBar.add(getFormatMenu());
        JMenu[] userMenus = getUserMenus();
        if(userMenus != null) {
            for (JMenu m : userMenus) {
                if (m != null) {
                    menuBar.add(m);
                }
            }
        }
    }
    
    public JMenu[] getUserMenus() {
        return null;
    }
    
    public void initWindow(Wandora wandora) {
        findPanel.setVisible(false);
        replacePanel.setVisible(false);
        
        Options options = wandora.options;
        if(options != null) {
            try {
                fontFace = options.get(getOptionsPrefix()+"font.face", "Sans Serif");
                fontSize = options.getInt(getOptionsPrefix()+"font.size", 12);
                inverseColors = options.getBoolean(getOptionsPrefix()+"inverseColors", false);
                
                updateEditorFont();
                updateEditorColors();
            }
            catch(Exception e) {}
        }
        initMenuBar();
        
        placeWindow();
    }
    
    
    
    public void placeWindow() {
        boolean placedSuccessfully = false;
        Options options = wandora.options;
        if(options != null) {
            try {
                int x = Integer.parseInt(options.get(getOptionsPrefix()+"window.x"));
                int y = Integer.parseInt(options.get(getOptionsPrefix()+"window.y"));
                int width = Integer.parseInt(options.get(getOptionsPrefix()+"window.width"));
                int height = Integer.parseInt(options.get(getOptionsPrefix()+"window.height"));
                if(x > 0 && y > 0 && width > 0 && height > 0) {
                    super.setSize(width, height);
                    super.setLocation(x, y);
                    placedSuccessfully = true;
                }
            }
            catch (Exception e) {
                System.out.println("No options for text editor. Using defaults!");
            }
        }
        if(!placedSuccessfully) {
            Dimension preferred = new Dimension(700,400);
            String initText = textPane.getText();
            if(initText != null) { 
                if(initText.length() > 1000) preferred = new Dimension(900,500);
                else if(initText.length() > 500) preferred = new Dimension(800,400);
            }
            this.setSize(preferred);
            wandora.centerWindow(this);
        }
    }
    
    
    
    public void exitTextEditor(boolean acceptingChanges) {
        this.acceptChanges=acceptingChanges;
        try {
            Dimension d = super.getSize();
            Point l = super.getLocation();
            Options options = wandora.options;
            if(options != null) {
                options.put(getOptionsPrefix()+"window.width", d.width);
                options.put(getOptionsPrefix()+"window.height", d.height);
                options.put(getOptionsPrefix()+"window.x", this.getX());
                options.put(getOptionsPrefix()+"window.y", this.getY());
                options.put(getOptionsPrefix()+"font.face", this.fontFace);
                options.put(getOptionsPrefix()+"font.size", this.fontSize);
                options.put(getOptionsPrefix()+"inverseColors", inverseColors ? 1 : 0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        this.setVisible(false);
    }
    
    
    public boolean acceptChanges() {
        return acceptChanges;
    }
    
    
    public JMenu getFileMenu() {
        fileMenu = new SimpleMenu("File", (Icon) null);
        Object[] menuStructure = new Object[] {
            "Open...", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/file_open.png"),
            "Save...", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/file_save.png"),
            "---",
            "Print...", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/print.png"),
            "---",
            "Close", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/exit.png"), 
        };
        fileMenu.removeAll();
        UIBox.attachMenu( fileMenu, menuStructure, this );
        return fileMenu;
    }
    
    
    
    public JMenu getEditMenu() {
        editMenu = new SimpleMenu("Edit", (Icon) null);

        Object[] menuStructure = new Object[] {
            "Undo", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/undo_undo.png"),
            "Redo", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/undo_redo.png"),
            "---",
            "Cut",  KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/cut.png"),
            "Copy",  KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/copy.png"),
            "Paste",  KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/paste.png"),
            "Clear", UIBox.getIcon("gui/icons/clear.png"),
            "Trim", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/trim.png"),
            "---",
            "Select all", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/select_all.png"),
            "---",
            "Find...", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/find_generic.png"),
            "Replace...", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK), UIBox.getIcon("gui/icons/replace.png"),
            "---",
            "Encode HTML entities", 
            "Decode HTML entities",
            "Strip HTML tags",
            "Make HTML clean up", 
            "Make XML clean up",
            "Prettyprint JSON",
            "---",
            "Uppercase",
            "Lowercase",
        };
        editMenu.removeAll();
        UIBox.attachMenu( editMenu, menuStructure, this );
        return editMenu;
    }
    
    
    public JMenu getFormatMenu() {
        formatMenu = new SimpleMenu("Format", (Icon) null);
        updateFormatMenu();
        return formatMenu;
    }
    public void updateFormatMenu() {
        Object[] menuStructure = new Object[] {
            (shouldWrapLines ? "X " : "") + "Wrap lines", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK), 
            "---",
            "Font", new Object[] {
                ("Sans Serif".equalsIgnoreCase(fontFace) ? "X " : "") + "Sans Serif",
                ("Serif".equalsIgnoreCase(fontFace) ? "X " : "") + "Serif",
                ("Monospaced".equalsIgnoreCase(fontFace) ? "X " : "") + "Monospaced"
            },
            "Font size", new Object[] {
                (fontSize == 9 ? "X " : "") + "9 px",
                (fontSize == 10 ? "X " : "") + "10 px",
                (fontSize == 12 ? "X " : "") + "12 px",
                (fontSize == 14 ? "X " : "") + "14 px",
                (fontSize == 16 ? "X " : "") + "16 px",
                (fontSize == 18 ? "X " : "") + "18 px",
                (fontSize == 20 ? "X " : "") + "20 px",
                (fontSize == 30 ? "X " : "") + "30 px"
            },
            "---",
            "Inverse colors"
        };
        formatMenu.removeAll();
        UIBox.attachMenu( formatMenu, menuStructure, this );
    }
    

    public String getText() {
        return textPane.getText();
    }
    public String getSelectedText() {
        return this.textPane.getSelectedText();
    }
    public void setText(String text) {
        textPane.setText(text);
    }
    public void setSuperText(String text) {
        ((SimpleTextPane) textPane).setSuperText(text);
    }
    public void setContentType(String contentType) {
        textPane.setContentType(contentType);
    }
    
    
    // -------------------------------------------------------------------------
    // ----------------------------------------------------- EXECUTE ACTIONS ---
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        if(actionEvent == null) return;
        String c = actionEvent.getActionCommand();
        if(c == null) return;
        
        try {
            // --- Edit -------------------------------
            if("Trim".equalsIgnoreCase(c)) {
                String changeThis = simpleTextPane.getText();
                simpleTextPane.setText(changeThis.trim());
            }
            else if("Uppercase".equalsIgnoreCase(c)) {
                String changeThis = simpleTextPane.getSelectedOrAllText();
                simpleTextPane.replaceSelectedOrAllText(changeThis.toUpperCase());
            }
            else if("Lowercase".equalsIgnoreCase(c)) {
                String changeThis = simpleTextPane.getSelectedOrAllText();
                simpleTextPane.replaceSelectedOrAllText(changeThis.toLowerCase());
            }
            else if("Encode HTML entities".equalsIgnoreCase(c)) {
                String changeThis = simpleTextPane.getSelectedOrAllText();
                simpleTextPane.replaceSelectedOrAllText(HTMLEntitiesCoder.encode(changeThis));
            }
            else if("Decade HTML entities".equalsIgnoreCase(c)) {
                String changeThis = simpleTextPane.getSelectedOrAllText();
                simpleTextPane.replaceSelectedOrAllText(HTMLEntitiesCoder.decode(changeThis));
            }
            else if("Make HTML clean up".equalsIgnoreCase(c)) {
                String changeThis = simpleTextPane.getText();

                org.w3c.tidy.Tidy tidy = new org.w3c.tidy.Tidy();
                tidy.setTidyMark(false);
                ByteArrayOutputStream tidyOutput = null;
                tidyOutput = new ByteArrayOutputStream();       
                tidy.parse(new ByteArrayInputStream(changeThis.getBytes()), tidyOutput);
                
                simpleTextPane.setText(tidyOutput.toString());
            }
            else if("Strip HTML tags".equalsIgnoreCase(c)) {
                String changeThis = simpleTextPane.getText();
                simpleTextPane.setText(XMLbox.naiveGetAsText(changeThis));
            }
            else if("Make XML clean up".equalsIgnoreCase(c)) {
                String changeThis = simpleTextPane.getText();

                org.w3c.tidy.Tidy tidy = new org.w3c.tidy.Tidy();
                tidy.setTidyMark(false);
                tidy.setXmlOut(true);
                tidy.setXmlPi(true);
                ByteArrayOutputStream tidyOutput = null;
                tidyOutput = new ByteArrayOutputStream();       
                tidy.parse(new ByteArrayInputStream(changeThis.getBytes()), tidyOutput);
                
                simpleTextPane.setText(tidyOutput.toString());
            }
            else if("Prettyprint JSON".equalsIgnoreCase(c)) {
                String changeThis = simpleTextPane.getText();

                String prettyOutput;
                try {
                    JSONObject obj = new JSONObject(changeThis); // Try object
                    prettyOutput = obj.toString(2);
                    simpleTextPane.setText(prettyOutput);
                } catch (JSONException objEx) { // Not a JSON Object
                    try {
                        JSONArray arr = new JSONArray(changeThis); // Try array
                        prettyOutput = arr.toString(2);
                        simpleTextPane.setText(prettyOutput);
                    } catch (JSONException arrEx) { // Give up
                        WandoraOptionPane.showMessageDialog(Wandora.getWandora(),
                                "Couldn't parse given text as JSON",
                                "JSON parse error",
                                WandoraOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
            else if("undo".equalsIgnoreCase(c)) {
                try {
                    simpleTextPane.undo.undo();
                    infoLabel.setText("Undo OK!");
                }
                catch(Exception e) {
                    infoLabel.setText("Unable to undo!");
                }
            }
            else if("redo".equalsIgnoreCase(c)) {
                try {
                    simpleTextPane.undo.redo();
                    infoLabel.setText("Redo OK!");
                }
                catch(Exception e) {
                    infoLabel.setText("Unable to redo!");
                }
            }
            else if("cut".equalsIgnoreCase(c)) {
                simpleTextPane.cut();
                infoLabel.setText("Cut OK!");
            }
            else if("copy".equalsIgnoreCase(c)) {
                simpleTextPane.copy();
                infoLabel.setText("Copy OK!");
            }
            else if("paste".equalsIgnoreCase(c)) {
                simpleTextPane.paste();
                infoLabel.setText("Paste OK!");
            }
            else if("clear".equalsIgnoreCase(c)) {
                simpleTextPane.setText("");
                infoLabel.setText("Clear OK!");
            }
            else if("find...".equalsIgnoreCase(c)) {
                findPanel.setVisible(true);
                replacePanel.setVisible(false);
                findTextField.requestFocus();
            }
            else if("replace...".equalsIgnoreCase(c)) {
                findPanel.setVisible(true);
                replacePanel.setVisible(true);
                findTextField.requestFocus();
            }
            else if("select all".equalsIgnoreCase(c)) {
                simpleTextPane.selectAll();
                infoLabel.setText("Select all OK!");
            }

            // --- File -------------------------------
            else if("open...".equalsIgnoreCase(c)) {
                infoLabel.setText("Reading text from file!");
                simpleTextPane.load();
                infoLabel.setText("OK!");
            }
            else if("save...".equalsIgnoreCase(c)) {
                infoLabel.setText("Saving text to file!");
                simpleTextPane.save();
                infoLabel.setText("OK!");
            }
            else if("print...".equalsIgnoreCase(c)) {
                infoLabel.setText("Printing text!");
                try{
                    simpleTextPane.print();
                }
                catch(java.awt.print.PrinterException pe){
                    wandora.handleError(pe);
                }
            }
            else if("close".equalsIgnoreCase(c)) {
                //int a = WandoraOptionPane.showConfirmDialog(admin, "Accept changes?", "Accept changes?", WandoraOptionPane.OK_CANCEL_OPTION);
                JDialog acceptDialog = new JDialog(this, true);
                acceptDialog.setTitle("Accept changes?");
                acceptDialog.add(acceptPanel);
                acceptDialog.setSize(350, 180);
                wandora.centerWindow(acceptDialog, this);
                acceptDialog.setVisible(true);
            }

            // --- Format -------------------------
            else if("wrap lines".equalsIgnoreCase(c)) {
                wrapLines();
            }
            else if("Sans Serif".equalsIgnoreCase(c)) {
                fontFace = "Sans Serif";
                updateEditorFont();
            }
            else if("Serif".equalsIgnoreCase(c)) {
                fontFace = "Serif";
                updateEditorFont();
            }
            else if("Monospaced".equalsIgnoreCase(c)) {
                fontFace = "Monospaced";
                updateEditorFont();
            }
            else if(c.endsWith(" px")) {
                String[] parts = c.split(" ");
                String size = parts[0];
                try {
                    fontSize = Integer.parseInt(size);
                }
                catch(Exception e) {}
                updateEditorFont();
            }
            else if("Inverse colors".equalsIgnoreCase(c)) {
                inverseColors = !inverseColors;
                updateEditorColors();
            }
            else {
                System.out.println("Passing actionEvent '"+c+"' to SimpleTextPane.");
                simpleTextPane.actionPerformed(actionEvent);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    
    private void updateEditorFont() {
        simpleTextPane.setFont(new Font(fontFace, Font.PLAIN, fontSize));
        updateFormatMenu();
    }
    private void updateEditorColors() {
        Color background = Color.WHITE;
        Color foreground = Color.BLACK;
        if(inverseColors) {
            background = Color.BLACK;
            foreground = Color.WHITE;
        }
        simpleTextPane.setForeground(foreground);
        simpleTextPane.setBackground(background);
    }
    

    
    public void setCustomButtons(JComponent custom) {
        this.customButtons.add(custom);
    }
    
   
    public void wrapLines() {
        wrapLines(!shouldWrapLines);
    }
    
    public void wrapLines(boolean shouldWrap) {
        shouldWrapLines = shouldWrap;
        updateFormatMenu();
        simpleTextPane.setLineWrap(shouldWrapLines);
        if(shouldWrapLines) infoLabel.setText("Line wrap set ON");
        else infoLabel.setText("Line wrap set OFF");
    }
    
    
    public void refreshCaretInfo() {
        try {
            int pos = simpleTextPane.getCaretPosition();
            int len = simpleTextPane.getDocument().getLength();
            infoLabel.setText("" + pos + "/" + len);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        acceptPanel = new javax.swing.JPanel();
        acceptLabel = new org.wandora.application.gui.simple.SimpleLabel();
        jPanel1 = new javax.swing.JPanel();
        acceptButton = new org.wandora.application.gui.simple.SimpleButton();
        rejectButton = new org.wandora.application.gui.simple.SimpleButton();
        editorPanel = new javax.swing.JPanel();
        toolPanel = new javax.swing.JPanel();
        findPanel = new javax.swing.JPanel();
        findTextField = new org.wandora.application.gui.simple.SimpleField();
        findButton = new org.wandora.application.gui.simple.SimpleButton();
        replacePanel = new javax.swing.JPanel();
        replaceLabel = new org.wandora.application.gui.simple.SimpleLabel();
        replaceTextField = new org.wandora.application.gui.simple.SimpleField();
        replaceButton = new org.wandora.application.gui.simple.SimpleButton();
        replaceAllButton = new org.wandora.application.gui.simple.SimpleButton();
        jSeparator1 = new javax.swing.JSeparator();
        findCloseButton = new org.wandora.application.gui.simple.SimpleButton();
        centerPanel = new javax.swing.JPanel();
        scrollPane = new SimpleScrollPane();
        textPane = new SimpleTextPane();
        buttonPanel = new javax.swing.JPanel();
        infoLabel = new org.wandora.application.gui.simple.SimpleLabel();
        customButtons = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();
        menuBar = new javax.swing.JMenuBar();

        acceptPanel.setLayout(new java.awt.GridBagLayout());

        acceptLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        acceptLabel.setText("Do you accept text changes?");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        acceptPanel.add(acceptLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        acceptPanel.add(jPanel1, gridBagConstraints);

        acceptButton.setText("Accept");
        acceptButton.setMargin(new java.awt.Insets(1, 14, 1, 14));
        acceptButton.setPreferredSize(new java.awt.Dimension(70, 21));
        acceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 2);
        acceptPanel.add(acceptButton, gridBagConstraints);

        rejectButton.setText("Reject");
        rejectButton.setMargin(new java.awt.Insets(1, 14, 1, 14));
        rejectButton.setPreferredSize(new java.awt.Dimension(70, 21));
        rejectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rejectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        acceptPanel.add(rejectButton, gridBagConstraints);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit text");
        setName("textEditorDialog"); // NOI18N

        editorPanel.setLayout(new java.awt.BorderLayout());

        toolPanel.setLayout(new java.awt.BorderLayout());

        findPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 3);
        findPanel.add(findTextField, gridBagConstraints);

        findButton.setFont(org.wandora.application.gui.UIConstants.smallButtonLabelFont);
        findButton.setText("Find");
        findButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        findButton.setMaximumSize(new java.awt.Dimension(50, 23));
        findButton.setMinimumSize(new java.awt.Dimension(50, 20));
        findButton.setPreferredSize(new java.awt.Dimension(50, 20));
        findButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        findPanel.add(findButton, gridBagConstraints);

        replacePanel.setLayout(new java.awt.GridBagLayout());

        replaceLabel.setFont(org.wandora.application.gui.UIConstants.smallButtonLabelFont);
        replaceLabel.setText("replace with");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        replacePanel.add(replaceLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        replacePanel.add(replaceTextField, gridBagConstraints);

        replaceButton.setFont(org.wandora.application.gui.UIConstants.smallButtonLabelFont);
        replaceButton.setText("Replace");
        replaceButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        replaceButton.setMaximumSize(new java.awt.Dimension(60, 23));
        replaceButton.setMinimumSize(new java.awt.Dimension(60, 20));
        replaceButton.setPreferredSize(new java.awt.Dimension(60, 20));
        replaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        replacePanel.add(replaceButton, gridBagConstraints);

        replaceAllButton.setFont(org.wandora.application.gui.UIConstants.smallButtonLabelFont);
        replaceAllButton.setText("All");
        replaceAllButton.setToolTipText("Replace All");
        replaceAllButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        replaceAllButton.setMaximumSize(new java.awt.Dimension(50, 23));
        replaceAllButton.setMinimumSize(new java.awt.Dimension(50, 20));
        replaceAllButton.setPreferredSize(new java.awt.Dimension(50, 20));
        replaceAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAllButtonActionPerformed(evt);
            }
        });
        replacePanel.add(replaceAllButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 0);
        findPanel.add(replacePanel, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 3, 7, 3);
        findPanel.add(jSeparator1, gridBagConstraints);

        findCloseButton.setFont(org.wandora.application.gui.UIConstants.smallButtonLabelFont);
        findCloseButton.setActionCommand("Close");
        findCloseButton.setLabel("Hide");
        findCloseButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        findCloseButton.setMaximumSize(new java.awt.Dimension(50, 23));
        findCloseButton.setMinimumSize(new java.awt.Dimension(50, 20));
        findCloseButton.setPreferredSize(new java.awt.Dimension(50, 20));
        findCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findCloseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        findPanel.add(findCloseButton, gridBagConstraints);

        toolPanel.add(findPanel, java.awt.BorderLayout.CENTER);

        editorPanel.add(toolPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.BorderLayout());

        scrollPane.setBackground(new java.awt.Color(255, 255, 255));

        textPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textChangeRegistered(evt);
            }
        });
        textPane.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                refreshCaretPositionInfo(evt);
            }
        });
        scrollPane.setViewportView(textPane);

        centerPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        editorPanel.add(centerPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(editorPanel, java.awt.BorderLayout.CENTER);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        infoLabel.setFont(org.wandora.application.gui.UIConstants.smallButtonLabelFont);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 5);
        buttonPanel.add(infoLabel, gridBagConstraints);

        customButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        buttonPanel.add(customButtons, gridBagConstraints);

        okButton.setText("OK");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.setMaximumSize(new java.awt.Dimension(70, 23));
        okButton.setMinimumSize(new java.awt.Dimension(70, 23));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                acceptEditedText(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelEdit(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 3, 4, 4);
        buttonPanel.add(cancelButton, gridBagConstraints);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rejectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rejectButtonActionPerformed
        exitTextEditor(false);
    }//GEN-LAST:event_rejectButtonActionPerformed

    private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
        exitTextEditor(true);
    }//GEN-LAST:event_acceptButtonActionPerformed

    private void replaceAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAllButtonActionPerformed
        int replaceCount = ((SimpleTextPane) textPane).findAndReplaceAll(findTextField.getText(), replaceTextField.getText());
        infoLabel.setText("Replaced "+replaceCount+ " OK!");
    }//GEN-LAST:event_replaceAllButtonActionPerformed

    private void replaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceButtonActionPerformed
        boolean found = ((SimpleTextPane) textPane).findAndReplaceNext(findTextField.getText(), replaceTextField.getText());
        if(found) infoLabel.setText("Replace OK!");
        else infoLabel.setText("No text replaced!");
    }//GEN-LAST:event_replaceButtonActionPerformed

    private void findCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findCloseButtonActionPerformed
        findPanel.setVisible(false);
        replacePanel.setVisible(false);
    }//GEN-LAST:event_findCloseButtonActionPerformed

    private void findActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findActionPerformed
        boolean found = ((SimpleTextPane) textPane).findAndSelectNext(findTextField.getText());
        if(found) infoLabel.setText("Found next!");
        else infoLabel.setText("No text found!");
    }//GEN-LAST:event_findActionPerformed

    private void refreshCaretPositionInfo(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_refreshCaretPositionInfo
        refreshCaretInfo();
    }//GEN-LAST:event_refreshCaretPositionInfo

    private void textChangeRegistered(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textChangeRegistered
        refreshCaretInfo();
    }//GEN-LAST:event_textChangeRegistered

    private void cancelEdit(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelEdit
        exitTextEditor(false);
    }//GEN-LAST:event_cancelEdit

    private void acceptEditedText(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_acceptEditedText
        exitTextEditor(true);
    }//GEN-LAST:event_acceptEditedText
    

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptButton;
    private javax.swing.JLabel acceptLabel;
    private javax.swing.JPanel acceptPanel;
    protected javax.swing.JPanel buttonPanel;
    protected javax.swing.JButton cancelButton;
    protected javax.swing.JPanel centerPanel;
    protected javax.swing.JPanel customButtons;
    protected javax.swing.JPanel editorPanel;
    protected javax.swing.JButton findButton;
    protected javax.swing.JButton findCloseButton;
    protected javax.swing.JPanel findPanel;
    protected javax.swing.JTextField findTextField;
    protected javax.swing.JLabel infoLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    protected javax.swing.JMenuBar menuBar;
    protected javax.swing.JButton okButton;
    private javax.swing.JButton rejectButton;
    protected javax.swing.JButton replaceAllButton;
    protected javax.swing.JButton replaceButton;
    protected javax.swing.JLabel replaceLabel;
    protected javax.swing.JPanel replacePanel;
    protected javax.swing.JTextField replaceTextField;
    protected javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextPane textPane;
    protected javax.swing.JPanel toolPanel;
    // End of variables declaration//GEN-END:variables
    
}
