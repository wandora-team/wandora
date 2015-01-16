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
 * BibtexExtractor.java
 *
 * Created on 17. lokakuuta 2007, 10:21
 *
 */

package org.wandora.application.tools.extractors.bibtex;

import org.wandora.utils.GripCollections;
import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.topicmap.TMBox;
import org.wandora.piccolo.utils.crawler.*;
import org.wandora.piccolo.utils.crawler.handlers.*;
import org.wandora.utils.swing.*;
import org.wandora.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;


/**
 *
 * @author olli
 */
public class BibtexExtractor extends AbstractExtractor implements WandoraTool {
    
    private String baseLocator = "http://wandora.org/si/bibtex/";
    private String defaultEncoding = "ISO-8859-1";

    /** Creates a new instance of BibtexExtractor */
    public BibtexExtractor() {
    }

    public String getName() {
        return "BibTeX extractor (old version)";
    }
    public String getDescription(){
        return "Extracts information from BibTeX files!";
    }
    
    
    public String getGUIText(int textType) {
        return super.getGUIText(textType);
    }
    
    private Topic getOrCreateTopic(TopicMap tm,String topic) throws TopicMapException {
        return getOrCreateTopic(tm,topic,false);
    }
    private Topic getOrCreateTopic(TopicMap tm,String topic,boolean addToBibTexClass) throws TopicMapException {
        Locator l=buildSI(topic.toLowerCase());
        Topic t=tm.getTopic(l);
        if(t!=null) return t;
        t=tm.createTopic();
        t.addSubjectIdentifier(l);
        t.setBaseName(topic);
        if(addToBibTexClass){
            Topic bc = TMBox.getOrCreateTopic(tm,baseLocator);
            bc.setBaseName("Bibtex");
            Topic c = TMBox.getOrCreateTopic(tm,TMBox.WANDORACLASS_SI);
            t.addType(bc);
            bc.addType(c);
        }
        return t;
    }
    
    private void addOccurrence(TopicMap tm,Topic entryTopic,BibtexEntry entry,String key) throws TopicMapException {
        Object o=entry.getValue(key);
        if(o==null) return;
        Topic type=getOrCreateTopic(tm,key);
        Topic lang=TMBox.getOrCreateTopic(tm,XTMPSI.LANG_INDEPENDENT);
        entryTopic.setData(type,lang,o.toString());
    }
    private void addAssociation(TopicMap tm,Topic entryTopic,BibtexEntry entry,String key) throws TopicMapException {
        Object o=entry.getValue(key);
        if(o==null) return;
        
        Topic type=getOrCreateTopic(tm,key);
        Topic citation=getOrCreateTopic(tm,"citation");
        Topic lang=TMBox.getOrCreateTopic(tm,XTMPSI.LANG_INDEPENDENT);
        
        ArrayList a;
        if(o instanceof ArrayList) a=(ArrayList)o;
        else {
            a=new ArrayList();
            a.add(o);
        }
        for(Object v : a){
            Association as=tm.createAssociation(type);
            as.addPlayer(entryTopic,citation);
            Topic p;
            if(v instanceof BibtexPerson){
                p=createPersonTopic(tm,(BibtexPerson)v);
            }
            else{
                p=tm.createTopic();
                p.addType(type);
                p.setBaseName(v.toString());
            }
            as.addPlayer(p,type);
        }
    }
    
    private Topic createPersonTopic(TopicMap tm,BibtexPerson p) throws TopicMapException {
        String name=p.getLastName();
        if(p.getFirstName()!=null) name+=", "+p.getFirstName();
        if(p.getInitials()!=null) name+=" "+p.getInitials();
        Topic t=tm.createTopic();
        t.setBaseName(name);
        Topic type=getOrCreateTopic(tm,"Person");
        t.addType(type);
        return t;
    }
    
    public static final HashSet<String> associationFields=GripCollections.newHashSet("author","editor","institution","organization","booktitle","journal",
                                                                                    "publisher","school","series","year","volume","number","month","type","chapter","edition","howpublished");
    public static final HashSet<String> occurrenceFields=GripCollections.newHashSet("address","annote","crossref",
                                                                                    "key","note","pages");
    public boolean _extractTopicsFrom(Reader reader, TopicMap tm) throws Exception {
        BibtexParser parser=new BibtexParser();
        try{
            parser.parse(reader);
            ArrayList<BibtexEntry> entries=parser.getEntries();
                        
            for(BibtexEntry e : entries){
                String typeS=e.getType();
                Topic type=getOrCreateTopic(tm,typeS,true);
                Object titleS=e.getValue("title");
                Topic entry=null;
                if(titleS!=null) entry=tm.getTopicWithBaseName(titleS.toString());
                if(entry==null) entry=tm.createTopic();
                entry.addType(type);
                if(titleS!=null) entry.setBaseName(titleS.toString());
                
                Map<String,Object> values=e.getValues();
                for(String key : values.keySet()){
                    if(associationFields.contains(key)){
                        addAssociation(tm,entry,e,key);
                    }
                    else {
                        addOccurrence(tm,entry,e,key);
                    }
                }                
            }
            if(entries.size()>0) return true;
        }catch(IOException ioe){
            log(ioe);
        }
        return false;
    }
    
    public boolean _extractTopicsFrom(URL file, TopicMap topicMap) throws Exception {
        URLConnection con=file.openConnection();
        Wandora.initUrlConnection(con);
        String enc=con.getContentEncoding();
        Reader reader=null;
        if(enc==null) enc=defaultEncoding;
        reader=new InputStreamReader(con.getInputStream(),enc);
        return _extractTopicsFrom(reader,topicMap);
    }

    public boolean _extractTopicsFrom(File file, TopicMap topicMap) throws Exception {
        Reader reader=new InputStreamReader(new FileInputStream(file),defaultEncoding);
        return _extractTopicsFrom(reader,topicMap);
    }
    public boolean _extractTopicsFrom(String str, TopicMap topicMap) throws Exception {
        Reader reader=new BufferedReader(new StringReader(str));
        return _extractTopicsFrom(reader, topicMap);
    }



    
    
    @Override
    public Locator buildSI(String siend) {
          if(!baseLocator.endsWith("/")) baseLocator = baseLocator + "/";
          return new Locator(TopicTools.cleanDirtyLocator(baseLocator + siend));
    }

    

    private final String[] contentTypes=new String[] { "text/plain", "application/x-bibtex", "text/x-bibtex" };

    public String[] getContentTypes() {
        return contentTypes;
    }    
}
