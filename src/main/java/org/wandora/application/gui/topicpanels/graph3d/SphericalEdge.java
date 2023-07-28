
package org.wandora.application.gui.topicpanels.graph3d;

import java.awt.Color;

/**
 *
 * @author olli
 */
public class SphericalEdge extends AbstractEdge {

    public SphericalEdge(SphericalNode n1, SphericalNode n2) {
        super(n1,n2);
    }

    public Vector3[] line3(double granularity){
        Vector3 a=n1.getPos();
        Vector3 b=n2.getPos();

        double theta0=a.angle(b);
        int segments=(int)Math.ceil(theta0/granularity)+1;
        Vector3[] ret=new Vector3[segments];
        double dtheta=theta0/(double)(segments-1);

        Vector3 rot=a.cross(b);
        if(rot.isZero()) rot=new Vector3(a.y,a.z,a.x);
        else rot=rot.normalize();

        Vector3 rotcrossa=rot.cross(a);
        double rotdota=rot.dot(a);

        double theta=0;
        for(int i=0;i<segments;i++,theta+=dtheta){
            double costheta=Math.cos(theta);
            ret[i]=a.mul(costheta).add(rotcrossa.mul(Math.sin(theta))).add(rot.mul(rotdota).mul(1-costheta));
        }
        return ret;
    }


}
