/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.ModuleException;
import org.wandora.modules.ModuleManager;
import org.wandora.modules.servlet.ModulesServlet.HttpMethod;

/**
 * <p>
 * An action that makes it possible to chain together two, or more,
 * actions into one. This is useful if you have an action that already 
 * fits the situation but needs one little thing added to it. For example, you
 * could just use GenericTemplateAction, but you also want to send an email.
 * You can chain together a SendEmailAction and the GenericTemplateAction with
 * a ChainedAction. The ChainedAction is the action that receives the actual
 * HTTP request, it then passes it on to the first action in the chain, if the
 * action succeeded, it passes it on to the next action and so on. Note that
 * in most cases only one of the actions in the chain should write anything
 * in the response object.
 * </p>
 * <p>
 * You have other options as well to resolve the above use case. You could just
 * extend GenericTemplateAction to make the action you need. Or, without
 * extending the action, you could just pass in its template context a helper
 * object that can do the other things you need to be done and then do them in
 * the template.
 * </p>
 * <p>
 * To use this action, give the chained actions in an initialisation parameter
 * named chain as a semicolon separated list. Each action in the chain must be
 * named (using the name attribute in the module, like naming modules normally)
 * and then the names used in the chain parameter. Usually you don't specify
 * any action keys (the action or actions parameter) for the chained actions to
 * prevent them from being used directly outside the chain. Although in some
 * cases this could be desirable too.
 * </p>
 * 
 * @author olli
 */

public class ChainedAction extends AbstractAction {

    protected ArrayList<AbstractAction> chain;
    protected ArrayList<String> chainNames;
    
    @Override
    public boolean handleAction(HttpServletRequest req, HttpServletResponse resp, HttpMethod method, String action, org.wandora.modules.usercontrol.User user) throws ServletException, IOException, ActionException {
        for(AbstractAction a : chain){
            boolean b=a.handleAction(req, resp, method, action, user);
            if(!b) return false;
        }
        return true;
    }

    @Override
    public Collection<Module> getDependencies(ModuleManager manager) throws ModuleException {
        Collection<Module> deps=super.getDependencies(manager);
        
        requireLogging(manager, deps);
        
        for(String s : chainNames){
            manager.requireModule(s, AbstractAction.class, deps);
        }
        
        return deps;
    }

    @Override
    public void init(ModuleManager manager, HashMap<String, Object> settings) throws ModuleException {
        chainNames=new ArrayList<String>();
        
        Object o=settings.get("chain");
        if(o!=null){
            String[] s=o.toString().split(";");
            for(int i=0;i<s.length;i++){
                String n=s[i].trim();
                if(n.length()>0) chainNames.add(n);
            }
        }
        
        super.init(manager, settings);
    }

    @Override
    public void start(ModuleManager manager) throws ModuleException {
        chain=new ArrayList<AbstractAction>();

        for(String s : chainNames){
            chain.add(manager.findModule(s, AbstractAction.class));
        }        
        
        if(chain.isEmpty()) logging.warn("Chain is empty");
        
        super.start(manager);
    }

    @Override
    public void stop(ModuleManager manager) {
        chain=new ArrayList<AbstractAction>();
        
        super.stop(manager);
    }
    
}
