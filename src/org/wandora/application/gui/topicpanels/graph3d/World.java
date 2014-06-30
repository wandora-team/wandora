
package org.wandora.application.gui.topicpanels.graph3d;

import java.util.Collection;

/**
 *
 * @author olli
 */
public interface World {
    public void simulate(double time);
    public Node addNode(Object nodeObject);
    public Edge addEdge(Node node1,Node node2);
    public void removeNode(Node node);
    public void removeEdge(Edge edge);
    public Collection<? extends Node> getNodes();
    public Collection<? extends Edge> getEdges();
    public void setProperty(String prop,Object value);
}
