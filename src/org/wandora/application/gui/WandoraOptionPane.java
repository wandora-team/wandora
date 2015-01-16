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
 * WandoraOptionPane.java
 *
 * Created on 14. kesäkuuta 2006, 11:16
 */

package org.wandora.application.gui;

import org.wandora.utils.ClipboardBox;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.wandora.application.Wandora;




/**
 *
 * @author  akivela
 */
public class WandoraOptionPane extends javax.swing.JPanel implements ActionListener, MouseListener {
    public static final int YES_OPTION = 1100;
    public static final int YES_TO_ALL_OPTION = 1102;
    public static final int NO_OPTION = 1110;
    public static final int CANCEL_OPTION = 1120;
    public static final int OK_OPTION = 1130;
    public static final int CLOSED_OPTION = 1190;
    
    public static final int DEFAULT_OPTION = 1240;
    public static final int YES_NO_OPTION = 1200;
    public static final int YES_TO_ALL_NO_OPTION = 1202;
    public static final int YES_NO_CANCEL_OPTION = 1210;
    public static final int YES_TO_ALL_NO_CANCEL_OPTION = 1212;
    public static final int OK_CANCEL_OPTION = 1230;
    
    public static final int ERROR_MESSAGE = 2100;
    public static final int INFORMATION_MESSAGE = 2200;
    public static final int WARNING_MESSAGE = 2300;
    public static final int PLAIN_MESSAGE = 2400;
    public static final int QUESTION_MESSAGE = 2500;
    
    
    public static Icon askIcon = UIBox.getIcon("gui/icons/dialog/ask.gif");
    public static Icon informIcon = UIBox.getIcon("gui/icons/dialog/inform.gif");
    public static Icon questionIcon = UIBox.getIcon("gui/icons/dialog/question.gif");
    public static Icon warnIcon = UIBox.getIcon("gui/icons/dialog/warn.gif");
    
    
    private static WandoraOptionPane optionPane = new WandoraOptionPane();
    private static JDialog dialog = null;
    
    
    private static int answer = 0;
    private static String inputAnswer = null;
    private static Object optionAnswer = null;
    
    
    
    /** Creates new form WandoraOptionPane */
    public WandoraOptionPane() {
        initComponents();
        messageLabel.addMouseListener(this);
        messageLabel.setComponentPopupMenu(getCopyMenu());
    }
    
    
    
    public JPopupMenu getCopyMenu() {
        Object[] copyMenuObjects = new Object[] {
            "Copy", UIBox.getIcon("gui/icons/copy.png"),
        };
        return UIBox.makePopupMenu(copyMenuObjects, this);
    }
    
    
  
    // -------------------------------------------------------------------------
    // ------------------------------------------------------- CONFIRM PANEL ---
    // -------------------------------------------------------------------------
    
    
    
    
    
    public static int showConfirmDialog(Component parent, String message) {
        return showConfirmDialog(parent, message, "Confirm", DEFAULT_OPTION);
    }
    
    public static int showConfirmDialog(Component parent, String message, String title) {
        return showConfirmDialog(parent, message, title, DEFAULT_OPTION);
    }
    
    
    public static int showConfirmDialog(Component parent, String message, String title, int type) {
        initConfirmPanel(parent, message);
        dialog.setTitle(title);
                
        switch(type) {
            case YES_NO_OPTION: {
                optionPane.confirmCancelButton.setVisible(false);
                optionPane.confirmOkButton.setVisible(false);
                optionPane.confirmOkButton.requestFocus();
                break;
            }
            case YES_TO_ALL_NO_OPTION: {
                optionPane.confirmCancelButton.setVisible(false);
                optionPane.confirmOkButton.setVisible(false);
                optionPane.confirmYesToAllButton.setVisible(true);
                optionPane.confirmOkButton.requestFocus();
                break;
            }
            case YES_NO_CANCEL_OPTION: {
                optionPane.confirmOkButton.setVisible(false);
                optionPane.confirmOkButton.requestFocus();
                break;
            }
            case YES_TO_ALL_NO_CANCEL_OPTION: {
                optionPane.confirmOkButton.setVisible(false);
                optionPane.confirmYesToAllButton.setVisible(true);
                optionPane.confirmYesToAllButton.requestFocus();
                break;
            }
            case OK_CANCEL_OPTION: {
                optionPane.confirmIconLabel.setIcon(informIcon);
                optionPane.confirmYesButton.setVisible(false);
                optionPane.confirmNoButton.setVisible(false);
                optionPane.confirmYesButton.requestFocus();
                break;
            }
            default: {
                optionPane.confirmCancelButton.setVisible(false);
                optionPane.confirmOkButton.setVisible(false);
                optionPane.confirmOkButton.requestFocus();
                break;
            }
        }

        centerDialog(parent, dialog);
        answer = CLOSED_OPTION;
        dialog.setVisible(true);
        return answer;
    }
    
    
    
    public static void initConfirmPanel(Component parent, String message) {
        if(parent instanceof JDialog) {
            dialog = new JDialog((JDialog) parent, true);
        }
        else if(parent instanceof Frame) {
            dialog = new JDialog((Frame) parent, true);
        }
        else {
            dialog = new JDialog((Frame) null, true);
        }
        setDialogSize(message);
        
        centerDialog(parent, dialog);
        dialog.setLayout(new BorderLayout());
        dialog.add(optionPane.confirmPanel, BorderLayout.CENTER);
        
        optionPane.confirmMessageLabel.setText(makeHTML( message.toString() ));
        
        optionPane.confirmIconLabel.setIcon(questionIcon);
        optionPane.confirmOkButton.setVisible(true);
        optionPane.confirmCancelButton.setVisible(true);
        optionPane.confirmYesToAllButton.setVisible(false);
        optionPane.confirmYesButton.setVisible(true);
        optionPane.confirmNoButton.setVisible(true);
        dialog.toFront();
    }
    
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------ MESSAGE DIALOG ---
    // -------------------------------------------------------------------------
    
    
    
    
    public static void showMessageDialog(Component parent, String message, int type) {
        WandoraOptionPane.showMessageDialog(parent, message, null, type, null);
    } 
    public static void showMessageDialog(Component parent, String message) {
        WandoraOptionPane.showMessageDialog(parent, message, null, INFORMATION_MESSAGE, null);
    } 
    public static void showMessageDialog(Component parent, String message, String title) {
        WandoraOptionPane.showMessageDialog(parent, message, title, INFORMATION_MESSAGE, null);
    } 
    
    public static void showMessageDialog(Component parent, String message, String title, int type) {
        WandoraOptionPane.showMessageDialog(parent, message, title, type, null);
    }
    
    public static void showMessageDialog(Component parent, String message, String title, int type, Icon icon) {
        initMessagePanel(parent, message);
        if(title != null) {
            dialog.setTitle(title);
        }
        if(icon != null) {
            optionPane.messageIconLabel.setIcon(icon);
        }
        switch(type) {
            case ERROR_MESSAGE: {
                if(icon == null) optionPane.messageIconLabel.setIcon(warnIcon);
                if(title == null) dialog.setTitle("Error");
                break;
            }
            case WARNING_MESSAGE: {
                if(icon == null) optionPane.messageIconLabel.setIcon(warnIcon);
                if(title == null) dialog.setTitle("Warning");
                break;
            }
            case PLAIN_MESSAGE: {
                if(icon == null) optionPane.messageIconLabel.setIcon(informIcon);
                if(title == null) dialog.setTitle("Message");
                break;
            }
            case INFORMATION_MESSAGE: {
                if(icon == null) optionPane.messageIconLabel.setIcon(informIcon);
                if(title == null) dialog.setTitle("Message");
                break;
            }
            default: {
                if(icon == null) optionPane.messageIconLabel.setIcon(informIcon);
                if(title == null) dialog.setTitle("Message");
                break;
            }
        }
        centerDialog(parent, dialog);
        dialog.setVisible(true);
    }
    
    
       
    public static void initMessagePanel(Component parent, String message) {
        if(parent instanceof JDialog) {
            dialog = new JDialog((JDialog) parent, true);
        }
        else if(parent instanceof Frame) {
            dialog = new JDialog((Frame) parent, true);
        }
        else {
            dialog = new JDialog((Frame) null, true);
        }
        setDialogSize(message);

        centerDialog(parent, dialog);
        dialog.setLayout(new BorderLayout());
        dialog.add(optionPane.messagePanel, BorderLayout.CENTER);
        
        optionPane.messageLabel.setText(makeHTML( message.toString() ));
        optionPane.messageIconLabel.setIcon(questionIcon);
        dialog.toFront();
        optionPane.messageOkButton.requestFocus();
    }
        
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------- INPUT DIALOG ---
    // -------------------------------------------------------------------------
    
    
    public static String showInputDialog(Component parent, String message) {
        return WandoraOptionPane.showInputDialog(parent, message, "", "Input", QUESTION_MESSAGE);
    }
    public static String showInputDialog(String message) {
        return WandoraOptionPane.showInputDialog(null, message, "", "Input", QUESTION_MESSAGE);
    }
    public static String showInputDialog(String message, String initialText) {
        return WandoraOptionPane.showInputDialog(null, message, initialText, "Input", QUESTION_MESSAGE);
    }
    public static String showInputDialog(Component parent, String message, String initialText) {
        return WandoraOptionPane.showInputDialog(parent, message, initialText, "Input", QUESTION_MESSAGE);
    }
    public static String showInputDialog(Component parent, String message, String initialText, String title) {
        return WandoraOptionPane.showInputDialog(parent, message, initialText, title, QUESTION_MESSAGE);
    }
    public static String showInputDialog(Component parent, String message, String initialText, int type) {
        return WandoraOptionPane.showInputDialog(parent, message, initialText, "Input", type);
    }
    public static String showInputDialog(Component parent, String message, String initialText, String title, int type) {
        initInputPanel(parent, message, title, initialText);
        centerDialog(parent, dialog);
        inputAnswer = null;
        dialog.setVisible(true);
        return inputAnswer;
    }

    
    public static void initInputPanel(Component parent, String message, String title, String initialText) {
        if(parent instanceof JDialog) {
            dialog = new JDialog((JDialog) parent, true);
        }
        else if(parent instanceof Frame) {
            dialog = new JDialog((Frame) parent, true);
        }
        else {
            dialog = new JDialog((Frame) null, true);
        }
        if(title == null) title = "Input";
        dialog.setTitle(title);
        dialog.setSize(450, 180);
        centerDialog(parent, dialog);
        dialog.setLayout(new BorderLayout());
        dialog.add(optionPane.inputPanel, BorderLayout.CENTER);
        
        optionPane.inputMessageLabel.setText(makeHTML( message.toString() ));
        if(initialText == null) initialText = "";
        optionPane.inputField.setText(initialText);
        
        optionPane.inputIconLabel.setIcon(askIcon);
        dialog.toFront();
        optionPane.inputField.requestFocus();
    }
        
    
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------- OPTION DIALOG ---
    // -------------------------------------------------------------------------
    
    
    
    
     public static String showOptionDialog(Component parent, String message, String title, int type, String[] options, String initialValue) {
        initOptionPanel(parent, message, title, options, initialValue);
        centerDialog(parent, dialog);
        optionAnswer = null;
        dialog.setVisible(true);
        return (String)optionAnswer;
     }
     
     public static Object showOptionDialog(Component parent, String message, String title, int type, Object[] options, Object initialValue) {
        initOptionPanel(parent, message, title, options, initialValue);
        centerDialog(parent, dialog);
        optionAnswer = null;
        dialog.setVisible(true);
        return optionAnswer;
     }

    
    public static void initOptionPanel(Component parent, String message, String title, Object[] options, Object initialValue) {
        if(parent instanceof JDialog) {
            dialog = new JDialog((JDialog) parent, true);
        }
        else if(parent instanceof Frame) {
            dialog = new JDialog((Frame) parent, true);
        }
        else {
            dialog = new JDialog((Frame) null, true);
        }
        if(title == null) title = "Select";
        dialog.setTitle(title);
        dialog.setSize(450, 180);
        centerDialog(parent, dialog);
        dialog.setLayout(new BorderLayout());
        dialog.add(optionPane.optionPanel, BorderLayout.CENTER);
        
        optionPane.optionMessageLabel.setText(makeHTML( message.toString() ));
        
        optionPane.optionsComboBox.setEditable(false);
        optionPane.optionsComboBox.removeAllItems();
        for(int i=0; i<options.length; i++) {
            optionPane.optionsComboBox.addItem(options[i]);
        }
        optionPane.optionsComboBox.setSelectedItem(initialValue);
        
        optionPane.optionsIconLabel.setIcon(questionIcon);
        dialog.toFront();
    }
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    private static void setDialogSize(String message) {
        if(dialog != null && message != null) {
            if(message.length() > 1000) dialog.setSize(800, 500);
            else if(message.length() > 500) dialog.setSize(650, 400);
            else if(message.length() > 250) dialog.setSize(600, 200);
            else if(message.length() > 150) dialog.setSize(550, 180);
            else dialog.setSize(550, 180);
        }
    }
    
    
    
    private static void centerDialog(Component parent, JDialog d) {
        if(d != null) {
            if(parent == null) parent = Wandora.getWandora();
            if(parent != null) {
                int x = parent.getX()+parent.getWidth()/2-d.getWidth()/2;
                int y = parent.getY()+parent.getHeight()/2-d.getHeight()/2;
                d.setLocation(x, y);
            }
            else {
                try {
                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    int x = dim.width/2-d.getWidth()/2;
                    int y = dim.height/2-d.getHeight()/2;
                    d.setLocation(x, y);
                }
                catch(Exception e) { /*PASS SILENTLY*/}
            }
        }
    }
    
    
    private static String makeHTML(String txt) {
        if(txt != null && txt.trim().startsWith("<html>")) return txt.trim();
        else return "<html>" + txt + "</html>";
    }
    
    
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        confirmPanel = new javax.swing.JPanel();
        confirmIconPanel = new javax.swing.JPanel();
        confirmIconLabel = new javax.swing.JLabel();
        confirmMessagePanel = new javax.swing.JPanel();
        confirmMessageLabel = new org.wandora.application.gui.simple.SimpleLabel();
        confirmButtonPanel = new javax.swing.JPanel();
        confirmEmptyPanel = new javax.swing.JPanel();
        confirmYesToAllButton = new org.wandora.application.gui.simple.SimpleButton();
        confirmYesButton = new org.wandora.application.gui.simple.SimpleButton();
        confirmNoButton = new org.wandora.application.gui.simple.SimpleButton();
        confirmOkButton = new org.wandora.application.gui.simple.SimpleButton();
        confirmCancelButton = new org.wandora.application.gui.simple.SimpleButton();
        inputPanel = new javax.swing.JPanel();
        inputIconPanel = new javax.swing.JPanel();
        inputIconLabel = new javax.swing.JLabel();
        inputMessagePanel = new javax.swing.JPanel();
        inputMessageLabel = new org.wandora.application.gui.simple.SimpleLabel();
        inputFieldPanel = new javax.swing.JPanel();
        inputField = new org.wandora.application.gui.simple.SimpleField();
        inputButtonPanel = new javax.swing.JPanel();
        inputEmptyPanel = new javax.swing.JPanel();
        inputOkButton = new org.wandora.application.gui.simple.SimpleButton();
        inputCancelButton = new org.wandora.application.gui.simple.SimpleButton();
        messagePanel = new javax.swing.JPanel();
        messageIconPanel = new javax.swing.JPanel();
        messageIconLabel = new javax.swing.JLabel();
        messageMessagePanel = new javax.swing.JPanel();
        messageLabel = new org.wandora.application.gui.simple.SimpleLabel();
        messageButtonPanel = new javax.swing.JPanel();
        messageEmptyPanel = new javax.swing.JPanel();
        messageOkButton = new org.wandora.application.gui.simple.SimpleButton();
        optionPanel = new javax.swing.JPanel();
        optionIconPanel = new javax.swing.JPanel();
        optionsIconLabel = new javax.swing.JLabel();
        optionMessagePanel = new javax.swing.JPanel();
        optionMessageLabel = new org.wandora.application.gui.simple.SimpleLabel();
        optionOptionsPanel = new javax.swing.JPanel();
        optionsComboBox = new org.wandora.application.gui.simple.SimpleComboBox();
        optionButtonPanel = new javax.swing.JPanel();
        optionEmptyPanel = new javax.swing.JPanel();
        optionsOkButton = new org.wandora.application.gui.simple.SimpleButton();

        confirmPanel.setLayout(new java.awt.GridBagLayout());

        confirmIconPanel.setMinimumSize(new java.awt.Dimension(65, 65));
        confirmIconPanel.setPreferredSize(new java.awt.Dimension(65, 65));
        confirmIconPanel.setLayout(new java.awt.BorderLayout());

        confirmIconLabel.setMinimumSize(new java.awt.Dimension(65, 65));
        confirmIconLabel.setPreferredSize(new java.awt.Dimension(65, 65));
        confirmIconPanel.add(confirmIconLabel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        confirmPanel.add(confirmIconPanel, gridBagConstraints);

        confirmMessagePanel.setLayout(new java.awt.BorderLayout());

        confirmMessageLabel.setText("Confirm message");
        confirmMessagePanel.add(confirmMessageLabel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        confirmPanel.add(confirmMessagePanel, gridBagConstraints);

        confirmButtonPanel.setLayout(new java.awt.GridBagLayout());

        confirmEmptyPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        confirmButtonPanel.add(confirmEmptyPanel, gridBagConstraints);

        confirmYesToAllButton.setText("Yes to all");
        confirmYesToAllButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        confirmYesToAllButton.setMaximumSize(new java.awt.Dimension(70, 23));
        confirmYesToAllButton.setMinimumSize(new java.awt.Dimension(70, 23));
        confirmYesToAllButton.setPreferredSize(new java.awt.Dimension(70, 23));
        confirmYesToAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmYesToAllButtonActionPerformed(evt);
            }
        });
        confirmYesToAllButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                confirmYesToAllButtonKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        confirmButtonPanel.add(confirmYesToAllButton, gridBagConstraints);

        confirmYesButton.setText("Yes");
        confirmYesButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        confirmYesButton.setMaximumSize(new java.awt.Dimension(70, 23));
        confirmYesButton.setMinimumSize(new java.awt.Dimension(70, 23));
        confirmYesButton.setPreferredSize(new java.awt.Dimension(70, 23));
        confirmYesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmYesButtonActionPerformed(evt);
            }
        });
        confirmYesButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                confirmYesButtonKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        confirmButtonPanel.add(confirmYesButton, gridBagConstraints);

        confirmNoButton.setText("No");
        confirmNoButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        confirmNoButton.setMaximumSize(new java.awt.Dimension(70, 23));
        confirmNoButton.setMinimumSize(new java.awt.Dimension(70, 23));
        confirmNoButton.setPreferredSize(new java.awt.Dimension(70, 23));
        confirmNoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmNoButtonActionPerformed(evt);
            }
        });
        confirmButtonPanel.add(confirmNoButton, new java.awt.GridBagConstraints());

        confirmOkButton.setText("OK");
        confirmOkButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        confirmOkButton.setMaximumSize(new java.awt.Dimension(70, 23));
        confirmOkButton.setMinimumSize(new java.awt.Dimension(70, 23));
        confirmOkButton.setPreferredSize(new java.awt.Dimension(70, 23));
        confirmOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmOkButtonActionPerformed(evt);
            }
        });
        confirmOkButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                confirmOkButtonKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        confirmButtonPanel.add(confirmOkButton, gridBagConstraints);

        confirmCancelButton.setText("Cancel");
        confirmCancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        confirmCancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        confirmCancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        confirmCancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        confirmCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmCancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        confirmButtonPanel.add(confirmCancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        confirmPanel.add(confirmButtonPanel, gridBagConstraints);

        inputPanel.setLayout(new java.awt.GridBagLayout());

        inputIconPanel.setPreferredSize(new java.awt.Dimension(65, 65));
        inputIconPanel.setLayout(new java.awt.BorderLayout());

        inputIconLabel.setMaximumSize(new java.awt.Dimension(65, 65));
        inputIconLabel.setMinimumSize(new java.awt.Dimension(65, 65));
        inputIconLabel.setPreferredSize(new java.awt.Dimension(65, 65));
        inputIconPanel.add(inputIconLabel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 0);
        inputPanel.add(inputIconPanel, gridBagConstraints);

        inputMessagePanel.setLayout(new java.awt.BorderLayout());

        inputMessageLabel.setText("Message");
        inputMessageLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        inputMessagePanel.add(inputMessageLabel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        inputPanel.add(inputMessagePanel, gridBagConstraints);

        inputFieldPanel.setLayout(new java.awt.GridBagLayout());

        inputField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                inputFieldKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        inputFieldPanel.add(inputField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        inputPanel.add(inputFieldPanel, gridBagConstraints);

        inputButtonPanel.setLayout(new java.awt.GridBagLayout());

        inputEmptyPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        inputButtonPanel.add(inputEmptyPanel, gridBagConstraints);

        inputOkButton.setText("OK");
        inputOkButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        inputOkButton.setMaximumSize(new java.awt.Dimension(70, 23));
        inputOkButton.setMinimumSize(new java.awt.Dimension(70, 23));
        inputOkButton.setPreferredSize(new java.awt.Dimension(70, 23));
        inputOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputOkButtonActionPerformed(evt);
            }
        });
        inputOkButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                inputOkButtonKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        inputButtonPanel.add(inputOkButton, gridBagConstraints);

        inputCancelButton.setText("Cancel");
        inputCancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        inputCancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        inputCancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        inputCancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        inputCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputCancelButtonActionPerformed(evt);
            }
        });
        inputButtonPanel.add(inputCancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        inputPanel.add(inputButtonPanel, gridBagConstraints);

        messagePanel.setLayout(new java.awt.GridBagLayout());

        messageIconPanel.setMaximumSize(new java.awt.Dimension(65, 65));
        messageIconPanel.setMinimumSize(new java.awt.Dimension(65, 65));
        messageIconPanel.setLayout(new java.awt.BorderLayout());

        messageIconLabel.setMaximumSize(new java.awt.Dimension(65, 65));
        messageIconLabel.setMinimumSize(new java.awt.Dimension(65, 65));
        messageIconLabel.setPreferredSize(new java.awt.Dimension(65, 65));
        messageIconPanel.add(messageIconLabel, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        messagePanel.add(messageIconPanel, gridBagConstraints);

        messageMessagePanel.setLayout(new java.awt.BorderLayout());

        messageLabel.setText("Message");
        messageMessagePanel.add(messageLabel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 10);
        messagePanel.add(messageMessagePanel, gridBagConstraints);

        messageButtonPanel.setPreferredSize(new java.awt.Dimension(10, 23));
        messageButtonPanel.setLayout(new java.awt.GridBagLayout());

        messageEmptyPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        messageButtonPanel.add(messageEmptyPanel, gridBagConstraints);

        messageOkButton.setText("OK");
        messageOkButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        messageOkButton.setMaximumSize(new java.awt.Dimension(70, 23));
        messageOkButton.setMinimumSize(new java.awt.Dimension(70, 23));
        messageOkButton.setPreferredSize(new java.awt.Dimension(70, 23));
        messageOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageOkButtonActionPerformed(evt);
            }
        });
        messageOkButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                messageOkButtonKeyReleased(evt);
            }
        });
        messageButtonPanel.add(messageOkButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        messagePanel.add(messageButtonPanel, gridBagConstraints);

        optionPanel.setLayout(new java.awt.GridBagLayout());

        optionIconPanel.setLayout(new java.awt.BorderLayout());

        optionsIconLabel.setMaximumSize(new java.awt.Dimension(65, 65));
        optionsIconLabel.setMinimumSize(new java.awt.Dimension(65, 65));
        optionsIconLabel.setPreferredSize(new java.awt.Dimension(65, 65));
        optionIconPanel.add(optionsIconLabel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 5);
        optionPanel.add(optionIconPanel, gridBagConstraints);

        optionMessagePanel.setLayout(new java.awt.BorderLayout());

        optionMessageLabel.setText("Message");
        optionMessageLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        optionMessagePanel.add(optionMessageLabel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        optionPanel.add(optionMessagePanel, gridBagConstraints);

        optionOptionsPanel.setLayout(new java.awt.GridBagLayout());

        optionsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        optionsComboBox.setMinimumSize(new java.awt.Dimension(51, 21));
        optionsComboBox.setPreferredSize(new java.awt.Dimension(55, 21));
        optionsComboBox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                optionsComboBoxKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        optionOptionsPanel.add(optionsComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        optionPanel.add(optionOptionsPanel, gridBagConstraints);

        optionButtonPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        optionButtonPanel.add(optionEmptyPanel, gridBagConstraints);

        optionsOkButton.setText("OK");
        optionsOkButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        optionsOkButton.setMaximumSize(new java.awt.Dimension(70, 23));
        optionsOkButton.setMinimumSize(new java.awt.Dimension(70, 23));
        optionsOkButton.setPreferredSize(new java.awt.Dimension(70, 23));
        optionsOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsOkButtonActionPerformed(evt);
            }
        });
        optionButtonPanel.add(optionsOkButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        optionPanel.add(optionButtonPanel, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private void confirmYesToAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmYesToAllButtonActionPerformed
        answer = YES_TO_ALL_OPTION;
        dialog.setVisible(false);
    }//GEN-LAST:event_confirmYesToAllButtonActionPerformed

    private void optionsOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsOkButtonActionPerformed
        optionAnswer = optionPane.optionsComboBox.getSelectedItem();
        dialog.setVisible(false);
    }//GEN-LAST:event_optionsOkButtonActionPerformed

    private void confirmOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmOkButtonActionPerformed
        answer = OK_OPTION;
        dialog.setVisible(false);
    }//GEN-LAST:event_confirmOkButtonActionPerformed

    private void inputCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputCancelButtonActionPerformed
        dialog.setVisible(false);
    }//GEN-LAST:event_inputCancelButtonActionPerformed

    private void inputOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputOkButtonActionPerformed
        inputAnswer = optionPane.inputField.getText();
        dialog.setVisible(false);
    }//GEN-LAST:event_inputOkButtonActionPerformed

    private void messageOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageOkButtonActionPerformed
        dialog.setVisible(false);
    }//GEN-LAST:event_messageOkButtonActionPerformed

    private void confirmCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmCancelButtonActionPerformed
        answer = CANCEL_OPTION;
        dialog.setVisible(false);
    }//GEN-LAST:event_confirmCancelButtonActionPerformed

    private void confirmNoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmNoButtonActionPerformed
        answer = NO_OPTION;
        dialog.setVisible(false);
    }//GEN-LAST:event_confirmNoButtonActionPerformed

    private void confirmYesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmYesButtonActionPerformed
        answer = YES_OPTION;
        dialog.setVisible(false);
    }//GEN-LAST:event_confirmYesButtonActionPerformed

    private void messageOkButtonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_messageOkButtonKeyReleased
        if(KeyEvent.VK_ENTER == evt.getKeyCode()) {
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_messageOkButtonKeyReleased

    private void confirmOkButtonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_confirmOkButtonKeyReleased
        if(KeyEvent.VK_ENTER == evt.getKeyCode()) {
            answer = OK_OPTION;
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_confirmOkButtonKeyReleased

    private void confirmYesButtonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_confirmYesButtonKeyReleased
        if(KeyEvent.VK_ENTER == evt.getKeyCode()) {
            answer = YES_OPTION;
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_confirmYesButtonKeyReleased

    private void confirmYesToAllButtonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_confirmYesToAllButtonKeyReleased
        if(KeyEvent.VK_ENTER == evt.getKeyCode()) {
            answer = YES_TO_ALL_OPTION;
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_confirmYesToAllButtonKeyReleased

    private void inputFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inputFieldKeyReleased
        if(KeyEvent.VK_ENTER == evt.getKeyCode()) {
            inputAnswer = optionPane.inputField.getText();
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_inputFieldKeyReleased

    private void inputOkButtonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inputOkButtonKeyReleased
        if(KeyEvent.VK_ENTER == evt.getKeyCode()) {
            inputAnswer = optionPane.inputField.getText();
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_inputOkButtonKeyReleased

    private void optionsComboBoxKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_optionsComboBoxKeyReleased
        if(KeyEvent.VK_ENTER == evt.getKeyCode()) {
            optionAnswer = optionPane.optionsComboBox.getSelectedItem();
            dialog.setVisible(false);
        }
    }//GEN-LAST:event_optionsComboBoxKeyReleased
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel confirmButtonPanel;
    private javax.swing.JButton confirmCancelButton;
    private javax.swing.JPanel confirmEmptyPanel;
    private javax.swing.JLabel confirmIconLabel;
    private javax.swing.JPanel confirmIconPanel;
    private javax.swing.JLabel confirmMessageLabel;
    private javax.swing.JPanel confirmMessagePanel;
    private javax.swing.JButton confirmNoButton;
    private javax.swing.JButton confirmOkButton;
    private javax.swing.JPanel confirmPanel;
    private javax.swing.JButton confirmYesButton;
    private javax.swing.JButton confirmYesToAllButton;
    private javax.swing.JPanel inputButtonPanel;
    private javax.swing.JButton inputCancelButton;
    private javax.swing.JPanel inputEmptyPanel;
    private javax.swing.JTextField inputField;
    private javax.swing.JPanel inputFieldPanel;
    private javax.swing.JLabel inputIconLabel;
    private javax.swing.JPanel inputIconPanel;
    private javax.swing.JLabel inputMessageLabel;
    private javax.swing.JPanel inputMessagePanel;
    private javax.swing.JButton inputOkButton;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JPanel messageButtonPanel;
    private javax.swing.JPanel messageEmptyPanel;
    private javax.swing.JLabel messageIconLabel;
    private javax.swing.JPanel messageIconPanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPanel messageMessagePanel;
    private javax.swing.JButton messageOkButton;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JPanel optionButtonPanel;
    private javax.swing.JPanel optionEmptyPanel;
    private javax.swing.JPanel optionIconPanel;
    private javax.swing.JLabel optionMessageLabel;
    private javax.swing.JPanel optionMessagePanel;
    private javax.swing.JPanel optionOptionsPanel;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JComboBox optionsComboBox;
    private javax.swing.JLabel optionsIconLabel;
    private javax.swing.JButton optionsOkButton;
    // End of variables declaration//GEN-END:variables
 
    
    
    
    
    
    
    
    // ---- actions -----
    
    
    @Override
    public void actionPerformed(ActionEvent event) {
        String c = event.getActionCommand();
        
        if("Copy".equalsIgnoreCase(c)) {
            ClipboardBox.setClipboard(messageLabel.getText());
        }
    }
    
    // ---- mouse -----
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
    }    
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }    
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    
    
}
