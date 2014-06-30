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
 *
 * TopicMapIndexBuilder.java
 *
 * Created on February 27, 2002, 7:21 PM
 */

package org.wandora.indexer;



import java.util.*;
import java.net.*;
import java.io.*;
import org.wandora.topicmap.*;
import org.apache.lucene.index.*;
import org.apache.lucene.document.Document;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraToolLogger;
import org.wandora.piccolo.*;
import org.wandora.topicmap.memory.TopicMapImpl;



/**
 *
 * @author  olli, akivela
 */
public class TopicMapIndexBuilder extends AbstractIndexBuilder {

    
    
    
    /** Creates new TopicMapIndexBuilder */
    public TopicMapIndexBuilder() {
        super();
    }
    
    public TopicMapIndexBuilder(Logger logger) {
        super(logger);
    }
    
    public TopicMapIndexBuilder(WandoraToolLogger toolLogger) {
        super(toolLogger);
    }
    
    
    protected boolean checkVisibility=true;
    
    public void setCheckVisibility(boolean b){checkVisibility=b;}
    public boolean getCheckVisibility(){return checkVisibility;}
    

    
    
    public void processTopic(Topic topic, IndexWriter writer) throws IOException, TopicMapException {
        if(topic == null || topic.isRemoved()) return;
        
        Set sis = getTopicSubjectIndicators(topic);
        if(sis==null || sis.isEmpty()) return;
        if(checkVisibility && !TMBox.topicVisible(topic)) return;

        Set dependent=new HashSet();

        Topic dispT=topic.getTopicMap().getTopic(XTMPSI.DISPLAY);
        
        HashSet namehash=new HashSet();
        StringBuilder associatedB=new StringBuilder();
        StringBuilder namesB=new StringBuilder();
        Set scopes=topic.getVariantScopes();
        Iterator iter=scopes.iterator();
        boolean found=false;
        while(iter.hasNext()){
            Set scope=(Set)iter.next();
            if(dispT!=null && scope.contains(dispT)){
                String data=topic.getVariant(scope).trim();
                if(!namehash.contains(data)){
                    found=true;
                    namesB.append(" ").append(data);
                    namehash.add(data);
                }
            }
        }        
        if(!found){
            if(topic.getBaseName()!=null) namesB.append(" ").append(topic.getBaseName());
        }



        for(Association a : topic.getAssociations()) {
            for(Topic role : a.getRoles()){
                Topic p=a.getPlayer(role);
                boolean visible=true;
                if(checkVisibility) {
                    visible=TMBox.topicVisible(p);
                }
                if(p!=topic) {
                    try {
                        String si=p.getOneSubjectIdentifier().toExternalForm();
                        if(!dependent.contains(si)) dependent.add(si);
                    }
                    catch(NullPointerException e){}
                    if(visible) {
                        String k=getKeyNames(p).trim();
                        associatedB.append(k).append(" ");
                    }
                }
            }
        }
        
        StringBuilder dependentB=new StringBuilder();
        String dependentS="";
        iter=dependent.iterator();
        while(iter.hasNext()){
//            dependentS+=iter.next().toString()+"\n";
            dependentB.append(iter.next().toString()).append("\n");
        }
        dependentS=dependentB.toString();
        
        for(Topic type : topic.getDataTypes()){
            if((!checkVisibility) || TMBox.topicVisible(type)){
                Hashtable ht=topic.getData(type);
                Iterator iter2=ht.values().iterator();
                while(iter2.hasNext()){
                    associatedB.append(iter2.next()).append(" ");
                }
            }
        }
        
        String associated=associatedB.toString();
        String names=namesB.toString();
        
        Document d = buildDocument(sis,"topic",names,"",associated,"",dependentS);
        writer.addDocument(d);

        org.wandora.topicmap.Locator l=topic.getSubjectLocator();
        if(l!=null){
            try {
                URL url=new URL(l.toExternalForm());
                URLConnection uc=url.openConnection();
                Wandora.initUrlConnection(uc);
                String contentType=uc.getContentType();
                if(contentType!=null){
                    Extractor ex=(Extractor)extractors.get(contentType);
                    if(ex != null){
                        InputStream is=uc.getInputStream();
                        String[] content=ex.extract(is,url);
                        try{is.close();}catch(IOException e){}
                        if(content!=null) {
                            writer.addDocument(buildDocument(sis,"occurrence",content[0],content[1],content[2],url.toString(),""));
                        }
                    }                        
                }
            }
            catch(MalformedURLException e){ log("WRN","Exception when building search index",e); }
            catch(Exception e){ log("WRN","Exception when building search index",e); }
        }
    }
	
    
    
    
    

    public static void main (String args[]) throws Exception {        
        System.out.println("*****************************************************");
        System.out.println("***        TopicMapIndexBuilder @ Wandora         ***");
        System.out.println("***    (c) Grip Studios Interactive, 2001-11      ***");
        System.out.println("*****************************************************");       
        
        long startTime = System.currentTimeMillis();
        String indexName = (args.length>=2) ? (args[1]) : "tmindex";
        String topicMapName = (args.length>=2) ? (args[0]) : "testnew.xtm";
        
        TopicMap tm=new TopicMapImpl();
        tm.importXTM(topicMapName);
        
        TopicMapIndexBuilder indexBuilder=new TopicMapIndexBuilder();
        indexBuilder.processTopicMap(tm, indexName);
        
        long endTime = System.currentTimeMillis();       
        System.out.println("Indexing took " + ((int) ((endTime-startTime)/1000)) + " seconds.");
        
        System.out.println("Done");
        System.exit(0);
    }
    
}

