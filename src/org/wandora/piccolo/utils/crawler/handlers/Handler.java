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
 *
 * 
 *
 * Handler.java
 *
 * Created on December 5, 2001, 4:27 PM
 */

package org.wandora.piccolo.utils.crawler.handlers;
import org.wandora.piccolo.utils.crawler.*;
import java.net.URL;
import java.io.InputStream;
/**
 * 
 *
 * @author  olli
 */
public interface Handler {
    
    
    
    /**
     * Processes the given page.
     * The given <code>InputStream</code> contains the data of an object that is
     * of the content-type this content handler accepts. May use the given
     * <code>CrawlerAccess</code> object to add further pages to the queue of the
     * <code>WebCrawler</code> that asked to process the page. 
     *
     * @param crawler The call back object for the handler. Any objects built from
     * the content of the page can be sent to this.
     * @param in The <code>InputStream</code> of the page.
     * @param depth The depth remaining depth. When reporting another page to
     * the queue, the depth of that page should be set to this depth-1.
     * @param page The <code>URL</code> of the page.
     */
    public void handle(CrawlerAccess crawler,InputStream in,int depth,URL page);
    
    
    
    /**
     * Returns an array of String containing the content-types this
     * <code>ContentHandler</code> can process.
     */
    public String[] getContentTypes();
}

