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
 */

package org.wandora.application.tools.extractors.mediawikiapi;

import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;

import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;

import org.wandora.utils.language.LanguageBox;

/**
 *
 * @author Eero
 */

abstract class AbstractMediaWikiAPIExtractor extends AbstractExtractor {
    
    protected static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
    protected static final String SI_ROOT = "http://wandora.org/si/mediawiki/api/";
    protected static final String PAGE_SI = SI_ROOT + "page/";
    protected static final String CONTENT_TYPE_SI = SI_ROOT + "content/";
    
    // -------------------------------------------------------------------------

    protected static Topic getMediaWikiClass(TopicMap tm) throws TopicMapException {
        Topic reddit = getOrCreateTopic(tm, SI_ROOT, "MediaWiki API");
        makeSubclassOf(tm, reddit, getWandoraClassTopic(tm));
        return reddit;
    }
    
    protected static Topic getWandoraClassTopic(TopicMap tm)
            throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si)
            throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn)
            throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass)
            throws TopicMapException {

        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        
        return getOrCreateTopic(tm, LANG_SI);
    }
    
    protected static Topic getLangTopic(TopicMap tm, String lang) throws TopicMapException{
        Topic t = tm.getTopic(XTMPSI.getLang(lang));
        if(t == null){
            t = LanguageBox.createTopicForLanguageCode(lang, tm);
        }
        return t;
    }
    
    protected static Topic getContentTypeTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, CONTENT_TYPE_SI, "Content (MediaWiki API)");
    }
    
    // -------------------------------------------------------------------------
    
   
}
