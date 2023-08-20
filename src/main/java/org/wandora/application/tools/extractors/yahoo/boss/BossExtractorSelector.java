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

package org.wandora.application.tools.extractors.yahoo.boss;



import java.awt.Component;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleComboBox;
import org.wandora.application.gui.simple.SimpleField;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleTabbedPane;



/**
 *
 * @author akivela
 */
public class BossExtractorSelector extends javax.swing.JDialog {

	private static final long serialVersionUID = 1L;

	private Wandora wandora = null;
    private boolean accepted = false;
    private Context context = null;


    /** Creates new form BingExtractorSelector */
    public BossExtractorSelector(Wandora wandora) {
        super(wandora, true);
        initComponents();
        searchTypeComboBox.setEditable(false);
        setSize(450,250);
        setTitle("Yahoo! BOSS extractor");
        wandora.centerWindow(this);
        this.wandora = wandora;
        accepted = false;
    }


    public void setWandora(Wandora wandora) {
        this.wandora = wandora;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean wasAccepted() {
        return accepted;
    }
    public void setAccepted(boolean b) {
        accepted = b;
    }



    public WandoraTool getWandoraTool(WandoraTool parentTool) {
        Component component = bossTabbedPane.getSelectedComponent();
        WandoraTool wt = null;

        // ***** BOSS SEARCH *****
        if(searchPanel.equals(component)) {
            String query = searchTextField.getText();
            String offsetStr = offsetTextField.getText();
            String numberStr = numberTextField.getText();
            int offset = 0;
            int number = 50;
            if(query == null || query.trim().length() == 0) {
                parentTool.log("No query given. Aborting.");
                return null;
            }
            try {
                offset = Integer.parseInt(offsetStr);
            }
            catch(Exception e) {
                parentTool.log("Illegal offset number '"+offsetStr+"'. Using default offset '"+offset+"'.");
            }
            try {
                number = Integer.parseInt(numberStr);
            }
            catch(Exception e) {
                parentTool.log("Illegal counter number '"+numberStr+"'. Using default count '"+number+"'.");
            }

            String sourceType = ""+searchTypeComboBox.getSelectedItem();
            if(sourceType.length() == 0) {
                parentTool.log("No search source (web, images, news) is selected. Can't perform Yahoo! BOSS search. Aborting.");
            }
            else {
                BossSearchResultExtractor bse = new BossSearchResultExtractor();
                bse.setSearchString(query);

                String apikey = bse.solveAppId();
                String bossURL = null;
                bossURL = AbstractBossExtractor.WEB_SERVICE_URL+"/"+sourceType+"/v1/"+urlEncode(query);
                bossURL += "?appid="+urlEncode(apikey)+"&format=xml&style=raw&abstract=long";
                bossURL += "&count="+number+"&start="+offset;

                System.out.println("BOSS URL: "+bossURL);
                bse.setForceUrls(new String[] { bossURL } );
                wt = bse;
            }
        }
        return wt;
    }





    public String[] commaSplitter(String str) {
        ArrayList<String> strList=new ArrayList<String>();
        int startPos=0;
        for(int i=0;i<str.length()+1;i++){
            if(i<str.length()-1 && str.charAt(i)==',' && str.charAt(i+1)==','){
                i++;
                continue;
            }
            if(i==str.length() || str.charAt(i)==','){
                String s=str.substring(startPos,i).trim().replace(",,", ",");
                if(s.length()>0) strList.add(s);
                startPos=i+1;
            }
        }
        return strList.toArray(new String[strList.size()]);
/*
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
        */
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



    public String urlEncode(String url) {
        if(url != null) {
            try {
                url = URLEncoder.encode(url, "UTF-8");
                url = url.replace("+", "%20");
                url = url.replace(" ", "%20");
            }
            catch(Exception e) { }
        }
        return url;
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

        tabbedPanel = new javax.swing.JPanel();
        bossTabbedPane = new SimpleTabbedPane();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new SimpleLabel();
        searchOptsPanel = new javax.swing.JPanel();
        searchTextField = new SimpleField();
        searchTypeComboBox = new SimpleComboBox();
        buttonPanel = new javax.swing.JPanel();
        offsetLabel = new SimpleLabel();
        offsetTextField = new SimpleField();
        numberLabel = new SimpleLabel();
        numberTextField = new SimpleField();
        fillerPanel = new javax.swing.JPanel();
        ExtractButton = new SimpleButton();
        cancelButton = new SimpleButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        tabbedPanel.setLayout(new java.awt.GridBagLayout());

        searchPanel.setLayout(new java.awt.GridBagLayout());

        searchLabel.setText("<html>Perform Yahoo! BOSS search and convert search result to a topic map. Write search query to the field below.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        searchPanel.add(searchLabel, gridBagConstraints);

        searchOptsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 5, 3);
        searchOptsPanel.add(searchTextField, gridBagConstraints);

        searchTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "web", "images", "news" }));
        searchTypeComboBox.setMinimumSize(new java.awt.Dimension(80, 23));
        searchTypeComboBox.setPreferredSize(new java.awt.Dimension(80, 23));
        searchOptsPanel.add(searchTypeComboBox, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        searchPanel.add(searchOptsPanel, gridBagConstraints);

        bossTabbedPane.addTab("Search", searchPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        tabbedPanel.add(bossTabbedPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(tabbedPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        offsetLabel.setText("offset");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        buttonPanel.add(offsetLabel, gridBagConstraints);

        offsetTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        offsetTextField.setText("0");
        offsetTextField.setMinimumSize(new java.awt.Dimension(40, 23));
        offsetTextField.setPreferredSize(new java.awt.Dimension(40, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        buttonPanel.add(offsetTextField, gridBagConstraints);

        numberLabel.setText("get");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        buttonPanel.add(numberLabel, gridBagConstraints);

        numberTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numberTextField.setText("20");
        numberTextField.setMinimumSize(new java.awt.Dimension(40, 23));
        numberTextField.setPreferredSize(new java.awt.Dimension(40, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        buttonPanel.add(numberTextField, gridBagConstraints);

        fillerPanel.setPreferredSize(new java.awt.Dimension(100, 10));

        javax.swing.GroupLayout fillerPanelLayout = new javax.swing.GroupLayout(fillerPanel);
        fillerPanel.setLayout(fillerPanelLayout);
        fillerPanelLayout.setHorizontalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 130, Short.MAX_VALUE)
        );
        fillerPanelLayout.setVerticalGroup(
            fillerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(fillerPanel, gridBagConstraints);

        ExtractButton.setText("Extract");
        ExtractButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        ExtractButton.setMinimumSize(new java.awt.Dimension(75, 21));
        ExtractButton.setPreferredSize(new java.awt.Dimension(75, 21));
        ExtractButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExtractButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(ExtractButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        cancelButton.setMinimumSize(new java.awt.Dimension(75, 21));
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 21));
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
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setAccepted(false);
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void ExtractButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExtractButtonActionPerformed
        this.setAccepted(true);
        this.setVisible(false);
    }//GEN-LAST:event_ExtractButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ExtractButton;
    private javax.swing.JTabbedPane bossTabbedPane;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JLabel numberLabel;
    private javax.swing.JTextField numberTextField;
    private javax.swing.JLabel offsetLabel;
    private javax.swing.JTextField offsetTextField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchOptsPanel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JComboBox searchTypeComboBox;
    private javax.swing.JPanel tabbedPanel;
    // End of variables declaration//GEN-END:variables

}
