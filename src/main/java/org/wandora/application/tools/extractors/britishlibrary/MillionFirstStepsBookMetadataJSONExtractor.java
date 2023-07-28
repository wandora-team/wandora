/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 */


package org.wandora.application.tools.extractors.britishlibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.utils.IObox;

/**
 *
 * @author akivela
 */


public class MillionFirstStepsBookMetadataJSONExtractor extends AbstractMillionFirstStepsExtractor {
    

	private static final long serialVersionUID = 1L;




	@Override
    public String getName() {
        return "BL's million first steps book JSON extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extracts topic map from the British Library's a million first steps book metadata https://github.com/BL-Labs/imagedirectory";
    }


    
    // -------------------------------------------------------------------------
    
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        String in = IObox.loadFile(f);
        JSONObject json = new JSONObject(in);
        parse(json, tm);
        return true;
    }

    
    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        try {
            String in = doUrl(u);
            JSONObject json = new JSONObject(in);
            parse(json, tm);
   
        } 
        catch (Exception e){
           e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        JSONObject json = new JSONObject(str);
        parse(json, tm);
        return true;
    }
    
    
    
    public String doUrl (URL url) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        
        if (url != null) {
            URLConnection con = url.openConnection();
            Wandora.initUrlConnection(con);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-type", "text/plain");
                
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName(defaultEncoding)));

                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    if(!(s.endsWith("\n") || s.endsWith("\r"))) sb.append("\n");
                }
                in.close();
            } 
            catch (Exception ex) {
                log(ex);
            }
        }
        
        return sb.toString();
    }
    
    
    
    
 
    
    
    // ------------------------------- PARSING ---------------------------------
    
    
    public void parse(JSONObject json, TopicMap tm) throws TopicMapException {
        Iterator keys = json.keys();
        int i=0;
        while(keys.hasNext() && !forceStop()) {
            try {
                String key = (String) keys.next();
                JSONObject bookData = json.getJSONObject(key);
                parseBookData(key, bookData, tm);
                hlog("Parsing "+key+" ("+i+")");
                setProgress(++i);
            }
            catch(Exception ex) {
                log(ex);
            }
        }
    }

    public void parseBookData(String key, JSONObject bookData, TopicMap tm) {
        if(key != null && bookData != null && tm != null) {
            if(bookData.has("identifier")) {
                try {
                    String identifier = bookData.getString("identifier");
                    Topic bookTopic = getBookTopic(identifier, tm);

                    if(bookData.has("title")) {
                        parseN(bookTopic, bookData.get("title"), identifier, tm);
                        parseO(bookTopic, bookData.get("title"), TITLE_SI, "Title (British Library)", tm);
                    }
                    if(bookData.has("authors")) {
                        parseA(bookTopic, bookData.get("authors"), AUTHOR_SI, "Author (British Library)", tm);
                    }
                    if(bookData.has("corporate")) {
                        parseA(bookTopic, bookData.get("corporate"), CORPORATE_SI, "Corporate (British Library)", tm);
                    }
                    if(bookData.has("place")) {
                        parseA(bookTopic, bookData.get("place"), PLACE_SI, "Place (British Library)", tm);
                    }
                    if(bookData.has("datefield")) {
                        parseO(bookTopic, bookData.get("datefield"), DATEFIELD_SI, "Datefield (British Library)", tm);
                    }
                    if(bookData.has("date")) {
                        String date = bookData.getString("date");
                        if(date != null && date.length() > 0) {
                            Topic dateTopic = getDateTopic(date, tm);
                            Topic dateTypeTopic = getDateTypeTopic(tm);
                            
                            Association a = tm.createAssociation(dateTypeTopic);
                            a.addPlayer(dateTopic, dateTypeTopic);
                            a.addPlayer(bookTopic, getBookTypeTopic(tm));
                        }
                    }
                    if(bookData.has("issuance")) {
                        String issuance = bookData.getString("issuance");
                        if(issuance != null && issuance.length() > 0) {
                            Topic issuanceTopic = getIssuanceTopic(issuance, tm);
                            Topic issuanceTypeTopic = getIssuanceTypeTopic(tm);
                            
                            Association a = tm.createAssociation(issuanceTypeTopic);
                            a.addPlayer(issuanceTopic, issuanceTypeTopic);
                            a.addPlayer(bookTopic, getBookTypeTopic(tm));
                        }
                    }
                    if(bookData.has("publisher")) {
                        parseA(bookTopic, bookData.get("publisher"), PUBLISHER_SI, "Publisher (British Library)", tm);
                    }
                    if(bookData.has("edition")) {
                        String edition = bookData.getString("edition");
                        if(edition != null && edition.length() > 0) {
                            Topic editionTopic = getEditionTopic(edition, tm);
                            Topic editionTypeTopic = getEditionTypeTopic(tm);
                            
                            Association a = tm.createAssociation(editionTypeTopic);
                            a.addPlayer(editionTopic, editionTypeTopic);
                            a.addPlayer(bookTopic, getBookTypeTopic(tm));
                        }
                    }
                    if(bookData.has("shelfmarks")) {
                        parseO(bookTopic, bookData.get("shelfmarks"), SHELFMARK_SI, "Shelfmark (British Library)", tm);
                    }
                    if(bookData.has("flickr_url_to_book_images")) {
                        parseA(bookTopic, bookData.get("flickr_url_to_book_images"), IMAGE_SI, "Image (British Library)", tm);
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }
    }
    
    
    


    
    public Association parseA(Topic bookTopic, Object data, String typesi, String type, TopicMap tm) {
        if(data != null) {
            if(data instanceof String) {
                String dataString = (String) data;
                if(dataString.length() > 0) {
                    try {
                        Topic topic = getATopic(dataString, typesi, type, tm);
                        Topic typeTopic = getATypeTopic(typesi, type, tm);

                        Association a = tm.createAssociation(typeTopic);
                        a.addPlayer(topic, typeTopic);
                        a.addPlayer(bookTopic, getBookTypeTopic(tm));
                        return a;
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
            else if(data instanceof JSONArray) {
                JSONArray dataArray = (JSONArray) data;
                for(int i=0; i<dataArray.length(); i++) {
                    try {
                        Association a = parseA(bookTopic, dataArray.get(i), typesi, type, tm);
                        if(a != null && dataArray.length() > 1) {
                            Topic orderTopic = getOrderTopic(""+i, tm);
                            Topic orderType = getOrderTypeTopic(tm);
                            a.addPlayer(orderTopic, orderType);
                        }
                        return a;
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
            else if(data instanceof JSONObject) {
                JSONObject dataObject = (JSONObject) data;
                Iterator keys = dataObject.keys();
                
                while( keys.hasNext() ) {
                    try {
                        String key = (String) keys.next();
                        Object innerData = dataObject.get(key);
                        if(innerData != null) {
                            Association a = parseA(bookTopic, innerData, typesi, type, tm);
                            if(a != null) {
                                a.addPlayer(getRoleTopic(key, tm), getRoleTypeTopic(tm));
                            }
                            return a;
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
        }
        return null;
    }
    

    
    
    
    public void parseO(Topic bookTopic, Object data, String typesi, String type, TopicMap tm) {
        if(data != null) {
            if(data instanceof String) {
                String dataString = (String) data;
                if(dataString.length() > 0) {
                    try {
                        Topic typeTopic = getATypeTopic(typesi, type, tm);

                        String oldOccurrence = bookTopic.getData(typeTopic, getLangTopic(tm));
                        if(oldOccurrence != null) {
                            dataString = oldOccurrence+"\n\n"+dataString;
                        }
                        bookTopic.setData(typeTopic, getLangTopic(tm), dataString);
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
            else if(data instanceof JSONArray) {
                JSONArray dataArray = (JSONArray) data;
                for(int i=0; i<dataArray.length(); i++) {
                    try {
                        parseO(bookTopic, dataArray.get(i), typesi, type, tm);
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
            else if(data instanceof JSONObject) {
                JSONObject dataObject = (JSONObject) data;
                Iterator keys = dataObject.keys();
                
                while( keys.hasNext() ) {
                    try {
                        String key = (String) keys.next();
                        Object innerData = dataObject.get(key);
                        if(innerData != null) {
                            parseO(bookTopic, innerData, typesi, type, tm);
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
        }
    }
    
    
    
    
    public void parseN(Topic bookTopic, Object data, String id, TopicMap tm) {
        if(data != null) {
            if(data instanceof String) {
                String dataString = (String) data;
                if(dataString.length() > 0) {
                    try {
                        bookTopic.setBaseName(dataString+" ("+id+")");
                        bookTopic.setDisplayName(defaultLang, dataString);
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
            else if(data instanceof JSONArray) {
                JSONArray dataArray = (JSONArray) data;
                for(int i=0; i<dataArray.length(); i++) {
                    try {
                        parseN(bookTopic, dataArray.get(i), id, tm);
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
            else if(data instanceof JSONObject) {
                JSONObject dataObject = (JSONObject) data;
                Iterator keys = dataObject.keys();
                
                while( keys.hasNext() ) {
                    try {
                        String key = (String) keys.next();
                        Object innerData = dataObject.get(key);
                        if(innerData != null) {
                            parseN(bookTopic, innerData, id, tm);
                        }
                    }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
        }
    }
    

}
