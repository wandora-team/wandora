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
 * GeoNamesNeighbours.java
 */

package org.wandora.application.tools.extractors.geonames;


import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;
import java.net.*;
import java.util.regex.*;



/**
 * See http://ws.geonames.org/neighbours?geonameId=2658434&style=full
 *
 * @author akivela
 */
public class GeoNamesNeighbours extends AbstractGeoNamesExtractor {

	private static final long serialVersionUID = 1L;
	
	public String dataLang = "en";
    public String requestGeoObject = null;
    
    
    @Override
    public String getName() {
        return "GeoNames neighbours extractor";
    }
    
    
    @Override
    public String getDescription(){
        return "Get the neighbours for given geo location and convert the neighbours to a topic map.";
    }
    
    public void setRequestGeoObject(String p) {
        this.requestGeoObject = p;
    }
    
    
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
    
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        NeighboursParser parserHandler = new NeighboursParser(topicMap,this,dataLang);
        parserHandler.setRequestGeoObject(requestGeoObject);
        
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        if(parserHandler.progress == 0) log("No neighbors found!");
        else if(parserHandler.progress == 1) log("One neighbor found!");
        else log("Total " + parserHandler.progress + " neighbors found!");
        requestGeoObject = null; // FORCE NULL AS THIS OBJECT IS REUSED.
        return true;
    }
    

    
    
    
    
    // -------------------------------------------------------------------------
    // --- GEONAMES XML FEED PARSER --------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    private static class NeighboursParser extends AbstractGeoNamesParser {
        
        
        public NeighboursParser(TopicMap tm, AbstractGeoNamesExtractor parent, String lang){
            super(tm, parent, lang);
        }
        
        public void handleGeoNameElement() {
            if(data_name.length() > 0 || data_geonameid.length() > 0) {
                try {
                    theGeoObject=getGeoTopic(tm, data_geonameid, data_name, lang);
                    theGeoObjectSI=theGeoObject.getOneSubjectIdentifier().toExternalForm();
                    parent.setProgress(progress++);

                    try {
                        if(isValid(data_fcode)) {
                            Topic fcodeTopic = getFCodeTopic(tm, data_fcode, data_fcodename);
                            theGeoObject.addType(fcodeTopic);
                        }
                        if(isValid(data_fcl)) {
                            Topic fclTopic = getFCLTopic(tm, data_fcl, data_fclname);
                            theGeoObject.addType(fclTopic);
                        }
                        if(isValid(data_countrycode) &&
                           isValid(data_countryname)) {
                            Topic countryTopic=getCountryTopic(tm, data_countrycode, data_countryname, lang);
                            if(theGeoObject.isRemoved()) theGeoObject=tm.getTopic(theGeoObjectSI);
                            makeGeoCountry(theGeoObject, countryTopic, tm);
                        }
                        if(isValid(data_continentcode)) {
                            Topic continentTopic=getContinentTopic(tm, data_continentcode);
                            if(theGeoObject.isRemoved()) theGeoObject=tm.getTopic(theGeoObjectSI);
                            makeGeoContinent(theGeoObject, continentTopic, tm);
                        }
                        if(isValid(requestGeoObject)) {
                            Topic requestTopic=getGeoTopic(tm, requestGeoObject);
                            if(theGeoObject.isRemoved()) theGeoObject=tm.getTopic(theGeoObjectSI);
                            makeNeighbours(requestTopic, theGeoObject, tm);
                        }
                        if(isValid(data_lat) &&
                           isValid(data_lng)) {
                             makeLatLong(data_lat, data_lng, theGeoObject, tm);
                        }
                        if(data_alternatename_all != null && data_alternatename_all.size() > 0) {
                            if(theGeoObject.isRemoved()) theGeoObject=tm.getTopic(theGeoObjectSI);
                            nameGeoObjectTopic(theGeoObject, data_alternatename_all);
                        }
                        if(isValid(data_population)) {
                            if(theGeoObject.isRemoved()) theGeoObject=tm.getTopic(theGeoObjectSI);
                            Topic populationTypeTopic = getPopulationTypeTopic(tm);
                            theGeoObject.setData(populationTypeTopic, TMBox.getLangTopic(theGeoObject, lang), data_population);
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
