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
 * WandoraServiceSoapBindingSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.wandora.webapi.implementation;

public class WandoraServiceSoapBindingSkeleton implements org.wandora.webapi.implementation.WandoraService_PortType, org.apache.axis.wsdl.Skeleton {
    private org.wandora.webapi.implementation.WandoraService_PortType impl;
    private static java.util.Map _myOperations = new java.util.Hashtable();
    private static java.util.Collection _myOperationsList = new java.util.ArrayList();

    /**
    * Returns List of OperationDesc objects with this name
    */
    public static java.util.List getOperationDescByName(java.lang.String methodName) {
        return (java.util.List)_myOperations.get(methodName);
    }

    /**
    * Returns Collection of OperationDescs
    */
    public static java.util.Collection getOperationDescs() {
        return _myOperationsList;
    }

    static {
        org.apache.axis.description.OperationDesc _oper;
        org.apache.axis.description.FaultDesc _fault;
        org.apache.axis.description.ParameterDesc [] _params;
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTopic", _params, new javax.xml.namespace.QName("", "getTopicReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:WandoraService", "WSTopic"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:WandoraService", "getTopic"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTopic") == null) {
            _myOperations.put("getTopic", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTopic")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:WandoraService", "ArrayOf_soapenc_string"), java.lang.String[].class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTopics", _params, new javax.xml.namespace.QName("", "getTopicsReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:WandoraService", "ArrayOfWSTopic"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:WandoraService", "getTopics"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTopics") == null) {
            _myOperations.put("getTopics", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTopics")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTopicWithBaseName", _params, new javax.xml.namespace.QName("", "getTopicWithBaseNameReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:WandoraService", "WSTopic"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:WandoraService", "getTopicWithBaseName"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTopicWithBaseName") == null) {
            _myOperations.put("getTopicWithBaseName", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTopicWithBaseName")).add(_oper);
        _params = new org.apache.axis.description.ParameterDesc [] {
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"), java.lang.String.class, false, false), 
            new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false), 
        };
        _oper = new org.apache.axis.description.OperationDesc("getTopicsOfType", _params, new javax.xml.namespace.QName("", "getTopicsOfTypeReturn"));
        _oper.setReturnType(new javax.xml.namespace.QName("urn:WandoraService", "ArrayOfWSTopic"));
        _oper.setElementQName(new javax.xml.namespace.QName("urn:WandoraService", "getTopicsOfType"));
        _oper.setSoapAction("");
        _myOperationsList.add(_oper);
        if (_myOperations.get("getTopicsOfType") == null) {
            _myOperations.put("getTopicsOfType", new java.util.ArrayList());
        }
        ((java.util.List)_myOperations.get("getTopicsOfType")).add(_oper);
    }

    public WandoraServiceSoapBindingSkeleton() {
        this.impl = new org.wandora.webapi.implementation.WandoraServiceSoapBindingImpl();
    }

    public WandoraServiceSoapBindingSkeleton(org.wandora.webapi.implementation.WandoraService_PortType impl) {
        this.impl = impl;
    }
    public org.wandora.webapi.implementation.WSTopic getTopic(java.lang.String in0, boolean in1) throws java.rmi.RemoteException
    {
        org.wandora.webapi.implementation.WSTopic ret = impl.getTopic(in0, in1);
        return ret;
    }

    public org.wandora.webapi.implementation.WSTopic[] getTopics(java.lang.String[] in0, boolean in1) throws java.rmi.RemoteException
    {
        org.wandora.webapi.implementation.WSTopic[] ret = impl.getTopics(in0, in1);
        return ret;
    }

    public org.wandora.webapi.implementation.WSTopic getTopicWithBaseName(java.lang.String in0, boolean in1) throws java.rmi.RemoteException
    {
        org.wandora.webapi.implementation.WSTopic ret = impl.getTopicWithBaseName(in0, in1);
        return ret;
    }

    public org.wandora.webapi.implementation.WSTopic[] getTopicsOfType(java.lang.String in0, boolean in1) throws java.rmi.RemoteException
    {
        org.wandora.webapi.implementation.WSTopic[] ret = impl.getTopicsOfType(in0, in1);
        return ret;
    }

}
