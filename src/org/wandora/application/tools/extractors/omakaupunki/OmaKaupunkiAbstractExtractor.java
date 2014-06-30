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


package org.wandora.application.tools.extractors.omakaupunki;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import javax.swing.Icon;
import org.json.JSONObject;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.IObox;

/**
 *
 * @author akivela
 */


public abstract class OmaKaupunkiAbstractExtractor extends AbstractExtractor {

    public static boolean USE_EXISTING_TOPICS = false;
    
    public static final String OMA_KAUPUNKI_SI = "http://omakaupunki.fi/";
    
    // ****** EVENTS ******
    public static final String OMA_KAUPUNKI_EVENT_SI = OMA_KAUPUNKI_SI + "event/";
    public static final String OMA_KAUPUNKI_EVENT_START_TIME_SI = OMA_KAUPUNKI_EVENT_SI + "start-time";
    public static final String OMA_KAUPUNKI_EVENT_END_TIME_SI = OMA_KAUPUNKI_EVENT_SI + "end-time";
    public static final String OMA_KAUPUNKI_EVENT_CREATED_SI = OMA_KAUPUNKI_EVENT_SI + "created";
    public static final String OMA_KAUPUNKI_EVENT_TITLE_SI = OMA_KAUPUNKI_EVENT_SI + "title";
    public static final String OMA_KAUPUNKI_EVENT_BODY_SI = OMA_KAUPUNKI_EVENT_SI + "body";
    public static final String OMA_KAUPUNKI_EVENT_CATEGORY_PARENT_SI = OMA_KAUPUNKI_EVENT_SI + "category-parent";
    public static final String OMA_KAUPUNKI_EVENT_CATEGORY_PLURAL_SI = OMA_KAUPUNKI_EVENT_SI + "category-plural";
    public static final String OMA_KAUPUNKI_EVENT_CATEGORY_IDENTIFIER_SI = OMA_KAUPUNKI_EVENT_SI + "category-identifier";
    public static final String OMA_KAUPUNKI_EVENT_CATEGORY_SI = OMA_KAUPUNKI_SI + "category";
    public static final String OMA_KAUPUNKI_EVENT_VENUE_SI = OMA_KAUPUNKI_SI + "venue";
    public static final String OMA_KAUPUNKI_EVENT_IDENTIFIER_SI = OMA_KAUPUNKI_SI + "identifier";
    
    // ****** SERVICES ******
    public static final String OMA_KAUPUNKI_SERVICE_SI = OMA_KAUPUNKI_SI + "service/";
    public static final String OMA_KAUPUNKI_SERVICE_IDENTIFIER_SI = OMA_KAUPUNKI_SERVICE_SI + "identifier";
    public static final String OMA_KAUPUNKI_SERVICE_CREATED_SI = OMA_KAUPUNKI_SERVICE_SI + "created";
    public static final String OMA_KAUPUNKI_SERVICE_DESCRIPTION_SI = OMA_KAUPUNKI_SERVICE_SI + "description";
    public static final String OMA_KAUPUNKI_SERVICE_CATEGORY_SI = OMA_KAUPUNKI_SERVICE_SI + "category";
    public static final String OMA_KAUPUNKI_SERVICE_CATEGORY_PARENT_SI = OMA_KAUPUNKI_SERVICE_SI + "category-parent";
    public static final String OMA_KAUPUNKI_SERVICE_CATEGORY_PLURAL_SI = OMA_KAUPUNKI_SERVICE_SI + "category-plural";
    public static final String OMA_KAUPUNKI_SERVICE_CATEGORY_IDENTIFIER_SI = OMA_KAUPUNKI_SERVICE_SI + "category-identifier";
    public static final String OMA_KAUPUNKI_SERVICE_PHONE_SI = OMA_KAUPUNKI_SERVICE_SI + "phone";
    public static final String OMA_KAUPUNKI_SERVICE_TITLE_SI = OMA_KAUPUNKI_SERVICE_SI + "title";
    public static final String OMA_KAUPUNKI_SERVICE_TAG_SI = OMA_KAUPUNKI_SERVICE_SI + "tag";
    public static final String OMA_KAUPUNKI_SERVICE_ADDRESS_SI = OMA_KAUPUNKI_SERVICE_SI + "address";
    public static final String OMA_KAUPUNKI_SERVICE_HOMEPAGE_SI = OMA_KAUPUNKI_SERVICE_SI + "homepage";
    
    public static final String GEO_COORDINATE_TYPE_SI = "http://www.geonames.org/coordinates";
    
    public static final String OMA_KAUPUNKI_DATE_SI = "http://omakaupunki.fi/date";
    
    
    // Default language for occurrences, variant names, and web API requests.
    public static final String LANG = "fi";


    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_oma_kaupunki.png");
    }
    
    @Override
    public String getName() {
        return "OmaKaupunki abstract extractor";
    }

    
    private final String[] contentTypes=new String[] { "text/json", "application/json" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    
    
    
    
    private URL extractURL = null;

    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        extractURL = url;
        return _extractTopicsFrom(url.openStream(),topicMap);
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        extractURL = null;
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        extractURL = null;
        return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
    }
    

    
    public boolean _extractTopicsFrom(InputStream inputStream, TopicMap topicMap) throws Exception {
        String jsonString = IObox.loadFile(inputStream, "UTF-8");
        
        System.out.println("jsonString-----------------------------");
        System.out.println(jsonString);
        
        JSONObject json = new JSONObject(jsonString);
        return parseOmaKaupunki(json, topicMap);
    }
    
    
    public abstract boolean parseOmaKaupunki(JSONObject json, TopicMap topicMap);
    
    
    
    protected void handleError(JSONObject error) {
        // {"code": 999, "error": "Unexpected error"}
        try {
            int code = error.getInt("code");
            String message = error.getString("error");
            log("Error '"+message+"' occurred while accessing Oma kaupunki API.");
        }
        catch(Exception e) {
            // SKIP
        }
    }
    
    
    private boolean shouldHandlePagination = true;
    private String defaultPagingOption = null;
    
    protected void handlePagination(JSONObject pagination, TopicMap tm) {
        if(shouldHandlePagination) {
            if(extractURL == null) return;
            try {
                String originalExtractURLStr = extractURL.toExternalForm();
                long total = robustGetLong( pagination, "total" );
                long pageSize = robustGetLong( pagination, "pagesize" );
                String pageString = robustGetString( pagination, "page" );
                long page = Long.parseLong(pageString);
                long totalPages = total/pageSize;
                String originalPageStr = "page="+page;
                if(page < totalPages) {
                    String[] pagingOptions = new String[] {
                        "Do not extract any more pages",
                        "Extract only next page",
                        "Extract next page",
                        "Extract 10 next pages",
                        "Extract all next pages"
                    };
                    String message = "You have just extracted page "+page+". There is total "+totalPages+" pages available. What would you like to do?";
                    if(defaultPagingOption == null) defaultPagingOption = pagingOptions[0];
                    String a = WandoraOptionPane.showOptionDialog(Wandora.getWandora(), message, "Found more pages",  WandoraOptionPane.OK_CANCEL_OPTION, pagingOptions, defaultPagingOption);
                    defaultPagingOption = a;
                    if(a != null) {
                        if(pagingOptions[1].equals(a)) {
                            System.out.println("Selected to extract only next page");
                            String extractURLStr = null;
                            shouldHandlePagination = false;
                            if(originalExtractURLStr.indexOf(originalPageStr) != -1) {
                                extractURLStr = originalExtractURLStr.replace(originalPageStr, "page="+(page+1));
                            }
                            else {
                                extractURLStr = originalExtractURLStr + "&page=" + (page+1);
                            }
                            _extractTopicsFrom(new URL(extractURLStr), tm);
                        }
                        
                        else if(pagingOptions[2].equals(a)) {
                            System.out.println("Selected to extract next page");
                            String extractURLStr = null;
                            if(originalExtractURLStr.indexOf(originalPageStr) != -1) {
                                extractURLStr = originalExtractURLStr.replace(originalPageStr, "page="+(page+1));
                            }
                            else {
                                extractURLStr = originalExtractURLStr + "&page=" + (page+1);
                            }
                            _extractTopicsFrom(new URL(extractURLStr), tm);
                        }
                        
                        else if(pagingOptions[3].equals(a)) {
                            System.out.println("Selected to extract 10 next pages");
                            shouldHandlePagination = false;
                            setProgress(1);
                            setProgressMax(10);
                            int progress = 1;
                            for(long p=page+1; p<Math.min(page+10, totalPages) && !forceStop(); p++) {
                                String extractURLStr = null;
                                if(originalExtractURLStr.indexOf(originalPageStr) != -1) {
                                    extractURLStr = originalExtractURLStr.replace(originalPageStr, "page="+p);
                                }
                                else {
                                    extractURLStr = originalExtractURLStr + "&page=" + p;
                                }
                                if(p == page+9) shouldHandlePagination = true;
                                _extractTopicsFrom(new URL(extractURLStr), tm);
                                setProgress(progress++);
                                nap();
                            }
                        }
                        
                        else if(pagingOptions[4].equals(a)) {
                            System.out.println("Selected to extract all pages");
                            shouldHandlePagination = false;
                            setProgress(1);
                            setProgressMax((int) (totalPages-page));
                            int progress = 1;
                            for(long p=page+1; p<totalPages && !forceStop(); p++) {
                                String extractURLStr = null;
                                if(originalExtractURLStr.indexOf(originalPageStr) != -1) {
                                    extractURLStr = originalExtractURLStr.replace(originalPageStr, "page="+p);
                                }
                                else {
                                    extractURLStr = originalExtractURLStr + "&page=" + p;
                                }
                                _extractTopicsFrom(new URL(extractURLStr), tm);
                                setProgress(progress++);
                                nap();
                            }
                            shouldHandlePagination = true;
                        }
                    }
                }
            }
            catch(Exception e) {
                Wandora.getWandora().handleError(e);
            }
        }
    }
    
    
    private void nap() {
        try {
            Thread.sleep(200);
        }
        catch(Exception e) {
            // WAKE UP
        }
    }
    
    
    
    
    // ------------------------------------
    
    
    
    
    public String robustGetString(JSONObject json, String key) {
        if(json != null && json.has(key)) {
            try {
                return json.getString(key);
            }
            catch(Exception e) { }
        }
        return null;
    }
    
    
    
    
    public long robustGetLong(JSONObject json, String key) {
        if(json != null && json.has(key)) {
            try {
                return json.getLong(key);
            }
            catch(Exception e) { }
        }
        return 0;
    }
    
    
    
    public double robustGetDouble(JSONObject json, String key) {
        if(json != null && json.has(key)) {
            try {
                return json.getDouble(key);
            }
            catch(Exception e) { }
        }
        return 0;
    }
    
    
    // ******** TOPIC MAPS *********
    
    
    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }



    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }
    
    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn, Topic type) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, type, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    
    
    // -------------------------------------------------------------------------
    

    protected Topic getATopic(String str, String si, Topic type, TopicMap tm) throws TopicMapException {
        if(str != null && si != null) {
            str = str.trim();
            if(str.length() > 0) {
                Topic topic=getOrCreateTopic(tm, si+"/"+urlEncode(str), str);
                if(type != null) topic.addType(type);
                return topic;
            }
        }
        return null;
    }

    protected Topic getUTopic(String si, Topic type, TopicMap tm) throws TopicMapException {
        if(si != null) {
            si = si.trim();
            if(si.length() > 0) {
                Topic topic=getOrCreateTopic(tm, si, null);
                if(type != null) topic.addType(type);
                return topic;
            }
        }
        return null;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public Topic getGeoCoordinateType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, GEO_COORDINATE_TYPE_SI, "Geo coordinates");
    }
    
    public Topic getEventIdentifierType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_IDENTIFIER_SI, "Oma kaupunki event identifier");
    }
    
    public Topic getEventVenueType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_VENUE_SI, "Oma kaupunki event venue");
    }
    public Topic getEventVenueTopic(long venueID, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_VENUE_SI + "/" + venueID, null, getEventVenueType(tm));
    }
    
    public Topic getDateType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_DATE_SI, "Oma kaupunki event date");
    }
    public Topic getDateTopic(String date, TopicMap tm) throws TopicMapException {
        return getATopic(date, OMA_KAUPUNKI_DATE_SI, getDateType(tm), tm);
    }
    
    public Topic getEventStartTimeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_START_TIME_SI, "Oma kaupunki event start time");
    }
    
    public Topic getEventEndTimeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_END_TIME_SI, "Oma kaupunki event end time");
    }
    
    public Topic getEventCreatedType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_CREATED_SI, "Oma kaupunki event created");
    }
    
    public Topic getEventTitleType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_TITLE_SI, "Oma kaupunki event title");
    }
    
    public Topic getEventBodyType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_BODY_SI, "Oma kaupunki event body");
    }
    
    

            
    public Topic getEventCategoryPluralType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_CATEGORY_PLURAL_SI, "Oma kaupunki event plural");
    }
    
    public Topic getEventCategoryIdentifierType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_CATEGORY_IDENTIFIER_SI, "Oma kaupunki event identifier");
    }
    
    
    public Topic getEventCategoryType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_CATEGORY_SI, "Oma kaupunki event category", getOmaKaupunkiType(tm));
    }
    public Topic getEventCategoryParentType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_CATEGORY_PARENT_SI, "Oma kaupunki event parent");
    }
    public Topic getEventCategoryTopic(long categoryID, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_CATEGORY_SI + "/" + categoryID);
    }

    
     
    public Topic getEventType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_EVENT_SI, "Oma kaupunki event", getOmaKaupunkiType(tm));
    }

    
    
    
    // ----------------------------
    
    
    public Topic getServiceCategoryPluralType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_CATEGORY_PLURAL_SI, "Oma kaupunki service plural");
    }
    
    public Topic getServiceCategoryIdentifierType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_CATEGORY_IDENTIFIER_SI, "Oma kaupunki service identifier");
    }
    
    public Topic getServiceCreatedType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_CREATED_SI, "Oma kaupunki service created");
    }
    
    public Topic getServicePhoneType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_PHONE_SI, "Oma kaupunki service phone");
    }
    
    public Topic getServiceTitleType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_TITLE_SI, "Oma kaupunki service title");
    }
    
    public Topic getServiceDescriptionType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_DESCRIPTION_SI, "Oma kaupunki service description");
    }
    
    public Topic getServiceCategoryType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_CATEGORY_SI, "Oma kaupunki service category", getOmaKaupunkiType(tm));
    }
    public Topic getServiceCategoryParentType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_CATEGORY_PARENT_SI, "Oma kaupunki service parent");
    }
    public Topic getServiceCategoryTopic(long categoryID, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_CATEGORY_SI + "/" + categoryID);
    }
    
    public Topic getServiceTagTopic(String tag, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_TAG_SI + "/" + urlEncode(tag), tag);
    }
    public Topic getServiceTagType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_TAG_SI, "Oma kaupunki service tag");
    }
    public Topic getServiceHomepageType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_HOMEPAGE_SI, "Oma kaupunki service homepage");
    }
    public Topic getServiceAddressType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_ADDRESS_SI, "Oma kaupunki service address");
    }
    
    public Topic getServiceIdentifierType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_IDENTIFIER_SI, "Oma kaupunki service identifier");
    }
    
    public Topic getServiceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, OMA_KAUPUNKI_SERVICE_SI, "Oma kaupunki services", getOmaKaupunkiType(tm));
    }

        
        
    // --------------------------------
    
    
    
    
    
    public Topic getOmaKaupunkiType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, OMA_KAUPUNKI_SI, "Oma kaupunki");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }
    
    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }
    
    public Topic getDefaultLangTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, XTMPSI.getLang(LANG));
    }
    
}
