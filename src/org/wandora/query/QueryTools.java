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
 * QueryTools.java
 *
 * Created on 25. lokakuuta 2007, 13:13
 *
 */

package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * @deprecated
 *
 * @author olli
 */
public class QueryTools {
    
    public static ArrayList<Locator> makeLocatorArray(Collection<String> a){
        if(a==null) return null;
        ArrayList<Locator> ret=new ArrayList<Locator>();
        for(String s : a){
            ret.add(new Locator(s));
        }
        return ret;
    }
    public static ArrayList<Locator> makeLocatorArray(Locator ... a){
        ArrayList<Locator> ret=new ArrayList<Locator>();
        for(int i=0;i<a.length;i++){
            ret.add(a[i]);
        }
        return ret;
    }
    public static ArrayList<Locator> makeLocatorArray(String ... a){
        ArrayList<Locator> ret=new ArrayList<Locator>();
        for(int i=0;i<a.length;i++){
            ret.add(new Locator(a[i]));
        }
        return ret;
    }
    
}
