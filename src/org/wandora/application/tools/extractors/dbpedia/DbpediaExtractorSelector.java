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
 */



package org.wandora.application.tools.extractors.dbpedia;


import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.gui.simple.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.net.*;

/**
 *
 * @author akivela
 */
public class DbpediaExtractorSelector extends JDialog {

    public static String webServiceBase = "http://www.dbpedia.org/";
    public static String sparqlServiceBase = "http://dbpedia.org/sparql/";

    private Wandora admin = null;
    private Context context = null;
    private boolean accepted = false;


    /** Creates new form OpenCycExtractorSelector */
    public DbpediaExtractorSelector(Wandora admin) {
        super(admin, true);
        initComponents();
        setTitle("DBpedia extractors");
        setSize(450,300);
        admin.centerWindow(this);
        this.admin = admin;
        accepted = false;
    }




    public void setWandora(Wandora wandora) {
        this.admin = wandora;
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

        Component component = dbpediaTabbedPane.getSelectedComponent();
        WandoraTool wt = null;


        // ***** TERMS *****
        if(termPanel.equals(component)) {
            String termsAll = termsTextArea.getText();
            String[] terms = urlEncode(space2Underline(newlineSplitter(termsAll)));

            // Example URL: http://dbpedia.org/data/Berlin.rdf
            String[] termUrls = completeString(webServiceBase+"data/__1__.rdf", terms);

            DbpediaRDFExtractor ex = new DbpediaRDFExtractor();
            ex.setForceUrls( termUrls );
            wt = ex;
        }
        if(sparqlPanel.equals(component)) {
            String query = sparqlTextArea.getText();
            if(query != null && query.trim().length() > 0) {
                query = query.trim();
                query = urlEncode(query);
                String url = sparqlServiceBase+"?query="+query+"&format=application%2Frdf%2Bxml";
                //System.out.println("url == '"+url+"'");
                //url = "http://dbpedia.org/sparql/?query=PREFIX+dbo%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3E%0D%0A%0D%0ASELECT+%3Fname+%3Fbirth+%3Fdescription+%3Fperson+WHERE+{%0D%0A+++++%3Fperson+dbo%3Abirthplace+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FBerlin%3E+.%0D%0A+++++%3Fperson+skos%3Asubject+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FCategory%3AGerman_musicians%3E+.%0D%0A+++++%3Fperson+dbo%3Abirthdate+%3Fbirth+.%0D%0A+++++%3Fperson+foaf%3Aname+%3Fname+.%0D%0A+++++%3Fperson+rdfs%3Acomment+%3Fdescription+.%0D%0A+++++FILTER+%28LANG%28%3Fdescription%29+%3D+%27en%27%29+.%0D%0A}%0D%0AORDER+BY+%3Fname&format=application%2Frdf%2Bxml";
                DbpediaRDFExtractor ex = new DbpediaRDFExtractor();
                ex.setForceUrls( new String[] { url } );
                wt = ex;
            }
            else {
                parentTool.log("Given SPARQL query is zero length.");
            }
        }
        
        return wt;
    }






    public String[] space2Underline(String[] strs) {
        ArrayList<String> strs2 = new ArrayList<String>();
        if(strs != null && strs.length > 0) {
            for(int i=0; i<strs.length; i++) {
                if(strs[i] != null) {
                   strs2.add(strs[i].replace(" ", "_"));
                }
            }
        }
        return strs2.toArray( new String[] {} );
    }





    public String[] newlineSplitter(String str) {
        if(str.indexOf('\n') != -1) {
            String[] strs = str.split("\n");
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



    public String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        }
        catch(Exception e) {
            return url;
        }
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
                        str = t.getBaseName();
                        if(str != null) {
                            str = str.trim();
                        }
                        else {
                            Collection<Locator> sis = t.getSubjectIdentifiers();
                            for(Locator l : sis) {
                                if(l != null) {
                                    String locatorPrefix = "http://dbpedia.org/resource/";
                                    if(l.toExternalForm().startsWith(locatorPrefix)) {
                                        str = l.toExternalForm().substring(locatorPrefix.length());
                                        str = URLDecoder.decode(str, "UTF-8");
                                        str = str.replace("_", " ");
                                        break;
                                    }
                                    locatorPrefix = "http://www.dbpedia.org/resource/";
                                    if(l.toExternalForm().startsWith(locatorPrefix)) {
                                        str = l.toExternalForm().substring(locatorPrefix.length());
                                        str = URLDecoder.decode(str, "UTF-8");
                                        str = str.replace("_", " ");
                                        break;
                                    }
                                    locatorPrefix = "http://www.dbpedia.org/data/";
                                    if(l.toExternalForm().startsWith(locatorPrefix)) {
                                        str = l.toExternalForm().substring(locatorPrefix.length());
                                        str = URLDecoder.decode(str, "UTF-8");
                                        str = str.replace("_", " ");
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if(str != null && str.length() > 0) {
                        sb.append(str);
                        if(contextObjects.hasNext()) {
                            sb.append("\n");
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









    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sparqlPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        sparqlLabel = new javax.swing.JLabel();
        sparqlScrollPane = new javax.swing.JScrollPane();
        sparqlTextArea = new javax.swing.JTextArea();
        dbpediaTabbedPane = new SimpleTabbedPane();
        termPanel = new javax.swing.JPanel();
        termsInnerPanel = new javax.swing.JPanel();
        termsLabel = new SimpleLabel();
        termsScrollPane = new SimpleScrollPane();
        termsTextArea = new SimpleTextArea();
        termsGetContextPanel = new javax.swing.JPanel();
        getContextButton = new SimpleButton();
        buttonPanel = new javax.swing.JPanel();
        buttonFillerPanel = new javax.swing.JPanel();
        extractButton = new SimpleButton();
        cancelButton = new SimpleButton();

        sparqlPanel.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        sparqlLabel.setText("<html>Access DBpedia's SPARQL endpoint with given query. Note: Wandora doesn't evaluate or check given query.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel1.add(sparqlLabel, gridBagConstraints);

        sparqlTextArea.setColumns(20);
        sparqlTextArea.setRows(5);
        sparqlScrollPane.setViewportView(sparqlTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(sparqlScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sparqlPanel.add(jPanel1, gridBagConstraints);

        getContentPane().setLayout(new java.awt.GridBagLayout());

        termPanel.setLayout(new java.awt.GridBagLayout());

        termsInnerPanel.setLayout(new java.awt.GridBagLayout());

        termsLabel.setText("<html>Extract terms from DBpedia. Use newline character to separate different terms. You can also get context terms to extractor.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        termsInnerPanel.add(termsLabel, gridBagConstraints);

        termsTextArea.setColumns(20);
        termsTextArea.setRows(5);
        termsScrollPane.setViewportView(termsTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        termsInnerPanel.add(termsScrollPane, gridBagConstraints);

        termsGetContextPanel.setLayout(new java.awt.GridBagLayout());

        getContextButton.setText("Get context");
        getContextButton.setMargin(new java.awt.Insets(1, 4, 1, 4));
        getContextButton.setMaximumSize(new java.awt.Dimension(85, 21));
        getContextButton.setMinimumSize(new java.awt.Dimension(85, 21));
        getContextButton.setPreferredSize(new java.awt.Dimension(85, 21));
        getContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getContextButtonMouseReleased(evt);
            }
        });
        termsGetContextPanel.add(getContextButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        termsInnerPanel.add(termsGetContextPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        termPanel.add(termsInnerPanel, gridBagConstraints);

        dbpediaTabbedPane.addTab("Terms", termPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(dbpediaTabbedPane, gridBagConstraints);

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

        extractButton.setText("Extract");
        extractButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        extractButton.setMaximumSize(new java.awt.Dimension(75, 23));
        extractButton.setMinimumSize(new java.awt.Dimension(75, 23));
        extractButton.setPreferredSize(new java.awt.Dimension(75, 23));
        extractButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                extractButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 3);
        buttonPanel.add(extractButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        cancelButton.setMaximumSize(new java.awt.Dimension(75, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(75, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 23));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cancelButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void extractButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extractButtonMouseReleased
        this.accepted = true;
        setVisible(false);
    }//GEN-LAST:event_extractButtonMouseReleased

    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        this.accepted = false;
        setVisible(false);
    }//GEN-LAST:event_cancelButtonMouseReleased

    private void getContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getContextButtonMouseReleased
        this.termsTextArea.setText(getContextAsString());
    }//GEN-LAST:event_getContextButtonMouseReleased



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTabbedPane dbpediaTabbedPane;
    private javax.swing.JButton extractButton;
    private javax.swing.JButton getContextButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel sparqlLabel;
    private javax.swing.JPanel sparqlPanel;
    private javax.swing.JScrollPane sparqlScrollPane;
    private javax.swing.JTextArea sparqlTextArea;
    private javax.swing.JPanel termPanel;
    private javax.swing.JPanel termsGetContextPanel;
    private javax.swing.JPanel termsInnerPanel;
    private javax.swing.JLabel termsLabel;
    private javax.swing.JScrollPane termsScrollPane;
    private javax.swing.JTextArea termsTextArea;
    // End of variables declaration//GEN-END:variables

}
