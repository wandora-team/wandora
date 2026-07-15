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
 */


package org.wandora.application.tools.occurrences;


import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;


/**
 *
 * @author akivela
 */
public class MakeOccurrenceFromSubjectLocator extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static boolean overWrite = false;

    /**
     * Creates a new instance of MakeOccurrenceFromSubjectLocator
     */
    public MakeOccurrenceFromSubjectLocator() {
    }
    public MakeOccurrenceFromSubjectLocator(Context preferredContext) {
        setContext(preferredContext);
    }



    @Override
    public String getName() {
        return "Make occurrence using topic's subject locator.";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies topic's subject locator to occurrence.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Copying subject locator to topic occurrence");
            log("Copying subject locator to topic occurrence");

            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String slstr = null;
            Locator sl = null;
            String occurrence = null;

            Topic type = wandora.showTopicFinder("Select occurrence type...");
            if(type == null) return;

            Topic language = wandora.showTopicFinder("Select occurrence language...");
            if(language == null) return;

            int progress = 0;
            int count = 0;

            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        sl = topic.getSubjectLocator();
                        if(sl != null) {
                            slstr = sl.toExternalForm();
                            occurrence = topic.getData(type, language);
                            if(occurrence == null || overWrite) {
                                topic.setData(type, language, slstr);
                                count++;
                            }
                        }
                    }
                }
                catch(Exception e) {
                    log(e);
                }
            }
            if(count == 0) log("No subject locators copied to occurrences.");
            else log("Total "+count+" subject locators copied to occurrences.");
            log("Ready.");
            setState(WAIT);
        }
        catch (Exception e) {
            log(e);
        }
    }

}
