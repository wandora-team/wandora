/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * DefaultProjection.java
 *
 * Created on 18. heinäkuuta 2007, 15:25
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel.projections;


import org.wandora.utils.Options;
import org.wandora.application.gui.*;
import org.wandora.application.gui.topicpanels.graphpanel.*;
import org.wandora.utils.*;
import static org.wandora.utils.Tuples.*;
import java.awt.*;
import javax.swing.*;


/**
 *
 * @author olli, akivela
 */

public class DefaultProjection implements Projection {
    
    
    private double scale = 1.0;
    private double viewWidth = 0;
    private double viewHeight = 0;
    private double viewX = 0;
    private double viewY = 0;
    
    
    private double projectionSettings[][] = new double[][] 
        { { 0.8 },
          { 1.0 },
          { 2.0 }, // scale
          { 4.0 },
          { 8.0 },
          { 16.0 } };
    private int nextProjectionSettings = 0;
    public void useNextProjectionSettings() {
        if(nextProjectionSettings >= projectionSettings.length) nextProjectionSettings = 0;
        scale = projectionSettings[nextProjectionSettings][0]; 
        nextProjectionSettings++;
    }

    public void modify(int param, double delta) {
        modify(param, delta, 1.5);
    }
    public void modify(int param, double delta, double multiplier) {
        switch(param) {
            case SCALE: {
                if(delta<0) scale*=Math.pow(multiplier,-delta);
                else scale/=Math.pow(multiplier,delta);
                break;
            }
        }
    }
    public void set(int param, double value) {
        switch(param) {
            case VIEW_X: viewX = value;
            case VIEW_Y: viewY = value;
            case VIEW_WIDTH: viewWidth = value;
            case VIEW_HEIGHT: viewHeight = value;
            case SCALE: scale = value;
        }
    }
    public double get(int param) {
        switch(param) {
            case VIEW_X: return viewX;
            case VIEW_Y: return viewY;
            case VIEW_WIDTH: return viewWidth;
            case VIEW_HEIGHT: return viewHeight;
            case SCALE: return scale;
        }
        return 0.0;
    }

    public double scale(double x, double y) {
        return scale;
    }
    public T2<Double,Double> worldToScreen(double x,double y){
        return t2(viewWidth/2+(x-viewX)*scale,viewHeight/2-(y-viewY)*scale);
    }
    public T2<Double,Double> screenToWorld(double x,double y){
        return t2((x-viewWidth/2)/scale+viewX,-(y-viewHeight/2)/scale+viewY);
    }
    public void draw(Graphics2D g){}

    public void initialize(Options options, String prefix) {
        scale=options.getDouble(prefix+"scale", 1.0);
        viewX=options.getDouble(prefix+"viewx", 0.0);
        viewY=options.getDouble(prefix+"viewy", 0.0);
    }

    
    public String getName() {
        return "Default projection";
    }
    public String getDescription() {
        return getName();
    }
    
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/empty.png");
    }
}
