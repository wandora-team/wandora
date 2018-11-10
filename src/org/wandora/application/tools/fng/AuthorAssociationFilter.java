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
 * AuthorAssociationFilter.java
 *
 * Created on August 25, 2004, 2:56 PM
 */

package org.wandora.application.tools.fng;


import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.util.*;


/**
 *
 * @author  olli
 */
public class AuthorAssociationFilter extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	Wandora wandora = null;
    
    
    /** Creates a new instance of AuthorAssociationFilter */
    public AuthorAssociationFilter() {
    }
    
    
    public String getName() {
        return "FNG Author association filter";
    }
    

    
    
    public void execute(Wandora wandora, Context context) {      
        this.wandora = wandora;
        setDefaultLogger();

        try {
            log("Processing artwork authors...");
            process(wandora.getTopicMap());
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
    
    

    
    
    public TopicMap process(TopicMap tm) throws TopicMapException {
        long startTime = System.currentTimeMillis();
        
        Topic newProducedBy=tm.getTopic("http://wandora.org/si/has_produced");
        if(newProducedBy == null) {
            newProducedBy = tm.createTopic();
            newProducedBy.addSubjectIdentifier(new Locator("http://wandora.org/si/has_produced"));
            newProducedBy.setBaseName("Teoksen tekijä");
        }
        Topic newProducedByRole=tm.getTopic("http://wandora.org/si/author_role");
        if(newProducedByRole == null) {
            newProducedByRole = tm.createTopic();
            newProducedByRole.addSubjectIdentifier(new Locator("http://wandora.org/si/author_role"));
            newProducedByRole.setBaseName("Teoksen tekijän rooli");
        }
        
        Topic newTechnique=tm.getTopic("http://wandora.org/si/technique");
        if(newTechnique == null) {
            newTechnique = tm.createTopic();
            newTechnique.addSubjectIdentifier(new Locator("http://wandora.org/si/technique"));
            newTechnique.setBaseName("Teoksen tekniikka");
        }
        
        Topic newTiming=tm.getTopic("http://wandora.org/si/time_apellation");
        if(newTiming == null) {
            newTiming = tm.createTopic();
            newTiming.addSubjectIdentifier(new Locator("http://wandora.org/si/time_apellation"));
            newTiming.setBaseName("Teoksen ajoitus");
        }
        
        Topic order = tm.getTopic("http://www.muusa.net/Order");
        Topic producedBy=tm.getTopic("http://www.muusa.net/P108.has_produced");
        Topic producedByRole=tm.getTopic("http://www.muusa.net/P108.has_produced_role_1");
        Topic techniqueOf=tm.getTopic("http://www.muusa.net/P32.used_general_technique");
        Topic timeAppellation=tm.getTopic("http://www.muusa.net/P4.has_time-span");
        Topic work=tm.getTopic("http://www.muusa.net/Teos");
        if(producedBy==null || techniqueOf==null || timeAppellation==null || work==null){
            log("Couldn't find all needed topics.");
            return tm;
        }
        
        int counter = 0;
        int authorCounter = 0;
        int techniqueCounter = 0;
        int timingCounter = 0;
               
        
        Topic artwork = null;
        Iterator artworkAssociations = null;
        Association artworkAssociation = null;
        Topic productionEvent = null;
        
        Topic authorAType = tm.getTopic("http://www.muusa.net/P14.carried_out_by");
        Topic authorRole = tm.getTopic("http://www.muusa.net/P14.carried_out_by_role_0");
        Topic authorRoleRole = tm.getTopic("http://www.muusa.net/P14.carried_out_by_role_3");
        Iterator authorAssociations = null;
        Association authorAssociation = null;
        Topic author = null;
        Topic authorR = null;
        Association authorA = null;
        
        Topic techniqueRole = tm.getTopic("http://www.muusa.net/P32.used_general_technique_role_1");
        Association techniqueAssociation = null;
        Topic technique = null;
        Topic techniqueOrder = null;
        Association techniqueA = null;
        
        Topic timingRole = tm.getTopic("http://www.muusa.net/P4.has_time-span_role_1");
        Association timingAssociation = null;
        Topic time = null;
        Association timingA = null;
        
        
        Iterator iter=tm.getTopicsOfType(work).iterator();
        while(iter.hasNext() && !forceStop()){
            artwork=(Topic)iter.next();
            hlog("Processing artwork '"+getTopicName(artwork)+"'.");
            artworkAssociations=artwork.getAssociations(producedBy).iterator();
            while(artworkAssociations.hasNext() && !forceStop()) {
                artworkAssociation=(Association)artworkAssociations.next();
                productionEvent = artworkAssociation.getPlayer(producedByRole);
                
                authorAssociations=productionEvent.getAssociations(authorAType).iterator();
                while(authorAssociations.hasNext()) {
                    authorAssociation=(Association)authorAssociations.next();
                    author = authorAssociation.getPlayer(authorRole);
                    authorR = authorAssociation.getPlayer(authorRoleRole);
                    if(author != null && authorR != null) {
                        authorA=tm.createAssociation(newProducedBy);
                        authorA.addPlayer(artwork, work);
                        authorA.addPlayer(author, newProducedBy);
                        authorA.addPlayer(authorR, newProducedByRole);
                        //authorAssociations.remove();
                        authorCounter++;
                    }
                }
                
                Iterator techniqueAssociations=productionEvent.getAssociations(techniqueOf).iterator();
                while(techniqueAssociations.hasNext()) {
                    techniqueAssociation=(Association)techniqueAssociations.next();
                    technique=techniqueAssociation.getPlayer(techniqueRole);
                    techniqueOrder = techniqueAssociation.getPlayer(order);
                    
                    if(technique != null && techniqueOrder != null) {
                        techniqueA=tm.createAssociation(newTechnique);
                        techniqueA.addPlayer(artwork, work);
                        techniqueA.addPlayer(technique, newTechnique);
                        techniqueA.addPlayer(techniqueOrder, order);
                        //techniqueAssociation.remove();
                        techniqueCounter++;
                    }
                }
                    
                Iterator timingAssociations=productionEvent.getAssociations(timeAppellation).iterator();
                while(timingAssociations.hasNext()) {
                    timingAssociation=(Association)timingAssociations.next();
                    time=timingAssociation.getPlayer(timingRole);
                    
                    if(time != null) {
                        timingA=tm.createAssociation(newTiming);
                        timingA.addPlayer(artwork, work);
                        timingA.addPlayer(time, newTiming);
                        //timingAssociation.remove();
                        timingCounter++;
                    }
                }
                
                productionEvent.remove();
                //artworkAssociation.remove();
                counter++;
            }
        }
        long endTime = System.currentTimeMillis();
        
        log("Processed "+counter+" artworks.");
        log("Processed "+authorCounter+" production events.");
        log("Processed "+techniqueCounter+" production event techniques.");
        log("Processed "+timingCounter+" production event timings.");
        log("Processing took "+((endTime-startTime)/1000)+" seconds.");

        return tm;
    }    
}
