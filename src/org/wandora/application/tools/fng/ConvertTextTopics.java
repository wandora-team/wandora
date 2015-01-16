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
 * ConvertTextTopics.java
 *
 * Created on September 15, 2004, 6:41 PM
 */

package org.wandora.application.tools.fng;



import org.wandora.topicmap.Locator;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.XTMPSI;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import gnu.regexp.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.wandora.piccolo.Logger;
import org.wandora.utils.*;
import org.wandora.topicmap.*;



/**
 *
 * @author  akivela
 */
public class ConvertTextTopics {
    
    public static TopicMap targetTopicMap = new org.wandora.topicmap.memory.TopicMapImpl();
    
    
    /** Creates a new instance of ConvertTextTopics */
    public ConvertTextTopics() {
    }
    
    
    
    public static String trimExtraSpaces(String string) {
        return trimEndingSpaces(trimStartingSpaces(string));
    }
    

    public static String chop(String text) {
        return trimEndingSpaces(text);
    }
    
    
    
    public static String trimEndingSpaces(String string) {
        if (string != null) {
            int i = string.length()-1;
            while(i > 0 && Character.isWhitespace(string.charAt(i))) i--;
            string = string.substring(0, i+1);
        }
        return string;
    }

   
    public static String trimStartingSpaces(String string) {
        if (string != null) {
            int i = 0;
            while(i < string.length() && Character.isWhitespace(string.charAt(i))) i++;
            string = string.substring(i);
        }
        return string;
    }
    
    public static String removeNewLines(String string) {
        StringBuffer sb = new StringBuffer("");
        if (string != null) {
            for(int i=0; i<string.length(); i++) {
                try {
                    if(string.charAt(i) != '\n' && string.charAt(i) != '\r') {
                        sb.append(string.charAt(i));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
    
    
    
        
    public static void setDisplayName(Topic t, String lang, String name) throws TopicMapException {
        String langsi=XTMPSI.getLang(lang);
        Topic langT=t.getTopicMap().getTopic(langsi);
        String dispsi=XTMPSI.DISPLAY;
        Topic dispT=t.getTopicMap().getTopic(dispsi);
        HashSet scope=new HashSet();
        if(langT!=null) scope.add(langT);
        if(dispT!=null) scope.add(dispT);
        t.setVariant(scope, name);
    }
    
    
        
    public static void createTopic(TopicMap tm, String si, String bn, String fname, String ename, String[] types) throws TopicMapException {
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            Topic t = tm.createTopic();
            Locator siLocator = tm.createLocator(si);
            t.addSubjectIdentifier(siLocator);
            t.setBaseName(bn);
            setDisplayName(t, "fi" , fname);
            setDisplayName(t, "en" , ename);
            for(int i=0; i<types.length; i++) {
                Topic typeTopic = tm.getTopic(types[i]);
                if(typeTopic != null) {
                    t.addType(typeTopic);
                }
            }
            System.out.println("  topic created " + t.getBaseName());
        }
    }
    
    
    
    public static void createTopicsForehand() throws TopicMapException {
        createTopic(targetTopicMap,
            "http://www.fng.fi/wandora/wandora-fng.xtm#kp-tekstitiedosto",
            "tekstitiedosto",
            "tekstitiedosto", 
            "text document",
            new String[] {}
        );
        
        createTopic(targetTopicMap,
            "http://www.fng.fi/wandora/wandora-fng.xtm#kp-henkilö",
            "henkilö",
            "henkilö", 
            "person",
            new String[] {}
        );
        
        createTopic(targetTopicMap,
            "http://www.fng.fi/wandora/wandora-fng.xtm#kp-toimija",
            "toimija",
            "toimija", 
            "actor",
            new String[] {
                "http://www.fng.fi/wandora/wandora-fng.xtm#kp-henkilö"
            }
        );
        
        createTopic(targetTopicMap,
            "http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#kirjoittaja",
            "kaupunki tekstin kirjoittaja",
            "kaupunki tekstin kirjoittaja", 
            "City text author",
            new String[] {
                "http://www.fng.fi/wandora/wandora-fng.xtm#kp-henkilö",
                "http://www.fng.fi/wandora/wandora-fng.xtm#kp-toimija"
            }
        );
        
        createTopic(targetTopicMap,
            "http://www.fng.fi/wandora/wandora-fng.xtm#kp-kaupunki-teksti",
            "kaupunki teksti",
            "kaupunki teksti", 
            "City text",
            new String[] {}
        );
    }
    
    
    
    
    public static void removeOccurrences(String occurrenceType, TopicMap tm) throws TopicMapException {
        Vector v=new Vector();
        Topic typeTopic = tm.getTopic(occurrenceType);    
        
        Iterator iter=tm.getTopics();
        while(iter.hasNext()) {
            Topic t=(Topic)iter.next();
            Hashtable data = t.getData(typeTopic);
            if(data.size() > 0) {
                v.add(t);
            }
        }
        iter=v.iterator();
        int c=0;
        int unregocnized = 0;
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            try {
                t.removeData(typeTopic);
            }
            catch (Exception e) {
                System.out.println("Can't remove occurrence from " + t.getBaseName());
                e.printStackTrace();
            }
        }

        System.out.println("Total " + c + " occurrences removed!");   
    }
    
    
    
    
    public static void moveOccurrences(Topic source, Topic target) throws TopicMapException  {
        Collection sourceOTypes = source.getDataTypes();
        Vector occusToDel = new Vector();
        for(Iterator iter = sourceOTypes.iterator(); iter.hasNext(); ) {
            try {
                Topic oType = (Topic) iter.next();
                target.setData(oType, source.getData(oType));
                occusToDel.add(oType);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        for(int i=0; i<occusToDel.size(); i++) {
            source.removeData((Topic) occusToDel.get(i));
        }
        
    }
    
    
    
    
    public static void fixAineisto(TopicMap tm) throws TopicMapException  {
        Vector v=new Vector();
        Topic typeTopic = tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-kaupunki-teksti");    
        Topic aineistoTypeTopic = tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-aineisto");
        Topic occurrenceRoleTopic = tm.getTopic("http://wandora.org/si/compatibility/occurrenceroletopic");
        
        Iterator iter=tm.getTopics();
        while(iter.hasNext()) {
            Topic t=(Topic)iter.next();
            if(t.isOfType(typeTopic)) {
                v.add(t);
            }
        }
        iter=v.iterator();
        int c=0;
        int unregocnized = 0;
        while(iter.hasNext()){
            Vector assocsToMove=new Vector();
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            try {
                Collection col = t.getAssociations();
                Association a;
                for(Iterator i = col.iterator(); i.hasNext();) {
                    a = (Association) i.next();
                    if(a.getType().equals(aineistoTypeTopic)) {
                        Topic aineistoTopic = a.getPlayer(aineistoTypeTopic);
                        Collection aineistoAssociations = aineistoTopic.getAssociations();
                        Association aa;
                        for(Iterator ai = aineistoAssociations.iterator(); ai.hasNext();) {
                            assocsToMove.add((Association) ai.next());
                        }
                        moveOccurrences(aineistoTopic, t);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Can't copy assocs/occurrences to " + t.getBaseName());
                e.printStackTrace();
            }
            for(int j=0; j < assocsToMove.size(); j++) {
                Association a = (Association) assocsToMove.get(j);
                // copy aa (from aineistoTopic) --> t
                //aa.getPlayer(aineistoTypeTopic);
                a.removePlayer(aineistoTypeTopic);
                a.removePlayer(occurrenceRoleTopic);
                a.addPlayer(t, typeTopic);
            }
        }
        

        System.out.println("Total " + c + " occurrences/associations copied!");           
    }
    
    
    
    
    public static void fixDocumentOccurrences(TopicMap tm) throws TopicMapException {
        Vector v=new Vector(); 
        Topic tekstiTiedostoType = tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-tekstitiedosto");
        
        Iterator iter=tm.getTopics();
        while(iter.hasNext()) {
            Topic t=(Topic)iter.next();
            if(t.getSubjectLocator() != null) {
                v.add(t);
            }
        }
        iter=v.iterator();
        int c=0;
        int unregocnized = 0;
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            try {
                String subjectLocator = t.getSubjectLocator().toExternalForm();
                int index = subjectLocator.lastIndexOf('/');
                if(index != -1) {
                    subjectLocator = "http://wandora.fng.fi/wandora/repository/tekstit" + subjectLocator.substring(index);
                    t.setSubjectLocator(new Locator(subjectLocator));
                    t.setBaseName("Occurrence " + subjectLocator);
                    t.addType(tekstiTiedostoType);
                }
            }
            catch (Exception e) {
                System.out.println("Can't remove occurrence from " + t.getBaseName());
                e.printStackTrace();
            }
        }

        System.out.println("Total " + c + " occurrences removed!");       
    }
    
    
    
    public static void removeAssociationsOfType(TopicMap tm, String si) throws TopicMapException {
        Vector v=new Vector();
        Topic typeTopic = tm.getTopic(si);    
        
        Iterator iter=tm.getAssociations();
        while(iter.hasNext()) {
            Association a=(Association)iter.next();
            if(a.getType().equals(typeTopic)) {
                v.add(a);
            }
        }
        iter=v.iterator();
        int c=0;
        int unregocnized = 0;
        while(iter.hasNext()){
            Association a=(Association)iter.next();
            try {
                a.remove();
                c++;
            }
            catch (Exception e) {
                System.out.println("Can't remove association");
                e.printStackTrace();
            }
        }

        System.out.println("Total " + c + " associations removed!");   
    }
    
    
    
    
    public static void removeTopicsOfType(TopicMap tm, String si) throws TopicMapException {
        Vector v=new Vector();
        Topic typeTopic = tm.getTopic(si);    
        
        Iterator iter=tm.getTopics();
        while(iter.hasNext()) {
            Topic t=(Topic)iter.next();
            if(t.isOfType(typeTopic)) {
                v.add(t);
            }
        }
        iter=v.iterator();
        int c=0;
        int unregocnized = 0;
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            try {
                t.remove();
                c++;
            }
            catch (Exception e) {
                System.out.println("Can't remove topic " + t.getBaseName());
                e.printStackTrace();
            }
        }

        System.out.println("Total " + c + " topics removed!");   
    }
    
    
    
    
    
    
    public static void process(TopicMap tm) throws TopicMapException {
        createTopicsForehand();
        copyInstancesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-kaupunki-teksti", tm, targetTopicMap);
        copyInstancesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-aineisto", tm, targetTopicMap);
        copyInstancesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-tekstin-kirjoittaja", tm, targetTopicMap);
        copyInstancesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-sisältötyyppi-paikka", tm, targetTopicMap);
        
        //BaseNameCleaner baseNameCleaner = new BaseNameCleaner();
        //targetTopicMap = baseNameCleaner.process(targetTopicMap, Logger.getLogger());
        
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-henkilö", targetTopicMap);
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-paikka", targetTopicMap);
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-asema", targetTopicMap);
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-tekstin-kirjoittaja", targetTopicMap);
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-aineisto", targetTopicMap);
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-sisällön-ajoitus", targetTopicMap);
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-sisältötyyppi", targetTopicMap);
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-juridinen-henkilö", targetTopicMap);
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-kaupunki-teksti", " (kaupunki teksti)", targetTopicMap);
        fixBaseNamesOf("http://www.fng.fi/wandora/wandora-fng.xtm#kp-tekstin-nro", targetTopicMap);
        
        removeOccurrences("http://wandora.org/si/waonder/wandora.xtm#templatetopic", targetTopicMap);
        
        fixAineisto(targetTopicMap);
        fixDocumentOccurrences(targetTopicMap);
        removeTopicsOfType(targetTopicMap, "http://www.fng.fi/wandora/wandora-fng.xtm#kp-aineisto");
        removeAssociationsOfType(targetTopicMap, "http://www.fng.fi/wandora/wandora-fng.xtm#kp-aineisto");
    }
    
    
    
    public static void fixBaseNamesOf(String si, TopicMap tm) throws TopicMapException {
        fixBaseNamesOf(si, "", tm);
    }
    
    public static void fixBaseNamesOf(String si, String postfix, TopicMap tm) throws TopicMapException {
        Vector v=new Vector();
        Topic typeTopic = tm.getTopic(si);    
        
        Iterator iter=tm.getTopics();
        while(iter.hasNext()) {
            Topic t=(Topic)iter.next();
            if(t.isOfType(typeTopic)) {
                v.add(t);
            }
        }
        iter=v.iterator();
        int c=0;
        int unregocnized = 0;
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            try {
                String name = trimExtraSpaces(t.getDisplayName("fi"));
                if(name == null || name.length() == 0) {
                    name = trimExtraSpaces(t.getDisplayName("en"));
                    if(name == null || name.length() == 0) {
                        setDisplayName(t, "fi", name);
                    }
                }
                if(name != null && name.length() > 0) {
                    t.setBaseName(name + postfix);
                    c++;
                }
            }
            catch (Exception e) {
                System.out.println("Can't copy " + t.getBaseName());
                e.printStackTrace();
            }
        }

        System.out.println("Total " + c + " topics basename-renamed");
    }       

    
    
    
    
    public static void copyInstancesOf(String si, TopicMap source, TopicMap target) throws TopicMapException {
        Vector v=new Vector();
        Topic typeTopic = source.getTopic(si);    
        
        Iterator iter=source.getTopics();
        while(iter.hasNext()) {
            Topic t=(Topic)iter.next();
            if(t.isOfType(typeTopic)) {
                v.add(t);
            }
        }
        iter=v.iterator();
        int c=0;
        int unregocnized = 0;
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            if(t.isRemoved()) continue;
            try {
                c++;
                Topic copyTopic = target.copyTopicIn(t, false);
                
                Collection col = t.getAssociations();
                Association a;
                for(Iterator i = col.iterator(); i.hasNext();) {
                    a = (Association) i.next();
                    targetTopicMap.copyAssociationIn(a);
                }
                
            }
            catch (Exception e) {
                System.out.println("Can't copy " + t.getBaseName());
                e.printStackTrace();
            }
        }

        System.out.println("Total " + c + " topics copied!");
    }
    
      
    
    // *************************************************************************
    // *************************************************************************
    // *************************************************************************
    
    
    

    // simple way to get milliseconds of the specified time (use to set expiration)
    public static void main(String args[]) throws Exception {       
        TopicMap tm=new org.wandora.topicmap.memory.TopicMapImpl();
        InputStream in=new FileInputStream(args[0]);
        tm.importXTM(in);
        in.close();
        
        ConvertTextTopics.process(tm);
        
        OutputStream out=new FileOutputStream(args[1]);
        targetTopicMap.exportXTM(out);
        out.close();
    }
    
    
    
}
