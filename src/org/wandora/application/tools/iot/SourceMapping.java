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
 */

package org.wandora.application.tools.iot;

import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 *
 * @author Eero Lehtonen
 */


final class SourceMapping {

    protected static final String BASE_URL = "http://wandora.org/si/iot/source/";
    
    private static final ArrayList<IoTSource> sources;
    
    static {
        sources = new ArrayList<>();
        sources.add(new TimeSource());
        sources.add(new GeolocationSource());
        sources.add(new TMStatsSource());
    }
    
    private static SourceMapping instance = null;
    
    protected static synchronized SourceMapping getInstance() {
        if(instance == null) instance = new SourceMapping();
        return instance;
    }
    
    
    public IoTSource match(String url) {
        
        for(IoTSource source : sources) {
            try{
                if(source.matches(url)) {
                    return source;
                }
            } 
            catch (MalformedURLException e) {
            }
            
        }
        
        return null;
    }

    
}
