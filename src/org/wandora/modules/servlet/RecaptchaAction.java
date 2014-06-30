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
 */package org.wandora.modules.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.usercontrol.User;

/**
 *
 * @author olli
 */


public class RecaptchaAction extends AbstractAction {

    protected String privateKey;
    protected String publicKey;
    
    protected AbstractAction errorAction;
    protected String errorActionName;   
    protected AbstractAction successAction;
    protected String successActionName;   
    
    public ReCaptcha makeReCaptcha(){
        return ReCaptchaFactory.newReCaptcha(publicKey, privateKey, false);
    }
    
    public String createFormHtml(){
        ReCaptcha rc=makeReCaptcha();
        return rc.createRecaptchaHtml(null, null);
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager); 
        if(errorActionName!=null) manager.requireModule(this, errorActionName, AbstractAction.class, deps);
        if(successActionName!=null) manager.requireModule(this, successActionName, AbstractAction.class, deps);
        return deps;
    }
    

    @Override
    public void stop(ModuleManager manager) {
        errorAction=null;
        successAction=null;
        
        super.stop(manager); 
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        if(publicKey==null) throw new ModuleException("publicKey is null");
        if(privateKey==null) throw new ModuleException("privateKey is null");

        if(errorActionName!=null) errorAction=manager.findModule(this, errorActionName, AbstractAction.class);
        if(successActionName!=null) successAction=manager.findModule(this, successActionName, AbstractAction.class);
        
        super.start(manager);
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        Object o=settings.get("privateKey");
        if(o!=null) privateKey=o.toString().trim();
        
        o=settings.get("publicKey");
        if(o!=null) publicKey=o.toString().trim();
        
        o=settings.get("errorAction");
        if(o!=null) errorActionName=o.toString().trim();

        o=settings.get("successAction");
        if(o!=null) successActionName=o.toString().trim();
        
        super.init(manager, settings); 
    }
    
    public boolean validateCaptcha(HttpServletRequest request){
        String remoteAddr = request.getRemoteAddr();
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey(privateKey);

        String challenge = request.getParameter("recaptcha_challenge_field");
        String uresponse = request.getParameter("recaptcha_response_field");
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);

        return reCaptchaResponse.isValid();
    }
    
    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, String action, User user) throws ServletException, IOException, ActionException {
        if(validateCaptcha(req)){
            if(successAction!=null) return successAction.handleAction(req, resp, method, action, user);
            else return true;
        }
        else {
            if(errorAction!=null) return errorAction.handleAction(req, resp, method, action, user);
            else return false;
        }
    }
    
}
