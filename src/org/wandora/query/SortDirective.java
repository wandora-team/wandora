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
 * 
 *
 * SortDirective.java
 *
 * Created on 1. marraskuuta 2007, 15:02
 */

package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;

/**
 * @deprecated
 *
 * @author olli
 */
public class SortDirective implements Directive {
    
    private Directive query;
    private ArrayList<Locator> sortColumns;
    private boolean descending;
    
    /** Creates a new instance of SortDirective */
    public SortDirective(Directive query,ArrayList sortColumns,boolean descending) {
        this.query=query;
        this.sortColumns=new ArrayList<Locator>();
        for(Object o : sortColumns){
            if(o instanceof Locator) this.sortColumns.add((Locator)o);
            else this.sortColumns.add(new Locator((String)o));
        }
        this.descending=descending;
    }
    public SortDirective(Directive query,Locator sortColumn,boolean descending) {
        this(query,QueryTools.makeLocatorArray(sortColumn),descending);
    }
    public SortDirective(Directive query,String sortColumn,boolean descending) {
        this(query,QueryTools.makeLocatorArray(sortColumn),descending);
    }
    public SortDirective(Directive query,Locator sortColumn) {
        this(query,QueryTools.makeLocatorArray(sortColumn),false);
    }
    public SortDirective(Directive query,String sortColumn) {
        this(query,QueryTools.makeLocatorArray(sortColumn),false);
    }

    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        ArrayList<ResultRow> inner=query.query(context);
        ArrayList<ResultRow> res=new ArrayList<ResultRow>(inner);
        Collections.sort(res,new RowComparator(context.getContextTopic().getTopicMap(),context.getContextLanguage()));
        return res;
    }
    
    private class RowComparator implements Comparator<ResultRow> {
        public TopicMap tm;
        public String lang;
        public HashMap<Locator,String> nameCache;
        public RowComparator(TopicMap tm,String lang){
            this.tm=tm;
            this.lang=lang;
            this.nameCache=new HashMap<Locator,String>();
        }
        public int compare(ResultRow o1, ResultRow o2) {
            return compare(o1,o2,tm,lang);
        }
        public int compare(ResultRow o1, ResultRow o2,TopicMap tm,String lang) {
            for(Locator sortColumn : sortColumns){
                try{
                    int r=compareRoles(o1,o2,sortColumn,tm,lang);
                    if(r!=0) return (descending?-r:r);
                }
                catch(TopicMapException tme){
                    tme.printStackTrace(); 
                    break;
                }
            }
            return 0;
        }
        public int compareRoles(ResultRow o1, ResultRow o2, Locator role,TopicMap tm,String lang) throws TopicMapException {
            Object p1=o1.getValue(role);
            Object p2=o2.getValue(role);
            if(p1==null){
                if(p2==null) return 0;
                else return -1;
            }
            else if(p2==null){
                return 1;
            }
            if(p1 instanceof String){
                if(p2 instanceof String){
                    return ((String)p1).compareTo((String)p2);
                }
                else return -1;
            }
            else if(p2 instanceof String) return 1;
            
            Topic t1=tm.getTopic((Locator)p1);
            Topic t2=tm.getTopic((Locator)p2);
            if(t1==null){
                if(t2==null) return 0;
                else return -1;
            }
            else if(t2==null){
                return 1;
            }

            String n1=nameCache.get((Locator)p1);
            String n2=nameCache.get((Locator)p2);
            
            if(n1==null) {
                n1=t1.getSortName(lang);
                nameCache.put((Locator)p1,n1);
            }
            if(n2==null) {
                n2=t2.getSortName(lang);
                nameCache.put((Locator)p2,n2);
            }
            
            if(n1==null){
                if(n2==null) return 0;
                else return -1;
            }
            else if(n2==null){
                return 1;
            }
            return n1.compareTo(n2);
        }
    }
    public boolean isContextSensitive(){
        return query.isContextSensitive();
    }

}
