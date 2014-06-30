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
 * ResultIterator.java
 *
 *
 */
package org.wandora.query2;
import java.util.*;

/**
 *
 * @author olli
 */
public abstract class ResultIterator {

    /**
     * Gets the next row in the results and moves the iterator forward one row.
     * If there are no more rows NoSuchElementException is thrown.
     */
    public abstract ResultRow next() throws QueryException, NoSuchElementException;
    /**
     * Checks if there is another row available.
     */
    public abstract boolean hasNext() throws QueryException;
    /**
     * Finalizes the iterator and performs any cleanup needed. This iterator
     * cannot be used at all after a call to this.
     */
    public abstract void dispose() throws QueryException;
    /**
     * Resets the iterator to its initial position.
     */
    public abstract void reset() throws QueryException;

    public static class ListIterator extends ResultIterator {
        public int pointer;
        public ArrayList<ResultRow> res;
        public ListIterator(ArrayList<ResultRow> res){
            this.res=res;
            pointer=0;
        }
        public boolean hasNext(){
            return pointer<res.size();
        }
        public ResultRow next() throws NoSuchElementException {
            if(pointer>=res.size()) throw new NoSuchElementException();
            return res.get(pointer++);
        }
        public void dispose(){
        }
        public void reset(){
            pointer=0;
        }
    }

    public static class CachedIterator extends ResultIterator {
        public ArrayList<ResultRow> cache;
        public int maxCacheSize=1000;
        public ResultIterator iter;
        public int pointer=-1;
        public CachedIterator(ResultIterator iter){
            cache=new ArrayList<ResultRow>();
            this.iter=iter;
        }
        public boolean hasNext() throws QueryException {
            if(pointer>=0){
                return pointer<cache.size();
            }
            else {
                if(iter.hasNext()) return true;
                else {
                    if(cache!=null) pointer=cache.size(); // pointer>=0 marks cache usable
                    return false;
                }
            }
        }
        public ResultRow next() throws QueryException, NoSuchElementException {
            if(pointer>=0){
                if(pointer>=cache.size()) throw new NoSuchElementException();
                return cache.get(pointer++);
            }
            else{
                if(iter.hasNext()){
                    ResultRow row=iter.next();
                    if(cache!=null){
                        if(cache.size()<maxCacheSize) {
                            cache.add(row);
                        }
                        else {
                            cache=null; // don't try to use cache from now on
                        }
                    }
                    return row;
                }
                else throw new NoSuchElementException();
            }
        }
        public void dispose() throws QueryException {
            if(cache!=null) cache=null;
            iter.dispose();
        }
        public void reset() throws QueryException {
            if(pointer>=0) {
                pointer=0;
            }
            else {
                if(cache!=null) cache=new ArrayList<ResultRow>();
                iter.reset();
            }
        }
    }

    public static class BufferedIterator extends ResultIterator {
        public ResultIterator iter;
        public ArrayList<ResultRow> buffer;
        public int pointer;
        public int bufferSize=5000;

        public BufferedIterator(ResultIterator iter){
            this.iter=iter;
            pointer=0;
        }

        private boolean fillBuffer() throws QueryException {
            if(buffer!=null) buffer.clear();
            else buffer=new ArrayList<ResultRow>(bufferSize/4);
            while(iter.hasNext() && buffer.size()<bufferSize){
                buffer.add(iter.next());
            }
            pointer=0;
            if(buffer.size()>0) return true;
            else return false;
        }

        public boolean hasNext() throws QueryException {
            if(buffer==null || pointer>=buffer.size()) {
                return fillBuffer();
            }
            return true;
        }
        public ResultRow next() throws QueryException, NoSuchElementException {
            if(!hasNext()) throw new NoSuchElementException();
            return buffer.get(pointer++);
        }
        public void dispose() throws QueryException {
            buffer=null;
            iter.dispose();
        }
        public void reset() throws QueryException {
            buffer=null;
            pointer=0;
            iter.reset();
        }
    }

    public static class EmptyIterator extends ResultIterator {
        public EmptyIterator(){}
        public boolean hasNext(){
            return false;
        }
        public ResultRow next() throws NoSuchElementException {
            throw new NoSuchElementException();
        }
        public void dispose(){}
        public void reset(){}
    }

    public static class SingleIterator extends ResultIterator {
        public ResultRow row;
        public boolean returned;
        public SingleIterator(ResultRow row){
            this.row=row;
            this.returned=false;
        }
        public boolean hasNext(){
            return !returned;
        }
        public ResultRow next() throws NoSuchElementException {
            if(!returned) {
                returned=true;
                return row;
            }
            else throw new NoSuchElementException();
        }
        public void dispose(){}
        public void reset(){
            returned=false;
        }
    }
}
