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
 * GenericDatabaseExtractorConfigurationPanel.java
 *
 * Created on 14. helmikuuta 2007, 11:03
 */

package org.wandora.application.tools.extractors;


import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.wandora.application.gui.simple.SimpleLabel;


/**
 *
 * @author  olli
 */
public class GenericDatabaseExtractorConfigurationPanel extends javax.swing.JPanel implements TableModelListener {
    

	private static final long serialVersionUID = 1L;
	
	
//    private GenericDatabaseExtractor.DatabaseSchema schema;
    private String table;
    private JTableHeader jTableHeader;
    private boolean ignoreChanges=false;
    
    /** Creates new form GenericDatabaseExtractorConfigurationPanel */
    public GenericDatabaseExtractorConfigurationPanel(GenericDatabaseExtractor.DatabaseSchema schema,String table) {
        this.table=table;
        initComponents();
        
        jTableHeader=new JTableHeader(jTable.getColumnModel());
        jTable.setTableHeader(jTableHeader);
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0; gbc.gridy=1; gbc.fill=gbc.HORIZONTAL; gbc.anchor=gbc.SOUTH;
        gbc.insets=new java.awt.Insets(5,5,0,5);
        this.add(jTableHeader,gbc);
        
        tableLabel.setText(table);

        JComboBox refCombo=new JComboBox();
        refCombo.addItem("<None>");
        for(ArrayList<GenericDatabaseExtractor.DBColumn> cs : schema.columns.values()){
            for(GenericDatabaseExtractor.DBColumn c : cs){
                refCombo.addItem(c);
            }
        }
        
        DefaultTableModel model=(DefaultTableModel)jTable.getModel();
        for(GenericDatabaseExtractor.DBColumn column : schema.columns.get(table)){
            model.addRow(new Object[]{column.column,
//                                      column.references==null?"":"-> "+column.references.table+"."+column.references.column,
                                      column.references==null?"<None>":column.references,
                                      true,
                                      column.baseName,
                                      column.makeTopics});
        }
        TableColumnModel cmodel=jTable.getColumnModel();
        cmodel.getColumn(1).setCellEditor(new DefaultCellEditor(refCombo));
        model.addTableModelListener(this);
        
    }
    
    public void updateSchema(GenericDatabaseExtractor.DatabaseSchema schema){
        ArrayList<GenericDatabaseExtractor.DBColumn> columns=schema.columns.get(table);
        HashMap<String,GenericDatabaseExtractor.DBColumn> columnHash=new HashMap<String,GenericDatabaseExtractor.DBColumn>();
        for(GenericDatabaseExtractor.DBColumn column : columns){
            columnHash.put(column.column,column);
        }
        DefaultTableModel model=(DefaultTableModel)jTable.getModel();
        for(int i=0;i<model.getRowCount();i++){
            String columnName=(String)model.getValueAt(i,0);
            GenericDatabaseExtractor.DBColumn column=columnHash.get(columnName);
            if(column==null) continue;
            Object refObject=model.getValueAt(i,1);
            boolean include=(Boolean)model.getValueAt(i,2);
            boolean baseName=(Boolean)model.getValueAt(i,3);
            boolean makeTopics=(Boolean)model.getValueAt(i,4);
            if(refObject==null || !(refObject instanceof GenericDatabaseExtractor.DBColumn))
                column.references=null;
            else column.references=(GenericDatabaseExtractor.DBColumn)refObject;
            column.include=include;
            column.baseName=baseName;
            column.makeTopics=makeTopics;
        }
    }

    public void tableChanged(TableModelEvent e) {
        if(e.getColumn()==3){
            if(ignoreChanges) return;
            DefaultTableModel model=(DefaultTableModel)jTable.getModel();
            synchronized(this){
                for(int i=e.getFirstRow();i<=e.getLastRow();i++){
                    if((Boolean)model.getValueAt(i,3)){
                        ignoreChanges=true;
                        for(int j=0;j<model.getRowCount();j++){

                            if(j==i) continue;
                            model.setValueAt((Boolean)false,j,3);
                        }
                        ignoreChanges=false;
                        break;
                    }
                }
            }
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

        jScrollPane1 = new javax.swing.JScrollPane();
        tableLabel = new SimpleLabel();
        jTable = new javax.swing.JTable();

        setLayout(new java.awt.GridBagLayout());

        tableLabel.setText("Table name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(tableLabel, gridBagConstraints);

        jTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Column", "Foreign key", "Include column", "Basename", "Make topics"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 15, 5);
        add(jTable, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
    
}
