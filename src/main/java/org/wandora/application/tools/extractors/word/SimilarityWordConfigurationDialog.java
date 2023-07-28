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
 */
package org.wandora.application.tools.extractors.word;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;

/**
 *
 * @author Eero Lehtonen
 */
class SimilarityWordConfigurationDialog extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	private Wandora wandora = null;
    private boolean wasAccepted = false;
    private JDialog myDialog = null;
    private SimilarityWordConfiguration config;
    private SimilarityWordConfiguration newConfig;

    private static final float THRESHOLD_RESOLUTION = 100f;
    
    protected boolean wasAccepted() {
        return wasAccepted;
    }

    public SimilarityWordConfigurationDialog(Wandora w) {
        wandora = w;
        initComponents();

    }

    public void openDialog(SimilarityWordConfiguration c) {
        wasAccepted = false;
        if (myDialog == null) {
            myDialog = new JDialog(wandora, true);
            myDialog.add(this);
            myDialog.setSize(330, 310);
            myDialog.setTitle("Word Similarity Extractor Configuration");
            UIBox.centerWindow(myDialog, wandora);
        }

        newConfig = (c != null) ? c : new SimilarityWordConfiguration();
        toggleCaseSensitive.setSelected(newConfig.getCaseSensitive());
        toggleBaseName.setSelected(newConfig.getBaseName());
        toggleVariantName.setSelected(newConfig.getVariantName());
        toggleInstanceData.setSelected(newConfig.getInstanceData());

        PlainDocument thresholdDocument = (PlainDocument)thresholdDisplay.getDocument();
        thresholdDocument.setDocumentFilter(new ThresholdFilter());
        
        thresholdDisplay.setValue(newConfig.getThreshold());
        
        thresholdDisplay.addPropertyChangeListener(new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                float displayValue;
                try {
                    displayValue = (float)thresholdDisplay.getValue();
                } catch (ClassCastException cce) {
                    displayValue = ((Double)thresholdDisplay.getValue()).floatValue();
                }
                int newVal = Math.round(displayValue*THRESHOLD_RESOLUTION);
                
                if(newVal != thresholdSlider.getValue())
                    thresholdSlider.setValue(newVal);
            }
            
        });
        
        thresholdSlider.setMinimum(0);
        thresholdSlider.setMaximum(Math.round(THRESHOLD_RESOLUTION));
        
        thresholdSlider.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                float newVal = thresholdSlider.getValue()/THRESHOLD_RESOLUTION;
                float displayValue;
                try {
                    displayValue = (float)thresholdDisplay.getValue();
                } catch (ClassCastException cce) {
                    displayValue = ((Double)thresholdDisplay.getValue()).floatValue();
                }
                if(newVal != displayValue)
                    thresholdDisplay.setValue(thresholdSlider.getValue()/THRESHOLD_RESOLUTION);
            }
            
        });
        
        List<String> MetricNameList = newConfig.getSTringMetricNames();
        String[] metricNameArray = new String[MetricNameList.size()];
        metrics.setModel(new DefaultComboBoxModel(MetricNameList.toArray(metricNameArray)));
        
        myDialog.setVisible(true);
    }
    
    class ThresholdFilter extends DocumentFilter{
        
        private boolean test(String s){
            try {
                float f = Float.parseFloat(s);
                return (f >= 0 && f <= 1);
            } catch (NumberFormatException e) {
                System.out.println(s);
                return false;
            }
        }
        
        @Override
        public void insertString(FilterBypass fb, int offset, String string,
         AttributeSet attr) throws BadLocationException {
            
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.insert(offset, string);
            
            if(test(sb.toString())){
                super.insertString(fb, offset, string, attr);
            }
            
        }
        
        @Override
        public void replace(FilterBypass fb, int offset, int length, String string,
         AttributeSet attr) throws BadLocationException {
            
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.replace(offset, offset+length, string);
            
            if(test(sb.toString())){
                super.replace(fb, offset, length, string, attr);
            }
            
        }
        
        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {
            
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.delete(offset, offset+length);
            
            if(test(sb.toString())){
                super.remove(fb, offset, length);
            }
            
        }
    }

    private void saveConfiguration() {
        if (newConfig == null) {
            newConfig = new SimilarityWordConfiguration();
        }

        newConfig.setCaseSensitive(toggleCaseSensitive.isSelected());
        newConfig.setBaseName(toggleBaseName.isSelected());
        newConfig.setVariantName(toggleVariantName.isSelected());
        newConfig.setInstanceData(toggleInstanceData.isSelected());

        float displayValue;
        try {
            displayValue = (float)thresholdDisplay.getValue();
        } catch (ClassCastException cce) {
            displayValue = ((Double)thresholdDisplay.getValue()).floatValue();
        }
        
        newConfig.setThreshold(displayValue);
        
        config = newConfig;

    }

    protected SimilarityWordConfiguration getConfiguration() {
        return config;
    }

    ;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        container = new javax.swing.JPanel();
        toggleCaseSensitive = new SimpleCheckBox();
        toggleBaseName = new SimpleCheckBox();
        toggleVariantName = new SimpleCheckBox();
        toggleInstanceData = new SimpleCheckBox();
        metrics = new javax.swing.JComboBox();
        metricsLabel = new javax.swing.JLabel();
        thresholdSlider = new javax.swing.JSlider();
        thresholdLabel = new javax.swing.JLabel();
        thresholdDisplay = new javax.swing.JFormattedTextField();
        submit = new SimpleButton();
        cancel = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        container.setLayout(new java.awt.GridBagLayout());

        toggleCaseSensitive.setText("Case sensitive");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleCaseSensitive, gridBagConstraints);

        toggleBaseName.setText("Base Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleBaseName, gridBagConstraints);

        toggleVariantName.setText("Variant Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleVariantName, gridBagConstraints);

        toggleInstanceData.setText("Instance Data");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        container.add(toggleInstanceData, gridBagConstraints);

        metrics.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        container.add(metrics, gridBagConstraints);

        metricsLabel.setText("Similarity metric");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        container.add(metricsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        container.add(thresholdSlider, gridBagConstraints);

        thresholdLabel.setText("Threshold");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        container.add(thresholdLabel, gridBagConstraints);

        thresholdDisplay.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        container.add(thresholdDisplay, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(container, gridBagConstraints);

        submit.setText("OK");
        submit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        add(submit, gridBagConstraints);

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 4);
        add(cancel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        wasAccepted = false;
        if (myDialog != null) {
            myDialog.setVisible(false);
        }
    }//GEN-LAST:event_cancelActionPerformed

    private void submitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitActionPerformed
        wasAccepted = true;
        saveConfiguration();
        if (myDialog != null) {
            myDialog.setVisible(false);
        }
    }//GEN-LAST:event_submitActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel;
    private javax.swing.JPanel container;
    private javax.swing.JComboBox metrics;
    private javax.swing.JLabel metricsLabel;
    private javax.swing.JButton submit;
    private javax.swing.JFormattedTextField thresholdDisplay;
    private javax.swing.JLabel thresholdLabel;
    private javax.swing.JSlider thresholdSlider;
    private javax.swing.JCheckBox toggleBaseName;
    private javax.swing.JCheckBox toggleCaseSensitive;
    private javax.swing.JCheckBox toggleInstanceData;
    private javax.swing.JCheckBox toggleVariantName;
    // End of variables declaration//GEN-END:variables
}
