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
 */


package org.wandora.utils.language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.GenericOptionsDialog;
import org.wandora.topicmap.Topic;
import org.wandora.utils.Base64;
import org.wandora.utils.Tuples;
import org.wandora.utils.Tuples.T3;

/**
 *
 * @author akivela
 */


public class WatsonTranslateBox {
    

    
    public static String translate(String text, Topic sourceLangTopic, Topic targetLangTopic, boolean markTranslation) {
        try {
            String sourceLangStr = LanguageBox.getCodeForLangTopic(sourceLangTopic);
            String targetLangStr = LanguageBox.getCodeForLangTopic(targetLangTopic);

            return translate(text, sourceLangStr, targetLangStr, markTranslation);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String translate(String str, String sourceLang, String targetLang) {
        return translate(str, sourceLang, targetLang, false);
    }
    
    public static String translate(String str, String sourceLang, String targetLang, boolean markTranslation) {
        return translate(str, getLanguagesCodeFor(sourceLang, targetLang), markTranslation);
    }
    
    public static String translate(String str, String sid, boolean markTranslation) {
        if(str != null) {
            if(sid != null) {
                Tuples.T3<String,String,String> connectionOptions = getServiceConnectionOptions();
                if(connectionOptions != null) {
                    String url = connectionOptions.e1;
                    String username = connectionOptions.e2;
                    String password = connectionOptions.e3;
                    try {
                        //System.out.println("WATSON SID:"+sid);
                        //System.out.println("WATSON TEXT:"+str);
                        
                        String requestData = "sid="+sid+"&rt=text&txt="+URLEncoder.encode(str, "utf-8");
                        String response = doUrl(new URL(url), username, password, requestData, "application/x-www-form-urlencoded", "POST");
                        //System.out.println("WATSON RESPONSE:"+response);
                        if(response != null && markTranslation) {
                            response = response + " [WATSON TRANSLATION]";
                        }
                        return response;

                    }
                    catch(Exception e) {
                        if(Wandora.getWandora() != null) {
                            Wandora.getWandora().handleError(e);
                        }
                        else {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Given connection settings are invalid. Can't use translation service of Watson.", "Illegal connection settings", WandoraOptionPane.WARNING_MESSAGE);
                }
            }
            else {
                // WandoraOptionPane.showMessageDialog(Wandora.getWandora(), "Wandora's Watson translation doesn't support given languages", "Unsupported languages", WandoraOptionPane.WARNING_MESSAGE);
            }
        }
        return null;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    
    public static final String[] translationCodeMapping = {
        "mt-enus-eses", "en", "es", "English to Spanish",
        "mt-eses-enus", "es", "en", "Spanish to English",
        "mt-enus-frfr", "en", "fr", "English to French",
        "mt-frfr-enus", "fr", "en", "French to English",
        "mt-enus-ptbr", "en", "pt", "English to Portuguese (Brazil)",
        "mt-ptbr-enus", "pt", "en", "Portuguese (Brazil) to English",
        "mt-arar-enus", "ar", "en", "Arabic to English"
    };
    
    
    public static String getLanguagesCodeFor(String sourceLang, String targetLang) {
        String translationCode = null;
        if(sourceLang != null && targetLang != null) {
            for(int i=0; i<translationCodeMapping.length; i=i+4) {
                if(sourceLang.equalsIgnoreCase(translationCodeMapping[i+1]) && targetLang.equalsIgnoreCase(translationCodeMapping[i+2])) {
                    translationCode = translationCodeMapping[i];
                    break;
                }
            }
        }
        return translationCode;
    }
    
    
    public static String getLanguagesCodeFor(String sourceTargetLanguages) {
        String translationCode = null;
        if(sourceTargetLanguages != null) {
            for(int i=0; i<translationCodeMapping.length; i=i+4) {
                if(sourceTargetLanguages.equalsIgnoreCase(translationCodeMapping[i+3])) {
                    translationCode = translationCodeMapping[i];
                    break;
                }
            }
        }
        return translationCode;
    }
    
    
    public static String[] getAvailableTranslations() {
        HashSet<String> sources = new LinkedHashSet();
        for(int i=0; i<translationCodeMapping.length; i=i+4) {
            sources.add(translationCodeMapping[i+3]);
        }
        return sources.toArray( new String[] {} );
    }
    

    
    public static String getSourceLanguageCodeFor(String languages) {
        if(languages != null) {
            for(int i=0; i<translationCodeMapping.length; i=i+4) {
                if(languages.equalsIgnoreCase(translationCodeMapping[i+3])) {
                    return translationCodeMapping[i+1];
                }
            }
        }
        return null;
    }
    
    
    public static String getTargetLanguageCodeFor(String languages) {
        if(languages != null) {
            for(int i=0; i<translationCodeMapping.length; i=i+4) {
                if(languages.equalsIgnoreCase(translationCodeMapping[i+3])) {
                    return translationCodeMapping[i+2];
                }
            }
        }
        return null;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    private static String doUrl(URL url, String authUser, String authPassword, String data, String ctype, String method) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        try {
            if(url != null) {
                String userPassword = authUser + ":" + authPassword;
                String encodedUserPassword = Base64.encodeBytes(userPassword.getBytes());
                URLConnection con = (HttpURLConnection) url.openConnection();
                Wandora.initUrlConnection(con);
                con.setRequestProperty ("Authorization", "Basic " + encodedUserPassword);

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
                //DataInputStream in = new DataInputStream(con.getInputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                }
                in.close();
            }
        }
        catch(IOException ioe) {
            Wandora.getWandora().handleError(ioe);
        }
        catch(Exception e) {
            Wandora.getWandora().handleError(e);
        }
        return sb.toString();
    }

    
    
    
    private static boolean rememberConnectionOptions = true;
    private static String connectionUrl = "";
    private static String connectionUsername = "";
    private static String connectionPassword = "";
    
    
    
    private static T3<String,String,String> getServiceConnectionOptions() {
        Wandora wandora = Wandora.getWandora();
        String url = null;
        String username = null;
        String password = null;
    
        GenericOptionsDialog god = new GenericOptionsDialog(
                wandora,
                "Watson connection options",
                "Watson connection url, username and password. You must register the IBM Bluemix service to get required credentials. Read more at https://ace.ng.bluemix.net/ .",
                true,new String[][]{
                    new String[]{"Connection url","string",connectionUrl,"Connection url specifies the translation API endpoint at Bluemix."},
                    new String[]{"Username","string",connectionUsername, "Valid username for Watson API service at Bluemix."},
                    new String[]{"Password","string",connectionPassword, "Valid password for Watson API service at Bluemix."},
                    new String[]{"Remember options","boolean",Boolean.toString(rememberConnectionOptions),"If checked, Wandora remembers these connection options during this use session. Wandora forgets connection settings always at startup."},
                },
                wandora);
        god.setVisible(true);
        if(god.wasCancelled()) return null;
        
        Map<String,String> values=god.getValues();
        url = values.get("Connection url");
        username = values.get("Username");
        password = values.get("Password");

        if(values.get("Remember options")!=null && values.get("Remember options").length()>0) {
            try {
                rememberConnectionOptions = Boolean.parseBoolean(values.get("Remember options"));
            }
            catch(Exception e) {}
        }
        if(rememberConnectionOptions) {
            connectionUrl = url;
            connectionUsername = username;
            connectionPassword = password;
        }
        else {
            connectionUrl = "";
            connectionUsername = "";
            connectionPassword = "";
        }
        if(validConnectionOptionValue(url) && validConnectionOptionValue(username) && validConnectionOptionValue(password)) {
            return new T3(url, username, password);
        }
        else return null;
    }
    
    
    
    private static boolean validConnectionOptionValue(String val) {
        if(val == null) return false;
        if(val.length() == 0) return false;
        return true;
    }
    
    
    
    public static void resetServiceConnectionOptions() {
        connectionUrl = "";
        connectionUsername = "";
        connectionPassword = "";
    }
}
