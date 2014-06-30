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
 * TopicFilter.java
 *
 * Created on 19. heinäkuuta 2005, 9:26
 */

package org.wandora.piccolo.actions;
import org.wandora.topicmap.*;
import java.util.Collection;
/**
 *
 * @author olli
 */
public interface TopicFilter {
    
    public boolean topicVisible(Topic t) throws TopicMapException ;
    public boolean associationVisible(Association a) throws TopicMapException ;
    public TopicFilter makeNew(Object request) throws TopicMapException ;
    public Collection filterTopics(Collection topics) throws TopicMapException ;
    public Collection filterAssociations(Collection associations) throws TopicMapException ;
    public String getFilterCacheKey();
}
