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
 *
 * AbstractAlchemyExtractor.java
 *
 *
 */



package org.wandora.application.tools.extractors.alchemy;




import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.wandora.application.tools.browserextractors.BrowserPluginExtractor;
import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import org.wandora.utils.*;



/**
 * This abstract class is used as a basis for Alchemy extractors. Alchemy
 * extractors usually take a text fragment and somehow analyzes it using
 * Alchemy API's web service. Associations and topics will be created using the
 * results.
 *
 * @author akivela
 */
public abstract class AbstractAlchemyExtractor extends AbstractExtractor {

    protected boolean EXTRACT_RELEVANCE = true;

    
    protected String defaultEncoding = "UTF-8";

    // Default language of occurrences and variant names.
    public static String LANG = "en";

    public static final String ALCHEMY_URL = "http://access.alchemyapi.com/";


    public static final String SOURCE_SI = "http://wandora.org/si/source";
    public static final String DOCUMENT_SI = "http://wandora.org/si/document";
    public static final String IMAGE_SI = "http://wandora.org/si/image";
    public static final String TOPIC_SI = "http://wandora.org/si/topic";

    public static final String ALCHEMY_SI = "http://www.alchemyapi.com";

    public static final String ALCHEMY_ENTITY_SI = "http://wandora.org/si/alchemyapi/entity";
    public static final String ALCHEMY_ENTITY_TYPE_SI = "http://wandora.org/si/alchemyapi/entity/type";
    public static final String ALCHEMY_ENTITY_RELEVANCE_SI = "http://wandora.org/si/alchemyapi/entity/relevance";

    public static final String ALCHEMY_IMAGE_KEYWORD_SI = "http://wandora.org/si/alchemyapi/image-keyword";
    public static final String ALCHEMY_KEYWORD_SI = "http://wandora.org/si/alchemyapi/keyword";
    public static final String ALCHEMY_CATEGORY_SI = "http://wandora.org/si/alchemyapi/category";
    public static final String ALCHEMY_CATEGORY_SCORE_SI = "http://wandora.org/si/alchemyapi/category-score";
    public static final String ALCHEMY_LANGUAGE_SI = "http://wandora.org/si/alchemyapi/language";

    public static final String ALCHEMY_SENTIMENT_TYPE_SI = "http://wandora.org/si/alchemyapi/sentiment-type";
    public static final String ALCHEMY_SENTIMENT_SCORE_SI = "http://wandora.org/si/alchemyapi/sentiment-score";

    public static final String AAPI_SCHEMA_BASE = "http://rdf.alchemyapi.com/rdf/v1/s/aapi-schema.rdf";
    public static final String SUBTYPE_SI = AAPI_SCHEMA_BASE+"#SubType";
    public static final String GEO_SI = AAPI_SCHEMA_BASE+"#Geo";
    public static final String SAME_AS_SI = "http://wandora.org/si/alchemyapi/sameAs";

    public static final String ALCHEMY_SCORE_SI = "http://wandora.org/si/alchemyapi/score";
    public static final String ALCHEMY_SCORE_TYPE_SI = "http://wandora.org/si/alchemyapi/score/type";

    public static final String ALCHEMY_SUBJECT_SI = "http://wandora.org/si/alchemyapi/subject";
    public static final String ALCHEMY_ACTION_SI = "http://wandora.org/si/alchemyapi/action";
    public static final String ALCHEMY_OBJECT_SI = "http://wandora.org/si/alchemyapi/object";
    public static final String ALCHEMY_RELATION_SI = "http://wandora.org/si/alchemyapi/relation";
    
    public static final String ALCHEMY_VERB_SI = "http://wandora.org/si/alchemyapi/verb";
    public static final String ALCHEMY_TENSE_SI = "http://wandora.org/si/alchemyapi/tense";
    
    

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_alchemy.png");
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
        AlchemyExtractorConfiguration dialog=new AlchemyExtractorConfiguration(admin,options,this);
        dialog.setVisible(true);
    }
    @Override
    public void writeOptions(Wandora admin,org.wandora.utils.Options options,String prefix){
    }


    // -------------------------------------------------------------------------



    
    public abstract boolean _extractTopicsFrom(InputStream in, TopicMap topicMap) throws Exception;




    /* ---------------------------------------------------------------------- */
    /* ---------------------------------------------------------------------- */



    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        setWandora(wandora);
        return ExtractHelper.doBrowserExtractForClassifiers(this, request, wandora, defaultEncoding);
    }


    /* ---------------------------------------------------------------------- */




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
    
    
    public void fillImageTopic(Topic imageTopic, TopicMap topicMap, BufferedImage image) {
        try {
            /*
            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            String content = Base64.encodeBytes(pixels);
            if(content != null && content.length() > 0) {
                Topic contentType = createTopic(topicMap, "document-text");
                setData(textTopic, contentType, "en", content);
            }
            */
            // imageTopic.setBaseName("Image-"+System.currentTimeMillis());
            Topic imageType = getImageType(topicMap);
            imageTopic.addType(imageType);
        }
        catch(Exception e) {
            log(e);
        }
    }


    // ******** TOPIC MAPS *********

    public void createSameAsAssociationWith(String sameAsIdentifier, Topic topic, Topic topicType, TopicMap tm) {
        try {
            Topic sameAsTopic = getOrCreateTopic(tm, sameAsIdentifier);
            if(sameAsTopic != null) {
                Topic sameAsType = getSameAsType(tm);
                Association saa = tm.createAssociation(sameAsType);
                saa.addPlayer(topic, topicType);
                saa.addPlayer(sameAsTopic, sameAsType);
            }
        }
        catch(Exception e) {
            log(e);
        }
    }

    public Topic getEntityTypeTopic(String entityType, TopicMap tm) throws TopicMapException {
        if(entityType != null) {
            entityType = entityType.trim();
            if(entityType.length() > 0) {
                Topic entityTypeTopic=getOrCreateTopic(tm, ALCHEMY_ENTITY_TYPE_SI+"/"+urlEncode(entityType), entityType);
                Topic entityTypeTypeTopic = getEntityType(tm);
                entityTypeTopic.addType(entityTypeTypeTopic);
                return entityTypeTopic;
            }
        }
        return null;
    }




    public Topic getEntityTopic(String entity, TopicMap tm) throws TopicMapException {
        if(entity != null) {
            entity = entity.trim();
            if(entity.length() > 0) {
                Topic entityTopic=getOrCreateTopic(tm, ALCHEMY_ENTITY_SI+"/"+urlEncode(entity), entity);
                Topic entityTypeTopic = getEntityType(tm);
                entityTopic.addType(entityTypeTopic);
                return entityTopic;
            }
        }
        return null;
    }


    public Topic getKeywordTopic(String keyword, TopicMap tm) throws TopicMapException {
        if(keyword != null) {
            keyword = keyword.trim();
            if(keyword.length() > 0) {
                Topic keywordTopic = getOrCreateTopic(tm, ALCHEMY_KEYWORD_SI+"/"+urlEncode(keyword), keyword);
                Topic keywordTypeTopic = getKeywordType(tm);
                keywordTopic.addType(keywordTypeTopic);
                return keywordTopic;
            }
        }
        return null;
    }


    public Topic getCategoryTopic(String category, TopicMap tm) throws TopicMapException {
        if(category != null) {
            category = category.trim();
            if(category.length() > 0) {
                Topic categoryTopic = getOrCreateTopic(tm, ALCHEMY_CATEGORY_SI+"/"+urlEncode(category), category);
                Topic categoryTypeTopic = getCategoryType(tm);
                categoryTopic.addType(categoryTypeTopic);
                return categoryTopic;
            }
        }
        return null;
    }

    public Topic getCategoryScoreTopic(String categoryScore, TopicMap tm) throws TopicMapException {
        if(categoryScore != null) {
            categoryScore = categoryScore.trim();
            if(categoryScore.length() > 0) {
                Topic categoryScoreTopic = getOrCreateTopic(tm, ALCHEMY_CATEGORY_SCORE_SI+"/"+urlEncode(categoryScore), categoryScore);
                Topic categoryScoreTypeTopic = getCategoryScoreType(tm);
                categoryScoreTopic.addType(categoryScoreTypeTopic);
                return categoryScoreTopic;
            }
        }
        return null;
    }


    public Topic getLanguageTopic(String language, TopicMap tm) throws TopicMapException {
        if(language != null) {
            language = language.trim();
            if(language.length() > 0) {
                Topic languageTopic = getOrCreateTopic(tm, ALCHEMY_LANGUAGE_SI+"/"+urlEncode(language), language);
                Topic languageTypeTopic = getLanguageType(tm);
                languageTopic.addType(languageTypeTopic);
                return languageTopic;
            }
        }
        return null;
    }

    public Topic getRelevanceTopic(String relevance, TopicMap tm) throws TopicMapException {
        if(relevance != null) {
            relevance = relevance.trim(); 
            if(relevance.length() > 0) {
                Topic relevanceTopic=getOrCreateTopic(tm, ALCHEMY_ENTITY_RELEVANCE_SI+"/"+urlEncode(relevance), relevance);
                Topic relevanceTypeTopic = getRelevanceType(tm);
                relevanceTopic.addType(relevanceTypeTopic);
                return relevanceTopic;
            }
        }
        return null;
    }

    public Topic getSentimentTypeTopic(String str, TopicMap tm) throws TopicMapException {
        if(str != null) {
            str = str.trim();
            if(str.length() > 0) {
                Topic t=getOrCreateTopic(tm, ALCHEMY_SENTIMENT_TYPE_SI+"/"+urlEncode(str), str);
                Topic ty = getSentimentTypeType(tm);
                t.addType(ty);
                return t;
            }
        }
        return null;
    }
    public Topic getSentimentScoreTopic(String str, TopicMap tm) throws TopicMapException {
        if(str != null) {
            str = str.trim(); 
            if(str.length() > 0) {
                Topic t=getOrCreateTopic(tm, ALCHEMY_SENTIMENT_SCORE_SI+"/"+urlEncode(str), str);
                Topic ty = getSentimentScoreType(tm);
                t.addType(ty);
                return t;
            }
        }
        return null;
    }
    
    public Topic getScoreTopic(String str, TopicMap tm) throws TopicMapException {
        if(str != null) {
            str = str.trim(); 
            if(str.length() > 0) {
                Topic t=getOrCreateTopic(tm, ALCHEMY_SCORE_SI+"/"+urlEncode(str), str);
                Topic ty = getScoreType(tm);
                t.addType(ty);
                return t;
            }
        }
        return null;
    }

    // --- Relations ---
    
    public Topic getSubjectTopic(String subject, TopicMap tm) throws TopicMapException {
        if(subject != null) {
            subject = subject.trim();
            if(subject.length() > 0) {
                Topic subjectTopic=getOrCreateTopic(tm, ALCHEMY_SUBJECT_SI+"/"+urlEncode(subject), subject);
                Topic subjectTypeTopic = getSubjectType(tm);
                subjectTopic.addType(subjectTypeTopic);
                return subjectTopic;
            }
        }
        return null;
    }
    public Topic getActionTopic(String action, TopicMap tm) throws TopicMapException {
        if(action != null) {
            action = action.trim();
            if(action.length() > 0) {
                Topic actionTopic=getOrCreateTopic(tm, ALCHEMY_ACTION_SI+"/"+urlEncode(action), action);
                Topic actionTypeTopic = getActionType(tm);
                actionTopic.addType(actionTypeTopic);
                return actionTopic;
            }
        }
        return null;
    }
    public Topic getObjectTopic(String object, TopicMap tm) throws TopicMapException {
        if(object != null) {
            object = object.trim();
            if(object.length() > 0) {
                Topic objectTopic=getOrCreateTopic(tm, ALCHEMY_OBJECT_SI+"/"+urlEncode(object), object);
                Topic objectTypeTopic = getObjectType(tm);
                objectTopic.addType(objectTypeTopic);
                return objectTopic;
            }
        }
        return null;
    }
    public Topic getVerbTopic(String verb, TopicMap tm) throws TopicMapException {
        if(verb != null) {
            verb = verb.trim();
            if(verb.length() > 0) {
                Topic verbTopic=getOrCreateTopic(tm, ALCHEMY_VERB_SI+"/"+urlEncode(verb), verb);
                Topic verbTypeTopic = getVerbType(tm);
                verbTopic.addType(verbTypeTopic);
                return verbTopic;
            }
        }
        return null;
    }
    public Topic getTenseTopic(String tense, TopicMap tm) throws TopicMapException {
        if(tense != null) {
            tense = tense.trim();
            if(tense.length() > 0) {
                Topic tenseTopic=getOrCreateTopic(tm, ALCHEMY_TENSE_SI+"/"+urlEncode(tense), tense);
                Topic tenseTypeTopic = getTenseType(tm);
                tenseTopic.addType(tenseTypeTopic);
                return tenseTopic;
            }
        }
        return null;
    }
    
    // --- Types ---
    
    public Topic getRelevanceType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_ENTITY_RELEVANCE_SI, "Alchemy Entity Relevance", getAlchemyType(tm));
    }

    public Topic getEntityTypeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_ENTITY_TYPE_SI, "Alchemy Entity Type", getAlchemyType(tm));
    }

    public Topic getEntityType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_ENTITY_SI, "Alchemy Entity", getAlchemyType(tm));
    }
    
    public Topic getSubjectType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_SUBJECT_SI, "Alchemy Subject", getAlchemyType(tm));
    }
    public Topic getActionType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_ACTION_SI, "Alchemy Action", getAlchemyType(tm));
    }
    public Topic getObjectType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_OBJECT_SI, "Alchemy Object", getAlchemyType(tm));
    }
    public Topic getRelationType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_RELATION_SI, "Alchemy Relation", getAlchemyType(tm));
    }
    public Topic getVerbType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_VERB_SI, "Alchemy Verb", getAlchemyType(tm));
    }
    public Topic getTenseType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_TENSE_SI, "Alchemy Tense", getAlchemyType(tm));
    }
    
    public Topic getKeywordType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_KEYWORD_SI, "Alchemy Keyword", getAlchemyType(tm));
    }
    
    public Topic getImageKeywordType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_IMAGE_KEYWORD_SI, "Alchemy Image Keyword", getAlchemyType(tm));
    }

    public Topic getScoreType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_SCORE_SI, "Alchemy Score", getAlchemyType(tm));
    }
    
    public Topic getCategoryType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_CATEGORY_SI, "Alchemy Category", getAlchemyType(tm));
    }

    public Topic getLanguageType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_LANGUAGE_SI, "Alchemy Language", getAlchemyType(tm));
    }

    public Topic getCategoryScoreType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_CATEGORY_SCORE_SI, "Alchemy Category Score", getAlchemyType(tm));
    }

    public Topic getSentimentTypeType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_SENTIMENT_TYPE_SI, "Alchemy Sentiment Type", getAlchemyType(tm));
    }
    
    public Topic getSentimentScoreType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, ALCHEMY_SENTIMENT_SCORE_SI, "Alchemy Sentiment Score", getAlchemyType(tm));
    }

    public Topic getSameAsType(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, SAME_AS_SI, "Same as", getAlchemyType(tm));
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

    public Topic getAlchemyType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, ALCHEMY_SI, "Alchemy");
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
    
    public Topic getImageType(TopicMap tm) throws TopicMapException {
        Topic type = getOrCreateTopic(tm, IMAGE_SI, "Image");
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





    private static String apikey = null;
    public String solveAPIKey(Wandora wandora) {
        setWandora(wandora);
        return solveAPIKey();
    }
    public String solveAPIKey() {
        if(apikey == null) {
            apikey = "";
            apikey = WandoraOptionPane.showInputDialog(getWandora(), "Please give valid apikey for AlchemyAPI. You can register your apikey at http://www.alchemyapi.com/", apikey, "Alchemy apikey", WandoraOptionPane.QUESTION_MESSAGE);
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
