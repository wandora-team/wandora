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
 * FreeOccurrencePrompt.java
 *
 * Created on 15.6.2006, 16:38
 */

package org.wandora.application.gui;



import static org.wandora.utils.Tuples.t2;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.wandora.application.Wandora;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.SchemaBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.Tuples.T2;

/**
 *
 * @author  olli
 */
public class FreeOccurrencePrompt extends javax.swing.JDialog {
    
	
	private static final long serialVersionUID = 1L;
	
	
    private Topic topic;
    private Wandora wandora;
    private boolean cancelled;
    private ResourceEditor editor;
    private GetTopicButton typeButton;

    private static Topic lastOccurrenceType = null;
    private Topic originalType = null;
    
    
    /** Creates new form FreeOccurrencePrompt */
    public FreeOccurrencePrompt(final Wandora wandora, final Topic topic, Topic occurrenceType) throws TopicMapException {
        super(wandora, true);
        this.wandora = wandora;
        this.topic = topic;
        this.originalType = occurrenceType;
        
        typeButton=new GetTopicButton(null,wandora,this,false,new GetTopicButton.ButtonHandler(){
            private Vector<Topic> players=null;
            private Vector<Topic> suggested=null;
            @Override
            public T2<Topic,Boolean> pressed(GetTopicButton button) throws TopicMapException {
                suggested=suggestOccurrenceType(topic);
                if(suggested==null || suggested.isEmpty()) {
                    return button.defaultPressHandler();
                }
                else {
                    TabbedTopicSelector finder=wandora.getTopicFinder();
                    finder.insertTab(new TopicListSelector(suggested),0);
                    Topic s=wandora.showTopicFinder(wandora,finder);
                    if(s==null) return t2(null,true);
                    else return t2(s,false);                    
                }
            }
        });
        typeButton.addPopupList(new GetTopicButton.PopupListHandler() {
            @Override
            public Collection<Topic> getListTopics() throws TopicMapException {
                return suggestOccurrenceType(topic);
            }
        });
        initComponents();
        this.cancelled=true;

        if(lastOccurrenceType == null) {
            useLastButton.setEnabled(false);
        }
        
        if(occurrenceType != null) {
            typeButton.setTopic(originalType);
        }
        
        makeDataPanel();
        this.setSize(1200, 400);
        wandora.centerWindow(this);
    }
    
    
    
    /** Creates new form FreeOccurrencePrompt */
    public FreeOccurrencePrompt(final Wandora wandora, final Topic topic) throws TopicMapException {
        this(wandora, topic, null);
    }
    
    
    

    
    
    public Vector<Topic> suggestOccurrenceType(Topic topic){
        Vector<Topic> ret=new Vector<Topic>();
        try {
            Collection<Topic> types=SchemaBox.getOccurrenceTypesFor(topic);
            for(Topic t : types) {
                Hashtable<Topic,String> data=topic.getData(t);
                if(data==null || data.isEmpty()) ret.add(t);
            }
        }
        catch(TopicMapException tme) {
            wandora.handleError(tme);
        }
        return ret;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        dataPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        useDefaultButton = new org.wandora.application.gui.simple.SimpleButton();
        useLastButton = new org.wandora.application.gui.simple.SimpleButton();
        jPanel1 = new javax.swing.JPanel();
        okButton = new org.wandora.application.gui.simple.SimpleButton();
        cancelButton = new org.wandora.application.gui.simple.SimpleButton();
        _typeButton = typeButton;
        jLabel1 = new org.wandora.application.gui.simple.SimpleLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Occurrence editor");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        dataPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        getContentPane().add(dataPanel, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        useDefaultButton.setText("Use default");
        useDefaultButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        useDefaultButton.setMaximumSize(new java.awt.Dimension(90, 23));
        useDefaultButton.setMinimumSize(new java.awt.Dimension(90, 23));
        useDefaultButton.setPreferredSize(new java.awt.Dimension(90, 23));
        useDefaultButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                useDefaultButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(useDefaultButton, gridBagConstraints);

        useLastButton.setText("Use last");
        useLastButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        useLastButton.setMaximumSize(new java.awt.Dimension(90, 23));
        useLastButton.setMinimumSize(new java.awt.Dimension(90, 23));
        useLastButton.setPreferredSize(new java.awt.Dimension(90, 23));
        useLastButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                useLastButtonMouseReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        buttonPanel.add(useLastButton, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        buttonPanel.add(jPanel1, gridBagConstraints);

        okButton.setText("OK");
        okButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        okButton.setMaximumSize(new java.awt.Dimension(70, 23));
        okButton.setMinimumSize(new java.awt.Dimension(70, 23));
        okButton.setPreferredSize(new java.awt.Dimension(70, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 3);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        cancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        cancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        cancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(buttonPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        getContentPane().add(_typeButton, gridBagConstraints);

        jLabel1.setText("Occurrence type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        getContentPane().add(jLabel1, gridBagConstraints);

        setSize(new java.awt.Dimension(786, 354));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelled=true;
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        cancelled=false;
        boolean close=true;
        try {
            close = makeOccurrence();
        }
        catch(TopicMapException tme){
            wandora.handleError(tme);
        }
        if(close) {
            lastOccurrenceType = typeButton.getTopic();
            this.setVisible(false);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void useDefaultButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_useDefaultButtonMouseReleased
        try {
            typeButton.setTopic(SchemaBox.DEFAULT_OCCURRENCE_SI);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_useDefaultButtonMouseReleased

    private void useLastButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_useLastButtonMouseReleased
        try {
            if(lastOccurrenceType != null && !lastOccurrenceType.isRemoved()) {
                typeButton.setTopic(lastOccurrenceType);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_useLastButtonMouseReleased
    
    
    
    private boolean makeOccurrence() throws TopicMapException {
        Topic type=typeButton.getTopic();
        if(type == null) {
            return false;
        }
        if(editor != null) {
            if(!type.equals(originalType)) {
                Hashtable<Topic,String> oldData = topic.getData(type);
                if(oldData != null && oldData.size() > 0) {
                    int c = WandoraOptionPane.showConfirmDialog(wandora,
                            "Topic already contains occurrences with type "+TopicToString.toString(type)+".<br>"+
                            "Do you want to overwrite old occurrences?");
                    if(c != WandoraOptionPane.YES_OPTION) {
                        return false;
                    }
                }
                if(originalType != null) {
                    topic.removeData(originalType);
                }
            }
            ((OccurrencePanel)editor).setOccurrenceType(type);
            editor.applyChanges(topic, wandora); 
            return true;
        }
        return false;
    }
    
    
    public boolean wasCancelled(){
        return cancelled;
    }

    
    private void makeDataPanel() throws TopicMapException {
        editor=new OccurrencePanel();
        editor.initializeOccurrence(topic,originalType,wandora);
        dataPanel.removeAll();
        dataPanel.add(editor,java.awt.BorderLayout.CENTER);
        dataPanel.validate();
        this.repaint();
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _typeButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JButton useDefaultButton;
    private javax.swing.JButton useLastButton;
    // End of variables declaration//GEN-END:variables
    
}
