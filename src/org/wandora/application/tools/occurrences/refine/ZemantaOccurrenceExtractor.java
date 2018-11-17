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
 */



package org.wandora.application.tools.occurrences.refine;

import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.extractors.zemanta.ZemantaExtractor;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author akivela
 */
public class ZemantaOccurrenceExtractor extends AbstractOccurrenceExtractor {


	private static final long serialVersionUID = 1L;







	public ZemantaOccurrenceExtractor() {
    }
    public ZemantaOccurrenceExtractor(Context preferredContext) {
        super(preferredContext);
    }




    @Override
    public String getName() {
        return "Zemanta occurrence extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts terms out of given occurrences using Zemanta web service.";
    }







    public boolean _extractTopicsFrom(String occurrenceData, Topic masterTopic, TopicMap topicMap, Wandora wandora) throws Exception {
        if(occurrenceData != null && occurrenceData.length() > 0) {
            ZemantaExtractor extractor = new ZemantaExtractor();
            extractor.setToolLogger(getDefaultLogger());
            if(masterTopic != null) extractor.setMasterSubject(masterTopic);
            extractor._extractTopicsFrom(occurrenceData, topicMap);
        }
        return true;
    }


}
