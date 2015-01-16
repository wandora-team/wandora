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
 * WSPlayer.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.wandora.application.server.topicmapservice;

public class WSPlayer  implements java.io.Serializable {
    private java.lang.String role;

    private java.lang.String member;

    public WSPlayer() {
    }

    public WSPlayer(
           java.lang.String role,
           java.lang.String member) {
           this.role = role;
           this.member = member;
    }


    /**
     * Gets the role value for this WSPlayer.
     * 
     * @return role
     */
    public java.lang.String getRole() {
        return role;
    }


    /**
     * Sets the role value for this WSPlayer.
     * 
     * @param role
     */
    public void setRole(java.lang.String role) {
        this.role = role;
    }


    /**
     * Gets the member value for this WSPlayer.
     * 
     * @return member
     */
    public java.lang.String getMember() {
        return member;
    }


    /**
     * Sets the member value for this WSPlayer.
     * 
     * @param member
     */
    public void setMember(java.lang.String member) {
        this.member = member;
    }

}
