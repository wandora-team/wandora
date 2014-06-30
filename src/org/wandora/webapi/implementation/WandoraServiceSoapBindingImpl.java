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
 * WandoraServiceSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.wandora.webapi.implementation;

public class WandoraServiceSoapBindingImpl implements org.wandora.webapi.implementation.WandoraService_PortType{
    private WandoraPiccoloWebapiService inner;
    public WandoraServiceSoapBindingImpl(){
        inner=new WandoraPiccoloWebapiService();
    }
    
    public org.wandora.webapi.implementation.WSTopic getTopic(java.lang.String in0, boolean in1) throws java.rmi.RemoteException {
        System.out.println("WS:getTopic(\""+in0+"\","+in1+")");
        return inner.getTopic(in0,in1);
    }

    public org.wandora.webapi.implementation.WSTopic[] getTopics(java.lang.String[] in0, boolean in1) throws java.rmi.RemoteException {
        System.out.println("WS:getTopics(count:"+in0.length+","+in1+")");
        return inner.getTopics(in0,in1);
    }

    public org.wandora.webapi.implementation.WSTopic getTopicWithBaseName(java.lang.String in0, boolean in1) throws java.rmi.RemoteException {
        System.out.println("WS:getTopicWithBaseName(\""+in0+"\","+in1+")");
        return inner.getTopicWithBaseName(in0,in1);
    }

    public org.wandora.webapi.implementation.WSTopic[] getTopicsOfType(java.lang.String in0, boolean in1) throws java.rmi.RemoteException {
        System.out.println("WS:getTopicsOfType(\""+in0+"\","+in1+")");
        return inner.getTopicsOfType(in0,in1);
    }

}
