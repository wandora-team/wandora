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
 */

package org.wandora.application.tools.topicnames;


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


public class VariantWatsonTranslate extends AbstractWandoraTool implements WandoraTool {

    public static final boolean OVERRIDE_EXISTING_VARIANTS = true;



    /** Creates a new instance of VariantWatsonTranslate */
    public VariantWatsonTranslate() {
    }
    
    public VariantWatsonTranslate(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public String getName() {
        return "Translate variant with Watson";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and translates given variant names using Watson Translate.";
    }


    @Override
    public void execute(Wandora wandora, Context context) {
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;

        try {
            if(topics.hasNext()) {
                TopicMap tm = wandora.getTopicMap();

                SelectWatsonTranslationLanguagesPanel selectLanguages = new SelectWatsonTranslationLanguagesPanel();
                selectLanguages.openInDialog(wandora);

                // WAITING TILL CLOSED

                if(selectLanguages.wasAccepted()) {

                    setDefaultLogger();
                    boolean overrideExisting = selectLanguages.overrideExisting();
                    boolean createTopics = selectLanguages.createTopics();
                    boolean markTranslation = selectLanguages.markTranslatedText();

                    log("Translating variant names with Watson Translate.");

                    String languages = selectLanguages.getSelectedLanguages();
                    String sourceLang = WatsonTranslateBox.getSourceLanguageCodeFor(languages);
                    Set<Topic> sourceScope = new HashSet<Topic>();

                    String targetLang = WatsonTranslateBox.getTargetLanguageCodeFor(languages);
                    Set<Topic> targetScope = new HashSet<Topic>();

                    int progress = 0;
                    int count = 0;

                    Topic displayScope = tm.getTopic(XTMPSI.DISPLAY);
                    if(displayScope != null) {
                        Topic sourceLangTopic = tm.getTopic(XTMPSI.LANG_PREFIX+sourceLang);
                        if(sourceLangTopic != null) {
                            sourceScope.add(displayScope);
                            sourceScope.add(sourceLangTopic);

                            Topic targetLangTopic = tm.getTopic(XTMPSI.LANG_PREFIX+targetLang);
                            if(targetLangTopic == null && createTopics) {
                                targetLangTopic = tm.createTopic();
                                targetLangTopic.addSubjectIdentifier(new Locator(XTMPSI.LANG_PREFIX+targetLang));
                            }
                            if(targetLangTopic != null) {
                                targetScope.add(displayScope);
                                targetScope.add(targetLangTopic);
                            }
                            else {
                                log("Found no topic for target language '"+targetLang+"'.");
                                log("Aborting.");
                                setState(WAIT);
                                return;
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
                                        String variant = topic.getVariant(sourceScope);
                                        if(variant != null) {
                                            String existingVariant = topic.getVariant(targetScope);
                                            if(overrideExisting || existingVariant == null) {
                                                String translatedVariant = WatsonTranslateBox.translate(variant, sourceLang, targetLang, markTranslation);
                                                if(translatedVariant != null) {
                                                    log("Found translation '"+translatedVariant + "' for '"+variant+"'." );
                                                    topic.setVariant(targetScope, translatedVariant);
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
                            log("Total "+count+" variant names translated!");
                        }
                        else {
                            log("Found no topic for source language '"+sourceLang+"'.");
                        }
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
