
package org.wandora.application.gui.topicpanels.graph3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author olli
 */
public class EuclideanWorld implements World {
    private final ArrayList<EuclideanNode> nodes=new ArrayList<EuclideanNode>();
    private final ArrayList<EuclideanEdge> edges=new ArrayList<EuclideanEdge>();

    private static final double REPEL_FORCE=0.0;
    private static final double EDGE_FORCE=0.0;

    public double repelForce=REPEL_FORCE;
    public double edgeForce=EDGE_FORCE;
    public double edgeLength=1.0;

    public void setProperty(String prop, Object value) {
        if(prop.equals("repelForce")) repelForce=(Double)value;
        else if(prop.equals("edgeForce")) edgeForce=(Double)value;
        else if(prop.equals("edgeLength")) edgeLength=(Double)value;
    }

    public Edge addEdge(Node node1, Node node2) {
        if(!(node1 instanceof EuclideanNode) || !(node2 instanceof EuclideanNode)) throw new ClassCastException();
        synchronized(nodes){ // synchronize both nodes and edges with nodes list
            EuclideanEdge e=new EuclideanEdge((EuclideanNode)node1, (EuclideanNode)node2);
            edges.add(e);
            return e;
        }
    }

    public Node addNode(Object nodeObject) {
        synchronized(nodes){
            EuclideanNode n=new EuclideanNode(nodeObject);
            nodes.add(n);
            return n;
        }
    }

    public void removeEdge(Edge edge) {
        synchronized(nodes){
            for(int i=0;i<edges.size();i++){
                if(edges.get(i)==edge) {
                    edges.remove(i);
                    return;
                }
            }
        }
    }

    public void removeNode(Node node) {
        synchronized(nodes){
            for(int i=0;i<nodes.size();i++){
                if(nodes.get(i)==node){
                    nodes.remove(i);
                    return;
                }
            }
        }
    }



    public Collection<? extends Node> getNodes(){
        return nodes;
    }

    public Collection<? extends Edge> getEdges(){
        return edges;
    }

    public void simulate(double t){
        synchronized(nodes){
            double edgeLength=this.edgeLength/(double)nodes.size();

            for(int i=0;i<nodes.size();i++){
                EuclideanNode n1=nodes.get(i);
                if(!n1.isVisible()) continue;
                for(int j=i+1;j<nodes.size();j++){
                    EuclideanNode n2=nodes.get(j);
                    if(!n2.isVisible()) continue;

                    Vector3 v1=n1.getPos();
                    Vector3 v2=n2.getPos();

                    Vector3 d=v1.diff(v2);

                    double f=1.0/d.lengthSquared();
                    if(f>10.0) f=10.0;
                    f*=repelForce;
                    d=d.normalize();

                    n1.addDelta(d.mul(-f));
                    n2.addDelta(d.mul(f));
                }
            }

            for(int i=0;i<edges.size();i++){
                EuclideanEdge e=edges.get(i);
                EuclideanNode n1=(EuclideanNode)e.getNode1();
                EuclideanNode n2=(EuclideanNode)e.getNode2();
                if(!n1.isVisible() || !n2.isVisible() || !e.isVisible()) continue;

                Vector3 v1=n1.getPos();
                Vector3 v2=n2.getPos();

                Vector3 d=v1.diff(v2);

                double l=d.length();
                d.mul(1.0/l);

                if(l>edgeLength){
                    double f=(l-edgeLength)/edgeLength*edgeForce;
                    n1.addDelta(d.mul(f));
                    n2.addDelta(d.mul(-f));
                }
            }

            for(int i=0;i<nodes.size();i++){
                EuclideanNode n=nodes.get(i);

                if(!n.isPinned()){
                    Vector3 v=n.getPos();
                    Vector3 d=n.getDelta();

                    n.setPos(v.add(d));
                }
                n.resetDelta();
            }

        }
    }

    public void makeRandomWorld(int numNodes, int numEdges){
        for(int i=0;i<numNodes;i++){
            SphericalNode n=(SphericalNode)addNode( i );
            // not uniform distribution but good enough
            n.randomPos();
            n.setSize(Math.random()*5.0);
            Vector3 c=new Vector3(Math.random()*1.0, Math.random()*1.0, Math.random()*1.0);
            c=c.normalize();
            n.setColor(new Color((float)c.x,(float)c.y,(float)c.z));
        }
        for(int i=0;i<numEdges;i++){
            int i1=(int)(Math.random()*(double)nodes.size());
            int i2=(int)(Math.random()*(double)(nodes.size()-1));
            if(i2>=i1) i2++;
            Node n1=nodes.get(i1);
            Node n2=nodes.get(i2);
            addEdge(n1, n2);
        }
    }

}
