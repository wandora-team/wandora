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
 * Forward.java
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
public class Forward extends AbstractWandoraTool {
    
    /** Creates a new instance of Forward */
    public Forward() {
    }
    
    @Override
    public void execute(Wandora admin, Context context) {
        admin.forward();
    }

    @Override
    public String getName() {
        return "Forward";
    }

    @Override
    public String getDescription() {
        return "Go back to the next topic in the topic history.";
    }
    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/forward.png");
    }
    
    
    @Override
    public boolean runInOwnThread(){
        return false;
    }

    
}
