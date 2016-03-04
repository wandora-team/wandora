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
 * SessionObject.java
 *
 * Created on January 21, 2002, 10:56 AM
 */

package org.wandora.utils.sessions;

/**
 *
 * @author  marko
 */
public class SessionObject {

    private HistoryList history = null;
    
    private String name = null;
    
    private String id = null;
    
    
    
    /** Creates new SessionObject */
    public SessionObject(HistoryList sessionHistory, String topicName, String sessionID) {
        history = sessionHistory;
        name = topicName;
        id = sessionID;
    }
    
    

    public HistoryList getHistory() {
        return history;
    }
    
    
    
    public String getID() {
        return id;
    }

    
    
    public String getName() {
        return name;
    }
    
}
