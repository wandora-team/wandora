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
 *
 *
 * BaseNameDirective.java
 *
 *
 */
package org.wandora.query;
import org.wandora.topicmap.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class BaseNameDirective implements Directive {

    private Locator nameRole;

    public static final String NAME_SI="http://wandora.org/si/query/basename";

    public BaseNameDirective() {
        this(NAME_SI);
    }

    public BaseNameDirective(Locator nameRole) {
        this.nameRole=nameRole;
    }
    public BaseNameDirective(String nameRole) {
        this(new Locator(nameRole));
    }
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException {
        Topic contextTopic=context.getContextTopic();
        TopicMap tm=contextTopic.getTopicMap();
        ArrayList<ResultRow> res=new ArrayList<ResultRow>();
        res.add(new ResultRow(nameRole,nameRole,contextTopic.getBaseName()));

        return res;
    }
    public boolean isContextSensitive(){
        return true;
    }

}
