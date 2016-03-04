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
 * RemoteAssociation.java
 *
 * Created on August 16, 2004, 12:25 PM
 */

package org.wandora.topicmap.remote;
import org.wandora.topicmap.*;
import org.wandora.topicmap.memory.*;


/**
 *
 * @author  olli
 */
public class RemoteAssociation extends AssociationImpl {
    private boolean remoteInitialized=false;
    /** Creates a new instance of RemoteAssociation */
    public RemoteAssociation(RemoteTopicMap tm,Topic type)  throws TopicMapException {
        super(tm,type);
        remoteInitialized=true;
    }
    public boolean isRemoteInitialized(){
        return remoteInitialized;
    }
    
}
