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
 * CompareDirective.java
 *
 *
 */
package org.wandora.query;
import org.wandora.topicmap.*;

/**
 *
 * @author olli
 */
public class CompareDirective extends FilterDirective {

    public static final int EQ=0;
    public static final int NE=1;
    public static final int LT=2;
    public static final int GT=3;
    public static final int LE=4;
    public static final int GE=5;
    public static final int TOPIC_EQUAL=6;
    public static final int TOPIC_NEQUAL=7;

    private Locator role1;
    private Locator role2;
    private int comp;
    private boolean numeric;

    public CompareDirective(Directive query,Locator role1,int comp,Locator role2,boolean numeric){
        super(query,false);
        this.role1=role1;
        this.role2=role2;
        this.comp=comp;
        this.numeric=numeric;
    }
    public CompareDirective(Directive query,Locator role1,String comp,Locator role2,boolean numeric){
        this(query,role1,parseComp(comp),role2,numeric);
    }
    public CompareDirective(Directive query,Locator role1,String comp,Locator role2){
        this(query,role1,parseComp(comp),role2,false);
    }
    public CompareDirective(Directive query,Locator role1,Locator role2,boolean numeric){
        this(query,role1,EQ,role2,numeric);
    }
    public CompareDirective(Directive query,Locator role1,Locator role2){
        this(query,role1,EQ,role2,false);
    }

    public CompareDirective(Directive query,String role1,int comp,String role2,boolean numeric){
        this(query,new Locator(role1),comp,new Locator(role2),numeric);
    }
    public CompareDirective(Directive query,String role1,String comp,String role2,boolean numeric){
        this(query,role1,parseComp(comp),role2,numeric);
    }
    public CompareDirective(Directive query,String role1,String comp,String role2){
        this(query,role1,parseComp(comp),role2,false);
    }
    public CompareDirective(Directive query,String role1,String role2,boolean numeric){
        this(query,role1,EQ,role2,numeric);
    }
    public CompareDirective(Directive query,String role1,String role2){
        this(query,role1,EQ,role2,false);
    }



    public static int parseComp(String comp){
        if(comp.equals("=") || comp.equals("==") || comp.equalsIgnoreCase("eq")) return EQ;
        else if(comp.equals("!=") || comp.equals("<>") || comp.equals("~=") ||
                comp.equalsIgnoreCase("ne") || comp.equalsIgnoreCase("neq")) return NE;
        else if(comp.equals("<") || comp.equalsIgnoreCase("lt")) return LT;
        else if(comp.equals(">") || comp.equalsIgnoreCase("gt")) return GT;
        else if(comp.equals("<=") || comp.equals("le") || comp.equalsIgnoreCase("lte")) return LE;
        else if(comp.equals(">=") || comp.equals("ge") || comp.equalsIgnoreCase("gte")) return GE;
        else if(comp.equalsIgnoreCase("topic") || comp.equalsIgnoreCase("te")) return TOPIC_EQUAL;
        else if(comp.equalsIgnoreCase("notopic") || comp.equalsIgnoreCase("ntopic") ||
                comp.equalsIgnoreCase("te") || comp.equalsIgnoreCase("tne") ||
                comp.equalsIgnoreCase("tn")) return TOPIC_NEQUAL;
        else return EQ;
    }

    protected int _includeRow(ResultRow row,Topic context,TopicMap tm,Object param) throws TopicMapException {

        Object v1=row.getValue(role1);
        Object v2=row.getValue(role2);

        int compare=0;

        if(numeric){
            if(v1!=null) {
                try{
                    v1=Double.parseDouble(v1.toString());
                }catch(NumberFormatException nfe){v1=null;}
            }
            if(v2!=null) {
                try{
                    v2=Double.parseDouble(v2.toString());
                }catch(NumberFormatException nfe){v2=null;}
            }
            if(v1==null && v2==null) compare=0;
            else if(v1==null && v2!=null) compare=-1;
            else if(v2!=null && v2==null) compare=1;
            else compare=Double.compare((Double)v1, (Double)v2);
        }
        else if(comp==TOPIC_EQUAL || comp==TOPIC_NEQUAL){
            if(v1==null && v2==null) compare=0;
            else if(v1==null && v2!=null) compare=-1;
            else if(v2!=null && v2==null) compare=1;
            else {
                Topic t1=tm.getTopic((Locator)v1);
                Topic t2=tm.getTopic((Locator)v2);
                if(t1==null || t2==null) compare=-1;
                else if(t1.mergesWithTopic(t2)) compare=0;
                else compare=1;
            }
        }
        else {
            if(v1==null && v2==null) compare=0;
            else if(v1==null && v2!=null) compare=-1;
            else if(v2!=null && v2==null) compare=1;
            else compare=v1.toString().compareTo(v2.toString());
        }

        switch(comp){
            case EQ:
                return compare==0?RES_INCLUDE:RES_EXCLUDE;
            case NE:
                return compare!=0?RES_INCLUDE:RES_EXCLUDE;
            case LT:
                return compare<0?RES_INCLUDE:RES_EXCLUDE;
            case GT:
                return compare>0?RES_INCLUDE:RES_EXCLUDE;
            case LE:
                return compare<=0?RES_INCLUDE:RES_EXCLUDE;
            case GE:
                return compare>=0?RES_INCLUDE:RES_EXCLUDE;
            case TOPIC_EQUAL:
                return compare==0?RES_INCLUDE:RES_EXCLUDE;
            case TOPIC_NEQUAL:
                return compare!=0?RES_INCLUDE:RES_EXCLUDE;
        }

        return RES_EXCLUDE;
    }
}
