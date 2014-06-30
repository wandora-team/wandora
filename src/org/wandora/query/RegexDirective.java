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
 * RegexDirective.java
 *
 *
 */

package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;
import java.util.regex.*;

/**
 *
 * @author olli
 */
public class RegexDirective implements Directive {

    public static final int MODE_MATCH=1;
    public static final int MODE_GLOBAL=2;
    public static final int MODE_ICASE=4;

    private Directive inner;
    private Locator role;
    private String regex;
    private String replace;
    private boolean match;
    private boolean global;
    private boolean icase;
    private Pattern pattern;

    public RegexDirective(Directive inner,Locator role,String regex,String replace,int mode){
        this.inner=inner;
        this.role=role;
        this.regex=regex;
        this.replace=replace;
        match=((mode&MODE_MATCH)>0);
        global=((mode&MODE_GLOBAL)>0);
        icase=((mode&MODE_ICASE)>0);
        this.pattern=Pattern.compile(regex,icase?Pattern.CASE_INSENSITIVE:0);
    }

    public RegexDirective(Directive inner,String role,String regex,String replace,int mode){
        this(inner,new Locator(role),regex,replace,mode);
    }

    public RegexDirective(Directive inner,Locator role,String regex,String replace){
        this(inner,role,regex,replace,MODE_GLOBAL);
    }

    public RegexDirective(Directive inner,String role,String regex,String replace){
        this(inner,new Locator(role),regex,replace);
    }

    public RegexDirective(Directive inner,Locator role,String regex){
        this(inner,role,regex,null,MODE_MATCH|MODE_GLOBAL);
    }

    public RegexDirective(Directive inner,String role,String regex){
        this(inner,new Locator(role),regex);
    }

    private ResultRow makeRow(ResultRow original,Locator role,String replacement){
        ArrayList<Locator> newRoles=new ArrayList<Locator>();
        ArrayList<Object> newValues=new ArrayList<Object>();
        for(int i=0;i<original.getNumValues();i++){
            Locator r=original.getRole(i);
            Object v=original.getValue(i);
            if(r.equals(role)) v=replacement;
            newRoles.add(r);
            newValues.add(v);
        }
        return new ResultRow(original.getType(),newRoles, newValues);
    }


    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        Topic contextTopic=context.getContextTopic();
        TopicMap tm=contextTopic.getTopicMap();
        ArrayList<ResultRow> res=inner.query(context);
        ArrayList<ResultRow> ret=new ArrayList<ResultRow>();
        
        for(int i=0;i<res.size();i++){
            ResultRow row=res.get(i);
            Object value=row.getValue(role);
            if(value==null){
                if(!match) ret.add(row);
            }
            else if(replace!=null){
                String s=value.toString();
                Matcher m=pattern.matcher(s);
                if(global) s=m.replaceAll(replace);
                else s=m.replaceFirst(replace);
                if(match){
                    try{
                        m.group();
                        ret.add(makeRow(row,role,s));
                    }catch(IllegalStateException e){}
                }
                else ret.add(makeRow(row,role,s));
            }
            else {
                String s=value.toString();
                Matcher m=pattern.matcher(s);
                if(global) {
                    if(m.matches()) ret.add(makeRow(row,role,s));
                }
                else {
                    if(m.find()) ret.add(makeRow(row,role,s));
                }
            }
        }

        return ret;
    }

    public boolean isContextSensitive(){
        return inner.isContextSensitive();
    }

}
