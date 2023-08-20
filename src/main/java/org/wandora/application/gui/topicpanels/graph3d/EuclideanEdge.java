
package org.wandora.application.gui.topicpanels.graph3d;

/**
 *
 * @author olli
 */
public class EuclideanEdge extends AbstractEdge {

    public EuclideanEdge(EuclideanNode n1, EuclideanNode n2) {
        super(n1,n2);
    }

    public Vector3[] line3(double granularity) {
        Vector3 v1=n1.getPos();
        Vector3 v2=n2.getPos();
        Vector3 d=v1.diff(v2);
        double l=d.length();

        int segments=Math.min((int)Math.ceil(l/granularity)+1,100);

        d=d.mul(1.0/(segments-1));
        Vector3[] ret=new Vector3[segments];
        Vector3 p=v1;
        for(int i=0;i<ret.length;i++){
            ret[i]=p;
            p=p.add(d);
        }
        return ret;
    }

}
