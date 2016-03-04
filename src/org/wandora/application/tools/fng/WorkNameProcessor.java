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
 * WorkNameProcessor.java
 *
 * Created on August 25, 2004, 9:17 AM
 */

package org.wandora.application.tools.fng;


import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.util.*;

/**
 *
 * @author  olli, akivela
 */


public class WorkNameProcessor extends AbstractWandoraTool implements WandoraTool {
    Wandora admin = null;
    
    
    /** Creates a new instance of WorkNameProcessor */
    public WorkNameProcessor() {
    }

    
    public String getName() {
        return "FNG Work name processor";
    }
    
    
    public void execute(Wandora admin, Context context) {      
        setDefaultLogger();
        try {
            log("Processing work names...");
            process(admin.getTopicMap());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        setState(WAIT);
    }
    
    
    

    public TopicMap process(TopicMap tm) throws TopicMapException {
        log("Applying WorkNameProcessor filter");
        
        Topic indepLang = tm.getTopic(TMBox.LANGINDEPENDENT_SI);
        Topic fiLang = tm.getTopic("http://www.muusa.net/E55.Type_fi");
        Topic swLang = tm.getTopic("http://www.muusa.net/E55.Type_se");
        Topic enLang = tm.getTopic("http://www.muusa.net/E55.Type_en");
        
        Topic inventaarionNumeroTopic = tm.getTopic("http://www.muusa.net/inventaarionumero");
        if(inventaarionNumeroTopic == null) {
            inventaarionNumeroTopic = tm.createTopic();
            inventaarionNumeroTopic.addSubjectIdentifier(new Locator("http://www.muusa.net/inventaario_numero"));
            inventaarionNumeroTopic.setBaseName("inventaarionumero (teos tunniste)");
        }
        
        Topic teosNimiTopic = tm.getTopic("http://www.muusa.net/E55.Type_teosnimi");
        
        Topic workType=tm.getTopic("http://www.muusa.net/Teos");
        Topic nameAssociationType=tm.getTopic("http://www.muusa.net/P102.has_title"); // "http://www.muusa.net/P102.has_title"
        Topic titleOf=tm.getTopic("http://www.muusa.net/P102.has_title_role_0"); // "http://www.muusa.net/P102.has_title_role_0"
        if(nameAssociationType==null || workType==null || titleOf==null){
            log("Couldn't find all needed topics.");
            return tm;
        }
        int counter=0;
        Iterator workiter=tm.getTopicsOfType(workType).iterator();
        
        Topic work = null;
        Iterator nameiter = null;
        
        Association nameAssociation = null;
        Topic nameT = null;
        String basename = null;
        Topic nameType = null;
        Topic lang = null;
        boolean remove = false;

        Collection<Association> nameAssociations = null;
        Iterator<Association> nameAssociationIter = null;
        
        // Iterate through artworks!
        while(workiter.hasNext() && !forceStop()) {
            work = (Topic)workiter.next();
            hlog("Processing artwork '"+ getTopicName(work) +"'.");
            nameiter=new ArrayList(work.getAssociations(nameAssociationType)).iterator();
            
            // Make basename (inventory number) occurrence of the artwork topic!
            work.setData(inventaarionNumeroTopic, indepLang, work.getBaseName());
                       
            // Iterate through artwork's name associations!
            while(nameiter.hasNext() && !forceStop()) {
                nameAssociation = (Association)nameiter.next();
                nameT = nameAssociation.getPlayer(titleOf);
                if(nameT==null) continue;
                
                nameType = TopicTools.getFirstPlayer(nameT, "http://www.muusa.net/P2.has_type", "http://www.muusa.net/P2.has_type_role_1");
                if(nameType.equals(teosNimiTopic)) {
                    lang = TopicTools.getFirstPlayer(nameT, "http://www.muusa.net/P72.has_language", "http://www.muusa.net/P72.has_language_role_1");
                    if(lang != null) {
                        if(! nameT.getBaseName().startsWith("E35.Title")) {
                            remove = false;
                            basename = nameT.getBaseName();
                            if(basename == null || basename.length() == 0) continue;
                            
                            if(lang.mergesWithTopic(fiLang)) {
                                //log("Found fi name '"+basename+"'.");
                                work.setDisplayName("fi", basename);
                                work.setBaseName(basename + " ("+work.getBaseName()+")");
                                remove = true;
                            }
                            if(lang.mergesWithTopic(swLang)) {
                                //log("Found se name '"+basename+"'.");
                                work.setDisplayName("se", basename);
                                remove = true;
                            }
                            if(lang.mergesWithTopic(enLang)) {
                                //log("Found en name '"+basename+"'.");
                                work.setDisplayName("en", basename);
                                remove = true;
                            }
                            if(remove) {
                                /*
                                // IT IS ASSUMED THAT DELETING TOPIC DOES ASSOCIATION DELETION FASTER!
                                nameAssociations = nameT.getAssociations();
                                nameAssociationIter = nameAssociations.iterator();
                                while( nameAssociationIter.hasNext() ) {
                                    try {
                                        nameAssociationIter.next().remove();
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                */
                                try {
                                    nameT.remove();
                                } catch(TopicInUseException tiue) {
                                    log(tiue);
                                }
                                counter++;
                            }
                        }
                    }
                }
            }
        }
        log("Fixed "+counter+" names");
        log("Deleted "+counter+" name associations");
        return tm;
    }
}
