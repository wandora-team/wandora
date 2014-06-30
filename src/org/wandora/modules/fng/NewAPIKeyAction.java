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
package org.wandora.modules.fng;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.wandora.modules.EmailModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ActionException;
import org.wandora.modules.servlet.GenericTemplateAction;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;
import org.wandora.modules.servlet.SendEmailAction;
import org.wandora.modules.servlet.SendEmailAction.EmailContent;
import org.wandora.modules.servlet.Template;
import org.wandora.modules.usercontrol.ModifyableUserStore;
import org.wandora.modules.usercontrol.User;
import org.wandora.modules.usercontrol.UserStoreException;

/**
 *
 * @author olli
 */


public class NewAPIKeyAction extends GenericTemplateAction {

    protected static class UserParam {
        String requestKey;
        String userKey;
        String pattern="^[^\\s].*";
        String value=null;
        boolean unique=false;
        public UserParam(){}
    }
    
    protected ArrayList<String> userRoles;
    protected HashMap<String,UserParam> userParams;
    protected ModifyableUserStore userStore;
    
    protected EmailModule email;
    
    protected String emailTemplateKey;
    public String emailSubject;
    public String emailFrom;

    protected ArrayList<String> checkParams(HttpServletRequest req,HashMap<String,UserParam> params) throws UserStoreException {
        ArrayList<String> errors=new ArrayList<String>();
        
        for(UserParam param : params.values() ) {
            if(param.value!=null) continue;
            String value=req.getParameter(param.requestKey);
            if(value==null) value="";
            value=value.trim();
            Pattern pattern=Pattern.compile(param.pattern);
            Matcher m=pattern.matcher(value);
            if(!m.matches()) errors.add(param.requestKey);
            
            if(param.unique){
                if(!userStore.findUsers(param.userKey, value).isEmpty()) errors.add(param.requestKey+";notunique");
            }
        }
        
        return errors;
    }
    
    protected final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @Override
    protected HashMap<String, Object> getTemplateContext(Template template, HttpServletRequest req, HttpMethod method, String action, User user) throws ActionException {
        Template emailTemplate=templateManager.getTemplate(emailTemplateKey, null);
        if(emailTemplate==null){
            logging.warn("Couldn't find email template");
            return null;
        }

        HashMap<String,Object> params=super.getTemplateContext(template, req, method, action, user);
        
        boolean sent=false;
        String error=null;
        try{
            ArrayList<String> paramErrors=checkParams(req, userParams);
            String apikey=null;
            
            params.put("paramErrors",paramErrors);

            if(paramErrors.isEmpty()){

                apikey=generateApiKey();
                while(userStore.getUser(apikey)!=null) { apikey=generateApiKey(); }

                params.put("apikey", apikey);

                User u=userStore.newUser(apikey);
                if(u!=null){
                    for(String r : userRoles){
                        u.addRole(r);
                    }

                    for(UserParam param : userParams.values()){
                        if(param.value!=null) {
                            String value=param.value;
                            value=doReplacements(value, req, method, action, user);
                            u.setOption(param.userKey, value);
                        }
                        else {
                            String value=req.getParameter(param.requestKey);
                            if(value==null) value="";
                            value=value.trim();
                            u.setOption(param.userKey, value);
                        }
                    }
                    if(u.saveUser()){
                        ByteArrayOutputStream baos=new ByteArrayOutputStream();
                        emailTemplate.process(params, baos);

                        try{
                            EmailContent ec=SendEmailAction.parseEmail(baos.toByteArray(), emailTemplate.getEncoding(), true, false, false);
                            if(ec!=null){
                                email.send(ec.recipients, emailFrom, emailSubject, ec.message, emailTemplate.getMimeType());
                                sent=true;
                            }
                            else {
                                logging.warn("Couldn't parse email template");
                            }
                        }
                        catch(IOException ioe){
                            logging.warn("Couldn't send api key email",ioe);
                        }
                    }
                    else logging.warn("Unable to save user with key "+apikey);
                }
            }
            
        }
        catch(UserStoreException use){
            error="Couldn't send API key. "+use.getMessage();
        }
        params.put("emailSent",sent);
        params.put("error",error);

        return params;
    }
    
    protected String generateApiKey(){
        // TODO: something a bit more clever
        String random=Integer.toString((int)(Math.random()*Math.pow(36,5)),36);
        String time=Long.toString(System.currentTimeMillis(),36);
        while(random.length()<5) random="0"+random;
        return time+random;
    }
    
    @Override
    protected String getCacheKey(HttpServletRequest req, HttpMethod method, String action) {
        return null; // no caching at all
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        manager.requireModule(EmailModule.class, deps);
        manager.requireModule(ModifyableUserStore.class, deps);
        requireLogging(manager, deps);
        return deps;
    }
    

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        userParams=new HashMap<String, UserParam>();
        userRoles=new ArrayList<String>();
        
        Object o;
        o=settings.get("emailTemplateKey");
        if(o!=null) emailTemplateKey=o.toString();
        
        o=settings.get("emailSubject");
        if(o!=null) emailSubject=o.toString();
        
        o=settings.get("emailFrom");
        if(o!=null) emailFrom=o.toString();
        
        o=settings.get("userRoles");
        if(o!=null){
            String[] roles=o.toString().split("[;\\n]");
            for(int i=0;i<roles.length;i++){
                String r=roles[i].trim();
                if(r.length()>0) userRoles.add(r);
            }
        }
        
        for(Map.Entry<String,Object> e : settings.entrySet() ){
            String key=e.getKey();
            String value=e.getValue().toString();
            if(key.startsWith("userParam.")){
                key=key.substring("userParam.".length());
                
                String paramName;
                
                int ind=key.indexOf(".");
                if(ind<0) {
                    paramName=key;
                    key="";
                }
                else {                
                    paramName=key.substring(0,ind);
                    key=key.substring(ind+1);
                }
                
                UserParam param=userParams.get(paramName);
                if(param==null){
                    param=new UserParam();
                    param.requestKey=paramName;
                    param.userKey=paramName;
                    userParams.put(paramName,param);
                }
                
                if(key.equalsIgnoreCase("key")) {
                    param.userKey=value;
                }
                else if(key.equalsIgnoreCase("pattern")) {
                    param.pattern=value;
                }
                else if(key.equalsIgnoreCase("value")) {
                    param.value=value;
                }
                else if(key.equalsIgnoreCase("unique")) {
                    param.unique=Boolean.parseBoolean(value);
                }
            }
        }
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        email=manager.findModule(EmailModule.class);
        userStore=manager.findModule(ModifyableUserStore.class);
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        email=null;
        userStore=null;
        
        super.stop(manager);
    }
    
}
