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
 * Context.java
 *
 * Created on 7. huhtikuuta 2006, 12:30
 *
 */

package org.wandora.application.contexts;


import java.util.*;
import org.wandora.application.*;
import java.awt.event.*;




/**
 * <p>
 * Context contains the execution environment of a tool.
 * </p>
 * <p>
 * Context must always initialized before usage.
 * </p>
 * 
 * @author akivela
 */

public interface Context {
    
    /**
     * Initializes context with
     * - Wandora: The Application.
     * - ActionEvent: Event that triggered the execution of tool.
     * - AdminTool: The Tool to be executed. Note that Context _is not_ executing the tool!
     */
    public void initialize(Wandora admin, ActionEvent actionEvent, WandoraTool contextOwner);
    
    /**
     * Returns Iterator for accessible object in context ie. object the tool may
     * modify or otherwise access. Generally returned iterator contains Topic(s) found in
     * the GUI element that originated tool's action event.
     *
     * @return <tt>Iterator</tt> containing all the context objects.
     */
    public Iterator getContextObjects();

    /**
     * Sets the origin of context. Normally context origin is a GUI element
     * that triggered the tool execution.
     */
    public void setContextSource(Object contextSource);
    
    /**
     * Returns the origin of context. Normally context origin is a GUI element
     * that triggered the tool execution.
     */
    public Object getContextSource();
    
    
    /**
     * @return The event that triggered the tool execution.
     */
    public ActionEvent getContextEvent();
}
