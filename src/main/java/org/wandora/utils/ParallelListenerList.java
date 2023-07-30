/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2023 Wandora Team
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
 */
package org.wandora.utils;

import static org.wandora.utils.Tuples.t2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is like ListenerList but several events can be firing simultaneously
 * in several threads. ListenerList synchronizes on the list itself meaning that
 * another event will not start processing until the previous one is done, this class
 * avoids that problem by using a different locking mechanism.
 * 
 * This somewhat obfuscates the behaviour of modifying the list while events are
 * firing. Changes are still gathered into a separate changes list but those
 * changes are not processed until all currently firing events have finished.
 * If the changes list is not empty, processing of new events will wait until the
 * changes list has been cleared.
 * 
 * @author olli
 */


public class ParallelListenerList <T> {
    
    protected final ArrayList<T> listeners;
    protected final ArrayList<Tuples.T2<T,Boolean>> changes;
    protected Class<T> cls;
    protected final HashMap<String,Method> methods;
    protected boolean returnValues=false;
    protected int iterating=0;

    public ParallelListenerList(Class<T> cls){
        this.cls=cls;
        listeners=new ArrayList<T>();
        changes=new ArrayList<Tuples.T2<T,Boolean>>();
        methods=new HashMap<String,Method>();
    }

    public int size(){
        return listeners.size();
    }

    public boolean isEmpty(){
        return listeners.isEmpty();
    }

    public boolean isReturnValues() {
        return returnValues;
    }

    public void setReturnValues(boolean returnValues) {
        this.returnValues = returnValues;
    }


    public void addListener(T l){
        synchronized(changes){
            if(iterating==0){
                listeners.add(l);
            }
            else {
                changes.add(t2(l,true));
            }
        }
    }
    public void removeListener(T l){
        synchronized(changes){
            if(iterating==0){
                listeners.remove(l);
            }
            else {
                changes.add(t2(l,false));
            }
        }
    }

    protected void processChanges(){
        // you must hold the changes lock and iterating must be 0 before calling this
        for( Tuples.T2<T,Boolean> c : changes ){
            if(c.e2) listeners.add(c.e1);
            else listeners.remove(c.e1);
        }
        changes.clear();
        synchronized(changes){ // we should already have this lock, just do it again to avoid the warning of calling notifyAll outside synchronized block
            changes.notifyAll();
        }
    }

    public Method findMethod(String event){
        synchronized(methods){
            Method m=methods.get(event);
            if(m==null){
                if(!methods.containsKey(event)){
                    Method[] ms=cls.getMethods();
                    for(int i=0;i<ms.length;i++){
                        if(ms[i].getName().equals(event)) {
                            m=ms[i];
                            break;
                        }
                    }
                    methods.put(event,m);
                }
            }
            return m;
        }
    }
    public Object[] fireEvent(Method m,Object ... params){
        return fireEventFiltered(m,null,params);
    }

    public Object[] fireEventFiltered(Method m,ListenerList.ListenerFilter<T> filter,Object ... params){
        synchronized(changes){
            while(!changes.isEmpty()) {
                try{
                    changes.wait();
                }catch(InterruptedException ie){ return null; }
            }
            iterating++;
        }
        Object[] ret=null;
        try{
            if(returnValues && m.getReturnType()!=null) ret=new Object[listeners.size()];

            try{
                for(int i=0;i<listeners.size();i++){
                    T l=listeners.get(i);
                    if(filter==null || filter.invokeListener(l)){
                        if(ret!=null) ret[i]=m.invoke(l,params);
                        else m.invoke(l, params);
                    }
                }
            }
            catch(IllegalAccessException iae){
                throw new RuntimeException(iae);
            }
            catch(InvocationTargetException ite){
                throw new RuntimeException(ite);
            }
        }
        finally{
            synchronized(changes){
                iterating--;
                if(iterating==0 && !changes.isEmpty()) processChanges();
            }
        }
        return ret;                    
    }


    public Object[] fireEvent(String event,Object ... params){
        return fireEventFiltered(event,null,params);
    }
    public Object[] fireEventFiltered(String event,ListenerList.ListenerFilter<T> filter,Object ... params){
        Method m=findMethod(event);
        if(m==null) throw new RuntimeException("Trying to fire event "+event+" but method not found.");
        return fireEventFiltered(m,filter,params);
    }
    
    public void forEach(ListenerList.EachDelegate delegate,Object ... params) {
        synchronized(changes){
            while(!changes.isEmpty()) {
                try{
                    changes.wait();
                }catch(InterruptedException ie){ return; }
            }
            iterating++;
        }
        try{
            for(int i=0;i<listeners.size();i++){
                T l=listeners.get(i);
                delegate.run(l,params);
            }
        }
        finally{
            synchronized(changes){
                iterating--;
                if(iterating==0 && !changes.isEmpty()) processChanges();
            }
        }        
    }

    public static interface EachDelegate<T> {
        public void run(T listener,Object ... params);
    }
    
    public static interface ListenerFilter<T> {
        public boolean invokeListener(T listener);
    }
    
}
