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
 *
 * 
 * 
 * AbstractGeoNamesExtractor.java
 * 
 * 
 */


package org.wandora.application.tools.extractors.geonames;

import java.net.*;
import java.io.*;
import java.util.*;
import org.wandora.topicmap.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.application.gui.*;
import javax.swing.*;
import java.util.regex.*;


/**
 *
 * @author akivela
 */
public abstract class AbstractGeoNamesExtractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;



	public static boolean USE_EXISTING_TOPICS = false;
    
    
    
    // Default language for occurrences, variant names, and web API requests.
    public static final String LANG = "en";
    
    public static final String GEONAMES_PREFIX="http://www.geonames.org/";
    
    public static final String GEONAMEID_SI="http://www.geonames.org";
    
    public static final String LANGUAGE_SI = GEONAMES_PREFIX+"language";
    
    public static final String COUNTRY_SI = GEONAMES_PREFIX+"country";
    public static final String COUNTRY_CODE_SI = GEONAMES_PREFIX+"country-code";
    
    public static final String CONTINENT_SI = GEONAMES_PREFIX+"continent";
    public static final String COUNTRY_CAPITAL_SI = GEONAMES_PREFIX+"country-capital";
    public static final String CAPITAL_SI = GEONAMES_PREFIX+"capital-city";
    public static final String CITY_SI = GEONAMES_PREFIX+"city";
    
    public static final String BBOX_SI = GEONAMES_PREFIX+"bbox";
    public static final String BBOXWEST_SI = GEONAMES_PREFIX+"bbox-west";
    public static final String BBOXEAST_SI = GEONAMES_PREFIX+"bbox-east";
    public static final String BBOXSOUTH_SI = GEONAMES_PREFIX+"bbox-south";
    public static final String BBOXNORTH_SI = GEONAMES_PREFIX+"bbox-north";
    
    public static final String GEOOBJECT_SI = GEONAMES_PREFIX+"geo-object";
    
    
    public static final String GPS_SI = GEONAMES_PREFIX+"gps-number";
    public static final String AREA_SI = GEONAMES_PREFIX+"area";
    public static final String POPULATION_SI = GEONAMES_PREFIX+"population";
    public static final String CURRENCY_SI = GEONAMES_PREFIX+"currency";
    
    
    public static final String ISOALPHA3_SI = "http://www.iso.org/ISO3166-1alpha-3/";
    public static final String ISONUMERIC_SI = "http://www.iso.org/ISO3166-1numeric/";
    public static final String FIPS_SI = "http://www.itl.nist.gov/fipspubs/10-4/";
    
    public static final String LOCATION_SI = GEONAMES_PREFIX+"location";
    public static final String LOCATED_SI = GEONAMES_PREFIX+"located";
    
    public static final String LNG_SI = GEONAMES_PREFIX+"lng";
    public static final String LAT_SI = GEONAMES_PREFIX+"lat";
    
    public static final String FCL_SI = GEONAMES_PREFIX+"fcl";
    public static final String FCODE_SI = GEONAMES_PREFIX+"fcode";
    
    public static final String PARENT_CHILD_SI = GEONAMES_PREFIX+"parent-child"; // Association type
    public static final String PARENT_SI = GEONAMES_PREFIX+"parent"; // role
    public static final String CHILD_SI = GEONAMES_PREFIX+"child"; // role
    
    public static final String PART_WHOLE_SI = GEONAMES_PREFIX+"part-whole"; // Association type
    public static final String WHOLE_SI = GEONAMES_PREFIX+"whole"; // role
    public static final String PART_SI = GEONAMES_PREFIX+"part"; // role
    
    public static final String NEIGHBOURS_SI = GEONAMES_PREFIX+"neighbours"; // Association type
    public static final String NEIGHBOUR_SI = GEONAMES_PREFIX+"neighbour"; // role
    public static final String NEIGHBOUR2_SI = GEONAMES_PREFIX+"neighbour2"; // role
    
    public static final String SIBLINGS_SI = GEONAMES_PREFIX+"siblings"; // Association type
    public static final String SIBLING_SI = GEONAMES_PREFIX+"sibling"; // role
    public static final String SIBLING2_SI = GEONAMES_PREFIX+"sibling2"; // role
    
    public static boolean USE_FCODES_AS_PRIMARY_CATEGORIZATION = true;
    public static boolean USE_ALL_VARIANT_NAMES = true;

    public static final String ELEVATION_SI = GEONAMES_PREFIX+"elevation";
    
    
    
    // **** WEATHER ****
    public static final String WEATHER_SI = GEONAMES_PREFIX+"weather";
    public static final String WEATHER_OBSERVATION_SI = WEATHER_SI + "/observation";
    public static final String WEATHER_OBSERVATIONTIME_SI = WEATHER_SI + "/time";
    public static final String WEATHER_STATION_SI = WEATHER_SI + "/station";
    public static final String WEATHER_ICAO_SI = GEONAMES_PREFIX + "/icao";
    public static final String WEATHER_ELEVATION_SI = WEATHER_SI + "/elevation";
    public static final String WEATHER_TEMPERATURE_SI = WEATHER_SI + "/temperature";
    public static final String WEATHER_DEWPOINT_SI = WEATHER_SI + "/dewpoint";
    public static final String WEATHER_HUMIDITY_SI = WEATHER_SI + "/humidity";
    public static final String WEATHER_CLOUDS_SI = WEATHER_SI + "/clouds";
    public static final String WEATHER_WEATHERCONDITION_SI = WEATHER_SI + "/weathercondition";
    public static final String WEATHER_HECTOPASCALTIMETER_SI = WEATHER_SI + "/hectopascaltimeter";
    public static final String WEATHER_WINDDIRECTION_SI = WEATHER_SI + "/wind/direction";
    public static final String WEATHER_WINDSPEED_SI = WEATHER_SI + "/wind/speed";

    
    // **** WIKIPEDIA ****
    public static final String WIKIPEDIA_GEO_PREFIX="http://www.geonames.org/wikipedia";
    public static final String WIKIPEDIA_GEO_ENTRY_SI=WIKIPEDIA_GEO_PREFIX+"/entry";
    public static final String WIKIPEDIA_GEO_FEATURE_SI=WIKIPEDIA_GEO_PREFIX+"/feature";
    public static final String WIKIPEDIA_GEOOBJECT_SI = WIKIPEDIA_GEO_PREFIX+"/geo-object";
    public static final String WIKIPEDIA_GEO_SUMMARY_SI = WIKIPEDIA_GEO_PREFIX+"/summary";
    public static final String WIKIPEDIA_GEO_THUMBNAIL_SI = WIKIPEDIA_GEO_PREFIX+"/thumbnail";

    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_geonames.png");
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
    

    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(url.openStream(),topicMap);
    }
    
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new ByteArrayInputStream(str.getBytes()), topicMap);
    }
    

    
    public abstract boolean _extractTopicsFrom(InputStream inputStream, TopicMap topicMap) throws Exception;
    
    
    
    // ******** TOPIC MAPS *********
    
    
    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }



    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }


    
    // -------------------------------------------------------------------------
    // --- TYPE TOPICS ---------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    public static Topic getCountryCapitalTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, COUNTRY_CAPITAL_SI, "country-capital (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    
    public static Topic getCountryTypeTopic(TopicMap tm) throws TopicMapException {
        if(USE_FCODES_AS_PRIMARY_CATEGORIZATION) return getFCodeTopic(tm, "PCLI", "independent political entity");
        else return getFCLTopic(tm, "A", "country, state, region,...");
        /*
        Topic type = getOrCreateTopic(tm, COUNTRY_SI, "country (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
        */
    }
    
    
    public static Topic getContinentTypeTopic(TopicMap tm) throws TopicMapException {
        if(USE_FCODES_AS_PRIMARY_CATEGORIZATION) return getFCodeTopic(tm, "CONT", "continent");
        else return getFCLTopic(tm, "L", "parks,area, ...");
        /*
        Topic type = getOrCreateTopic(tm, CONTINENT_SI, "continent (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
        */
    }
    
    public static Topic getCapitalTypeTopic(TopicMap tm) throws TopicMapException {
        //return getCityTypeTopic(tm);

        Topic type = getOrCreateTopic(tm, CAPITAL_SI, "capital (geonames)");
        //Topic gnClass = getGeoNamesClassTopic(tm);
        //makeSubclassOf(tm, type, gnClass);
        Topic cityType = getCityTypeTopic(tm);
        makeSubclassOf(tm, type, cityType);
        return type;
    }
    
    
    public static Topic getCityTypeTopic(TopicMap tm) throws TopicMapException {
        if(USE_FCODES_AS_PRIMARY_CATEGORIZATION) return getFCodeTopic(tm, "PPLA", "seat of a first-order administrative division");
        else return getFCLTopic(tm, "P", "city, village,...");
        /*
        Topic type = getOrCreateTopic(tm, CITY_SI, "city (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
        */
    }
    
    
    public static Topic getBBoxTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, BBOX_SI, "bounding box (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    public static Topic getBBoxWestTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, BBOXWEST_SI, "west of bounding box (geonames)");
        return type;
    }
    public static Topic getBBoxEastTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, BBOXEAST_SI, "east of bounding box (geonames)");
        return type;
    }
    public static Topic getBBoxSouthTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, BBOXSOUTH_SI, "south of bounding box (geonames)");
        return type;
    }
    public static Topic getBBoxNorthTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, BBOXNORTH_SI, "north of bounding box (geonames)");
        return type;
    }
    
    public static Topic getGPSNumberTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, GPS_SI, "gps number (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    
    public static Topic getGeoObjectTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, GEOOBJECT_SI, "geo-object (geonames)");
        return type;
    }
    
    public static Topic getWikipediaGeoObjectTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WIKIPEDIA_GEOOBJECT_SI, "wikipedia geo-object (geonames)");
        return type;
    }
    
    public static Topic getWikipediaSummaryTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WIKIPEDIA_GEO_SUMMARY_SI, "wikipedia geo-summary (geonames)");
        return type;
    }
    
    public static Topic getWikipediaThumbnailTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WIKIPEDIA_GEO_THUMBNAIL_SI, "wikipedia thumbnail (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    
    public static Topic getWikipediaFeatureTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WIKIPEDIA_GEO_FEATURE_SI, "wikipedia geo-feature (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    
    public static Topic getLanguageTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, LANGUAGE_SI, "language (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    public static Topic getCurrencyTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, CURRENCY_SI, "currency (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    
    public static Topic getAreaTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, AREA_SI, "area (geonames)");
        return type;
    }
    public static Topic getPopulationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, POPULATION_SI, "population (geonames)");
        return type;
    }
    
    
    public static Topic getElevationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, ELEVATION_SI, "elevation (geonames)");
        return type;
    }
    
    // GPS LOCATION!
    public static Topic getLocationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, LOCATION_SI, "gps location (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    public static Topic getLocatedTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, LOCATED_SI, "located (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    
    public static Topic getLatTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, LAT_SI, "latitude (geonames)");
        return type;
    }
    public static Topic getLngTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, LNG_SI, "longitude (geonames)");
        return type;
    }
    
    public static Topic getPartWholeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, PART_WHOLE_SI, "part-whole (geonames)");
        return type;
    }
    public static Topic getWholeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WHOLE_SI, "whole (geonames)");
        return type;
    }
    public static Topic getPartTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, PART_SI, "part (geonames)");
        return type;
    }
    

    
    public static Topic getParentChildTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, PARENT_CHILD_SI, "parent-child (geonames)");
        return type;
    }
    public static Topic getParentTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, PARENT_SI, "parent (geonames)");
        return type;
    }
    public static Topic getChildTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, CHILD_SI, "child (geonames)");
        return type;
    }

    
    
    
    public static Topic getNeighboursTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, NEIGHBOURS_SI, "neighbours (geonames)");
        return type;
    }
    public static Topic getNeighbourTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, NEIGHBOUR_SI, "neighbour (geonames)");
        return type;
    }
    public static Topic getNeighbour2TypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, NEIGHBOUR2_SI, "neighbour-2 (geonames)");
        return type;
    }
    
    
    
    public static Topic getSiblingsTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, SIBLINGS_SI, "siblings (geonames)");
        return type;
    }
    public static Topic getSiblingTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, SIBLING_SI, "sibling (geonames)");
        return type;
    }
    public static Topic getSibling2TypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, SIBLING2_SI, "sibling-2 (geonames)");
        return type;
    }
    
    
    
    public static Topic getFCLTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, FCL_SI, "FCL (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    public static Topic getFCodeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, FCODE_SI, "fcode (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    
    // ----- ACTUAL INSTANCE TOPICS ------
    
    
    public static Topic getGeoTopic(TopicMap tm, String geonameid)  throws TopicMapException {
        String geoSI = createGeonameSI(geonameid);
        Topic geoTopic = null;
        if(USE_EXISTING_TOPICS) geoTopic = tm.getTopic(geoSI);
        if(geoTopic == null) {
            geoTopic=tm.createTopic();
            geoTopic.addSubjectIdentifier(tm.createLocator(geoSI));
        }
        return geoTopic;
    }
    
    
    public static Topic getGeoTopic(TopicMap tm, String geonameid, String name, String lang)  throws TopicMapException {
        String geoSI = createGeonameSI(geonameid);
        Topic geoTopic = null;
        if(USE_EXISTING_TOPICS) geoTopic = tm.getTopic(geoSI);
        if(geoTopic == null) {
            geoTopic=tm.createTopic();
            geoTopic.addSubjectIdentifier(tm.createLocator(geoSI));
            geoTopic.setBaseName(name + " ("+geonameid+")");
            if(lang != null) geoTopic.setDisplayName(lang, name);
            else geoTopic.setDisplayName(LANG, name);
        }
        return geoTopic;
    }
    
    
    public static Topic getWikipediaGeoTopic(TopicMap tm, String title, String wikipediaUrl, String lang)  throws TopicMapException {
        String si = null;
        if(wikipediaUrl != null && wikipediaUrl.length() > 0) {
            si = wikipediaUrl;
        }
        else {
            si = WIKIPEDIA_GEO_ENTRY_SI + "/" + encode(title);
        }
        Topic geoTopic = null;
        if(USE_EXISTING_TOPICS) geoTopic = tm.getTopic(si);
        if(geoTopic == null) {
            geoTopic=tm.createTopic();
            geoTopic.addSubjectIdentifier(tm.createLocator(si));
            if(title != null && title.length() > 0) {
                geoTopic.setBaseName(title + " (wikipedia geo-object)");
                if(lang != null) geoTopic.setDisplayName(lang, title);
                else geoTopic.setDisplayName(LANG, title);
            }
        }
        return geoTopic;
    }
    
    
    public static Topic getCountryTopic(TopicMap tm, String countrycode, String lang) throws TopicMapException {
        String countryname = solveCountryName(countrycode);
        return getCountryTopic(tm, countrycode, countryname, lang);
    }
    
    
    
    public static Topic getCountryTopic(TopicMap tm, String countrycode, String countryname, String lang) throws TopicMapException {
        String countrySI = COUNTRY_CODE_SI + "/" + encode(countrycode);
        Topic theCountry = null;
        if(USE_EXISTING_TOPICS) theCountry = tm.getTopic(countrySI);
        if(theCountry == null) {
            Topic countryType = getCountryTypeTopic(tm);
            theCountry=tm.createTopic();
            theCountry.addSubjectIdentifier(tm.createLocator(countrySI));
            //theCountry.setBaseName(countryname + " (geoname)");
            if(lang != null) theCountry.setDisplayName(lang, countryname);
            else theCountry.setDisplayName(LANG, countryname);
            theCountry.addType(countryType);
        }
        return theCountry;
    }
    
    
    
    
    public static Topic getCountryTopic(TopicMap tm, String countrycode, String geonameid, String countryname, String lang) throws TopicMapException {
        String countrySI = createGeonameSI(geonameid);
        Topic theCountry = null;
        if(USE_EXISTING_TOPICS) theCountry = tm.getTopic(countrySI);
        if(theCountry == null) {
            Topic countryType = getCountryTypeTopic(tm);
            theCountry=tm.createTopic();
            theCountry.addSubjectIdentifier(tm.createLocator(countrySI));
            if(countrycode!=null && countrycode.length()>0) {
                theCountry.addSubjectIdentifier(tm.createLocator(COUNTRY_CODE_SI+"/"+encode(countrycode)));
            }
            theCountry.setBaseName(countryname + " ("+geonameid+")");
            if(lang != null) theCountry.setDisplayName(lang, countryname);
            else theCountry.setDisplayName(LANG, countryname);
            theCountry.addType(countryType);
        }
        return theCountry;
    }
    
    
    
    
    public static Topic getContinentTopic(TopicMap tm, String continentcode) throws TopicMapException {
        String continentSI = CONTINENT_SI + "/" + encode(continentcode);
        Topic theContinent = null;
        if(USE_EXISTING_TOPICS) theContinent = tm.getTopic(continentSI);
        if(theContinent == null) {
            Topic continentType = getContinentTypeTopic(tm);
            theContinent=tm.createTopic();
            theContinent.addSubjectIdentifier(tm.createLocator(continentSI));
            //theContinent.setBaseName(continentcode + " (geoname)");
            theContinent.addType(continentType);
        }
        return theContinent;
    }
    
    
    
    // -------
    
    
    public static Topic getCityTopic(TopicMap tm, String city, String lang) throws TopicMapException {
        String citySI = CITY_SI + "/" + encode(city);
        Topic theCity = null;
        if(USE_EXISTING_TOPICS) theCity = tm.getTopic(citySI);
        if(theCity == null) {
            Topic cityType = getCityTypeTopic(tm);
            theCity=tm.createTopic();
            theCity.addSubjectIdentifier(tm.createLocator(citySI));
            //theCity.setBaseName(city + " (geoname)");
            if(lang != null) theCity.setDisplayName(lang, city);
            else theCity.setDisplayName("en", city);
            theCity.addType(cityType);
        }
        return theCity;
    }
    
    public static Topic getCityTopic(TopicMap tm, String geonameid, String cityname, String lang) throws TopicMapException {
        String citySI = createGeonameSI(geonameid);
        Topic theCity = null;
        if(USE_EXISTING_TOPICS) theCity = tm.getTopic(citySI);
        if(theCity == null) {
            Topic cityType = getCityTypeTopic(tm);
            theCity=tm.createTopic();
            theCity.addSubjectIdentifier(tm.createLocator(citySI));
            theCity.setBaseName(cityname + " ("+geonameid+")");
            if(lang != null) theCity.setDisplayName(lang, cityname);
            else theCity.setDisplayName(LANG, cityname);
            theCity.addType(cityType);
        }
        return theCity;
    }
    
    // -------
    
    
    
    public static Topic getCurrencyTopic(TopicMap tm, String currency, String lang) throws TopicMapException {
        String currencySI = CURRENCY_SI + "/" + encode(currency);
        Topic theCurrency = null;
        if(USE_EXISTING_TOPICS) theCurrency = tm.getTopic(currencySI);
        if(theCurrency == null) {
            Topic currencyType = getCurrencyTypeTopic(tm);
            theCurrency=tm.createTopic();
            theCurrency.addSubjectIdentifier(tm.createLocator(currencySI));
            theCurrency.setBaseName(currency + " (currency)");
            if(lang != null) theCurrency.setDisplayName(lang, currency);
            else theCurrency.setDisplayName("en", currency);
            theCurrency.addType(currencyType);
        }
        return theCurrency;
    }
    
    
    
    
    public static Topic getGPSNumberTopic(TopicMap tm, String gpsnumber) throws TopicMapException {
        String gpsSI = GPS_SI + "/" + encode(gpsnumber);
        Topic theGPSNumber = null;
        if(USE_EXISTING_TOPICS) theGPSNumber = tm.getTopic(gpsSI);
        if(theGPSNumber == null) {
            Topic gpsNumberType = getGPSNumberTypeTopic(tm);
            theGPSNumber=tm.createTopic();
            theGPSNumber.addSubjectIdentifier(tm.createLocator(gpsSI));
            theGPSNumber.setBaseName(gpsnumber + " (gps number)");
            theGPSNumber.setDisplayName(null, gpsnumber);
            theGPSNumber.addType(gpsNumberType);
        }
        return theGPSNumber;
    }
    
    
    
    public static Topic getLanguageTopic(TopicMap tm, String l) throws TopicMapException {
        String languageSI = LANGUAGE_SI + "/" + encode(l);
        Topic theLanguage = null;
        if(USE_EXISTING_TOPICS) theLanguage = tm.getTopic(languageSI);
        if(theLanguage == null) {
            Topic languageType = getLanguageTypeTopic(tm);
            theLanguage=tm.createTopic();
            theLanguage.addSubjectIdentifier(tm.createLocator(languageSI));
            theLanguage.setBaseName(l + " (language)");
            theLanguage.addType(languageType);
        }
        return theLanguage;
    }
    
    
    
    
    public static Topic getFCLTopic(TopicMap tm, String fclCode, String fclName) throws TopicMapException {
        String fclSI = FCL_SI + "/" + encode(fclCode);
        Topic theFCL = null;
        if(USE_EXISTING_TOPICS) theFCL = tm.getTopic(fclSI);
        if(theFCL == null) {
            Topic fclType = getFCLTypeTopic(tm);
            theFCL=tm.createTopic();
            theFCL.addSubjectIdentifier(tm.createLocator(fclSI));
            if(fclName != null && fclName.length() > 0) theFCL.setBaseName(fclName + " (fcl)");
            theFCL.addType(fclType);
        }
        return theFCL;
    }
    
    
    
    public static Topic getFCodeTopic(TopicMap tm, String fCode, String fCodeName) throws TopicMapException {
        String fCodeSI = FCODE_SI + "/" + encode(fCode);
        Topic fCodeTopic = null;
        if(USE_EXISTING_TOPICS) fCodeTopic = tm.getTopic(fCodeSI);
        if(fCodeTopic == null) {
            Topic fCodeType = getFCodeTypeTopic(tm);
            fCodeTopic=tm.createTopic();
            fCodeTopic.addSubjectIdentifier(tm.createLocator(fCodeSI));
            if(fCodeName != null && fCodeName.length() > 0) fCodeTopic.setBaseName(fCodeName + " (fcode)");
            fCodeTopic.addType(fCodeType);
        }
        return fCodeTopic;
    }
    
    
    public static Topic getWikipediaGeoFeatureTopic(TopicMap tm, String feature) throws TopicMapException {
        String si = WIKIPEDIA_GEO_FEATURE_SI + "/" + encode(feature);
        Topic featureTopic = null;
        if(USE_EXISTING_TOPICS) featureTopic = tm.getTopic(si);
        if(featureTopic == null) {
            Topic featureType = getWikipediaFeatureTypeTopic(tm);
            featureTopic=tm.createTopic();
            featureTopic.addSubjectIdentifier(tm.createLocator(si));
            if(feature != null && feature.length() > 0) featureTopic.setBaseName(feature + " (wikipedia geo-feature)");
            featureTopic.addType(featureType);
        }
        return featureTopic;
    }
    
    
    public static Topic getElevationTopic(TopicMap tm, String elevation, String lang) throws TopicMapException {
        String elevationSI = ELEVATION_SI + "/" + encode(elevation);
        Topic elevationTopic = null;
        if(USE_EXISTING_TOPICS) elevationTopic = tm.getTopic(elevationSI);
        if(elevationTopic == null) {
            Topic elevationType = getElevationTypeTopic(tm);
            elevationTopic=tm.createTopic();
            elevationTopic.addSubjectIdentifier(tm.createLocator(elevationSI));
            if(elevation != null && elevation.length() > 0) {
                elevationTopic.setBaseName(elevation + " (elevation)");
                elevationTopic.setDisplayName(null, elevation);
            }
            elevationTopic.addType(elevationType);
        }
        return elevationTopic;
    }
    


    public static Topic getGeoNamesClassTopic(TopicMap tm) throws TopicMapException {
        Topic gnTopic = getOrCreateTopic(tm, GEONAMES_PREFIX, "GeoNames");
        gnTopic.addType(getWandoraClassTopic(tm));
        return gnTopic;
    }



    public static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }

    public static Topic getGenericTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, "http://wandora.org/si/topic", "Topic");
    }


    
    public static String createCountryAlpha3SI(String alpha3) {
        return ISOALPHA3_SI+alpha3;
    }
    public static String createCountryNumericSI(String n) {
        return ISONUMERIC_SI+n;
    }
    public static String createFIPSSI(String fips) {
        return FIPS_SI+fips;
    }
    public static String createGeonameSI(String geonameid) {
        return GEONAMEID_SI+"/"+geonameid;
    }
    
    
    public static boolean isValidGPSCoordinate(String coordinate) {
        boolean isValid = false;
        
        Pattern GPSPattern = Pattern.compile("[\\+\\-]?\\d+(\\.\\d+)?");
        Matcher GPSMatcher = GPSPattern.matcher(coordinate);
        if(GPSMatcher.matches()) isValid = true;
        
        return isValid;
    }
    
    
    public static void nameGeoObjectTopic(Topic geoTopic, HashMap<String, String> names) {
        if(geoTopic == null || names == null || names.isEmpty()) return;
        Set<String> keys = names.keySet();
        String alang = null;
        String aname = null;
        for(Iterator<String> keyIter = keys.iterator(); keyIter.hasNext(); ) {
            try {
                alang = keyIter.next();
                if(USE_ALL_VARIANT_NAMES || isValidSchemaLanguage(geoTopic, alang)) {
                    aname = names.get(alang);
                    geoTopic.setDisplayName(alang, aname);
                }
            }
            catch(Exception e) {
                // DO NOTHING!
            }
        }
    }
    
    
    public static boolean isValidSchemaLanguage(Topic referenceTopic, String lang) {
        boolean isValid = false;
        String langSI = XTMPSI.getLang(lang);
        TopicMap tm = referenceTopic.getTopicMap();
        if(tm != null && langSI != null) {
            try {
                Topic langTopic = tm.getTopic(langSI);
                if(langTopic != null) {
                    Collection<Topic> languageTopics = tm.getTopicsOfType(TMBox.LANGUAGE_SI);
                    Topic schemaLangTopic = null;
                    for(Iterator<Topic> i=languageTopics.iterator(); i.hasNext(); ) {
                        schemaLangTopic = (Topic) i.next();
                        if(langTopic.mergesWithTopic(schemaLangTopic)) {
                            isValid = true;
                            break;
                        }
                    }
                }
            }
            catch(Exception e) {
                // DO NOTHING...
            }
        }
        return isValid;
    }
    
    
    
    public static String encode(String str) {
        try {
            str = URLEncoder.encode(str, "UTF-8");
        }
        catch(Exception e) {
            // DO NOTHING
        }
        return str;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // --- DEFAULT RELATIONS ---------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    public static void makeGeoContinent(Topic geoTopic, Topic continent, TopicMap tm) throws TopicMapException {
        if(geoTopic == null || continent == null || geoTopic.mergesWithTopic(continent)) return;

        Topic continentType=getContinentTypeTopic(tm);
        Topic geoObjectType=getGeoObjectTypeTopic(tm);

        Association a=tm.createAssociation(continentType);
        a.addPlayer(geoTopic, geoObjectType);
        a.addPlayer(continent, continentType);
    }
    
    
    
    
    public static void makeGeoCountry(Topic geoTopic, Topic country, TopicMap tm) throws TopicMapException {
        if(geoTopic == null || country == null || geoTopic.mergesWithTopic(country)) return;

        Topic countryType=getCountryTypeTopic(tm);
        Topic geoObjectType=getGeoObjectTypeTopic(tm);

        Association a=tm.createAssociation(countryType);
        a.addPlayer(geoTopic, geoObjectType);
        a.addPlayer(country, countryType);
    }
    
    
    
    
    public static void makeChildParent(Topic child, Topic parent, TopicMap tm) throws TopicMapException {
        if(child == null || parent == null || child.mergesWithTopic(parent)) return;

        Topic parentChildType=getParentChildTypeTopic(tm);
        Topic parentType=getParentTypeTopic(tm);
        Topic childType=getChildTypeTopic(tm);

        Association a=tm.createAssociation(parentChildType);
        a.addPlayer(child, childType);
        a.addPlayer(parent, parentType);
    }
    
    
    
    
    public static void makePartWhole(Topic part, Topic whole, TopicMap tm) throws TopicMapException {
        if(part == null || whole == null || part.mergesWithTopic(whole)) return;

        Topic partWholeType=getPartWholeTypeTopic(tm);
        Topic wholeType=getWholeTypeTopic(tm);
        Topic partType=getPartTypeTopic(tm);

        Association a=tm.createAssociation(partWholeType);
        a.addPlayer(part, partType);
        a.addPlayer(whole, wholeType);
    }
    
    
    
    public static void makeSiblings(Topic s1, Topic s2, TopicMap tm) throws TopicMapException {
        if(s1 == null || s2 == null || s1.mergesWithTopic(s2)) return;

        Topic siblingsType=getSiblingsTypeTopic(tm);
        Topic siblingType=getSiblingTypeTopic(tm);
        Topic sibling2Type=getSibling2TypeTopic(tm);

        Association a=tm.createAssociation(siblingsType);
        a.addPlayer(s1, siblingType);
        a.addPlayer(s2, sibling2Type);
    }
    
    
    
    
    public static void makeNeighbours(Topic n1, Topic n2, TopicMap tm) throws TopicMapException {
        if(n1 == null || n2 == null || n1.mergesWithTopic(n2)) return;

        Topic neighboursType=getNeighboursTypeTopic(tm);
        Topic neighbourType=getNeighbourTypeTopic(tm);
        Topic neighbour2Type=getNeighbour2TypeTopic(tm);

        Association a=tm.createAssociation(neighboursType);
        a.addPlayer(n1, neighbourType);
        a.addPlayer(n2, neighbour2Type);
    }
    

    
    public static void makeLatLong(String latStr, String lngStr, Topic geoTopic, TopicMap tm) throws TopicMapException {
        Topic locatedType=getLocatedTypeTopic(tm);
        Topic locationType=getLocationTypeTopic(tm);
        Topic latType=getLatTypeTopic(tm);
        Topic lngType=getLngTypeTopic(tm);

        Topic lat = getGPSNumberTopic(tm, latStr);
        Topic lng = getGPSNumberTopic(tm, lngStr);

        Association la = tm.createAssociation(locationType);
        la.addPlayer(lat, latType);
        la.addPlayer(lng, lngType);
        la.addPlayer(geoTopic, locatedType);
    }
    
    
    
    
    public static void makeBoundingBox(Topic geoTopic, String n, String w, String s, String e, TopicMap tm) throws TopicMapException {
        Topic geoObjectType=getGeoObjectTypeTopic(tm);
        Topic bBoxType=getBBoxTypeTopic(tm);
        Topic bBoxWestType=getBBoxWestTypeTopic(tm);
        Topic bBoxEastType=getBBoxEastTypeTopic(tm);
        Topic bBoxNorthType=getBBoxNorthTypeTopic(tm);
        Topic bBoxSouthType=getBBoxSouthTypeTopic(tm);

        Topic west = getGPSNumberTopic(tm, w);
        Topic east = getGPSNumberTopic(tm, e);
        Topic north = getGPSNumberTopic(tm, n);
        Topic south = getGPSNumberTopic(tm, s);

        Association bba = tm.createAssociation(bBoxType);
        bba.addPlayer(east, bBoxEastType);
        bba.addPlayer(west, bBoxWestType);
        bba.addPlayer(north, bBoxNorthType);
        bba.addPlayer(south, bBoxSouthType);
        bba.addPlayer(geoTopic, geoObjectType);
    }
    
    
    
    public static void makeCountryCapital(Topic country, Topic capital, TopicMap tm) throws TopicMapException {
        Topic countryCapitalType = getCountryCapitalTypeTopic(tm);
        Topic countryType = getCountryTypeTopic(tm);
        Topic capitalType = getCapitalTypeTopic(tm);
        capital.addType(capitalType);

        Association a=tm.createAssociation(countryCapitalType);
        a.addPlayer(country, countryType);
        a.addPlayer(capital, capitalType);
    }
    
    
    
    public static void makeGeoElevation(Topic geoObject, Topic elevation, TopicMap tm) throws TopicMapException {
        Topic elevationType = getElevationTypeTopic(tm);
        Topic geoObjectType = getGeoObjectTypeTopic(tm);

        Association a=tm.createAssociation(elevationType);
        a.addPlayer(geoObject, geoObjectType);
        a.addPlayer(elevation, elevationType);
    }
    
    
    // -------------------------------------------------------------------------
    
    
    

    
    
    public static String[][] getFeatureClassData() {
        String[][] continentData = new String[][] {
            { "A", "country, state, region,..." },
            { "H", "stream, lake, ..." },
            { "L", "parks,area, ..." },
            { "P", "city, village,..." },
            { "R", "road, railroad " },
            { "S", "spot, building, farm" },
            { "T", "mountain,hill,rock,... " },
            { "U", "undersea" },
            { "V", "forest,heath,..." },
        };
        return continentData;
    }
    
    
    
    public static String solveFeatureClassCode(String featureClassName) {
        if(featureClassName == null) return null;
        String featureClassCode = null;
        String[][] featureClassData = getFeatureClassData();
        for(int j=0; j<featureClassData.length; j++) {
            if(featureClassName.equalsIgnoreCase(featureClassData[j][1])) {
                featureClassCode = featureClassData[j][0];
                break;
            }
        }
        return featureClassCode;
    }
    
    
    
    public static String[][] getContinentData() {
        String[][] continentData = new String[][] {
            { "AF", "Africa" },
            { "AS", "Asia" },
            { "EU", "Europe" },
            { "NA", "North America" },
            { "OC", "Oceanic" },
            { "SA", "South America" },
            { "AN", "Antarctica" },
        };
        return continentData;
    }
    
    
    public static String solveContinentCode(String continentName) {
        if(continentName == null) return null;
        String continentCode = null;
        String[][] continentData = getContinentData();
        for(int j=0; j<continentData.length; j++) {
            if(continentName.equalsIgnoreCase(continentData[j][1])) {
                continentCode = continentData[j][0];
                break;
            }
        }
        return continentCode;
    }
    
    
    
    
    public static String[][] getCountryData() {
        String[][] countryData = new String[][] {
            { "AD", "Andorra" },
            { "AE", "United Arab Emirate" },
            { "AF", "Afghanistan" },
            { "AG", "Antigua and Barbuda" },
            { "AI", "Anguilla" },
            { "AL", "Albania" },
            { "AM", "Armenia" },
            { "AN", "Netherlands Antilles" },
            { "AO", "Angola" },
            { "AQ", "Antarctica" },
            { "AR", "Argentina" },
            { "AS", "American Samoa" },
            { "AT", "Austria" },
            { "AU", "Australia" },
            { "AW", "Aruba" },
            { "AZ", "Azerbaijan" },
            { "BA", "Bosnia and Herzegovina" },
            { "BB", "Barbados" },
            { "BD", "Bangladesh" },
            { "BE", "Belgium" },
            { "BF", "Burkina Faso" },
            { "BG", "Bulgaria" },
            { "BH", "Bahrain" },
            { "BI", "Burundi" },
            { "BJ", "Benin" },
            { "BL", "Saint Barth�lemy" },
            { "BM", "Bermuda" },
            { "BN", "Brunei" },
            { "BO", "Bolivia" },
            { "BR", "Brazil" },
            { "BS", "Bahamas" },
            { "BT", "Bhutan" },
            { "BV", "Bouvet Island" },
            { "BW", "Botswana" },
            { "BY", "Belarus" },
            { "BZ", "Belize" },
            { "CA", "Canada" },
            { "CC", "Cocos Islands" },
            { "CD", "Congo - Kinshasa" },
            { "CF", "Central African Republic" },
            { "CG", "Congo - Brazzaville" },
            { "CH", "Switzerland" },
            { "CI", "Ivory Coast" },
            { "CK", "Cook Islands" },
            { "CL", "Chile" },
            { "CM", "Cameroon" },
            { "CN", "China" },
            { "CO", "Colombia" },
            { "CR", "Costa Rica" },
            { "CU", "Cuba" },
            { "CV", "Cape Verde" },
            { "CX", "Christmas Island" },
            { "CY", "Cyprus" },
            { "CZ", "Czech Republic" },
            { "DE", "Germany" },
            { "DJ", "Djibouti" },
            { "DK", "Denmark" },
            { "DM", "Dominica" },
            { "DO", "Dominican Republic" },
            { "DZ", "Algeria" },
            { "EC", "Ecuador" },
            { "EE", "Estonia" },
            { "EG", "Egypt" },
            { "EH", "Western Sahara" },
            { "ER", "Eritrea" },
            { "ES", "Spain" },
            { "ET", "Ethiopia" },
            { "FI", "Finland" },
            { "FJ", "Fiji" },
            { "FK", "Falkland Islands" },
            { "FM", "Micronesia" },
            { "FO", "Faroe Islands" },
            { "FR", "France" },
            { "GA", "Gabon" },
            { "GB", "United Kingdom" },
            { "GD", "Grenada" },
            { "GE", "Georgia" },
            { "GF", "French Guiana" },
            { "GG", "Guernsey" },
            { "GH", "Ghana" },
            { "GI", "Gibraltar" },
            { "GL", "Greenland" },
            { "GM", "Gambia" },
            { "GN", "Guinea" },
            { "GP", "Guadeloupe" },
            { "GQ", "Equatorial Guinea" },
            { "GR", "Greece" },
            { "GS", "South Georgia and the South Sandwich Islands" },
            { "GT", "Guatemala" },
            { "GU", "Guam" },
            { "GW", "Guinea-Bissau" },
            { "GY", "Guyana" },
            { "HK", "Hong Kong" },
            { "HM", "Heard Island and McDonald Islands" },
            { "HN", "Honduras" },
            { "HR", "Croatia" },
            { "HT", "Haiti" },
            { "HU", "Hungary" },
            { "ID", "Indonesia" },
            { "IE", "Ireland" },
            { "IL", "Israel" },
            { "IM", "Isle of Man" },
            { "IN", "India" },
            { "IO", "British Indian Ocean Territory" },
            { "IQ", "Iraq" },
            { "IR", "Iran" },
            { "IS", "Iceland" },
            { "IT", "Italy" },
            { "JE", "Jersey" },
            { "JM", "Jamaica" },
            { "JO", "Jordan" },
            { "JP", "Japan" },
            { "KE", "Kenya" },
            { "KG", "Kyrgyzstan" },
            { "KH", "Cambodia" },
            { "KI", "Kiribati" },
            { "KM", "Comoros" },
            { "KN", "Saint Kitts and Nevis" },
            { "KP", "North Korea" },
            { "KR", "South Korea" },
            { "KW", "Kuwait" },
            { "KY", "Cayman Islands" },
            { "KZ", "Kazakhstan" },
            { "LA", "Laos" },
            { "LB", "Lebanon" },
            { "LC", "Saint Lucia" },
            { "LI", "Liechtenstein" },
            { "LK", "Sri Lanka" },
            { "LR", "Liberia" },
            { "LS", "Lesotho" },
            { "LT", "Lithuania" },
            { "LU", "Luxembourg" },
            { "LV", "Latvia" },
            { "LY", "Libya" },
            { "MA", "Morocco" },
            { "MC", "Monaco" },
            { "MD", "Moldova" },
            { "ME", "Montenegro" },
            { "MF", "Saint Martin" },
            { "MG", "Madagascar" },
            { "MH", "Marshall Islands" },
            { "MK", "Macedonia" },
            { "ML", "Mali" },
            { "MM", "Myanmar" },
            { "MN", "Mongolia" },
            { "MO", "Macao" },
            { "MP", "Northern Mariana Islands" },
            { "MQ", "Martinique" },
            { "MR", "Mauritania" },
            { "MS", "Montserrat" },
            { "MT", "Malta" },
            { "MU", "Mauritius" },
            { "MV", "Maldives" },
            { "MW", "Malawi" },
            { "MX", "Mexico" },
            { "MY", "Malaysia" },
            { "MZ", "Mozambique" },
            { "NA", "Namibia" },
            { "NC", "New Caledonia" },
            { "NE", "Niger" },
            { "NF", "Norfolk Island" },
            { "NG", "Nigeria" },
            { "NI", "Nicaragua" },
            { "NL", "Netherlands" },
            { "NO", "Norway" },
            { "NP", "Nepal" },
            { "NR", "Nauru" },
            { "NU", "Niue" },
            { "NZ", "New Zealand" },
            { "OM", "Oman" },
            { "PA", "Panama" },
            { "PE", "Peru" },
            { "PF", "French Polynesia" },
            { "PG", "Papua New Guinea" },
            { "PH", "Philippines" },
            { "PK", "Pakistan" },
            { "PL", "Poland" },
            { "PM", "Saint Pierre and Miquelon" },
            { "PN", "Pitcairn" },
            { "PR", "Puerto Rico" },
            { "PS", "Palestinian Territory" },
            { "PT", "Portugal" },
            { "PW", "Palau" },
            { "PY", "Paraguay" },
            { "QA", "Qatar" },
            { "RE", "Reunion" },
            { "RO", "Romania" },
            { "RS", "Serbia" },
            { "RU", "Russia" },
            { "RW", "Rwanda" },
            { "SA", "Saudi Arabia" },
            { "SB", "Solomon Islands" },
            { "SC", "Seychelles" },
            { "SD", "Sudan" },
            { "SE", "Sweden" },
            { "SG", "Singapore" },
            { "SH", "Saint Helena" },
            { "SI", "Slovenia" },
            { "SJ", "Svalbard and Jan Mayen" },
            { "SK", "Slovakia" },
            { "SL", "Sierra Leone" },
            { "SM", "San Marino" },
            { "SN", "Senegal" },
            { "SO", "Somalia" },
            { "SR", "Suriname" },
            { "ST", "Sao Tome and Principe" },
            { "SV", "El Salvador" },
            { "SY", "Syria" },
            { "SZ", "Swaziland" },
            { "TC", "Turks and Caicos Islands" },
            { "TD", "Chad" },
            { "TF", "French Southern Territories" },
            { "TG", "Togo" },
            { "TH", "Thailand" },
            { "TJ", "Tajikistan" },
            { "TK", "Tokelau" },
            { "TL", "East Timor" },
            { "TM", "Turkmenistan" },
            { "TN", "Tunisia" },
            { "TO", "Tonga" },
            { "TR", "Turkey" },
            { "TT", "Trinidad and Tobago" },
            { "TV", "Tuvalu" },
            { "TW", "Taiwan" },
            { "TZ", "Tanzania" },
            { "UA", "Ukraine" },
            { "UG", "Uganda" },
            { "UM", "United States Minor Outlying Islands" },
            { "US", "United States" },
            { "UY", "Uruguay" },
            { "UZ", "Uzbekistan" },
            { "VA", "Vatican" },
            { "VC", "Saint Vincent and the Grenadines" },
            { "VE", "Venezuela" },
            { "VG", "British Virgin Islands" },
            { "VI", "U.S. Virgin Islands" },
            { "VN", "Vietnam" },
            { "VU", "Vanuatu" },
            { "WF", "Wallis and Futuna" },
            { "WS", "Samoa" },
            { "YE", "Yemen" },
            { "YT", "Mayotte" },
            { "ZA", "South Africa" },
            { "ZM", "Zambia" },
            { "ZW", "Zimbabwe" },
        };
        return countryData;
    }
    
    
    
    public static String solveCountryCode(String countryName) {
        if(countryName == null) return null;
        String countryCode = null;
        String[][] countryData = getCountryData();
        for(int j=0; j<countryData.length; j++) {
            if(countryName.equalsIgnoreCase(countryData[j][1])) {
                countryCode = countryData[j][0];
                break;
            }
        }
        return countryCode;
    }
    
    
    
    public static String solveCountryName(String countryCode) {
        if(countryCode == null) return null;
        String countryName = null;
        String[][] countryData = getCountryData();
        for(int j=0; j<countryData.length; j++) {
            if(countryCode.equalsIgnoreCase(countryData[j][0])) {
                countryName = countryData[j][1];
                break;
            }
        }
        return countryName;
    }
    
    
    
    
    // -------------------------------------------------------------------------
    // ------------------------------------------------------------- WEATHER ---
    // -------------------------------------------------------------------------
    
    
    
    protected static Topic getWeatherObservationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_OBSERVATION_SI, "weather observation (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    
    protected static Topic getWeatherObservationTimeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_OBSERVATIONTIME_SI, "weather observation time (geonames)");
        return type;
    }
    
    protected static Topic getWeatherStationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_STATION_SI, "weather station (geonames)");
        Topic gnClass = getGeoNamesClassTopic(tm);
        makeSubclassOf(tm, type, gnClass);
        return type;
    }
    
    protected static Topic getWeatherICAOTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_ICAO_SI, "weather station ICAO (geonames)");
        return type;
    }
    
    protected static Topic getWeatherElevationTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_ELEVATION_SI, "weather station elevation (geonames)");
        return type;
    }
    
    protected static Topic getWeatherTemperatureTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_TEMPERATURE_SI, "weather temperature (geonames)");
        return type;
    }
    
    protected static Topic getWeatherDewPointTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_DEWPOINT_SI, "weather dew point (geonames)");
        return type;
    }
    protected static Topic getWeatherHumidityTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_HUMIDITY_SI, "weather humidity (geonames)");
        return type;
    }
    protected static Topic getWeatherCloudsTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_CLOUDS_SI, "weather clouds (geonames)");
        return type;
    }
    protected static Topic getWeatherConditionTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_WEATHERCONDITION_SI, "weather condition (geonames)");
        return type;
    }
    protected static Topic getWeatherHectoPascAltimeterTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_HECTOPASCALTIMETER_SI, "weather hectopascaltimeter (geonames)");
        return type;
    }
    protected static Topic getWeatherWindDirectionTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_WINDDIRECTION_SI, "weather wind direction (geonames)");
        return type;
    }
    protected static Topic getWeatherWindSpeedTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, WEATHER_WINDSPEED_SI, "weather wind speed (geonames)");
        return type;
    }
    
    public static Topic getWeatherObservationTopic(TopicMap tm, String observation, String lang) throws TopicMapException {
        String observationSI = null;
        try {
            observationSI = WEATHER_OBSERVATION_SI + "/" + URLEncoder.encode(observation, "UTF-8");
        }
        catch(Exception e) {
            observationSI = WEATHER_OBSERVATION_SI + "/" + observation;
        }
        Topic theObservation = null;
        if(USE_EXISTING_TOPICS) theObservation = tm.getTopic(observationSI);
        if(theObservation == null) {
            Topic observationType = getWeatherObservationTypeTopic(tm);
            theObservation=tm.createTopic();
            theObservation.addSubjectIdentifier(tm.createLocator(observationSI));
            if(observation != null && observation.length() > 0) theObservation.setBaseName(observation);
            theObservation.addType(observationType);
        }
        return theObservation;
    }
    
    
    
    public static Topic getWeatherStationTopic(TopicMap tm, String station, String lang) throws TopicMapException {
        String stationSI = null;
        try {
            stationSI = WEATHER_STATION_SI + "/" + URLEncoder.encode(station, "UTF-8");
        }
        catch(Exception e) {
            stationSI = WEATHER_STATION_SI + "/" + station;
        }
        Topic theStation = null;
        if(USE_EXISTING_TOPICS) theStation = tm.getTopic(stationSI);
        if(theStation == null) {
            Topic stationType = getWeatherStationTypeTopic(tm);
            theStation=tm.createTopic();
            theStation.addSubjectIdentifier(tm.createLocator(stationSI));
            if(station != null && station.length() > 0) theStation.setBaseName(station);
            theStation.addType(stationType);
        }
        return theStation;
    }
    
    
    public static Topic getWeatherCloudsTopic(TopicMap tm, String clouds, String lang) throws TopicMapException {
        String cloudsSI = null;
        try {
            cloudsSI = WEATHER_CLOUDS_SI + "/" + URLEncoder.encode(clouds, "UTF-8");
        }
        catch(Exception e) {
            cloudsSI = WEATHER_CLOUDS_SI + "/" + clouds;
        }
        Topic theClouds = null;
        if(USE_EXISTING_TOPICS) theClouds = tm.getTopic(cloudsSI);
        if(theClouds == null) {
            Topic cloudsType = getWeatherCloudsTypeTopic(tm);
            theClouds=tm.createTopic();
            theClouds.addSubjectIdentifier(tm.createLocator(cloudsSI));
            if(clouds != null && clouds.length() > 0) theClouds.setBaseName(clouds);
            theClouds.addType(cloudsType);
        }
        return theClouds;
    }
    
    
    
    
    public static void makeWeatherStation(Topic observation, Topic station, TopicMap tm) throws TopicMapException {
        if(observation == null || station == null || observation.mergesWithTopic(station)) return;

        Topic stationType=getWeatherStationTypeTopic(tm);
        Topic observationType=getWeatherObservationTypeTopic(tm);

        Association a=tm.createAssociation(stationType);
        a.addPlayer(station, stationType);
        a.addPlayer(observation, observationType);
    }
    
    
    public static void makeWeatherClouds(Topic observation, Topic clouds, TopicMap tm) throws TopicMapException {
        if(observation == null || clouds == null || observation.mergesWithTopic(clouds)) return;

        Topic cloudsType=getWeatherCloudsTypeTopic(tm);
        Topic observationType=getWeatherObservationTypeTopic(tm);

        Association a=tm.createAssociation(cloudsType);
        a.addPlayer(clouds, cloudsType);
        a.addPlayer(observation, observationType);
    }
    
    
    
    
    
// -------------------------------------------------------------------------





    
}



