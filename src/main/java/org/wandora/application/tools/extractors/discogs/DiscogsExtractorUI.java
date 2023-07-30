/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2013 Wandora Team
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

package org.wandora.application.tools.extractors.discogs;

import java.awt.Component;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDialog;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleComboBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author nlaitine
 */

public class DiscogsExtractorUI extends javax.swing.JPanel {
    

	private static final long serialVersionUID = 1L;
	
	
	private boolean accepted = false;
    private JDialog dialog = null;
    private Context context = null;
    private static final String DISCOGS_API_BASE = "http://api.discogs.com/database/search";

    /**
     * Creates new form DiscogsExtractorUI
     */
    public DiscogsExtractorUI() {
        initComponents();
    }
    
    public boolean wasAccepted() {
        return accepted;
    }

    public void setAccepted(boolean b) {
        accepted = b;
    }

    public void open(Wandora w, Context c) {
        context = c;
        accepted = false;
        dialog = new JDialog(w, true);
        dialog.setSize(500, 240);
        dialog.add(this);
        dialog.setTitle("Discogs API extractor");
        UIBox.centerWindow(dialog, w);
        dialog.setVisible(true);
    }

    public WandoraTool[] getExtractors(DiscogsExtractor tool) throws TopicMapException {
        WandoraTool[] wtArray = null;
        WandoraTool wt = null;
        List<WandoraTool> wts = new ArrayList<>();
        Component tab = TabsPane.getSelectedComponent();
        String extractUrl;

        // ***** SEARCH *****
        if (searchPanel.equals(tab)) { 
            String type = searchTypeComboBox.getSelectedItem().toString();
            String query = searchTextField.getText();

            extractUrl = DISCOGS_API_BASE + "?type=" + type + "&q=" + urlEncode(query);
            System.out.println("Search URL: " + extractUrl);
            
            if (type.equals("release")) {
                DiscogsReleaseExtractor ex = new DiscogsReleaseExtractor();
                ex.setForceUrls(new String[]{extractUrl});
                wts.add(ex);
                wtArray = wts.toArray(new WandoraTool[]{});
            } else if (type.equals("master")) {
                DiscogsMasterExtractor ex = new DiscogsMasterExtractor();
                ex.setForceUrls(new String[]{extractUrl});
                wts.add(ex);
                wtArray = wts.toArray(new WandoraTool[]{});
            } else if (type.equals("artist")) {
                DiscogsArtistExtractor ex = new DiscogsArtistExtractor();
                ex.setForceUrls(new String[]{extractUrl});
                wts.add(ex);
                wtArray = wts.toArray(new WandoraTool[]{});
            } else if (type.equals("label")) {
                DiscogsLabelExtractor ex = new DiscogsLabelExtractor();
                ex.setForceUrls(new String[]{extractUrl});
                wts.add(ex);
                wtArray = wts.toArray(new WandoraTool[]{});
            }
        }
        
        if (releaseSearchPanel.equals(tab)) {
            String query = releaseTextField.getText();
            extractUrl = DISCOGS_API_BASE + "?type=release&q=" + urlEncode(query);
            System.out.println("Search URL: " + extractUrl);
            
            DiscogsReleaseExtractor ex = new DiscogsReleaseExtractor();
            ex.setForceUrls( new String[] {extractUrl} );
            wts.add(ex);
            wtArray = wts.toArray(new WandoraTool[]{});
        } else if (masterSearchPanel.equals(tab)) {
            String query = masterTextField.getText();
            extractUrl = DISCOGS_API_BASE + "?type=master&q=" + urlEncode(query);
            System.out.println("Search URL: " + extractUrl);
            
            DiscogsMasterExtractor ex = new DiscogsMasterExtractor();
            ex.setForceUrls( new String[] {extractUrl} );
            wts.add(ex);
            wtArray = wts.toArray(new WandoraTool[]{});
        } else if (artistSearchPanel.equals(tab)) {
            String query = artistTextField.getText();
            extractUrl = DISCOGS_API_BASE + "?type=artist&q=" + urlEncode(query);
            System.out.println("Search URL: " + extractUrl);
            
            DiscogsArtistExtractor ex = new DiscogsArtistExtractor();
            ex.setForceUrls( new String[] {extractUrl} );
            wts.add(ex);
            wtArray = wts.toArray(new WandoraTool[]{});
        } else if (labelSearchPanel.equals(tab)) {
            String query = labelTextField.getText();
            extractUrl = DISCOGS_API_BASE + "?type=label&q=" + urlEncode(query);
            System.out.println("Search URL: " + extractUrl);
            
            DiscogsLabelExtractor ex = new DiscogsLabelExtractor();
            ex.setForceUrls( new String[] {extractUrl} );
            wts.add(ex);
            wtArray = wts.toArray(new WandoraTool[]{});
        }
        
        return wtArray;
    }

    protected static String urlEncode(String str) {
        try {
            str = URLEncoder.encode(str, "utf-8");
        } catch (Exception e) {
        }
        return str;
    }
    
    public String getContextAsString() {
        StringBuilder sb = new StringBuilder("");
        if(context != null) {
            try {
                Iterator contextObjects = context.getContextObjects();
                String str = null;
                Object o = null;
                while(contextObjects.hasNext()) {
                    str = null;
                    o = contextObjects.next();
                    if(o instanceof Topic) {
                        Topic t = (Topic) o;
                        str = t.getDisplayName(AbstractDiscogsExtractor.LANG);
                        if(str != null) {
                            str = str.trim();
                        }
                    }
                    
                    if(str != null && str.length() > 0) {
                        sb.append(str);
                        if(contextObjects.hasNext()) {
                            sb.append(", ");
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        MainPanel = new javax.swing.JPanel();
        TabsPane = new SimpleTabbedPane();
        searchPanel = new javax.swing.JPanel();
        searchTypeComboBox = new SimpleComboBox();
        headlineLabel = new SimpleLabel();
        searchTextField = new SimpleField();
        releaseSearchPanel = new javax.swing.JPanel();
        releaseLabel = new SimpleLabel();
        releaseTextField = new SimpleField();
        releaseButton = new SimpleButton();
        masterSearchPanel = new javax.swing.JPanel();
        masterLabel = new SimpleLabel();
        masterTextField = new SimpleField();
        masterButton = new SimpleButton();
        artistSearchPanel = new javax.swing.JPanel();
        artistLabel = new SimpleLabel();
        artistTextField = new SimpleField();
        artistButton = new SimpleButton();
        labelSearchPanel = new javax.swing.JPanel();
        labelLabel = new SimpleLabel();
        labelTextField = new SimpleField();
        labelButton = new SimpleButton();
        bottomPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setMinimumSize(new java.awt.Dimension(360, 250));
        setPreferredSize(new java.awt.Dimension(360, 250));
        setLayout(new java.awt.GridBagLayout());

        MainPanel.setMinimumSize(new java.awt.Dimension(250, 100));
        MainPanel.setPreferredSize(new java.awt.Dimension(250, 100));
        MainPanel.setLayout(new java.awt.GridBagLayout());

        TabsPane.setMinimumSize(new java.awt.Dimension(80, 160));
        TabsPane.setPreferredSize(new java.awt.Dimension(32767, 32767));

        searchPanel.setMinimumSize(new java.awt.Dimension(70, 140));
        searchPanel.setLayout(new java.awt.GridBagLayout());

        searchTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "release", "master", "artist", "label" }));
        searchTypeComboBox.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 4);
        searchPanel.add(searchTypeComboBox, gridBagConstraints);

        headlineLabel.setText("Search Discogs database by entering a search term");
        headlineLabel.setMaximumSize(new java.awt.Dimension(64, 14));
        headlineLabel.setMinimumSize(new java.awt.Dimension(64, 14));
        headlineLabel.setPreferredSize(new java.awt.Dimension(230, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        searchPanel.add(headlineLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        searchPanel.add(searchTextField, gridBagConstraints);

        TabsPane.addTab("Search", searchPanel);

        releaseSearchPanel.setLayout(new java.awt.GridBagLayout());

        releaseLabel.setText("<html>Search releases by context. Press Get context and Extract to extract by context.</html>");
        releaseLabel.setToolTipText("");
        releaseLabel.setMaximumSize(new java.awt.Dimension(2147483647, 50));
        releaseLabel.setMinimumSize(new java.awt.Dimension(250, 50));
        releaseLabel.setPreferredSize(new java.awt.Dimension(250, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        releaseSearchPanel.add(releaseLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 0, 20);
        releaseSearchPanel.add(releaseTextField, gridBagConstraints);

        releaseButton.setText("Get context");
        releaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                releaseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        releaseSearchPanel.add(releaseButton, gridBagConstraints);

        TabsPane.addTab("Release search", releaseSearchPanel);

        masterSearchPanel.setLayout(new java.awt.GridBagLayout());

        masterLabel.setText("<html>Search masters by context. Press Get context and Extract to extract by context.</html>");
        masterLabel.setMaximumSize(new java.awt.Dimension(250, 50));
        masterLabel.setMinimumSize(new java.awt.Dimension(250, 50));
        masterLabel.setPreferredSize(new java.awt.Dimension(250, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        masterSearchPanel.add(masterLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 0, 20);
        masterSearchPanel.add(masterTextField, gridBagConstraints);

        masterButton.setText("Get context");
        masterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                masterButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        masterSearchPanel.add(masterButton, gridBagConstraints);

        TabsPane.addTab("Master search", masterSearchPanel);

        artistSearchPanel.setLayout(new java.awt.GridBagLayout());

        artistLabel.setText("<html>Search artists by context. Press Get context and Extract to extract by context.</html>");
        artistLabel.setMaximumSize(new java.awt.Dimension(2147483647, 50));
        artistLabel.setMinimumSize(new java.awt.Dimension(250, 50));
        artistLabel.setPreferredSize(new java.awt.Dimension(250, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        artistSearchPanel.add(artistLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 0, 20);
        artistSearchPanel.add(artistTextField, gridBagConstraints);

        artistButton.setText("Get context");
        artistButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                artistButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        artistSearchPanel.add(artistButton, gridBagConstraints);

        TabsPane.addTab("Artist search", artistSearchPanel);

        labelSearchPanel.setLayout(new java.awt.GridBagLayout());

        labelLabel.setText("<html>Search labels by context. Press Get context and Extract to extract by context.</html>");
        labelLabel.setMaximumSize(new java.awt.Dimension(2147483647, 50));
        labelLabel.setMinimumSize(new java.awt.Dimension(250, 50));
        labelLabel.setPreferredSize(new java.awt.Dimension(250, 50));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        labelSearchPanel.add(labelLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 20, 0, 20);
        labelSearchPanel.add(labelTextField, gridBagConstraints);

        labelButton.setText("Get context");
        labelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        labelSearchPanel.add(labelButton, gridBagConstraints);

        TabsPane.addTab("Label search", labelSearchPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        MainPanel.add(TabsPane, gridBagConstraints);
        TabsPane.getAccessibleContext().setAccessibleName("search");

        bottomPanel.setMinimumSize(new java.awt.Dimension(250, 35));
        bottomPanel.setPreferredSize(new java.awt.Dimension(250, 35));
        bottomPanel.setLayout(new java.awt.GridBagLayout());

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
        bottomPanel.add(fillerPanel, gridBagConstraints);

        okButton.setText("Extract");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        bottomPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        bottomPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        MainPanel.add(bottomPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(MainPanel, gridBagConstraints);
        MainPanel.getAccessibleContext().setAccessibleName("Discogs search");
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        accepted = true;
        if (this.dialog != null) {
            this.dialog.setVisible(false);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        accepted = false;
        if (this.dialog != null) {
            this.dialog.setVisible(false);
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void releaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_releaseButtonActionPerformed
            releaseTextField.setText(getContextAsString());
    }//GEN-LAST:event_releaseButtonActionPerformed

    private void masterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_masterButtonActionPerformed
            masterTextField.setText(getContextAsString());
    }//GEN-LAST:event_masterButtonActionPerformed

    private void artistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_artistButtonActionPerformed
            artistTextField.setText(getContextAsString());
    }//GEN-LAST:event_artistButtonActionPerformed

    private void labelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_labelButtonActionPerformed
            labelTextField.setText(getContextAsString());
    }//GEN-LAST:event_labelButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DiscogsExtractorUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DiscogsExtractorUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DiscogsExtractorUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DiscogsExtractorUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DiscogsExtractorUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel MainPanel;
    private javax.swing.JTabbedPane TabsPane;
    private javax.swing.JButton artistButton;
    private javax.swing.JLabel artistLabel;
    private javax.swing.JPanel artistSearchPanel;
    private javax.swing.JTextField artistTextField;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel headlineLabel;
    private javax.swing.JButton labelButton;
    private javax.swing.JLabel labelLabel;
    private javax.swing.JPanel labelSearchPanel;
    private javax.swing.JTextField labelTextField;
    private javax.swing.JButton masterButton;
    private javax.swing.JLabel masterLabel;
    private javax.swing.JPanel masterSearchPanel;
    private javax.swing.JTextField masterTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JButton releaseButton;
    private javax.swing.JLabel releaseLabel;
    private javax.swing.JPanel releaseSearchPanel;
    private javax.swing.JTextField releaseTextField;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JComboBox searchTypeComboBox;
    // End of variables declaration//GEN-END:variables
}
