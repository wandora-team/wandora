/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2016 Wandora Team
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
package org.wandora.modules.usercontrol;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wandora.modules.Module;
import org.wandora.modules.servlet.ModulesServlet;

/**
 * <p>
 * The base interface for authenticators. Authenticators authenticate a
 * user based on the received HTTP request. They may also hijack the request and
 * even reply to it, for example to send back a login form. By completing the
 * login form, the user will send the required login details to the authenticator
 * and then it lets the user access other features.
 * </p>
 * <p>
 * The result of the authentication is returned as an AuthenticationResult object.
 * This has three fields. The authenticated indicates tells whether authentication
 * succeeded. The responded field indicates whether a response was sent in case
 * the authentication didn't succeed. The response could be an error message or
 * a login form or something similar. Finally the user field contains the
 * authenticated user. The user field may be set even if authentication fails.
 * This would be the case if the user provided a correct user name and password
 * but the user doesn't have the required privileges. Also, the user is not
 * necessarily set even if authentication succeeds. This would be the case if
 * anonymous was allowed.
 * </p>
 *
 * @author olli
 */


public interface UserAuthenticator extends Module {

    /**
     * <p>
     * Authenticates a user. If requiredRole is non-null, the logged in user
     * must be of that role for the authentication to succeed. Otherwise there
     * are two possible options in how to implement the authentication. It may
     * be required that the user provides valid login details for authentication
     * to succeed. Or it could be that anonymous logins are also authorised and
     * the authentication succeeds without the user field set in the result.
     * What exactly happens is implementation specific, possibly even dependent
     * on the authenticator initialisation parameters.
     * </p>
     * 
     * @param requiredRole The role the user should have or null if no role is required.
     * @param req The HTTP request.
     * @param resp The HTTP response.
     * @param method The method of the HTTP request.
     * @return An AuthenticationResult what happened with the authentication.
     * @throws IOException
     * @throws AuthenticationException 
     */
    public AuthenticationResult authenticate(String requiredRole, HttpServletRequest req, HttpServletResponse resp, ModulesServlet.HttpMethod method) throws IOException, AuthenticationException ;
    
    /**
     * A class containing information about the authentication. See
     * UserAuthenticator documentation for details.
     */
    public static class AuthenticationResult {

        public AuthenticationResult(boolean authenticated, User user, boolean responded) {
            this.authenticated=authenticated;
            this.user=user;
            this.responded=responded;
        }
        
        public boolean authenticated=false;
        public User user=null;
        public boolean responded=false;
    }
}
