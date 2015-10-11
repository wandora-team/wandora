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
 */
package org.wandora.application.tools.extractors.zemanta;


import java.net.*;
import java.io.*;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.application.tools.extractors.ExtractHelper;
import org.xml.sax.*;

import org.wandora.topicmap.*;
import org.wandora.utils.*;




/**
 *
 * @author akivela
 */
public class ZemantaExtractor extends AbstractZemantaExtractor implements BrowserPluginExtractor {



    @Override
    public String getName() {
        return "Zemanta extractor";
    }

    @Override
    public String getDescription(){
        return "Extracts information out of given text using Zemanta. Read more at http://www.zemanta.com/.";
    }





    public boolean _extractTopicsFrom(URL url, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(ExtractHelper.getContent(url),topicMap);
    }


    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        return _extractTopicsFrom(new FileInputStream(file),topicMap);
    }


    public boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception {
        String data = IObox.loadFile(in, defaultEncoding);
        return _extractTopicsFrom(data, topicMap);
    }


    public boolean _extractTopicsFrom(String data, TopicMap topicMap) throws Exception {
        String apikey = solveAPIKey();
        if(data != null && data.length() > 0) { 
            if(apikey != null) apikey = apikey.trim();
            if(apikey != null && apikey.length() > 0) {
                String content = data;
                
                String zemantaURL = ZEMANTA_URL;
                String zemantaData = "api_key="+URLEncoder.encode(apikey, "utf-8")+"&method=zemanta.suggest&format=xml&text="+URLEncoder.encode(content, "utf-8");
                String result = sendRequest(new URL(zemantaURL), zemantaData, "application/x-www-form-urlencoded", "POST");

                System.out.println("Zemanta returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();
                ZemantaParser parserHandler = new ZemantaParser(getMasterSubject(), content, topicMap, this);
                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try {
                    reader.parse(new InputSource(new StringReader(result)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                
                if(getCurrentLogger() != null) {
                    if(parserHandler.keywordCount > 0) {
                        log("Zemanta found " + parserHandler.keywordCount + " keywords.");
                    }
                    if(parserHandler.categoryCount > 0) {
                        log("Zemanta found " + parserHandler.categoryCount + " categories.");
                    }
                    if(parserHandler.imageCount > 0) {
                        log("Zemanta found " + parserHandler.imageCount + " images.");
                    }
                    if(parserHandler.articleCount > 0) {
                        log("Zemanta found " + parserHandler.articleCount + " articles.");
                    }
                    log("Ready.");
                }
            }
            else {
                log("No valid API key given! Aborting!");
            }
        }
        else {
            log("No valid data given! Aborting!");
        }
        return true;
    }







    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------






    public class ZemantaParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {




        public ZemantaParser(String term, String data, TopicMap tm, AbstractZemantaExtractor parent){
            this.tm=tm;
            this.parent=parent;

            try {
                if(term != null) {
                    masterTopic = tm.getTopicWithBaseName(term);
                    if(masterTopic == null) masterTopic = tm.getTopic(term);
                }
            }
            catch(Exception e) {
                parent.log(e);
            }
            if(masterTopic == null && data != null && data.length() > 0) {
                try {
                    masterTopic = tm.createTopic();
                    masterTopic.addSubjectIdentifier(tm.makeSubjectIndicatorAsLocator());
                    parent.fillDocumentTopic(masterTopic, tm, data);
                }
                catch(Exception e) {
                    parent.log(e);
                }
            }
            this.tm=tm;
            this.parent=parent;
        }


        Topic masterTopic = null;
        public int keywordCount = 0;
        public int categoryCount = 0;
        public int imageCount = 0;
        public int articleCount = 0;

        private TopicMap tm;
        private AbstractZemantaExtractor parent;


        public static final String TAG_RSP="rsp";
        public static final String TAG_RESULTS="results";
        public static final String TAG_STATUS="status";
        public static final String TAG_RID="rid";
        public static final String TAG_ARTICLES="articles";
        public static final String TAG_KEYWORDS="keywords";
        public static final String TAG_IMAGES="images";
        public static final String TAG_MARKUP="markup";
        public static final String TAG_CATEGORIES="categories";
        public static final String TAG_SIGNATURE="signature";


        public static final String TAG_ARTICLE="article";
        public static final String TAG_KEYWORD="keyword";
        public static final String TAG_IMAGE="image";
        public static final String TAG_CATEGORY="category";

        public static final String TAG_URL="url";
        public static final String TAG_TITLE="title";
        public static final String TAG_PUBLISHED_DATETIME="published_datetime";
        public static final String TAG_CONFIDENCE="confidence";
        public static final String TAG_ZEMIFIED="zemified";

        public static final String TAG_NAME="name";
        public static final String TAG_SCHEMA="schema";

        public static final String TAG_URL_L="url_l";
        public static final String TAG_URL_M="url_m";
        public static final String TAG_URL_S="url_s";
        public static final String TAG_URL_L_W="url_l_w";
        public static final String TAG_URL_M_W="url_m_w";
        public static final String TAG_URL_S_W="url_s_w";
        public static final String TAG_URL_L_H="url_l_h";
        public static final String TAG_URL_M_H="url_m_h";
        public static final String TAG_URL_S_H="url_s_h";

        public static final String TAG_SOURCE_URL="source_url";
        public static final String TAG_LICENSE="license";
        public static final String TAG_DESCRIPTION="description";
        public static final String TAG_ATTRIBUTION="attribution";

        public static final String TAG_TEXT="text";
        public static final String TAG_LINKS="links";
        public static final String TAG_LINK="link";
        public static final String TAG_RELEVANCE="relevance";

        public static final String TAG_ANCHOR="anchor";
        public static final String TAG_TARGET="target";

        public static final String TAG_TYPE="type";

        public static final String TAG_CATEGORIZATION="categorization";




        private static final int STATE_START=0;
        private static final int STATE_RSP=1;
        private static final int STATE_RSP_STATUS=11;

        private static final int STATE_RSP_ARTICLES=12;
        private static final int STATE_RSP_ARTICLES_ARTICLE=121;
        private static final int STATE_RSP_ARTICLES_ARTICLE_URL=1211;
        private static final int STATE_RSP_ARTICLES_ARTICLE_CONFIDENCE=1212;
        private static final int STATE_RSP_ARTICLES_ARTICLE_PUBLISHED_DATETIME=1213;
        private static final int STATE_RSP_ARTICLES_ARTICLE_TITLE=1214;
        private static final int STATE_RSP_ARTICLES_ARTICLE_ZEMIFIED=1215;

        private static final int STATE_RSP_MARKUP=13;
        private static final int STATE_RSP_MARKUP_TEXT=131;
        private static final int STATE_RSP_MARKUP_LINKS=132;
        private static final int STATE_RSP_MARKUP_LINKS_LINK=1321;
        private static final int STATE_RSP_MARKUP_LINKS_LINK_CONFIDENCE=13211;
        private static final int STATE_RSP_MARKUP_LINKS_LINK_ANCHOR=13212;
        private static final int STATE_RSP_MARKUP_LINKS_LINK_TARGET=13213;
        private static final int STATE_RSP_MARKUP_LINKS_LINK_TARGET_URL=132131;
        private static final int STATE_RSP_MARKUP_LINKS_LINK_TARGET_TYPE=132132;
        private static final int STATE_RSP_MARKUP_LINKS_LINK_TARGET_TITLE=132133;
        private static final int STATE_RSP_MARKUP_LINKS_LINK_RELEVANCE=13214;

        private static final int STATE_RSP_IMAGES=14;
        private static final int STATE_RSP_IMAGES_IMAGE=141;
        private static final int STATE_RSP_IMAGES_IMAGE_DESCRIPTION=1411;
        private static final int STATE_RSP_IMAGES_IMAGE_ATTRIBUTION=1412;
        private static final int STATE_RSP_IMAGES_IMAGE_LICENSE=1413;
        private static final int STATE_RSP_IMAGES_IMAGE_SOURCE_URL=1414;
        private static final int STATE_RSP_IMAGES_IMAGE_CONFIDENCE=1415;
        private static final int STATE_RSP_IMAGES_IMAGE_URL_S=1416;
        private static final int STATE_RSP_IMAGES_IMAGE_URL_S_W=14161;
        private static final int STATE_RSP_IMAGES_IMAGE_URL_S_H=14162;
        private static final int STATE_RSP_IMAGES_IMAGE_URL_M=1417;
        private static final int STATE_RSP_IMAGES_IMAGE_URL_M_W=14171;
        private static final int STATE_RSP_IMAGES_IMAGE_URL_M_H=14172;
        private static final int STATE_RSP_IMAGES_IMAGE_URL_L=1418;
        private static final int STATE_RSP_IMAGES_IMAGE_URL_L_W=14181;
        private static final int STATE_RSP_IMAGES_IMAGE_URL_L_H=14182;

        private static final int STATE_RSP_KEYWORDS=15;
        private static final int STATE_RSP_KEYWORDS_KEYWORD=151;
        private static final int STATE_RSP_KEYWORDS_KEYWORD_NAME=1511;
        private static final int STATE_RSP_KEYWORDS_KEYWORD_CONFIDENCE=1512;
        private static final int STATE_RSP_KEYWORDS_KEYWORD_SCHEMA=1513;

        private static final int STATE_RSP_CATEGORIES=16;
        private static final int STATE_RSP_CATEGORIES_CATEGORY=161;
        private static final int STATE_RSP_CATEGORIES_CATEGORY_NAME=1611;
        private static final int STATE_RSP_CATEGORIES_CATEGORY_CONFIDENCE=1612;
        private static final int STATE_RSP_CATEGORIES_CATEGORY_CATEGORIZATION=1613;

        private static final int STATE_RSP_SIGNATURE = 17;
        
        private static final int STATE_RSP_RID = 18;

        private int state=STATE_START;


        private String data_status = "";
        private String data_signature = "";
        private String data_rid = "";

        private String data_keyword_name = "";
        private String data_keyword_confidence = "";
        private String data_keyword_schema = "";

        private String data_category_name = "";
        private String data_category_confidence = "";
        private String data_category_categorization = "";

        private String data_link_anchor = "";
        private String data_link_confidence = "";
        private String data_link_target_url = "";
        private String data_link_target_type = "";
        private String data_link_target_title = "";
        private String data_link_relevance = "";

        private String data_article_url = "";
        private String data_article_title = "";
        private String data_article_published_datetime = "";
        private String data_article_confidence = "";
        private String data_article_zemified = "";


        private String data_image_url_l = "";
        private String data_image_url_m = "";
        private String data_image_url_s = "";

        private String data_image_url_l_w = "";
        private String data_image_url_m_w = "";
        private String data_image_url_s_w = "";

        private String data_image_url_l_h = "";
        private String data_image_url_m_h = "";
        private String data_image_url_s_h = "";

        private String data_image_source_url = "";
        private String data_image_license = "";
        private String data_image_description = "";
        private String data_image_attribution = "";
        private String data_image_confidence = "";


        public void startDocument() throws SAXException {
        }
        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if(parent.forceStop()){
                throw new SAXException("User interrupt");
            }
            switch(state){
                case STATE_START:
                    if(equalTags(qName, TAG_RSP)) {
                        state = STATE_RSP;
                    }
                    break;

                case STATE_RSP:
                    if(equalTags(qName, TAG_STATUS)) {
                        state = STATE_RSP_STATUS;
                        data_status = "";
                    }
                    else if(equalTags(qName, TAG_ARTICLES))  {
                        state = STATE_RSP_ARTICLES;
                    }
                    else if(equalTags(qName, TAG_KEYWORDS))  {
                        state = STATE_RSP_KEYWORDS;
                    }
                    else if(equalTags(qName, TAG_IMAGES))  {
                        state = STATE_RSP_IMAGES;
                    }
                    else if(equalTags(qName, TAG_MARKUP))  {
                        state = STATE_RSP_MARKUP;
                    }
                    else if(equalTags(qName, TAG_CATEGORIES))  {
                        state = STATE_RSP_CATEGORIES;
                    }
                    else if(equalTags(qName, TAG_SIGNATURE))  {
                        data_signature = "";
                        state = STATE_RSP_SIGNATURE;
                    }
                    else if(equalTags(qName, TAG_RID))  {
                        data_rid = "";
                        state = STATE_RSP_RID;
                    }
                    break;


                // ****** LIST LEVEL ******
                case STATE_RSP_ARTICLES: {
                    if(equalTags(qName, TAG_ARTICLE)) {
                        data_article_url = "";
                        data_article_title = "";
                        data_article_published_datetime = "";
                        data_article_confidence = "";
                        data_article_zemified = "";
                        state = STATE_RSP_ARTICLES_ARTICLE;
                    }
                    break;
                }
                case STATE_RSP_KEYWORDS: {
                    if(equalTags(qName, TAG_KEYWORD)) {
                        data_keyword_name = "";
                        data_keyword_confidence = "";
                        data_keyword_schema = "";
                        state = STATE_RSP_KEYWORDS_KEYWORD;
                    }
                    break;
                }
                case STATE_RSP_IMAGES: {
                    if(equalTags(qName, TAG_IMAGE)) {
                        data_image_url_l = "";
                        data_image_url_m = "";
                        data_image_url_s = "";

                        data_image_url_l_w = "";
                        data_image_url_m_w = "";
                        data_image_url_s_w = "";

                        data_image_url_l_h = "";
                        data_image_url_m_h = "";
                        data_image_url_s_h = "";

                        data_image_source_url = "";
                        data_image_license = "";
                        data_image_description = "";
                        data_image_attribution = "";
                        data_image_confidence = "";
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;
                }
                case STATE_RSP_CATEGORIES: {
                    if(equalTags(qName, TAG_CATEGORY)) {
                        data_category_name = "";
                        data_category_confidence = "";
                        data_category_categorization = "";
                        state = STATE_RSP_CATEGORIES_CATEGORY;
                    }
                    break;
                }

                // ****** MARKUP *******
                case STATE_RSP_MARKUP: {
                    if(equalTags(qName, TAG_TEXT)) {
                        state = STATE_RSP_MARKUP_TEXT;
                    }
                    else if(equalTags(qName, TAG_LINKS)) {
                        state = STATE_RSP_MARKUP_LINKS;
                    }
                    break;
                }


                // ****** ARTICLE *******
                case STATE_RSP_ARTICLES_ARTICLE: {
                    if(equalTags(qName, TAG_URL)) {
                        data_article_url = "";
                        state = STATE_RSP_ARTICLES_ARTICLE_URL;
                    }
                    else if(equalTags(qName, TAG_TITLE)) {
                        data_article_title = "";
                        state = STATE_RSP_ARTICLES_ARTICLE_TITLE;
                    }
                    else if(equalTags(qName, TAG_PUBLISHED_DATETIME)) {
                        data_article_published_datetime = "";
                        state = STATE_RSP_ARTICLES_ARTICLE_PUBLISHED_DATETIME;
                    }
                    else if(equalTags(qName, TAG_CONFIDENCE)) {
                        data_article_confidence = "";
                        state = STATE_RSP_ARTICLES_ARTICLE_CONFIDENCE;
                    }
                    else if(equalTags(qName, TAG_ZEMIFIED)) {
                        data_article_zemified = "";
                        state = STATE_RSP_ARTICLES_ARTICLE_ZEMIFIED;
                    }
                    break;
                }


                // ****** KEYWORD *******
                case STATE_RSP_KEYWORDS_KEYWORD: {
                    if(equalTags(qName, TAG_NAME)) {
                        data_keyword_name = "";
                        state = STATE_RSP_KEYWORDS_KEYWORD_NAME;
                    }
                    else if(equalTags(qName, TAG_CONFIDENCE)) {
                        data_keyword_confidence = "";
                        state = STATE_RSP_KEYWORDS_KEYWORD_CONFIDENCE;
                    }
                    else if(equalTags(qName, TAG_SCHEMA)) {
                        data_keyword_schema = "";
                        state = STATE_RSP_KEYWORDS_KEYWORD_SCHEMA;
                    }
                    break;
                }


                // ****** CATEGORY *******
                case STATE_RSP_CATEGORIES_CATEGORY: {
                    if(equalTags(qName, TAG_NAME)) {
                        data_category_name = "";
                        state = STATE_RSP_CATEGORIES_CATEGORY_NAME;
                    }
                    else if(equalTags(qName, TAG_CONFIDENCE)) {
                        data_category_confidence = "";
                        state = STATE_RSP_CATEGORIES_CATEGORY_CONFIDENCE;
                    }
                    else if(equalTags(qName, TAG_CATEGORIZATION)) {
                        data_category_categorization = "";
                        state = STATE_RSP_CATEGORIES_CATEGORY_CATEGORIZATION;
                    }
                    break;
                }

                // ****** IMAGE *******
                case STATE_RSP_IMAGES_IMAGE: {
                    if(equalTags(qName, TAG_URL_L)) {
                        data_image_url_l = "";
                        state = STATE_RSP_IMAGES_IMAGE_URL_L;
                    }
                    else if(equalTags(qName, TAG_URL_M)) {
                        data_image_url_m = "";
                        state = STATE_RSP_IMAGES_IMAGE_URL_M;
                    }
                    else if(equalTags(qName, TAG_URL_S)) {
                        data_image_url_s = "";
                        state = STATE_RSP_IMAGES_IMAGE_URL_S;
                    }
                    else if(equalTags(qName, TAG_URL_L_W)) {
                        data_image_url_l_w = "";
                        state = STATE_RSP_IMAGES_IMAGE_URL_L_W;
                    }
                    else if(equalTags(qName, TAG_URL_M_W)) {
                        data_image_url_m_w = "";
                        state = STATE_RSP_IMAGES_IMAGE_URL_M_W;
                    }
                    else if(equalTags(qName, TAG_URL_S_W)) {
                        data_image_url_s_w = "";
                        state = STATE_RSP_IMAGES_IMAGE_URL_S_W;
                    }
                    else if(equalTags(qName, TAG_URL_L_H)) {
                        data_image_url_l_h = "";
                        state = STATE_RSP_IMAGES_IMAGE_URL_L_H;
                    }
                    else if(equalTags(qName, TAG_URL_M_H)) {
                        data_image_url_m_h = "";
                        state = STATE_RSP_IMAGES_IMAGE_URL_M_H;
                    }
                    else if(equalTags(qName, TAG_URL_S_H)) {
                        data_image_url_s_h = "";
                        state = STATE_RSP_IMAGES_IMAGE_URL_S_H;
                    }
                    else if(equalTags(qName, TAG_SOURCE_URL)) {
                        data_image_source_url = "";
                        state = STATE_RSP_IMAGES_IMAGE_SOURCE_URL;
                    }
                    else if(equalTags(qName, TAG_LICENSE)) {
                        data_image_license = "";
                        state = STATE_RSP_IMAGES_IMAGE_LICENSE;
                    }
                    else if(equalTags(qName, TAG_DESCRIPTION)) {
                        data_image_description = "";
                        state = STATE_RSP_IMAGES_IMAGE_DESCRIPTION;
                    }
                    else if(equalTags(qName, TAG_ATTRIBUTION)) {
                        data_image_attribution = "";
                        state = STATE_RSP_IMAGES_IMAGE_ATTRIBUTION;
                    }
                    else if(equalTags(qName, TAG_CONFIDENCE)) {
                        data_image_confidence = "";
                        state = STATE_RSP_IMAGES_IMAGE_CONFIDENCE;
                    }
                    break;
                }


                // ****** MARKUP LINKS *******
                case STATE_RSP_MARKUP_LINKS: {
                    if(equalTags(qName, TAG_LINK)) {
                        data_link_anchor = "";
                        data_link_confidence = "";
                        data_link_target_url = "";
                        data_link_target_type = "";
                        data_link_target_title = "";
                        data_link_relevance="";
                        state = STATE_RSP_MARKUP_LINKS_LINK;
                    }
                    break;
                }
                case STATE_RSP_MARKUP_LINKS_LINK: {
                    if(equalTags(qName, TAG_ANCHOR)) {
                        data_link_anchor = "";
                        state = STATE_RSP_MARKUP_LINKS_LINK_ANCHOR;
                    }
                    else if(equalTags(qName, TAG_CONFIDENCE)) {
                        data_link_confidence = "";
                        state = STATE_RSP_MARKUP_LINKS_LINK_CONFIDENCE;
                    }
                    else if(equalTags(qName, TAG_TARGET)) {
                        data_link_target_url = "";
                        data_link_target_type = "";
                        data_link_target_title = "";
                        state = STATE_RSP_MARKUP_LINKS_LINK_TARGET;
                    }
                    else if(equalTags(qName, TAG_RELEVANCE)) {
                        data_link_relevance = "";
                        state = STATE_RSP_MARKUP_LINKS_LINK_RELEVANCE;
                    }
                    break;
                }
                case STATE_RSP_MARKUP_LINKS_LINK_TARGET: {
                    if(equalTags(qName, TAG_URL)) {
                        data_link_target_url = "";
                        state = STATE_RSP_MARKUP_LINKS_LINK_TARGET_URL;
                    }
                    else if(equalTags(qName, TAG_TYPE)) {
                        data_link_target_type = "";
                        state = STATE_RSP_MARKUP_LINKS_LINK_TARGET_TYPE;
                    }
                    else if(equalTags(qName, TAG_TITLE)) {
                        data_link_target_title = "";
                        state = STATE_RSP_MARKUP_LINKS_LINK_TARGET_TITLE;
                    }
                    break;
                }
            }
        }







        public void endElement(String uri, String localName, String qName) throws SAXException {
            // System.out.println("   "+state);
            switch(state) {
                case STATE_RSP:
                    if(equalTags(qName, TAG_RSP)) {
                        state = STATE_START;
                    }
                    break;

                // ****** TOP LEVEL *******
                case STATE_RSP_STATUS:
                    if(equalTags(qName, TAG_STATUS)) {
                        if(data_status != null && data_status.length() > 0) {
                            log("Zemanta says: "+data_status);
                        }
                        state = STATE_RSP;
                    }
                    break;
                case STATE_RSP_SIGNATURE:
                    if(equalTags(qName, TAG_SIGNATURE)) {
                        state = STATE_RSP;
                    }
                    break;
                case STATE_RSP_RID:
                    if(equalTags(qName, TAG_RID)) {
                        state = STATE_RSP;
                    }
                    break;
                    
                case STATE_RSP_ARTICLES:
                    if(equalTags(qName, TAG_ARTICLES)) {
                        state = STATE_RSP;
                    }
                    break;

               case STATE_RSP_KEYWORDS:
                    if(equalTags(qName, TAG_KEYWORDS)) {
                        state = STATE_RSP;
                    }
                    break;

               case STATE_RSP_IMAGES:
                    if(equalTags(qName, TAG_IMAGES))  {
                        state = STATE_RSP;
                    }
                    break;

               case STATE_RSP_MARKUP:
                    if(equalTags(qName, TAG_MARKUP))  {
                        state = STATE_RSP;
                    }
                    break;

               case STATE_RSP_CATEGORIES:
                    if(equalTags(qName, TAG_CATEGORIES))  {
                        state = STATE_RSP;
                    }
                    break;


                // ****** MARKUP LINKS *******

                case STATE_RSP_MARKUP_LINKS_LINK: {
                    if(equalTags(qName, TAG_LINK)) {
                        if(masterTopic != null) {
                            try {
                                if(isValid(data_link_target_url)) {

                                    Topic urlTopic = getLinkUrlTopic(data_link_target_url, tm);

                                    if(isValid(data_link_target_type)) {
                                        Topic typeTopic = getLinkTypeTopic(data_link_target_type, tm);
                                        urlTopic.addType(typeTopic);
                                    }
                                    if(isValid(data_link_target_title)) {
                                        urlTopic.setBaseName(data_link_target_title);
                                    }
                                    
                                    Topic at = getLinkType(tm);
                                    Topic documentType = getDocumentType(tm);
                                    Topic urlType = getLinkUrlType(tm);
                                    
                                    Association a = tm.createAssociation(at);

                                    a.addPlayer(masterTopic, documentType);
                                    a.addPlayer(urlTopic, urlType);

                                    if(isValid(data_link_anchor)) {
                                        Topic anchorType = getLinkAnchorType(tm);
                                        Topic anchorTopic = getLinkAnchorTopic(data_link_anchor, tm);
                                        a.addPlayer(anchorTopic, anchorType);
                                    }
                                    if(isValid(data_link_confidence)) {
                                        Topic confidenceType = getConfidenceType(tm);
                                        Topic confidenceTopic = getConfidenceTopic(data_link_confidence, tm);
                                        a.addPlayer(confidenceTopic, confidenceType);
                                    }
                                    if(isValid(data_link_relevance)) {
                                        Topic relevanceType = getRelevanceType(tm);
                                        Topic relevanceTopic = getRelevanceTopic(data_link_relevance, tm);
                                        a.addPlayer(relevanceTopic, relevanceType);
                                    }
                                    if(false && isValid(data_link_target_type)) {
                                        Topic typeType = getLinkTypeType(tm);
                                        Topic typeTopic = getLinkTypeTopic(data_link_target_type, tm);
                                        a.addPlayer(typeTopic, typeType);
                                    }
                                    if(false && isValid(data_link_target_title)) {
                                        Topic titleType = getLinkTitleType(tm);
                                        Topic titleTopic = getLinkTitleTopic(data_link_target_title, tm);
                                        a.addPlayer(titleTopic, titleType);
                                    }
                                }
                            }
                            catch (Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_RSP_MARKUP_LINKS;
                    }
                    break;
                }
                case STATE_RSP_MARKUP_LINKS_LINK_ANCHOR: {
                    if(equalTags(qName, TAG_ANCHOR)) {
                        state = STATE_RSP_MARKUP_LINKS_LINK;
                    }
                    break;
                }
                case STATE_RSP_MARKUP_LINKS_LINK_CONFIDENCE: {
                    if(equalTags(qName, TAG_CONFIDENCE)) {
                        state = STATE_RSP_MARKUP_LINKS_LINK;
                    }
                    break;
                }
                case STATE_RSP_MARKUP_LINKS_LINK_TARGET: {
                    if(equalTags(qName, TAG_TARGET)) {
                        state = STATE_RSP_MARKUP_LINKS_LINK;
                    }
                    break;
                }
                case STATE_RSP_MARKUP_LINKS_LINK_TARGET_URL: {
                    if(equalTags(qName, TAG_URL)) {
                        state = STATE_RSP_MARKUP_LINKS_LINK_TARGET;
                    }
                    break;
                }
                case STATE_RSP_MARKUP_LINKS_LINK_TARGET_TYPE: {
                    if(equalTags(qName, TAG_TYPE)) {
                        state = STATE_RSP_MARKUP_LINKS_LINK_TARGET;
                    }
                    break;
                }
                case STATE_RSP_MARKUP_LINKS_LINK_TARGET_TITLE: {
                    if(equalTags(qName, TAG_TITLE)) {
                        state = STATE_RSP_MARKUP_LINKS_LINK_TARGET;
                    }
                    break;
                }
                case STATE_RSP_MARKUP_LINKS_LINK_RELEVANCE: {
                    if(equalTags(qName, TAG_RELEVANCE)) {
                        state = STATE_RSP_MARKUP_LINKS_LINK;
                    }
                    break;
                }



                // ****** IMAGE *******
                case STATE_RSP_IMAGES_IMAGE_URL_L: {
                    if(equalTags(qName, TAG_URL_L)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;
                }
                case STATE_RSP_IMAGES_IMAGE_URL_M:
                    if(equalTags(qName, TAG_URL_M)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_URL_S:
                    if(equalTags(qName, TAG_URL_S)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_URL_L_W:
                    if(equalTags(qName, TAG_URL_L_W)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_URL_M_W:
                    if(equalTags(qName, TAG_URL_M_W)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_URL_S_W:
                    if(equalTags(qName, TAG_URL_S_W)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_URL_L_H:
                    if(equalTags(qName, TAG_URL_L_H)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_URL_M_H:
                    if(equalTags(qName, TAG_URL_M_H)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_URL_S_H:
                    if(equalTags(qName, TAG_URL_S_H)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_SOURCE_URL:
                    if(equalTags(qName, TAG_SOURCE_URL)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_LICENSE:
                    if(equalTags(qName, TAG_LICENSE)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_DESCRIPTION:
                    if(equalTags(qName, TAG_DESCRIPTION)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_ATTRIBUTION:
                    if(equalTags(qName, TAG_ATTRIBUTION)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;

                case STATE_RSP_IMAGES_IMAGE_CONFIDENCE:
                    if(equalTags(qName, TAG_CONFIDENCE)) {
                        state = STATE_RSP_IMAGES_IMAGE;
                    }
                    break;



                // ****** KEYWORD *******
                case STATE_RSP_KEYWORDS_KEYWORD_NAME:
                    if(equalTags(qName, TAG_NAME)) {
                        state = STATE_RSP_KEYWORDS_KEYWORD;
                    }
                    break;

                case STATE_RSP_KEYWORDS_KEYWORD_CONFIDENCE:
                    if(equalTags(qName, TAG_CONFIDENCE)) {
                        state = STATE_RSP_KEYWORDS_KEYWORD;
                    }
                    break;

                case STATE_RSP_KEYWORDS_KEYWORD_SCHEMA:
                    if(equalTags(qName, TAG_SCHEMA)) {
                        state = STATE_RSP_KEYWORDS_KEYWORD;
                    }
                    break;


                // ****** CATEGORY *******
                case STATE_RSP_CATEGORIES_CATEGORY_NAME:
                    if(equalTags(qName, TAG_NAME)) {
                        state = STATE_RSP_CATEGORIES_CATEGORY;
                    }
                    break;

                case STATE_RSP_CATEGORIES_CATEGORY_CONFIDENCE:
                    if(equalTags(qName, TAG_CONFIDENCE)) {
                        state = STATE_RSP_CATEGORIES_CATEGORY;
                    }
                    break;

                case STATE_RSP_CATEGORIES_CATEGORY_CATEGORIZATION:
                    if(equalTags(qName, TAG_CATEGORIZATION)) {
                        state = STATE_RSP_CATEGORIES_CATEGORY;
                    }
                    break;



                // ****** MARKUP *******
                case STATE_RSP_MARKUP_TEXT:
                    if(equalTags(qName, TAG_TEXT)) {
                        state = STATE_RSP_MARKUP;
                    }
                    break;

                case STATE_RSP_MARKUP_LINKS:
                    if(equalTags(qName, TAG_LINKS)) {
                        state = STATE_RSP_MARKUP;
                    }
                    break;



                // ****** ARTICLE *******
                case STATE_RSP_ARTICLES_ARTICLE_URL:
                    if(equalTags(qName, TAG_URL)) {
                        state = STATE_RSP_ARTICLES_ARTICLE;
                    }
                    break;

                case STATE_RSP_ARTICLES_ARTICLE_TITLE:
                    if(equalTags(qName, TAG_TITLE)) {
                        state = STATE_RSP_ARTICLES_ARTICLE;
                    }
                    break;

                case STATE_RSP_ARTICLES_ARTICLE_PUBLISHED_DATETIME:
                    if(equalTags(qName, TAG_PUBLISHED_DATETIME)) {
                        state = STATE_RSP_ARTICLES_ARTICLE;
                    }
                    break;

                case STATE_RSP_ARTICLES_ARTICLE_CONFIDENCE:
                    if(equalTags(qName, TAG_CONFIDENCE)) {
                        state = STATE_RSP_ARTICLES_ARTICLE;
                    }
                    break;

                case STATE_RSP_ARTICLES_ARTICLE_ZEMIFIED:
                    if(equalTags(qName, TAG_ZEMIFIED)) {
                        state = STATE_RSP_ARTICLES_ARTICLE;
                    }
                    break;


                // BACK TO LIST LEVEL

                case STATE_RSP_ARTICLES_ARTICLE: {
                    if(equalTags(qName, TAG_ARTICLE)) {
                        if(masterTopic != null) {
                            try {
                                if(isValid(data_article_url)) {
                                    articleCount++;

                                    Topic documentType = getDocumentType(tm);
                                    Topic articleType = getArticleType(tm);
                                    Topic articleTopic = getArticleTopic(data_article_url, tm);

                                    if(isValid(data_article_title)) {
                                        articleTopic.setBaseName(data_article_title);
                                    }
                                    if(isValid(data_article_published_datetime)) {
                                        Topic dateType = getPublishedDateType(tm);
                                        articleTopic.setData(dateType, getDefaultLangType(tm), data_article_published_datetime);
                                    }

                                    Association a = tm.createAssociation(articleType);

                                    a.addPlayer(masterTopic, documentType);
                                    a.addPlayer(articleTopic, articleType);

                                    if(false && isValid(data_article_title)) {
                                        Topic titleType = getArticleTitleType(tm);
                                        Topic titleTopic = getArticleTitleTopic(data_article_title, tm);
                                        a.addPlayer(titleTopic, titleType);
                                    }
                                    if(false && isValid(data_article_published_datetime)) {
                                        Topic dateType = getDateType(tm);
                                        Topic dateTopic = getDateTopic(data_article_published_datetime, tm);
                                        a.addPlayer(dateTopic, dateType);
                                    }
                                    if(isValid(data_article_confidence)) {
                                        Topic confidenceType = getConfidenceType(tm);
                                        Topic confidenceTopic = getConfidenceTopic(data_article_confidence, tm);
                                        a.addPlayer(confidenceTopic, confidenceType);
                                    }
                                    if(false && isValid(data_article_zemified)) {
                                        Topic zemifiedType = getZemifiedType(tm);
                                        Topic zemifiedTopic = getZemifiedTopic(data_article_zemified, tm);
                                        a.addPlayer(zemifiedTopic, zemifiedType);
                                    }
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_RSP_ARTICLES;
                    }
                    break;
                }
                case STATE_RSP_KEYWORDS_KEYWORD: {
                    if(equalTags(qName, TAG_KEYWORD)) {
                        if(masterTopic != null) {
                            try {
                                Topic keywordTopic = null;
                                Topic confidenceTopic = null;
                                Topic schemaTopic = null;
                                if(isValid(data_keyword_name)) {
                                    keywordCount++;
                                    keywordTopic = getKeywordTopic(data_keyword_name, tm);
                                    if(data_keyword_confidence != null && data_keyword_confidence.length()>0) {
                                        confidenceTopic = getConfidenceTopic(data_keyword_confidence, tm);
                                    }
                                    if(data_keyword_schema != null && data_keyword_schema.length()>0) {
                                        schemaTopic = getSchemaTopic(data_keyword_schema, tm);
                                    }

                                    Topic keywordType = getKeywordType(tm);
                                    Topic documentType = getDocumentType(tm);

                                    Association a = tm.createAssociation(keywordType);
                                    a.addPlayer(keywordTopic, keywordType);
                                    a.addPlayer(masterTopic, documentType);

                                    if(confidenceTopic != null) {
                                        Topic confidenceType = getConfidenceType(tm);
                                        a.addPlayer(confidenceTopic, confidenceType);
                                    }
                                    if(schemaTopic != null) {
                                        Topic schemaType = getSchemaType(tm);
                                        a.addPlayer(schemaTopic, schemaType);
                                    }
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_RSP_KEYWORDS;
                    }
                    break;
                }
                case STATE_RSP_IMAGES_IMAGE: {
                    if(equalTags(qName, TAG_IMAGE)) {
                        if(masterTopic != null) {
                            try {
                                if(isValid(data_image_source_url)) {
                                    imageCount++;

                                    Topic documentType = getDocumentType(tm);
                                    Topic imageType = getImageType(tm);
                                    Topic imageTopic = getImageTopic(data_image_source_url, tm);

                                    Association a = tm.createAssociation(imageType);

                                    a.addPlayer(masterTopic, documentType);
                                    a.addPlayer(imageTopic, imageType);

                                    if(isValid(data_image_confidence)) {
                                        Topic confidenceType = getConfidenceType(tm);
                                        Topic confidenceTopic = getConfidenceTopic(data_image_confidence, tm);
                                        a.addPlayer(confidenceTopic, confidenceType);
                                    }
                                    if(isValid(data_image_url_l)) {
                                        Topic imageUrlType = getLargeImageType(tm);
                                        imageTopic.setData(imageUrlType, getDefaultLangType(tm), data_image_url_l);
                                    }
                                    if(isValid(data_image_url_m)) {
                                        Topic imageUrlType = getMediumImageType(tm);
                                        imageTopic.setData(imageUrlType, getDefaultLangType(tm), data_image_url_m);
                                    }
                                    if(isValid(data_image_url_s)) {
                                        Topic imageUrlType = getSmallImageType(tm);
                                        imageTopic.setData(imageUrlType, getDefaultLangType(tm), data_image_url_s);
                                    }

                                    /* UNUSED
                                    data_image_url_l_w = "";
                                    data_image_url_m_w = "";
                                    data_image_url_s_w = "";

                                    data_image_url_l_h = "";
                                    data_image_url_m_h = "";
                                    data_image_url_s_h = "";
                                    */

                                    if(isValid(data_image_description)) {
                                        Topic imageDescriptionType = getImageDescriptionType(tm);
                                        imageTopic.setData(imageDescriptionType, getDefaultLangType(tm), data_image_description);
                                    }
                                    if(isValid(data_image_license)) {
                                        Topic imageDescriptionType = getImageLicenseType(tm);
                                        imageTopic.setData(imageDescriptionType, getDefaultLangType(tm), data_image_license);
                                    }
                                    if(isValid(data_image_attribution)) {
                                        Topic imageAttributionType = getImageAttributionType(tm);
                                        imageTopic.setData(imageAttributionType, getDefaultLangType(tm), data_image_attribution);
                                    }
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_RSP_IMAGES;
                    }
                    break;
                }
                case STATE_RSP_CATEGORIES_CATEGORY: {
                    if(equalTags(qName, TAG_CATEGORY)) {
                        if(masterTopic != null) {
                            try {
                                Topic categoryTopic = null;
                                Topic confidenceTopic = null;
                                Topic categorizationTopic = null;
                                if(isValid(data_category_name)) {
                                    categoryCount++;
                                    categoryTopic = getCategoryTopic(data_category_name, tm);
                                    if(data_category_confidence != null && data_category_confidence.length()>0) {
                                        confidenceTopic = getConfidenceTopic(data_keyword_confidence, tm);
                                    }
                                    if(data_category_categorization != null && data_category_categorization.length()>0) {
                                        categorizationTopic = getCategorizationTopic(data_category_categorization, tm);
                                    }

                                    Topic categoryType = getKeywordType(tm);
                                    Topic documentType = getDocumentType(tm);

                                    Association a = tm.createAssociation(categoryType);
                                    a.addPlayer(categoryTopic, categoryType);
                                    a.addPlayer(masterTopic, documentType);

                                    if(confidenceTopic != null) {
                                        Topic confidenceType = getConfidenceType(tm);
                                        a.addPlayer(confidenceTopic, confidenceType);
                                    }
                                    if(categorizationTopic != null) {
                                        Topic categorizationType = getCategorizationType(tm);
                                        a.addPlayer(categorizationTopic, categorizationType);
                                    }
                                }
                            }
                            catch(Exception e) {
                                log(e);
                            }
                        }
                        state = STATE_RSP_CATEGORIES;
                    }
                    break;
                }
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            String str = new String(ch,start,length);
            switch(state){
                case STATE_RSP_STATUS:
                    data_status+=str;
                    break;
                case STATE_RSP_SIGNATURE:
                    data_signature+=str;
                    break;
                case STATE_RSP_RID:
                    data_rid+=str;
                    break;


                case STATE_RSP_KEYWORDS_KEYWORD_NAME:
                    data_keyword_name += str;
                    break;
                case STATE_RSP_KEYWORDS_KEYWORD_CONFIDENCE:
                    data_keyword_confidence += str;
                    break;
                case STATE_RSP_KEYWORDS_KEYWORD_SCHEMA:
                    data_keyword_schema += str;
                    break;


                case STATE_RSP_CATEGORIES_CATEGORY_NAME:
                    data_category_name += str;
                    break;
                case STATE_RSP_CATEGORIES_CATEGORY_CONFIDENCE:
                    data_category_confidence += str;
                    break;
                case STATE_RSP_CATEGORIES_CATEGORY_CATEGORIZATION:
                    data_category_categorization += str;
                    break;

                case STATE_RSP_MARKUP_LINKS_LINK_ANCHOR:
                    data_link_anchor += str;
                    break;
                case STATE_RSP_MARKUP_LINKS_LINK_CONFIDENCE:
                    data_link_confidence += str;
                    break;
                case STATE_RSP_MARKUP_LINKS_LINK_TARGET_URL:
                    data_link_target_url += str;
                    break;
                case STATE_RSP_MARKUP_LINKS_LINK_TARGET_TYPE:
                    data_link_target_type += str;
                    break;
                case STATE_RSP_MARKUP_LINKS_LINK_TARGET_TITLE:
                    data_link_target_title += str;
                    break;
                case STATE_RSP_MARKUP_LINKS_LINK_RELEVANCE:
                    data_link_relevance += str;
                    break;


                case STATE_RSP_ARTICLES_ARTICLE_URL:
                    data_article_url += str;
                    break;
                case STATE_RSP_ARTICLES_ARTICLE_TITLE:
                    data_article_title += str;
                    break;
                case STATE_RSP_ARTICLES_ARTICLE_PUBLISHED_DATETIME:
                    data_article_published_datetime += str;
                    break;
                case STATE_RSP_ARTICLES_ARTICLE_CONFIDENCE:
                    data_article_confidence += str;
                    break;
                case STATE_RSP_ARTICLES_ARTICLE_ZEMIFIED:
                    data_article_zemified += str;
                    break;


                case STATE_RSP_IMAGES_IMAGE_URL_L:
                    data_image_url_l += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_URL_M:
                    data_image_url_m += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_URL_S:
                    data_image_url_s += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_URL_L_W:
                    data_image_url_l_w += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_URL_M_W:
                    data_image_url_m_w += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_URL_S_W:
                    data_image_url_s_w += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_URL_L_H:
                    data_image_url_l_h += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_URL_M_H:
                    data_image_url_m_h += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_URL_S_H:
                    data_image_url_s_h += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_SOURCE_URL:
                    data_image_source_url += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_LICENSE:
                    data_image_license += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_DESCRIPTION:
                    data_image_description += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_ATTRIBUTION:
                    data_image_attribution += str;
                    break;
                case STATE_RSP_IMAGES_IMAGE_CONFIDENCE:
                    data_image_confidence += str;
                    break;
            }
        }

        public void warning(SAXParseException exception) throws SAXException {
        }

        public void error(SAXParseException exception) throws SAXException {
            parent.log("Error parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            parent.log("Fatal error parsing XML document at "+exception.getLineNumber()+","+exception.getColumnNumber(),exception);
        }


        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
        public void processingInstruction(String target, String data) throws SAXException {}
        public void startPrefixMapping(String prefix, String uri) throws SAXException {}
        public void endPrefixMapping(String prefix) throws SAXException {}
        public void setDocumentLocator(org.xml.sax.Locator locator) {}
        public void skippedEntity(String name) throws SAXException {}



        public boolean equalTags(String t1, String t2) {
            if(t1 != null && t2 != null) {
                return t1.equals(t2);
            }
            return false;
        }

        public boolean isValid(String str) {
            if(str != null && str.length() > 0) return true;
            return false;
        }

    }


}
