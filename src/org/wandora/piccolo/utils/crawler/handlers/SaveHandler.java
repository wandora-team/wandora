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
 * SaveHandler.java
 *
 * Created on December 5, 2001, 4:26 PM
 */

package org.wandora.piccolo.utils.crawler.handlers;


import javax.swing.text.html.parser.*;
import javax.swing.text.*;
import java.net.*;
import java.io.*;
import java.util.*;

import org.apache.lucene.index.*;
import org.apache.lucene.document.*;

import org.wandora.piccolo.utils.crawler.*;
import org.wandora.piccolo.*;


/**
 *
 * @author  olli
 */
public class SaveHandler extends Object implements Handler {

	private OutputMapper fileNameMapper;
	
    public SaveHandler(OutputMapper fileNameMapper) {
		this.fileNameMapper=fileNameMapper;
    }
	public SaveHandler(Object[] o){
		org.w3c.dom.Element rootElement=(org.w3c.dom.Element)o[0];
		
		try{
			org.w3c.dom.Element outputmapper=(org.w3c.dom.Element)(rootElement.getElementsByTagName("outputmapper").item(0));
			this.fileNameMapper=(OutputMapper)AbstractCrawler.createObject(outputmapper);
		}catch(Exception e){
			Logger.getLogger().writelog("WRN","Error initializing HTMLSaveHandler "+e.getMessage());
		}
	}
    
    public void handle(CrawlerAccess crawler, InputStream in, int depth, URL page) {
		
		try{
			OutputStream out=new FileOutputStream(fileNameMapper.map(page.toExternalForm(),null,page));
			byte[] buf=new byte[2024];
			int read;
			while( (read=in.read(buf,0,buf.length))!=-1 ) {
				out.write(buf,0,read);
			}			
			out.close();
			
		} catch(IOException e){ return; }
		
    }
    
    
    
    public static final String[] contentTypes=new String[] {"*"};
    public String[] getContentTypes() {
        return contentTypes;
    }
}
