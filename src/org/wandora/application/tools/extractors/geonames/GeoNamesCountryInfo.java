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
 * 
 * GeoNamesCountryInfo.java
 * 
 * 
 */



package org.wandora.application.tools.extractors.geonames;


import java.io.*;
import org.xml.sax.*;
import org.wandora.topicmap.*;



/**
 * See http://ws.geonames.org/countryInfo?lang=en&country=FI&style=full
 *
 * @author akivela
 */
public class GeoNamesCountryInfo extends AbstractGeoNamesExtractor {

	private static final long serialVersionUID = 1L;
	
	public String dataLang = "en";
    
    
    /** Creates a new instance of GeoNamesCountryInfo */
    public GeoNamesCountryInfo() {
    }
    @Override
    public String getName() {
        return "GeoNames country info extractor";
    }
    @Override
    public String getDescription(){
        return "Get country data from geo names web api and convert the data to a topic map. ";
    }

    
    
    
    
    
    
    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {        
        javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        javax.xml.parsers.SAXParser parser=factory.newSAXParser();
        XMLReader reader=parser.getXMLReader();
        CountryInfoParser parserHandler = new CountryInfoParser(topicMap,this,dataLang);
        reader.setContentHandler(parserHandler);
        reader.setErrorHandler(parserHandler);
        try{
            reader.parse(new InputSource(in));
        }
        catch(Exception e){
            if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
        }
        if(parserHandler.progress == 0) log("No countries found!");
        else if(parserHandler.progress == 1) log("One country found!");
        else log("Total " + parserHandler.progress + " countries found!");
        return true;
    }
    



    // -------------------------------------------------------------------------
    // --- GEONAMES XML FEED PARSER --------------------------------------------
    // -------------------------------------------------------------------------


    private static class CountryInfoParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        
        
        public CountryInfoParser(TopicMap tm, GeoNamesCountryInfo parent, String lang){
            this.lang=lang;
            this.tm=tm;
            this.parent=parent;
        }
        
        private String lang = "en";
        public int progress=0;
        private TopicMap tm;
        private GeoNamesCountryInfo parent;
        
        public static final String TAG_GEONAMES="geonames";
        public static final String TAG_STATUS="status";
        public static final String TAG_COUNTRY="country";
        public static final String TAG_COUNTRYCODE="countryCode";
        public static final String TAG_COUNTRYNAME="countryName";
        public static final String TAG_ISONUMERIC="isoNumeric";
        public static final String TAG_ISOALPHA3="isoAlpha3";
        public static final String TAG_FIPSCODE="fipsCode";
        public static final String TAG_CONTINENT="continent";
        public static final String TAG_CAPITAL="capital";
        public static final String TAG_AREAINSQKM="areaInSqKm";
        public static final String TAG_POPULATION="population";
        public static final String TAG_CURRENCYCODE="currencyCode";
        public static final String TAG_LANGUAGES="languages";
        public static final String TAG_GEONAMEID="geonameId";
        public static final String TAG_BBOXWEST="bBoxWest";
        public static final String TAG_BBOXNORTH="bBoxNorth";
        public static final String TAG_BBOXEAST="bBoxEast";
        public static final String TAG_BBOXSOUTH="bBoxSouth";
        
        private static final int STATE_START=0;
        private static final int STATE_GEONAMES=2;
        private static final int STATE_GEONAMES_STATUS=200;
        private static final int STATE_COUNTRY=3;
        private static final int STATE_COUNTRY_COUNTRYCODE=4;
        private static final int STATE_COUNTRY_COUNTRYNAME=5;
        private static final int STATE_COUNTRY_ISONUMERIC=6;
        private static final int STATE_COUNTRY_ISOALPHA3=7;
        private static final int STATE_COUNTRY_FIPSCODE=8;
        private static final int STATE_COUNTRY_CONTINENT=9;
        private static final int STATE_COUNTRY_CAPITAL=10;
        private static final int STATE_COUNTRY_AREAINSQKM=11;
        private static final int STATE_COUNTRY_POPULATION=12;
        private static final int STATE_COUNTRY_CURRENCYCODE=13;
        private static final int STATE_COUNTRY_LANGUAGES=14;
        private static final int STATE_COUNTRY_GEONAMEID=15;
        private static final int STATE_COUNTRY_BBOXWEST=16;
        private static final int STATE_COUNTRY_BBOXNORTH=17;
        private static final int STATE_COUNTRY_BBOXEAST=18;
        private static final int STATE_COUNTRY_BBOXSOUTH=19;

        private int state=STATE_START;

        
        private String data_countrycode;
        private String data_countryname;
        private String data_isonumeric;
        private String data_isoalpha3;
        private String data_fipscode;
        private String data_continent;
        private String data_capital;
        private String data_areainsqkm;
        private String data_population;
        private String data_currencycode;
        private String data_languages;
        private String data_geonameid;
        private String data_bboxwest;
        private String data_bboxnorth;
        private String data_bboxeast;
        private String data_bboxsouth;

        private Topic theCountry;
        
        
        
        public void startDocument() throws SAXException {
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START:
                    if(qName.equals(TAG_GEONAMES)) {
                        state = STATE_GEONAMES;
                        break;
                    }
                case STATE_GEONAMES: {
                    if(qName.equals(TAG_STATUS)) {
                        state = STATE_GEONAMES_STATUS;
                        String msg = atts.getValue("message");
                        String val = atts.getValue("value");
                        if(parent != null) {
                            parent.log("GeoNames web service says: "+msg+" ("+val+")");
                        }
                        else {
                            System.out.println("GeoNames web service says: "+msg+" ("+val+")");
                        }
                    }
                    else if(qName.equals(TAG_COUNTRY)) {
                        data_countrycode = "";
                        data_countryname = "";
                        data_isonumeric = "";
                        data_isoalpha3 = "";
                        data_fipscode = "";
                        data_continent = "";
                        data_capital = "";
                        data_areainsqkm = "";
                        data_population = "";
                        data_currencycode = "";
                        data_languages = "";
                        data_geonameid = "";
                        data_bboxwest = "";
                        data_bboxnorth = "";
                        data_bboxeast = "";
                        data_bboxsouth = "";
                        state = STATE_COUNTRY;
                    }
                    break;
                }
                case STATE_COUNTRY:
                    if(qName.equals(TAG_COUNTRYCODE)) {
                        state = STATE_COUNTRY_COUNTRYCODE;
                        data_countrycode = "";
                    }
                    else if(qName.equals(TAG_COUNTRYNAME)) {
                        state = STATE_COUNTRY_COUNTRYNAME;
                        data_countryname = "";
                    }
                    else if(qName.equals(TAG_ISONUMERIC)) {
                        state = STATE_COUNTRY_ISONUMERIC;
                        data_isonumeric = "";
                    }
                    else if(qName.equals(TAG_ISOALPHA3)) {
                        state = STATE_COUNTRY_ISOALPHA3;
                        data_isoalpha3= "";
                    }
                    else if(qName.equals(TAG_FIPSCODE)) {
                        state = STATE_COUNTRY_FIPSCODE;
                        data_fipscode = "";
                    }
                    else if(qName.equals(TAG_CONTINENT)) {
                        state = STATE_COUNTRY_CONTINENT;
                        data_continent = "";
                    }
                    else if(qName.equals(TAG_CAPITAL)) {
                        state = STATE_COUNTRY_CAPITAL;
                        data_capital = "";
                    }
                    else if(qName.equals(TAG_AREAINSQKM)) {
                        state = STATE_COUNTRY_AREAINSQKM;
                        data_areainsqkm = "";
                    }
                    else if(qName.equals(TAG_POPULATION)) {
                        state = STATE_COUNTRY_POPULATION;
                        data_population = "";
                    }
                    else if(qName.equals(TAG_CURRENCYCODE)) {
                        state = STATE_COUNTRY_CURRENCYCODE;
                        data_currencycode = "";
                    }
                    else if(qName.equals(TAG_LANGUAGES)) {
                        state = STATE_COUNTRY_LANGUAGES;
                        data_languages = "";
                    }
                    else if(qName.equals(TAG_GEONAMEID)) {
                        state = STATE_COUNTRY_GEONAMEID;
                        data_geonameid= "";
                    }
                    else if(qName.equals(TAG_BBOXWEST)) {
                        state = STATE_COUNTRY_BBOXWEST;
                        data_bboxwest = "";
                    }
                    else if(qName.equals(TAG_BBOXNORTH)) {
                        state = STATE_COUNTRY_BBOXNORTH;
                        data_bboxnorth = "";
                    }
                    else if(qName.equals(TAG_BBOXEAST)) {
                        state = STATE_COUNTRY_BBOXEAST;
                        data_bboxeast = "";
                    }
                    else if(qName.equals(TAG_BBOXSOUTH)) {
                        state = STATE_COUNTRY_BBOXSOUTH;
                        data_bboxsouth = "";
                    }
                    break;
            }
        }
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_COUNTRY: {
                    if(TAG_COUNTRY.equals(qName)) {
                        if(data_countrycode.length() > 0 || data_geonameid.length() > 0) {
                            try {
                                //Topic countryType=getCountryTypeTopic(tm);
                                theCountry=getCountryTopic(tm, data_countrycode, data_geonameid, data_countryname, lang);
                                String theCountrySI=theCountry.getOneSubjectIdentifier().toExternalForm();
                                parent.setProgress(progress++);

                                try {
                                    if(isValid(data_continent)) {
                                        Topic continentTopic=getContinentTopic(tm, data_continent);
                                        if(theCountry.isRemoved()) theCountry=tm.getTopic(theCountrySI);
                                        makeGeoContinent(theCountry, continentTopic, tm);
                                    }
                                    if(isValid(data_capital)) {
                                        Topic capitalTopic=getCityTopic(tm, data_capital, lang);
                                        makeCountryCapital(theCountry, capitalTopic, tm);
                                    }
                                    if(isValid(data_bboxwest) &&
                                       isValid(data_bboxnorth) &&
                                       isValid(data_bboxeast) &&
                                       isValid(data_bboxsouth)) {
                                         makeBoundingBox(theCountry, data_bboxnorth, data_bboxwest, data_bboxsouth, data_bboxeast, tm);
                                    }
                                    if(isValid(data_languages)) {
                                        String[] languages = data_languages.split(",");
                                        Topic lanType = getLanguageTypeTopic(tm);
                                        Topic geoObjectType = getGeoObjectTypeTopic(tm);
                                        if(theCountry.isRemoved()) theCountry=tm.getTopic(theCountrySI);
                                        if(languages.length > 0) {
                                            for(int i=0; i<languages.length; i++) {
                                                if(languages[i]!=null && languages[i].length()>0) {
                                                    Topic languageTopic = getLanguageTopic(tm, languages[i]);
                                                    if(languageTopic != null) {
                                                        Association la = tm.createAssociation(lanType);
                                                        la.addPlayer(languageTopic, lanType);
                                                        la.addPlayer(theCountry, geoObjectType);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if(isValid(data_currencycode)) {
                                        Topic currencyType = getCurrencyTypeTopic(tm);
                                        Topic currencyTopic = getCurrencyTopic(tm, data_currencycode, lang);
                                        Topic geoObjectType = getGeoObjectTypeTopic(tm);
                                        if(currencyTopic != null && currencyType != null && !currencyTopic.isRemoved() && !currencyType.isRemoved()) {
                                            if(theCountry.isRemoved()) theCountry=tm.getTopic(theCountrySI);
                                            Association cura = tm.createAssociation(currencyType);
                                            cura.addPlayer(theCountry, geoObjectType);
                                            cura.addPlayer(currencyTopic, currencyType);
                                        }
                                    }
                                    if(isValid(data_population)) {
                                        Topic populationTypeTopic = getPopulationTypeTopic(tm);
                                        if(theCountry.isRemoved()) theCountry=tm.getTopic(theCountrySI);
                                        theCountry.setData(populationTypeTopic, TMBox.getLangTopic(theCountry, lang), data_population);
                                    }
                                    if(isValid(data_isoalpha3)) {
                                        if(theCountry.isRemoved()) theCountry=tm.getTopic(theCountrySI);
                                        theCountry.addSubjectIdentifier(new org.wandora.topicmap.Locator(createCountryAlpha3SI(data_isoalpha3)));
                                    }
                                    if(isValid(data_isonumeric)) {
                                        if(theCountry.isRemoved()) theCountry=tm.getTopic(theCountrySI);
                                        theCountry.addSubjectIdentifier(new org.wandora.topicmap.Locator(createCountryNumericSI(data_isonumeric)));
                                    }
                                    if(isValid(data_areainsqkm)) {
                                        Topic areaTypeTopic = getAreaTypeTopic(tm);
                                        if(theCountry.isRemoved()) theCountry=tm.getTopic(theCountrySI);
                                        theCountry.setData(areaTypeTopic, TMBox.getLangTopic(theCountry, lang), data_areainsqkm);
                                    }
                                    if(isValid(data_fipscode)) {
                                        if(theCountry.isRemoved()) theCountry=tm.getTopic(theCountrySI);
                                        theCountry.addSubjectIdentifier(new org.wandora.topicmap.Locator(createFIPSSI(data_fipscode)));
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
                        state=STATE_GEONAMES;
                    }
                    break;
                }
                case STATE_GEONAMES_STATUS:
                    if(qName.equals(TAG_STATUS)) {
                        state = STATE_GEONAMES;
                    }
                    break;
                case STATE_GEONAMES:
                    state=STATE_START;
                    break;
                case STATE_COUNTRY_COUNTRYCODE: {
                    if(TAG_COUNTRYCODE.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_COUNTRYNAME: {
                    if(TAG_COUNTRYNAME.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_ISONUMERIC: {
                    if(TAG_ISONUMERIC.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_ISOALPHA3: {
                    if(TAG_ISOALPHA3.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_FIPSCODE: {
                    if(TAG_FIPSCODE.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_CONTINENT: {
                    if(TAG_CONTINENT.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_CAPITAL: {
                    if(TAG_CAPITAL.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_AREAINSQKM: {
                    if(TAG_AREAINSQKM.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_POPULATION: {
                    if(TAG_POPULATION.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_CURRENCYCODE: {
                    if(TAG_CURRENCYCODE.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_LANGUAGES: {
                    if(TAG_LANGUAGES.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_GEONAMEID: {
                    if(TAG_GEONAMEID.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_BBOXWEST: {
                    if(TAG_BBOXWEST.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_BBOXNORTH: {
                    if(TAG_BBOXNORTH.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_BBOXEAST: {
                    if(TAG_BBOXEAST.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
                case STATE_COUNTRY_BBOXSOUTH: {
                    if(TAG_BBOXSOUTH.equals(qName)) 
                        state=STATE_COUNTRY;
                    break;
                }
            }
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state){
                case STATE_COUNTRY_COUNTRYCODE:
                    data_countrycode+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_COUNTRYNAME:
                    data_countryname+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_ISONUMERIC:
                    data_isonumeric+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_ISOALPHA3:
                    data_isoalpha3+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_CONTINENT:
                    data_continent+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_CAPITAL:
                    data_capital+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_AREAINSQKM:
                    data_areainsqkm+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_POPULATION:
                    data_population+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_CURRENCYCODE:
                    data_currencycode+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_LANGUAGES:
                    data_languages+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_GEONAMEID:
                    data_geonameid+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_BBOXWEST:
                    data_bboxwest+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_BBOXNORTH:
                    data_bboxnorth+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_BBOXEAST:
                    data_bboxeast+=new String(ch,start,length);
                    break;
                case STATE_COUNTRY_BBOXSOUTH:
                    data_bboxsouth+=new String(ch,start,length);
                    break;
            }
        }
        
        public void warning(SAXParseException exception) throws SAXException {
        }

        public void error(SAXParseException exception) throws SAXException {
            parent.log("Error parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            parent.log("Fatal error parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
        }
        

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
        public void processingInstruction(String target, String data) throws SAXException {}
        public void startPrefixMapping(String prefix, String uri) throws SAXException {}
        public void endPrefixMapping(String prefix) throws SAXException {}
        public void setDocumentLocator(org.xml.sax.Locator locator) {}
        public void skippedEntity(String name) throws SAXException {}
        
        
        public boolean isValid(String str) {
            if(str != null && str.length() > 0) return true;
            else return false;
        }
        
    }
    
    
}
