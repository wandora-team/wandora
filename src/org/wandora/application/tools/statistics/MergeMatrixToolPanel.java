/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * MergeMatrixToolPanel.java
 *
 */
package org.wandora.application.tools.statistics;

import javax.swing.JDialog;
import javax.swing.table.TableModel;
import org.wandora.application.gui.UIBox;
import org.wandora.application.Wandora;
import java.util.*;
import javax.swing.event.TableModelListener;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleRadioButton;
import org.wandora.utils.ClipboardBox;
import org.wandora.application.tools.statistics.MergeMatrixCellRenderer;

/**
 *
 * @author
 * Eero
 */


public class MergeMatrixToolPanel extends javax.swing.JPanel {
    
    private List<List> data = null;
    private String html = null;
    private String delim = null;
    private JDialog dialog = null;
    private TableModel tableModel = null;
    private MergeMatrixCellRenderer renderer = null;

    /**
     * Creates
     * new
     * form
     * MergeMatrixToolPanel
     */
    
    public MergeMatrixToolPanel(Wandora w,List<List> data) {
        /*
         * The data is structured in the following manner:
         * [
         *  [layer #0, layer #0 value #1, layer #0 value #2,...],
         *  [layer #1, layer #1 value #1, layer #1 value #2,...],
         *  .
         *  .
         *  .
         * ]
         */
        this.data = data;
        this.html = this.dataToHtml();
        String[] colNames = new String[this.data.size()+1];
        colNames[0] = "";
        Object[][] tableData = new Object[this.data.size()+1][];
        tableData[0] = new Object[this.data.size()+1];
        for(int i = 0;i<this.data.size();i++){
            colNames[i+1] = (String)this.data.get(i).get(0);
            tableData[0][i+1] = (String)this.data.get(i).get(0);
            
            tableData[i+1] = new Object[this.data.get(i).size()];
            for(int j = 0; j < this.data.get(i).size(); j++){
                tableData[i+1][j] = this.data.get(i).get(j);
            }
        }
        this.tableModel = new javax.swing.table.DefaultTableModel(tableData,colNames);
        this.renderer = new MergeMatrixCellRenderer();
        initComponents();
        MMTPDelimGroup.add(MMTPDelimTab);
        MMTPDelimGroup.add(MMTPDelimDot);
        dialog = new JDialog(w,true);
        dialog.add(this);
        dialog.setSize(800,350);
        dialog.setTitle("Merge matrix tool");
        UIBox.centerWindow(dialog, w);
        dialog.setVisible(true);
    }
    
    private String dataToHtml(){
        StringBuilder sb = new StringBuilder();
        String cellWidth = Double.toString(100.0 / this.data.get(0).size()) + "%";
        sb.append("<html>\n")
          .append("\t<body>\n")
          .append("\t\t<table>\n")
          .append("\t\t\t<th>\n");
        for(List row : this.data){
            sb.append("\t\t\t\t<td>\n")
              .append("\t\t\t\t\t").append(row.get(0)).append("\n")
              .append("\t\t\t\t</td>\n");
        }
        for(List row : this.data){
            sb.append("\t\t\t<tr>\n");
            for(Object cell : row){
                String s = (String)cell;
                sb.append("\t\t\t\t<td>\n")
                  .append("\t\t\t\t\t").append(s).append("\n")
                  .append("\t\t\t\t</td>\n");
            }
            sb.append("\t\t\t<tr>\n");
        }
        sb.append("\t\t</table>\n")
          .append("\t</body>\n")
          .append("</html>\n");
        String out = sb.toString();
        return out;
    }
    
    public void close() {
        if(dialog != null) {
            dialog.setVisible(false);
        }
    }
    
    private void delimToClipboard(){
        this.delim = "";
        String delimeter = (MMTPDelimDot.isSelected()) ? ";" : "\t";
        String eol = System.getProperty("line.separator");
        delim += delimeter;
        for (List row : data) {
            delim += (String)row.get(0) + delimeter;
        }
        delim += eol;
        for (List row : data){
            for (Object cell : row){
                String s  = (String)cell;
                delim += s + delimeter;
            }
            delim += eol;
        }
        ClipboardBox.setClipboard(delim);
    }
    
    private void htmlToClipboard(){
        ClipboardBox.setClipboard(this.html);
    }

    /**
     * This
     * method
     * is
     * called
     * from
     * within
     * the
     * constructor
     * to
     * initialize
     * the
     * form.
     * WARNING:
     * Do
     * NOT
     * modify
     * this
     * code.
     * The
     * content
     * of
     * this
     * method
     * is
     * always
     * regenerated
     * by
     * the
     * Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        MMTPDelimGroup = new javax.swing.ButtonGroup();
        MMTPCloseButton = new SimpleButton();
        MMTPCopyAsHTML = new SimpleButton();
        MMTPCopyAsDelimited = new SimpleButton();
        MMTPDelimTab = new SimpleRadioButton();
        MMTPDelimDot = new SimpleRadioButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        MMTPTable = new javax.swing.JTable();
        MMTPDescription = new SimpleLabel();

        setLayout(new java.awt.GridBagLayout());

        MMTPCloseButton.setText("Close");
        MMTPCloseButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                MMTPCloseButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(MMTPCloseButton, gridBagConstraints);

        MMTPCopyAsHTML.setText("Copy as HTML");
        MMTPCopyAsHTML.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                MMTPCopyAsHTMLMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(MMTPCopyAsHTML, gridBagConstraints);

        MMTPCopyAsDelimited.setText("Copy as delimited");
        MMTPCopyAsDelimited.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                MMTPCopyAsDelimitedMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(MMTPCopyAsDelimited, gridBagConstraints);

        MMTPDelimTab.setSelected(true);
        MMTPDelimTab.setText("tab");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        add(MMTPDelimTab, gridBagConstraints);

        MMTPDelimDot.setText(";");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        add(MMTPDelimDot, gridBagConstraints);

        MMTPTable.setModel(this.tableModel);
        MMTPTable.setDefaultRenderer(Object.class, this.renderer);
        MMTPTable.setTableHeader(null);
        jScrollPane2.setViewportView(MMTPTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane2, gridBagConstraints);

        MMTPDescription.setText("<html>The merge matrix tool has computed the amount of merges between topics and it's displayed below. The data is formatted in the following fashion:  each cell has a value computed as the ratio of merging topics between the row layer and the column layer to the total amount of topics in the row layer. Cells in the diagonal have the maximum value as all topics merge with themselves. Maximum value in other cells usually corresponds to the row layer being a child to the column layer.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(MMTPDescription, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void MMTPCopyAsDelimitedMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MMTPCopyAsDelimitedMouseReleased
        delimToClipboard();
    }//GEN-LAST:event_MMTPCopyAsDelimitedMouseReleased

    private void MMTPCopyAsHTMLMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MMTPCopyAsHTMLMouseReleased
        htmlToClipboard();
    }//GEN-LAST:event_MMTPCopyAsHTMLMouseReleased

    private void MMTPCloseButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_MMTPCloseButtonMouseReleased
        close();
    }//GEN-LAST:event_MMTPCloseButtonMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton MMTPCloseButton;
    private javax.swing.JButton MMTPCopyAsDelimited;
    private javax.swing.JButton MMTPCopyAsHTML;
    private javax.swing.JRadioButton MMTPDelimDot;
    private javax.swing.ButtonGroup MMTPDelimGroup;
    private javax.swing.JRadioButton MMTPDelimTab;
    private javax.swing.JLabel MMTPDescription;
    private javax.swing.JTable MMTPTable;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
