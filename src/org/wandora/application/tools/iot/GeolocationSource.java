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
package org.wandora.application.tools.iot;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.net.MalformedURLException;
import java.net.URL;
import org.wandora.dep.json.JSONObject;

/**
 *
 * @author Eero Lehtonen
 */
public class GeolocationSource extends AbstractIoTSource implements IoTSource {

    private static final String HOST = "wandora.org";
    private static final String PATH = "/si/iot/source/geolocation";

    private static final String API_URL = "http://ip-api.com/json";

    @Override
    public String getData(String url) {
        try {
            JsonNode resp = Unirest.get(API_URL).asJson().getBody();
            JSONObject obj = resp.getObject();

            return Double.toString(obj.getDouble("lat")) + ", " + Double.toString(obj.getDouble("lon"));
        } 
        catch (UnirestException ex) {
            // IGNORE
        }

        return null;
    }
    

    @Override
    public boolean matches(String url) throws MalformedURLException {
        URL u = new URL(url);
        return u.getHost().equals(HOST) && u.getPath().equals(PATH);
    }

}
