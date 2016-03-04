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
 * WandoraServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.wandora.webapi.implementation;

public class WandoraServiceServiceLocator extends org.apache.axis.client.Service implements org.wandora.webapi.implementation.WandoraServiceService {

    public WandoraServiceServiceLocator() {
    }


    public WandoraServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WandoraServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for WandoraService
    private java.lang.String WandoraService_address = "http://localhost:8080/wandora/services/WandoraService";

    public java.lang.String getWandoraServiceAddress() {
        return WandoraService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WandoraServiceWSDDServiceName = "WandoraService";

    public java.lang.String getWandoraServiceWSDDServiceName() {
        return WandoraServiceWSDDServiceName;
    }

    public void setWandoraServiceWSDDServiceName(java.lang.String name) {
        WandoraServiceWSDDServiceName = name;
    }

    public org.wandora.webapi.implementation.WandoraService_PortType getWandoraService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WandoraService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWandoraService(endpoint);
    }

    public org.wandora.webapi.implementation.WandoraService_PortType getWandoraService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.wandora.webapi.implementation.WandoraServiceSoapBindingStub _stub = new org.wandora.webapi.implementation.WandoraServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getWandoraServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWandoraServiceEndpointAddress(java.lang.String address) {
        WandoraService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.wandora.webapi.implementation.WandoraService_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.wandora.webapi.implementation.WandoraServiceSoapBindingStub _stub = new org.wandora.webapi.implementation.WandoraServiceSoapBindingStub(new java.net.URL(WandoraService_address), this);
                _stub.setPortName(getWandoraServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("WandoraService".equals(inputPortName)) {
            return getWandoraService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:WandoraService", "WandoraServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:WandoraService", "WandoraService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("WandoraService".equals(portName)) {
            setWandoraServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
