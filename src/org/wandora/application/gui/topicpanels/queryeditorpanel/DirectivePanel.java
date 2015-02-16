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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import org.wandora.application.Wandora;
import org.wandora.application.gui.topicpanels.queryeditorpanel.DirectiveEditor.AddonParameters;
import org.wandora.application.gui.topicpanels.queryeditorpanel.DirectiveEditor.BoundParameter;
import org.wandora.application.gui.topicpanels.queryeditorpanel.DirectiveEditor.DirectiveParameters;
import org.wandora.query2.Directive;
import org.wandora.query2.DirectiveUIHints;
import org.wandora.query2.DirectiveUIHints.Constructor;

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
    
//    protected final ArrayList<ConnectorAnchor> paramsConnectors=new ArrayList<>();
    
    protected DirectiveParameters directiveParameters;
    
    protected final ArrayList<ParamAnchorInfo> paramAnchors=new ArrayList<>();
    
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
    
    protected static class ParamAnchorInfo implements Comparable {
        public ConnectorAnchor anchor;
        public JPanel component;
        public String order;

        public ParamAnchorInfo(){}
        
        public ParamAnchorInfo(ConnectorAnchor anchor, JPanel component, String order) {
            this.anchor = anchor;
            this.component = component;
            this.order = order;
        }
        
        
        
        @Override
        public int compareTo(Object o) {
            if(o==null) return 1;
            if(!o.getClass().equals(this.getClass())) return 1;
            ParamAnchorInfo p=(ParamAnchorInfo)o;
            
            if(this.order==null && p.order==null) return 0;
            else if(this.order==null && p.order!=null) return -1;
            else if(this.order!=null && p.order==null) return 1;
            else return this.order.compareTo(p.order);
        }
    
    }
    
    protected void updateParamAnchors(){
        synchronized(paramAnchors){
            Collections.sort(paramAnchors);
            
            parameterDirectiveAnchors.removeAll();
            final GridLayout layout=(GridLayout)parameterDirectiveAnchors.getLayout();
            layout.setColumns(paramAnchors.size());
            
            for(ParamAnchorInfo p : paramAnchors){
                parameterDirectiveAnchors.add(p.component);
            }
        }
        parameterDirectiveAnchors.revalidate();
        getEditor().repaint();
    }
    
    public ConnectorAnchor connectParamAnchor(ConnectorAnchor from,String ordering){
        synchronized(paramAnchors){
            final JPanel anchorComp=new JPanel();
            final ParamAnchorInfo info=new ParamAnchorInfo(null, anchorComp, ordering);
            
            ComponentConnectorAnchor anchor=new ComponentConnectorAnchor(anchorComp,ConnectorAnchor.Direction.DOWN, true, false){
                @Override
                public boolean setFrom(ConnectorAnchor from) {
                    boolean ret=super.setFrom(from); 
                    if(from==null){
                        synchronized(paramAnchors){
                            paramAnchors.remove(info);
                            updateParamAnchors();
                        }
                    }
                    return ret;
                }
            };
            info.anchor=anchor;
            paramAnchors.add(info);
            from.setTo(anchor);
            updateParamAnchors();
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
    
    protected Object buildConstructor(boolean script){
        
        if(!script) throw new RuntimeException("not implemented");

        StringBuilder sb=new StringBuilder();
        sb.append("new ");
        sb.append(hints.getDirectiveClass().getSimpleName());
        sb.append("(");

        if(directiveParameters!=null){
            Constructor c=directiveParameters.constructor;

            BoundParameter[] parameters=directiveParameters.parameters;
            for(int i=0;i<parameters.length;i++){
                BoundParameter p=parameters[i];
                if(i>0) sb.append(", ");
                sb.append(p.getScriptValue());
            }
        }
        sb.append(")");
        return sb.toString();
    }
    
    public String buildAddonScript(AddonParameters addon){
        
        StringBuilder sb=new StringBuilder();
        sb.append(".");
        sb.append(addon.addon.getMethod());
        sb.append("(");
        for(int i=0;i<addon.parameters.length;i++){
            BoundParameter p=addon.parameters[i];
            if(i>0) sb.append(", ");
            sb.append(p.getScriptValue());
        }
        sb.append(")");
        return sb.toString();
    }
    
    public String buildScript(){
        
        String s=(String)buildConstructor(true);

        if(directiveParameters!=null){
            for(AddonParameters addon : directiveParameters.addons){
                String addonScript=buildAddonScript(addon);
                if(addonScript!=null) s+=addonScript;
            }
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

    }
    
    public Directive buildDirective(){
        throw new RuntimeException("not implemnted");
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
        parameterDirectiveAnchors.setLayout(new java.awt.GridLayout(1, 0));
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
    protected javax.swing.JLabel directiveLabel;
    protected javax.swing.JLabel fromDirectiveAnchor;
    private javax.swing.JPanel parameterDirectiveAnchors;
    protected javax.swing.JLabel toDirectiveAnchor;
    // End of variables declaration//GEN-END:variables


}
