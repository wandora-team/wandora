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
 * HTMLEncoder.java
 *
 * Created on 19.7.2005, 14:04
 */

package org.wandora.modules.velocityhelpers;

/**
 *
 * @author olli
 */
public class HTMLEncoder {
    
    /** Creates a new instance of HTMLEncoder */
    public HTMLEncoder() {
    }
    
    public String encode(String s){
        s=s.replaceAll("&","&amp;");
        s=s.replaceAll("<","&lt;");
        return s;
    }
    
    public String encodeAttribute(String s){
        return encode(s).replaceAll("\"","&quot;");
    }
}
