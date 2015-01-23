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
 * 
 */

package org.wandora.application.gui.topicpanels.queryeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.wandora.application.Wandora;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveUIHints;
import org.wandora.query2.DirectiveUIHints.Addon;
import org.wandora.query2.DirectiveUIHints.BoundParameter;
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
    protected ConnectorAnchor toConnectorAnchor;
    protected ConnectorAnchor fromConnectorAnchor;
    
    protected Constructor selectedConstructor;
    protected AbstractTypePanel[] constructorParamPanels;
    
    protected Dimension normalDimensions;
    
    /**
     * Creates new form DirectivePanel
     */
    public DirectivePanel() {
        initComponents();
        this.toConnectorAnchor=new ComponentConnectorAnchor(toDirectiveAnchor,ConnectorAnchor.Direction.RIGHT,true,false);
        this.fromConnectorAnchor=new ComponentConnectorAnchor(fromDirectiveAnchor,ConnectorAnchor.Direction.LEFT,false,true);
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
                QueryEditorComponent editor=getEditor();
                if(editor!=null) editor.selectPanel(DirectivePanel.this);
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
        
        
        DnDTools.setDragSourceHandler(fromDirectiveAnchor, "directivePanel", DnDTools.directivePanelDataFlavor, new DnDTools.DragSourceCallback<DirectivePanel>() {
            @Override
            public DirectivePanel callback(JComponent component) {
                return DirectivePanel.this;
            }
        });
        
        DnDTools.addDropTargetHandler(toDirectiveAnchor, DnDTools.directivePanelDataFlavor, new DnDTools.DropTargetCallback<DirectivePanel>(){
            @Override
            public boolean callback(JComponent component, DirectivePanel o, TransferHandler.TransferSupport support) {
                if(o==DirectivePanel.this) return false;
                toConnectorAnchor.setFrom(o.getFromConnectorAnchor());
                return true;
            }
        });
    }
    
    protected Object build(boolean script){
        Object o=constructorComboBox.getSelectedItem();
        if(o==null) return null;
        
        ConstructorComboItem cci=(ConstructorComboItem)o;
        Constructor c=cci.c;
        
        BoundParameter[] parameters=getParameters(script);
        try{
            if(script) return c.newScript(parameters, hints.getDirectiveClass());
            else return c.newInstance(parameters, hints.getDirectiveClass());
        }
        catch(IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException | NoSuchMethodException e){
            Wandora.getWandora().handleError(e);
            return null;
        }        
    }
    
    public String buildScript(){
        String s=(String)build(true);
        ConnectorAnchor fromA=toConnectorAnchor.getFrom();
        if(fromA!=null){
            JComponent component=fromA.getComponent();
            if(component==null) return null;
            DirectivePanel p=QueryEditorComponent.resolveDirectivePanel(component);
            if(p==null) return null;
            
            String s2=p.buildScript();
            s2=DirectiveUIHints.indent(s2, 2);
            return s+".from(\n"+s2+"\n)";
        }
        else return s;
    }
    
    public Directive buildDirective(){
        return (Directive)build(false);
    }
    
    public BoundParameter[] getParameters(boolean script){
        BoundParameter[] ret=new BoundParameter[constructorParamPanels.length];
        for(int i=0;i<constructorParamPanels.length;i++){
            AbstractTypePanel paramPanel=constructorParamPanels[i];
            if(script) {
                String s=paramPanel.getValueScript();
                ret[i]=paramPanel.getParameter().bindSerial(s);
            }
            else {
                Object val=paramPanel.getValue();
                ret[i]=paramPanel.getParameter().bind(val);
            }
        }
        return ret;
    }
    
    protected static class MinimizedComponent {
        public Component component;
        public Dimension minSize,prefSize,maxSize,size;
        public ArrayList<MinimizedComponent> children=new ArrayList<>();
        public MinimizedComponent(Component component){
            this.component=component;
            minSize=component.getMinimumSize();
            prefSize=component.getPreferredSize();
            maxSize=component.getMaximumSize();
            size=component.getSize();
            component.setMinimumSize(new Dimension(0,0));
            component.setPreferredSize(new Dimension(0,0));
            component.setMaximumSize(new Dimension(0,0));
            component.setSize(new Dimension(0,0));
            component.setVisible(false);
/*            
            if(component instanceof Container){
                for(Component c : ((Container)component).getComponents()) {
                    children.add(new MinimizedComponent(c));
                }
            }*/
            component.invalidate();
        }
        public void restore(){
            component.setMaximumSize(maxSize);
            component.setPreferredSize(prefSize);
            component.setMinimumSize(minSize);            
            component.setSize(size);
            component.setVisible(true);
            
//            for(MinimizedComponent mc : children) mc.restore();
            component.invalidate();
        }
    }
    protected final ArrayList<MinimizedComponent> minimizedComponents=new ArrayList<>();
    
    public synchronized void setMinimized(boolean b){
        if(b){
            if(this.normalDimensions!=null) return;
            this.normalDimensions=new Dimension(this.getSize());
            minimizedComponents.add(new MinimizedComponent(constructorComboBox));
            minimizedComponents.add(new MinimizedComponent(constructorParametersScroll));
            minimizedComponents.add(new MinimizedComponent(addonComboBox));
            minimizedComponents.add(new MinimizedComponent(addAddonButton));
            minimizedComponents.add(new MinimizedComponent(addonPanel));
            minimizedComponents.add(new MinimizedComponent(resizeWidget));
            
            this.setSize(new Dimension(normalDimensions.width,30));
        }
        else {
            if(this.normalDimensions==null) return;
            this.setSize(this.normalDimensions);
            this.normalDimensions=null;
            for(MinimizedComponent mc : minimizedComponents){
                mc.restore();
            }
            minimizedComponents.clear();
        }
        this.validate();
        getEditor().panelMoved(this);
    }
    
    public void toggleMinimized(){
        setMinimized(this.normalDimensions==null);
    }
    
    public void setSelected(boolean b){
        if(b){
            setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 255), 2));
        }
        else {
            setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));            
        }
    }
    
    public ConnectorAnchor getToConnectorAnchor(){
        return toConnectorAnchor;
    }
    public ConnectorAnchor getFromConnectorAnchor(){
        return fromConnectorAnchor;
    }
    
    public void disconnectConnectors(){
        toConnectorAnchor.setFrom(null);
        fromConnectorAnchor.setTo(null);
    }

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
    
    private class AddonComboItem {
        public Addon a;
        public String label;
        public AddonComboItem(Addon a,String label){
            this.a=a;
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
     
        Addon[] addons=hints.getAddons();
        if(addons!=null){
            for(Addon a : addons) {
                String label=a.getLabel()+"(";
                boolean first=true;
                for(Parameter p : a.getParameters()){
                    if(!first) label+=",";
                    else first=false;
                    label+=p.getLabel();
                }
                label+=")";
                addonComboBox.addItem(new AddonComboItem(a,label));            
            }
        }
    }
    
    protected AbstractTypePanel makeMultiplePanel(Parameter param,Class<? extends AbstractTypePanel> typePanel,String label){
        MultipleParameterPanel p=new MultipleParameterPanel(param,typePanel);
        p.setLabel(label);
        return p;
    }
    
    protected Class<? extends AbstractTypePanel> getTypePanelClass(Parameter p){
        Class<?> cls=p.getType();
        if(cls.equals(Integer.class) || cls.equals(Integer.TYPE)) return IntegerParameterPanel.class;
        else if(cls.equals(String.class)) return StringParameterPanel.class;
        else if(cls.equals(TopicOperand.class)) return TopicOperandParameterPanel.class;
        else if(cls.equals(Operand.class)) return OperandParameterPanel.class;
        else if(Directive.class.isAssignableFrom(cls)) return DirectiveParameterPanel.class;
        // this is a guess really
        else if(cls.equals(Object.class)) return TopicOperandParameterPanel.class;

        else return UnknownParameterTypePanel.class;
    }
    
    
    protected void populateParametersPanel(Constructor c){
        if(c==null) return;

        if(constructorParamPanels!=null){
            for(AbstractTypePanel panel : constructorParamPanels){
                panel.disconnect();
            }
        }
        constructorParameters.removeAll();
        
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.weightx=1.0;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        
        Parameter[] parameters=c.getParameters();
        this.constructorParamPanels=new AbstractTypePanel[parameters.length];
        
        for(int i=0;i<parameters.length;i++){
            Parameter p=parameters[i];
            Class<? extends AbstractTypePanel> panelCls=getTypePanelClass(p);
            if(panelCls==null) continue;
            
            AbstractTypePanel panel;
            if(p.isMultiple()){
                panel=makeMultiplePanel(p, panelCls, p.getLabel());
            }
            else {
                try{
                    java.lang.reflect.Constructor<? extends AbstractTypePanel> panelConstructor=panelCls.getConstructor(Parameter.class);
                    panel=panelConstructor.newInstance(p);
                    panel.setLabel(p.getLabel());
                    
                    if(panel instanceof DirectiveParameterPanel){
                        if(!p.getType().equals(Directive.class)){
                            Class<? extends Directive> cls=(Class<? extends Directive>)p.getType();
                            ((DirectiveParameterPanel)panel).setDirectiveType(cls);
                        }
                    }
                }catch(IllegalAccessException | InstantiationException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e){
                    Wandora.getWandora().handleError(e);
                    return;
                }
            }
            
            constructorParamPanels[i]=panel;
            
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
        toDirectiveAnchor = new javax.swing.JLabel();
        fromDirectiveAnchor = new javax.swing.JLabel();
        minimizeButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        setLayout(new java.awt.GridBagLayout());

        directiveLabel.setText("Directive label");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(directiveLabel, gridBagConstraints);

        constructorComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                constructorComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(constructorComboBox, gridBagConstraints);

        constructorParameters.setLayout(new java.awt.GridBagLayout());
        constructorParametersScroll.setViewportView(constructorParameters);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(constructorParametersScroll, gridBagConstraints);

        addAddonButton.setText("Add addon");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
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
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        add(resizeWidget, gridBagConstraints);

        toDirectiveAnchor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        toDirectiveAnchor.setText("<-");
        toDirectiveAnchor.setMaximumSize(new java.awt.Dimension(20, 20));
        toDirectiveAnchor.setMinimumSize(new java.awt.Dimension(20, 20));
        toDirectiveAnchor.setPreferredSize(new java.awt.Dimension(20, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(toDirectiveAnchor, gridBagConstraints);

        fromDirectiveAnchor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fromDirectiveAnchor.setText("<-");
        fromDirectiveAnchor.setMaximumSize(new java.awt.Dimension(20, 20));
        fromDirectiveAnchor.setMinimumSize(new java.awt.Dimension(20, 20));
        fromDirectiveAnchor.setPreferredSize(new java.awt.Dimension(20, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(fromDirectiveAnchor, gridBagConstraints);

        minimizeButton.setText("_");
        minimizeButton.setBorderPainted(false);
        minimizeButton.setContentAreaFilled(false);
        minimizeButton.setFocusPainted(false);
        minimizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimizeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        add(minimizeButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void constructorComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_constructorComboBoxActionPerformed
        Object o=constructorComboBox.getSelectedItem();
        ConstructorComboItem c=(ConstructorComboItem)o;
        this.selectedConstructor=c.c;
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
            getEditor().panelMoved(this);
        }
    }//GEN-LAST:event_resizeWidgetMouseDragged

    private void minimizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeButtonActionPerformed
        toggleMinimized();
    }//GEN-LAST:event_minimizeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAddonButton;
    private javax.swing.JComboBox addonComboBox;
    private javax.swing.JPanel addonPanel;
    private javax.swing.JComboBox constructorComboBox;
    private javax.swing.JPanel constructorParameters;
    private javax.swing.JScrollPane constructorParametersScroll;
    private javax.swing.JLabel directiveLabel;
    private javax.swing.JLabel fromDirectiveAnchor;
    private javax.swing.JButton minimizeButton;
    private javax.swing.JLabel resizeWidget;
    private javax.swing.JLabel toDirectiveAnchor;
    // End of variables declaration//GEN-END:variables
}
