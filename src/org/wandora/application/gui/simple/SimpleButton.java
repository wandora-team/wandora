/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * SimpleButton.java
 *
 * Created on 17. lokakuuta 2005, 19:23
 *
 */

package org.wandora.application.gui.simple;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.swing.*;
import org.wandora.application.gui.*;






/**
 *
 * @author akivela
 */
public class SimpleButton extends JButton {
    
    /** Creates a new instance of SimpleButton */
    public SimpleButton() {
        initialize();
    }
    public SimpleButton(String label) {
        initialize();
        setText(label);
    }
    public SimpleButton(Icon icon) {
        initialize();
        this.setIcon(icon);
    }
    
    
    
    protected void initialize() {

        this.setFocusPainted(false);
        this.setFont(UIConstants.buttonLabelFont);
        UIConstants.setFancyFont(this);

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.setOpaque(true);
        this.setBackground(UIConstants.defaultActiveBackground);
        this.addMouseListener(
            new MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if(evt.getComponent().isEnabled())
                        evt.getComponent().setBackground(UIConstants.defaultActiveBackground);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if(evt.getComponent().isEnabled())
                        evt.getComponent().setBackground(UIConstants.defaultActiveBackground);
                }
            }
        );
    }
    
    
    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }
    
    
    
}
