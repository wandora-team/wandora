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
package org.wandora.modules.usercontrol;

import org.wandora.modules.servlet.GenericContext;
import java.io.IOException;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;

/**
 * A context which restricts access to the source IP address. The allowed
 * addresses are specified in the initialisation parameters using parameter names
 * address and mask. The address specifies an IP address and mask a mask to use
 * with the address. Any incoming request which matches the IP address where the
 * mask bits are 1, are allowed access, all others are denied access. The default
 * value for mask is 255.255.255.255, so if you only specify the IP address, only
 * that single IP address will be allowed access. The default value for address
 * is 127.0.0.1 for local access only.
 * 
 * 
 * @author olli
 */


public class SourceRestrictedContext extends GenericContext {

    protected int address=0x7f000001; // 127.0.0.1
    protected int mask=0xffffffff;
    
    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        Object o=settings.get("address");
        if(o!=null){
            String s=o.toString();
            int ind=s.indexOf("/");
            if(ind>0){
                address=makeAddress(s.substring(0,ind));
                mask=makeMask(s.substring(ind+1));
            }
            else address=makeAddress(s);
        }
        o=settings.get("mask");
        if(o!=null) mask=makeMask(o.toString());
        
        super.init(manager, settings);
    }

    
    public static int makeAddress(String address){
        String[] s=address.split("\\.");
        int ret=0;
        try{
            for(int i=0;i<4;i++){
                int n=0;
                if(i<s.length) n=Math.min(Math.max(Integer.parseInt(s[i]),0),255);
                ret=((ret<<8)|n);
            }
        }catch(NumberFormatException nfe) {return 0;}
        return ret;
    }
    
    public static int makeMask(String mask){
        if(mask.indexOf(".")>=0) return makeAddress(mask);
        int bits=Math.min(Math.max(Integer.parseInt(mask),0),32);
        int ret=0;
        for(int i=0;i<bits;i++) ret=((ret<<1)|1);
        if(bits<32) ret=(ret<<(32-bits));
        return ret;
    }
    
    @Override
    protected ForwardResult doForwardRequest(HttpServletRequest req, HttpServletResponse resp, HttpMethod method) throws ServletException, IOException {
        String remoteAddress=req.getRemoteAddr();
        int addr=makeAddress(remoteAddress);
        if(addr==0) return new ForwardResult(false,false);
        if((addr&mask)==address) return new ForwardResult(true, false);
        else return new ForwardResult(false,false);
    }
    
}
