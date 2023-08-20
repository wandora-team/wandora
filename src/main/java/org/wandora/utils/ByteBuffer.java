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
 *
 * 
 *
 * ByteBuffer.java
 *
 * Created on 18. maaliskuuta 2003, 16:26
 */

package org.wandora.utils;

/**
 *
 * @author  akivela
 */
public class ByteBuffer {
    
    
    private static final int BUFFER=8192;
    private byte[] buffer;
    private int ptr;
    
    
    
    public ByteBuffer(){
        buffer=new byte[BUFFER];
        ptr=0;
    }
    
    
    public void append(byte[] b){
        append(b,0,b.length);
    }
    
    
    public void append(byte[] b,int offs,int length){
        if(ptr+length<=buffer.length){
            System.arraycopy(b,offs,buffer,ptr,length);
            ptr+=length;
        }
        else{
            int l=buffer.length;
            while(ptr+length>l){
                l*=2;
            }
            byte[] n=new byte[l];
            System.arraycopy(buffer,0,n,0,ptr);
            buffer=n;
            append(b,offs,length);
        }
    }
    
    public byte[] getArray() { return buffer; }
    
    public int getLength() { return ptr; }
}

