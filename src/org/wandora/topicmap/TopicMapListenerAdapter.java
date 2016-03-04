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
 * 
 *
 * TopicMapListenerAdapter.java
 *
 * Created on 26. toukokuuta 2006, 13:16
 *
 */

package org.wandora.topicmap;
import java.util.*;
/**
 *
 * @author olli
 */
public abstract class TopicMapListenerAdapter implements TopicMapListener {
    
    public void topicChanged(Topic t) throws TopicMapException{
        
    }
    public void topicRemoved(Topic t) throws TopicMapException{
        
    }
            
    public void associationChanged(Association a) throws TopicMapException{
        
    }
    public void associationRemoved(Association a) throws TopicMapException{
        
    }

    
    
    public void topicSubjectIdentifierChanged(Topic t,Locator added,Locator removed) throws TopicMapException{
        topicChanged(t);
    }
    public void topicBaseNameChanged(Topic t,String newName,String oldName) throws TopicMapException{
        topicChanged(t);        
    }
    public void topicTypeChanged(Topic t,Topic added,Topic removed) throws TopicMapException {
        topicChanged(t);        
    }
    public void topicVariantChanged(Topic t,Collection<Topic> scope,String newName,String oldName) throws TopicMapException {
        topicChanged(t);        
    }
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException {
        topicChanged(t);        
    }
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException {
        topicChanged(t);        
    }
    
    
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException {
        associationChanged(a);
    }
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException {
        associationChanged(a);        
    }
    
}
