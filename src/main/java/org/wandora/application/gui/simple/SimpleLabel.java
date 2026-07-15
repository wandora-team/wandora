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
 * Created on November 12, 2004, 3:21 PM
 */

package org.wandora.application.gui.simple;

import java.awt.Graphics;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import org.wandora.application.gui.UIConstants;



/**
 *
 * @author  akivela
 */
public class SimpleLabel extends JLabel implements MouseListener {
    
    private static final long serialVersionUID = 1L;
    
    /** Creates a new instance of SimpleLabel */
    public SimpleLabel() {
        initialize();
    }
    
    public SimpleLabel(String text){
        super(text);
        initialize();
    }
    
    
    
    public void initialize() {
        this.addMouseListener(this);
        this.setFont(UIConstants.labelFont);
        UIConstants.setFancyFont(this);
        this.setBorder(UIConstants.defaultLabelBorder);
    }
    
    
    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
    }
    
    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
    }
    
    
    

    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }
    
    
}
