/*
 * GroupSearchDialog.java
 *
 * Created on 27. toukokuuta 2008, 11:55
 */

package org.wandora.application.tools.extractors.flickr;

import java.awt.Frame;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIConstants;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author  anttirt
 */
public class GroupSearchDialog extends javax.swing.JDialog {

	private static final long serialVersionUID = 1L;
	
	private FlickrState flickrState;
    private DefaultListModel listModel;
    private Frame parentFrame;
    private HashMap<String, Topic> allGroups;
    private ArrayList<Topic> selectedGroups;
    private FlickrExtractor flickrExtractor;
    private boolean cancelled;
    private volatile boolean stopSearch;
    
    public Collection<Topic> selection() {
        return selectedGroups;
    }
    
    /** Creates new form GroupSearchDialog */
    public GroupSearchDialog(java.awt.Frame parent, boolean modal, FlickrState state, FlickrExtractor extractor) {
        super(parent, modal);
        initComponents();
        flickrState = state;
        flickrExtractor = extractor;
        parentFrame = parent;
        listModel = new DefaultListModel();
        groupList.setModel(listModel);
        allGroups = new HashMap<String, Topic>();
        if(parent instanceof Wandora)
        {
            Wandora admin = (Wandora)parent;
            admin.centerWindow(this);
        }
        groupList.setFont(UIConstants.plainFont);
        cancelled = true;
        stopSearch = false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        wandoraLabel1 = new org.wandora.application.gui.simple.SimpleLabel();
        searchText = new org.wandora.application.gui.simple.SimpleField();
        jScrollPane1 = new javax.swing.JScrollPane();
        groupList = new javax.swing.JList();
        btnOk = new org.wandora.application.gui.simple.SimpleButton();
        btnCancel = new org.wandora.application.gui.simple.SimpleButton();
        wandoraLabel2 = new org.wandora.application.gui.simple.SimpleLabel();
        btnSearch = new org.wandora.application.gui.simple.SimpleButton();
        warningLabel = new org.wandora.application.gui.simple.SimpleLabel();
        warningLabel.setFont(UIConstants.smallButtonLabelFont);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Flickr group search");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onWindowClosing(evt);
            }
        });

        wandoraLabel1.setText("<html><body><p>Please enter some text to search for in Flickr groups.</p></body></html>");

        jScrollPane1.setViewportView(groupList);

        btnOk.setText("OK");
        btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        wandoraLabel2.setText("<html><body><p>Select one or more groups from the list.</p></body></html>");

        btnSearch.setText("Search");
        btnSearch.setMargin(new java.awt.Insets(1, 4, 2, 4));
        btnSearch.setPreferredSize(new java.awt.Dimension(70, 20));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        warningLabel.setText("<html><body><p>Warning: Getting all the information for even a single group may take a very long time; it's recommended to only get information for one group at a time.</p></body></html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(searchText, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                            .addComponent(wandoraLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                            .addComponent(btnSearch, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(wandoraLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE)
                            .addComponent(warningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(wandoraLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wandoraLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private GroupRequestThread reqThread;
    
    
    private class GroupRequestThread implements Runnable {
        public void run() {
            try {
                SortedMap<String, String> args = new TreeMap();
                args.put("text", URLEncoder.encode(searchText.getText(), "UTF-8"));
                JSONObject result;

                // if we have read permissions we might get additional stuff if the user is age 18+
                if(flickrState.PermissionLevel.equals(FlickrState.PermNone))
                    result = flickrState.unauthorizedCall("flickr.groups.search", args);
                else
                    result = flickrState.authorizedCall("flickr.groups.search", args, FlickrState.PermRead, parentFrame);

                JSONArray groupArray = FlickrUtils.searchJSONArray(result, "groups.group");
                
                for(int i = 0; i < groupArray.length() && !stopSearch; ++i) {
                    try {
                        final JSONObject group = groupArray.getJSONObject(i);
                        final String name = group.getString("name");
                        final FlickrGroup g = new FlickrGroup(group);
                        final Topic groupTopic = g.makeTopic(flickrExtractor);
                        
                        
                        SwingUtilities.invokeLater(new Runnable() { public void run() {
                            listModel.addElement(name);
                            allGroups.put(name, groupTopic);
                        }});
                        
                    } catch(JSONException e) {}
                }
            }
            catch(FlickrExtractor.RequestFailure e) {

            }
            catch(UnsupportedEncodingException e) {

            }
            catch(JSONException e) {

            }
            catch(TopicMapException e) {

            }
            catch(FlickrExtractor.UserCancellation e) {
                SwingUtilities.invokeLater(new Runnable() { public void run() { flickrExtractor.log("User cancelled"); } });
            }
            finally {
                SwingUtilities.invokeLater(new Runnable() { public void run() { reqThread = null; btnSearch.setText("Search"); btnSearch.setEnabled(true); } });
            }
        }
    }
    
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        if(reqThread != null)
            return;
        
        listModel.clear();
        allGroups.clear();
        
        btnSearch.setText("Searching...");
        btnSearch.setEnabled(false);
        reqThread = new GroupRequestThread();
        new Thread(reqThread).start();
    }//GEN-LAST:event_btnSearchActionPerformed
    
    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        stopSearch = true;
        cancelled = false;
        selectedGroups = new ArrayList<Topic>();
        
        for(Object elem : groupList.getSelectedValues()) {
            selectedGroups.add(allGroups.get(elem));
        }
        setVisible(false);
        
    }//GEN-LAST:event_btnOkActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        cancelled = true;
        stopSearch = true;
        setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed

    private void onWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onWindowClosing
        cancelled = true;
        stopSearch = true;
    }//GEN-LAST:event_onWindowClosing
    
    public boolean wasCancelled() {
        return cancelled;
    }
    /**
     * @param args the command line arguments
     *
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                GroupSearchDialog dialog = new GroupSearchDialog(new javax.swing.JFrame(), true, null);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    */
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.wandora.application.gui.simple.SimpleButton btnCancel;
    private org.wandora.application.gui.simple.SimpleButton btnOk;
    private org.wandora.application.gui.simple.SimpleButton btnSearch;
    private javax.swing.JList groupList;
    private javax.swing.JScrollPane jScrollPane1;
    private org.wandora.application.gui.simple.SimpleField searchText;
    private org.wandora.application.gui.simple.SimpleLabel wandoraLabel1;
    private org.wandora.application.gui.simple.SimpleLabel wandoraLabel2;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
    
}
