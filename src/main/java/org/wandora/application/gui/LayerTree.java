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
 */


package org.wandora.application.gui;


import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraMenuManager;
import org.wandora.application.WandoraTool;
import org.wandora.application.WandoraToolManager;
import org.wandora.application.gui.simple.SimpleLabelField;
import org.wandora.application.gui.simple.SimpleMenu;
import org.wandora.application.gui.simple.SimpleToggleButton;
import org.wandora.application.tools.ChainExecuter;
import org.wandora.application.tools.importers.TopicMapImport;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapType;
import org.wandora.topicmap.TopicMapTypeManager;
import org.wandora.topicmap.layered.ContainerTopicMap;
import org.wandora.topicmap.layered.Layer;
import org.wandora.topicmap.layered.LayerStack;
import org.wandora.topicmap.memory.TopicMapImpl;
import org.wandora.topicmap.undowrapper.UndoTopicMap;
import org.wandora.utils.Delegate;
import org.wandora.utils.DnDBox;
import org.wandora.utils.swing.DragJTree;
import org.wandora.utils.swing.SwingTools;



/**
 *
 * @author olli
 */
public class LayerTree extends DragJTree {

	
	private static final long serialVersionUID = 1L;
    
    
    private static Color bgColor=UIConstants.defaultInactiveBackground; // new Color(238,238,238);
    private static Color selectedColor=UIConstants.defaultActiveBackground; // new Color(197,197,197);
    private static Color selectedColor2=UIConstants.defaultActiveBackground; // new Color(220,220,220);
    private static Color brokenColor=new Color(250,106,106);
    
    private ContainerTopicMap rootStack;
    private LayerTreeModel model;

    public Delegate<Object,Object> listenerChanged;
    public Delegate<Boolean,Object> listenerChanging;    
    
    private int nonLocalDragRow;
    
    private Wandora wandora;
    
    private Layer lastClickedLayer;
    
    private Object selectedValue;
    
    private boolean disableNotifications=false;

    
    public LayerTree(ContainerTopicMap rootStack) {
        this(rootStack,null);
    }
    public LayerTree(ContainerTopicMap rootStack, Wandora wandora) {
        super();
        this.wandora=wandora;
        this.setUI(new LayerTreeUI());
        nonLocalDragRow=-1;
        this.model=new LayerTreeModel();
        this.rootStack=rootStack;
        this.setModel(model);
        this.setCellRenderer(new LayerCellRenderer());
//        this.setCellEditor(new LayerCellEditor());
//        this.setEditable(true);
        this.setOpaque(false);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.putClientProperty("JTree.lineStyle", "None");
        this.setRootVisible(false);
        
        
        
        this.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){
                refreshTree();
            }
        });
        
        
        
        this.addTreeWillExpandListener(new TreeWillExpandListener(){
            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                throw new ExpandVetoException(event);                
            }
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {}
        });
        
        
        
        this.addTreeExpansionListener(new TreeExpansionListener(){
            @Override
            public void treeCollapsed(TreeExpansionEvent event) {}
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                refreshTree();
            }
        });
        
        
        
        this.addMouseListener(new MouseAdapter(){
            public void checkPopup(MouseEvent e) {
                if(e.isPopupTrigger()) {
                    TreePath path=LayerTree.this.getPathForLocation(e.getX(), e.getY());
                    if(path!=null){
                        openPopupFor(path,e.getSource(),e.getX(),e.getY());
                    }
                    else {
                        openPopupFor(new TreePath(LayerTree.this.rootStack),e.getSource(),e.getX(),e.getY());
                    }
                }
            }
            @Override
            public void mouseClicked(MouseEvent e){
                checkPopup(e);
            }
            @Override
            public void mousePressed(MouseEvent e){
                checkPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e){
                if(e.getButton()==MouseEvent.BUTTON1){
                    handleMouseClick(e);
                }
                else {
                    checkPopup(e);
                }
            }
        });
        
        

        
        this.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener(){
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path=LayerTree.this.getSelectionPath();
                Object newSelected=null;
                if(path!=null) newSelected=path.getLastPathComponent();
                if(newSelected==selectedValue) return;
                if(!notifyChanging()) {
                    setSelectedValue(selectedValue);
                }
                else {
                    selectedValue=newSelected;
                    Layer l=getSelectedLayer();
                    while(l!=null){
                        l.getContainer().selectLayer(l);
                        l=findLayerFor(l.getContainer());                    
                    }
                    notifyChanged();
                    refreshTree();
                }
            }
        });

        expandAll();
        try {
            this.setSelectionRow(rootStack.getSelectedIndex());
        }
        catch(Exception e) {
            this.setSelectionRow(0);
        }
    }
/*    
    protected void notifyTreeChanges(Layer layer){
        TreePath path=findPathFor(layer.getTopicMap());
        Object[] p=path.getPath();
        for(int i=p.length-2;i>=1;i--){
            Layer l=((LayerWrapper)p[i]).layer;
            TopicMap tm=l.getTopicMap();
            if(tm instanceof LayerStack) ((LayerStack)tm).clearTopicIndex();
        }
        rootStack.clearTopicIndex();
    }*/
    
    public ContainerTopicMap getRootStack(){
        return rootStack;
    }
    
    protected void handleMouseClick(MouseEvent e){
        int x=e.getX();
        int y=e.getY();
        int row=this.getRowForLocation(x, y);
        if(row==-1) return;
        Rectangle rect=getRowBounds(row);
        x-=rect.x;
        y-=rect.y;
        
        // See also selectPathForEvent method below at LayerTreeUI.
        if(y>=8 && y<=23) {
            TreePath path=getPathForRow(row);
            if(path==null || path.getPathCount()<=1) return;
            Layer l=((LayerWrapper)path.getLastPathComponent()).layer;
            if(x>=1 && x<=24) {
                if(notifyChanging()) {
                    l.setVisible(!l.isVisible());
                    resetLayers();
                    notifyChanged();
                }
                e.consume();
            }
            else if(x>=30 && x<=45) {
                l.setReadOnly(!l.isReadOnly());
                resetLayers();
                e.consume();                
            }
        }
    }
    
    
    
    public void selectLayer(Layer layer) {
        setSelectionPath(findPathFor(layer));
    }
    
    
    
    protected void setSelectedValue(Object value){
        if(value==null) this.clearSelection();
        if(value==rootStack) this.setSelectionPath(new TreePath(rootStack));
        for(int i=0;i<this.getRowCount();i++){
            TreePath path=this.getPathForRow(i);
            if(path.getLastPathComponent().equals(value)){
                this.setSelectionPath(path);
                break;
            }
        }
    }
    
    public Layer getLastClickedLayer(){
        return lastClickedLayer;
    }
    
    protected void openPopupFor(TreePath path,Object source,int x,int y){
        if(path==null) return;
        JPopupMenu menu=null;
        if(path.getPathCount()>1){
            LayerWrapper wrapper=(LayerWrapper)path.getLastPathComponent();
            lastClickedLayer=wrapper.layer;
            menu=getContextMenuFor(wrapper);
        }
        else {
            lastClickedLayer=getSelectedLayer();
            if(lastClickedLayer!=null){
                LayerWrapper wrapper=(LayerWrapper)findPathFor(lastClickedLayer.getTopicMap()).getLastPathComponent();
                menu=getContextMenuFor(wrapper);
            }
            else{
                menu=getContextMenuFor(rootStack);
                try{
                    lastClickedLayer=new Layer(rootStack,"root",null);
                }catch(TopicMapException tme){tme.printStackTrace();}
            }
        }
        if(menu!=null) menu.show((JComponent)source,x,y);
    }
    
    protected JPopupMenu getContextMenuFor(LayerWrapper layerWrapper){
        if(layerWrapper.cachedMenu!=null) return layerWrapper.cachedMenu;
        Layer layer=layerWrapper.layer;
        layerWrapper.cachedMenu= getContextMenuFor(layer.getTopicMap());
        return layerWrapper.cachedMenu;        
    }
    
    
    
    protected JPopupMenu getContextMenuFor(TopicMap tm){
        Object[] menuStructure = WandoraMenuManager.getLayerTreeMenu();
        Object[] menu = null;
        
        TopicMapType type=TopicMapTypeManager.getType(tm);
        if(type != null) {
            javax.swing.JMenuItem[] m = type.getTopicMapMenu(tm, wandora);
            menu=UIBox.fillMenuTemplate("___TOPICMAPMENU___",m, menuStructure);
        }
        else {
            System.out.println("Warning. LayerTree didn't find layer type (getContextMenuFor).");
            menu=UIBox.fillMenuTemplate("___TOPICMAPMENU___",null, menuStructure);
        }
        
        JMenu importToLayerMenu = new SimpleMenu("Merge to layer", null);
        JMenu generateLayerMenu = new SimpleMenu("Generate to layer", null);
        JMenu exportLayerMenu = new SimpleMenu("Export layer", null);
        wandora.toolManager.getImportMergeMenu(importToLayerMenu);
        wandora.toolManager.getGeneratorMenu(generateLayerMenu);
        wandora.toolManager.getExportMenu(exportLayerMenu);
        
        menu=UIBox.fillMenuTemplate("___IMPORTMENU___", new Object[] { importToLayerMenu }, menu);
        menu=UIBox.fillMenuTemplate("___GENERATEMENU___", new Object[] { generateLayerMenu }, menu);
        menu=UIBox.fillMenuTemplate("___EXPORTMENU___", new Object[] { exportLayerMenu }, menu);
        return UIBox.makePopupMenu(menu, wandora);
    }
    
    
    public Layer getSelectedLayer(){
        if(selectedValue==null || selectedValue==rootStack) return null;
        return ((LayerWrapper)selectedValue).layer;
    }
    
    public boolean isSelectedReadOnly(){
        Layer l=getSelectedLayer();
        if(l==null) return true;
        else return l.isReadOnly();
    }
    
    public String getSelectedName(){
        Layer l=getSelectedLayer();
        if(l==null) return null;
        else return l.getName();
    }
    
    
    public static final int MOVE_LAYER_UP = 100;
    public static final int MOVE_LAYER_DOWN = 200;
    public static final int MOVE_LAYER_TOP = 300;
    public static final int MOVE_LAYER_BOTTOM = 400;
    public static final int REVERSE_LAYERS = 500;
    
    
    public void arrangeLayers(int options) {
        arrangeLayers(getSelectedLayer(), options);
    }

    public void arrangeLayers(Layer selected, int options) {
        if(selected==null) return;
        if(!notifyChanging()) return;
        ContainerTopicMap layerStack=selected.getContainer();
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
    
    public void deleteLayer() {
        deleteLayer(getSelectedLayer());
    }
    public void deleteLayer(Layer l) {
        if(l!=null){
            boolean resetSelection=false;
            int row=-1;
            if(selectedValue!=null && selectedValue instanceof LayerWrapper){
                resetSelection=((LayerWrapper)selectedValue).layer.equals(l);
                row=findRowFor(l.getTopicMap());
            }
            
            if(!notifyChanging()) return;
            int c=WandoraOptionPane.showConfirmDialog(wandora,"Are you sure you want to delete layer '"+l.getName() + "'? Deletion is undoable.","Confirm layer delete",WandoraOptionPane.YES_NO_OPTION);
            if(c==WandoraOptionPane.YES_OPTION) {
                l.getContainer().removeLayer(l);
                resetLayers();
                if(resetSelection) {
                    if(getRowCount()>0){
                        TreePath path=getPathForRow(row<getRowCount()?row:(row-1));
                        if(path!=null) setSelectionPath(path);
                    }
                    else setSelectionPath(null);
                }                
            }
        }
    }
    
    /*
    public void editLayerName(Layer layer){
        throw new RuntimeException("Not implemented");
    }
    */
    
    public void createLayer(ContainerTopicMap container){
        if(!notifyChanging()) return;
        final JDialog jd=new JDialog(wandora,true);
        final ContainerTopicMap containerStack=(container==null?wandora.getTopicMap():container);
        final LayerStack rootMap=wandora.getTopicMap();
        
        NewTopicMapPanel p = new NewTopicMapPanel(wandora, new Delegate<Object,NewTopicMapPanel>() {
            @Override
            public Object invoke(NewTopicMapPanel p) {
                Layer layer = null;
                TopicMap tm = null;
                try {
                    String name=p.getName().trim();
                    if(name.length()==0) {
                        WandoraOptionPane.showMessageDialog(jd, "You have not given name for the layer. Enter name for the layer!");
                        return null;
                    }
                    for(Layer l : rootMap.getTreeLayers()) {
                        if(l.getName().equals(name)) {
                            WandoraOptionPane.showMessageDialog(jd, "Layer name is already in use!", null, WandoraOptionPane.WARNING_MESSAGE);
                            return null;
                        }
                    }
                    tm=p.createTopicMap();
                    if(tm==null){
                        WandoraOptionPane.showMessageDialog(jd, "Error creating topic map for the layer!");
                        return null;
                    }
                    tm.resetTopicMapChanged();
//                    layerStack.addLayer(new Layer(tm,name,layerStack));
                    layer=new Layer(tm,name,containerStack);
                    containerStack.addLayer(layer);
                }
                catch(TopicMapException tme){
                    tme.printStackTrace(); // TODO EXCEPTION
                }
                resetLayers();
                jd.setVisible(false);
                expandAll();
                if(containerStack==rootStack && rootStack.getNumLayers()==1){
                    setSelectionPath(new TreePath(new Object[]{rootStack, new LayerWrapper(layer)}));
                }
                if(tm != null) {
                    setSelectionPath(findPathFor(tm));
                }
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
        jd.setSize(600,550);
        if(wandora != null) wandora.centerWindow(jd);
        jd.setTitle("Create new layer");
        jd.setVisible(true);
    }
    
    
    
    
    public ArrayList<Layer> getAllLayers(){
        ArrayList<Layer> ret=new ArrayList<Layer>();
        for(int i=0;i<this.getRowCount();i++){
            TreePath path=this.getPathForRow(i);
            if(path.getPathCount()<=1) continue;
            ret.add(((LayerWrapper)path.getLastPathComponent()).layer);
        }
        return ret;
    }
    public TreePath findPathFor(Layer layer){
        for(int i=0;i<this.getRowCount();i++){
            TreePath path=this.getPathForRow(i);
            if(path.getPathCount()<=1) continue;
            Layer l=((LayerWrapper)path.getLastPathComponent()).layer;
            if(layer == l) return path;
        }
        return null;        
    }
    public TreePath findPathFor(TopicMap tm){
        for(int i=0;i<this.getRowCount();i++){
            TreePath path=this.getPathForRow(i);
            if(path.getPathCount()<=1) continue;
            Layer l=((LayerWrapper)path.getLastPathComponent()).layer;
            TopicMap ltm = l.getTopicMap();
            if(ltm==tm) return path;
            if(ltm instanceof UndoTopicMap) {
                ltm = ((UndoTopicMap) ltm).getWrappedTopicMap();
                if(ltm==tm) return path;
            }
        }
        return null;        
    }
    public Layer findLayerFor(TopicMap tm){
        for(int i=0;i<this.getRowCount();i++){
            TreePath path=this.getPathForRow(i);
            if(path.getPathCount()<=1) continue;
            Layer l=((LayerWrapper)path.getLastPathComponent()).layer;
            TopicMap ltm = l.getTopicMap();
            if(ltm==tm) return l;
            if(ltm instanceof UndoTopicMap) {
                ltm = ((UndoTopicMap) ltm).getWrappedTopicMap();
                if(ltm==tm) return l;
            }
        }
        return null;        
    }
    public int findRowFor(TopicMap tm){
        for(int i=0;i<this.getRowCount();i++){
            TreePath path=this.getPathForRow(i);
            if(path.getPathCount()<=1) continue;
            Layer l=((LayerWrapper)path.getLastPathComponent()).layer;
            TopicMap ltm = l.getTopicMap();
            if(ltm==tm) return i;
            if(ltm instanceof UndoTopicMap) {
                ltm = ((UndoTopicMap) ltm).getWrappedTopicMap();
                if(ltm==tm) return i;
            }
        }
        return -1;        
    }
    
    public void setChangedListener(Delegate<Object,Object> listener){
        this.listenerChanged=listener;
    }
    public void setChangingListener(Delegate<Boolean,Object> listener){
        this.listenerChanging=listener;
    }
    
    protected boolean notifyChanging(){
        if(!disableNotifications && listenerChanging!=null) {
            return listenerChanging.invoke(null);
        }
        else return true;
    }
    
    protected void notifyChanged(){
        if(!disableNotifications && listenerChanged!=null) {
            listenerChanged.invoke(null);
        }
    }
    
    
    
    public void refreshTree(){
        for(int i=0;i<this.getRowCount();i++){
            TreePath path=this.getPathForRow(i);
            TreePath parent=path.getParentPath();
            if(parent==null || parent.getPathCount()==0){
                model.fireTreeNodesChanged(new TreeModelEvent(this,path));                
            }
            else{
                int index=treeModel.getIndexOfChild(parent.getLastPathComponent(), path.getLastPathComponent());
                if(index!=-1) model.fireTreeNodesChanged(new TreeModelEvent(this,parent,new int[]{index},new Object[]{path.getLastPathComponent()}));
            }
        }
        repaint();
    }
    
    public void modifyLayer() {
        modifyLayer(getSelectedLayer());
    }
    
    public void modifyLayer(Layer modifyLayer) {
        int layerZPos=-1;
        ContainerTopicMap container=modifyLayer.getContainer();
        if(getSelectedLayer()==modifyLayer && container!=null) {
            layerZPos=modifyLayer.getZPos();
        }
        ModifyLayerDialog d=new ModifyLayerDialog(wandora,modifyLayer);
        d.setVisible(true);
        resetLayers();
        if(layerZPos!=-1){
            Layer l=container.getLayers().get(layerZPos);
            setSelectedValue(new LayerWrapper(l));
        }
    }
    
    
    public void resetLayers(){
        final TreePath selection=getSelectionPath();
        SwingTools.swingOperationBlock(new Runnable(){public void run(){
            model.fireTreeStructureChanged(new TreeModelEvent(this,new TreePath(rootStack)));
            expandAll();
            LayerTree.this.setSelectionPath(selection);
            notifyChanged();
        }});
    }
    
    public ArrayList<String> treeToNamePath(TreePath path){
        ArrayList<String> names=new ArrayList<String>();
        names.add("ROOT");
        Object[] os=path.getPath();
        for(int i=1;i<os.length;i++){
            LayerWrapper wrapper=(LayerWrapper)os[i];
            names.add(wrapper.layer.getName());
        }
        return names;
    }
    public TreePath nameToTreePath(ArrayList<String> path){
        Object[] os=new Object[path.size()];
        os[0]=rootStack;
        ContainerTopicMap lastStack=rootStack;
        for(int i=1;i<path.size();i++){
            String name=path.get(i);
            Layer layer=lastStack.getLayer(name);
            if(layer==null) return null;
            os[i]=new LayerWrapper(layer);
            TopicMap tm=layer.getTopicMap();
            if(!isContainerTopicMap(tm)) return null;
            lastStack=getContainerTopicMap(tm);
        }
        return new TreePath(os);
    }
    public ArrayList<ArrayList<String>> getAllExpandedPaths() {
        return getAllExpandedPaths(new TreePath(rootStack));
    }
    public ArrayList<ArrayList<String>> getAllExpandedPaths(TreePath root) {
        ArrayList<ArrayList<String>> expandedPaths=new ArrayList<ArrayList<String>>();
        Enumeration<TreePath> e=this.getExpandedDescendants(root);
        if(e==null) return expandedPaths;
        while(e.hasMoreElements()){
            TreePath tp=e.nextElement();
            expandedPaths.add(treeToNamePath(tp));
            expandedPaths.addAll(getAllExpandedPaths(tp));
        }
        return expandedPaths;
    }
    
    public void expandPath(ArrayList<String> path){
        TreePath treePath=nameToTreePath(path);
        if(treePath!=null) expandPath(treePath);
    }
    
    public void expandAll(){
        expandAll(new TreePath(rootStack));
    }
    protected void expandAll(TreePath path){
        expandPath(path);
        Object last=path.getLastPathComponent();
        int children=model.getChildCount(last);
        for(int i=0;i<children;i++){
            Object child=model.getChild(last, i);
            expandAll(path.pathByAddingChild(child));
        }
    }
    
    
    
    
    private boolean isContainerTopicMap(TopicMap tm) {
        if(tm != null) {
            if(tm instanceof ContainerTopicMap) return true;
            if(tm instanceof UndoTopicMap) {
                TopicMap wrapped = ((UndoTopicMap) tm).getWrappedTopicMap();
                if(wrapped != null && wrapped instanceof ContainerTopicMap) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private ContainerTopicMap getContainerTopicMap(TopicMap tm) {
        if(tm != null) {
            if(tm instanceof ContainerTopicMap) return (ContainerTopicMap) tm;
            if(tm instanceof UndoTopicMap) {
                TopicMap wrapped = ((UndoTopicMap) tm).getWrappedTopicMap();
                if(wrapped != null && wrapped instanceof ContainerTopicMap) {
                    return (ContainerTopicMap) wrapped;
                }
            }
        }
        return null;
    }
    
    


    // -------------------------------------------------------------------------
    // ----------------------------------------------------------------- DND ---
    // -------------------------------------------------------------------------



    public int allowNonLocalDrop(DropTargetEvent e){
        DataFlavor fileListFlavor = DataFlavor.javaFileListFlavor;
        DataFlavor stringFlavor = DataFlavor.stringFlavor;
        DataFlavor topicFlavor = DnDHelper.topicDataFlavor;
        if(e instanceof DropTargetDropEvent){
            DropTargetDropEvent ev=(DropTargetDropEvent)e;
            if(ev.isDataFlavorSupported(fileListFlavor) || 
                    ev.isDataFlavorSupported(stringFlavor) ||
                    ev.isDataFlavorSupported(DnDBox.uriListFlavor) ||
                    ev.isDataFlavorSupported(topicFlavor)) {
                return DnDConstants.ACTION_COPY;
            }        
        }
        else if(e instanceof DropTargetDragEvent){
            DropTargetDragEvent ev=(DropTargetDragEvent)e;
/*
            DataFlavor[] flavors=ev.getCurrentDataFlavors();
            System.out.println("------------------");
            for(int i=0;i<flavors.length;i++){
                System.out.println(flavors[i].getMimeType());
            }
*/
            if(ev.isDataFlavorSupported(fileListFlavor) || 
                    ev.isDataFlavorSupported(stringFlavor) ||
                    ev.isDataFlavorSupported(DnDBox.uriListFlavor) ||
                    ev.isDataFlavorSupported(topicFlavor)) {
                return DnDConstants.ACTION_COPY;
            }                    
        }
        return DnDConstants.ACTION_NONE;
    }
    
    @Override
    public void nonLocalDrop(DropTargetDropEvent dtde){
        int action=allowNonLocalDrop(dtde);
        if(action==DnDConstants.ACTION_NONE || nonLocalDragRow==-1 || !notifyChanging()){
            dtde.rejectDrop();
            return;
        }
        if(dtde.getTransferable().isDataFlavorSupported(DnDHelper.topicDataFlavor)) {
            try {
                TopicMap source = wandora.getTopicMap();
                Object o=getPathForRow(nonLocalDragRow).getLastPathComponent();
                Layer l=((LayerWrapper)o).layer;
                TopicMap target = l.getTopicMap();
                
                if(source != null && target != null) {
                    List<Topic> topics=DnDHelper.getTopicList(dtde.getTransferable(), source, true);
                    if(topics==null) return;
                    for(Topic t : topics) {
                        if(t != null && !t.isRemoved()) {
                            //target.copyTopicIn(t, false);
                            Topic nt = target.createTopic();
                            nt.addSubjectIdentifier(t.getOneSubjectIdentifier());
                        }
                    }
                    wandora.doRefresh();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }


        else {
            try {
                List<File> files=DnDBox.acceptFileList(dtde);
                if(files==null) dtde.rejectDrop();
                else{
                    boolean newLayer=false;
                    ArrayList<WandoraTool> importTools=null;
                    Object context=this;
                    if(nonLocalDragRow<this.getRowCount()){
                        importTools=WandoraToolManager.getImportTools(files, TopicMapImport.TOPICMAP_DIRECT_MERGE);
                        Object o=getPathForRow(nonLocalDragRow).getLastPathComponent();
                        context=((LayerWrapper)o).layer;
                    }
                    else {
                        importTools=WandoraToolManager.getImportTools(files, TopicMapImport.TOPICMAP_MAKE_NEW_LAYER);
                        newLayer=true;
                    }
                    ActionEvent fakeEvent = new ActionEvent(context, 0, "merge");
                    ChainExecuter chainExecuter = new ChainExecuter(importTools);
                    try{
                        chainExecuter.execute(wandora, fakeEvent);

                        if(newLayer){
                            int newIndex=rootStack.getLayers().size()-1;
                            Object newChild=model.getChild(rootStack, newIndex);
                            model.fireTreeNodesInserted(new TreeModelEvent(this,new TreePath(rootStack),new int[]{newIndex},new Object[]{newChild}));
                        }

                    }
                    catch(TopicMapException tme){
                        wandora.handleError(tme);
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        nonLocalDragRow=-1;
        expandAll();
        repaint();
        notifyChanged();
    }


    @Override
    public void nonLocalDragEnter(DropTargetDragEvent dtde){
        nonLocalDragOver(dtde);
    }


    @Override
    public void nonLocalDragOver(DropTargetDragEvent dtde){
        int action=allowNonLocalDrop(dtde);
        if(action==DnDConstants.ACTION_NONE){
            dtde.rejectDrag();
            return;
        }
        dtde.acceptDrag(action);
        Point p=dtde.getLocation();
        int old=nonLocalDragRow;
        nonLocalDragRow=this.getRowForLocation(p.x, p.y);
        if(nonLocalDragRow==-1) nonLocalDragRow=this.getRowCount();
        if(old!=nonLocalDragRow) repaint();
    }
    @Override
    public void nonLocalDragExit(DropTargetEvent dtde){
        nonLocalDragRow=-1;
        repaint();
    }



    @Override
    public void nonLocalDropActionChanged(DropTargetDragEvent dtde) {
        int action=allowNonLocalDrop(dtde);
        if(action==DnDConstants.ACTION_NONE){
            dtde.rejectDrag();
            return;
        }
    }



    @Override
    public int allowDrop(TreePath destinationParent, TreePath destinationPosition, TreePath source) {
        if(destinationParent==null) return DnDConstants.ACTION_NONE;
        Object last=destinationParent.getLastPathComponent();
        if(last==rootStack) return DnDConstants.ACTION_MOVE;
        if(destinationParent.getPathCount()>=source.getPathCount() &&
                destinationParent.getPathComponent(source.getPathCount()-1).equals(source.getLastPathComponent()))
                    return DnDConstants.ACTION_NONE;
        LayerWrapper wrapper=(LayerWrapper)last;
        TopicMap tm=wrapper.layer.getTopicMap();
        if(isContainerTopicMap(tm)) return DnDConstants.ACTION_MOVE;
        return DnDConstants.ACTION_NONE;
    }



    @Override
    public void doDrop(TreePath destinationParent, TreePath destinationPosition, TreePath source, int action) {
        if(action!=DnDConstants.ACTION_MOVE) return;
        
        if(!notifyChanging()) return;
        disableNotifications=true;
        
        ContainerTopicMap oldParent=null;
        ContainerTopicMap newParent=null;
        
        Object last=source.getParentPath().getLastPathComponent();
        if(last==rootStack) oldParent=rootStack;
        else oldParent=(ContainerTopicMap)((LayerWrapper)last).layer.getTopicMap();
        int oldIndex=model.getIndexOfChild(last,source.getLastPathComponent());
        
        last=destinationParent.getLastPathComponent();
        if(last==rootStack) newParent=rootStack;
        else newParent=(ContainerTopicMap)((LayerWrapper)last).layer.getTopicMap();
        
        Layer layer=((LayerWrapper)source.getLastPathComponent()).layer;
        
        try {
//            notifyTreeChanges(layer);
            model.fireTreeNodesRemoved(new TreeModelEvent(this,source.getParentPath(),new int[]{oldIndex},new Object[]{source.getLastPathComponent()}));
            oldParent.removeLayer(layer);
            
            Layer newLayer=new Layer(layer.getTopicMap(),layer.getName(),newParent);
            newLayer.setReadOnly(layer.isReadOnly());
            newLayer.setVisible(layer.isVisible());
            newLayer.setColor(layer.getColor());
            int newIndex=0;
            if(destinationPosition!=null) newIndex=model.getIndexOfChild(destinationParent.getLastPathComponent(), destinationPosition.getLastPathComponent())+1;
            newParent.addLayer(newLayer,newIndex);
            LayerWrapper newWrapper=new LayerWrapper(newLayer);
            model.fireTreeNodesInserted(new TreeModelEvent(this,destinationParent,new int[]{newIndex},new Object[]{newWrapper}));
            model.fireTreeNodesChanged(new TreeModelEvent(this,destinationParent,new int[]{newIndex},new Object[]{newWrapper}));

            setSelectionPath(destinationParent.pathByAddingChild(newWrapper));
            
            expandAll();
            disableNotifications=false;
//            notifyTreeChanges(newLayer);
            notifyChanged();
            
        }
        catch(TopicMapException tme){
            tme.printStackTrace();
        }
    }




    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------



    
    @Override
    public void paint(Graphics g){
        Graphics2D g2=(Graphics2D)g;
        g2.setColor(bgColor);
//        g2.fillRect(0,0,this.getWidth(),this.getHeight());
        
        Layer selection=getSelectedLayer();
        if(selection!=null){
            TreePath path=findPathFor(selection.getTopicMap());
/*            for(int i=1;i<path.getPathCount()-1;i++){
                Layer l=((LayerWrapper)path.getPathComponent(i)).layer;
                TreePath p=findPathFor(l.getTopicMap());
                Rectangle rect=getPathBounds(p);
                g2.setColor(selectedColor2);
                g2.fillRect(0,rect.y,this.getWidth(),rect.height);                                
            }*/
/*            {
                Rectangle rect=getPathBounds(path);
                g2.setColor(selectedColor);
                if(rect!=null) g2.fillRect(0,rect.y,this.getWidth(),rect.height);                                
            }*/
 /*           TopicMap tm=selection.getTopicMap();
            while(tm instanceof LayerStack){
                Layer l=((LayerStack)tm).getSelectedLayer();
                if(l==null) break;
                TreePath p=findPathFor(l.getTopicMap());
                if(p==null) break;
                Rectangle rect=getPathBounds(p);
                g2.setColor(selectedColor2);
                g2.fillRect(0,rect.y,this.getWidth(),rect.height);
                tm=l.getTopicMap();
            }*/
        }
        
/*        
        for(int i=0;i<this.getRowCount();i++){
            TreePath row=this.getPathForRow(i);
            if(row==null || row.getPathCount()<=1) continue;
            Layer rowLayer=((LayerWrapper)row.getLastPathComponent()).layer;
            if(rowLayer.getBroken()) {
                Rectangle rect=getRowBounds(i);
                g2.setColor(brokenColor);
                g2.fillRect(0,rect.y,this.getWidth(),rect.height);                
            }
        }*/
/*        
        int[] selection=this.getSelectionRows();
        if(selection!=null && selection.length>0){
            int selected=selection[0];
            Rectangle rect=getRowBounds(selected);
            g2.setColor(selectedColor);
            g2.fillRect(0,rect.y,this.getWidth(),rect.height);
        }*/
        
        super.paint(g);
        
        if(nonLocalDragRow!=-1){
            if(nonLocalDragRow<this.getRowCount()){
                Rectangle rect=getRowBounds(nonLocalDragRow);
                g2.setColor(Color.BLACK);
                g2.setStroke(new java.awt.BasicStroke(2));
                g2.drawRect(rect.x, rect.y, rect.width, rect.height);
            }
            else{
                LayerTreeUI tui=(LayerTreeUI)this.getUI();
                int indent=(this.isRootVisible()?tui.getChildIndent():0);
                Rectangle rect=getRowBounds(this.getRowCount()-1);
                g2.setColor(Color.BLACK);
                g2.setStroke(new java.awt.BasicStroke(2));
                g2.drawRect(indent, rect.y+rect.height, this.getWidth()-indent, rect.height);
            }
        }
    }

    
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    
    
    
    private class LayerTreeModel implements TreeModel {

        public ArrayList<TreeModelListener> listeners;
        
        public LayerTreeModel(){
            listeners=new ArrayList<TreeModelListener>();
        }
        
        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }
        
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }        
        
        public void fireTreeNodesChanged(TreeModelEvent e){
            for(TreeModelListener l : listeners) l.treeNodesChanged(e);
        }
        public void fireTreeNodesInserted(TreeModelEvent e){
            for(TreeModelListener l : listeners) l.treeNodesInserted(e);
        }
        public void fireTreeNodesRemoved(TreeModelEvent e){
            for(TreeModelListener l : listeners) l.treeNodesRemoved(e);
        }
        public void fireTreeStructureChanged(TreeModelEvent e){
            for(TreeModelListener l : listeners) l.treeStructureChanged(e);
        }

        @Override
        public Object getChild(Object parent, int index) {
            if(parent==rootStack){
                List<Layer> layers=rootStack.getLayers();
                if(index==-1){
                    int dummy=1;
                }
                if(index<layers.size() && index>=0) return new LayerWrapper(layers.get(index));
                else return null;
            }
            else{
                if(!(parent instanceof LayerWrapper)) return null;
                LayerWrapper wrapper=(LayerWrapper)parent;
                TopicMap tm=wrapper.layer.getTopicMap();
                if(!isContainerTopicMap(tm)) return null;
                List<Layer> layers=getContainerTopicMap(tm).getLayers();
                if(index<layers.size() && index>=0) return new LayerWrapper(layers.get(index));
                else return null;
            }
        }

        @Override
        public int getChildCount(Object parent) {
            if(parent==rootStack) return rootStack.getLayers().size();
            else{
                LayerWrapper wrapper=(LayerWrapper)parent;
                TopicMap tm=wrapper.layer.getTopicMap();
                if(isContainerTopicMap(tm)) return getContainerTopicMap(tm).getLayers().size();
                else return 0;
            }
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if(parent==null || child==null) 
                return -1;
            if(!(child instanceof LayerWrapper)) 
                return -1;
            LayerWrapper childWrapper=(LayerWrapper)child;
            if(parent==rootStack){
                List<Layer> layers=rootStack.getLayers();
                for(int i=0;i<layers.size();i++){
                    Layer l=layers.get(i);
                    if(l==childWrapper.layer) return i;
                }
                return -1;
            }
            else{
                if(!(parent instanceof LayerWrapper)) 
                    return -1;
                LayerWrapper parentWrapper=(LayerWrapper)parent;
                TopicMap tm=parentWrapper.layer.getTopicMap();
                if(!isContainerTopicMap(tm)) 
                    return -1;
                List<Layer> layers=getContainerTopicMap(tm).getLayers();
                for(int i=0;i<layers.size();i++){
                    Layer l=layers.get(i);
                    if(l==childWrapper.layer) return i;
                }
                return -1;
            }
        }

        @Override
        public Object getRoot() {
            return rootStack;
        }

        @Override
        public boolean isLeaf(Object node) {
            if(node==rootStack){
                return rootStack.getLayers().isEmpty();
            }
            else{
                LayerWrapper wrapper=(LayerWrapper)node;
                TopicMap tm=wrapper.layer.getTopicMap();
                if(!isContainerTopicMap(tm)) return true;
                return getContainerTopicMap(tm).getLayers().isEmpty();
            }
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }
        
    }
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    public static class LayerWrapper {
        public Layer layer;
        public JPopupMenu cachedMenu=null;
        public LayerWrapper(Layer layer){
            this.layer=layer;
        }
        @Override
        public String toString(){
            return layer.getName();
        }
        @Override
        public int hashCode(){
            return layer.hashCode();
        }
        @Override
        public boolean equals(Object o){
            if(o!=null && o instanceof LayerWrapper){
                return layer.equals(((LayerWrapper)o).layer);
            }
            else return false;
        }
    }
    
    private void initCellRenderer(CellRenderComponent c,Object value,int row){
        if(value==rootStack) {
            c.nameLabel.setText("ROOT");
            c.initInfoIcons(rootStack);
            c.visibleToggle.setSelected(true);
            c.lockToggle.setSelected(false);
        }
        else {
            Layer layer=((LayerWrapper)value).layer;
            TreePath path=findPathFor(layer.getTopicMap());
            TreePath selectedPath=null;
            Layer selectedLayer=null;
            if(selectedValue!=null) {
                selectedLayer=((LayerWrapper)selectedValue).layer;
                selectedPath=findPathFor(selectedLayer.getTopicMap());
            }
            if(layer.getBroken()) c.setBackground(brokenColor);
            else if(selectedValue!=null && ((LayerWrapper)selectedValue).layer.equals(layer))
                c.setBackground(selectedColor);
            else if(path!=null && selectedPath!=null && path.isDescendant(selectedPath) )
                c.setBackground(selectedColor2);
            else {
                Color bg=null;
                if(selectedPath!=null && path!=null && selectedPath.isDescendant(path)){
                    TopicMap tm=selectedLayer.getTopicMap();
                    while(isContainerTopicMap(tm)) {
                        Layer l=getContainerTopicMap(tm).getSelectedLayer();
                        if(l.equals(layer)) {
                            bg=selectedColor2;
                            break;
                        }
                        tm=l.getTopicMap();
                    }
                }
                if(bg==null) bg=bgColor;
                c.setBackground(bg);
            }
            c.nameLabel.setText( layer.getName() );
            c.initInfoIcons(layer.getTopicMap());
            c.visibleToggle.setSelected(layer.isVisible());
            c.lockToggle.setSelected(layer.isReadOnly());
        }        
        TreePath path=getPathForRow(row);
        if(path!=null){
            LayerTreeUI tui=(LayerTreeUI)this.getUI();
            int indent=tui.getChildIndent()*(path.getPathCount()-(this.isRootVisible()?1:2));
//            int indent=20*(path.getPathCount()-1);
            Dimension d=new Dimension(this.getWidth()-indent,c.getPreferredSize().height);
            c.setMinimumSize(d);
            c.setPreferredSize(d);
            c.setMaximumSize(d);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    private class LayerCellEditor extends AbstractCellEditor implements TreeCellEditor {
        private CellRenderComponent c;
        
        public LayerCellEditor(){
            c=new CellRenderComponent();
        }
        
        public Object getClickedLayer(EventObject anEvent){
            if(anEvent instanceof MouseEvent){
                MouseEvent m=(MouseEvent)anEvent;
                TreePath path=LayerTree.this.getPathForLocation(m.getX(), m.getY());
                if(path==null) return null;
                Object last=path.getLastPathComponent();
                return last;
            }
            else return null;            
        }
        
        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            initCellRenderer(c,value,row);
            c.layer=value;
            return c;
        }

        @Override
        public Object getCellEditorValue() {
            return c.layer;
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            Object value=getClickedLayer(anEvent);
            if(value==null || value==rootStack) return false;
            else return true;
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }
        
        
    }
    
    private class LayerCellRenderer implements TreeCellRenderer {
        private CellRenderComponent c;
        public LayerCellRenderer(){
            c=new CellRenderComponent();
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            initCellRenderer(c,value,row);
            return c;
        }
        
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    private class CellRenderComponent extends JPanel {
        public Object layer;
        
        public javax.swing.JPanel dragPanel;
        public javax.swing.JPanel indentPanel;
        public javax.swing.JPanel infoPanel;
        public javax.swing.JToggleButton lockToggle;
        public SimpleLabelField nameLabel;
        public javax.swing.JToggleButton visibleToggle;
        
        public static final String VISIBLE_ICON = "gui/icons/view.png";
        public static final String INVISIBLE_ICON = "gui/icons/view_no.png";

        public static final String LOCKED_ICON = "gui/icons/locked.png";
        public static final String UNLOCKED_ICON = "gui/icons/unlocked.png";

        public static final String TOPICMAP_CHANGED = "gui/icons/layerinfo/topicmap_changed.png";
        
        public CellRenderComponent(){
            initComponents();
        }
        public void initInfoIcons(TopicMap topicMap) {
            infoPanel.removeAll();
            JLabel iconLabel = null;

            boolean changed=false;
            try {
                changed=topicMap.isTopicMapChanged();
            } 
            catch(TopicMapException tme) {
                tme.printStackTrace(); // TODO EXCEPTION
            }

            if(changed) {
                //iconLabel = new JLabel(UIBox.getIcon(TOPICMAP_CHANGED));
                //iconLabel.setToolTipText("Layer's topic map has changed!");
                //infoPanel.add(iconLabel);
            }

            TopicMapType type=TopicMapTypeManager.getType(topicMap);
            if(type!=null){
                Icon icon=type.getTypeIcon();
                //String name=type.getTypeName();
                if(icon!=null){
                    iconLabel = new JLabel(icon);
                    // tooltip doesn't work in a JTree
                    //if(name!=null) iconLabel.setToolTipText("Layer contains a "+name.toLowerCase()+" topic map!");
                    infoPanel.add(iconLabel);                    
                }
            }
        }
        
        
        
        private void initComponents() {
            java.awt.GridBagConstraints gridBagConstraints;

            this.setOpaque(true);
            
            dragPanel = new JPanel();//new JPanelWithBackground();
            indentPanel = new javax.swing.JPanel();
            visibleToggle = new SimpleToggleButton(VISIBLE_ICON, INVISIBLE_ICON, true);
            lockToggle = new SimpleToggleButton(LOCKED_ICON, UNLOCKED_ICON, false);
            nameLabel = new SimpleLabelField();
            infoPanel = new javax.swing.JPanel();

            setMaximumSize(new java.awt.Dimension(2147483647, 30));
            setMinimumSize(new java.awt.Dimension(76, 30));
            setPreferredSize(new java.awt.Dimension(176, 30));
            setLayout(new java.awt.BorderLayout());

            dragPanel.setOpaque(false);
            dragPanel.setInheritsPopupMenu(true);
            dragPanel.setLayout(new java.awt.GridBagLayout());

            indentPanel.setOpaque(false);
            indentPanel.setLayout(new java.awt.GridBagLayout());
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
            dragPanel.add(indentPanel, gridBagConstraints);

            visibleToggle.setActionCommand("toggleVisibility");
            visibleToggle.setBorder(null);
            visibleToggle.setMargin(new java.awt.Insets(0, 0, 0, 0));
            visibleToggle.setPreferredSize(new java.awt.Dimension(16, 16));
//            visibleToggle.addActionListener(this);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(1, 8, 1, 1);
            dragPanel.add(visibleToggle, gridBagConstraints);
            
            visibleToggle.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(layer!=null && layer instanceof LayerWrapper){
                        if(!notifyChanging()){
                            visibleToggle.setSelected(!visibleToggle.isSelected());
                        }
                        else {
                            ((LayerWrapper)layer).layer.setVisible(visibleToggle.isSelected());
//                            notifyTreeChanges(((LayerWrapper)layer).layer);
                            notifyChanged();
                        }
                    }
                }
            });

            lockToggle.setActionCommand("toggleLock");
            lockToggle.setBorder(null);
            lockToggle.setMargin(new java.awt.Insets(0, 0, 0, 0));
            lockToggle.setPreferredSize(new java.awt.Dimension(16, 16));
//            lockToggle.addActionListener(this);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(1, 4, 1, 1);
            dragPanel.add(lockToggle, gridBagConstraints);
            lockToggle.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(layer!=null && layer instanceof LayerWrapper){
                        if(!notifyChanging()) {
                            lockToggle.setSelected(((LayerWrapper)layer).layer.isReadOnly());
                        }
                        else {
                            ((LayerWrapper)layer).layer.setReadOnly(lockToggle.isSelected());
                            notifyChanged();
                        }
                    }
                }
            });

            nameLabel.setOpaque(false);
            nameLabel.setInheritsPopupMenu(true);
            nameLabel.setMinimumSize(new java.awt.Dimension(10, 20));
            nameLabel.setPreferredSize(new java.awt.Dimension(10, 20));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 1);
            dragPanel.add(nameLabel, gridBagConstraints);

            infoPanel.setOpaque(false);
            infoPanel.setPreferredSize(new java.awt.Dimension(40, 16));
            infoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(1, 1, 1, 2);
            dragPanel.add(infoPanel, gridBagConstraints);

            add(dragPanel, java.awt.BorderLayout.CENTER);
        }
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    
    
    public static class LayerTreeUI extends javax.swing.plaf.metal.MetalTreeUI {
        public LayerTreeUI(){
            super();
        }
        public int getChildIndent(){
            return totalChildIndent;
        }
        @Override
        public Icon getExpandedIcon(){return null;}
        @Override
        public Icon getCollapsedIcon(){return null;}
        @Override
        protected CellRendererPane createCellRendererPane(){
            return new CellRendererPane(){
                @Override
                public void paintComponent(Graphics g, Component c, java.awt.Container p, int x, int y, int w, int h, boolean shouldValidate) {
                    super.paintComponent(g,c,p,x,y,w,h,shouldValidate);
                    g.setColor(c.getBackground());
                    g.fillRect(0,y,x,h);
                    
                    if(c.getBackground().equals(UIConstants.defaultActiveBackground)) {
                        g.setColor(c.getBackground().brighter());
                        g.drawRect(0,y,w+x,0);
                        g.setColor(c.getBackground().darker());
                        g.drawRect(0,y+h-1,w+x,0);
                    }
                }
            };
        }
        
        
        @Override
        protected void selectPathForEvent(TreePath path, MouseEvent event) {
            try {
                // Filter out mouse clicks on layer visibility and lock icons!
                int x = event.getPoint().x;
                if(x<45) return;
            }
            catch(Exception e) {}
            super.selectPathForEvent(path, event);
        }
    }
    
    
    
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static void main(String[] args) {
        try{
            final JFrame w=new JFrame();
            LayerStack stack=new LayerStack();
            stack.addLayer(new Layer(new TopicMapImpl(),"L1",stack));
            LayerStack stackB=new LayerStack();
            stackB.addLayer(new Layer(new TopicMapImpl(),"L2A",stackB));
            stackB.addLayer(new Layer(new TopicMapImpl(),"L2B",stackB));
            stack.addLayer(new Layer(stackB,"L2",stack));
            stack.addLayer(new Layer(new TopicMapImpl(),"L3",stack));        
            w.add(new LayerTree(stack));
            w.pack();
            w.setSize(400,400);
            w.addWindowListener(new WindowAdapter(){
                @Override
                public void windowClosing(WindowEvent e){
                    w.setVisible(false);
                    w.dispose();
                    System.exit(0);
                }
            });
            w.setVisible(true);        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
