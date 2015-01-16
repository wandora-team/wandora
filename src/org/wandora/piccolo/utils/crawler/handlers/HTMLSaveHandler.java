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
 * HTMLSaveHandler.java
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

import org.wandora.utils.*;
import org.wandora.piccolo.utils.crawler.*;
import org.wandora.piccolo.*;

import gnu.regexp.*;
import org.w3c.dom.*;

/**
 *
 * @author  olli
 */
public class HTMLSaveHandler extends Object implements Handler {

    private RE[] matchers;
	private OutputMapper[] mappers;
	private String[] substitutes;
	private OutputMapper fileNameMapper;
	private String inencoding="UTF-8"; // default encoding, read from config file
	private String outencoding="UTF-8"; // default encoding, read from config file
	
    public HTMLSaveHandler() {
	}
    public HTMLSaveHandler(Object[] o) {
		org.w3c.dom.Element rootElement=(org.w3c.dom.Element)o[0];
		
		try{
			org.w3c.dom.Element outputmapper=(org.w3c.dom.Element)(rootElement.getElementsByTagName("outputmapper").item(0));
			this.fileNameMapper=(OutputMapper)AbstractCrawler.createObject(outputmapper);
			
			Vector mappers=new Vector();
			Vector matchers=new Vector();
			Vector substitutes=new Vector();
			
			org.w3c.dom.Element mappersE=(org.w3c.dom.Element)(rootElement.getElementsByTagName("mappers").item(0));
			NodeList nl=mappersE.getElementsByTagName("mapper");
			for(int i=0;i<nl.getLength();i++){
				org.w3c.dom.Element e=(org.w3c.dom.Element)nl.item(i);
				NamedNodeMap nnm=e.getAttributes();
				OutputMapper mapper=(OutputMapper)AbstractCrawler.createObject(e);
				String match=nnm.getNamedItem("match").getNodeValue();
				String substitute=nnm.getNamedItem("get").getNodeValue();
				mappers.add(mapper);
				matchers.add(new RE(match));
				substitutes.add(substitute);
			}
			this.mappers=(OutputMapper[])mappers.toArray(new OutputMapper[mappers.size()]);
			this.matchers=(RE[])matchers.toArray(new RE[matchers.size()]);
			this.substitutes=(String[])substitutes.toArray(new String[substitutes.size()]);

			org.w3c.dom.NodeList nodelist=rootElement.getElementsByTagName("inencoding");
			if(nodelist!=null){
				org.w3c.dom.Element encodingE=(org.w3c.dom.Element)nodelist.item(0);
				if(encodingE!=null){
					inencoding=encodingE.getFirstChild().getNodeValue();
				}
			}
			nodelist=rootElement.getElementsByTagName("outencoding");
			if(nodelist!=null){
				org.w3c.dom.Element encodingE=(org.w3c.dom.Element)nodelist.item(0);
				if(encodingE!=null){
					outencoding=encodingE.getFirstChild().getNodeValue();
				}
			}
			
		}catch(Exception e){
			Logger.getLogger().writelog("WRN","Error initializing HTMLSaveHandler "+e.getMessage());
			e.printStackTrace();
		}
	}	
    public HTMLSaveHandler(RE[] matchers,OutputMapper[] mappers,OutputMapper fileNameMapper) {
		this.matchers=matchers;
		this.mappers=mappers;
		this.fileNameMapper=fileNameMapper;
    }
    public HTMLSaveHandler(RE[] matchers,String[] substitutes,OutputMapper[] mappers,OutputMapper fileNameMapper,String inencoding,String outencoding) {
		this.matchers=matchers;
                this.substitutes=substitutes;
		this.mappers=mappers;
		this.fileNameMapper=fileNameMapper;
                this.inencoding=inencoding;
                this.outencoding=outencoding;
    }

    
    public void handle(CrawlerAccess crawler, InputStream in, int depth, URL page) {
		Logger.getLogger().writelog("DBG","    Scanning URL "+page+" depth is "+depth);        
				
		try{
			byte[] buf=new byte[2024];
			int length=0;
			int read;
			while( (read=in.read(buf,length,buf.length-length))!=-1 ) {
				length+=read;
				if(length==buf.length){
					byte[] b=new byte[buf.length*2];
					System.arraycopy(buf,0,b,0,buf.length);
					buf=b;
				}
			}
			String temp=new String(buf,0,length,inencoding);
			
			Reader reader=new StringReader(temp);
			HTMLParser p=new HTMLParser(new String[0]);
			p.parse(page,reader);
	
			if(depth > 0){
				URL[] newUrls = p.getNewURLs();
				for(int i=0; i<newUrls.length; i++) {
					Logger.getLogger().writelog("DBG","    Adding URL "+newUrls[i]+" depth is "+(depth-1));
					crawler.add(newUrls[i], depth-1);
				}
			}
			else Logger.getLogger().writelog("DBG","    Depth is 0");

			
			
			StringBuffer content=new StringBuffer(temp);
			for(int i=0;i<mappers.length;i++){
				REMatch[] matches=matchers[i].getAllMatches(content);
				int offs=0;
				for(int j=0;j<matches.length;j++){
					String replace=mappers[i].map(matches[j],substitutes[i],page);
					content.replace(matches[j].getStartIndex()+offs,matches[j].getEndIndex()+offs,replace);
					offs-=matches[j].toString().length()-replace.length();
				}
			}
			
			OutputStream out=new FileOutputStream(fileNameMapper.map(page.toExternalForm(),null,page));
			temp=new String(content);
			out.write(temp.getBytes(outencoding));
			out.close();
			
		} catch(IOException e){ return; }
		
    }
    
    
    
    public static final String[] contentTypes=new String[] {"text/html"};
    public String[] getContentTypes() {
        return contentTypes;
    }
}
