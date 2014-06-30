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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * The primary exception type for Actions, or any RequestListener, to
 * throw when handling an action. GenericContext provides some mechanisms to
 * add exception handlers for this type of exception.
 * </p>
 *
 * @author olli
 */


public class ActionException extends Exception {

    protected ActionHandler action;

    public ActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ActionException(Throwable cause) {
        super(cause);
    }

    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActionException(String message) {
        super(message);
    }

    public ActionException() {
    }

    public ActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,ActionHandler action) {
        super(message, cause, enableSuppression, writableStackTrace);
        setAction(action);
    }

    public ActionException(Throwable cause,ActionHandler action) {
        super(cause);
        setAction(action);
    }

    public ActionException(String message, Throwable cause,ActionHandler action) {
        super(message, cause);
        setAction(action);
    }

    public ActionException(String message,ActionHandler action) {
        super(message);
        setAction(action);
    }

    public ActionException(ActionHandler action) {
        setAction(action);
    }
    
    /**
     * Gets the action where the exception occurred.
     * @return The action.
     */
    public ActionHandler getAction() {
        return action;
    }

    /**
     * Sets the action where this exception occurred.
     * @param action The action.
     */
    public void setAction(ActionHandler action) {
        this.action = action;
    }

    
}
