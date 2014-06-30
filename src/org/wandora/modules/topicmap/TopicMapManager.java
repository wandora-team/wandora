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
 */
package org.wandora.modules.topicmap;

import org.wandora.modules.Module;
import org.wandora.topicmap.TopicMap;

/**
 * <p>
 * The interface for a module that provides a topic map, and related
 * services, for other modules. Just providing access to the topic map
 * is the main function of this. In addition to that, there are some locking
 * mechanisms, but depending on the implementation, they may or may not actually
 * do anything. Implementation details may also vary but the idea is that any 
 * number of read locks can be acquired at the same time but write locks are
 * exclusive, no readers or other writers are allowed at the same time.
 * </p>
 * <p>
 * You can also add listeners that will be notified if the whole topic map 
 * is changed. Note that this is different to just changes in the topic map, the 
 * topic map itself has listener mechanisms for that. The listener you register
 * with the manager is notified when the entire topic map is replaced with another
 * topic map.
 * </p>
 * 
 * @author olli
 */


public interface TopicMapManager extends Module {
    /**
     * Locks the topic map for reading. You must eventually release
     * the lock with unlockRead if this method succeeds and returns true.
     * @return True if the locking succeeded, false otherwise.
     */
    public boolean lockRead();
    /**
     * Releases a previously acquired read lock. 
     */
    public void unlockRead();
    /**
     * Locks the topic map for writing. You must eventually release
     * the lock with unlockWrite if this method succeeds and returns true.
     * @return True if the locking succeeded, false otherwise.
     */
    public boolean lockWrite();
    /**
     * Releases a previously acquired write lock.
     */
    public void unlockWrite();
    
    /**
     * Returns the topic map managed by this topic map manager.
     * @return The topic map of this manager.
     */
    public TopicMap getTopicMap();
    
    /**
     * Adds a topic map listener that will be notified when the managed
     * topic map is replaced with another topic map.
     * @param listener The topic map listener.
     */
    public void addTopicMapManagerListener(TopicMapManagerListener listener);
    /**
     * Removes a topic map listener.
     * @param listener The listener.
     */
    public void removeTopicMapManagerListener(TopicMapManagerListener listener);
    
    /**
     * An interface for objects that want to be notified when the managed topic 
     * map is replaced with another topic map. This listener is not notified
     * of changes in the topic map itself, the topic map class has mechanisms for
     * that.
     */
    public interface TopicMapManagerListener {
        /**
         * The managed topic map has been replaced with another topic map.
         * @param old The old topic map.
         * @param neu The new topic map.
         */
        public void topicMapChanged(TopicMap old,TopicMap neu);
    }
}
