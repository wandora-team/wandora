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
 * TwineExtractor.java
 *
 * Created on 7.2.2009,12:32
 */


package org.wandora.application.tools.extractors.rdf;


import java.net.*;
import javax.swing.*;
import org.wandora.application.tools.extractors.rdf.*;
import org.wandora.application.tools.extractors.rdf.rdfmappings.*;
import org.wandora.topicmap.*;


import org.wandora.application.gui.*;



/**
 *
 * @author akivela
 */
public class TwineExtractor extends AbstractRDFExtractor {
  
    private RDF2TopicMapsMapping[] mappings = new RDF2TopicMapsMapping[] {
        new TwineMapping(),
        new RDFSMapping(),
        new RDFMapping(),
    };
    
    
    
    
    public TwineExtractor() {
        
    }
    

    @Override
    public String getName() {
        return "Twine RDF extractor...";
    }
    
    @Override
    public String getDescription(){
        return "Read Twine RDF feed and convert it to a topic map.";
    }
       
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_twine.png");
    }

    public RDF2TopicMapsMapping[] getMappings() {
        return mappings;
    }
    
    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        url = fixTwineURL("http://www.twine.com/item/", url);
        url = fixTwineURL("http://www.twine.com/twine/", url);
        return super._extractTopicsFrom(url, topicMap);
    }
    
    
    
    public URL fixTwineURL(String twineUrlBody, URL url) {
        String urlStr = url.toExternalForm();
        if(urlStr != null && urlStr.length() > 0) {
            if(!urlStr.endsWith("?rdf") && urlStr.startsWith(twineUrlBody)) {
                int startIndex = twineUrlBody.length();
                int endIndex = startIndex;
                while(endIndex<urlStr.length() &&
                        urlStr.charAt(endIndex)!='/' &&
                        urlStr.charAt(endIndex)!='?' &&
                        urlStr.charAt(endIndex)!=' ') endIndex++;
                String twineId = urlStr.substring(startIndex, endIndex);
                if(twineId != null && twineId.length() > 0) {
                    try {
                        String fixedUrlStr = twineUrlBody+twineId+"?rdf";
                        log("Fixed Twine url: "+fixedUrlStr);
                        url = new URL(fixedUrlStr);
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
        }
        return url;
    }

    
    
    
    
}
