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
 */

package org.wandora.application.tools.extractors.facebook.v2;

import com.restfb.types.FacebookType;
import com.restfb.types.Page;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen
 */


public class PageWrapper extends AbstractFBTypeWrapper {

    private static final String SI_BASE = AbstractFBGraphExtractor.SI_BASE + "page/";

    
    private final Page page;
    PageWrapper(Page p){
        page = p;
    }
    
    @Override
    FacebookType getEnclosedEntity() {
        return page;
    }

    @Override
    String getType() {
        return "Page";
    }

    @Override
    String getSIBase() {
        return SI_BASE;
    }

    @Override
    Topic mapToTopicMap(TopicMap tm) throws TopicMapException {
        
        // ToDo, handle Page specific fields
        // See http://restfb.com/javadoc/com/restfb/types/Page.html
        
        return super.mapToTopicMap(tm);
    }
    
    
    
}
