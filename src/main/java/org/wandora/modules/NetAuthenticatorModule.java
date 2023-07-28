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
package org.wandora.modules;


import java.util.LinkedHashMap;
import java.util.Map;
import org.wandora.utils.MultiNetAuthenticator;
import org.wandora.utils.SimpleNetAuthenticator;

/**
 *
 * @author olli
 */


public class NetAuthenticatorModule extends AbstractModule {

    private static class Login {
        public String host;
        public String user;
        public String password;
    }
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        Map<String,Login> logins=new LinkedHashMap<String,Login>();
        
        for(Map.Entry<String,Object> e : settings.entrySet()){
            String key=e.getKey();
            if(key.startsWith("login.")) {
                key=key.substring("login.".length());
                
                int ind=key.indexOf(".");
                if(ind<0) continue;
                
                String name=key.substring(0,ind);
                key=key.substring(ind+1);
                
                Login l=logins.get(name);
                if(l==null){
                    l=new Login();
                    logins.put(name,l);
                }
                
                String value=e.getValue().toString();
                
                if(key.equalsIgnoreCase("host")) l.host=value;
                else if(key.equalsIgnoreCase("user")) l.user=value;
                else if(key.equalsIgnoreCase("password")) l.password=value;
            }
        }
        
        for(Login l : logins.values()){
            MultiNetAuthenticator.getInstance().addAuthenticator(new SimpleNetAuthenticator(l.host, l.user, l.password));
        }
        
        super.init(manager, settings);
    }
    
}
