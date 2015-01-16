/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2015 Wandora Team
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



package org.wandora.application.tools.extractors.tagthe;




import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;
import org.wandora.application.gui.*;
import javax.swing.*;
import java.io.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;



/**
 *
 * @author akivela
 */
public abstract class AbstractTagtheExtractor extends AbstractExtractor {

    
    protected String defaultEncoding = "UTF-8";

    // Default language of occurrences and variant names.
    public static String LANG = "en";

    public static final String WEB_SERVICE_URL = "http://tagthe.net/api/";


    public static final String SOURCE_SI = "http://wandora.org/si/source";
    public static final String DOCUMENT_SI = "http://wandora.org/si/document";
    public static final String TOPIC_SI = "http://wandora.org/si/topic";

    public static final String TAGTHE_SI = "http://tagthe.net";
    public static final String TAGTHE_TERM_SI = "http://tagthe.net/term";
    public static final String TAGTHE_TERM_TYPE_SI = "http://tagthe.net/type";




    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_tagthe.png");
    }
    
    @Override
    public WandoraToolType getType() {
        return WandoraToolType.createExtractType();
    }

    private final String[] contentTypes=new String[] { "text/plain", "text/html" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }



    // -------------------------------------------------------------------------


    
    @Override
    public boolean isConfigurable(){
        return false;
    }
    

    // -------------------------------------------------------------------------

    

    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        setWandora(wandora);
        return ExtractHelper.doBrowserExtractForClassifiers(this, request, wandora, defaultEncoding);
    }

    // -------------------------------------------------------------------------


    
    
    public abstract boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception;






    public String solveTitle(String content) {
        if(content == null || content.length() == 0) return "empty-document";

        boolean forceTrim = false;
        String title = null;
        int i = content.indexOf("\n");
        if(i > 0) title = content.substring(0, i);
        else {
            title = content.substring(0, Math.min(80, content.length()));
            forceTrim = true;
        }

        if(title != null && (forceTrim || title.length() > 80)) {
            title = title.substring(0, Math.min(80, title.length()));
            while(!title.endsWith(" ") && title.length()>10) {
                title = title.substring(0, title.length()-1);
            }
            title = Textbox.trimExtraSpaces(title) + "...";
        }
        return title;
    }




    public void fillDocumentTopic(Topic textTopic, TopicMap topicMap, String content) {
        try {
            String trimmedText = Textbox.trimExtraSpaces(content);
            if(trimmedText != null && trimmedText.length() > 0) {
                Topic contentType = createTopic(topicMap, "document-text");
                setData(textTopic, contentType, "en", trimmedText);
            }
            String title = solveTitle(trimmedText);
            if(title != null) {
                textTopic.setBaseName(title + " (" + content.hashCode() + ")");
                textTopic.setDisplayName("en", title);
            }
            Topic documentType = getDocumentType(topicMap);
            textTopic.addType(documentType);
        }
        catch(Exception e) {
            log(e);
        }
    }



    // ******** TOPIC MAPS *********


    
    public Topic getTermTypeType(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, TAGTHE_TERM_TYPE_SI, "Tagthe Term");
        t.addType(getTagtheClass(tm));
        return t;
    }


    
    public Topic getTermType(String type, TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, TAGTHE_TERM_TYPE_SI+"/"+type, type);
        t.addType(getTermTypeType(tm));
        return t;
    }


    public Topic getTermTopic(String term, String type, TopicMap tm) throws TopicMapException {
        if(term != null) {
            term = term.trim();
            if(term.length() > 0) {
                Topic entityTopic=getOrCreateTopic(tm, TAGTHE_TERM_SI+"/"+term, term);
                if(type != null && type.length() > 0) {
                    Topic entityTypeTopic = getTermType(type, tm);
                    entityTopic.addType(entityTypeTopic);
                }
                return entityTopic;
            }
        }
        return null;
    }

    public Topic getTagtheClass(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, TAGTHE_SI,"Tagthe.net");
        makeSubclassOf(tm, t, getWandoraClass(tm));
        //t.addType(getWandoraClass(tm));
        return t;
    }

    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }
    
    public Topic getTopicType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TOPIC_SI, "Topic");
    }

    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SOURCE_SI, "Source");
    }

    public Topic getDocumentType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, DOCUMENT_SI, "Document");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }

    // --------

    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si,String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }



    // -------------------------------------------------------------------------







    protected String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);

            return writer.toString();
        }

        catch(TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }




    // utility function
    protected String getFileContents(File file) throws IOException, FileNotFoundException {
        StringBuilder contents = new StringBuilder();
        BufferedReader input =  new BufferedReader(new FileReader(file));

        try {
            String line = null;

            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        }
        finally {
            input.close();
        }
        return contents.toString();
    }

}
