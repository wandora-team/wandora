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
 */
package org.wandora.modules.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;

/**
 *
 * @author olli
 */


public class RequestForwarder extends CachedAction {

    protected LinkedHashMap<String,String> additionalParams;
    protected ArrayList<String> forwardParams;
    protected boolean forwardAllParams=false;
    
    protected String destinationProtocol;
    protected String destinationHost;
    protected int destinationPort;
    protected String destinationPath;
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        additionalParams=new LinkedHashMap<String,String>();
        forwardParams=new ArrayList<String>();
        
        for(Map.Entry<String,Object> e : settings.entrySet()){
            String key=e.getKey();
            if(key.startsWith("additionalParam.")){
                key=key.substring("additionalParam.".length());
                Object o=e.getValue();
                if(o==null) additionalParams.put(key,null);
                else additionalParams.put(key,o.toString());
            }
        }
        
        Object o;
        o=settings.get("forwardParams");
        if(o!=null){
            String[] split=o.toString().split("[;\n]");
            for(int i=0;i<split.length;i++){
                String s=split[i].trim();
                if(s.length()>0) forwardParams.add(s);
            }
        }
        
        o=settings.get("forwardAllParams");
        if(o!=null) forwardAllParams=Boolean.parseBoolean(o.toString());
        
        Pattern destinationParser=Pattern.compile("^(?:(http(?:s)?)://)?([^/:]+)(?::(\\d+))?(?:(/.*))?$");
        o=settings.get("destination");
        if(o!=null) {
            Matcher m=destinationParser.matcher(o.toString());
            if(m.matches()){
                destinationProtocol=m.group(1);
                if(destinationProtocol==null || destinationProtocol.length()==0) destinationProtocol="http";
                destinationHost=m.group(2);
                String destinationPortS=m.group(3);
                destinationPath=m.group(4);
                if(destinationPath==null || destinationPath.length()==0) destinationPath="/";
                if(destinationPortS!=null && destinationPortS.length()>0) destinationPort=Integer.parseInt(destinationPortS);
                else destinationPort=-1; // this gets translated to protocol default later
            }
        }
        
        super.init(manager, settings);
    }
    
    protected LinkedHashMap<String,String> getSendParams(HttpServletRequest req){
        LinkedHashMap<String,String> params=new LinkedHashMap<String,String>();
        if(forwardAllParams){
            params.putAll(req.getParameterMap());            
        }
        else {
            for(String s : forwardParams){
                String v=req.getParameter(s);
                if(v!=null) params.put(s,v);
            }
        }
        params.putAll(additionalParams);
        return params;
    }

    protected String makeSendParamString(HttpServletRequest req){
        LinkedHashMap<String,String> params=getSendParams(req);

        StringBuilder paramString=new StringBuilder();
        for(Map.Entry<String,String> e : params.entrySet()){
            try{
                String value=e.getValue();

                if(paramString.length()>0) paramString.append("&");
                paramString.append(URLEncoder.encode(e.getKey(), "UTF-8"));
                paramString.append("=");
                paramString.append(URLEncoder.encode(value, "UTF-8"));
            }catch(UnsupportedEncodingException uee){
                throw new RuntimeException(uee); // shouldn't happen, hardcoded UTF-8
            }
        }
        
        return paramString.toString();
    }

    @Override
    protected void returnOutput(InputStream cacheIn, HttpServletResponse resp) throws IOException {
        try{
            Map<String,String> metadata=readMetadata(cacheIn);
            String contentType=metadata.get("contentType");
            if(contentType.length()>0) resp.setContentType(contentType);
            String encoding=metadata.get("encoding");
            if(encoding.length()>0) resp.setCharacterEncoding(encoding);
            super.returnOutput(cacheIn, resp);
        }
        finally{
            cacheIn.close();
        }
    }
    
    @Override
    protected boolean doOutput(HttpServletRequest req, HttpMethod method, String action, OutputProvider out, org.wandora.modules.usercontrol.User user) throws ServletException, IOException {
        logging.debug("Start forwarding request");
        
        HttpURLConnection connection;

        String path=destinationPath;
        String paramString=makeSendParamString(req);
        if(path.indexOf("?")>0) path+="&"+paramString;
        else path+="?"+paramString;
            
        connection=(HttpURLConnection)new URL(destinationProtocol,destinationHost,destinationPort,path).openConnection();

        int response=connection.getResponseCode();
        if(response!=HttpURLConnection.HTTP_OK) {
            logging.info("Got response code "+response);
            return false;
        }
        
        InputStream in;
        try{
            in=connection.getInputStream();
        }catch(IOException ioe){
            logging.warn(ioe);
            return false;
        }
        
        logging.debug("Starting response");        
        
        HashMap<String,String> metadata=new HashMap<String,String>();
        String contentType=connection.getContentType();
        if(contentType==null) contentType="";
        String encoding=connection.getContentEncoding();
        if(encoding==null) encoding="";
        metadata.put("contentType",contentType);
        metadata.put("encoding",encoding);
        
        OutputStream outStream=out.getOutputStream();
        try{
            writeMetadata(metadata, outStream);
            
            byte[] buf=new byte[2048];
            int read;
            while( (read=in.read(buf))!=-1 ){
                outStream.write(buf,0,read);
            }
        }
        finally{
            outStream.close();
        }
        
        
        return true;
    }

    @Override
    protected String getCacheKey(HttpServletRequest req, HttpMethod method, String action) {
        LinkedHashMap<String,String> params=getSendParams(req);
        params.put("requestAction",action);
        return buildCacheKey(params);
    }
    
}
