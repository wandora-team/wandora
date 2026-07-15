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
 * JavaScriptEncoder.java
 *
 * Created on July 28, 2004, 12:38 PM
 */

package org.wandora.modules.velocityhelpers;

/**
 *
 * @author  olli
 */
public class JavaScriptEncoder {
    
    /** Creates a new instance of JavaScriptEncoder */
    public JavaScriptEncoder() {
    }
    
    public String encode(String s){
/*        s=s.replaceAll("\\\\", "\\\\");
        s=s.replaceAll("\"", "\\\"");
        s=s.replaceAll("\'", "\\\'");*/
        return s;
    }
    
    public String encodeString(String s){
        s=s.replaceAll("\\\\", "\\\\");
        s=s.replaceAll("\"", "\\\"");
        s=s.replaceAll("\'", "\\\'");
        return s;        
    }
}
