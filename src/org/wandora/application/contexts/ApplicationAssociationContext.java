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
 * ApplicationAssociationContext.java
 *
 * Created on 23. huhtikuuta 2006, 13:38
 *
 */

package org.wandora.application.contexts;

import org.wandora.application.*;
import org.wandora.topicmap.*;
import java.awt.event.*;
import java.util.*;


/**
 *
 * @author akivela
 */
public class ApplicationAssociationContext implements Context {
    
    private Object contextSource;
    protected WandoraTool contextOwner = null;
    protected ActionEvent actionEvent = null;
    protected Wandora admin = null;  
    
    
    
    /** Creates a new instance of ApplicationAssociationContext */
    public ApplicationAssociationContext() {
    }

    @Override
    public Iterator getContextObjects() {
        ArrayList contextAssociations = new ArrayList();
        try {
            Wandora w = (Wandora) contextSource;
            Topic currentTopic = w.getOpenTopic();
            if(currentTopic != null) {
                return currentTopic.getAssociations().iterator();
            }
        }
        catch (Exception e) {
            log(e);
        }
        return contextAssociations.iterator();
    }
    
    
    
    @Override
    public ActionEvent getContextEvent() {
        return actionEvent;
    }
    
    
    
    @Override
    public void initialize(Wandora admin, ActionEvent actionEvent, WandoraTool contextOwner) {
        this.admin = admin;
        this.actionEvent = actionEvent;
        this.contextOwner = contextOwner;
        
        setContextSource( admin );
    }
    
    
    @Override
    public void setContextSource(Object proposedContextSource) {
        contextSource = proposedContextSource;
    }
    
    @Override
    public Object getContextSource() {
        return contextSource;
    }
    
    
    
    // -------------------------------------------------------------------------
    
    
    
    public void log(Exception e) {
        if(contextOwner != null) contextOwner.log(e);
        else e.printStackTrace();
    }


}
