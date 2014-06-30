/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * WandoraFNGExtractor.java
 *
 * Created on 7. maaliskuuta 2003, 15:10
 */

package org.wandora.utils.regexextractor.extractors.wandorafng;


import org.wandora.utils.Textbox;
import org.wandora.utils.Rexbox;
import java.util.*;
import org.wandora.utils.*;
import org.wandora.utils.regexextractor.extractors.*;
import org.wandora.utils.regexextractor.*;





public abstract class WandoraFNGExtractor extends ExpressionExtractor implements Extractor {
    
    private Vector cacheObject = null;
           
    
    // -------------------------------------------------------------------------
    
    
    
    public Vector getCached(String[][][] expressions, String text) {
        if (cacheObject == null) cacheObject = extractExpressions(expressions, text);
        return cacheObject;
    }
    
    
    public Vector getCached(String[][] expressions, String text) {
        if (cacheObject == null) cacheObject = extractExpressions(expressions, text);
        return cacheObject;
    }
    
    
    public void clearCache() {
        cacheObject = null;
    }
    
    
    public Object cleanUp(Object o) {
        if(o instanceof String) {
            String s = Textbox.encodeXML(Textbox.trimExtraSpaces(Textbox.removeQuotes(Textbox.trimExtraSpaces((String) o))));
            s = Rexbox.replace(s, "\"\"", "\"");
            return s;
        }
        else {
            return o;
        }
    }
}
