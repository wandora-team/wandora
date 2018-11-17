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
 */


package org.wandora.application.tools.occurrences.refine;

import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.extractors.alchemy.AlchemyEntityExtractor;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author akivela
 */
public class AlchemyEntityOccurrenceExtractor extends AbstractOccurrenceExtractor {


	private static final long serialVersionUID = 1L;




	public AlchemyEntityOccurrenceExtractor() {
    }
    public AlchemyEntityOccurrenceExtractor(Context preferredContext) {
        super(preferredContext);
    }




    @Override
    public String getName() {
        return "Alchemy entity occurrence extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts entities out of given occurrences using AlchemyAPI. Read more at http://www.alchemyapi.com/.";
    }




    @Override
    public boolean _extractTopicsFrom(String occurrenceData, Topic masterTopic, TopicMap topicMap, Wandora wandora) throws Exception {
        AlchemyEntityExtractor extractor = new AlchemyEntityExtractor();
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

                String alchemyURL = AlchemyEntityExtractor.ALCHEMY_URL+"calls/text/TextGetNamedEntities"; // ?apikey="+apikey+"&linkedData=1&disambiguate=1&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String alchemyData = "apikey="+URLEncoder.encode(apikey, "utf-8")+"&linkedData=1&disambiguate=1&outputMode=xml&text="+URLEncoder.encode(content, "utf-8");
                String result = AlchemyEntityExtractor.sendRequest(new URL(alchemyURL), alchemyData, "application/x-www-form-urlencoded", "POST");

                // System.out.println("----------------------------------------");
                // System.out.println("Occurrence == "+content);
                // System.out.println("Alchemy returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                AlchemyEntityExtractor.AlchemyEntityParser parserHandler = extractor.new AlchemyEntityParser(masterTerm, content, topicMap, extractor);
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
