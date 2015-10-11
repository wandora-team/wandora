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
 * PasteBinOccurrenceUploader.java
 *
 * Created on 2011-12-06
 *
 */

package org.wandora.application.tools.occurrences.clipboards;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.*;


/**
 * Uploads occurrence resource to PastBin service and optionally replaces the
 * occurrence resource with PastBin resource URL address.
 *
 * @author akivela
 */


public class PasteBinOccurrenceUploader extends AbstractWandoraTool implements WandoraTool {
    
    private static final String apibase = "http://pastebin.com/api/api_post.php";
    private static final String apikey = "e3dbea933563db6ad9d11e2a5a2ab99c";
    private static PasteBinConfiguration configuration = null;

    private boolean requiresRefresh = false;
    private boolean isConfigured = false;
    private boolean cancelled = false;
    
    private boolean uploadAll = false;

    
    public PasteBinOccurrenceUploader() {
    }
    
    public PasteBinOccurrenceUploader(boolean upAll) {
        this.uploadAll = upAll;
    }
    
    public PasteBinOccurrenceUploader(Context proposedContext) {
        this.setContext(proposedContext);
    }

    @Override
    public String getName() {
        return "Upload occurrence to Pastebin";
    }

    @Override
    public String getDescription() {
        return "Upload occurrence to Pastebin web service.";
    }

    
    
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        requiresRefresh = false;
        isConfigured = false;
        cancelled = false;
        Object source = context.getContextSource();
        String o = null;
        Topic carrier = null;
        Topic type = null;
        Topic lang = null;
        
        try {
            if(source instanceof OccurrenceTable) {
                setDefaultLogger();
                OccurrenceTable ot = (OccurrenceTable) source;
                o = ot.getPointedOccurrence();
                type = ot.getPointedOccurrenceType();
                lang = ot.getPointedOccurrenceLang();
                carrier = ot.getTopic();
                if(o != null) {
                    pasteBin(wandora, carrier, type, lang, o);
                }
            }
            else {
                Iterator contextObjects = context.getContextObjects();
                if(!contextObjects.hasNext()) return;
                
                boolean wasUploaded = false;
                int uploadCounter = 0;
                Topic uploadType = null;
                Topic uploadLang = null;
                if(!uploadAll) {
                    GenericOptionsDialog god=new GenericOptionsDialog(wandora,"Occurrence upload options","Occurrence upload options. If you want to limit occurrences, select occurrence type and scope. Leave both topics empty to upload all occurrences.",true,new String[][]{
                        new String[]{"Occurrence type topic","topic","","Which occurrences are uploaded. Leave blank to include all occurrence types."},
                        new String[]{"Occurrence scope topic","topic","","Which occurrences are uploaded. Leave blank to include all occurrence scopes."},
                    },wandora);
                    god.setVisible(true);
                    if(god.wasCancelled()) return;
                    Map<String,String> values=god.getValues();
                    if(values.get("Occurrence type topic")!=null && values.get("Occurrence type topic").length()>0) {
                        uploadType=wandora.getTopicMap().getTopic(values.get("Occurrence type topic"));
                    }
                    if(values.get("Occurrence scope topic")!=null && values.get("Occurrence scope topic").length()>0) {
                        uploadLang=wandora.getTopicMap().getTopic(values.get("Occurrence scope topic"));
                    }
                }
                setDefaultLogger();
                while(contextObjects.hasNext() && !forceStop() && !cancelled) {
                    Object co = contextObjects.next();
                    if(co != null) {
                        if(co instanceof Topic) {
                            carrier = (Topic) co;
                            Collection<Topic> occurrenceTypes = carrier.getDataTypes();
                            for(Topic otype : occurrenceTypes) {
                                if(forceStop()) break;
                                if(cancelled) break;
                                type = otype;
                                if(uploadType == null || uploadType.mergesWithTopic(type)) {
                                    Hashtable<Topic,String> occurrences = carrier.getData(type);
                                    for(Enumeration<Topic> langs = occurrences.keys() ; langs.hasMoreElements() ; ) {
                                        lang = langs.nextElement();
                                        if(lang != null) {
                                            if(uploadLang == null || lang.mergesWithTopic(uploadLang)) {
                                                o = occurrences.get(lang);
                                                if(o != null) {
                                                    wasUploaded = pasteBin(wandora, carrier, type, lang, o);
                                                    if(wasUploaded) uploadCounter++;
                                                    if(cancelled) break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(uploadCounter > 0) {
                    log("Total "+uploadCounter+" occurrences uploaded to Pastebin.");
                }
                else if(uploadCounter == 0) {
                    log("No occurrences uploaded to Pastebin.");
                }
                log("Ready.");
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }

    
    
    
    
    public boolean pasteBin(Wandora wandora, Topic carrier, Topic type, Topic lang, String o) throws TopicMapException {
        if(!isConfigured) {
            isConfigured = true;
            if(configuration == null) {
                configuration = new PasteBinConfiguration();
            }
            configuration.open(wandora, this);
            if(!configuration.wasAccepted()) {
                cancelled = true;
                return false;
            }
        }

        String apiUserKey = configuration.getUserId();
        String apiPastePrivate = (configuration.getPrivatePaste() ? "1" : "0");
        String apiPasteName = "Wandora-"+System.currentTimeMillis();
        String apiPasteExpireDate = configuration.getExpiration();
        String apiPasteFormat = null;
        String apiDevKey = apikey;
        String reply = pasteBin(apiUserKey, apiPastePrivate, apiPasteName, apiPasteExpireDate, apiPasteFormat, apiDevKey, o);

        if(reply.startsWith("Bad API request")) {
            log("Bad API request: "+reply);
        }
        else {
            log("Occurrence successfully uloaded to Pastebin. Pastebin returned '"+reply+"'.");
            if(configuration.getReplaceOccurrence()) {
                if(carrier != null && type != null && lang != null) {
                    carrier.setData(type, lang, reply);
                    requiresRefresh = true;
                    log("Replaced occurrence with a Pastebin URL '"+reply+"'.");
                }
            }
            return true;
        }
        return false;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public String pasteBin(String apiUserKey, String apiPastePrivate, String apiPasteName, String apiPasteExpireDate, String apiPasteFormat, String apiDevKey, String apiPasteCode) {
        StringBuilder data = new StringBuilder("");
        data.append("api_option=paste");
        if(apiUserKey != null && apiUserKey.length() > 0) data.append("&api_user_key=").append(apiUserKey);
        if(apiPastePrivate != null) data.append("&api_paste_private=").append(apiPastePrivate);
        if(apiPasteName != null) data.append("&api_paste_name=").append(encode(apiPasteName));
        if(apiPasteExpireDate != null) data.append("&api_paste_expire_date=").append(apiPasteExpireDate);
        if(apiPasteFormat != null) data.append("&api_paste_format=").append(apiPasteFormat);
        if(apiDevKey != null) data.append("&api_dev_key=").append(apiDevKey);
        if(apiPasteCode != null) data.append("&api_paste_code=").append(encode(apiPasteCode));
        
        String reply = null;
        try {
            reply = sendRequest(new URL(apibase), data.toString(), "application/x-www-form-urlencoded", "POST");
        }
        catch(Exception e) {
            reply = e.getMessage();
        }
        return reply;
    }
    
    
    
    
    
    private String encode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        }
        catch(Exception e) {
            // PASS
        }
        return str;
    }

    
    
    
    
    public static String sendRequest(URL url, String data, String ctype, String method) throws IOException {
        StringBuilder sb = new StringBuilder(1000);
        if (url != null) {
            URLConnection con = url.openConnection();
            Wandora.initUrlConnection(con);
            con.setDoInput(true);
            con.setUseCaches(false);

            if(method != null && con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).setRequestMethod(method);
                //System.out.println("****** Setting HTTP request method to "+method);
            }

            if(ctype != null) {
                con.setRequestProperty("Content-type", ctype);
            }

            if(data != null && data.length() > 0) {
                con.setRequestProperty("Content-length", data.length() + "");
                con.setDoOutput(true);
                PrintWriter out = new PrintWriter(con.getOutputStream());
                out.print(data);
                out.flush();
                out.close();
            }
//            DataInputStream in = new DataInputStream(con.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String s;
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
        }
        return sb.toString();
    }
    
    
    
    
    
    
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
}
