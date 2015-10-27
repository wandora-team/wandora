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
 * InfoDialog.java
 *
 * Created on 30. toukokuuta 2006, 17:08
 */

package org.wandora.application.gui;


import org.wandora.utils.ClipboardBox;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.simple.*;


/**
 *
 * @author  akivela
 */
public class InfoDialog extends JDialog implements WandoraToolLogger, TopicMapLogger, ActionListener, MouseListener {
    
    private Wandora wandora;
    public boolean locked = false;
    public boolean forceStop = false;
    private int state = 0;
    
    private StringBuilder history = null;
    
    private long startTime = 0;
    private long endTime = 0;
    private int maximumProgress = 100;




    /**
     * Creates new form InfoDialog
     */
    public InfoDialog(Wandora wandora) {
        super(wandora, true);
        this.history = new StringBuilder();
        this.wandora = wandora;
        initComponents();
        this.setSize(600, 300);
        wandora.centerWindow(this);
        textArea.addMouseListener(this);
        textArea.setComponentPopupMenu(getCopyMenu());
        textArea.setText("<html></html>");
        textArea.setText("");
        iconLabel.setIcon(UIBox.getIcon("gui/icons/dialog/cogwheel.gif"));
        setState(EXECUTE);
    }
    
    
    public void open() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }
    
    
    
    public void waitUntilVisible() {
        int c = 0;
        do {
            c++;
            try { Thread.sleep(40); }
            catch(Exception e) {};
        }
        while(!isVisible() && c < 100);
    }
    
    
   
    @Override
    public void lockLog(boolean lock) {
        locked = lock;
    }
    
    
    @Override
    public void hlog(String message) {
        try {
            if(!locked) textArea.setText("<html>"+message+"</html>");
        }
        catch(Exception e) {} 
    }
    
    
    @Override
    public void log(String message) {
        try {
            if(!locked) textArea.setText("<html>"+message+"</html>");
        }
        catch(Exception e) {}       
        history.append(message).append("\n");
    }
    
    
    @Override
    public void log(String message, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log(message + "\n" + sw.toString());
    }
    
    @Override
    public void log(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log(sw.toString());
    }
    
    @Override
    public void log(Error e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log(sw.toString());
    }
    

    
    @Override
    public void setLogTitle(String title) {
        this.setTitle(title);
    }
    
    
    
    @Override
    public void setProgress(int n) {
        if(progressBar.isIndeterminate()) progressBar.setIndeterminate(false);
        if(n > maximumProgress) n = n % maximumProgress;
        progressBar.setValue(n);
    }
    @Override
    public void setProgressMax(int maxn) {
        maximumProgress = Math.max(1, maxn);
        progressBar.setMaximum(maxn);
    }
    
    
    
    @Override
    public String getHistory() {
        return history.toString();
    }
    
    
 
    
    
    public long getExecuteTime() {
        return endTime - startTime;
    }
    
    
    @Override
    public boolean forceStop() {
        return forceStop;
    }
    
    
    @Override
    public void setState(int state) {
        if(this.state == state) return;
        this.state = state;
        switch(state) {
            case EXECUTE: {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        startTime = System.currentTimeMillis();
                        textArea.setText("");
                        progressBar.setIndeterminate(true);
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        containerPanel.removeAll();
                        containerPanel.add(processPanel, BorderLayout.CENTER);
                        containerPanel.revalidate();
                        forceStop = false;
                        open();
                    }
                });
                waitUntilVisible();
                return;
            }
            case WAIT: {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        endTime = System.currentTimeMillis();
                        try {
                            containerPanel.removeAll();
                        }
                        catch(Exception e) {
                            // e.printStackTrace();
                        }
                        containerPanel.add(waitPanel, BorderLayout.CENTER);
                        String historyString = getHistory();
                        //history = new StringBuffer();
                        logTextPane.setText(historyString);
                        logTextPane.setCaretPosition(logTextPane.getDocument().getLength());
                        containerPanel.revalidate();
                    }
                });
                return;
            }
            case CLOSE: {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setVisible(false);
                        logTextPane.setText("");
                        //history = new StringBuffer();
                        forceStop = false;
                    }
                });
                return;
            }
            case INVISIBLE: {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setVisible(false);
                    }
                });
                return;
            }
            case VISIBLE: {
                open();
                waitUntilVisible();
                return;
            }
        }
    }
    @Override
    public int getState() {
        return state;
    }
    
    
    
    public JPopupMenu getCopyMenu() {
        Object[] copyMenuObjects = new Object[] {
            "Copy", UIBox.getIcon("gui/icons/copy.png"),
        };
        return UIBox.makePopupMenu(copyMenuObjects, this);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        waitPanel = new javax.swing.JPanel();
        scrollContainer = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        logTextPane = new SimpleTextPane();
        waitButtonPanel = new javax.swing.JPanel();
        closeButton = new SimpleButton();
        containerPanel = new javax.swing.JPanel();
        processPanel = new javax.swing.JPanel();
        iconLabel = new javax.swing.JLabel();
        textArea = new org.wandora.application.gui.simple.SimpleLabel();
        progressBar = new javax.swing.JProgressBar();
        buttonPanel = new javax.swing.JPanel();
        stopButton = new org.wandora.application.gui.simple.SimpleButton();

        waitPanel.setLayout(new java.awt.GridBagLayout());

        scrollContainer.setBackground(new java.awt.Color(255, 255, 255));
        scrollContainer.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

        ((SimpleTextPane) logTextPane).setLineWrap(false);
        jScrollPane1.setViewportView(logTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        scrollContainer.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        waitPanel.add(scrollContainer, gridBagConstraints);

        waitButtonPanel.setLayout(new java.awt.GridBagLayout());

        closeButton.setText("Close");
        closeButton.setMaximumSize(new java.awt.Dimension(70, 23));
        closeButton.setMinimumSize(new java.awt.Dimension(70, 23));
        closeButton.setPreferredSize(new java.awt.Dimension(70, 23));
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        waitButtonPanel.add(closeButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        waitPanel.add(waitButtonPanel, gridBagConstraints);

        setTitle("Processing...");

        containerPanel.setLayout(new java.awt.BorderLayout());

        processPanel.setLayout(new java.awt.GridBagLayout());

        iconLabel.setMaximumSize(new java.awt.Dimension(65, 65));
        iconLabel.setMinimumSize(new java.awt.Dimension(65, 65));
        iconLabel.setPreferredSize(new java.awt.Dimension(65, 65));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 8, 2);
        processPanel.add(iconLabel, gridBagConstraints);

        textArea.setText("text");
        textArea.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 9, 8, 9);
        processPanel.add(textArea, gridBagConstraints);

        progressBar.setMinimumSize(new java.awt.Dimension(10, 10));
        progressBar.setPreferredSize(new java.awt.Dimension(150, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 8, 20);
        processPanel.add(progressBar, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        stopButton.setText("Stop");
        stopButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        stopButton.setMaximumSize(new java.awt.Dimension(70, 23));
        stopButton.setMinimumSize(new java.awt.Dimension(70, 23));
        stopButton.setPreferredSize(new java.awt.Dimension(70, 23));
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(stopButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 6, 8);
        processPanel.add(buttonPanel, gridBagConstraints);

        containerPanel.add(processPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(containerPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(false);
            }
        });
    }//GEN-LAST:event_closeButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        forceStop = true;
    }//GEN-LAST:event_stopButtonActionPerformed
    

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane logTextPane;
    private javax.swing.JPanel processPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel scrollContainer;
    private javax.swing.JButton stopButton;
    private javax.swing.JLabel textArea;
    private javax.swing.JPanel waitButtonPanel;
    private javax.swing.JPanel waitPanel;
    // End of variables declaration//GEN-END:variables
    
    
    
    
    
    // ---- actions -----
    
    
    @Override
    public void actionPerformed(ActionEvent event) {
        String c = event.getActionCommand();
        
        if("Copy".equalsIgnoreCase(c)) {
            ClipboardBox.setClipboard(textArea.getText());
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
