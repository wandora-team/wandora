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
 * VariantPanel.java
 *
 * Created on 2008-11-14
 *
 */


package org.wandora.application.gui.topicpanels.elements;

import org.wandora.application.gui.*;
import org.wandora.application.*;
import org.wandora.topicmap.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;


/**
 *
 * @author akivela
 */
public class VariantPanel extends JPanel {

    private Topic topic = null;
    private Wandora admin = null;
    protected HashSet<Locator> visibleTopics;
    
    
    public VariantPanel(Wandora admin, Topic t) {
        this.topic = t;
        this.admin = admin;
    }
    
    
    public void setTopic(Topic t) {
        this.topic = t;
    }
    
    
    
    public Topic getTopic() {
        return topic;
    }
    
    
    
    
    public void initialize() {
        
    }

}
