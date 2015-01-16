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
 * RemoveEmptyFilter.java
 *
 * Created on 24. marraskuuta 2004, 19:28
 */

package org.wandora.application.tools.extractors.datum;
import java.util.*;
/**
 *
 * @author  olli
 */
public class RemoveEmptyFilter implements DatumFilter {
    
    /** Creates a new instance of RemoveEmptyFilter */
    public RemoveEmptyFilter() {
    }
    
    public Object filter(Object value) {
        if(value instanceof String){
            String s=(String)value;
            s=s.trim();
            if(s.length()==0) return new Vector();
            else return s;
        }
        else{
            Vector v=new Vector();
            Iterator iter=((Collection)value).iterator();
            while(iter.hasNext()){
                String s=(String)iter.next();
                s=s.trim();
                if(s.length()>0) v.add(s);
            }
            return v;
        }
    }
    
}
