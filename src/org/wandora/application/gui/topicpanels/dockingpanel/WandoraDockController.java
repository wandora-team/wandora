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
 */



package org.wandora.application.gui.topicpanels.dockingpanel;

import bibliothek.gui.DockController;
import bibliothek.gui.DockTheme;
import bibliothek.gui.dock.util.IconManager;
import bibliothek.gui.dock.util.Priority;
import bibliothek.gui.dock.util.color.ColorManager;
import bibliothek.gui.dock.util.font.FontManager;
import java.awt.Color;
import org.wandora.application.gui.UIConstants;

/**
 *
 * @author akivela
 */


public class WandoraDockController extends DockController {
    
    
    public WandoraDockController() {
        super();
        DockTheme dockTheme = new WandoraDockTheme();
        this.setTheme(dockTheme);
        
        ColorManager colorManager = this.getColors();
        colorManager.put(Priority.CLIENT, "title.active", UIConstants.defaultActiveBackground);
        
        FontManager fontManager = this.getFonts();

        IconManager iconManager = this.getIcons();
    }
    
}
