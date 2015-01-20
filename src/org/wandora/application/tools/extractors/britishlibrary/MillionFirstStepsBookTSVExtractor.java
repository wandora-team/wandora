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
 */


package org.wandora.application.tools.extractors.britishlibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import org.wandora.application.Wandora;
import static org.wandora.application.tools.extractors.britishlibrary.AbstractMillionFirstStepsExtractor.defaultEncoding;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.memory.TopicMapImpl;


/**
 *
 * @author akivela
 */


public class MillionFirstStepsBookTSVExtractor extends AbstractMillionFirstStepsExtractor {
    
       
    @Override
    public String getName() {
        return "BL's million first steps book TSV extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extracts topic map from the British Library's a million first steps book TSV https://github.com/BL-Labs/imagedirectory";
    }


    // -------------------------------------------------------------------------
    
    
    
    
    @Override
    public boolean _extractTopicsFrom(File f, TopicMap tm) throws Exception {
        if(f != null) {
            if(f.isDirectory()) {
                _extractTopicsFrom(f.listFiles());
            }
            else {
                log("Reading file "+f.getName());
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), defaultEncoding));
                    StringBuilder stringBuilder = new StringBuilder("");
                    String str;
                    while ((str = in.readLine()) != null) {
                        stringBuilder.append(str);
                        stringBuilder.append("\n");
                    }
                    in.close();
                    parse(stringBuilder.toString(), tm);
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }
        return true;
    }

    
    public void _extractTopicsFrom(File[] f) throws Exception {
        Arrays.sort(f);
        int i=0;
        File file = null;
        String year = null;
        String filename = null;
        String nextYear = null;
        while(i<f.length && !forceStop()) {
            file = f[i];
            filename = file.getName();
            if(filename.endsWith(".tsv") && !file.isDirectory()) {
                year = filename.substring(0, 4);
                TopicMap tm = new TopicMapImpl();
                do {
                    _extractTopicsFrom(file, tm);
                    i++;
                    file = f[i];
                    nextYear = file.getName().substring(0, 3);
                }
                while(year.equalsIgnoreCase(nextYear) && !forceStop());

                File parent = file.getParentFile();
                File exportFolder = new File( parent.getAbsolutePath()+"/"+"xtm" );
                if(!exportFolder.exists()) exportFolder.mkdir();
                String tmFilename = exportFolder.getAbsolutePath()+"/"+year+".xtm";
                log("Exporting topic map to "+tmFilename);
                tm.exportXTM(tmFilename);
            }
            else {
                i++;
            }
        }
    }
    
    
    
    @Override
    public void handleFiles(File[] files, TopicMap tm) {
        if(files == null || files.length == 0) return;
        for(int i=0; i<files.length && !forceStop(); i++) {
            if(files[i] != null) {
                try {
                    extractTopicsFrom(files[i], tm);
                }
                catch(Exception e) {
                    log(e);
                    log("Extraction from '" + croppedFilename(files[i].getName()) + "' failed.");
                }
            }
        }
     }
     
    
    
    
    
    
    @Override
    public boolean _extractTopicsFrom(URL u, TopicMap tm) throws Exception {
        try {
            log("Reading URL "+u.toExternalForm());
            String in = doUrl(u);
            parse(in, tm);
   
        } 
        catch (Exception e){
           log(e);
        }
        return true;
    }

    
    
    @Override
    public boolean _extractTopicsFrom(String str, TopicMap tm) throws Exception {
        log("Processing string.");
        parse(str, tm);
        return true;
    }
    
    
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void parse(String str, TopicMap tm) {
        if(str != null && str.length() > 0) {
            HashMap<String,Integer> columnIndexes = new HashMap();
            String[] lines = str.split("\n");
            if(lines.length > 1) {
                log("Parsing data.");
                setProgressMax(lines.length);
                parseColumnNames(lines[0], columnIndexes);
                for(int i=1; i<lines.length && !forceStop(); i++) {
                    parseLine(lines[i], columnIndexes, tm);
                    setProgress(i);
                }
            }
            else {
                log("Data should contain more than one line. First line contains column labels.");
            }
        }
        else {
            log("No data for parser.");
        }
    }
    
    
    
    public void parseColumnNames(String columns, HashMap<String,Integer> columnIndexes) {
        String[] columnNames = columns.split("\t");
        for(int i=0; i<columnNames.length; i++) {
            columnIndexes.put(columnNames[i], i);
        }
    }
    
    
    
    
    
    public void parseLine(String str, HashMap<String,Integer> columnIndexes, TopicMap tm) {
        if(str != null && str.length()>0) {
            String[] tokens = str.split("\t");
            if(tokens.length > 4) {
                String flickr_id = getIndex("flickr_id", tokens, columnIndexes);
                String flickr_url = getIndex("flickr_url", tokens, columnIndexes);
                String book_identifier = getIndex("book_identifier", tokens, columnIndexes);
                String title = getIndex("title", tokens, columnIndexes);
                String first_author = getIndex("first_author", tokens, columnIndexes);
                String pubplace = getIndex("pubplace", tokens, columnIndexes);
                String publisher = getIndex("publisher", tokens, columnIndexes);
                String date = getIndex("date", tokens, columnIndexes);
                String volume = getIndex("volume", tokens, columnIndexes);
                String page = getIndex("page", tokens, columnIndexes);
                String image_idx = getIndex("image_idx", tokens, columnIndexes);
                String ARK_id_of_book = getIndex("ARK_id_of_book", tokens, columnIndexes);
                String BL_DLS_ID = getIndex("BL_DLS_ID", tokens, columnIndexes);
                String flickr_original_source = getIndex("flickr_original_source", tokens, columnIndexes);
                String flickr_original_height = getIndex("flickr_original_height", tokens, columnIndexes);
                String flickr_original_width = getIndex("flickr_original_width", tokens, columnIndexes);
                String flickr_large_source = getIndex("flickr_large_source", tokens, columnIndexes);
                String flickr_large_height = getIndex("flickr_large_height", tokens, columnIndexes);
                String flickr_large_width = getIndex("flickr_large_width", tokens, columnIndexes);
                String flickr_medium_source = getIndex("flickr_medium_source", tokens, columnIndexes);
                String flickr_medium_height = getIndex("flickr_medium_height", tokens, columnIndexes);
                String flickr_medium_width = getIndex("flickr_medium_width", tokens, columnIndexes);
                String flickr_small_source = getIndex("flickr_small_source", tokens, columnIndexes);
                String flickr_small_height = getIndex("flickr_small_height", tokens, columnIndexes);
                String flickr_small_width = getIndex("flickr_small_width", tokens, columnIndexes);
                
                if(isValid(book_identifier)) {
                    try {
                        Topic bookTopic = getBookTopic(book_identifier, tm);
                        if(isValid(title)) {
                            bookTopic.setBaseName(title+" ("+book_identifier+")");
                            bookTopic.setDisplayName(defaultLang, title);
                        }
                        if(isValid(ARK_id_of_book)) {
                            Topic typeTopic = getArkIdTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            bookTopic.setData(typeTopic, langTopic, ARK_id_of_book);
                        }
                        if(isValid(BL_DLS_ID)) {
                            Topic typeTopic = getBLDLSIdTypeTopic(tm);
                            Topic langTopic = getLangTopic(tm);
                            bookTopic.setData(typeTopic, langTopic, BL_DLS_ID);
                            
                            Topic pdfTypeTopic = getPDFTypeTopic(tm);
                            bookTopic.setData(pdfTypeTopic, langTopic, "http://access.bl.uk/item/pdf/"+BL_DLS_ID);
                        }
                        if(isValid(first_author)) {
                            Topic topic = getAuthorTopic(first_author, tm);
                            Topic typeTopic = getAuthorTypeTopic(tm);

                            Association a = tm.createAssociation(typeTopic);
                            a.addPlayer(topic, typeTopic);
                            a.addPlayer(bookTopic, getBookTypeTopic(tm));
                        }
                        if(isValid(flickr_id) && isValid(flickr_original_source)) {
                            Topic imageTopic = getImageTopic(flickr_id, flickr_original_source, tm);
                            Topic typeTopic = getImageTypeTopic(tm);

                            Association a = tm.createAssociation(typeTopic);
                            a.addPlayer(imageTopic, typeTopic);
                            a.addPlayer(bookTopic, getBookTypeTopic(tm));
                            
                            if(imageTopic != null) {
                                if(isValid(image_idx)) {
                                    Topic imageIdxTypeTopic = getImageIdxTypeTopic(tm);
                                    Topic langTopic = getLangTopic(tm);
                                    imageTopic.setData(imageIdxTypeTopic, langTopic, image_idx);
                                }
                                if(isValid(volume)) {
                                    Topic volumeTypeTopic = getVolumeTypeTopic(tm);
                                    Topic langTopic = getLangTopic(tm);
                                    imageTopic.setData(volumeTypeTopic, langTopic, volume);
                                }
                                if(isValid(page)) {
                                    Topic pageTypeTopic = getPageTypeTopic(tm);
                                    Topic langTopic = getLangTopic(tm);
                                    imageTopic.setData(pageTypeTopic, langTopic, page);
                                }
                            }
                        }
                        if(isValid(first_author)) {
                            Topic authorTopic = getAuthorTopic(first_author, tm);
                            Topic typeTopic = getAuthorTypeTopic(tm);

                            Association a = tm.createAssociation(typeTopic);
                            a.addPlayer(authorTopic, typeTopic);
                            a.addPlayer(bookTopic, getBookTypeTopic(tm));
                        }
                        if(isValid(publisher)) {
                            Topic publisherTopic = getPublisherTopic(publisher, tm);
                            Topic typeTopic = getPublisherTypeTopic(tm);

                            Association a = tm.createAssociation(typeTopic);
                            a.addPlayer(publisherTopic, typeTopic);
                            a.addPlayer(bookTopic, getBookTypeTopic(tm));
                        }
                        if(isValid(pubplace)) {
                            Topic pubplaceTopic = getPlaceTopic(pubplace, tm);
                            Topic typeTopic = getPlaceTypeTopic(tm);

                            Association a = tm.createAssociation(typeTopic);
                            a.addPlayer(pubplaceTopic, typeTopic);
                            a.addPlayer(bookTopic, getBookTypeTopic(tm));
                        }
                        if(isValid(date)) {
                            Topic dateTopic = getDateTopic(date, tm);
                            Topic typeTopic = getDateTypeTopic(tm);

                            Association a = tm.createAssociation(typeTopic);
                            a.addPlayer(dateTopic, typeTopic);
                            a.addPlayer(bookTopic, getBookTypeTopic(tm));
                        }
                     }
                    catch(Exception e) {
                        log(e);
                    }
                }
            }
        }
    }
    
    
    
    private boolean isValid(String data) {
        if(data != null && data.length() > 0) {
            return true;
        }
        return false;
    }
    
    
    
    private String getIndex(String indexName, String[] array, HashMap<String,Integer> columnIndexes) {
        if(array != null && indexName != null && columnIndexes != null) {
            try {
                return array[columnIndexes.get(indexName)];
            }
            catch(Exception e) {}
        }
        return null;
    }
}
