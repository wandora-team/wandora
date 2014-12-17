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
 */
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import org.wandora.application.Wandora;
import org.wandora.application.gui.TextEditor;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveManager;
import org.wandora.query2.DirectiveUIHints;

/**
 *
 * @author olli
 */


public class QueryEditorComponent extends javax.swing.JPanel {

    protected final ArrayList<Connector> connectors=new ArrayList<Connector>();
    
    protected DirectivePanel selectedPanel;
    
    protected ConnectorAnchor finalResultAnchor;
    
    /**
     * Creates new form QueryEditorComponent
     */
    public QueryEditorComponent() {
        initComponents();
        populateDirectiveList();
        
        finalResultAnchor=new ComponentConnectorAnchor(finalResultLabel, ConnectorAnchor.Direction.RIGHT, true, false);
        
        DnDTools.addDropTargetHandler(queryGraphPanel, DnDTools.directiveHintsDataFlavor, 
                new DnDTools.DropTargetCallback<DirectiveUIHints>(){
                    @Override
                    public boolean callback(JComponent component, DirectiveUIHints hints, TransferHandler.TransferSupport support) {
                        DirectivePanel panel=addDirective(hints);

                        Point point=support.getDropLocation().getDropPoint();
                        panel.setBounds(point.x,point.y,panel.getWidth(),panel.getHeight());
                        return true;                        
                    }
                });

        DnDTools.addDropTargetHandler(finalResultLabel, DnDTools.directivePanelDataFlavor, 
                new DnDTools.DropTargetCallback<DirectivePanel>(){
                    @Override
                    public boolean callback(JComponent component, DirectivePanel panel, TransferHandler.TransferSupport support) {
                        finalResultAnchor.setFrom(panel.getFromConnectorAnchor());
                        return true;                        
                    }
                });
        
    }
    
    public static DirectivePanel resolveDirectivePanel(Component component){
        while(component!=null && !(component instanceof DirectivePanel)){
            component=component.getParent();
        }
        return (DirectivePanel)component; // this could be null but the cast will work
    }    
    
    
    public String buildScript(){
        ConnectorAnchor from=finalResultAnchor.getFrom();
        if(from==null) return null;
        JComponent component=from.getComponent();
        if(component==null) return null;
        DirectivePanel p=resolveDirectivePanel(component);
        if(p==null) return null;
        else return p.buildScript();
    }
    
    public void selectPanel(DirectivePanel panel){
        DirectivePanel old=this.selectedPanel;
        this.selectedPanel=panel;
        if(old!=null) old.setSelected(false);
        if(panel!=null) panel.setSelected(true);
        this.repaint();
    }
    
    public void addConnector(Connector c){
        synchronized(connectors){
            connectors.add(c);
        }
        queryGraphPanel.repaint();
    }
    
    public void removeConnector(Connector c){
        synchronized(connectors){
            connectors.remove(c);
        }
        queryGraphPanel.repaint();
    }
    
    protected void populateDirectiveList(){
        directiveListPanel.removeAll();
        directiveListPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.anchor=GridBagConstraints.WEST;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.weightx=1.0;
        
        DirectiveManager directiveManager=DirectiveManager.getDirectiveManager();
        List<Class<? extends Directive>> directives=directiveManager.getDirectives();
        List<DirectiveUIHints> hints=new ArrayList<>();
        
        for(Class<? extends Directive> dir : directives){
            DirectiveUIHints h=DirectiveUIHints.getDirectiveUIHints(dir);
            hints.add(h);
        }
        
        Collections.sort(hints,new Comparator<DirectiveUIHints>(){
            @Override
            public int compare(DirectiveUIHints o1, DirectiveUIHints o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        for(DirectiveUIHints h: hints){
            DirectiveListLine line=new DirectiveListLine(h);
            directiveListPanel.add(line, gbc);
            
            gbc.gridy++;
        }
    }
    
    public void addDirectivePanel(DirectivePanel panel){
        this.queryGraphPanel.add(panel);
        this.queryGraphPanel.invalidate();
        this.queryGraphPanel.repaint();
    }
    
    public DirectivePanel addDirective(Class<? extends Directive> dir){
        return addDirective(DirectiveUIHints.getDirectiveUIHints(dir));
    }
    
    public DirectivePanel addDirective(DirectiveUIHints hints){
        DirectivePanel panel=new DirectivePanel(hints);
        addDirectivePanel(panel);
        panel.setBounds(10, 10, 200, 100);
        panel.revalidate();
        return panel;
    }
    
    public JPanel getGraphPanel(){
        return queryGraphPanel;
    }
    
    public void panelMoved(DirectivePanel panel){
        Rectangle rect=panel.getBounds();
        int width=Math.max(rect.x+rect.width,queryGraphPanel.getWidth());
        int height=Math.max(rect.y+rect.height,queryGraphPanel.getHeight());
        if(width!=this.getWidth() || height!=this.getHeight()){
            Dimension d=new Dimension(width,height);
            queryGraphPanel.setMaximumSize(d);
            queryGraphPanel.setPreferredSize(d);
            queryGraphPanel.setMinimumSize(d);
            queryGraphPanel.setSize(d);
        }
        
        queryGraphPanel.invalidate();
        queryGraphPanel.repaint();
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        directiveListPanel = new javax.swing.JPanel();
        queryScrollPane = new javax.swing.JScrollPane();
        queryGraphPanel = new GraphPanel();
        finalResultLabel = new javax.swing.JLabel();
        toolBar = new javax.swing.JToolBar();
        buildButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(600);

        directiveListPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(directiveListPanel);

        jSplitPane1.setRightComponent(jScrollPane1);

        queryGraphPanel.setLayout(null);

        finalResultLabel.setText("FInal result <-");
        queryGraphPanel.add(finalResultLabel);
        finalResultLabel.setBounds(10, 10, 100, 17);

        queryScrollPane.setViewportView(queryGraphPanel);

        jSplitPane1.setLeftComponent(queryScrollPane);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        toolBar.setRollover(true);

        buildButton.setText("Build");
        buildButton.setFocusable(false);
        buildButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buildButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buildButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildButtonActionPerformed(evt);
            }
        });
        toolBar.add(buildButton);

        deleteButton.setText("Delete");
        deleteButton.setFocusable(false);
        deleteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        toolBar.add(deleteButton);

        add(toolBar, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void buildButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildButtonActionPerformed
        try{
            String s=buildScript();
            
            TextEditor editor=new TextEditor(Wandora.getWandora(), true, s);
            editor.setVisible(true);
            
        }catch(Exception e){
            Wandora.getWandora().handleError(e);
        }
    }//GEN-LAST:event_buildButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        if(selectedPanel!=null){
            selectedPanel.disconnectConnectors();
            queryGraphPanel.remove(selectedPanel);
            selectedPanel=null;
            queryGraphPanel.repaint();
        }
    }//GEN-LAST:event_deleteButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buildButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JPanel directiveListPanel;
    private javax.swing.JLabel finalResultLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel queryGraphPanel;
    private javax.swing.JScrollPane queryScrollPane;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    private class GraphPanel extends JPanel {
        public GraphPanel(){
            super();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            synchronized(connectors){
                for(Connector c : connectors){
                    c.repaint(g);
                }
            }
        }
        
    }
}
