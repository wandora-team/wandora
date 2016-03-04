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
 */




package org.wandora.utils.language;


import java.util.HashMap;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;

/**
 *
 * @author akivela
 */
public class LanguageBox {



    public static final String[] languageNamesAndCodes = new String[] {
        "Abkhazian", "abk", "ab",
        "Afar", "aar", "aa",
        "Afrikaans", "afr", "af",
        "Akan", "aka", "ak",
        "Albanian", "alb", "sq",
        "Amharic", "amh", "am",
        "Arabic", "ara", "ar",
        "Aragonese", "arg", "an",
        "Armenian", "arm", "hy",
        "Assamese", "asm", "as",
        "Avaric", "ava", "av",
        "Avestan", "ave", "ae",
        "Aymara", "aym", "ay",
        "Azerbaijani", "aze", "az",
        "Bambara", "bam", "bm",
        "Bashkir", "bak", "ba",
        "Basque", "baq", "eu",
        "Belarusian", "bel", "be",
        "Bengali", "ben", "bn",
        "Bihari", "bih", "bh",
        "Bislama", "bis", "bi",
        "Bosnian", "bos", "bs",
        "Breton", "bre", "br",
        "Bulgarian", "bul", "bg",
        "Burmese", "bur", "my",
        "Catalan", "cat", "ca",
        "Chamorro", "cha", "ch",
        "Chechen", "che", "ce",
        "Chinese", "chi", "zh",
        "Chuang", "zha", "za",
        "Church Slavic", "chu", "cu",
        "Chuvash", "chv", "cv",
        "Cornish", "cor", "kw",
        "Corsican", "cos", "co",
        "Cree", "cre", "cr",
        "Croatian", "scr", "hr",
        "Czech", "cze", "cs",
        "Danish", "dan", "da",
        "Divehi", "div", "dv",
        "Dutch", "dut", "nl",
        "Dzongkha", "dzo", "dz",
        "English", "eng", "en",
        "Esperanto", "epo", "eo",
        "Estonian", "est", "et",
        "Ewe", "ewe", "ee",
        "Faroese", "fao", "fo",
        "Fijian", "fij", "fj",
        "Finnish", "fin", "fi",
        "French", "fre", "fr",
        "Frisian", "fry", "fy",
        "Fulah", "ful", "ff",
        "Gaelic", "gla", "gd",
        "Galician", "glg", "gl",
        "Ganda", "lug", "lg",
        "Georgian", "geo", "ka",
        "German", "ger", "de",
        "Gikuyu", "kik", "ki",
        "Greek", "gre", "el",
        "Greenlandic", "kal", "kl",
        "Guarani", "grn", "gn",
        "Gujarati", "guj", "gu",
        "Haitian", "hat", "ht",
        "Hausa", "hau", "ha",
        "Hebrew", "heb", "he",
        "Herero", "her", "hz",
        "Hindi", "hin", "hi",
        "Hiri Motu", "hmo", "ho",
        "Hungarian", "hun", "hu",
        "Icelandic", "ice", "is",
        "Ido", "ido", "io",
        "Igbo", "ibo", "ig",
        "Indonesian", "ind", "id",
        "Interlingua", "ina", "ia",
        "Interlingue", "ile", "ie",
        "Inuktitut", "iku", "iu",
        "Inupiaq", "ipk", "ik",
        "Irish", "gle", "ga",
        "Italian", "ita", "it",
        "Japanese", "jpn", "ja",
        "Javanese", "jav", "jv",
        "Kannada", "kan", "kn",
        "Kanuri", "kau", "kr",
        "Kashmiri", "kas", "ks",
        "Kazakh", "kaz", "kk",
        "Khmer", "khm", "km",
        "Kikuyu", "kik", "ki",
        "Kinyarwanda", "kin", "rw",
        "Kirghiz", "kir", "ky",
        "Komi", "kom", "kv",
        "Kongo", "kon", "kg",
        "Korean", "kor", "ko",
        "Kuanyama", "kua", "kj",
        "Kurdish", "kur", "ku",
        "Kwanyama", "kua", "kj",
        "Lao", "lao", "lo",
        "Latin", "lat", "la",
        "Latvian", "lav", "lv",
        "Luxembourgish", "ltz", "lb",
        "Limburgan", "lim", "li",
        "Lingala", "lin", "ln",
        "Lithuanian", "lit", "lt",
        "Luba-Katanga", "lub", "lu",
        "Macedonian", "mac", "mk",
        "Malagasy", "mlg", "mg",
        "Malay", "may", "ms",
        "Malayalam", "mal", "ml",
        "Maltese", "mlt", "mt",
        "Manx", "glv", "gv",
        "Maori", "mao", "mi",
        "Marathi", "mar", "mr",
        "Marshallese", "mah", "mh",
        "Moldavian", "mol", "mo",
        "Mongolian", "mon", "mn",
        "Nauru", "nau", "na",
        "Navaho", "nav", "nv",
        "Ndonga", "ndo", "ng",
        "Nepali", "nep", "ne",
        "Northern Sami", "sme", "se",
        "North Ndebele", "nde", "nd",
        "Norwegian", "nor", "no",
        "Nyanja", "nya", "ny",
        "Ojibwa", "oji", "oj",
        "Oriya", "ori", "or",
        "Oromo", "orm", "om",
        "Ossetian", "oss", "os",
        "Pali", "pli", "pi",
        "Panjabi", "pan", "pa",
        "Persian", "per", "fa",
        "Polish", "pol", "pl",
        "Portuguese", "por", "pt",
        "Pushto", "pus", "ps",
        "Quechua", "que", "qu",
        "Raeto-Romance", "roh", "rm",
        "Romanian", "rum", "ro",
        "Rundi", "run", "rn",
        "Russian", "rus", "ru",
        "Samoan", "smo", "sm",
        "Sango", "sag", "sg",
        "Sanskrit", "san", "sa",
        "Sardinian", "srd", "sc",
        "Serbian", "scc", "sr",
        "Shona", "sna", "sn",
        "Sichuan Yi", "iii", "ii",
        "Sindhi", "snd", "sd",
        "Sinhala", "sin", "si",
        "Slovak", "slo", "sk",
        "Slovenian", "slv", "sl",
        "Somali", "som", "so",
        "Spanish", "spa", "es",
        "Sundanese", "sun", "su",
        "Swahili", "swa", "sw",
        "Swedish", "swe", "sv",
        "Tagalog", "tgl", "tl",
        "Tahitian", "tah", "ty",
        "Tajik", "tgk", "tg",
        "Tamil", "tam", "ta",
        "Tatar", "tat", "tt",
        "Telugu", "tel", "te",
        "Thai", "tha", "th",
        "Tibetan", "tib", "bo",
        "Tigrinya", "tir", "ti",
        "Tonga", "ton", "to",
        "Tsonga", "tso", "ts",
        "Tswana", "tsn", "tn",
        "Turkish", "tur", "tr",
        "Turkmen", "tuk", "tk",
        "Twi", "twi", "tw",
        "Uighur", "uig", "ug",
        "Ukrainian", "ukr", "uk",
        "Urdu", "urd", "ur",
        "Uzbek", "uzb", "uz",
        "Venda", "ven", "ve",
        "Vietnamese", "vie", "vi",
        "Welsh", "wel", "cy",
        "Wolof", "wol", "wo",
        "Xhosa", "xho", "xh",
        "Yiddish", "yid", "yi",
        "Yoruba", "yor", "yo",
        "Zulu", "zul", "zu",
    };

    public static HashMap<String,String> nameToCode6391 = null;
    public static HashMap<String,String> nameToCode6392 = null;
    public static HashMap<String,String> code6391ToName = null;
    public static HashMap<String,String> code6392ToName = null;


    public static String getNameFor6391Code(String code) {
        if(code == null) return null;
        if(code6391ToName == null) initializeHashMaps();
        return code6391ToName.get(code.toLowerCase());
    }

    public static String getNameFor6392Code(String code) {
        if(code == null) return null;
        if(code6392ToName == null) initializeHashMaps();
        return code6392ToName.get(code.toLowerCase());
    }

    public static String get6391ForName(String name) {
        if(name == null) return null;
        if(nameToCode6391 == null) initializeHashMaps();
        return nameToCode6391.get(name.toLowerCase());
    }


    public static void initializeHashMaps() {
        nameToCode6391 = new HashMap();
        nameToCode6392 = new HashMap();
        code6391ToName = new HashMap();
        code6392ToName = new HashMap();
        for(int i=0; i<languageNamesAndCodes.length; i=i+3) {
            nameToCode6392.put(languageNamesAndCodes[i].toLowerCase(), languageNamesAndCodes[i+1]);
            nameToCode6391.put(languageNamesAndCodes[i].toLowerCase(), languageNamesAndCodes[i+2]);
            code6391ToName.put(languageNamesAndCodes[i+2], languageNamesAndCodes[i]);
            code6392ToName.put(languageNamesAndCodes[i+1], languageNamesAndCodes[i]);
        }
    }




    public static Topic createTopicForLanguageName(String language, TopicMap tm) {
        Topic t = null;
        if(tm != null) {
            try {
                t = tm.createTopic();
                String code = get6391ForName(language);
                if(code == null) code = language.toLowerCase();
                t.addSubjectIdentifier(new Locator(XTMPSI.LANG_PREFIX+code));
                t.setBaseName(toTitleCase(language) + " language");
                t.setDisplayName("en", toTitleCase(language));
                t.addType(tm.getTopic(TMBox.LANGUAGE_SI));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }




    
    public static Topic createTopicForLanguageCode(String code, TopicMap tm) {
        Topic t = null;
        if(tm != null) {
            try {
                t = tm.createTopic();
                String name = getNameFor6391Code(code);
                if(name == null) name = code.toLowerCase();
                t.addSubjectIdentifier(new Locator(XTMPSI.LANG_PREFIX+code));
                t.setBaseName(toTitleCase(name) + " language");
                t.setDisplayName("en", toTitleCase(name));
                t.addType(tm.getTopic(TMBox.LANGUAGE_SI));
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }


    public static String getNameForLangTopic(Topic langTopic) {
        String langName = null;
        if(langTopic != null) {
            try {
                langName = langTopic.getDisplayName("en");
                if(langName != null) {
                    langName = langName.toLowerCase();
                    if(nameToCode6391 == null) initializeHashMaps();
                    if(nameToCode6391.containsKey(langName)) return langName;
                }
                String langCode = null;
                for(Locator l : langTopic.getSubjectIdentifiers()) {
                    if(l != null) {
                        if(l.toExternalForm().startsWith(XTMPSI.LANG_PREFIX)) {
                            langCode = l.toExternalForm().substring(XTMPSI.LANG_PREFIX.length());
                            langName = code6391ToName.get(langCode);
                            if(langName != null) break;
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }

        }
        return langName;
    }




    public static String getCodeForLangTopic(Topic langTopic) {
        if(langTopic != null) {
            try {
                String langName = langTopic.getDisplayName("en");
                if(langName != null) {
                    langName = langName.toLowerCase();
                    if(nameToCode6391 == null) initializeHashMaps();
                    if(nameToCode6391.containsKey(langName) ) return nameToCode6391.get(langName);
                }
                String langCode = null;
                for(Locator l : langTopic.getSubjectIdentifiers()) {
                    if(l != null) {
                        if(l.toExternalForm().startsWith(XTMPSI.LANG_PREFIX)) {
                            langCode = l.toExternalForm().substring(XTMPSI.LANG_PREFIX.length());
                            if(code6391ToName.containsKey(langCode)) return langCode;
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }




    public static String toTitleCase(String str) {
        if(str != null) {
            return str.substring(0,1).toUpperCase()+str.substring(1).toLowerCase();
        }
        return null;
    }




}
