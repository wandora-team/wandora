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
 * AdvancedHashtable.java
 *
 * Created on 16. joulukuuta 2004, 19:19
 */

package org.wandora.utils;


import java.util.Hashtable;


/**
 *
 * @author  akivela
 */
public class AdvancedHashtable extends Hashtable {
    
    private static final long serialVersionUID = 1L;




    /** Creates a new instance of AdvancedHashtable */
    public AdvancedHashtable() {
    }
    
    
    
    
    public String gets(Object key) {
        try {
            return (String) this.get(key);
        }
        catch (Exception e) {}
        return "";
    }
    
    
}
