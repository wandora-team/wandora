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

import com.restfb.types.Category;
import com.restfb.types.FacebookType;
import com.restfb.types.Location;
import com.restfb.types.Place;
import java.util.List;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen
 */


public class PlaceWrapper extends AbstractFBTypeWrapper {

    private static final String SI_BASE = AbstractFBGraphExtractor.SI_BASE + "place/";

    
    private final Place place;
    
    PlaceWrapper(Place place){
        this.place = place;
    }
    
    @Override
    public FacebookType getEnclosedEntity() {
        return this.place;
    }

    @Override
    public Topic mapToTopicMap(TopicMap tm) throws TopicMapException {
        
        Topic placeTopic = super.mapToTopicMap(tm);
        
        Topic placeType = getOrCreateType(tm, getType());
        
        List<Category> categories = place.getCategoryList();
        for(Category c: categories){
            CategoryWrapper categoryWrapper = new CategoryWrapper(c);
            Topic categoryTopic = categoryWrapper.mapToTopicMap(tm);
            Topic categoryType = getOrCreateType(tm, "Category");
            
            Association a = tm.createAssociation(categoryType);
            a.addPlayer(placeTopic, placeType);
            a.addPlayer(categoryTopic, categoryType);
            
        }
        
        Location location = place.getLocation();
        LocationWrapper locationWrapper = new LocationWrapper(location);
        Topic locationTopic = locationWrapper.mapToTopicMap(tm);
        Topic locationType = getOrCreateType(tm, "Location");
        
        Association a = tm.createAssociation(locationType);
        a.addPlayer(placeTopic, placeType);
        a.addPlayer(locationTopic, locationType);

        return placeTopic;
    }

    @Override
    public String getType() {
        return "Place";
    }

    @Override
    public String getSIBase() {
        return SI_BASE;
    }
    
}
