/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * 
 * HyperbolicProjection.java
 *
 * Created on 18.7.2007, 15:17
 *
 */



package org.wandora.application.gui.topicpanels.graphpanel.projections;

import static org.wandora.utils.Tuples.t2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.Icon;

import org.wandora.application.gui.UIBox;
import org.wandora.utils.Options;
import org.wandora.utils.Tuples.T2;



/**
 *
 * @author olli, akivela
 */
public class HyperbolicProjection implements Projection {
    public static final double MAX_SCALE_VALUE = 5.0;
    public static final double MIN_SCALE_VALUE = 0.05;
    public static final double MAX_CURVATURE_VALUE = 10.0;
    public static final double MIN_CURVATURE_VALUE = 0.5;
    
    
    private double scale = 1.0;
    private double curvature = 1.0;
    private double viewWidth = 0;
    private double viewHeight = 0;
    private double viewX = 0;
    private double viewY = 0;
    private Options options = null;
    private String optionsPrefix = "";

    public static final int CURVATURE = 200;
    
    private double projectionSettings[][] = new double[][] 
        { { 0.9, 0.8 },
          { 1.0, 1.0 },
          { 1.2, 2.0 }, // scale, curvature
          { 1.5, 4.0 },
          { 1.7, 8.0 },
          { 2.0, 16.0 } };
    private int nextProjectionSettings = 0;
    
    
    @Override
    public void useNextProjectionSettings() {
        if(nextProjectionSettings >= projectionSettings.length) nextProjectionSettings = 0;
        scale = projectionSettings[nextProjectionSettings][0]; 
        storeValue("scale", scale);
        curvature = projectionSettings[nextProjectionSettings][1];
        storeValue("curvature", curvature);
        nextProjectionSettings++;
        precalc();
    }


    @Override
    public void modify(int param, double delta) {
        modify(param, delta, 1.0);
    }
    
    
    @Override
    public void modify(int param, double delta, double multiplier) {
        switch(param) {
            case VIEW_X: {
                viewX += delta*multiplier;
                storeValue("viewx", viewX);
                break;
            }
            case VIEW_Y: {
                viewY += delta*multiplier;
                storeValue("viewy", viewY);
                break;
            }
            case VIEW_WIDTH: {
                viewWidth += delta;
                break;
            }
            case VIEW_HEIGHT: {
                viewHeight += delta;
                break;
            }
            case SCALE: {
                if(delta<0) scale*=Math.pow(multiplier,-delta);
                else scale/=Math.pow(multiplier,delta);
                if(scale > MAX_SCALE_VALUE) scale = MAX_SCALE_VALUE;
                if(scale < MIN_SCALE_VALUE) scale = MIN_SCALE_VALUE;
                storeValue("scale", scale);
                break;
            }
            case CURVATURE: {
                if(delta<0) curvature*=Math.pow(multiplier,-delta);
                else curvature/=Math.pow(multiplier,delta);
                if(curvature > MAX_CURVATURE_VALUE) curvature = MAX_CURVATURE_VALUE;
                if(curvature < MIN_CURVATURE_VALUE) curvature = MIN_CURVATURE_VALUE;
                storeValue("curvature", curvature);
                break;
            }
        }
        precalc();
    }
    
    
    @Override
    public void set(int param, double value) {
        switch(param) {
            case VIEW_X: { 
                viewX = value;
                storeValue("viewx", value);
                break;
            }
            case VIEW_Y: { 
                viewY = value;
                storeValue("viewy", value);
                break; 
            }
            case VIEW_WIDTH: { 
                viewWidth = value; 
                break; 
            }
            case VIEW_HEIGHT: { 
                viewHeight = value; 
                break; 
            }
            case SCALE: { 
                scale = value;
                storeValue("scale", value);
                break; 
            }
            case CURVATURE: { 
                curvature = value;
                storeValue("curvature", value);
                break; 
            }
        }
        precalc();
    }
    
    
    @Override
    public double get(int param) {
        switch(param) {
            case VIEW_X: return viewX;
            case VIEW_Y: return viewY;
            case VIEW_WIDTH: return viewWidth;
            case VIEW_HEIGHT: return viewHeight;
            case SCALE: return scale;
            case CURVATURE: return curvature;
        }
        return 0.0;
    }
    
    
    private void precalc(){
        viewR=viewWidth;
        if(viewHeight<viewWidth) viewR=viewHeight;
        viewR/=2.0/curvature;
        A=viewR*viewR/scale;        
        ApViewR=A/viewR;
    }
    
    
    private double viewR=0.0;
    private double A=0.0;
    private double ApViewR=0.0;
    
    
    @Override
    public double scale(double x, double y) {
        double dx=x-viewX;
        double dy=y-viewY;
        double worldR=Math.sqrt(dx*dx+dy*dy);
        double screenR1=worldR*viewR/(worldR+ApViewR);
        double screenR2=(worldR+1.0)*viewR/(worldR+1.0+ApViewR);
//            return (screenR2-screenR1)*getScale();
        return screenR2-screenR1;
    }
    
    
    @Override
    public T2<Double,Double> worldToScreen(double x,double y){
        double dx=x-viewX;
        double dy=y-viewY;
        double worldR=Math.sqrt(dx*dx+dy*dy);
        double screenR=worldR*viewR/(worldR+ApViewR);

        return t2(viewWidth/2.0+dx/worldR*screenR,viewHeight/2.0-dy/worldR*screenR);
    }

    
    @Override
    public T2<Double,Double> screenToWorld(double x,double y){
        double dx=x-viewWidth/2.0;
        double dy=viewHeight/2.0-y;
        double screenR=Math.sqrt(dx*dx+dy*dy);
        double worldR=0;
        if(screenR>=viewR) return t2(Double.NaN,Double.NaN);
        else worldR=A/(viewR-screenR)-ApViewR;
        return t2(viewX+dx/screenR*worldR,viewY+dy/screenR*worldR);
    }
    
    
    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(new BasicStroke(1));
        g.drawOval((int)(viewWidth/2.0-viewR),(int)(viewHeight/2.0-viewR),
                (int)(viewR*2.0),(int)(viewR*2.0));
    }

    
    @Override
    public void initialize(Options options, String prefix) {
        this.options = options;
        this.optionsPrefix = prefix;
        
        scale=options.getDouble(prefix+"scale", 1.0);
        curvature=options.getDouble(prefix+"curvature", 1.0);
        viewX=options.getDouble(prefix+"viewx", 0.0);
        viewY=options.getDouble(prefix+"viewy", 0.0);
        precalc();
    }

    
    private synchronized void storeValue(String key, double value) {
        if(options != null) {
            options.put(optionsPrefix+key, value);
        }
    }
    
    
    @Override
    public String getName() {
        return "Hyperbolic projection";
    }
    
    
    @Override
    public String getDescription() {
        return getName();
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/empty.png");
    }
    
}
