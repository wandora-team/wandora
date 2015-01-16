
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



package org.wandora.application.tools.extractors;

import java.net.URL;
import org.wandora.application.Wandora;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.IObox;
import org.wandora.utils.MSOfficeBox;
import org.wandora.utils.PDFbox;
import org.wandora.utils.Textbox;
import org.wandora.utils.XMLbox;

/**
 *
 * @author akivela
 */
public class ExtractHelper {
    public static final String TOPIC_SI = "http://wandora.org/si/topic";
    public static final String SOURCE_SI = "http://wandora.org/si/source";
    public static final String DOCUMENT_SI = "http://wandora.org/si/document";




    public static Topic getOrCreateTopic(String si, TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(si, null, tm);
    }


    
    public static Topic getOrCreateTopic(String si, String bn, TopicMap tm) throws TopicMapException {
        if(si != null) {
            Locator l = tm.createLocator(si);
            Topic t=tm.getTopic(l);
            if(t==null) {
                if(bn != null) {
                    t = tm.getTopicWithBaseName(bn);
                }
                if(t == null) {
                    t=tm.createTopic();
                    t.addSubjectIdentifier(l);
                    if(bn!=null) t.setBaseName(bn);
                }
                else {
                    t.addSubjectIdentifier(l);
                }
            }
            return t;
        }
        else{
            Topic t=tm.getTopicWithBaseName(bn);
            if(t==null) {
                t=tm.createTopic();
                t.setBaseName(bn);
                t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
            }
            return t;
        }
    }



    public static Topic getOrCreateTopic(String si, String bn, Topic type, TopicMap tm) throws TopicMapException {
        if(si != null) {
            Locator l = tm.createLocator(si);
            Topic t=tm.getTopic(l);
            if(t==null) {
                if(bn != null) {
                    t = tm.getTopicWithBaseName(bn);
                }
                if(t == null) {
                    t=tm.createTopic();
                    t.addSubjectIdentifier(l);
                    if(bn!=null) t.setBaseName(bn);
                }
                else {
                    t.addSubjectIdentifier(l);
                }
            }
            if(t != null && type != null) {
                t.addType(type);
            }
            return t;
        }
        else{
            Topic t=tm.getTopicWithBaseName(bn);
            if(t==null){
                t=tm.createTopic();
                if(bn!=null) t.setBaseName(bn);
                t.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
            }
            if(t != null && type != null) {
                t.addType(type);
            }
            return t;
        }
    }






    public static Topic getOrCreateTopic(Locator si, String bn, String displayName, Topic typeTopic, TopicMap tm) throws TopicMapException {
        Topic topic = null;
        if(si != null) {
            topic = tm.getTopic(si);
            if(topic == null) {
                if(bn != null) {
                    bn = bn.trim();
                    if(bn.length() > 0) {
                        topic = tm.getTopicWithBaseName(bn);
                    }
                }
                if(topic == null) {
                    topic = tm.createTopic();
                }
                topic.addSubjectIdentifier(si);
                if(bn != null) {
                    if(!bn.equals(topic.getBaseName()))
                        topic.setBaseName(bn);
                }
                if(displayName != null) {
                    displayName = displayName.trim();
                    if(displayName.length() > 0) {
                        topic.setDisplayName("en", displayName);
                    }
                }
                if(typeTopic != null) {
                    if(!topic.isOfType(typeTopic))
                        topic.addType(typeTopic);
                }
            }
            else {
                if(bn != null && topic.getBaseName() == null) {
                    topic.setBaseName(bn);
                }
                if(displayName != null && topic.getDisplayName("en") == null) {
                    displayName = displayName.trim();
                    if(displayName.length() > 0) {
                        topic.setDisplayName("en", displayName);
                    }
                }
                if(typeTopic != null) {
                    if(!topic.isOfType(typeTopic))
                        topic.addType(typeTopic);
                }
            }
        }
        return topic;
    }







    // -------------------------------------------------------------------------




    public static void makeSubclassOf(Topic subclass, Topic superclass, TopicMap tm) throws TopicMapException {
        Topic supersubClassTopic = getOrCreateTopic(XTMPSI.SUPERCLASS_SUBCLASS, "superclass-subclass", tm);
        Topic subclassTopic = getOrCreateTopic(XTMPSI.SUBCLASS, "subclass", tm);
        Topic superclassTopic = getOrCreateTopic(XTMPSI.SUPERCLASS, "superclass", tm);
        Association ta = tm.createAssociation(supersubClassTopic);
        ta.addPlayer(subclass, subclassTopic);
        ta.addPlayer(superclass, superclassTopic);
    }




    // -------------------------------------------------------------------------



    public static String getContent(URL url) {
        return getContent(url.toExternalForm());
    }

    public static String getContent(String url) {
        String content = null;
        try {
            if(url.endsWith(".pdf") || url.endsWith(".PDF")) {
                System.out.println("Found no content. Reading the url content as a PDF.");
                content = PDFbox.extractTextOutOfPDF(url);
            }
            else if(url.endsWith(".doc") || url.endsWith(".DOC") || url.endsWith(".ppt") || url.endsWith(".PPT")) {
                System.out.println("Found no content. Reading the url content as a MS Office file.");
                content = MSOfficeBox.getText(new URL(url));
            }
            else {
                System.out.println("Found no content. Reading the url content.");
                content = IObox.doUrl(new URL(url));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return content;
    }



    public static String getContent(BrowserExtractRequest request) {
        String url = request.getSource();
        String content = request.getSelection();
        
        if(content == null && url != null) {
            try {
                // Reading url content from url instead of request.getContent().
                // Sometimes browser modifies the content and wrong content is transferred
                // to Wandora.
                String lurl = url.toLowerCase();
                if(lurl.endsWith(".pdf")) {
                    System.out.println("Found no content. Reading the url content as a PDF.");
                    content = PDFbox.extractTextOutOfPDF(url);
                }
                else if(lurl.endsWith(".doc") || lurl.endsWith(".ppt") || lurl.endsWith(".xsl")) {
                    System.out.println("Found no content. Reading the url content as a MS Office file.");
                    content = MSOfficeBox.getText(new URL(url));
                }
                else if(lurl.endsWith(".xml")) {
                    System.out.println("Found no content. Reading the url content as an XML file.");
                    content = getContent(url);
                }
                else if(lurl.endsWith(".rdf") || lurl.endsWith(".owl")) {
                    System.out.println("Found no content. Reading the url content as an RDF file.");
                    content = getContent(url);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        if(content == null) {
            content = request.getContent();
        }
        if(content == null && url != null) {
            try {
                System.out.println("Found no content. Reading the url content.");
                content = getContent(url);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return content;
    }



    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------



    public static String doBrowserExtractForClassifiers(AbstractExtractor callback, BrowserExtractRequest request, Wandora wandora, String defaultEncoding) throws TopicMapException {
        try {
            String url=request.getSource();
            TopicMap tm=wandora.getTopicMap();
            Topic theTopic=null;
            String content = request.getSelection();

            // SOURCE IS A FRACTION OF URL
            if(content!=null) {
                System.out.println("Found selection.");
                String tidyContent = XMLbox.cleanUp( content );
                if(tidyContent != null && tidyContent.length() > 0) {
                    content = XMLbox.getAsText(tidyContent, defaultEncoding);
                }

                Topic sourceTopic=tm.getTopicBySubjectLocator(url);
                if(sourceTopic==null) {
                    sourceTopic=tm.createTopic();
                    org.wandora.topicmap.Locator l = tm.createLocator(url);
                    sourceTopic.addSubjectIdentifier(l);
                    sourceTopic.setSubjectLocator(l);
                }
                theTopic = tm.createTopic();
                theTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                fillDocumentTopic(callback, theTopic, tm, content);

                Association a = tm.createAssociation(getSourceType(tm));
                a.addPlayer(theTopic, getDocumentType(tm));
                a.addPlayer(sourceTopic, getSourceType(tm));
            }
            // SOURCE IS A COMPLETE URL
            else {
                if(content == null && url != null) {
                    content = ExtractHelper.getContent(url);
                }
                if(content == null) {
                    content = request.getContent();
                }

                theTopic=tm.getTopicBySubjectLocator(url);
                if(theTopic==null) {
                    theTopic=tm.createTopic();
                    org.wandora.topicmap.Locator l = tm.createLocator(url);
                    theTopic.addSubjectIdentifier(l);
                    theTopic.setSubjectLocator(l);
                    fillDocumentTopic(callback, theTopic, tm, content);
                }
            }

            callback.setMasterSubject( theTopic.getOneSubjectIdentifier().toExternalForm() );
            callback._extractTopicsFrom(content, tm);
            wandora.doRefresh();
            callback.clearMasterSubject();
            return null;
        }
        catch(Exception e) {
            callback.clearMasterSubject();
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }
    }




    public static String solveTitle(String content) {
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




    public static void fillDocumentTopic(AbstractExtractor callback, Topic textTopic, TopicMap topicMap, String content) {
        try {
            String trimmedText = Textbox.trimExtraSpaces(content);
            if(trimmedText != null && trimmedText.length() > 0) {
                Topic contentType = callback.createTopic(topicMap, "document-text");
                callback.setData(textTopic, contentType, "en", trimmedText);
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
            callback.log(e);
        }
    }

    public static Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(SOURCE_SI, "Source", tm);
    }

    public static Topic getDocumentType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(DOCUMENT_SI, "Document", tm);
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(type, wandoraClass, tm);
        return type;
    }

    public static Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(TMBox.WANDORACLASS_SI, "Wandora class", tm);
    }

    
    
    // -------------------------------------------------------------------------
    
    
    
    public static String getTextData(String data) {
        String tdata = data;
        try {
            tdata = XMLbox.cleanUp(data);
            if(tdata == null || tdata.length() < 1) {
                // Tidy failed to fix the file...
                tdata = data;
                //contentType = "text/html";
            }
            else {
                // Ok, Tidy fixed the html/xml document
                tdata = XMLbox.getAsText(tdata, "UTF-8");
                //System.out.println("content after getAsText: "+content);
                //contentType = "text/txt";
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            tdata = data;
            //contentType = "text/raw";
        }
        return tdata;
    }
    
    
    
}
