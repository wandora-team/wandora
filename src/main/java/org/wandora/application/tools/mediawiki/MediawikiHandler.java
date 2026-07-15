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
 */


package org.wandora.application.tools.mediawiki;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wandora.application.Wandora;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.utils.IObox;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author nlaitine
 */

public abstract class MediawikiHandler extends AbstractWandoraTool {

	private static final long serialVersionUID = 1L;

	private static boolean logged = false;
    private HashMap<String, String> cookies = new HashMap<String, String>(12);
    
    private static final String VERSION = "1.0";
    private static final String USER_AGENT = "MediawikiHandler " + VERSION;
    
    //Time to open a connection
    private static final int CONNECTION_CONNECT_TIMEOUT_MSEC = 30000; // 30 seconds
    private static final int CONNECTION_READ_TIMEOUT_MSEC = 180000; // 180 seconds
    
    
    public MediawikiHandler() {};
    
    public boolean callWiki(String apiWikiUrl, URL fileUrl, String filename, String description, boolean stream) {
        boolean success = false;
        
        if(logged) {
            String response = null;
            String uploadResult = null;
            
            if(!stream) {
                uploadResult = uploadToWiki(apiWikiUrl, fileUrl, filename, description);
            } else {
                uploadResult = uploadStreamToWiki(apiWikiUrl, fileUrl, filename, description);
            }
            
            if(uploadResult.contains("result=\"Success\"")) {
                log("Successfully uploaded to Mediawiki.");
                response = getResponseMessage(uploadResult, "upload");
                log("Server response: " + response);
                success = true;
            } else {
                log("Uploading failed.");
                response = getResponseMessage(uploadResult, "upload");
                log("Server response: " + response);
            }
        } else {
            log("Not logged in.");
        }
        
        return success;
    }
    
    private String getResponseMessage(String xml, String caller) {
        
        String reply = null;
        String error = "error";
        String info = "info";
        String node = null;
        String attribute = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        
        if(caller.equals("login")) {
            node = "login";
            attribute = "result";
        } else if(caller.equals("logintoken")) {
            node = "login";
            attribute = "token";
        } else if(caller.equals("edittoken")) {
            node = "tokens";
            attribute = "edittoken";
        } else if (caller.equals("upload")) { 
            node = "upload";
            attribute = "result";
        }
        
        try {
            db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document doc = null;
            
            try {
                doc = db.parse(is);
                Element docRoot = doc.getDocumentElement();
                NodeList errors = docRoot.getElementsByTagName(error);
                NodeList nodes = docRoot.getElementsByTagName(node);
                
                for( int i = 0; i < errors.getLength(); i++ ) {
                    Element element = (Element) errors.item(i);
                    reply = element.getAttributes().getNamedItem(info).getNodeValue();
                }
                
                for( int i = 0; i < nodes.getLength(); i++ ) {
                    Element element = (Element) nodes.item(i);
                    reply = element.getAttributes().getNamedItem(attribute).getNodeValue();
                }
                
            } catch (SAXException e) {
                return "SAXException: Error reading server reply.";
            } catch (IOException e) {
                return "IOException: Error reading server reply.";
            }
        } catch (ParserConfigurationException e) {
            return "ParserConfigurationException: Error reading server reply.";
        }
        
        return reply;
    }
    
    // -------------------------------------------------------------------------
    
    /*
     * Wiki session handling
     */
    
    private String getLoginToken(String apiUrl, String apiUser, String apiPass) {
        StringBuilder data = new StringBuilder("");
        
        String token = null;
        String reply = null;
        String tokenUrl = apiUrl + "/api.php";
        
        apiUser = encode(apiUser);
        apiPass = encode(apiPass);
        
        data.append("action=login");
        data.append("&lgname=").append(apiUser);
        data.append("&lgpassword=").append(apiPass);
        data.append("&format=xml");
        
        //log("Getting Login token...");
        //log(tokenUrl + "?" + data.toString());
        
        try {
            reply = sendRequest(new URL(tokenUrl), data.toString(), "application/x-www-form-urlencoded", "POST");
            token = getResponseMessage(reply, "logintoken");
        } catch (Exception e) {
            log("Login token error: " + e.getMessage());
        }
        
        return token;
    }
    
    private String getEditToken(String apiUrl) {
        String token = null;
        String reply = null;
        String tokenUrl = apiUrl + "/api.php?action=tokens&format=xml";
        
        //log("Getting Edit token...");
        //log(tokenUrl);
        
        try {
            reply = sendRequest(new URL(tokenUrl), null, "application/x-www-form-urlencoded", "POST");
            token = getResponseMessage(reply, "edittoken");
        } catch (Exception e) {
            log("Edit token error: " + e.getMessage());
        }

        return token;
    }
    
    public boolean login(String apiWikiUploadUrl, String apiUserName, String apiPasswd) {       
        if(!logged) {
            StringBuilder data = new StringBuilder("");
            String apiLoginToken = getLoginToken(apiWikiUploadUrl, apiUserName, apiPasswd);

            //Encode parameters
            String apiBase = apiWikiUploadUrl + "/api.php";
            apiUserName = encode(apiUserName);
            apiPasswd = encode(apiPasswd);
            apiLoginToken = encode(apiLoginToken);

            //Login to wiki
            data.append("action=login");
            if(apiUserName != null && apiUserName.length() > 0) data.append("&lgname=").append(apiUserName);
            if(apiPasswd != null && apiPasswd.length() > 0) data.append("&lgpassword=").append(apiPasswd);
            if(apiLoginToken != null && apiLoginToken.length() > 0) data.append("&lgtoken=").append(apiLoginToken);
            data.append("&format=xml");

            log("Logging in...");
            log(apiBase + "?" + data.toString());

            try {
                String reply = sendRequest(new URL(apiBase), data.toString(), "application/x-www-form-urlencoded", "POST");
                if(reply.contains("result=\"Success\"")) {
                    logged = true;
                } else {
                    String error = getResponseMessage(reply, "login");
                    log("Bad login request: " + error);
                }
            }
            catch(Exception e) {
                log("Login error: " + e.getMessage());
            }
        }
        
        return logged;
    }
    
    public boolean logout(String wikiUrl) {
        if(logged) {
            String logoutUrl = wikiUrl + "/api.php?action=logout";

            try {
                sendRequest(new URL(logoutUrl), null, "text/html", "GET");
                cookies.clear();
                logged = false;
                log("Logged out.");
            } catch (Exception e) {
                log(e.getMessage());
                return false;
            }
        }
        
        return !logged;
    }
    
    /*
     * Cookie handling
     */
    
    private boolean setCookies(URLConnection con) {
        StringBuilder cookie = new StringBuilder(100);
        
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            cookie.append(entry.getKey());
            cookie.append("=");
            cookie.append(entry.getValue());
            cookie.append("; ");
        }
        
        con.setRequestProperty("Cookie", cookie.toString());
        con.setRequestProperty("User-Agent", USER_AGENT);
        
        return true;
    }
    
    private boolean grabCookies(URLConnection u) {
        String headerName;
        for (int i = 1; (headerName = u.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                String cookie = u.getHeaderField(i);
                cookie = cookie.substring(0, cookie.indexOf(';'));
                String name = cookie.substring(0, cookie.indexOf('='));
                String value = cookie.substring(cookie.indexOf('=') + 1, cookie.length());
                cookies.put(name, value);
            }
        }
        
        return true;
    }
    
    /*
     * Wiki operations
     */
    
    public String uploadToWiki(String apiWikiUploadUrl, URL apiOccurranceUrl, String apiOccurranceFilename, String apiOccurranceDescription) {
        StringBuilder data = new StringBuilder("");
        String reply = null;
        
        String apiFilename = null;
        String apiFileUrl = null;
        String apiDescription = null;
        String apiEditToken = null;
        
        //Get file Url String
        apiFileUrl = apiOccurranceUrl.toString();
        
        //Get edit token
        apiEditToken = getEditToken(apiWikiUploadUrl);
        
        //Encode parameters
        String apiBaseUrl = apiWikiUploadUrl + "/api.php";
        apiFilename = encode(apiOccurranceFilename);
        apiFileUrl = encode(apiFileUrl);
        apiDescription = encode(apiOccurranceDescription);
        apiEditToken = encode(apiEditToken);
        
        //Upload content to wiki
        data.append("action=upload");
        if(apiFilename != null && apiFilename.length() > 0) data.append("&filename=").append(apiFilename);
        if(apiFileUrl != null && apiFileUrl.length() > 0) data.append("&url=").append(apiFileUrl);
        if(apiDescription != null && apiDescription.length() > 0) data.append("&comment=").append(apiDescription);
        if(apiEditToken != null && apiEditToken.length() > 0) data.append("&token=").append(apiEditToken);
        data.append("&format=xml");
        
        log("Uploading " + apiFilename + "...");
        log(apiBaseUrl + "?" + data.toString());
        
        try {
            reply = sendRequest(new URL(apiBaseUrl), data.toString(), "application/x-www-form-urlencoded", "POST");
        }
        catch(Exception e) {
            log("Uploading error: " + e.getMessage());
        }
                
        return reply;
    }
    
    public String uploadStreamToWiki(String apiWikiUploadUrl, URL apiOccurranceUrl, String apiOccurranceFilename, String apiOccurranceDescription) {
        String reply = null;

        String apiFilename = null;
        String apiDescription = null;
        String apiEditToken = null;
        byte[] apiFile = null;

        String apiBaseUrl = apiWikiUploadUrl + "/api.php";
        
        try {            
            //Get file
            apiFile = getFileBytes(apiOccurranceUrl);
            
            //Get filename
            apiFilename = apiOccurranceFilename;

            //Get description
            apiDescription = apiOccurranceDescription;
            
            //Get edit token
            apiEditToken = getEditToken(apiWikiUploadUrl);
            
            //Set parameters
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("action", "upload");
            params.put("format", "xml");
            params.put("ignorewarnings", "true");
            if(apiFilename != null && apiFilename.length() > 0) params.put("filename", apiFilename);
            if(apiDescription != null && apiDescription.length() > 0) params.put("comment", apiDescription);
            if(apiEditToken != null && apiEditToken.length() > 0) params.put("token", apiEditToken);
            if(apiFile != null) params.put("file\"; filename=\"" + apiFilename, apiFile);
            
            log("Uploading " + apiFilename + "...");
            log(apiBaseUrl + "?action=" + params.get("action") +
                    "&format=" + params.get("format") +
                    "&ignorewarnings=" + params.get("ignorewarnings") +
                    "&filename=" + params.get("filename") +
                    "&description=" + params.get("description") +
                    "&token=" + params.get("token"));
            
            //Set up connection
            URLConnection connection = null;
            connection = new URL(apiBaseUrl).openConnection();
            
            String boundary = "----------NEXT PART----------";
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            setCookies(connection);
            connection.setDoOutput(true);
            connection.setConnectTimeout(CONNECTION_CONNECT_TIMEOUT_MSEC);
            connection.setReadTimeout(CONNECTION_READ_TIMEOUT_MSEC);
            connection.connect();
            boundary = "--" + boundary + "\r\n";
            
            //Write to buffer
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bout);
            out.writeBytes(boundary);
            
            //Write params
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                out.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"\r\n");
                if (value instanceof String) {
                    out.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n\r\n");
                    out.write(((String) value).getBytes("UTF-8"));
                } else if (value instanceof byte[]) {
                    out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                    out.write((byte[]) value);
                } else {
                    throw new UnsupportedOperationException("Unrecognized data type");
                }
                out.writeBytes("\r\n");
                out.writeBytes(boundary);
            }
            
            out.writeBytes("--\r\n");
            out.close();
            // write the buffer to the URLConnection
            OutputStream uout = connection.getOutputStream();
            uout.write(bout.toByteArray());
            uout.close();
            
            //Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            grabCookies(connection);
            String line;
            StringBuilder temp = new StringBuilder(100000);
            while ((line = in.readLine()) != null) {
                temp.append(line);
                temp.append("\n");
            }
            in.close();
            reply = temp.toString();
                       
        } catch (Exception e) {
            log("Uploading Stream error: " + e.getMessage());
        }
                
        return reply;
    }
    
    /*
     * Remote file operations
     */
    
    private byte[] getFileBytes(URL fileUrl) {
        byte[] file = null;
        String scheme = fileUrl.getProtocol();
        String host = fileUrl.getHost();
           
        if ("file".equalsIgnoreCase(scheme) && (host == null || "".equals(host)) ) {            
            try {
                Path path = Paths.get(fileUrl.toURI());
                file = Files.readAllBytes(path);                
            } catch (Exception e) {
                log(e.getMessage());
            }
        } else {
            try {
               file = IObox.fetchUrl(fileUrl);
            } catch (Exception e) {
                log(e.getMessage());
            }
        }
        
        return file;
    }
    
    /*
     * Helper methods
     */
    
    private String encode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        }
        catch(Exception e) {
            // PASS
        }
        return str;
    }
    
    private String sendRequest(URL url, String data, String ctype, String method) throws IOException {
        StringBuilder sb = new StringBuilder(1000);
        if (url != null) {
            URLConnection con = url.openConnection();
            setCookies(con);
            
            Wandora.initUrlConnection(con);
            con.setDoInput(true);
            con.setUseCaches(false);

            if(method != null && con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).setRequestMethod(method);
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
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            grabCookies(con);

            String s;
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
        }
        return sb.toString();
    }
    
    
    
    
    public boolean isValidResourceReference(String str) {
        if(str == null) return false;
        if(str.length() == 0) return false;
        
        try {
            URL u = new URL(str);
            if(u != null) return true;
        }
        catch(Exception e) {}
        try {
            File f = new File(str);
            if(f != null && f.exists()) return true;
        }
        catch(Exception e) {}
        return false;
    }
}
