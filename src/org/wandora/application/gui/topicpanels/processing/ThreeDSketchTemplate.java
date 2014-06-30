

/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2014 Wandora Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * ThreeDSketchTemplate.java
 *
 */
package org.wandora.application.gui.topicpanels.processing;
import java.util.ArrayList;
import org.wandora.application.gui.topicpanels.graph3d.Vector3;
import processing.core.*;

/**
 *
 * @author olli
 */
public class ThreeDSketchTemplate extends SketchTemplate {

    protected final ArrayList<ThreeDObject> objects=new ArrayList<ThreeDObject>();

    protected PGraphics clickBuffer;

    protected String renderer=P3D;

    public ThreeDSketchTemplate() {
        PGraphics g=null;
        
    }
    
    public void addObject(ThreeDObject o){
        synchronized(objects){
            objects.add(o);
        }
    }
    
    public void removeObject(ThreeDObject o){
        synchronized(objects){
            objects.remove(o);
        }
    }
    
    public void prepareBuffer(){
        
    }
    
    private int getColor(int id){return -(id+2);}
    private int getId(int color){return -(color+2);}
    
    public ThreeDObject findObject(){
        if(clickBuffer==null){
            clickBuffer=createGraphics(width,height,renderer);
        }

        clickBuffer.beginDraw();
        clickBuffer.noSmooth();
        clickBuffer.background(getColor(-1));
        clickBuffer.noStroke();
        PMatrix3D m=g.getMatrix((PMatrix3D)null);
        clickBuffer.setMatrix(m);
        
        for(int i=0;i<objects.size();i++){
            ThreeDObject o=objects.get(i);
            if(o.isVisible()) o.drawBuffer(clickBuffer, getColor(i));
        }
        clickBuffer.endDraw();
        
        int color=clickBuffer.get(mouseX,mouseY);
        int id=getId(color);
        if(id>=0 && id<objects.size()) return objects.get(id);
        else return null;
    }

    public void drawObjects() {
        for(ThreeDObject o : objects){
            if(o.isVisible()) o.draw(g);
        }
    }


    public void applyMatrix(PGraphics g,float[] m){
        if(m.length==12){
            g.applyMatrix(
                m[0],m[1],m[2],m[3],
                m[4],m[5],m[6],m[7],
                m[8],m[9],m[10],m[11],
                0,0,0,1
            );
        }
        else applyMatrix(g, m, 0, 0, 0);
    }
    public void applyMatrix(PGraphics g,float[] m,float tx,float ty,float tz){
        if(m.length!=9) throw new RuntimeException("matrix size mismatch");
        g.applyMatrix(
            m[0],m[1],m[2],tx,
            m[3],m[4],m[5],ty,
            m[6],m[7],m[8],tz,
            0,0,0,1
        );
    }

    public float[] invertMatrix(float[] m){
        if(m.length==9){
            float det=m[0]*(m[4]*m[8]-m[5]*m[7])+m[1]*(m[5]*m[6]-m[3]*m[8])+m[2]*(m[3]*m[7]-m[4]*m[6]);
            if(det!=0.0f){
                det=1f/det;
                return new float[]{
                    det*(m[4]*m[8]-m[5]*m[7]), det*(m[2]*m[7]-m[1]*m[8]), det*(m[1]*m[5]-m[2]*m[4]),
                    det*(m[5]*m[6]-m[3]*m[8]), det*(m[0]*m[8]-m[2]*m[6]), det*(m[2]*m[3]-m[0]*m[5]),
                    det*(m[3]*m[7]-m[4]*m[6]), det*(m[6]*m[1]-m[0]*m[7]), det*(m[0]*m[4]-m[1]*m[3])
                };
            }
            else return null;
        }
        else if(m.length==12) {
//            float det=m[0]*m[5]*m[10]+m[1]*m[6]*m[8]+m[2]*m[4]*m[9]
//                      -m[0]*m[10]*m[9]-m[1]*m[4]*m[10]-m[2]*m[5]*m[8];
            float det=m[0]*m[5]*m[10]+m[1]*m[6]*m[8]+m[2]*m[4]*m[9]
                      -m[0]*m[6]*m[9]-m[1]*m[4]*m[10]-m[2]*m[5]*m[8];
            if(det!=0.0f){
                det=1f/det;
                return new float[]{
                    det*(m[5]*m[10]-m[6]*m[ 9]), det*(m[2]*m[ 9]-m[1]*m[10]), det*(m[1]*m[6]-m[2]*m[5]), det*(m[1]*m[7]*m[10]+m[2]*m[5]*m[11]+m[3]*m[6]*m[ 9]-m[1]*m[6]*m[11]-m[2]*m[7]*m[ 9]-m[3]*m[5]*m[10]),
                    det*(m[6]*m[ 8]-m[4]*m[10]), det*(m[0]*m[10]-m[2]*m[ 8]), det*(m[2]*m[4]-m[0]*m[6]), det*(m[0]*m[6]*m[11]+m[2]*m[7]*m[ 8]+m[3]*m[4]*m[10]-m[0]*m[7]*m[10]-m[2]*m[4]*m[11]-m[3]*m[6]*m[ 8]),
                    det*(m[4]*m[ 9]-m[5]*m[ 8]), det*(m[1]*m[ 8]-m[0]*m[ 9]), det*(m[0]*m[5]-m[1]*m[4]), det*(m[0]*m[7]*m[ 9]+m[1]*m[4]*m[11]+m[3]*m[5]*m[ 8]-m[0]*m[5]*m[11]-m[1]*m[7]*m[ 8]-m[3]*m[4]*m[ 9])
                };
            }
            else return null;
        }
        else throw new RuntimeException("invalid size matrix");
    }

    public float[] axisAngleMatrix(Vector3 a,double t){
        float ct=(float)Math.cos(t);
        float st=(float)Math.sin(t);
        float x=(float)a.x;
        float y=(float)a.y;
        float z=(float)a.z;
        return new float[]{
            ct+x*x*(1-ct), x*y*(1-ct)-z*st, x*z*(1-ct)+y*st,
            x*y*(1-ct)+z*st, ct+y*y*(1-ct), y*z*(1-ct)-x*st,
            z*x*(1-ct)-y*st, z*y*(1-ct)+x*st, ct+z*z*(1-ct)
        };
    }

    public Vector3 vectorTransform(Vector3 v,float[] rot){ return vectorRotate(v,rot); }

    public float zPos(Vector3 v,float[] rot){
        if(rot.length==9) return (float)(rot[6]*v.x+rot[7]*v.y+rot[8]*v.z);
        else if(rot.length==12) return (float)(rot[8]*v.x+rot[9]*v.y+rot[10]*v.z+rot[11]);
        else throw new RuntimeException("invalid size matrix");
    }
    public float zPos(float x,float y,float z,float[] rot){
        if(rot.length==9) return rot[6]*x+rot[7]*y+rot[8]*z;
        else if(rot.length==12) return rot[8]*x+rot[9]*y+rot[10]*z+rot[11];
        else throw new RuntimeException("invalid size matrix");
    }

    public Vector3 vectorRotate(Vector3 v,float[] rot){
        if(rot.length==9){
            return new Vector3(
                rot[0]*v.x+rot[1]*v.y+rot[2]*v.z,
                rot[3]*v.x+rot[4]*v.y+rot[5]*v.z,
                rot[6]*v.x+rot[7]*v.y+rot[8]*v.z
            );
        }
        else if(rot.length==12){
            return new Vector3(
                rot[0]*v.x+rot[1]*v.y+rot[2]*v.z+rot[3],
                rot[4]*v.x+rot[5]*v.y+rot[6]*v.z+rot[7],
                rot[8]*v.x+rot[9]*v.y+rot[10]*v.z+rot[11]
            );
        }
        else throw new RuntimeException("invalid size matrix");
    }

    public float[] matrixMultiply(float[] m1,float[] m2){
        if(m1.length==9 && m2.length==9) {
            return new float[]{
                m1[0]*m2[0]+m1[1]*m2[3]+m1[2]*m2[6], m1[0]*m2[1]+m1[1]*m2[4]+m1[2]*m2[7], m1[0]*m2[2]+m1[1]*m2[5]+m1[2]*m2[8],
                m1[3]*m2[0]+m1[4]*m2[3]+m1[5]*m2[6], m1[3]*m2[1]+m1[4]*m2[4]+m1[5]*m2[7], m1[3]*m2[2]+m1[4]*m2[5]+m1[5]*m2[8],
                m1[6]*m2[0]+m1[7]*m2[3]+m1[8]*m2[6], m1[6]*m2[1]+m1[7]*m2[4]+m1[8]*m2[7], m1[6]*m2[2]+m1[7]*m2[5]+m1[8]*m2[8],
            };
        }
        else if(m1.length==12 && m2.length==12) {
            return new float[]{
                m1[0]*m2[0]+m1[1]*m2[4]+m1[ 2]*m2[8], m1[0]*m2[1]+m1[1]*m2[5]+m1[ 2]*m2[9], m1[0]*m2[2]+m1[1]*m2[6]+m1[ 2]*m2[10], m1[0]*m2[3]+m1[1]*m2[7]+m1[ 2]*m2[11]+m1[3],
                m1[4]*m2[0]+m1[5]*m2[4]+m1[ 6]*m2[8], m1[4]*m2[1]+m1[5]*m2[5]+m1[ 6]*m2[9], m1[4]*m2[2]+m1[5]*m2[6]+m1[ 6]*m2[10], m1[4]*m2[3]+m1[5]*m2[7]+m1[ 6]*m2[11]+m1[7],
                m1[8]*m2[0]+m1[9]*m2[4]+m1[10]*m2[8], m1[8]*m2[1]+m1[9]*m2[5]+m1[10]*m2[9], m1[8]*m2[2]+m1[9]*m2[6]+m1[10]*m2[10], m1[8]*m2[3]+m1[9]*m2[7]+m1[10]*m2[11]+m1[11]
            };
        }
        else if(m1.length==9 && m2.length==12){
            return matrixMultiply(new float[]{
                m1[0],m1[1],m1[2],0,
                m1[3],m1[4],m1[5],0,
                m1[6],m1[7],m1[8],0,
            },m2);
        }
        else if(m1.length==12 && m2.length==9){
            return matrixMultiply(m1,new float[]{
                m2[0],m2[1],m2[2],0,
                m2[3],m2[4],m2[5],0,
                m2[6],m2[7],m2[8],0,
            });
        }
        else throw new RuntimeException("matrix sizes don't match");
    }

    public static class ThreeDBox extends ThreeDShape {

        public ThreeDBox() {
        }

        public ThreeDBox(float x,float y,float z){
            this.x=x;
            this.y=y;
            this.z=z;
        }
        
        public ThreeDBox(float x,float y,float z,float w,float h,float d){
            this(x,y,z);
            this.w=w;
            this.h=h;
            this.d=d;
        }

        public ThreeDBox(float x,float y,float z,float w,float h,float d,int colour){
            this(x, y, z, w, h, d);
            this.colour=colour;
        }
        
        public void draw(PGraphics g) {
            g.pushMatrix();
            g.translate(x,y,z);
            g.fill(colour);
            g.stroke(strokeColour);
            g.strokeWeight(strokeWeight);
            g.box(w,h,d);
            g.popMatrix();
        }

        public void drawBuffer(PGraphics g, int colour) {
            g.pushMatrix();
            g.noStroke();
            g.fill(colour);
            g.box(w,h,d);
            g.popMatrix();
        }
        
    }

    public static abstract class ThreeDShape implements ThreeDObject {
        public float x,y,z=0.0f;
        public float w,h,d=1.0f;
        public int colour=0xffffff;
        public int strokeWeight=1;
        public int strokeColour=0;
        public Object userObject;
        public boolean isVisible(){return true;}
    }

    public static interface ThreeDObject {
        public void draw(PGraphics g);
        public void drawBuffer(PGraphics g,int colour);
        public boolean isVisible();
    }

}
