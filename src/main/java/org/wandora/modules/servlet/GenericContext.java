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
package org.wandora.modules.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.wandora.modules.AbstractModule;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.usercontrol.User;
import org.wandora.utils.ListenerList;
import org.wandora.utils.ParallelListenerList;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 * A base for context modules. Context modules split a ServletModule into
 * several different contexts. The context modules themselves are also 
 * ServletModules, they just take some of the root servlet module requests and
 * forward them to actions registered to the specific context module. Actions see
 * the context as any other ServletModule.
 * </p>
 * <p>
 * Context modules may do something to the request before forwarding it, for
 * example to restrict the use of the other modules registered to it. This could,
 * for example, be user authentication with a username and a password, or checking
 * that the request originates from localhost or something similar. You could also
 * just add more specific logging for each request and the unconditionally pass
 * on the request.
 * </p>
 * <p>
 * The GenericContext contains some common features, and can serve as a base class
 * for other contexts, although it's not required to extend it. You can also just
 * directly implement ServletModule and appear as a servlet to other modules.
 * Naturally you will also need to implement ServletModule.RequestListener to
 * register your context to the root servlet module. It is also recommended that
 * you implement ActionHandler, which adds some features to the RequestListener
 * and aids in forwarding requests.
 * </p>
 * <p>
 * GenericContext is not abstract and can be used on its own for some purposes.
 * It can be used to add custom exception handling for actions inside the context,
 * or to group actions by request path or local server directory.
 * </p>
 * <p>
 * Grouping actions by request path works with the initialisation parameter
 * urlPrefix. This sets a prefix that the request path must use for the request
 * to be passed in this context. The prefix is also removed from the request so
 * that you can nest more urlPrefix restricted contexts inside.
 * </p>
 * <p>
 * The local context path can be changed with the contextPath initialisation
 * parameter. By default the context path is the server path. Actions may use
 * this as a basis for accessing their files, but are not required to. The
 * contextPath resets this path. You can also use contextDir initialisation
 * parameter to set both urlPrefix and contextPath at the same time. For example,
 * if your server directory has a subdirectory "webapp", then setting contextPath
 * to "webapp" would set that directory as the default path for actions in this
 * context, and would pass the request to them only when the request has the path
 * prefix "webapp".
 * </p>
  * <p>
 * Another thing that GenericContext does without any extending is provide a hook
 * for exception handling in actions. This is done by including any module that
 * implements ActionExceptionHandler. GenericContext uses it as an optional
 * dependency, so one will be found automatically if you define it in your config.
 * To have different exception handlers for different contexts, use the priority
 * or useService mechanics to specify which one to use with which context.
 * Whenever an ActionException occurs within an action inside this context,
 * it'll be given to the exception handler if one is available. Other exceptions
 * than ActionExceptions won't be caught by the handler. See ActionExceptionHandler
 * for more details.
 * </p> 
 * <p>
 * Overriding classes should override at least doForwardRequest, which checks if the
 * request should be forwarded on. You can implement access restrictions by
 * just overriding this. You may also want to override isHandleAction, it is 
 * one of the first checks after receiving a request. Basically it checks if any
 * of the registered actions is going to be interested in the request, if not,
 * the context will drop the request immediately. Naturally you may also
 * override handleRequest to do your own processing and then possibly call the
 * super implementation.
 * </p>
 * <p>
 * In the initialisation parameters, you may set checkActions to false to disable
 * the initial check of whether any registered actions are interested in the request.
 * The initial check is possibly only for actions implementing ActionHandler,
 * if any of your actions does not implement ActionHandler, you must disable the
 * initial check or they will never receive the requests.
 * </p>
 * <p>
 * You can set the parameter exceptionOnAuthentication to false to disable throwing
 * of exceptions when user fails to authenticate, or generally just when
 * doForwardRequest returns a value that the request should not be forwarded. The
 * default behaviour is to throw an exception which will then be handled by the 
 * previous context, or the root servlet, in some way, most likely as presenting 
 * an error of some kind to the user. If this behaviour is disabled, then
 * handleRequest will just simply return false and the previous context will try
 * other possible handlers. Thus you can have behaviour where this context handles
 * requests if the user is logged in, but if they aren't, it's not strictly an
 * error condition.
 * </p>

 * 
 * @author olli
 */


public class GenericContext extends AbstractModule implements ServletModule, ServletModule.RequestListener, ActionHandler  {

    protected ServletModule servlet;
    protected boolean checkActions=true;
    
    protected ActionExceptionHandler exceptionHandler=null;
    
    protected boolean exceptionOnAuthentication=true;
    protected String authenticationErrorMessage="Authentication error";
    
    protected String urlPrefix;
    protected String contextPath;
    
    protected final ParallelListenerList<RequestListener> requestListeners=new ParallelListenerList<RequestListener>(RequestListener.class);
    
    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        requireLogging(manager, deps);
        ServletModule m=manager.requireModule(this, ServletModule.class, deps);
        if(m==this) throw new ModuleException("Got itself as the servlet module. Modules are misconfigured.");
        
        manager.optionalModule(this, ActionExceptionHandler.class, deps);
        
        return deps;
    }

    @Override
    public void init(ModuleManager manager, Map<String, Object> settings) throws ModuleException {
        Object o=settings.get("checkActions");
        if(o!=null && o.toString().equalsIgnoreCase("false")) checkActions=false;
        
        o=settings.get("exceptionOnAuthentication");
        if(o!=null) exceptionOnAuthentication=Boolean.parseBoolean(o.toString());
        
        o=settings.get("authenticationErrorMessage");
        if(o!=null) authenticationErrorMessage=o.toString();
        
        o=settings.get("contextDir");
        if(o!=null) {
            urlPrefix=o.toString().trim();
            if(!urlPrefix.endsWith(File.separator)) urlPrefix+=File.separator;
            
            contextPath=urlPrefix;
            
            if(!File.separator.equals("/")) urlPrefix=urlPrefix.replace(File.separator, "/");            
        }
        
        o=settings.get("urlPrefix");
        if(o!=null) urlPrefix=o.toString().trim();
        
        o=settings.get("contextPath");
        if(o!=null) {
            contextPath=o.toString().trim();
            if(!contextPath.endsWith("/")) contextPath+="/";
        }
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        servlet=manager.findModule(this, ServletModule.class);
        if(servlet==this) throw new ModuleException("Got itself as the servlet module. Modules are misconfigured.");
        
        servlet.addRequestListener(this);
        
        exceptionHandler=manager.findModule(this, ActionExceptionHandler.class);
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        servlet.removeRequestListener(this);
        servlet=null;
        
        super.stop(manager);
    }

    @Override
    public void addRequestListener(RequestListener listener) {
        requestListeners.addListener(listener);
    }

    @Override
    public void removeRequestListener(RequestListener listener) {
        requestListeners.removeListener(listener);
    }

    @Override
    public String getServletURL() {
        if(servlet==null) return null;
        else return servlet.getServletURL()+urlPrefix;
    }

    @Override
    public String getContextPath() {
        if(contextPath!=null) {
            if(contextPath.startsWith("/")) return contextPath;
            else return servlet.getContextPath()+contextPath;
        }
        else return servlet.getContextPath();
    }

    
    private HttpServletRequest wrapRequest(final HttpServletRequest req){
        if(urlPrefix==null) return req;
        
        String prefix=urlPrefix;
        
        String request=req.getRequestURI();
        if(!prefix.startsWith("/") && request.startsWith("/")) prefix="/"+prefix;
        if(!request.endsWith("/") && prefix.endsWith("/") && (request+"/").equals(prefix)) 
            prefix=prefix.substring(0, prefix.length()-1);
        
        if(!request.startsWith(prefix)) return null;
        final String fPrefix=prefix;
     
        return new HttpServletRequestWrapper(req){
            @Override
            public String getContextPath() {
                return fPrefix;
            }
        };        
    }
    
    /**
     * <p>
     * Checks if a request should be forwarded to the registered listeners.
     * The return value is in the form of a ForwardResult object. This combines
     * multiple things about how the request can be handled. See its documentation
     * for more detail.
     * </p>
     * <p>
     * The default implementation simply returns
     * new ForwardResult(true, false, null), thus forwarding all requests without
     * any additional handling.
     * </p>
     * 
     * @param req The HTTP request.
     * @param resp The HTTP response.
     * @param method The HTTP method.
     * @return A ForwardResult object indicating what to do, or what has been done,
     *          with the request.
     * @throws ServletException
     * @throws IOException
     * @throws ActionException 
     */
    protected ForwardResult doForwardRequest(final HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method) throws ServletException, IOException, ActionException {
        if(urlPrefix!=null){
            ForwardResult ret=new ForwardResult(true, false, null);
            ret.request=wrapRequest(req);
            if(ret.request==null) return null;
            return ret;
        }
        else return new ForwardResult(true, false, null);
    }
    
    @Override
    public boolean handleRequest(HttpServletRequest request, HttpServletResponse response, ModulesServlet.HttpMethod method, final User user) throws ServletException, IOException, ActionException {
        try{
            if(checkActions){
                if(!isHandleAction(request, response, method)) return false;
            }

            ForwardResult fres=doForwardRequest(request, response, method);
            if(!fres.forward){
                if(exceptionOnAuthentication && !fres.responded) {
                    // For more specific messages, throw this exception in doForwardRequest
                    // of your subclass.
                    throw new ActionAuthenticationException(authenticationErrorMessage,this,fres.user);
                }
                return fres.responded;
            }

            final User authenticatedUser=(fres.user!=null?fres.user:user);
            final ServletException[] se=new ServletException[1];
            final ActionException[] ae=new ActionException[1];
            final IOException[] ioe=new IOException[1];
            final boolean[] handledA=new boolean[]{false};
            final HttpServletRequest req=(fres.request!=null?fres.request:request);
            final HttpServletResponse resp=(fres.response!=null?fres.response:response);
            final ModulesServlet.HttpMethod meth=(fres.method!=null?fres.method:method);

            requestListeners.forEach(new ListenerList.EachDelegate<RequestListener>(){
                private boolean handled=false;
                @Override
                public void run(RequestListener listener, Object... params) {
                    if(handled) return;
                    try{
                        handled=listener.handleRequest(req, resp, meth, authenticatedUser);
                        if(handled) handledA[0]=true;
                    }
                    catch(ServletException ex){
                        handled=true;
                        se[0]=ex;
                    }
                    catch(IOException ex){
                        handled=true;
                        ioe[0]=ex;
                    }
                    catch(ActionException ex){
                        handled=true;
                        ae[0]=ex;
                    }
                }
            });
            if(se[0]!=null) throw se[0];
            else if(ioe[0]!=null) throw ioe[0];
            else if(ae[0]!=null) throw ae[0];
            
            return handledA[0];
        }
        catch(ActionException ae){
            if(exceptionHandler!=null) {
                return exceptionHandler.handleActionException(request, response, method, user, ae);
            }
            else throw ae;
        }
    }
    
    /**
     * <p>
     * Gets the Action that will probably handle the given request.
     * Note that this only returns the first action that gets the opportunity to handle the request
     * that claims that it can handle it. The action might later decide not to handle the request 
     * in which case the request is passed to the next action that claims it can handle the request.
     * So this method is really only a guess at what action will probably handle the request.
     * </p>
     * <p>
     * Furthermore, this check depends on actions implementing the ActionHandler
     * interface. Actions in general are not required to do this, although it is
     * recommended that they do. If they don't, this method will just skip over
     * them and never return any such action. However, when the request is actually
     * handled by this context, even actions not implementing ActionHandler will
     * be offered a chance to handle it. Thus it could end up handling the request,
     * even before the action returned by this method.
     * </p>
     * 
     * @param req The HTTP request.
     * @param resp The HTTP response.
     * @param method The method of the HTTP request.
     * @return The action that will first get the opportunity to handle the request
     *          and that claims that it will try to handle it.
     */
    public ActionHandler getHandlingAction(final HttpServletRequest req, final HttpServletResponse resp, final ModulesServlet.HttpMethod method) {
        final ActionHandler[] ret=new ActionHandler[]{null};
        requestListeners.forEach(new ListenerList.EachDelegate<RequestListener>(){
            @Override
            public void run(RequestListener listener, Object... params) {
                if(ret[0]!=null) return;
                if(listener instanceof ActionHandler){
                    if(((ActionHandler)listener).isHandleAction(req, resp, method)){
                        ret[0]=(ActionHandler)listener;
                    }
                }
            }
        });
        return ret[0];
        
    }
    
    @Override
    public boolean isHandleAction(final HttpServletRequest req, final HttpServletResponse resp, final ModulesServlet.HttpMethod method) {
        if(!checkActions) return true;
        HttpServletRequest wrappedReq=wrapRequest(req);
        if(wrappedReq==null) return false;
        return getHandlingAction(wrappedReq, resp, method)!=null;
    }
    
    /**
     * <p>
     * A helper class to contain information about forwarding a request
     * to actions. It has three fields. The forward field simply indicates
     * whether the request should be forwarded on. The responded field
     * indicates whether a response has already been sent to the user. This
     * could be the case, for example, if a login page is sent as a reply and 
     * the request should not be forwarded until the user has logged in. Finally,
     * the user field contains the logged in user, or null if not relevant or not
     * logged in.
     * </p>
     * <p>
     * All three parameters can be passed straight to the constructor in the order
     * forward, responded, user. There is also a two-parameter constructor without
     * the user, which will then be set to null. A normal forward with no other
     * handling would be new ForwardResult(true,false,null) and normal blocking
     * new ForwardResult(false,false,null). If you resolved a user, add it as
     * the last parameter in case the login attempt succeeded.
     * </p>
     */
    public static class ForwardResult {
        public ForwardResult(){}
        public ForwardResult(boolean forward,boolean responded){
            this(forward, responded, null);
        }
        public ForwardResult(boolean forward,boolean responded,User user){
            this.forward=forward;
            this.responded=responded;
            this.user=user;
        }
        /**
         * Indicates if the request should be forwarded on to other actions.
         */
        public boolean forward;
        /**
         * Indicates if a reply has already been sent to the request. For
         * example, you could decide not to forward the request, and set
         * forwarded=false, and then send a login page to give the user a chance
         * to login. Thus you would set responded=true because you already sen
         * a response. This will prevent an error message from being sent.
         */
        public boolean responded;
        /**
         * The user that's logged in. You should somehow resolve this from
         * the authentication details in the request and then set the field.
         * If authentication is not relevant, or the user failed to login, set
         * to null.
         */
        public User user;
        
        /**
         * Overwritten request object. If non-null, should use this as the
         * request object when forwarding the request.
         */
        public HttpServletRequest request;
        /**
         * Overwritten response object. If non-null, should use this as the
         * response object when forwarding the request.
         */
        public HttpServletResponse response;
        /**
         * Overwritten method. If non-null, should use this as the
         * method when forwarding the request.
         */
        public ModulesServlet.HttpMethod method;
    }
    
}
