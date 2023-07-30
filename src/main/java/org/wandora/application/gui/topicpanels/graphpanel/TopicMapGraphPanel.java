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
 * TopicMapGraphPanel.java
 *
 * Created on 4.6.2007, 11:57
 */

package org.wandora.application.gui.topicpanels.graphpanel;


import static org.wandora.utils.Tuples.t2;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.wandora.application.RefreshListener;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraMenuManager;
import org.wandora.application.contexts.GraphAllNodesContext;
import org.wandora.application.gui.DnDHelper;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.topicpanels.graphpanel.mousetools.DragNodeMouseTool;
import org.wandora.application.gui.topicpanels.graphpanel.mousetools.DrawAssociationMouseTool;
import org.wandora.application.gui.topicpanels.graphpanel.mousetools.EraserTool;
import org.wandora.application.gui.topicpanels.graphpanel.mousetools.ExpandMouseTool;
import org.wandora.application.gui.topicpanels.graphpanel.mousetools.MenuMouseTool;
import org.wandora.application.gui.topicpanels.graphpanel.mousetools.OpenTopicMouseTool;
import org.wandora.application.gui.topicpanels.graphpanel.mousetools.RotateMouseTool;
import org.wandora.application.gui.topicpanels.graphpanel.mousetools.SelectMouseTool;
import org.wandora.application.gui.topicpanels.graphpanel.projections.HyperbolicProjection;
import org.wandora.application.gui.topicpanels.graphpanel.projections.Projection;
import org.wandora.application.tools.CopyAsImage;
import org.wandora.application.tools.associations.ChangeAssociationRole;
import org.wandora.application.tools.associations.ChangeAssociationType;
import org.wandora.application.tools.associations.CopyAssociations;
import org.wandora.application.tools.associations.DeleteAssociations;
import org.wandora.application.tools.associations.ModifySchemalessAssociation;
import org.wandora.application.tools.graph.CenterCurrentTopic;
import org.wandora.application.tools.graph.CloseTopicNode;
import org.wandora.application.tools.graph.CollapseTool;
import org.wandora.application.tools.graph.ConnectNodesTool;
import org.wandora.application.tools.graph.ExpandNodeTool;
import org.wandora.application.tools.graph.ExpandNodesRecursivelyTool;
import org.wandora.application.tools.graph.ToggleAnimationTool;
import org.wandora.application.tools.graph.ToggleAntialiasTool;
import org.wandora.application.tools.graph.ToggleFreezeForMouseOverTool;
import org.wandora.application.tools.graph.ToggleLabelEdges;
import org.wandora.application.tools.graph.ToggleProjectionSettings;
import org.wandora.application.tools.graph.ToggleStaticWidthNodeBoxes;
import org.wandora.application.tools.graph.ToggleViewFilterInfo;
import org.wandora.application.tools.graph.export.GraphDOTExport;
import org.wandora.application.tools.graph.export.GraphGMLExport;
import org.wandora.application.tools.graph.export.GraphGraphMLExport;
import org.wandora.application.tools.graph.export.GraphGraphXMLExport;
import org.wandora.application.tools.graph.filters.ClearEdgeFilters;
import org.wandora.application.tools.graph.filters.ClearNodeFilters;
import org.wandora.application.tools.graph.filters.FilterEdges;
import org.wandora.application.tools.graph.filters.FilterNode;
import org.wandora.application.tools.graph.filters.FilterNodesOfType;
import org.wandora.application.tools.graph.filters.ReleaseEdges;
import org.wandora.application.tools.graph.filters.ReleaseNodesOfType;
import org.wandora.application.tools.graph.pinning.ReversePinningTool;
import org.wandora.application.tools.graph.pinning.SetPinnedTool;
import org.wandora.application.tools.graph.pinning.SetUnpinnedTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicMapListener;
import org.wandora.utils.Options;
import org.wandora.utils.Tuples.T2;




/**
 *
 * @author  olli
 */
public class TopicMapGraphPanel extends javax.swing.JPanel implements Runnable, MouseListener, MouseMotionListener, ComponentListener, MouseWheelListener, TopicMapListener, RefreshListener, KeyListener  {

	private static final long serialVersionUID = 1L;
	
	
	private String OPTIONS_PREFIX = "gui.graphTopicPanel.";
    private String OPTIONS_VIEW_PREFIX = OPTIONS_PREFIX + "view.";
       
    //private int viewWidth,viewHeight;   
    
    private boolean makeLocalSettingsGlobal = true;
    
    private VModel model;
    private TopicMapModel tmModel;
    
    private GraphFilter graphFilter;
    
    private Thread thread;
    private double framerate = 25.0;
    private boolean running;
    private boolean animation = true;
    private boolean freeze = false;
    private boolean labelEdges = false;
    private boolean cropNodeBoxes = true;
    
    private BufferedImage doubleBuffer;
    
    private Projection projection;
    
    private double mouseX,mouseY;
    private VNode mouseOverNode;
    private VEdge mouseOverEdge;
    
    private boolean freezeForPopup=false;
    private boolean freezeForMouseOver=true;
    
//    private boolean draggingNode=false;
//    private boolean draggingView=false;
//    private boolean drawingSelection=false;
//    private boolean popupOpen=false;
//    private double dragOffsX=0.0;
//    private double dragOffsY=0.0;
//    private double dragOffsX2=0.0;
//    private double dragOffsY2=0.0;
    
    private double minX=-1.0,maxX=1.0,minY=-1.0,maxY=1.0;

    private Map renderingHints;
    
    private Topic rootTopic;
    
    private VNode followNode;
    
    public static final int TOOL_OPEN=10;
    public static final int TOOL_SELECT=11;
    public static final int TOOL_ASSOCIATION=12;
    public static final int TOOL_ERASER=13;
    
    private int mouseTool;
    
    private MouseToolManager mouseToolManager;
    
    private java.util.List<T2<Double,Double>> selectPath;
    
    private Wandora wandora = null;
    private Options options = null;
    
    private boolean needsRefresh;
    
    private FilterManagerPanel filterManagerPanel;
    private JDialog filterDialog;
    
    
    
    
    
    /** Creates new form TopicMapGraphPanel */
    public TopicMapGraphPanel(Wandora w, Options opts) {
        this.wandora = w;
        
        // Notice, the opts coming here is a *copy* of global options. Setting
        // options doesn't affect global settings.
        this.options = opts;
        renderingHints = new LinkedHashMap<>();

        this.mouseX=0.0;
        this.mouseY=0.0;
        initComponents();
        
//        projection=new DefaultProjection();
        projection=new HyperbolicProjection();
        projection.initialize(options, OPTIONS_PREFIX+"HyperbolicProjection.");
        projection.set(Projection.VIEW_WIDTH, this.getWidth());
        projection.set(Projection.VIEW_HEIGHT, this.getHeight());
        
        mouseToolManager=new MouseToolManager(this);
        
        this.addComponentListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.addKeyListener(this);
        
        if(options != null) {
            try {
                this.setMouseTool(options.getInt(OPTIONS_PREFIX+"currentTool", TOOL_OPEN));
                this.setAnimationEnabled(options.isTrue(OPTIONS_PREFIX+"animated"));
                this.setAntialized(options.isTrue(OPTIONS_PREFIX+"antialized"));
                this.setFreezeForMouseOver(options.isTrue(OPTIONS_PREFIX+"freezeForMouseOver"));
                this.setLabelEdges(options.isTrue(OPTIONS_PREFIX+"labelEdges"));
                this.setCropNodeBoxes(options.isTrue(OPTIONS_PREFIX+"cropNodeBoxes"));
                this.setViewFilterInfo(options.isTrue(OPTIONS_PREFIX+"viewFilterInfo"));
            }
            catch(Exception e) {
                wandora.displayException("Exception occurred while initializing TopicMapGraphPanel", e);
            }
        }
        else {
            setMouseTool(TOOL_OPEN);
        }
        this.setTransferHandler(new TopicPanelTransferHandler());
        needsRefresh=false;   
    }
    

    public void setFilterManagerPanel(FilterManagerPanel filterManagerPanel){
        this.filterManagerPanel=filterManagerPanel;
        if(filterDialog != null && filterDialog.isVisible()) openFilterManager();
    }
    
    public void openFilterManager() {
        if(filterDialog == null) {
            filterDialog = filterManagerPanel.getDialogForMe(wandora);
        }
        filterDialog.setVisible(true);
    }
    

    public void updateMouseWorldCoordinates(int x,int y){
        T2<Double,Double> p=projection.screenToWorld(x,y);
        if(!p.e1.isNaN() && !p.e2.isNaN()){
            mouseX=p.e1;
            mouseY=p.e2;
        }        
    }
    
    public void setMouseFollowNode(VNode node){
        followNode=node;
    }
    
    public void setFreezeForPopup(boolean b){
        freezeForPopup=b;
    }

    public T2<Double,Double> getMouseWorldCoordinates(){
        return t2(mouseX,mouseY);
    }

    public T2<Double,Double> getViewCoordinates(){
        return t2(projection.get(Projection.VIEW_X),projection.get(Projection.VIEW_Y));
    }
    
    public void setViewCoordinates(T2<Double,Double> p){
        setViewCoordinates(p.e1,p.e2);
    }
    
    public void setViewCoordinates(double x,double y){
        projection.set(Projection.VIEW_X, x);
        projection.set(Projection.VIEW_Y, y);
    }

    public VNode getMouseOverNode(){
        return mouseOverNode;
    }
    
    public Topic getMouseOverTopic(){
        if(mouseOverNode==null) return null;
        Node n=mouseOverNode.getNode();
        if(n instanceof TopicNode) return ((TopicNode)n).getTopic();
        else return null;
    }
    
    public VEdge getMouseOverEdge(){
        return mouseOverEdge;
    }
    
    public Association getMouseOverAssociation(){
        if(mouseOverEdge==null) return null;
        Edge e=mouseOverEdge.getEdge();
        if(e instanceof AssociationEdge) return ((AssociationEdge)e).getAssociation();
        else return null;
    }
    
    public VModel getModel(){
        return model;
    }
    
    public TopicMapModel getTopicMapModel(){
        return tmModel;
    }
    
    public int getMouseTool(){
        return mouseTool;
    }
    
    public void setMouseTool(int tool) {
        mouseTool=tool;
//        updateCursor();
        
        if(tool==TOOL_OPEN){
            mouseToolManager.clearToolStack();
            mouseToolManager.addTool(MouseToolManager.EVENT_LEFTCLICK,new ExpandMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENT_RIGHTCLICK,new MenuMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG,new DragNodeMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG,new MoveViewMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG,MouseToolManager.MASK_SHIFT,new RotateMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_RIGHTDRAG,new SelectMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENT_LEFTDOUBLECLICK,new OpenTopicMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENT_LEFTCLICK,MouseToolManager.MASK_SHIFT,new SelectMouseTool(false));
        }
        else if(tool==TOOL_SELECT){
            mouseToolManager.clearToolStack();
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG|MouseToolManager.EVENT_LEFTCLICK,new SelectMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENT_LEFTCLICK,new SelectMouseTool(true));
            mouseToolManager.addTool(MouseToolManager.EVENT_LEFTCLICK,MouseToolManager.MASK_SHIFT,new SelectMouseTool(false));
            mouseToolManager.addTool(MouseToolManager.EVENTS_RIGHTDRAG,new DragNodeMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_RIGHTDRAG,new MoveViewMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_RIGHTDRAG,MouseToolManager.MASK_SHIFT,new RotateMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENT_RIGHTCLICK,new MenuMouseTool());
        }
        else if(tool==TOOL_ASSOCIATION){
            mouseToolManager.clearToolStack();
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG,new DrawAssociationMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG,new MoveViewMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG,MouseToolManager.MASK_SHIFT,new RotateMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_RIGHTDRAG,new DragNodeMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_RIGHTDRAG,new SelectMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENT_RIGHTCLICK,new MenuMouseTool());            
        }
        else if(tool==TOOL_ERASER) {
            mouseToolManager.clearToolStack();
            mouseToolManager.addTool(MouseToolManager.EVENT_LEFTCLICK,new EraserTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG,new DrawAssociationMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG,new MoveViewMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_LEFTDRAG,MouseToolManager.MASK_SHIFT,new RotateMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_RIGHTDRAG,new DragNodeMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENTS_RIGHTDRAG,new SelectMouseTool());
            mouseToolManager.addTool(MouseToolManager.EVENT_RIGHTCLICK,new MenuMouseTool()); 
        }
        if(options != null) {
            options.put(this.OPTIONS_PREFIX+"currentTool", ""+tool);
        }
    }
    
    
    public void updateCursor(Cursor cursor) {
        if(cursor == null) return;
        if(!this.getCursor().equals(cursor)) {
            this.setCursor(cursor);
        }
    }
    
    public static String getToolName(int toolType) {
        if(toolType==TOOL_OPEN){
           return "Open and move";
        }
        else if(toolType==TOOL_SELECT){
            return "Select nodes";
        }
        else if(toolType==TOOL_ASSOCIATION){
            return "Draw associations";
        }
        else if(toolType==TOOL_ERASER) {
            return "Hide nodes and associations";
        }
        return "";
    }
    
    public static String getToolDescription(int toolType) {
        if(toolType==TOOL_OPEN){
           return "Open and move with mouse";
        }
        else if(toolType==TOOL_SELECT){
            return "Select nodes with mouse";
        }
        else if(toolType==TOOL_ASSOCIATION){
            return "Draw associations with mouse";
        }
        else if(toolType==TOOL_ERASER) {
            return "Hide nodes and associations";
        }
        return "";
    }
    
/*    public void updateCursor() {
        switch(mouseTool) {
            case TOOL_OPEN:
                updateCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                break;
            case TOOL_SELECT:
                updateCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
        }
    }*/
            
    /*
    public TopicMapGraphPanel(Topic t) {
        this();        
        setRootTopic(t);
    }
    */
    
    public Topic getRootTopic(){
        return rootTopic;
    }
    
    public VNode getRootNode(){
        return model.getNode(tmModel.getNodeFor(rootTopic));
    }
    
    public Set<VEdge> getSelectedEdges(){
        Set<VEdge> ret=model.getSelectedEdges();
        if(ret==null) return new HashSet<VEdge>();
        else return ret;
    }
    
    public Set<VNode> getSelectedNodes(){
        Set<VNode> ret=model.getSelectedNodes();
        if(ret==null) {
            return new HashSet<VNode>();
        }
        else return ret;
    }
    
    public Collection<Topic> getSelectedTopics(){
        ArrayList<Topic> ret=new ArrayList<Topic>();
        Set<VNode> selection=model.getSelectedNodes();
        if(selection==null) return new ArrayList<Topic>();
        for(VNode vnode : selection){
            Node n=vnode.getNode();
            if(n instanceof TopicNode) ret.add(((TopicNode)n).getTopic());
        }
        return ret;
    }
    
    public void clearModel(){
        // System.out.println("Clear model");
        model=null;
        tmModel=null;
        stopThread();
        repaint();
    }
    
    public void remakeModels(TopicMap tm){
        // System.out.println("Remake model");
        needsRefresh=false;
        model=new VModel(this);
        tmModel=new TopicMapModel(model,tm);
        if(graphFilter == null) {
            graphFilter=new GraphFilter(tmModel);
        }
        else {
            graphFilter.setTopicMapModel(tmModel);
        }
        model.setNodeFilter(graphFilter);        
        model.setEdgeFilter(graphFilter);
        if(filterManagerPanel!=null) {
            filterManagerPanel.setTopicNodeFilter(graphFilter);
        }
    }
    
    public void setRootTopic(Topic t) {
        if(t == null) return;
        synchronized(this) {
            rootTopic=t;
            if(model==null){
                // System.out.println("setRootTopic, remake");
                remakeModels(t.getTopicMap());

                TopicNode n=tmModel.getNodeFor(t);
                model.addNode(n);
                model.openNode(n);

                if(!running) startThread();
            }
            else {
                // System.out.println("setRootTopic, reuse");
                Node n=tmModel.getNodeFor(t);
                boolean exists=(model.getNode(n)!=null);
                VNode vn=null;
                if(exists) vn=model.addNode(n);
                else vn=model.addNode(n, projection.get(Projection.VIEW_X)+20.0,projection.get(Projection.VIEW_Y)-20.0);
                if(!exists) model.connectNode(vn);
                followNode=vn;
            }
        }
    }
    
    /*
    public void setWandoraAdmin(Wandora admin) {
        this.admin = admin;
    }
     */
    
    public Wandora getWandora(){
        return wandora;
    }
    
    
    public void setViewFilterInfo(boolean b){
        viewFilterInfo=b;
        setOption("viewFilterInfo", ""+b);
    }
    
    public boolean getViewFilterInfo() {
        return viewFilterInfo;
    }
    
    
    
    public void setCropNodeBoxes(boolean b){
        cropNodeBoxes=b;
        setOption("cropNodeBoxes", ""+b);
    }
    
    public boolean getCropNodeBoxes() {
        return cropNodeBoxes;
    }
    
    
    
    public void setFramerate(double fr){
        framerate=fr;
        setOption("framerate", ""+fr);
    }
    
    public double getFramerate() {
        return framerate;
    }
    
    
    public void setFreezeForMouseOver(boolean b){
        freezeForMouseOver=b;
        setOption("freezeForMouseOver", ""+b);
    }
    
    public boolean getFreezeForMouseOver() {
        return freezeForMouseOver;
    }
    
    
    public void setLabelEdges(boolean b) {
        labelEdges = b;
        setOption("labelEdges", ""+b);
    }
    
    public boolean getLabelEdges() {
        return labelEdges;
    }
    
    
    public void setAnimationEnabled(boolean b) {
        animation=b;
        setOption("animated", ""+b);
    }
    
    public boolean getAnimationEnabled() {
        return animation;
    }
    
    
    
    private void setOption(String optionKey, String optionValue) {
        if(options != null) {
            options.put(OPTIONS_PREFIX+optionKey, optionValue);
            if(makeLocalSettingsGlobal) {
                Options globalOptions = wandora.getOptions();
                if(globalOptions != null) {
                    globalOptions.put(OPTIONS_PREFIX+optionKey, optionValue);
                }
            }
        }
    }
    
    
    
    public void setAntialized(boolean b) {
        if(b)
            renderingHints.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        else
            renderingHints.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
        setOption("antialized", ""+b);
    }
    
    public boolean getAntialized() {
        Object antialized=renderingHints.get(RenderingHints.KEY_ANTIALIASING);
        if(antialized==null || antialized==RenderingHints.VALUE_ANTIALIAS_OFF)
            return false;
        else
            return true;
    }
    
    
    
    
    public GraphFilter getGraphFilter() {
        return graphFilter;
    }
    
    
    
    public void startThread() {
        if(!running) {
            this.running=true;
            this.thread=new Thread(this);
            thread.start();
        }
    }
    
    public void stopThread() {
        this.running=false;
    }
    

    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------------- CLUSTER ---
    // -------------------------------------------------------------------------
    
    
    
    
    
    
    private static class Cluster {
        public double x,y;
        public int size;
        public double radius;
        public double mass;
        public double averageDist;
        public int id;
        public java.util.List<VNode> nodes;
        public Cluster(){x=y=0; size=0; radius=0; mass=0; averageDist=0; id=0;}
        public Cluster(VNode vnode){this(); x=vnode.x;y=vnode.y; }
        public Cluster(VNode vnode,int id){this(vnode); this.id=id; }
        public Cluster(int id){this(); this.id=id; }
    }
    
    private ArrayList<Cluster> doClustering(){
        // initialize cluster centers
        int maxClusters=model.getNodes().size()/20+1;
        ArrayList<Cluster> clusters=new ArrayList<Cluster>();
        for(VNode vnode : model.getNodes()){
            if(clusters.size()>=maxClusters) break;
            if(vnode.getEdges().size()>10) clusters.add(new Cluster(vnode,clusters.size()));
        }
        // if not enough natural center points, just make some clusters
        int counter=0;
        if(!model.getNodes().isEmpty()) {
            while(clusters.size()<model.getNodes().size()/100+1){
                while(model.getNodes().get(counter).getEdges().size()>10) counter++;
                clusters.add(new Cluster(model.getNodes().get(counter),clusters.size()));            
                counter++;
            }
        }
        // k-means clustering
        while(true){
            for(Cluster cluster : clusters) { cluster.radius=0.0; }
            boolean changed=false;
            for(VNode vnode : model.getNodes()){
                int old=vnode.cluster;
                // find closest cluster and put node there
                double minDist=Double.MAX_VALUE;
                for(int i=0;i<clusters.size();i++){
                    Cluster cluster=clusters.get(i);
                    double dx=cluster.x-vnode.x;
                    double dy=cluster.y-vnode.y;
                    double d=dx*dx+dy*dy;
                    if(d<minDist) {
                        vnode.cluster=i;
                        minDist=d;
                    }
                }
                Cluster cluster=clusters.get(vnode.cluster);
                if(cluster.radius<minDist) cluster.radius=minDist;
                if(old!=vnode.cluster) changed=true;
            }
            // if nodes stayed in same clusters, break
            if(!changed) break;
            // update cluster centers 
            for(Cluster cluster : clusters) { cluster.x=0; cluster.y=0; cluster.size=0; }
            for(VNode vnode : model.getNodes()){
                Cluster cluster=clusters.get(vnode.cluster);
                cluster.x+=vnode.x;
                cluster.y+=vnode.y;
                cluster.size++;
            }
            for(Cluster cluster : clusters) {cluster.x/=cluster.size; cluster.y/=cluster.size;}
        }

        
        // Next we compact clusters by removing nodes that are much further away from cluster
        // center than most other cluster nodes. We put these nodes in a cluster of their own.
        Cluster others=new Cluster(clusters.size());
        // make array lists for cluster nodes
        others.nodes=new ArrayList<VNode>();
        for(Cluster cluster : clusters) { 
            cluster.nodes=new ArrayList<VNode>(cluster.size);
        }
        // put nodes in the cluster ArrayLists, calculate cluster mass and average node distance from
        // cluster center
        for(VNode vnode : model.getNodes()){
            Cluster cluster=clusters.get(vnode.cluster);
            cluster.nodes.add(vnode);
            double dx=cluster.x-vnode.x;
            double dy=cluster.y-vnode.y;
            double d=Math.sqrt(dx*dx+dy*dy);
            cluster.averageDist+=d;
            cluster.mass+=vnode.getNode().getMass();
        }
        // the compacting itself
        for(Cluster cluster : clusters) {
            // finalize average distance and radius
            cluster.averageDist/=cluster.size;
            cluster.radius=Math.sqrt(cluster.radius);
            // only compact if necessary
            if(cluster.radius>cluster.averageDist*2.0){
                cluster.radius=0.0;
                for(int i=0;i<cluster.nodes.size();i++){
                    VNode vnode=cluster.nodes.get(i);
                    double dx=cluster.x-vnode.x;
                    double dy=cluster.y-vnode.y;
                    double d=Math.sqrt(dx*dx+dy*dy);
                    if(d>cluster.averageDist*2.0){
                        // remove node from cluster and move to others
                        cluster.nodes.remove(i);
                        cluster.size--;
                        i--;
                        cluster.mass-=vnode.getNode().getMass();
                        others.nodes.add(vnode);
                        others.mass+=vnode.getNode().getMass();
                        others.size++;
                        vnode.cluster=clusters.size();
                    }
                    else if(d>cluster.radius) cluster.radius=d;
                }
            }
        }
        
        // calculate others center and radius
        for(VNode node : others.nodes){
            others.x+=node.x;
            others.y+=node.y;
        }
        others.x=others.size;
        others.y=others.size;
        for(VNode vnode : others.nodes){
            double dx=others.x-vnode.x;
            double dy=others.y-vnode.y;
            double d=Math.sqrt(dx*dx+dy*dy);
            if(others.radius<d) others.radius=d;
        }        
                
        
        clusters.add(others);
        
        return clusters;
    }
    
    
    private void updateClusters(Cluster c1,Cluster c2){
        VNode vin,vjn;
        VEdge ve;
        T2<VNode,VNode> vns;
        Edge e;
        Node in,jn;
        double m,dx,dy,d2,d,f,dd,f2;

        dx=c2.x-c1.x;
        dy=c2.y-c1.y;
        d=Math.sqrt(dx*dx+dy*dy);
        if(d-c2.radius-c1.radius>0){
            f=1/(d*d*d);
            f2=f*c2.mass;
            for(int i=0;i<c1.nodes.size();i++){
                vin=c1.nodes.get(i);
                in=vin.getNode();
                if(!vin.isPinned()){
                    vin.x-=dx*in.getMass()*f2;
                    vin.y-=dy*in.getMass()*f2;
                }
            }
            f2=f*c1.mass;
            for(int j=0;j<c2.nodes.size();j++){
                vjn=c2.nodes.get(j);
                jn=vjn.getNode();
                if(!vjn.isPinned()){
                    vjn.x+=dx*jn.getMass()*f2;
                    vjn.y+=dy*jn.getMass()*f2;
                }
            }
        }
        else{
            for(int i=0;i<c1.nodes.size();i++){
                vin=c1.nodes.get(i);
                in=vin.getNode();
                if(in.getMass()==0.0) continue;

                int j=(c1==c2?i+1:0);
                for(;j<c2.nodes.size();j++){
                    vjn=c2.nodes.get(j);
                    jn=vjn.getNode();
                    if(jn.getMass()==0.0) continue;
                    m=in.getMass()*jn.getMass();
                    dx=vjn.x-vin.x;
                    dy=vjn.y-vin.y;
                    d2=dx*dx+dy*dy;
                    if(d2<100.0) d2=100.0;
                    d=Math.sqrt(d2);
                    f=m/d2;
    //                if(f>10.0) f=10.0;
                    f/=d;
                    if(!vin.isPinned()){
                        vin.x-=dx*f;
                        vin.y-=dy*f;
                    }
                    if(!vjn.isPinned()){
                        vjn.x+=dx*f;
                        vjn.y+=dy*f;
                    }
                }
            }
        }
    }
    
    
    
    
//    long t1,t2,t3,t4,t5;
    public void updateWorld(){
        if(model==null) return;
        VNode vin,vjn;
        VEdge ve;
        T2<VNode,VNode> vns;
        Edge e;
        Node in,jn;
        double m,dx,dy,d2,d,f,dd;
        
//        t1=System.currentTimeMillis();
        ArrayList<Cluster> clusters=doClustering();
//        t2=System.currentTimeMillis();
        
        java.util.List<VNode> nodes=model.getNodes();
        java.util.List<VEdge> edges=model.getEdges();
        if(!freeze && animation) {
            for(int i=0;i<clusters.size();i++){
                Cluster ic=clusters.get(i);
                for(int j=i+1;j<clusters.size();j++){
                    Cluster jc=clusters.get(j);
                    updateClusters(ic,jc);
                }
                updateClusters(ic,ic);
            }
            
            for(int i=0;i<edges.size();i++){
                ve=edges.get(i);
                e=ve.getEdge();
                vns=ve.getNodes();
                vin=vns.e1;
                vjn=vns.e2;

                dx=vjn.x-vin.x;
                dy=vjn.y-vin.y;
                d2=dx*dx+dy*dy;
                d=Math.sqrt(d2);
                if(d>e.getLength()) {
                    dd=d-e.getLength();
                    f=dd*ve.getEdge().getStiffness();
    //                if(f>10.0) f=10.0;
                    f/=d;
                    if(!vin.isPinned()){
                        vin.x+=dx*f;
                        vin.y+=dy*f;
                    }
                    if(!vjn.isPinned()){
                        vjn.x-=dx*f;
                        vjn.y-=dy*f;
                    }
                }
                if(labelEdges) {
                    ve.labelEdges = true;
                }
                else {
                    ve.labelEdges = false;
                }
            }

        }
//        t3=System.currentTimeMillis();
        
        if(followNode!=null){
            dx=followNode.x-projection.get(Projection.VIEW_X);//viewX;
            dy=followNode.y-projection.get(Projection.VIEW_Y);//viewY;
            projection.modify(Projection.VIEW_X, dx*0.3);
            projection.modify(Projection.VIEW_Y, dy*0.3);
        }
        
        if(!freezeForPopup){
            VNode over=null;
            for(int i=0;i<nodes.size();i++){
                vin=nodes.get(i);
                if(vin.pointInside(mouseX,mouseY)){
                    if(over!=null) over.mouseOver=false;
                    vin.mouseOver=true;
                    over=vin;
                }
                else vin.mouseOver=false;
            }
            mouseOverNode=over;
            
            if(freezeForMouseOver) freeze = true;
            if(mouseOverNode==null){
                freeze = false;
                double u,ux,uy,dx2,dy2,md2,ndx,ndy;
                double minDist=100.0;
                VEdge minEdge=null;
                for(int i=0;i<edges.size();i++){
                    ve=edges.get(i);
                    e=ve.getEdge();
                    vns=ve.getNodes();
                    vin=vns.e1;
                    vjn=vns.e2;

                    dx=vjn.x-vin.x;
                    dy=vjn.y-vin.y;
                    d2=dx*dx+dy*dy;
                    if(d2==0) continue;

                    u=((mouseX-vin.x)*dx+(mouseY-vin.y)*dy)/d2;

                    if(u<0.0 || u>1.0) continue;

                    d=Math.sqrt(d2);
                    ndx=dx/d;
                    ndy=dy/d;

                    f=Math.sin(Math.PI*u)*ve.curvature;
                    ux=vin.x+u*dx+f*ndy;
                    uy=vin.y+u*dy-f*ndx;
                    dx2=mouseX-ux;
                    dy2=mouseY-uy;
                    md2=dx2*dx2+dy2*dy2;
                    if(md2<minDist){
                        minDist=md2;
                        minEdge=ve;
                    }
                }
                if(mouseOverEdge!=null) {
                    mouseOverEdge.mouseOver=false;
                }
                mouseOverEdge=minEdge;
                if(minEdge!=null) {
                    minEdge.mouseOver=true;
                    if(freezeForMouseOver) freeze = true;
                }
            }
            else if(mouseOverEdge!=null){
                mouseOverEdge.mouseOver=false;
                mouseOverEdge=null;
            }
        }
/*        if(draggingNode) {
            mouseOverNode.x=mouseX+dragOffsX;
            mouseOverNode.y=mouseY+dragOffsY;
        }*/
        minX=-1.0; maxX=1.0; minY=-1.0; maxY=1.0;
        for(int i=0;i<nodes.size();i++){
            vin=nodes.get(i);
            if(vin.x<minX) minX=vin.x;
            if(vin.x>maxX) maxX=vin.x;
            if(vin.y<minY) minY=vin.y;
            if(vin.y>maxY) maxY=vin.y;
        }
        
    }
    
    public Projection getProjection(){
        return projection;
    }
    
    @Override
    public void update(Graphics g){
        paint(g);
    }
    
    public Map getRenderingHints(){
        return renderingHints;
    }
    
    
    
    
    private Font infoFont=new Font(Font.SANS_SERIF,Font.PLAIN,12);
    private FontMetrics infoFontMetrics = null;
    private boolean viewInfo = true;
    private boolean viewFilterInfo = true;
    
    
    
    @Override
    public void paint(Graphics g){
        synchronized(this) {
            int selectedNodes = 0;
            int nodeCount = 0;
            int edgeCount = 0;

            g.setClip(0, 0, this.getWidth(),this.getHeight());
            
            Graphics2D g2=(Graphics2D)g;
            
            g2.addRenderingHints(renderingHints);
            g2.setColor(Color.WHITE);
            g2.fillRect(0,0,this.getWidth(),this.getHeight());
            
            if(model!=null){
                T2<VNode,VNode> nodes;
                for(VEdge e : model.getEdges()) {
                    nodes=e.getNodes();
                    if(!nodes.e1.equals(nodes.e2)) {
                        e.draw(g2,projection);
                        edgeCount++;
                    }
                }
                for(VNode n : model.getNodes()) {
                    if(n.isSelected()) selectedNodes++;
                    n.draw(g2,projection);
                    nodeCount++;
                }
                if(mouseOverEdge!=null) {
                    mouseOverEdge.draw(g2,projection);
                }
                if(mouseOverNode!=null) {
                    mouseOverNode.draw(g2,projection);
                }
            }
            
            mouseToolManager.paint(g2);
            
            projection.draw(g2);

            // ****** INFO AT RIGHT BOTTOM CORNER ******
            if(viewInfo) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.setFont(infoFont);
                infoFontMetrics = g.getFontMetrics();
                int yloc = getHeight()-10+16;
                int xloc = getWidth()-10;
                String str = "";

                yloc -= 16;
                str = edgeCount+" edges";
                g2.drawString(str ,xloc-infoFontMetrics.stringWidth(str), yloc);

                yloc -= 16;
                if(selectedNodes == 0) {
                    str = nodeCount+" nodes";
                }
                else {
                    str = nodeCount+" nodes, "+selectedNodes+" selected";
                }
                g2.drawString(str ,xloc-infoFontMetrics.stringWidth(str), yloc);
   
                yloc -= 16;
                str = Math.round(fps)+" fps";
                g2.drawString(str ,xloc-infoFontMetrics.stringWidth(str), yloc);
                
                // g2.drawString(""+(t2-t1),10,32);
                // g2.drawString(""+(t3-t2),10,44);
                // g2.drawString(""+(t5-t4),10,56);
            }
            
            
            if(viewFilterInfo && graphFilter != null) {
                String filterDescription = graphFilter.describeFilters();
                if(filterDescription != null && filterDescription.length() > 0) {
                    String[] filterDescriptions = filterDescription.split("\n");
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.setFont(infoFont);
                    infoFontMetrics = g.getFontMetrics();
                    int yloc = 20;
                    int xloc = getWidth()-10;
                    for(String str : filterDescriptions) {
                        g2.drawString(str ,xloc-infoFontMetrics.stringWidth(str), yloc);
                        yloc += 16;
                    }
                }
            }
        }
    }

    private volatile double fps=0.0;
    @Override
    public void run() {
        long delay;
        long timerStart;
        long timerEnd;
        long timer;

        while(running) {
            delay=(long)(1000/framerate);
            timerStart = System.currentTimeMillis();
            timer = timerStart;
            timerEnd = timer+delay;
 
            synchronized(this){
                updateWorld();
            }
            repaint();

            timer=System.currentTimeMillis();
            while(timer<timerEnd && running) {
                try{
                    Thread.sleep(timerEnd-timer);
                }
                catch(InterruptedException ie){}
                timer=System.currentTimeMillis();
            }

            fps=1000.0/(timer-timerStart);
        }
    }

    @Override
    public void componentShown(ComponentEvent e) {
        projection.set(Projection.VIEW_WIDTH, TopicMapGraphPanel.this.getWidth());
        projection.set(Projection.VIEW_HEIGHT, TopicMapGraphPanel.this.getHeight());
    }

    @Override
    public void componentResized(ComponentEvent e) {
        projection.set(Projection.VIEW_WIDTH, TopicMapGraphPanel.this.getWidth());
        projection.set(Projection.VIEW_HEIGHT, TopicMapGraphPanel.this.getHeight());
    }

    @Override
    public void componentHidden(ComponentEvent e) {}

    @Override
    public void componentMoved(ComponentEvent e) {}

    
    /*
    public JPopupMenu buildNodePopup(Object[] tools){
        JPopupMenu menu=new JPopupMenu();
        buildMenu(tools,0,menu,null);
        return menu;
    }
    public JPopupMenu buildEdgePopup(Object[] tools){
        JPopupMenu menu=new JPopupMenu();
        buildMenu(tools,1,menu,null);        
        return menu;
    }
    public JPopupMenu buildGeneralPopup(Object[] tools){
        JPopupMenu menu=new JPopupMenu();
        buildMenu(tools,2,menu,null);        
        return menu;
    }
    public void buildMenu(Object[] tools,final int mode,JPopupMenu popup,JMenu menu){
        for(int i=0;i<tools.length;i++){
            if(tools[i] instanceof GraphTool){
                final GraphTool tool=(GraphTool)tools[i];
                JMenuItem item=new JMenuItem(tool.getName(),tool.getIcon());
                if(popup!=null) popup.add(item);
                else menu.add(item);
                item.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        synchronized(TopicMapGraphPanel.this){
                            if(mode==0) tool.invokeNode(mouseOverNode,model,TopicMapGraphPanel.this);
                            else if(mode==1) tool.invokeEdge(mouseOverEdge,model,TopicMapGraphPanel.this);
                            else if(mode==2) tool.invokeGeneral(model,TopicMapGraphPanel.this);                        
                        }
                    }
                });
            }
            else if(tools[i] instanceof String){
                JMenu subMenu=new JMenu((String)tools[i]);
                i++;
                if(tools[i] instanceof Object[])
                    buildMenu((Object[])tools[i],mode,null,subMenu);
                else if(tools[i] instanceof Collection)
                    buildMenu(((Collection)tools[i]).toArray(),mode,null,subMenu);
                if(popup!=null) popup.add(subMenu);
                else menu.add(subMenu);
            }
        }
        if(popup!=null){
            popup.addPopupMenuListener(new PopupMenuListener() {
                public void popupMenuCanceled(PopupMenuEvent e) {}
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popupOpen=false;
                }
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            });
        }
    }
     */
    
    
    
    public void selectNodesWithPath(ArrayList<T2<Double,Double>> path){
        if(!path.get(0).equals(path.get(path.size()-1))) path.add(path.get(0));
        
        ArrayList<VNode> nodes=model.getNodes();
        int[] counters=new int[nodes.size()];
        
        for(int i=0;i<path.size()-1;i++){
            T2<Double,Double> p1=path.get(i);
            T2<Double,Double> p2=path.get(i+1);
            double dx=p2.e1-p1.e1;
            if(dx==0) continue;
            double dy=p2.e2-p1.e2;
            for(int j=0;j<nodes.size();j++){
                VNode vnode=nodes.get(j);
                double xpos=(vnode.x-p1.e1)/dx;
                if(xpos>=0.0 && xpos<=1.0){
                    if(p1.e2+dy*xpos>=vnode.y) counters[j]++;
                }
            }
        }
        
        for(int i=0;i<counters.length;i++){
            if( (counters[i]%2)==1 ){
                model.addSelection(nodes.get(i));
            }
        }
        
    }
    
    public boolean lockMouseTool(MouseTool tool){
        return mouseToolManager.lockTool(tool);
    }
    public void releaseMouseTool(){
        mouseToolManager.releaseLockedTool();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        requestFocus();
        T2<Double,Double> p=projection.screenToWorld(e.getX(),e.getY());
        if(!p.e1.isNaN() && !p.e2.isNaN()){
            mouseX=p.e1;
            mouseY=p.e2;
/*            if(draggingView){
                viewX=dragOffsX+(dragOffsX2-mouseX);
                viewY=dragOffsY+(dragOffsY2-mouseY);

                p=projection.screenToWorld(e.getX(),e.getY());
                mouseX=p.e1;
                mouseY=p.e2;
                dragOffsX=viewX;
                dragOffsY=viewY;
                dragOffsX2=mouseX;
                dragOffsY2=mouseY;                        
            }
            if(drawingSelection){
                selectPath.add(t2(mouseX,mouseY));
            }*/
        }
        mouseToolManager.mouseDragged(e);
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        requestFocus();
/*        if(e.getButton()==e.BUTTON3){
            if(mouseOverNode!=null){
                popupOpen=true;
                if(!mouseOverNode.selected) model.setSelection(mouseOverNode);
                JPopupMenu menu=getNodeMenu();
                menu.show(this,e.getX(),e.getY());
            }
            else if(mouseOverEdge!=null){
                popupOpen=true;
                if(!mouseOverEdge.selected) model.setSelection(mouseOverEdge);
                JPopupMenu menu=getEdgeMenu();
                menu.show(this,e.getX(),e.getY());
                
            }
            else {
                popupOpen=true;               
                JPopupMenu menu=getGeneralMenu();
                menu.show(this,e.getX(),e.getY());
            }
        }
        
        if(e.getButton()==e.BUTTON1){
            if(e.getClickCount() == 2 && mouseOverNode!=null) {
                Node n = mouseOverNode.getNode();
                if(n instanceof TopicNode && admin != null) {
                    Topic t = ((TopicNode) n).getTopic();
                    admin.applyChangesAndOpen(t);                    
                }
            }
            else if(mouseTool==TOOL_SELECT || (e.getModifiers()&e.SHIFT_MASK)!=0) {
                if(mouseOverNode!=null){
                    if((e.getModifiers()&e.SHIFT_MASK)==0) model.setSelection(mouseOverNode);
                    else{
                        if(mouseOverNode.isSelected()) model.deselectNode(mouseOverNode);
                        else model.addSelection(mouseOverNode);
                    }
                }
                else{
                    if((e.getModifiers()&e.SHIFT_MASK)==0) model.deselectAll();
                }
            }
            else if(mouseTool==TOOL_OPEN && mouseOverNode!=null) model.openNode(mouseOverNode);
        }*/
        mouseToolManager.mouseClicked(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocus();
/*        if(mouseTool==TOOL_SELECT || (e.getModifiers()&e.SHIFT_MASK)!=0){
            drawingSelection=true;
            selectPath=new ArrayList<T2<Double,Double>>();
            selectPath.add(t2(mouseX,mouseY));
        }
        else if(mouseTool==TOOL_OPEN){
            if(mouseOverNode!=null) {
                draggingNode=true;
                followNode=null;
                dragOffsX=mouseOverNode.x-mouseX;
                dragOffsY=mouseOverNode.y-mouseY;
            }
            else{
                draggingView=true;
                followNode=null;
                dragOffsX=viewX;
                dragOffsY=viewY;
                dragOffsX2=mouseX;
                dragOffsY2=mouseY;
            }
        }*/
        mouseToolManager.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        requestFocus();
/*        draggingNode=false;
        draggingView=false;
        if(drawingSelection){
            drawingSelection=false;
            if(selectPath.size()>10){
                if((e.getModifiers()&e.SHIFT_MASK)==0) model.deselectAll();
                selectNodesWithPath(selectPath);
            }
        }*/
        mouseToolManager.mouseReleased(e);
        mouseToolManager.updateCursor(e.getModifiers(),e.getX(),e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        T2<Double,Double> p=projection.screenToWorld(e.getX(),e.getY());
        if(!p.e1.isNaN() && !p.e2.isNaN()){
            mouseX=p.e1;
            mouseY=p.e2;
        }
        mouseToolManager.mouseMoved(e);
        mouseToolManager.updateCursor(e.getModifiers(),e.getX(),e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches=e.getWheelRotation();
        double multiplier=1.5;
        if((e.getModifiers()&MouseWheelEvent.ALT_MASK)!=0) multiplier=1.1;
        if((e.getModifiers()&MouseWheelEvent.SHIFT_MASK)!=0){
            projection.modify(Projection.MOUSEWHEEL2, notches, multiplier);
        }
        else {
            projection.modify(Projection.MOUSEWHEEL1, notches, multiplier);
        }
    }

    
    
    public JPopupMenu getGeneralMenu() {
        JPopupMenu popup = null;
        
        try {
            Object[] menuStructure = new Object[] {
                "Center graph", new CenterCurrentTopic(this),
                "Change projection", new ToggleProjectionSettings(this),
                "---",
                "Filter nodes", UIBox.makeMenuStruct(FilterNodesOfType.makeTools(model.getNodes(),graphFilter,null)),
                "Release node filters", UIBox.makeMenuStruct(ReleaseNodesOfType.makeTools(graphFilter,null)),
                "Clear all node filters", new ClearNodeFilters(graphFilter),
                "---",
                "Filter edges", UIBox.makeMenuStruct(FilterEdges.makeTools(model.getEdges(),graphFilter,null)),
                "Release edge filters", UIBox.makeMenuStruct(ReleaseEdges.makeTools(model.getEdges(),graphFilter,null)),
                "Clear all edge filters", new ClearEdgeFilters(graphFilter),
                "---",
                /*
                "Pin nodes", new Object[] {
                    "Set all pinned", new SetPinnedTool(new GraphAllNodesContext()),
                    "Set all unpinned", new SetUnpinnedTool(new GraphAllNodesContext()),
                    "Reverse pinning", new ReversePinningTool(new GraphAllNodesContext()),
                },
                "---",
                 **/
                "Select", WandoraMenuManager.getDefaultSelectMenuStruct(wandora, this),
                "Topics", WandoraMenuManager.getDefaultTopicMenuStruct(wandora, this),
                "Options", getOptionsMenuStruct(),
                "---",
                "Copy as image", new CopyAsImage(),
                "Export graph as", new Object[] {
                    "Export graph as DOT...", new GraphDOTExport(),
                    "Export graph as GraphML...", new GraphGraphMLExport(),
                    "Export graph as GraphXML...", new GraphGraphXMLExport(),
                    "Export graph as GML...", new GraphGMLExport(),
                }
            };
            popup=UIBox.makePopupMenu(menuStructure, Wandora.getWandora(this));
            popup.addPopupMenuListener(new PopupListener());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return popup;
    }
    
    
    public JPopupMenu getEdgeMenu() {
        Object[] menuStructure = new Object[] {
            "Copy association", new Object[] {
                "Copy association as tab text", new CopyAssociations(),
                "Copy association as HTML", new CopyAssociations(CopyAssociations.HTML_OUTPUT),
            },
            /*
            "Paste associations", new Object[] {
                "Paste tab text associations",
            },
             */
            "Change associations", new Object[] {
                "Change association type...", new ChangeAssociationType(),
                "Change association role...", new ChangeAssociationRole(),
            },
            /*
            "Make players instance of role...",
             **/
            //"Add associations...", new AddAssociations(new ApplicationContext()),
            "Modify association...", new ModifySchemalessAssociation(),
            "Delete associations...", new DeleteAssociations(),
            //"Duplicate associations...",
            //"---",
            /*
            "Refactor", new Object[] {
                "Break Binary Association Topics", new BinaryAssociationTopicBreaker(),
            }
            */

            /*
             "---",
             "Cut associations", new Object[] {
                "Copy all associations as tab text", new CopyAssociations(),
                "Copy all associations as HTML", new CopyAssociations(CopyAssociations.HTML_OUTPUT),
            },
             **/
            "---",
            "Filter nodes", UIBox.makeMenuStruct(FilterNodesOfType.makeTools(model.getNodes(),graphFilter,null)),
            "Release node filters", UIBox.makeMenuStruct(ReleaseNodesOfType.makeTools(graphFilter,null)),
            "Clear all node filters", new ClearNodeFilters(graphFilter),
            "---",
            "Filter edges", UIBox.makeMenuStruct(FilterEdges.makeTools(model.getEdges(),graphFilter,null)),
            "Release edge filters", UIBox.makeMenuStruct(ReleaseEdges.makeTools(model.getEdges(),graphFilter,null)),
            "Clear all edge filters", new ClearEdgeFilters(graphFilter),
            "---",
            "Select", WandoraMenuManager.getDefaultSelectMenuStruct(wandora, this),
            "Options", getOptionsMenuStruct(),
            "---",
            "Copy as image", new CopyAsImage(),
            "Export graph as", new Object[] {
                "Export graph as DOT...", new GraphDOTExport(),
                "Export graph as GraphML...", new GraphGraphMLExport(),
                "Export graph as GraphXML...", new GraphGraphXMLExport(),
                "Export graph as GML...", new GraphGMLExport(),
            }
        };
        JPopupMenu popup=UIBox.makePopupMenu(menuStructure, Wandora.getWandora(this));
        popup.addPopupMenuListener(new PopupListener());
        return popup;
    }
    
    
    
    public JPopupMenu getNodeMenu() {
        Object[] menuStructure = null;
        try {
            menuStructure = new Object[] {
                "Open in",
                WandoraMenuManager.getOpenInMenu(),
                "---",
                "Expand node", new ExpandNodeTool(this),
                "Expand nodes recusively", new ExpandNodesRecursivelyTool(this),
                "Connect all visible nodes", new ConnectNodesTool(this),
                "---",
                "Close node", new CloseTopicNode(this),
                "Close all nodes but", new CloseTopicNode(this, true),
                "Collapse", new Object[] {
                    "Collapse nodes", new CollapseTool(this),
                    "Collapse nodes to 2", new CollapseTool(this, 2),
                    "Collapse nodes to 3", new CollapseTool(this, 3),
                },
                "---",
                "Pin nodes", new Object[] {
                    "Set selection pinned", new SetPinnedTool(this),
                    "Set selection unpinned", new SetUnpinnedTool(this),
                    "Reverse selection pinning", new ReversePinningTool(this),
                    "---",
                    "Set all pinned", new SetPinnedTool(this, new GraphAllNodesContext()),
                    "Set all unpinned", new SetUnpinnedTool(this, new GraphAllNodesContext()),
                    "Reverse pinning", new ReversePinningTool(this, new GraphAllNodesContext()),
                },
                "---",
                "Filter node", new FilterNode(graphFilter),
                "Filter nodes", UIBox.makeMenuStruct(FilterNodesOfType.makeTools(model.getNodes(),graphFilter,null)),
                "Release node filters", UIBox.makeMenuStruct(ReleaseNodesOfType.makeTools(graphFilter,null)),
                "Clear all node filters", new ClearNodeFilters(graphFilter),
                "---",
                "Filter edges", UIBox.makeMenuStruct(FilterEdges.makeTools(model.getEdges(),graphFilter,null)),
                "Release edge filters", UIBox.makeMenuStruct(ReleaseEdges.makeTools(model.getEdges(),graphFilter,null)),
                "Clear all edge filters", new ClearEdgeFilters(graphFilter),
                "---",
                "Select", WandoraMenuManager.getDefaultSelectMenuStruct(wandora, this),
                "Topics", WandoraMenuManager.getDefaultTopicMenuStruct(wandora, this),
                "Options", getOptionsMenuStruct(),
                "---",
                "Copy as image", new CopyAsImage(),
                "Export graph as", new Object[] {
                    "Export graph as DOT...", new GraphDOTExport(),
                    "Export graph as GraphML...", new GraphGraphMLExport(),
                    "Export graph as GraphXML...", new GraphGraphXMLExport(),
                    "Export graph as GML...", new GraphGMLExport(),
                }
            };
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        JPopupMenu popup=UIBox.makePopupMenu(menuStructure, Wandora.getWandora(this));
        popup.addPopupMenuListener(new PopupListener());
        return popup;
    }
    
    
    
    public Object[] getOptionsMenuStruct() {
        Map hints=this.getRenderingHints();
        Object antialized=hints.get(RenderingHints.KEY_ANTIALIASING);
        return new Object[] {
            "Antialised", (antialized==null || antialized==RenderingHints.VALUE_ANTIALIAS_OFF ? UIBox.getIcon("gui/icons/checkbox.png") : UIBox.getIcon("gui/icons/checkbox_selected.png")), new ToggleAntialiasTool(this),
            "Animate", (this.getAnimationEnabled() ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")), new ToggleAnimationTool(this),
            "Freeze while mouse over", (freezeForMouseOver ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")), new ToggleFreezeForMouseOverTool(this),
            "Label edges", (this.getLabelEdges() ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")), new ToggleLabelEdges(this),
            "Static width node boxes", (getCropNodeBoxes() ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")), new ToggleStaticWidthNodeBoxes(this),
            "View filter info", (getViewFilterInfo() ? UIBox.getIcon("gui/icons/checkbox_selected.png") : UIBox.getIcon("gui/icons/checkbox.png")), new ToggleViewFilterInfo(this),
        };
    }
    
    
    
    
    
    private class PopupListener implements PopupMenuListener {
        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {}
        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            freezeForPopup=false;
            //popupOpen=false;
        }
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            freezeForPopup=true;
            //popupOpen=true;
        }
    }

    private Topic getNewTopicFor(Topic t, TopicMap tm) throws TopicMapException {
        Topic r=null;
        if(t != null) {
            if(t.getSubjectLocator()!=null) {
                r=tm.getTopicBySubjectLocator(t.getSubjectLocator());
                if(r!=null) return r;
            }
            for(Locator l : t.getSubjectIdentifiers()){
                r=tm.getTopic(l);
                if(r!=null) return r;
            }
            if(t.getBaseName()!=null) {
                r=tm.getTopicWithBaseName(t.getBaseName());
                if(r!=null) return r;
            }
        }
        return null;
    }
    
    private Association getNewAssociationFor(Association a, TopicMap tm) throws TopicMapException {
        if(a == null) return null;
        
        Topic oldType=a.getType();
        Topic newType=getNewTopicFor(oldType,tm);
        if(newType==null) return null;
        
        Collection<Topic> oldRoles=a.getRoles();
        for(Topic oldRole : oldRoles){
            Topic newRole=getNewTopicFor(oldRole,tm);
            if(newRole==null) continue;
            Topic oldPlayer=a.getPlayer(oldRole);
            Topic newPlayer=getNewTopicFor(oldPlayer,tm);
            if(newPlayer==null) continue;
            
            Collection<Association> newAssociations=newPlayer.getAssociations(newType,newRole);
            
            NewAssociations: for(Association newAssociation : newAssociations){
                if(oldRoles.size()!=newAssociation.getRoles().size()) continue;
                for(Topic oldRole2 : oldRoles){
                    Topic newRole2=getNewTopicFor(oldRole2,tm);
                    if(newRole2==null) continue NewAssociations;
                    Topic oldPlayer2=a.getPlayer(oldRole2);
                    Topic newPlayer2=getNewTopicFor(oldPlayer2,tm);
                    if(newPlayer2==null) continue NewAssociations;
                    
                    Topic newPlayer3=newAssociation.getPlayer(newRole2);
                    if(newPlayer3==null) continue NewAssociations;
                    if(!newPlayer3.mergesWithTopic(newPlayer2)) continue NewAssociations;
                }
                return newAssociation;
            } 
        }
        
        return null;
    }
    
    
    
    
    
    public void refreshGraph() {
        if(model != null && projection != null) {
            projection.set(Projection.VIEW_WIDTH, this.getWidth());
            projection.set(Projection.VIEW_HEIGHT, this.getHeight());
        }

        // System.out.println("Refreshing graph");
        synchronized(this){
            needsRefresh=false;
            TopicMap tm=tmModel.getTopicMap();
            VModel oldModel=model;
            TopicMapModel oldTMModel=tmModel;
            Topic oldRoot=rootTopic;
            ArrayList<VNode> oldNodes=oldModel.getNodes();
            ArrayList<VEdge> oldEdges=oldModel.getEdges();
            
            remakeModels(tm);
            
            try{
                for(VNode vnode : oldNodes) {
                    Node node=vnode.getNode();
                    VNode nn=null;
                    if(node instanceof TopicNode){
                        TopicNode tn=(TopicNode)node;
                        Topic t=tn.getTopic();
                        Topic nt=getNewTopicFor(t,tm);
                        if(nt!=null){
                            nn=model.addNode(tmModel.getNodeFor(nt));
                        }
                    }
                    else if(node instanceof AssociationNode) {
                        AssociationNode an=(AssociationNode)node;
                        Association a=an.getAssociation();
                        Association na=getNewAssociationFor(a,tm);
                        if(na!=null){
                            if(na.getRoles().size()!=2) {
                                nn=model.addNode(tmModel.getNodeFor(na));
                            }
                        }
                    }
                    else if(node instanceof OccurrenceNode) {
                        OccurrenceNode on=(OccurrenceNode)node;
                        Topic carrier = on.getCarrier();
                        Topic type = on.getType();
                        Topic scope = on.getScope();
                        Topic ncarrier = getNewTopicFor(carrier,tm);
                        Topic ntype = getNewTopicFor(type,tm);
                        Topic nscope = getNewTopicFor(scope,tm);
                        if(ncarrier != null && ntype != null && nscope != null) {
                            String o = ncarrier.getData(ntype, nscope);
                            if(o != null) {
                                nn=model.addNode(tmModel.getNodeFor(ncarrier, ntype, nscope, o));
                            }
                        }
                    }
                    if(nn!=null) {
                        nn.x=vnode.x;
                        nn.y=vnode.y;
                        nn.pinned=vnode.pinned;
                        if(vnode.selected) model.addSelection(nn);
                    }
                }
                for(VEdge vedge : oldEdges) {
                    Edge edge=vedge.getEdge();
                    VEdge ne = null;
                    if(edge instanceof AssociationEdge) {
                        AssociationEdge ae=(AssociationEdge)edge;
                        Association a=ae.getAssociation();
                        Association na=getNewAssociationFor(a,tm);
                        if(na!=null){
                            if(a.getRoles().size()==na.getRoles().size()){
                                if(a.getRoles().size()==2){
                                    ne = model.addEdge(tmModel.getEdgeFor(na));
                                }
                                else {
                                    AssociationNode an=tmModel.getNodeFor(na);
                                    VNode vn=model.addNode(an);
                                    model.openNode(vn);
                                    // TODO currently opens entire association instead of edges that were previously open
                                }
                            }
                        }
                    }
                    else if(edge instanceof InstanceEdge) {
                        InstanceEdge ie=(InstanceEdge)edge;
                        Topic instance=ie.getInstance();
                        Topic type=ie.getType();
                        Topic ninstance=getNewTopicFor(instance,tm);
                        Topic ntype=getNewTopicFor(type,tm);
                        if(ninstance!=null && ntype!=null){
                            ne = model.addEdge(tmModel.getInstanceEdgeFor(ntype,ninstance));
                        }
                    }
                    else if(edge instanceof OccurrenceEdge) {
                        OccurrenceEdge oe=(OccurrenceEdge)edge;
                        Topic carrier = oe.getCarrier();
                        Topic type = oe.getType();
                        Topic scope = oe.getScope();
                        Topic ncarrier = getNewTopicFor(carrier,tm);
                        Topic ntype = getNewTopicFor(type,tm);
                        Topic nscope = getNewTopicFor(scope,tm);
                        if(ntype != null && nscope != null && ncarrier != null) {
                            String o = ncarrier.getData(ntype, nscope);
                            if(o != null) {
                                ne = model.addEdge(tmModel.getOccurrenceEdgeFor(ncarrier, ntype, nscope, o));
                            }
                        }
                    }
                    
                    if(ne != null) {
                        if(vedge.selected) {
                            model.setSelection(ne);
                        }
                        ne.mouseOver = vedge.mouseOver;
                        ne.labelEdges = vedge.labelEdges;
                        ne.curvature = vedge.curvature;
                    }
                }
                rootTopic=getNewTopicFor(oldRoot,tm);
                followNode=null;
            }
            catch(TopicMapException tme) {
                tme.printStackTrace();
            }
        }
    }
    
    @Override
    public void doRefresh() throws TopicMapException {
        if(needsRefresh){
            refreshGraph();
        }
    }

    
    // -------------------------------------------------------------------------
    
    @Override
    public void topicTypeChanged(Topic t, Topic added, Topic removed) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.topicIsIndexed(t)) needsRefresh=true;
    }

    @Override
    public void topicSubjectIdentifierChanged(Topic t, Locator added, Locator removed) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.topicIsIndexed(t)) needsRefresh=true;
    }

    @Override
    public void topicSubjectLocatorChanged(Topic t, Locator newLocator, Locator oldLocator) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.topicIsIndexed(t)) needsRefresh=true;
    }

    @Override
    public void associationChanged(Association a) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.associationIsIndexed(a)) needsRefresh=true;
    }

    @Override
    public void associationRemoved(Association a) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.associationIsIndexed(a)) needsRefresh=true;
    }

    @Override
    public void topicBaseNameChanged(Topic t, String newName, String oldName) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.topicIsIndexed(t)) needsRefresh=true;
    }

    @Override
    public void topicDataChanged(Topic t, Topic type, Topic version, String newValue, String oldValue) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.topicIsIndexed(t)) needsRefresh=true;
    }

    @Override
    public void topicVariantChanged(Topic t, Collection<Topic> scope, String newName, String oldName) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.topicIsIndexed(t)) needsRefresh=true;
    }

    @Override
    public void associationPlayerChanged(Association a, Topic role, Topic newPlayer, Topic oldPlayer) throws TopicMapException {
        if(needsRefresh) return;
        if(newPlayer!=null && tmModel.topicIsIndexed(newPlayer)) needsRefresh=true;
        if(oldPlayer!=null && tmModel.topicIsIndexed(oldPlayer)) needsRefresh=true;
        if(tmModel.associationIsIndexed(a)) needsRefresh=true;
    }

    @Override
    public void associationTypeChanged(Association a, Topic newType, Topic oldType) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.associationIsIndexed(a)) needsRefresh=true;
    }

    @Override
    public void topicChanged(Topic t) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.topicIsIndexed(t)) needsRefresh=true;
    }

    @Override
    public void topicRemoved(Topic t) throws TopicMapException {
        if(needsRefresh) return;
        if(tmModel.topicIsIndexed(t)) needsRefresh=true;
    }

    
    
    // -------------------------------------------------------------------------
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_PLUS) {
            projection.modify(Projection.MOUSEWHEEL1, -0.5, 1.5);
        }
        else if(e.getKeyCode() == KeyEvent.VK_MINUS) {
            projection.modify(Projection.MOUSEWHEEL1, +0.5, 1.5);
        }
        mouseToolManager.updateCursor(e.getModifiers(),-1,-1);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        mouseToolManager.updateCursor(e.getModifiers(),-1,-1);
    }
    
    

    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    

    
    

    private class TopicPanelTransferHandler extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            if(!support.isDrop()) return false;
            return support.isDataFlavorSupported(DnDHelper.topicDataFlavor) ||
                   support.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
//            return DnDHelper.makeTopicTableTransferable(data,getSelectedRows(),getSelectedColumns());
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if(!support.isDrop()) return false;
            try {
                TopicMap tm=wandora.getTopicMap();
                ArrayList<Topic> topics=DnDHelper.getTopicList(support, tm, true);
                if(topics==null) return false;

                for(Topic t : topics) {
                    if(t != null && !t.isRemoved()) {
                        setRootTopic(t);
                    }
                }
                wandora.doRefresh();
            }
            catch(TopicMapException tme){tme.printStackTrace();}
            catch(Exception ce){}
            return false;
        }
    }
    
    
}
