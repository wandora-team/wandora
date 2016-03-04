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
 * MergePlayers.java
 *
 * Created on 2012-02-04
 *
 */
package org.wandora.application.tools.associations;


import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.table.AssociationTable;
import java.util.*;



/**
 *
 * @author akivela
 */


public class MergePlayers extends AbstractWandoraTool implements WandoraTool {
    private boolean requiresRefresh = false;
    

    public MergePlayers() {
        setContext(new AssociationContext());
    }
    public MergePlayers(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Merge players";
    }
    @Override
    public String getDescription() {
        return "Merge selected association player topics per association. "+
               "Doesn't remove duplicate players in associations.";
    }
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
    
    
    @Override
    public void execute(Wandora admin, Context context)  throws TopicMapException {
        requiresRefresh = false;
        Iterator associations = context.getContextObjects();
        Association association = null;
        int count = 0;

        try {
            if(associations != null && associations.hasNext()) {

                AssociationTable associationTable = null;
                Object contextSource = context.getContextSource();
                if(contextSource instanceof AssociationTable) {
                    associationTable = (AssociationTable) context.getContextSource();
                }
                if(associationTable != null) {
                    Map<Association,ArrayList<Topic>> associationsWithSelectedRoles = associationTable.getSelectedAssociationsWithSelectedRoles();
                    Set<Association> associationKeys = associationsWithSelectedRoles.keySet();
                    Iterator<Association> associationKeyIterator = associationKeys.iterator();
                    while(associationKeyIterator.hasNext() && !forceStop()) {
                        association = (Association) associationKeyIterator.next();
                        if(association != null && !association.isRemoved()) {
                            ArrayList<Topic> mergeRoles = associationsWithSelectedRoles.get(association);
                            Topic mergedTopic = null;
                            for(Topic mergeRole : mergeRoles) {
                                Topic t = association.getPlayer(mergeRole);
                                if(t != null && !t.isRemoved()) {
                                    if(mergedTopic == null) mergedTopic = t;
                                    else {
                                        Locator tsi = t.getOneSubjectIdentifier();
                                        mergedTopic.addSubjectIdentifier(tsi);
                                        requiresRefresh = true;
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                    log("Total " + count + " associations processed.");
                }
            }
        }   
        catch(Exception e) {
            singleLog(e);
        }
    }

}
