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
 */


package org.wandora.application.tools.occurrences.refine;

import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.extractors.alchemy.*;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author akivela
 */
public class AlchemyKeywordOccurrenceExtractor extends AbstractOccurrenceExtractor {




    public AlchemyKeywordOccurrenceExtractor() {
    }
    public AlchemyKeywordOccurrenceExtractor(Context preferredContext) {
        super(preferredContext);
    }




    @Override
    public String getName() {
        return "Alchemy keyword occurrence extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts keyword out of given occurrences using AlchemyAPI. Read more at http://www.alchemyapi.com/.";
    }




    @Override
    public boolean _extractTopicsFrom(String occurrenceData, Topic masterTopic, TopicMap topicMap, Wandora wandora) throws Exception {
        AlchemyKeywordExtractor extractor = new AlchemyKeywordExtractor();
        extractor.setToolLogger(this.getDefaultLogger());
        extractor.setMasterSubject(masterTopic);
        String apikey = extractor.solveAPIKey(wandora);
        String masterTerm = null;
        if(masterTopic != null) masterTerm = masterTopic.getOneSubjectIdentifier().toExternalForm();
        if(occurrenceData != null && occurrenceData.length() > 0) {
            if(apikey != null) apikey = apikey.trim();
            if(apikey != null && apikey.length() > 0) {
                String content = null;
                content = occurrenceData;

                String alchemyURL = AlchemyKeywordExtractor.ALCHEMY_URL+"calls/text/TextGetKeywords"; // ?apikey="+apikey+"&maxRetrieve=100&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String alchemyData = "apikey="+URLEncoder.encode(apikey, "utf-8")+"&maxRetrieve=100&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String result = AlchemyKeywordExtractor.sendRequest(new URL(alchemyURL), alchemyData, "application/x-www-form-urlencoded", "POST");

                // System.out.println("----------------------------------------");
                // System.out.println("Occurrence == "+content);
                // System.out.println("Alchemy returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                AlchemyKeywordExtractor.AlchemyKeywordParser parserHandler = extractor.new AlchemyKeywordParser(masterTerm, content, topicMap, extractor);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try {
                    reader.parse(new InputSource(new StringReader(result)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
            }
            else {
                log("No valid API key given! Aborting!");
            }
        }
        else {
            log("No valid data given! Aborting!");
        }
        return true;
    }




}
