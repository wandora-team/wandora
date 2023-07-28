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
 * PreContext.java
 *
 * Created on 15. elokuuta 2006, 20:42
 *
 */

package org.wandora.application.contexts;




import org.wandora.application.*;
import org.wandora.topicmap.*;

import java.util.*;
import java.awt.event.*;


/**
 *
 * @author akivela
 */
public class PreContext implements Context {
    
    
    private Object contextSource;
    protected WandoraTool contextOwner = null;
    protected ActionEvent actionEvent = null;
    protected Wandora wandora = null;
    
    private Collection<Object> contextObjects;
    
    
    /** Creates a new instance of PreContext */
    public PreContext(Locator locator) {
        contextObjects = new ArrayList<>();
        contextObjects.add(locator);
    }
    
    public PreContext(Locator[] locators) {
        contextObjects = new ArrayList<>();
        contextObjects.addAll(Arrays.asList(locators));
    }
    public PreContext(Topic locator) {
        contextObjects = new ArrayList<>();
        contextObjects.add(locator);
    }
    
    public PreContext(Topic[] locators) {
        contextObjects = new ArrayList<>();
        contextObjects.addAll(Arrays.asList(locators));
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public void initialize(Wandora wandora, ActionEvent actionEvent, WandoraTool contextOwner) {
        this.wandora = wandora;
        this.actionEvent = actionEvent;
        this.contextOwner = contextOwner;
    }
    
    /**
     * Returns Iterator for accessible object in context ie. object the tool may
     * modify or otherwise access. Generally returned iterator contains Topic(s) found in
     * the GUI element that originated tool's action event.
     *
     * @return <tt>Iterator</tt> containing all the context objects.
     */
    @Override
    public Iterator getContextObjects() {
        
        return new Iterator() {
            Iterator iterator = contextObjects.iterator();
            Object next = solveNext();
            
            @Override
            public boolean hasNext() {
                if(next == null) return false;
                return true;
            }

            @Override
            public Object next() {
                Object current = next;
                next = solveNext();
                //System.out.println("next == " + current);
                return current;
            }

            @Override
            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();            
            }
            
            private Object solveNext() {
                if(iterator != null && iterator.hasNext()) {
                    try {
                        Object o = iterator.next();
                        if(o instanceof Topic) {
                            return o;
                        }
                        if(o instanceof Locator) {
                            return wandora.getTopicMap().getTopic((Locator) o);
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
    }

    
    
    
    /**
     * Sets the origin of context. Normally context origin is a GUI element
     * that triggered the tool execution.
     */
    @Override
    public void setContextSource(Object contextSource) {
        // DO NOTHING
    }
    
    /**
     * Returns the origin of context. Normally context origin is a GUI element
     * that triggered the tool execution. <code>PreContext</pre> has no context
     * source as the context is set during construction.
     */
    @Override
    public Object getContextSource() {
        return null;
    }
    
    
    
    @Override
    public ActionEvent getContextEvent() {
        return actionEvent;
    }
}
