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


import java.io.ByteArrayInputStream;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.extractors.opencalais.OpenCalaisClassifier;
import org.wandora.application.tools.extractors.opencalais.webservice.CalaisClient;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author akivela
 */
public class OpenCalaisOccurrenceExtractor extends AbstractOccurrenceExtractor {

	private static final long serialVersionUID = 1L;
	
	private String params = "<c:params xmlns:c=\"http://s.opencalais.com/1/pred/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">"+
            "<c:processingDirectives c:contentType=\"__CONTENTTYPE__\" c:outputFormat=\"Text/Simple\">"+
            "</c:processingDirectives>"+
            "<c:userDirectives c:allowDistribution=\"false\" c:allowSearch=\"false\" c:externalID=\"17cabs901\" c:submitter=\"Wandora\">"+
            "</c:userDirectives>"+
            "<c:externalMetadata>"+
            "</c:externalMetadata>"+
            "</c:params>";


    

    public OpenCalaisOccurrenceExtractor() {
    }
    public OpenCalaisOccurrenceExtractor(Context preferredContext) {
        super(preferredContext);
    }




    @Override
    public String getName() {
        return "OpenCalais occurrence extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts entities out of given occurrences using OpenCalais web service. Read more at http://www.opencalais.com/.";
    }







    public boolean _extractTopicsFrom(String occurrenceData, Topic masterTopic, TopicMap topicMap, Wandora wandora) throws Exception {
        try {
            String masterTerm = null;
            if(masterTopic != null) masterTerm = masterTopic.getOneSubjectIdentifier().toExternalForm();
            String content = occurrenceData;
            String contentType = "text/raw";

            String licenseID = "9yuug2dbqh5tphk5pnn79ydb";
            String paramsXML = params.replace("__CONTENTTYPE__", contentType);
            CalaisClient client = new CalaisClient("http://api.opencalais.com/enlighten/");
            String result = client.enlighten(licenseID, content, paramsXML);

            // System.out.println("Result = "+result);
            if(result.startsWith("<Error")) {
                log("OpenCalais could not process given document!");
                int startIndex = result.indexOf("<Exception>");
                int endIndex = result.indexOf("</Exception>");
                if(startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    String errorMessage = result.substring(startIndex+11, endIndex);
                    log(errorMessage);
                }
                hlog("No documents processed!");
            }
            else {
                // ---- Parse results ----
                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();

                OpenCalaisClassifier extractor = new OpenCalaisClassifier();
                extractor.setToolLogger(this.getDefaultLogger());

                OpenCalaisClassifier.OpenCalaisSimpleResultParser parserHandler = extractor.new OpenCalaisSimpleResultParser(masterTerm, content, topicMap, extractor);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try{
                    reader.parse(new InputSource(new ByteArrayInputStream(result.getBytes())));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                hlog("Total " + parserHandler.progress + " OpenCalais tags parsed!");
            }
        }
        catch (Exception ex) {
           log(ex);
        }
        return true;
    }


}
