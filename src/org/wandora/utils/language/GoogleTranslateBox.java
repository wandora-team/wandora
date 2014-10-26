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
 */



package org.wandora.utils.language;

import com.google.api.GoogleAPI;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import org.wandora.application.Wandora;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;


/**
 *
 * @author akivela
 */
public class GoogleTranslateBox {


    private static String apikey = null;


    
    public static void resetAPIKey() {
        apikey = null;
    }
    
    public static void setAPIKey(String newkey) {
        apikey = newkey;
    }
    
    public static String translate(String text, Topic sourceLangTopic, Topic targetLangTopic) {
        return translate(text, sourceLangTopic, targetLangTopic, false);
    }

    public static String translate(String text, Topic sourceLangTopic, Topic targetLangTopic, boolean markTranslation) {
        try {
            String sourceLangStr = LanguageBox.getCodeForLangTopic(sourceLangTopic);
            String targetLangStr = LanguageBox.getCodeForLangTopic(targetLangTopic);

            if(sourceLangStr != null && targetLangStr != null) {
                Language sourceLang = Language.fromString(sourceLangStr);
                Language targetLang = Language.fromString(targetLangStr);

                if(sourceLang != null && targetLang != null) {
                    return translate(text, sourceLang, targetLang, markTranslation);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String translate(String text, Language sourceLang, Language targetLang) {
        return translate(text, sourceLang, targetLang, false);
    }

    public static String translate(String text, Language sourceLang, Language targetLang, boolean markTranslation) {
        String translatedText = null;
        if(text != null && sourceLang != null && targetLang != null) {
            try {
                System.out.println("Google Translating '"+text+"' from '"+sourceLang+"' to '"+targetLang+"'");
                GoogleAPI.setHttpReferrer("http://wandora.org");
                if(apikey == null || apikey.length() == 0) {
                    apikey = WandoraOptionPane.showInputDialog(Wandora.getWandora(), "Give your Google Translate API key?", "", "Give your Google Translate API key?");
                    if(apikey != null) apikey = apikey.trim();
                }
                GoogleAPI.setKey(apikey);
                translatedText = Translate.DEFAULT.execute(text, sourceLang, targetLang);
                if(translatedText != null && translatedText.length() == 0) {
                    translatedText = null;
                }
                if(translatedText != null && markTranslation) {
                    translatedText += " [GOOGLE TRANSLATION]";
                }
            }
            catch(Exception e) {
                Wandora.getWandora().handleError(e);
                //e.printStackTrace();
            }
        }
        return translatedText;
    }





    public static Language getLanguageForCode(String code) {
        if(code == null) return null;
        return Language.fromString(LanguageBox.getNameFor6391Code(code));
    }





    public static String getLanguageCodeFor(Language language) {
        if(language == null) return null;
        return LanguageBox.get6391ForName(language.name());
    }





    public static Topic createTopicForLanguage(Language language, TopicMap tm) {
        return LanguageBox.createTopicForLanguageName(language.name(), tm);
    }



    



}
