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
 * ReaderWriterLock.java
 *
 * Created on July 9, 2004, 2:17 PM
 */

package org.wandora.utils;

/**
 *
 * ReaderWriterLock is a locking mechanism that allows several readers to access a resource simultaneously
 * but only single writer. When writer has locked the resource neither readers nor other writers are allowed
 * acces until the resource has been released by the writer who has the lock. Also writer will not get a lock
 * if a reader or some other writer has the lock. This lock operates with the writers-first principle meaning
 * that if a writer is waiting for the lock, no more readers are allowed to get the lock. When you get a lock
 * (and the get*Lock method returns true) you should always put the following code inside a try block and release
 * the lock in the finally block after the try. This will ensure that the lock is released, even if an exception is
 * thrown and not handled. If you fail to release the lock, your application will most likely get stuck.
 *
 * Note that if the same thread tries to get a second writer lock, the thread will get blocked indefinitely and
 * also block any other threads trying to use this lock. This behavior is somewhat different than synchronized
 * blocks in java which will get a lock for the executing thread to the specified object's monitor if, it does 
 * not have that allready.
 *
 * Take care that you release exactly as many locks as you acquire. If you release more, a RuntimeException
 * will be thrown and if you release less, others won't be able to get the lock.
 *
 * @author  olli
 */
public class ReaderWriterLock {
    public static final int LOCK_READ=0;
    public static final int LOCK_WRITE=1;
    
    private int numReaders;
    private int numWriters;
    private int waitingWriters;
    
    public ReaderWriterLock(){
        numReaders=0;
        numWriters=0;
        waitingWriters=0;
    }
    public synchronized boolean getReaderLockNonBlocking(){
        if(numWriters==0 && waitingWriters==0) {
            numReaders++;
            return true;
        }
        else return false;
    }
    public synchronized boolean getReaderLock(){
        while(numWriters>0 || waitingWriters>0) {
            try{
                this.wait();
            }catch(InterruptedException ie){return false;}
        }
        numReaders++;        
        return true;
    }
    public synchronized void releaseReaderLock(){
        numReaders--;
        if(numReaders<0) throw new RuntimeException("Too many readers realeased");
        this.notifyAll();
    }
    public synchronized boolean getWriterLockNonBlocking(){
        if(numWriters==0 && numReaders==0) {
            numWriters++;
            return true;
        }
        else return false;
    }
    public synchronized boolean getWriterLock(){
        waitingWriters++;
        while(numWriters>0 || numReaders>0) {
            try{
                this.wait();
            }catch(InterruptedException ie){return false;}
        }            
        waitingWriters--;
        numWriters++;        
        return true;
    }
    public synchronized void releaseWriterLock(){
        numWriters--;
        if(numWriters<0) throw new RuntimeException("Too many writers realeased");
        this.notifyAll();
    }
    public synchronized boolean getLockNonBlocking(int type){
        if(type==LOCK_READ) return getReaderLockNonBlocking();
        else return getWriterLockNonBlocking();
    }
    public synchronized boolean getLock(int type){
        if(type==LOCK_READ) return getReaderLock();
        else return getWriterLock();
    }
    public synchronized void releaseLock(int type){
        if(type==LOCK_READ) releaseReaderLock();
        else releaseWriterLock();
    }
    
}
