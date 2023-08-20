/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * TopicMapListener.java
 *
 * Created on July 13, 2004, 9:54 AM
 */

package org.wandora.topicmap;
import java.util.Collection;
/**
 * A listener for various changes in a topic map.
 *
 * @author  olli
 */
public interface TopicMapListener {
    
/*    public void topicChanged(Topic t) throws TopicMapException ;
    public void topicRemoved(Topic t) throws TopicMapException ;
    public void associationChanged(Association a) throws TopicMapException ;
    public void associationRemoved(Association a) throws TopicMapException ;
*/
    /*
     * MergedTopicMap reports general changes with topicSubjectIdentifierChanged(topic,null,null) and
     * associationPlayerChanged(association,null,null,null).
     */
    
    /**
     * Notification that a subject identifier has changed. Either added or removed is non-null
     * and indicates that a subject identifier has been added or removed, respectively.
     * The non-null parameter contains the new subject identifier or the removed subject identifier.
     */
    public void topicSubjectIdentifierChanged(Topic t,Locator added,Locator removed) throws TopicMapException ;
    /**
     * Notification that the base name has been changed. Either newName or oldName may
     * be null indicating that base name was removed or old name was empty.
     */
    public void topicBaseNameChanged(Topic t,String newName,String oldName) throws TopicMapException ;
    /**
     * Notification that a topic type has changed. Either added or removed is non-null
     * and indicates that a type has been added or removed, respectively.
     * The non-null parameter contains the new type or the removed type.
     */
    public void topicTypeChanged(Topic t,Topic added,Topic removed) throws TopicMapException ;
    /**
     * Notification that a variant name has been changed. Either newName or oldName may
     * be null indicating that the variant name was removed or old name was empty.
     */
    public void topicVariantChanged(Topic t,Collection<Topic> scope,String newName,String oldName) throws TopicMapException ;
    /**
     * Notification that topic occurrence has been changed. Either newValue or oldValue may
     * be null indicating that the occurrence was removed or old value was empty.
     */
    public void topicDataChanged(Topic t,Topic type,Topic version,String newValue,String oldValue) throws TopicMapException ;
    /**
     * Notification that the subject locator has been changed. Either newLocator or oldLocator may
     * be null indicating that locator was removed or there was no old locator.
     */
    public void topicSubjectLocatorChanged(Topic t,Locator newLocator,Locator oldLocator) throws TopicMapException ;
    /**
     * Notification that a topic has been completely removed.
     */
    public void topicRemoved(Topic t) throws TopicMapException ;
    /**
     * A notification used to report general or large changes in topic, for example when topics are merged.
     * Such a large change may consist of any number of smaller changes that are NOT reported
     * individually with the other listener methods. This method is used when tracking
     * individual changes is hard to implement.
     */
    public void topicChanged(Topic t) throws TopicMapException ;
    
    /**
     * A notification that association type has changed. Either newType or oldType
     * can be null to indicate that old or new type was null. When type
     * is changed from one topic to another, both will be non-null.
     */
    public void associationTypeChanged(Association a,Topic newType,Topic oldType) throws TopicMapException ;
    /**
     * A notification that a player in an association with a certain role has
     * been changed. Either newPlayer or oldPlayer can be null to indicate that
     * the no new player for the role was set or that there was no player
     * with the role previously. Both can also be non-null to indicate that
     * the player of the role changed.
     */
    public void associationPlayerChanged(Association a,Topic role,Topic newPlayer,Topic oldPlayer) throws TopicMapException ;
    /**
     * A notification that an association has been completely removed.
     */
    public void associationRemoved(Association a) throws TopicMapException ;
    /**
     * A notification used to report general or large changes in association.
     * Such a large change may consist of any number of smaller changes that are NOT reported
     * individually with the other listener methods. This method is used when tracking
     * individual changes is hard to implement.
     */
    public void associationChanged(Association a) throws TopicMapException ;
}
