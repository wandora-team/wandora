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
 * CalaisClient.java
 *
 * Created on 2009-10-26, 10:18
 *
 */
package org.wandora.application.tools.extractors.opencalais.webservice;


import org.apache.axis2.*;
import java.rmi.*;


/**
 *
 * @author akivela
 */
public class CalaisClient {
    private final CalaisStub calaisStub;


    public CalaisClient(String serviceURL) {
        try {
            calaisStub = new CalaisStub(serviceURL);
            calaisStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(180000);
            // DISABLING_CHUNK_MODE:
            calaisStub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        }
        catch (AxisFault e) {
            throw new RuntimeException("Failed creating a connection to tags service in url: " + serviceURL, e);
        }
    }



    public String enlighten(String licenseId, String content, String paramsXML) throws RemoteException {
        CalaisStub.Enlighten params = new CalaisStub.Enlighten();
        params.setLicenseID(licenseId);
        params.setContent(content);
        params.setParamsXML(paramsXML);

        String response = calaisStub.enlighten(licenseId, content, paramsXML);
        //CalaisStub.EnlightenResponse response = calaisStub.Enlighten(params);

        return response;
    }

}
