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
 * LimitedInputStream.java
 *
 * Created on June 9, 2004, 11:06 AM
 */

package org.wandora.tools;
import java.io.*;

/**
 * An InputStream which ends after reading a specified number of bytes from some
 * other InputStream (which may still continue after that).
 *
 * @author  olli
 */
public class LimitedInputStream extends InputStream {
    
    private InputStream in;
    private long length;
    private long read;
    
    public LimitedInputStream(InputStream in,long length){
        this.in=in;
        this.length=length;
    }
    
    public int read() throws IOException {
        if(read>=length) return -1;
        else{
            int c=in.read();
            if(c!=-1) read++;
            return c;
        }
    }
    
    @Override
    public int read(byte[] buf) throws IOException {
        if(buf.length>length-read)
            return read(buf,0,(int)(length-read));
        else return read(buf,0,buf.length);
    }
    
    @Override
    public int read(byte[] buf,int off,int num) throws IOException {
        int newnum=num;
        if(newnum>length-read) newnum=(int)(length-read);
        if(newnum==0) return -1;
        int count=in.read(buf,off,newnum);
        if(count!=-1) read+=count;
        return count;
    }
    
    @Override
    public int available() throws IOException {
        int a=in.available();
        if(a>length-read) return (int)(length-read);
        else return a;
    }

    @Override
    public boolean markSupported(){
        return false;
    }
    
    @Override
    public long skip(long n) throws IOException {
        long skipped;
        if(length-read==0) return -1;
        else if(n>length-read) skipped=in.skip(length-read);
        else skipped=in.skip(n);
        if(skipped>=0) read+=skipped;
        return skipped;
    }
    
    @Override
    public void close(){
        // do nothing, especially do not close in
    }
    
}