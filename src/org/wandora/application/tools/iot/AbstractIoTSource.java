 /*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 */

package org.wandora.application.tools.iot;

import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eero Lehtonen
 */


abstract class AbstractIoTSource {
    
    protected Map<String,String> parseParams(URL u) {
        Map<String, String> params = new HashMap<>();
        String q = u.getQuery();
        
        if(q == null) return params;
        
        String[] splits = q.split("&");
        
        for(String split: splits) {
            int idx = split.indexOf("=");
            try {
                String k = URLDecoder.decode(split.substring(0, idx), "UTF-8");
                String v = URLDecoder.decode(split.substring(idx+1), "UTF-8");
                params.put(k,v);    
            } 
            catch (Exception e) {
                // IGNORE
            }
        }
        
        return params;
    }
}
