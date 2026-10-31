
package org.wandora.application.gui.topicpanels.graph3d;

/**
 *
 * @author olli
 */
public class Vector3 {
    public double x,y,z;

    public Vector3(){};

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double lengthSquared(){
        return x*x+y*y+z*z;
    }

    public double length(){
        return Math.sqrt(x*x+y*y+z*z);
    }

    public boolean isZero(){
        return x==0 && y==0 && z==0;
    }

    public Vector3 mul(double m){
        return new Vector3(x*m,y*m,z*m);
    }

    public Vector3 add(Vector3 v){
        return new Vector3(x+v.x,y+v.y,z+v.z);
    }

    public Vector3 diff(Vector3 v){
        return new Vector3(v.x-x,v.y-y,v.z-z);
    }

    public Vector3 normalize(){
        double l=length();
        if(l>0) return new Vector3(x/l,y/l,z/l);
        else return this;
    }

    public double dot(Vector3 v){
        return v.x*x+v.y*y+v.z*z;
    }

    public Vector3 cross(Vector3 v){
        return new Vector3(
                    y*v.z-z*v.y,
                    z*v.x-x*v.z,
                    x*v.y-y*v.x
                );
    }

    public double angle(Vector3 v){
        return Math.acos(this.dot(v));
    }

}
