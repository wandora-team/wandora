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
 * ResourceAssociationEditor.java
 *
 * Created on August 9, 2004, 2:47 PM
 */

package org.wandora.application.gui;



import org.wandora.topicmap.*;
import org.wandora.application.*;

/**
 *
 * @author  olli
 */
public abstract class ResourceEditor extends javax.swing.JPanel {   
    public abstract void initializeAssociation(Topic t,Association a,Wandora parent) throws TopicMapException ;
    public abstract void initializeOccurrence(Topic t,Topic otype,Wandora parent) throws TopicMapException ;
    public abstract boolean applyChanges(Topic t,Wandora parent) throws TopicMapException ;
    public abstract boolean hasChanged() throws TopicMapException ;
}
