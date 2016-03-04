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
 * 
 *
 * AbstractCommentHandler.java
 *
 * Created on January 7, 2002, 1:33 PM
 */

package org.wandora.piccolo.utils.crawler.handlers;

import org.wandora.piccolo.utils.crawler.*;
import java.util.StringTokenizer;
import java.util.*;
/**
 *
 * @author  olli
 */
public abstract class AbstractCommentHandler extends Object implements JPEGCommentHandler {

    
    abstract public HashMap handleComment(byte[] comment);
    
    
    
    protected Object parseName(String name){
        name=name.trim();        
        if(name.indexOf(";")!=-1){
            StringTokenizer st=new StringTokenizer(name,";");
            Vector names=new Vector();
            while(st.hasMoreTokens()){
                names.add(parseName(st.nextToken()));
            }
            return names;
        }
        else if(name.indexOf(",")==-1){
            int ind2=name.indexOf(" ");
            if(ind2!=-1)
                name=name.substring(0,ind2)+","+name.substring(ind2);
        }        
        return name;
    }    
    
}
