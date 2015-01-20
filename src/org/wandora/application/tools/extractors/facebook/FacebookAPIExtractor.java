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
 */


package org.wandora.application.tools.extractors.facebook;


import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.*;
import org.wandora.utils.*;
import org.wandora.utils.Tuples.*;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

import java.net.*;
import org.json.*;


/**
 *
 * @author akivela
 */
public class FacebookAPIExtractor  extends AbstractExtractor {

    private FacebookExtractorPanel fbep = null;
    private String defaultEncoding = "ISO-8859-1";
    private String accessToken = null;
    private int browseDepth = 1;



    /** Creates a new instance of FacebookAPIExtractor */
    public FacebookAPIExtractor() {
    }



    @Override
    public String getName() {
        return "Facebook Graph extractor";
    }

    @Override
    public String getDescription(){
        return "Converts Facebook Graph feeds to topics maps.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_facebook.png");
    }


    private final String[] contentTypes=new String[] { "text/plain", "text/json", "application/json" };



    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }


    @Override
    public void execute(Wandora application, Context context) {
        fbep = new FacebookExtractorPanel(application);
        fbep.open(application, this, context);

        // WAIT TILL CLOSED

        if(!fbep.wasCancelled()) {
            if(fbep.continueWithoutAuthorization()) {
                browseDepth = 1;
                super.execute(application, context);
            }
            else {
                browseDepth = fbep.getDepth();
                ArrayList<String> urls = fbep.getExtractUrls();
                this.setForceUrls(urls.toArray( new String[] {} ));
                setDefaultLogger();
                super.execute(application, context);
                setState(WAIT);
            }
        }
    }




    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        accessToken = null;
        if(url != null) {
            String query = url.getQuery();
            if(query != null && query.indexOf("access_token=") > -1) {
                String token = query.substring(query.indexOf("access_token=")+13);
                if(token.indexOf("&")>-1) token = token.substring(0, token.indexOf("&"));
                accessToken = token;
            }

            try {
                FacebookGraphParser parser = new FacebookGraphParser(topicMap, this, browseDepth);
                if(accessToken != null) parser.setAccessToken(accessToken);
                parser.clearHistory();
                System.out.println("Facebook url: "+url);
                parser.parse(url);
                log("Facebook graph parser read and processed total "+parser.getHistorySize()+" URLs.");
                return true;
            }
            catch (Exception ex) {
               log(ex);
            }

        }
        return false;
    }


    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        String str = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(str, topicMap);
    }


    @Override
    public boolean _extractTopicsFrom(String in, TopicMap topicMap) throws Exception {
        try {
            FacebookGraphParser parser = new FacebookGraphParser(topicMap, this, browseDepth);
            if(accessToken != null) parser.setAccessToken(accessToken);
            parser.clearHistory();
            parser.parse(new JSONObject(in));
        }
        catch (Exception ex) {
           log(ex);
        }
        return true;
    }




}
