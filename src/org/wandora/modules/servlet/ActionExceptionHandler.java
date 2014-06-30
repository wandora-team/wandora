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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.usercontrol.User;

/**
 * <p>
 * An interface for handlers that can take care of ActionExceptions
 * thrown when handling an HTTP request. By including a module that implements
 * this interface, GenericContext will automatically use it whenever an ActionException
 * inside it occurs. Note that the root servlet does not (necessarily) perform
 * similarly. If you want to include custom ActionException handling, you should
 * use a context for all your actions. GenericContext can be used as is for this
 * purpose.
 * </p>
 * <p>
 * See TemplateActionExceptionHandler for a generic implementation of this
 * using templates.
 * </p>
 * <p>
 * Implementations of this should not extends AbstractAction, or if they do,
 * they need to take special care about dependency handling. The reason is that
 * this will easily cause a circular dependency. AbstractAction will require a
 * ServletModule. ServletModules, which GenericContext implements, may then
 * optionally require an ActionExceptionHandler. If that is an AbstractAction,
 * it will again look for a ServletModule and so on. So if you do extend
 * AbstractAction, you will need to override getDependencies to break the
 * circular dependency problem.
 * </p>
 * 
 * @author olli
 */


public interface ActionExceptionHandler extends Module {
    /**
     * Handles an exception that occurred when handling an HTTP request.
     * The ActionException itself should contain the action where it occurred.
     * The return value will act as if the original action return it. In other
     * words, true indicates that the request was handled while false means that
     * the context, or root servlet, should continue giving other actions a chance
     * to handle it. Note that even returning false indicates that you have handled
     * the exception in some way. If you decide not to handle the exception at all,
     * rethrow it in the handler instead of returning either true or false. That,
     * or any other exception thrown, will not be caught by this same handler, 
     * but may be caught by another handler higher up in the context tree.
     * 
     * @param req The HTTP request.
     * @param resp The HTTP request where the response can be written.
     * @param method The HTTP method used with the request.
     * @param user The authenticated user, or null if not applicable.
     * @param ae The ActionException that caused this to be called.
     * @return True if the action has been handled, false if other actions should
     *          still try to handle it.
     * @throws ServletException
     * @throws IOException
     * @throws ActionException 
     */
    public boolean handleActionException(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, User user, ActionException ae) throws ServletException, IOException, ActionException  ;
}
