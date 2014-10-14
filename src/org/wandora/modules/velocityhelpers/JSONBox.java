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



package org.wandora.modules.velocityhelpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONObject;

/**
 *
 * @author akivela
 */


public class JSONBox {
    
    
    public JSONBox() {
        
    }
    
    public static JSONObject load(String urlStr) {
        JSONObject json = null;
        try {
            if(urlStr != null) {
                URL url = new URL(urlStr);
                URLConnection urlConnection = url.openConnection();

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder sb = new StringBuilder("");
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                }
                in.close();
                json = new JSONObject(sb.toString());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return json;
    }
    
    
    
}
