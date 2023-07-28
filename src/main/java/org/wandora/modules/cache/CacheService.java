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
package org.wandora.modules.cache;

import org.wandora.modules.Module;

/**
 * <p>
 * An interface for caching services. Pages, which can be any content really,
 * can be stored in the cache using cache keys. The key is just any string that 
 * identifies the cached page. The pages are then retrieved using the same keys.
 * In addition, timestamps in milliseconds can be used so that cached pages older
 * than a specified value are not reused.
 * </p>
 * <p>
 * It is vital that the InputStreams and OutputStreams returned by getPage and
 * storePage be properly closed in all cases, even if exceptions are encountered.
 * Failing to do so may cause the cache to remain in a locked state and other
 * threads be unable to use it.
 * </p>
 * <p>
 * See CachedAction for an abstract action that can take advantage of caching
 * services.
 * </p>
 * @author olli
 */


public interface CacheService extends Module {
    /**
     * Gets a cached version of a page if one exists in the cache. If
     * modifyTime parameter is greater than 0, the page must be never than the
     * time indicated by the parameter. If a cached version doesn't exist, or
     * it is too old, returns null.
     * 
     * @param key The key of the cached page.
     * @param modifyTime The minimum caching time of the page, or 0 if no such limit is used.
     * @return An input stream from which the stored page can be read. The 
     *          stream must be properly closed afterwards.
     */
    public java.io.InputStream getPage(String key,long modifyTime);
    /**
     * Stores a cached version of a page with the given time stamp.
     * 
     * @param key The key of the cached page.
     * @param modifyTime A timestamp for the cached version.
     * @return An output stream into which the cached version is written. The
     *          stream must be properly closed afterwards.
     */
    public java.io.OutputStream storePage(String key,long modifyTime);
}
