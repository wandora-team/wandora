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

package org.wandora.application.tools.extractors.reddit;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import java.util.HashMap;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;

/**
 *
 * @author Eero Lehtonen <eero.lehtonen@gripstudios.com>
 */


abstract class ParseCallback<Object> {
  
    TopicMap tm;
    HashMap<String, Topic> thingTypes;
    
    public ParseCallback(TopicMap tm, HashMap<String, Topic> thingTypes) {
        this.tm = tm;
        this.thingTypes = thingTypes;
    }

    public ParseCallback() {
        this(null ,null);
    }

    abstract public void run(HttpResponse<JsonNode> response);

    public void error(Exception e){
        this.error(e, null);
    }

    abstract protected void error(Exception e, String body);
  
  
}
