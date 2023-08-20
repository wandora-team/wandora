
package org.wandora.application.gui.topicpanels.graph3d;

/**
 *
 * @author olli
 */
public class SphericalNode extends AbstractNode {


    public SphericalNode() {
    }

    public SphericalNode(Object nodeObject) {
        super(nodeObject);
    }

    public double distance(SphericalNode n){
        return pos.angle(n.pos);
    }

    public void randomPos(){
        Vector3 p=new Vector3(Math.random()*2.0-1.0,Math.random()*2.0-1.0,Math.random()*2.0-1.0);
        p=p.normalize();
        this.setPos(p);
    }


}
