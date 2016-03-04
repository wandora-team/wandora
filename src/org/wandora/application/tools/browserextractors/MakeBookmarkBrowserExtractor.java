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



import java.io.File;
import java.net.URL;
import org.wandora.topicmap.*;
import org.wandora.application.Wandora;
import java.util.regex.*;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;



/**
 *
 * @author olli
 */
public class MakeBookmarkBrowserExtractor extends AbstractExtractor implements BrowserPluginExtractor {

    public static final String bookmarkSI = "http://wandora.org/si/bookmark";
    
    public MakeBookmarkBrowserExtractor() {
        
    }


    @Override
    public String getName() {
        return "Add bookmark";
    }
    @Override
    public String getDescription() {
        return "Creates a topic for given URL. Feature resembles boomarking. Tools is used mainly as browser extractor.";
    }


    protected Topic extractOneLink(String url,String title,Wandora wandora) throws TopicMapException {
        TopicMap tm = wandora.getTopicMap();
        return extractOneLink(url, title, tm);
    }

    protected Topic extractOneLink(String url, String title, TopicMap tm) throws TopicMapException {
        Topic bookmarkTopic = TMBox.getOrCreateTopic(tm, bookmarkSI, "Bookmark");
        Topic wc = TMBox.getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
        ExtractHelper.makeSubclassOf(bookmarkTopic, wc, tm);
        Topic t = tm.createTopic();
        if(title!=null && title.length()>0) t.setBaseName(title);
        t.setSubjectLocator(tm.createLocator(url));                
        t.addSubjectIdentifier(tm.createLocator(url));
        t.addType(bookmarkTopic);
        return t;
    }




    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        String content=request.getContent();
        String url=request.getSource();
        if(request.getSelection()!=null) {
            String[] links=BrowserExtractorTools.extractLinks(request);
            for(int i=0;i<links.length;i++){
                extractOneLink(links[i],null,wandora);
            }
        }
        else {
            String title=null;
            if(content!=null){
                Pattern p=Pattern.compile("<title>(.*?)</title>",Pattern.DOTALL);
                Matcher m=p.matcher(content);
                if(m.find()) title=m.group(1).trim();
            }
            extractOneLink(url,title,wandora);
        }
        wandora.doRefresh();
        return null;
    }

    @Override
    public boolean acceptBrowserExtractRequest(BrowserExtractRequest request,Wandora wandora) throws TopicMapException {
        if(request.isApplication(BrowserExtractRequest.APP_FIREFOX))
            return true;
        else 
            return false;
    }

    @Override
    public String getBrowserExtractorName() {
        return "Add bookmark";
    }

    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        if(f != null && t != null) {
            String n = f.getAbsolutePath();
            String u = f.toURI().toURL().toExternalForm();
            extractOneLink(u, n, t);
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        if(u != null && t != null) {
            extractOneLink(u.toExternalForm(), u.toExternalForm(), t);
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
