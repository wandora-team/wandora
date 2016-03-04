/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2016 Wandora Team
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
 * XMLHandler.java
 *
 * Created on January 28, 2002, 5:46 PM
 */

package org.wandora.piccolo.utils.crawler.handlers;


import org.wandora.utils.ByteBuffer;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;
import java.io.*;
import java.net.*;

import org.wandora.piccolo.utils.crawler.*;
import org.wandora.utils.*;

/**
 *
 * @author  olli
 */


public class XMLHandler extends Object implements Handler {

    private HashMap handlers=new HashMap();
    
    
    /** Creates new XMLHandler */
    public XMLHandler() {
    }
    public XMLHandler(XMLContentHandler[] hs) {
        for(int i=0;i<hs.length;i++){
            addContentHandler(hs[i]);
        }
    }
    
    public XMLHandler(Object[] hs) {
        for(int i=0;i<hs.length;i++){
            addContentHandler((XMLContentHandler)hs[i]);
        }
    }

    public void handle(CrawlerAccess crawler,InputStream in,int depth,URL page) {
        ByteBuffer bb=new ByteBuffer();
        byte[] buf=new byte[8192];
        int l=-1;
        try{
            while( (l=in.read(buf))!=-1 ){
                bb.append(buf,0,l);
            }
        }catch(IOException e){return;}
        
        DOMParser parser=new DOMParser();
        try{
            parser.setFeature( "http://xml.org/sax/features/validation", false);
            parser.parse(new InputSource(new ByteArrayInputStream(bb.getArray(),0,bb.getLength())));
        } catch(SAXException e){e.printStackTrace();return;}
        catch(SecurityException e){e.printStackTrace();return;}
        catch(IOException e){e.printStackTrace();return;}
        org.w3c.dom.Document doc=parser.getDocument();
        
        String docType=doc.getDocumentElement().getTagName().toLowerCase();
        XMLContentHandler h=(XMLContentHandler)handlers.get(docType);
        if(h!=null){
            h.handle(crawler,new ByteArrayInputStream(bb.getArray(),0,bb.getLength()),depth,page);
        }
    }
    
    private static final String[] contentTypes=new String[] {"text/xml","application/xml"};
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    public void addContentHandler(XMLContentHandler h){
        String[] dts=h.getDocTypes();
        for(int i=0;i<dts.length;i++){
            handlers.put(dts[i].toLowerCase(),h);
        }
    }
}





