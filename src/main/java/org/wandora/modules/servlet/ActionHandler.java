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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * This interface adds a feature to Actions needed by all the various
 * contexts. The ServletModule.RequestListener is a little too generic
 * for more complicated handler resolution. Thus actions should generally try
 * to also implement this interface. Actions that don't implement this interface
 * cannot be used in context derived from GenericContext unless the checkActions
 * parameter of the context is set to false.
 * 
 * @author olli
 */


public interface ActionHandler {
    /**
     * Checks if this action handler will try to handle the given action
     * without handling it yet or causing any side effects. Returning true
     * means that the action handler will try to handle it, not necessarily that
     * the action will succeed. You may also return false from the handleRequest
     * method even if you return true here. The return value should be your best
     * guess as to whether you will handle it without spending too much time
     * checking.
     * 
     * 
     * @param req The HTTP request.
     * @param resp The HTTP response.
     * @param method The HTTP method of the request.
     * @return 
     */
    public boolean isHandleAction(HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method);
}
