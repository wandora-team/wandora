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
 *
 */

package org.wandora.application.tools.occurrences;


import com.google.api.translate.Language;
import java.util.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.utils.Tuples.T2;
import org.wandora.utils.language.GoogleTranslateBox;
import org.wandora.utils.language.SelectGoogleTranslationLanguagesPanel;
import org.wandora.utils.language.SelectWatsonTranslationLanguagesPanel;
import org.wandora.utils.language.WatsonTranslateBox;



/**
 * @author akivela
 */


public class OccurrenceWatsonTranslate extends AbstractWandoraTool implements WandoraTool {




    /** Creates a new instance of OccurrenceWatsonTranslate */
    public OccurrenceWatsonTranslate() {
    }

    public OccurrenceWatsonTranslate(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public String getName() {
        return "Translate occurrence with IBM Watson";
    }

    @Override
    public String getDescription() {
        return "Translates given occurrence texts using IBM Watson.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        Object contextSource = context.getContextSource();

        if(contextSource instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) contextSource;
            ot.watsonTranslate();
        }
        else {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            try {
                if(topics.hasNext()) {
                    TopicMap tm = wandora.getTopicMap();

                    Topic occurrenceType = wandora.showTopicFinder("Select occurrence type...");
                    if(occurrenceType == null) return;

                    SelectWatsonTranslationLanguagesPanel selectLanguages = new SelectWatsonTranslationLanguagesPanel();
                    selectLanguages.openInDialog(wandora);

                    // WAITING TILL CLOSED

                    if(selectLanguages.wasAccepted()) {

                        setDefaultLogger();
                        boolean overrideExisting = selectLanguages.overrideExisting();
                        boolean createTopics = selectLanguages.createTopics();
                        boolean markTranslation = selectLanguages.markTranslatedText();

                        log("Translating occurrences with IBM Watson.");

                        String languages = selectLanguages.getSelectedLanguages();
                        
                        int progress = 0;
                        int count = 0;

                        Topic sourceScope = tm.getTopic(XTMPSI.LANG_PREFIX+WatsonTranslateBox.getSourceLanguageCodeFor(languages));
                        Topic targetScope = tm.getTopic(XTMPSI.LANG_PREFIX+WatsonTranslateBox.getTargetLanguageCodeFor(languages));
                        
                        if(sourceScope != null) {
                            if(targetScope != null) {

                                Topic topic = null;
                                ArrayList<Topic> layerStackTopics = new ArrayList<Topic>();
                                while(topics.hasNext() && !forceStop()) {
                                    try {
                                        topic = (Topic) topics.next();
                                        layerStackTopics.add(tm.getTopic(topic.getOneSubjectIdentifier()));
                                    }
                                    catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                int i = 0;
                                int s = layerStackTopics.size();
                                while(i < s && !forceStop()) {
                                    try {
                                        topic = layerStackTopics.get(i);
                                        if(topic != null && !topic.isRemoved()) {
                                            progress++;
                                            String occurrence = topic.getData(occurrenceType, sourceScope);
                                            if(occurrence != null) {
                                                String existingOccurrence = topic.getData(occurrenceType, targetScope);
                                                if(overrideExisting || existingOccurrence == null) {
                                                    String translatedOccurrence = WatsonTranslateBox.translate(occurrence, languages, markTranslation);
                                                    if(translatedOccurrence != null) {
                                                        log("Found translation '"+translatedOccurrence + "' for '"+occurrence+"'." );
                                                        topic.setData(occurrenceType, targetScope, translatedOccurrence);
                                                        count++;
                                                    }
                                                }

                                            }
                                        }
                                    }
                                    catch(Exception e) {
                                        log(e);
                                    }
                                    i++;
                                }
                                log("Total "+count+" occurrences translated!");
                            }
                            else {
                                log("Found no topic for target language '"+languages+"'.");
                            }
                        }
                        else {
                            log("Found no topic for source language '"+languages+"'.");
                        }
                    }
                }
            }
            catch (Exception e) {
                log(e);
            }
            setState(WAIT);
        }
    }

}
