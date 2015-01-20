/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2015 Wandora Team
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
 * MultiContextCollected.java
 *
 * Created on 8. huhtikuuta 2006, 20:36
 *
 */

package org.wandora.application.contexts;


import org.wandora.application.*;
import java.awt.event.*;
import java.util.*;

/**
 *
 * @author akivela
 */
public class MultiContextCollected implements Context {
    private ArrayList<Context> multiContext = new ArrayList();
    public boolean removeDuplicates = true;
    private Object contextSource;
    private ActionEvent contextEvent;
    
    
    
    /**
     * Creates a new instance of MultiContextCollected
     */
    public MultiContextCollected() {
    }
    public MultiContextCollected(Context context) {
        addContext(context);
    }
    public MultiContextCollected(ArrayList<Context> contexts) {
        addContexts(contexts);
    }
    
    
    
    //--------------------------------------------------------------------------
    
    
    
    public void addContext(Context context) {
        multiContext.add(context);
    }
    public void addContexts(ArrayList<Context> contexts) {
        for(Iterator<Context> contextIterator=contexts.iterator(); contextIterator.hasNext(); ) {
            multiContext.add(contextIterator.next());
        }
    }
    public void clearContext() {
        multiContext = new ArrayList();
    }

    
    
    
    @Override
    public Iterator getContextObjects() {
        Collection contextObjects = new ArrayList();
        Iterator tempContextObjects;
        Object contextObject = null;
        Context context = null;
        for(Iterator<Context> contextIterator=multiContext.iterator(); contextIterator.hasNext(); ) {
            context = contextIterator.next();
            if(context != null) {
                if(removeDuplicates) {
                    tempContextObjects = context.getContextObjects();
                    while(tempContextObjects.hasNext()) {
                        contextObject = tempContextObjects.next();
                        if( !contextObjects.contains(contextObject) ) {
                            contextObjects.add(contextObject);
                        }
                    }
                }
                else {
                    tempContextObjects = context.getContextObjects();
                    while(tempContextObjects.hasNext()) {
                        contextObjects.add( tempContextObjects.next() );
                    }
                }
            }
        }
        return contextObjects.iterator();
    }
    
    
    @Override
    public ActionEvent getContextEvent() {
        return contextEvent;
    }
    
    
    @Override
    public void setContextSource(Object proposedContextSource) {
        contextSource = proposedContextSource;
        Context context = null;
        for(Iterator<Context> contextIterator=multiContext.iterator(); contextIterator.hasNext(); ) {
            context = contextIterator.next();
            if(context != null) {
                context.setContextSource(proposedContextSource);
            }
        }
    }
    
    
    @Override
    public Object getContextSource() {
        return contextSource;
    }

    
    @Override
    public void initialize(Wandora admin, ActionEvent actionEvent, WandoraTool contextOwner) {
        contextEvent = actionEvent;
        Context context = null;
        for(Iterator<Context> contextIterator=multiContext.iterator(); contextIterator.hasNext(); ) {
            context = contextIterator.next();
            if(context != null) {
                context.initialize(admin, actionEvent, contextOwner);
            }
        }
    }

}
