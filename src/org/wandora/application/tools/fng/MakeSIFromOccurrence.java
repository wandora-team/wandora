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
import org.wandora.piccolo.Logger;
import java.util.*;


/**
 *
 * @author  akivela
 */
public class MakeSIFromOccurrence extends AbstractWandoraTool implements WandoraTool {
    Wandora admin = null;
    
    
    /** Creates a new instance of MakeSIFromOccurrence */
    public MakeSIFromOccurrence() {
    }

    
    public String getName() {
        return "Make SI from occurrence";
    }
    
    
    public void execute(Wandora admin, Context context) {      

        try {
            log("Processing works...");
            process(admin.getTopicMap(), Logger.getLogger());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    

    public TopicMap process(TopicMap tm, Logger logger) throws TopicMapException {
        logger.writelog("Applying MakeSIFromOccurrence filter");
        
        Topic indepLang = tm.getTopic("http://wandora.org/si/core/lang-independent");
        Topic fiLang = tm.getTopic("http://www.muusa.net/E55.Type_fi");
        Topic swLang = tm.getTopic("http://www.muusa.net/E55.Type_sw");
        Topic enLang = tm.getTopic("http://www.muusa.net/E55.Type_en");
        
        Topic inventaarionNumeroTopic = tm.getTopic("http://www.muusa.net/inventaarionumero");
        if(inventaarionNumeroTopic == null) {
            inventaarionNumeroTopic = tm.createTopic();
            inventaarionNumeroTopic.addSubjectIdentifier(new Locator("http://www.muusa.net/inventaario_numero"));
            inventaarionNumeroTopic.setBaseName("inventaarionumero (teos tunniste)");
        }
        
        Topic workType=tm.getTopic("http://www.muusa.net/Teos");
        if(workType==null){
            logger.writelog("Couldn't find all needed topics.");
            return tm;
        }
        int counter=0;
        Iterator workiter=tm.getTopicsOfType(workType).iterator();
        
        //Iterate through artworks!
        while(workiter.hasNext()){
            Topic work = (Topic)workiter.next();
            try {
                // Get inventory number occurrence!
                String inventoryNumber = work.getData(inventaarionNumeroTopic, indepLang);
                if(inventoryNumber != null) {
                    if(inventoryNumber.length() > 0) {
                        String newSI = TopicTools.cleanDirtyLocator("http://www.muusa.net/inventory/" + inventoryNumber);
                        if( tm.getTopic(newSI) == null ) {
                            work.addSubjectIdentifier(new Locator(newSI));
                            counter++;
                        }
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        logger.writelog("Added "+counter+" SIs");
        return tm;
    }
}
