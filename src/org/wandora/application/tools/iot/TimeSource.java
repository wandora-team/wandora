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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
/**
 *
 * @author Eero Lehtonen
 */


class TimeSource extends AbstractIoTSource implements IoTSource {

    private static final String HOST = "wandora.org";
    private static final String PATH = "/si/iot/source/time";
    
    @Override
    public String getData(String url) {
        
        Map<String,String> params;
        
        try {
            URL u = new URL(url);
            params = parseParams(u);
            if(params != null && params.containsKey("format")) {
                String formatString = params.get("format");
                
                DateFormat format = new SimpleDateFormat(formatString);
                Date date = new Date();
                
                return format.format(date);
            }
        } 
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return Long.toString(System.currentTimeMillis()); // Default
    }

    
    @Override
    public boolean matches(String url) throws MalformedURLException{
        URL u = new URL(url);
        return u.getHost().equals(HOST) && u.getPath().equals(PATH);
    }
    
}
