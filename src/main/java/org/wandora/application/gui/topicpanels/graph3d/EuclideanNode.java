
package org.wandora.application.gui.topicpanels.graph3d;

/**
 *
 * @author olli
 */
public class EuclideanNode extends AbstractNode {

    public EuclideanNode() {
    }

    public EuclideanNode(Object nodeObject) {
        super(nodeObject);
    }

    public double distance(SphericalNode n){
        return n.pos.diff(pos).length();
    }

}
