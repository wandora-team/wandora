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
package org.wandora.application.modulesserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.AbstractAction;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.ModulesServlet;
import org.wandora.modules.usercontrol.User;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.memory.TopicMapImpl;
import org.wandora.utils.IObox;


/**
 * <p>
 * WaianaService implements an API used to store and retrieve topic maps via
 * JSON fragments submitted over HTTP. The service stores topic maps as XTM 
 * files into the directory of service. The API mimics Maiana API
 * (http://projects.topicmapslab.de/projects/maiana/wiki/API_Controller)
 * created in Topic Maps Labs (http://www.topicmapslab.de/).
 * </p>
 * <p>
 * Wandora features separate import and export tools that use original Maiana API,
 * that can be used to import and export topic maps into Waiana too. These import
 * and export tools locate in the <b>org.wandora.application.tools.maiana</b> package.
 * </p>
 * 
 * @author akivela
 */


public class WaianaService extends AbstractTopicWebApp {

    
    private WaianaStorage storage = null;
    private boolean allowURLParameterUsage = true;

    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        /*
         The default actionParamKey is "action" which conflicts with operation of
         this action. The actionParamKey can still be set in module initialisation
         parameters if needed. For usual bundle operations this action will be made
         the default action so action param is not needed.
        */
        actionParamKey=null;

        storage = new WaianaStorage();
        
        try {
            Object basePath = settings.get("basePath");
            if(basePath != null) storage.setBasePath(basePath.toString().trim());
            
            Object topicMapsPath = settings.get("topicMapsPath");
            if(topicMapsPath != null) storage.setTopicMapsPath(topicMapsPath.toString().trim());

            Object storageFilename = settings.get("storageFilename");
            if(storageFilename != null) storage.setStorageFilename(storageFilename.toString().trim());
        
            Object skipRightsManagement = settings.get("skipRightsManagement");
            if(skipRightsManagement != null) storage.setSkipRightsManagement(Boolean.parseBoolean(skipRightsManagement.toString().trim()));
            
            Object blockAllWrites = settings.get("blockAllWrites");
            if(blockAllWrites != null) storage.setBlockAllWrites(Boolean.parseBoolean(blockAllWrites.toString().trim()));
            
            Object saveAlwaysAfterChange = settings.get("saveAlwaysAfterChange");
            if(saveAlwaysAfterChange != null) storage.setSaveAlwaysAfterChange(Boolean.parseBoolean(saveAlwaysAfterChange.toString().trim()));
            
            Object keepDeletedFiles = settings.get("keepDeletedFiles");
            if(keepDeletedFiles != null) storage.setKeepDeletedFiles(Boolean.parseBoolean(keepDeletedFiles.toString().trim()));
        }
        catch(Exception e) {};
        
        storage.loadData();
        
        super.init(manager, settings);

    }

    
    
    @Override
    public void stop(ModuleManager manager) {
        storage.saveData();
        
        super.stop(manager); 
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        super.start(manager);
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }

    
    
    
    
    @Override
    public boolean handleAction(HttpServletRequest request, HttpServletResponse response, ModulesServlet.HttpMethod _method, String _action, User user) throws ServletException, IOException, ActionException {
        boolean success = true;
        JSONObject responseJSON = new JSONObject();

        try {
            if(storage != null) {
                JSONObject requestJSON = getRequestJSON(request);
                if(requestJSON != null) {

                    String api_key = WaianaAPIRequestUtils.getAPIKey(requestJSON);
                    String command = WaianaAPIRequestUtils.getCommand(requestJSON);

                    if("show_local_file_list".equalsIgnoreCase(command) || "show_topic_map_list".equalsIgnoreCase(command) || "show_local_topic_map_list".equalsIgnoreCase(command)) {
                        responseJSON = storage.getIndex();
                    }
                    else if("download_topic_map".equalsIgnoreCase(command) || "download_local_file".equalsIgnoreCase(command)) {
                        responseJSON = storage.getTopicMap(user, WaianaAPIRequestUtils.getShortName(requestJSON));

                    }
                    else if("create_local_file".equalsIgnoreCase(command) || "create_topic_map".equalsIgnoreCase(command) || "update_local_file".equalsIgnoreCase(command) || "update_topic_map".equalsIgnoreCase(command)) {
                        String data = WaianaAPIRequestUtils.getData(requestJSON);
                        String shortName = WaianaAPIRequestUtils.getShortName(requestJSON);
                        String name = WaianaAPIRequestUtils.getName(requestJSON);
                        boolean isPublic = WaianaAPIRequestUtils.getIsPublic(requestJSON);
                        boolean isDownloadable = WaianaAPIRequestUtils.getIsDownloadable(requestJSON);
                        boolean isEditable = WaianaAPIRequestUtils.getIsEditable(requestJSON);
                        boolean isSchema = WaianaAPIRequestUtils.getIsSchema(requestJSON);

                        responseJSON = storage.putTopicMap(user, name, shortName, isPublic, isDownloadable, isEditable, isSchema, data, request.getRequestURL());
                    }
                    else if("delete_local_file".equalsIgnoreCase(command) || "delete_topic_map".equalsIgnoreCase(command)) {
                        responseJSON = storage.deleteTopicMap(user, WaianaAPIRequestUtils.getShortName(requestJSON));
                    }
                    else {
                        responseJSON = createReply(1, "Illegal command value '"+command+"' given in request JSON.");
                    }
                }

                // The request has no JSON data or the JSON has no command.
                else if(allowURLParameterUsage) {
                    String apiKey = request.getParameter("apiKey");
                    String command = request.getParameter("command");
                    if(command != null && command.length() > 0) {
                        if("show_local_file_list".equalsIgnoreCase(command) || "show_topic_map_list".equalsIgnoreCase(command) || "show_local_topic_map_list".equalsIgnoreCase(command)) {
                            responseJSON = storage.getIndex();
                        }
                        else if("download_topic_map".equalsIgnoreCase(command) || "download_local_file".equalsIgnoreCase(command)) {
                            String shortName = request.getParameter("shortName");
                            responseJSON = storage.getTopicMap(user, shortName);
                        }
                        else if("stream".equalsIgnoreCase(command)) {
                            String shortName = request.getParameter("shortName");
                            String format = request.getParameter("format");
                            return storage.streamTopicMap(response, user, shortName, format);
                        }
                        else if("create_local_file".equalsIgnoreCase(command) || "create_topic_map".equalsIgnoreCase(command) || "update_local_file".equalsIgnoreCase(command) || "update_topic_map".equalsIgnoreCase(command)) {
                            String data = request.getParameter("data");
                            String shortName = request.getParameter("shortName");
                            String name = request.getParameter("name");
                            boolean isPublic = false;
                            if(request.getParameter("isPublic") != null) {
                                isPublic = Boolean.parseBoolean(request.getParameter("isPublic"));
                            }
                            boolean isDownloadable = false;
                            if(request.getParameter("isDownloadable") != null) {
                                isDownloadable = Boolean.parseBoolean(request.getParameter("isDownloadable"));
                            }
                            boolean isEditable = false;
                            if(request.getParameter("isEditable") != null) {
                                isEditable = Boolean.parseBoolean(request.getParameter("isEditable"));
                            }
                            boolean isSchema = false;
                            if(request.getParameter("isSchema") != null) {
                                Boolean.parseBoolean(request.getParameter("isSchema"));
                            }
                            responseJSON = storage.putTopicMap(user, name, shortName, isPublic, isDownloadable, isEditable, isSchema, data, request.getRequestURL());
                        }
                        else if("delete_local_file".equalsIgnoreCase(command) || "delete_topic_map".equalsIgnoreCase(command)) {
                            String shortName = request.getParameter("shortName");
                            responseJSON = storage.deleteTopicMap(user, shortName);
                        }
                        else {
                            responseJSON = createReply(1, "Illegal command value '"+command+"' given in URL parameter.");
                        }
                    }
                    else {
                        responseJSON = createReply(1, "Invalid command parameter value '"+command+"' found.");
                    }
                }
                else {
                    responseJSON = createReply(1, "Can't find required parameters in the request.");
                }
            }
        }
        catch(Exception e) {
            responseJSON = createReply(1, "Exception '"+e.toString()+"' occurred while processing the maiana api request.");
            e.printStackTrace();
        }
        if(responseJSON == null || !responseJSON.has("code")) {
            responseJSON = createReply(1, "The response JSON has not been set successfully.");
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(),"UTF-8"));
        writer.print(responseJSON.toString());
        writer.flush();
        writer.close();
        
        return success;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    
    protected JSONObject getRequestJSON(HttpServletRequest request) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        
        String str;
        while ((str = in.readLine()) != null) {
            sb.append(str);
        }
        in.close();
        
        // System.out.println("FOUND:\n"+sb.toString());
        
        if(sb.length() > 0) {
            try {
                JSONObject requestJSON = new JSONObject(sb.toString());
                // System.out.println("requestJSON:" +requestJSON.toString());
                return requestJSON;
            } 
            catch (JSONException ex) {
                // This exception is not logged because the user may user URL 
                // parameters instead of JSON data.
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    protected static class WaianaAPIRequestUtils {
        
        public static String getAPIKey(JSONObject json) {
            return getStringSafely(json, "api_key");
        }
        
        public static String getData(JSONObject json) {
            return getStringSafely(json, "data");
        }
        
        public static JSONObject getParameters(JSONObject json) {
            try {
                return json.getJSONObject("parameters");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        
        public static String getCommand(JSONObject json) {
            JSONObject parameters = getParameters(json);
            return getStringSafely(parameters, "command");
        }
        
        public static String getEndpointType(JSONObject json) {
            JSONObject parameters = getParameters(json);
            return getStringSafely(parameters, "endpoint_type");
        }
        
        public static String getShortName(JSONObject json) {
            JSONObject parameters = getParameters(json);
            return getStringSafely(parameters, "short_name");
        }
        
        public static String getName(JSONObject json) {
            JSONObject parameters = getParameters(json);
            return getStringSafely(parameters, "name");
        }
        
        public static boolean getIsPublic(JSONObject json) {
            JSONObject parameters = getParameters(json);
            return getBooleanSafely(parameters, "is_public", false);
        }
        
        public static boolean getIsDownloadable(JSONObject json) {
            JSONObject parameters = getParameters(json);
            return getBooleanSafely(parameters, "is_downloadable", false);
        }
        
        public static boolean getIsEditable(JSONObject json) {
            JSONObject parameters = getParameters(json);
            return getBooleanSafely(parameters, "is_editable", false);
        }
        
        public static boolean getIsSchema(JSONObject json) {
            JSONObject parameters = getParameters(json);
            return getBooleanSafely(parameters, "is_schema", false);
        }
        
        
        public static String getStringSafely(JSONObject json, String key) {
            try {
                if(json != null && json.has(key)) {
                    return json.getString(key);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        
        public static boolean getBooleanSafely(JSONObject json, String key, boolean defaultValue) {
            try {
                if(json != null && json.has(key)) {
                    return json.getBoolean(key);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return defaultValue;
        }
    }
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------- WaianaStorage ---
    // -------------------------------------------------------------------------
    
    
    
    public class WaianaStorage {
        private boolean blockAllWrites = false;
        private boolean skipRightsManagement = false;
        private boolean saveAlwaysAfterChange = true;
        private boolean keepDeletedFiles = false;
        
        private String basePath = "";
        private String topicMapsPath = "topicmaps";
        private String storageFilename = "topicmaps_data.json";
        
        private HashMap<String, JSONObject> data = null;
        
        
        
        public WaianaStorage() {
            data = new LinkedHashMap();
        }
        
        
        
        // ------------------------------------------------------------ SETS ---
        
        
        public void setSkipRightsManagement(boolean b) {
            skipRightsManagement = b;
        }
        
        public void setBlockAllWrites(boolean b) {
            blockAllWrites = b;
        }
        
        public void setStorageFilename(String s) {
            if(s != null) storageFilename = s;
        }
        
        public void setBasePath(String b) {
            if(b != null) basePath = b;
        }
        
        public void setSaveAlwaysAfterChange(boolean b) {
            saveAlwaysAfterChange = b;
        }
        
        public void setKeepDeletedFiles(boolean b) {
            keepDeletedFiles = b;
        }
        
        public void setTopicMapsPath(String l) {
            if(l != null) topicMapsPath = l;
        }
        
        
        // ---------------------------------------------------------------------
        
        
        public void loadData() {
            try {
                String inStr = readFile(basePath+"/"+storageFilename);
                if(inStr != null) {
                    JSONArray dataArray = new JSONArray(inStr);
                    for(int i=0; i<dataArray.length(); i++) {
                        JSONObject d = dataArray.getJSONObject(i);
                        if(!d.has("short_name")) {
                            d.put("short_name", "temporal-"+i);
                        }
                        data.put(d.getString("short_name"), d);
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        
        public void saveData() {
            try {
                JSONArray topicmapArray = new JSONArray();
                for(JSONObject d : data.values()) {
                    topicmapArray.put(d);
                }
                String outStr = topicmapArray.toString();
                IObox.saveFile(basePath+"/"+storageFilename, outStr);
            } 
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        
        // ---------------------------------------------------------------------
        
        
        
        public JSONObject putTopicMap(User user, String name, String shortName, boolean isPublic, boolean isDownloadable, boolean isEditable, boolean isSchema, String topicMapData, StringBuffer url) {
            shortName = sanitizeShortName(shortName);
            
            JSONObject dataEntry = data.get(shortName);
            if(hasWriteRights(user, dataEntry)) {
                try {
                    if(dataEntry == null) {
                        dataEntry = new JSONObject();
                        dataEntry.put("creation_date", getDateString());
                    }

                    dataEntry.put("name", name);
                    dataEntry.put("short_name", shortName);
                    dataEntry.put("is_public", isPublic);
                    dataEntry.put("is_downloadable", isDownloadable);
                    dataEntry.put("is_editable", isEditable);
                    dataEntry.put("is_schema", isSchema);
                    dataEntry.put("edit_date", getDateString());
                    if(user != null) dataEntry.put("owner", user.getUserName());

                    writeFile(basePath+"/"+topicMapsPath+"/"+shortName+".xtm", topicMapData);
                } 
                catch (IOException ex) {
                    return createReply(1, "Exception '"+ex.getMessage()+"' occurred while saving topic map '"+shortName+"' data.");
                }
                catch(JSONException jex) {
                    return createReply(1, "Exception '"+jex.getMessage()+"' occurred while processing input data for '"+shortName+"'.");
                }
                
                data.put(shortName, dataEntry);

                if(saveAlwaysAfterChange) saveData();

                return createReply(0, "Successfully created topic map '"+name+"'", url+"?command=stream&shortName="+shortName);
            }
            else {
                return createReply(1, "Waiana storage already contains a topic map with a short name '"+shortName+"' and you have no sufficient rights to update it.");
            }
        }
        
        
        
        public JSONObject deleteTopicMap(User user, String shortName) {
            shortName = sanitizeShortName(shortName);
            JSONObject dataEntry = data.get(shortName);
            if(dataEntry != null) {
                if(hasDeleteRights(user, dataEntry)) {
                    data.remove(shortName);
                    if(!keepDeletedFiles) {
                        try {
                            IObox.deleteFile(basePath+"/"+topicMapsPath+"/"+shortName+".xtm");
                        } 
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    if(saveAlwaysAfterChange) saveData();
                    return createReply(0, "Successfully deleted topic map '"+shortName+"'.");
                }
                else {
                    return createReply(1, "You have no sufficient rights to delete the topic map with a short name '"+shortName+"'.");
                }
            }
            else {
                return createReply(1, "Waiana storage doesn't contain topic map with a short name '"+shortName+"'.");
            }
        }
        
        
        
        
        public JSONObject getTopicMap(User user, String shortName) {
            shortName = sanitizeShortName(shortName);
            JSONObject dataEntry = data.get(shortName);
            if(dataEntry != null) {
                if(hasReadRights(user, dataEntry)) {
                    JSONObject reply = new JSONObject();
                    try {
                        reply.put("code", 0);
                        try {
                            String topicMapData = readFile(basePath+"/"+topicMapsPath+"/"+shortName+".xtm");
                            
                            // System.out.println(topicMapData);
                            
                            reply.put("data", topicMapData);
                            return reply;
                        } 
                        catch (IOException ex) {
                            return createReply(1, "Exception '"+ex.getMessage()+"' occurred while loading topic map '"+shortName+"' data.");
                        }
                    }
                    catch(Exception e) {
                        return createReply(1, "Internal exception '"+e.getMessage()+"' occurred while building topic map reply.");
                    }
                }
                else {
                    return createReply(1, "You have no sufficient rights to read the topic map with a short name '"+shortName+"'.");
                }
            }
            else {
                return createReply(1, "Waiana storage doesn't contain topic map with a short name '"+shortName+"'.");
            }
        }
        
        
        
        public boolean streamTopicMap(HttpServletResponse response, User user, String shortName, String format) {
            shortName = sanitizeShortName(shortName);
            JSONObject dataEntry = data.get(shortName);
            if(dataEntry != null) {
                if(hasReadRights(user, dataEntry)) {
                    try {
                        try {
                            if("jtm".equalsIgnoreCase(format)) {
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Content-Disposition", "filename="+shortName+".jtm" );
                                OutputStream out = response.getOutputStream();
                                TopicMap tm = new TopicMapImpl();
                                tm.importXTM(new FileInputStream(new File(basePath+"/"+topicMapsPath+"/"+shortName+".xtm")));
                                tm.exportJTM(out);
                                out.close();
                                return true;
                            }
                            else {
                                response.setContentType("application/xml");
                                response.setCharacterEncoding("UTF-8");
                                response.setHeader("Content-Disposition", "filename="+shortName+".xtm" );
                                OutputStream out = response.getOutputStream();
                                InputStream in = new FileInputStream(basePath+"/"+topicMapsPath+"/"+shortName+".xtm");
                                IObox.moveData(in, out);
                                out.close();
                                return true;
                            }
                        } 
                        catch (IOException ex) {
                            writeStringToResponse(response, "Exception '"+ex.getMessage()+"' occurred while loading topic map '"+shortName+"' data.");
                        }
                    }
                    catch(Exception e) {
                        writeStringToResponse(response, "Internal exception '"+e.getMessage()+"' occurred while building topic map reply.");
                    }
                }
                else {
                    writeStringToResponse(response, "You have no sufficient rights to read the topic map with a short name '"+shortName+"'.");
                }
            }
            else {
                writeStringToResponse(response, "Waiana storage doesn't contain topic map with a short name '"+shortName+"'.");
            }
            return false;
        }
        
        
        
        
        public JSONObject getIndex() {
            JSONObject indexReply = new JSONObject();
            try {
                indexReply.put("code", 0);

                JSONArray allDatas = new JSONArray();
                for(JSONObject d : data.values()) {
                    allDatas.put(d);
                }
                indexReply.put("data", allDatas);
                return indexReply;
            }
            catch(Exception e) {
                return createReply(1, "Internal exception '"+e.getMessage()+"' occurred while building index.");
            }
        }
        
        
        // ---------------------------------------------------------------------
        
        
        
        
        public boolean hasWriteRights(User user, JSONObject dataEntry) {
            if(blockAllWrites) return false;
            if(skipRightsManagement) return true;
            if(dataEntry == null) return true;
            if(dataEntry.has("is_editable")) {
                try {
                    if(dataEntry.has("is_editable")) {
                        boolean isEditable = dataEntry.getBoolean("is_editable");
                        if(isEditable) return true;
                    }
                } catch (JSONException ex) {
                }
            }
            if(dataEntry.has("owner")) {
                if(user == null) return false;
                else {
                    try {
                        if(dataEntry.has("owner")) {
                            if(dataEntry.getString("owner").equals(user.getUserName())) {
                                return true;
                            }
                        }
                    } catch (JSONException ex) {
                    }
                }
            }
            return false;
        }
        
        
        
        public boolean hasReadRights(User user, JSONObject dataEntry) {
            if(skipRightsManagement) return true;
            if(dataEntry.has("is_public")) {
                try {
                    if(dataEntry.has("is_public")) {
                        boolean isPublic = dataEntry.getBoolean("is_public");
                        if(isPublic) return true;
                    }
                } 
                catch (JSONException ex) {}
            }
            if(dataEntry.has("owner")) {
                if(user == null) return false;
                else {
                    try {
                        if(dataEntry.has("owner")) {
                            if(dataEntry.getString("owner").equals(user.getUserName())) {
                                return true;
                            }
                        }
                    } 
                    catch (JSONException ex) {}
                }
            }
            return false;
        }
        
        public boolean hasDeleteRights(User user, JSONObject dataEntry) {
            if(blockAllWrites) return false;
            if(dataEntry == null) return false;
            if(skipRightsManagement) return true;
            if(dataEntry.has("is_editable")) {
                try {
                    if(dataEntry.has("is_editable")) {
                        boolean isEditable = dataEntry.getBoolean("is_editable");
                        if(isEditable) return true;
                    }
                } catch (JSONException ex) {
                }
            }
            if(dataEntry.has("owner")) {
                if(user == null) return true;
                else {
                    try {
                        if(dataEntry.has("owner")) {
                            if(dataEntry.getString("owner").equals(user.getUserName())) {
                                return true;
                            }
                        }
                    } catch (JSONException ex) {
                    }
                }
            }
            return false;
        }
        
        
        // ---------------------------------------------------------------------
    
    
        private String sanitizeShortName(String sn) {
            if(sn == null || sn.length() == 0) sn = "temp-"+System.currentTimeMillis();
            sn = sn.toLowerCase();
            sn = sn.replace(' ', '_');
            StringBuilder nsn = new StringBuilder("");
            for(int i=0; i<sn.length(); i++) {
                if(Character.isLetterOrDigit(sn.charAt(i))) {
                    nsn.append(sn.charAt(i));
                }
                else {
                    nsn.append('_');
                }
            }
            return nsn.toString();
        }
    }
    
    

    
    
    // ---------------------------------------------------------------------
        
        
    protected static JSONObject createReply(int code, String msg, String data) {
        JSONObject reply = new JSONObject();
        try {
            reply.put("code", code);
            reply.put("msg", msg);
            reply.put("data", data);
        }
        catch(Exception e) { e.printStackTrace(); }
        return reply;
    }

    protected static JSONObject createReply(int code, String msg) {
        JSONObject reply = new JSONObject();
        try {
            reply.put("code", code);
            reply.put("msg", msg);
        }
        catch(Exception e) { e.printStackTrace(); }
        return reply;
    }
    
    protected static DateFormat df = new SimpleDateFormat("y-M-d H:m");
    protected static String getDateString() {
        return df.format(new Date(System.currentTimeMillis()));
    }
    
    
    protected static void writeStringToResponse(HttpServletResponse response, String str) {
        try {
            response.setContentType("application/text");
            response.setCharacterEncoding("UTF-8");
            OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
            out.write(str);
            out.flush();
            out.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    protected static void writeFile(String fname, String data) throws IOException {
        File pf = new File(fname);
        if (pf.exists()) {
            pf.delete();
            System.out.println("Deleting previously existing file '" + fname + "' before save file operation!");
        }

        PrintWriter writer=new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname),"UTF-8"));
        writer.print(data);
        writer.flush();
        writer.close();
        System.out.println("Saving a file '" + fname + "'");
    }
    
    
    
    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, "UTF-8");
    }
    
    
}
