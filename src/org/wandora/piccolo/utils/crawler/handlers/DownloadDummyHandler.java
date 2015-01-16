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
 * DownloadDummyHandler.java
 *
 * Created on July 29, 2004, 12:33 PM
 */

package org.wandora.piccolo.utils.crawler.handlers;

import java.net.*;
import java.io.*;
import java.util.*;

import org.apache.lucene.index.*;
import org.apache.lucene.document.*;

import org.wandora.utils.*;
import org.wandora.piccolo.utils.crawler.*;

/**
 *
 * @author  olli
 */
public class DownloadDummyHandler implements Handler {
    private String[] contentTypes;
    
    /** Creates a new instance of AcceptAllDummyHandler */
    public DownloadDummyHandler(String[] contentTypes) {
        this.contentTypes=contentTypes;
    }
    
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    public void handle(CrawlerAccess crawler, InputStream in, int depth, URL page) {
        byte[] buf=new byte[4096];
        try{
            while( in.read(buf)!=-1 ){}
        }catch(IOException ieo){}
    }
    
}
