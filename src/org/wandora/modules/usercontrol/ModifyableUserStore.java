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
package org.wandora.modules.usercontrol;

/**
 * An interface that adds to the basic UserBase interface methods which
 * allow users to be modified.
 * 
 * @author olli
 */


public interface ModifyableUserStore extends UserStore {
    /**
     * Creates a new user in the user store. More information about the user
     * can then be added using the returned user object. Will throw an exception
     * if a user with that name already exists, or if the user cannot be created
     * for any other reason.
     * 
     * @param user The user name of the new user.
     * @return A user object representing the new user. 
     * @throws UserStoreException 
     */
    public User newUser(String user) throws UserStoreException;
    /**
     * Saves a user which originates from this database. Note that you
     * cannot give just any user object, the parameter must be a user object
     * you originally got from this user store. Use getUser to get an existing
     * user or newUser to create a new one.
     * @param user The user object to save.
     * @return True if the operation succeeded.
     * @throws UserStoreException 
     */
    public boolean saveUser(User user) throws UserStoreException;
    /**
     * Deletes a user from this user store.
     * @param user The user name of the user to delete.
     * @return True if the operation succeeded.
     * @throws UserStoreException 
     */
    public boolean deleteUser(String user) throws UserStoreException;
}
