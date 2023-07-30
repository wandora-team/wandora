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
 * HistoryList.java
 *
 * Created on January 15, 2002, 3:41 PM
 */

package org.wandora.utils.sessions;
 

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author  marko
 */
public class HistoryList {
    
    private int currentIndex = -1;
    
    private ArrayList list = new ArrayList();
    
    private int maxSize = 0;
    
    /** Creates new HistoryList */
    public HistoryList(int maxS) {
        maxSize = maxS;
    }
    
    public Boolean add(Object obj, Boolean rmDuplicate) {
        if (rmDuplicate == Boolean.TRUE) {
            if ((!list.isEmpty()) && (list.lastIndexOf(obj) == list.size()-1)) return (Boolean.FALSE);            
        }
        this.add(obj);
        return (Boolean.TRUE);
    }
    
    public void add(Object obj) {
        List delList;
        if (list.size() >= maxSize){
            Iterator listItr = list.iterator();
            int ind = list.size() - maxSize;
            while ((ind > 0) && (listItr.hasNext())) {
                listItr.remove();
                ind--;
            }
        }
        // if adding in the middle of the list and we are following
        // different branch than old then remove old branch
        if ((currentIndex < this.lastIndex()) &&
        (obj != this.getObjectAt(currentIndex+1))) {
            this.removeFrom(currentIndex+1);
        }
        list.add(obj);
        currentIndex++;
        if (currentIndex >= list.size()) currentIndex = list.size()-1;
        System.out.println("adding object "+currentIndex);
    }
    
    public void clear() {
        list.clear();
    }
    
    public void removeFrom(int index) {
        List delList;
        if (list.size() > 0) {
            while (list.size()-1 >= index) {
                list.remove(list.size()-1);
            }
            
            if (currentIndex >= list.size()) currentIndex = list.size()-1;
        }
    }
    
    public int lastIndex() {
        return(list.size()-1);
    }
    
    public int getCurrentIndex() {
        return(currentIndex);
    }
    
    public Object getCurrentObject() {
        return(list.get(currentIndex));
    }
    
    public Object getObjectAt(int index) {
        if ((index > 0) && (index < list.size())) return(list.get(index));
        return(null);
    }
    
    public List getNewest(int size) {
        if (size > list.size()) return(list);
        if (size > 0) return(list.subList(list.size()-size,list.size()));
        return(null);
    }
    
    public List getRange(int start, int end) {
        
        if (start < 0) start = 0;
        if (end > list.size()) end = list.size();
        return(list.subList(start,end));
    }
    
    
    public boolean isEmpty() {
        return(list.isEmpty());
    }
    
    public void moveBack() {
        if (currentIndex > 0) currentIndex--;
        System.out.println("Index:"+currentIndex);
    }
    
    public void moveForward() {
        if (currentIndex < list.size()-1) currentIndex++;
    }
    
    public void moveTo(int index) {
        if ((index >= 0) && (index < list.size())) currentIndex = index;
    }
    
    public void moveToEnd() {
        currentIndex = list.size()-1;
    }
}
