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
 * MakeOccurrenceFromBasename.java
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
public class MakeOccurrenceFromBasename extends AbstractWandoraTool implements WandoraTool {

    public static boolean overWrite = false;

    /**
     * Creates a new instance of MakeOccurrenceFromBasename
     */
    public MakeOccurrenceFromBasename() {
    }
    public MakeOccurrenceFromBasename(Context preferredContext) {
        setContext(preferredContext);
    }



    @Override
    public String getName() {
        return "Make occurrence using topic's base name.";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies topic's base name to occurrence.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Copying base name to topic occurrence");
            log("Copying base name to topic occurrence");

            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String basename = null;
            String occurrence = null;

            Topic type = wandora.showTopicFinder("Select occurrence type...");
            if(type == null) return;

            Topic language = wandora.showTopicFinder("Select occurrence language...");
            if(language == null) return;

            int progress = 0;

            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        basename = topic.getBaseName();
                        occurrence = topic.getData(type, language);
                        if(occurrence == null || overWrite) {
                            topic.setData(type, language, basename);
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            log("Ready.");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }

}
