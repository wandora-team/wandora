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
 * ApplyChanges.java
 *
 * Created on 19. toukokuuta 2006, 11:12
 */

package org.wandora.application.tools;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;


/**
 *
 * @deprecated 
 * @author olli
 */
public class ApplyChanges extends AbstractWandoraTool {
    
    /** Creates a new instance of ApplyChanges */
    public ApplyChanges() {
    }

    @Override
    public String getName() {
        return "Apply changes";
    }

    @Override
    public String getDescription() {
        return "Deprecated. Applies current changes to the opened topic";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        // Deprecated tool.
        // Apply changes is actually done by default in AbstractWandoraTool.
    }    
}
