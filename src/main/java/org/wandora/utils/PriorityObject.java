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
 * 
 *
 */

package org.wandora.utils;




public class PriorityObject extends Object implements Comparable, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    
    public final static int HIGHEST_PRIORITY = 10000;
    public final static int HIGHER_PRIORITY  = 1000;
    public final static int DEFAULT_PRIORITY = 100;
    public final static int LOWER_PRIORITY   = 10;
    public final static int LOWEST_PRIORITY  = 1;
    
    protected int priority = DEFAULT_PRIORITY;
    protected Object object = null;


    
    
    public PriorityObject(Object object) {
        this.object = object;
        this.priority = DEFAULT_PRIORITY;
    }
    
    
    public PriorityObject(Object object, int priority) {
        this.object = object;
        this.priority = priority;
    }
    

    
    // -------------------------------------------------------------------------
    
    public synchronized int getPriority() {
        return priority;
    }
    
    
    public synchronized void setPriority(int newPriority) {
        priority = newPriority;
    }

    
    public synchronized boolean isSuperior(PriorityObject priorityObject) {
        if (priorityObject != null) {
            if (priority > priorityObject.getPriority()) return true;
        }
        return false;
    }
   
    public int compareTo(Object o) {
        if (o != null && o instanceof PriorityObject) {
            if (priority > ((PriorityObject)o).getPriority())
                return 1;
            if (priority < ((PriorityObject)o).getPriority())
                return -1;
            return 0;
        }
        return 0;
    }
    
    // -------------------------------------------------------------------------
    
    public synchronized Object getObject() { return object; }
    public synchronized void setObject(Object newObject) { object = newObject; }
       
    // -------------------------------------------------------------------------
    
    public synchronized void adjustPriority( int amount ) {
        this.priority += amount;
    }
    

    public String toString() {
        if( null==object )
            return "PriorityObject[null pri="+priority+"]";
        else
            return "PriorityObject["+object.toString()+" pri="+priority+"]";
    }

}
