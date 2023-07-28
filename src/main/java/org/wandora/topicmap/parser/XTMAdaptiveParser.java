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
 */


package org.wandora.topicmap.parser;



import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapLogger;
import org.xml.sax.*;


/**
 *
 * @author olli
 */
public class XTMAdaptiveParser implements ContentHandler, ErrorHandler {

    protected ContentHandler xtm1parser;
    protected ContentHandler xtm2parser;
    protected ErrorHandler xtm1errorHandler;
    protected ErrorHandler xtm2errorHandler;
    
    protected ContentHandler selectedContentHandler;
    protected ErrorHandler selectedErrorHandler;

    protected TopicMapLogger logger;
    
    public XTMAdaptiveParser(TopicMap tm,TopicMapLogger logger,ContentHandler xtm1parser){
        this.xtm1parser=xtm1parser;
        this.xtm1errorHandler=(ErrorHandler)xtm1parser;
        this.xtm2parser=new XTMParser2(tm,logger);
        this.xtm2errorHandler=(ErrorHandler)xtm2parser;
        this.logger=logger;
    }
    
    public void selectXTM2(){
        logger.log("Using XTM 2.0 parser");
        selectedContentHandler=xtm2parser;
        selectedErrorHandler=xtm2errorHandler;        
    }
    public void selectXTM1(){
        logger.log("Using XTM 1.0 parser");
        selectedContentHandler=xtm1parser;
        selectedErrorHandler=xtm1errorHandler;
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(selectedContentHandler!=null) selectedContentHandler.characters(ch,start,length);
    }

    @Override
    public void endDocument() throws SAXException {
        if(selectedContentHandler!=null) selectedContentHandler.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(selectedContentHandler!=null) selectedContentHandler.endElement(uri,localName,qName);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if(selectedContentHandler!=null) selectedContentHandler.endPrefixMapping(prefix);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if(selectedContentHandler!=null) selectedContentHandler.ignorableWhitespace(ch,start,length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if(selectedContentHandler!=null) selectedContentHandler.processingInstruction(target,data);
    }

    @Override
    public void setDocumentLocator(org.xml.sax.Locator locator) {
        if(selectedContentHandler!=null) selectedContentHandler.setDocumentLocator(locator);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        if(selectedContentHandler!=null) selectedContentHandler.skippedEntity(name);
    }

    @Override
    public void startDocument() throws SAXException {
        if(selectedContentHandler!=null) selectedContentHandler.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if(selectedContentHandler==null && qName.equals("topicMap")){
            String version=atts.getValue("version");
            if(version!=null && version.startsWith("2")) selectXTM2();
            else selectXTM1();
        }
        if(selectedContentHandler!=null) selectedContentHandler.startElement(uri,localName,qName,atts);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
/*        if(selectedContentHandler==null){
            if(prefix==null || prefix.length()==0){
                if(uri.equals("http://www.topicmaps.org/xtm/")) selectXTM2();
                else if(uri.equals("http://www.topicmaps.org/xtm/1.0")) selectXTM1();
            }
        }*/
        if(selectedContentHandler!=null) selectedContentHandler.startPrefixMapping(prefix,uri);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        if(selectedErrorHandler!=null) selectedErrorHandler.error(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        if(selectedErrorHandler!=null) selectedErrorHandler.fatalError(exception);
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        if(selectedErrorHandler!=null) selectedErrorHandler.warning(exception);
    }

}
