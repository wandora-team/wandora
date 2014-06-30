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
 * Back.java
 *
 * Created on 28. huhtikuuta 2006, 14:24
 *
 */

package org.wandora.application.tools.navigate;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.application.gui.*;
import org.wandora.*;
import javax.swing.*;


/**
 *
 * @author olli
 */
public class Back extends AbstractWandoraTool {
    
    /** Creates a new instance of Back */
    public Back() {
    }
    
    @Override
    public void execute(Wandora wandora, Context context) {
        wandora.back();
    }

    @Override
    public String getName() {
        return "Back";
    }

    @Override
    public String getDescription() {
        return "Go back to the previous topic in the topic history.";
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/back.png");
    }
    
    @Override
    public boolean runInOwnThread(){
        return false;
    }

    
}
