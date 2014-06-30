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
 * FixKaupunkiTextPlaces.java
 *
 * Created on 13. joulukuuta 2004, 19:14
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

import org.wandora.utils.*;
import org.wandora.topicmap.*;

/**
 *
 * @author  akivela
 */
public class FixKaupunkiTextPlaces {
    
    /** Creates a new instance of FixTimeApellations */
    public FixKaupunkiTextPlaces() {
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
    
    
    

    
    
    public static void setDisplayName(Topic t, String lang, String name) throws TopicMapException  {
        String langsi=XTMPSI.getLang(lang);
        Topic langT=t.getTopicMap().getTopic(langsi);
        String dispsi=XTMPSI.DISPLAY;
        Topic dispT=t.getTopicMap().getTopic(dispsi);
        HashSet scope=new HashSet();
        if(langT!=null) scope.add(langT);
        if(dispT!=null) scope.add(dispT);
        t.setVariant(scope, name);
    }
    
    
    
    
    public static void createTopic(TopicMap tm, String si, String fname, String ename, String[] types) throws TopicMapException  {
        Topic topic = tm.getTopic(si);
        if(topic == null) {
            Topic t = tm.createTopic();
            Locator siLocator = tm.createLocator(si);
            t.addSubjectIdentifier(siLocator);
            t.setBaseName(fname);
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
    
    
 
    
    
    
    public static void process(TopicMap tm) throws TopicMapException {
        Vector v=new Vector();
        Topic ktextTopic = tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-kaupunki-teksti");
        System.out.println("Kaupunki text type == " + ktextTopic.getBaseName());       

        Topic alueTopic = tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-alue-tai-lääni");
        Topic kaupunkiTopic = tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-kaupunki-tai-pitäjä");
        Topic kaupunginosaTopic = tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-kaupunginosa-tai-kylä");
        Topic osoiteTopic = tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-osoite-tai-paikka");
        
        Topic tekstinPaikkaviittaus = tm.getTopic("http://wandora.org/si/temp/10975062838905043041_copy");
        Topic paikkaTopic = tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-paikka");
        
        Iterator iter=tm.getTopics();
        while(iter.hasNext()) {
            Topic t=(Topic)iter.next();
            if(t.isOfType(ktextTopic)) {
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
                System.out.println("Fixing " + t.getBaseName());
                c++;
                Collection ass = t.getAssociations();
                for(Iterator asi = ass.iterator(); asi.hasNext();) {
                    Association a = (Association) asi.next();
                    if(a.getType().equals(osoiteTopic)) {
                        a.setType(tekstinPaikkaviittaus);
                        Topic p = a.getPlayer(osoiteTopic);
                        a.removePlayer(osoiteTopic);
                        a.addPlayer(p, paikkaTopic);
                    }
                    else if(a.getType().equals(kaupunginosaTopic)) {
                        a.setType(tekstinPaikkaviittaus);
                        Topic p = a.getPlayer(kaupunginosaTopic);
                        a.removePlayer(kaupunginosaTopic);
                        a.addPlayer(p, paikkaTopic);
                    }
                    else if(a.getType().equals(kaupunkiTopic)) {
                        a.setType(tekstinPaikkaviittaus);
                        Topic p = a.getPlayer(kaupunkiTopic);
                        a.removePlayer(kaupunkiTopic);
                        a.addPlayer(p, paikkaTopic);
                    }
                    else if(a.getType().equals(alueTopic)) {
                        a.setType(tekstinPaikkaviittaus);
                        Topic p = a.getPlayer(alueTopic);
                        a.removePlayer(alueTopic);
                        a.addPlayer(p, paikkaTopic);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Can't fix " + t.getBaseName());
                e.printStackTrace();
            }
        }
        System.out.println("Total " + c + " topics fixed!");
        System.out.println("Total unregocnized " + unregocnized + " topics!");        
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
        
        FixKaupunkiTextPlaces.process(tm);
        
        OutputStream out=new FileOutputStream(args[1]);
        tm.exportXTM(out);
        out.close();
    }
    
   
    
}

    
