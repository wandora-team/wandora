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
 * PalvelukarttaSelector.java
 *
 * Created on Dec 16, 2011, 6:49:47 PM
 */

package org.wandora.application.tools.extractors.palvelukartta;

import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JDialog;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleRadioButton;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;

/**
 *
 * @author akivela
 */


public class PalvelukarttaSelector extends javax.swing.JPanel {

    private static final String BASE_URL = "http://www.hel.fi/palvelukarttaws/rest/v1/";
    private static final String ORGANIZATION_BASE_URL = BASE_URL + "organization/";
    private static final String DEPARTMENT_BASE_URL = BASE_URL + "department/";
    private static final String UNIT_BASE_URL = BASE_URL + "unit/";
    private static final String SERVICE_BASE_URL = BASE_URL + "service/";
    
    private boolean wasAccepted = false;
    private JDialog myDialog = null;
    private Wandora wandora = null;
    private Context context = null;
    
    
    /** Creates new form PalvelukarttaSelector */
    public PalvelukarttaSelector() {
        initComponents();
    }
    
    
    

    
    public void setContext(Context c) {
        context = c;
    }
    
    public void setWandora(Wandora w) {
        wandora = w;
    }
    
    public void setAccepted(boolean isAccepted) {
        wasAccepted = isAccepted;
    }
    
    public boolean wasAccepted() {
        return wasAccepted;
    }
    
    public void openDialog(Wandora w) {
        wasAccepted = false;
        if(myDialog == null) {
            myDialog = new JDialog(w, true);
            myDialog.setTitle("Palvelukartta extractor");
            wandora = w;
            myDialog.add(this);
            myDialog.setSize(600,300);
            UIBox.centerWindow(myDialog, w);
        }
        myDialog.setVisible(true);
    }
    
    
    public ArrayList<WandoraTool> getSelectedExtractors(PalvelukarttaExtractor pke) {
        ArrayList<WandoraTool> extractors = new ArrayList();
        Component component = pkTabbedPane.getSelectedComponent();

        // ***** EASY/ALL *****
        if(easyPanel.equals(component)) {
            
            if(allOrganizationsCheckBox.isSelected()) {
                PalvelukarttaOrganizationExtractor e = new PalvelukarttaOrganizationExtractor();
                e.setForceUrls( new String[] { ORGANIZATION_BASE_URL } );
                extractors.add(e);
            }
            if(allDepartmentsCheckBox.isSelected()) {
                PalvelukarttaDepartmentExtractor e = new PalvelukarttaDepartmentExtractor();
                e.setForceUrls( new String[] { DEPARTMENT_BASE_URL } );
                extractors.add(e);
            }
            if(allUnitsCheckBox.isSelected()) {
                boolean deepExtraction = deepUnitsCheckBox.isSelected();
                PalvelukarttaUnitExtractor e = new PalvelukarttaUnitExtractor(deepExtraction);
                e.setForceUrls( new String[] { UNIT_BASE_URL } );
                extractors.add(e);
            }
            if(allServicesCheckBox.isSelected()) {
                PalvelukarttaServiceExtractor e = new PalvelukarttaServiceExtractor();
                e.setForceUrls( new String[] { SERVICE_BASE_URL } );
                extractors.add(e);
            }
        }
        
        if(idPanel.equals(component)) {
            
            String idListString = idTextField.getText();
            String[] idList = urlEncode(commaSplitter(idListString));
            
            if(organizationIdRadioButton.isSelected()) {
                String extractUrl = ORGANIZATION_BASE_URL + "__1__";
                String[] extractUrls = completeString(extractUrl, idList);
                PalvelukarttaOrganizationExtractor e = new PalvelukarttaOrganizationExtractor();
                e.setForceUrls( extractUrls );
                extractors.add(e);
            }
            if(departmentIdRadioButton.isSelected()) {
                String extractUrl = DEPARTMENT_BASE_URL + "__1__";
                String[] extractUrls = completeString(extractUrl, idList);
                PalvelukarttaDepartmentExtractor e = new PalvelukarttaDepartmentExtractor();
                e.setForceUrls( extractUrls );
                extractors.add(e);
            }
            if(unitIdRadioButton.isSelected()) {
                String extractUrl = UNIT_BASE_URL + "__1__";
                String[] extractUrls = completeString(extractUrl, idList);
                PalvelukarttaUnitExtractor e = new PalvelukarttaUnitExtractor();
                e.setForceUrls( extractUrls );
                extractors.add(e);
            }
            if(serviceIdRadioButton.isSelected()) {
                String extractUrl = SERVICE_BASE_URL + "__1__";
                String[] extractUrls = completeString(extractUrl, idList);
                PalvelukarttaServiceExtractor e = new PalvelukarttaServiceExtractor();
                e.setForceUrls( extractUrls );
                extractors.add(e);
            }
        }

        return extractors;
    }
    
    
    
    
    

    public String[] commaSplitter(String str) {
        if(str.indexOf(',') != -1) {
            String[] strs = str.split(",");
            ArrayList<String> strList = new ArrayList<String>();
            String s = null;
            for(int i=0; i<strs.length; i++) {
                s = strs[i];
                s = s.trim();
                if(s.length() > 0) {
                    strList.add(s);
                }
            }
            return strList.toArray( new String[] {} );
        }
        else {
            return new String[] { str };
        }
        
    }
    
    
    
    public String[] completeString(String template, String[] strs) {
        if(strs == null || template == null) return null;
        String[] completed = new String[strs.length];
        for(int i=0; i<strs.length; i++) {
            completed[i] = template.replaceAll("__1__", strs[i]);
        }
        return completed;
    }
    
    
    public String[] completeString(String template, String[] strs1, String[] strs2) {
        if(strs1 == null || strs2 == null || template == null) return null;
        if(strs1.length != strs2.length) return null;
        
        String[] completed = new String[strs1.length];
        for(int i=0; i<strs1.length; i++) {
            completed[i] = template.replaceAll("__1__", strs1[i]);
            completed[i] = completed[i].replaceAll("__2__", strs2[i]);
        }
        return completed;
    }
    
    
    public String[] urlEncode(String[] urls) {
        if(urls == null) return null;
        String[] cleanUrls = new String[urls.length];
        for(int i=0; i<urls.length; i++) {
            cleanUrls[i] = urlEncode(urls[i]);
        }
        return cleanUrls;
    }
    
    
    
    protected static String urlEncode(String str) {
        try {
            str = URLEncoder.encode(str, "utf-8");
        }
        catch(Exception e) {}
        return str;
    }

    
    
    private void getIdContext() {
        try {
            if(context != null) {
                Iterator i = context.getContextObjects();
                if(i.hasNext()) {
                    boolean addComma = false;
                    StringBuilder sb = new StringBuilder("");
                    while(i.hasNext()) {
                        Object o = i.next();
                        if(o != null) {
                            if(o instanceof Topic) {
                                Topic t = (Topic) o;
                                Collection<Locator> sis = t.getSubjectIdentifiers();
                                for(Locator si : sis) {
                                    if(si != null) {
                                        String siStr = si.toExternalForm();
                                        if(siStr.startsWith(ORGANIZATION_BASE_URL)) {
                                            String id = siStr.substring(ORGANIZATION_BASE_URL.length());
                                            if(id != null && id.length() > 0) {
                                                if(addComma) sb.append(", ");
                                                sb.append(id);
                                                addComma = true;
                                                organizationIdRadioButton.setSelected(true);
                                            }
                                        }
                                        else if(siStr.startsWith(DEPARTMENT_BASE_URL)) {
                                            String id = siStr.substring(DEPARTMENT_BASE_URL.length());
                                            if(id != null && id.length() > 0) {
                                                if(addComma) sb.append(", ");
                                                sb.append(id);
                                                addComma = true;
                                                departmentIdRadioButton.setSelected(true);
                                            }
                                        }
                                        else if(siStr.startsWith(UNIT_BASE_URL)) {
                                            String id = siStr.substring(UNIT_BASE_URL.length());
                                            if(id != null && id.length() > 0) {
                                                if(addComma) sb.append(", ");
                                                sb.append(id);
                                                addComma = true;
                                                unitIdRadioButton.setSelected(true);
                                            }
                                        }
                                        else if(siStr.startsWith(SERVICE_BASE_URL)) {
                                            String id = siStr.substring(SERVICE_BASE_URL.length());
                                            if(id != null && id.length() > 0) {
                                                if(addComma) sb.append(", ");
                                                sb.append(id);
                                                addComma = true;
                                                serviceIdRadioButton.setSelected(true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(sb.length() > 0) {
                        idTextField.setText(sb.toString());
                    }
                }
            }
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        idButtonGroup = new javax.swing.ButtonGroup();
        pkTabbedPane = new SimpleTabbedPane();
        easyPanel = new javax.swing.JPanel();
        allPanel = new javax.swing.JPanel();
        allLabel = new SimpleLabel();
        allOrganizationsCheckBox = new SimpleCheckBox();
        allDepartmentsCheckBox = new SimpleCheckBox();
        allUnitsCheckBox = new SimpleCheckBox();
        deepUnitsCheckBox = new SimpleCheckBox();
        allServicesCheckBox = new SimpleCheckBox();
        idPanel = new javax.swing.JPanel();
        inPanelInner = new javax.swing.JPanel();
        idLabel = new SimpleLabel();
        idTextField = new SimpleField();
        jPanel2 = new javax.swing.JPanel();
        organizationIdRadioButton = new SimpleRadioButton();
        departmentIdRadioButton = new SimpleRadioButton();
        unitIdRadioButton = new SimpleRadioButton();
        serviceIdRadioButton = new SimpleRadioButton();
        jPanel1 = new javax.swing.JPanel();
        idFieldClearButton = new SimpleButton();
        idGetContextButton = new SimpleButton();
        pkButtonPanel = new javax.swing.JPanel();
        openButton = new SimpleButton();
        pkFillerPanel = new javax.swing.JPanel();
        extractButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        easyPanel.setLayout(new java.awt.GridBagLayout());

        allPanel.setLayout(new java.awt.GridBagLayout());

        allLabel.setText("<html>Extract complete lists from Palvelukartta. You can extract a list of organizations, a list of departments, units and services. You can select to extract multiple lists at once.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        allPanel.add(allLabel, gridBagConstraints);

        allOrganizationsCheckBox.setSelected(true);
        allOrganizationsCheckBox.setText("Extract all organizations");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        allPanel.add(allOrganizationsCheckBox, gridBagConstraints);

        allDepartmentsCheckBox.setSelected(true);
        allDepartmentsCheckBox.setText("Extract all departments");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        allPanel.add(allDepartmentsCheckBox, gridBagConstraints);

        allUnitsCheckBox.setSelected(true);
        allUnitsCheckBox.setText("Extract all units");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        allPanel.add(allUnitsCheckBox, gridBagConstraints);

        deepUnitsCheckBox.setSelected(true);
        deepUnitsCheckBox.setText("Trigger separate extraction for each unit (more information but slower)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        allPanel.add(deepUnitsCheckBox, gridBagConstraints);

        allServicesCheckBox.setSelected(true);
        allServicesCheckBox.setText("Extract all services");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        allPanel.add(allServicesCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        easyPanel.add(allPanel, gridBagConstraints);

        pkTabbedPane.addTab("Lists", easyPanel);

        idPanel.setLayout(new java.awt.GridBagLayout());

        inPanelInner.setLayout(new java.awt.GridBagLayout());

        idLabel.setText("<html>Extract Palvelukartta organizations, departments, units or services with a comma separated list of identifiers.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        inPanelInner.add(idLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        inPanelInner.add(idTextField, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        idButtonGroup.add(organizationIdRadioButton);
        organizationIdRadioButton.setSelected(true);
        organizationIdRadioButton.setText("Organization");
        jPanel2.add(organizationIdRadioButton, new java.awt.GridBagConstraints());

        idButtonGroup.add(departmentIdRadioButton);
        departmentIdRadioButton.setText("Department");
        jPanel2.add(departmentIdRadioButton, new java.awt.GridBagConstraints());

        idButtonGroup.add(unitIdRadioButton);
        unitIdRadioButton.setText("Unit");
        jPanel2.add(unitIdRadioButton, new java.awt.GridBagConstraints());

        idButtonGroup.add(serviceIdRadioButton);
        serviceIdRadioButton.setText("Service");
        jPanel2.add(serviceIdRadioButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        inPanelInner.add(jPanel2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        idFieldClearButton.setText("Clear");
        idFieldClearButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                idFieldClearButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel1.add(idFieldClearButton, gridBagConstraints);

        idGetContextButton.setText("Get context");
        idGetContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                idGetContextButtonMouseReleased(evt);
            }
        });
        jPanel1.add(idGetContextButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        inPanelInner.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);
        idPanel.add(inPanelInner, gridBagConstraints);

        pkTabbedPane.addTab("With id", idPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pkTabbedPane, gridBagConstraints);

        pkButtonPanel.setLayout(new java.awt.GridBagLayout());

        openButton.setText("Open Palvelukartta");
        openButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        openButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                openButtonMouseReleased(evt);
            }
        });
        pkButtonPanel.add(openButton, new java.awt.GridBagConstraints());

        pkFillerPanel.setPreferredSize(new java.awt.Dimension(8, 8));
        pkFillerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        pkButtonPanel.add(pkFillerPanel, gridBagConstraints);

        extractButton.setText("Extract");
        extractButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                extractButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        pkButtonPanel.add(extractButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        pkButtonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(pkButtonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        wasAccepted = false;
        if(myDialog != null) myDialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonMouseReleased

    private void extractButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extractButtonMouseReleased
        wasAccepted = true;
        if(myDialog != null) myDialog.setVisible(false);
    }//GEN-LAST:event_extractButtonMouseReleased

    private void idFieldClearButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_idFieldClearButtonMouseReleased
        idTextField.setText("");
    }//GEN-LAST:event_idFieldClearButtonMouseReleased

    private void idGetContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_idGetContextButtonMouseReleased
        getIdContext();
    }//GEN-LAST:event_idGetContextButtonMouseReleased

    private void openButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openButtonMouseReleased
        Desktop dt = Desktop.getDesktop();
        if(dt != null) {
            try {
                dt.browse(new URI("http://www.hel.fi/palvelukartta"));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_openButtonMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allDepartmentsCheckBox;
    private javax.swing.JLabel allLabel;
    private javax.swing.JCheckBox allOrganizationsCheckBox;
    private javax.swing.JPanel allPanel;
    private javax.swing.JCheckBox allServicesCheckBox;
    private javax.swing.JCheckBox allUnitsCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox deepUnitsCheckBox;
    private javax.swing.JRadioButton departmentIdRadioButton;
    private javax.swing.JPanel easyPanel;
    private javax.swing.JButton extractButton;
    private javax.swing.ButtonGroup idButtonGroup;
    private javax.swing.JButton idFieldClearButton;
    private javax.swing.JButton idGetContextButton;
    private javax.swing.JLabel idLabel;
    private javax.swing.JPanel idPanel;
    private javax.swing.JTextField idTextField;
    private javax.swing.JPanel inPanelInner;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton openButton;
    private javax.swing.JRadioButton organizationIdRadioButton;
    private javax.swing.JPanel pkButtonPanel;
    private javax.swing.JPanel pkFillerPanel;
    private javax.swing.JTabbedPane pkTabbedPane;
    private javax.swing.JRadioButton serviceIdRadioButton;
    private javax.swing.JRadioButton unitIdRadioButton;
    // End of variables declaration//GEN-END:variables





  
    // -------------------------------------------------------------------------
    



}
