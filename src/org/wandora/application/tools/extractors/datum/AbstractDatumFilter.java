/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * AbstractDatumFilter.java
 *
 * Created on 29. marraskuuta 2004, 14:33
 */

package org.wandora.application.tools.extractors.datum;
import org.wandora.application.tools.extractors.*;
import java.util.*;
/**
 *
 * @author  olli
 */
public abstract class AbstractDatumFilter implements DatumFilter {
    
    public abstract String filterString(String value);
    
    public Object filter(Object value){
        if(value instanceof Collection){
            Vector v=new Vector();
            Iterator iter=((Collection)value).iterator();
            while(iter.hasNext()){
                String s=filterString((String)iter.next());
                if(s!=null) v.add(s);
            }
            return v;
        }
        else{
            String s=filterString((String)value);
            if(s==null) s="";
            return s;
        }
    }
    
}
