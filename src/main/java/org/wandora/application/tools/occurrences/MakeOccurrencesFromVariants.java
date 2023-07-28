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
 * MakeOccurrencesFromVariants.java
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
public class MakeOccurrencesFromVariants extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	public static boolean overWrite = false;

    /**
     * Creates a new instance of MakeOccurrencesFromVariants
     */
    public MakeOccurrencesFromVariants() {
    }
    public MakeOccurrencesFromVariants(Context preferredContext) {
        setContext(preferredContext);
    }



    @Override
    public String getName() {
        return "Makes occurrences using topic's variant names.";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and copies all topic's display variant name to occurrences.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        try {
            setDefaultLogger();
            setLogTitle("Copying variant names to topic occurrences");
            log("Copying variant names to topic occurrences");

            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            Topic topic = null;
            String variant = null;
            String occurrence = null;

            Topic type = wandora.showTopicFinder("Select occurrence type...");
            if(type == null) return;

            Collection<Topic> languages = wandora.getTopicMap().getTopicsOfType(TMBox.LANGUAGE_SI);
            Topic language = null;
            Iterator<Topic> languageIterator = null;

            Set<Topic> scope = null;
            Topic displayScope = wandora.getTopicMap().getTopic(XTMPSI.DISPLAY);

            int progress = 0;

            while(topics.hasNext() && !forceStop()) {
                try {
                    topic = (Topic) topics.next();
                    if(topic != null && !topic.isRemoved()) {
                        setProgress(progress++);
                        if(languages != null && !languages.isEmpty()) {
                            languageIterator = languages.iterator();
                            while(languageIterator.hasNext()) {
                                try {
                                    language = (Topic) languageIterator.next();
                                    scope = new LinkedHashSet<>();
                                    scope.add(language);
                                    scope.add(displayScope);
                                    variant = topic.getVariant(scope);
                                    occurrence = topic.getData(type, language);
                                    if(occurrence == null || overWrite) {
                                        topic.setData(type, language, variant);
                                    }
                                }
                                catch(Exception e) {
                                    log(e);
                                }
                            }
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
