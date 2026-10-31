
package org.wandora.application.gui.topicpanels.graph3d;

import java.awt.Color;

/**
 *
 * @author olli
 */
public abstract class AbstractEdge implements Edge{
    protected Object edgeObject;

    protected AbstractNode n1;
    protected AbstractNode n2;
    protected Color color=Color.WHITE;

    protected boolean visible=true;

    public AbstractEdge(AbstractNode n1, AbstractNode n2) {
        this.n1 = n1;
        this.n2 = n2;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Node getNode1() {
        return n1;
    }

    public Node getNode2() {
        return n2;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Object getEdgeObject() {
        return edgeObject;
    }

    public void setEdgeObject(Object edgeObject) {
        this.edgeObject = edgeObject;
    }

}
