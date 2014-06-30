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
 * 
 * MakeInstancesWithAssociations.java
 *
 * Created on 6. heinäkuuta 2006, 14:19
 *
 */

package org.wandora.application.tools;
import org.wandora.application.contexts.*;
import org.wandora.application.gui.*;
import org.wandora.topicmap.*;
import org.wandora.application.*;

import java.util.*;


/**
 * Makes players of one role of association instances to players 
 * of another role in the association.
 *
 * @author olli
 */
public class MakeInstancesWithAssociations extends AbstractWandoraTool {

    
    /** Creates a new instance of MakeInstancesWithAssociations */
    public MakeInstancesWithAssociations() {
        this(new AssociationContext());
    }
    public MakeInstancesWithAssociations(Context preferredContext) {
        setContext(preferredContext);
    }

    @Override
    public String getName() {
        return "Make instances with associations";
    }


    @Override
    public String getDescription() {
        return "Makes players of one role of association instances to players of another role in the association.";
    }

    
    @Override
    public void execute(Wandora wandora, Context context) {
        Iterator associations=null;
        if(context instanceof AssociationContext) {
            associations = context.getContextObjects();
        }
        else return;

        Topic classRole=null;
        Topic instanceRole=null;
        
        try{
            while(associations.hasNext()){
                Association a=(Association)associations.next();
                
                if(classRole==null){
                    Object[] roles=a.getRoles().toArray();
                    classRole=(Topic)WandoraOptionPane.showOptionDialog(wandora,"Select role topic for classes","Select topic",WandoraOptionPane.YES_NO_OPTION,roles,roles[0]);
                    //classRole=admin.showTopicFinder("Select role of class topic");                
                    if(classRole==null) return;
                }
                if(instanceRole==null){
                    Object[] roles=a.getRoles().toArray();
                    instanceRole=(Topic)WandoraOptionPane.showOptionDialog(wandora,"Select role topic for instances","Select topic",WandoraOptionPane.YES_NO_OPTION,roles,roles[0]);
                    //instanceRole=admin.showTopicFinder("Select role of instance topic");                
                    if(instanceRole==null) return;
                }
                
                Topic c=a.getPlayer(classRole);
                Topic i=a.getPlayer(instanceRole);
                if(c!=null && i!=null){
                    i.addType(c);
                }
            }
        }
        catch(Exception e){
            log(e);
        }
        
    }    
}
