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
 * VModel.java
 *
 * Created on 4. kesäkuuta 2007, 14:28
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;

import java.util.*;
import static org.wandora.utils.Tuples.*;


/**
 *
 * @author olli
 */
public class VModel {
    private GraphStyle graphStyle = null;
        
    private ArrayList<VNode> nodes;
    private ArrayList<VEdge> edges;
    
    private HashMap<Node,VNode> nodeMap;
    private HashMap<Edge,VEdge> edgeMap;
    
    private NodeFilter nodeFilter=null;
    private EdgeFilter edgeFilter=null;
    
    private HashSet<VNode> selectedNodes;
    private HashSet<VEdge> selectedEdges;
    
    private TopicMapGraphPanel panel;
    
    private HashMap<T2<VNode,VNode>,ArrayList<VEdge>> edgeClusters;
    
    
    
    
    /** Creates a new instance of VModel */
    public VModel(TopicMapGraphPanel panel) {
        this.panel=panel;
        nodes=new ArrayList<VNode>();
        edges=new ArrayList<VEdge>();
        nodeMap=new HashMap<Node,VNode>();
        edgeMap=new HashMap<Edge,VEdge>();
        selectedNodes=new HashSet<VNode>();
        selectedEdges=new HashSet<VEdge>();
        edgeClusters=new HashMap<T2<VNode,VNode>,ArrayList<VEdge>>();
        graphStyle = new DefaultGraphStyle();
    }
    
    public TopicMapGraphPanel getPanel(){
        return panel;
    }
    
    public Set<VNode> getSelectedNodes(){
        return selectedNodes;
    }
    public Set<VEdge> getSelectedEdges(){
        return selectedEdges;
    }
    public VNode getSelectedNode(){
        if(selectedNodes.isEmpty()) return null;
        else return selectedNodes.iterator().next();
    }
    public VEdge getSelectedEdge(){
        if(selectedEdges.isEmpty()) return null;
        else return selectedEdges.iterator().next();
    }
    
    public void deselectAllNodes(){
        for(VNode vn : selectedNodes){
            vn.selected=false;
        }
        selectedNodes.clear();        
    }
    public void deselectAllEdges(){
        for(VEdge ve : selectedEdges){
            ve.selected=false;
        }
        selectedEdges.clear();
    }
    public void deselectAll(){
        deselectAllNodes();
        deselectAllEdges();
    }
    
    public void addSelection(VEdge vedge){
        vedge.selected=true;
        selectedEdges.add(vedge);
    }
    public void addSelection(VNode vnode){
        vnode.selected=true;
        selectedNodes.add(vnode);
    }
    
    public void deselectNode(VNode vnode){
        vnode.selected=false;
        selectedNodes.remove(vnode);
    }
    
    public void deselectEdge(VEdge vedge){
        vedge.selected=false;
        selectedEdges.remove(vedge);
    }
    
    public void deselectNodes(Collection<VNode> vnodes){
        for(VNode vn : vnodes) deselectNode(vn);
    }
    public void deselectEdges(Collection<VEdge> vedges){
        for(VEdge ve : vedges) deselectEdge(ve);
    }
    
    public void setSelection(VNode vnode){
        deselectAll();
        addSelection(vnode);
    }
    public void setSelection(VEdge vedge){
        deselectAll();
        addSelection(vedge);
    }
    
    public VNode getNode(Node n){
        return nodeMap.get(n);
    }
    public VEdge getEdge(Edge e){
        return edgeMap.get(e);
    }
    
    public ArrayList<VNode> getNodes(){
        return nodes;
    }
    public ArrayList<VEdge> getEdges(){
        return edges;
    }
    public Collection<ArrayList<VEdge>> getEdgeClusters(){
        return edgeClusters.values();
    }
    
    public VNode addNode(Node n){
        return addNode(n,0.0,0.0);
    }
    
    private void setEdgeClusterCurvatures(ArrayList<VEdge> cluster){
        double c=-edgeCurvatureDelta/2.0*(cluster.size()-1);
        for(VEdge e : cluster){
            e.curvature=c;
            c+=edgeCurvatureDelta;
        }
    }
    
    private int nodeCounter=0;
    private static double edgeCurvatureDelta=20.0;
    public VNode addNode(Node n, double x, double y){
        VNode vn=nodeMap.get(n);
        if(vn!=null) return vn;
        vn=new VNode(n,this,nodeCounter++);
        vn.x=x;
        vn.y=y;
        nodes.add(vn);
        nodeMap.put(n,vn);
        return vn;
    }
    
    public VEdge addEdge(Edge e){
        VEdge ve=edgeMap.get(e);
        if(ve!=null) return ve;
        ve=new VEdge(e,this);
        T2<VNode,VNode> ns=ve.getNodes();
        ns.e1.addEdge(ve);
        ns.e2.addEdge(ve);
        edges.add(ve);
        edgeMap.put(e,ve);
        
        ArrayList<VEdge> cluster=edgeClusters.get(ns);
        if(cluster!=null){
            cluster.add(ve);
            setEdgeClusterCurvatures(cluster);
        }
        else{
            for(VEdge ve2 : edges){
                if(ve2==ve) continue;
                if(ve2.getNodes().equals(ns)){
                    cluster=new ArrayList<VEdge>();
                    cluster.add(ve2);
                    cluster.add(ve);
                    ve2.curvature=-edgeCurvatureDelta/2.0;
                    ve.curvature=edgeCurvatureDelta/2.0;
                    edgeClusters.put(ns,cluster);
                    break;
                }
            }
        }
        
        return ve;
    }
    
    public void removeNode(VNode n){
        nodes.remove(n);
        nodeMap.remove(n.getNode());
        ArrayList<VEdge> remove=new ArrayList<VEdge>();
        for(VEdge edge : edges){
            T2<VNode,VNode> ns=edge.getNodes();
            if(ns.e1.equals(n) || ns.e2.equals(n)) remove.add(edge);
        }
        for(VEdge edge : remove){
            removeEdge(edge);
        }
    }
    
    public void removeEdge(VEdge e){
        ArrayList<VEdge> cluster=edgeClusters.get(e.getNodes());
        if(cluster!=null){
            if(cluster.size()<=2) {
                edgeClusters.remove(e.getNodes());
                for(VEdge ve : cluster) ve.curvature=0.0;
            }
            else {
                cluster.remove(e);
                setEdgeClusterCurvatures(cluster);
            }
        }
        
        edges.remove(e);
        edgeMap.remove(e.getEdge());
        T2<VNode,VNode> ns=e.getNodes();
        ns.e1.removeEdge(e);
        ns.e2.removeEdge(e);
    }
    
    public void connectNode(VNode vn){
        for(Edge e : vn.getNode().getEdges()){
            T2<Node,Node> ns=e.getNodes();
            VNode n1=nodeMap.get(ns.e1);
            VNode n2=nodeMap.get(ns.e2);
            if(n1==null || n2==null) continue;
            if(edgeFilter==null || !edgeFilter.isEdgeFiltered(e)){
                addEdge(e);
            }
        }
    }
    
    public void connectAllNodes(){
        for(VNode n : nodes){
            connectNode(n);
        }
    }
    
    public void collapseNode(VNode vn){
        HashSet<VNode> remove=new HashSet<VNode>();
        for(VEdge edge : vn.getEdges()){
            T2<VNode,VNode> ns=edge.getNodes();
            VNode other=null;
            if(ns.e1.equals(vn)) other=ns.e2;
            else if(ns.e2.equals(vn)) other=ns.e1;
            if(other.edgeCount<=1) remove.add(other);
        }        
        for(VNode n : remove) removeNode(n);
    }
    
    public void openNode(VNode vn){
        if(vn==null) return;
        Node n=vn.getNode();
        Collection<Edge> edges=n.getEdges();
        for(Edge e : edges){
            if(edgeFilter==null || !edgeFilter.isEdgeFiltered(e)){
                if(edgeMap.get(e)!=null) continue;
                T2<Node,Node> nodes=e.getNodes();
                Node other=nodes.e1;
                if(other==n) other=nodes.e2;
                if(nodeFilter==null || !nodeFilter.isNodeFiltered(other)){
                    double a=2.0*Math.PI*Math.random();
                    VNode newNode=addNode(other,vn.x+Math.cos(a)*100.0,vn.y+Math.sin(a)*100.0);
                    addEdge(e);
                    connectNode(newNode);
                    if(newNode.getNode().autoOpen()) openNode(newNode);
                }
            }
        }
    }
    public void openNode(Node n){
        openNode(nodeMap.get(n));
    }
    
    public NodeFilter setNodeFilter(NodeFilter f){
        NodeFilter old=nodeFilter;
        nodeFilter=f;
        return old;
    }
    public EdgeFilter setEdgeFilter(EdgeFilter f){
        EdgeFilter old=edgeFilter;
        edgeFilter=f;
        return old;
    }
    
    
    // --------------------------------------------------------- GRAPH STYLE ---
    
    
    public GraphStyle getGraphStyle() {
        return graphStyle;
    }
    public void setGraphStyle(GraphStyle style) {
        if(style == null) {
            graphStyle = new DefaultGraphStyle();
        }
        else {
            this.graphStyle = style;
        }
        for(Iterator<VNode>iter = nodes.iterator(); iter.hasNext(); ) {
            iter.next().setGraphStyle(style);
        }
        for(Iterator<VEdge>iter = edges.iterator(); iter.hasNext(); ) {
            iter.next().setGraphStyle(style);
        }
    }
    
    
}
