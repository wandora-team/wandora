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
 * 
 * Created on Oct 19, 2011, 8:12:21 PM
 */

package org.wandora.application.gui.topicpanels.treemap;

/**
 *
 * @author elias, akivela
 */


public class Rect {
    public double x=0;
    public double y=0;
    public double w=1;
    public double h=1;

    public Rect() {
        this(0,0,1,1);
    }

    public Rect(Rect r) {
       setRect(r.x, r.y, r.w, r.h);
    }

    public Rect(double x, double y, double w, double h) {
       setRect(x, y, w, h);
    }

    public void setRect(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public double aspectRatio() {
        return Math.max(w/h, h/w);
    }

    public double distance(Rect r) {
        return Math.sqrt((r.x-x)*(r.x-x)+
                         (r.y-y)*(r.y-y)+
                         (r.w-w)*(r.w-w)+
                         (r.h-h)*(r.h-h));
    }

    public Rect copy() {
        return new Rect(x,y,w,h);
    }

    @Override
    public String toString() {
        return "Rect: "+x+", "+y+", "+w+", "+h;
    }
}


