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
 * 
 *
 * LayerControlPanel.java
 *
 * Created on 20. lokakuuta 2005, 13:24
 */

package org.wandora.topicmap.layered;




import org.wandora.application.Wandora;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import javax.swing.*;
import java.util.*;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.event.*;
import org.wandora.utils.Delegate;
import org.wandora.application.gui.simple.*;



/**
 *
 * Use LayerTree instead!
 * 
 * @deprecated
 * @author  olli, akivela
 */
public class LayerControlPanel extends javax.swing.JPanel implements AmbiguityResolver, ActionListener, MouseListener  {
    
    public JMenu importToLayerMenu = new SimpleMenu("Merge to layer", null);
    public JMenu exportLayerMenu = new SimpleMenu("Export layer", null);
    
    public LayerStack layerStack;
    public Delegate<Object,Object> listenerChanged;
    public Delegate<Boolean,Object> listenerChanging;
    
    private java.awt.image.BufferedImage dragImage=null;
    private int drawDragLine=-1;
    private int draggingLayer=-1;
    private int dragPos=-1;
    private int dragOffsX=-1;
    private int dragOffsY=-1;
    private int dragMouseX=-1;
    private int dragMouseY=-1;
    private Wandora wandora;
    
    private LayerStatusPanel lsp;
    
    
    /** Creates new form LayerControlPanel */
    public LayerControlPanel(LayerStack layerStack, Wandora w) {
        this.wandora=w;
        this.layerStack=layerStack;
        initComponents();
        
        layersScrollPane.addMouseListener(this);
        // layersScrollPane.setComponentPopupMenu(UIBox.makePopupMenu(simpleMenuStructure, admin));
        layerStack.setAmbiguityResolver(this);
        setFocusable(true);
        initKeyBindings(wandora);
    }
    
    
    
    public void initKeyBindings(Wandora wandora) {
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), "selectLayerDown");
        getActionMap().put("selectLayerDown",
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    //System.out.println("ActionEvent: " + e);
                    if(layersChanging()) {
                        int layerIndex = layerStack.getSelectedIndex();
                        if(layerStack.getLayers().size() > layerIndex+1) {
                            Layer l = layerStack.getLayers().get(layerIndex + 1);
                            layerStack.selectLayer(l);
                            resetLayers();
                        }
                    }
                }
            }
        );
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), "selectLayerUp");
        getActionMap().put("selectLayerUp",
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    //System.out.println("ActionEvent: " + e);
                    if(layersChanging()) {
                        int layerIndex = layerStack.getSelectedIndex();
                        if(layerIndex > 0) {
                            Layer l = layerStack.getLayers().get(layerIndex - 1);
                            layerStack.selectLayer(l);
                            resetLayers();
                        }
                    }
                }
            }
        );
    }
    
    
    public void setChangedListener(Delegate<Object,Object> listener){
        this.listenerChanged=listener;
    }
    public void setChangingListener(Delegate<Boolean,Object> listener){
        this.listenerChanging=listener;
    }
    
    public void ambiguity(String s){
        appendAmbiguity(s);
    }
    
    public AmbiguityResolution resolveAmbiguity(String event){
        return resolveAmbiguity(event,null);
    }
    public AmbiguityResolution resolveAmbiguity(String event,String msg){
//        ambiguity(event+(msg==null?"":(" "+msg)));
        if(msg!=null) ambiguity(msg+" ("+event+")");
        else ambiguity(event);
        return AmbiguityResolution.addToSelected;
    }
    
    
    
    public LayerStatusPanel getPanelFor(Layer l) {
        if(l == null) return null;
        Component[] layerComponents = layerPanel.getComponents();
        LayerStatusPanel layerStatusPanel = null;
        for(int i=0; i<layerComponents.length; i++) {
            if(layerComponents[i] != null && layerComponents[i] instanceof LayerStatusPanel) {
                layerStatusPanel = (LayerStatusPanel) layerComponents[i];
                if(l.equals(layerStatusPanel.getLayer())) return layerStatusPanel;
            }
        }
        return null;
    }
    public String getNameFor(TopicMap topicMap) {
        if(topicMap == null) return null;
        Component[] layerComponents = layerPanel.getComponents();
        Layer layer = null;
        LayerStatusPanel layerStatusPanel = null;
        for(int i=0; i<layerComponents.length; i++) {
            if(layerComponents[i] != null && layerComponents[i] instanceof LayerStatusPanel) {
                layerStatusPanel = (LayerStatusPanel) layerComponents[i];
                layer = layerStatusPanel.getLayer();
                if(layer != null && topicMap.equals(layer.getTopicMap())) return layer.getName();
            }
        }
        return null;
    }
    
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        newButton = new org.wandora.application.gui.simple.SimpleButton();
        deleteButton = new org.wandora.application.gui.simple.SimpleButton();
        modifyDialog = new JDialog(wandora, true);
        modifyContainerPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        modifyOKButton = new org.wandora.application.gui.simple.SimpleButton();
        modifyCancelButton = new org.wandora.application.gui.simple.SimpleButton();
        jLabel1 = new org.wandora.application.gui.simple.SimpleLabel();
        modifyNameTextField = new org.wandora.application.gui.simple.SimpleField();
        jScrollPane2 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        layersScrollPane = new javax.swing.JScrollPane();
        layersPanel = new EditorPanel(wandora, org.wandora.application.tools.importers.AbstractImportTool.TOPICMAP_MAKE_NEW_LAYER, wandora);
        layerPanel = new javax.swing.JPanel(){
            public void paint(java.awt.Graphics g){
                super.paint(g);
                drawPanelOverlay(g);
            }
        };

        upButton.setText("Up");
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        downButton.setText("Down");
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        jPanel3.setLayout(new java.awt.GridBagLayout());

        newButton.setText("New");
        newButton.setPreferredSize(new java.awt.Dimension(66, 23));
        newButton.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(newButton, gridBagConstraints);

        deleteButton.setText("Delete");
        deleteButton.setPreferredSize(new java.awt.Dimension(66, 23));
        deleteButton.addActionListener(this);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel3.add(deleteButton, gridBagConstraints);

        modifyDialog.setTitle("Modify layer configuration");
        modifyDialog.setModal(true);
        modifyDialog.getContentPane().setLayout(new java.awt.GridBagLayout());

        modifyContainerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        modifyDialog.getContentPane().add(modifyContainerPanel, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        modifyOKButton.setText("OK");
        modifyOKButton.setMaximumSize(new java.awt.Dimension(70, 23));
        modifyOKButton.setMinimumSize(new java.awt.Dimension(70, 23));
        modifyOKButton.setPreferredSize(new java.awt.Dimension(70, 23));
        modifyOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyOKButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(modifyOKButton, gridBagConstraints);

        modifyCancelButton.setText("Cancel");
        modifyCancelButton.setMaximumSize(new java.awt.Dimension(70, 23));
        modifyCancelButton.setMinimumSize(new java.awt.Dimension(70, 23));
        modifyCancelButton.setPreferredSize(new java.awt.Dimension(70, 23));
        modifyCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifyCancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(modifyCancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        modifyDialog.getContentPane().add(jPanel2, gridBagConstraints);

        jLabel1.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
        modifyDialog.getContentPane().add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 5);
        modifyDialog.getContentPane().add(modifyNameTextField, gridBagConstraints);

        jScrollPane2.setMaximumSize(new java.awt.Dimension(32767, 50));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(4, 50));

        textArea.setEditable(false);
        textArea.setFont(org.wandora.application.gui.UIConstants.plainFont);
        jScrollPane2.setViewportView(textArea);

        setLayout(new java.awt.GridBagLayout());

        layersPanel.setBackground(UIConstants.defaultInactiveBackground);
        ((EditorPanel) layersPanel).setImage("gui/components/LayerControlPanelBackground.jpg");
        ((EditorPanel) layersPanel).setAlign(EditorPanel.BOTTOM_LEFT_ALIGN);
        layersPanel.setLayout(new java.awt.GridBagLayout());

        layerPanel.setLayout(new java.awt.GridLayout(1, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        layersPanel.add(layerPanel, gridBagConstraints);

        layersScrollPane.setViewportView(layersPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(layersScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
    private TopicMapConfigurationPanel modifyPanel;
    private TopicMapType modifyType;
    private Layer modifyLayer;
    public void modifyLayer() {
        modifyLayer(layerStack.getSelectedLayer());
    }
    
    public void modifyLayer(Layer modifyLayer) {
        if(!layersChanging()) return;
        this.modifyLayer=modifyLayer;
        TopicMap tm=modifyLayer.getTopicMap();
        modifyType=TopicMapTypeManager.getType(tm);
        modifyPanel=modifyType.getModifyConfigurationPanel(wandora, wandora.getOptions(),tm);
        if(modifyPanel==null){
            WandoraOptionPane.showMessageDialog(wandora, "This layer cannot be reconfigured.", null, WandoraOptionPane.WARNING_MESSAGE);
            return;
        }
        modifyContainerPanel.removeAll();
        modifyContainerPanel.add(modifyPanel);
        modifyNameTextField.setText(modifyLayer.getName());
        modifyDialog.setSize(400,350);
        UIBox.centerWindow(modifyDialog, wandora);
        modifyDialog.setVisible(true);
    }
    
    private void modifyCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyCancelButtonActionPerformed
        modifyDialog.setVisible(false);
    }//GEN-LAST:event_modifyCancelButtonActionPerformed

    private void modifyOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifyOKButtonActionPerformed
        Object params=modifyPanel.getParameters();
        try{
            TopicMap tm=modifyType.createTopicMap(params);

            String name=modifyNameTextField.getText().trim();
            if(name.length()==0) {
                WandoraOptionPane.showMessageDialog(wandora, "Enter name for the layer!");
                return;
            }
            for(Layer l : layerStack.getLayers()){
                if(l.getName().equals(name) && l!=modifyLayer) {
                    WandoraOptionPane.showMessageDialog(wandora, "Layer name is already in use!", null, WandoraOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            modifyDialog.setVisible(false);

            layerStack.setLayer(new Layer(tm,name,layerStack),modifyLayer.getZPos());
        }
        catch(TopicMapException tme){
            tme.printStackTrace(); // TODO EXCEPTION
        }
        resetLayers();        
    }//GEN-LAST:event_modifyOKButtonActionPerformed

    
    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        this.requestFocusInWindow();
        Layer selected=layerStack.getSelectedLayer();
        int pos=layerStack.getLayerZPos(selected);
        if(pos<layerStack.getLayers().size()-1){
            layerStack.moveLayer(selected,pos+1);        
            resetLayers();
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        this.requestFocusInWindow();
        Layer selected=layerStack.getSelectedLayer();
        int pos=layerStack.getLayerZPos(selected);
        if(pos>0) {
            layerStack.moveLayer(selected,pos-1);
            resetLayers();
        }
    }//GEN-LAST:event_upButtonActionPerformed
    
    public void appendAmbiguity(String s){
        textArea.append(s+"\n");
    }
    
    boolean layersChanging(){
        if(listenerChanging!=null) return listenerChanging.invoke(null).booleanValue();
        else return true;
    }
    
    public void drawPanelOverlay(java.awt.Graphics g){
        if(draggingLayer!=-1){
            g.setColor(java.awt.Color.BLACK);
            g.drawLine(0, drawDragLine, layerPanel.getWidth(), drawDragLine);
            g.drawLine(0, drawDragLine+1, layerPanel.getWidth(), drawDragLine+1);
            g.drawImage(dragImage,dragMouseX-dragOffsX,dragMouseY-dragOffsY,null);
        }
    }
    
    
    
    public void createLayer() {
        createLayer(null);
    }
    public void createLayer(final Layer container) {
        if(!layersChanging()) return;
        final JDialog jd=new JDialog(wandora,true);
        final LayerStack containerStack=(container==null?wandora.getTopicMap():(LayerStack)container.getTopicMap());
        NewTopicMapPanel p=new NewTopicMapPanel(wandora,new Delegate<Object,NewTopicMapPanel>(){
            public Object invoke(NewTopicMapPanel p){
                try{
                    String name=p.getName().trim();
                    if(name.length()==0) {
                        WandoraOptionPane.showMessageDialog(jd, "You have not given name for the layer. Enter name for the layer!");
                        return null;
                    }
                    for(Layer l : layerStack.getLayers()){
                        if(l.getName().equals(name)) {
                            WandoraOptionPane.showMessageDialog(jd, "Layer name is already in use!", null, WandoraOptionPane.WARNING_MESSAGE);
                            return null;
                        }
                    }
                    TopicMap tm=p.createTopicMap();
                    if(tm==null){
                        WandoraOptionPane.showMessageDialog(jd, "Error creating topic map for the layer!");
                        return null;
                    }
                    tm.resetTopicMapChanged();
//                    layerStack.addLayer(new Layer(tm,name,layerStack));
                    Layer layer=new Layer(tm,name,containerStack);
                    containerStack.addLayer(layer);
                }
                catch(TopicMapException tme){
                    tme.printStackTrace(); // TODO EXCEPTION
                }
                resetLayers();
                jd.setVisible(false);
                return null;
            }
        },
        new Delegate<Object,NewTopicMapPanel>(){
            public Object invoke(NewTopicMapPanel o){
                jd.setVisible(false);
                return null;
            }
        });
        jd.getContentPane().add(p);
        jd.setSize(400,350);
        if(wandora != null) wandora.centerWindow(jd);
        jd.setTitle("Create new layer");
        jd.setVisible(true);
    }
    

    private int makeLayerPanels(final List<Layer> layers,int indent,int grid){
        int originalGrid=grid;
        for(int i=0;i<layers.size();i++){
            final Layer l=layers.get(i);
            final LayerStatusPanel lsp=new LayerStatusPanel(wandora, l,layerStack,this);
            lsp.setIndent(indent);
            final int pos=grid;
            grid++;
            final LayerControlPanel lcp = this;
            
            lsp.addMouseMotionListener(new java.awt.event.MouseMotionListener(){
                public void mouseDragged(java.awt.event.MouseEvent e){
                    int x=lsp.getLocation().x+e.getX();
                    int y=lsp.getLocation().y+e.getY();
                    dragMouseX=x;
                    dragMouseY=y;
                    if(y<0) y=0;
                    java.awt.Component lastComponent=layerPanel.getComponent(layerPanel.getComponentCount()-1);
                    if(y>lastComponent.getLocation().y+lastComponent.getHeight()) y=lastComponent.getLocation().y+lastComponent.getHeight();
                    
                    if(dragOffsX==-1){
                        dragOffsX=e.getX();
                        dragOffsY=e.getY();                        
                    }
                    
                    java.awt.Component c=layerPanel.getComponentAt(0,y);
                    if(c!=null && c instanceof LayerStatusPanel){
                        Layer l=((LayerStatusPanel)c).getLayer();
                        int ind=layers.indexOf(l);
                        if(y>c.getLocation().y+c.getHeight()/2) ind++;
                        int newPos=ind;
                        if(newPos>pos) newPos--;
                        if(newPos!=pos || draggingLayer!=-1){
                            if(draggingLayer==-1){
                                dragImage=new java.awt.image.BufferedImage(lsp.getWidth(),lsp.getHeight(),java.awt.image.BufferedImage.TYPE_INT_ARGB);
                                java.awt.Graphics2D g2=dragImage.createGraphics();
                                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC,0.5f));
                                lsp.paint(g2);
                            }
                            dragPos=newPos;
                            draggingLayer=pos;
                            if(ind==layerPanel.getComponentCount())
                                drawDragLine=lastComponent.getLocation().y+lastComponent.getHeight()-2;
                            else
                                drawDragLine=layerPanel.getComponent(ind).getLocation().y;
                            layerPanel.repaint();
                        }
                    }
                    lcp.requestFocusInWindow();
//                    System.out.println("Dragged "+x+","+y);
                }
                public void mouseMoved(java.awt.event.MouseEvent e){
                }
            });
            lsp.addMouseListener(new java.awt.event.MouseAdapter(){
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e){
                    dragOffsX=-1;
                    dragOffsY=-1;
                    if(draggingLayer!=-1){
                        if(draggingLayer!=dragPos){
                            layerStack.moveLayer(layers.get(draggingLayer),dragPos);
                            draggingLayer=-1;
                            resetLayers();
                        }
                        else {
                            draggingLayer=-1;
                            repaint();
                        }
                    }
                    else{
                        if(layersChanging()){
                            layerStack.selectLayer(l);
                            resetLayers();
                        }
                    }
                    lcp.requestFocusInWindow();
                }
            });

            layerPanel.add(lsp);
            
            if(l.getTopicMap() instanceof LayerStack){
                LayerStack ls=(LayerStack)l.getTopicMap();
                grid+=makeLayerPanels(ls.getLayers(),indent+1,grid);
            }
        }    
        return grid-originalGrid;
    }
    
    public void resetLayers(List<Layer> layers){
        layerPanel.removeAll();
        layerPanel.setLayout(new java.awt.GridLayout(0,1));
        makeLayerPanels(layers,0,0);
        this.revalidate();
        if(listenerChanged!=null) listenerChanged.invoke(null);
    }

    
    public void deleteLayer() {
        if(!layersChanging()) return;
        Layer l=layerStack.getSelectedLayer();
        deleteLayer(l);
    }
    public void deleteLayer(Layer l) {
        if(l!=null){
            int c=WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to delete layer '"+l.getName() + "'?","Confirm layer delete",WandoraOptionPane.YES_NO_OPTION);
            if(c==WandoraOptionPane.YES_OPTION) {
//                try{
                    layerStack.removeLayer(l);
//                }catch(TopicMapException tme){
//                    tme.printStackTrace(); 
//                }
                resetLayers();
            }
        }
    }

    public void resetLayers(){
        resetLayers(layerStack.getLayers());
    }
    
    
    public static final int MOVE_LAYER_UP = 100;
    public static final int MOVE_LAYER_DOWN = 200;
    public static final int MOVE_LAYER_TOP = 300;
    public static final int MOVE_LAYER_BOTTOM = 400;
    public static final int REVERSE_LAYERS = 500;
    
    
    public void arrangeLayers(int options) {
        arrangeLayers(layerStack.getSelectedLayer(), options);
    }

    public void arrangeLayers(Layer selected, int options) {
        if(options == MOVE_LAYER_DOWN) {
            int pos=layerStack.getLayerZPos(selected);
            if(pos<layerStack.getLayers().size()-1){
                layerStack.moveLayer(selected,pos+1);        
                resetLayers();
            }
        }
        
        else if(options == MOVE_LAYER_UP) {
            int pos=layerStack.getLayerZPos(selected);
            if(pos>0) {
                layerStack.moveLayer(selected,pos-1);
                resetLayers();
            }
        }
        
        else if(options == MOVE_LAYER_BOTTOM) {
            int numberOfLayers = layerStack.getLayers().size();
            int pos=layerStack.getLayerZPos(selected);
            if(pos<numberOfLayers-1){
                layerStack.moveLayer(selected,numberOfLayers-1);        
                resetLayers();
            }
        }
        
        else if(options == MOVE_LAYER_TOP) {
            int pos=layerStack.getLayerZPos(selected);
            if(pos>0) {
                layerStack.moveLayer(selected,0);
                resetLayers();
            }
        }
        
        else if(options == REVERSE_LAYERS) {
            layerStack.reverseLayerOrder();
            resetLayers();
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton downButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel layerPanel;
    private javax.swing.JPanel layersPanel;
    private javax.swing.JScrollPane layersScrollPane;
    private javax.swing.JButton modifyCancelButton;
    private javax.swing.JPanel modifyContainerPanel;
    private javax.swing.JDialog modifyDialog;
    private javax.swing.JTextField modifyNameTextField;
    private javax.swing.JButton modifyOKButton;
    private javax.swing.JButton newButton;
    private javax.swing.JTextArea textArea;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables
    
    
    
    public void actionPerformed(ActionEvent event) {
        String actionCommand = event.getActionCommand();
        /*
        if("Delete layer...".equalsIgnoreCase(actionCommand) || "Delete".equalsIgnoreCase(actionCommand)) {
            deleteLayer();
        }
        
        else if("New layer...".equalsIgnoreCase(actionCommand) || "New".equalsIgnoreCase(actionCommand)) {
            createLayer();
        }
        
        else if("Rename layer...".equalsIgnoreCase(actionCommand) ) {
            Layer selected=layerStack.getSelectedLayer();
            LayerStatusPanel statusPanel = getPanelFor(selected);
            if(statusPanel != null) statusPanel.editName();
        }
        
        else if("Configure layer...".equalsIgnoreCase(actionCommand) || "Configure".equalsIgnoreCase(actionCommand)) {
            modifyLayer();
        }
         **/
        System.out.println("Action event '" + actionCommand + "' captured by LayerControlPanel! Should not be here!");
    }
    
    
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
    }
    public void mouseReleased(MouseEvent e) {
        requestFocusInWindow();
    }
    public void mouseClicked(MouseEvent e) {
        requestFocusInWindow();
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
}
