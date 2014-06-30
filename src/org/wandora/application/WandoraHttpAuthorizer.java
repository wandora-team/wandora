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
 * WandoraHttpAuthorizer.java
 *
 * Created on 17. toukokuuta 2006, 12:46
 *
 */

package org.wandora.application;

import org.wandora.utils.IObox;
import org.wandora.utils.Base64;
import org.wandora.utils.HttpAuthorizer;
import java.net.*;

import org.wandora.application.gui.*;



/**
 * WandoraHttpAuthorizer is a helper class to handle URLs requiring
 * HTTP authorization. Class captures unauthorized requests and
 * asks Wandora user for a username and a password to access the
 * URL.
 *
 * @author akivela
 */
public class WandoraHttpAuthorizer extends HttpAuthorizer {
    
    Wandora admin = null;
    
    
    /**
     * Creates a new instance of HttpAuthorizer
     */
    public WandoraHttpAuthorizer(Wandora admin) {
        this.admin = admin;
    }
    
    public WandoraHttpAuthorizer(Wandora admin, String storeResource) {
        super(storeResource);
        this.admin = admin;
    }

        
    
    // -------------------------------------------------------------------------
    
    
    @Override
    public URLConnection getAuthorizedAccess(URL url) throws Exception {
        if(url.toExternalForm().startsWith("https://")) {
            IObox.disableHTTPSCertificateValidation();
            try {
                url = new URL(url.toExternalForm());
            }
            catch (MalformedURLException e) {
            }
        }
        // -------------------------------------
        
        URLConnection uc = url.openConnection();
        Wandora.initUrlConnection(uc);
        if(uc instanceof HttpURLConnection) {
            int res = ((HttpURLConnection) uc).getResponseCode();
            boolean tried = false;
            while(res == HttpURLConnection.HTTP_UNAUTHORIZED) {
                String authUser = quessAuthorizedUserFor(url);
                String authPassword = quessAuthorizedPasswordFor(url);
                
                if("__IGNORE".equals(authUser) && "__IGNORE".equals(authPassword)) continue;

                if(authUser == null || authPassword == null || tried == true) {
                    PasswordPrompt pp = new PasswordPrompt(admin, true);
                    pp.setTitle(uc.getURL().toExternalForm());
                    admin.centerWindow(pp);
                    pp.setVisible(true);
                    if(!pp.wasCancelled()) {
                        authUser = pp.getUsername();
                        authPassword = new String(pp.getPassword());
                        addAuthorization(url, authUser, authPassword);
                    }
                    else {
                        addAuthorization(url, "__IGNORE", "__IGNORE");
                        continue;
                    }
                }
                
                if(authUser != null && authPassword != null) {
                    String userPassword = authUser + ":" + authPassword;
//                    String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
                    String encodedUserPassword = Base64.encodeBytes(userPassword.getBytes());
                    uc = (HttpURLConnection) uc.getURL().openConnection();
                    Wandora.initUrlConnection(uc);
                    uc.setUseCaches(false);
                    uc.setRequestProperty ("Authorization", "Basic " + encodedUserPassword);
                }
                tried = true;
                res = ((HttpURLConnection) uc).getResponseCode();
            }
        }
        return uc;
    }
    
    
    
}
