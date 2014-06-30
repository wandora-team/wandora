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
 * GeoNamesWikipediaSearch.java
 * 
 */



package org.wandora.application.tools.extractors.geonames;

import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;




/**
 *
 * @author akivela
 */
public class GeoNamesWikipediaSearch extends AbstractGeoNamesExtractor {
    public String dataLang = "en";
    public String requestGeoObject = null;
    
    
    @Override
    public String getName() {
        return "GeoNames wikipedia search extractor";
    }
    
    
    @Override
    public String getDescription(){
        return "Search wikipedia geo locations from GeoNames web api and convert search results to a topic map.";
    }
    
    public void setRequestGeoObject(String p) {
        this.requestGeoObject = p;
    }
    
    /*
    @Override
    public synchronized void extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        try {
            String str = url.toExternalForm();
            Pattern p = Pattern.compile("geonameId=(\\w+)");
            Matcher m = p.matcher(str);
            if(m.find()) {
                String geoid = m.group(1);
                if(geoid != null && geoid.length() > 0) {
                    setRequestGeoObject(geoid);
                }
            }
        }
        catch(Exception e) {}
        super.extractTopicsFrom(url, topicMap);
    }
    */
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        GeoNamesWikipediaSearchParser parserHandler = new GeoNamesWikipediaSearchParser(topicMap,this,dataLang);
        parserHandler.setRequestGeoObject(requestGeoObject);
        
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        if(parserHandler.progress == 0) log("No search results found!");
        else if(parserHandler.progress == 1) log("One search result found!");
        else log("Total " + parserHandler.progress + " search result found!");
        requestGeoObject = null; // FORCE NULL AS THIS OBJECT IS REUSED.
        return true;
    }
    

    
    
    
    
    // -------------------------------------------------------------------------
    // --- GEONAMES XML FEED PARSER --------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    private class GeoNamesWikipediaSearchParser extends AbstractGeoNamesWikipediaParser {

        
        public GeoNamesWikipediaSearchParser(TopicMap tm, AbstractGeoNamesExtractor parent, String lang){
            super(tm, parent, lang);
        }
        
        
        
        public void handleEntryElement() {
            if(data_title.length() > 0) {
                try {
                    Topic wikiGeoObject = getWikipediaGeoTopic(tm, data_title, data_wikipediaurl, lang);
                    parent.setProgress(progress++);

                    try {
                        if(isValid(data_summary)) {
                            Topic typeTopic = getWikipediaSummaryTypeTopic(tm);
                            wikiGeoObject.setData(typeTopic, TMBox.getLangTopic(wikiGeoObject, lang), data_summary);
                        }
                        if(isValid(data_thumbnailimg)) {
                            Topic img = tm.createTopic();
                            img.addSubjectIdentifier(new org.wandora.topicmap.Locator(data_thumbnailimg));
                            img.setSubjectLocator(new org.wandora.topicmap.Locator(data_thumbnailimg));
                            Topic imgTypeTopic = getWikipediaThumbnailTypeTopic(tm);
                            Topic geoType = getWikipediaGeoObjectTypeTopic(tm);
                            Association a = tm.createAssociation(imgTypeTopic);
                            a.addPlayer(img, imgTypeTopic);
                            a.addPlayer(wikiGeoObject, geoType);
                        }
                        if(isValid(data_feature)) {
                            Topic featureTopic = getWikipediaGeoFeatureTopic(tm, data_feature);
                            wikiGeoObject.addType(featureTopic);
                        }
                        if(isValid(data_countrycode)) {
                            Topic countryTopic=getCountryTopic(tm, data_countrycode, lang);
                            makeGeoCountry(wikiGeoObject, countryTopic, tm);
                        }
                        if(isValid(data_lat) &&
                           isValid(data_lng)) {
                             makeLatLong(data_lat, data_lng, wikiGeoObject, tm);
                        }
                        if(isValid(data_population) && !data_population.equals("0")) {
                            Topic populationTypeTopic = getPopulationTypeTopic(tm);
                            wikiGeoObject.setData(populationTypeTopic, TMBox.getLangTopic(wikiGeoObject, lang), data_population);
                        }
                    }
                    catch(Exception e) {
                        parent.log(e);
                    }
                }
                catch(TopicMapException tme){
                    parent.log(tme);
                }
            }
        }
    }
}
