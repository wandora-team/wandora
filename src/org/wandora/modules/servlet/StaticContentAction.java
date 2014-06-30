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
 */
package org.wandora.modules.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.spec.PSSParameterSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.usercontrol.User;

/**
 *
 * @author olli
 */


public class StaticContentAction extends AbstractAction {

    protected String mountPoint=null;
    protected ArrayList<Pattern> forbidden=new ArrayList<Pattern>();
    protected String urlPrefix="";
    
    private org.mortbay.jetty.MimeTypes mimeTypes;

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        return deps;
    }
    
    
    
    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        this.isDefaultAction=true; // defaultAction defaults to true, can still be changed with init params
        
        Object o;

        o=settings.get("urlPrefix");
        if(o!=null) urlPrefix=o.toString().trim();
        
        o=settings.get("mountPoint");
        if(o!=null) {
            try{
                File f=new File(o.toString().trim());
                if(!f.exists() || !f.isDirectory()) {
                    throw new ModuleException("Mount point \""+o.toString().trim()+"\" doesn't exist or is not a directory");
                }
                mountPoint=f.getCanonicalPath();
                if(!mountPoint.endsWith(File.separator)) mountPoint+=File.separator;
                
            }catch(IOException ioe){
                throw new ModuleException("Couldn't get canonical path of mount point",ioe);
            }
        }
        else {
            String moduleSource=manager.getModuleSettings(this).source;
            if(moduleSource!=null){
                File f=new File(moduleSource);
                if(f.exists()){
                    f=f.getAbsoluteFile();
                    if(!f.isDirectory()) {
                        f=f.getParentFile();
                    }
                    if(f!=null){
                        try{
                            mountPoint=f.getCanonicalPath();
                            if(!mountPoint.endsWith(File.separator)) mountPoint+=File.separator;
                        }catch(IOException ioe){
                            throw new ModuleException("Couldn't get canonical path of mount point",ioe);
                        }
                    }
                }
            }
        }
        if(mountPoint==null) throw new ModuleException("Mount point not specified and could not determine it automatically");
            
        o=settings.get("forbidden");
        if(o==null) o=".*config.xml\n.*/templates/.*\n.*cache/.*\n~";
        String[] split=o.toString().split("\n+");
        for(String s : split){
            forbidden.add(Pattern.compile(s));
        }
        
        mimeTypes=new org.mortbay.jetty.MimeTypes();
        
        super.init(manager, settings);
    }
    
    

    protected String getLocalPath(HttpServletRequest req){
        // ContextPath is the part of the request uri which was used to select
        // the servlet context. Remove it from the start of the request uri
        // to get the target relative to this context. Request URI does not
        // contain any parameters, even if they were used in the http request.
        
        String context=req.getContextPath();
        if(context==null || context.length()==0) context="/";
        if(!context.endsWith("/")) context+="/";
        
        String target=req.getRequestURI();
        if(target.startsWith(context)) {
            target=target.substring(context.length());
        }
        else return null;
        
        if(!target.startsWith(urlPrefix)) return null;
        
        target=target.substring(urlPrefix.length());
        
        if(!File.separator.equals("/")) {
            target=target.replace("/", File.separator);
        }
        
        String localPath=mountPoint+target;
        File f=new File(localPath);
        if(!f.exists() || f.isDirectory()) return null;
        try{
            localPath=f.getCanonicalPath();
            /* Checking that canonical form of localPath starts with the canonical
             * form of mountPoint makes sure that localPath really is under
             * mountPoint. Tricks like using .. in the path will be taken care of
             * by this. The downside is that symbolic links under the mount point pointing
             * to a destination outside the mount point will not work.
             */
            if(!localPath.startsWith(mountPoint)) return null;
            
            for(Pattern p : forbidden){
                if(p.matcher(localPath).matches()) return null;
            }
            
            return localPath;
        }catch(IOException ioe){
            logging.warn("Couldn't get canonical path of requested file",ioe);
            return null;
        }
    }
    
    protected boolean returnFile(HttpServletResponse response,File f) throws IOException {
        if(!f.exists() || f.isDirectory()){
            return false;
        }

        Object o=mimeTypes.getMimeByExtension(f.getName());
        if(o!=null) response.setContentType(o.toString());
        else response.setContentType("text/plain");
        OutputStream out=response.getOutputStream();

        InputStream in=new FileInputStream(f);
        byte[] buf=new byte[8192];
        int read=-1;
        while((read=in.read(buf))!=-1){
            out.write(buf,0,read);
        }
        out.flush();
        in.close();
        
        return true;
    }

    @Override
    public boolean isHandleAction(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method) {
        if(super.isHandleAction(req, resp, method)){
            return getLocalPath(req)!=null;
        }
        else return false;
    }
    
    
    
    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, String action, User user) throws ServletException, IOException, ActionException {
        String localPath=getLocalPath(req);
        if(localPath!=null) {
            return returnFile(resp,new File(localPath));
        }
        else return false;
    }
    
}
