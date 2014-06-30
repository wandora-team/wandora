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
 * Regex.java
 *
 *
 */
package org.wandora.query2;
import java.util.*;
import java.util.regex.*;

/**
 *
 * @author olli
 */
public class Regex extends WhereDirective {
    public static final int MODE_MATCH=1;
    public static final int MODE_GLOBAL=2;
    public static final int MODE_ICASE=4;

    private Operand regex;
    private Operand replace;
    private boolean match;
    private boolean global;
    private boolean icase;
    private Pattern pattern;

    public Regex(Object regex,Object replace,int mode){
        this.regex=new Operand(regex);
        this.replace=(replace==null?null:new Operand(replace));
        match=((mode&MODE_MATCH)>0);
        global=((mode&MODE_GLOBAL)>0);
        icase=((mode&MODE_ICASE)>0);
    }

    public Regex(String regex,String replace){
        this(regex,replace,MODE_GLOBAL);
    }

    public Regex(String regex){
        this(regex,null,MODE_MATCH|MODE_GLOBAL);
    }
    public Regex(String regex,int mode){
        this(regex,null,MODE_MATCH|mode);
    }

    @Override
    public void endQuery(QueryContext context) throws QueryException {
        regex.endQuery(context);
        if(replace!=null) replace.endQuery(context);
        pattern=null;
    }

    @Override
    public boolean startQuery(QueryContext context) throws QueryException {
        return regex.startQuery(context)&(replace==null?true:replace.startQuery(context));
    }


    private ResultRow makeRow(ResultRow original,String role,String replacement){
        ArrayList<String> newRoles=new ArrayList<String>();
        ArrayList<Object> newValues=new ArrayList<Object>();
        for(int i=0;i<original.getNumValues();i++){
            String r=original.getRole(i);
            Object v=original.getValue(i);
            if(r.equals(role)) v=replacement;
            newRoles.add(r);
            newValues.add(v);
        }
        return new ResultRow(newRoles, newValues,original.getActiveColumn(),true);
    }

    @Override
    public ResultRow processRow(QueryContext context,ResultRow input) throws QueryException {
        if(pattern==null || !regex.isStatic()){
            String re=regex.getOperandString(context, input);
            pattern=Pattern.compile(re,icase?Pattern.CASE_INSENSITIVE:0);
        }

        if(replace==null) {
            if(includeRow(context,input)) return input;
            else return null;
        }
        else {
            Object value=input.getActiveValue();
            String role=input.getRole(input.getActiveColumn());
            if(value==null){
                if(!match) return input;
                else return null;
            }
            String s=value.toString();
            Matcher m=pattern.matcher(s);
            if(global) s=m.replaceAll(replace.getOperandString(context, input));
            else s=m.replaceFirst(replace.getOperandString(context, input));
            if(match){
                try{
                    m.group();
                    return makeRow(input,role,s);
                }catch(IllegalStateException e){
                    return null;
                }
            }
            else return makeRow(input,role,s);
        }
    }

    @Override
    public boolean includeRow(QueryContext context, ResultRow input) throws QueryException {
        if(replace!=null) throw new QueryException("Regex directive cannot be used as WhereDirective with replace string.");

        Object value=input.getActiveValue();
        String role=input.getRole(input.getActiveColumn());
        if(value==null) return false;
        String s=value.toString();
        Matcher m=pattern.matcher(s);
        if(global) {
            if(m.matches()) return true;
        }
        else {
            if(m.find()) return true;
        }
        return false;
    }


}
