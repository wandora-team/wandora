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
 *
 * 
 */

package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import org.wandora.application.Wandora;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveUIHints;
import org.wandora.query2.DirectiveUIHints.Constructor;
import org.wandora.query2.DirectiveUIHints.Parameter;
import org.wandora.query2.Operand;
import org.wandora.query2.TopicOperand;

/**
 *
 * @author olli
 */


public class DirectivePanel extends javax.swing.JPanel {

    protected DirectiveUIHints hints;
    protected DirectivePanel to;
    protected DirectivePanel from;
    protected Connector toConnector;
    protected Connector fromConnector;
    protected ConnectorAnchor connectorAnchor;
    
    /**
     * Creates new form DirectivePanel
     */
    public DirectivePanel() {
        initComponents();
        this.connectorAnchor=new ComponentConnectorAnchor(directiveAnchor,ConnectorAnchor.Direction.LEFT,true,true);
    }
    
    protected boolean dragging=false;
    protected int dragStartX=-1;
    protected int dragStartY=-1;
    

    public DirectivePanel(DirectiveUIHints hints){
        this();
        setDirective(hints);
        
        directiveLabel.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseReleased(MouseEvent e) {
                dragging=false;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragging=true;
                dragStartX=e.getX();
                dragStartY=e.getY();
            }
        });
        directiveLabel.addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseDragged(MouseEvent e) {
                QueryEditorComponent editor=getEditor();
                if(editor!=null) {
                    Rectangle rect=getBounds();
                    setBounds(  Math.max(rect.x+e.getX()-dragStartX,0),
                                Math.max(rect.y+e.getY()-dragStartY,0),
                                rect.width,rect.height);
                    editor.panelMoved(DirectivePanel.this);
                }
            }            
        });
        
        
        DnDTools.setDragSourceHandler(directiveAnchor, "directivePanel", DnDTools.directivePanelDataFlavor, new DnDTools.DragSourceCallback<DirectivePanel>() {
            @Override
            public DirectivePanel callback(JComponent component) {
                return DirectivePanel.this;
            }
        });
        
        DnDTools.addDropTargetHandler(directiveAnchor, DnDTools.directivePanelDataFlavor, new DnDTools.DropTargetCallback<DirectivePanel>(){
            @Override
            public boolean callback(JComponent component, DirectivePanel o, TransferHandler.TransferSupport support) {
                if(o==DirectivePanel.this) return false;
                connectorAnchor.setFrom(o.getConnectorAnchor());
                return true;
            }
        });
    }
    
    public ConnectorAnchor getConnectorAnchor(){
        return connectorAnchor;
    }
/*    
    public static void setLink(QueryEditorComponent editor,DirectivePanel from,DirectivePanel to){
        // Things are nulled seemingly unnecessarily because the recursive calls
        // would otherwise cause infinite recursion. Nulling them acts as a
        // marker that we've done that panel already.
        
        if(from==null || from.to!=to){
            if(from!=null){
                if(from.toConnector!=null){
                    editor.removeConnector(from.toConnector);
                    from.toConnector=null;
                }
                DirectivePanel old=from.to;
                from.to=null;
                if(old!=null) old.setFrom(null);
                from.to=to;
            }
            
            if(to!=null){
                if(to.fromConnector!=null){
                    editor.removeConnector(to.fromConnector);
                    to.fromConnector=null;
                }
                DirectivePanel old=to.from;
                to.from=null;
                if(old!=null) old.setTo(null);
                to.from=from;
                
                if(from!=null){
                    from.toConnector=new Connector((JComponent)from.getParent(), from.directiveAnchor, to.directiveAnchor);
                    to.fromConnector=from.toConnector;
                    editor.addConnector(from.toConnector);
                }
            }
        }        
    }
    
    public void setFrom(DirectivePanel panel){
        setLink(getEditor(),panel,this);
    }
    
    public void setTo(DirectivePanel panel){
        setLink(getEditor(),this,panel);
    }
    */
    protected QueryEditorComponent getEditor(){
        Container parent=getParent();
        while(parent!=null && !(parent instanceof QueryEditorComponent)){
            parent=parent.getParent();
        }
        if(parent!=null) return (QueryEditorComponent)parent;
        else return null;
    }
    
    private class ConstructorComboItem {
        public Constructor c;
        public String label;
        public ConstructorComboItem(Constructor c,String label){
            this.c=c;
            this.label=label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
    
    public void setDirective(DirectiveUIHints hints){
        this.hints=hints;
        
        this.directiveLabel.setText(hints.getLabel());
        for(Constructor c : hints.getConstructors()){
            String label="(";
            boolean first=true;
            for(Parameter p : c.getParameters()){
                if(!first) label+=",";
                else first=false;
                label+=p.getLabel();
            }
            label+=")";
            constructorComboBox.addItem(new ConstructorComboItem(c,label));
        }
        
    }
    
    protected JPanel makeMultiplePanel(Class<? extends AbstractTypePanel> typePanel,String label){
        MultipleParameterPanel p=new MultipleParameterPanel(typePanel);
        p.setLabel(label);
        return p;
    }
    
    protected Class<? extends AbstractTypePanel> getTypePanelClass(Parameter p){
        Class<?> cls=p.getType();
        if(Directive.class.isAssignableFrom(cls)) return DirectiveParameterPanel.class;
        else if(cls.equals(Integer.class) || cls.equals(Integer.TYPE)) return IntegerParameterPanel.class;
        else if(cls.equals(String.class)) return StringParameterPanel.class;
        else if(cls.equals(TopicOperand.class)) return TopicOperandParameterPanel.class;
        else if(cls.equals(Operand.class)) return OperandParameterPanel.class;
        // this is a guess really
        else if(cls.equals(Object.class)) return TopicOperandParameterPanel.class;

        else return UnknownParameterTypePanel.class;
    }
    
    
    protected void populateParametersPanel(Constructor c){
        constructorParameters.removeAll();
        if(c==null) return;
        
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.weightx=1.0;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        
        constructorParameters.removeAll();
        for(Parameter p : c.getParameters()){
            Class<? extends AbstractTypePanel> panelCls=getTypePanelClass(p);
            if(panelCls==null) continue;
            
            JPanel panel;
            if(p.isMultiple()){
                panel=makeMultiplePanel(panelCls, p.getLabel());
            }
            else {
                try{
                    panel=panelCls.newInstance();
                    ((AbstractTypePanel)panel).setLabel(p.getLabel());
                    
                    if(panel instanceof DirectiveParameterPanel){
                        if(!p.getType().equals(Directive.class)){
                            Class<? extends Directive> cls=(Class<? extends Directive>)p.getType();
                            ((DirectiveParameterPanel)panel).setDirectiveType(cls);
                        }
                    }
                }catch(IllegalAccessException | InstantiationException e){
                    Wandora.getWandora().handleError(e);
                    return;
                }
            }
            
            constructorParameters.add(panel,gbc);
            gbc.gridy++;
        }
        
        this.revalidate();
        constructorParameters.repaint();
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

        directiveLabel = new javax.swing.JLabel();
        constructorComboBox = new javax.swing.JComboBox();
        constructorParametersScroll = new javax.swing.JScrollPane();
        constructorParameters = new javax.swing.JPanel();
        addAddonButton = new javax.swing.JButton();
        addonComboBox = new javax.swing.JComboBox();
        addonPanel = new javax.swing.JPanel();
        resizeWidget = new javax.swing.JLabel();
        directiveAnchor = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        setLayout(new java.awt.GridBagLayout());

        directiveLabel.setText("Directive label");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(directiveLabel, gridBagConstraints);

        constructorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                constructorComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(constructorComboBox, gridBagConstraints);

        constructorParameters.setLayout(new java.awt.GridBagLayout());
        constructorParametersScroll.setViewportView(constructorParameters);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(constructorParametersScroll, gridBagConstraints);

        addAddonButton.setText("Add addon");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(addAddonButton, gridBagConstraints);

        addonComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addonComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(addonComboBox, gridBagConstraints);

        addonPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(addonPanel, gridBagConstraints);

        resizeWidget.setText("//");
        resizeWidget.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                resizeWidgetMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                resizeWidgetMouseReleased(evt);
            }
        });
        resizeWidget.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                resizeWidgetMouseDragged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        add(resizeWidget, gridBagConstraints);

        directiveAnchor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        directiveAnchor.setText("*");
        directiveAnchor.setMaximumSize(new java.awt.Dimension(20, 20));
        directiveAnchor.setMinimumSize(new java.awt.Dimension(20, 20));
        directiveAnchor.setPreferredSize(new java.awt.Dimension(20, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(directiveAnchor, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void constructorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_constructorComboBoxActionPerformed
        Object o=constructorComboBox.getSelectedItem();
        ConstructorComboItem c=(ConstructorComboItem)o;
        populateParametersPanel(c.c);
    }//GEN-LAST:event_constructorComboBoxActionPerformed

    private void addonComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addonComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addonComboBoxActionPerformed

    
    private int resizeStartX;
    private int resizeStartY;
    private int resizeStartW;
    private int resizeStartH;
    private boolean resizing=false;
    private void resizeWidgetMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resizeWidgetMousePressed
        resizing=true;
        resizeStartX=evt.getXOnScreen();
        resizeStartY=evt.getYOnScreen();
        Rectangle rect=this.getBounds();
        resizeStartW=rect.width;
        resizeStartH=rect.height;
    }//GEN-LAST:event_resizeWidgetMousePressed

    private void resizeWidgetMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resizeWidgetMouseReleased
        resizing=false;
    }//GEN-LAST:event_resizeWidgetMouseReleased

    private void resizeWidgetMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resizeWidgetMouseDragged
        if(resizing){
            int dx=evt.getXOnScreen()-resizeStartX;
            int dy=evt.getYOnScreen()-resizeStartY;
            Rectangle rect=this.getBounds();
            this.setBounds(rect.x,rect.y,resizeStartW+dx,resizeStartH+dy);
            this.revalidate();
        }
    }//GEN-LAST:event_resizeWidgetMouseDragged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAddonButton;
    private javax.swing.JComboBox addonComboBox;
    private javax.swing.JPanel addonPanel;
    private javax.swing.JComboBox constructorComboBox;
    private javax.swing.JPanel constructorParameters;
    private javax.swing.JScrollPane constructorParametersScroll;
    private javax.swing.JLabel directiveAnchor;
    private javax.swing.JLabel directiveLabel;
    private javax.swing.JLabel resizeWidget;
    // End of variables declaration//GEN-END:variables
}
