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
 */

package org.wandora.application.tools.extractors.europeana;

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
 * @author nlaitinen
 */


public abstract class AbstractEuropeanaExtractor extends AbstractExtractor {

    
    @Override
    public String getName() {
        return "Abstract Europeana API extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extracts data from The Europeana data API at http://pro.europeana.eu";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_europeana.png");
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
        throw new UnsupportedOperationException("This extractor is a frontend for other Europeana extractors. It doesn't perform extration it self.");
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other Europeana extractors. It doesn't perform extration it self.");
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("This extractor is a frontend for other Europeana extractors. It doesn't perform extration it self.");
    }
    
     

  // --------------------------- SEARCH ----------------------------------------
     
        public static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
        public static final String EUROPEANA_SI = "http://pro.europeana.eu";
        public static final String ITEM_SI = "http://wandora.org/si/europeana/item";
        public static final String PROVIDER_SI = "http://wandora.org/si/europeana/item/provider";
        public static final String LANGUAGE_SI = "http://wandora.org/si/europeana/item/language";
        public static final String YEAR_SI = "http://wandora.org/si/europeana/item/year";
        public static final String RIGHTS_LINK_SI = "http://wandora.org/si/europeana/item/rightsLink";
        public static final String TITLE_SI = "http://wandora.org/si/europeana/item/title";
        public static final String DC_CREATOR_SI = "http://wandora.org/si/europeana/item/dcCreator";
        public static final String COUNTRY_SI = "http://wandora.org/si/europeana/item/country";
        public static final String COLLECTION_NAME_SI = "http://wandora.org/si/europeana/item/collectionName";
        public static final String CONCEPT_LABEL_SI = "http://wandora.org/si/europeana/item/conceptLabel";
        public static final String TYPE_SI = "http://wandora.org/si/europeana/item/type";
        public static final String DATA_PROVIDER_SI = "http://wandora.org/si/europeana/item/dataProvider";
        public static final String PLACE_LABEL_SI = "http://wandora.org/si/europeana/item/placeLabel";
        public static final String PREVIEW_LINK_SI = "http://wandora.org/si/europeana/item/previewLink";
        public static final String GUID_LINK_SI = "http://wandora.org/si/europeana/item/guidLink";
       


        protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
            Topic lang = getOrCreateTopic(tm, LANG_SI);
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
        
        public static Topic getEuropeanaTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, EUROPEANA_SI, "Europeana API");
            Topic wandoraClass = getWandoraClassTopic(tm);
            makeSubclassOf(tm, type, wandoraClass);
            return type;
        }


        
        public static Topic getItemTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, ITEM_SI, "item (Europeana API)");
            Topic nytTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, nytTopic);
            return type;
        }
        
        public static Topic getProviderTopic(String provider, TopicMap tm) throws TopicMapException {
            Topic resTopic=getOrCreateTopic(tm, provider);
            resTopic.addType(getProviderTypeTopic(tm));
            return resTopic;
        }
        
        public static Topic getProviderTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, PROVIDER_SI, "provider (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getLanguageTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, LANGUAGE_SI, "language (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getYearTopic(String year, TopicMap tm) throws TopicMapException {
            Topic resTopic=getOrCreateTopic(tm, year);
            resTopic.addType(getYearTypeTopic(tm));
            return resTopic;
        }
        
        public static Topic getYearTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, YEAR_SI, "year (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getRightsLinkTopic(String rights, TopicMap tm) throws TopicMapException {
            Topic resTopic=getOrCreateTopic(tm, rights);
            resTopic.addType(getRightsLinkTypeTopic(tm));
            return resTopic;
        }
        
        public static Topic getRightsLinkTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, RIGHTS_LINK_SI, "rights-link (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getTitleTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, TITLE_SI, "title (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getDcCreatorTopic(String dcCreator, TopicMap tm) throws TopicMapException {
            Topic resTopic=getOrCreateTopic(tm, dcCreator);
            resTopic.addType(getDcCreatorTypeTopic(tm));
            return resTopic;
        }
        
        public static Topic getDcCreatorTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, DC_CREATOR_SI, "dcCreator (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getCountryTopic(String country, TopicMap tm) throws TopicMapException {
            Topic resTopic=getOrCreateTopic(tm, country);
            resTopic.addType(getCountryTypeTopic(tm));
            return resTopic;
        }
        
        public static Topic getCountryTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, COUNTRY_SI, "country (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getCollectionNameTopic(String collectionName, TopicMap tm) throws TopicMapException {
            Topic resTopic=getOrCreateTopic(tm, collectionName);
            resTopic.addType(getCollectionNameTypeTopic(tm));
            return resTopic;
        }
        
        public static Topic getCollectionNameTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, COLLECTION_NAME_SI, "collection-name (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getConceptLabelTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, CONCEPT_LABEL_SI, "concept-label (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getTypeTopic(String type, TopicMap tm) throws TopicMapException {
            Topic resTopic=getOrCreateTopic(tm, type);
            resTopic.addType(getTypeTypeTopic(tm));
            return resTopic;
        }
        
        public static Topic getTypeTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, TYPE_SI, "type (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getDataProviderTopic(String dataProvider, TopicMap tm) throws TopicMapException {
            Topic resTopic=getOrCreateTopic(tm, dataProvider);
            resTopic.addType(getDataProviderTypeTopic(tm));
            return resTopic;
        }
        
        public static Topic getDataProviderTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, DATA_PROVIDER_SI, "data-provider (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getPlaceLabelTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, PLACE_LABEL_SI, "place-label (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getPreviewLinkTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, PREVIEW_LINK_SI, "preview-link (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
        
        public static Topic getGuidLinkTypeTopic(TopicMap tm) throws TopicMapException {
            Topic type=getOrCreateTopic(tm, GUID_LINK_SI, "guid-link (Europeana API)");
            Topic europeanaTopic = getEuropeanaTypeTopic(tm);
            makeSubclassOf(tm, type, europeanaTopic);
            return type;
        }
}
