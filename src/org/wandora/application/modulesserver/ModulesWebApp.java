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
package org.wandora.application.modulesserver;

import org.wandora.modules.Module;

/**
 * An interface for modules that are web apps. A web app here means that
 * the module is usable in a meaningful way from a normal web browser. The
 * web app should have a name and usually also a start page of some kind. In
 * addition, it may also have a page for each topic, however this isn't always
 * the case.
 *
 * @author olli
 */


public interface ModulesWebApp extends Module {
    /**
     * Returns the name of the web app. 
     * @return The name of the web app.
     */
    public String getAppName();
    /**
     * Returns the URL for the start page of the web app. If the web app
     * doesn't have a meaningful start page, return null instead.
     * 
     * @return The start page of the web app.
     */
    public String getAppStartPage();
    /**
     * Returns the URL representing the topic with the given subject
     * identifier. If there is no meaningful page for the topic, return null.
     * This should not be dependent on whether the topic exists or not, the returned
     * page should be the URL if the topic is assumed to exist.
     * 
     * @param si The subject identifier of the topic.
     * @return The URL of the page representing the topic.
     */
    public String getAppTopicPage(String si);
}
