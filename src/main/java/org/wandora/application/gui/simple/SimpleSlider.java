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
 * SimpleSlider.java
 *
 */
package org.wandora.application.gui.simple;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JSlider;

/**
 *
 * @author akivela
 */


public class SimpleSlider extends JSlider implements MouseWheelListener {
    
    
    public SimpleSlider(int orientation, int min, int max, int value) {
        super(orientation, min, max, value);
        this.addMouseWheelListener(this);
    }

    
    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(e.getWheelRotation() > 0) this.setValue(this.getValue() - 1);
        else this.setValue(this.getValue() + 1);
    }
    
    
    
}
