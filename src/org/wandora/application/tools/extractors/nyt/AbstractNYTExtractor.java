/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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


package org.wandora.application.tools.extractors.nyt;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;




public abstract class AbstractNYTExtractor extends AbstractExtractor {

    @Override
    public String getName() {
        return "Abstract New York Times API extractor";
    }

    @Override
    public String getDescription(){
        return "Abstract extractor for The New York Times API.";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_nyt.png");
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
    
    
    // -------------------------------------------------------------------------
    
    // ------------------------------------------------------ ARTICLE SEARCH ---
    
    
    
    public static final String DBPEDIA_RESOURCE_SI = "http://wandora.org/si/nytimes/dbpedia";
    public static final String BYLINE_SI = "http://wandora.org/si/nytimes/byline";    
    public static final String ARTICLE_SI = "http://api.nytimes.com/svc/search/v1/article";
    public static final String BODY_SI = "http://wandora.org/si/nytimes/body";
    public static final String TEXT_SI = "http://wandora.org/si/nytimes/text";
    public static final String ABSTRACT_SI = "http://wandora.org/si/nytimes/abstract";
    public static final String LEAD_PARAGRAPH_SI = "http://wandora.org/si/nytimes/lead-paragraph";
    public static final String DATE_SI = "http://wandora.org/si/nytimes/date";
    public static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#en";
    public static final String FACET_SI = "http://wandora.org/si/nytimes/facet";
    public static final String DES_FACET_SI = "http://wandora.org/si/nytimes/facet/des";
    public static final String GEO_FACET_SI = "http://wandora.org/si/nytimes/facet/geo";
    public static final String ORG_FACET_SI = "http://wandora.org/si/nytimes/facet/org";
    public static final String PER_FACET_SI = "http://wandora.org/si/nytimes/facet/per";
    public static final String SOURCE_FACET_SI = "http://wandora.org/si/nytimes/facet/source";
    public static final String CLASSIFIER_FACET_SI = "http://wandora.org/si/nytimes/facet/classifier";
    public static final String COLUMN_FACET_SI = "http://wandora.org/si/nytimes/facet/column";
    public static final String MATERIAL_TYPE_FACET_SI = "http://wandora.org/si/nytimes/facet/material-type";
    public static final String NYT_SI = "http://www.nytimes.com";
    
    
    
    
    
    
    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        Topic lang = getOrCreateTopic(tm, LANG_SI);
        return lang;
    }
    
    
    
    public static Topic getBylineTopic(String byline, TopicMap tm) throws TopicMapException {
        Topic facetTopic=getOrCreateTopic(tm, BYLINE_SI+urlEncode(byline), byline);
        facetTopic.addType(getBylineTypeTopic(tm));
        return facetTopic;
    }
    
    
    
    public static Topic getBylineTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, BYLINE_SI, "byline (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    public static Topic getMaterialTypeFacetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, MATERIAL_TYPE_FACET_SI, "article-material-type (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    public static Topic getColumnFacetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, COLUMN_FACET_SI, "article-column-facet (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getClassifierFacetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, CLASSIFIER_FACET_SI, "article-classifier-facet (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    public static Topic getPerFacetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, PER_FACET_SI, "article-per-facet (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    
    public static Topic getOrgFacetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ORG_FACET_SI, "article-org-facet (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
   
    
    public static Topic getGeoFacetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, GEO_FACET_SI, "article-geo-facet (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    public static Topic getSourceFacetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SOURCE_FACET_SI, "article-source-facet (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }

    
    public static Topic getDesFacetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DES_FACET_SI, "article-des-facet (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    
    public static Topic getFacetTopic(String facet, TopicMap tm) throws TopicMapException {
        Topic facetTopic=getOrCreateTopic(tm, FACET_SI+"/"+urlEncode(facet), facet);
        facetTopic.addType(getFacetTypeTopic(tm));
        return facetTopic;
    }
    
    
    
    public static Topic getFacetTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, FACET_SI, "article-facet (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    public static Topic getDBpediaResourceTopic(String res, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, res);
        resTopic.addType(getDBpediaResourceTypeTopic(tm));
        return resTopic;
    }
    
    
    
    public static Topic getDBpediaResourceTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DBPEDIA_RESOURCE_SI, "article-dbpedia-resource (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    
    
    public static Topic getAbstractTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ABSTRACT_SI, "article-abstract (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    
    public static Topic getBodyTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, BODY_SI, "article-body (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    public static Topic getTextTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TEXT_SI, "article-text (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getLeadParagraphTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, LEAD_PARAGRAPH_SI, "article-lead-paragraph (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    public static Topic getArticleTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ARTICLE_SI, "article (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    public static Topic getDateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DATE_SI, "article-date (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    
    
    
    public static Topic getNYTTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, NYT_SI, "New York Times API");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
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

    // ------------------------------------------------------ EVENT SEARCH -----
    
    // Also uses getNYTTypeTopic from above
    
    public static final String EVENT_SI = "http://api.nytimes.com/svc/events/v2";
    public static final String EVENT_DETAIL_URL_SI = "http://wandora.org/si/nytimes/event/url";
    public static final String EVENT_NAME_SI = "http://wandora.org/si/nytimes/event/name";
    public static final String EVENT_DESCRIPTION_SI = "http://wandora.org/si/nytimes/event/description";
    public static final String EVENT_VENUE_SI = "http://wandora.org/si/nytimes/event/venue";
    public static final String EVENT_CATEGORY_SI = "http://wandora.org/si/nytimes/event/category";
    public static final String EVENT_LATITUDE_SI = "http://wandora.org/si/nytimes/event/latitude";
    public static final String EVENT_LONGITUDE_SI = "http://wandora.org/si/nytimes/event/longitude";
    public static final String EVENT_DATE_SI = "http://wandora.org/si/nytimes/eventDate";
    public static final String START_DATE_SI = "http://wandora.org/si/nytimes/startDate";
    public static final String END_DATE_SI = "http://wandora.org/si/nytimes/endDate";
    public static final String WEEKDAY_SI = "http://wandora.org/si/nytimes/dayOfWeek/";
    public static final String RECURRING_DAY_SI = "http://wandora.org/si/nytimes/recurringDay/";
    
    public static Topic getEventTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EVENT_SI, "Event (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getDescriptionTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EVENT_DESCRIPTION_SI, "Event description (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getLatitudeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EVENT_LATITUDE_SI, "Event latitude (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getLongitudeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EVENT_LONGITUDE_SI, "Event longitude (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }    
    
    public static Topic getVenueTopic(String venue, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, venue);
        resTopic.addType(getVenueTypeTopic(tm));
        return resTopic;
    }
    
    public static Topic getVenueTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EVENT_VENUE_SI, "Event venue (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getCategoryTopic(String category, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, category);
        resTopic.addType(getCategoryTypeTopic(tm));
        return resTopic;
    }
    
    public static Topic getCategoryTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EVENT_CATEGORY_SI, "Event category (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getEventDateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EVENT_DATE_SI, "Event date (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getStartDateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, START_DATE_SI, "Event start date (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getEndDateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, END_DATE_SI, "Event end date (New York Times API)");
        Topic nytTopic = getNYTTypeTopic(tm);
        makeSubclassOf(tm, type, nytTopic);
        return type;
    }
    
    public static Topic getDayOfWeekTypeTopic(TopicMap tm) throws TopicMapException {
      Topic type = getOrCreateTopic(tm, WEEKDAY_SI, "Day of Week (New York Times API)");
      Topic nytTopic = getNYTTypeTopic(tm);
      makeSubclassOf(tm, type, nytTopic);
      return type;
    }
    
    public static Topic getRecurringDayTypeTopic(TopicMap tm) throws TopicMapException{
      return getOrCreateTopic(tm, RECURRING_DAY_SI, "Recurring Day (New York Times API)");
    }
    
    private enum DayOfWeek{
      MONDAY    ("monday",    "Monday"),
      TUESDAY   ("tuesday",   "Tuesday"),
      WEDNESDAY ("wednesday", "Wednesday"),
      THURSDAY  ("thursday",  "Thursday"),
      FRIDAY    ("friday",    "Friday"),
      SATURDAY  ("saturday",  "Saturday"),
      SUNDAY    ("sunday",    "Sunday");
      
      public final String urlStub;
      public final String display;
      
      DayOfWeek(String urlStub, String display){
        this.urlStub = urlStub;
        this.display = display;
      }

    }
    
    static final Map<String, DayOfWeek> abbrToDayOfWeek = new HashMap<String, DayOfWeek>(){{
      put("mon", DayOfWeek.MONDAY);
      put("tue", DayOfWeek.TUESDAY);
      put("wed", DayOfWeek.WEDNESDAY);
      put("thu", DayOfWeek.THURSDAY);
      put("fri", DayOfWeek.FRIDAY);
      put("sat", DayOfWeek.SATURDAY);
      put("sun", DayOfWeek.SUNDAY);
    }};
    
    public static Topic getRecurringDayTopic(TopicMap tm, String abbr) throws TopicMapException{
      
      DayOfWeek wd = abbrToDayOfWeek.get(abbr);
      
      String si = WEEKDAY_SI + wd.urlStub;
      String baseName = wd.display + " (New York Times API)";
      Topic dayTopic = getOrCreateTopic(tm, si, baseName);
      
      dayTopic.addType(getDayOfWeekTypeTopic(tm));
      
      return dayTopic;
    }
    
}
