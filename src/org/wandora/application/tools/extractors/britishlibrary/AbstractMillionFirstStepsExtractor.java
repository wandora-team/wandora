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


package org.wandora.application.tools.extractors.britishlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import javax.swing.Icon;
import org.wandora.application.Wandora;
import org.wandora.application.gui.UIBox;
import org.wandora.application.tools.extractors.AbstractExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TMBox;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;

/**
 *
 * @author akivela
 */


public abstract class AbstractMillionFirstStepsExtractor extends AbstractExtractor {
    

	private static final long serialVersionUID = 1L;

	protected static String defaultEncoding = "UTF-8";
    protected static String defaultLang = "en";
    

    public static final String LANG_SI = "http://www.topicmaps.org/xtm/1.0/language.xtm#"+defaultLang;
    
    public static final String BRITISH_LIBRARY_SI = "http://www.bl.uk/";
    
    public static final String BASE_SI = "http://wandora.org/si/british-libary/";
    
    public static final String BOOK_SI = BASE_SI + "book";
    public static final String AUTHOR_SI = BASE_SI + "author";
    public static final String CORPORATE_SI = BASE_SI + "corporate";
    public static final String TITLE_SI = BASE_SI + "title";
    public static final String PLACE_SI = BASE_SI + "publishing-place";
    public static final String DATE_SI = BASE_SI + "date";
    public static final String DATEFIELD_SI = BASE_SI + "date-field";
    public static final String PUBLISHER_SI = BASE_SI + "publisher";
    public static final String EDITION_SI = BASE_SI + "edition";
    public static final String ISSUANCE_SI = BASE_SI + "issuance";
    public static final String SHELFMARK_SI = BASE_SI + "shelfmark";
    public static final String IMAGE_SI = BASE_SI + "image";
    public static final String IMAGEIDX_SI = BASE_SI + "image-idx";
    public static final String ROLE_SI = BASE_SI + "role";
    public static final String ORDER_SI = BASE_SI + "order";
    public static final String ARKID_SI = BASE_SI + "ARK_id_of_book";
    public static final String BL_DLS_SI = BASE_SI + "BL_DLS_ID";
    public static final String PAGE_SI = BASE_SI + "page";
    public static final String VOLUME_SI = BASE_SI + "volume";
    public static final String PDF_SI = BASE_SI + "pdf";
    
   
    
    protected static Topic getLangTopic(TopicMap tm) throws TopicMapException {
        Topic lang = getOrCreateTopic(tm, LANG_SI);
        return lang;
    }
        

    protected static Topic getWandoraClassTopic(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si, null);
    }

    protected static Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }

    protected static void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }

    public static Topic getBritishLibraryTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, BRITISH_LIBRARY_SI, "British Library");
        Topic wandoraClass = getWandoraClassTopic(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public static Topic getATopic(String str, String si, String type, TopicMap tm) throws TopicMapException {
        Topic resTopic=null;
        if(str.startsWith("http://")) {
            resTopic = getOrCreateTopic(tm, str, null);
        }
        else {
            resTopic = getOrCreateTopic(tm, si+"/"+urlEncode(str), str);
        }
        Topic typeTopic = getATypeTopic(si, type, tm);
        if(!resTopic.isOfType(typeTopic)) resTopic.addType(typeTopic);
        return resTopic;
    }

    
    
    public static Topic getATypeTopic(String si, String type, TopicMap tm) throws TopicMapException {
        Topic typeTopic=getOrCreateTopic(tm, si, type);
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, typeTopic, blTopic);
        return typeTopic;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getBookTopic(String book, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, BOOK_SI+"/"+urlEncode(book), book);
        resTopic.addType(getBookTypeTopic(tm));
        return resTopic;
    }

    public static Topic getBookTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, BOOK_SI, "Book (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static Topic getArkIdTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ARKID_SI, "Ark id of book (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    public static Topic getBLDLSIdTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, BL_DLS_SI, "Digital Library Service identifier (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    public static Topic getPDFTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, PDF_SI, "PDF (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    public static Topic getImageIdxTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, IMAGEIDX_SI, "Image idx (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getAuthorTopic(String author, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, AUTHOR_SI+"/"+urlEncode(author), author);
        resTopic.addType(getAuthorTypeTopic(tm));
        return resTopic;
    }

    public static Topic getAuthorTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, AUTHOR_SI, "Author (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getCorporateTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, CORPORATE_SI+"/"+urlEncode(str), str);
        resTopic.addType(getCorporateTypeTopic(tm));
        return resTopic;
    }

    public static Topic getCorporateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, CORPORATE_SI, "Corporate (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getTitleTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, TITLE_SI+"/"+urlEncode(str), str);
        resTopic.addType(getTitleTypeTopic(tm));
        return resTopic;
    }

    public static Topic getTitleTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, TITLE_SI, "Title (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getPageTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, PAGE_SI+"/"+urlEncode(str), str);
        resTopic.addType(getTitleTypeTopic(tm));
        return resTopic;
    }

    public static Topic getPageTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, PAGE_SI, "Page (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getVolumeTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, VOLUME_SI+"/"+urlEncode(str), str);
        resTopic.addType(getTitleTypeTopic(tm));
        return resTopic;
    }

    public static Topic getVolumeTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, VOLUME_SI, "Volume (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    // -------------------------------------------------------------------------
    
    public static Topic getPlaceTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, PLACE_SI+"/"+urlEncode(str), str);
        resTopic.addType(getPlaceTypeTopic(tm));
        return resTopic;
    }

    public static Topic getPlaceTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, PLACE_SI, "Place of publishing (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getDatefieldTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, DATEFIELD_SI+"/"+urlEncode(str), str);
        resTopic.addType(getDatefieldTypeTopic(tm));
        return resTopic;
    }

    public static Topic getDatefieldTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DATEFIELD_SI, "Datefield (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getDateTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, DATE_SI+"/"+urlEncode(str), str);
        resTopic.addType(getDateTypeTopic(tm));
        return resTopic;
    }

    public static Topic getDateTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, DATE_SI, "Date (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getPublisherTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, PUBLISHER_SI+"/"+urlEncode(str), str);
        resTopic.addType(getPublisherTypeTopic(tm));
        return resTopic;
    }

    public static Topic getPublisherTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, PUBLISHER_SI, "Publisher (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getEditionTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, EDITION_SI+"/"+urlEncode(str), str);
        resTopic.addType(getEditionTypeTopic(tm));
        return resTopic;
    }

    public static Topic getEditionTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, EDITION_SI, "Edition (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static Topic getIssuanceTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, ISSUANCE_SI+"/"+urlEncode(str), str);
        resTopic.addType(getIssuanceTypeTopic(tm));
        return resTopic;
    }

    public static Topic getIssuanceTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ISSUANCE_SI, "Issuance (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getShelfmarkTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, SHELFMARK_SI+"/"+urlEncode(str), str);
        resTopic.addType(getShelfmarkTypeTopic(tm));
        return resTopic;
    }

    public static Topic getShelfmarkTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, SHELFMARK_SI, "Shelfmark (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    
    public static Topic getImageTopic(String str, String sl, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, sl, str);
        resTopic.setSubjectLocator(new Locator(sl));
        resTopic.addType(getImageTypeTopic(tm));
        return resTopic;
    }
    
    public static Topic getImageTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, str);
        resTopic.setSubjectLocator(new Locator(str));
        resTopic.addType(getImageTypeTopic(tm));
        return resTopic;
    }

    public static Topic getImageTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, IMAGE_SI, "Image (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getOrderTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, ORDER_SI+"/"+urlEncode(str), str);
        resTopic.addType(getOrderTypeTopic(tm));
        return resTopic;
    }

    public static Topic getOrderTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ORDER_SI, "Order (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    public static Topic getRoleTopic(String str, TopicMap tm) throws TopicMapException {
        Topic resTopic=getOrCreateTopic(tm, ROLE_SI+"/"+urlEncode(str), str);
        resTopic.addType(getRoleTypeTopic(tm));
        return resTopic;
    }

    public static Topic getRoleTypeTopic(TopicMap tm) throws TopicMapException {
        Topic type=getOrCreateTopic(tm, ROLE_SI, "Role (British Library)");
        Topic blTopic = getBritishLibraryTypeTopic(tm);
        makeSubclassOf(tm, type, blTopic);
        return type;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
      
    
   
    @Override
    public String getName() {
        return "Abstract Million First Steps extractor";
    }
    
    @Override
    public String getDescription(){
        return "Extracts topic map from the British Library's a million first steps metadata https://github.com/BL-Labs/imagedirectory";
    }

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_britishlibrary.png");
    }
    
    @Override
    public boolean runInOwnThread() {
      return true;
    }

    @Override
    public boolean useTempTopicMap() {
      return false;
    }

    @Override
    public boolean useURLCrawler() {
      return false;
    }
    
    // -------------------------------------------------------------------------
    
    
    

    protected String doUrl (URL url) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        
        if (url != null) {
            URLConnection con = url.openConnection();
            Wandora.initUrlConnection(con);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-type", "text/plain");
                
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName(defaultEncoding)));

                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    if(!(s.endsWith("\n") || s.endsWith("\r"))) sb.append("\n");
                }
                in.close();
            } 
            catch (Exception ex) {
                log(ex);
            }
        }
        
        return sb.toString();
    }
}
