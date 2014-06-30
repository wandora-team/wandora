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
 */




package org.wandora.application.tools.extractors.youtube;

import org.wandora.application.tools.extractors.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.utils.*;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

import com.google.gdata.client.youtube.*;
import com.google.gdata.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;

/**
 *
 * @author olli
 */
public class YouTubeURLExtractor extends AbstractExtractor {

    private YouTubeVideoFeedExtractor parentExtractor;

    public YouTubeURLExtractor(){
        parentExtractor=new YouTubeVideoFeedExtractor();
    }

    @Override
    public String getName() {
        return "YouTube video URL extractor...";
    }
    @Override
    public String getDescription(){
        return "Convert YouTube Video to a Topic Map";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_youtube.png");
    }

    @Override
    public boolean useURLCrawler() {
        return false;
    }

    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        YouTubeExtractorConfiguration dialog=new YouTubeExtractorConfiguration(admin,options,parentExtractor);
        dialog.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
    }

    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        Pattern p = Pattern.compile("http\\:\\/\\/www\\.youtube\\.com\\/watch\\?v\\=[^\" \\n]+?");
        Matcher m = p.matcher(str);
        ArrayList<String> youtubeUrls = new ArrayList<String>();
        int l = 0;
        while( l<str.length() && m.find(l) ) {
            String g = m.group();
            g = g.trim();
            if(g.startsWith("\"")) g = g.substring(1);
            if(g.endsWith("\"")) g = g.substring(0, g.length()-1);
            youtubeUrls.add( g );
            l = m.end();
        }

        for( String u : youtubeUrls ) {
            System.out.println("Extracting youtube url: " + u);
            _extractTopicsFrom(new URL(u), tm);
        }
        return true;
    }


    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        throw new UnsupportedOperationException("Youtube Extraction not supported in files.");
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        YouTubeVideoFeedSelector feedSelector = null;
        int counter=0;
        try {
            YouTubeService service = parentExtractor.initializeService(getWandora());
            feedSelector = new YouTubeVideoFeedSelector(getWandora(), this);
            setDefaultLogger();
            counter+=feedSelector.processVideoFeedURL(getWandora(), tm, service, parentExtractor, u.toExternalForm());
        }
        catch(AuthenticationException ae) {
            singleLog("Invalid username or password.");
            parentExtractor.forgetAuthorization();
            setState(WAIT);
        }
        catch(Exception e) {
            singleLog(e);
            setState(WAIT);
        }
        if(counter>0) return true;
        else return false;
    }

    @Override
    public int getExtractorType() {
        return URL_EXTRACTOR;
    }


    // ---------------------------------------------------- PLUGIN EXTRACTOR ---




    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        try {
            setWandora(wandora);
            String url = request.getSource();
            TopicMap tm = wandora.getTopicMap();
            if(url.startsWith("http://www.youtube.com/watch?v")) {
                _extractTopicsFrom(new URL(url), tm);
                wandora.doRefresh();
                return null;
            }
            else {
                String content = ExtractHelper.getContent(request);
                if(content != null) {
                    System.out.println("--- browser plugin processing content ---");
                    System.out.println(content);
                    _extractTopicsFrom(content, tm);
                    wandora.doRefresh();
                    return null;
                }
                else {
                    return BrowserPluginExtractor.RETURN_ERROR+"Couldn't solve browser extractor content. Nothing extracted.";
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }
    }




    // -------------------------------------------------------------------------




}
