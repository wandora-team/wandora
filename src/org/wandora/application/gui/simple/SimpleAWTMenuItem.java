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
 * SimpleAWTMenuItem.java
 *
 */

package org.wandora.application.gui.simple;


import java.awt.*;
import java.awt.event.ActionListener;
import org.wandora.application.gui.*;



/**
 *
 * @author anttirt
 */
public class SimpleAWTMenuItem extends MenuItem {
    public SimpleAWTMenuItem() {
    }
    
    
    public SimpleAWTMenuItem(String menuName) {
        this(menuName, null);
    }
    
    
    public SimpleAWTMenuItem(String menuName, ActionListener defaultListener) {
        boolean enabled = true;
        //String iconResource = "gui/icons/empty.png";
        
        if(menuName.startsWith("[") && menuName.endsWith("]")) {
            enabled = false;
            menuName = menuName.substring(1, menuName.length()-1);
        }
        if(menuName.startsWith("X ") || menuName.startsWith("O ")) {
            String name = menuName.substring(2);
            if(menuName.startsWith("X ")) {
               //menuItem=new javax.swing.JCheckBoxMenuItem(name, true);
                //iconResource = "gui/icons/checked.png";
            }
            else {
                //menuItem=new javax.swing.JCheckBoxMenuItem(name, false);
                //iconResource = "gui/icons/unchecked.png";
            }
            menuName = name;
        }

        //if(iconResource != null) setIcon(UIBox.getIcon(iconResource));
        setFont(UIConstants.menuFont);
        setLabel(menuName);
        setName(menuName);
        setActionCommand(menuName);
        if(defaultListener != null) addActionListener(defaultListener);
        setEnabled(enabled);
    }
    
    
    

}
