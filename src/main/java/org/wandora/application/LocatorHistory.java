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
 * LocatorHistory.java
 *
 * Created on 18. huhtikuuta 2006, 15:42
 *
 */

package org.wandora.application;


import java.util.*;
import org.wandora.topicmap.*;
import org.wandora.application.tools.navigate.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import static org.wandora.utils.Tuples.*;
import javax.swing.*;


/**
 * <p>
 * LocatorHistory is used to track browse history in Wandora. Whenever user
 * opens a topic, topic's subject identifier is stored into LocatorHistory.
 * Stored subject identifier is later acquired to restore a topic in
 * browse history.
 * </p>
 * <p>
 * Arrow buttons in Wandora window are used to navigate browse history.
 * </p>
 */
public class LocatorHistory {
    private static final int DEFAULT_MAX_SIZE = 999;
    
    private ArrayList<T2<Locator,Integer>> history;
    
    // location of history "cursor"
    private int index;
    // Size of current history
    private int top;
    
    private int maxsize;

    
    
    public LocatorHistory() {
        this(DEFAULT_MAX_SIZE);
    }
    
    
    /** Creates a new instance of History */
    public LocatorHistory(int max) {
        maxsize = max;
        clear();
    }
    
    
    public void clear() {
        history = new ArrayList<T2<Locator,Integer>>(maxsize);
        for(int i=0; i<maxsize; i++) history.add(null);
        index = 0;
        top = 0;
    }
    
    public void setCurrentViewPosition(int pos){
        T2<Locator,Integer> page=peekCurrent();
        if(page==null) return;
        history.set(index-1, t2(page.e1,pos));
    }
    
    public void add(Locator o) {
        add(o,0);
    }
    public void add(Locator o,int pos) {
        if(index == 0 || (o != null && !o.equals( history.get(index-1).e1 ))) {
            if(index >= history.size()) {
                history.remove(0);
                history.add(null);
                index--;
            }
            history.set(index++, t2(o,pos));
            top = index;
            //Logger.println("history: " + index + ", " + top);
        }
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public T2<Locator,Integer> getPrevious() {
        if(index > 1) {
            index = index - 2;
            return history.get(index++);
        }
        return null;
    }
    
    
    public T2<Locator,Integer> getNext() {
        if(top > index) {
            return history.get(index++);
        }
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public T2<Locator,Integer> peekPrevious() {
        if(index > 1) {
            return history.get(index-2);
        }
        return null;      
    }
    
    
    public T2<Locator,Integer> peekNext() {
        if(top > index) {
            return history.get(index);
        }
        return null;
    }
    
    public T2<Locator,Integer> peekCurrent(){
        if(index > 0) {
            return history.get(index-1);
        }
        return null;      
    }
    
    
    public boolean isEmpty() {
        return top == 0;
    }
    
    
    
    public Collection<Locator> getLocators() {
        Collection<Locator> locs = new ArrayList<>();
        Locator l = null;
        for(int i=0; i<top; i++) {
            l = history.get(i).e1;
            if(l != null) {
                locs.add(l);
            }
        }
        return locs;
    }
    
    
    
    
    
    public Object[] getBackPopupStruct(Wandora admin) {
        Collection<Object> struct = new ArrayList<>();
        Locator l = null;
        Topic t = null;
        String name = null;
        Icon icon = UIBox.getIcon("gui/icons/shortcut.png");
        int max = 10;
        for(int i=index-2; i>=0 && max-- >= 0; i--) {
            l = history.get(i).e1;
            if(l != null) {
                try {
                    t = admin.getTopicMap().getTopic(l);
                    if(t != null) {
                        name = t.getBaseName();
                        if(name != null && name.length() > 0) {
                            struct.add(name);
                        }
                        else {
                            struct.add(l.toExternalForm());
                        }
                        struct.add(new OpenTopic(new PreContext(l)));
                        struct.add(icon);
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return struct.toArray();
    }
    
    
    
    
    public Object[] getForwardPopupStruct(Wandora admin) {
        Collection<Object> struct = new ArrayList<>();
        Locator l = null;
        Topic t = null;
        String name = null;
        Icon icon = UIBox.getIcon("gui/icons/shortcut.png");
        int max = 10;
        for(int i=index; i<top && max-- >= 0; i++) {
            l = history.get(i).e1;
            if(l != null) {
                try {
                    t = admin.getTopicMap().getTopic(l);
                    if(t != null) {
                        name = t.getBaseName();
                        if(name != null && name.length() > 0) {
                            struct.add(name);
                        }
                        else {
                            struct.add(l.toExternalForm());
                        }
                        struct.add(new OpenTopic(new PreContext(l)));
                        struct.add(icon);
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return struct.toArray();
    }
    
}
