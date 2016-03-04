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
 * MediaExtractor.java
 *
 * Created on 2015-04-30
 */

package org.wandora.application.tools.extractors.yle.elavaarkisto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.CSVParser;


/**
 * Finnish broadcasting company (YLE) has published Elava arkisto 
 * metadata under the CC-BY-SA 4.0 license. Wandora features a set of extractors
 * that transfer the published metadata to Topic Maps. This extractor is one of
 * these extractor. The metadata and it's documentation is available at 
 * 
 * http://elavaarkisto.kokeile.yle.fi/data/
 * http://elavaarkisto.kokeile.yle.fi/data/media.csv
 *
 * @author akivela
 */



public class MediaExtractor extends AbstractElavaArkistoExtractor {
    
    public static boolean CREATE_MID_IF_MISSING = true;
    

    @Override
    public String getName() {
        return "YLE Elava arkisto media extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "YLE Elava arkisto media extractor reads CSV feeds like http://elavaarkisto.kokeile.yle.fi/data/media.csv";
    }
    
    
    // -------------------------------------------------------------------------
    

    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        CSVParser csvParser = new CSVParser();
        CSVParser.Table table = csvParser.parse(f, "UTF-8");
        return _extractTopicsFrom(table, tm);
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        InputStream in = u.openStream();
        CSVParser.Table table = null;
        try {
            CSVParser csvParser = new CSVParser();
            table = csvParser.parse(in, "UTF-8");
        } 
        finally {
            in.close();
        }
        if(table != null) return _extractTopicsFrom(table, tm);
        return false;
    }
    
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        InputStream in = new ByteArrayInputStream(str.getBytes("UTF-8"));
        CSVParser csvParser = new CSVParser();
        CSVParser.Table table = csvParser.parse(in, "UTF-8");
        in.close();
        return _extractTopicsFrom(table, tm);
    }
    


    public boolean _extractTopicsFrom(CSVParser.Table table, TopicMap tm) throws Exception {
        if(table == null || tm == null) return false;
        setProgressMax(table.size());
        int i = 0;
        
        for(CSVParser.Row row : table) {
            setProgress(i++);
            if(i == 1) continue; // Skip column labels
            if(row.size() == 29) {
                try {
                    String mid = stringify(row.get(0));
                    boolean midIsReal = true;
                    
                    if(CREATE_MID_IF_MISSING && !isValidData(mid)) {
                        mid = System.currentTimeMillis() + "-" + Math.round(Math.random() * 9999);
                        log("Media has no MID identifier. Creating MID '"+mid+"' for the media.");
                        midIsReal = false;
                    }
                    
                    if(isValidData(mid)) {
                        Topic mediaTopic = getElavaArkistoMediaTopic(mid, tm);
                        Topic mediaType = getElavaArkistoMediaType(tm);
                        if(mediaTopic != null && mediaType != null) {
                            if(midIsReal) {
                                Topic midType = getElavaArkistoMediaMidType(tm);
                                Topic enLang = tm.getTopic(XTMPSI.getLang("en"));
                                if(midType != null && enLang != null) {
                                    mediaTopic.setData(midType, enLang, mid);
                                }
                            }
                            
                            String mediatype = stringify(row.get(1));
                            if(isValidData(mediatype)) {
                                Topic mediaTypeType = getElavaArkistoMediaTypeType(tm);
                                Topic mediaTypeTopic = getElavaArkistoMediaTypeTopic(mediatype, tm);
                                if(mediaTypeType != null && mediaTypeTopic != null) {
                                    Association a = tm.createAssociation(mediaTypeType);
                                    a.addPlayer(mediaTypeTopic, mediaTypeType);
                                    a.addPlayer(mediaTopic, mediaType);
                                }
                            }
                            
                            String titleFi = stringify(row.get(2));
                            String titleSv = stringify(row.get(3));
                            boolean hasBaseName = false;
                            if(isValidData(titleFi)) {
                                mediaTopic.setDisplayName("fi", titleFi);
                                mediaTopic.setBaseName(titleFi + " ("+mid+")");
                                hasBaseName = true;
                            }
                            if(isValidData(titleSv)) {
                                mediaTopic.setDisplayName("sv", titleSv);
                                if(!hasBaseName) {
                                    mediaTopic.setBaseName(titleSv + " ("+mid+")");
                                    hasBaseName = true;
                                }
                            }

                            String promoTitleFi = stringify(row.get(4));
                            String promoTitleSv = stringify(row.get(5));
                            if(isValidData(promoTitleFi)) {
                                Topic promoTitleType = getElavaArkistoMediaPromoTitleType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(promoTitleType != null && fiLang != null) {
                                    mediaTopic.setData(promoTitleType, fiLang, promoTitleFi);
                                    if(!hasBaseName) {
                                        mediaTopic.setBaseName(promoTitleFi + " ("+mid+")");
                                        hasBaseName = true;
                                    }
                                }
                            }
                            if(isValidData(promoTitleSv)) {
                                Topic promoTitleType = getElavaArkistoMediaPromoTitleType(tm);
                                Topic svLang = tm.getTopic(XTMPSI.getLang("sv"));
                                if(promoTitleType != null && svLang != null) {
                                    mediaTopic.setData(promoTitleType, svLang, promoTitleSv);
                                    if(!hasBaseName) {
                                        mediaTopic.setBaseName(promoTitleSv + " ("+mid+")");
                                        hasBaseName = true;
                                    }
                                }
                            }
                            
                            String originalTitleFi = stringify(row.get(6));
                            String originalTitleSv = stringify(row.get(7));
                            if(isValidData(originalTitleFi)) {
                                Topic originalTitleType = getElavaArkistoMediaOriginalTitleType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(originalTitleType != null && fiLang != null) {
                                    mediaTopic.setData(originalTitleType, fiLang, originalTitleFi);
                                    if(!hasBaseName) {
                                        mediaTopic.setBaseName(originalTitleFi + " ("+mid+")");
                                        hasBaseName = true;
                                    }
                                }
                            }
                            if(isValidData(originalTitleSv)) {
                                Topic originalTitleType = getElavaArkistoMediaOriginalTitleType(tm);
                                Topic svLang = tm.getTopic(XTMPSI.getLang("sv"));
                                if(originalTitleType != null && svLang != null) {
                                    mediaTopic.setData(originalTitleType, svLang, originalTitleSv);
                                    if(!hasBaseName) {
                                        mediaTopic.setBaseName(originalTitleSv + " ("+mid+")");
                                        hasBaseName = true;
                                    }
                                }
                            }
                            
                            String descriptionFi = stringify(row.get(8));
                            String descriptionSv = stringify(row.get(9));
                            if(isValidData(descriptionFi)) {
                                Topic descriptionType = getElavaArkistoMediaDescriptionType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(descriptionType != null && fiLang != null) {
                                    mediaTopic.setData(descriptionType, fiLang, descriptionFi);
                                }
                            }
                            if(isValidData(descriptionSv)) {
                                Topic descriptionType = getElavaArkistoMediaDescriptionType(tm);
                                Topic svLang = tm.getTopic(XTMPSI.getLang("sv"));
                                if(descriptionType != null && svLang != null) {
                                    mediaTopic.setData(descriptionType, svLang, descriptionSv);
                                }
                            }
                            
                            
                            String keywords = stringify(row.get(10));
                            if(isValidData(keywords)) {
                                Topic keywordsType = getElavaArkistoMediaKeywordsType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(keywordsType != null && fiLang != null) {
                                    mediaTopic.setData(keywordsType, fiLang, keywords);
                                }
                            }
                            
                            String classificationAnalytics = stringify(row.get(11));
                            if(isValidData(classificationAnalytics)) {
                                Topic classificationAnalyticsType = getElavaArkistoMediaClassificationAnalyticsType(tm);
                                Topic classificationAnalyticsTopic = getElavaArkistoMediaClassificationAnalyticsTopic(classificationAnalytics, tm);
                                if(classificationAnalyticsType != null && classificationAnalyticsTopic != null) {
                                    Association a = tm.createAssociation(classificationAnalyticsType);
                                    a.addPlayer(classificationAnalyticsTopic, classificationAnalyticsType);
                                    a.addPlayer(mediaTopic, mediaType);
                                }
                            }
                            
                            String classificationMainClass = stringify(row.get(12));
                            if(isValidData(classificationMainClass)) {
                                Topic classificationMainClassType = getElavaArkistoMediaClassificationMainClassType(tm);
                                Topic classificationMainClassTopic = getElavaArkistoMediaClassificationMainClassTopic(classificationMainClass, tm);
                                if(classificationMainClassType != null && classificationMainClassTopic != null) {
                                    Association a = tm.createAssociation(classificationMainClassType);
                                    a.addPlayer(classificationMainClassTopic, classificationMainClassType);
                                    a.addPlayer(mediaTopic, mediaType);
                                }
                            }
                            
                            String classificationSubClass = stringify(row.get(13));
                            if(isValidData(classificationSubClass)) {
                                Topic classificationSubClassType = getElavaArkistoMediaClassificationSubClassType(tm);
                                Topic classificationSubClassTopic = getElavaArkistoMediaClassificationSubClassTopic(classificationSubClass, tm);
                                if(classificationSubClassType != null && classificationSubClassTopic != null) {
                                    Association a = tm.createAssociation(classificationSubClassType);
                                    a.addPlayer(classificationSubClassTopic, classificationSubClassType);
                                    a.addPlayer(mediaTopic, mediaType);
                                }
                            }
                            
                            String actors = stringify(row.get(14));
                            if(isValidData(actors)) {
                                Topic actorsType = getElavaArkistoMediaActorsType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(actorsType != null && fiLang != null) {
                                    mediaTopic.setData(actorsType, fiLang, actors);
                                }
                            }
                            
                            
                            String contributors = stringify(row.get(15));
                            if(isValidData(contributors)) {
                                Topic contributorsType = getElavaArkistoMediaContributorsType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(contributorsType != null && fiLang != null) {
                                    mediaTopic.setData(contributorsType, fiLang, contributors);
                                }
                            }
                            
                            String format = stringify(row.get(16));
                            if(isValidData(format)) {
                                Topic mediaFormatType = getElavaArkistoMediaFormatType(tm);
                                Topic mediaFormatTopic = getElavaArkistoMediaFormatTopic(format, tm);
                                if(mediaFormatType != null && mediaFormatTopic != null) {
                                    Association a = tm.createAssociation(mediaFormatType);
                                    a.addPlayer(mediaFormatTopic, mediaFormatType);
                                    a.addPlayer(mediaTopic, mediaType);
                                }
                            }
                            
                            String color = stringify(row.get(17));
                            if(isValidData(color)) {
                                Topic colorFormatType = getElavaArkistoColorFormatType(tm);
                                Topic colorFormatTopic = getElavaArkistoColorFormatTopic(color, tm);
                                if(colorFormatType != null && colorFormatTopic != null) {
                                    Association a = tm.createAssociation(colorFormatType);
                                    a.addPlayer(colorFormatTopic, colorFormatType);
                                    a.addPlayer(mediaTopic, mediaType);
                                }
                            }
                            
                            String language = stringify(row.get(18));
                            if(isValidData(language)) {
                                Topic languageTopic = TMBox.getLangTopic(mediaTopic, language);
                                Topic languageType = tm.getTopic(TMBox.LANGUAGE_SI);
                                if(languageTopic != null && languageType != null) {
                                    Association a = tm.createAssociation(languageType);
                                    a.addPlayer(mediaTopic, mediaType);
                                    a.addPlayer(languageTopic, languageType);
                                }
                            }
                            
                            String duration = stringify(row.get(19));
                            if(isValidData(duration)) {
                                Topic durationType = getElavaArkistoMediaDurationType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(durationType != null && fiLang != null) {
                                    mediaTopic.setData(durationType, fiLang, duration);
                                }
                            }
                            
                            String durationPercentage = stringify(row.get(20));
                            if(isValidData(durationPercentage)) {
                                Topic durationPercentageType = getElavaArkistoMediaDurationPercentageType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(durationPercentageType != null && fiLang != null) {
                                    mediaTopic.setData(durationPercentageType, fiLang, durationPercentage);
                                }
                            }
                            
                            String firstRun = stringify(row.get(21));
                            if(isValidData(firstRun)) {
                                Topic firstRunTypeTopic = getElavaArkistoMediaFirstRunType(tm);
                                Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);

                                if(firstRunTypeTopic != null && langIndependent != null) {
                                    mediaTopic.setData(firstRunTypeTopic, langIndependent, firstRun);
                                }
                                
                                try {
                                    firstRun = firstRun.trim();
                                    SimpleDateFormat dateParser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzzz yyyy", Locale.ENGLISH);
                                    SimpleDateFormat altDateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                    SimpleDateFormat alt2DateParser = new SimpleDateFormat("yyyy", Locale.ENGLISH);
                                    Date date = null;
                                    
                                    try { date = dateParser.parse(firstRun); }
                                    catch(ParseException pe1) {
                                        try { date = altDateParser.parse(firstRun); }
                                        catch(ParseException pe2) {
                                            try { date = alt2DateParser.parse(firstRun); }
                                            catch(ParseException pe3) {
                                                log("Can't parse first run date '"+firstRun+"'.");
                                            }
                                        }
                                    }
                                    
                                    if(date != null) {
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                        String formattedDate = formatter.format(date);
                                        Topic englishLangTopic = tm.getTopic(XTMPSI.getLang("en"));
                                        if(firstRunTypeTopic != null &&  englishLangTopic != null && formattedDate != null) {
                                            mediaTopic.setData(firstRunTypeTopic, englishLangTopic, formattedDate);
                                        }
                                        Topic dateType = getElavaArkistoDateType(tm);
                                        Topic dateTopic = getElavaArkistoDateTopic(formattedDate, tm);

                                        if(dateTopic != null && dateType != null && firstRunTypeTopic != null) {
                                            Association a = tm.createAssociation(firstRunTypeTopic);
                                            a.addPlayer(mediaTopic, mediaType);
                                            a.addPlayer(dateTopic, dateType);
                                        }
                                    }
                                }
                                catch(Exception e) {
                                    log("Coudn't parse media first run date '"+firstRun+"'");
                                    e.printStackTrace();
                                }
                            }
                            
                            String publications = stringify(row.get(22));
                            if(isValidData(publications)) {
                                Topic publicationsTypeTopic = getElavaArkistoMediaPublicationsType(tm);
                                Topic langIndependent = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
                                
                                if(publicationsTypeTopic != null && langIndependent != null) {
                                    mediaTopic.setData(publicationsTypeTopic, langIndependent, publications);
                                }
                                
                                try {
                                    publications = publications.trim();
                                    String[] publicationsArray = publications.split(";");
                                    SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);
                                    SimpleDateFormat altDateParser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzzz yyyy", Locale.ENGLISH);
                                    SimpleDateFormat alt2DateParser = new SimpleDateFormat("yyyy", Locale.ENGLISH);
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                                    for(String publication : publicationsArray) {
                                        publication = publication.trim();
                                        try {
                                            Date date = null;
                                            try { date = dateParser.parse(publication); }
                                            catch(ParseException pe1) {
                                                try { date = altDateParser.parse(publication); }
                                                catch(ParseException pe2) {
                                                    try { date = alt2DateParser.parse(publication); }
                                                    catch(ParseException pe3) {
                                                        log("Can't parse publication date '"+publication+"'.");
                                                    }
                                                }
                                            }
                                            
                                            if(date != null) {
                                                String formattedDate = formatter.format(date);
                                                String formattedTime = timeFormatter.format(date);

                                                Topic dateType = getElavaArkistoDateType(tm);
                                                Topic dateTopic = getElavaArkistoDateTopic(formattedDate, tm);

                                                Topic timeType = getElavaArkistoTimeType(tm);
                                                Topic timeTopic = getElavaArkistoTimeTopic(formattedTime, tm);

                                                if(dateTopic != null && dateType != null && publicationsTypeTopic != null) {
                                                    Association a = tm.createAssociation(publicationsTypeTopic);
                                                    a.addPlayer(mediaTopic, mediaType);
                                                    a.addPlayer(dateTopic, dateType);
                                                    if(timeTopic != null && timeType != null) {
                                                        a.addPlayer(timeTopic, timeType);
                                                    }
                                                }
                                            }
                                        }
                                        catch(Exception e) {
                                            log("Coudn't parse media publication date '"+publication+"'");
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                catch(Exception e) {
                                    log("Coudn't parse media publication dates '"+publications+"'");
                                    e.printStackTrace();
                                }
                                
                            }
                            
                            String geoRestriction = stringify(row.get(23));
                            if(isValidData(geoRestriction)) {
                                if("1".equals(geoRestriction)) {
                                    Topic isGeoRestrictedType = getElavaArkistoMediaIsGeoRestrictedType(tm);
                                    Topic isGeoRestrictedTopic = getElavaArkistoMediaIsGeoRestrictedTopic(geoRestriction, tm);
                                    if(isGeoRestrictedType != null && isGeoRestrictedTopic != null) {
                                        Association a = tm.createAssociation(isGeoRestrictedType);
                                        a.addPlayer(isGeoRestrictedTopic, isGeoRestrictedType);
                                        a.addPlayer(mediaTopic, mediaType);
                                    }
                                }
                            }
                            
                            String download = stringify(row.get(24));
                            if(isValidData(download)) {
                                if("1".equals(download)) {
                                    Topic isDownloadableType = getElavaArkistoMediaIsDownloadableType(tm);
                                    Topic isDownloadableTopic = getElavaArkistoMediaIsDownloadableTopic(download, tm);
                                    if(isDownloadableType != null && isDownloadableTopic != null) {
                                        Association a = tm.createAssociation(isDownloadableType);
                                        a.addPlayer(isDownloadableTopic, isDownloadableType);
                                        a.addPlayer(mediaTopic, mediaType);
                                    }
                                }
                            }
                            
                            String embed = stringify(row.get(25));
                            if(isValidData(embed)) {
                                if("1".equals(embed)) {
                                    Topic isEmbeddableType = getElavaArkistoMediaIsEmbeddableType(tm);
                                    Topic isEmbeddableTopic = getElavaArkistoMediaIsEmbeddableTopic(embed, tm);
                                    if(isEmbeddableType != null && isEmbeddableTopic != null) {
                                        Association a = tm.createAssociation(isEmbeddableType);
                                        a.addPlayer(isEmbeddableTopic, isEmbeddableType);
                                        a.addPlayer(mediaTopic, mediaType);
                                    }
                                }
                            }
                            
                            // --- meta ---
                            
                            String eaCreator = stringify(row.get(26));
                            if(isValidData(eaCreator)) {
                                Topic type = getElavaArkistoMediaEACreatorType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(type != null && fiLang != null) {
                                    mediaTopic.setData(type, fiLang, eaCreator);
                                }
                            }
                            
                            String eaPublished = stringify(row.get(27));
                            if(isValidData(eaPublished)) {
                                Topic type = getElavaArkistoMediaEAPublishedType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(type != null && fiLang != null) {
                                    mediaTopic.setData(type, fiLang, eaPublished);
                                }
                                
                                try {
                                    eaPublished = eaPublished.trim();
                                    SimpleDateFormat dateParser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzzz yyyy", Locale.ENGLISH);
                                    Date date = dateParser.parse(eaPublished);
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                    String formattedDate = formatter.format(date);
                                    Topic englishLangTopic = tm.getTopic(XTMPSI.getLang("en"));
                                    if(type != null &&  englishLangTopic != null && formattedDate != null) {
                                        mediaTopic.setData(type, englishLangTopic, formattedDate);
                                    }
                                }
                                catch(Exception e) {
                                    log("Coudn't parse media EA published date '"+eaPublished+"'");
                                    e.printStackTrace();
                                }
                            }
                            
                            String eaUpdated = stringify(row.get(28));
                            if(isValidData(eaUpdated)) {
                                Topic type = getElavaArkistoMediaEAUpdatedType(tm);
                                Topic fiLang = tm.getTopic(XTMPSI.getLang("fi"));
                                if(type != null && fiLang != null) {
                                    mediaTopic.setData(type, fiLang, eaUpdated);
                                }
                            }
                        }
                    }
                    else {
                        log("Invalid identifier '"+mid+"' in CSV row. Skipping row "+i);
                    }
                }
                catch(Exception e) {
                    log(e);
                    break;
                }
            }
            else {
                System.out.println("Row has invalid number of values ("+ row.size() +"). Skipping the row.");
            }
            if(forceStop()) {
                log("Extraction stopped.");
                break;
            }
        }
        return true;
    }
    
    
    // -------------------------------------------------------------------------
    
   
    public static final String ELAVA_ARKISTO_MEDIA_TYPE_SI = ELAVA_ARKISTO_SI+"/media-type";
    public static final String ELAVA_ARKISTO_MEDIA_MID_SI = ELAVA_ARKISTO_SI+"/media-mid";
    public static final String ELAVA_ARKISTO_MEDIA_PROMO_TITLE_SI = ELAVA_ARKISTO_SI+"/media-promo-title";
    public static final String ELAVA_ARKISTO_MEDIA_ORIGINAL_TITLE_SI = ELAVA_ARKISTO_SI+"/media-original-title";
    public static final String ELAVA_ARKISTO_MEDIA_DESCRIPTION_SI = ELAVA_ARKISTO_SI+"/media-description";
    public static final String ELAVA_ARKISTO_MEDIA_KEYWORDS_SI = ELAVA_ARKISTO_SI+"/media-keywords";
    public static final String ELAVA_ARKISTO_MEDIA_ACTORS_SI = ELAVA_ARKISTO_SI+"/media-actors";
    public static final String ELAVA_ARKISTO_MEDIA_CONTRIBUTORS_SI = ELAVA_ARKISTO_SI+"/media-contributors";
    public static final String ELAVA_ARKISTO_MEDIA_FORMAT_SI = ELAVA_ARKISTO_SI+"/media-format";
    public static final String ELAVA_ARKISTO_MEDIA_COLOR_FORMAT_SI = ELAVA_ARKISTO_SI+"/media-color-format";
    public static final String ELAVA_ARKISTO_MEDIA_DURATION_SI = ELAVA_ARKISTO_SI+"/media-duration";
    public static final String ELAVA_ARKISTO_MEDIA_DURATION_PERCENTAGE_SI = ELAVA_ARKISTO_SI+"/media-duration-percentage";
    public static final String ELAVA_ARKISTO_MEDIA_FIRST_RUN_SI = ELAVA_ARKISTO_SI+"/media-first-run";
    public static final String ELAVA_ARKISTO_MEDIA_PUBLICATIONS_SI = ELAVA_ARKISTO_SI+"/media-publications";
    public static final String ELAVA_ARKISTO_MEDIA_IS_GEO_RESTRICTED_SI = ELAVA_ARKISTO_SI+"/media-is-geo-restricted";
    public static final String ELAVA_ARKISTO_MEDIA_IS_DOWNLOADABLE_SI = ELAVA_ARKISTO_SI+"/media-is-downloadable";
    public static final String ELAVA_ARKISTO_MEDIA_IS_EMBEDDABLE_SI = ELAVA_ARKISTO_SI+"/media-is-embeddable";
    
    public static final String ELAVA_ARKISTO_MEDIA_EA_CREATOR_SI = ELAVA_ARKISTO_SI+"/media-ea-created";
    public static final String ELAVA_ARKISTO_MEDIA_EA_PUBLISHED_SI = ELAVA_ARKISTO_SI+"/media-ea-published";
    public static final String ELAVA_ARKISTO_MEDIA_EA_UPDATED_SI = ELAVA_ARKISTO_SI+"/media-ea-updated";
    
    public static final String ELAVA_ARKISTO_MEDIA_CLASSIFICATION_SUBCLASS_SI = ELAVA_ARKISTO_SI+"/media-subclass";
    public static final String ELAVA_ARKISTO_MEDIA_CLASSIFICATION_MAIN_CLASS_SI = ELAVA_ARKISTO_SI+"/media-main-class";
    public static final String ELAVA_ARKISTO_MEDIA_CLASSIFICATION_ANALYTICS_SI = ELAVA_ARKISTO_SI+"/media-analytics";
    
    
    
    
    public Topic getElavaArkistoMediaTypeType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_TYPE_SI, "Elava-arkisto media type", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaTypeTopic(String type, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_TYPE_SI+"/"+urlEncode(type);
        Topic typeTopic = null;
        try {
            typeTopic = tm.getTopic(si);
            if(typeTopic == null) {
                typeTopic = tm.createTopic();
                typeTopic.addSubjectIdentifier(new Locator(si));
                typeTopic.addType(getElavaArkistoMediaTypeType(tm));
                typeTopic.setBaseName(type + " (YLE media type)");
            }
        }
        catch(Exception e) {
            log(e);
        }
        return typeTopic;
    }
    
    
    public Topic getElavaArkistoMediaMidType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_MID_SI, "Elava-arkisto media mid", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    public Topic getElavaArkistoMediaPromoTitleType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_PROMO_TITLE_SI, "Elava-arkisto media promo title", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaOriginalTitleType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_ORIGINAL_TITLE_SI, "Elava-arkisto media original title", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaDescriptionType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_DESCRIPTION_SI, "Elava-arkisto media description", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaKeywordsType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_KEYWORDS_SI, "Elava-arkisto media keywords", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaActorsType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_ACTORS_SI, "Elava-arkisto media actors", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaContributorsType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_CONTRIBUTORS_SI, "Elava-arkisto media contributors", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaFormatType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_FORMAT_SI, "Elava-arkisto media format", getElavaArkistoType(tm), tm);
        return type;
    }
    

    public Topic getElavaArkistoMediaFormatTopic(String format, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_FORMAT_SI+"/"+urlEncode(format);
        Topic formatTopic = null;
        try {
            formatTopic = tm.getTopic(si);
            if(formatTopic == null) {
                formatTopic = tm.createTopic();
                formatTopic.addSubjectIdentifier(new Locator(si));
                formatTopic.addType(getElavaArkistoMediaFormatType(tm));
                formatTopic.setBaseName(format + " (YLE media format)");
            }
        }
        catch(Exception e) {
            log(e);
        }
        return formatTopic;
    }
    

    public Topic getElavaArkistoColorFormatType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_COLOR_FORMAT_SI, "Elava-arkisto color format", getElavaArkistoType(tm), tm);
        return type;
    }
    

    public Topic getElavaArkistoColorFormatTopic(String format, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_COLOR_FORMAT_SI+"/"+urlEncode(format);
        Topic formatTopic = null;
        try {
            formatTopic = tm.getTopic(si);
            if(formatTopic == null) {
                formatTopic = tm.createTopic();
                formatTopic.addSubjectIdentifier(new Locator(si));
                formatTopic.addType(getElavaArkistoColorFormatType(tm));
                formatTopic.setBaseName(format + " (YLE media color format)");
            }
        }
        catch(Exception e) {
            log(e);
        }
        return formatTopic;
    }
    
    
    public Topic getElavaArkistoMediaDurationType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_DURATION_SI, "Elava-arkisto media duration", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaDurationPercentageType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_DURATION_PERCENTAGE_SI, "Elava-arkisto media duration percentage", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaFirstRunType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_FIRST_RUN_SI, "Elava-arkisto media first run", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaPublicationsType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_PUBLICATIONS_SI, "Elava-arkisto media publications", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaIsGeoRestrictedType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_IS_GEO_RESTRICTED_SI, "Elava-arkisto media is geo restricted", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaIsGeoRestrictedTopic(String isGeoRestricted, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_IS_GEO_RESTRICTED_SI+"/"+urlEncode(isGeoRestricted);
        Topic isTopic = null;
        try {
            isTopic = tm.getTopic(si);
            if(isTopic == null) {
                isTopic = tm.createTopic();
                isTopic.addSubjectIdentifier(new Locator(si));
                isTopic.addType(getElavaArkistoMediaIsGeoRestrictedType(tm));
            }
        }
        catch(Exception e) {
            log(e);
        }
        return isTopic;
    }
    
    public Topic getElavaArkistoMediaIsDownloadableType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_IS_DOWNLOADABLE_SI, "Elava-arkisto media is downlodable", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaIsDownloadableTopic(String isDownloadable, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_IS_DOWNLOADABLE_SI+"/"+urlEncode(isDownloadable);
        Topic isTopic = null;
        try {
            isTopic = tm.getTopic(si);
            if(isTopic == null) {
                isTopic = tm.createTopic();
                isTopic.addSubjectIdentifier(new Locator(si));
                isTopic.addType(getElavaArkistoMediaIsDownloadableType(tm));
            }
        }
        catch(Exception e) {
            log(e);
        }
        return isTopic;
    }
    
    public Topic getElavaArkistoMediaIsEmbeddableType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_IS_EMBEDDABLE_SI, "Elava-arkisto media is embeddable", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaIsEmbeddableTopic(String isEmbeddable, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_IS_EMBEDDABLE_SI+"/"+urlEncode(isEmbeddable);
        Topic isTopic = null;
        try {
            isTopic = tm.getTopic(si);
            if(isTopic == null) {
                isTopic = tm.createTopic();
                isTopic.addSubjectIdentifier(new Locator(si));
                isTopic.addType(getElavaArkistoMediaIsEmbeddableType(tm));
            }
        }
        catch(Exception e) {
            log(e);
        }
        return isTopic;
    }
    
    public Topic getElavaArkistoMediaEACreatorType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_EA_CREATOR_SI, "Elava-arkisto media EA creator", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaEAPublishedType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_EA_PUBLISHED_SI, "Elava-arkisto media EA published", getElavaArkistoType(tm), tm);
        return type;
    }
    
    public Topic getElavaArkistoMediaEAUpdatedType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_EA_UPDATED_SI, "Elava-arkisto media EA updated", getElavaArkistoType(tm), tm);
        return type;
    }
    
    
    
    
    public Topic getElavaArkistoMediaClassificationAnalyticsType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_CLASSIFICATION_ANALYTICS_SI, "Elava-arkisto classification analytics", getElavaArkistoType(tm), tm);
        return type;
    }
    

    public Topic getElavaArkistoMediaClassificationAnalyticsTopic(String cl, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_CLASSIFICATION_ANALYTICS_SI+"/"+urlEncode(cl);
        Topic topic = null;
        try {
            topic = tm.getTopic(si);
            if(topic == null) {
                topic = tm.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
                topic.addType(getElavaArkistoMediaClassificationAnalyticsType(tm));
                topic.setBaseName(cl + " (YLE media classification analytics)");
            }
        }
        catch(Exception e) {
            log(e);
        }
        return topic;
    }
    
    
    

    
    public Topic getElavaArkistoMediaClassificationMainClassType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_CLASSIFICATION_MAIN_CLASS_SI, "Elava-arkisto classification main class", getElavaArkistoType(tm), tm);
        return type;
    }
    

    public Topic getElavaArkistoMediaClassificationMainClassTopic(String cl, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_CLASSIFICATION_MAIN_CLASS_SI+"/"+urlEncode(cl);
        Topic topic = null;
        try {
            topic = tm.getTopic(si);
            if(topic == null) {
                topic = tm.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
                topic.addType(getElavaArkistoMediaClassificationMainClassType(tm));
                topic.setBaseName(cl + " (YLE media classification main class)");
            }
        }
        catch(Exception e) {
            log(e);
        }
        return topic;
    }
    
    
    

    

    
    public Topic getElavaArkistoMediaClassificationSubClassType(TopicMap tm) throws TopicMapException {
        Topic type = ExtractHelper.getOrCreateTopic(ELAVA_ARKISTO_MEDIA_CLASSIFICATION_SUBCLASS_SI, "Elava-arkisto classification subclass", getElavaArkistoType(tm), tm);
        return type;
    }
    

    public Topic getElavaArkistoMediaClassificationSubClassTopic(String cl, TopicMap tm) throws TopicMapException {
        String si = ELAVA_ARKISTO_MEDIA_CLASSIFICATION_SUBCLASS_SI+"/"+urlEncode(cl);
        Topic topic = null;
        try {
            topic = tm.getTopic(si);
            if(topic == null) {
                topic = tm.createTopic();
                topic.addSubjectIdentifier(new Locator(si));
                topic.addType(getElavaArkistoMediaClassificationSubClassType(tm));
                topic.setBaseName(cl + " (YLE media classification subclass)");
            }
        }
        catch(Exception e) {
            log(e);
        }
        return topic;
    }
    
    
    
}




/*

Example of extracted CSV:

MID,TYPE,TITLE_FI,TITLE_SV,PROMOTITLE_FI,PROMOTITLE_SV,ORIGINALTITLE_FI,ORIGINALTITLE_SV,DESCRIPTION_FI,DESCRIPTION_SV,KEYWORDS,CLASSIFICATION_ANALYTICS,CLASSIFICATION_MAIN_CLASS,CLASSIFICATION_SUB_CLASS,ACTORS,CONTRIBUTORS,FORMAT,COLOR,LANGUAGE,DURATION,DURATIONPERCENTAGE,FIRSTRUN,PUBLICATIONS,GEORESTRICTION,DOWNLOAD,EMBED,EA_CREATOR,EA_PUBLISHED,EA_UPDATED
26-103988,TVClip,"Hyvää yötä ja huomenta materiaalinauhat",,"Laulava ratikkakuski Aarne Tenkanen oli Göstä Sundqvistin luomus",,Leirintäalue,,"Kaide Järvisen esittämä raitiovaununkuljettaja Aarne Tenkanen esiintyi useissa Gösta Sundqvistin radio-ohjelmissa ennen siirtymistään levy-ja lavatähdeksi.",,"aihe: fiktiiviset henkilöt; henkilö: Kai Järvinen; aihe: huumorimusiikki; tapahtuma: 1998; aihe: laulajat; henkilö: Aarne Tenkanen; aihe: raitiovaununkuljettajat; ohjelmatyyppi: haastattelut",,,,"Kai 'Kaide' Järvinen (Aarne Tenkanen)","Axa Sorjanen - ohjaaja, haastattelut; Raino Kuisma - kuvaus; Tuomas Palola - äänitys; Lasse Wikman - tuottaja",video,,fi,2399000,,1998-11-09,,0,0,1,"Axa Sorjanen",2014-05-19T15:10:09+03:00,
26-66172,TVClip,,,,"Det finlandssvenska vädret från Kaisaniemiparken",,,"Äidinkieli.","Kurre Österberg och meteorolog Kenneth Holmlund, Yle bildband",,,,,,"Tuottaja: Torsten Bergman; Käsikirjoittaja: Anneli Sjöstedt; Ohjaaja: Öivind Nyquist; Kuvaussihteeri: Barbro Rännäri; Käsikirjoittaja: Mikaela Groop; Käsikirjoittaja: Ann-Kristin Schevelew; Musiikin suunnittelija: Kaj Wessman; Lukija (kertoja/speak): Ann-Kristin Schevelew; Kuvaussuunnittelija: Pentti Jurvanen; Lavastussuunnittelija: Julia Tallgren; Järjestäjä: Juha Suomela; Naamioitsija: P Aiho; Naamioitsija: N Bang; Pukusuunnittelija: Maija Kalkas; Graafinen suunnittelija: Pirjo Puolakka; muut: Anneli Sjöstedt, Birgitta Ohralahti, Immo Puustinen, Byman, Häikiö, Tonteri, Stig Granström",video,Väri,sv,298000,6,1988-11-05,,0,0,1,"Ida Fellman",2011-05-27T13:54:23+03:00,
26-42871,RadioClip,,,"102 b. Murhemielin, kyynelöiden",,,,"Jukka Lintinen",,,,,,,,audio,,fi,290000,,,,0,0,1,"Elina Yli-Ojanperä",2011-08-17T15:32:46+03:00,


where:

    MID = median Yle ID (Media ID)
    TYPE = median tyyppi (TVClip, TVProgram, RadioClip, RadioProgram)
    TITLE_FI/TITLE_SV = median otsikko suomeksi/ruotsiksi
    PROMOTITLE_FI/PROMOTITLE_SV = median mainosotsikko suomeksi/ruotsiksi
    ORIGINALTITLE_FI/ORIGINALTITLE_SV = alkuperäisohjelman nimi suomeksi/ruotsiksi
    DESCRIPTION_FI/DESCRIPTION_SV = sisältökuvaus suomeksi/ruotsiksi
    KEYWORDS = avainsanat
    CLASSIFICATION_ANALYTICS = tilastointiluokka
    CLASSIFICATION_MAIN_CLASS, CLASSIFICATION_SUB_CLASS = sisällön pääluokka, aliluokka
    ACTORS = esiintyjät
    CONTRIBUTORS = tekijät
    FORMAT = mediaformaatti (video, audio)
    COLOR = väri (Muva, Väri, ...)
    LANGUAGE = kulutuskieli (fi, sv)
    DURATION = median kesto millisekunneissa
    DURATIONPERCENTAGE = median keston osuus alkuperäisen ohjelman kestosta (0-100)
    FIRSTRUN = ensijulkaisupäivä (tarkkuus vaihtelee vuosikymmenestä päivän tarkkuuteen)
    PUBLICATIONS = esitetysajat Ylen kanavilla
    GEORESTRICTION = maarajattu (1=kyllä, 0=ei)
    DOWNLOAD = ladattavissa (1=kyllä, 0=ei)
    EMBED = upotettavissa (1=kyllä, 0=ei)
    EA_CREATOR = Elävään arkistoon julkaissut henkilö
    EA_PUBLISHED = julkaisuaika Elävässä arkistossa
    EA_UPDATED = viimeisin muokkausaika Elävässä arkistossa


*/
