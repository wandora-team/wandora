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
 * SimpleAWTMenu.java
 *
 */

package org.wandora.application.gui.simple;

import java.awt.*;
import org.wandora.application.gui.*;



/**
 *
 * @author anttirt
 */
public class SimpleAWTMenu extends Menu {
    public SimpleAWTMenu() {
    }
    
    public SimpleAWTMenu(String menuName) {
        if(menuName.startsWith("[") && menuName.endsWith("]")) {
            setEnabled(false);
            menuName = menuName.substring(1, menuName.length()-1);
        }
        setFont(UIConstants.menuFont);
        setLabel(menuName);
        setName(menuName);
    }
    
    
    
    

}
