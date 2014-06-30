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
 * MakeOccurrenceFromVariant.java
 *
 * Created on 2.3.2010, 14:55
 *
 */



package org.wandora.application.tools.occurrences;

import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;


/**
 *
 * @author akivela
 */
public class MakeOccurrenceFromVariant extends AbstractWandoraTool implements WandoraTool {

    public static boolean overWrite = false;

    /**
     * Creates a new instance of MakeOccurrenceFromVariant
     */
    public MakeOccurrenceFromVariant() {
    }
    public MakeOccurrenceFromVariant(Context preferredContext) {
        setContext(preferredContext);
    }



    @Override
    public String getName() {
        return "Makes occurrence using topic's variant name.";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies topic's display variant name to occurrence.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Copying variant name to topic occurrence");
            log("Copying variant name to topic occurrence");

            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String variant = null;
            String occurrence = null;

            Topic type = wandora.showTopicFinder("Select occurrence type...");
            if(type == null) return;

            Topic language = wandora.showTopicFinder("Select language...");
            if(language == null) return;

            Set<Topic> scope = new HashSet<Topic>();
            Topic displayScope = wandora.getTopicMap().getTopic(XTMPSI.DISPLAY);
            scope.add(language);
            scope.add(displayScope);

            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        variant = topic.getVariant(scope);
                        occurrence = topic.getData(type, language);
                        if(occurrence == null || overWrite) {
                            topic.setData(type, language, variant);
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            log("OK.");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
            setState(WAIT);
        }
    }

}
