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
 * Directive.java
 *
 * Created on 25. lokakuuta 2007, 11:29
 *
 */

package org.wandora.query;
import java.util.ArrayList;

import org.wandora.topicmap.TopicMapException;

/**
 * @deprecated
 *
 * @author olli
 */
public interface Directive {
    
    public ArrayList<ResultRow> query(QueryContext context) throws TopicMapException;
    public boolean isContextSensitive();


}
