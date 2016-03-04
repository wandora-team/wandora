/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.Array;
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
import org.wandora.query2.DirectiveUIHints.Addon;
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
        
        titleLabel.addMouseListener(new MouseAdapter(){
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
        titleLabel.addMouseMotionListener(new MouseMotionAdapter(){
            @Override
            public void mouseDragged(MouseEvent e) {
                QueryEditorComponent editor=getEditor();
                if(editor!=null) {
                    Rectangle rect=getBounds();
                    setBounds(  rect.x+e.getX()-dragStartX,
                                rect.y+e.getY()-dragStartY,
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
    
    public static DirectivePanel findDirectivePanel(Component c){
        while(c!=null && !(c instanceof DirectivePanel) && c instanceof Container){
            c=((Container)c).getParent();
        }
        if(c instanceof DirectivePanel) return (DirectivePanel)c;
        else return null;
    }
    
    public void setDetailsText(String text){
        if(text==null) text="";
        if(!text.isEmpty()) {
            text=text.replace("&","&amp;");
            text=text.replace("<","&lt;");
            text=text.replace("\n", "<br>");
            text="<html>"+text+"</html>";
        }
        
        detailsLabel.setText(text);
        //int height=titleLabel.getPreferredSize().height+detailsLabel.getPreferredSize().height+5;
        //this.setSize(this.getSize().width, height);
        this.setSize(this.getPreferredSize());
        this.repaint();
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
    
    protected BoundParameter clearParameterValue(BoundParameter param,Object value){
        if(param==null) return null;
        if(param.value==value) return new BoundParameter(param.getParameter(),null);
        if(param.getParameter().isMultiple()){
            for(int i=0;i<Array.getLength(param.value);i++){
                if(Array.get(param.value, i)==value) {
                    Array.set(param.value, i, null);
                }
            }
        }
        return param;
    }
    
    protected void directiveConnectionRemoved(DirectivePanel panel){
        if(panel==null) return;
        if(directiveParameters==null) return;
        
        if(directiveParameters.parameters!=null){
            for(int i=0;i<directiveParameters.parameters.length;i++){
                directiveParameters.parameters[i]=clearParameterValue(directiveParameters.parameters[i],panel);
            }
        }
        if(directiveParameters.addons!=null){
            for(AddonParameters a : directiveParameters.addons){
                if(a.parameters!=null){
                    for(int i=0;i<a.parameters.length;i++){
                        a.parameters[i]=clearParameterValue(a.parameters[i],panel);
                    }
                }
            }
        }
    }
        
    public ConnectorAnchor connectParamAnchor(ConnectorAnchor from,String ordering){
        synchronized(paramAnchors){
            final JPanel anchorComp=new JPanel();
            final ParamAnchorInfo info=new ParamAnchorInfo(null, anchorComp, ordering);
            
            ComponentConnectorAnchor anchor=new ComponentConnectorAnchor(anchorComp,ConnectorAnchor.Direction.DOWN, true, false){
                @Override
                public boolean setFrom(ConnectorAnchor from) {
                    ConnectorAnchor oldFromAnchor=getFrom();
                    DirectivePanel oldFrom=DirectivePanel.findDirectivePanel(oldFromAnchor.getComponent());
                    directiveConnectionRemoved(oldFrom);
                    
                    boolean ret=super.setFrom(from); 
                    if(from==null){
                        synchronized(paramAnchors){
                            paramAnchors.remove(info);
                            updateParamAnchors();
                            updateDetailsText();
                        }
                    }
                    return ret;
                }
            };
            info.anchor=anchor;
            paramAnchors.add(info);
            from.setTo(anchor);
            updateParamAnchors();
            getEditor().applyInspectorChanges();
            updateDetailsText();
            return anchor;
        }
    }
    
    /**
     * Sets directive parameters and connects any connectors set in the params
     * as well as sets the panel dimensions.
     * @param params 
     */
    public void setDirectiveParams(DirectiveParameters params){
        saveDirectiveParameters(params);
        Rectangle bounds=getBounds();
        setBounds(new Rectangle(params.posx,params.posy,bounds.width,bounds.height));
        
        params.connectAnchors(this);
    }
    
    /**
     * Sets directive parameters but does not connect connectors, or panel
     * dimensions, only stores the parameters variable.
     * @param params 
     */
    public void saveDirectiveParameters(DirectiveParameters params){
        this.directiveParameters=params;
        updateDetailsText();
    }
    
    public void updateDetailsText(){
        setDetailsText(buildDetailsText());        
    }
    
    public String buildDetailsLine(String text,String next){
        StringBuilder sb=new StringBuilder(text);
        buildDetailsLine(sb,next);
        return sb.toString();
    }
    public void buildDetailsLine(StringBuilder text,String next){
        int ind=text.lastIndexOf("\n");
        int lastLineLength=text.length()-(ind+1);
        
        ind=next.indexOf("\n");
        int nextLineLength=(ind>=0?ind:(next.length()));
        
        if(lastLineLength+nextLineLength>25 && text.length()>0) text.append("\n").append(next);
        else {
            if(text.length()>0) text.append(" ");
            text.append(next);
        }
    }
    
    public String buildDetailsParamText(Object o){
        if(o==null){
            return "null";
        }
        else if(o instanceof DirectivePanel){
            return "<DIR>";
        }
        else if(o.getClass().isArray()){
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<Array.getLength(o);i++){
                if(i>0) sb.append(",");
                buildDetailsLine(sb,buildDetailsParamText(Array.get(o, i)));
            }
            sb.append("]");
            return "["+sb.toString();
        }
        else {
            String s=o.toString();
            if(s.length()>15){
                int ind=s.lastIndexOf("#");
                if(ind>=0) s=s.substring(ind);
                else {
                    ind=s.lastIndexOf("/");
                    if(ind>=0) s=s.substring(ind);
                }
                if(s.length()>15) s="..."+s.substring(s.length()-15);
                else s="..."+s;
            }
            if(o instanceof String) return "\""+s+"\"";
            else return "<"+s+">";
        }
        
    }
    
    public String buildDetailsParamsText(BoundParameter[] params){
        StringBuilder sb=new StringBuilder();
        
        for(BoundParameter p : params){
            Object o=p.getValue();
            String s=buildDetailsParamText(o);
            if(s!=null) {
                if(sb.length()>0) sb.append(",");
                buildDetailsLine(sb,s);
            }
        }
        
        return sb.toString();
    }
    
    public String buildDetailsText(){
        
        StringBuilder sb=new StringBuilder();
        String constructor=buildDetailsParamsText(this.directiveParameters.parameters);
        if(constructor.length()>0) sb.append("(").append(constructor).append(")");
        
        AddonParameters[] addons=this.directiveParameters.addons;
        for(AddonParameters a : addons){
            String addon=buildDetailsParamsText(a.parameters);
            if(addon.length()>0){
                if(sb.length()>0) sb.append("\n");
                sb.append(".").append(a.addon.getMethod());
                sb.append("(").append(addon).append(")");
            }
        }
        
        return sb.toString();
    }
    
    public DirectiveParameters getDirectiveParameters(){
        if(directiveParameters==null){
            Constructor c=null;
            if(hints.getConstructors().length>0) c=hints.getConstructors()[0];
            BoundParameter[] params=new BoundParameter[0];
            if(c!=null && c.getParameters().length>0) params=new BoundParameter[c.getParameters().length];
            directiveParameters=new DirectiveParameters(getDirectiveId(),c,params,new AddonParameters[0]);
        }
        
        directiveParameters.id=this.getDirectiveId();
        directiveParameters.from=getFromPanel();
        directiveParameters.cls=hints.getDirectiveClass().getName();
        
        Rectangle bounds=getBounds();
        directiveParameters.posx=bounds.x;
        directiveParameters.posy=bounds.y;
        
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
    
    public DirectivePanel getFromPanel(){
        ConnectorAnchor fromA=toConnectorAnchor.getFrom();
        if(fromA!=null){
            JComponent component=fromA.getComponent();
            if(component==null) return null;
            DirectivePanel p=QueryEditorComponent.resolveDirectivePanel(component);
            return p;
        }
        else return null;
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
        
        DirectivePanel p=getFromPanel();
        if(p!=null){
            String s2=p.buildScript();
            s2=DirectiveUIHints.indent(s2, 2);
            s+=".from(\n"+s2+"\n)";
        }
        return s;

    }
    
    public Directive buildDirective(){

        Object[] params=null;
        java.lang.reflect.Constructor constr=null;
        try{
            if(directiveParameters!=null) {
                constr=directiveParameters.constructor.getReflectConstructor(hints.getDirectiveClass());
                params=new Object[directiveParameters.parameters.length];

                for(int i=0;i<directiveParameters.parameters.length;i++){
                    BoundParameter param=directiveParameters.parameters[i];
                    params[i]=param.getBuildValue();
                }
            }
            else {
                Constructor[] cs=hints.getConstructors();
                for(Constructor c : cs){
                    if(c.getParameters().length==0){
                        constr=c.getReflectConstructor(hints.getDirectiveClass());
                        params=new Object[0];
                        break;
                    }
                }            
            }

            if(constr!=null){
                try{
                    Object newInst=constr.newInstance(params);
                    Directive dir=(Directive)newInst;

                    if(directiveParameters!=null){
                        for(AddonParameters addon : directiveParameters.addons){
                            java.lang.reflect.Method method=addon.addon.resolveMethod(dir.getClass());
                            Object[] addonParams=new Object[addon.parameters.length];
                            for(int i=0;i<addon.parameters.length;i++){
                                BoundParameter param=addon.parameters[i];
                                addonParams[i]=param.getBuildValue();
                            }
                            dir=(Directive)method.invoke(dir, addonParams);
                        }
                    }

                    DirectivePanel p=getFromPanel();
                    if(p!=null){
                        Directive from=p.buildDirective();
                        return dir.from(from);
                    }
                    else return dir;

                }
                catch(IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException e){
                    Wandora.getWandora().handleError(e);
                }
            }        
        }
        catch(NoSuchMethodException e){
            Wandora.getWandora().handleError(e);            
        }
        return null;
    }
    
    public String getDirectiveId(){
        return ""+System.identityHashCode(this);
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
    

    
    public void setDirective(DirectiveUIHints hints){
        this.hints=hints;
        this.titleLabel.setText(this.hints.getLabel());
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

        detailsLabel = new javax.swing.JLabel();
        toDirectiveAnchor = new javax.swing.JLabel();
        fromDirectiveAnchor = new javax.swing.JLabel();
        parameterDirectiveAnchors = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(detailsLabel, gridBagConstraints);

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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(parameterDirectiveAnchors, gridBagConstraints);

        titleLabel.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        titleLabel.setText("Title");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        add(titleLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JLabel detailsLabel;
    protected javax.swing.JLabel fromDirectiveAnchor;
    private javax.swing.JPanel parameterDirectiveAnchors;
    private javax.swing.JLabel titleLabel;
    protected javax.swing.JLabel toDirectiveAnchor;
    // End of variables declaration//GEN-END:variables


}
