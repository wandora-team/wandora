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
 * SimpleTree.java
 *
 * Created on 29. joulukuuta 2005, 20:14
 *
 */

package org.wandora.application.gui.simple;


import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.wandora.application.gui.*;



import org.wandora.application.*;

/**
 *
 * @author akivela
 */
public class SimpleTree extends JTree implements SimpleComponent {
    
    /** Creates a new instance of SimpleTree */
    public SimpleTree() {
        this.putClientProperty("JTree.lineStyle", "None");
        this.setFocusable(true);
        this.addFocusListener(this);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    
    
    
    @Override
    public void focusGained(java.awt.event.FocusEvent focusEvent) {
        Wandora w = Wandora.getWandora(this);
        if(w != null) {
            w.gainFocus(this);
        }
    }
    
    @Override
    public void focusLost(java.awt.event.FocusEvent focusEvent) {
    }
    
    
    
    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }
}
