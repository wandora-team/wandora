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
 */


package org.wandora.application.tools.extractors.bing;

import com.mashape.unirest.http.Unirest;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import org.xml.sax.*;

import org.wandora.topicmap.*;




/**
 *
 * @author akivela
 */
public class BingSearchExtractor extends BingSearchResultExtractor {




	private static final long serialVersionUID = 1L;

	@Override
    public String getName() {
        return "Bing search extractor";
    }

    @Override
    public String getDescription(){
        return "Performs a Microsoft Bing search and converts resulting XML feed to a topic map.";
    }

    @Override
    public boolean _extractTopicsFrom(String data, TopicMap topicMap) throws Exception {
        HashMap<String,String> auth = solveAuth(getWandora());
        if(data != null && data.length() > 0) {
            if(auth != null && auth.containsKey("Username") 
            && auth.containsKey("Account key")) {
                
                String content = "'" + data + "'";
                String uname = urlEncode(auth.get("Username"));
                String key   = urlEncode(auth.get("Account key"));
                
                String bingURL = "https://" + uname + ":" + key + "@" + BING_URL
                               + "?Version=2.2"
                               + "&Query=" + URLEncoder.encode(content, "utf-8")
                               + "&Sources=web&web.count=20&xmltype=attributebased";
                //String result = IObox.doUrl(new URL(bingURL));
                
                String result = Unirest.get(bingURL).asString().getBody();

                System.out.println("Bing returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                BingParser parserHandler = new BingParser(getMasterSubject(), content, topicMap,this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try {
                    reader.parse(new InputSource(new StringReader(result)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                log("Bing search extraction finished!");
            }
            else {
                log("No valid API key given! Aborting!");
            }
        }
        else {
            log("No valid data given! Aborting!");
        }
        return true;
    }


}
