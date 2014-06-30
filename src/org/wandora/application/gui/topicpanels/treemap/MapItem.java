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


public class MapItem {
    protected double size;
    protected Rect bounds;
    protected int order = 0;
    protected int depth;

    public void setDepth(int depth) {
        this.depth=depth;
    }

    public int getDepth() {
        return depth;
    }

    public MapItem() {
        this(1,0);
    }

    public MapItem(double size, int order) {
        this.size = size;
        this.order = order;
        bounds = new Rect();
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public Rect getBounds() {
        return bounds;
    }

    public void setBounds(Rect bounds) {
        this.bounds = bounds;
    }

    public void setBounds(double x, double y, double w, double h) {
        bounds.setRect(x, y, w, h);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
