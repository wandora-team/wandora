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
 * RecursiveDirective.java
 *
 * Created on 11. helmikuuta 2008, 13:50
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
public class RecursiveDirective implements Directive {
 
    private Directive recursion;
    private Locator recursionContext;
    private Locator recursionSourceContext;
    private int maxDepth;
    private boolean onlyLast;
    private boolean removeDuplicates;
    
    /** Creates a new instance of RecursiveDirective */
    public RecursiveDirective(Directive recursion,Locator recursionContext,int maxDepth,boolean onlyLast,boolean removeDuplicates) {
        this.recursion=recursion;
        this.recursionContext=recursionContext;
        this.maxDepth=maxDepth;
        this.onlyLast=onlyLast;
        this.removeDuplicates=removeDuplicates;
    }
    public RecursiveDirective(Directive recursion,Locator recursionContext) {
        this(recursion,recursionContext,-1,false,true);
    }
    public RecursiveDirective(Directive recursion,String recursionContext) {
        this(recursion,new Locator(recursionContext));
    }
    public RecursiveDirective(Directive recursion,String recursionContext,int maxDepth,boolean onlyLast,boolean removeDuplicates) {
        this(recursion,new Locator(recursionContext),maxDepth,onlyLast,removeDuplicates);
    }

    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        TopicMap tm=context.getContextTopic().getTopicMap();
        HashSet<Locator> processed=new HashSet<Locator>();
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();
        ArrayList<ResultRow> next=null;
        int depth=0;
        while(true){
            if(next==null) next=recursion.query(context);
            else {
                ArrayList<ResultRow> res2=new ArrayList<ResultRow>();
                for(ResultRow r : next){
                    Locator l=r.getPlayer(recursionContext);
                    if(l!=null){
                        if(processed.contains(l)) continue;
                        processed.add(l);
                        Topic t=tm.getTopic(l);
                        if(t!=null) {
                            ArrayList<ResultRow> res3=recursion.query(context.makeNewWithTopic(t));
                            if(onlyLast && res3.size()==0) res.add(r);
                            res2.addAll(res3);
                        }
                    }
                }
                next=res2;
            }
            if(!onlyLast) res.addAll(next);
            depth++;
            if(maxDepth!=-1 && depth>=maxDepth) break;
            if(next.isEmpty()) break;
        }
        if(removeDuplicates) res=UnionDirective.removeDuplicates(res);
        return res;
    }
    public boolean isContextSensitive(){
        return recursion.isContextSensitive();
    }
    
}
