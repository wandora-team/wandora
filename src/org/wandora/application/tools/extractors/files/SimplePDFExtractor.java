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
 * SimplePDFExtractor.java
 *
 * Created on 9. kesäkuuta 2006, 15:08
 *
 */

package org.wandora.application.tools.extractors.files;


import org.wandora.utils.Textbox;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import org.apache.pdfbox.util.*;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.utils.*;



/**
 *
 * @author akivela
 */
public class SimplePDFExtractor extends AbstractExtractor {

    public boolean makePageTopics = false;
    public boolean makeVariantFromTitle = true;
    private String defaultLang = "en";
    
    
    public SimplePDFExtractor() {
    }
    

    @Override
    public String getName() {
        return "Simple PDF extractor...";
    }
    @Override
    public String getDescription(){
        return "Extracts text and metadata from PDF files.";
    }
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR;
    }


    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select PDF file(s) or directories containing PDF files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking PDF files!";
        
            case FILE_PATTERN: return ".*\\.(pdf|PDF)";
            
            case DONE_FAILED: return "Done! No extractions! %1 pdf file(s) crawled!";
            case DONE_ONE: return "Done! Successful extraction! %1 pdf file(s) crawled!";
            case DONE_MANY: return "Done! Total %0 successful extractions! %1 pdf files crawled!";
            
            case LOG_TITLE: return "Simple PDF Extraction Log";
        }
        return "";
    }
    
    
    
    
    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        try {
            setWandora(wandora);
            String url = request.getSource();
            TopicMap tm = wandora.getTopicMap();
            if(url != null && url.endsWith(".pdf")) {
                _extractTopicsFrom(new URL(url), tm);
                wandora.doRefresh();
                return null;
            }
            else {
                String content = request.getSelection();
                if(content == null) {
                    content = request.getContent();
                }
                if(content == null && url != null) {
                    try {
                        System.out.println("Found no content. Reading the url content.");
                        content = IObox.doUrl(new URL(url));
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                if(content != null) {
                    System.out.println("--- browser plugin processing content ---");
                    System.out.println(content);

                    Pattern p = Pattern.compile("\"[^\"]+?\\.pdf\"");
                    Matcher m = p.matcher(content);
                    ArrayList<String> pdfUrls = new ArrayList<String>();
                    int l = 0;
                    while( l<content.length() && m.find(l) ) {
                        String g = m.group();
                        if(g.startsWith("\"")) g = g.substring(1);
                        if(g.endsWith("\"")) g = g.substring(0, g.length()-1);
                        pdfUrls.add( g );
                        l = m.end();
                    }

                    for( String u : pdfUrls ) {
                        System.out.println("Extracting pdf url: " + u);
                        _extractTopicsFrom(new URL(u), tm);
                    }
                    wandora.doRefresh();
                    return null;
                }
                else {
                    return BrowserPluginExtractor.RETURN_ERROR+"Couldn't solve browser extractor content. Nothing extracted.";
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }

    }

    // -------------------------------------------------------------------------
    
    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        
        try {
            Topic pdfType = createPDFTypeTopic(topicMap);
            String location = url.toExternalForm();
            long hash = location.hashCode();
            String urlfile = url.getFile();
            String name = urlfile;
            if(urlfile.lastIndexOf("/") > -1) {
                name = urlfile.substring(urlfile.lastIndexOf("/")+1);
            }
            Topic pdfTopic = createTopic(topicMap, location, " ("+hash+")", name, pdfType);
            pdfTopic.setSubjectLocator(new Locator( location ));
            
            URLConnection uc = null;
            if(getWandora() != null) {
                uc = getWandora().wandoraHttpAuthorizer.getAuthorizedAccess(url);
            }
            else {
                uc = url.openConnection();
                Wandora.initUrlConnection(uc);
            }
            _extractTopicsFromStream(url.toExternalForm(), uc.getInputStream(), topicMap, pdfTopic);

            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat();
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(pdfTopic, extractionTimeType, defaultLang, dateString);

            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from url\n" + url.toExternalForm(), e);
            takeNap(1000);
        }
        return false;
    }

    
    

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        throw(new Exception(STRING_EXTRACTOR_NOT_SUPPORTED_MESSAGE));
    }



    
    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file == null || file.isDirectory()) return false;
        
        try {

            Topic pdfType = createPDFTypeTopic(topicMap);
            String location = file.toURI().toURL().toExternalForm();
            long hash = location.hashCode();
            Topic pdfTopic = createTopic(topicMap, location, " ("+hash+")", file.getName(), pdfType);
            pdfTopic.setSubjectLocator(new Locator( location ));
            
            // --- ADD LAST MODIFICATION TIME AS OCCURRENCE ---
            try {
                DateFormat dateFormatter = new SimpleDateFormat();
                Topic modType = createTopic(topicMap, "file-modified");
                String dateString = dateFormatter.format( new Date(file.lastModified()) );
                setData(pdfTopic, modType, defaultLang, dateString);
            }
            catch(Exception e) {
                log("Exception occurred while setting file topic's modification time!", e);
            }

            // --- ADD FILE SIZE AS OCCURRENCE ---
            try {
                Topic sizeType = createTopic(topicMap, "file-size");
                setData(pdfTopic, sizeType, defaultLang, ""+file.length());
            }
            catch(Exception e) {
                log("Exception occurred while setting file topic's file size!", e);
            }
        
            FileInputStream fis = new FileInputStream(file);
            try {
                _extractTopicsFromStream(file.getPath(), fis, topicMap, pdfTopic);
            }
            finally {
                if(fis != null) fis.close();
            }

            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat();
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(pdfTopic, extractionTimeType, defaultLang, dateString);

            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from file " + file.getName(), e);
            takeNap(1000);
        }
        return false;
    }

    
    
    
    public void _extractTopicsFromStream(String locator, InputStream inputStream, TopicMap topicMap, Topic pdfTopic) {
        PDDocument doc = null;
        try {
            if(locator.startsWith("http://")) {
                doc = PDDocument.load(new URL(locator));
            }
            else {
                doc = PDDocument.load(new File(locator));
            }
            PDDocumentInformation info = doc.getDocumentInformation();
            DateFormat dateFormatter = new SimpleDateFormat();

            // --- PDF PRODUCER ---
            String producer = info.getProducer();
            if(producer != null && producer.length() > 0) {
                Topic producerType = createTopic(topicMap, "pdf-producer");
                setData(pdfTopic, producerType, defaultLang, producer.trim());
            }

            // --- PDF MODIFICATION DATE ---
            Calendar mCal = info.getModificationDate();
            if(mCal != null) {
                String mdate = dateFormatter.format(mCal.getTime());
                if(mdate != null && mdate.length() > 0) {
                    Topic modificationDateType = createTopic(topicMap, "pdf-modification-date");
                    setData(pdfTopic, modificationDateType, defaultLang, mdate.trim());
                }
            }

            // --- PDF CREATOR ---
            String creator = info.getCreator();
            if(creator != null && creator.length() > 0) {
                Topic creatorType = createTopic(topicMap, "pdf-creator");
                setData(pdfTopic, creatorType, defaultLang, creator.trim());
            }

            // --- PDF CREATION DATE ---
            Calendar cCal = info.getCreationDate();
            if(cCal != null) {
                String cdate = dateFormatter.format(cCal.getTime());
                if(cdate != null && cdate.length() > 0) {
                    Topic creationDateType = createTopic(topicMap, "pdf-creation-date");
                    setData(pdfTopic, creationDateType, defaultLang, cdate.trim());
                }
            }

            // --- PDF AUTHOR ---
            String author = info.getAuthor();
            if(author != null && author.length() > 0) {
                Topic authorType = createTopic(topicMap, "pdf-author");
                setData(pdfTopic, authorType, defaultLang, author.trim());
            }

            // --- PDF SUBJECT ---
            String subject = info.getSubject();
            if(subject != null && subject.length() > 0) {
                Topic subjectType = createTopic(topicMap, "pdf-subject");
                setData(pdfTopic, subjectType, defaultLang, subject.trim());
            }
            
            // --- PDF TITLE ---
            String title = info.getSubject();
            if(title != null && title.length() > 0) {
                if(makeVariantFromTitle) {
                    pdfTopic.setDisplayName(defaultLang, title);
                }
                else {
                    Topic titleType = createTopic(topicMap, "pdf-title");
                    setData(pdfTopic, titleType, defaultLang, title.trim());
                }
            }
            
            // --- PDF KEYWORDS (SEPARATED WITH SEMICOLON) ---
            String keywords = info.getKeywords();
            if(keywords != null && keywords.length() > 0) {
                Topic keywordType = createTopic(topicMap, "pdf-keyword");
                String[] keywordArray = keywords.split(";");
                String keyword = null;
                for(int i=0; i<keywordArray.length; i++) {
                    keyword = Textbox.trimExtraSpaces(keywordArray[i]);
                    if(keyword != null && keyword.length() > 0) {
                        Topic keywordTopic = createTopic(topicMap, keyword, keywordType);
                        createAssociation(topicMap, keywordType, new Topic[] { pdfTopic, keywordTopic } );
                    }
                }
            }
            
            // --- PDF TEXT CONTENT ---
            PDFTextStripper stripper = new PDFTextStripper();            
            String content = new String();

            if(makePageTopics) {
                int pages=doc.getNumberOfPages();
                String pageContent = null;
                for(int i=0;i<pages;i++) {
                    stripper.setStartPage(i);
                    stripper.setEndPage(i);
                    pageContent = stripper.getText(doc);
                    Topic pageType = createTopic(topicMap, "pdf-page");
                    Topic pageTopic = createTopic(topicMap, pdfTopic.getBaseName() + " (page "+i+")", pageType);
                    Topic orderType = createTopic(topicMap, "order");
                    Topic orderTopic = createTopic(topicMap, i + ".", orderType);
                    Topic contentType = createTopic(topicMap, "pdf-text");
                    setData(pageTopic, contentType, defaultLang, pageContent.trim());
                    createAssociation(topicMap, pageType, new Topic[] { pdfTopic, pageTopic, orderTopic } );
                }
            }
            else {
                content = stripper.getText(doc);
            }
            
            if(!makePageTopics && content != null && content.length() > 0) {
                Topic contentType = createTopic(topicMap, "pdf-text");
                setData(pdfTopic, contentType, defaultLang, content.trim());
            }
            doc.close();
        }
        catch(Exception e) {
            e.printStackTrace();
            try {
                if(doc != null) doc.close();
            }
            catch(Exception ix) {
                e.printStackTrace();
            }
        }
    }




    // -------------------------------------------------------------------------


    
    public static final String[] contentTypes=new String[] { "application/pdf" };
    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }

    

    @Override
    public Locator buildSI(String siend) {
        if(siend == null) siend = "" + System.currentTimeMillis() + Math.random()*999999;
        if(siend.startsWith("http://")) return new Locator(siend);
        if(siend.startsWith("file:/")) return new Locator(siend);
        if(siend.startsWith("/")) siend = siend.substring(1);
        return new Locator("http://wandora.org/si/pdf/" + urlEncode(siend));
    }



    // -------------------------------------------------------------------------



    public Topic createPDFTypeTopic(TopicMap tm) throws TopicMapException {
        Topic t = createTopic(tm, "PDF resource");
        Topic w = getWandoraClass(tm);
        makeSubclassOf(tm, t, w);
        return t;
    }


    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }
}
