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
 * WSAssociation.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.wandora.application.server.topicmapservice;

public class WSAssociation  implements java.io.Serializable {
    private java.lang.String type;

    private WSPlayer[] players;

    public WSAssociation() {
    }

    public WSAssociation(
           java.lang.String type,WSPlayer[] players) {
           this.type = type;
           this.players = players;
    }


    /**
     * Gets the type value for this WSAssociation.
     * 
     * @return type
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this WSAssociation.
     * 
     * @param type
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the players value for this WSAssociation.
     * 
     * @return players
     */
    public WSPlayer[] getPlayers() {
        return players;
    }


    /**
     * Sets the players value for this WSAssociation.
     * 
     * @param players
     */
    public void setPlayers(WSPlayer[] players) {
        this.players = players;
    }

}
