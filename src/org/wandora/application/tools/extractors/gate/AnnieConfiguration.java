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
 */




package org.wandora.application.tools.extractors.gate;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import javax.swing.JDialog;
import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.utils.Options;


/**
 *
 * @author akivela
 */
public class AnnieConfiguration extends JDialog {


    private boolean accepted = false;
    private Wandora wandora = null;
    private AnnieExtractor parent = null;
    private HashMap oldData = new HashMap();



    /** Creates new form AnnieConfiguration */
    public AnnieConfiguration(Wandora w, AnnieExtractor p) {
        super(w, true);
        wandora = w;
        parent = p;
        initComponents();
        setSize(350,460);
        if(wandora != null) {
            wandora.centerWindow(this);
        }
    }

    
    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            oldData.put(firstPersonCheckBox,    firstPersonCheckBox.isSelected());
            oldData.put(lookupCheckBox,         lookupCheckBox.isSelected());
            oldData.put(personCheckBox,         personCheckBox.isSelected());
            oldData.put(sentenceCheckBox,       sentenceCheckBox.isSelected());
            oldData.put(spaceTokenCheckBox,     spaceTokenCheckBox.isSelected());
            oldData.put(splitCheckBox,          splitCheckBox.isSelected());
            oldData.put(tokenCheckBox,          tokenCheckBox.isSelected());
            oldData.put(unknownCheckBox,        unknownCheckBox.isSelected());

            oldData.put(dateCheckBox,           dateCheckBox.isSelected());
            oldData.put(jobTitleCheckBox,       jobTitleCheckBox.isSelected());
            oldData.put(locationCheckBox,       locationCheckBox.isSelected());
            oldData.put(organizationCheckBox,   organizationCheckBox.isSelected());
            oldData.put(tempCheckBox,           tempCheckBox.isSelected());

            oldData.put(identifierCheckBox,     identifierCheckBox.isSelected());
            oldData.put(moneyCheckBox,          moneyCheckBox.isSelected());
            oldData.put(percentCheckBox,        percentCheckBox.isSelected());
            oldData.put(titleCheckBox,          titleCheckBox.isSelected());

            oldData.put(otherTextField,         otherTextField.getText());
        }
        super.setVisible(visible);
    }




    public boolean wasAccepted() {
        return accepted;
    }



    public boolean acceptAllAnnotationTypes() {
        return allCheckBox.isSelected();
    }

    
    private boolean annotationTypeUpdateRequired = true;
    private HashSet annotationTypes = null;
    public boolean acceptAnnotationType(String t) {
        if(annotationTypes == null || annotationTypeUpdateRequired) {
            annotationTypes = getAnnotationTypes();
            annotationTypeUpdateRequired = false;
        }
        if(annotationTypes.contains(t)) return true;
        return false;
    }


    
    // ADD Date, JobTitle, Location, Organization, Temp


    public HashSet getAnnotationTypes() {
        LinkedHashSet<String> types = new LinkedHashSet<String>();
        if(firstPersonCheckBox.isSelected())    types.add("FirstPerson");
        if(lookupCheckBox.isSelected())         types.add("Lookup");
        if(personCheckBox.isSelected())         types.add("Person");
        if(sentenceCheckBox.isSelected())       types.add("Sentence");
        if(spaceTokenCheckBox.isSelected())     types.add("SpaceToken");
        if(splitCheckBox.isSelected())          types.add("Split");
        if(tokenCheckBox.isSelected())          types.add("Token");
        if(unknownCheckBox.isSelected())        types.add("Unknown");

        if(dateCheckBox.isSelected())           types.add("Date");
        if(jobTitleCheckBox.isSelected())       types.add("JobTitle");
        if(locationCheckBox.isSelected())       types.add("Location");
        if(organizationCheckBox.isSelected())   types.add("Organization");
        if(tempCheckBox.isSelected())           types.add("Temp");

        if(identifierCheckBox.isSelected())     types.add("Identifier");
        if(moneyCheckBox.isSelected())          types.add("Money");
        if(percentCheckBox.isSelected())        types.add("Percent");
        if(titleCheckBox.isSelected())          types.add("Title");

        String other = otherTextField.getText();
        String[] others = other.split(",");
        if(others.length > 0) {
            for(int i=0; i<others.length; i++) {
                String o = others[i];
                if(o != null && o.trim().length() > 0) {
                    types.add(o);
                }
            }
        }
        return types;
    }



    private void restoreOldData() {
        try {
            firstPersonCheckBox.setSelected(((Boolean)oldData.get(firstPersonCheckBox)).booleanValue());
            lookupCheckBox.setSelected(((Boolean)oldData.get(lookupCheckBox)).booleanValue());
            personCheckBox.setSelected(((Boolean)oldData.get(personCheckBox)).booleanValue());
            sentenceCheckBox.setSelected(((Boolean)oldData.get(sentenceCheckBox)).booleanValue());
            spaceTokenCheckBox.setSelected(((Boolean)oldData.get(spaceTokenCheckBox)).booleanValue());
            splitCheckBox.setSelected(((Boolean)oldData.get(splitCheckBox)).booleanValue());
            tokenCheckBox.setSelected(((Boolean)oldData.get(tokenCheckBox)).booleanValue());
            unknownCheckBox.setSelected(((Boolean)oldData.get(unknownCheckBox)).booleanValue());

            dateCheckBox.setSelected(((Boolean)oldData.get(unknownCheckBox)).booleanValue());
            jobTitleCheckBox.setSelected(((Boolean)oldData.get(jobTitleCheckBox)).booleanValue());
            locationCheckBox.setSelected(((Boolean)oldData.get(locationCheckBox)).booleanValue());
            organizationCheckBox.setSelected(((Boolean)oldData.get(organizationCheckBox)).booleanValue());
            tempCheckBox.setSelected(((Boolean)oldData.get(tempCheckBox)).booleanValue());

            identifierCheckBox.setSelected(((Boolean)oldData.get(identifierCheckBox)).booleanValue());
            moneyCheckBox.setSelected(((Boolean)oldData.get(moneyCheckBox)).booleanValue());
            percentCheckBox.setSelected(((Boolean)oldData.get(percentCheckBox)).booleanValue());
            titleCheckBox.setSelected(((Boolean)oldData.get(titleCheckBox)).booleanValue());

            otherTextField.setText( (String) oldData.get(otherTextField) );
        }
        catch(Exception e) {}
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

        annieTabbedPane = new SimpleTabbedPane();
        acceptedAnnotationsPanel = new javax.swing.JPanel();
        annotationTypeLabel = new SimpleLabel();
        typesPanel = new javax.swing.JPanel();
        allCheckBox = new SimpleCheckBox();
        jSeparator = new javax.swing.JSeparator();
        checkboxesPanel = new javax.swing.JPanel();
        checkboxesPanel1 = new javax.swing.JPanel();
        firstPersonCheckBox = new SimpleCheckBox();
        lookupCheckBox = new SimpleCheckBox();
        personCheckBox = new SimpleCheckBox();
        sentenceCheckBox = new SimpleCheckBox();
        spaceTokenCheckBox = new SimpleCheckBox();
        splitCheckBox = new SimpleCheckBox();
        tokenCheckBox = new SimpleCheckBox();
        unknownCheckBox = new SimpleCheckBox();
        dateCheckBox = new SimpleCheckBox();
        checkboxesPanel2 = new javax.swing.JPanel();
        jobTitleCheckBox = new SimpleCheckBox();
        locationCheckBox = new SimpleCheckBox();
        organizationCheckBox = new SimpleCheckBox();
        tempCheckBox = new SimpleCheckBox();
        identifierCheckBox = new SimpleCheckBox();
        moneyCheckBox = new SimpleCheckBox();
        percentCheckBox = new SimpleCheckBox();
        titleCheckBox = new SimpleCheckBox();
        otherPanel = new javax.swing.JPanel();
        otherLabel = new SimpleLabel();
        otherTextField = new SimpleField();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setTitle("Configure GATE Annie extractor");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        acceptedAnnotationsPanel.setLayout(new java.awt.GridBagLayout());

        annotationTypeLabel.setText("<html>Select annotation types which Wandora should convert to topics and associations. Write additional types to 'Other' field as a comma separated list. Selecting 'Accept ALL' includes also nonlisted annotation types.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 12, 6);
        acceptedAnnotationsPanel.add(annotationTypeLabel, gridBagConstraints);

        typesPanel.setLayout(new java.awt.GridBagLayout());

        allCheckBox.setText("Accept ALL annotation types");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        typesPanel.add(allCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        typesPanel.add(jSeparator, gridBagConstraints);

        checkboxesPanel.setLayout(new java.awt.GridBagLayout());

        checkboxesPanel1.setLayout(new java.awt.GridBagLayout());

        firstPersonCheckBox.setSelected(true);
        firstPersonCheckBox.setText("FirstPerson");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        checkboxesPanel1.add(firstPersonCheckBox, gridBagConstraints);

        lookupCheckBox.setSelected(true);
        lookupCheckBox.setText("Lookup");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        checkboxesPanel1.add(lookupCheckBox, gridBagConstraints);

        personCheckBox.setSelected(true);
        personCheckBox.setText("Person");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        checkboxesPanel1.add(personCheckBox, gridBagConstraints);

        sentenceCheckBox.setText("Sentence");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        checkboxesPanel1.add(sentenceCheckBox, gridBagConstraints);

        spaceTokenCheckBox.setText("SpaceToken");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        checkboxesPanel1.add(spaceTokenCheckBox, gridBagConstraints);

        splitCheckBox.setText("Split");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        checkboxesPanel1.add(splitCheckBox, gridBagConstraints);

        tokenCheckBox.setText("Token");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        checkboxesPanel1.add(tokenCheckBox, gridBagConstraints);

        unknownCheckBox.setSelected(true);
        unknownCheckBox.setText("Unknown");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        checkboxesPanel1.add(unknownCheckBox, gridBagConstraints);

        dateCheckBox.setSelected(true);
        dateCheckBox.setText("Date");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        checkboxesPanel1.add(dateCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        checkboxesPanel.add(checkboxesPanel1, gridBagConstraints);

        checkboxesPanel2.setLayout(new java.awt.GridBagLayout());

        jobTitleCheckBox.setText("JobTitle");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        checkboxesPanel2.add(jobTitleCheckBox, gridBagConstraints);

        locationCheckBox.setSelected(true);
        locationCheckBox.setText("Location");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        checkboxesPanel2.add(locationCheckBox, gridBagConstraints);

        organizationCheckBox.setSelected(true);
        organizationCheckBox.setText("Organization");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        checkboxesPanel2.add(organizationCheckBox, gridBagConstraints);

        tempCheckBox.setText("Temp");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        checkboxesPanel2.add(tempCheckBox, gridBagConstraints);

        identifierCheckBox.setSelected(true);
        identifierCheckBox.setText("Identifier");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        checkboxesPanel2.add(identifierCheckBox, gridBagConstraints);

        moneyCheckBox.setText("Money");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        checkboxesPanel2.add(moneyCheckBox, gridBagConstraints);

        percentCheckBox.setText("Percent");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        checkboxesPanel2.add(percentCheckBox, gridBagConstraints);

        titleCheckBox.setSelected(true);
        titleCheckBox.setText("Title");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        checkboxesPanel2.add(titleCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        checkboxesPanel.add(checkboxesPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        typesPanel.add(checkboxesPanel, gridBagConstraints);

        otherPanel.setLayout(new java.awt.GridBagLayout());

        otherLabel.setText("Other");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        otherPanel.add(otherLabel, gridBagConstraints);

        otherTextField.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        otherPanel.add(otherTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        typesPanel.add(otherPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 6, 12);
        acceptedAnnotationsPanel.add(typesPanel, gridBagConstraints);

        annieTabbedPane.addTab("Annotation types", acceptedAnnotationsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(annieTabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

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

        okButton.setText("OK");
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
        annotationTypeUpdateRequired = true;
        accepted = true;
        setVisible(false);
    }//GEN-LAST:event_okButtonMouseReleased

    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        accepted = false;
        setVisible(false);
        restoreOldData();
    }//GEN-LAST:event_cancelButtonMouseReleased



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel acceptedAnnotationsPanel;
    private javax.swing.JCheckBox allCheckBox;
    private javax.swing.JTabbedPane annieTabbedPane;
    private javax.swing.JLabel annotationTypeLabel;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel checkboxesPanel;
    private javax.swing.JPanel checkboxesPanel1;
    private javax.swing.JPanel checkboxesPanel2;
    private javax.swing.JCheckBox dateCheckBox;
    private javax.swing.JCheckBox firstPersonCheckBox;
    private javax.swing.JCheckBox identifierCheckBox;
    private javax.swing.JSeparator jSeparator;
    private javax.swing.JCheckBox jobTitleCheckBox;
    private javax.swing.JCheckBox locationCheckBox;
    private javax.swing.JCheckBox lookupCheckBox;
    private javax.swing.JCheckBox moneyCheckBox;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox organizationCheckBox;
    private javax.swing.JLabel otherLabel;
    private javax.swing.JPanel otherPanel;
    private javax.swing.JTextField otherTextField;
    private javax.swing.JCheckBox percentCheckBox;
    private javax.swing.JCheckBox personCheckBox;
    private javax.swing.JCheckBox sentenceCheckBox;
    private javax.swing.JCheckBox spaceTokenCheckBox;
    private javax.swing.JCheckBox splitCheckBox;
    private javax.swing.JCheckBox tempCheckBox;
    private javax.swing.JCheckBox titleCheckBox;
    private javax.swing.JCheckBox tokenCheckBox;
    private javax.swing.JPanel typesPanel;
    private javax.swing.JCheckBox unknownCheckBox;
    // End of variables declaration//GEN-END:variables

}
