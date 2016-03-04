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

package org.wandora.modules.usercontrol;

import java.util.Collection;
import org.wandora.modules.Module;

/**
 * The base interface for a UserStore. Has basic methods to retrieve users
 * and nothing else. ModifyableUserStore extends this interface with methods
 * that allow users to be also modified. Any user store which is not modifyable
 * is assumed to be set up and modified by editing some files while the user store
 * is not running.
 *
 * @author olli
 */


public interface UserStore extends Module {
    
    /**
     * Finds a user with a user name.
     * @param user The user name.
     * @return A user object which contains more details about the user, or null
     *          if the user is not found.
     * @throws UserStoreException 
     */
    public User getUser(String user) throws UserStoreException;
    /**
     * Returns a collection of all users. Note that in some cases the collection
     * could be very big, or some implementations may simply not implement this
     * method at all.
     * @return A collection containing all users.
     * @throws UserStoreException 
     */
    public Collection<User> getAllUsers() throws UserStoreException;
    /**
     * Finds users based on a property value. Since there are no uniqueness
     * constraints with any of the properties in general, may return multiple
     * users.
     * @param key The property key used for the search.
     * @param value The value of the property.
     * @return A collection of users matching the property, or an empty collection
     *          if none matched.
     * @throws UserStoreException 
     */
    public Collection<User> findUsers(String key,String value) throws UserStoreException;
    
}
