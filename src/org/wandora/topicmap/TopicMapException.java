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
 * TopicMapException.java
 *
 * Created on 11. toukokuuta 2006, 11:18
 *
 */

package org.wandora.topicmap;

/**
 *
 * @author olli
 */
public class TopicMapException extends org.wandora.exceptions.WandoraException {
    
    /** Creates a new instance of TopicMapException */
    public TopicMapException() {
    }
    public TopicMapException(String s){
        super(s);
    }
    public TopicMapException(Throwable cause){
        super(cause);
    }
    public TopicMapException(String s,Throwable cause){
        super(s,cause);
    }
    
}
