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
 * WandoraToggleIcon.java
 *
 * Created on 25. marraskuuta 2005, 12:59
 *
 */

package org.wandora.application.gui.simple;



import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import org.wandora.application.gui.*;




/**
 *
 * @author akivela
 */
public class SimpleToggleButton extends JToggleButton {
    
    Icon onIcon = null;
    Icon offIcon = null;
    
    
    
    public SimpleToggleButton(String label) {
        this();
        this.setText(label);
    }
    
    public SimpleToggleButton() {
        this.setFocusPainted(false);
        this.setFont(UIConstants.buttonLabelFont);
        UIConstants.setFancyFont(this);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.setOpaque(true);
        this.setBackground(UIConstants.defaultActiveBackground);
        this.offIcon = UIBox.getIcon("gui/icons/checkbox.png");
        this.onIcon = UIBox.getIcon("gui/icons/checkbox_selected.png");
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
    
    
    public SimpleToggleButton(String onIconResource, String offIconResource) {
        this(onIconResource, offIconResource, false);
    }
    
    public SimpleToggleButton(String onIconResource, String offIconResource, boolean isSelected) {
        this.onIcon = UIBox.getIcon(onIconResource);
        this.offIcon = UIBox.getIcon(offIconResource);
        
        this.setBackground(null);
        this.setForeground(null);
        
        setSelected(isSelected);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setMargin(null);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    
    public SimpleToggleButton(Icon onIcon, Icon offIcon, boolean isSelected) {
        this.onIcon = onIcon;
        this.offIcon = offIcon;
        
        setSelected(isSelected);
    }
    
    
    @Override
    public void setSelected(boolean state) {
        super.setSelected(state);
        if(state) setIcon(onIcon);
        else setIcon(offIcon);
    }
    
    
    
    @Override
    public void paint(Graphics graphics) {
        if(isSelected()) setIcon(onIcon);
        else setIcon(offIcon);
        UIConstants.preparePaint(graphics);
        super.paint(graphics);
    }
    
    
    
}
