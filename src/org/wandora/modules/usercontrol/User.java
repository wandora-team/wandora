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
 */
package org.wandora.modules.usercontrol;

import java.util.Collection;

/**
 * <p>
 * A basis for users. This sets some very basic features all users objects
 * should have. The three main things are: a user name, a set of roles and a
 * set of other options and properties. The user name identifies the user and
 * should be unique. The set of roles are used to specify what types of actions
 * the user is allowed to perform. Everything else is stored in the generic
 * options map, which is a collection of key value pairs. 
 * </p>
 * <p>
 * Note that a password would be stored in the options map. This class does not
 * specify how or with what key it would be stored. That's up to whatever 
 * performs user authentication. Some might store it in plain text (usually not
 * a good idea) while some others could use different hashing methods to store it
 * and authenticate a user.
 * </p>
 * <p>
 * You typically get users from a UserStore. Any changes to the user object can
 * also be saved to the store where the user originally came from with the
 * saveUser method.
 * </p>
 *
 * @author olli
 */


public abstract class User {
    /**
     * Gets the user name.
     * @return The user name.
     */
    public abstract String getUserName();
    /**
     * Gets one of the stored options.
     * @param optionKey The key of the property.
     * @return The value of the property, or null if it doesn't exist.
     */
    public abstract String getOption(String optionKey);
    /**
     * Gets all stored option keys. 
     * @return A collection containing all stored option keys.
     */
    public abstract Collection<String> getOptionKeys();
    /**
     * Sets a stored option.
     * @param optionKey The option key.
     * @param value The value of the option.
     */
    public abstract void setOption(String optionKey,String value);
    /**
     * Removes a stored option from the options map.
     * @param optionKey The key of the stored option to remove.
     */
    public abstract void removeOption(String optionKey);
    /**
     * Gets all the roles this user belongs to.
     * @return A collection of role identifiers.
     */
    public abstract Collection<String> getRoles();
    /**
     * Removes a role from this user. The user will not be part of that
     * role anymore.
     * @param role The role to remove.
     */
    public abstract void removeRole(String role);
    /**
     * Adds a role to this user.
     * @param role The role to add.
     */
    public abstract void addRole(String role);
    /**
     * Saves any changes made to the user into the persistent user
     * store where this user object originally came from.
     * @return True if the saving succeeded, false otherwise.
     * @throws UserStoreException 
     */
    public abstract boolean saveUser() throws UserStoreException;
    /**
     * Checks if this user is of a specified role.
     * @param role The role identifier to check against.
     * @return True if the user is of the specified role.
     */
    public boolean isOfRole(String role){
        return getRoles().contains(role);
    }
}