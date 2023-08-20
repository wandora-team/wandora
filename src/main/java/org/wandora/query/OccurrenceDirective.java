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
 *
 *
 *
 * OccurrenceDirective.java
 *
 *
 */

package org.wandora.query;
import java.util.ArrayList;
import java.util.HashSet;

import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author olli
 */
public class OccurrenceDirective implements Directive {
    private Locator occurrenceRole;
    private Locator occurrenceType;
    private Locator occurrenceVersion;

    public static final String OCCURRENCE_SI="http://wandora.org/si/query/occurrence";

    public OccurrenceDirective(Locator occurrenceType,Locator occurrenceVersion,Locator occurrenceRole) {
        this.occurrenceType=occurrenceType;
        this.occurrenceVersion=occurrenceVersion;
        this.occurrenceRole=occurrenceRole;
    }
    public OccurrenceDirective(Locator occurrenceType,Locator occurrenceVersion) {
        this(occurrenceType,occurrenceVersion,new Locator(OCCURRENCE_SI));
    }
    public OccurrenceDirective(String occurrenceType,String occurrenceVersion,String occurrenceRole) {
        this(new Locator(occurrenceType),new Locator(occurrenceVersion),new Locator(occurrenceRole));
    }
    public OccurrenceDirective(String occurrenceType,String occurrenceVersion) {
        this(new Locator(occurrenceType),new Locator(occurrenceVersion),new Locator(OCCURRENCE_SI));
    }
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        Topic contextTopic=context.getContextTopic();
        TopicMap tm=contextTopic.getTopicMap();
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();

        String occurrence=null;
        Topic t=tm.getTopic(occurrenceType);
        Topic v=tm.getTopic(occurrenceVersion);
        if(t!=null && v!=null){
            HashSet<Topic> scope=new HashSet<Topic>();
            scope.add(t);
            scope.add(v);
            occurrence=contextTopic.getData(t, v);
        }
        res.add(new ResultRow(occurrenceRole,occurrenceRole,occurrence));

        return res;
    }
    public boolean isContextSensitive(){
        return true;
    }


}
