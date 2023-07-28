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
 * ApplicationlessContext.java
 *
 * Created on 22. huhtikuuta 2006, 11:15
 *
 */

package org.wandora.application.contexts;


import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.awt.event.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public class ApplicationlessContext extends LayeredTopicContext {
    

    
    /**
     * Creates a new instance of ApplicationlessContext
     */
    public ApplicationlessContext() {
    }
    
    
    @Override
    public void initialize(Wandora wandora, ActionEvent actionEvent, WandoraTool contextOwner) {
        this.wandora = wandora;
        this.actionEvent = actionEvent;
        this.contextOwner = contextOwner;
        
        Object proposedContextSource = UIBox.getActionsRealSource(actionEvent);
        setContextSource( proposedContextSource );
    }
    
    
    @Override
    public Iterator getContextObjects() {
        Object contextSource = getContextSource();
        if(contextSource != null && !(contextSource instanceof Wandora)) {
            return getContextObjects( getContextSource() );
        }
        else {
            return null;
        }
    }
}
