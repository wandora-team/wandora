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
 * ClearToolLocks.java
 *
 * Created on 13. lokakuuta 2006, 23:14
 *
 */

package org.wandora.application.tools;



import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.*;

import javax.swing.*;


/**
 * Clears all tool locks preventing sequential tool executions. Notice that clearing 
 * locks doesn't stop threads running in tools.
 *
 * @author akivela
 */


public class ClearToolLocks extends AbstractWandoraTool implements WandoraTool {
    
    /** Creates a new instance of ClearToolLocks */
    public ClearToolLocks() {
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        int n = clearToolLocks();
        if(n > 0) {
            log("There exists "+n+" tool(s) locked. All tool locks released. "+
                "Unlocked tools may be executed once again but they may fail "+
                "depending on the execution history of the tool.");
        }
        else {
            log("There is no tools locked at the moment. No locks released.");
        }
    }
    
    
    @Override
    public boolean runInOwnThread() {
        return false;
    }
    
    @Override
    public boolean allowMultipleInvocations(){
        return true;
    }
    
    @Override
    public String getName() {
        return "Clear tool locks";
    }
    
    @Override
    public String getDescription() {
        return "Clears all tool locks and enable sequential tool executions. Notice that clearing locks doesn't stop threads in tools.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/clear_tool_locks.png");
    }
}
