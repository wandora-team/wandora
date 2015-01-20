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
 * HTMLSurfer.java
 *
 * Created on July 29, 2004, 12:10 PM
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

/**
 *
 * @author  olli
 */
public class HTMLSurfer implements Handler {
    private String[] linkTypes=new String[0];
    
    private String regexAll;
    private String regexOneOf;
    private String startURL;
    /** Creates new CrawlerParser */

    public HTMLSurfer(String[] linkTypes,String regexAll,String regexOneOf,String startURL) {
        this.linkTypes=linkTypes;
        this.regexAll=regexAll;
        this.regexOneOf=regexOneOf;
        this.startURL=startURL;
    }
    
    
    public void handle(CrawlerAccess crawler, InputStream in, int depth, URL page) {
        HTMLParser p=new HTMLParser(linkTypes);
        try {
            p.parse(page, new InputStreamReader(in));
        } 
        catch(IOException e) { return; }
        
        if(depth > 0){
            ArrayList al=new ArrayList();
            URL[] newUrls = p.getNewURLs();
            for(int i=0; i<newUrls.length; i++) {
                if(newUrls[i].toExternalForm().matches(regexAll)){
                    crawler.add(newUrls[i], depth-1);
                }else if(newUrls[i].toExternalForm().matches(regexOneOf)){
                    al.add(newUrls[i]);
                }
            }
            if(al.size()>0){
                crawler.add((URL)al.get((int)(Math.random()*al.size())),depth-1);
            }
            else{
                try{
                    crawler.add(new URL(startURL),depth-1);
                }catch(MalformedURLException e){}
            }
        }
        
    }
    
    
    
    public static final String[] contentTypes=new String[] {"text/html"};
    public String[] getContentTypes() {
        return contentTypes;
    }
}
