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
 */


package org.wandora.application.tools.extractors.uclassify;

/**
 *
 * @author akivela
 */

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
import org.wandora.utils.*;
import org.wandora.application.gui.*;
import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.wandora.application.tools.browserextractors.BrowserExtractRequest;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;





public abstract class AbstractUClassifier extends AbstractExtractor {
    
    
    private String REQUEST_TEMPLATE = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"+
        "<uclassify xmlns=\"http://api.uclassify.com/1/RequestSchema\" version=\"1.01\">"+
          "<texts>"+
            "<textBase64 id=\"UnknownText1\">__REQUEST_DATA__</textBase64>"+
          "</texts>"+
          "<readCalls readApiKey=\"__API_KEY__\">"+
            "<classify id=\"Classify\" username=\"__PUBLISHED_USER__\" classifierName=\"__PUBLISHED_CLASSIFIER__\" textId=\"UnknownText1\"/>"+
          "</readCalls>"+
        "</uclassify>";
    
    
    
    
    protected static final String API_URL = "http://api.uclassify.com";
    
    protected String defaultEncoding = "UTF-8";
    
    public static final String SOURCE_SI = "http://wandora.org/si/source";
    public static final String DOCUMENT_SI = "http://wandora.org/si/document";
    public static final String TOPIC_SI = "http://wandora.org/si/topic";

    public static final String UCLASSIFY_SI = "http://www.uclassify.com";

    public static final String UCLASSIFY_CLASSIFIER_SI = "http://wandora.org/si/uclassify/classifier";
    public static final String UCLASSIFY_CLASSIFIER_TYPE_SI = "http://wandora.org/si/uclassify/classifier-type";
    
    public static final String UCLASSIFY_CLASS_SI = "http://wandora.org/si/uclassify/term";
    public static final String UCLASSIFY_CLASS_TYPE_SI = "http://wandora.org/si/uclassify/term-type";
    
    public static final String UCLASSIFY_PROBABILITY_SI = "http://wandora.org/si/uclassify/probability";
    public static final String UCLASSIFY_PROBABILITY_TYPE_SI = "http://wandora.org/si/uclassify/probability-type";
    
    

    @Override
    public Icon getIcon() {
        return UIBox.getIcon("gui/icons/extract_uclassify.png");
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
        return false;
    }
    

    // -------------------------------------------------------------------------

    

    @Override
    public String doBrowserExtract(BrowserExtractRequest request, Wandora wandora) throws TopicMapException {
        setWandora(wandora);
        return ExtractHelper.doBrowserExtractForClassifiers(this, request, wandora, defaultEncoding);
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



    // ******** TOPIC MAPS *********


    public Topic getUClassifierType(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, UCLASSIFY_CLASSIFIER_TYPE_SI, "uClassify classifier");
        makeSubclassOf(tm, t, getUClassifyClass(tm));
        return t;
    }
    
    
    

    public Topic getUClassifierTopic(String uclassifier, TopicMap tm) throws TopicMapException {
        if(uclassifier != null) {
            uclassifier = uclassifier.trim();
            if(uclassifier.length() > 0) {
                Topic uTopic=getOrCreateTopic(tm, UCLASSIFY_CLASSIFIER_TYPE_SI+"/"+encode(uclassifier), uclassifier);
                Topic classifierType = getUClassifierType(tm);
                uTopic.addType(classifierType);
                return uTopic;
            }
        }
        return null;
    }

    
    
    
    
    public Topic getUClassType(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, UCLASSIFY_CLASS_TYPE_SI, "uClassify class");
        makeSubclassOf(tm, t, getUClassifyClass(tm));
        return t;
    }
    
    
    

    public Topic getUClassTopic(String uclass, TopicMap tm) throws TopicMapException {
        if(uclass != null) {
            uclass = uclass.trim();
            if(uclass.length() > 0) {
                Topic classTopic=getOrCreateTopic(tm, UCLASSIFY_CLASS_SI+"/"+encode(uclass), uclass);
                Topic classType = getUClassType(tm);
                classTopic.addType(classType);
                return classTopic;
            }
        }
        return null;
    }

    

    
    public Topic getUProbabilityType(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, UCLASSIFY_PROBABILITY_TYPE_SI, "uClassifier probability");
        makeSubclassOf(tm, t, getUClassifyClass(tm));
        return t;
    }


    public Topic getUProbabilityTopic(String uprobability, TopicMap tm) throws TopicMapException {
        if(uprobability != null) {
            uprobability = uprobability.trim();
            if(uprobability.length() > 0) {
                Topic uTopic=getOrCreateTopic(tm, UCLASSIFY_PROBABILITY_SI+"/"+encode(uprobability), uprobability);
                Topic probabilityType = getUProbabilityType(tm);
                uTopic.addType(probabilityType);
                return uTopic;
            }
        }
        return null;
    }
    
    
    
    public Topic getUClassifyClass(TopicMap tm) throws TopicMapException {
        Topic t = getOrCreateTopic(tm, UCLASSIFY_SI, "uClassify");
        makeSubclassOf(tm, t, getWandoraClass(tm));
        //t.addType(getWandoraClass(tm));
        return t;
    }

    public Topic getWandoraClass(TopicMap tm) throws TopicMapException {
        return getOrCreateTopic(tm, TMBox.WANDORACLASS_SI, "Wandora class");
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
        return getOrCreateTopic(tm, si,null);
    }


    protected Topic getOrCreateTopic(TopicMap tm, String si,String bn) throws TopicMapException {
        return ExtractHelper.getOrCreateTopic(si, bn, tm);
    }


    protected void makeSubclassOf(TopicMap tm, Topic t, Topic superclass) throws TopicMapException {
        ExtractHelper.makeSubclassOf(t, superclass, tm);
    }


    // -------------------------------------------------------------------------
    
    
    public String encode(String str) {
        try {
            str = URLEncoder.encode(str, defaultEncoding);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return str;
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

    
    
    // -------------------------------------------------------------------------
    
    
    public String uClassify(String data, String classifier, String classifierOwner, double thresholdProbability, TopicMap tm) {
        try {
            String requestData = REQUEST_TEMPLATE;
            String apikey = solveAPIKey();
            if(apikey != null) {
                requestData = requestData.replace("__API_KEY__", apikey);
                requestData = requestData.replace("__REQUEST_DATA__", Base64.encodeBytes(data.getBytes()));
                requestData = requestData.replace("__PUBLISHED_USER__", classifierOwner);
                requestData = requestData.replace("__PUBLISHED_CLASSIFIER__", classifier);

                System.out.println("Sending: "+requestData);

                String result = sendRequest(new URL(API_URL), requestData, "text/xml; charset=utf-8", "POST");

                System.out.println("uClassifier returned == "+result);

                javax.xml.parsers.SAXParserFactory factory=javax.xml.parsers.SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                javax.xml.parsers.SAXParser parser=factory.newSAXParser();
                XMLReader reader=parser.getXMLReader();

                UClassifyParser parserHandler = new UClassifyParser(getMasterSubject(), data, classifier, thresholdProbability, tm, this);

                reader.setContentHandler(parserHandler);
                reader.setErrorHandler(parserHandler);
                try {
                    reader.parse(new InputSource(new StringReader(result)));
                }
                catch(Exception e){
                    if(!(e instanceof SAXException) || !e.getMessage().equals("User interrupt")) log(e);
                }
                if(getCurrentLogger() != null) 
                    log("Total " + parserHandler.progress + " classes found by uClassify");
            }
            else {
                log("No Apikey available. Aborting.");
            }
        }
        catch(Exception e) {
            log(e);
        }
        return null;
    }
    
    
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





    private static String apikey = null; // "t0ckynnsXWB0WLHQCuLx6m8IOs";
    public String solveAPIKey(Wandora wandora) {
        setWandora(wandora);
        return solveAPIKey();
    }
    public String solveAPIKey() {
        if(apikey == null) {
            apikey = "";
            apikey = WandoraOptionPane.showInputDialog(getWandora(), "Please give valid apikey for uClassify. You can register your apikey at http://www.uclassify.com/", apikey, "uClassify apikey", WandoraOptionPane.QUESTION_MESSAGE);
        }
        return apikey;
    }



    
    public void forgetAuthorization() {
        apikey = null;
    }


    
    
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    
    


    public class UClassifyParser implements org.xml.sax.ContentHandler, org.xml.sax.ErrorHandler {

        private int totalClassesFound = 0;
        private int acceptedClassesFound = 0;

        private double thresholdProbability = 0.0;
        private String classifier = null;
        private Topic masterTopic = null;
        public int progress=0;
        private TopicMap tm;
        private AbstractUClassifier parent;


        public UClassifyParser(String term, String data, String c, double tp, TopicMap tm, AbstractUClassifier parent){
            this.tm=tm;
            this.parent=parent;
            this.classifier = c;
            this.thresholdProbability = tp;

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



        public static final String TAG_UCLASSIFY="uclassify";
        public static final String TAG_STATUS="status";
        public static final String TAG_READCALLS="readCalls";
        public static final String TAG_CLASSIFY="classify";
        public static final String TAG_CLASSIFICATION="classification";
        public static final String TAG_CLASS="class";
        

        private static final int STATE_START=0;
        private static final int STATE_UCLASSIFY=1;
        private static final int STATE_UCLASSIFY_READCALLS=11;
        private static final int STATE_UCLASSIFY_READCALLS_CLASSIFY=111;
        private static final int STATE_UCLASSIFY_READCALLS_CLASSIFY_CLASSIFICATION=1111;
        private static final int STATE_UCLASSIFY_READCALLS_CLASSIFY_CLASSIFICATION_CLASS=11111;
        
        private static final int STATE_UCLASSIFY_STATUS=12;

        
       
        private int state=STATE_START;

        
        private String classifyId = null;
        private String textCoverage = null;
        private String className = null;
        private String p = null;
        private String statusText = "";
        
        

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
                    if(qName.equals(TAG_UCLASSIFY)) {
                        state = STATE_UCLASSIFY;
                    }
                    break;
                case STATE_UCLASSIFY:
                    if(qName.equals(TAG_STATUS)) {
                        state = STATE_UCLASSIFY_STATUS;
                        String statusCode = atts.getValue("statusCode");
                        String success = atts.getValue("success");
                        statusText = "";
                        if(!"true".equalsIgnoreCase(success)) {
                            parent.log("Warning: uClassify returned error code "+statusCode);
                        }
                    }
                    else if(qName.equals(TAG_READCALLS)) {
                        state = STATE_UCLASSIFY_READCALLS;
                    }
                    break;

                case STATE_UCLASSIFY_READCALLS:
                    if(qName.equals(TAG_CLASSIFY)) {
                        classifyId = atts.getValue("id");
                        state = STATE_UCLASSIFY_READCALLS_CLASSIFY;
                    }
                    break;

                case STATE_UCLASSIFY_READCALLS_CLASSIFY:
                    if(qName.equals(TAG_CLASSIFICATION)) {
                        textCoverage = atts.getValue("textCoverage");
                        state = STATE_UCLASSIFY_READCALLS_CLASSIFY_CLASSIFICATION;
                    }
                    break;
                    
                case STATE_UCLASSIFY_READCALLS_CLASSIFY_CLASSIFICATION:
                    if(qName.equals(TAG_CLASS)) {
                        className = atts.getValue("className");
                        p = atts.getValue("p");
                        
                        if(className != null && className.length() > 0) {
                            totalClassesFound++;
                            try {
                                if(parent.getCurrentLogger() != null) parent.log("uClassify found class '"+className+"'" + (p != null ? " with probability "+p+"." : "."));
                                double dp = 1.0;
                                if(p != null && p.length() > 0) {
                                    try {
                                        dp = Double.parseDouble(p);
                                    }
                                    catch(Exception e) {
                                        // PASS SILENTLY
                                    }
                                }
                                if(dp > thresholdProbability) {
                                    Topic uClassTopic = parent.getUClassTopic(className, tm);
                                    if(masterTopic != null && uClassTopic != null) {
                                        Topic uClassType = parent.getUClassType(tm);
                                        Association a = tm.createAssociation(uClassType);
                                        a.addPlayer(masterTopic, parent.getTopicType(tm));
                                        a.addPlayer(uClassTopic, uClassType);
                                        if(p != null && p.length() > 0) {
                                            Topic uProbabilityType = parent.getUProbabilityType(tm);
                                            Topic uProbabilityTopic = parent.getUProbabilityTopic(p, tm);
                                            a.addPlayer(uProbabilityTopic, uProbabilityType);
                                        }
                                        if(classifier != null && classifier.length() > 0) {
                                            Topic uClassifierType = parent.getUClassifierType(tm);
                                            Topic uClassifierTopic = parent.getUClassifierTopic(classifier, tm);
                                            a.addPlayer(uClassifierTopic, uClassifierType);
                                        }
                                        acceptedClassesFound++;
                                    }
                                }
                            }
                            catch(Exception e) {
                                parent.log(e);
                            }
                        }
                        
                        state = STATE_UCLASSIFY_READCALLS_CLASSIFY_CLASSIFICATION_CLASS;
                    }
                    break;
            }
        }







        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(state) {
                case STATE_UCLASSIFY_READCALLS_CLASSIFY_CLASSIFICATION_CLASS:
                    if(qName.equals(TAG_CLASS)) {
                        parent.setProgress( progress++ );
                        state = STATE_UCLASSIFY_READCALLS_CLASSIFY_CLASSIFICATION;
                    }
                    break;

                    
                case STATE_UCLASSIFY_READCALLS_CLASSIFY_CLASSIFICATION:
                    if(qName.equals(TAG_CLASSIFICATION)) {
                        state = STATE_UCLASSIFY_READCALLS_CLASSIFY;
                    }
                    break;


                    
                case STATE_UCLASSIFY_READCALLS_CLASSIFY:
                    if(qName.equals(TAG_CLASSIFY)) {
                        state = STATE_UCLASSIFY_READCALLS;
                    }
                    break;


                case STATE_UCLASSIFY_READCALLS:
                    if(qName.equals(TAG_READCALLS)) {
                        state = STATE_UCLASSIFY;
                    }
                    break;

                case STATE_UCLASSIFY_STATUS:
                    if(qName.equals(TAG_STATUS)) {
                        if(statusText != null && statusText.length() > 0) {
                            parent.log(statusText);
                        }
                        state = STATE_UCLASSIFY;
                    }
                    break;

                case STATE_UCLASSIFY:
                    if(qName.equals(TAG_UCLASSIFY)) {
                        state = STATE_START;
                    }
                    break;
            }
        }






        public void characters(char[] ch, int start, int length) throws SAXException {
            switch(state) {
                case STATE_UCLASSIFY_STATUS: {
                    statusText += new String(ch,start,length);
                }
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

    }


    
    
}
