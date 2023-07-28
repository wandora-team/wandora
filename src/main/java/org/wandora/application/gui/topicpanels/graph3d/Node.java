
package org.wandora.application.gui.topicpanels.graph3d;

import java.awt.Color;

/**
 *
 * @author olli
 */
public interface Node {
    public Object getNodeObject();
    public Vector3 getPos();
    public void setPos(Vector3 pos);
    public double getSize();
    public void setSize(double s);
    public Color getColor();
    public void setColor(Color c);
    public boolean isVisible();
    public void setVisible(boolean b);
}
