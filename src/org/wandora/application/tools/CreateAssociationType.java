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
 * CreateAssociationType.java
 *
 * Created on 15. kesäkuuta 2006, 10:36
 *
 */

package org.wandora.application.tools;
import org.wandora.topicmap.*;
import org.wandora.application.*;
import org.wandora.application.gui.*;
import org.wandora.application.contexts.*;
import java.util.*;

/**
 *
 * @author olli
 */
public class CreateAssociationType extends AbstractWandoraTool {
    
    /** Creates a new instance of CreateAssociationType */
    public CreateAssociationType(Context preferredContext) {
        setContext(preferredContext);
    }
    public CreateAssociationType() {
        setContext(new TopicContext());        
    }

    @Override
    public String getName() {
        return "Create association type";
    }

    @Override
    public String getDescription() {
        return "Opens an editor used to create a new association type. ";
    }
    
    @Override
    public void execute(Wandora wandora, Context context) throws TopicMapException  {
        Iterator contextObjects = context.getContextObjects();
        Vector<Topic> objects=new Vector<Topic>();
        Association a=null;
        if(contextObjects!=null && contextObjects.hasNext()){
            Object o=contextObjects.next();
            if(o instanceof Association) a=(Association)o;
            else if(o instanceof Topic){
                objects.add((Topic)o);
                while(contextObjects.hasNext()){
                    objects.add((Topic)contextObjects.next());
                }
            }
        }
        CreateAssociationTypePrompt d=null;
        if(a!=null) d=new CreateAssociationTypePrompt(wandora,a);
        else if(objects.size()>0) d=new CreateAssociationTypePrompt(wandora,objects);
        else d=new CreateAssociationTypePrompt(wandora);
        d.setVisible(true);
    }
  
    
}
