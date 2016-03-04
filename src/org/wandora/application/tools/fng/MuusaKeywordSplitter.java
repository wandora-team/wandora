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
 * MuusaKeywordSplitter.java
 *
 * Created on August 25, 2004, 9:17 AM
 */

package org.wandora.application.tools.fng;


import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.piccolo.Logger;
import org.wandora.utils.*;
import java.util.*;



/**
 *
 * @author  olli, akivela
 */
public class MuusaKeywordSplitter extends AbstractWandoraTool implements WandoraTool {
    Wandora admin = null;
    
    
    /** Creates a new instance of InventoryNumbenCleaner */
    public MuusaKeywordSplitter() {
    }

    
    public String getName() {
        return "Muusa Keyword Splitter";
    }
    
    
    public void execute(Wandora admin, Context context) {      
        setDefaultLogger();
        
        try {
            setLogTitle("Splitting Muusa Keywords...");
            process(admin.getTopicMap(), Logger.getLogger());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        setState(WAIT);
    }
    
    
    

    public TopicMap process(TopicMap tm,Logger logger) throws TopicMapException {
        log("Splitting Muusa Keywords...");
        
        Topic fiLang = tm.getTopic("http://www.muusa.net/E55.Type_fi");
        Topic swLang = tm.getTopic("http://www.muusa.net/E55.Type_sw");
        Topic enLang = tm.getTopic("http://www.muusa.net/E55.Type_en");

        Topic work=tm.getTopic("http://www.muusa.net/Teos");
        Topic isAboutType=tm.getTopic("http://www.muusa.net/P129.is_about");
        Topic isAboutRole=tm.getTopic("http://www.muusa.net/P129.is_about_role_0");
        Topic requiredRole=tm.getTopic("http://www.muusa.net/E32.Authority_Document");
        Topic requiredPlayer=tm.getTopic("http://www.muusa.net/P71_lists_aihe");
        if( work==null || isAboutType==null || isAboutRole==null || requiredRole==null || requiredPlayer==null ){
            log("Couldn't find all required topics.");
            return tm;
        }
        
        Topic keywordClassTopic=tm.getTopic("http://www.muusa.net/keyword");
        if(keywordClassTopic==null) {
            keywordClassTopic=tm.createTopic();
            keywordClassTopic.setBaseName("asiasana (muusa)");
            keywordClassTopic.addSubjectIdentifier(new Locator("http://www.muusa.net/keyword"));
        }
        
        int splitCounter=0;
        int createTopicCounter=0;
        int createAssociationCounter=0;
        Iterator iter=tm.getTopicsOfType(work).iterator();
        while(iter.hasNext()){
            Topic t=(Topic)iter.next();
            Vector<Topic> keywords = TopicTools.getPlayers(t, isAboutType, isAboutRole, requiredRole, requiredPlayer);
            int s = keywords.size();
            for(int i=0; i<s; i++) {
                Topic keywordTopic = keywords.elementAt(i);
                if(keywordTopic != null) {
                    hlog("Splitting topic '"+ getTopicName(keywordTopic) +"'.");                    
                    StringTokenizer st = new StringTokenizer(keywordTopic.getBaseName(), ",;");
                    while(st.hasMoreTokens()) {
                        String token = st.nextToken();
                        token = fixKeyword(token);
                        if(token != null && token.length() > 0 && !token.startsWith("P129F") && !token.startsWith("?")) {
                            String siString = "http://www.muusa.net/keywords/"+token;
                            if(siString.length() > 255) siString = siString.substring(0,255);
                            Locator l = cleanDirtyLocator(new Locator(siString));
                            Topic newKeyword = tm.getTopic(l);
                            if(newKeyword == null) {
                                newKeyword = tm.createTopic();
                                newKeyword.setBaseName(token + " (asiasana)");
                                newKeyword.setDisplayName("fi", token);
                                newKeyword.addSubjectIdentifier(l);
                                newKeyword.addType(keywordClassTopic);
                                createTopicCounter++;
                                log("Created new keyword topic '"+ getTopicName(newKeyword) +"'.");
                            }
                            
                            Association a = tm.createAssociation(keywordClassTopic);
                            a.addPlayer(newKeyword, keywordClassTopic);
                            a.addPlayer(t, work);
                            createAssociationCounter++;
                        }
                    }
                }
                try {
                    for(Iterator assos = keywordTopic.getAssociations().iterator(); assos.hasNext(); ) {
                        Association a = (Association) assos.next();
                        if(a != null) a.remove();
                    }
                    keywordTopic.remove();
                    splitCounter++;
                }
                catch(Exception e) {
                    log(e);
                }
            }
        }
        log("Splitted "+splitCounter+" compound keyword topics");
        log("Created "+createAssociationCounter+" keyword associations");
        log("Created "+createTopicCounter+" keyword topics");
        return tm;
    }    
    

    public String fixKeyword(String keyword) {
        if(keyword == null) return keyword;
        try {
            keyword = Textbox.trimExtraSpaces(keyword);
            if(keyword.startsWith("\"") && keyword.endsWith("\"")) return keyword;
            if(keyword.endsWith("?")) keyword = keyword.substring(0, keyword.length()-1);
            if(keyword.endsWith("(?)")) keyword = keyword.substring(0, keyword.length()-3);
            keyword = Textbox.trimExtraSpaces(keyword);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return keyword;
    }
    
    
    
    public boolean isDirtySI(Locator l) {
        String s = l.toExternalForm();
        for(int k=0; k<s.length(); k++) {
            if(isDirtyCharacter(s.charAt(k))) return true;
        }
        return false;
    }
    
    
    public boolean isDirtyCharacter(char c) {
        if("1234567890poiuytrewqasdfghjklmnbvcxzPOIUYTREWQASDFGHJKLMNBVCXZ.:-_/#+%&?".indexOf(c) != -1) return false;
        else return true;
    }
    
    
    public char repacementCharacterFor(char c) {
        int i = "ÄÖÅäöåéü".indexOf(c);
        if(i == -1) return '_';
        else return "AOAaoaeu".charAt(i);
    }
    
    
    public Locator cleanDirtyLocator(Locator l) {
        StringBuffer sb = new StringBuffer();
        String s = l.toExternalForm();
        for(int k=0; k<s.length(); k++) {
            char c = s.charAt(k);
            if(isDirtyCharacter(c)) sb.append(repacementCharacterFor(c));
            else sb.append(c);
        }
        return new Locator(sb.toString());
    }
    
    
}
