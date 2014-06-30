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
 *
 * Application.java
 *
 * Created on July 8, 2004, 4:05 PM
 */

package org.wandora.piccolo;
import org.wandora.utils.XMLParamAware;
import org.wandora.utils.XMLParamProcessor;
import java.util.*;
import javax.servlet.*;
import org.wandora.utils.*;
import org.wandora.piccolo.utils.*;
import org.w3c.dom.*;
/**
 *
 * A generic Piccolo application. Application should be initialized with XMLParamProcessor from
 * PiccoloServlet. This class is XMLParamAware and has special handling for xml parameters. service
 * elements are used to register services to application (parsed according to normal XMLParamProcessor rules).
 * 
 * actionmap element contains the action map for the application. It can contain any number of mapentry elements
 * which each contain a key element and an action element. Key element should contain the string used as the
 * action key and action element is parsed with KMLParamProcessor and should produce the action object.
 * Depending on the used action, you might need to pass some parameters to the action.
 *
 * templates element is used to specify the used templates. It can contain any number of template elements.
 * Each of these are parsed with XMLParamProcessor. Usually these will be VelocityTemplates (an XMLParamAware class)
 * in which case each template element should contain the following elements (each having a plain string value): 
 * key, version, mimetype, encoding, template, caching. Of these, the value of caching should be either true or false.
 * You can use other kinds of templates too, they just need to implement the Template interface.
 *
 * properties element is used to specify the application properties (retrieved with getProperty). It is parsed
 * with MapParser, so it may contain any number of property elements each containing key and value elements.
 * Both of these are parsed with XMLParamProcessor, so they may be any objects.
 *
 * Finally, the application element may contain any number of contextobject elements. Each is parsed with
 * XMLParamProcessor. Context objects are returned by getDefaultContext which is usually called to initialize
 * Velocity contexts. Thus these objects are usable in Velocity templates. The key for the object in the contexct
 * is taken from key attribute (note that this is without namespace) in the contextobject element.
 * 
 * Some special actions may be specified in the action map. Action with key "invaliduser" is executed when
 * the request does not specify a valid user (e.g. wrong password). However, note that the current
 * getUser implementation (which is used to get the user specified by the request) always returns a user so
 * unless you override getUser, this action will never be called. Action with key "invalidaction" is executed
 * when an invalid action key is specified in the request. If these actions are not specified but the event
 * arises where they would be used, a log entry for the event is created and nothing is returned to the client
 * who made the request.
 *
 * Some of the properties are also specifically used by Application. "defaultlang" property can be used
 * to specify the language returned by getDefaultLang, which in turn is used by getUser, when no language
 * is specified in the request parameters.
 *
 *
 * @author  olli
 */
public class Application implements XMLParamAware {
    
    protected HashMap actionMap;
    protected HashMap services;
    protected HashMap templates;
    protected HashMap properties;
    protected HashMap context;
    
    protected Logger logger;
    
    protected UserManager userManager;
    
    public Application(){
        actionMap=new HashMap();
        services=new HashMap();
        templates=new HashMap();
        properties=new HashMap();
        context=new HashMap();
    }
    
    protected User getUser(ServletRequest request){
        if(userManager!=null){
            return userManager.getUser(request);
        }
        else {
            User user=new SimpleUser();
            String lang=request.getParameter("lang");
            if(lang==null) lang=request.getLocale().getLanguage();
            if(lang==null) lang=getDefaultLanguage();
            user.setProperty(User.KEY_LANG,lang);
            return user;
        }
    }
    
    protected void updateUser(User user){
        if(userManager!=null) {
            userManager.updateUser(user);
        }
        // default implementation does nothing
    }
    
    protected Action getAction(User user,ServletRequest request){
        String actionS=request.getParameter("action");
        Action action=(Action)actionMap.get(actionS);
        return action;
    }
    
    protected void invalidUser(ServletRequest request,ServletResponse response){
        Action action=(Action)actionMap.get("invaliduser");
        if(action!=null){
            action.doAction(null,request,response,this);
        }
        else{
            logger.writelog("DBG","Invalid user");
            // TODO: Default invalid user handling
        }
    }
    protected void invalidAction(User user,ServletRequest request,ServletResponse response){
        Action action=(Action)actionMap.get("invalidaction");
        if(action!=null){
            action.doAction(user,request,response,this);
        }
        else{
            logger.writelog("DBG","Invalid action");
            // TODO: Default invalid action handling
        }        
    }
    
    public void handleRequest(ServletRequest request,ServletResponse response){
        User user=getUser(request);
        if(user==null){
            invalidUser(request,response);
            return;
        }
        Action action=getAction(user,request);
        if(action==null){
            invalidAction(user,request,response);
            return;
        }
        action.doAction(user,request,response,this);
        updateUser(user);
    }
    
    protected String getDefaultLanguage(){
        String lang="en";
        if(getProperty("defaultlang")!=null) lang=getProperty("defaultlang").toString();
        return lang;
    }
    
    protected String getDefaultTemplateVersion(){
        return getDefaultLanguage();
    }
    
    protected String getTemplateVersion(User user){
        String lang=(String)user.getProperty(User.KEY_LANG);
        if(lang==null) lang=getDefaultLanguage();
        return lang;
    }
    
    /**
     * Gets the template with the specified key and version according to user preferences. If no template
     * is found with the preferred user version, then a template with default version is returned.
     */
    public Template getTemplate(String key,User user){
        HashMap hm=(HashMap)templates.get(key);
        if(hm==null) return null;
        Template t=(Template)hm.get(getTemplateVersion(user));
        if(t==null) t=(Template)hm.get(getDefaultTemplateVersion());
        return t;
    }
    
    /**
     * Returns a property defined in the XML params file used to initialize this application.
     */
    public Object getProperty(Object key){
        return properties.get(key);
    }
    
    protected Logger getDefaultLogger(){
        return new SimpleLogger();        
    }
    
    public Service getService(String serviceType){
        return (Service)services.get(serviceType);
    }
    
    protected void processParamElement(Element element,XMLParamProcessor processor) throws Exception {
        String name=element.getNodeName();
        if(name.equals("service")){
            Object o=processor.createObject(element);
            if(!(o instanceof Service)) logger.writelog("WRN","Configured service is not instance of Service");
            else{
                services.put(((Service)o).getServiceType(),o);
            }
        }
        else if(name.equals("templates")){
            if(processor.getClassMapping("template")==null)
                processor.mapClass("template","org.wandora.piccolo.VelocityTemplate");
            Template[] ts=(Template[])processor.createArray(element,new Template[0]);
            for(int i=0;i<ts.length;i++){
                HashMap hm=(HashMap)templates.get(ts[i].getKey());
                if(hm==null){
                    hm=new HashMap();
                    templates.put(ts[i].getKey(),hm);
                }
                hm.put(ts[i].getVersion(),ts[i]);
            }
        }
        else if(name.equals("actionmap")){
            NodeList nl=element.getChildNodes();
            for(int i=0;i<nl.getLength();i++){
                Node n=nl.item(i);
                if(n instanceof Element){
                    Element e=(Element)n;
                    if(e.getNodeName().equals("mapentry")){
                        String key=null;
                        Action action=null;
                        NodeList nl2=e.getChildNodes();
                        for(int j=0;j<nl2.getLength();j++){
                            Node n2=nl2.item(j);
                            if(n2 instanceof Element){
                                Element e2=(Element)n2;
                                if(e2.getNodeName().equals("key")){
                                    key=processor.getElementContents(e2);
                                }
                                else if(e2.getNodeName().equals("action")){
                                    Object o=processor.createObject(e2);
                                    if(!(o instanceof Action)){
                                        logger.writelog("WRN","Action map action "+o.getClass().getName()+" is not an instance of action.");
                                    }
                                    else action=(Action)o;
                                }
                                else{
                                    logger.writelog("WRN","Unknown element named "+e2.getNodeName()+" in actionmap mapentry.");
                                }
                            }
                        }
                        if(key==null){
                            logger.writelog("WRN","Key not specified for action.");
                        }
                        else if(action==null){
                            logger.writelog("WRN","Action not specified for action.");                            
                        }
                        else{
                            actionMap.put(key,action);
                        }
                    }
                    else{
                        logger.writelog("WRN","Unknown element named "+e.getNodeName()+" in actionmap.");                        
                    }
                }
            }
        }
        else if(name.equals("properties")){
            MapParser mp=(MapParser)processor.createObject(element,"org.wandora.piccolo.MapParser");
            properties.putAll(mp);
        }
        else if(name.equals("contextobject")){
            Object o=processor.createObject(element);
            String key=element.getAttribute("key");
            context.put(key,o);
        }
        else if(name.equals("usermanager")){
            Object o=processor.createObject(element);
            userManager=(UserManager)o;
        }
        else {
            logger.writelog("WRN","Unknown element named "+name+" in configuration file.");
        }
    }
    
    /**
     * Returns a default context that should be used as the base for most Velocity templates. The returned
     * context will contain all context objects specified in the XML parameters and this Application with key
     * "application", the user provided in the parametrs with key "user" and the language of the user with
     * key "lang".
     */
    public HashMap getDefaultContext(User user){
        HashMap context=new HashMap();
        context.put("lang",user.getProperty(User.KEY_LANG));
        context.put("user",user);
        context.put("application",this);
        context.putAll(this.context);
        return context;
    }
        
    /**
     * XMLParamAware implementation. Handles initialization from XML parameters.
     */
    public void xmlParamInitialize(org.w3c.dom.Element element, org.wandora.utils.XMLParamProcessor processor) {
        logger=(Logger)processor.getObject("logger");
        Logger.setLogger(logger);
        if(logger==null){
            logger=getDefaultLogger();
            logger.writelog("INF","No logger defined, using default.");            
        }
        processor.addObjectToTable("logger",logger);
        // now initialize everything else
        NodeList nl=element.getChildNodes();
        for(int i=0;i<nl.getLength();i++){
            Node n=nl.item(i);
            if(n instanceof Element){
                Element e=(Element)n;
                try{
                    processParamElement(e,processor);
                }catch(Exception ex){
                    logger.writelog("WRN",ex.getClass().getName()+" when processing config file, element "+e.getNodeName()+". "+ex.getMessage());  
                }
            }
        }
    }    
    
}
