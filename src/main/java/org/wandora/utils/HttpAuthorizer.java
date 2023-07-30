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
 *
 * 
 *
 * HttpAuthorizer.java
 *
 * Created on 17. toukokuuta 2006, 12:46
 *
 */

package org.wandora.utils;


import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.wandora.application.Wandora;



/**
 *
 * @author akivela
 */
public class HttpAuthorizer extends Options {
    

    /**
     * Creates a new instance of HttpAuthorizer
     */
    public HttpAuthorizer() {
    }
    
    public HttpAuthorizer(String storeResource) {
        super(storeResource);
    }
    
    
    
    
    public void addAuthorization(URL url, String user, String password) {
        addAuthorization(makeKeyAddress(url), user, password);
    }
    public void addAuthorization(String address, String user, String password) {
        put(makeKeyAddress(address) + ".user", user);
        put(makeKeyAddress(address) + ".password", password);
    }
    
    
    
    
    public String getAuthorizedUserFor(URL url) {
        return getAuthorizedUserFor(makeKeyAddress(url));
    }    
    public String getAuthorizedPasswordFor(URL url) {
        return getAuthorizedPasswordFor(makeKeyAddress(url));
    }
    
    
    
    public String getAuthorizedUserFor(String address) {
        return get(makeKeyAddress(address) + ".user");
    }
    public String getAuthorizedPasswordFor(String address) {
        return get(makeKeyAddress(address) + ".password");
    }
    
    
    
    public String quessAuthorizedUserFor(String address) {
        return get( makeKeyAddress(address) + ".user");
    }
    public String quessAuthorizedPasswordFor(String address) {
        return get( makeKeyAddress(address) + ".password");
    }
    
    
    public String quessAuthorizedUserFor(URL url) {
        return quessAuthorizedUserFor(makeKeyAddress(url));
    }
    public String quessAuthorizedPasswordFor(URL url) {
        return quessAuthorizedPasswordFor(makeKeyAddress(url));
    }
    
    
    
    
    public String makeKeyAddress(String address) {
        return "httpAuth." + address;
    }
    public String makeKeyAddress(URL url) {
        return url.getHost();
    }
    
    
    
    // -------------------------------------------------------------------------
    
    

    public URLConnection getAuthorizedAccess(URL url) throws Exception {
        URLConnection uc = url.openConnection();
        Wandora.initUrlConnection(uc);
        if(uc instanceof HttpURLConnection) {
            int res = 0;
            try {
                res = ((HttpURLConnection) uc).getResponseCode();
            }
            catch(Exception e) {
                if(e.toString().indexOf("HTTP response code: 401") != -1) {
                    res = HttpURLConnection.HTTP_UNAUTHORIZED;
                }
            }
            boolean tried = false;
            if(res == HttpURLConnection.HTTP_UNAUTHORIZED) {
                String authUser = quessAuthorizedUserFor(url);
                String authPassword = quessAuthorizedPasswordFor(url);

                if(authUser != null && authPassword != null) {
                    String userPassword = authUser + ":" + authPassword;
//                    String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
                    String encoding = Base64.encodeBytes(userPassword.getBytes());
                    uc = (HttpURLConnection) uc.getURL().openConnection();
                    Wandora.initUrlConnection(uc);
                    uc.setRequestProperty ("Authorization", "Basic " + encoding);
                }
                tried = true;
                res = ((HttpURLConnection) uc).getResponseCode();
            }
        }
        return uc;
    }
    
    
    
}
