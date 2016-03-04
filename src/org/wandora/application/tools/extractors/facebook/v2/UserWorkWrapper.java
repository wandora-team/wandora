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

import com.restfb.types.NamedFacebookType;
import com.restfb.types.User;
import static org.wandora.application.tools.extractors.facebook.v2.AbstractFBWrapper.getOrCreateType;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


public class UserWorkWrapper extends AbstractFBWrapper {

    private static final String SI_BASE = AbstractFBGraphExtractor.SI_BASE + "work/";

    private final User.Work work;
    UserWorkWrapper(User.Work work){
        this.work = work;
    }

    @Override
    String getType() {
        return "Work";
    }

    @Override
    String getSIBase() {
        return SI_BASE;
    }
    
    @Override
    public Topic mapToTopicMap(TopicMap tm) throws TopicMapException {
        
        Topic langTopic = getLangTopic(tm);

        
        Topic workTopic = super.mapToTopicMap(tm);
        
        Topic workType = getOrCreateType(tm, "Work");
        
        
        final String[][] occurrenceData = {
            {"startDate", work.getStartDate() != null ? work.getStartDate().toString() : null},
            {"endDate",   work.getEndDate() != null ? work.getEndDate().toString() : null},
            {"description", work.getDescription()}
        };
                
        for (String[] datum : occurrenceData) {
            Topic type = getOrCreateType(tm, datum[0]);
            workTopic.setData(type, langTopic, datum[1]);
        }
        
        NamedFacebookType location = work.getLocation();
        associateNamedType(tm, location, "Location", workTopic, workType);
        
        NamedFacebookType employer = work.getEmployer();
        associateNamedType(tm, employer, "Employer", workTopic, workType);
        
        NamedFacebookType position = work.getPosition();
        associateNamedType(tm, position, "Position", workTopic, workType);
        
        return workTopic;
    }
    
    
}
