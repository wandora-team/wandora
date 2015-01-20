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
 *
 * AbstractBingExtractor.java
 *
 *
 */
package org.wandora.application.tools.extractors.bing;




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
import org.wandora.application.gui.*;
import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import org.wandora.application.tools.GenericOptionsDialog;




/**
 *
 * @author akivela
 */
public abstract class AbstractBingExtractor extends AbstractExtractor {

    protected boolean EXTRACT_RELEVANCE = true;


    protected String defaultEncoding = "ISO-8859-1";

    // Default language of occurrences and variant names.
    public static String LANG = "en";


    public static final String BING_ROOT = "http://www.bing.com";

    public static final String BING_URL = "api.datamarket.azure.com/Bing/Search/Web";



    public static final String SOURCE_SI = "http://wandora.org/si/source";
    public static final String QUERY_SI = "http://wandora.org/si/bing/query";
    public static final String TOPIC_SI = "http://wandora.org/si/topic";

    public static final String BING_TITLE_SI = "http://wandora.org/si/bing/title";
    public static final String BING_DESCRIPTION_SI = "http://wandora.org/si/bing/description";
    public static final String BING_DATETIME_SI = "http://wandora.org/si/bing/datetime";

    public static final String BING_WIDTH_SI = "http://wandora.org/si/bing/width";
    public static final String BING_HEIGHT_SI = "http://wandora.org/si/bing/height";
    public static final String BING_CONTENTTYPE_SI = "http://wandora.org/si/bing/contenttype";
    public static final String BING_FILESIZE_SI = "http://wandora.org/si/bing/filesize";

    public static final String BING_WEBRESULT_SI = "http://wandora.org/si/bing/webresult";
    public static final String BING_IMAGERESULT_SI = "http://wandora.org/si/bing/imageresult";




    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_bing.png");
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
        return true;
    }
    @Override
    public void configure(Wandora wandora, org.wandora.utils.Options options, String prefix) throws TopicMapException {
        BingExtractorConfiguration dialog=new BingExtractorConfiguration(wandora,options,this);
        dialog.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora wandora,org.wandora.utils.Options options,String prefix){
    }
    


    // -------------------------------------------------------------------------





    public abstract boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception;






    /*
    public String solveTitle(String content) {
        if(content == null || content.length() == 0) return "empty-search";
        content = content.trim();
        
        boolean forceTrim = false;
        String title = null;
        int i = content.indexOf("\n");
        if(i > 0) {
            title = content.substring(0, i);
            title.trim();
        }
        else {
            title = content.substring(0, Math.min(80, content.length()));
            forceTrim = true;
        }

        if(title != null && (forceTrim || title.length() > 80)) {
            title = title.substring(0, Math.min(80, title.length()));
            while(!title.endsWith(" ") && title.length()>60) {
                title = title.substring(0, title.length()-1);
            }
            title = title.trim() + "...";
        }
        return title;
    }

    */



    public void fillQueryTopic(Topic queryTopic, TopicMap topicMap, String query) {
        try {
            String trimmedQuery = query.trim();
            if(trimmedQuery != null && trimmedQuery.length() > 0) {
                Topic contentType = createTopic(topicMap, "query-text");
                setData(queryTopic, contentType, "en", trimmedQuery);
            }
            String title = trimmedQuery+" (Bing query)";
            if(title != null) {
                queryTopic.setBaseName(title);
                queryTopic.setDisplayName("en", title);
            }
            Topic queryType = getQueryType(topicMap);
            queryTopic.addType(queryType);
        }
        catch(Exception e) {
            log(e);
        }
    }



    // ******** TOPIC MAPS *********




    public Topic getWebSearchResultTopic(TopicMap tm, String title, String description, String url, String datetime) throws TopicMapException {
        if(title != null && url != null) {
            Topic searchResultTopic = getOrCreateTopic(tm, url, title+" ("+url.hashCode()+")");
            searchResultTopic.setSubjectLocator(new Locator(url));
            searchResultTopic.setDisplayName(LANG, title);

            if(description != null && description.length() > 0) {
                Topic descriptionType = getDescriptionType(tm);
                searchResultTopic.setData(descriptionType, tm.getTopic(XTMPSI.LANG_PREFIX+LANG), description);
            }

            if(datetime != null && datetime.length() > 0) {
                Topic datetimeType = getDateTimeType(tm);
                searchResultTopic.setData(datetimeType, tm.getTopic(XTMPSI.LANG_PREFIX+LANG), datetime);
            }

            searchResultTopic.addType(getWebSearchResultType(tm));

            return searchResultTopic;
        }
        return null;
    }




    public Topic getImageSearchResultTopic(TopicMap tm, String title, String url, String width, String height, String contentType, String fileSize ) throws TopicMapException {
        if(title != null && url != null) {
            Topic searchResultTopic = getOrCreateTopic(tm, url, title+" ("+url.hashCode()+")");
            searchResultTopic.setSubjectLocator(new Locator(url));
            searchResultTopic.setDisplayName(LANG, title);

            if(width != null && width.length() > 0) {
                Topic widthType = getWidthType(tm);
                searchResultTopic.setData(widthType, tm.getTopic(XTMPSI.LANG_PREFIX+LANG), width);
            }

            if(height != null && height.length() > 0) {
                Topic heightType = getHeightType(tm);
                searchResultTopic.setData(heightType, tm.getTopic(XTMPSI.LANG_PREFIX+LANG), height);
            }

            if(contentType != null && contentType.length() > 0) {
                Topic contentTypeType = getContentTypeType(tm);
                searchResultTopic.setData(contentTypeType, tm.getTopic(XTMPSI.LANG_PREFIX+LANG), contentType);
            }

            if(fileSize != null && fileSize.length() > 0) {
                Topic fileSizeType = getFileSizeType(tm);
                searchResultTopic.setData(fileSizeType, tm.getTopic(XTMPSI.LANG_PREFIX+LANG), fileSize);
            }

            searchResultTopic.addType(getImageSearchResultType(tm));

            return searchResultTopic;
        }
        return null;
    }






    
    public Topic getTitleTopic(String title, TopicMap tm) throws TopicMapException {
        if(title != null) {
            title = title.trim();
            if(title.length() > 0) {
                Topic titleTopic = getOrCreateTopic(tm, BING_TITLE_SI+"/"+title, title);
                Topic titleTypeTopic = getTitleType(tm);
                titleTopic.addType(titleTypeTopic);
                return titleTopic;
            }
        }
        return null;
    }


    public Topic getDescriptionTopic(String description, TopicMap tm) throws TopicMapException {
        if(description != null) {
            description = description.trim();
            if(description.length() > 0) {
                Topic descriptionTopic = getOrCreateTopic(tm, BING_DESCRIPTION_SI+"/"+description, description);
                Topic descriptionTypeTopic = getDescriptionType(tm);
                descriptionTopic.addType(descriptionTypeTopic);
                return descriptionTopic;
            }
        }
        return null;
    }

    public Topic getDateTimeTopic(String datetime, TopicMap tm) throws TopicMapException {
        if(datetime != null) {
            datetime = datetime.trim();
            if(datetime.length() > 0) {
                Topic datetimeTopic = getOrCreateTopic(tm, BING_DATETIME_SI+"/"+datetime, datetime);
                Topic datetimeTypeTopic = getDateTimeType(tm);
                datetimeTopic.addType(datetimeTypeTopic);
                return datetimeTopic;
            }
        }
        return null;
    }


    public Topic getTitleType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BING_TITLE_SI, "Bing title");
    }

    public Topic getDescriptionType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BING_DESCRIPTION_SI, "Bing description");
    }

    public Topic getDateTimeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BING_DATETIME_SI, "Bing datetime");
    }

    public Topic getWidthType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BING_WIDTH_SI, "Bing image width");
    }

    public Topic getHeightType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BING_HEIGHT_SI, "Bing image height");
    }

    public Topic getContentTypeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BING_CONTENTTYPE_SI, "Bing content type");
    }

    public Topic getFileSizeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BING_FILESIZE_SI, "Bing file size");
    }


    

    // ----


    public Topic getBingClass(TopicMap tm) throws TopicMapException {
        Topic b = getOrCreateTopic(tm, BING_ROOT, "Bing");
        Topic w = getWandoraClass(tm);
        makeSubclassOf(tm, b, w);
        return b;
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



    public Topic getQueryType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, QUERY_SI, "Bing query");
        Topic b = getBingClass(tm);
        makeSubclassOf(tm, type, b);
        return type;
    }

    public Topic getWebSearchResultType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, BING_WEBRESULT_SI, "Bing web search result");
        Topic b = getBingClass(tm);
        makeSubclassOf(tm, type, b);
        return type;
    }

    public Topic getImageSearchResultType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, BING_IMAGERESULT_SI, "Bing image search result");
        Topic b = getBingClass(tm);
        makeSubclassOf(tm, type, b);
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
    // -------------------------------------------------------- BING API KEY ---
    // -------------------------------------------------------------------------



    private static HashMap<String,String> auth = null;
    
    private static final String dialogName 
            = "Provide authentication details for the BING api";
    
    
    
    public HashMap<String,String> solveAuth(Wandora wandora) {
        if(auth == null) {
            
            GenericOptionsDialog god=new GenericOptionsDialog(wandora,dialogName,dialogName,true,new String[][]{
                new String[]{"Username","string","","Your username"},
                new String[]{"Account key","string","","Your account key"},
            },wandora);
            
            god.setVisible(true);
            if(god.wasCancelled()) return null;
            
             auth = (HashMap) god.getValues();
            
        }
        
        return auth;
    }




    public static void forgetAuth() {
        //System.out.println("Bing API key forgot!");
        auth = null;
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

