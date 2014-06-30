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
 * SimpleMenu.java
 *
 * Created on 2. marraskuuta 2005, 15:27
 *
 */

package org.wandora.application.gui.simple;

import javax.swing.*;
import java.awt.*;
import org.wandora.application.gui.*;




/**
 *
 * @author akivela
 */
public class SimpleMenu extends JMenu {
    
    /** Creates a new instance of SimpleMenu */
    public SimpleMenu() {
        super();
    }
    
    public SimpleMenu(String menuName) {
        this(menuName, UIBox.getIcon("gui/icons/empty.png"));
    }
    
    public SimpleMenu(String menuName, Icon icon) {
        if(menuName.startsWith("[") && menuName.endsWith("]")) {
            setEnabled(false);
            menuName = menuName.substring(1, menuName.length()-1);
        }
        setFont(UIConstants.menuFont);
        UIConstants.setFancyFont(this);
        setForeground(UIConstants.menuColor);
        setText(menuName);
        setName(menuName);
        setIcon(icon);
    }
    
    
    @Override
    public void paint(Graphics g) {
        UIConstants.preparePaint(g);
        super.paint(g);
    }
    
}
