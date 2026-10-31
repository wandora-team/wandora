
package org.wandora.application.gui.topicpanels.graph3d;

import java.awt.Color;

/**
 *
 * @author olli
 */
public interface Edge {
    public Node getNode1();
    public Node getNode2();
    public void setEdgeObject(Object o);
    public Object getEdgeObject();
    public Color getColor();
    public void setColor(Color c);
    public boolean isVisible();
    public void setVisible(boolean b);
    public Vector3[] line3(double granularity);
}
