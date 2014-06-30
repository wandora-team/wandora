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
 *
 * SelectTranslationLanguagesPanel.java
 *
 * Created on 6.2.2011, 21:19:12
 */




package org.wandora.application.gui;

import com.memetix.mst.language.Language;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JDialog;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import org.wandora.application.Wandora;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleCheckBox;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author akivela
 */
public class SelectMicrosoftTranslationLanguagesPanel extends javax.swing.JPanel {

    private static Language source = null;
    private static Language[] targets = null;

    private boolean accepted = false;
    private JDialog dialog = null;

    private Language[] langs = Language.values();
    private Language[] someLangs = null;



    /** Creates new form SelectMicrosoftTranslationLanguagesPanel */
    public SelectMicrosoftTranslationLanguagesPanel() {
        initComponents();
    }


    public void openInDialog(Wandora wandora) {
        someLangs = filterLangs(wandora.getTopicMap(), langs);
        setLanguages();

        accepted = false;

        dialog = new JDialog(wandora, true);
        dialog.setSize(500,470);
        dialog.setTitle("Select source and target languages");
        wandora.centerWindow(dialog);
        dialog.add(this);
        dialog.setVisible(true);
    }


    
    public void notInTopicMapsContext() {
        overrideCheckBox.setVisible(false);
        createTopicsCheckBox.setVisible(false);
    }


    private Language[] filterLangs(TopicMap tm, Language[] ls) {
        ArrayList<Language> some = new ArrayList<Language>();
        some.add(Language.ENGLISH);
        some.add(Language.FINNISH);
        some.add(Language.SWEDISH);
        some.add(Language.FRENCH);
        some.add(Language.GERMAN);
        some.add(Language.SPANISH);
        some.add(Language.CHINESE_SIMPLIFIED);
        return some.toArray( new Language[] {} );
    }

    /*
    private Language[] filterLangs(TopicMap tm, Language[] ls) {
        ArrayList<Language> some = new ArrayList<Language>();
        for(int i=0; i<ls.length; i++) {
            try {
                Language l = ls[i];
                String n = l.name();
                String abb = n.toLowerCase().substring(0,2);
                Topic lant = tm.getTopic(XTMPSI.LANG_PREFIX+abb);
                if(lant != null) {
                    some.add(l);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return some.toArray( new Language[] {} );
    }
     */


    private void setLanguages() {
        if(allLanguagesCheckBox.isSelected()) {
            sourceList.setModel(new LanguageListModel(langs));
            targetList.setModel(new LanguageListModel(langs));
        }
        else {
            sourceList.setModel(new LanguageListModel(someLangs));
            targetList.setModel(new LanguageListModel(someLangs));
        }

        if(source != null) sourceList.setSelectedValue(source.name(), true);
        //if(targets != null) targetList.setS(targets);
    }


    public boolean wasAccepted() {
        return accepted;
    }
    public boolean overrideExisting() {
        return overrideCheckBox.isSelected();
    }
    public boolean createTopics() {
        return createTopicsCheckBox.isSelected();
    }
    public boolean markTranslatedText() {
        return markTranslationCheckBox.isSelected();
    }

    

    public Language getSourceLanguage() {
        int i = sourceList.getSelectedIndex();
        if(allLanguagesCheckBox.isSelected()) {
            source = langs[i];
        }
        else {
            source = someLangs[i];
        }
        return source;
    }


    public Collection<Language> getTargetLanguages() {
        int[] iis = targetList.getSelectedIndices();
        Collection<Language> languages = new ArrayList<Language>();
        if(allLanguagesCheckBox.isSelected()) {
            for(int i=0; i<iis.length; i++) {
                languages.add(langs[iis[i]]);
            }
        }
        else {
            for(int i=0; i<iis.length; i++) {
                languages.add(someLangs[iis[i]]);
            }
        }
        targets = languages.toArray( new Language[] {} );
        return languages;
    }




    /* ---------------------------------------------------------------------- */
    /* ---------------------------------------------------------------------- */



    private class LanguageListModel implements ListModel {
        Language[] model = null;


        public LanguageListModel(Language[] m) {
            model = m;
        }


        public int getSize() {
            return model.length;
        }

        public Object getElementAt(int index) {
            String lan = model[index].name();
            lan = lan.substring(0,1)+lan.substring(1).toLowerCase();
            return lan;
        }

        public void addListDataListener(ListDataListener l) {

        }

        public void removeListDataListener(ListDataListener l) {

        }

    }




    /* ---------------------------------------------------------------------- */
    /* ---------------------------------------------------------------------- */
    /* ---------------------------------------------------------------------- */

    


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        innerPanel = new javax.swing.JPanel();
        contentPanel = new javax.swing.JPanel();
        infoLabel = new SimpleLabel();
        sourceLabel = new SimpleLabel();
        sourceScrollPane = new javax.swing.JScrollPane();
        sourceList = new javax.swing.JList();
        targetLabel = new SimpleLabel();
        targetScrollPane = new javax.swing.JScrollPane();
        targetList = new javax.swing.JList();
        allLanguagesCheckBox = new SimpleCheckBox();
        overrideCheckBox = new SimpleCheckBox();
        createTopicsCheckBox = new SimpleCheckBox();
        markTranslationCheckBox = new SimpleCheckBox();
        buttonPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();
        translateButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        innerPanel.setLayout(new java.awt.GridBagLayout());

        contentPanel.setLayout(new java.awt.GridBagLayout());

        infoLabel.setText("<html>Select source and target langugages. Checking Show all languages views all languages available in Microsoft Translate API. Checking Mark translated text adds translation a static suffix helping to recognize and verify translations.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        contentPanel.add(infoLabel, gridBagConstraints);

        sourceLabel.setText("Source language");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 4);
        contentPanel.add(sourceLabel, gridBagConstraints);

        sourceScrollPane.setMinimumSize(new java.awt.Dimension(100, 100));
        sourceScrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        sourceList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        sourceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sourceScrollPane.setViewportView(sourceList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        contentPanel.add(sourceScrollPane, gridBagConstraints);

        targetLabel.setText("Target languages");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 4);
        contentPanel.add(targetLabel, gridBagConstraints);

        targetScrollPane.setMinimumSize(new java.awt.Dimension(100, 100));
        targetScrollPane.setPreferredSize(new java.awt.Dimension(100, 100));

        targetList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        targetScrollPane.setViewportView(targetList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 3, 3, 0);
        contentPanel.add(targetScrollPane, gridBagConstraints);

        allLanguagesCheckBox.setText("Show all languages");
        allLanguagesCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                allLanguagesCheckBoxMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        contentPanel.add(allLanguagesCheckBox, gridBagConstraints);

        overrideCheckBox.setSelected(true);
        overrideCheckBox.setText("Override existing");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        contentPanel.add(overrideCheckBox, gridBagConstraints);

        createTopicsCheckBox.setText("Create necessary topics");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        contentPanel.add(createTopicsCheckBox, gridBagConstraints);

        markTranslationCheckBox.setSelected(true);
        markTranslationCheckBox.setText("Mark translated text");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        contentPanel.add(markTranslationCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        innerPanel.add(contentPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout fillerPanelLayout = new javax.swing.GroupLayout(fillerPanel);
        fillerPanel.setLayout(fillerPanelLayout);
        fillerPanelLayout.setHorizontalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        fillerPanelLayout.setVerticalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(fillerPanel, gridBagConstraints);

        translateButton.setText("Continue");
        translateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                translateButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(translateButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 2, 0);
        innerPanel.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(innerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void translateButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_translateButtonMouseReleased
        accepted = true;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_translateButtonMouseReleased

    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        accepted = false;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonMouseReleased

    private void allLanguagesCheckBoxMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_allLanguagesCheckBoxMouseReleased
        setLanguages();
    }//GEN-LAST:event_allLanguagesCheckBoxMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allLanguagesCheckBox;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JCheckBox createTopicsCheckBox;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JCheckBox markTranslationCheckBox;
    private javax.swing.JCheckBox overrideCheckBox;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JList sourceList;
    private javax.swing.JScrollPane sourceScrollPane;
    private javax.swing.JLabel targetLabel;
    private javax.swing.JList targetList;
    private javax.swing.JScrollPane targetScrollPane;
    private javax.swing.JButton translateButton;
    // End of variables declaration//GEN-END:variables

}
