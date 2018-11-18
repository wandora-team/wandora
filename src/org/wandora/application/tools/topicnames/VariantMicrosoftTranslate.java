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


import org.wandora.utils.language.SelectMicrosoftTranslationLanguagesPanel;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import java.util.*;

import com.memetix.mst.language.Language;
import org.wandora.utils.language.MicrosoftTranslateBox;
import org.wandora.utils.language.MicrosoftTranslateConfiguration;
import org.wandora.utils.Tuples.T2;



/**

 * @author akivela
 */


public class VariantMicrosoftTranslate extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;

	public static final boolean OVERRIDE_EXISTING_VARIANTS = true;



    /** Creates a new instance of VariantMicrosoftTranslate */
    public VariantMicrosoftTranslate() {
    }

    public VariantMicrosoftTranslate(Context preferredContext) {
        setContext(preferredContext);
    }


    @Override
    public String getName() {
        return "Translate variant with Microsoft Translator";
    }

    @Override
    public String getDescription() {
        return "Iterates through selected topics and translates given variant names using Microsoft Translator.";
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






    public void execute(Wandora wandora, Context context) {
        Iterator topics = context.getContextObjects();
        if(topics == null || !topics.hasNext()) return;

        try {
            if(topics.hasNext()) {
                TopicMap tm = wandora.getTopicMap();

                SelectMicrosoftTranslationLanguagesPanel selectLanguages = new SelectMicrosoftTranslationLanguagesPanel();
                selectLanguages.openInDialog(wandora);

                // WAITING TILL CLOSED

                if(selectLanguages.wasAccepted()) {

                    setDefaultLogger();
                    boolean overrideExisting = selectLanguages.overrideExisting();
                    boolean createTopics = selectLanguages.createTopics();
                    boolean markTranslation = selectLanguages.markTranslatedText();

                    log("Translating variant names with Microsoft Translate.");

                    Language sourceLang = selectLanguages.getSourceLanguage();
                    Set<Topic> sourceScope = new HashSet<Topic>();

                    //Collection<String> targetLangs = new ArrayList<String>();
                    Collection<Language> targetLangs = selectLanguages.getTargetLanguages();
                    Collection<T2<Language,Set<Topic>>> targetScopes = new ArrayList<T2<Language,Set<Topic>>>();

                    int progress = 0;
                    int count = 0;

                    Topic displayScope = tm.getTopic(XTMPSI.DISPLAY);
                    if(displayScope != null) {
                        Topic sourceLangTopic = tm.getTopic(XTMPSI.LANG_PREFIX+MicrosoftTranslateBox.getLanguageCodeFor(sourceLang));
                        if(sourceLangTopic != null) {
                            sourceScope.add(displayScope);
                            sourceScope.add(sourceLangTopic);

                            for(Language targetLang : targetLangs) {
                                Topic targetLangTopic = tm.getTopic(XTMPSI.LANG_PREFIX+MicrosoftTranslateBox.getLanguageCodeFor(targetLang));
                                if(targetLangTopic == null && createTopics) {
                                    targetLangTopic = MicrosoftTranslateBox.createTopicForLanguage(targetLang, tm);
                                }
                                if(targetLangTopic != null) {
                                    Set<Topic> targetScope = new HashSet<Topic>();
                                    targetScope.add(displayScope);
                                    targetScope.add(targetLangTopic);
                                    targetScopes.add(new T2<Language,Set<Topic>>(targetLang, targetScope));
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
                                        String variant = topic.getVariant(sourceScope);
                                        if(variant != null) {
                                            for(T2<Language,Set<Topic>> target : targetScopes) {
                                                Language targetLang = target.e1;
                                                Set<Topic> targetScope = target.e2;
                                                String existingVariant = topic.getVariant(targetScope);
                                                if(overrideExisting || existingVariant == null) {
                                                    String translatedVariant = MicrosoftTranslateBox.translate(variant, sourceLang, targetLang, markTranslation);
                                                    if(translatedVariant != null) {
                                                        log("Found translation '"+translatedVariant + "' for '"+variant+"'." );
                                                        topic.setVariant(targetScope, translatedVariant);
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
