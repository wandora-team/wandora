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
 * SwingTools.java
 *
 * Created on 18. toukokuuta 2005, 16:36
 *
 * Copyright 2004-2005 Grip Studios Interactive Oy (office@gripstudios.com)
 * Created by Olli Lyytinen, AKi Kivela
 */

package org.wandora.utils.swing;
import org.wandora.utils.*;
import javax.swing.SwingUtilities;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
/**
 *
 * @author olli
 */
public class SwingTools {
    
    /**
     * Use this to debug access to Swing objects outside event dispatch thread.
     */
    public static void debugCheckEventThread(){
        if(!EventQueue.isDispatchThread()){
            System.out.println("WARNING: not event dispatch thread");
            try{
                throw new Exception("Not event dispatch thread");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
            
    /**
     * Runs the given Runnable immediately if called from event dispatch thread
     * or at some later time inside event dispatch thread otherwise.
     */
    public static void swingOperation(Runnable run){
        if(!EventQueue.isDispatchThread()) SwingUtilities.invokeLater(run);
        else run.run();
    }
    
    /**
     * Runs the given Runnable immediately if called from event dispatch thread
     * or at some later time inside event dispatch thread otherwise and blocks
     * until it is ready. Doesn't throw exceptions. Instead returns false in
     * case of an exception.
     */
    public static boolean swingOperationBlock(Runnable run) {
        if(!EventQueue.isDispatchThread()) {
            try{
                SwingUtilities.invokeAndWait(run);
                return true;
            }
            catch(Exception e){
                return false;
            }
        }
        else {
            run.run();
            return true;
        }
    }
    
    /**
     * Runs the given RunnableReturn immediately if called from event dispatch thread
     * or at some later time inside event dispatch thread otherwise. Returns
     * a special return value which can be used to get the return value of
     * RunnableReturn parameter.
     */
    public static <R> SwingReturn<R> swingOperation(final RunnableReturn<R> run){
        if(!EventQueue.isDispatchThread()) {
            final ValueContainer<R> container=new ValueContainer<R>();
            final Semaphore semaphore=new Semaphore(0);
            swingOperation(new Runnable(){public void run(){
                R r=run.run();
                container.setValue(r);
                semaphore.release();
            }});
            return new SwingReturn(semaphore,container);
        }
        else return new SwingReturn<R>(new Semaphore(1),new ValueContainer<R>(run.run()));
    }

    /**
     * Runs the given RunnableReturn immediately if called from event dispatch thread
     * or at some later time inside event dispatch thread otherwise. Blocks until
     * ready and returns value from RunnableReturn param.
     */
    public static <R> R swingOperationBlock(final RunnableReturn<R> run){
        SwingReturn<R> ret=swingOperation(run);
        return ret.getValueNoInterrupt();
    }
    
    public static interface RunnableReturn<R>{
        public R run();
    }
    
    /**
     * A class used for the return value of swingOperation(RunnableReturn). The
     * getValue method will return the return value of the operation when it is
     * ready. It will block if necessary.
     */
    public static class SwingReturn<R>{
        protected Semaphore semaphore;
        protected ValueContainer<R> container;
        protected SwingReturn(Semaphore semaphore,ValueContainer<R> container){
            this.semaphore=semaphore;
            this.container=container;
        }
        public R getValue() throws InterruptedException {
            semaphore.acquire();
            return container.getValue();
        }
        public R getValueNoInterrupt(){
            try{
                return getValue();
            }
            catch(InterruptedException e){
                return null;
            }
        }
    }
    
    public static class ValueContainer<T>{
        private T value;
        public ValueContainer(){}
        public ValueContainer(T v){
            value=v;
        }
        public synchronized T getValue(){return value;}
        public synchronized void setValue(T v){value=v;}
    }
    
    private static final Object workLock=new Object();
    private static Thread workThread=null;
    public static boolean doWork(final Runnable run,final WaitNotificationHandler wnh){
        boolean runInCurrent=false;
        SyncBlock: synchronized(workLock){
            if(Thread.currentThread()==workThread) {
                runInCurrent=true;
                break SyncBlock;
            }
            if(workThread!=null) {System.out.println("Work ignored"); return false;}
            final ValueContainer<Integer> container=new ValueContainer<Integer>(0);
            Thread dialogThread=new Thread(){
                @Override
                public void run(){
                    try{
                        Thread.sleep(500);
                    }catch(InterruptedException ie){}
                    int v=0;
                    synchronized(workLock){
                        container.setValue(container.getValue()+1);
                        v=container.getValue();
                    }
                    if(v==1) wnh.showNotification();
                }
            };
            workThread = new Thread(){
                @Override
                public void run(){
                    Exception exception=null;
                    try{
                        run.run();
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        exception=e;
                    }
                    int v=0;
                    synchronized(workLock){
                        v=container.getValue();
                        container.setValue(container.getValue()-1);
                        workThread=null;
                    }
                    if(v==1){
                        while(!wnh.isNotificationVisible()){
                            Thread.yield();
                        }
                        wnh.hideNotification();
                    }
    /*                if(exception!=null && wdh!=null){
                        wdh.showExceptionDialog(exception);
                    }*/
                }
            };
            dialogThread.start();
            workThread.start();
            return true;
        }
        if(runInCurrent){
            run.run();
            return true;
        }
        else return false; // should not happen
    }
}
