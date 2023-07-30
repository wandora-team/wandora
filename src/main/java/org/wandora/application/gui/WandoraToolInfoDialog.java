/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
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
 * WandoraToolInfoDialog.java
 *
 * Created on 18. helmikuuta 2009, 18:22
 */

package org.wandora.application.gui;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolLogger;
import org.wandora.application.WandoraToolManager2.ToolInfo;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.simple.SimpleTextPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.utils.Tuples.T2;


/**
 *
 * @author  akivela
 */
public class WandoraToolInfoDialog extends javax.swing.JDialog {


	private static final long serialVersionUID = 1L;

	
	private Wandora wandora;
    
    
    /** Creates new form WandoraToolInfoDialog */
    public WandoraToolInfoDialog(Wandora w, WandoraTool tool, ToolInfo toolInfo) {
        super(w, true);
        wandora = w;
        initComponents();
        initToolInfo(tool,toolInfo);
        initToolLogs(tool);
        this.setSize(500, 250);
        org.wandora.utils.swing.GuiTools.centerWindow(this, wandora);
        this.setVisible(true);
    }
    
    
    

    public void initToolInfo(WandoraTool tool,ToolInfo toolInfo) {
        StringBuilder toolInfoText = new StringBuilder("");
        toolInfoText.append("Name:\n").append(tool.getName()).append("\n\n");
        toolInfoText.append("Class:\n").append(tool.getClass().toString()).append("\n\n");
        if(toolInfo!=null) toolInfoText.append("Source: ").append(toolInfo.sourceType).append(":").append(toolInfo.source).append("\n\n");
        toolInfoText.append("Description:\n").append(tool.getDescription()).append("\n\n");
        toolInfoText.append("Types:\n").append(tool.getType().toString()).append("\n\n");
        toolInfoText.append("Is configurable:\n").append(tool.isConfigurable()).append("\n\n");
        
        if(tool instanceof AbstractWandoraTool) {
            AbstractWandoraTool at = (AbstractWandoraTool) tool;
            toolInfoText.append("Allow multiple invocations:\n").append(at.allowMultipleInvocations()).append("\n\n");
            ArrayList<T2<Thread,Long>> threads = at.getThreads();
            if(threads.size() > 0) {
                toolInfoText.append("Running threads:\n");
                String dateStr = "";
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                for( T2<Thread,Long> timedThread : threads ) {
                    if(timedThread != null) {
                        dateStr = df.format(new Date(timedThread.e2.longValue()));
                        toolInfoText.append(timedThread.e1.toString() + " since " + dateStr + "\n");
                    }
                }
            }
            else {
                toolInfoText.append("No threads running at the moment.\n");
            }
        }
        
        toolInfoTextPane.setText(toolInfoText.toString());
        toolInfoTextPane.setCaretPosition(0);
    }

    
    public void initToolLogs(WandoraTool tool) {
        if(tool instanceof AbstractWandoraTool) {
            AbstractWandoraTool at = (AbstractWandoraTool) tool;
            WandoraToolLogger logger = at.getLastLogger();
            if(logger != null) {
                String logs = logger.getHistory();
                if(logs != null && logs.length() > 0) {
                    logTextPane.setText(logs);
                }
                else {
                    logTextPane.setText("No logs available!");
                }
            }
            else {
                logTextPane.setText("No logger available!");
            }
        }
        else {
            logTextPane.setText("Unable to restore tool logs!");
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

        toolInfoPanel = new javax.swing.JPanel();
        toolTabbedPane = new SimpleTabbedPane();
        infoPanel = new javax.swing.JPanel();
        toolInfoScrollPane = new SimpleScrollPane();
        toolInfoTextPane = new SimpleTextPane();
        logPanel = new javax.swing.JPanel();
        logScrollPane = new SimpleScrollPane();
        logTextPane = new SimpleTextPane();
        toolInfoButtonPanel = new javax.swing.JPanel();
        toolInfoCloseButton = new SimpleButton();

        setTitle("Tool Info");

        toolInfoPanel.setLayout(new java.awt.GridBagLayout());

        infoPanel.setLayout(new java.awt.GridBagLayout());

        toolInfoScrollPane.setViewportView(toolInfoTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        infoPanel.add(toolInfoScrollPane, gridBagConstraints);

        toolTabbedPane.addTab("Info", infoPanel);

        logPanel.setLayout(new java.awt.GridBagLayout());

        logScrollPane.setViewportView(logTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        logPanel.add(logScrollPane, gridBagConstraints);

        toolTabbedPane.addTab("Logs", logPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        toolInfoPanel.add(toolTabbedPane, gridBagConstraints);

        toolInfoButtonPanel.setLayout(new java.awt.GridBagLayout());

        toolInfoCloseButton.setText("Close");
        toolInfoCloseButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        toolInfoCloseButton.setPreferredSize(new java.awt.Dimension(60, 21));
        toolInfoCloseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                toolInfoCloseButtonMousePressed(evt);
            }
        });
        toolInfoButtonPanel.add(toolInfoCloseButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        toolInfoPanel.add(toolInfoButtonPanel, gridBagConstraints);

        getContentPane().add(toolInfoPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void toolInfoCloseButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toolInfoCloseButtonMousePressed
    this.setVisible(false);
}//GEN-LAST:event_toolInfoCloseButtonMousePressed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel infoPanel;
    private javax.swing.JPanel logPanel;
    private javax.swing.JScrollPane logScrollPane;
    private javax.swing.JTextPane logTextPane;
    private javax.swing.JPanel toolInfoButtonPanel;
    private javax.swing.JButton toolInfoCloseButton;
    private javax.swing.JPanel toolInfoPanel;
    private javax.swing.JScrollPane toolInfoScrollPane;
    private javax.swing.JTextPane toolInfoTextPane;
    private javax.swing.JTabbedPane toolTabbedPane;
    // End of variables declaration//GEN-END:variables

}
