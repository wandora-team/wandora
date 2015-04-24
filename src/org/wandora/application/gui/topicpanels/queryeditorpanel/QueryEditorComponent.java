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
 */
package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import org.wandora.application.Wandora;
import org.wandora.application.gui.texteditor.TextEditor;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.topicpanels.queryeditorpanel.DirectiveEditor.DirectiveParameters;
import org.wandora.application.gui.topicpanels.queryeditorpanel.QueryLibraryPanel.StoredQuery;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveUIHints;
import org.wandora.query2.DirectiveUIHints.Addon;
import org.wandora.query2.DirectiveUIHints.Constructor;

/**
 *
 * @author olli
 */


public class QueryEditorComponent extends javax.swing.JPanel {

    protected final ArrayList<Connector> connectors=new ArrayList<Connector>();
    
    protected DirectivePanel selectedPanel;
    
    protected FinalResultPanel finalResultPanel;
    
    protected ConnectorAnchor finalResultAnchor;
    
    /**
     * Creates new form QueryEditorComponent
     */
    public QueryEditorComponent() {
        initComponents();
        
        finalResultPanel=new FinalResultPanel();
        addDirectivePanel(finalResultPanel);
        finalResultPanel.setSize(finalResultPanel.getPreferredSize());
        
        finalResultAnchor=finalResultPanel.getToConnectorAnchor();
        
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
/*
        DnDTools.addDropTargetHandler(finalResultLabel, DnDTools.directivePanelDataFlavor, 
                new DnDTools.DropTargetCallback<DirectivePanel>(){
                    @Override
                    public boolean callback(JComponent component, DirectivePanel panel, TransferHandler.TransferSupport support) {
                        finalResultAnchor.setFrom(panel.getFromConnectorAnchor());
                        return true;                        
                    }
                });
*/        
        
        
        Object[] buttonStruct = {
            "New",
            UIBox.getIcon(0xF016), // See resources/gui/fonts/FontAwesome.ttf for alternative icons.
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    newButtonActionPerformed(evt);
                }
            },
            "Build script",
            UIBox.getIcon(0xF085), // See resources/gui/fonts/FontAwesome.ttf for alternative icons.
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    buildButtonActionPerformed(evt);
                }
            },
            "Run",
            UIBox.getIcon(0xF04B), 
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    runButtonActionPerformed(evt);
                }
            },
/*            "Delete",
            UIBox.getIcon(0xF014),
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    deleteButtonActionPerformed(evt);
                }
            }*/
        };
        JComponent buttonContainer = UIBox.makeButtonContainer(buttonStruct, Wandora.getWandora());
        buttonPanel.add(buttonContainer);
        
    }
    
    public static DirectivePanel resolveDirectivePanel(Component component){
        while(component!=null && !(component instanceof DirectivePanel)){
            component=component.getParent();
        }
        return (DirectivePanel)component; // this could be null but the cast will work
    }    
    
    public QueryEditorDockPanel findDockPanel(){
        Container c=this;
        while(c!=null && !(c instanceof QueryEditorDockPanel)){
            c=c.getParent();
        }
        
        if(c==null) return null;
        return (QueryEditorDockPanel)c;
    }
    public QueryEditorInspectorPanel findInspector(){
        QueryEditorDockPanel p=findDockPanel();
        if(p==null) return null;
        else return p.getInspector();
    }
    
    public void applyInspectorChanges(){
        if(this.selectedPanel!=null) {
            QueryEditorInspectorPanel inspector=findInspector();
            if(inspector!=null){
                inspector.saveChanges();
            }            
        }        
    }
    
    public DirectivePanel getRootPanel(){
        ConnectorAnchor from=finalResultAnchor.getFrom();
        if(from==null) return null;
        JComponent component=from.getComponent();
        if(component==null) return null;
        DirectivePanel p=resolveDirectivePanel(component);
        if(p==null) return null;
        return p;
    }
    
    public String buildScript(){
        applyInspectorChanges();
        DirectivePanel p=getRootPanel();
        if(p==null) return null;
        else return "importPackage(org.wandora.query2);\n"+p.buildScript();
    }
    
    public Directive buildDirective(){
        applyInspectorChanges();
        DirectivePanel p=getRootPanel();
        if(p==null) return null;
        else return p.buildDirective();
    }
    
    
    public StoredQuery getStoredQuery(){
        applyInspectorChanges();
        
        DirectivePanel rootPanel=getRootPanel();
        if(rootPanel==null) return null;
                
        StoredQuery ret=new StoredQuery();
        ret.name=null;
        ret.rootDirective=rootPanel.getDirectiveId();
        
        ArrayList<DirectiveParameters> directiveParams=new ArrayList<>();
        
        for(int i=0;i<queryGraphPanel.getComponentCount();i++){
            Component c=queryGraphPanel.getComponent(i);
            if(c instanceof DirectivePanel){
                DirectivePanel panel=(DirectivePanel)c;
                directiveParams.add(panel.getDirectiveParameters());
            }
        }
        
        ret.directiveParameters=directiveParams;
        
        return ret;
    }
    
    public void clearQuery(){
        ArrayList<Component> remove=new ArrayList<>();
        for(int i=0;i<queryGraphPanel.getComponentCount();i++){
            Component c=queryGraphPanel.getComponent(i);
            synchronized(connectors){
                connectors.clear();
            }
            selectPanel(null);
            finalResultAnchor.setFrom(null);
            if(c instanceof DirectivePanel && c!=finalResultPanel){
                remove.add(c);
            }
        }
        
        for(Component c : remove) queryGraphPanel.remove(c);
        Rectangle bounds=finalResultPanel.getBounds();
        finalResultPanel.setBounds(new Rectangle(0,0,bounds.width,bounds.height));
    }
    
    public void openStoredQuery(StoredQuery query){
        
        HashMap<String,DirectivePanel> directiveMap=new HashMap<>();
        
        for(DirectiveParameters params : query.directiveParameters){
            try{
                Class cls=Class.forName(params.cls);
                DirectivePanel panel=null;
                if(!Directive.class.isAssignableFrom(cls)) throw new ClassCastException("Stored query class is not a Directive");
                if(cls.equals(FinalResultDirective.class)){
                    panel=finalResultPanel;
                }
                else {
                    DirectiveUIHints hints=DirectiveUIHints.getDirectiveUIHints(cls);
                    panel=addDirective(hints);
                }
                                
                directiveMap.put(params.id, panel);
                
            }catch(ClassNotFoundException | ClassCastException e){
                Wandora.getWandora().handleError(e);
            }
        }
        
        for(DirectiveParameters params : query.directiveParameters){
            params.resolveDirectiveValues(directiveMap);
        }
        
        for(DirectiveParameters params : query.directiveParameters){
            DirectivePanel panel=directiveMap.get(params.id);
            panel.setDirectiveParams(params);
        }        
        
        updatePanelSize();
        
    }
    
    public void selectPanel(DirectivePanel panel){
        DirectivePanel old=this.selectedPanel;
        this.selectedPanel=panel;
        if(old!=null) old.setSelected(false);
        if(panel!=null) panel.setSelected(true);
        this.repaint();
        
        QueryEditorInspectorPanel inspector=findInspector();
        if(inspector!=null){
            inspector.setSelection(panel);
        }
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
        //panel.setBounds(10, 10, 300, 200);
        panel.revalidate();
        return panel;
    }
    
    public JPanel getGraphPanel(){
        return queryGraphPanel;
    }
    
    private void changePanelSize(int width,int height, int offsx, int offsy){
        Dimension d=new Dimension(width,height);
        queryGraphPanel.setMaximumSize(d);
        queryGraphPanel.setPreferredSize(d);
        queryGraphPanel.setMinimumSize(d);
        queryGraphPanel.setSize(d);

        if(offsx!=0 || offsy!=0){
            for(int i=0;i<queryGraphPanel.getComponentCount();i++){
                Component c=queryGraphPanel.getComponent(i);
                Rectangle b=c.getBounds();
                
                c.setBounds(b.x+offsx, b.y+offsy, b.width, b.height);
            }
            Point p=queryScrollPane.getViewport().getViewPosition();
            queryScrollPane.getViewport().setViewPosition(new Point(p.x+offsx,p.y+offsy));
        }
    }
    
    private void updatePanelSize(){
        int w=0;
        int h=0;
        for(int i=0;i<queryGraphPanel.getComponentCount();i++){
            Component c=queryGraphPanel.getComponent(i);
            if(c instanceof DirectivePanel){
                Rectangle rect=c.getBounds();
                w=Math.max(w,rect.x+rect.width);
                h=Math.max(h,rect.y+rect.height);
            }
        }
        
        if(w!=this.getWidth() || h!=this.getHeight()){
            changePanelSize(w,h,0,0);
        }        
        
    }
    
    public void panelMoved(DirectivePanel panel){
        Rectangle rect=panel.getBounds();
        if(rect.x<0){
            changePanelSize(queryGraphPanel.getWidth()-rect.x,queryGraphPanel.getHeight(),-rect.x,0);
            rect=panel.getBounds();
        }
        if(rect.y<0){
            changePanelSize(queryGraphPanel.getWidth(),queryGraphPanel.getHeight()-rect.y,0,-rect.y);            
            rect=panel.getBounds();
        }
        
        updatePanelSize();
        
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
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        directiveListPanel = new javax.swing.JPanel();
        queryScrollPane = new javax.swing.JScrollPane();
        queryGraphPanel = new GraphPanel();
        toolBar = new javax.swing.JToolBar();
        buttonPanel = new javax.swing.JPanel();
        fillerPanel = new javax.swing.JPanel();
        innerFillerPanel = new javax.swing.JPanel();

        directiveListPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(directiveListPanel);

        setLayout(new java.awt.BorderLayout());

        queryGraphPanel.setBackground(new java.awt.Color(255, 255, 255));
        queryGraphPanel.setLayout(null);
        queryScrollPane.setViewportView(queryGraphPanel);

        add(queryScrollPane, java.awt.BorderLayout.CENTER);

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
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel directiveListPanel;
    private javax.swing.JPanel fillerPanel;
    private javax.swing.JPanel innerFillerPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel queryGraphPanel;
    private javax.swing.JScrollPane queryScrollPane;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    
    public void removeDirective(DirectivePanel panel){
        if(panel!=null && panel!=finalResultPanel){
            panel.disconnectConnectors();
            queryGraphPanel.remove(panel);
            if(selectedPanel==panel) selectPanel(null);
            queryGraphPanel.repaint();
        }        
    }
    

    
    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        clearQuery();
    }
    
    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        QueryEditorDockPanel p=findDockPanel();
        if(p==null) return;
        p.bringResultsFront();
        ResultsPanel res=p.getResultsPanel();
        res.executeQuery();
    }
    
    private void buildButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        try{
            String s=buildScript();
            TextEditor editor=new TextEditor(Wandora.getWandora(), true, s);
            editor.setVisible(true);
        }
        catch(Exception e){
            Wandora.getWandora().handleError(e);
        }
    }     
    
    /*
     This is just a placeholder class used for the final result panel.
     It serves no other function than to identify the panel in some cases.
    */
    protected static class FinalResultDirective extends Directive {
        public FinalResultDirective(){}
    }
    
    protected static class FinalResultPanel extends DirectivePanel {
        public FinalResultPanel(){
            super(new DirectiveUIHints(FinalResultDirective.class, new Constructor[]{}, new Addon[]{}, "Final result", null));
            fromDirectiveAnchor.setVisible(false);
        }

        @Override
        public JPanel getEditorPanel() {
            return null;
        }
        
        
    }
    
    
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
