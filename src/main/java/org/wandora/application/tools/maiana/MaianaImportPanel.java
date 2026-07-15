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
 *
 * MaianaImportPanel.java
 *
 * Created on 12.10.2010, 10:34:55
 */




package org.wandora.application.tools.maiana;



import java.awt.Cursor;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.utils.IObox;


/**
 *
 * @author akivela
 */
public class MaianaImportPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	private boolean autoLoadList = false;
    private boolean wasAccepted = false;
    private JDialog window = null;
    private JTable mapTable = null;


    
    /** Creates new form MaianaImportPanel */
    public MaianaImportPanel() {
        initComponents();
    }



    public void open(Wandora wandora) {
        window = new JDialog(wandora, true);
        window.setSize(800, 450);
        window.add(this);
        window.setTitle("Import from Waiana");
        wandora.centerWindow(window);

        if(autoLoadList) {
            setTopicMapsList();
        }

        window.setVisible(true);
    }



    public void setAccepted(boolean accepted) {
        wasAccepted = accepted;
    }

    

    public boolean wasAccepted() {
        return wasAccepted;
    }



    public String[] getTopicMapNames() {
        if(nameTextField.getText() != null && nameTextField.getText().length() > 0) {
            return new String[] { nameTextField.getText() };
        }
        else {
            if(mapTable != null) {
                return getSelectedRowsOfColumn(0);
            }
        }
        return new String[] { };
    }



    public String[] getTopicMapShortNames() {
        if(nameTextField.getText() != null && nameTextField.getText().length() > 0) {
            return new String[] { nameTextField.getText() };
        }
        else {
            if(mapTable != null) {
                return getSelectedRowsOfColumn(1);
            }
        }
        return new String[] { };
    }



    
    public String[] getOwners() {
        if(ownerTextField.getText() != null && ownerTextField.getText().length() > 0) {
            return new String[] { ownerTextField.getText() };
        }
        else {
            if(mapTable != null) {
                return getSelectedRowsOfColumn(2);
            }
        }
        return new String[] { };
    }


    
    private String[] getSelectedRowsOfColumn(int col) {
        int[] rows = mapTable.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            rows[i] = mapTable.convertRowIndexToModel(rows[i]);
        }
        String[] data = new String[rows.length];
        for (int i = 0; i < rows.length; i++) {
            data[i] = (String) mapTable.getModel().getValueAt(rows[i], col);
        }
        return data;
    }




    public void setTopicMapName(String n) {
        nameTextField.setText(n);
    }

    public void setOwner(String o) {
        ownerTextField.setText(o);
    }

    public void setApiKey(String key) {
        apiKeyTextField.setText(key);
    }

    public String getApiKey() {
        return apiKeyTextField.getText();
    }
    
    public String getFormat() {
        return "xtm20";
    }

    public void setFormat(String format) {
    
    }

    public String getApiEndPoint() {
        return apiEndPointField.getSelectedItem().toString();
    }

    public void setApiEndPoint(String endpoint) {
        apiEndPointField.setSelectedItem(endpoint);
    }
    
    
    

    public void setTopicMapsList() {
        if(getApiKey() != null) {
            try {
                JSONObject list = MaianaUtils.listAvailableTopicMaps(getApiEndPoint(), getApiKey());
                if(list.has("msg")) {
                    WandoraOptionPane.showMessageDialog(window, list.getString("msg"), "API says", WandoraOptionPane.WARNING_MESSAGE);
                    //System.out.println("REPLY:"+list.toString());
                }

                if(list.has("data")) {
                    mapTable = new JTable();
                    mapTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if(list != null) {
                        JSONArray datas = list.getJSONArray("data");
                        TopicMapsTableModel myModel = new TopicMapsTableModel(datas);
                        mapTable.setModel(myModel);
                        mapTable.setRowSorter(new TableRowSorter(myModel));

                        mapTable.setColumnSelectionAllowed(false);
                        mapTable.setRowSelectionAllowed(true);
                        mapTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                        
                        TableColumn column = null;
                        for (int i=0; i < mapTable.getColumnCount(); i++) {
                            column = mapTable.getColumnModel().getColumn(i);
                            column.setPreferredWidth(myModel.getColumnWidth(i));
                        }
                        tableScrollPane.setViewportView(mapTable);
                    }
                }
            }
            catch(Exception e) {
                Wandora.getWandora().displayException("Exception '"+e.getMessage()+"' occurred while getting the list of topic maps.", e);
            }
        }


    }



    // -------------------------------------------------------------------------


    


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        entryPanel = new javax.swing.JPanel();
        entryInnerPanel = new javax.swing.JPanel();
        nameLabel = new SimpleLabel();
        nameTextField = new SimpleField();
        ownerLabel = new SimpleLabel();
        ownerTextField = new SimpleField();
        contentPanel = new javax.swing.JPanel();
        infoLabel = new SimpleLabel();
        namePanel = new javax.swing.JPanel();
        apiEndPointLabel = new javax.swing.JLabel();
        apiEndPointField = new javax.swing.JComboBox();
        apiKeyLabel = new javax.swing.JLabel();
        apiKeyTextField = new SimpleField();
        refreshListButton = new SimpleButton();
        tablePanel = new javax.swing.JPanel();
        tableScrollPane = new SimpleScrollPane();
        tempLabel = new SimpleLabel();
        buttonPanel = new javax.swing.JPanel();
        deleteButton = new SimpleButton();
        buttonFillerPanel = new javax.swing.JPanel();
        importButton = new SimpleButton();
        cancelButton = new SimpleButton();

        entryPanel.setLayout(new java.awt.GridBagLayout());

        entryInnerPanel.setLayout(new java.awt.GridBagLayout());

        nameLabel.setText("Topic map name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        entryInnerPanel.add(nameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        entryInnerPanel.add(nameTextField, gridBagConstraints);

        ownerLabel.setText("Topic map owner");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        entryInnerPanel.add(ownerLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        entryInnerPanel.add(ownerTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        entryPanel.add(entryInnerPanel, gridBagConstraints);

        setLayout(new java.awt.GridBagLayout());

        contentPanel.setLayout(new java.awt.GridBagLayout());

        infoLabel.setText("<html>Import topic maps from Waiana or Maiana. Select API endpoint and enter your personal API key to the fields below. Then fetch the list of available topic maps and select one or more in the list.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        contentPanel.add(infoLabel, gridBagConstraints);

        namePanel.setLayout(new java.awt.GridBagLayout());

        apiEndPointLabel.setText("API endpoint");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        namePanel.add(apiEndPointLabel, gridBagConstraints);

        apiEndPointField.setEditable(true);
        apiEndPointField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "http://127.0.0.1:8898/waiana/", "http://maiana.topicmapslab.de/api" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        namePanel.add(apiEndPointField, gridBagConstraints);

        apiKeyLabel.setText("API key");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        namePanel.add(apiKeyLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        namePanel.add(apiKeyTextField, gridBagConstraints);

        refreshListButton.setText("Fetch list");
        refreshListButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        refreshListButton.setMaximumSize(new java.awt.Dimension(75, 24));
        refreshListButton.setMinimumSize(new java.awt.Dimension(75, 24));
        refreshListButton.setPreferredSize(new java.awt.Dimension(75, 24));
        refreshListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshListButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        namePanel.add(refreshListButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        contentPanel.add(namePanel, gridBagConstraints);

        tablePanel.setLayout(new java.awt.BorderLayout());

        tempLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tempLabel.setText("Press Fetch list button...");
        tableScrollPane.setViewportView(tempLabel);

        tablePanel.add(tableScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        contentPanel.add(tablePanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(contentPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        deleteButton.setText("Delete");
        deleteButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        deleteButton.setMinimumSize(new java.awt.Dimension(65, 23));
        deleteButton.setPreferredSize(new java.awt.Dimension(65, 23));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(deleteButton, new java.awt.GridBagConstraints());

        javax.swing.GroupLayout buttonFillerPanelLayout = new javax.swing.GroupLayout(buttonFillerPanel);
        buttonFillerPanel.setLayout(buttonFillerPanelLayout);
        buttonFillerPanelLayout.setHorizontalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        buttonFillerPanelLayout.setVerticalGroup(
            buttonFillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(buttonFillerPanel, gridBagConstraints);

        importButton.setText("Import");
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        buttonPanel.add(importButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshListButtonActionPerformed
        if(getApiKey() != null) {
            setTopicMapsList();
        }
        else {
            WandoraOptionPane.showMessageDialog(window, "In order to fetch a topic map list, Wandora needs a valid api key. Please, write your API key to the api key field first.", "Missing API key", WandoraOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_refreshListButtonActionPerformed

    private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
        wasAccepted = true;
        if(window != null) window.setVisible(false);
    }//GEN-LAST:event_importButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        wasAccepted = false;
        if(window != null) window.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        deleteSelectedTopicMaps();
    }//GEN-LAST:event_deleteButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox apiEndPointField;
    private javax.swing.JLabel apiEndPointLabel;
    private javax.swing.JLabel apiKeyLabel;
    private javax.swing.JTextField apiKeyTextField;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JPanel entryInnerPanel;
    private javax.swing.JPanel entryPanel;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel ownerLabel;
    private javax.swing.JTextField ownerTextField;
    private javax.swing.JButton refreshListButton;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel tempLabel;
    // End of variables declaration//GEN-END:variables






    // -------------------------------------------------------------------------
    


    class TopicMapsTableModel extends DefaultTableModel implements TableModel {

		private static final long serialVersionUID = 1L;

		JSONArray jsonModel = null;

        public TopicMapsTableModel(JSONArray m) {
            jsonModel = m;
        }


        @Override
        public int getRowCount() {
            if(jsonModel == null) return 0;
            return jsonModel.length();
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch(columnIndex) {
                case 0: return "Name";
                case 1: return "Short name";
                case 2: return "Owner";
                case 3: return "Is schema";
                case 4: return "Is editable";
                case 5: return "Is downloadable";
                default: return "Illegal column";
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(jsonModel == null) return null;
            if(rowIndex >= 0 && rowIndex < jsonModel.length()) {
                if(columnIndex >= 0 && columnIndex < 6) {
                    try {
                        JSONObject v = jsonModel.getJSONObject(rowIndex);
                        switch(columnIndex) {
                            case 0: return v.get("name").toString();
                            case 1: return v.get("short_name").toString();
                            case 2: return v.get("owner").toString();
                            case 3: return v.get("is_schema").toString();
                            case 4: return v.get("is_editable").toString();
                            case 5: return v.get("is_downloadable").toString();
                        }
                    }
                    catch(Exception e) {
                        return "";
                    }
                }
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        }


        public int getColumnWidth(int col) {
            switch(col) {
                case 0: return 200;
                case 1: return 100;
                case 2: return 100;
                case 3: return 20;
                case 4: return 20;
                case 5: return 20;
                default: return 100;
            }
        }

    }

    
    
    // -------------------------------------------------------------------------
    
    
    private void deleteSelectedTopicMaps() {
        boolean requiresListRefresh = false;

        MaianaUtils.setApiKey(getApiKey());
        MaianaUtils.setApiEndPoint(getApiEndPoint());

        String[] shortNames = getTopicMapShortNames();
        String[] names = getTopicMapNames();
        String[] owners = getOwners();

        String apikey = MaianaUtils.getApiKey();

        if(shortNames.length == 0) {
            log("You didn't specify which topic maps should be deleted.");
        }
        else {
            int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Are you sure you want to delete selected topic maps?", "Are you sure?", WandoraOptionPane.YES_NO_OPTION);
            if(a != WandoraOptionPane.YES_OPTION) return;
        }

        try {
            for(int i=0; i<shortNames.length; i++) {
                String sn = shortNames[i];
                String o = "";
                try { o = owners[i]; } catch(Exception e) {}
                String n = "";
                try { n = names[i]; } catch(Exception e) { n = sn; }

                String request = MaianaUtils.getDeleteTemplate(apikey, sn);
                String apiEndPoint = getApiEndPoint();
                MaianaUtils.checkForLocalService(apiEndPoint);

                requiresListRefresh = true;
                String reply = IObox.doUrl(new URL(apiEndPoint), request, "application/json");

                //System.out.println("reply:\n"+reply);

                JSONObject replyObject = new JSONObject(reply);

                if(replyObject.has("code")) {
                    String code = replyObject.getString("code");
                    if(!"0".equals(code)) {
                        if(i<shortNames.length) {
                            int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "An error occurred while deleting topic map '"+n+"' created by '"+o+"'. Do you want to continue deletion?", "Continue?", WandoraOptionPane.YES_NO_OPTION);
                            if(a != WandoraOptionPane.YES_OPTION) break;
                        }
                        else {
                            log("An error occurred while deleting topic map '"+n+"' created by '"+o+"'.");
                        }
                    }
                }
                if(replyObject.has("msg")) {
                    String msg = replyObject.getString("msg");
                    // log(msg);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if(requiresListRefresh) {
            setTopicMapsList();
        }
    }
    
    private void log(String msg) {
        WandoraOptionPane.showMessageDialog(Wandora.getWandora(), msg);
    }
}
