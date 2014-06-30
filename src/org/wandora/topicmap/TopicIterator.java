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
 */
package org.wandora.topicmap;

import java.util.Iterator;

/**
 *
 * An iterator with a mechanism to dispose of it cleanly. When iterating
 * topics, some implementations need to dispose of the iterator properly.
 * Previously this was done when the iterator had been completely iterated
 * through. But in some cases it may be desirable to stop iterating earlier and
 * going through the whole thing could be time consuming. This class is designed
 * to address this issue. Whenever a topic map implementation returns a TopicIterator,
 * its dispose method may be called to stop the iteration process early. If a
 * normal Iterator is returned, then it should be assumed that it must be iterated
 * completely or things might not be cleaned up properly.
 * 
 * @author olli
 */


public interface TopicIterator extends Iterator<Topic> {
    public void dispose();
}
