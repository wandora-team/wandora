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
 *
 * Created on May 26, 2003, 8:34 PM
 */

package org.wandora.utils.regexextractor.extractors.wandorafng.teoksetExcel;



import java.util.*;
/**
 *
 * @author  ph
 */
public class MitatExtractor extends org.wandora.utils.regexextractor.extractors.wandorafng.WandoraFNGExtractor {
    
    // digikuvanumero	inv.nro	taiteilija	s./1xxx-1xxx	teoksen nimi	vuosi	haltija pääluokka erikoisluokka   tekniikka (ei pakollinen)	kork.cm leveys.cm   syv.cm  kuvailu	valokuvaaja	iconclass.fi    iconcalss.en

    
    String[][][] expressions1 = {
        {
           { "^(?:[^\\t]*?\\t){10}([^\\t]*?)\\t.*$", "1" },  
        },
        {
            { "\\t", "!" }, 
        }
        
    };
    
        
    String[][][] expressions2 = {
        {
           { "^(?:[^\\t]*?\\t){11}([^\\t]*?)\\t.*$", "1" },  
        },
        {
            { "\\t", "!" }, 
        }
        
    };
    
    String[][][] expressions3 = {
        {
           { "^(?:[^\\t]*?\\t){12}([^\\t]*?)\\t.*$", "1" },  
        },
        {
            { "\\t", "!" }, 
        }
        
    };
    
    public Object extract(String text) {
        String x = null;
        String y = null;
        String z = null;
        
        Vector e1 = extractExpressions(expressions1, text);
        if(e1 != null && e1.size()>0) {
            x = (String) cleanUp(e1.elementAt(0));
        }
        Vector e2 = extractExpressions(expressions2, text);
        if(e2 != null && e2.size()>0) {
            y = (String) cleanUp(e2.elementAt(0));
        }
        Vector e3 = extractExpressions(expressions3, text);
        if(e3 != null && e3.size()>0) {
            z = (String) cleanUp(e3.elementAt(0));
        }
        String d = " x ";
        String unit = " cm";
        if(x != null && y != null && z != null) return x + unit + d + y + unit + d + z + unit;
        if(x != null && y != null && z == null) return x + unit + d + y + unit;
        if(x != null && y == null && z == null) return x + unit;
        
        return "";
    }
    
}
