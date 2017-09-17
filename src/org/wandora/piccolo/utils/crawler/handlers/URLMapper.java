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
 */


package org.wandora.piccolo.utils.crawler.handlers;


import java.net.*;
import java.util.*;

import org.wandora.piccolo.*;

import org.w3c.dom.*;
import gnu.regexp.*;

/**
 *
 * @author  olli
 */
public class URLMapper implements OutputMapper {
	
	private String prefix,postfix;
	
	public URLMapper(){
		prefix="";
		postfix="";
	}
	public URLMapper(String prefix,String postfix){
		this.prefix=prefix;
		this.postfix=postfix;
	}
	
	public URLMapper(Object[] o){
		try{
			Element rootElement=(Element)o[0];
			org.w3c.dom.Element e;
			try{
				e=(org.w3c.dom.Element)(rootElement.getElementsByTagName("prefix").item(0));
				this.prefix=e.getFirstChild().getNodeValue();
			} catch(Exception ex){}			
			try{
				e=(org.w3c.dom.Element)(rootElement.getElementsByTagName("postfix").item(0));
				this.postfix=e.getFirstChild().getNodeValue();
			} catch(Exception ex){}
			if(this.prefix==null) this.prefix="";
			if(this.postfix==null) this.postfix="";
		} catch(Exception e){
			Logger.getLogger().writelog("WRN","Error parsing URLMapper parameters: "+e.getMessage());
		}
	}
        
        private static int encodeCounter=0;
        private static Hashtable urlMap=new Hashtable();
	
	private static RE doctypeRE=null;
	public static String encodeURL(String url){
                int pagenum=-1;
                synchronized(URLMapper.class){
                    Integer num=(Integer)urlMap.get(url);
                    if(num==null){
                        num=new Integer(encodeCounter++);
                        urlMap.put(url,num);
                    }
                    pagenum=num.intValue();
                }
		if(doctypeRE==null){
			try{
				doctypeRE=new RE("[a-zA-Z0-9_]*");
			} catch( REException e){
				Logger.getLogger().writelog("ERR","REException "+e.getMessage());
			}
		}
		int ind=url.lastIndexOf(".");
		String type=".html";
		if(ind>-1){
			type=url.substring(ind+1);
			if(doctypeRE.isMatch(type)){url=url.substring(0,ind); type="."+type;}
			else type=".html";
		}
                
                return "file"+pagenum+type;
                
/*		byte[] bytes=url.getBytes();
                String base64=com.gripstudios.utils.Base64.encodeBytes(bytes,0,bytes.length,com.gripstudios.utils.Base64.DONT_BREAK_LINES);
                base64=base64.replaceAll("\\+", "_");
                base64=base64.replaceAll("\\\\", "~");
                return base64+type;
                */
                
/*                byte[] bytes=null;
                try{
                    java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
                    java.util.zip.GZIPOutputStream zip=new java.util.zip.GZIPOutputStream(baos);
                    zip.write(url.getBytes());
                    zip.finish();
                    zip.close();
                    bytes=baos.toByteArray();
                }catch(IOException ioe){
                }
                
		StringBuffer out=new StringBuffer();
		for(int i=0;i<bytes.length;i++){
                        int c=bytes[i];
                        if(c<0) c+=256;
			if(c<16) out.append("0");
			out.append(Integer.toString(c,16));
		}
		return new String(out)+type;*/
	}
	
	public String map(Object match,String get,URL page){
		String matchs=match.toString();
		if(matchs.indexOf("javascript:")!=-1 || matchs.indexOf("mailto:")!=-1){
			return matchs;
		}		
		String m=null;
		String prefix=this.prefix;
		String postfix=this.postfix;
		if(get==null) m=(String)match;
		else {
			m=((REMatch)match).substituteInto(get);
			prefix=((REMatch)match).substituteInto(prefix);
			postfix=((REMatch)match).substituteInto(postfix);
		}
		m=m.replaceAll(" ","+");
		try{
			URL u=null;
			if(m.startsWith("?")){
				// new URL(page,m) can't handle this correctly
				String oldurl=page.toExternalForm();
				int ind=oldurl.indexOf("?");
				if(ind==-1) u=new URL(page,m);
				else u=new URL(oldurl.substring(0,ind)+m);
			}
			else u=new URL(page,m);
			return prefix+encodeURL(u.toExternalForm())+postfix;
		} catch(MalformedURLException e){ Logger.getLogger().writelog("WRN","Malformed url exception caught when mapping url. (\""+m+"\")");}
		return null;
	}
}
