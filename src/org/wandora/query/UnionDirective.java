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
 * UnionDirective.java
 *
 * Created on 8. helmikuuta 2008, 15:48
 *
 */

package org.wandora.query;
import org.wandora.topicmap.*;
import org.wandora.utils.GripCollections;
import java.util.*;

/**
 * @deprecated
 *
 * @author olli
 */
public class UnionDirective implements Directive {
    
    private Directive[] directives;
    private boolean removeDuplicates;
    
    /** Creates a new instance of UnionDirective */
    public UnionDirective(Directive[] ds) {
        this(ds,true);
    }
    public UnionDirective(Directive[] ds,boolean removeDuplicates) {
        directives=ds;
        this.removeDuplicates=removeDuplicates;
    }
    public UnionDirective(Collection<Directive> ds) {
        this(ds.toArray(new Directive[ds.size()]));
    }
    public UnionDirective(Directive d1) {
        this(new Directive[]{d1});
    }
    public UnionDirective(Directive d1,Directive d2) {
        this(new Directive[]{d1,d2});
    }
    public UnionDirective(Directive d1,Directive d2,Directive d3) {
        this(new Directive[]{d1,d2,d3});
    }
    public UnionDirective(Directive d1,Directive d2,Directive d3,Directive d4) {
        this(new Directive[]{d1,d2,d3,d4});
    }
    public UnionDirective(Directive d1,Directive d2,Directive d3,Directive d4,Directive d5) {
        this(new Directive[]{d1,d2,d3,d4,d5});
    }
    
    public static void joinResults(ArrayList<ResultRow> dest,ArrayList<ResultRow> source){
        // TODO do something smart if results have different roles
        dest.addAll(source);
    }
    
    public static ArrayList<ResultRow> removeDuplicates(ArrayList<ResultRow> r){
        LinkedHashSet<ResultRow> s=new LinkedHashSet<ResultRow>(r);
        return new ArrayList<ResultRow>(s);
    }

    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        ArrayList<ResultRow> rows=new ArrayList<ResultRow>();
        for(int i=0;i<directives.length;i++){
            joinResults(rows,directives[i].query(context));
        }
        if(removeDuplicates) rows=removeDuplicates(rows);
        return rows;
    }
    public boolean isContextSensitive(){
        for(int i=0;i<directives.length;i++){
            if(directives[i].isContextSensitive()) return true;
        }
        return false;
    }
    
}
