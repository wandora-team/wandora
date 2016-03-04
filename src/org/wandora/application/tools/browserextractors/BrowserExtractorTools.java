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
 */
package org.wandora.application.tools.browserextractors;
import java.util.regex.*;
import java.util.*;
/**
 *
 * @author olli
 */
public class BrowserExtractorTools {
    
    private static final Pattern urlPattern=Pattern.compile("href\\s*=\\s*(['\"]?)(.+?)\\1",Pattern.CASE_INSENSITIVE);
    public static String extractFirstLink(BrowserExtractRequest request){
        String content=request.getSelection();
        if(content==null) content=request.getContent();
        Matcher m=urlPattern.matcher(content);
        if(m.find()) return m.group(2);
        return null;        
    }
    public static String[] extractLinks(BrowserExtractRequest request){
        String content=request.getSelection();
        if(content==null) content=request.getContent();
        Matcher m=urlPattern.matcher(content);
        ArrayList<String> extracted=new ArrayList<String>(0);
        while(m.find()) {
            extracted.add(m.group(2));
        }
        return extracted.toArray(new String[extracted.size()]);        
    }
    
}
