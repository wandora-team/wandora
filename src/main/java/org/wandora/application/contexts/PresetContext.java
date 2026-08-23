/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * https://wandora.org
 * 
 * Copyright (C) 2004-2026 Wandora Team
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




import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.utils.logger.Log4j2Logger;


/**
 *
 * @author akivela
 */
public class PresetContext<T> implements Context<T> {
	private static final Log4j2Logger logger = Log4j2Logger.getLogger(PresetContext.class);
    
    protected WandoraTool contextOwner = null;
    protected ActionEvent actionEvent = null;
    protected Wandora wandora = null;
    
    private Collection<T> contextObjects;
    
    
    /** Creates a new instance of PresetContext */
    public PresetContext(T o) {
        contextObjects = new ArrayList<>();
        contextObjects.add(o);
    }
    
    public PresetContext(T[] objects) {
        contextObjects = new ArrayList<>();
        contextObjects.addAll(Arrays.asList(objects));
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
    public Iterator<T> getContextObjects() {
        
        return new Iterator<T>() {
            Iterator<T> iterator = contextObjects.iterator();
            T next = solveNext();
            
            @Override
            public boolean hasNext() {
                if(next == null) return false;
                return true;
            }

            @Override
            public T next() {
                T current = next;
                next = solveNext();
                return current;
            }

            @Override
            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();            
            }
            
            private T solveNext() {
                if(iterator != null && iterator.hasNext()) {
                    try {
                        return iterator.next();
                    }
                    catch(Exception e) {
                    	logger.error(e);
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
