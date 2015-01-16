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
 * Tuples.java
 *
 * Created on 4. tammikuuta 2005, 14:46
 */

package org.wandora.utils;

/**
 * <p>
 * A Tuple library to make it easy to return two or more values from a method.
 * Requires Java 1.5. To use do something like
 * <code><pre>
 * import static com.gripstudios.utils.Tuples.*;
 * ...
 *      T2&lt;Integer,String> foo(int x,int y){
 *          return t2(y,new String(x));
 *      }
 * ...
 * </pre></code>
 * Note that this is typesafe where returning an Object[] is not. Note that it
 * is always a good idea to consider making a real class for the return value.
 * Especially if you need a tuple of more than 4 values (only T2,T3 and T4 are
 * provided in this class) you probably should make it a real class.
 * </p>
 * <p>
 * The Tuple classes override equals to check the equality for each element
 * of the tuples respectively. hashCode is also overridden so Tuples can
 * safely be used in hashMaps. You may put nulls in tuples.
 * </p>
 * <p>
 * Note that tuples are immutable, you can't change the elements in them.
 * Instead create a new tuple, for example
 * <code><pre>
 *  T2&lt;String,Integer> a=t2("aaa",2);
 *  ...
 *  a=t2(a.e1,a.e2+1);
 * </pre></code>
 * </p>
 * <p>
 * Note that t2("",new Vector()) will create a T2<String,new Vector()> rather than
 * T2<String,Collection>. If you want the second element to be a Collection you will
 * have to cast the parameter:
 * <code>t2(String,(Collection)new Vector())</code> or 
 * <code>t2(String,(Collection&lt;Integer>)new Vector())</code>
 * to be more specific.
 * </p>
 *
 * @author olli
 */
public class Tuples {
    
    private Tuples() {}
    private static boolean e(Object o1,Object o2){
        return (o1==null?o2==null:o1.equals(o2));
    }
    public static class T2<E1,E2>{
        public final E1 e1;
        public final E2 e2;
        public T2(final E1 e1,final E2 e2){
            this.e1=e1; this.e2=e2;
        }
        @Override
        public boolean equals(Object obj){
            if(obj instanceof T2){
                return e(e1,((T2)obj).e1) && e(e2,((T2)obj).e2);
            }
            else return false;
        }
        @Override
        public int hashCode(){
            return (e1==null?0:e1.hashCode())+(e2==null?0:e2.hashCode());
        }
        @Override
        public String toString(){
            return "("+e1+","+e2+")";
        }
    }
    public static class T3<E1,E2,E3>{
        public final E1 e1;
        public final E2 e2;
        public final E3 e3;
        public T3(final E1 e1,final E2 e2,final E3 e3){
            this.e1=e1; this.e2=e2; this.e3=e3;
        }
        @Override
        public boolean equals(Object obj){
            if(obj instanceof T3){
                return e(e1,((T3)obj).e1) && e(e2,((T3)obj).e2) && e(e3,((T3)obj).e3);
            }
            else return false;
        }
        @Override
        public int hashCode(){
            return (e1==null?0:e1.hashCode())+(e2==null?0:e2.hashCode())+(e3==null?0:e3.hashCode());
        }
        @Override
        public String toString(){
            return "("+e1+","+e2+","+e3+")";
        }
    }
    public static class T4<E1,E2,E3,E4>{
        public final E1 e1;
        public final E2 e2;
        public final E3 e3;
        public final E4 e4;
        public T4(final E1 e1,final E2 e2,final E3 e3,final E4 e4){
            this.e1=e1; this.e2=e2; this.e3=e3; this.e4=e4;
        }
        @Override
        public boolean equals(Object obj){
            if(obj instanceof T4){
                return e(e1,((T4)obj).e1) && e(e2,((T4)obj).e2) && e(e3,((T4)obj).e3) && e(e4,((T4)obj).e4);
            }
            else return false;
        }
        @Override
        public int hashCode(){
            return (e1==null?0:e1.hashCode())+(e2==null?0:e2.hashCode())+(e3==null?0:e3.hashCode())+(e4==null?0:e4.hashCode());
        }
        @Override
        public String toString(){
            return "("+e1+","+e2+","+e3+","+e4+")";
        }
    }
    public static class T5<E1,E2,E3,E4,E5>{
        public final E1 e1;
        public final E2 e2;
        public final E3 e3;
        public final E4 e4;
        public final E5 e5;
        public T5(final E1 e1,final E2 e2,final E3 e3,final E4 e4,final E5 e5){
            this.e1=e1; this.e2=e2; this.e3=e3; this.e4=e4; this.e5=e5;
        }
        @Override
        public boolean equals(Object obj){
            if(obj instanceof T5){
                return e(e1,((T5)obj).e1) && e(e2,((T5)obj).e2) && e(e3,((T5)obj).e3) && e(e4,((T5)obj).e4) && e(e5,((T5)obj).e5);
            }
            else return false;
        }
        @Override
        public int hashCode(){
            return (e1==null?0:e1.hashCode())+(e2==null?0:e2.hashCode())+(e3==null?0:e3.hashCode())+(e4==null?0:e4.hashCode())+(e5==null?0:e5.hashCode());
        }
        @Override
        public String toString(){
            return "("+e1+","+e2+","+e3+","+e4+","+e5+")";
        }
    }
    public static class T6<E1,E2,E3,E4,E5,E6>{
        public final E1 e1;
        public final E2 e2;
        public final E3 e3;
        public final E4 e4;
        public final E5 e5;
        public final E6 e6;
        public T6(final E1 e1,final E2 e2,final E3 e3,final E4 e4,final E5 e5,final E6 e6){
            this.e1=e1; this.e2=e2; this.e3=e3; this.e4=e4; this.e5=e5; this.e6=e6;
        }
        @Override
        public boolean equals(Object obj){
            if(obj instanceof T6){
                return e(e1,((T6)obj).e1) && e(e2,((T6)obj).e2) && e(e3,((T6)obj).e3) && e(e4,((T6)obj).e4) && e(e5,((T6)obj).e5) && e(e6,((T6)obj).e6);
            }
            else return false;
        }
        @Override
        public int hashCode(){
            return (e1==null?0:e1.hashCode())+(e2==null?0:e2.hashCode())+(e3==null?0:e3.hashCode())+(e4==null?0:e4.hashCode())+(e5==null?0:e5.hashCode())+(e6==null?0:e6.hashCode());
        }
        @Override
        public String toString(){
            return "("+e1+","+e2+","+e3+","+e4+","+e5+","+e6+")";
        }
    }
    public static <E1,E2> T2<E1,E2> t2(final E1 e1,final E2 e2){
        return new T2<E1,E2>(e1,e2);
    }
    public static <E1,E2,E3> T3<E1,E2,E3> t3(final E1 e1,final E2 e2,final E3 e3){
        return new T3<E1,E2,E3>(e1,e2,e3);
    }
    public static <E1,E2,E3,E4> T4<E1,E2,E3,E4> t4(final E1 e1,final E2 e2,final E3 e3,final E4 e4){
        return new T4<E1,E2,E3,E4>(e1,e2,e3,e4);
    }
    public static <E1,E2,E3,E4,E5> T5<E1,E2,E3,E4,E5> t5(final E1 e1,final E2 e2,final E3 e3,final E4 e4,final E5 e5){
        return new T5<E1,E2,E3,E4,E5>(e1,e2,e3,e4,e5);
    }
    public static <E1,E2,E3,E4,E5,E6> T6<E1,E2,E3,E4,E5,E6> t6(final E1 e1,final E2 e2,final E3 e3,final E4 e4,final E5 e5,final E6 e6){
        return new T6<E1,E2,E3,E4,E5,E6>(e1,e2,e3,e4,e5,e6);
    }
    
}
