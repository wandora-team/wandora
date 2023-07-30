/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2013 Wandora Team
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
 */

package org.wandora.application.tools.extractors.discogs;

import java.io.File;
import java.net.URL;

import javax.swing.Icon;

import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author nlaitine
 */

public class AbstractDiscogsExtractor  extends AbstractExtractor {


	private static final long serialVersionUID = 1L;
	
	
	// Default language of occurrences and variant names.
    public static String LANG = "en";
    public static String LANGUAGE_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
    
    public static final String DISCOGS_SI = "http://www.discogs.com";
    public static final String DISCOG_API_SI = "http://api.discogs.com";
    public static final String RELEASE_SI = "http://www.discogs.com/release";
    public static final String MASTER_SI = "http://www.discogs.com/master";
    public static final String ARTIST_SI = "http://www.discogs.com/artist";
    public static final String LABEL_SI = "http://www.discogs.com/label";
    
    public static final String TYPE_SI = DISCOG_API_SI + "/type";
    public static final String TITLE_SI = DISCOG_API_SI + "/title";
    public static final String COUNTRY_SI = DISCOG_API_SI + "/country";
    public static final String YEAR_SI = DISCOG_API_SI + "/year";
    public static final String STYLE_SI = DISCOG_API_SI + "/style";
    public static final String GENRE_SI = DISCOG_API_SI + "/genre";
    public static final String FORMAT_SI =  DISCOG_API_SI + "/format";
    public static final String IMAGE_SI = DISCOG_API_SI + "/image";    
    public static final String CATNO_SI = DISCOG_API_SI + "/catno";
    public static final String BARCODE_SI = DISCOG_API_SI + "/barcode";

    
    @Override
    public String getName() {
        return "Abstract Discogs API extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extracts data from The Discogs data API at http://api.discogs.com";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_discogs.png");
    }

    private final String[] contentTypes=new String[] { "text/plain", "text/json", "application/json" };
    
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    @Override
    public boolean runInOwnThread() {
        return false;
    }
    
// -----------------------------------------------------------------------------
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other Discogs extractors. It doesn't perform extration it self.");
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other Discogs extractors. It doesn't perform extration it self.");
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other Discogs extractors. It doesn't perform extration it self.");
    }
    
// --------------------------- TOPIC MAPS ------------------------------------
    
    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        Topic lang = getOrCreateTopic(tm, LANGUAGE_SI);
        return lang;
    }

    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    public static Topic getDiscogsTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, DISCOG_API_SI, "Discogs API");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }

// ------------------------------- TOPICS ------------------------------------

    public static Topic getReleaseTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, RELEASE_SI, "release (Discogs API)");
        Topic nytTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getMasterTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, MASTER_SI, "master (Discogs API)");
        Topic nytTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getArtistTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, ARTIST_SI, "artist (Discogs API)");
        Topic nytTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getTitleTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TITLE_SI, "title (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
    public static Topic getCountryTopic(String provider, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, provider);
        resTopic.addType(getCountryTypeTopic(tm));
        return resTopic;
    }
    
    public static Topic getCountryTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, COUNTRY_SI, "country (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
    public static Topic getYearTopic(String provider, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, provider);
        resTopic.addType(getYearTypeTopic(tm));
        return resTopic;
    }
    
    public static Topic getYearTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, YEAR_SI, "year (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
    public static Topic getStyleTopic(String provider, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, provider);
        resTopic.addType(getStyleTypeTopic(tm));
        return resTopic;
   }
    
    public static Topic getStyleTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, STYLE_SI, "style (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
    public static Topic getGenreTopic(String provider, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, provider);
        resTopic.addType(getGenreTypeTopic(tm));
        return resTopic;
   }
    
    public static Topic getGenreTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, GENRE_SI, "genre (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
    public static Topic getLabelTopic(String provider, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, provider);
        resTopic.addType(getLabelTypeTopic(tm));
        return resTopic;
    }
    
    public static Topic getLabelTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, LABEL_SI, "label (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
        
    public static Topic getFormatTopic(String provider, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, provider);
        resTopic.addType(getFormatTypeTopic(tm));
        return resTopic;
    }
    
    public static Topic getFormatTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, FORMAT_SI, "format (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
    public static Topic getImageTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, IMAGE_SI, "image (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
    public static Topic getCatnoTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, CATNO_SI, "catno (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
    public static Topic getBarcodeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, BARCODE_SI, "barcode (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
    public static Topic getTypeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TYPE_SI, "type (Discogs API)");
        Topic discogsTopic = getDiscogsTypeTopic(tm);
        makeSubclassOf(tm, type, discogsTopic);
        return type;
    }
    
}