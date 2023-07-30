/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
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
 * DeleteAssociations.java
 *
 * Created on 23. huhtikuuta 2006, 15:23
 *
 */

package org.wandora.application.tools.associations;


import java.util.Iterator;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.application.contexts.AssociationContext;
import org.wandora.application.contexts.Context;
import org.wandora.application.gui.WandoraOptionPane;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMapException;



/**
 * <p>
 * Deletes given associations. Wandora user must confirm deletion.
 * </p>
 * 
 * @author akivela
 */
public class DeleteAssociations extends AbstractWandoraTool implements WandoraTool {

	private static final long serialVersionUID = 1L;
	
    public boolean forceDelete = true;
    public boolean confirm = true;
    public boolean shouldContinue = true;
    protected Wandora wandora = null;
    private boolean requiresRefresh = false;
    
    
    public DeleteAssociations() {
        setContext(new AssociationContext());
    }
    public DeleteAssociations(Context preferredContext) {
        setContext(preferredContext);
    }
    
    @Override
    public String getName() {
        return "Delete associations";
    }
    @Override
    public String getDescription() {
        return "Deletes context associations.";
    }

    
    @Override
    public void execute(Wandora wandora, Context context)  throws TopicMapException {
        this.wandora = wandora;
        Iterator associations = context.getContextObjects();
        Association association = null;
        int count = 0;
        shouldContinue = true;
        confirm = true;
        requiresRefresh = false;
        yesToAll = false;
        
 
        if(associations != null && associations.hasNext()) {
            while(associations.hasNext() && shouldContinue) {
                association = (Association) associations.next();
                if(association != null && !association.isRemoved()) {
                    if(shouldDelete(association)) {
                        try {
                            association.remove();
                            count++;
                            requiresRefresh = true;
                            if(count == 50) {
                                setDefaultLogger();
                            }
                        }
                        catch(Exception e2) {
                            log(e2);
                        }
                    }
                }
            }
        }
        if(count > 10) log("Total " + count + " associations deleted!");
        if(count >= 50) setState(WAIT);
    }
 
    
    
    
    
    public boolean shouldDelete(Association association)  throws TopicMapException {
        if(confirm) {
            return confirmDelete(association);
        }
        else {
            return true;
        }
    }
    

    public boolean yesToAll = false;
    public boolean confirmDelete(Association association)  throws TopicMapException {
        if(yesToAll) return true;
        if(association != null) {
            String typeName = association.getType() != null ? association.getType().getBaseName() : "";
            Iterator<Topic> roleIterator = association.getRoles().iterator();
            StringBuilder playerDescription = new StringBuilder("");
            while(roleIterator.hasNext()) {
                Topic role = (Topic) roleIterator.next();
                Topic player = association.getPlayer(role);
                playerDescription.append("'").append(getTopicName(player)).append("'");
                if(roleIterator.hasNext()) playerDescription.append(" and ");
            }
            String confirmMessage = "Would you like delete '" + typeName + "' association between\n" + playerDescription.toString() + "?";
            int answer = WandoraOptionPane.showConfirmDialog(wandora, confirmMessage,"Confirm delete", WandoraOptionPane.YES_TO_ALL_NO_CANCEL_OPTION );
            if(answer == WandoraOptionPane.YES_OPTION) {
                return true;
            }
            if(answer == WandoraOptionPane.YES_TO_ALL_OPTION) {
                yesToAll = true;
                return true;
            }
            else if(answer == WandoraOptionPane.CANCEL_OPTION) {
                shouldContinue = false;
            }
        }
        return false;
    }
    
    
    @Override
    public boolean requiresRefresh() {
        return requiresRefresh;
    }
}
