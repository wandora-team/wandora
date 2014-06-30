/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * SetResourceRefFieldProcesor.java
 *
 * Created on 3. joulukuuta 2004, 14:29
 */

package org.wandora.application.tools.extractors.datum;
import org.wandora.topicmap.*;
import org.wandora.piccolo.Logger;
import java.util.*;

/**
 *
 * @author  olli
 */
public class SetResourceLocatorFieldProcessor implements FieldProcessor {
    
    protected ExtractionHelper helper;
    protected String target;
    
    /** Creates a new instance of SetResourceRefFieldProcesor */
    public SetResourceLocatorFieldProcessor(ExtractionHelper helper, String target) {
        this.helper=helper;
        this.target=target;
    }
    
    public void processDatum(Map datum, String field, TopicMap tm, Logger logger) throws ExtractionException {
        Object o=datum.get(field);
        String loc=null;
        if(o instanceof String) loc=(String)o;
        else {
            Iterator iter=((Collection)o).iterator();
            if(iter.hasNext()) loc=(String)iter.next();
            else return;
        }
        // do not use getOrCreateTopics (collection version) because setting resource locator to the same will
        // merge the topics which probably is not what was intended
        try{
            Topic t=helper.getOrCreateTopic(datum,target,tm,false);
            t.setSubjectLocator(tm.createLocator(loc));
        }catch(TopicMapException tme){throw new ExtractionException(tme);}
    }
    
}
