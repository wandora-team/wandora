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
 * WandoraService_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.wandora.webapi.implementation;

public interface WandoraService_PortType extends java.rmi.Remote {
    public org.wandora.webapi.implementation.WSTopic getTopic(java.lang.String in0, boolean in1) throws java.rmi.RemoteException;
    public org.wandora.webapi.implementation.WSTopic[] getTopics(java.lang.String[] in0, boolean in1) throws java.rmi.RemoteException;
    public org.wandora.webapi.implementation.WSTopic getTopicWithBaseName(java.lang.String in0, boolean in1) throws java.rmi.RemoteException;
    public org.wandora.webapi.implementation.WSTopic[] getTopicsOfType(java.lang.String in0, boolean in1) throws java.rmi.RemoteException;
}
