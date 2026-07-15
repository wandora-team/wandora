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

import java.io.IOException;

import org.wandora.modules.Module;
import org.wandora.modules.usercontrol.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A module that hooks to a servlet, or something similar. The typical
 * case is where it is an actual servlet from which HTTP requests are received.
 * RequestListeners can register themselves to the servlet module and then get
 * a chance to handle the requests.
 * 
 * @author olli
 */

public interface ServletModule extends Module {
    public void addRequestListener(ServletModule.RequestListener listener);
    public void removeRequestListener(ServletModule.RequestListener listener);
    /**
     * Returns the base URL for this servlet.
     * @return The base URL for this servlet.
     */
    public String getServletURL();
    /**
     * Returns the local path which this servlet should use as a basis
     * for files it needs to access.
     * @return A local path which should be the basis of local files.
     */
    public String getContextPath();
    
    /**
     * An interface for all RequestListeners which wish to be notified of
     * incoming requests.
     */
    public static interface RequestListener {
        /**
         * <p>
         * Handles the request, if this is the type of request the listener
         * is interested in. If the listener does not wish to handle the request,
         * it should return false. False return value indicates that the request
         * should be passed to other listeners, a true return value indicates
         * that this should not happen and a response to the request has been
         * sent. Thus it is possible that you perform some action and still
         * return false and let some other action write the response.
         * </p>
         * <p>
         * However,
         * keep in mind that some other listener may grab any request before
         * your listener and your listener will then never even see the request.
         * Thus, you will not necessarily be notified of every single request.
         * If this is your goal, you should probably make a separate context
         * that all requests will pass through before they are passed on to
         * other actions, see GenericContext.
         * </p>
         * 
         * @param req The HTTP request.
         * @param resp The HTTP response.
         * @param method The HTTP method of the request.
         * @param user The logged in user if applicable or null.
         * @return True if a response has been sent and no further listener should
         *          try to process the request. False if the request can be forwarded
         *          to other listeners for processing.
         * @throws ServletException
         * @throws IOException
         * @throws ActionException 
         */
        public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method, User user) throws ServletException, IOException, ActionException;
    }    
}
