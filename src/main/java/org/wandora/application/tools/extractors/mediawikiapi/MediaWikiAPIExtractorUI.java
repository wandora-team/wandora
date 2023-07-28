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
 */

package org.wandora.application.tools.extractors.mediawikiapi;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.simple.*;

/**
 *
 * @author Eero
 */

public class MediaWikiAPIExtractorUI extends javax.swing.JPanel {


	private static final long serialVersionUID = 1L;
	
	
	private int LIMIT = 100;
    private String FORMAT = "json";
    
    private boolean accepted = false;
    private JDialog dialog = null;
    private Context context = null;
    private Wandora wandora = null;
    
    /**
     * Creates new form MediaWikiAPIExtractorUI
     */
    public MediaWikiAPIExtractorUI() {
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
        wandora = w;
        accepted = false;
        dialog = new JDialog(w, true);
        dialog.setSize(550, 300);
        dialog.add(this);
        dialog.setTitle("MediaWiki API extractor");
        UIBox.centerWindow(dialog, w);
        
        dialog.setVisible(true);
    }

    public WandoraTool[] getExtractors(MediaWikiAPIExtractor tool) 
            throws Exception {
        
        WandoraTool wt;
        List<WandoraTool> wts = new ArrayList<>();
        
        StringBuilder urlBuilder = new StringBuilder();
        String baseUrl, extractUrl;
        String[] qType = new String[2];
        baseUrl = urlField.getText();
        
        boolean crawlClasses = classCrawlToggle.isSelected();

        MediaWikiAPIPageExtractor ex= 
                new MediaWikiAPIPageExtractor(baseUrl, qType, crawlClasses);
        
        //General parameters
        
        urlBuilder.append(baseUrl)
            .append("/api.php?action=query")
            .append("&format=")
            .append(FORMAT)
            .append("&continue="); //For legacy query continue
        
        Component selected = modeTabs.getSelectedComponent();
        
        if(selected.equals(catergoryPanel)){
            
            String category = categoryField.getText();
            if(category.length() > 0){
                qType[0] = "categorymembers";
                qType[1] = "cm";
                urlBuilder.append("&list=categorymembers&cmtitle=");
                if(category.length() < 9 || 
                   !category.substring(0, 9).equals("Category:")){
                    
                    urlBuilder.append("Category:");
                }
                urlBuilder.append(category)
                    .append("&cmlimit=")
                    .append(LIMIT);
            } else {
                qType[0] = "allpages";
                qType[1] = "ap";
                urlBuilder.append("&list=allpages")
                    .append("&aplimit=")
                    .append(LIMIT);
            }
            extractUrl = urlBuilder.toString();
            ex.setForceUrls(new String[]{extractUrl});
        } else if(selected.equals(prefixPanel)){
            qType[0] = "allpages";
            qType[1] = "ap";
            urlBuilder.append("&list=allpages")
                .append("&apfrom=")
                .append(prefixField.getText())
                .append("&aplimit=")
                .append(LIMIT);
            
            extractUrl = urlBuilder.toString();
            ex.setForceUrls(new String[]{extractUrl});
        } else if(selected.equals(searchPanel)){
            qType[0] = "search";
            qType[1] = "sr";
            urlBuilder.append("&list=search")
                .append("&srsearch=")
                .append(searchField.getText())
                .append("&srlimit=")
                .append(LIMIT);
            
            extractUrl = urlBuilder.toString();
            ex.setForceUrls(new String[]{extractUrl});
        } else if(selected.equals(titlePanel)){
            ex.setForceContent(titleTextArea.getText());
        } else {
            throw new Exception("Invalid panel!");
        }
        
        ex.setQueryUrl(urlBuilder.toString());
        
        wt = ex;
        wts.add(ex);
        
        return wts.toArray(new WandoraTool[]{});
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

        configPanel = new javax.swing.JPanel();
        urlLabel = new javax.swing.JLabel();
        urlField = new javax.swing.JTextField();
        modeTabs = new SimpleTabbedPane();
        catergoryPanel = new javax.swing.JPanel();
        categoryDescription = new SimpleLabel();
        categoryLabel = new SimpleLabel();
        categoryField = new javax.swing.JTextField();
        prefixPanel = new javax.swing.JPanel();
        prefixDescription = new SimpleLabel();
        prefixLabel = new SimpleLabel();
        prefixField = new javax.swing.JTextField();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new SimpleLabel();
        searchField = new javax.swing.JTextField();
        searchDescription = new SimpleLabel();
        titlePanel = new javax.swing.JPanel();
        titleDescription = new SimpleLabel();
        titleScrollPane = new SimpleScrollPane();
        titleTextArea = new SimpleTextArea();
        description = new SimpleLabel();
        buttonPanel = new javax.swing.JPanel();
        FillerjPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();
        classCrawlToggle = new SimpleCheckBox();

        setMinimumSize(new java.awt.Dimension(700, 150));
        setPreferredSize(new java.awt.Dimension(900, 150));
        setLayout(new java.awt.GridBagLayout());

        configPanel.setLayout(new java.awt.GridBagLayout());

        urlLabel.setText("Instance URL: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        configPanel.add(urlLabel, gridBagConstraints);

        urlField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 311;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        configPanel.add(urlField, gridBagConstraints);

        catergoryPanel.setLayout(new java.awt.GridBagLayout());

        categoryDescription.setText("<html><head></head><body>Specify a category used to filter articles in the wiki. Leave blank to extract all articles in the wiki.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 4);
        catergoryPanel.add(categoryDescription, gridBagConstraints);

        categoryLabel.setText("Category:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 4);
        catergoryPanel.add(categoryLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        catergoryPanel.add(categoryField, gridBagConstraints);

        modeTabs.addTab("Category", catergoryPanel);

        prefixPanel.setLayout(new java.awt.GridBagLayout());

        prefixDescription.setText("<html><head></head><body>Specify a title prefix used to filter articles in the wiki. Leave blank to extract all articles in the wiki.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 4);
        prefixPanel.add(prefixDescription, gridBagConstraints);

        prefixLabel.setText("Prefix:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(7, 4, 0, 4);
        prefixPanel.add(prefixLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        prefixPanel.add(prefixField, gridBagConstraints);

        modeTabs.addTab("Prefix", prefixPanel);

        searchPanel.setLayout(new java.awt.GridBagLayout());

        searchLabel.setText("Search:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 4);
        searchPanel.add(searchLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        searchPanel.add(searchField, gridBagConstraints);

        searchDescription.setText("<html><head></head><body>Specify a search term used to filter articles in the wiki. This is equivalent to the free text search available on the web page.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 4);
        searchPanel.add(searchDescription, gridBagConstraints);

        modeTabs.addTab("Search", searchPanel);

        titlePanel.setLayout(new java.awt.GridBagLayout());

        titleDescription.setText("<html><head></head><body>Use a comma separated list of article names to extract.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 4);
        titlePanel.add(titleDescription, gridBagConstraints);

        titleTextArea.setColumns(20);
        titleTextArea.setRows(5);
        titleScrollPane.setViewportView(titleTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        titlePanel.add(titleScrollPane, gridBagConstraints);

        modeTabs.addTab("Titles", titlePanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        configPanel.add(modeTabs, gridBagConstraints);

        description.setText("<html><head></head><body>Specify the URL where the wiki instance's api.php and index.php are reached. (http://en.wikipedia.org/w/ for the English Wikipedia) A few methods for filtering the articles to be extracted are presented below. Stub articles may be extracted for the article categories found for each article.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        configPanel.add(description, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(configPanel, gridBagConstraints);

        buttonPanel.setMaximumSize(new java.awt.Dimension(116, 25));
        buttonPanel.setMinimumSize(new java.awt.Dimension(116, 25));
        buttonPanel.setPreferredSize(new java.awt.Dimension(116, 25));
        buttonPanel.setLayout(new java.awt.GridBagLayout());

        FillerjPanel.setMaximumSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout FillerjPanelLayout = new javax.swing.GroupLayout(FillerjPanel);
        FillerjPanel.setLayout(FillerjPanelLayout);
        FillerjPanelLayout.setHorizontalGroup(
            FillerjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        FillerjPanelLayout.setVerticalGroup(
            FillerjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(FillerjPanel, gridBagConstraints);

        okButton.setText("OK");
        okButton.setMaximumSize(new java.awt.Dimension(70, 23));
        okButton.setMinimumSize(new java.awt.Dimension(70, 23));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                okButtonMouseReleased(evt);
            }
        });
        okButton.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                okButtonKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(cancelButton, gridBagConstraints);

        classCrawlToggle.setText("Crawl Article Categories");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        buttonPanel.add(classCrawlToggle, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void urlFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_urlFieldActionPerformed

    private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
        accepted = true;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_okButtonMouseReleased

    private void okButtonKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_okButtonKeyReleased
        accepted = true;
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if(dialog != null) {
                dialog.setVisible(false);
            }
        }
    }//GEN-LAST:event_okButtonKeyReleased

    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        accepted = false;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FillerjPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel categoryDescription;
    private javax.swing.JTextField categoryField;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JPanel catergoryPanel;
    private javax.swing.JCheckBox classCrawlToggle;
    private javax.swing.JPanel configPanel;
    private javax.swing.JLabel description;
    private javax.swing.JTabbedPane modeTabs;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel prefixDescription;
    private javax.swing.JTextField prefixField;
    private javax.swing.JLabel prefixLabel;
    private javax.swing.JPanel prefixPanel;
    private javax.swing.JLabel searchDescription;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JLabel titleDescription;
    private javax.swing.JPanel titlePanel;
    private javax.swing.JScrollPane titleScrollPane;
    private javax.swing.JTextArea titleTextArea;
    private javax.swing.JTextField urlField;
    private javax.swing.JLabel urlLabel;
    // End of variables declaration//GEN-END:variables
}
