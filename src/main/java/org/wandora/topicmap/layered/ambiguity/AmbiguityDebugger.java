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
 * AmbiguityDebugger.java
 *
 * Created on 11. joulukuuta 2006, 12:15
 *
 */

package org.wandora.topicmap.layered.ambiguity;


import org.wandora.topicmap.layered.*;

/**
 *
 * @author akivela
 */
public class AmbiguityDebugger implements AmbiguityResolver {
       
    /** Creates a new instance of AmbiguityDebugger */
    public AmbiguityDebugger() {
    }
    
    
    @Override
    public void ambiguity(String s) {
        System.out.println(s);
    }
    
    @Override
    public AmbiguityResolution resolveAmbiguity(String event) {
        return resolveAmbiguity(event,null);
    }
    
    @Override
    public AmbiguityResolution resolveAmbiguity(String event,String msg) {
        ambiguity(event+(msg==null?"":(" "+msg)));
        return AmbiguityResolution.addToSelected;
    }
}
