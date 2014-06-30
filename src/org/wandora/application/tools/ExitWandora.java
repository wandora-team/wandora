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
 * 
 * ExitWandora.java
 *
 * Created on September 21, 2004, 4:50 PM
 */

package org.wandora.application.tools;



import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import javax.swing.*;


/**
 * Class implements a Wandora tool used to exit the application.
 *
 * @author  akivela
 */



public class ExitWandora extends AbstractWandoraTool implements WandoraTool {
    
    /** Creates a new instance of ExitWandora */
    public ExitWandora() {
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/exit.png");
    }

    @Override
    public String getName() {
        return "Exit Wandora";
    }

    @Override
    public String getDescription() {
        return "Exit Wandora application.";
    }
    
    
    @Override
    public void execute(Wandora wandora, Context context) {
        wandora.tryExit();
    }

    @Override
    public boolean runInOwnThread() {
        return false;
    }
    
    @Override
    public boolean requiresRefresh() {
        return false;
    }
}
