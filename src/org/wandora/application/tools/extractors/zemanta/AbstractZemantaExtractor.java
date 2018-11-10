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
 *
 */



package org.wandora.application.tools.extractors.zemanta;



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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;



/**
 *
 * @author akivela
 */
public abstract class AbstractZemantaExtractor extends AbstractExtractor {

	private static final long serialVersionUID = 1L;


	protected boolean EXTRACT_RELEVANCE = true;

    
    protected String defaultEncoding = "UTF-8";

    // Default language of occurrences and variant names.
    public static String LANG = "en";

    public static final String ZEMANTA_URL = "http://api.zemanta.com/services/rest/0.0/";


    public static final String SOURCE_SI = "http://wandora.org/si/source";
    public static final String DOCUMENT_SI = "http://wandora.org/si/document";
    public static final String TOPIC_SI = "http://wandora.org/si/topic";

    public static final String ZEMANTA_SI = "http://api.zemanta.com";

    
    public static final String ZEMANTA_IMAGE_SI = ZEMANTA_SI+"/image";
    public static final String ZEMANTA_LARGE_IMAGE_SI = ZEMANTA_SI+"/image-large";
    public static final String ZEMANTA_MEDIUM_IMAGE_SI = ZEMANTA_SI+"/image-medium";
    public static final String ZEMANTA_SMALL_IMAGE_SI = ZEMANTA_SI+"/image-small";

    public static final String ZEMANTA_IMAGE_DESCRIPTION_SI = ZEMANTA_SI+"/image-description";
    public static final String ZEMANTA_IMAGE_ATTRIBUTION_SI = ZEMANTA_SI+"/image-attribution";
    public static final String ZEMANTA_IMAGE_LICENSE_SI = ZEMANTA_SI+"/image-license";

    public static final String ZEMANTA_KEYWORD_SI = ZEMANTA_SI+"/keyword";
    public static final String ZEMANTA_CATEGORY_SI = ZEMANTA_SI+"/category";
    public static final String ZEMANTA_CATEGORIZATION_SI = ZEMANTA_SI+"/categorization";
    public static final String ZEMANTA_CONFIDENCE_SI = ZEMANTA_SI+"/confidence";
    public static final String ZEMANTA_RELEVANCE_SI = ZEMANTA_SI+"/relevance";
    public static final String ZEMANTA_SCHEMA_SI = ZEMANTA_SI+"/schema";

    public static final String ZEMANTA_LINK_SI = ZEMANTA_SI+"/link";
    public static final String ZEMANTA_LINK_ANCHOR_SI = ZEMANTA_SI+"/link-anchor";
    public static final String ZEMANTA_LINK_URL_SI = ZEMANTA_SI+"/link-url";
    public static final String ZEMANTA_LINK_TYPE_SI = ZEMANTA_SI+"/link-type";
    public static final String ZEMANTA_LINK_TITLE_SI = ZEMANTA_SI+"/link-title";

    public static final String ZEMANTA_ARTICLE_SI = ZEMANTA_SI+"/article";
    public static final String ZEMANTA_ARTICLE_TITLE_SI = ZEMANTA_SI+"/article-title";
    public static final String ZEMANTA_DATE_SI = ZEMANTA_SI+"/date";
    public static final String ZEMANTA_PUBLISHED_DATE_SI = ZEMANTA_SI+"/published-date";
    public static final String ZEMANTA_ZEMIFIED_SI = ZEMANTA_SI+"/zemified";






    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_zemanta.png");
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



    /* ---------------------------------------------------------------------- */
    /* ---------------------------------------------------------------------- */



    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        setWandora(wandora);
        return ExtractHelper.doBrowserExtractForClassifiers(this, request, wandora, defaultEncoding);
    }


    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    


    public static String sendRequest(URL url, String data, String ctype, String method) throws IOException {
        StringBuilder sb = new StringBuilder(5000);
        if (url != null) {
            URLConnection con = url.openConnection();
            Wandora.initUrlConnection(con);
            con.setDoInput(true);
            con.setUseCaches(false);

            if(method != null && con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).setRequestMethod(method);
                //System.out.println("****** Setting HTTP request method to "+method);
            }

            if(ctype != null) {
                con.setRequestProperty("Content-type", ctype);
            }

            if(data != null && data.length() > 0) {
                con.setRequestProperty("Content-length", data.length() + "");
                con.setDoOutput(true);
                PrintWriter out = new PrintWriter(con.getOutputStream());
                out.print(data);
                out.flush();
                out.close();
            }
//            DataInputStream in = new DataInputStream(con.getInputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String s;
            while ((s = in.readLine()) != null) {
                sb.append(s);
                if(!(s.endsWith("\n") || s.endsWith("\r"))) sb.append("\n");
            }
            in.close();
        }
        return sb.toString();
    }









    // -------------------------------------------------------------------------


    
    @Override
    public boolean isConfigurable(){
        return true;
    }
    @Override
    public void configure(Wandora admin,org.wandora.utils.Options options,String prefix) throws TopicMapException {
        ZemantaExtractorConfiguration dialog=new ZemantaExtractorConfiguration(admin,options,this);
        dialog.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
    }


    // -------------------------------------------------------------------------



    
    public abstract boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception;






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
                Topic contentType = createTopic(topicMap, "document-text");
                setData(textTopic, contentType, "en", trimmedText);
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
            log(e);
        }
    }



    // ******** TOPIC MAPS *****************************************************



    public Topic getKeywordTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_KEYWORD_SI, getKeywordType(tm), tm);
    }





    public Topic getCategoryTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_CATEGORY_SI, getCategoryType(tm), tm);
    }



    public Topic getConfidenceTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_CONFIDENCE_SI, getConfidenceType(tm), tm);
    }
    public Topic getRelevanceTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_RELEVANCE_SI, getRelevanceType(tm), tm);
    }


    public Topic getSchemaTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_SCHEMA_SI, getSchemaType(tm), tm);
    }


    public Topic getCategorizationTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_CATEGORIZATION_SI, getSchemaType(tm), tm);
    }

    public Topic getLinkUrlTopic(String url, TopicMap tm) throws TopicMapException {
        return getUTopic(url, getLinkUrlType(tm), tm);
    }


    public Topic getLinkAnchorTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_LINK_ANCHOR_SI, getLinkAnchorType(tm), tm);
    }

    public Topic getLinkTypeTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_LINK_TYPE_SI, getLinkTypeType(tm), tm);
    }

    public Topic getLinkTitleTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_LINK_TITLE_SI, getLinkTitleType(tm), tm);
    }

    public Topic getZemifiedTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_ZEMIFIED_SI, getZemifiedType(tm), tm);
    }

    public Topic getDateTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_DATE_SI, getDateType(tm), tm);
    }


    
    public Topic getArticleTopic(String url, TopicMap tm) throws TopicMapException {
        return getUTopic(url, getArticleType(tm), tm);
    }


    public Topic getArticleTitleTopic(String str, TopicMap tm) throws TopicMapException {
        return getATopic(str, ZEMANTA_ARTICLE_TITLE_SI, getArticleTitleType(tm), tm);
    }

    public Topic getImageTopic(String url, TopicMap tm) throws TopicMapException {
        return getUTopic(url, getImageType(tm), tm);
    }

    
    private Topic getATopic(String str, String si, Topic type, TopicMap tm) throws TopicMapException {
        if(str != null && si != null) {
            str = str.trim();
            if(str.length() > 0) {
                Topic topic=getOrCreateTopic(tm, si+"/"+urlEncode(str), str);
                if(type != null) topic.addType(type);
                return topic;
            }
        }
        return null;
    }

    private Topic getUTopic(String si, Topic type, TopicMap tm) throws TopicMapException {
        if(si != null) {
            si = si.trim();
            if(si.length() > 0) {
                Topic topic=getOrCreateTopic(tm, si, null);
                topic.setSubjectLocator(new Locator(si));
                if(type != null) topic.addType(type);
                return topic;
            }
        }
        return null;
    }


    // ------


    public Topic getImageType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_IMAGE_SI, "Zemanta Image", getZemantaType(tm));
    }

    public Topic getLargeImageType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_LARGE_IMAGE_SI, "Zemanta Large Image", getZemantaType(tm));
    }
    public Topic getMediumImageType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_MEDIUM_IMAGE_SI, "Zemanta Medium Image", getZemantaType(tm));
    }
    public Topic getSmallImageType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_SMALL_IMAGE_SI, "Zemanta Small Image", getZemantaType(tm));
    }
    public Topic getDefaultLangType(TopicMap tm) throws TopicMapException {
        return TMBox.getLangTopic(getWandoraClass(tm), LANG);
    }
    
    public Topic getImageDescriptionType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_IMAGE_DESCRIPTION_SI, "Zemanta Image Description", getZemantaType(tm));
    }
    public Topic getImageLicenseType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_IMAGE_LICENSE_SI, "Zemanta Image License", getZemantaType(tm));
    }
    public Topic getImageAttributionType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_IMAGE_ATTRIBUTION_SI, "Zemanta Image Attribution", getZemantaType(tm));
    }





    public Topic getArticleType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_ARTICLE_SI, "Zemanta Article", getZemantaType(tm));
    }

    public Topic getArticleTitleType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_ARTICLE_TITLE_SI, "Zemanta Article Title", getZemantaType(tm));
    }

    public Topic getDateType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_DATE_SI, "Zemanta Date", getZemantaType(tm));
    }
    public Topic getPublishedDateType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_PUBLISHED_DATE_SI, "Zemanta Published Date", getZemantaType(tm));
    }

    public Topic getZemifiedType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_ZEMIFIED_SI, "Zemified", getZemantaType(tm));
    }





    public Topic getLinkType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_LINK_SI, "Zemanta Link", getZemantaType(tm));
    }

    public Topic getLinkTitleType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_LINK_TITLE_SI, "Zemanta Link Target Title", getZemantaType(tm));
    }

    public Topic getLinkTypeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_LINK_TYPE_SI, "Zemanta Link Target Type", getZemantaType(tm));
    }

    public Topic getLinkAnchorType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_LINK_ANCHOR_SI, "Zemanta Link Anchor", getZemantaType(tm));
    }

    public Topic getLinkUrlType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_LINK_URL_SI, "Zemanta Link Url", getZemantaType(tm));
    }

    // ------
    public Topic getRelevanceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_RELEVANCE_SI, "Zemanta Relevance", getZemantaType(tm));
    }
    public Topic getConfidenceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_CONFIDENCE_SI, "Zemanta Confidence", getZemantaType(tm));
    }


    public Topic getSchemaType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_SCHEMA_SI, "Zemanta Schema", getZemantaType(tm));
    }

    public Topic getCategorizationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_CATEGORIZATION_SI, "Zemanta Categorization", getZemantaType(tm));
    }

    public Topic getKeywordType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_KEYWORD_SI, "Zemanta Keyword", getZemantaType(tm));
    }


    public Topic getCategoryType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ZEMANTA_CATEGORY_SI, "Zemanta Category", getZemantaType(tm));
    }


    // ----
    
    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI,"Wandora class");
    }
    
    public Topic getTopicType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TOPIC_SI, "Topic");
    }

    public Topic getSourceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SOURCE_SI, "Source");
    }

    public Topic getZemantaType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, ZEMANTA_SI, "Zemanta");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }

    public Topic getDocumentType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, DOCUMENT_SI, "Document");
        Topic wandoraClass = getWandoraClass(tm);
        makeSubclassOf(tm, type, wandoraClass);
        return type;
    }

    // --------

    protected Topic getOrCreateTopic(TopicMap tm, String si) throws TopicMapException {
        return getOrCreateTopic(tm, si,null);
    }

    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn) throws TopicMapException {
        return getOrCreateTopic(tm, si, bn, null);
    }

    protected Topic getOrCreateTopic(TopicMap tm, String si, String bn, Topic type) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, type, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }



    // -------------------------------------------------------------------------





    private static String apikey = null; // "8efp9qgq8eqnks4b5jx9cbvw";
    public String solveAPIKey(Wandora wandora) {
        setWandora(wandora);
        return solveAPIKey();
    }
    public String solveAPIKey() {
        if(apikey == null) {
            apikey = "";
            apikey = WandoraOptionPane.showInputDialog(getWandora(), "Please give valid apikey for Zemanta. You can register your apikey at http://www.zemanta.com/", apikey, "Zemanta apikey", WandoraOptionPane.QUESTION_MESSAGE);
        }
        return apikey;
    }



    
    public void forgetAuthorization() {
        apikey = null;
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
