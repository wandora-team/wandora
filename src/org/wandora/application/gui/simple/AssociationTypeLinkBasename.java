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
 * AssociationTypeLinkBasename.java
 *
 * Created on 17. heinäkuuta 2007, 13:15
 *
 */

package org.wandora.application.gui.simple;

import org.wandora.application.gui.table.AssociationTable;
import org.wandora.topicmap.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;


/**
 *
 * @author akivela
 */
public class AssociationTypeLinkBasename extends TopicLinkBasename {
    
    private AssociationTable associationTable = null;
    
    
    /** Creates a new instance of AssociationTypeLinkBasename */
    public AssociationTypeLinkBasename(AssociationTable at, Topic t, Wandora parent) {
        super(t, parent);
        associationTable = at;
    }
    
    
    public AssociationTable getAssociationTable() {
        return associationTable;
    }
    
}
