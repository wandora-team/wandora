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
 * HTMLParser.java
 *
 * Created on 18. maaliskuuta 2003, 17:03
 */

package org.wandora.piccolo.utils.crawler;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.text.html.parser.*;
import javax.swing.text.*;

import org.wandora.utils.*;
import org.wandora.piccolo.*;

/**
 *
 * @author  olli, akivela
 */
public class HTMLParser extends Parser {
    
    
    private String[] linkTypes=new String[0];
    private ArrayList newURLs;

    private int state=STATE_OTHER;
    private static final int STATE_OTHER=0;
    private static final int STATE_TITLE=1;
    private static final int STATE_BODY=2;

    private String content;
    private String title;
    private URL currentPage = null;
    
    // ---
    
    private static DTD HTMLDTD;
    static {
        try {
            InputStream dtdStream = System.out.getClass().getResourceAsStream("/javax/swing/text/html/parser/html32.bdtd");
            DataInputStream dis = new DataInputStream (dtdStream);
            DTD dtd=DTD.getDTD("HTML");
            dtd.read (dis);
            dis.close();
            HTMLDTD=dtd;
        } 
        catch(IOException e){
            Logger.getLogger().writelog("ERR", "Unable to create DTD for HTML parser!");
        }
    }
            
    
    
    // -------------------------------------------------------------------------
    
    
    public HTMLParser(DTD dtd) {
        super(dtd);
    }

    
    public HTMLParser(DTD dtd,String[] linkTypes) {
        super(dtd);
        this.linkTypes=linkTypes;
    }


    public HTMLParser(String[] linkTypes) {
        super(HTMLDTD);
        this.linkTypes=linkTypes;
    }
    
    
    // -------------------------------------------------------------------------
  

    public void parse(URL url, Reader in) throws IOException {
        currentPage = url;
        content=new String();
        title=new String();
        newURLs=new ArrayList();
        super.parse(in);
    }

    
    public URL[] getNewURLs() { return (URL[])newURLs.toArray( new URL[] {} ); }
    public HashMap getOccurances() { return new HashMap(); }
    public String getContent() { return content; }
    public String getTitle() { return title; }

    
    
    private boolean passLink(String value) {
        if(value.indexOf("mailto:")==-1 && value.indexOf("javascript:")==-1){
            // Try to limit the crawling to file types that we know we can handle by
            // looking the filename. If we are not sure, it is okay to pass the link
            // forward. It will later be handled according to its content-type rather
            // than the filename.
            int ind=value.lastIndexOf(".");
            if(linkTypes == null || linkTypes.length==0) {
                return true;
            }
            if(value.length()-(ind+1)>=1 && value.length()-(ind+1)<=5 // the postfix should be 1-5 chars long
            && value.indexOf("?")==-1 // url should not contain parameters at the end
            && value.lastIndexOf("/")<ind) { //the last slash must be before the last dot
                String type=value.substring(ind+1);
                // Type could be the top level domain if url is "http://www.foo.com". 
                // We presume here that all absolute urls contain the protocol part.
                // If the protocol part is found, search for a "/" after the protocol part.
                ind=value.indexOf("://");
                if(ind!=-1 && value.lastIndexOf("/")<=ind+2) {
                    return true;
                }
                for(int i=0;i<linkTypes.length;i++) {
                    if(linkTypes[i].equalsIgnoreCase(type)) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
        return false;
    }

    
    @Override
    protected void handleStartTag(TagElement tag){
        String name=tag.getElement().getName();
        if(name.equalsIgnoreCase("a")){
            SimpleAttributeSet attrs=getAttributes();
            java.util.Enumeration enumeration=attrs.getAttributeNames();
            while(enumeration.hasMoreElements()){
                Object attr=enumeration.nextElement();
                String value=(String)attrs.getAttribute(attr);
                if(attr.toString().equalsIgnoreCase("href")) {
                    if(passLink(value)) {
                        handleLink(value);
                    }
                }
            }                
        }
        else if(name.equalsIgnoreCase("table")){
            SimpleAttributeSet attrs=getAttributes();
            java.util.Enumeration enumeration=attrs.getAttributeNames();
            while(enumeration.hasMoreElements()){
                Object attr=enumeration.nextElement();
                String value=(String)attrs.getAttribute(attr);
                if(attr.toString().equalsIgnoreCase("background")) {
                    if(passLink(value)) {
                        handleLink(value);
                    }
                }
            }                
	}
        else if(name.equalsIgnoreCase("td")){
            SimpleAttributeSet attrs=getAttributes();
            java.util.Enumeration enumeration=attrs.getAttributeNames();
            while(enumeration.hasMoreElements()){
                Object attr=enumeration.nextElement();
                String value=(String)attrs.getAttribute(attr);
                if(attr.toString().equalsIgnoreCase("background")) {
                    if(passLink(value)) {
                        handleLink(value);
                    }
                }
            }                
	}
        else if(name.equalsIgnoreCase("title")) {
            state=STATE_TITLE;
        }
        else if(name.equalsIgnoreCase("body")) {
            state=STATE_BODY;
        }
    }
    
    
    @Override
    protected void handleEmptyTag(TagElement tag){
        String name=tag.getElement().getName();
        if(name.equalsIgnoreCase("img")){
            SimpleAttributeSet attrs=getAttributes();
            java.util.Enumeration enumeration=attrs.getAttributeNames();
            while(enumeration.hasMoreElements()){
                Object attr=enumeration.nextElement();
                String value=(String)attrs.getAttribute(attr);
                if(attr.toString().equalsIgnoreCase("src")){
                    if(passLink(value)) {
                        handleLink(value);
                    }
                }
            }
        }
        else if(name.equalsIgnoreCase("link")){
            SimpleAttributeSet attrs=getAttributes();
            java.util.Enumeration enumeration=attrs.getAttributeNames();
            while(enumeration.hasMoreElements()){
                Object attr=enumeration.nextElement();
                String value=(String)attrs.getAttribute(attr);
                if(attr.toString().equalsIgnoreCase("href")){
                    if(passLink(value)) {
                        handleLink(value);
                    }
                }
            }                
        }
        else if(name.equalsIgnoreCase("br")){
            if(state==STATE_BODY) {
                content+="\n";
            }
        }
    }
    
    
    @Override
    protected void handleEndTag(TagElement tag){
        String name=tag.getElement().getName();
        if(name.equalsIgnoreCase("title")) {
            state=STATE_OTHER;
        }
    }
    
    
    @Override
    protected void handleText(char[] data){
        if(state==STATE_TITLE) {
            title=new String(data);
        }
        else if(state==STATE_BODY) {
            content+=new String(data);
        }
    }                

    
    private void handleLink(String url) {        
        URL u;
        url = url.replaceAll(" ","+");		
        try{
            if(currentPage!=null) {
                if(url.startsWith("?")) {
                    // new URL(currentPage,url) can't handle this correctly
                    String oldurl=currentPage.toExternalForm();
                    int ind=oldurl.indexOf("?");
                    if(ind==-1) {
                        u=new URL(currentPage,url);
                    }
                    else {
                        u=new URL(oldurl.substring(0,ind)+url);
                    }
                }
                else {
                    u=new URL(currentPage,url);
                }
            }
            else {
                u=new URL(url);
            }
        } 
        catch(MalformedURLException e){return;}
        newURLs.add(u);
    }

    
}
