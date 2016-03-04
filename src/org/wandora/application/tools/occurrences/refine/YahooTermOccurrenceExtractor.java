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
import org.wandora.application.tools.extractors.yahoo.termextraction.YahooTermExtractor;
import org.wandora.application.tools.extractors.yahoo.yql.SearchTermExtract;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.utils.IObox;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author akivela
 */
public class YahooTermOccurrenceExtractor extends AbstractOccurrenceExtractor {


    public YahooTermOccurrenceExtractor() {
    }
    public YahooTermOccurrenceExtractor(Context preferredContext) {
        super(preferredContext);
    }




    @Override
    public String getName() {
        return "Yahoo! YQL term occurrence extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts terms out of given occurrences using Yahoo! YQL. Read more at http://developer.yahoo.com/yql.";
    }







    public boolean _extractTopicsFrom(String occurrenceData, Topic masterTopic, TopicMap topicMap, Wandora wandora) throws Exception {
        if(occurrenceData != null && occurrenceData.length() > 0) {
            SearchTermExtract extractor = new SearchTermExtract();
            extractor.setToolLogger(getDefaultLogger());

            String appid = extractor.solveAppId(wandora);
            if(appid != null) appid = appid.trim();
            if(appid.length() > 0) {
                String masterTerm = null;
                if(masterTopic != null) masterTerm = masterTopic.getOneSubjectIdentifier().toExternalForm();

                String content = occurrenceData;

                String result = IObox.doUrl(new URL(extractor.makeYQLURLRequest(extractor.makeYQLQuery(content), appid)));

                System.out.println("Yahoo returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                SearchTermExtract.SearchTermExtractParser parserHandler = extractor.new SearchTermExtractParser(masterTerm, content, topicMap, extractor);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try {
                    reader.parse(new InputSource(new StringReader(result)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                hlog("Total " + parserHandler.progress + " terms extracted by Yahoo!");
            }
            else {
                log("No valid application identifier given! Aborting!");
            }
        }
        else {
            log("No valid data given! Aborting!");
        }
        return true;
    }


}
