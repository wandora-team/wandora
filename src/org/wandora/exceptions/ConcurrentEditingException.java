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
 * 
 *
 * ConcurrentEditingException.java
 *
 * Created on June 17, 2004, 10:30 AM
 */

package org.wandora.exceptions;
import org.wandora.*;
import java.util.*;
/**
 *
 * @author  olli
 */
public class ConcurrentEditingException extends WandoraException {
    
    private Set failedTopics;
    private Set removedTopics;
    public ConcurrentEditingException(Set failedTopics,Set removedTopics){
        this.failedTopics=failedTopics;
        this.removedTopics=removedTopics;
    }
    public Set getFailedTopics(){
        return failedTopics;
    }
    public Set getRemovedTopics(){
        return removedTopics;
    }
    public void setFailedTopics(Set s){
        failedTopics=s;
    }
    public void setRemovedTopics(Set s){
        removedTopics=s;
    }
}
