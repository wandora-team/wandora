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
 *
 *
 *
 */



package org.wandora.application.tools.maiana;

import java.io.IOException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.utils.IObox;

/**
 *
 * @author akivela
 */
public class MaianaUtils {

    private static String apiKey = "";
    private static String apiEndPoint = null;

    private static String EXPORT_CMD = "create_local_file"; // create_topic_map";
    private static String IMPORT_CMD = "download_local_file"; // download_topic_map;
    private static String DELETE_CMD = "delete_local_file"; // delete_topic_map;
    private static String LIST_CMD = "show_local_file_list"; // show_topic_map_list";

    

    
    public static String exportTemplate = 
            "{ \n"+
            "  api_key: \"__APIKEY__\", \n"+
            "  parameters: { \n"+
            "    command: \"__CMD__\", \n"+
            "    short_name: \"__SHORTNAME__\", \n"+
            "    name: \"__NAME__\", \n"+
            "    is_public: __ISPUBLIC__, \n"+
            "    is_downloadable: __ISDOWNLOADABLE__, \n"+
            "    is_editable: __ISEDITABLE__, \n"+
            "    is_schema: __ISSCHEMA__ \n"+
            "  }, \n"+
            "  data: \"__DATA__\" \n"+
            "} \n";

    
    public static String importTemplate =
            "{ \n"+
            "  api_key: \"__APIKEY__\", \n"+
            "  parameters: { \n"+
            "    command: \"__CMD__\", \n"+
            "    short_name: \"__SHORTNAME__\", \n"+
            "    owner: \"__NAME__\", \n"+
            "    format: \"__FORMAT__\" \n"+
            "  } \n"+
            "} \n";

    
    public static String deleteTemplate =
            "{ \n"+
            "  api_key: \"__APIKEY__\", \n"+
            "  parameters: { \n"+
            "    command: \"__CMD__\", \n"+
            "    short_name: \"__SHORTNAME__\", \n"+
            "  } \n"+
            "} \n";
    
    
    public static String listTemplate =
            "{ \n"+
            "  api_key: \"__APIKEY__\", \n"+
            "  parameters: { \n"+
            "    command: \"__CMD__\" \n"+
            "  } \n"+
            "} \n";

    
    public static void setApiKey(String key) {
        apiKey = key;
    }
    
    public static String getApiKey() {
        return apiKey;
    }

    public static void setApiEndPoint(String endpoint) {
        apiEndPoint = endpoint;
    }
    
    public static String getApiEndPoint() {
        return apiEndPoint;
    }
    

    public static String getExportTemplate(String apikey, String sn, String n, boolean isPublic, boolean isDownloadable, boolean isEditable, boolean isSchema, String data) {
        String json = exportTemplate;
        json = json.replace("__APIKEY__", makeJSON(apikey));
        json = json.replace("__CMD__", EXPORT_CMD);
        json = json.replace("__SHORTNAME__", makeJSON(makeShortName(sn)));
        json = json.replace("__NAME__", makeJSON(n) );
        json = json.replace("__ISPUBLIC__", isPublic ? "true" : "false" );
        json = json.replace("__ISDOWNLOADABLE__", isDownloadable ? "true" : "false" );
        json = json.replace("__ISEDITABLE__", isEditable ? "true" : "false" );
        json = json.replace("__ISSCHEMA__", isSchema ? "true" : "false" );
        //json = json.replace("__DATA__", "" );

        json = json.replace("__DATA__", makeJSON( data ) );

        return json;
    }
    

    public static String getImportTemplate(String apikey, String sn, String n, String format) {
        String json = importTemplate;
        json = json.replace("__APIKEY__", makeJSON(apikey));
        json = json.replace("__CMD__", IMPORT_CMD);
        json = json.replace("__SHORTNAME__", makeJSON(makeShortName(sn)));
        json = json.replace("__NAME__", makeJSON(n));
        json = json.replace("__FORMAT__", makeJSON(format));

        //System.out.println("json:\n"+json);

        return json;
    }

    
    public static String getDeleteTemplate(String apikey, String sn) {
        String json = deleteTemplate;
        json = json.replace("__APIKEY__", makeJSON(apikey));
        json = json.replace("__CMD__", DELETE_CMD);
        json = json.replace("__SHORTNAME__", makeJSON(makeShortName(sn)));
        return json;
    }
    

    public static String getListTemplate(String apikey) {
        String json = listTemplate;
        json = json.replace("__APIKEY__", makeJSON(apikey));
        json = json.replace("__CMD__", LIST_CMD);

        return json;
    }




    public static JSONObject listAvailableTopicMaps(String endpoint, String apikey) throws IOException, JSONException {
        String in = getListTemplate(apikey);
        checkForLocalService(endpoint);

        String reply = IObox.doUrl(new URL(endpoint), in, "application/json");

        //System.out.println("reply:\n"+reply);

        JSONObject replyObject = new JSONObject(reply);
        return replyObject;
    }



    // -------------------------------------------------------------------------


    public static String makeShortName(String sn) {
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


    public static String makeJSON(String json) {
        if(json == null) {
            return "";
        }
        else {
            json = json.replace("\\", "\\\\");
            json = json.replace("\"", "\\\"");
            json = json.replace("\b", "\\b");
            json = json.replace("\f", "\\f");
            json = json.replace("\n", "\\n");
            json = json.replace("\r", "\\r");
            json = json.replace("\t", "\\t");
        }
        return json;
    }


    
    // -------------------------------------------------------------------------
    
    
    
    
    public static void checkForLocalService(String endpoint) {
        try {
            if(endpoint != null && endpoint.startsWith("http://127.0.0.1:8898/waiana")) {
                Wandora wandora = Wandora.getWandora();
                if(wandora != null) {
                    if(!wandora.getHTTPServer().isRunning()) {
                        int a = WandoraOptionPane.showConfirmDialog(wandora, "Wandora's HTTP server is not running at the moment. Would you like to start the server first?", "Start HTTP server?", WandoraOptionPane.OK_CANCEL_OPTION);
                        if( a == WandoraOptionPane.OK_OPTION) {
                            wandora.startHTTPServer();
                            wandora.menuManager.refreshServerMenu();
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
