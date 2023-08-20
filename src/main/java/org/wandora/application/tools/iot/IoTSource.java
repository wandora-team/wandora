/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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

/**
 * An IoTSource represents a simple virtual source of data for the IoT tool. It
 * functions as a callback for a given URL in SourceMapping.
 * 
 * @author Eero Lehtonen
 */
public interface IoTSource {
    
    
    /**
     * getData returns a String representation corresponding to the output of
     * the virtual service. (e.g. a simple virtual time service just returns a
     * String representation of the current system time.)'
     * 
     * @param url
     * @return a response from the virtual endpoint
     */
    String getData(String url);
    
    boolean matches(String url) throws MalformedURLException;
    
}
