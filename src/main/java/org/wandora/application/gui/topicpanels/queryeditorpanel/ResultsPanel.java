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
 */
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;

import javax.swing.JComponent;

import org.wandora.application.Wandora;
import org.wandora.application.gui.GetTopicButton;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.search.QueryPanel;
import org.wandora.application.gui.table.MixedTopicTable;
import org.wandora.query2.Directive;
import org.wandora.query2.QueryException;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */


public class ResultsPanel extends javax.swing.JPanel {


	private static final long serialVersionUID = 1L;
	
	
	protected boolean autoUpdate=true;
    
    /**
     * Creates new form ResultsPanel
     */
    public ResultsPanel() {
        initComponents();
        
        Object[] buttonStruct = {
            "Run",
            UIBox.getIcon(0xF04B), // See gui/fonts/FontAwesome.ttf for alternative icons.
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    runButtonActionPerformed(evt);
                }
            }
        };
        JComponent buttonContainer = UIBox.makeButtonContainer(buttonStruct, Wandora.getWandora());
        buttonPanel.add(buttonContainer);
        
    }
    
    public QueryEditorDockPanel findDockPanel(){
        Container c=this;
        while(c!=null && !(c instanceof QueryEditorDockPanel)){
            c=c.getParent();
        }
        
        if(c==null) return null;
        return (QueryEditorDockPanel)c;
    }
    
    
    public void clearResults(){
        resultsPanel.removeAll();
        resultsScroll.setColumnHeaderView(null);
    }
    
    public void executeQuery(){
        clearResults();
        
        QueryEditorDockPanel p=findDockPanel();
        if(p==null) return;
        
        QueryEditorComponent editor=p.getQueryEditor();
        if(editor==null) return;
        
        Directive directive=editor.buildDirective();
        if(directive==null) return;
        
        ArrayList<Topic> context=new ArrayList<Topic>();
        
        Topic t=((GetTopicButton)contextTopicButton).getTopic();
        if(t!=null) context.add(t);
        
        try{
            Wandora wandora=Wandora.getWandora();
            MixedTopicTable table=QueryPanel.getTopicsByQuery(wandora, wandora.getTopicMap(), directive, context.iterator());
            if(table==null) return;
            
            resultsPanel.add(table, BorderLayout.CENTER);
            resultsScroll.setColumnHeaderView(table.getTableHeader());
            
        }
        catch(QueryException | TopicMapException e){
            Wandora.getWandora().handleError(e);
        }
        
    }
    
    public void setContextTopic(Topic topic){
        try{
            ((GetTopicButton)contextTopicButton).setTopic(topic);
            if(autoUpdate){
                if(topic==null) clearResults();
                else executeQuery();
            }
        }
        catch(TopicMapException tme){
            Wandora.getWandora().handleError(tme);
        }
    }

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        executeQuery();
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

        toolBar = new javax.swing.JToolBar();
        buttonPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();
        innerFillerPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        try{
            contextTopicButton = new GetTopicButton();
        }catch(TopicMapException tme){Wandora.getWandora().handleError(tme);}
        resultsScroll = new javax.swing.JScrollPane();
        resultsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        toolBar.add(buttonPanel);

        fillerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        fillerPanel.add(innerFillerPanel, gridBagConstraints);

        toolBar.add(fillerPanel);

        add(toolBar, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Context: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(jLabel1, gridBagConstraints);

        contextTopicButton.setText("Topic");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(contextTopicButton, gridBagConstraints);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        resultsPanel.setLayout(new java.awt.BorderLayout());
        resultsScroll.setViewportView(resultsPanel);

        jPanel1.add(resultsScroll, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton contextTopicButton;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JPanel innerFillerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JScrollPane resultsScroll;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
}
