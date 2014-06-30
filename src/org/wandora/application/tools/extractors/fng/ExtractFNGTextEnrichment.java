/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2014 Wandora Team
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
 * ExtractFNGTextEnrichment.java
 *
 * Created on 10. kesäkuuta 2006, 11:18
 *
 */

package org.wandora.application.tools.extractors.fng;


import org.wandora.utils.Textbox;
import org.wandora.utils.IObox;
import org.wandora.utils.MSOfficeBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TopicTools;
import org.wandora.topicmap.TopicMap;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.application.tools.extractors.*;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;



/**
 *
 * @author akivela
 */
public class ExtractFNGTextEnrichment extends AbstractExtractor implements WandoraTool {
    protected Wandora admin = null;
    protected File keywordFile = null;
    private String defaultLang = "en";
    
    
    /** Creates a new instance of ApplyKiasmaKeywords */
    public ExtractFNGTextEnrichment() {
    }
   
    
    @Override
    public String getName() {
        return "FNG Enrichment Text Extractor";
    }
    
    
    @Override
    public String getDescription() {
        return "Extract enrichment texts for FNG collection browser.";
    }
    
    

    
    

    @Override
    public String getGUIText(int textType) {
        switch(textType) {
            case SELECT_DIALOG_TITLE: return "Select enrichment text file(s) or directories containing enrichment text files!";
            case POINT_START_URL_TEXT: return "Where would you like to start the crawl?";
            case INFO_WAIT_WHILE_WORKING: return "Wait while seeking enrichment text files!";
        
            case FILE_PATTERN: return ".*\\.(pdf|txt|rtf|doc)";
            
            case DONE_FAILED: return "Done! No extractions! %1 enrichment text(s) and %2 other file(s) crawled!";
            case DONE_ONE: return "Done! Successful extraction! %1 enrichment text(s) and %2 other file(s) crawled!";
            case DONE_MANY: return "Done! Total %0 successful extractions! %1 enrichment text(s) and %2 other files crawled!";
            
            case LOG_TITLE: return "Enrichment Text Extraction Log";
        }
        return "";
    }
    
    
    
    
    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        if(url == null) return false;
        
        try {
            Topic textType = createTopic(topicMap, "tekstidokumentti");
            Topic textTopic = createTopic(topicMap, url.getFile(), " (tekstidokumentti)", url.getFile(), textType);
            textTopic.addSubjectIdentifier(new Locator(TopicTools.cleanDirtyLocator(url.toExternalForm())));
            
            URLConnection uc = null;
            if(admin != null) {
                uc = admin.wandoraHttpAuthorizer.getAuthorizedAccess(url);
            }
            else {
                uc = url.openConnection();
                Wandora.initUrlConnection(uc);
            }
            _extractTopicsFromStream(url.toExternalForm(), uc.getInputStream(), topicMap, textTopic);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from url\n" + url.toExternalForm(), e);
            takeNap(1000);
        }
        return false;
    }

    
    

    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        throw(new Exception(STRING_EXTRACTOR_NOT_SUPPORTED_MESSAGE));
    }
    
    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        if(file == null || file.isDirectory()) return false;
        
        try {
            Topic textType = createTopic(topicMap, "tekstidokumentti");
            Topic textTopic = createTopic(topicMap, file.getName(), " (tekstidokumentti)", file.getName(), textType);
            textTopic.addSubjectIdentifier(new Locator(TopicTools.cleanDirtyLocator(file.toURL().toExternalForm())));
            
            // --- ADD LAST MODIFICATION TIME AS OCCURRENCE ---
            try {
                Topic modType = createTopic(topicMap, "file-modified");
                String dateString = DateFormat.getDateInstance().format( new Date(file.lastModified()) );
                setData(textTopic, modType, "en", dateString);
                setData(textTopic, modType, "fi", dateString);
            }
            catch(Exception e) {
                log("Exception occurred while setting enrichment topic's modification time!", e);
            }
       
            _extractTopicsFromStream(file.getPath(), new FileInputStream(file), topicMap, textTopic);
            return true;
        }
        catch(Exception e) {
            log("Exception occurred while extracting from file " + file.getName(), e);
            takeNap(1000);
        }
        return false;
    }

    
    
    
    public void _extractTopicsFromStream(String locator, InputStream inputStream, TopicMap topicMap, Topic textTopic) {
        try {
            String lowerCaseLocator = locator.toLowerCase();

            // --- HANDLE PDF ENRICHMENT TEXT ---
            if(lowerCaseLocator.endsWith("pdf")) {

                PDDocument doc = PDDocument.load(new URL(locator));
                PDDocumentInformation info = doc.getDocumentInformation();

                // --- PDF SUBJECT ---
                String subject = info.getSubject();
                if(subject != null && subject.length() > 0) {
                    Topic subjectType = createTopic(topicMap, "subject");
                    setData(textTopic, subjectType, defaultLang, subject.trim());
                }

                // --- PDF TITLE ---
                String title = info.getTitle();
                if(title != null && title.length() > 0) {
                    Topic titleType = createTopic(topicMap, "title");
                    setData(textTopic, titleType, defaultLang, title.trim());
                }

                // --- PDF KEYWORDS ---
                String keywords = info.getKeywords();
                if(keywords != null && keywords.length() > 0) {
                    Topic keywordType = createTopic(topicMap, "keywords");
                    setData(textTopic, keywordType, defaultLang, keywords.trim());
                }

                // --- PDF TEXT CONTENT ---
                PDFTextStripper stripper = new PDFTextStripper();
                String content = stripper.getText(doc);
                setTextEnrichment(textTopic, topicMap, content);
                doc.close();
            }
            
            
            // --- HANDLE RTF DOCUMENTS ---
            else if(lowerCaseLocator.endsWith("rtf")) {
                String content = Textbox.RTF2PlainText(inputStream);
                setTextEnrichment(textTopic, topicMap, content);
            }
            
            // --- HANDLE OFFICE DOCUMENTS ---
            else if(lowerCaseLocator.endsWith("doc") || lowerCaseLocator.endsWith("docx") ||
               lowerCaseLocator.endsWith("ppt") ||
               lowerCaseLocator.endsWith("xsl") ||
               lowerCaseLocator.endsWith("vsd")
               ) {
                    String content = MSOfficeBox.getText(inputStream);
                    if(content != null) {
                        setTextEnrichment(textTopic, topicMap, content);
                    }
            }

            
            // --- HANDLE TXT DOCUMENTS ---
            else {
                String content = IObox.loadFile(new InputStreamReader(inputStream));
                setTextEnrichment(textTopic, topicMap, content);
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    
    
    public String solveTitle(String content) {
        String title = null;
        title = content.substring(0, Math.max(80, content.indexOf("\n")));
        if(title != null && title.length() > 80) {
            while(!title.endsWith(" ")) {
                title = title.substring(0, title.length()-1);
            }
            title = Textbox.trimExtraSpaces(title);
            if(title == null || title.length() == 0) return null;
        }
        return title;
    }
    
    
    
    public void setTextEnrichment(Topic textTopic, TopicMap topicMap, String content) {
        try {
            String trimmedText = Textbox.trimExtraSpaces(content);
            if(trimmedText != null && trimmedText.length() > 0) {
                Topic contentType = createTopic(topicMap, "teksti");
                setData(textTopic, contentType, "en", trimmedText);
                setData(textTopic, contentType, "fi", trimmedText);
                setData(textTopic, contentType, "se", trimmedText);
            }
            String title = solveTitle(trimmedText);
            if(title != null) {
                textTopic.setDisplayName("en", title);
                textTopic.setDisplayName("fi", title);
                textTopic.setDisplayName("se", title);
            }
        }
        catch(Exception e) {
            log(e);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    
    
    
    public static final String[] contentTypes=new String[] { "application/pdf", "text/plain", "application/rtf", "application/msword" };
    public String[] getContentTypes() {
        return contentTypes;
    }
}
