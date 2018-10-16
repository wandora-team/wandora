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
 * DelimTokenizer.java
 *
 * Created on 24. marraskuuta 2004, 19:00
 */

package org.wandora.application.tools.extractors.datum;


import java.util.*;
import java.util.regex.*;


/**
 *
 * @author  olli
 */
public class PatternTokenizer extends Tokenizer {
    
    protected String pattern;
    
    /** Creates a new instance of DelimTokenizer */
    public PatternTokenizer(String pattern) {
        this.pattern=pattern;
    }
    
    public java.util.Collection tokenize(String value) {
        Pattern p=Pattern.compile(pattern);
        Vector v=new Vector();
        Matcher m=p.matcher(value);
        int offs=0;
        while(m.find(offs)){
            v.add(value.substring(offs,m.start()));
            offs=m.end();
        }
        v.add(value.substring(offs));
        return v;
    }

}
