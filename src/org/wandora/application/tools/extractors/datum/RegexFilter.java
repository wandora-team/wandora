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
 * RegexFilter.java
 *
 * Created on 6. joulukuuta 2004, 12:46
 */

package org.wandora.application.tools.extractors.datum;
import java.util.regex.*;
/**
 *
 * @author  olli
 */
public class RegexFilter extends AbstractDatumFilter {
    
    protected Pattern compiled;
    protected String replace;
    protected String otherwise;
    
    /** Creates a new instance of RegexFilter */
    public RegexFilter(String match,String replace) {
        this(match,replace,null);
    }
    public RegexFilter(String match,String replace,String otherwise) {
        compiled=Pattern.compile(match);
        this.replace=replace;
        this.otherwise=otherwise;
    }
    
    public String filterString(String value){
        Matcher m=compiled.matcher(value);
        if(m.matches()){
            return m.replaceAll(replace);
        }
        else{
            if(otherwise!=null) return otherwise;
            return value;
        }
    }
    
}
