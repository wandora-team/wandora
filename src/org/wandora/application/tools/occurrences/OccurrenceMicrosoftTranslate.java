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


import org.wandora.utils.language.SelectMicrosoftTranslationLanguagesPanel;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import java.util.*;

import com.memetix.mst.language.Language;
import org.wandora.utils.language.MicrosoftTranslateBox;
import org.wandora.utils.language.MicrosoftTranslateConfiguration;
import org.wandora.utils.Tuples.T2;


/**
 *
 * @author akivela
 */
public class OccurrenceMicrosoftTranslate extends AbstractWandoraTool implements WandoraTool {




    /** Creates a new instance of OccurrenceMicrosoftTranslate */
    public OccurrenceMicrosoftTranslate() {
    }

    public OccurrenceMicrosoftTranslate(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public String getName() {
        return "Translate occurrence with Microsoft";
    }

    @Override
    public String getDescription() {
        return "Translates given occurrence texts using Microsoft Translate.";
    }

    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        MicrosoftTranslateConfiguration configurator = new MicrosoftTranslateConfiguration();
        configurator.open(wandora);
    }




    @Override
    public void execute(Wandora wandora, Context context) {
        Object contextSource = context.getContextSource();

        if(contextSource instanceof OccurrenceTable) {
            OccurrenceTable ot = (OccurrenceTable) contextSource;
            ot.translate(OccurrenceTable.MICROSOFT_TRANSLATE);
        }
        else {
            Iterator topics = context.getContextObjects();
            if(topics == null || !topics.hasNext()) return;

            try {
                if(topics.hasNext()) {
                    TopicMap tm = wandora.getTopicMap();

                    Topic occurrenceType = wandora.showTopicFinder("Select occurrence type...");
                    if(occurrenceType == null) return;

                    SelectMicrosoftTranslationLanguagesPanel selectLanguages = new SelectMicrosoftTranslationLanguagesPanel();
                    selectLanguages.openInDialog(wandora);

                    // WAITING TILL CLOSED

                    if(selectLanguages.wasAccepted()) {

                        setDefaultLogger();
                        boolean overrideExisting = selectLanguages.overrideExisting();
                        boolean createTopics = selectLanguages.createTopics();
                        boolean markTranslation = selectLanguages.markTranslatedText();

                        log("Translating occurrences with Microsoft Translate.");

                        Language sourceLang = selectLanguages.getSourceLanguage();
                        Topic sourceScope = null;

                        //Collection<String> targetLangs = new ArrayList<String>();
                        Collection<Language> targetLangs = selectLanguages.getTargetLanguages();
                        Collection<T2<Language,Topic>> targetScopes = new ArrayList<T2<Language,Topic>>();

                        int progress = 0;
                        int count = 0;

                        sourceScope = tm.getTopic(XTMPSI.LANG_PREFIX+MicrosoftTranslateBox.getLanguageCodeFor(sourceLang));
                        if(sourceScope != null) {
                            for(Language targetLang : targetLangs) {
                                Topic targetLangTopic = tm.getTopic(XTMPSI.LANG_PREFIX+MicrosoftTranslateBox.getLanguageCodeFor(targetLang));
                                if(targetLangTopic == null && createTopics) {
                                    targetLangTopic = MicrosoftTranslateBox.createTopicForLanguage(targetLang, tm);
                                }
                                if(targetLangTopic != null) {
                                    targetScopes.add(new T2(targetLang, targetLangTopic));
                                }
                                else {
                                    log("Found no topic for target language '"+targetLang+"'.");
                                }
                            }

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
                                            for(T2<Language,Topic> target : targetScopes) {
                                                Language targetLang = target.e1;
                                                Topic targetScope = target.e2;
                                                String existingOccurrence = topic.getData(occurrenceType, targetScope);
                                                if(overrideExisting || existingOccurrence == null) {
                                                    String translatedOccurrence = MicrosoftTranslateBox.translate(occurrence, sourceLang, targetLang, markTranslation);
                                                    if(translatedOccurrence != null) {
                                                        log("Found translation '"+translatedOccurrence + "' for '"+occurrence+"'." );
                                                        topic.setData(occurrenceType, targetScope, translatedOccurrence);
                                                        count++;
                                                    }
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
                            log("Found no topic for source language '"+sourceLang+"'.");
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
