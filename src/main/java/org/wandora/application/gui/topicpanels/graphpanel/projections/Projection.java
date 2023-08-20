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
 * Projection.java
 *
 * Created on 5.6.2007, 10:37
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel.projections;



import javax.swing.Icon;

import org.wandora.utils.Options;
import org.wandora.utils.Tuples.T2;


/**
 *
 * @author olli
 */
public interface Projection {
    public static final int VIEW_X = 10;
    public static final int VIEW_Y = 11;
    public static final int VIEW_WIDTH = 12;
    public static final int VIEW_HEIGHT = 13;
    
    public static final int MOUSEWHEEL1 = 100;
    public static final int MOUSEWHEEL2 = 200;
    public static final int SCALE = 100;
    
    public String getName();
    public String getDescription();
    public Icon getIcon();
    
    public T2<Double,Double> worldToScreen(double x,double y);
    public T2<Double,Double> screenToWorld(double x,double y);
    public double scale(double x,double y);
    public void draw(java.awt.Graphics2D g);
    
    public void modify(int param, double delta, double multiplier);
    public void modify(int param, double delta);
    public void set(int param, double value);
    public double get(int param);
    
    public void initialize(Options options, String prefix);
    public void useNextProjectionSettings();
}
