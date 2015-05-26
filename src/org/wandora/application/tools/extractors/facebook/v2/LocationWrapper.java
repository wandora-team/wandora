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
 */

package org.wandora.application.tools.extractors.facebook.v2;

import com.restfb.types.Location;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


public class LocationWrapper extends AbstractFBWrapper {

    private static final String SI_BASE = AbstractFBGraphExtractor.SI_BASE + "location/";

    
    private final Location location;
    public LocationWrapper(Location l) {
        location = l;
    }

    @Override
    String getType() {
        return "Location";
    }

    @Override
    String getSIBase() {
        return SI_BASE;
    }

    @Override
    Topic mapToTopicMap(TopicMap tm) throws TopicMapException {

        Topic locationTopic = super.mapToTopicMap(tm);
        
        Topic langTopic = getLangTopic(tm);

        final String[][] occurrenceData = {
            {"city",      location.getCity()},
            {"country",   location.getCountry()},
            {"latitude",  Double.toString(location.getLatitude())},
            {"longiture", Double.toString(location.getLongitude())},
            {"state",     location.getState()},
            {"street",    location.getStreet()}
          };
        
        for (String[] datum : occurrenceData) {
            Topic type = getOrCreateType(tm, datum[0]);
            locationTopic.setData(type, langTopic, datum[1]);
        }
        
        return locationTopic;
    }
    
    
    
}
