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
 * TopicMapSQLException.java
 *
 * Created on 15. toukokuuta 2006, 14:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wandora.topicmap.database;
import org.wandora.topicmap.TopicMapException;
/**
 *
 * @author olli
 */
public class TopicMapSQLException extends TopicMapException {
    
    /** Creates a new instance of TopicMapSQLException */
    public TopicMapSQLException() {
    }
    public TopicMapSQLException(String s) {
        super(s);
    }
    public TopicMapSQLException(Throwable cause){
        super(cause);
    }
    public TopicMapSQLException(String s,Throwable cause){
        super(s,cause);
    }
    
}
