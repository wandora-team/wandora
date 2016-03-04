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
 * SimpleDocumentExtractor.java
 *
 * Created on 31. lokakuuta 2007, 16:07
 *
 */

package org.wandora.application.tools.extractors.files;



import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

import org.wandora.application.tools.browserextractors.*;
import org.wandora.topicmap.TMBox;
import org.wandora.utils.IObox;
import org.wandora.utils.Textbox;
import org.wandora.utils.MSOfficeBox;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.*;
import org.wandora.utils.*;


import java.util.*;
import java.text.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import javax.swing.Icon;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;


/**
 *
 * @author akivela
 */
public class SimpleDocumentExtractor extends AbstractExtractor implements WandoraTool, BrowserPluginExtractor {
    protected static String TOPIC_SI = "http://wandora.org/si/topic";
    protected static String SOURCE_SI = "http://wandora.org/si/source";
    protected static String DOCUMENT_SI = "http://wandora.org/si/document";

    
    protected static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private String defaultLang = "en";
    
    private Wandora admin = null;

    private ArrayList<String> visitedDirectories = new ArrayList<String>();
    
    
    /** Creates a new instance of SimpleDocumentExtractor */
    public SimpleDocumentExtractor() {
    }
    
    
    @Override
    public String getName() {
        return "Simple document extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Creates a topic for given document and stores document content to an occurrence attached to the topic.";
    }

    
    
    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf016); // or 0xf15b
    }
    
    
    
    @Override
    public boolean useTempTopicMap(){
        return false;
    }
    
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    
    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select document(s) or directories containing documents!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking documents!";
        
            case FILE_PATTERN: return ".*";
            
            case DONE_FAILED: return "Ready. No extractions! %1 file(s) crawled!";
            case DONE_ONE: return "Ready. Successful extraction! %1 file(s) crawled!";
            case DONE_MANY: return "Ready. Total %0 successful extractions! %1 files crawled!";
            
            case LOG_TITLE: return "Simple Text Document Extraction Log";
        }
        return "";
    }
    
    
    // ---------------------------------------------------- PLUGIN EXTRACTOR ---


    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        String url=request.getSource();
        try {
            TopicMap tm=wandora.getTopicMap();
            Topic theTopic=null;
            String content = request.getSelection();

            // SOURCE IS A FRACTION OF URL
            if(content!=null) {
                String tidyContent = XMLbox.cleanUp( content );
                if(tidyContent != null && tidyContent.length() > 0) {
                    content = XMLbox.getAsText(tidyContent, "ISO-8859-1");
                }

                Topic sourceTopic=tm.getTopicBySubjectLocator(url);
                if(sourceTopic==null) {
                    sourceTopic=tm.createTopic();
                    Locator l = tm.createLocator(url);
                    sourceTopic.addSubjectIdentifier(l);
                    sourceTopic.setSubjectLocator(l);
                }
                theTopic = tm.createTopic();
                theTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());

                fillDocumentTopic(theTopic, tm, content);

                Association a = tm.createAssociation(getSourceType(tm));
                a.addPlayer(theTopic, getDocumentType(tm));
                a.addPlayer(sourceTopic, getSourceType(tm));
            }
            // SOURCE IS A COMPLETE URL
            else {
                content = ExtractHelper.getContent(request);
                String tidyContent = XMLbox.cleanUp(content);
                if(tidyContent != null && tidyContent.length() > 0) {
                    content = XMLbox.getAsText(tidyContent, "ISO-8859-1");
                }

                theTopic=tm.getTopicBySubjectLocator(url);
                if(theTopic==null) {
                    theTopic=tm.createTopic();
                    Locator l = tm.createLocator(url);
                    theTopic.addSubjectIdentifier(l);
                    theTopic.setSubjectLocator(l);
                }
                fillDocumentTopic(theTopic, tm, content);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }
        wandora.doRefresh();
        return null;
    }

    
    @Override
    public boolean browserExtractorConsumesPlainText() {
        return true;
    }




    // -------------------------------------------------------------------------



    
    @Override
    public void execute(Wandora wandora, Context context) {
        visitedDirectories = new ArrayList<String>();
        super.execute(wandora, context);
    }



    @Override
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        if(str == null || str.length() == 0) return false;

        try {
            int hash = str.hashCode();
            Topic documentType = this.getDocumentType(topicMap);
            String locator = "http://wandora.org/si/simple-document-extractor/"+hash;

            String name = null;
            if(str.length() > 80) {
                name = str.substring(0, 80) + " ("+hash+")";
            }
            else {
                name = str;
            }

            Topic documentTopic = topicMap.getTopic(locator);
            if(documentTopic == null) documentTopic = topicMap.createTopic();
            documentTopic.addSubjectIdentifier(new Locator( locator ));
            documentTopic.setBaseName(name);
            documentTopic.addType(documentType);

            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(documentTopic, extractionTimeType, defaultLang, dateString);

            _extractTopicsFromStream(locator, new ByteArrayInputStream(str.getBytes()), topicMap, documentTopic);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from input string.", e);
            takeNap(1000);
        }
        return false;
    }



    
    @Override
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null || url.toExternalForm().length() == 0) return false;
        
        try {
            Topic documentType = this.getDocumentType(topicMap);
            String locator = url.toExternalForm();
            int hash = locator.hashCode();
            String name = url.getFile();
            if(name != null && name.length() > 0) {
                if(name.lastIndexOf("/") > -1) {
                    name = name.substring(name.lastIndexOf("/")+1);
                }
            }
            else {
                name = locator;
            }

            Topic documentTopic = topicMap.getTopic(locator);
            if(documentTopic == null) documentTopic = topicMap.createTopic();
            documentTopic.addSubjectIdentifier(new Locator( locator ));
            documentTopic.setBaseName(name + " ("+hash+")");
            documentTopic.setSubjectLocator(new Locator( locator ));
            documentTopic.addType(documentType);

            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(documentTopic, extractionTimeType, defaultLang, dateString);

            URLConnection uc = null;
            if(admin != null) {
                uc = admin.wandoraHttpAuthorizer.getAuthorizedAccess(url);
            }
            else {
                uc = url.openConnection();
                Wandora.initUrlConnection(uc);
            }
            _extractTopicsFromStream(url.toExternalForm(), uc.getInputStream(), topicMap, documentTopic);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from url '" + url.toExternalForm()+"'.", e);
            takeNap(1000);
        }
        return false;
    }

    
    
    
    
    @Override
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file == null) return false;
        
        try {
            if(file.isDirectory()) {
                /*
                if(!visitedDirectories.contains(file.getAbsolutePath())) {
                    visitedDirectories.add(file.getAbsolutePath());
                    File[] fs = file.listFiles();
                    for(int i=0; i<fs.length && !forceStop(); i++) {
                        _extractTopicsFrom(fs[i], topicMap);
                    }
                }
                */
                return true;
            }
        }
        catch(Exception e) {
            log(e);
        }

        try {
            Topic documentType = this.getDocumentType(topicMap);
            String locator = file.toURI().toURL().toExternalForm();
            int hash = locator.hashCode();

            Topic documentTopic = topicMap.getTopic(locator);
            if(documentTopic == null) documentTopic = topicMap.createTopic();
            documentTopic.addSubjectIdentifier(new Locator( locator ));
            documentTopic.setBaseName(file.getName() + " ("+hash+")");
            documentTopic.setSubjectLocator(new Locator( locator ));
            documentTopic.addType(documentType);

            // --- ADD EXTRACTION TIME AS OCCURRENCE ---
            DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            Topic extractionTimeType = createTopic(topicMap, "extraction-time");
            String dateString = dateFormatter.format( new Date(System.currentTimeMillis()) );
            setData(documentTopic, extractionTimeType, defaultLang, dateString);
            
            // --- ADD MODIFICATION TIME AS OCCURRENCE ---
            long lastModified = file.lastModified();
            Topic modificationTimeType = createTopic(topicMap, "modification-time");
            String modificationDateString = dateFormatter.format( new Date(lastModified) );
            setData(documentTopic, modificationTimeType, defaultLang, modificationDateString);
            
            // --- ADD ABSOLUTE FILE NAME AS OCCURRENCE ---
            Topic fileType = createTopic(topicMap, "file-name");
            String fileString = file.getAbsolutePath();
            setData(documentTopic, fileType, defaultLang, fileString);

            _extractTopicsFromStream(file.getPath(), new FileInputStream(file), topicMap, documentTopic);

            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from file '" + file.getName()+"'.", e);
            takeNap(1000);
        }
        return false;
    }

    
    
    
    public void _extractTopicsFromStream(String locator, InputStream inputStream, TopicMap topicMap, Topic topic) {
        try {
            String name = locator;
            if(name.indexOf("/") != -1) {
                name = name.substring(name.lastIndexOf("/")+1);
            }
            else if(name.indexOf("\\") != -1) {
                name = name.substring(name.lastIndexOf("\\")+1);
            }
            String lowerCaseLocator = locator.toLowerCase();

            // --- HANDLE PDF ENRICHMENT TEXT ---
            if(lowerCaseLocator.endsWith("pdf")) {
                PDDocument doc = PDDocument.load(locator);
                PDDocumentInformation info = doc.getDocumentInformation();
                DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

                // --- PDF PRODUCER ---
                String producer = info.getProducer();
                if(producer != null && producer.length() > 0) {
                    Topic producerType = createTopic(topicMap, "pdf-producer");
                    setData(topic, producerType, defaultLang, producer.trim());
                }

                // --- PDF MODIFICATION DATE ---
                Calendar mCal = info.getModificationDate();
                if(mCal != null) {
                    String mdate = dateFormatter.format(mCal.getTime());
                    if(mdate != null && mdate.length() > 0) {
                        Topic modificationDateType = createTopic(topicMap, "pdf-modification-date");
                        setData(topic, modificationDateType, defaultLang, mdate.trim());
                    }
                }

                // --- PDF CREATOR ---
                String creator = info.getCreator();
                if(creator != null && creator.length() > 0) {
                    Topic creatorType = createTopic(topicMap, "pdf-creator");
                    setData(topic, creatorType, defaultLang, creator.trim());
                }

                // --- PDF CREATION DATE ---
                Calendar cCal = info.getCreationDate();
                if(cCal != null) {
                    String cdate = dateFormatter.format(cCal.getTime());
                    if(cdate != null && cdate.length() > 0) {
                        Topic creationDateType = createTopic(topicMap, "pdf-creation-date");
                        setData(topic, creationDateType, defaultLang, cdate.trim());
                    }
                }

                // --- PDF AUTHOR ---
                String author = info.getAuthor();
                if(author != null && author.length() > 0) {
                    Topic authorType = createTopic(topicMap, "pdf-author");
                    setData(topic, authorType, defaultLang, author.trim());
                }

                // --- PDF SUBJECT ---
                String subject = info.getSubject();
                if(subject != null && subject.length() > 0) {
                    Topic subjectType = createTopic(topicMap, "pdf-subject");
                    setData(topic, subjectType, defaultLang, subject.trim());
                }

                // --- PDF TITLE ---
                String title = info.getSubject();
                if(title != null && title.length() > 0) {
                    Topic titleType = createTopic(topicMap, "pdf-title");
                    setData(topic, titleType, defaultLang, title.trim());
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
                            createAssociation(topicMap, keywordType, new Topic[] { topic, keywordTopic } );
                        }
                    }
                }
                
                // --- PDF TEXT CONTENT ---
                PDFTextStripper stripper = new PDFTextStripper();
                String content = stripper.getText(doc);
                doc.close();
                setTextEnrichment(topic, topicMap, content, name);
            }
            
            
            // --- HANDLE RTF DOCUMENTS ---
            else if(lowerCaseLocator.endsWith("rtf")) {
                String content = Textbox.RTF2PlainText(inputStream);
                setTextEnrichment(topic, topicMap, content, name);
            }
            
            // --- HANDLE OFFICE DOCUMENTS ---
            else if(lowerCaseLocator.endsWith("doc") || lowerCaseLocator.endsWith("docx") ||
               lowerCaseLocator.endsWith("ppt") ||
               lowerCaseLocator.endsWith("xls") ||
               lowerCaseLocator.endsWith("vsd")
               ) {
                    String content = MSOfficeBox.getText(inputStream);
                    if(content != null) {
                        setTextEnrichment(topic, topicMap, content, name);
                    }
            }

            else if(lowerCaseLocator.endsWith("odt") || 
                    lowerCaseLocator.endsWith("odp") || 
                    lowerCaseLocator.endsWith("odg") || 
                    lowerCaseLocator.endsWith("ods")) {
                
                        org.odftoolkit.simple.Document oodocument = org.odftoolkit.simple.Document.loadDocument(inputStream);
                        String content = OpenOfficeBox.getText(oodocument);
                        setTextEnrichment(topic, topicMap, content, name);

                        org.odftoolkit.simple.meta.Meta meta = oodocument.getOfficeMetadata();
                        
                        // --- OO KEYWORDS ---
                        List<String> keywords = meta.getKeywords();
                        if(keywords != null && !keywords.isEmpty()) {
                            Topic keywordType = createTopic(topicMap, "oo-keyword");
                            for( String keyword : keywords) {
                                keyword = keyword.trim();
                                if(keyword != null && keyword.length() > 0) {
                                    Topic keywordTopic = createTopic(topicMap, keyword, keywordType);
                                    createAssociation(topicMap, keywordType, new Topic[] { topic, keywordTopic } );
                                }
                            }
                        }
                        
                        // --- OO TITLE ---
                        String title = meta.getTitle();
                        if(title != null && title.length() > 0) {
                            Topic titleType = createTopic(topicMap, "oo-title");
                            setData(topic, titleType, defaultLang, title.trim());
                        }
                        
                        // --- OO SUBJECT ---
                        String subject = meta.getSubject();
                        if(subject != null && subject.length() > 0) {
                            Topic subjectType = createTopic(topicMap, "oo-subject");
                            setData(topic, subjectType, defaultLang, subject.trim());
                        }
                        
                        // --- OO CREATOR ---
                        String author = meta.getCreator();
                        if(author != null && author.length() > 0) {
                            Topic authorType = createTopic(topicMap, "oo-author");
                            setData(topic, authorType, defaultLang, author.trim());
                        }
                        
                        // --- OO CREATION DATE ---
                        Calendar cCal = meta.getCreationDate();
                        if(cCal != null) {
                            DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
                            String cdate = dateFormatter.format(cCal.getTime());
                            if(cdate != null && cdate.length() > 0) {
                                Topic creationDateType = createTopic(topicMap, "oo-creation-date");
                                setData(topic, creationDateType, defaultLang, cdate.trim());
                            }
                        }
                        
                        // --- OO DESCRIPTION ---
                        String description = meta.getDescription();
                        if(description != null && description.length() > 0) {
                            Topic descriptionType = createTopic(topicMap, "oo-description");
                            setData(topic, descriptionType, defaultLang, description.trim());
                        }

                        // --- OO GENERATOR ---
                        String generator = meta.getGenerator();
                        if(generator != null && generator.length() > 0) {
                            Topic generatorType = createTopic(topicMap, "oo-generator");
                            setData(topic, generatorType, defaultLang, generator.trim());
                        }
            }
            
            else if(lowerCaseLocator.endsWith("html") ||
                    lowerCaseLocator.endsWith("htm")) {
                        String content = IObox.loadFile(new InputStreamReader(inputStream));
                        setTextEnrichment(topic, topicMap, content, name);
            }
            
            else if(lowerCaseLocator.endsWith("txt") ||
                    lowerCaseLocator.endsWith("text")) {
                        String content = IObox.loadFile(new InputStreamReader(inputStream));
                        setTextEnrichment(topic, topicMap, content, name);
            }
            
            // --- HANDLE ANY OTHER DOCUMENTS ---
            else {
                byte[] content = IObox.loadBFile(inputStream);
                String mimeType = "";
                MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
                Collection<MimeType> mimeTypes = new ArrayList();
                if(locator != null) {
                    if(MimeTypes.getMimeType(locator) != null) {
                        mimeTypes.add(new MimeType(MimeTypes.getMimeType(locator)));
                    }
                    mimeTypes.addAll(MimeUtil.getMimeTypes(locator));
                }
                mimeTypes.addAll(MimeUtil.getMimeTypes(content));
                boolean isText = false;
                for(MimeType mime : mimeTypes) {
                    if(MimeUtil.isTextMimeType(mime)) {
                        isText = true;
                        break;
                    }
                }
                if(isText) {
                    setTextEnrichment(topic, topicMap, new String(content), name);
                }
                else {
                    if(!mimeTypes.isEmpty()) {
                        MimeType mime = mimeTypes.iterator().next();
                        mimeType = mime.toString();
                    }
                    setBinaryEnrichment(topic, topicMap, content, mimeType);
                }
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    
    
    
    
    public void setBinaryEnrichment(Topic textTopic, TopicMap topicMap, byte[] content, String mimeType) {
        try {
            if(content != null && content.length > 0) {
                Topic contentType = createTopic(topicMap, "document-content");
                DataURL dataURL = new DataURL(mimeType, content);
                setData(textTopic, contentType, defaultLang, dataURL.toExternalForm());
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    
    public void setTextEnrichment(Topic topic, TopicMap topicMap, String content, String title) {
        try {
            String trimmedText = Textbox.trimExtraSpaces(content);
            if(trimmedText != null && trimmedText.length() > 0) {
                Topic contentType = createTopic(topicMap, "document-content");
                setData(topic, contentType, defaultLang, trimmedText);
            }
            if(title == null || title.length() == 0) {
                title = solveTitle(trimmedText);
            }
            if(title != null) {
                // textTopic.setBaseName(title + " ("+trimmedText.hashCode()+")");
                // textTopic.setDisplayName(defaultLang, title);
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    

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
                Topic contentType = createTopic(topicMap, "document-content");
                setData(textTopic, contentType, defaultLang, trimmedText);
            }
            String title = solveTitle(trimmedText);
            if(title != null) {
                textTopic.setBaseName(title + " (" + content.hashCode() + ")");
                textTopic.setDisplayName(defaultLang, title);
            }
            Topic documentType = getDocumentType(topicMap);
            textTopic.addType(documentType);
        }
        catch(Exception e) {
            log(e);
        }
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
        return getOrCreateTopic(tm, si, null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }


    // -------------------------------------------------------------------------
    
    
    public static final String[] contentTypes=new String[] { "application/pdf", "text/plain", "text/html", "application/rtf", "application/xml", "application/msword" };
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    
}
