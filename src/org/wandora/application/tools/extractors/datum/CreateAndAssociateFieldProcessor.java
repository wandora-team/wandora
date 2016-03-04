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
 * CreateAndAssociatieFieldProcessor.java
 *
 * Created on 24. marraskuuta 2004, 18:15
 */

package org.wandora.application.tools.extractors.datum;
import org.wandora.application.tools.extractors.*;
import org.wandora.topicmap.*;
import java.util.*;
/**
 *
 * @author  olli
 */
public class CreateAndAssociateFieldProcessor implements FieldProcessor {
    
    protected ExtractionHelper helper;
    protected String otherField;
    protected String otherRole;
    protected String associationType;
    protected String thisRole;
    protected boolean setBaseName;
    protected String topicType;
    /** Creates a new instance of CreateAndAssociatieFieldProcessor */
    public CreateAndAssociateFieldProcessor(ExtractionHelper helper, String otherField, String otherRole, String associationType, String thisRole, boolean setBaseName) {
        this(helper,otherField,otherRole,associationType,thisRole,setBaseName,null);
    }
    public CreateAndAssociateFieldProcessor(ExtractionHelper helper, String otherField, String otherRole, String associationType, String thisRole, boolean setBaseName, String topicType) {
        this.helper=helper;
        this.otherField=otherField;
        this.otherRole=otherRole;
        this.associationType=associationType;
        this.thisRole=thisRole;
        this.setBaseName=setBaseName;
        this.topicType=topicType;
    }
    
    public void processDatum(java.util.Map datum, String field, org.wandora.topicmap.TopicMap tm, org.wandora.piccolo.Logger logger) throws ExtractionException {
        try{
            Collection thists=helper.getOrCreateTopics(datum, field, tm,setBaseName);
            if(thists==null){
                logger.writelog("WRN","Null value for field "+field);
                return;
            }
            Collection otherts=helper.getOrCreateTopics(datum,otherField,tm,false);
            if(otherts==null){
                logger.writelog("WRN","Null value for field "+otherField);
                return;
            }

            Topic type=null;
            if(topicType!=null) type=helper.getOrCreateTopic(tm,topicType);
            Iterator iter=thists.iterator();
            while(iter.hasNext()){
                Topic thist=(Topic)iter.next();
                if(type!=null) thist.addType(type);
                Iterator iter2=otherts.iterator();
                while(iter2.hasNext()){
                    Topic othert=(Topic)iter2.next();
                    Association a=tm.createAssociation(helper.getOrCreateTopic(tm,associationType));
                    a.addPlayer(thist,helper.getOrCreateTopic(tm,thisRole));
                    a.addPlayer(othert,helper.getOrCreateTopic(tm,otherRole));
                }
            }
        }catch(TopicMapException tme){throw new ExtractionException(tme);}
    }
    
}
