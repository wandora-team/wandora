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
package org.wandora.utils;

import java.net.Authenticator.RequestorType;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;

/**
 *
 * @author olli
 */


public class SimpleNetAuthenticator implements MultiNetAuthenticator.SingleAuthenticator {
    
    protected String host;
    protected String user;
    protected char[] password;
    
    public SimpleNetAuthenticator(String host, String user, String password){
        this(host,user,password.toCharArray());
    }
    public SimpleNetAuthenticator(String host, String user, char[] password){
        this.host=host;
        this.user=user;
        this.password=password;
    }
    
    public SimpleNetAuthenticator(){}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    
    

    @Override
    public PasswordAuthentication getPasswordAuthentication(String host, InetAddress addr, int port, String protocol, String prompt, String scheme, URL url, RequestorType reqType) {
        if(this.host.equalsIgnoreCase(host)) {
            return new PasswordAuthentication(user, password);
        }
        else return null;
    }
    
}
