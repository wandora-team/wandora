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

import org.wandora.modules.usercontrol.User;

/**
 *
 * @author olli
 */


public class ActionAuthenticationException extends ActionException {

    protected User user;
    
    public ActionAuthenticationException(ActionHandler action) {
        super(action);
    }

    public ActionAuthenticationException(String message, ActionHandler action) {
        super(message, action);
    }

    public ActionAuthenticationException(String message, Throwable cause, ActionHandler action) {
        super(message, cause, action);
    }

    public ActionAuthenticationException(Throwable cause, ActionHandler action) {
        super(cause, action);
    }

    public ActionAuthenticationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ActionHandler action) {
        super(message, cause, enableSuppression, writableStackTrace, action);
    }

    public ActionAuthenticationException() {
    }

    public ActionAuthenticationException(String message) {
        super(message);
    }

    public ActionAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionAuthenticationException(Throwable cause) {
        super(cause);
    }

    public ActionAuthenticationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public ActionAuthenticationException(String message,User user) {
        super(message);
        this.user=user;
    }

    public ActionAuthenticationException(String message, Throwable cause, User user) {
        super(message, cause);
        this.user=user;
    }
    
    public ActionAuthenticationException(String message, ActionHandler action,User user) {
        super(message, action);
        this.user=user;
    }
    
    
    public User getUser(){return user;}
    public void setUser(User user){this.user=user;}
}
