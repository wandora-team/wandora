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
 */



package org.wandora.application.tools.extractors.yahoo.boss;




import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.tools.extractors.*;
import org.wandora.utils.*;
import org.wandora.application.gui.*;
import javax.swing.*;
import java.io.*;




/**
 *
 * @author akivela
 */
public abstract class AbstractBossExtractor extends AbstractExtractor {


	private static final long serialVersionUID = 1L;

	protected String defaultEncoding = "UTF-8";

    // Default language of occurrences and variant names.
    public static String LANG = "en";

    // http://yboss.yahooapis.com/ysearch
    public static final String WEB_SERVICE_URL = "http://yboss.yahooapis.com/ysearch";


    public static final String BOSS_ROOT = "http://developer.yahoo.com/boss";
    public static final String QUERY_SI = "http://wandora.org/si/yahoo/boss/query";
    
    public static final String SOURCE_SI = "http://wandora.org/si/source";
    public static final String DOCUMENT_SI = "http://wandora.org/si/document";
    public static final String TOPIC_SI = "http://wandora.org/si/topic";

    public static final String BOSS_TITLE_SI = "http://wandora.org/si/yahoo/boss/title";
    public static final String BOSS_DESCRIPTION_SI = "http://wandora.org/si/yahoo/boss/description";
    public static final String BOSS_DATETIME_SI = "http://wandora.org/si/yahoo/boss/datetime";

    public static final String BOSS_WIDTH_SI = "http://wandora.org/si/yahoo/boss/width";
    public static final String BOSS_HEIGHT_SI = "http://wandora.org/si/yahoo/boss/height";
    public static final String BOSS_CONTENTTYPE_SI = "http://wandora.org/si/yahoo/boss/contenttype";
    public static final String BOSS_FILESIZE_SI = "http://wandora.org/si/yahoo/boss/filesize";

    public static final String BOSS_WEBRESULT_SI = "http://wandora.org/si/yahoo/boss/webresult";
    public static final String BOSS_IMAGERESULT_SI = "http://wandora.org/si/yahoo/boss/imageresult";




    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_yahoo.png");
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
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        BossExtractorConfiguration dialog=new BossExtractorConfiguration(admin,options,this);
        dialog.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
    }


    // -------------------------------------------------------------------------




    
    public abstract boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception;







    public void fillQueryTopic(Topic queryTopic, TopicMap topicMap, String query) {
        try {
            String trimmedQuery = query.trim();
            if(trimmedQuery != null && trimmedQuery.length() > 0) {
                Topic contentType = createTopic(topicMap, "query-text");
                setData(queryTopic, contentType, "en", trimmedQuery);
            }
            String title = trimmedQuery+" (Yahoo! BOSS query)";
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
                Topic titleTopic = getOrCreateTopic(tm, BOSS_TITLE_SI+"/"+urlEncode(title), title);
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
                Topic descriptionTopic = getOrCreateTopic(tm, BOSS_DESCRIPTION_SI+"/"+urlEncode(description), description);
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
                Topic datetimeTopic = getOrCreateTopic(tm, BOSS_DATETIME_SI+"/"+urlEncode(datetime), datetime);
                Topic datetimeTypeTopic = getDateTimeType(tm);
                datetimeTopic.addType(datetimeTypeTopic);
                return datetimeTopic;
            }
        }
        return null;
    }


    public Topic getTitleType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BOSS_TITLE_SI, "Yahoo! BOSS title");
    }

    public Topic getDescriptionType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BOSS_DESCRIPTION_SI, "Yahoo! BOSS abstract");
    }

    public Topic getDateTimeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BOSS_DATETIME_SI, "Yahoo! BOSS date");
    }

    public Topic getWidthType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BOSS_WIDTH_SI, "Yahoo! BOSS image width");
    }

    public Topic getHeightType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BOSS_HEIGHT_SI, "Yahoo! BOSS image height");
    }

    public Topic getContentTypeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BOSS_CONTENTTYPE_SI, "Yahoo! BOSS content type");
    }

    public Topic getFileSizeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, BOSS_FILESIZE_SI, "Yahoo! BOSS file size");
    }




    // ******** TOPIC MAPS *********


    
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



    public Topic getBossClass(TopicMap tm) throws TopicMapException {
        Topic b = getOrCreateTopic(tm, BOSS_ROOT, "Yahoo! BOSS");
        Topic w = getWandoraClass(tm);
        makeSubclassOf(tm, b, w);
        return b;
    }


    public Topic getQueryType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, QUERY_SI, "Yahoo! BOSS query");
        Topic b = getBossClass(tm);
        makeSubclassOf(tm, type, b);
        return type;
    }

    public Topic getWebSearchResultType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, BOSS_WEBRESULT_SI, "Yahoo! BOSS search result");
        Topic b = getBossClass(tm);
        makeSubclassOf(tm, type, b);
        return type;
    }
    public Topic getImageSearchResultType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, BOSS_IMAGERESULT_SI, "Yahoo! BOSS image search result");
        Topic b = getBossClass(tm);
        makeSubclassOf(tm, type, b);
        return type;
    }

    // --------

    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }


    
    


    // -------------------------------------------------------------------------





    private static String appid = "E81HVVrV34Go76YxgyKGeQjxGUhRrnzR939Jxdl8KJXpjJXITTehlcPxzA4jDHb_CQ--";
    protected String solveAppId() {
        if(appid == null) {
            appid = "";
            appid = WandoraOptionPane.showInputDialog(getWandora(), "Please give valid AppID for Yahoo! BOSS API. You can register your apikey at http://developer.yahoo.com/search/boss/", appid, "AppID", WandoraOptionPane.QUESTION_MESSAGE);
        }
        return appid;
    }



    
    public void forgetAuthorization() {
        appid = null;
    }





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
