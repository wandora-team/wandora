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



package org.wandora.application.tools.extractors.helmet;



import org.wandora.topicmap.*;
import org.wandora.utils.Tuples.*;
import org.wandora.utils.*;


import java.util.*;
import java.net.*;
import org.json.*;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.extractors.ExtractHelper;

/**
 *
 *
 *
 * <pre>
    {"records": [
        {"type":"book",
        "isbn":"0385410700",
        "title":"Deadline",
        "library_id":"(FI-HELMET)b1141424",
        "library_url":"http://www.helmet.fi/record=b1141424~S9*eng",
        "author":"Armstrong, Campbell",
        "author_details":[],
        "extent":["314, [1] s. ;"],
        "description":[],
        "contents":[]
    },...
 *  </pre>
 *
 * @author akivela
 */
public class HelmetJSONParser {

    private static boolean SPLIT_CONTENTS_TO_TOPICS = true;
    private static boolean TRIM_AUTHORS_ENDING_DOT = true;
    private static boolean REMOVE_ROLE_PARENTHESIS = true;
    private static boolean SPLIT_LANGUAGES_IN_TITLE = true;
    private static boolean REMOVE_ID_PREFIX = true;


    
    private static LinkedHashSet<URL> history = null;

    public static final String GENERAL_SI = "http://data.kirjastot.fi/";

    private TopicMap tm = null;
    private TopicMapLogger logger = null;

    private static int progress = 1;
    private static long waitBetweenURLRequests = 200;
    private static String DEFAULT_LANG = "fi";
    private static String DEFAULT_ENCODING = "UTF-8";
    
    private boolean extractAllPages = false;
    private String extractUrl = null;





    public HelmetJSONParser(TopicMap tm, TopicMapLogger logger, JSONObject inputJSON) {
        this(tm, logger);
        try {
            parse(inputJSON);
        }
        catch(Exception e) {
            log(e);
        }
    }






    public HelmetJSONParser(TopicMap tm, TopicMapLogger logger) {
        this.tm = tm;
        this.logger = logger;

        if(history == null) {
            clearHistory();
        }
    }





    // ------------------------------------------------------------- HISTORY ---



    public void clearHistory() {
        history = new LinkedHashSet<URL>();
    }

    public boolean inHistory(URL u) {
        if(history != null) {
            if(history.contains(u)) return true;
        }
        return false;
    }

    public void addToHistory(URL u) {
        if(history == null) clearHistory();
        history.add(u);
    }




    // -------------------------------------------------------------------------


    public boolean errorDetected(JSONObject inputJSON) {
        try {
            if(inputJSON.has("error")) {
                JSONObject error = inputJSON.getJSONObject("error");
                if(error != null) {
                    log(error.getString("message")+" ("+error.getString("type")+")");
                    return true;
                }
            }
        }
        catch(Exception e) {
            // NOTHING HERE
        }
        return false;
    }


    public void takeNap(long napTime) {
        try {
            Thread.currentThread().sleep(napTime);
        }
        catch(Exception e) {}
    }


    // -------------------------------------------------------------------------




    public void parse(JSONObject inputJSON) throws Exception {
        if(logger.forceStop()) return;
        if(logger != null) {
            logger.setProgress(progress++);
        }
        if(tm == null) {
            log("Warning: Parser has no Topic Map object for topics and associations. Aborting.");
            return;
        }
        if(errorDetected(inputJSON)) return;

        if(inputJSON.has("records")) {
            JSONArray records = inputJSON.getJSONArray("records");
            int s = records.length();
            for( int i=0; i<s; i++ ) {
                JSONObject record = records.getJSONObject(i);
                parseRecord(record);
            }
        }
        if(extractUrl != null) {
            if(inputJSON.has("current_page") && inputJSON.has("per_page") && inputJSON.has("total_entries")) {
                int cp = inputJSON.getInt("current_page");
                int pp = inputJSON.getInt("per_page");
                int te = inputJSON.getInt("total_entries");

                int tp = te / pp + 1;
                if(cp < tp) {
                    boolean extractNextPage = false;
                    if(!extractAllPages) {
                        int a = WandoraOptionPane.showConfirmDialog(Wandora.getWandora(), "Total "+te+" records found. Extracted records "+(cp*pp-pp+1)+"-"+Math.min(cp*pp, te)+". Would you like to continue?", "Continue?", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION);
                        if(a == WandoraOptionPane.YES_OPTION) extractNextPage = true;
                        else if(a == WandoraOptionPane.YES_TO_ALL_OPTION) extractAllPages = true;
                    }
                    if(extractAllPages || extractNextPage) {
                        String newExtractUrl = extractUrl + "&page="+(cp+1);
                        parse(new URL(newExtractUrl));
                    }
                }
            }
        }
    }

    
    public void parseRecord(JSONObject inputJSON) throws Exception {
        if(logger != null) {
            logger.setProgress(progress++);
        }
        if(tm == null) {
            log("Warning: Parser has no Topic Map object for topics and associations. Aborting.");
            return;
        }
        if(errorDetected(inputJSON)) return;
        
        String libraryId = robustJSONGet(inputJSON, "library_id");

        Topic recordT = getRecordTopic(
            robustJSONGet(inputJSON, "library_url"),
            libraryId,
            robustJSONGet(inputJSON, "isbn"),
            tm
        );
        

        if(recordT != null) {
            if(inputJSON.has("type")) {
                String type = inputJSON.getString("type");
                if(isNotNull(type)) {
                    Topic t = getTypeTopic(type, tm);
                    if(t != null) {
                        Association a = tm.createAssociation(getTypeType(tm));
                        a.addPlayer(t, getTypeType(tm));
                        a.addPlayer(recordT, getRecordType(tm));
                    }
                }
            }
            if(inputJSON.has("title")) {
                String title = inputJSON.getString("title");
                if(isNotNull(title)) {
                    if(SPLIT_LANGUAGES_IN_TITLE) {
                        String[] titles = title.split(" = ");
                        if(titles.length == 3) {
                            recordT.setDisplayName("fi", titles[0]);
                            recordT.setDisplayName("sv", titles[1]);
                            recordT.setDisplayName("en", titles[2]);
                        }
                        else if(titles.length == 2) {
                            recordT.setDisplayName("fi", titles[0]);
                            recordT.setDisplayName("sv", titles[1]);
                        }
                        else {
                            recordT.setDisplayName(DEFAULT_LANG, title);
                        }
                    }
                    else {
                        recordT.setDisplayName(DEFAULT_LANG, title);
                    }
                    String basename = title;
                    if(libraryId != null) {
                        if(REMOVE_ID_PREFIX) {
                            if(libraryId.startsWith("(FI-HELMET)")) libraryId = libraryId.substring(11);
                        }
                        basename = basename + " ("+libraryId+")";
                    }
                    else {
                        basename = basename + " (TEMP"+System.currentTimeMillis()+")";
                    }
                    recordT.setBaseName(basename);
                }
            }
            if(inputJSON.has("author")) {
                String author = inputJSON.getString("author");
                if(isNotNull(author)) {
                    if(TRIM_AUTHORS_ENDING_DOT) {
                        if(author.endsWith(".")) author = author.substring(0, author.length()-1);
                    }
                    Topic authorT = getAuthorTopic(author, tm);
                    if(authorT != null) {
                        Association a = tm.createAssociation(getAuthorType(tm));
                        a.addPlayer(recordT, getRecordType(tm));
                        a.addPlayer(authorT, getAuthorType(tm));
                    }
                }
            }
            if(inputJSON.has("author_details")) {
                JSONArray authorDetails = inputJSON.getJSONArray("author_details");
                if(authorDetails != null) {
                    int s = authorDetails.length();
                    for(int i=0; i<s; i++) {
                        JSONObject authorDetail = authorDetails.getJSONObject(i);
                        String role = null;
                        String name = null;
                        if(authorDetail.has("name")) {
                            name = authorDetail.getString("name");
                            if(TRIM_AUTHORS_ENDING_DOT) {
                                if(name.endsWith(".")) name = name.substring(0, name.length()-1);
                            }
                        }
                        if(authorDetail.has("role")) {
                            role = authorDetail.getString("role");
                            if(!isNotNull(role)) {
                                role = "(default-role)";
                            }
                            if(REMOVE_ROLE_PARENTHESIS) {
                                if(role.startsWith("(")) role = role.substring(1);
                                if(role.endsWith(")")) role = role.substring(0, role.length()-1);
                            }
                        }
                        if(role != null && name != null) {
                            Topic roleT = getRoleTopic(role, tm);
                            Topic author2T = getAuthorTopic(name, tm);
                            if(roleT != null && author2T != null) {
                                Association a2 = tm.createAssociation(getAuthorType(tm));
                                a2.addPlayer(recordT, getRecordType(tm));
                                a2.addPlayer(roleT, getAuthorRoleType(tm));
                                a2.addPlayer(author2T, getAuthorType(tm));
                            }
                        }
                    }
                }
            }

            if(inputJSON.has("extent")) {
                JSONArray extentArray = inputJSON.getJSONArray("extent");
                if(extentArray != null) {
                    int s = extentArray.length();
                    for(int i=0; i<s; i++) {
                        String extent = extentArray.getString(i);
                        if(extent != null && extent.length() > 0) {
                            recordT.setData(getExtentType(tm), TMBox.getLangTopic(recordT, DEFAULT_LANG), extent);
                        }
                    }
                }
            }
            if(inputJSON.has("description")) {
                JSONArray descriptionArray = inputJSON.getJSONArray("description");
                if(descriptionArray != null) {
                    int s = descriptionArray.length();
                    for(int i=0; i<s; i++) {
                        String description = descriptionArray.getString(i);
                        if(description != null && description.length() > 0) {
                            recordT.setData(getDescriptionType(tm), TMBox.getLangTopic(recordT, DEFAULT_LANG), description);
                        }
                    }
                }
            }
            if(inputJSON.has("contents")) {
                JSONArray contentsArray = inputJSON.getJSONArray("contents");
                if(contentsArray != null) {
                    int s = contentsArray.length();
                    for(int i=0; i<s; i++) {
                        String contents = contentsArray.getString(i);
                        if(contents != null && contents.length() > 0) {
                            if(SPLIT_CONTENTS_TO_TOPICS) {
                                String[] splittedContents = contents.split(" ; ");
                                for(int j=0; j<splittedContents.length; j++) {
                                    String split = splittedContents[j];
                                    split = split.trim();
                                    if(split.length() > 0) {
                                        Topic ct = getContentTopic(split, tm);
                                        if(ct != null) {
                                            Association a = tm.createAssociation(getContentsType(tm));
                                            a.addPlayer(ct, getContentsType(tm));
                                            a.addPlayer(recordT, getRecordType(tm));
                                        }
                                    }
                                }
                            }
                            else {
                                recordT.setData(getContentsType(tm), TMBox.getLangTopic(recordT, DEFAULT_LANG), contents);
                            }
                        }
                    }
                }
            }
        }
    }



    public void parse(URL url) throws Exception {
        if(extractUrl == null) {
            extractUrl = url.toExternalForm();
        }
        if(waitBetweenURLRequests > 0) {
            takeNap(waitBetweenURLRequests);
        }
        if(url != null && !logger.forceStop()) {
            if(!inHistory(url)) {
                addToHistory(url);
                System.out.println("Parsing URL "+url);
                String in = IObox.doUrl(url);
                JSONObject inputJSON = new JSONObject(in);
                parse(inputJSON);
            }
            else {
                System.out.println("Rejecting already parsed URL "+url);
            }
        }
    }


    public boolean isNotNull(String str) {
        if(str == null || str.length() == 0 || "null".equals(str)) return false;
        return true;
    }



    // -------------------------------------------------------------------------


    public String robustJSONGet(JSONObject json, String key) {
        try {
            if(json.has(key)) {
                return (String) json.get(key);
            }
        }
        catch(Exception e) {
            // NOTHING HERE
        }
        return null;
    }


    public boolean areEqual(String key, Object o) {
        if(key == null || o == null) return false;
        return key.equalsIgnoreCase(o.toString());
    }





    // -------------------------------------------------------------------------
    // -------------------------------------------------------------- TOPICS ---
    // -------------------------------------------------------------------------


    public static final String HELMET_SI = GENERAL_SI + "kirjastot";
    public static final String TYPE_SI = GENERAL_SI + "type";
    public static final String AUTHOR_SI = GENERAL_SI + "author";
    public static final String AUTHORROLE_SI = GENERAL_SI + "author-role";
    public static final String AUTHORDETAILS_SI = GENERAL_SI + "author-details";
    public static final String RECORD_SI = GENERAL_SI + "record";
    public static final String EXTENT_SI = GENERAL_SI + "extent";
    public static final String DESCRIPTION_SI = GENERAL_SI + "description";
    public static final String CONTENTS_SI = GENERAL_SI + "contents";
    public static final String ID_SI = GENERAL_SI + "id";
    public static final String ISBN_SI = GENERAL_SI + "isbn";
    public static final String ROLE_SI = GENERAL_SI + "role";




    
    public Topic getWandoraType( TopicMap tm ) throws Exception {
        return ExtractHelper.getOrCreateTopic(TMBox.WANDORACLASS_SI, tm);
    }

    public Topic getDataKirjastotType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(HELMET_SI, "HelMet", getWandoraType(tm), tm);
    }


    private Topic getTypeType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(TYPE_SI, "Type", getDataKirjastotType(tm), tm);
    }
    private Topic getAuthorType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(AUTHOR_SI, "Author", getDataKirjastotType(tm), tm);
    }
    private Topic getAuthorRoleType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(AUTHORROLE_SI, "Author Role", getDataKirjastotType(tm), tm);
    }
    private Topic getRecordType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(RECORD_SI, "Record", getDataKirjastotType(tm), tm);
    }
    private Topic getExtentType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(EXTENT_SI, "Extent", getDataKirjastotType(tm), tm);
    }
    private Topic getDescriptionType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(DESCRIPTION_SI, "Description", getDataKirjastotType(tm), tm);
    }
    private Topic getContentsType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(CONTENTS_SI, "Contents", getDataKirjastotType(tm), tm);
    }
    private Topic getIDType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(ID_SI, "HelMet ID", getDataKirjastotType(tm), tm);
    }
    private Topic getISBNType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(ISBN_SI, "ISBN", getDataKirjastotType(tm), tm);
    }
    private Topic getRoleType(TopicMap tm) throws Exception {
        return ExtractHelper.getOrCreateTopic(ROLE_SI, "Author Role", getDataKirjastotType(tm), tm);
    }



    private Topic getContentTopic(String c, TopicMap tm) throws Exception {
        return getATopic(c, CONTENTS_SI, getContentsType(tm), tm);
    }
    private Topic getRoleTopic(String r, TopicMap tm) throws Exception {
        return getATopic(r, ROLE_SI, getRoleType(tm), tm);
    }
    public Topic getTypeTopic(String token, TopicMap tm) throws Exception {
        return getATopic(token, TYPE_SI, getTypeType(tm), tm);
    }
    private Topic getAuthorTopic(String name, TopicMap tm) throws Exception {
        return getATopic(name, AUTHOR_SI, getAuthorType(tm), tm);
    }
    private Topic getAuthorTopic(String name, Topic type, TopicMap tm) throws Exception {
        return getATopic(name, AUTHOR_SI, type, tm);
    }

    private Topic getRecordTopic(String url, String id, String isbn, TopicMap tm) throws Exception {
        Topic t = null;
        if(url != null && url.length() > 0) {
            t = tm.getTopic(url);
            if(t == null) t = tm.getTopicBySubjectLocator(url);
            if(t == null) {
                t = tm.createTopic();
                t.addSubjectIdentifier(new Locator(url));
                if(id != null && id.length() > 0) {
                    if(REMOVE_ID_PREFIX) {
                        if(id.startsWith("(FI-HELMET)")) id = id.substring(11);
                    }
                    t.setData(getIDType(tm), TMBox.getLangTopic(t, DEFAULT_LANG), id);
                }
                if(isbn != null && isbn.length() > 0) {
                    t.setData(getISBNType(tm), TMBox.getLangTopic(t, DEFAULT_LANG), isbn);
                }
                Topic recordType = getRecordType(tm);
                t.addType(recordType);
            }
        }
        return t;
    }

    
    
    // --------


    private Topic getATopic(String str, String si, TopicMap tm) throws TopicMapException {
        return getATopic(str, si, null, tm);
    }



    private Topic getATopic(String str, String si, Topic type, TopicMap tm) throws TopicMapException {
        if(str != null && si != null) {
            str = str.trim();
            if(str.length() > 0) {
                Topic topic=ExtractHelper.getOrCreateTopic(si+"/"+urlEncode(str), str, tm);
                if(type != null) topic.addType(type);
                return topic;
            }
        }
        return null;
    }


    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, DEFAULT_ENCODING);
        }
        catch(Exception e) {}
        return str;
    }



    // ------




    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }


    // -------------------------------------------------------------------------

    private int maxLogs = 1000;
    private int logCount = 0;

    private void log(String str) {
        if(logCount<maxLogs) {
            logCount++;
            logger.log(str);
            if(logCount>=maxLogs) {
                logger.log("Silently passing rest logs...");
            }
        }
    }

    private void log(Exception ex) {
        logger.log(ex);
    }


}
