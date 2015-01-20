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
 */

package org.wandora.application.tools.extractors.bookmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.Icon;

import org.apache.commons.io.IOUtils;

import org.jsoup.*;
import org.jsoup.parser.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.wandora.application.gui.UIBox;


import org.wandora.application.tools.extractors.AbstractExtractor;
import static org.wandora.application.tools.extractors.AbstractExtractor.FILE_EXTRACTOR;
import static org.wandora.application.tools.extractors.AbstractExtractor.URL_EXTRACTOR;
import org.wandora.application.tools.extractors.ExtractHelper;
import static org.wandora.application.tools.extractors.list.DefinitionListExtractor.contentTypes;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author Eero
 */


public class BookmarkExtractor extends AbstractExtractor {

    private String ROOT_SI = "http://wandora.org/si/bookmark";
    private String HREF_SI = "http://wandora.org/si/bookmark/href";
    private String ADD_SI  = "http://wandora.org/si/bookmark/added";
    private String MOD_SI  = "http://wandora.org/si/bookmark/modified";
    private String ICON_SI = "http://wandora.org/si/bookmark/icon";

    private String LANG = XTMPSI.getLang("en");
    private Topic langTopic;
    private SimpleDateFormat df;
    
    private String[][] itemProps = {
        {HREF_SI, "href", "href"},
        {ICON_SI, "icon_uri", "icon uri"}
    };
    
    private String[][] itemTimeProps = {
        {ADD_SI, "add_date", "date added"},
        {MOD_SI, "last_modified", "date modified"}
    };
    
    
    
    @Override
    public String getName() {
        return "Firefox bookmark extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts topics and associations out of Firefox bookmark HTML file.";
    }
    
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR;
    }
    
    public static final String[] contentTypes=new String[] { "text/html" };
    
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_html.png");
    }
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        if(!f.isFile()) return false;
        try {
            Document d = Jsoup.parse(f, null);
            parse(d,t);
        } catch (Exception e) {
            log(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        try {
            Document d = Jsoup.connect(u.toString()).get();
            parse(d,t);
        } catch (Exception e) {
            log(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        try {
            Document d = Jsoup.parse(str);
            parse(d,t);
        } catch (Exception e) {
            log(e);
            return false;
        }
        return true;
    }

    private void parse(Document d, TopicMap t) 
            throws FileNotFoundException, IOException, TopicMapException, ParseException{
        
        langTopic = getOrCreateTopic(t, LANG);
        df  = new SimpleDateFormat();
        
        Elements menu = d.select("body > dl");
        for(Element e : menu){
            parseCategory(e,t);
        }
    }
    
    private void parseCategory(Element c, TopicMap t) throws TopicMapException, ParseException{
        Topic wc = getWandoraClass(t);
        Topic root = getOrCreateTopic(t, ROOT_SI, "Bookmark");
        root.setSubjectLocator(new Locator(ROOT_SI));
        root.setDisplayName(LANG, "Bookmark");
        makeSubclassOf(t, root, wc);
        for(Element child: c.children()){
            if(child.tagName().equals("dt")){
                for(Element grandChild: child.children()){
                    if(grandChild.tagName().equals("a"))
                        parseItem(grandChild,root,t);
                    else if(grandChild.tagName().equals("dl"))
                        parseCategory(child, root, t);
                }
            }
        }
        parseCategory(c, root, t);
    }
    
    private void parseCategory(Element c, Topic parent, TopicMap t) throws TopicMapException, ParseException{
        
        Topic cTopic = parent;
        Elements children = c.children();
        for(Element child : children){
            if(child.tagName().equals("h3")) {
                String cLocator = parent.getSubjectLocator().toString();
                cLocator += "/" + urlEncode(child.html());
                String cName = child.ownText();
                
                cTopic = getOrCreateTopic(t, cLocator);
                cTopic.setSubjectLocator(new Locator(cLocator));
                cTopic.setBaseName(cName + " (Bookmark)");
                cTopic.setDisplayName(LANG, cName);
                makeSubclassOf(t, cTopic, parent);
            }
        }
        
        for(Element child : children){
            if(!child.tagName().equals("dl")) continue;
            
            for(Element grandChild : child.children()){
                if(!grandChild.tagName().equals("dt")) continue;
                for(Element ggChild : grandChild.children()){
                    if(ggChild.tagName().equals("a"))
                        parseItem(ggChild,cTopic,t);
                    else if(ggChild.tagName().equals("dl"))
                        parseCategory(grandChild, cTopic, t);
                }
                
            }
            
        }
    }
    
    private void parseItem(Element i, Topic parent, TopicMap t) throws TopicMapException, ParseException{
        String cLocator = i.attr("href");
        Topic iTopic = getOrCreateTopic(t, cLocator);
        iTopic.setSubjectLocator(new Locator(cLocator));
        iTopic.setBaseName(i.ownText() + " (Bookmark)");
        iTopic.setDisplayName(LANG, i.ownText());
        iTopic.addType(parent);
        
        String attr;
        Topic type;
        for (int j = 0; j < itemProps.length; j++) {
            type = getOrCreateTopic(t, itemProps[j][0],itemProps[j][2]);
            attr = i.attr(itemProps[j][1]);
            if(attr.length() > 0) iTopic.setData(type, langTopic, attr);
        }
        long timeStamp;
        for (int j = 0; j < itemTimeProps.length; j++) {
            type = getOrCreateTopic(t, itemTimeProps[j][0],itemTimeProps[j][2]);
            attr = i.attr(itemTimeProps[j][1]);
            
            if(attr.length() > 0) {
                timeStamp = Integer.parseInt(attr);
                timeStamp *=  1000;
                iTopic.setData(type, langTopic, df.format(new Date(timeStamp)));
            }   
        }
        
    }
    
    private Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }
    
    private Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }


    private Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }
    
    private void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }
    
}
