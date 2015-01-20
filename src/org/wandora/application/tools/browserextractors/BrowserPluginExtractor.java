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

package org.wandora.application.tools.browserextractors;
import org.wandora.application.Wandora;
import org.wandora.topicmap.*;
/**
 *
 * @author olli
 */
public interface BrowserPluginExtractor {
    public static final String RETURN_INFO="INFO ";
    public static final String RETURN_ERROR="ERROR ";
    
    public boolean acceptBrowserExtractRequest(BrowserExtractRequest request, Wandora wandora) throws TopicMapException ;
    public String getBrowserExtractorName();
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException ;
}
