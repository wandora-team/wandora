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
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author Eero Lehtonen
 */
public class TMStatsSource extends AbstractIoTSource implements IoTSource {

    private static final String HOST = "wandora.org";
    private static final String PATH = "/si/iot/source/tm-stats";

    @Override
    public String getData(String url) {
        TopicMap tm = Wandora.getWandora().getTopicMap();
        
        try {
            JSONObject o = new JSONObject();
            o.put("num_topics", tm.getNumTopics());
            o.put("num_associations", tm.getNumAssociations());
            
            return o.toString();
        }
        catch (TopicMapException | JSONException e) {
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
