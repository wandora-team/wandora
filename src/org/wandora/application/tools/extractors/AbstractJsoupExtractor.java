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

package org.wandora.application.tools.extractors;

import java.io.File;
import java.net.URL;
import javax.swing.Icon;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.TopicTools;

/**
 *
 * @author Eero
 */


public abstract class AbstractJsoupExtractor extends AbstractExtractor {

    private static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
    
    private final String[] contentTypes= new String[] {
        "text/html"
    }; 
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf121);
    }
    
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        if(f.isDirectory()) 
            throw new Exception("Directories are not supported.");
        
        Document d = Jsoup.parse(f,"UTF-8");
        d.setBaseUri(f.getAbsolutePath());
        
        return extractTopicsFrom(d, f.getAbsolutePath(), t);
    }

    
    
    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        Document d = Jsoup.connect(u.toExternalForm()).get();
        return extractTopicsFrom(d, u.toExternalForm(), t);
    }

    
    
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        Document d = Jsoup.parse(str);
        return extractTopicsFrom(d,null,t);
    }
    
    
    
    
    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        if(si == null || si.length() == 0) 
            si = TopicTools.createDefaultLocator().toString();
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, LANG_SI);
    }
    
    public abstract boolean extractTopicsFrom(Document d, String u, TopicMap t) throws Exception;
        
}
