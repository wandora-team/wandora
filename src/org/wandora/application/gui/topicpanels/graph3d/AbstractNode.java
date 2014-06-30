
package org.wandora.application.gui.topicpanels.graph3d;

import java.awt.Color;

/**
 *
 * @author olli
 */
public class AbstractNode implements Node {
    protected Object nodeObject;

    protected Vector3 pos=new Vector3(1.0,0.0,0.0);
    protected Vector3 delta=new Vector3();

    protected Color color=Color.WHITE;
    protected double size=1.0;

    protected boolean visible=true;

    protected boolean pinned=false;

    public AbstractNode() {
    }

    public AbstractNode(Object nodeObject) {
        this.nodeObject = nodeObject;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }



    public Object getNodeObject() {
        return nodeObject;
    }

    public Vector3 getPos() {
        return pos;
    }
    public void setPos(Vector3 pos){
        this.pos=pos;
    }

    public void setDelta(Vector3 delta){
        this.delta=delta;
    }

    public Vector3 getDelta(){
        return delta;
    }
    public void addDelta(Vector3 d){
        delta=delta.add(d);
    }

    public void resetDelta(){
        delta=new Vector3(0.0,0.0,0.0);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void randomPos(){
        Vector3 p=new Vector3(Math.random()*2.0-1.0,Math.random()*2.0-1.0,Math.random()*2.0-1.0);
        p=p.normalize();
        this.setPos(p);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
