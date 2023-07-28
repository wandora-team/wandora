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
 * TransposeAssociations.java
 *
 * Created on 7. marraskuuta 2007, 14:51
 *
 */

package org.wandora.application.tools.associations;

import org.wandora.application.gui.table.AssociationTable;
import org.wandora.application.tools.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.contexts.*;
import java.util.*;
import org.wandora.application.gui.WandoraOptionPane;



/**
 * <p>
 * This rather complicated tool modifies given associations by rotating association
 * matrix (i.e. association table) 90 degrees clockwise. For example, lets think
 * association matrix:
 * </p>
 * <code>
 * 
 *     atype
 *     r1     r2 
 *     pa1    pa2
 *     pb1    pb2
 *     pc1    pc2
 * 
 * </code>
 * <p>
 * Here atype is association type, r? are role topics and p?? are player topics.
 * Transposing these three association generates association matrix:
 * </p>
 * <code>
 * 
 *     atype
 *     r1     pa1     pb1    pc1
 *     r2     pa2     pb2    pc2
 * 
 * </code>
 * <p>
 * where three binary associations have turned into one 4-ary association with
 * players r2, pa2, pb2, and pc2. Roles are r1, pa1, pb1, and pc1. Association type
 * remains.
 * </p>
 * 
 * @author akivela
 */


public class TransposeAssociations extends AbstractWandoraTool implements WandoraTool {


	private static final long serialVersionUID = 1L;



	public TransposeAssociations() {
        setContext(new AssociationContext());
    }
    
    
    public TransposeAssociations(Context preferredContext) {
        setContext(preferredContext);
    }
    
    

    @Override
    public String getName() {
        return "Transpose associations";
    }

    @Override
    public String getDescription() {
        return "Rotates associations 90 degrees clockwise. "+
               "First players of associations are new roles while old roles become new players.";
    }

    
    
    @Override
    public void execute(Wandora wandora, Context context) {      
        try {
            TopicMap topicmap = wandora.getTopicMap();
            
            int a = WandoraOptionPane.showConfirmDialog(wandora, "Transposing association interprets selected associations as a topic matrix and rotates the matrix 90 degrees counter clock wise. New associations will be created using rotated matrix. First row of rotated matrix is interpreted as role topics and old roles will become association player. Finally old associations will be deleted. Are you sure you want transpose selected associations?", "Transpose associations?");
            if(a != WandoraOptionPane.YES_OPTION) return;
            
            setDefaultLogger();
            
            if(context instanceof AssociationContext) { // ASSOCIATION CONTEXT!!
                AssociationTable associationTable = (AssociationTable) context.getContextSource();
                Collection associations = associationTable.getSelectedAssociations();                
                Topic associationType = ((Association) associations.iterator().next()).getType();
                Topic[][] newAssociations = new Topic[associationTable.getColumnCount()][associations.size()+1];
                
                int width = associationTable.getColumnCount();
                int height = associations.size()+1;
                
                // BUILDING TOPIC MATRIX FROM ASSOCIATIONS
                log("Building topic map matrix...");
                for(int x=0; x<width && !forceStop(); x++) {
                    Object o=associationTable.getColumnAt(associationTable.convertColumnIndexToModel(x));
                    if(o instanceof Topic) newAssociations[x][0] = (Topic)o;
                    else newAssociations[x][0] = null;
                }
                for(int y=1; y<height && !forceStop(); y++) {
                    for(int x=0; x<width; x++) {
                        newAssociations[x][y] = associationTable.getTopicAt(y-1,x);
                        //log(" ("+x+","+y+")="+newAssociations[x][y].getBaseName());
                    }
                }
                
                // CREATING ASSOCIATIONS WITH TOPIC MATRIX
                if(!forceStop()) {
                    log("Creating new transposed associations...");
                    int createCount = 0;
                    Topic role = null;
                    Topic player = null;
                    Association association = null;
                    for(int x=1; x<width && !forceStop(); x++) {
                        try {
                            createCount++;
                            association = topicmap.createAssociation(associationType);
                            for(int y=0; y<height && !forceStop(); y++) {
                                role = newAssociations[0][y];
                                player = newAssociations[x][y];
                                association.addPlayer(player,role);
                            }
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                    log("Created "+createCount+" associations.");
                }
                
                
                if(!forceStop()) {
                    int removeCount = 0;
                    log("Removing old associations...");
                    Association association = null;
                    Iterator associationIterator = associations.iterator();
                    while(associationIterator.hasNext() && !forceStop()) {
                        try {
                            association = (Association) associationIterator.next();
                            association.remove();
                            removeCount++;
                        }
                        catch(Exception e) {
                            log(e);
                        }
                    }
                    log("Removed "+removeCount+" associations.");
                }
            }
            else {
                log("Illegal context found. Expecting association context.");
            }
        }
        catch(Exception e) {
            log(e);
        }
        setState(WAIT);
    }
}
