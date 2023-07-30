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
 * HelmetUI.java
 *
 * Created on 25.5.2011, 21:58:53
 */

package org.wandora.application.tools.extractors.helmet;

import java.awt.Component;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JTextPane;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.gui.simple.SimpleButton;
import org.wandora.application.gui.simple.SimpleLabel;
import org.wandora.application.gui.simple.SimpleScrollPane;
import org.wandora.application.gui.simple.SimpleTabbedPane;
import org.wandora.application.gui.simple.SimpleTextPane;
import org.wandora.topicmap.Topic;

/**
 *
 * @author akivela
 */
public class HelmetUI extends javax.swing.JPanel {


	private static final long serialVersionUID = 1L;
	
	public static final String baseURL = "http://data.kirjastot.fi/search";
    public static final String authorURL = baseURL+"/author.json?query=__1__";
    public static final String titleURL = baseURL+"/title.json?query=__1__";
    public static final String isbnURL = baseURL+"/isbn.json?query=__1__";


    private Wandora wandora = null;
    private boolean accepted = false;
    private JDialog dialog = null;
    private Context context = null;


    /** Creates new form HelmetUI */
    public HelmetUI(Wandora w) {
        this.wandora = w;
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
        dialog.setSize(400,300);
        dialog.add(this);
        dialog.setTitle("HelMet data API extractor");
        UIBox.centerWindow(dialog, w);
        dialog.setVisible(true);
    }


    public String[] getQueryURLs(WandoraTool parentTool) {
        Component component = tabbedPane.getSelectedComponent();
        String[] q = null;

        // ***** AUTHOR *****
        if(authorPanel.equals(component)) {
            String query = authorTextPane.getText();
            if(query == null) query = "";
            String[] queries = newlineSplitter(query);
            String[] searchUrls = completeString(authorURL, urlEncode(queries));
            q = searchUrls;
        }

        // ***** TITLE *****
        else if(titlePanel.equals(component)) {
            String query = titleTextPane.getText();
            if(query == null) query = "";
            String[] queries = newlineSplitter(query);
            String[] searchUrls = completeString(titleURL, urlEncode(queries));
            q = searchUrls;
        }

        // ***** ISBN *****
        else if(titlePanel.equals(component)) {
            String query = isbnTextPane.getText();
            if(query == null) query = "";
            String[] queries = newlineSplitter(query);
            String[] searchUrls = completeString(isbnURL, urlEncode(queries));
            q = searchUrls;
        }
        return q;
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


    public String[] urlEncode(String[] strs) {
        if(strs == null) return null;
        String[] cleanStrs = new String[strs.length];
        for(int i=0; i<strs.length; i++) {
            cleanStrs[i] = urlEncode(strs[i]);
        }
        return cleanStrs;
    }



    public String urlEncode(String str) {
        try {
            // return URLEncoder.encode(str, "UTF-8");
            return URLEncoder.encode(new String(str.getBytes("UTF-8"), "ISO-8859-1"), "ISO-8859-1");
        }
        catch(Exception e) {
            return str;
        }
    }


    public void getContext() {
        JTextPane pane = null;
        Component component = tabbedPane.getSelectedComponent();
        if(authorPanel.equals(component)) {
            pane = authorTextPane;
        }
        if(titlePanel.equals(component)) {
            pane = titleTextPane;
        }
        if(isbnPanel.equals(component)) {
            pane = isbnTextPane;
        }

        StringBuilder sb = new StringBuilder("");
        if(context != null) {
            Iterator iterator = context.getContextObjects();
            while(iterator.hasNext()) {
                try {
                    Object o = iterator.next();
                    if(o == null) continue;
                    if(o instanceof Topic) {
                        String bn = ((Topic) o).getBaseName();
                        if(bn.indexOf('(') != -1) {
                            bn = bn.substring(0,bn.indexOf('('));
                            bn = bn.trim();
                        }
                        sb.append(bn);
                        sb.append('\n');
                    }
                    else if(o instanceof String) {
                        sb.append(((String) o));
                        sb.append('\n');
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(pane != null) {
            if(sb.length() > 0) {
                pane.setText(sb.toString());
            }
            else {
                WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "No valid context available. Couldn't get text out of context.", "No valid context available");
            }
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

        tabbedPane = new SimpleTabbedPane();
        authorPanel = new javax.swing.JPanel();
        authorPanelInner = new javax.swing.JPanel();
        authorLabel = new SimpleLabel();
        authorScrollPane = new SimpleScrollPane();
        authorTextPane = new SimpleTextPane();
        titlePanel = new javax.swing.JPanel();
        titlePanelInner = new javax.swing.JPanel();
        titleLabel = new SimpleLabel();
        titleScrollPane = new SimpleScrollPane();
        titleTextPane = new SimpleTextPane();
        isbnPanel = new javax.swing.JPanel();
        isbnPanelInner = new javax.swing.JPanel();
        isbnLabel = new SimpleLabel();
        isbnScrollPane = new SimpleScrollPane();
        isbnTextPane = new SimpleTextPane();
        buttonPanel = new javax.swing.JPanel();
        getContextButton = new SimpleButton();
        buttonFillerPanel = new javax.swing.JPanel();
        okButton = new SimpleButton();
        cancelButton = new SimpleButton();

        setLayout(new java.awt.GridBagLayout());

        authorPanel.setLayout(new java.awt.GridBagLayout());

        authorPanelInner.setLayout(new java.awt.GridBagLayout());

        authorLabel.setText("<html>Search HelMet library data using author names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        authorPanelInner.add(authorLabel, gridBagConstraints);

        authorScrollPane.setViewportView(authorTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        authorPanelInner.add(authorScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        authorPanel.add(authorPanelInner, gridBagConstraints);

        tabbedPane.addTab("Author", authorPanel);

        titlePanel.setLayout(new java.awt.GridBagLayout());

        titlePanelInner.setLayout(new java.awt.GridBagLayout());

        titleLabel.setText("<html>Search HelMet library data using title names.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        titlePanelInner.add(titleLabel, gridBagConstraints);

        titleScrollPane.setViewportView(titleTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        titlePanelInner.add(titleScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        titlePanel.add(titlePanelInner, gridBagConstraints);

        tabbedPane.addTab("Title", titlePanel);

        isbnPanel.setLayout(new java.awt.GridBagLayout());

        isbnPanelInner.setLayout(new java.awt.GridBagLayout());

        isbnLabel.setText("<html>Search HelMet library data using ISBN codes.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        isbnPanelInner.add(isbnLabel, gridBagConstraints);

        isbnScrollPane.setViewportView(isbnTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        isbnPanelInner.add(isbnScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        isbnPanel.add(isbnPanelInner, gridBagConstraints);

        tabbedPane.addTab("ISBN", isbnPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tabbedPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        getContextButton.setText("Get context");
        getContextButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        getContextButton.setMaximumSize(new java.awt.Dimension(90, 23));
        getContextButton.setMinimumSize(new java.awt.Dimension(90, 23));
        getContextButton.setPreferredSize(new java.awt.Dimension(90, 23));
        getContextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                getContextButtonMouseReleased(evt);
            }
        });
        buttonPanel.add(getContextButton, new java.awt.GridBagConstraints());

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
        okButton.setMargin(new java.awt.Insets(2, 5, 2, 5));
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
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
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
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(buttonPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cancelButtonMouseReleased
        this.accepted = false;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_cancelButtonMouseReleased

    private void okButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_okButtonMouseReleased
        this.accepted = true;
        if(dialog != null) dialog.setVisible(false);
    }//GEN-LAST:event_okButtonMouseReleased

    private void getContextButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_getContextButtonMouseReleased
        getContext();
    }//GEN-LAST:event_getContextButtonMouseReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel authorLabel;
    private javax.swing.JPanel authorPanel;
    private javax.swing.JPanel authorPanelInner;
    private javax.swing.JScrollPane authorScrollPane;
    private javax.swing.JTextPane authorTextPane;
    private javax.swing.JPanel buttonFillerPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton getContextButton;
    private javax.swing.JLabel isbnLabel;
    private javax.swing.JPanel isbnPanel;
    private javax.swing.JPanel isbnPanelInner;
    private javax.swing.JScrollPane isbnScrollPane;
    private javax.swing.JTextPane isbnTextPane;
    private javax.swing.JButton okButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel titlePanel;
    private javax.swing.JPanel titlePanelInner;
    private javax.swing.JScrollPane titleScrollPane;
    private javax.swing.JTextPane titleTextPane;
    // End of variables declaration//GEN-END:variables

}
