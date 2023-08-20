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
 * ApplicationContext.java
 *
 * Created on 22. huhtikuuta 2006, 10:38
 *
 */

package org.wandora.application.contexts;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.wandora.application.Wandora;
import org.wandora.application.WandoraTool;
import org.wandora.topicmap.Topic;

/**
 * ApplicationContext uses application ie. the Wandora as a context source.
 * Context object is a topic opened in topic panel.
 *
 * @author akivela
 */


public class ApplicationContext implements Context {
    
    private Object contextSource;
    protected WandoraTool contextOwner = null;
    protected ActionEvent actionEvent = null;
    protected Wandora wandora = null;  

    
    /** Creates a new instance of ApplicationContext */
    public ApplicationContext() {
    }

    
    
    
    @Override
    public Iterator getContextObjects() {
        List<Topic> contextTopics = new ArrayList<>();
        try {
            Wandora w = (Wandora) contextSource;
            Topic currentTopic = w.getOpenTopic();
            if(currentTopic != null) {
                contextTopics.add(currentTopic);
            }
        }
        catch (Exception e) {
            log(e);
        }
        return contextTopics.iterator();
    }
    
    
    @Override
    public ActionEvent getContextEvent() {
        return actionEvent;
    }
    
    
    @Override
    public void initialize(Wandora wandora, ActionEvent actionEvent, WandoraTool contextOwner) {
        this.wandora = wandora;
        this.actionEvent = actionEvent;
        this.contextOwner = contextOwner;
        
        setContextSource( wandora );
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
