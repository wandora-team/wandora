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
 * ResetWandora.java
 *
 * Created on September 21, 2004, 4:50 PM
 */

package org.wandora.application.tools.project;



import org.wandora.application.tools.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.*;
import javax.swing.*;


/**
 *
 * @author  akivela
 */
public class ResetWandora extends AbstractWandoraTool implements WandoraTool {
    

    public ResetWandora() {
    }
    
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/new_project.png");
    }
    
    @Override
    public String getName() {
        return "Restart Wandora";
    }

    @Override
    public String getDescription() {
        return "Restarts Wandora application.";
    }
    
    
    public void execute(Wandora wandora, Context context) {
        int a = WandoraOptionPane.showConfirmDialog(wandora, "Starting new Wandora project restarts Wandora. Restarting Wandora application you'll loose all changes you have made. Do you really want to restart Wandora?", "Restarting Wandora");
        if(a == WandoraOptionPane.YES_OPTION) {
            wandora.resetWandora();
        }
    }
    
}
