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
 * 
 * FindSubjectLocatorWithBasename.java
 *
 * Created on 30. toukokuuta 2006, 14:02
 *
 */

package org.wandora.application.tools.subjects;


import org.wandora.application.contexts.*;
import org.wandora.topicmap.*;


/**
 * Tool is used to locate missing subject locators that can be recognized with
 * the topic base name. Tool crawls web pages and matches each found URL to
 * context base names. If matching URL is found the topic is given URL as a
 * subject locator.
 *
 * This tool class extends <code>FindSubjectLocator</code>.
 *
 * @author akivela
 */


public class FindSubjectLocatorWithBasename extends FindSubjectLocator {
    
    /** Creates a new instance of FindSubjectLocatorWithBasename */
    public FindSubjectLocatorWithBasename() {
    }
    public FindSubjectLocatorWithBasename(Context preferredContext) {
        setContext(preferredContext);
    }
    
    
    public String getName() {
        return "Find subject locator with base name";
    }
    public String getDescription() {
        return "Looks for a real URL resource with topic base name as the pattern and sets found URL as a subject locator of topic.";
    }
    
    
    
    /**
     * Method solves search pattern for each topic. Search pattern is compared
     * to all found URL resources. If crawler finds URL resource matching the
     * search pattern, the URL is given to topic as a subject locator.
     * <code>FindSubjectLocatorWithBasename</code> uses topic's base name as the
     * search pattern.
     */
    public String solveURLPattern(Topic topic) {
        try {
            return topic.getBaseName();
        }
        catch(Exception e) {}
        return null;
    }
    
    
    
    
}
