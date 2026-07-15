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
 * URLEncoder.java
 *
 * Created on July 9, 2004, 2:55 PM
 */

package org.wandora.utils.velocity;

/**
 *
 * @author  olli
 */
public class URLEncoder {
    private String encoding;
    
    /** Creates a new instance of URLEncoder */
    public URLEncoder() {
        this("UTF-8");
    }
    public URLEncoder(String encoding) {
        this.encoding=encoding;
    }
    
    public String encode(String s){
        try{
            return java.net.URLEncoder.encode(s,encoding);
        }catch(Exception e){
            return s;
        }
    }
    
}
