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
 */


package org.wandora.application.tools.extractors.stands4;


import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.extractors.*;
import javax.swing.*;

import java.net.*;



/**
 *
 * @author akivela
 */
public abstract class AbstractStands4Extractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;

	protected String STANDS4_SI = "http://www.abbreviations.com/api.asp";

    protected String SYNONYMS_BASE = "http://www.synonyms.net/";
    protected String SYNONYMS_TERM_BASE = SYNONYMS_BASE+"synonym/";
    
    protected String DEFINITIONS_BASE = "http://www.definitions.net/";
    protected String DEFINITIONS_TERM_BASE = DEFINITIONS_BASE+"definition/";


    protected String ABBREVIATIONS_BASE = "http://www.abbreviations.com/";
    protected String ABBREVIATIONS_TERM_BASE = "http://www.abbreviations.com/bs.aspx?st=";

    protected String CATEGORY_BASE = "http://www.abbreviations.com/acronyms/";

    protected String ABBREVIATION_SI = ABBREVIATIONS_BASE;
    protected String DEFINITION_SI = DEFINITIONS_BASE;

    protected String CATEGORY_SI = CATEGORY_BASE+"category";
    protected String PARTOFSPEECH_SI = SYNONYMS_BASE+"part-of-speech";
    protected String EXAMPLE_SI = DEFINITIONS_BASE+"example";
    protected String SIMILARTERM_SI = SYNONYMS_BASE+"similar-term";
    protected String SYNONYMTERM_SI = SYNONYMS_BASE+"synonym-term";
    protected String ANTONYMTERM_SI = SYNONYMS_BASE+"antonym-term";

    protected String SOURCE_SI = "http://wandora.org/si/source";

    protected String TERM_COMPLEX_BASE = "http://wandora.org/si/term-complex/";
    protected String defaultEncoding = "ISO-8859-1";
    protected String defaultLang = "en";

    



    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_stands4.png");
    }


    private final String[] contentTypes=new String[] { "text/xml", "application/xml" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }



    // -------------------------------------------------------------------------



    protected String solveQueryTerm( URL url ) {
        String queryTerm = null;
        String urlStr = url.toExternalForm();
        int i = urlStr.indexOf("term=");
        if(i != -1) {
            urlStr = urlStr.substring(0, i);
            i = urlStr.lastIndexOf("&");
            if(i != -1) {
                queryTerm = urlStr.substring(i+1);
                try {
                    queryTerm = URLDecoder.decode(queryTerm, defaultEncoding);
                }
                catch(Exception e) {
                    // DO NOTHING...
                }
            }
        }
        return queryTerm;
    }


    // -------------------------------------------------------------------------


    public abstract String getTermBase();
    public abstract String getTermType();





    public Topic getStands4Type(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, STANDS4_SI, "Stands4");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }





    public Topic getAbbreviationType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, ABBREVIATION_SI, "Abbreviation");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }


    // -------

    
    public Topic getSimilarTermType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SIMILARTERM_SI, "Similar term");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }

    // -------


    public Topic getExampleType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EXAMPLE_SI, "Example");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }



   // -------


    public Topic getPartOfSpeechTopic(String p, TopicMap tm) throws TopicMapException {
        if(p != null) {
            p = p.trim();
            if(p.length() > 0) {
                Topic partOfSpeechTopic=getOrCreateTopic(tm, PARTOFSPEECH_SI+"/"+p, p);
                Topic partOfSpeechTypeTopic = getPartOfSpeechType(tm);
                partOfSpeechTopic.addType(partOfSpeechTypeTopic);
                return partOfSpeechTopic;
            }
        }
        return null;
    }




    public Topic getPartOfSpeechType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, PARTOFSPEECH_SI, "Part of speech");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }


    // -------


    public Topic getSynonymTermType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SYNONYMTERM_SI, "Synonym");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }


    // -------
    

    public Topic getAntonymTermType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ANTONYMTERM_SI, "Antonym");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }


    // -------


    public Topic getDefinitionTopic(String def, TopicMap tm) throws TopicMapException {
        if(def != null) {
            def = def.trim();
            if(def.length() > 0) {
                String si = null;
                try {
                    si = DEFINITION_SI+"definition/"+URLEncoder.encode(def, defaultEncoding);
                }
                catch(Exception e) {
                    si = DEFINITION_SI+"definition/"+def;
                }
                Topic defTopic=getOrCreateTopic(tm, si, def);
                Topic defTypeTopic = getDefinitionType(tm);
                defTopic.addType(defTypeTopic);
                return defTopic;
            }
        }
        return null;
    }




    public Topic getDefinitionType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DEFINITION_SI, "Definition");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }


// -------



    public Topic getTermComplexTopic(String term, String def, TopicMap tm) throws TopicMapException {
        if(term != null) {
            term = term.trim();
            if(def == null) def = "";
            def = def.trim();
            if(def.length()>50) def = def.substring(0,50)+"...";
            if(term.length() > 0) {
                String siTerm = term.replaceAll(", ", "_");
                String si = null;
                try {
                    si = TERM_COMPLEX_BASE+URLEncoder.encode(siTerm, defaultEncoding);
                    si = si + "#" + Math.abs(2*def.hashCode()+(def.hashCode()<0?1:0));
                }
                catch(Exception e) {
                    si = TERM_COMPLEX_BASE+siTerm+"#"+Math.abs(2*def.hashCode()+(def.hashCode()<0?1:0));
                }
                Topic termTopic=getOrCreateTopic(tm, si, term+" ("+def+")");
                termTopic.setDisplayName("en", term);
                Topic termTypeTopic = getTermComplexType(tm);
                termTopic.addType(termTypeTopic);
                return termTopic;
            }
        }
        return null;
    }



    public Topic getTermComplexType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TERM_COMPLEX_BASE, "Term complex");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }



    // -------
    // -------



    public Topic getTermTopic(String term, TopicMap tm) throws TopicMapException {
        if(term != null) {
            term = term.trim();
            if(term.length() > 0) {
                String si = null;
                try {
                    si = getTermBase()+URLEncoder.encode(term, defaultEncoding);
                }
                catch(Exception e) {
                    si = getTermBase()+term;
                }
                Topic termTopic=getOrCreateTopic(tm, si, term);
                termTopic.setDisplayName("en", term);
                Topic termTypeTopic = getTermType(tm);
                termTopic.addType(termTypeTopic);
                return termTopic;
            }
        }
        return null;
    }



    public Topic getTermType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, getTermType(), "Term");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }



    // -------

    public Topic getCategoryType(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, CATEGORY_SI, "Category");
        Topic superType = getStands4Type(tm);
        makeSubclassOf(tm, type, superType);
        return type;
    }

    public Topic getCategoryTopic(String cat, TopicMap tm) throws TopicMapException {
        if(cat != null) {
            cat = cat.trim();
            if(cat.length() > 0) {
                String si = null;
                try {
                    si = CATEGORY_BASE+URLEncoder.encode(cat, defaultEncoding);
                }
                catch(Exception e) {
                    si = CATEGORY_BASE+cat;
                }
                Topic catTopic=getOrCreateTopic(tm, si, cat);
                Topic catTypeTopic = getCategoryType(tm);
                catTopic.addType(catTypeTopic);
                return catTopic;
            }
        }
        return null;
    }



    // ------------------------


    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }

    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SOURCE_SI, "Source");
    }


    // --------

    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }




    // -------------------------------------------------------------------------



    private static String apikey = null;
    protected String solveAPIKey(Wandora wandora) {
        if(apikey == null) {
            apikey = "";
            apikey = WandoraOptionPane.showInputDialog(wandora, "Please give a valid apikey for Stands4. You can register your personal apikey at http://www.abbreviations.com/api.asp", apikey, "Stands4 apikey", WandoraOptionPane.QUESTION_MESSAGE);
        }
        return apikey;
    }




    public void forgetAuthorization() {
        apikey = null;
    }



}
