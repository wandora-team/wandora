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

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;

/**
 *
 * @author olli
 */


public class MultiNetAuthenticator extends Authenticator {

    private static MultiNetAuthenticator instance=null;
    protected final ArrayList<SingleAuthenticator> authenticators=new ArrayList<SingleAuthenticator>();
    
    public static synchronized MultiNetAuthenticator getInstance(){
        if(instance!=null) return instance;
        else {
            instance=new MultiNetAuthenticator();
            return instance;
        }
    }
    
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String host=this.getRequestingHost();
        InetAddress addr=this.getRequestingSite();
        int port=this.getRequestingPort();
        String protocol=this.getRequestingProtocol();
        String prompt=this.getRequestingPrompt();
        String scheme=this.getRequestingScheme();
        URL url=this.getRequestingURL();
        RequestorType reqType=this.getRequestorType();
        
        synchronized(authenticators){
            for(SingleAuthenticator auth : authenticators){
                PasswordAuthentication ret=auth.getPasswordAuthentication(host, addr, port, protocol, prompt, scheme, url, reqType);
                if(ret!=null) return ret;
            }
        }
        return null;
    }


    public void addAuthenticator(SingleAuthenticator auth){
        synchronized(authenticators){
            authenticators.add(auth);
            if(authenticators.size()==1) Authenticator.setDefault(this);
        }
    }
    
    public static interface SingleAuthenticator {
        public PasswordAuthentication getPasswordAuthentication(
                                    String host,
                                    InetAddress addr,
                                    int port,
                                    String protocol,
                                    String prompt,
                                    String scheme,
                                    URL url,
                                    RequestorType reqType);
    }
}

