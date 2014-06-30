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
 * URLMask.java
 *
 * Created on November 30, 2001, 2:58 PM
 */

package org.wandora.piccolo.utils.crawler;


import java.net.URL;
import java.util.Collection;



/**
 *
 * URLMask is used by WebCrawler to decide whether or not to crawl an url.
 * 
 * @author  olli
 */
public class URLMask extends Object {

    private String[] url;
    private int[] type;
    public static int TYPE_DENY=0;
    public static int TYPE_ALLOW=1;
    
    /** Creates new URLMask */
    public URLMask() {
        url=new String[0];
        type=new int[0];
    }

    public URLMask(String url,int type) {
        this.url = new String[] { url };
        this.type = new int[] { type };
    }

    public URLMask(String[] url,int[] type) {
        this.url = (String[])url.clone();
        this.type = (int[])type.clone();
    }
    
    public URLMask(Collection<String> url, Collection<Integer> type) {
        this.url = url.toArray( new String[] {} );
        this.type = new int[type.size()];
        int i=0;
        for(Integer t : type) {
            this.type[i++] = t.intValue();
        }
    }
    

    public URLMask(String[] params) {
        url=new String[params.length/2];
        type=new int[params.length/2];
        
        for(int i=0,j=0;j<params.length;i++,j+=2){
            url[i]=params[j];
            type[i]=TYPE_ALLOW;
            if(params[j+1].equalsIgnoreCase("deny")) {
                type[i]=TYPE_DENY;
            }
        }
    }
    

    
    
    
    protected boolean match(String test,String re) {
        if(test == null || re == null) {
            return false;
        }
        return test.matches(re);
    }
    
    
    
    public boolean allow(URL test){
        return allow(test.toString());
    }
    
    
    
    public boolean allow(String test){
        boolean ret=false;
        for(int i=0; i<url.length; i++) {
            if(type[i]==TYPE_DENY && ret) {
                if(match(test,url[i])) {
                    ret=false;
                }
            }
            else if(type[i]==TYPE_ALLOW && !ret) {
                if(match(test,url[i])) {
                    ret=true;
                }
            }
        }
        return ret;
    }

    
    
    
    public boolean allow(Object test){
        if(test instanceof URL) {
            return allow((URL) test);
        }
        else {
            return allow(test.toString());
        }
    }

}
