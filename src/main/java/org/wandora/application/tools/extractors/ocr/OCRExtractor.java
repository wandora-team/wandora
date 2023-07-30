/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 *
 * Copyright (C) 2004-2023 Wandora Team
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

package org.wandora.application.tools.extractors.ocr;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Icon;

import org.apache.commons.io.IOUtils;
/* wandora */
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.XTMPSI;
import org.wandora.utils.language.LanguageBox;




/**
 *
 * @author Eero Lehtonen
 */


public class OCRExtractor extends AbstractExtractor {
    
	private static final long serialVersionUID = 1L;
	
	protected String SOURCE_SI         = "http://wandora.org/si/source";
    protected String DOCUMENT_SI       = "http://wandora.org/si/document";
    
    protected String TEXT_CONTENT_SI   = "http://wandora.org/si/text_content";
    protected String DATE_EXTRACTED_SI = "http://wandora.org/si/time_extracted";
    protected String DATE_MODIFIED_SI  = "http://wandora.org/si/date_modified";
    protected String FILE_SIZE_SI      = "http://wandora.org/si/file_size";



    
    protected String TEMP_PATH = "temp"+File.separator+"ocr";
    protected SimpleDateFormat dateFormatter;
    
    
    @Override
    public String getName() {
        return "OCR Extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts topics and associations from image text data. ";
    }


    @Override
    public Icon getIcon() {
        return UIBox.getIcon(0xf1c5);
    }

    private final String[] contentTypes=new String[] { "image/jpeg", "image/jpg" };

    @Override
    public String[] getContentTypes() {
        return contentTypes;
    }
    
    @Override
    public boolean useURLCrawler() {
        return false;
    }
    
    @Override
    public int getExtractorType() {
        return FILE_EXTRACTOR | URL_EXTRACTOR;
    }
    
    
    // -------------------------------------------------------------------------
    
     @Override
    public boolean isConfigurable(){
        return false;
    }
     
    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        
        try {
            setWandora(wandora);
            String urlStr = request.getSource();
            
            URL u = new URL(urlStr);
            String mime = u.openConnection().getContentType();
            
            if(mime != null && mime.indexOf("image") > -1){
                _extractTopicsFrom(u, wandora.getTopicMap());
            } else {
                throw new Exception("incompatible mimetype");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return BrowserPluginExtractor.RETURN_ERROR+e.getMessage();
        }
        
        return null;
    }
    
    @Override
    public boolean acceptBrowserExtractRequest(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        return true;
    }

    @Override
    public String getBrowserExtractorName() {
        return getName();
    }
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap t) throws Exception {
        
        this.dateFormatter = new SimpleDateFormat();
        String si = f.toURI().toString();
        String lang = System.getenv("TESSERACT_LANG");
        if(lang == null) lang = "eng";
        
        Locator l = new Locator(si);
        Topic langTopic        = this.getOrCreateLangTopic(t, lang);
        Topic documentTopic    = t.getTopic(si);
        Topic dateModifiedType = this.getDateModifiedType(t);
        
        if(documentTopic == null) documentTopic = t.createTopic();
        
        documentTopic.addSubjectIdentifier(l);
        documentTopic.setSubjectLocator(l);
        documentTopic.setBaseName(f.getName());
        documentTopic.setDisplayName("en",f.getName());

        String modified = dateFormatter.format(new Date(f.lastModified()));
        documentTopic.setData(dateModifiedType, langTopic, modified);
        
        return this.processFile(f,t,documentTopic);
    }

    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap t) throws Exception {
        boolean success = false;
        URLConnection uc;
        File f = new File(TEMP_PATH + "_temp.dat");
        if(getWandora() != null){
            uc = getWandora().wandoraHttpAuthorizer.getAuthorizedAccess(u);
        } else {
            uc = u.openConnection();
            Wandora.initUrlConnection(uc);
        }
        
        String name = uc.getHeaderField("Content-Disposition");
        InputStream is = uc.getInputStream();
        try {
            FileOutputStream fos = new FileOutputStream(f);
            try{
                byte[] buffer = new byte[4096];  
                for (int n; (n = is.read(buffer)) != -1; )   
                fos.write(buffer, 0, n); 
            } finally {
                fos.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            is.close();
        }
        
        try{
            String si = u.toString();
            Locator l = new Locator(si);
            Topic documentTopic  = t.getTopic(si);
            
            if(documentTopic == null) documentTopic = t.createTopic();
            
            documentTopic.addSubjectIdentifier(l);
            documentTopic.setSubjectLocator(l);
            if(name != null) {
                documentTopic.setBaseName(name);
                documentTopic.setDisplayName("en",name);
            }
            
            documentTopic.addSubjectIdentifier(new Locator(si));
            success = processFile(f,t, documentTopic);
        } catch(Exception e){
            e.printStackTrace();
        }  finally {
            f.delete();
        }
        
        return success;
    }

    @Override
    public boolean _extractTopicsFrom(String str, TopicMap t) throws Exception {
        throw new UnsupportedOperationException("TODO");
    }
    
    private boolean processFile(File f, TopicMap tm, Topic documentTopic) throws TopicMapException{
        boolean success = false;
        this.dateFormatter = new SimpleDateFormat();
        String text = "";
        File tmp = new File(TEMP_PATH + ".txt");
        
        /*
         * Build the command to be executed in the form 
         *  <path/to/tesseract> <path/to/input> <path/to/output> -l <lang>
         * where the output file is temporary and is disposed of 
         * once it's contents are read.
         */
        
        ArrayList<String> cmd = new ArrayList<String>();
        String pathToTes = System.getenv("TESSERACT_PATH") + "tesseract";
        String lang      = System.getenv("TESSERACT_LANG");
        cmd.add(pathToTes);
        cmd.add(f.getAbsolutePath());
        cmd.add(TEMP_PATH);
        if(lang != null){
            cmd.add("-l");
            cmd.add(lang);
        }
                
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmd);
        
        try {
            Process p = pb.start();
            StreamGobbler gobbler = new StreamGobbler(p.getInputStream());
            StreamGobbler errorGobbler = new StreamGobbler((p.getErrorStream()));
            gobbler.start();
            errorGobbler.start();
            int w = p.waitFor();
            if(w == 0 && p.exitValue() == 0){ // Exited alright
                FileInputStream is = new FileInputStream(TEMP_PATH + ".txt");
                try{
                    text = IOUtils.toString(is);
                } finally {
                    is.close();
                }
            } else { // Something got messed up
                String error = errorGobbler.getMessage();
                if(error.length() == 0){
                    error = gobbler.getMessage();
                }
                System.out.println(error);
                throw new RuntimeException(error);
            }
            
            String extracted = dateFormatter.format(new Date());
            Long   size      = f.length();
            
            if(lang == null) lang = "eng";
            
            Topic langTopic          = getOrCreateLangTopic(tm, lang);
            Topic documentType       = createDocumentTypeTopic(tm);
            Topic contentType        = getContentType(tm); 
            Topic timeExtractedType  = getTimeExtractedType(tm);
            Topic fileSizeType       = getSizeType(tm);
            
            documentTopic.addType(documentType);
            
            documentTopic.setData(contentType, langTopic, text);
            documentTopic.setData(timeExtractedType,langTopic,extracted);
            documentTopic.setData(fileSizeType,langTopic,""+size);
            
            success = true;


        } catch(RuntimeException rte){
            log("The OCR runtime failed for " + f.getPath());
            log(rte.getMessage());
        } catch (TopicMapException tme) { // Adding the topic failed
            log("Failed to add the file topic with the path " + f.getPath());
        } catch (IOException ioe) { // A file operation failed
            log(ioe.getMessage());
        } catch(InterruptedException ie){
            log("The OCR process failed for the file " + f.getPath());
        } finally{ // Cleanup
            tmp.delete();
        }
        
        return success;
    }
    
    
    public Topic getContentType(TopicMap tm) throws TopicMapException {
       return getOrCreateTopic(tm,TEXT_CONTENT_SI, "Text Content");
        
    }
    
    public Topic getDateModifiedType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, DATE_MODIFIED_SI, "Date modified");
    }
    public Topic getTimeExtractedType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, DATE_EXTRACTED_SI, "Time extracted");
    }
    
    public Topic getSizeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, FILE_SIZE_SI, "Filesize");
    }
    
    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SOURCE_SI, "Source");
    }
    
    public Topic createDocumentTypeTopic(TopicMap tm) throws TopicMapException {
        Topic t = createTopic(tm, "OCR processed document");
        Topic w = getWandoraClass(tm);
        makeSubclassOf(tm, t, w);
        return t;
    }
    
    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return createTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }
    
    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }
    
    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }
    
    //lng in ISO 639-2 (three letters)
    protected Topic getOrCreateLangTopic(TopicMap tm, String lng6392) throws TopicMapException{
        String name = LanguageBox.getNameFor6392Code(lng6392);
        String lng6391 = LanguageBox.get6391ForName(name);
        Topic t = tm.getTopic(XTMPSI.getLang(lng6391));
        if(t == null){
            t = LanguageBox.createTopicForLanguageCode(lng6391, tm);
        }
        return t;
    }
    
}
