
package org.wandora.application.gui.topicpanels.graph3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author olli
 */
public class SphericalWorld implements World {

    private final ArrayList<SphericalNode> nodes=new ArrayList<SphericalNode>();
    private final ArrayList<SphericalEdge> edges=new ArrayList<SphericalEdge>();

    private static final double REPEL_FORCE=0.1;
    private static final double EDGE_FORCE=0.1;

    public double repelForce=REPEL_FORCE;
    public double edgeForce=EDGE_FORCE;
    public double edgeLength=10.0;

    public void setProperty(String prop, Object value) {
        if(prop.equals("repelForce")) repelForce=(Double)value;
        else if(prop.equals("edgeForce")) edgeForce=(Double)value;
        else if(prop.equals("edgeLength")) edgeLength=(Double)value;
    }


    public Edge addEdge(Node node1, Node node2) {
        if(!(node1 instanceof SphericalNode) || !(node2 instanceof SphericalNode)) throw new ClassCastException();
        synchronized(nodes){ // synchronize both nodes and edges with nodes list
            SphericalEdge e=new SphericalEdge((SphericalNode)node1, (SphericalNode)node2);
            edges.add(e);
            return e;
        }
    }

    public Node addNode(Object nodeObject) {
        synchronized(nodes){
            SphericalNode n=new SphericalNode(nodeObject);
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
                SphericalNode n1=nodes.get(i);
                if(!n1.isVisible()) continue;
                for(int j=i+1;j<nodes.size();j++){
                    SphericalNode n2=nodes.get(j);
                    if(!n2.isVisible()) continue;

                    Vector3 v1=n1.getPos();
                    Vector3 v2=n2.getPos();

                    Vector3 c=v1.cross(v2);
                    double cl=c.length();

                    // this is the angle between v1 and v2, and thus effectively
                    // the distance between the two points
                    double d=Math.atan2(cl, v1.dot(v2));
                    double f=repelForce/(d*d);

                    c=c.mul(1/cl); // normalize c

                    Vector3 c1=v1.cross(c).mul(f);
                    n1.addDelta(c1);
                    Vector3 c2=c.cross(v2).mul(f);
                    n2.addDelta(c2);
                }
            }

            for(int i=0;i<edges.size();i++){
                SphericalEdge e=edges.get(i);
                SphericalNode n1=(SphericalNode)e.getNode1();
                SphericalNode n2=(SphericalNode)e.getNode2();
                if(!n1.isVisible() || !n2.isVisible() || !e.isVisible()) continue;

                Vector3 v1=n1.getPos();
                Vector3 v2=n2.getPos();

                Vector3 c=v1.cross(v2);
                double cl=c.length();

                // this is the angle between v1 and v2, and thus effectively
                // the distance between the two points
                double d=Math.atan2(cl, v1.dot(v2));

                if(d>edgeLength){
                    double f=(d-edgeLength)/edgeLength*edgeForce;

                    c=c.mul(1/cl); // normalize c

                    Vector3 c1=c.cross(v1).mul(f);
                    n1.addDelta(c1);
                    Vector3 c2=v2.cross(c).mul(f);
                    n2.addDelta(c2);
                }
            }

            for(int i=0;i<nodes.size();i++){
                SphericalNode n=nodes.get(i);

                if(!n.isPinned()){
                    Vector3 v=n.getPos();
                    Vector3 d=n.getDelta();

                    Vector3 c=v.cross(d);
                    double theta=c.length();

                    if(Math.abs(theta)>1e-10){
                        c=c.mul(1/theta);
                        theta*=t;

                        double costheta=Math.cos(theta);
                        Vector3 newpos=v.mul(costheta).add(c.cross(v).mul(Math.sin(theta))).add(c.mul(c.dot(v)*(1.0-costheta)));
                        n.setPos(newpos);
                    }
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
