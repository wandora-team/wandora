/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wandora.utils;
import static org.wandora.utils.Tuples.t2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wandora.utils.Tuples.T2;
/**
 * Provides a list of objects, primarily intended for listeners, and a way to
 * invoke a method in all the registered listeners, that is fire an event.
 * Listeners can be added or removed while an event is being fired and the
 * listener list is being iterated.
 * 
 * Changing the listener list while an event is being fired works such that
 * all changes are stored in a separate list which is processed after the
 * event firing completes. This means that all listeners that were in the
 * listener list at the time event firing started will receive the event even
 * if they are removed from the listener list. Similarly, new listeners added
 * during event firing will not receive the event.
 * 
 * Events can be fired by providing the name of the method or a Method object
 * along with parameters for the method. Alternatively you can extend
 * this class to have separate fire methods for different events.
 *
 * This class is thread safe and no outside synchronization is needed.
 *
 * @author olli
 */
public class ListenerList <T> {
    protected final List<T> listeners;
    protected final List<T2<T,Boolean>> changes;
    protected boolean iterating;
    protected Class<T> cls;
    protected Map<String,Method> methods;
    protected boolean returnValues=false;

    public ListenerList(Class<T> cls){
        this.cls=cls;
        listeners=new ArrayList<T>();
        changes=new ArrayList<T2<T,Boolean>>();
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
        synchronized(listeners){
            if(iterating) changes.add(t2(l,true));
            else listeners.add(l);
        }
    }
    
    
    public void removeListener(T l){
        synchronized(listeners){
            if(iterating) changes.add(t2(l,false));
            else listeners.remove(l);
        }
    }

    
    protected void processChanges(){
        for( T2<T,Boolean> c : changes ){
            if(c.e2) listeners.add(c.e1);
            else listeners.remove(c.e1);
        }
        changes.clear();
    }

    
    public Method findMethod(String event){
        synchronized(listeners){
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

    
    public Object[] fireEventFiltered(Method m,ListenerFilter<T> filter,Object ... params){
        synchronized(listeners){
            iterating=true;

            Object[] ret=null;
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
            finally {
                processChanges();
                iterating=false;
            }
            return ret;
        }
    }


    public Object[] fireEvent(String event,Object ... params){
        return fireEventFiltered(event,null,params);
    }
    
    
    public Object[] fireEventFiltered(String event,ListenerFilter<T> filter,Object ... params){
        Method m=findMethod(event);
        if(m==null) throw new RuntimeException("Trying to fire event "+event+" but method not found.");
        return fireEventFiltered(m,filter,params);
    }
    
    
    public void forEach(EachDelegate delegate,Object ... params) {
        synchronized(listeners){
            iterating=true;

            try{
                for(int i=0;i<listeners.size();i++){
                    T l=listeners.get(i);
                    delegate.run(l,params);
                }
            }
            finally {
                processChanges();
                iterating=false;
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
