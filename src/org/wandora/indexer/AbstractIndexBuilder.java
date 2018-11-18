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
 *
 * AbstractIndexBuilder.java
 *
 * Created on 8. elokuuta 2006, 16:07
 *
 */

package org.wandora.indexer;


import java.util.*;
import java.io.*;
import org.wandora.topicmap.*;
import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.wandora.application.WandoraToolLogger;

import org.wandora.piccolo.*;

/**
 *
 * @author olli
 */
public abstract class AbstractIndexBuilder {

    
    protected Map<String,Extractor> extractors;
    protected Logger logger;
    protected WandoraToolLogger toolLogger;
    
    
    /** Creates new AbstractIndexBuilder */
    public AbstractIndexBuilder() {
        this(new SimpleLogger());
    }
    public AbstractIndexBuilder(Logger logger) {
        this.logger=logger;
        extractors=new LinkedHashMap<>();
    }
    public AbstractIndexBuilder(WandoraToolLogger logger) {
        this.toolLogger=logger;
        extractors=new LinkedHashMap<>();
    }
    
    
    public void addExtractor(Extractor e){
        String[] cts=e.getContentTypes();
        for(int i=0;i<cts.length;i++){
            extractors.put(cts[i],e);
        }
    }
    
    
        
    public Analyzer getWriterAnalyzer(){
//        PerFieldAnalyzerWrapper analyzer=new PerFieldAnalyzerWrapper(new StandardAnalyzer());
        PerFieldAnalyzerWrapper analyzer=new PerFieldAnalyzerWrapper(new Analyzer(){
            public TokenStream tokenStream(String fieldName,Reader reader){
                return new LowerCaseFilter(new StopFilter(
//                new LetterTokenizer(reader)
                new CharTokenizer(reader){
                    protected boolean isTokenChar(char c){
                        if(Character.isLetter(c) || Character.isDigit(c)) return true;
                        if(c==':' || c=='-') return true;
                        else return false;
                    }
                    protected char normalize(char c){
                        if(c==':' || c=='-') return c;                        
                        else return super.normalize(c);
                    }
                }
                ,StandardAnalyzer.STOP_WORDS));
            }
        });
        analyzer.addAnalyzer("topic",new NewlineAnalyzer());
        return analyzer;
    }
    
/*    public void processTopicMap(String tm,String index) throws IOException {
        processTopicMap(WandoraManager.readTopicMap(tm),index);
    }
    public void processTopicMap(String tm,IndexWriter writer) throws IOException{
        WandoraManager manager=new WandoraManager();
        processTopicMap(WandoraManager.readTopicMap(tm),writer);
    }*/
    
    public void processTopicMap(TopicMap tm,String index) throws IOException {
        IndexWriter writer = null;
        Analyzer analyzer=null;
        File indexf = null;
        try {
            analyzer=getWriterAnalyzer();
            indexf = new File(index);
            writer=new IndexWriter(indexf,analyzer,true);
            log("Indexing topic map!");
            processTopicMap(tm,writer);
            log("Optimizing index!");
            writer.optimize();
            log("Indexing done!");
        }
        catch (Exception e) {
            log("Exception with analyzer '" + analyzer + "'.");
            log("Exception with topicmap '" + tm + "'.");
            log("Exception with search index '" + index + "'.");
            log("Exception with search index file '" + indexf + "'.");
            log("Exception with search writer '" + writer + "'.");
            log(e);
        }
        
        try {
            writer.close();
        }
        catch (Exception e) {
            log(e);
        }
    }
    
    
    protected String getKeyNames(Topic t) throws TopicMapException {
        StringBuilder keywords=new StringBuilder();

        if(t.getBaseName()!=null) {
            keywords.append(t.getBaseName());
            StringTokenizer st = new StringTokenizer(t.getBaseName(), ",-. ");
            if(st.countTokens() > 0) {
                while(st.hasMoreTokens()) {
                    keywords.append(", ");
                    keywords.append(st.nextToken());
                }
            }
        }

        for(Set<Topic> variantScope : t.getVariantScopes()){
            String n=t.getVariant(variantScope);
            if(keywords.length()>0) keywords.append(", ");
            keywords.append(n);
        }
        return keywords.toString();
    }

    
    
    
    public String getTopicSubjectIndicator(Topic t) throws TopicMapException {
        org.wandora.topicmap.Locator l=t.getOneSubjectIdentifier();
        if(l==null) return null;
        else return l.toExternalForm();
    }
    
    
    
    public Set<String> getTopicSubjectIndicators(Topic t) throws TopicMapException {
        Set<String> s = new LinkedHashSet<>();
        for(org.wandora.topicmap.Locator si : t.getSubjectIdentifiers()) {
            s.add( si.toExternalForm() );
        }
        return s;
    }
    
    
    // ----------------------------------
    
    
    public void processTopic(String topicSI,TopicMap tm,IndexWriter writer) throws IOException, TopicMapException {
        processTopic(tm.getTopic(topicSI),writer);
    }
    
    public abstract void processTopic(Topic topic,IndexWriter writer) throws IOException, TopicMapException;
	
    public boolean includeTopic(Topic t){
        return true;
    }
    
    public void processTopicMap(TopicMap tm,IndexWriter writer) throws IOException, TopicMapException {
        Iterator<Topic> iter=tm.getTopics();
        int count = 0;
        while(iter.hasNext()) {
            try {
                Topic topic=(Topic)iter.next();
                if(!includeTopic(topic)) continue;
                org.wandora.topicmap.Locator l=topic.getOneSubjectIdentifier();
                log("Processing topic ("+(count+1)+") "+l);
                processTopic(topic,writer);
                count++;
            }
            catch (Exception e) {
                // Unable to process topic!
                log(e);
            }
        }
    }
	
    public void removeTopicFromIndex(String si,IndexReader reader) throws IOException {
        reader.deleteDocuments(new Term("topic",si));
    }
	
    public void removeTopicsFromIndex(Set<String> topics,IndexReader reader) throws IOException {
        Iterator<String> iter=topics.iterator();
        while(iter.hasNext()) {
            try {
                removeTopicFromIndex((String)iter.next(),reader);
            }
            catch(Exception e) {
                // Unable to remove topic!
                log(e);
            }
        }
    }
	
    public Set<String> getDependentTopics(String topic ,IndexReader reader) throws IOException {
        TermDocs docs=reader.termDocs(new Term("topic",topic));
        while(docs.next()){
            Document doc=reader.document(docs.doc());
            String type=doc.get("type");
            if(type!=null && type.toString().equals("topic")) {
                String d=doc.get("dependent");
                StringTokenizer st=new StringTokenizer(d,"\n");
                Set<String> s=new LinkedHashSet<>();
                while(st.hasMoreTokens()) {
                    s.add(st.nextToken());
                }
                return s;
            }
        }
        return null;
    }
	
    
    
    
    public void updateTopics(Set<String> topics, Set<String> topicsNoDependent, String index, TopicMap tm) throws IOException,TopicMapException {
        Iterator<String> iter=topics.iterator();
        Set<String> delete=new LinkedHashSet<>(); // put all deleted topics here, these are the topics that will need to be remade after deletion
        // first collect all topics that must be deleted before actually deleting anything. otherwise we might be unable to get dependent data for all topics.
        IndexReader reader=IndexReader.open(index);
        while(iter.hasNext()){
            String topic=(String)iter.next();
            delete.add(topic);
            Set<String> dep=getDependentTopics(topic,reader);
            if(dep!=null){
                Iterator<String> iter2=dep.iterator();
                while(iter2.hasNext()){
                    String topic2=(String)iter2.next();
                    delete.add(topic2);
                }
            }
        }
        iter=topicsNoDependent.iterator();
        while(iter.hasNext()){
            String topic=(String)iter.next();
            delete.add(topic);
        }
        
        // next delete topics
        iter=delete.iterator();
        while(iter.hasNext()){
            String topic=(String)iter.next();
            int count=reader.deleteDocuments(new Term("topic",topic));
        }
        reader.close();
        // now remake everything we deleted
        Set<Topic> processed=new LinkedHashSet<>(); // collect updated topic ids here so that we don't update the same topic twice
        IndexWriter writer=new IndexWriter(new File(index),getWriterAnalyzer(),false);
        iter=delete.iterator();
        while(iter.hasNext()){
            String topic=(String)iter.next();
            Topic t=tm.getTopic(topic);
            if(t==null) continue;
            if(!processed.contains(t)){
//                logger.writelog("DBG","Updating topic '"+topic+"'");
                processed.add(t);
                processTopic(t,writer);
            }
        }
        writer.optimize();
        writer.close();
    }
    
    
    public static Document buildDocument(Set<String> sisSet, String type, String name, String keywords, String text, String url, String dependent) {
        StringBuilder sis = new StringBuilder("");
        for(String si : sisSet){
            sis.append(si);
            sis.append("\n");
        }
        return buildDocument(sis.toString(),type,name,keywords,text,url,dependent);
    }
	
    
    
    public static Document buildDocument(String sis,String type,String name,String keywords,String text,String url,String dependent){
        Document doc=new Document();
       
        doc.add(new Field("topic", sis, Field.Store.YES, Field.Index.TOKENIZED)); // special tokenization for subject indicators
        doc.add(new Field("type", type, Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("name", name, Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("keyword", keywords, Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("text" ,text, Field.Store.NO, Field.Index.TOKENIZED));
        doc.add(new Field("url", url, Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("dependent", dependent, Field.Store.YES, Field.Index.NO));
        
        /*
         * LUCENE 1.3
         * 
       // Field(fieldName, value, store, index, tokenize)
         * 
        doc.add(new Field("topic",sis,true,true,true)); // special tokenization for subject indicators
        doc.add(new Field("type",type,true,true,false));
        doc.add(new Field("name",name,true,true,true));
        doc.add(new Field("keyword",keywords,true,true,true));
        doc.add(new Field("text",text,false,true,true));
        doc.add(new Field("url",url,true,true,false));
        doc.add(new Field("dependent",dependent,true,false,false));
         * 
         */
        
        // logger.writelog("INF","Adding document to index {"+sis+"} {"+type+"} {"+name+"} {"+keywords+"} {"+text+"} {"+url+"} {"+dependent+"}");
        return doc;
    }

    public static class NewlineTokenizer extends CharTokenizer {
        public NewlineTokenizer(Reader in) {
            super(in);
        }

        protected boolean isTokenChar(char c) {
            return c!='\n';
        }
    }
    public static class NewlineAnalyzer extends Analyzer {
        public TokenStream tokenStream(String fieldName, Reader reader) {
            return new NewlineTokenizer(reader);
        }
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void log(String str) {
        if(logger != null) logger.log(str);
        if(toolLogger != null) toolLogger.log(str);
        if(logger == null && toolLogger == null) {
            System.out.println(str);
        }
    }
    
    public void log(Exception e) {
        if(logger != null) logger.writelog("ERR", e);
        if(toolLogger != null) toolLogger.log(e);
        else e.printStackTrace();
    }
    
    public void log(String str, Exception e) {
        if(logger != null) logger.writelog("ERR", str, e);
        if(toolLogger != null) toolLogger.log(str, e);
        else {
            System.out.println(str);
            e.printStackTrace();
        }
    }
    public void log(String t, String str, Exception e) {
        if(logger != null) logger.writelog(t, str, e);
        if(toolLogger != null) toolLogger.log(str, e);
        else {
            System.out.println(str);
            e.printStackTrace();
        }
    }
    
}

