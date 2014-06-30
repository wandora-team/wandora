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
public class TaiteilijaExtractor extends org.wandora.utils.regexextractor.extractors.wandorafng.WandoraFNGExtractor {
    
    // digikuvanumero	inv.nro	taiteilija	s./1xxx-1xxx	teoksen nimi	vuosi	haltija pääluokka erikoisluokka   tekniikka (ei pakollinen)	kork.cm leveys.cm   syv.cm  kuvailu	valokuvaaja	iconclass.fi    iconcalss.en

    
    String[][][] expressions = {
        {
            { "^(?:[^\\t]*?\\t){2}([^\\t]*?)\\t.*$", "1" }, 
        },
        {
            { "\\t", "!" }, 
        }
        
    };
    
    
    
    public Object extract(String text) {
        Vector id = getCached(expressions, text);
        if(id != null && id.size()>0) {
            return cleanUp(id.elementAt(0));
        }
        return "";
    }
    
}
