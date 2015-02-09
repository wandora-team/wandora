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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import org.wandora.application.gui.topicpanels.queryeditorpanel.DirectiveEditor.DirectiveParameters;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveUIHints;

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
    
    protected final ArrayList<ConnectorAnchor> paramsConnectors=new ArrayList<>();
    
    protected DirectiveParameters directiveParameters;
    
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
    
    public ConnectorAnchor connectParamAnchor(ConnectorAnchor from){
        synchronized(paramsConnectors){
            final JPanel anchorComp=new JPanel();
            
            final GridLayout layout=(GridLayout)parameterDirectiveAnchors.getLayout();
            layout.setColumns(paramsConnectors.size()+1);
            parameterDirectiveAnchors.add(anchorComp);
            
            parameterDirectiveAnchors.revalidate();
            
            ComponentConnectorAnchor anchor=new ComponentConnectorAnchor(anchorComp,ConnectorAnchor.Direction.DOWN, true, false){
                @Override
                public boolean setFrom(ConnectorAnchor from) {
                    boolean ret=super.setFrom(from); 
                    if(from==null){
                        parameterDirectiveAnchors.remove(anchorComp);
                        layout.setColumns(paramsConnectors.size()-1);
                        synchronized(paramsConnectors){
                            paramsConnectors.remove(this);
                        }
                    }
                    return ret;
                }
            };
            paramsConnectors.add(anchor);
            from.setTo(anchor);
            return anchor;
        }
    }
    
    
    public void saveDirectiveParameters(DirectiveParameters params){
        this.directiveParameters=params;
    }
    
    public DirectiveParameters getDirectiveParameters(){
        return directiveParameters;
    }
    
    public JPanel getEditorPanel(){
        DirectiveEditor editor=new DirectiveEditor(this,hints);
        
        return editor;
    }
    
    protected Object build(boolean script){
        /*
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
        }        */
        return null;
    }
    
    public String buildScript(){
        /*
        String s=(String)build(true);
        
        for(AddonPanel addonPanel : addonPanels ){
            String addonScript=addonPanel.buildScript();
            if(addonScript!=null) s+=addonScript;
        }
        
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
        */
        return null;
    }
    
    public Directive buildDirective(){
        return (Directive)build(false);
        // TODO: Doesn't add connectors as From directives yet
    }
/*    
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
  */  

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
    

    
    public void setDirective(DirectiveUIHints hints){
        this.hints=hints;
        this.directiveLabel.setText(this.hints.getLabel());
        this.setSize(this.getPreferredSize());
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
        toDirectiveAnchor = new javax.swing.JLabel();
        fromDirectiveAnchor = new javax.swing.JLabel();
        parameterDirectiveAnchors = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        setLayout(new java.awt.GridBagLayout());

        directiveLabel.setText("Directive label");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        add(directiveLabel, gridBagConstraints);

        toDirectiveAnchor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        toDirectiveAnchor.setText("<-");
        toDirectiveAnchor.setMaximumSize(new java.awt.Dimension(20, 20));
        toDirectiveAnchor.setMinimumSize(new java.awt.Dimension(20, 20));
        toDirectiveAnchor.setPreferredSize(new java.awt.Dimension(20, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
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

        parameterDirectiveAnchors.setMinimumSize(new java.awt.Dimension(1, 1));
        parameterDirectiveAnchors.setLayout(new java.awt.GridLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(parameterDirectiveAnchors, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    private int resizeStartX;
    private int resizeStartY;
    private int resizeStartW;
    private int resizeStartH;
    private boolean resizing=false;
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel directiveLabel;
    private javax.swing.JLabel fromDirectiveAnchor;
    private javax.swing.JPanel parameterDirectiveAnchors;
    private javax.swing.JLabel toDirectiveAnchor;
    // End of variables declaration//GEN-END:variables


}
