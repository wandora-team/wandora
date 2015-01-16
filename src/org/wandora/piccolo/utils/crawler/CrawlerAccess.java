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
 *
 * 
 *
 * CrawlerAccess.java
 *
 * Created on December 5, 2001, 4:28 PM
 */

package org.wandora.piccolo.utils.crawler;



/**
 * A call back interface for the <code>WebCrawler</code>. Whenever a
 * <code>ContentHandler</code> wants to add a page to the queue of the
 * <code>WebCrawler</code> it will call the methods of the <code>CrawlerAccess</code>
 * object it receives. You can set your own <code>CrawlerAccess</code> implementation
 * between the <code>ContentHandler</code> and the <code>WebCrawler</code> with
 * <code>WebCrawler.setCallBack</code>
 *
 * @author  olli
 */
public interface CrawlerAccess {
    /**
     * Adds an url to the queue of the crawler.
     */
    public void add(Object crawlObject, int depth);
    public void forceExit();
    public void setProperty(String key, Object value);
    
    /**
     * Gives any object constructed from the crawled page to the call back object.
     * The type of the given data can be anything and it is up to the
     * <code>CrawlerAccess</code> implementation to decide what to do with it.
     */
    public void addObject(Object data);
}

