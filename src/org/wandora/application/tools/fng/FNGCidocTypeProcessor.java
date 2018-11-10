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
 * FNGCidocTypeProcessor.java
 *
 * Created on August 25, 2004, 10:09 AM
 */

package org.wandora.application.tools.fng;

import org.wandora.application.contexts.Context;
import org.wandora.topicmap.TopicInUseException;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.Association;
import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.topicmap.Topic;
import org.wandora.application.tools.AbstractWandoraTool;

import java.util.*;


/**
 * Tool converts given is-type-of associations into topic map's native
 * instance-of relation. 
 *
 * @author  olli, akivela
 */


public class FNGCidocTypeProcessor extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;

	private String[] makeTypes;
    private String[] deleteTypes;
    
    /** Creates a new instance of FNGCidocTypeProcessor */
    public FNGCidocTypeProcessor(String[] makeTypes,String[] deleteTypes) {
        this.makeTypes=makeTypes;
        this.deleteTypes=deleteTypes;
    }
    
    
    public String getName() {
        return "FNG Cidoc Type Processor";
    }
    
    public String getDescription() {
        return "Converts is-type-of associations into topic map's native instance-of relation.";
    }
    

    public void execute(Wandora admin, Context context) throws TopicMapException {
        
        setDefaultLogger();
        TopicMap tm = admin.getTopicMap();
        log("Applying FNGCidocTypeProcessor filter");
        
        Topic listedIn=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P71B_is_listed_in");
        Topic hasType=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#E55_Type");
        Topic isType=tm.getTopic("http://www.fng.fi/muusa/FNG_CIDOC_v3.4.dtd#P2B_is_type_of");
        Topic work=tm.getTopic("http://www.fng.fi/wandora/wandora-fng.xtm#kp-teos");
        if(listedIn==null || hasType==null || isType==null || work==null){
            log("Couldn't find all needed topics.");
            return;
        }
        Collection<Topic> makeC=new LinkedHashSet<>();
        for(int i=0;i<makeTypes.length;i++){
            Topic t=tm.getTopic(makeTypes[i]);
            if(t==null){
                log("Couldn't find topic "+makeTypes[i]+". Aborting.");
                return;
            }
            makeC.add(t);
        }
        Collection<Topic> deleteC=new LinkedHashSet<>();
        for(int i=0;i<deleteTypes.length;i++){
            Topic t=tm.getTopic(deleteTypes[i]);
            if(t==null){
                log("Couldn't find topic "+makeTypes[i]+". Aborting.");
                return;
            }
            deleteC.add(t);
        }
        
        
        int counter=0;
        int counter2=0;
        Iterator<Association> iter=new ArrayList(tm.getAssociationsOfType(hasType)).iterator();
        while(iter.hasNext() && !forceStop()) {
            Association a=(Association)iter.next();
            Topic l=a.getPlayer(listedIn);
            Topic c=a.getPlayer(hasType);
            Topic t=a.getPlayer(isType);
            if(t==null) continue;
            if( (l!=null && makeC.contains(l)) || (makeC.contains(c)) ){
                t.addType(c);
                a.remove();
                counter++;
            }
            else if( (l!=null && deleteC.contains(l)) || (deleteC.contains(c)) ){
                try{
                    t.remove();
                    counter2++;
                }catch(TopicInUseException tiue){}
                a.remove();
            }
        }
        log("Moved "+counter+" types from associations to instance-of and deleted "+counter2+" types and associations");
    }
}
