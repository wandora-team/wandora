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
 * OccurrenceNode.java
 *
 * Created on 5. kesäkuuta 2007, 15:18
 *
 */

package org.wandora.application.gui.topicpanels.graphpanel;

import java.awt.Color;
import java.util.*;
import org.wandora.application.gui.topicstringify.TopicToString;
import org.wandora.topicmap.*;
import static org.wandora.utils.Tuples.*;

/**
 *
 * @author akivela
 */
public class OccurrenceNode extends AbstractNode {
    public static int MAX_LABEL_LEN = 60;
    
    private Topic carrier;
    private Topic type;
    private Topic scope;
    private String occurrence;
    private TopicMapModel model;
    private Collection<Edge> edges;
    private String label;

    
    /** Creates a new instance of OccurrenceNode */
    public OccurrenceNode(Topic carrier, Topic type, Topic scope, String occurrence, TopicMapModel model) {
        this.occurrence = occurrence;
        this.carrier = carrier;
        this.type = type;
        this.scope = scope;
        this.model = model;
        if(occurrence != null && occurrence.length() > MAX_LABEL_LEN) {
            this.label = occurrence.substring(0, MAX_LABEL_LEN)+"...";
        }
        else {
            this.label = occurrence;
        }
    }

    
    
    public Topic getCarrier() {
        return carrier;
    }
    public Topic getType() {
        return type;
    }
    public Topic getScope() {
        return scope;
    }
    public String getOccurrence() {
        return occurrence;
    }

    
    

    @Override
    public double getMass() {
        return 25.0;
    }
    
    

    @Override
    public String getLabel() {
        return label;
    }
    

    @Override
    public Collection<Edge> getEdges() {
        if(edges == null) {
            edges = new ArrayList();
            edges.add(model.getOccurrenceEdgeFor(carrier, type, scope, occurrence));
        }
        return edges;
    }
    
    
    @Override
    public String toString(){
        return getLabel();
    }
}
